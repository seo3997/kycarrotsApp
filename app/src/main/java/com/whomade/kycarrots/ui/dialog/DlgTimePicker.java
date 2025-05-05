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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.whomade.kycarrots.R;

/**
 * 시간 팝업
 */
public class DlgTimePicker extends Activity implements View.OnClickListener, View.OnTouchListener {
    private int mHour;
    private int mMin;

    private EditText etHour;
    private EditText etMin;

    private String strHour;
    private String strMin;

    private Button btnDateOk;
    private Button btnDateCancel;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dlg_timepicker);


        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        intent = new Intent(this.getIntent());
        mHour = intent.getIntExtra("Hour", 0);
        mMin = intent.getIntExtra("Min", 0);

        etHour = (EditText) findViewById(R.id.et_hour);
        etMin = (EditText) findViewById(R.id.et_min);

        strHour = String.valueOf(mHour);
        if (strHour.length() < 2) {
            strHour = "0" + strHour;
        }
        etHour.setText(strHour);

        strMin = String.valueOf(mMin);
        if (strMin.length() < 2) {
            strMin = "0" + strMin;
        }
        etMin.setText(strMin);

        ((ImageButton) findViewById(R.id.ib_add_hour)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ib_add_min)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ib_minus_hour)).setOnClickListener(this);
        ((ImageButton) findViewById(R.id.ib_minus_min)).setOnClickListener(this);

        LinearLayout llDateOk = (LinearLayout) findViewById(R.id.ll_date_ok);
        btnDateOk = (Button) findViewById(R.id.btn_date_ok);
        llDateOk.setOnTouchListener(this);
        btnDateOk.setOnTouchListener(this);

        LinearLayout llDateCancel = (LinearLayout) findViewById(R.id.ll_date_cancel);
        btnDateCancel = (Button) findViewById(R.id.btn_date_cancel);
        llDateCancel.setOnTouchListener(this);
        btnDateCancel.setOnTouchListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.ib_add_hour) {
            ChangeHour(true);
        } else if (viewId == R.id.ib_add_min) {
            ChangeMin(true);
        } else if (viewId == R.id.ib_minus_hour) {
            ChangeHour(false);
        } else if (viewId == R.id.ib_minus_min) {
            ChangeMin(false);
        }
    }

    private void ChangeHour(boolean hour_mode) {
        etHour.requestFocus();
        etHour.setSelection(etHour.getText().length());


        if(hour_mode){
            mHour++;

            if(mHour > 23){
                mHour = 0;
            }
        }else{
            mHour--;

            if(mHour < 0){
                mHour = 23;
            }
        }

        strHour = String.valueOf(mHour);
        if (strHour.length() < 2) {
            strHour = "0" + strHour;
        }
        etHour.setText(strHour);
    }

    private void ChangeMin(boolean min_mode) {
        etMin.requestFocus();
        etMin.setSelection(etMin.getText().length());

        if(min_mode){
            mMin++;

            if(mMin > 59){
                mMin = 0;
            }
        }else{
            mMin--;

            if(mMin < 0){
                mMin = 59;
            }
        }

        strMin = String.valueOf(mMin);
        if (strMin.length() < 2) {
            strMin = "0" + strMin;
        }
        etMin.setText(strMin);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_date_ok || v.getId() == R.id.btn_date_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnDateOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                intent.putExtra("Hour", Integer.parseInt(etHour.getText().toString()));
                intent.putExtra("Min", Integer.parseInt(etMin.getText().toString()));
                setResult(Activity.RESULT_OK, intent);

                finish();
            }
            return true;
        }else if(v.getId() == R.id.ll_date_cancel || v.getId() == R.id.btn_date_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnDateCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) finish();
            return true;
        }

        return false;
    }
}
