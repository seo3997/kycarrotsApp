package com.whomade.kycarrots.ui.ad.makead;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.whomade.kycarrots.R;


/**
 * 광고제작 - 프리뷰 (광고주 정보)
 * 나의 광고 상세보기 (광고주 정보)
 */
public class FrMakeADPreviewAdvertiserInfo3 extends Fragment {
    private LinearLayout llAdvertiserErr;
    private ScrollView svAdvertiserInfo;
    private ImageView ivAdvertiserImg;
    private ProgressBar pbAdvertiser;
    private LinearLayout llAdvertiserImgErr;//광고주 사진
    private TextView txtAdvertiserImgErr; //통신오류 txt d/p
    private TextView txtTradeNm; //상호명
    private TextView txtRepresentativeNm; //대표자
    private TextView txtRepresentativeTel; //대표전화
    private TextView txtAddress; //주소
    private ImageView ivMapErr; //지도 표시 오류
    private LinearLayout llMap; //지도

    private FragmentActivity mActivity;


    private ADDetailInfo mADDetailInfo;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.ad_detail_advertiser_info_2, container, false );
        llAdvertiserErr = (LinearLayout) view.findViewById(R.id.ll_empty);
        svAdvertiserInfo = (ScrollView) view.findViewById(R.id.sv_info);
        ivAdvertiserImg = (ImageView) view.findViewById(R.id.iv_img);
        pbAdvertiser = (ProgressBar) view.findViewById(R.id.pb_advetiser);
        llAdvertiserImgErr = (LinearLayout) view.findViewById(R.id.ll_img);
        txtAdvertiserImgErr = (TextView) view.findViewById(R.id.txt_advertiser_err);
        txtTradeNm = (TextView) view.findViewById(R.id.txt_trade_nm);
        txtRepresentativeNm = (TextView) view.findViewById(R.id.txt_representative_nm);
        txtRepresentativeTel = (TextView) view.findViewById(R.id.txt_representative_tel);
        txtAddress = (TextView) view.findViewById(R.id.txt_address);
        ivMapErr = (ImageView) view.findViewById(R.id.iv_map_err);
        llMap = (LinearLayout) view.findViewById(R.id.ll_map);

        if(mADDetailInfo!=null){
            displayView();
        }else{
            llAdvertiserErr.setVisibility(View.VISIBLE);
            svAdvertiserInfo.setVisibility(View.GONE);
        }
        return view;
    }

    public void displayView(){
        llAdvertiserErr.setVisibility(View.GONE);
        svAdvertiserInfo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
