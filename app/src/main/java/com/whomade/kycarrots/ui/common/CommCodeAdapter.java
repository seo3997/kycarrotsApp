package com.whomade.kycarrots.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.whomade.kycarrots.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 공통 코드 adapter
 */
public class CommCodeAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ViewHolder vh = null;
    private List<TxtListDataInfo> arrData = new ArrayList<>();
    private int mLayout;

    public CommCodeAdapter(Context context, int layout, List<TxtListDataInfo> arrData){
        mContext = context;
        this.arrData.addAll(arrData);
        mLayout = layout;

        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrData.size();
    }

    @Override
    public Object getItem(int position) {
        return arrData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = null;

        if(v==null){
            vh = new ViewHolder();
            v = mInflater.inflate(mLayout, null);

            vh.txtItem = (TextView) v.findViewById(R.id.txt_item);
            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtItem.setText(arrData.get(position).getStrMsg());
        return v;
    }

    class ViewHolder{
        public TextView txtItem;
    }
}
