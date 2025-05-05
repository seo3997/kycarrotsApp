package com.whomade.kycarrots.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.whomade.kycarrots.R;

import java.util.ArrayList;

/**
 * Radio List Adapter
 */
public class RadioListAdapter  extends BaseAdapter{
    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<String> arrStrList = new ArrayList<String>();
    private int selectedIndex;

    private ViewHolder vh = null;

    public RadioListAdapter(Context context, ArrayList<String> arrStrList){
        mContext = context;
        this.arrStrList = arrStrList;

        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrStrList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrStrList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v==null){
            vh = new ViewHolder();
            v = mInflater.inflate(R.layout.dlg_radio_list_item, null);
            vh.txtMsg = (TextView) v.findViewById(R.id.txt_msg);
            vh.btnRadio = (RadioButton) v.findViewById(R.id.radio);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtMsg.setText(arrStrList.get(position));

        if (selectedIndex == position) {
            vh.btnRadio.setChecked(true);
        } else {
            vh.btnRadio.setChecked(false);
        }

        return v;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public class ViewHolder{
        private TextView txtMsg;
        private RadioButton btnRadio;
    }
}
