// ChatActivity.kt

package com.whomade.kycarrots.chatting

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.Constants
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter

    private val chatMessages = mutableListOf<ChatMessage>()
    private var topicPath: String? = null

    private lateinit var roomId: String
    private lateinit var buyerId: String
    private lateinit var branchId: String
    private lateinit var productId: String
    private lateinit var senderId: String
    private lateinit var otherId : String
    private lateinit var currentMemberCode: String
    private lateinit var currentUserId: String
    private var isBuyer: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        bindViews()
        initializeChat()
        setupSendButton()
        subscribeToMessages()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            title = "$otherId  님과의 대화"   // ← 여기!
        }
    }

    private fun bindViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        currentMemberCode = prefs.getString("LogIn_MEMBERCODE", "") ?: ""

        chatAdapter = ChatAdapter(chatMessages, currentMemberCode)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter
    }

    private fun initializeChat() {
        roomId = intent.getStringExtra("roomId") ?: return finishWithError("roomId 누락")
        buyerId = intent.getStringExtra("buyerId") ?: return finishWithError("buyerId 누락")
        branchId = intent.getStringExtra("branchId") ?: return finishWithError("branchId 누락")
        productId = intent.getStringExtra("productId") ?: return finishWithError("productId 누락")

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val sUID = prefs.getString("LogIn_ID", "") ?: ""
        val sMemberCode = prefs.getString("LogIn_MEMBERCODE", "") ?: ""

        lifecycleScope.launch {
            otherId = resolveOtherId(sUID, buyerId, branchId)
            setupToolbar()
        }

        currentUserId = sUID
        isBuyer = sMemberCode == Constants.ROLE_PUB
        senderId = sUID

        StompManager.connect(sUID)

        loadChatMessages(roomId)
    }

    private suspend fun resolveOtherId(myId: String, buyerId: String, branchId: String): String {
        val intentName = intent.getStringExtra("otherUserNm")
        if (!intentName.isNullOrEmpty()) return intentName

        val memberCode = LoginInfoUtil.getMemberCode(this)
        val branchNameFromPrefs = LoginInfoUtil.getBranchName(this)
        var sReturn = ""
        
        when (memberCode) {
            Constants.ROLE_PUB -> {
                sReturn = branchNameFromPrefs
            }
            Constants.ROLE_PROJ -> {
                sReturn = if (branchId == "2") "본사" else buyerId
            }
            Constants.ROLE_SELL -> {
                try {
                    val branchInfo = AppServiceProvider.getService().getBranchInfo(buyerId.toLong())
                    sReturn = branchInfo?.branchName ?: (buyerId + " 지점")
                } catch (e: Exception) {
                    sReturn = buyerId + " 지점"
                }
            }
        }
        return if (sReturn.isEmpty()) buyerId else sReturn
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val receiveGroup = when (currentMemberCode) {
                Constants.ROLE_PUB -> Constants.ROLE_PROJ
                Constants.ROLE_SELL -> Constants.ROLE_PROJ
                Constants.ROLE_PROJ -> {
                    if (branchId == "2") Constants.ROLE_SELL else Constants.ROLE_PUB
                }
                else -> Constants.ROLE_PROJ
            }

            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val message = ChatMessage(
                senderId = senderId,
                message = text,
                roomId = roomId,
                type = "text",
                time = currentTime,
                senderGroup = currentMemberCode,
                receiveGroup = receiveGroup,
                isMe = true
            )
            
            chatMessages.add(message)
            chatAdapter.notifyItemInserted(chatMessages.size - 1)
            chatRecyclerView.scrollToPosition(chatMessages.size - 1)
            messageEditText.text.clear()

            StompManager.sendMessageRoomId(message)
        }
    }

    private fun subscribeToMessages() {
        topicPath = "/topic/$roomId"
        StompManager.subscribe(topicPath!!) { received ->
            runOnUiThread {
                if (received.senderId != currentUserId) {
                    received.isMe = false
                    chatMessages.add(received)
                    chatAdapter.notifyItemInserted(chatMessages.size - 1)
                    chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                }
            }
        }
        StompManager.sendEnterRoom(roomId, currentUserId)
    }

    private fun loadChatMessages(roomId: String) {
        val appService = AppServiceProvider.getService()
        lifecycleScope.launch {
            try {
                val response = appService.getChatMessages(roomId)
                response?.let { messageList ->
                    val mapped = messageList.map {
                        ChatMessage(
                            roomId = it.roomId,
                            senderId = it.senderId,
                            senderGroup = it.senderGroup,
                            message = it.message,
                            type = "text",
                            time = it.time,
                            isMe = it.senderGroup == currentMemberCode
                        )
                    }
                    chatMessages.clear()
                    chatMessages.addAll(mapped)
                    chatAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "채팅 내역 로드 오류", e)
            }
        }
    }

    private fun finishWithError(message: String): Nothing {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
        throw IllegalArgumentException(message)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        topicPath?.let { StompManager.unsubscribe(it) }
        // 명시적 퇴장 신호 전송
        StompManager.sendExitRoom(roomId, currentUserId)
        StompManager.disconnect() // 방을(화면을) 완전히 나갈 때 소켓 종료
    }
}
