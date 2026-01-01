package com.whomade.kycarrots.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioButton
import android.widget.TextView
import com.whomade.kycarrots.R

class RadioListAdapter(
    private val context: Context,
    private val arrStrList: ArrayList<String>
) : BaseAdapter() {

    private var selectedIndex: Int = -1
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = arrStrList.size

    override fun getItem(position: Int): Any = arrStrList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.dlg_radio_list_item, parent, false)
            viewHolder = ViewHolder(
                txtMsg = view.findViewById(R.id.txt_msg),
                btnRadio = view.findViewById(R.id.radio)
            )
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        viewHolder.txtMsg.text = arrStrList[position]
        viewHolder.btnRadio.isChecked = (position == selectedIndex)

        return view
    }

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
    }

    private data class ViewHolder(
        val txtMsg: TextView,
        val btnRadio: RadioButton
    )
}