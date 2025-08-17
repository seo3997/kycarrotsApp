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


    private EditText etADName;                          //상품명
    private TextInputEditText et_quantity;              //남은수량
    private AutoCompleteTextView dropdownUnit;          //단위
    private EditText etADAmount;                        //상품금액
    private TextInputEditText etDesiredShippingDate;    //예상출하일자    
    private EditText etADDetail;                        //상세설명
    private AutoCompleteTextView categoryMid;           //카테고리
    private AutoCompleteTextView dropdownSubCategory;   //세부항목 
    private AutoCompleteTextView dropdownCity;          //도시
    private AutoCompleteTextView dropdownDistrict;      // 시/구 

    private List<TxtListDataInfo> arrUnitList;                                   //단위
    private String unitCode = "";
    private String unitCodeNm = "";

    private ArrayList<TxtListDataInfo> arrData1;                                                    //카테고리
    String categoryMidCd="";
    String categoryMidNm="";

    private ArrayList<TxtListDataInfo> arrSubCategory = new ArrayList<>();                          //세부코드
    private String categoryScls = "";
    private String categorySclsNm = "";

    private ArrayList<TxtListDataInfo> arrCityList;                                                 //도시

    private String areaMid = "";
    private String areaMidNm = "";

    private ArrayList<TxtListDataInfo> arrDistrictList  = new ArrayList<>();                        //시도
    private String areaScls = "";
    private String areaSclsNm = "";



    public final static String STR_PUT_AD_IDX = "AD_IDX";
    public final static String STR_PUT_AD_NAME = "AD_NAME";
    public final static String STR_PUT_AD_QUANTITY = "AD_QUANTITY";
    public final static String STR_PUT_AD_UNIT_CODE = "AD_UNIT_CODE";
    public final static String STR_PUT_AD_AMOUNT = "AD_AMOUNT";
    public final static String STR_PUT_AD_DESIRED_SHIPPING_DATE = "AD_DESIRED_SHIPPING_DATE";
    public final static String STR_PUT_AD_DETAIL = "AD_DETAIL";
    public final static String STR_PUT_AD_CATEGORY_MID = "AD_CATEGORY_MID";
    public final static String STR_PUT_AD_CATEGORY_SCLS = "AD_CATEGORY_SCLS";
    public final static String STR_PUT_AD_AREA_MID = "AD_AREA_MID";
    public final static String STR_PUT_AD_AREA_SCLS = "AD_AREA_SCLS";
    public final static String STR_PUT_AD_UNIT_CODEMM = "AD_UNIT_CODENM";
    public final static String STR_PUT_AD_CATEGORY_MIDNM = "AD_CATEGORY_MIDNM";
    public final static String STR_PUT_AD_CATEGORY_SCLSNM = "AD_CATEGORY_SCLSNM";
    public final static String STR_PUT_AD_AREA_MIDNM = "AD_AREA_MIDNM";
    public final static String STR_PUT_AD_AREA_SCLSNM = "AD_AREA_SCLSNM";


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
        etADName = (EditText) findViewById(R.id.et_input_ad_name);                                  //상품명
        et_quantity= (TextInputEditText) findViewById(R.id.et_quantity);               //남은수량
        dropdownUnit = (AutoCompleteTextView) findViewById(R.id.dropdown_unit);                     //단위
        etADAmount = (EditText) findViewById(R.id.et_input_ad_amount);                              //상품가격
        etDesiredShippingDate = (TextInputEditText) findViewById(R.id.et_desired_shipping_date);    //출하일자
        etADDetail = (EditText) findViewById(R.id.et_input_ad_detail);                              //상세설명
        categoryMid = ((AutoCompleteTextView) findViewById(R.id.dropdown_category));                //카테고리
        dropdownSubCategory = ((AutoCompleteTextView) findViewById(R.id.dropdown_subcategory));     //세부항목
        dropdownCity = ((AutoCompleteTextView) findViewById(R.id.dropdown_city));                   //도시
        dropdownDistrict = ((AutoCompleteTextView) findViewById(R.id.dropdown_district));           //시도

        //출하일자
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
        String strInputQuantity = et_quantity.getText().toString();

        Intent intent = new Intent(mContext, DlgBtnActivity.class);
        if (etADName.getText().toString().equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_name_err);
        } else if ( (strInputQuantity.trim().equals("") || strInputQuantity.equals("0"))) {
            strMsg = getResources().getString(R.string.str_make_ad_quantity_err);
        } else if (unitCode.equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_unit_err);
        } else if (strInputAmount.trim().equals("") || strInputAmount.equals("0")) {
            strMsg = String.format(getResources().getString(R.string.str_make_ad_amount_reinput), strInputAmount);
        } else if (etDesiredShippingDate.getText().toString().equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_desired_shipping_date_err);
        } else if (etADDetail.getText().toString().equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_detail_err);
        } else if (categoryMidCd.equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_category_err);
        } else if (categoryScls.equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_subcategory_err);
        } else if (areaMid.equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_cityt_err);
        } else if (areaScls.equals("")) {
            strMsg = getResources().getString(R.string.str_make_ad_district_err);
        }

        if (!strMsg.equals("")) {
            intent.putExtra("BtnDlgMsg", strMsg);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        } else {
            ArrayList<String> arrData = new ArrayList<String>();
            arrData.add(0, etADName.getText().toString());                                   //상품명
            arrData.add(1, et_quantity.getText().toString());                                //남은 수량
            arrData.add(2, unitCode);                                                        //단위
            arrData.add(3, etADAmount.getText().toString().replaceAll("[,\\\\]", "")); //상품가격
            arrData.add(4, etDesiredShippingDate.getText().toString());                      //희망출하일
            arrData.add(5, etADDetail.getText().toString());                                 //긴급사유
            arrData.add(6, categoryMidCd);                                                   //카테고리
            arrData.add(7, categoryScls);                                                    //세부항목
            //arrData.add(3, etADAmount.getText().toString().replaceAll(",", ""));
            arrData.add(8, areaMid);                                                         //도시
            arrData.add(9, areaScls);                                                        //시구

            String unitCodeNm      = getNameByCode(arrUnitList,     unitCode);      // 단위명
            String categoryMidCdNm = getNameByCode(arrData1,        categoryMidCd); // 카테고리 중분류 명
            String categorySclsNm  = getNameByCode(arrSubCategory,  categoryScls);  // 카테고리 소분류 명
            String areaMidNm       = getNameByCode(arrCityList,     areaMid);       // 도시 명
            String areaSclsNm      = getNameByCode(arrDistrictList, areaScls);      // 구/군 명



            arrData.add(unitCodeNm);        // index 11
            arrData.add(categoryMidCdNm);   // index 12
            arrData.add(categorySclsNm);    // index 13
            arrData.add(areaMidNm);         // index 14
            arrData.add(areaSclsNm);        // index 15

            mGetInfoData.onGetInfoData(arrData,categoryMidCd);
        }

    }

    private String getNameByCode(java.util.List<TxtListDataInfo> list, String code) {
        if (list == null || code == null) return "";
        for (TxtListDataInfo item : list) {
            if (code.equals(item.getStrIdx())) {
                return item.getStrMsg() == null ? "" : item.getStrMsg();
            }
        }
        return "";
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

        //남은수향
        et_quantity.setText(data.getQuantity());

        //단위
        unitCode = data.getUnitCode();
        if (dropdownUnit != null && arrUnitList != null) {
            String selectedUnitCode = data.getUnitCode();
            for (TxtListDataInfo item : arrUnitList) {
                if (item.getStrIdx().equals(selectedUnitCode)) {
                    dropdownUnit.setText(item.getStrMsg(), false); // 단위명 표시
                    break;
                }
            }
        }

        // 상품 가격
        if (etADAmount != null && data.getPrice() != null) {
            try {
                double priceDouble = Double.parseDouble(data.getPrice());
                long priceLong = (long) priceDouble;
                etADAmount.setText(String.valueOf(priceLong));
            } catch (NumberFormatException e) {
                etADAmount.setText("0"); // 예외 시 기본값
            }
        }

        // 출하일자
        etDesiredShippingDate.setText(data.getDesiredShippingDate());

        // 상세설명
        if (etADDetail != null) {
            etADDetail.setText(data.getDescription());
        }

        // 카테고리
        categoryMidCd = data.getCategoryMid();
        categoryScls = data.getCategoryScls();

        if (categorySelectedListener != null) {
            categorySelectedListener.onCategorySelected(categoryMidCd);
        }
        if (categoryMid != null && arrData1 != null) {
            for (TxtListDataInfo item : arrData1) {
                if (item.getStrIdx().equals(categoryMidCd)) {
                    categoryMid.setText(item.getStrMsg(), false); // 이제 오류 없음
                    categoryMidNm = item.getStrMsg();
                    break;
                }
            }
        }


        // 도시
        areaMid = data.getAreaMid();
        areaScls = data.getAreaScls();
        if (citySelectedListener != null) {
            citySelectedListener.onCitySelected(areaMid);
        }
        if (dropdownCity != null && arrCityList != null) {
            for (TxtListDataInfo item : arrCityList) {
                if (item.getStrIdx().equals(areaMid)) {
                    dropdownCity.setText(item.getStrMsg(), false); // 이제 오류 없음
                    areaMidNm = item.getStrMsg();
                    break;
                }
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

                categoryScls = "";
                categorySclsNm = "";
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
            categoryScls = selected.getStrIdx();
            categorySclsNm = selected.getStrMsg();
        });

        if (!categoryScls.isEmpty()) {
            if (dropdownSubCategory != null && arrSubCategory != null) {
                for (TxtListDataInfo item : arrSubCategory) {
                    if (item.getStrIdx().equals(categoryScls)) {
                        dropdownSubCategory.setText(item.getStrMsg(), false); // 드롭다운 항목 셋팅
                        categorySclsNm = item.getStrMsg();
                        break;
                    }
                }
            }
        }
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

                areaMid = selected.getStrIdx();
                areaMidNm = selected.getStrMsg();

                // 세부 항목 초기화 (예: arrDistrictList, dropdownDistrict 등)
                arrDistrictList.clear(); // 구/군 리스트 초기화
                dropdownDistrict.setText(""); // 선택 항목 초기화
                dropdownDistrict.setAdapter(null); // 어댑터 제거 (필요 시)
                areaScls = "";
                areaSclsNm = "";

                if (citySelectedListener != null) {
                    citySelectedListener.onCitySelected(areaMid);
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
            areaScls = selected.getStrIdx();
            areaSclsNm = selected.getStrMsg();
        });

        if(!areaScls.isEmpty()) {
            if (dropdownDistrict != null && arrDistrictList != null) {
                for (TxtListDataInfo item : arrDistrictList) {
                    if (item.getStrIdx().equals(areaScls)) {
                        dropdownDistrict.setText(item.getStrMsg(), false); // 드롭다운 항목 셋팅
                        areaSclsNm = item.getStrMsg();
                        break;
                    }
                }
            }
        }

    }

    public void setUnitList(List<TxtListDataInfo> codeList) {
        arrUnitList = new ArrayList<>(codeList);
        bindUnitDropdown();
    }

    private void bindUnitDropdown() {
        dropdownUnit = findViewById(R.id.dropdown_unit);

        if (arrUnitList == null || arrUnitList.isEmpty()) return;

        List<String> unitNames = new ArrayList<>();
        for (TxtListDataInfo item : arrUnitList) {
            unitNames.add(item.getStrMsg());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, R.layout.list_txt_item, unitNames);
        dropdownUnit.setAdapter(adapter);

        dropdownUnit.setOnClickListener(v -> dropdownUnit.showDropDown());

        dropdownUnit.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) dropdownUnit.showDropDown();
        });

        dropdownUnit.setOnItemClickListener((parent, view, position, id) -> {
            TxtListDataInfo selected = arrUnitList.get(position);
            unitCode = selected.getStrIdx();
            unitCodeNm = selected.getStrMsg(); // 선택한 단위 이름이 필요하면
        });
    }
}
