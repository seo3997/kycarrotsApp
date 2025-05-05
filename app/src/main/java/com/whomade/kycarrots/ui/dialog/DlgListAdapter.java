package com.whomade.kycarrots.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.whomade.kycarrots.R;

import java.util.ArrayList;

/**
 * list dialog adpter
 */
public class DlgListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<String> arrStrList = new ArrayList<String>();
    private ViewHolder vh = null;

    public DlgListAdapter (Context context, ArrayList<String> arrStr){

        mContext = context;
        this.arrStrList = arrStr;

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
            v = mInflater.inflate(R.layout.dlg_list_item, null);
            vh.txtMsg = (TextView) v.findViewById(R.id.txt_item);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtMsg.setText(arrStrList.get(position));
        return v;
    }

    public class ViewHolder{
        private TextView txtMsg;
    }
}
