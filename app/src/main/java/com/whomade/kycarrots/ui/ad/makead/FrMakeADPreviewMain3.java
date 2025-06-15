package com.whomade.kycarrots.ui.ad.makead;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.whomade.kycarrots.R;
import com.whomade.kycarrots.TitleBar;
import com.whomade.kycarrots.common.AppServiceProvider;
import com.whomade.kycarrots.data.model.ProductImageVo;
import com.whomade.kycarrots.data.model.ProductVo;
import com.whomade.kycarrots.domain.Helper.AppServiceHelper;
import com.whomade.kycarrots.domain.service.AppService;
import com.whomade.kycarrots.ui.common.ImageLoader;
import com.whomade.kycarrots.ui.dialog.DlgBtnActivity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 광고제작 - 프리뷰 (광고정보)
 */
public class FrMakeADPreviewMain3 extends Fragment implements View.OnClickListener {
    private FragmentActivity mActivity;
    private LinearLayout llPreviewDetailImg; //광고 상세 이미지
    private ImageView ivPreviewTitle; //광고 타이틀 img view
    private String strTitleImgPath; //광고 타이틀 이미지
    private String strTitleImgId; //광고 타이틀 이미지
    private boolean[] arrIsChangeDetaillmg;
    private boolean isChangeTitleIlmg;
    private boolean isRejudged = false; //재심사 여부
    private ArrayList<String> arrDetailImg = new ArrayList<String>();
    private ArrayList<String> arrDetailImgId = new ArrayList<String>();
    private LinearLayout llProgress;
    private ProgressBar pbTitleImg;

    private String strADIdx;                            //광고 idx
    private String strADName;                           //광고명
    private String strADQuantity;                       //남은수향
    private String strADUnitCode;                       //단위
    private String strADAmount;                         //광고 할 금액
    private String strADDesiredShippingDate;            //출하일자
    private String strADDetail;                         //상세정보
    private String strADCategory;                       //카테고리
    private String strADCategoryScls;                   //세부항목
    private String strADAreaMid;                        //도시
    private String strADAreaScls;                       //시구


