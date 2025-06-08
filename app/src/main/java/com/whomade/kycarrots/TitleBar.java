package com.whomade.kycarrots;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Title bar layout
 */
public class TitleBar extends LinearLayout {
    private Context mContext;
    private LayoutInflater inflater;
    private ImageButton btnTopBack;
    private ImageButton ibMenu;

    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setLayout();
    }

    /**
     * Title bar layout 구성
     */
    private void setLayout() {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.title_bar, this, true);

        LinearLayout llBack = (LinearLayout) findViewById(R.id.ll_back);
        btnTopBack = (ImageButton) findViewById(R.id.btn_back);
        llBack.setOnClickListener(mClick);
        btnTopBack.setOnClickListener(mClick);
        ((TextView) findViewById(R.id.txt_back)).setOnClickListener(mClick);

        ibMenu = (ImageButton) findViewById(R.id.ib_menu);
    }

    public OnClickListener mClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            ((Activity) mContext).onBackPressed();
        }
    };

    public void setTitle(String sTitle){
        ((TextView) findViewById(R.id.txt_page_title)).setText(sTitle);
    }
}
