package com.whomade.kycarrots.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.whomade.kycarrots.R;
import com.whomade.kycarrots.ui.common.ImageLoader;


import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 이미지 확대/축소 보기
 */
public class DlgImgZoom extends Activity implements View.OnTouchListener{
    private PhotoViewAttacher mAttacher;
    private String strPath;
    private Button btnOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dlg_zoom_activity);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        Intent intent = getIntent();
        if(intent!=null){
            strPath = intent.getStringExtra("Path");
        }

        ((LinearLayout)findViewById(R.id.ll_list_dig_ok)).setOnTouchListener(this);
        btnOk = (Button)findViewById(R.id.btn_list_dig_ok);
        btnOk.setOnTouchListener(this);

        ImageView ivImg = (ImageView) findViewById(R.id.iv_img);
        mAttacher = new PhotoViewAttacher(ivImg);
        mAttacher.update();

        if(!strPath.equals("")) {
            ImageLoader.loadImage(this, strPath, ivImg, null);

        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_list_dig_ok || v.getId() == R.id.btn_list_dig_ok) {
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                btnOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if (event.getAction() == MotionEvent.ACTION_UP) {
                finish();
            }
            return true;
        }

        return false;
    }
}
