package com.whomade.kycarrots.ui.ad.makead;

import android.app.DatePickerDialog;
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

import com.google.android.material.textfield.TextInputEditText;
import com.whomade.kycarrots.R;
import com.whomade.kycarrots.ui.common.CommCodeAdapter;
import com.whomade.kycarrots.ui.common.TxtListDataInfo;
import com.whomade.kycarrots.ui.dialog.DlgBtnActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 광고제작 - 1. 세부정보
 */
public class MakeADDetail1 extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater inflater;

    private OnGetInfoData mGetInfoData;

    private AutoCompleteTextView categoryMid;

    private  TextInputEditText etDesiredShippingDate;

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

    private AutoCompleteTextView dropdownSubCategory;
    private ArrayList<TxtListDataInfo> arrSubCategory = new ArrayList<>();
    private String subCategoryCd = "";
    private String subCategoryNm = "";
    Map<String, String> unitMap = new HashMap<>();

    private AutoCompleteTextView dropdownCity;
    private ArrayList<TxtListDataInfo> arrCityList;

    private AutoCompleteTextView dropdownDistrict; // 시/구 드롭다운
    private ArrayList<TxtListDataInfo> arrDistrictList  = new ArrayList<>();
    private String districtCd = "";
    private String districtNm = "";

    // 이벤트 인터페이스를 정의
    public interface OnGetInfoData {
        public void onGetInfoData(ArrayList<String> arrData, String arrCategory);
    }
    public interface OnCategorySelectedListener {
        void onCategorySelected(String code);
    }

    public void getOnInfoData(OnGetInfoData getInfoData) {
        mGetInfoData = getInfoData;
    }

    private OnCategorySelectedListener categorySelectedListener;

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.categorySelectedListener = listener;
    }

    public interface OnCitySelectedListener {
        void onCitySelected(String cityCode);
    }

    private OnCitySelectedListener citySelectedListener;

    public void setOnCitySelectedListener(OnCitySelectedListener listener) {
        this.citySelectedListener = listener;
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
        dropdownSubCategory = ((AutoCompleteTextView) findViewById(R.id.dropdown_subcategory));
        dropdownCity = ((AutoCompleteTextView) findViewById(R.id.dropdown_city));
        dropdownDistrict = ((AutoCompleteTextView) findViewById(R.id.dropdown_district));

        etADAmount = (EditText) findViewById(R.id.et_input_ad_amount);      //상품가격
        etDesiredShippingDate = (TextInputEditText) findViewById(R.id.et_desired_shipping_date);      //상품가격

        unitMap.put("Kg", "1");
        unitMap.put("박스", "2");

        List<String> unitOptions = new ArrayList<>(unitMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_dropdown_unit, // 커스텀 레이아웃 사용
                new ArrayList<>(unitMap.keySet())
        );



        AutoCompleteTextView dropdownUnit = (AutoCompleteTextView) findViewById(R.id.dropdown_unit); // 또는 binding.dropdownUnit
        dropdownUnit.setAdapter(adapter);

        etDesiredShippingDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedDate = sdf.format(selectedDate.getTime());

                        etDesiredShippingDate.setText(formattedDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });
        /*
        //String selectedText = dropdownUnit.getText().toString();
        //String selectedCode = unitMap.containsKey(selectedText) ? unitMap.get(selectedText) : "";

        Map<String, String> reverseMap = new HashMap<>();
        for (Map.Entry<String, String> entry : unitMap.entrySet()) {
            reverseMap.put(entry.getValue(), entry.getKey());
        }

        // 코드가 "1"일 경우 "kg"로 표시
        String displayText = reverseMap.get("1");
        dropdownUnit.setText(displayText, false);
         */
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

    public void setCityList(List<TxtListDataInfo> codeList) {
        arrCityList = new ArrayList<>(codeList);
        bindCityDropdown();
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


                // 세부 항목 초기화
                arrSubCategory.clear(); // 세부 항목 리스트 초기화
                dropdownSubCategory.setText(""); // UI에서 선택된 값 초기화
                dropdownSubCategory.setAdapter(null); // 어댑터 제거 (선택 사항)
                subCategoryCd = "";
                subCategoryNm = "";
                // 리스너에 선택된 카테고리 코드 전달
                if (categorySelectedListener != null) {
                    categorySelectedListener.onCategorySelected(categoryMidCd);
                }
            });
        }
    }

    public void setSubCategoryList(List<TxtListDataInfo> subCodeList) {
        arrSubCategory = new ArrayList<>(subCodeList);
        bindSubCategoryDropdown();
    }
    private void bindSubCategoryDropdown() {
        if (arrSubCategory == null || arrSubCategory.isEmpty()) return;

        List<String> subNames = new ArrayList<>();
        for (TxtListDataInfo item : arrSubCategory) {
            subNames.add(item.getStrMsg());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_txt_item, subNames);
        dropdownSubCategory.setAdapter(adapter);

        dropdownSubCategory.setOnClickListener(v -> dropdownSubCategory.showDropDown());
        dropdownSubCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) dropdownSubCategory.showDropDown();
        });

        dropdownSubCategory.setOnItemClickListener((parent, view, position, id) -> {
            TxtListDataInfo selected = arrSubCategory.get(position);
            subCategoryCd = selected.getStrIdx();
            subCategoryNm = selected.getStrMsg();
        });
    }

    private void bindCityDropdown() {
        dropdownCity = findViewById(R.id.dropdown_city);

        if (arrCityList != null && arrCityList.size() > 0) {
            List<String> cityNames = new ArrayList<>();
            for (TxtListDataInfo item : arrCityList) {
                cityNames.add(item.getStrMsg());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_txt_item, cityNames);
            dropdownCity.setAdapter(adapter);

            dropdownCity.setOnClickListener(v -> dropdownCity.showDropDown());

            dropdownCity.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) dropdownCity.showDropDown();
            });

            dropdownCity.setOnItemClickListener((parent, view, position, id) -> {
                TxtListDataInfo selected = arrCityList.get(position);
                String cityCode = selected.getStrIdx();
                String cityName = selected.getStrMsg();

                // 세부 항목 초기화 (예: arrDistrictList, dropdownDistrict 등)
                arrDistrictList.clear(); // 구/군 리스트 초기화
                dropdownDistrict.setText(""); // 선택 항목 초기화
                dropdownDistrict.setAdapter(null); // 어댑터 제거 (필요 시)
                districtCd = "";
                districtNm = "";

                if (citySelectedListener != null) {
                    citySelectedListener.onCitySelected(cityCode);
                }

                // 필요 시 로그 또는 리스너 호출
                // Log.d("CitySelected", cityName + " (" + cityCode + ")");
            });
        }
    }
    public void setDistrictList(List<TxtListDataInfo> districtList) {
        arrDistrictList = new ArrayList<>(districtList);
        bindDistrictDropdown();
    }

    private void bindDistrictDropdown() {
        if (arrDistrictList == null || arrDistrictList.isEmpty()) return;

        List<String> districtNames = new ArrayList<>();
        for (TxtListDataInfo item : arrDistrictList) {
            districtNames.add(item.getStrMsg());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_txt_item, districtNames);
        dropdownDistrict.setAdapter(adapter);

        dropdownDistrict.setOnClickListener(v -> dropdownDistrict.showDropDown());
        dropdownDistrict.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) dropdownDistrict.showDropDown();
        });

        dropdownDistrict.setOnItemClickListener((parent, view, position, id) -> {
            TxtListDataInfo selected = arrDistrictList.get(position);
            districtCd = selected.getStrIdx();
            districtNm = selected.getStrMsg();
        });
    }

}
