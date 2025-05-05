package com.whomade.kycarrots.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.whomade.kycarrots.R;

/**
 * two button Dialog (공통이므로 class로 구현함)
 */
public class DlgBtnActivity extends Activity implements View.OnTouchListener {
    private boolean isCallMode = false;
    private String sViewUrl = "";

    private Button btn1; //오른쪽
    private Button btn2; //왼쪽

    public static final String DIALOG_MODE_TWO = "Two";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dlg_btn_layout);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        Intent intent = getIntent();

        String sTitle = intent.getStringExtra("DlgTitle");
        String sMode = intent.getStringExtra("DlgMode");
        String sMsg = intent.getStringExtra("BtnDlgMsg");
        String sBtn1 = intent.getStringExtra("BtnDlgOneText");
        String sBtn2 = intent.getStringExtra("BtnDlgCancelText");

        isCallMode = intent.getBooleanExtra("CallMode", false);
        if(isCallMode){
            sViewUrl = intent.getStringExtra("ViewUrl");
        }

        TextView txtTitle = (TextView) findViewById(R.id.txt_dlg_title);
        if(sTitle!=null && !sTitle.equals("")) txtTitle.setText(sTitle);
        TextView txtMsg = (TextView) findViewById(R.id.txt_dlg_msg);
        txtMsg.setText(sMsg);

        LinearLayout ll1 = (LinearLayout) findViewById(R.id.ll1); //오른쪽 버튼
        btn1 = (Button) findViewById(R.id.btn1); //오른쪽 버튼
        TextView txt1 = (TextView) findViewById(R.id.txt1); //오른쪽 버튼
        ll1.setOnTouchListener(this);
        btn1.setOnTouchListener(this);
        if(sBtn1!=null && !sBtn1.equals("")) txt1.setText(sBtn1);

        if (sMode != null && !sMode.equals("") && sMode.equals("Two")) {
            LinearLayout llBtnDivider = (LinearLayout) findViewById(R.id.ll_btn_divider);
            LinearLayout ll2 = (LinearLayout) findViewById(R.id.ll2); //왼쪽 버튼
            btn2 = (Button) findViewById(R.id.btn2); //왼쪽 버튼
            TextView txt2 = (TextView) findViewById(R.id.txt2); //왼쪽 버튼

            if(sBtn2!=null && !sBtn2.equals("")) txt2.setText(sBtn2);
            ll2.setOnTouchListener(this);

            llBtnDivider.setVisibility(View.VISIBLE);
            ll2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll1 || v.getId() == R.id.btn1){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btn1.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                if(isCallMode && !sViewUrl.equals("")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sViewUrl));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else {
                    setResult(RESULT_OK);
                }
                finish();
            }
            return true;
        }else if(v.getId() == R.id.ll2){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btn2.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) finish();
            return true;
        }

        return false;
    }
}
