// DlgListAdapter.kt
package com.whomade.kycarrots.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.whomade.kycarrots.R

/**
 * list dialog adapter
 */
class DlgListAdapter(
    context: Context,
    private val arrStrList: ArrayList<String>
) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = arrStrList.size

    override fun getItem(position: Int): Any = arrStrList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val v: View
        val vh: ViewHolder

        if (convertView == null) {
            v = inflater.inflate(R.layout.dlg_list_item, parent, false)
            vh = ViewHolder(v.findViewById(R.id.txt_item))
            v.tag = vh
        } else {
            v = convertView
            vh = v.tag as ViewHolder
        }

        vh.txtMsg.text = arrStrList[position]
        return v
    }

    private data class ViewHolder(val txtMsg: TextView)
}
