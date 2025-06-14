package com.whomade.kycarrots.ui.ad.makead;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.whomade.kycarrots.MainTitleBar;
import com.whomade.kycarrots.R;
import com.whomade.kycarrots.TitleBar;
import com.whomade.kycarrots.common.AppServiceProvider;
import com.whomade.kycarrots.common.RetrofitProvider;
import com.whomade.kycarrots.data.api.AdApi;
import com.whomade.kycarrots.data.model.ProductImageVo;
import com.whomade.kycarrots.data.repository.RemoteRepository;
import com.whomade.kycarrots.domain.Helper.AppServiceHelper;
import com.whomade.kycarrots.domain.service.AppService;
import com.whomade.kycarrots.ui.dialog.DlgSelImg;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.yalantis.ucrop.UCrop;

/**
 * 광고제작
 * 1. 세부정보, 2. 이미지 등록
 */
public class MakeADMainActivity extends Activity implements View.OnClickListener, DialogInterface.OnDismissListener{
    private final int MAKE_AD_COMPLETE = 999;
    private LinearLayout llProgress;

    private TextView txtDetailInfo;
    private LinearLayout llDetailInfoUnder;
    private TextView txtRegiImg;
    private LinearLayout llRegiImgUnder;


    private MakeADDetail1 makeADDetail;//1. 세부정보 view
    private MakeADImgRegi2 makeADImgRegi;//2. 이미지 등록 view

    private DlgSelImg mSelDlg; //사진찍기, 앨범 선택 popup

    private Context mContext;

    private ModifyADInfo mModifyInfo;

    private String strADIdx=""; //광고 idx
    private String strADStatus=""; //광고 상태
    private boolean isModify = false; //수정 모드 여부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advertiser_make_ad_main_activity);

        MainTitleBar mainTitleBar = findViewById(R.id.main_title_bar);
        ImageButton ibRefresh = mainTitleBar.findViewById(R.id.ib_refresh);
        ImageButton ibHome = mainTitleBar.findViewById(R.id.ib_home);
        ibRefresh.setVisibility(View.GONE);
        ibHome.setVisibility(View.GONE);

        TitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setTitle(getString(R.string.str_make_ad));

        mContext = this;

        llProgress = (LinearLayout) findViewById(R.id.ll_progress_circle);
        txtDetailInfo = (TextView) findViewById(R.id.txt_ad_detail_info);
        llDetailInfoUnder = (LinearLayout) findViewById(R.id.ll_ad_detail_info_under);
        txtRegiImg = (TextView) findViewById(R.id.txt_regi_img);
        llRegiImgUnder = (LinearLayout) findViewById(R.id.ll_regi_img_under);

        Intent intent = getIntent();
        strADIdx = intent.getStringExtra("AD_IDX");
        strADStatus = intent.getStringExtra("AD_STATUS");
        boolean isModify = strADIdx != null && !strADIdx.isEmpty();
        AppService appService = AppServiceProvider.INSTANCE.getInstance();

        // 코드 리스트 먼저 불러오기
        AppServiceHelper.fetchCodeList(
                appService,
                "R010600",
                list -> {
                    makeADDetail.setCategoryList(list);
                    // 코드 목록 세팅 후 수정일 경우에만 데이터 로딩
                    if (isModify) {
                        loadModifyData(strADIdx);
                    }
                },
                throwable -> Toast.makeText(MakeADMainActivity.this, "카테고리 불러오기 실패", Toast.LENGTH_SHORT).show()
        );



        //1. 세부정보 view
        makeADDetail =  (MakeADDetail1) findViewById(R.id.make_ad_detail);
        makeADDetail.getOnInfoData(mNextInfo);

        //2. 이미지 등록 view
        makeADImgRegi =  (MakeADImgRegi2) findViewById(R.id.make_ad_img_regi);
        ((Button) makeADImgRegi.findViewById(R.id.btn_make_ad_img_registration_pre)).setOnClickListener(this);
        makeADImgRegi.setOnDetailImgClickListener(mDetailClick);
        makeADImgRegi.getOnData(mNextClick);

        checkPermissionAndPickImage();
        checkCameraPermission();

