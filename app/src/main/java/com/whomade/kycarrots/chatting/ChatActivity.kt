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

class ChatActivity : AppCompatActivity() {

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter

    private val chatMessages = mutableListOf<ChatMessage>()

    private lateinit var roomId: String
    private lateinit var buyerId: String
    private lateinit var sellerId: String
    private lateinit var productId: String
    private lateinit var currentUserId: String
    private lateinit var senderId: String

    private var isBuyer: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 툴바 설정
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back)
            title = "채팅"
        }

        // View 바인딩
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        // RecyclerView 초기화
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter
        // 인텐트로 받은 데이터
        roomId = intent.getStringExtra("roomId") ?: return finishWithError("roomId 누락")
        buyerId = intent.getStringExtra("buyerId") ?: return finishWithError("buyerId 누락")
        sellerId = intent.getStringExtra("sellerId") ?: return finishWithError("sellerId 누락")
        productId = intent.getStringExtra("productId") ?: return finishWithError("productId 누락")

        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        val sUID = prefs.getString("LogIn_ID", "") ?: ""
        val sUserType = prefs.getString("LogIn_USERTYPE", "") ?: ""

        // 로그인 사용자 ID 기준으로 발신자 판단
        currentUserId = sUID
        isBuyer = sUserType == "2"
        senderId = sUID

        // WebSocket 연결
        StompManager.connect()

        // 채팅 메시지 초기 로딩
        loadChatMessages(roomId)

        // 메시지 전송 처리
        sendButton.setOnClickListener {
            val text = messageEditText.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = ChatMessage(
                    senderId = senderId,
                    message = text,
                    roomId = roomId,
                    type = "text",
                    isMe = true
                )
                chatMessages.add(message)
                chatAdapter.notifyItemInserted(chatMessages.size - 1)
                chatRecyclerView.scrollToPosition(chatMessages.size - 1)
                messageEditText.text.clear()
                StompManager.sendMessageRoomId(message)
            }
        }

        // 메시지 수신 구독
        StompManager.subscribe("/topic/$roomId") { received ->
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

    private fun finishWithError(message: String): Nothing {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
        throw IllegalArgumentException(message)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
}
