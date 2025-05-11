package com.whomade.kycarrots.ui.ad.makead;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.whomade.kycarrots.R;
import com.whomade.kycarrots.ui.common.ImageLoader;
import com.whomade.kycarrots.ui.dialog.DlgBtnActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 광고제작 - 2. 이미지 등록
 */
public class MakeADImgRegi2 extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private LayoutInflater inflater;

    private ScrollView svDetailImg;
    private final int DETAIL_IMG_ADD_MAX = 3; //세부 이미지 추가 max 개수
    private LinearLayout llMakeDetailImg; //광고 세부이미지 add view
    private int mDetailImgCnt=0; //세부 이미지 추가 개수
    private Button btnDetailImgAdd; //세부 이미지 추가 btn
    private LinearLayout llTitleImg; //광고 타이틀 view
    private ImageView ivDetailTitle;
    private TextView txtDetailTitle;
    private ImageView ivTitle;
    private ProgressBar pbTitleImg;

    private String STR_TITLE_IMG = "Title";
    private String STR_DETAIL_IMG = "Detail";
    private String imageFilePath; // imageFilePath 변수 선언 (클래스 멤버 변수)

    private Map<String, View> viewsMap = new LinkedHashMap<>();
    private Map<String, Boolean> isChangedMap = new LinkedHashMap<>();
    private Map<String, String> imagePathMap = new LinkedHashMap<>();
    private Map<String, String> imageIdMap = new LinkedHashMap<>();
    private int imgCounter = 0; // 고유 키 생성용

    private View selView;
    private String selKey;

    public MakeADImgRegi2(Context context) {
        super(context);
        mContext = context;
        Init();
    }

    /**
     * 생성자
     * @param context
     * @param attrs
     */
    public MakeADImgRegi2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        Init();
    }

    /**
     * layout 구성
     */
    private void Init(){
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.advertiser_make_ad_img_registration, this, true);

        btnDetailImgAdd = (Button)findViewById(R.id.btn_detail_img_add);
        btnDetailImgAdd.setOnClickListener(this);

        llMakeDetailImg = (LinearLayout) findViewById(R.id.ll_make_ad_detail_img);

        llTitleImg = (LinearLayout) findViewById(R.id.ll_detail_title_img);
        ivTitle = (ImageView) findViewById(R.id.iv_detail_title_img);
        pbTitleImg = (ProgressBar) findViewById(R.id.pb_title_img);
        ivDetailTitle = (ImageView) findViewById(R.id.iv_img_title);
        txtDetailTitle = (TextView) findViewById(R.id.txt_title_img);
        llTitleImg.setOnClickListener(mDetailImgClick);
        ivTitle.setOnClickListener(mDetailImgClick);

        svDetailImg = (ScrollView) findViewById(R.id.sv_detail_img);
        ((Button) findViewById(R.id.btn_make_ad_img_registration_next)).setOnClickListener(mNext);

    }
    public void modifyAD(ModifyADInfo data){
        strTitleImgPath = data.getStrADTitleImgUrl().replace("\\", "//");
        strTitleImgid = data.getaDTitleimageId();

        llTitleImg.setVisibility(View.GONE);
        ivTitle.setVisibility(View.VISIBLE);
        if (pbTitleImg != null && !pbTitleImg.isShown()) pbTitleImg.setVisibility(View.VISIBLE);

        //ImageLoader.loadImage(this, strTitleImgPath, ivTitle, pbTitleImg);

        Glide.with(mContext)
                .load(strTitleImgPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        if (pbTitleImg != null) {
                            pbTitleImg.setVisibility(android.view.View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        if (pbTitleImg != null) {
                            pbTitleImg.setVisibility(android.view.View.GONE);
                        }
                        return false;
                    }
                })
                .into(ivTitle);



        String strDetailImgUrl1 = data.getStrADDetailImgUrl1();
        String strDetailImgUrl2 = data.getStrADDetailImgUrl2();
        String strDetailImgUrl3 = data.getStrADDetailImgUrl3();

        if(strDetailImgUrl1!=null && !strDetailImgUrl1.equals("")){
            DetailAdd(strDetailImgUrl1.replace("\\", "//"), data.getaDDetailimageId1());
        }
        if(strDetailImgUrl2!=null && !strDetailImgUrl2.equals("")){
            DetailAdd(strDetailImgUrl2.replace("\\", "//"), data.getaDDetailimageId2());
        }
        if(strDetailImgUrl3!=null && !strDetailImgUrl3.equals("")){
            DetailAdd(strDetailImgUrl3.replace("\\", "//"), data.getaDDetailimageId3());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_detail_img_add) {
            DetailAdd("","");
        }
    }

    /**
     * 광고 세부이미지 추가
     */
    private SparseArray<View> views = new SparseArray<View>();
    public void DetailAdd(String strUrl,String imageId){
        LayoutInflater inflaterDetailImg = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflaterDetailImg.inflate(R.layout.advertiser_make_ad_detail_img_add, null);

        String imgKey = "IMG_" + imgCounter++;
        view.setTag(imgKey);
        view.setOnClickListener(mDetailImgClick);
        LinearLayout llDetailImg = view.findViewById(R.id.ll_detail_img);

        if(strUrl != null && !strUrl.isEmpty()) {
            llDetailImg.setVisibility(View.GONE);
            ImageView ivDetailImg = view.findViewById(R.id.iv_detail_img);
            ivDetailImg.setVisibility(View.VISIBLE);
            ProgressBar pbDetailImg = view.findViewById(R.id.pb_detail_img);
            if (pbDetailImg != null && !pbDetailImg.isShown()) pbDetailImg.setVisibility(View.VISIBLE);
            ImageLoader.loadImage(mContext, strUrl, ivDetailImg, pbDetailImg);
        }

        llMakeDetailImg.addView(view);
        viewsMap.put(imgKey, view);
        imagePathMap.put(imgKey, strUrl);
        isChangedMap.put(imgKey, false);
        imageIdMap.put(imgKey, imageId); // 초기값

        if(viewsMap.size() >= DETAIL_IMG_ADD_MAX){
            btnDetailImgAdd.setVisibility(View.GONE);
        }

        svDetailImg.invalidate();
        svDetailImg.requestLayout();
    }

    private onDetailItemClick mClick;
    // 이벤트 인터페이스를 정의
    public interface onDetailItemClick {
        public void onDetailImgClick(boolean isImg, String strKind);
    }

    public void setOnDetailImgClickListener(onDetailItemClick listener)
    {
        mClick = listener;
    }


    public OnClickListener mDetailImgClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ll_detail_title_img || v.getId() == R.id.iv_detail_title_img) {
                mClick.onDetailImgClick(!txtDetailTitle.isShown(), STR_TITLE_IMG);
            } else {
                String key = (String) v.getTag();
                ViewGroup ro = (ViewGroup) viewsMap.get(key);

                selView = ro;
                selKey = key;

                mClick.onDetailImgClick(!((ImageView) ro.findViewById(R.id.iv_img_ad_detail)).isShown(), STR_DETAIL_IMG);
            }
        }
    };
    /**
     * 타이틀 or 세부 이미지 d/p
     * @param strPath
     */
    private String strTitleImgPath = "";
    private String strTitleImgid = "";
    private boolean isChangeTitleImg = false;

    public void setImg(String fileDir, String fileName, String strKind){

        String strPath = fileDir+fileName;
        Bitmap bitImgSize = decodeSampledPreviewBitmapFromPath(strPath, Integer.parseInt(mContext.getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(mContext.getResources().getString(R.string.str_ad_h)));
        if(strKind.equals(STR_TITLE_IMG)){
            ivDetailTitle.setVisibility(View.GONE);
            txtDetailTitle.setVisibility(View.GONE);
            ivTitle.setVisibility(View.GONE);
            llTitleImg.setVisibility(View.VISIBLE);

            strTitleImgPath = strPath;
            isChangeTitleImg = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llTitleImg.setBackground(new BitmapDrawable(bitImgSize));
            } else {
                llTitleImg.setBackgroundDrawable(new BitmapDrawable(bitImgSize));
            }
        }else if(strKind.equals(STR_DETAIL_IMG)) {
            LinearLayout llDeatilImg = (LinearLayout) selView.findViewById(R.id.ll_detail_img);
            llDeatilImg.setVisibility(View.VISIBLE);

            ImageView ivDetailImg = (ImageView) selView.findViewById(R.id.iv_detail_img);
            ivDetailImg.setVisibility(View.GONE);

            ((ImageView) selView.findViewById(R.id.iv_img_ad_detail)).setVisibility(View.GONE);
            ((TextView) selView.findViewById(R.id.txt_detail)).setVisibility(View.GONE);

            isChangedMap.put(selKey, true);
            imagePathMap.put(selKey, strPath);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llDeatilImg.setBackground(new BitmapDrawable(bitImgSize));
            } else {
                llDeatilImg.setBackgroundDrawable(new BitmapDrawable(bitImgSize));
            }
        }
    }
    public void setImg(String fileDirfileName, String strKind){

        String strPath = fileDirfileName;
        Bitmap bitImgSize = decodeSampledPreviewBitmapFromPath(strPath, Integer.parseInt(mContext.getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(mContext.getResources().getString(R.string.str_ad_h)));
        if(strKind.equals(STR_TITLE_IMG)){
            ivDetailTitle.setVisibility(View.GONE);
            txtDetailTitle.setVisibility(View.GONE);
            ivTitle.setVisibility(View.GONE);
            llTitleImg.setVisibility(View.VISIBLE);

            strTitleImgPath = strPath;
            isChangeTitleImg = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llTitleImg.setBackground(new BitmapDrawable(bitImgSize));
            } else {
                llTitleImg.setBackgroundDrawable(new BitmapDrawable(bitImgSize));
            }
        }else if(strKind.equals(STR_DETAIL_IMG)) {
            LinearLayout llDeatilImg = (LinearLayout) selView.findViewById(R.id.ll_detail_img);
            llDeatilImg.setVisibility(View.VISIBLE);

            ImageView ivDetailImg = (ImageView) selView.findViewById(R.id.iv_detail_img);
            ivDetailImg.setVisibility(View.GONE);

            ((ImageView) selView.findViewById(R.id.iv_img_ad_detail)).setVisibility(View.GONE);
            ((TextView) selView.findViewById(R.id.txt_detail)).setVisibility(View.GONE);

            isChangedMap.put(selKey, true);
            imagePathMap.put(selKey, strPath);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llDeatilImg.setBackground(new BitmapDrawable(bitImgSize));
            } else {
                llDeatilImg.setBackgroundDrawable(new BitmapDrawable(bitImgSize));
            }
        }
    }
    public void setImg(Uri fileUri, String strKind) {
        Bitmap bitImgSize = null;
        String strPath = null;

        try {
            InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                bitImgSize = BitmapFactory.decodeStream(inputStream);
                bitImgSize = Bitmap.createScaledBitmap(
                        bitImgSize,
                        Integer.parseInt(mContext.getResources().getString(R.string.str_ad_char_w)),
                        Integer.parseInt(mContext.getResources().getString(R.string.str_ad_h)),
                        false
                );
                inputStream.close();
            }

            File file = createImageFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitImgSize.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            strPath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitImgSize == null) return;

        if (strKind.equals(STR_TITLE_IMG)) {
            ivDetailTitle.setVisibility(View.GONE);
            txtDetailTitle.setVisibility(View.GONE);
            ivTitle.setVisibility(View.GONE);
            llTitleImg.setVisibility(View.VISIBLE);

            strTitleImgPath = strPath;
            isChangeTitleImg = true;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llTitleImg.setBackground(new BitmapDrawable(mContext.getResources(), bitImgSize));
            } else {
                llTitleImg.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitImgSize));
            }

        } else if (strKind.equals(STR_DETAIL_IMG)) {
            if (selKey == null || selView == null) return;

            LinearLayout llDetailImg = selView.findViewById(R.id.ll_detail_img);
            llDetailImg.setVisibility(View.VISIBLE);

            selView.findViewById(R.id.iv_detail_img).setVisibility(View.GONE);
            selView.findViewById(R.id.iv_img_ad_detail).setVisibility(View.GONE);
            selView.findViewById(R.id.txt_detail).setVisibility(View.GONE);

            isChangedMap.put(selKey, true);
            imagePathMap.put(selKey, strPath);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llDetailImg.setBackground(new BitmapDrawable(mContext.getResources(), bitImgSize));
            } else {
                llDetailImg.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitImgSize));
            }
        }
    }
    //createImageFile() 함수 추가
    public File createImageFile() throws IOException {
        // Create an image file name
        java.util.Date date = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timeStamp = dateFormat.format(date);
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            // 저장 디렉토리를 찾을 수 없는 경우 예외 발생
            throw new IOException("저장 디렉토리를 찾을 수 없습니다.");
        }
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        imageFilePath = image.getAbsolutePath();
        return image;
    }
    /**
     * 타이틀 or 세부 이미지 삭제
     */
    public void DelImg(String strKind){
        if(strKind.equals(STR_TITLE_IMG)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llTitleImg.setBackground(getResources().getDrawable(R.drawable.img));
            } else {
                llTitleImg.setBackgroundDrawable(getResources().getDrawable(R.drawable.img));
            }
            strTitleImgPath = "";
            llTitleImg.setVisibility(View.VISIBLE);
            ivTitle.setVisibility(View.GONE);
            ivDetailTitle.setVisibility(View.VISIBLE);
            txtDetailTitle.setVisibility(View.VISIBLE);
        } else if(strKind.equals(STR_DETAIL_IMG)) {
            if (selKey == null || !viewsMap.containsKey(selKey)) return;

            View viewToRemove = viewsMap.get(selKey);
            llMakeDetailImg.removeView(viewToRemove);

            viewsMap.remove(selKey);
            imagePathMap.remove(selKey);
            isChangedMap.remove(selKey);
            imageIdMap.remove(selKey);

            selKey = null;
            selView = null;

            if (viewsMap.size() < DETAIL_IMG_ADD_MAX) {
                btnDetailImgAdd.setVisibility(View.VISIBLE);
            }

            svDetailImg.invalidate();
            svDetailImg.requestLayout();
        }
    }
    //다음 버튼 클릭
    public OnClickListener mNext = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setData();
        }
    };

    private OnGetData mGetData;
    // 이벤트 인터페이스를 정의
    public interface OnGetData {
        public void onGetData(ArrayList<Boolean> arrIsChangeDetailImg, boolean isChangeTitleImg, String strTitle, ArrayList<String> arrDetailImg,String  titleImgId,ArrayList<String> arrDetailImgId);
    }

    public void getOnData(OnGetData getData)
    {
        mGetData = getData;
    }

    /**
     * 세부이미지 추가 사항에 대한 list 구성 (이미지 전달 data 필요)
     */
    public void setData(){
        if(strTitleImgPath == null || strTitleImgPath.isEmpty()) {
            Intent intent = new Intent(mContext, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_ad_title_img_err));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            return;
        }

        ArrayList<String> arrDetail = new ArrayList<>();
        ArrayList<Boolean> arrIsChangeDetail = new ArrayList<>();
        ArrayList<String> arrDetailImgId = new ArrayList<>();

        for (Map.Entry<String, View> entry : viewsMap.entrySet()) {
            String key = entry.getKey();
            ViewGroup ro = (ViewGroup) entry.getValue();
            if (!ro.findViewById(R.id.iv_img_ad_detail).isShown()) {
                arrDetail.add(imagePathMap.get(key));
                arrIsChangeDetail.add(isChangedMap.get(key));
                arrDetailImgId.add(imageIdMap.get(key));
            }
        }

        mGetData.onGetData(arrIsChangeDetail, isChangeTitleImg, strTitleImgPath, arrDetail, strTitleImgid, arrDetailImgId);
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

    public String getTitleImgId() {
        return strTitleImgid;
    }

    public String getSelectedDetailImgId() {
        return selKey != null ? imageIdMap.get(selKey) : "";
    }
}