        FrameLayout layoutBG = (FrameLayout) findViewById(R.id.fl_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_make_ad_img_registration_pre) {
            if(!makeADDetail.isShown()) makeADDetail.setVisibility(View.VISIBLE);
            if(makeADImgRegi.isShown()) makeADImgRegi.setVisibility(View.GONE);
       }
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    private ArrayList<String> arrDetailData; //세부정보들
    private String strCategory; //카테고리
    private MakeADDetail1.OnGetInfoData mNextInfo = new MakeADDetail1.OnGetInfoData() {
        @Override
        public void onGetInfoData(ArrayList<String> arrData, String strCategoryItem) {
            if (llProgress != null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            arrDetailData = arrData;
            strCategory = strCategoryItem;
            nextInfo();
        }
    };

    public void nextInfo(){
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);

        if(mModifyInfo!=null) {
            makeADImgRegi.modifyAD(mModifyInfo);
        }

        if (makeADDetail.isShown()) makeADDetail.setVisibility(View.GONE);
        if (!makeADImgRegi.isShown()) makeADImgRegi.setVisibility(View.VISIBLE);

        llRegiImgUnder.setVisibility(View.VISIBLE);
        llDetailInfoUnder.setVisibility(View.GONE);
        txtRegiImg.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
        txtDetailInfo.setBackgroundColor(getResources().getColor(R.color.color_white));

    }

    private MakeADImgRegi2.OnGetData mNextClick = new MakeADImgRegi2.OnGetData() {
        @Override
        public void onGetData(ArrayList<Boolean> arrIsChangeDetailImg, boolean isChangeTitleImg, String strTitle, ArrayList<String> arrDetailImg, String  titleImgId, ArrayList<String> arrDetailImgId) {
            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            DetailImgGetData(arrIsChangeDetailImg, isChangeTitleImg, strTitle, arrDetailImg,titleImgId,arrDetailImgId);
        }
    };

