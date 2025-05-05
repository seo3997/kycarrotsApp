package com.whomade.kycarrots.ui.ad.makead;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.whomade.kycarrots.R;

import java.util.ArrayList;

/**
 * 광고제작 - 미리보기
 */
public class MakeADPreviewActivity extends FragmentActivity {
    private ViewPager vpPreview;
    private PreviewPagerAdapter mPreviewAdapter;

    private String strTitle = null; //타이틀 이미지
    private boolean isChangeTitleImg = false; //타이틀 이미지
    private ArrayList<String> arrDetailImg = new ArrayList<String>(); //광고 상세 이미지

    private final int THIS_PAGE_1=1;
    private final int THIS_PAGE_2=0; //2번째 페이지가 왼쪽에 보이기 위해서 값이 '0' 임

    private ImageView ivPage1;
    private ImageView ivPage2;
    private LinearLayout llProgress;

    private String strADIdx; //광고 idx
    private String strADName; //광고명
    private String strADDetail; //상세정보
    private String strADCategory; //카테고리
    private String strADAmount; //광고 할 금액

    private boolean[] isChangeDetailImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_ad_detail);

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        Intent intent = getIntent();
        if(intent!=null) {
            strADIdx = intent.getStringExtra(MakeADDetail1.STR_PUT_AD_IDX);//광고 idx
            strADName = intent.getStringExtra(MakeADDetail1.STR_PUT_AD_NAME); //광고명
            strADDetail = intent.getStringExtra(MakeADDetail1.STR_PUT_AD_DETAIL); //상세설명
            strADCategory = intent.getStringExtra(MakeADDetail1.STR_PUT_AD_CATEGORY); //카테고리
            strADAmount = intent.getStringExtra(MakeADDetail1.STR_PUT_AD_AMOUNT); //광고 할 금액

            strTitle = (String) intent.getStringExtra("Title_Img");
            isChangeTitleImg = (boolean) intent.getBooleanExtra("ChangeTitleImg", false);
            arrDetailImg = (ArrayList<String>) intent.getExtras().get("Detail_Img");
            ArrayList<Boolean> arrIsChangeDetailImg = (ArrayList<Boolean>) intent.getExtras().get("ChangeDetailImg");
            if(arrIsChangeDetailImg!=null && arrIsChangeDetailImg.size()>0) {
                isChangeDetailImg = new boolean[arrIsChangeDetailImg.size()];
                for (int i = 0; i < arrIsChangeDetailImg.size(); i++) {
                    isChangeDetailImg[i] = arrIsChangeDetailImg.get(i);
                }
            }
        }

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);

        ivPage1 = (ImageView) findViewById(R.id.iv_page1);
        ivPage2 = (ImageView) findViewById(R.id.iv_page2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ivPage2.setBackground(getResources().getDrawable(R.drawable.one));
            ivPage1.setBackground(getResources().getDrawable(R.drawable.one_press));
        }else{
            ivPage2.setBackgroundDrawable(getResources().getDrawable(R.drawable.one));
            ivPage1.setBackgroundDrawable(getResources().getDrawable(R.drawable.one_press));
        }

        vpPreview = (ViewPager) findViewById(R.id.vp_ad_detail);
        mPreviewAdapter = new PreviewPagerAdapter(getSupportFragmentManager());
        vpPreview.setAdapter(mPreviewAdapter);
        vpPreview.setCurrentItem(THIS_PAGE_1);

        vpPreview.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == THIS_PAGE_1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ivPage2.setBackground(getResources().getDrawable(R.drawable.one));
                        ivPage1.setBackground(getResources().getDrawable(R.drawable.one_press));
                    } else {
                        ivPage2.setBackgroundDrawable(getResources().getDrawable(R.drawable.one));
                        ivPage1.setBackgroundDrawable(getResources().getDrawable(R.drawable.one_press));
                    }
                } else if (position == THIS_PAGE_2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ivPage2.setBackground(getResources().getDrawable(R.drawable.one_press));
                        ivPage1.setBackground(getResources().getDrawable(R.drawable.one));
                    } else {
                        ivPage2.setBackgroundDrawable(getResources().getDrawable(R.drawable.one_press));
                        ivPage1.setBackgroundDrawable(getResources().getDrawable(R.drawable.one));
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.fl_bg));
    }

    private void recycleView(View view) {
        if(view != null) {
            Drawable bg = view.getBackground();
            if(bg != null) {
                bg.setCallback(null);
                ((BitmapDrawable)bg).getBitmap().recycle();
                view.setBackgroundDrawable(null);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (llProgress != null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
            }
        }, 500);
    }

    public void finishAdd(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setResult(RESULT_OK);
                finish();
            }
        }, 300);
    }

    public class PreviewPagerAdapter extends FragmentStatePagerAdapter {
        private int TOTAL_PAGE = 2;
        private int mThisPage = THIS_PAGE_1;

        public PreviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            mThisPage = position;
            switch (position) {
                case THIS_PAGE_1:
                    fragment = new FrMakeADPreviewMain3();
                    Bundle bundle = new Bundle();

                    bundle.putString(MakeADDetail1.STR_PUT_AD_IDX, strADIdx);
                    bundle.putString(MakeADDetail1.STR_PUT_AD_NAME, strADName);
                    bundle.putString(MakeADDetail1.STR_PUT_AD_DETAIL, strADDetail);
                    bundle.putString(MakeADDetail1.STR_PUT_AD_CATEGORY, strADCategory);
                    bundle.putString(MakeADDetail1.STR_PUT_AD_AMOUNT, strADAmount);

                    bundle.putString("TITLE_IMG", strTitle);
                    bundle.putBoolean("CHANGE_TITLE_IMG", isChangeTitleImg);
                    bundle.putStringArrayList("DTAIL_IMG", arrDetailImg);
                    bundle.putBooleanArray("CHANGE_DTAIL_IMG", isChangeDetailImg);

                    fragment.setArguments(bundle);
                    break;
                case THIS_PAGE_2:
                    fragment = Fragment.instantiate(MakeADPreviewActivity.this, FrMakeADPreviewAdvertiserInfo3.class.getName());
                    break;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },1000);
            return fragment;
        }

        @Override
        public int getCount() {
            return TOTAL_PAGE;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
