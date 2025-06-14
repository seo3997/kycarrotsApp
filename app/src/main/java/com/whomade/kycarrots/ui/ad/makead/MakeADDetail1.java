package com.whomade.kycarrots.ui.ad.makead;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whomade.kycarrots.R;
import com.whomade.kycarrots.ui.common.CommCodeAdapter;
import com.whomade.kycarrots.ui.common.TxtListDataInfo;
import com.whomade.kycarrots.ui.dialog.DlgBtnActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 광고제작 - 1. 세부정보
 */
public class MakeADDetail1 extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater inflater;

    private OnGetInfoData mGetInfoData;

    private AutoCompleteTextView categoryMid;

    private EditText etADName;
    private EditText etADDetail; //상세설명
    private EditText etADAmount; //광고 할 금액

    private ArrayList<TxtListDataInfo> arrData1; //1차 분류 data

    public final static String STR_PUT_AD_IDX = "AD_IDX";
    public final static String STR_PUT_AD_NAME = "AD_NAME";
    public final static String STR_PUT_AD_DETAIL = "AD_DETAIL";
    public final static String STR_PUT_AD_CATEGORY = "AD_CATEGORY";
    public final static String STR_PUT_AD_AMOUNT = "AD_AMOUNT";

    String categoryMidCd="";
    String categoryMidNm="";

    // 이벤트 인터페이스를 정의
    public interface OnGetInfoData {
        public void onGetInfoData(ArrayList<String> arrData, String arrCategory);
    }

    public void getOnInfoData(OnGetInfoData getInfoData) {
        mGetInfoData = getInfoData;
    }

    public MakeADDetail1(Context context) {
        super(context);
        mContext = context;
        Init();
    }

    /**
     * 생성자
     * @param context
     * @param attrs
     */
    public MakeADDetail1(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        Init();
    }

    /**
     * layout 구성
     */
    private void Init(){
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.advertiser_make_ad_detail, this, true);
        etADName = (EditText) findViewById(R.id.et_input_ad_name);          //상품명
        etADDetail = (EditText) findViewById(R.id.et_input_ad_detail);      //상세설명
        categoryMid = ((AutoCompleteTextView) findViewById(R.id.dropdown_category));       //카테고리
        etADAmount = (EditText) findViewById(R.id.et_input_ad_amount);      //상품가격

        ((Button) findViewById(R.id.btn_make_ad_detail_next)).setOnClickListener(mNextInfo);

    }

    @Override
    public void onClick(View v) {
    }

    //다음 버튼 클릭
    public OnClickListener mNextInfo = new OnClickListener() {
        @Override
        public void onClick(View v) {
            SetInfoData();
        }
    };
    public void SetInfoData() {
        String strMsg="";
        String strInputAmount = etADAmount.getText().toString();

        Intent intent = new Intent(mContext, DlgBtnActivity.class);
        if (etADName.getText().toString().equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_name_err);
        } else if (etADDetail.getText().toString().equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_detail_err);
        } else if (categoryMidCd.equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_category_err);
        }

        if (strInputAmount.trim().equals("") || strInputAmount.equals("0")) {
            strMsg = String.format(getResources().getString(R.string.str_make_ad_amount_reinput), strInputAmount);
        }

        if (!strMsg.equals("")) {
            intent.putExtra("BtnDlgMsg", strMsg);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        } else {
            ArrayList<String> arrData = new ArrayList<String>(); //카테고리를 제외한 data
            arrData.add(0, etADName.getText().toString());
            arrData.add(1, etADDetail.getText().toString());
            arrData.add(2, categoryMidCd);
            //arrData.add(3, etADAmount.getText().toString().replaceAll(",", ""));
            arrData.add(3, etADAmount.getText().toString().replaceAll("[,\\\\]", ""));
            mGetInfoData.onGetInfoData(arrData,categoryMidCd);
        }

    }

    public void setCategoryList(List<TxtListDataInfo> codeList) {
        arrData1 = new ArrayList<>(codeList);
        bindCategoryDropdown();
    }



    public void modifyData(ModifyADInfo data){

        if (data == null) return;

        // 광고명
        if (etADName != null) {
            etADName.setText(data.getTitle());
        }

        // 상세설명
        if (etADDetail != null) {
            etADDetail.setText(data.getDescription());
        }

        // 카테고리
        if (categoryMid != null) {
            categoryMid.setText(data.getCategoryMid());
        }
        categoryMidCd =data.getCategoryMid();

        if (categoryMid != null && arrData1 != null) {
            String selectedCategoryMid = data.getCategoryMid();
            for (TxtListDataInfo item : arrData1) {
                if (item.getStrIdx().equals(categoryMidCd)) {
                    categoryMid.setText(item.getStrMsg()); // 이제 오류 없음
                    break;
                }
            }
        }

        // 광고 금액
        if (etADAmount != null && data.getPrice() != null) {
            try {
                double priceDouble = Double.parseDouble(data.getPrice());
                long priceLong = (long) priceDouble;
                etADAmount.setText(String.valueOf(priceLong));
            } catch (NumberFormatException e) {
                etADAmount.setText("0"); // 예외 시 기본값
            }
        }
    }

    public void bindCategoryDropdown() {
        if (arrData1 != null && arrData1.size() > 0) {
            List<String> categoryNames = new ArrayList<>();
            for (TxtListDataInfo item : arrData1) {
                categoryNames.add(item.getStrMsg());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_txt_item, categoryNames);
            categoryMid.setAdapter(adapter);

            categoryMid.setOnClickListener(v -> categoryMid.showDropDown());

            categoryMid.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) categoryMid.showDropDown();
            });

            categoryMid.setOnItemClickListener((parent, view, position, id) -> {
                TxtListDataInfo selected = arrData1.get(position);
                categoryMidNm = selected.getStrMsg();
                categoryMidCd = selected.getStrIdx();
            });
        }
    }
}
