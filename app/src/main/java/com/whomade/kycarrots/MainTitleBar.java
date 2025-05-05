package com.whomade.kycarrots;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 최상단 title bar
 */
public class MainTitleBar extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater inflater;

    public MainTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        setLayout();
    }

    /**
     * Title bar layout 구성
     */
    private void setLayout() {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.main_title_bar, this, true);

        ((ImageButton) findViewById(R.id.ib_home)).setOnClickListener(this);
        ((ImageView) findViewById(R.id.iv_logo)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
   }
}
