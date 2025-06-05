package com.whomade.kycarrots.chatting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R

class ChatAdapter(private val messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ME = 1
        private const val VIEW_TYPE_OTHER = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isMe) VIEW_TYPE_ME else VIEW_TYPE_OTHER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_ME) {
            val view = inflater.inflate(R.layout.item_chat_right, parent, false)
            RightViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_left, parent, false)
            LeftViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is RightViewHolder -> holder.bind(message)
            is LeftViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    class LeftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(msg: ChatMessage) {
            itemView.findViewById<TextView>(R.id.message_text).text = msg.message
        }
    }

    class RightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(msg: ChatMessage) {
            itemView.findViewById<TextView>(R.id.message_text).text = msg.message
        }
    }
}
