// ui/notification/NotificationListActivity.kt
package com.whomade.kycarrots.ui.Noti

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.whomade.kycarrots.AdDetailActivity
import com.whomade.kycarrots.BaseDrawerActivity
import com.whomade.kycarrots.R
import com.whomade.kycarrots.chatting.ChatActivity
import com.whomade.kycarrots.databinding.ActivityNotificationListBinding
import com.whomade.kycarrots.data.local.PushNotificationEntity
import com.whomade.kycarrots.data.local.PushRepositoryProvider
import kotlinx.coroutines.launch

class NotificationListActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityNotificationListBinding
    private lateinit var adapter: NotificationListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 표시
            title = "알림 리스트"
        }


        adapter = NotificationListAdapter(
            onClick = { item -> handleClick(item) },
            onDelete = { item -> deleteOne(item) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 진입 시: 모두 읽음 처리 + 목록 로드
        lifecycleScope.launch {
            val userId = getUserId() ?: return@launch
            val repo = PushRepositoryProvider.get(this@NotificationListActivity)
            repo.markAllRead(userId)
            loadList()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadList()
    }

    private fun loadList() {
        lifecycleScope.launch {
            val userId = getUserId() ?: return@launch
            val repo = PushRepositoryProvider.get(this@NotificationListActivity)
            val items = repo.list(userId, onlyUnread = false, limit = 100, offset = 0)

            adapter.submitList(items) {
                val hasItems = items.isNotEmpty()
                binding.emptyView.visibility = if (hasItems) View.GONE else View.VISIBLE
                binding.recyclerView.visibility = if (hasItems) View.VISIBLE else View.INVISIBLE
            }

            binding.emptyView.isVisible = if (items.isEmpty()) true else false
        }
    }

    private fun handleClick(item: PushNotificationEntity) {
        // 개별 읽음 처리
        lifecycleScope.launch {
            PushRepositoryProvider.get(this@NotificationListActivity).markRead(item.id)
        }

        // 라우팅
        when (item.type) {
            "CHAT" -> {
                val parts = item.roomId?.split("_") ?: emptyList()
                if (parts.size >= 3) {
                    val productId = parts[0]
                    val buyerId = parts[1]
                    val sellerId = parts[2]
                    openChatActivity(
                        roomId = item.roomId ?: return,
                        buyerId = buyerId,
                        sellerId = sellerId,
                        productId = productId
                    )
                }
            }
            "PRODUCT_REGISTERED", "PRODUCT_APPROVED", "PRODUCT_REJECTED", "PRODUCT" -> {
                val productId = item.productId?.toString() ?: ""


                val intent = Intent(this@NotificationListActivity, AdDetailActivity::class.java).apply {
                    putExtra(AdDetailActivity.EXTRA_PRODUCT_ID, productId)
                    putExtra(AdDetailActivity.EXTRA_USER_ID,    item.sellerId)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)

            }
            else -> {
                // 딥링크가 있으면 우선
                item.deeplink?.let {
                    runCatching {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                    }
                }
            }
        }
    }

    private fun deleteOne(item: PushNotificationEntity) {
        lifecycleScope.launch {
            val repo = PushRepositoryProvider.get(this@NotificationListActivity)
            repo.delete(item.id)
            loadList()
        }
    }

    private fun markAllRead() {
        lifecycleScope.launch {
            val userId = getUserId() ?: return@launch
            PushRepositoryProvider.get(this@NotificationListActivity).markAllRead(userId)
            loadList()
        }
    }

    private fun getUserId(): String? {
        val prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE)
        return prefs.getString("LogIn_ID", "")?.takeIf { it.isNotBlank() }
    }

    private fun openChatActivity(
        roomId: String,
        buyerId: String,
        sellerId: String,
        productId: String
    ) {
        val intent = Intent(this@NotificationListActivity, ChatActivity::class.java).apply {
            putExtra("roomId", roomId)
            putExtra("buyerId", buyerId)
            putExtra("sellerId", sellerId)
            putExtra("productId", productId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }



}
