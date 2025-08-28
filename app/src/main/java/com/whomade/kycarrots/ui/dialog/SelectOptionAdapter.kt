package com.whomade.kycarrots.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.whomade.kycarrots.R

class SelectOptionAdapter(
    private val items: List<SelectOption>,
    private val onClick: (SelectOption) -> Unit
) : RecyclerView.Adapter<SelectOptionAdapter.VH>() {

    inner class VH(val v: View) : RecyclerView.ViewHolder(v) {
        private val tv = v.findViewById<TextView>(R.id.tv_label)
        fun bind(item: SelectOption, pos: Int) {
            tv.text = "${pos + 1}. ${item.name}"
            v.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dlg_text, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}