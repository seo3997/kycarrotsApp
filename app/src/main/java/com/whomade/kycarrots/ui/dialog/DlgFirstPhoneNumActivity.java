package com.whomade.kycarrots.ui.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.whomade.kycarrots.R;
/**
 * 휴대폰 번호 첫번호 선택 Dialog
 */
public class DlgFirstPhoneNumActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes((WindowManager.LayoutParams) params);
        setContentView(R.layout.dlg_txt_list);

        ((TextView) findViewById(R.id.txt_dlg_title)).setText(getResources().getString(R.string.str_phone_number));

        final Intent intent = new Intent();
        final String[] strArrFirstPhoneNum= getResources().getStringArray(R.array.first_phone_num);

        final ListView lvTxt = (ListView) findViewById(R.id.lv_txt);
        ArrayAdapter<String> adapterGroup = new ArrayAdapter<String>(this, R.layout.list_txt_item, strArrFirstPhoneNum);
        lvTxt.setAdapter(adapterGroup);

        lvTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent.putExtra("FirstPhoneNum", strArrFirstPhoneNum[position]);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
