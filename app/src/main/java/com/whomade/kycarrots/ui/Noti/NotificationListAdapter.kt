// ui/notification/NotificationListAdapter.kt
package com.whomade.kycarrots.ui.Noti

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.data.local.PushNotificationEntity
import com.whomade.kycarrots.databinding.ItemNotificationBinding

class NotificationListAdapter(
    private val onClick: (PushNotificationEntity) -> Unit,
    private val onDelete: (PushNotificationEntity) -> Unit
) : ListAdapter<PushNotificationEntity, NotificationListAdapter.VH>(DIFF) {

    inner class VH(private val vb: ItemNotificationBinding) : RecyclerView.ViewHolder(vb.root) {
        fun bind(item: PushNotificationEntity) {
            vb.tvTitle.text = item.title
            vb.tvBody.text = item.body ?: ""
            vb.tvType.text = item.type
            vb.tvTime.text = android.text.format.DateFormat.format("MM-dd HH:mm", item.createdAt)

            // 읽음 여부 점 표시
            vb.unreadDot.alpha = if (item.isRead) 0.3f else 1.0f

            vb.root.setOnClickListener { onClick(item) }
            vb.btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val vb = ItemNotificationBinding.inflate(inflater, parent, false)
        return VH(vb)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PushNotificationEntity>() {
            override fun areItemsTheSame(a: PushNotificationEntity, b: PushNotificationEntity) = a.id == b.id
            override fun areContentsTheSame(a: PushNotificationEntity, b: PushNotificationEntity) = a == b
        }
    }
}
