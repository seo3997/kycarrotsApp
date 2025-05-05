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
            DetailAdd(strDetailImgUrl1.replace("\\", "//"));
        }
        if(strDetailImgUrl2!=null && !strDetailImgUrl2.equals("")){
            DetailAdd(strDetailImgUrl2.replace("\\", "//"));
        }
        if(strDetailImgUrl3!=null && !strDetailImgUrl3.equals("")){
            DetailAdd(strDetailImgUrl3.replace("\\", "//"));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_detail_img_add) {
            DetailAdd("");
        }
    }

    /**
     * 광고 세부이미지 추가
     */
    private SparseArray<View> views = new SparseArray<View>();
    public void DetailAdd(String strUrl){
        LayoutInflater inflaterDetailImg = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflaterDetailImg.inflate(R.layout.advertiser_make_ad_detail_img_add, null);
        view.setTag(mDetailImgCnt);
        view.setOnClickListener(mDetailImgClick);
        LinearLayout llDeatilImg = (LinearLayout) view.findViewById(R.id.ll_detail_img);

        if(strUrl!=null && !strUrl.equals("")) {
            llDeatilImg.setVisibility(View.GONE);

            ImageView ivDetailImg = (ImageView) view.findViewById(R.id.iv_detail_img);
            ivDetailImg.setVisibility(View.VISIBLE);
            final ProgressBar pbDetailImg = (ProgressBar) view.findViewById(R.id.pb_detail_img);
            if (pbDetailImg != null && !pbDetailImg.isShown()) pbDetailImg.setVisibility(View.VISIBLE);
            //ImageLoader.loadImage(mContext, strUrl, ivDetailImg, pbDetailImg);

            selView = view;
        }
        llMakeDetailImg.addView(view);
        views.put((Integer) view.getTag(), view);
        arrIsChangeDetailImgTemp.add(false);
        hmDetailPos.put((Integer) view.getTag(), strUrl);

        mDetailImgCnt++;
        if(views.size() >= DETAIL_IMG_ADD_MAX){
            btnDetailImgAdd.setVisibility(View.GONE);
        }

        //호출하면 스크롤뷰의 레이아웃이 재설정됨.
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

    private View selView;
    public OnClickListener mDetailImgClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.ll_detail_title_img || v.getId() == R.id.iv_detail_title_img){
                mClick.onDetailImgClick(!txtDetailTitle.isShown(), STR_TITLE_IMG);
            }else {
                int viewPos = (Integer)v.getTag();
                ViewGroup ro = (ViewGroup) views.get(viewPos);
                selView = ro;

                mClick.onDetailImgClick(!((ImageView) ro.findViewById(R.id.iv_img_ad_detail)).isShown(), STR_DETAIL_IMG);
            }
        }
    };

    /**
     * 타이틀 or 세부 이미지 d/p
     * @param strPath
     */
    private HashMap<Integer, String> hmDetailPos = new HashMap<Integer, String>();
    private String strTitleImgPath = "";
    private boolean isChangeTitleImg = false;
    private ArrayList<Boolean> arrIsChangeDetailImgTemp = new ArrayList<Boolean>();
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

            arrIsChangeDetailImgTemp.set((Integer) selView.getTag(), true);
            hmDetailPos.put((Integer) selView.getTag(), strPath);

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

            arrIsChangeDetailImgTemp.set((Integer) selView.getTag(), true);
            hmDetailPos.put((Integer) selView.getTag(), strPath);

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
            // ContentResolver를 사용하여 Uri에서 InputStream을 얻습니다.
            InputStream inputStream = mContext.getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                // BitmapFactory.decodeStream()을 사용하여 InputStream에서 Bitmap을 생성합니다.
                bitImgSize = BitmapFactory.decodeStream(inputStream);
                //이미지 크기 조절
                bitImgSize = Bitmap.createScaledBitmap(bitImgSize, Integer.parseInt(mContext.getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(mContext.getResources().getString(R.string.str_ad_h)), false);
                inputStream.close();
            }
            // Bitmap을 파일로 저장합니다.
            File file = createImageFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitImgSize.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            // 저장된 파일의 경로를 얻습니다.
            strPath = file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bitImgSize == null) {
            // Bitmap 생성 실패 처리
            return;
        }

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
            LinearLayout llDeatilImg = (LinearLayout) selView.findViewById(R.id.ll_detail_img);
            llDeatilImg.setVisibility(View.VISIBLE);

            ImageView ivDetailImg = (ImageView) selView.findViewById(R.id.iv_detail_img);
            ivDetailImg.setVisibility(View.GONE);

            ((ImageView) selView.findViewById(R.id.iv_img_ad_detail)).setVisibility(View.GONE);
            ((TextView) selView.findViewById(R.id.txt_detail)).setVisibility(View.GONE);

            arrIsChangeDetailImgTemp.set((Integer) selView.getTag(), true);
            hmDetailPos.put((Integer) selView.getTag(), strPath);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llDeatilImg.setBackground(new BitmapDrawable(mContext.getResources(), bitImgSize));
            } else {
                llDeatilImg.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitImgSize));
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
            }else{
                llTitleImg.setBackgroundDrawable(getResources().getDrawable(R.drawable.img));
            }
            strTitleImgPath = "";
            llTitleImg.setVisibility(View.VISIBLE);
            ivTitle.setVisibility(View.GONE);
            ivDetailTitle.setVisibility(View.VISIBLE);
            txtDetailTitle.setVisibility(View.VISIBLE);
        }else if(strKind.equals(STR_DETAIL_IMG)) {
            llMakeDetailImg.removeView(selView);
            hmDetailPos.remove(selView);
            arrIsChangeDetailImgTemp.remove((Integer) selView.getTag());
            views.remove((Integer) selView.getTag());

            if(views.size() < DETAIL_IMG_ADD_MAX){
                btnDetailImgAdd.setVisibility(View.VISIBLE);
            }
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
        public void onGetData(ArrayList<Boolean> arrIsChangeDetailImg, boolean isChangeTitleImg, String strTitle, ArrayList<String> arrDetailImg);
    }

    public void getOnData(OnGetData getData)
    {
        mGetData = getData;
    }

    /**
     * 세부이미지 추가 사항에 대한 list 구성 (이미지 전달 data 필요)
     */
    public void setData(){
        Intent intent = null;
        if(strTitleImgPath==null || strTitleImgPath.equals("")){
            intent = new Intent(mContext, DlgBtnActivity.class);
            intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_ad_title_img_err));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            return;
        }

        ArrayList<String> arrDetail = new ArrayList<String>();
        ArrayList<Boolean> arrIsChangeDetail = new ArrayList<Boolean>();
        for(int i=0; i<views.size(); i++){
            int v = (int) views.get(views.keyAt(i)).getTag();
            ViewGroup ro = (ViewGroup) views.get(v);
            if (!((ImageView) ro.findViewById(R.id.iv_img_ad_detail)).isShown()) {
                arrDetail.add(hmDetailPos.get(v));
                arrIsChangeDetail.add(arrIsChangeDetailImgTemp.get(v));
            }
        }
        mGetData.onGetData(arrIsChangeDetail, isChangeTitleImg, strTitleImgPath, arrDetail);
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
