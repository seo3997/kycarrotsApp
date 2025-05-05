package com.whomade.kycarrots.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.whomade.kycarrots.R;

import java.util.Calendar;

/**
 * 날짜 팝업
 */
public class DlgDatePicker extends Activity implements View.OnClickListener, View.OnTouchListener {
    private EditText etYear;
    private EditText etMonth;
    private EditText etDay;

    private String strYear;
    private String strMonth;
    private String strDay;

    private int startYear = 1900;
    private int endYear = 3000;

    private Intent intent;
    private Calendar cal;

    private int mYear;
    private int mMonth;
    private int mDay;

    private Button btnDateOk;
    private Button btnDateCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dlg_datepicker);

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);

        intent = new Intent(this.getIntent());
        mYear = intent.getIntExtra("Year", 0);
        mMonth = intent.getIntExtra("Month", 0);
        mDay = intent.getIntExtra("Day", 0);
        cal = Calendar.getInstance();
        cal.set(mYear, mMonth-1, mDay);

        ImageButton ibAddYear = (ImageButton) findViewById(R.id.ib_add_year);
        ImageButton ibAddMonth = (ImageButton) findViewById(R.id.ib_add_month);
        ImageButton ibAddDay = (ImageButton) findViewById(R.id.ib_add_day);

        etYear = (EditText) findViewById(R.id.et_year);
        etMonth = (EditText) findViewById(R.id.et_month);
        etDay = (EditText) findViewById(R.id.et_day);
        etYear.addTextChangedListener(date_watcher);
        etMonth.addTextChangedListener(date_watcher);
        etDay.addTextChangedListener(date_watcher);

        etYear.setText(String.valueOf(mYear));

//        if (mMonth >= 13) {
//            mMonth = 1;
//        }

//        mMonth = mMonth + 1;
        strMonth = String.valueOf(mMonth);
        if (strMonth.length() < 2) {
            strMonth = "0" + strMonth;
        }
        etMonth.setText(strMonth);
//        mMonth = mMonth + 1;

        strDay = String.valueOf(mDay);
        if (strDay.length() < 2) {
            strDay = "0" + strDay;
        }
        etDay.setText(strDay);

        ImageButton ibMinusYear = (ImageButton) findViewById(R.id.ib_minus_year);
        ImageButton ibMinusMonth = (ImageButton) findViewById(R.id.ib_minus_month);
        ImageButton ibMinusDay = (ImageButton) findViewById(R.id.ib_minus_day);

//        Button btnDateOk = (Button) findViewById(R.id.btn_date_ok);
//        Button btnDateCancel = (Button) findViewById(R.id.btn_date_cancel);

        LinearLayout llDateOk = (LinearLayout) findViewById(R.id.ll_date_ok);
        btnDateOk = (Button) findViewById(R.id.btn_date_ok);
        llDateOk.setOnTouchListener(this);
        btnDateOk.setOnTouchListener(this);

        LinearLayout llDateCancel = (LinearLayout) findViewById(R.id.ll_date_cancel);
        btnDateCancel = (Button) findViewById(R.id.btn_date_cancel);
        llDateCancel.setOnTouchListener(this);

        ibAddYear.setOnClickListener(this);
        ibAddMonth.setOnClickListener(this);
        ibAddDay.setOnClickListener(this);
        ibMinusYear.setOnClickListener(this);
        ibMinusMonth.setOnClickListener(this);
        ibMinusDay.setOnClickListener(this);
//        btnDateOk.setOnClickListener(this);
//        btnDateCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.ib_add_year) {
            ChangeYear(true);
        } else if (viewId == R.id.ib_add_month) {
            ChangeMonth(true);
        } else if (viewId == R.id.ib_add_day) {
            ChangeDay(true);
        } else if (viewId == R.id.ib_minus_year) {
            ChangeYear(false);
        } else if (viewId == R.id.ib_minus_month) {
            ChangeMonth(false);
        } else if (viewId == R.id.ib_minus_day) {
            ChangeDay(false);
        }
    }

    private void ChangeYear(boolean year_mode) {
        etYear.requestFocus();
        etYear.setSelection(etYear.getText().length());
        if (year_mode) {
            if (cal.get(Calendar.YEAR) >= endYear) {
                cal.set(Calendar.YEAR, startYear);
            } else {
                cal.add(Calendar.YEAR, 1);
            }
        } else {
            if (cal.get(Calendar.YEAR) <= startYear) {
                cal.set(Calendar.YEAR, endYear);
            } else {
                cal.add(Calendar.YEAR, -1);
            }
        }

        mYear = cal.get(Calendar.YEAR);
        strYear = String.valueOf(mYear);

        etYear.setText(strYear);
    }

    // month_mode : true-> add, false -> minuse
    private void ChangeMonth(boolean month_mode) {
        etMonth.requestFocus();
        etMonth.setSelection(etMonth.length());

        if (month_mode) {
            cal.add(Calendar.MONTH, 1);
        } else {
            cal.add(Calendar.MONTH, -1);
        }

        mMonth = cal.get(Calendar.MONTH) + 1;
//        mMonth = cal.get(Calendar.MONTH);
        if (mMonth >= 13) {
            mMonth = 1;
        }
        strMonth = String.valueOf(mMonth);
        if (strMonth.length() < 2) {
            strMonth = "0" + strMonth;
        }

        etMonth.setText(strMonth);

        int day = Integer.parseInt(etDay.getText().toString());
        if (day > cal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            etDay.setText(String.valueOf(cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
        }
    }

    // month_mode : true-> add, false -> minuse
    private void ChangeDay(boolean day_mode) {
        etDay.requestFocus();
        etDay.setSelection(etDay.length());
        if (day_mode) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        } else {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }

        mDay = cal.get(Calendar.DAY_OF_MONTH);
        strDay = String.valueOf(mDay);
        if (strDay.length() < 2) {
            strDay = "0" + strDay;
        }

        mMonth = cal.get(Calendar.MONTH) + 1;
//        mMonth = cal.get(Calendar.MONTH);
        if (mMonth >= 13) {
            mMonth = 1;
        }

        strMonth = String.valueOf(mMonth);
        if (strMonth.length() < 2) {
            strMonth = "0" + strMonth;
        }

        etMonth.setText(strMonth);
        etDay.setText(strDay);
    }

    TextWatcher date_watcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (etDay.isFocused()) {
                try {
                    if (s.toString().length() > 0) {
                        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.toString()));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (etYear.isFocused()) {
                try {
                    if (s.toString().length() == 4) {
                        int year = Integer.parseInt(s.toString());

                        if (year > endYear) {
                            cal.set(Calendar.YEAR, endYear);
                        } else if (year < startYear) {
                            cal.set(Calendar.YEAR, startYear);
                        } else {
                            cal.set(Calendar.YEAR, year);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_date_ok || v.getId() == R.id.btn_date_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnDateOk.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                intent.putExtra("Year", Integer.parseInt(etYear.getText().toString()));
                intent.putExtra("Month", Integer.parseInt(etMonth.getText().toString()) - 1);
                intent.putExtra("Day", Integer.parseInt(etDay.getText().toString()));
                setResult(Activity.RESULT_OK, intent);

                finish();
            }
            return true;
        }else if(v.getId() == R.id.ll_date_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnDateCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP) finish();
            return true;
        }

        return false;
    }
}