    public static final String STR_TITLE_IMG = "TITLE_IMG";
    public static final String STR_TITLE_IMGID = "TITLE_IMGID";
    public static final String STR_CHANGE_TITLE_IMG = "CHANGE_TITLE_IMG";
    public static final String STR_DETAIL_IMG = "DTAIL_IMG";
    public static final String STR_DETAIL_IMGID = "DTAIL_IMGID";
    public static final String STR_CHANGE_DETAIL_IMG = "CHANGE_DTAIL_IMG";



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity) activity;
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(mActivity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    public static FrMakeADPreviewMain3 newInstance(boolean isChangeTitleImg, String strTitle, ArrayList<String> arrBitmapData, boolean[] isChangeDetailImg) {
        FrMakeADPreviewMain3 fragment = new FrMakeADPreviewMain3();
        Bundle args = new Bundle();
        args.putString(STR_TITLE_IMG, strTitle);
        args.putBoolean(STR_CHANGE_TITLE_IMG, isChangeTitleImg);
        args.putStringArrayList(STR_DETAIL_IMG, arrBitmapData);
        args.putBooleanArray(STR_CHANGE_DETAIL_IMG, isChangeDetailImg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            strADIdx = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_IDX);
            strADName = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_NAME);
            strADQuantity = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_QUANTITY);
            strADUnitCode = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_UNIT_CODE);
            strADAmount = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_AMOUNT);
            strADDesiredShippingDate = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_DESIRED_SHIPPING_DATE);
            strADDetail = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_DETAIL);
            strADCategory = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_CATEGORY_MID);
            strADCategoryScls = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_CATEGORY_SCLS);
            strADAreaMid = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_AREA_MID);
            strADAreaScls = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_AREA_SCLS);

            strTitleImgPath = (String) getArguments().getString(STR_TITLE_IMG);
            strTitleImgId = (String) getArguments().getString(STR_TITLE_IMGID);
            arrIsChangeDetaillmg = getArguments().getBooleanArray(STR_CHANGE_DETAIL_IMG);
            isChangeTitleIlmg = getArguments().getBoolean(STR_CHANGE_TITLE_IMG);

            ArrayList<String> arrDetailImgTemp = (ArrayList<String>) getArguments().getStringArrayList(STR_DETAIL_IMG);
            if (arrDetailImgTemp != null && arrDetailImgTemp.size() > 0) {
                arrDetailImg.addAll((ArrayList<String>) getArguments().getStringArrayList(STR_DETAIL_IMG));
                arrDetailImgId.addAll((ArrayList<String>) getArguments().getStringArrayList(STR_DETAIL_IMGID));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ad_detail_main_1, container, false);
        llProgress = (LinearLayout) view.findViewById(R.id.ll_progress_circle);
        ((TitleBar) view.findViewById(R.id.title_bar)).setTitle(mActivity.getResources().getString(R.string.str_make_ad));
        ((TextView) view.findViewById(R.id.txt_ad_name)).setText(strADName);
        ((TextView) view.findViewById(R.id.txt_ad_detail)).setText(strADDetail);
        ((TextView) view.findViewById(R.id.txt_grade)).setText("3.0");
        ((ImageView) view.findViewById(R.id.iv_grade1)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star_press));
        ((ImageView) view.findViewById(R.id.iv_grade2)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star_press));
        ((ImageView) view.findViewById(R.id.iv_grade3)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star_press));

        Button btnRegiRequest = (Button) view.findViewById(R.id.btn_one);
        btnRegiRequest.setVisibility(View.VISIBLE);
        btnRegiRequest.setText(mActivity.getResources().getString(R.string.str_registration_request));
        btnRegiRequest.setOnClickListener(this);

        llPreviewDetailImg = (LinearLayout) view.findViewById(R.id.ll_ad_detail_img);
        ivPreviewTitle = (ImageView) view.findViewById(R.id.iv_ad_title);
        pbTitleImg = (ProgressBar) view.findViewById(R.id.pb_title_img);

        strTitleImgPath = (String) getArguments().getString(STR_TITLE_IMG);
        arrIsChangeDetaillmg = getArguments().getBooleanArray(STR_CHANGE_DETAIL_IMG);
        isChangeTitleIlmg = getArguments().getBoolean(STR_CHANGE_TITLE_IMG);



        SetDetailImg();
        return view;
    }

    /**
     * 상세 이미지 d/p
     */
    public void SetDetailImg() {
        //new getLatLng().execute(strADAddr);

        strTitleImgPath = strTitleImgPath.replace("\\", "//");
        if (isChangeTitleIlmg) {
            Bitmap bitTitle = decodeSampledPreviewBitmapFromPath(strTitleImgPath, Integer.parseInt(getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(getResources().getString(R.string.str_ad_h)));
            ivPreviewTitle.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ivPreviewTitle.setBackground(new BitmapDrawable(bitTitle));
            } else {
                ivPreviewTitle.setBackgroundDrawable(new BitmapDrawable(bitTitle));
            }
        } else {
            if (pbTitleImg != null && !pbTitleImg.isShown()) pbTitleImg.setVisibility(View.VISIBLE);

            ImageLoader.loadImage(mActivity, strTitleImgPath, ivPreviewTitle, pbTitleImg);

        }

        if (arrDetailImg != null) {
            for (int i = 0; i < arrDetailImg.size(); i++) {
                LayoutInflater inflaterDetailImg = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflaterDetailImg.inflate(R.layout.advertiser_make_ad_detail_img_add, null);

                ((ImageView) view.findViewById(R.id.iv_img_ad_detail)).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.txt_detail)).setVisibility(View.GONE);
                llPreviewDetailImg.addView(view);

                if (arrIsChangeDetaillmg[i]) {
                    Bitmap bImgDetail = decodeSampledPreviewBitmapFromPath(arrDetailImg.get(i), Integer.parseInt(getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(getResources().getString(R.string.str_ad_h)));

                    LinearLayout llDetailImg = (LinearLayout) llPreviewDetailImg.getChildAt(i).findViewById(R.id.ll_detail_img);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        llDetailImg.setBackground(new BitmapDrawable(bImgDetail));
                    } else {
                        llDetailImg.setBackgroundDrawable(new BitmapDrawable(bImgDetail));
                    }
                } else {
                    LinearLayout llDetailImg = (LinearLayout) llPreviewDetailImg.getChildAt(i).findViewById(R.id.ll_detail_img);
                    llDetailImg.setVisibility(View.GONE);

                    ImageView ivDetailImg = (ImageView) view.findViewById(R.id.iv_detail_img);
                    ivDetailImg.setVisibility(View.VISIBLE);

                    final ProgressBar pbDetailImg = (ProgressBar) view.findViewById(R.id.pb_detail_img);
                    if (pbDetailImg != null && pbDetailImg.isShown())
                        pbDetailImg.setVisibility(View.GONE);
                    ImageLoader.loadImage(mActivity, arrDetailImg.get(i).replace("\\", ""), ivDetailImg, pbDetailImg);

                }
            }
        }

        resizeImg();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_one) {
            //if (llProgress != null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            DataRequest();
        }
    }

    public void DataRequest() {
        // 1. 광고 정보 객체 생성
        ProductVo productVo = new ProductVo(
                strADIdx,              // productId
                "1",                   // userNo
                strADName,             // title
                strADDetail,           // description
                strADAmount,           // price
                "R010600",             // categoryGroup
                strADCategory,         // categoryMid
                strADCategoryScls,     // categoryScls
                "1",                   // saleStatus
                "R010070",             // cityGroup
                strADAreaMid,          // cityMid
                strADAreaScls,          // cityScls
                strADQuantity,         // quantity
                "R010620",             // unitGroup
                strADUnitCode,         // unitCode
                strADDesiredShippingDate, // desiredShippingDate
                "1",                   // registerNo
                "",                    // registDt
                "1",                   // updusrNo
                "",                    // updtDt
                ""// imageUrl 생략 가능 (기본값 null)
        );

        // 2. 이미지 파일 리스트 + imageMetaList 생성
        ArrayList<File> detailFiles = new ArrayList<>();
        List<ProductImageVo> imageMetaList = new ArrayList<>();
        ProductImageVo productImageVo = null;
        for (int i = 0; i < arrDetailImg.size(); i++) {
            if (arrIsChangeDetaillmg[i]) {
                File file = new File(arrDetailImg.get(i));
                detailFiles.add(file);

                String imageId = (arrDetailImgId.size() > i && arrDetailImgId.get(i) != null) ? arrDetailImgId.get(i) : "";

                 productImageVo = new ProductImageVo(
                        imageId,   // imageId
                        null,      // productId
                        "1",      // imageCd
                        null,      // imageUrl
                        null,      // imageName
                        "0",       // represent
                        null,      // imageSize
                        null,      // imageText
                        null,      // imageType
                        "",        // registerNo
                        null,      // registDt
                        "",        // updusrNo
                        null       // updtDt
                );
                imageMetaList.add(productImageVo);
            }
        }
        // 3. 제목 이미지 추가 (예: strTitleImgPath)
        if(isChangeTitleIlmg) {
            if (strTitleImgPath != null && !strTitleImgPath.isEmpty()) {
                File titleFile = isChangeTitleIlmg ? new File(strTitleImgPath) : new File(strTitleImgPath); // 필요시 수정
                detailFiles.add(0, titleFile); // 맨 앞에 제목 이미지 삽입

                productImageVo = new ProductImageVo(
                        strTitleImgId,   // imageId
                        null,   // productId
                        "1",   // imageCd
                        null,   // imageUrl
                        null,   // imageName
                        "1",    // represent (★ 꼭 입력)
                        null,   // imageSize
                        null,   // imageText
                        null,   // imageType
                        "",     // registerNo
                        null,   // registDt
                        "",     // updusrNo
                        null    // updtDt
                );
                imageMetaList.add(0, productImageVo);
            }
        }

        // 4. 서버 등록 요청

        AppService appService = AppServiceProvider.INSTANCE.getInstance();
        if (productVo.getProductId() == null || productVo.getProductId().isEmpty()) {
            AppServiceHelper.registerAdvertise(
                    appService,
                    productVo,
                    imageMetaList,
                    detailFiles,
                    () -> {
                        if (llProgress != null && llProgress.isShown())
                            llProgress.setVisibility(View.GONE);

                        Toast.makeText(mActivity, getResources().getString(R.string.str_ad_regi_success), Toast.LENGTH_SHORT).show();
                        ((MakeADPreviewActivity) getActivity()).finishAdd();
                        return null;
                    },
                    throwable -> {
                        if (llProgress != null && llProgress.isShown())
                            llProgress.setVisibility(View.GONE);

                        Toast.makeText(mActivity, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                        Log.e("registerAdvertise", "등록 실패", throwable);
                        return null;
                    }
            );
        } else {
            AppServiceHelper.updateAdvertise(
                    appService,
                    productVo,
                    imageMetaList,
                    detailFiles,
                    () -> {
                        if (llProgress != null && llProgress.isShown())
                            llProgress.setVisibility(View.GONE);

                        Toast.makeText(mActivity, "광고 수정 성공", Toast.LENGTH_SHORT).show();
                        ((MakeADPreviewActivity) getActivity()).finishAdd();
                        return null;
                    },
                    throwable -> {
                        if (llProgress != null && llProgress.isShown())
                            llProgress.setVisibility(View.GONE);

                        Toast.makeText(mActivity, "광고 수정 실패", Toast.LENGTH_SHORT).show();
                        Log.e("updateAdvertise", "수정 실패", throwable);
                        return null;
                    }
            );
        }

        if (llProgress != null && !llProgress.isShown())
            llProgress.setVisibility(View.VISIBLE);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (llProgress != null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }


    private File contentTitle;
    private File contentThumbnail;
    private ArrayList<File> contentDetail;

    private void resizeImg() {
        if (isChangeTitleIlmg) {
            File titleFile = new File(strTitleImgPath);
            File thumbnailFile = new File(strTitleImgPath);
            if (titleFile.exists() && thumbnailFile.exists()) {
                contentTitle = titleFile;
                contentThumbnail = thumbnailFile;
            } else {
                Intent intent = new Intent(mActivity, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", mActivity.getResources().getString(R.string.str_ad_img_err));
                startActivity(intent);
                return;
            }
        } else {
            contentTitle = new File(strTitleImgPath);
        }

        if (arrDetailImg.size() > 0) {
            contentDetail = new ArrayList<>();
            for (int i = 0; i < arrDetailImg.size(); i++) {
                File oFile = new File(arrDetailImg.get(i));
                if (oFile.exists()) {
                    contentDetail.add(oFile);
                }
            }
        }
    }


    private Bitmap decodeSampledPreviewBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeFile(path, options);
        src = src.createScaledBitmap(src, reqWidth, reqHeight, false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return src;
    }


}