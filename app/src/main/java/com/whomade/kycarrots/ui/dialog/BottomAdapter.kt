package com.whomade.kycarrots.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R

class BottomAdapter(
    private val onClick: (BottomDto) -> Unit
) : ListAdapter<BottomDto, BottomAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<BottomDto>() {
        override fun areItemsTheSame(o: BottomDto, n: BottomDto) = o.code == n.code
        override fun areContentsTheSame(o: BottomDto, n: BottomDto) = o == n
    }

    override fun onCreateViewHolder(p: ViewGroup, vtype: Int): VH {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_center, p, false)
        return VH(v, onClick)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    class VH(v: View, private val onClick: (BottomDto) -> Unit) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(R.id.tvName)
        private val sub   = v.findViewById<TextView>(R.id.tvSub)
        fun bind(item: BottomDto) {
            title.text = item.name
            sub.text   = listOfNotNull(item.text1, item.text2).joinToString(" • ")
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