    public void DetailImgGetData(ArrayList<Boolean> arrIsChangeDetailImg, boolean isChangeTitleImg, String strTitle, ArrayList<String> arrDetailImg, String  titleImgId, ArrayList<String> arrDetailImgId){
        //1. 세부정보
        Intent intent = new Intent(MakeADMainActivity.this, MakeADPreviewActivity.class);

        intent.putExtra(makeADDetail.STR_PUT_AD_IDX, strADIdx); //광고 idx
        intent.putExtra(makeADDetail.STR_PUT_AD_NAME, arrDetailData.get(0)); //광고명
        intent.putExtra(makeADDetail.STR_PUT_AD_DETAIL, arrDetailData.get(1)); //상세설명
        intent.putExtra(makeADDetail.STR_PUT_AD_CATEGORY, strCategory);
        intent.putExtra(makeADDetail.STR_PUT_AD_AMOUNT, arrDetailData.get(3)); //광고 할 금액

        //2. 이미지 등록
        if(!strTitle.equals("")){
            intent.putExtra("Title_Img", strTitle);
            intent.putExtra("Title_ImgId", titleImgId);
            intent.putExtra("ChangeTitleImg", isChangeTitleImg);
        }
        if(arrDetailImg!=null && arrDetailImg.size()>0){
            intent.putStringArrayListExtra("Detail_Img", arrDetailImg);
            intent.putStringArrayListExtra("Detail_ImgId", arrDetailImgId);
            intent.putExtra("ChangeDetailImg", arrIsChangeDetailImg);
        }

        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, MAKE_AD_COMPLETE);
    }

    private String strImgKind; // title img OR detail img
    private MakeADImgRegi2.onDetailItemClick mDetailClick = new MakeADImgRegi2.onDetailItemClick() {
        @Override
        public void onDetailImgClick(boolean isImg, String strKind) {
            strImgKind = strKind;
            mSelDlg = new DlgSelImg(MakeADMainActivity.this, isImg, false);
            mSelDlg.setonDismissListener(MakeADMainActivity.this);
            if(mSelDlg!=null && !mSelDlg.isShowing()) mSelDlg.show();
        }
    };

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (mSelDlg.getDelImg()) {
            // 이미지 종류가 타이틀인지 상세인지 구분
            String imageIdToDelete = null;
            if ("Title".equals(strImgKind)) {
                imageIdToDelete = makeADImgRegi.getTitleImgId(); // getter 필요
            } else if ("Detail".equals(strImgKind)) {
                imageIdToDelete = makeADImgRegi.getSelectedDetailImgId(); // getter 필요
            }

            if (imageIdToDelete != null && !imageIdToDelete.isEmpty()) {
                // 서버 삭제 호출
                AppService appService = AppServiceProvider.INSTANCE.getInstance();
                AppServiceHelper.deleteImageById(
                        appService,
                        imageIdToDelete,
                        () -> {
                            makeADImgRegi.DelImg(strImgKind); // 서버 삭제 성공 후 UI 삭제
                            return null;
                        },
                        throwable -> {
                            Toast.makeText(mContext, "서버 이미지 삭제 실패", Toast.LENGTH_SHORT).show();
                            Log.e("ImageDelete", "서버 이미지 삭제 실패", throwable);
                            return null;
                        }
                );
            } else {
                // 로컬에서만 존재하는 이미지인 경우 그냥 삭제
                makeADImgRegi.DelImg(strImgKind);
            }
        }
    }
    private Uri imageUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            Uri cropUri = null;
            Intent intent = null;
            int mRequestCode = -1;
            // Crop한 이미지를 저장할 Path
            File cropFile = null;
            switch (requestCode) {
                case MAKE_AD_COMPLETE:
                    finish();
                    break;
                case DlgSelImg.PICK_FROM_CAMERA:
                    /*
                    intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(mSelDlg.mImageUri, "image/*");

                    try {
                        cropFile = mSelDlg.createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        ex.printStackTrace();
                    }
                    if (cropFile != null) {
                        cropUri = FileProvider.getUriForFile(this,
                                getPackageName() + ".fileprovider",
                                cropFile);
                        //자르기 앱에 cropUri에 대한 권한을 부여합니다.
                        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            grantUriPermission(packageName, cropUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        //grantUriPermission(getPackageName(), cropUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra("output", cropUri);
                        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    intent.putExtra("crop", "true");
                    intent.putExtra("aspectX", 16); // crop 박스의 x축 비율
                    intent.putExtra("aspectY", 9); // crop 박스의 y축 비율
                    mRequestCode = DlgSelImg.CROP_FROM_GALLERY;

                     */
                    if (mSelDlg.mImageUri != null) {
                        Log.d("Camera URI", "Captured URI: " + mSelDlg.mImageUri);
                        startCrop(mSelDlg.mImageUri);
                    }
                    break;
                /*
                case DlgSelImg.CROP_FROM_CAMERA:
                    File file = new File(mSelDlg.mImageUri.getPath());
                    if(file.exists()){
                        file.delete();
                    }
                 */
                case DlgSelImg.PICK_FROM_GALLERY:
                    imageUri = data.getData();
                    Log.d("Gallery URI", "Selected URI: " + imageUri);
                    if (imageUri != null) {
                        Log.d("Gallery URI", "packageName: " +getPackageName());
                        //cropImage(imageUri);
                        startCrop(imageUri);
                    }
                    break;
                case DlgSelImg.CROP_FROM_GALLERY:
                    cropUri = null;
                    if(data != null){
                        cropUri = data.getData();
                    }
                    if(cropUri == null){
                        //이미지 자르기 앱이 data에 아무것도 반환하지 않은 경우
                        //cropUri에 이미지가 저장되었는지 확인
                        cropFile = null;
                        try {
                            cropFile = mSelDlg.createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            ex.printStackTrace();
                        }
                        if(cropFile != null){
                            cropUri = FileProvider.getUriForFile(this,
                                    getPackageName() + ".fileprovider",
                                    cropFile);
                        }
                    }
                    if (cropUri != null) {
                        makeADImgRegi.setImg(cropUri, strImgKind); // 수정된 코드
                    }

                    Log.d("temp","**************MakeADMainActivity onActivityResult CROP_FROM_GALLERY");
                    break;
                case  UCrop.REQUEST_CROP:
                    Uri croppedImageUri = UCrop.getOutput(data);
                    if (croppedImageUri != null) {
                        makeADImgRegi.setImg(croppedImageUri, strImgKind); // 수정된 코드
                    }
                    break;
                case  UCrop.RESULT_ERROR:
                    Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        cropError.printStackTrace();
                        Toast.makeText(this, "크롭 오류 발생: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }

            if(intent!=null && mRequestCode!=-1){
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, mRequestCode);
            }
        }
    }


    private static final int REQUEST_READ_MEDIA_IMAGES = 101;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 102; //
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(mContext,android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions((Activity)mContext,new String[]{android.Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 있는 경우 카메라 앱 실행
            //startCamera();
        }
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (TIRAMISU) 이상
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_MEDIA_IMAGES);
            } else {
                //pickImageFromGallery(); // 권한이 이미 있는 경우 호출
            }
        } else  {
            // Android 12 이하
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE); // 새로운 상수 사용
            } else {
                //pickImageFromGallery(); // 권한이 이미 있는 경우 호출
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //pickImageFromGallery();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 거부되었습니다.1", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //pickImageFromGallery();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 거부되었습니다.2", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //startCamera();
            } else {
                Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    private void startCrop(Uri sourceUri) {
        // 크롭 결과 저장 파일 생성
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "CroppedImage.jpg"));

        // UCrop 옵션 설정
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(90);
        options.setToolbarTitle("이미지 크롭");
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorRPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorRPrimaryDark));
        options.setActiveControlsWidgetColor(ContextCompat.getColor(this, R.color.colorRAccent));

        // UCrop 실행
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1) // 원하는 비율 설정 (1:1)
                .withOptions(options) // 옵션 적용
                .start(this);
    }

    private void loadModifyData(String productId) {
        AdApi adApi = RetrofitProvider.INSTANCE.getRetrofit().create(AdApi.class);
        RemoteRepository repository = new RemoteRepository(adApi);
        AppService appService = new AppService(repository);

        AppServiceHelper.getProductDetailAsync(
                appService,
                Long.parseLong(productId),
                result -> {
                    ModifyADInfo modifyInfo = new ModifyADInfo();
                    modifyInfo.setProductId(result.getProduct().getProductId());
                    modifyInfo.setTitle(result.getProduct().getTitle());
                    modifyInfo.setDescription(result.getProduct().getDescription());
                    modifyInfo.setPrice(result.getProduct().getPrice());
                    modifyInfo.setCategoryMid(result.getProduct().getCategoryMid());

                    List<ProductImageVo> images = result.getImageMetas();
                    for (ProductImageVo vo : images) {
                        if ("1".equals(vo.getRepresent())) {
                            modifyInfo.setStrADTitleImgUrl(vo.getImageUrl());
                            modifyInfo.setaDTitleimageId(vo.getImageId());
                        }
                    }

                    List<String> subUrls = new ArrayList<>();
                    for (ProductImageVo vo : images) {
                        if ("0".equals(vo.getRepresent())) {
                            subUrls.add(vo.getImageUrl());
                        }
                    }

                    if (subUrls.size() > 0) {
                        modifyInfo.setStrADDetailImgUrl1(subUrls.get(0));
                        modifyInfo.setaDDetailimageId1(images.get(1).getImageId());
                    }
                    if (subUrls.size() > 1) {
                        modifyInfo.setStrADDetailImgUrl2(subUrls.get(1));
                        modifyInfo.setaDDetailimageId2(images.get(2).getImageId());
                    }
                    if (subUrls.size() > 2) {
                        modifyInfo.setStrADDetailImgUrl3(subUrls.get(2));
                        modifyInfo.setaDDetailimageId3(images.get(3).getImageId());
                    }

                    // 뷰에 세팅
                    makeADDetail.modifyData(modifyInfo);
                    makeADImgRegi.modifyAD(modifyInfo);
                    return null;
                },
                throwable -> {
                    Log.e("getProductDetail", "데이터 조회 실패", throwable);
                    return null;
                }
        );
    }

}
