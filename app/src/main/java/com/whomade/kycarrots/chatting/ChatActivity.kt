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
        setupToolbar()
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

        otherId = resolveOtherId(sUID, buyerId, branchId)

        currentUserId = sUID
        isBuyer = sMemberCode == Constants.ROLE_PUB
        senderId = sUID

        StompManager.connect(sUID)

        loadChatMessages(roomId)
    }

    private fun resolveOtherId(myId: String, buyerId: String, branchId: String): String {
        var memberCode=LoginInfoUtil.getMemberCode(this)
        var branchName=LoginInfoUtil.getBranchName(this)
        var sRetrun =""
        if(memberCode==Constants.ROLE_PUB) {
            sRetrun = branchName
        } else if(memberCode==Constants.ROLE_PROJ){
            if(branchId.equals("2")) sRetrun = "본사"
            else sRetrun = buyerId
        } else if(memberCode==Constants.ROLE_SELL){
            sRetrun = buyerId+" 지점"
        }
        return sRetrun
        /*
        return when (myId) {
            buyerId -> branchId
            branchId -> buyerId
            else -> if (myId.isNotBlank()) listOf(buyerId, branchId).firstOrNull { it != myId } ?: branchId
            else branchId
        }
        */
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            // 사용자 지정 규칙 반영
            val receiveGroup = when (currentMemberCode) {
                Constants.ROLE_PUB -> Constants.ROLE_PROJ
                Constants.ROLE_SELL -> Constants.ROLE_PROJ
                Constants.ROLE_PROJ -> {
                    // 상대방이 본사('2')면 ROLE_SELL, 아니면 ROLE_PUB
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
        // 명시적 진입 신호 전송
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
