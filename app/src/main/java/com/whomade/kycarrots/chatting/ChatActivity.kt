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
import com.whomade.kycarrots.domain.service.AppServiceProvider
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
    private lateinit var sellerId: String
    private lateinit var productId: String
    private lateinit var currentUserId: String
    private lateinit var senderId: String
    private lateinit var otherId : String
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

        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter
    }

    private fun initializeChat() {
        roomId = intent.getStringExtra("roomId") ?: return finishWithError("roomId 누락")
        buyerId = intent.getStringExtra("buyerId") ?: return finishWithError("buyerId 누락")
        sellerId = intent.getStringExtra("sellerId") ?: return finishWithError("sellerId 누락")
        productId = intent.getStringExtra("productId") ?: return finishWithError("productId 누락")

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val sUID = prefs.getString("LogIn_ID", "") ?: ""
        val sMemberCode = prefs.getString("LogIn_MEMBERCODE", "") ?: ""

        otherId = resolveOtherId(sUID, buyerId, sellerId)

        currentUserId = sUID
        isBuyer = sMemberCode == "ROLE_PUB"
        senderId = sUID

        StompManager.connect(sUID)

        loadChatMessages(roomId)
    }

    private fun resolveOtherId(myId: String, buyerId: String, sellerId: String): String {
        return when (myId) {
            buyerId -> sellerId
            sellerId -> buyerId
            else -> if (myId.isNotBlank()) listOf(buyerId, sellerId).firstOrNull { it != myId } ?: sellerId
            else sellerId
        }
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            val message = ChatMessage(
                senderId = senderId,
                message = text,
                roomId = roomId,
                type = "text",
                time = currentTime,
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
                            message = it.message,
                            type = "text",
                            time = it.time,
                            isMe = it.senderId == currentUserId
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

    override fun onStop() {
        super.onStop()
        topicPath?.let { StompManager.unsubscribe(it) }
        StompManager.disconnect() // 방을 나가면 완전 오프라인 처리 원할 때
    }


}
