package com.whomade.kycarrots.ui.ad.makead

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.dialog.DlgSelImg
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class KtMakeADMainActivity : AppCompatActivity(), View.OnClickListener, DialogInterface.OnDismissListener {

    private lateinit var llProgress: LinearLayout
    private lateinit var txtDetailInfo: TextView
    private lateinit var txtRegiImg: TextView

    private lateinit var makeADDetail: KtMakeADDetailView
    private lateinit var makeADImgRegi: KtMakeADImgRegiView

    private val appService: AppService by lazy { AppServiceProvider.getService() }

    private var strADIdx: String? = null
    private var strADStatus: String? = null

    // 원본처럼 유지
    private var strImgKind: String = "Title"
    private var mSelDlg: DlgSelImg? = null

    private var arrDetailData: ArrayList<String> = arrayListOf()

    private var arrIsChangeDetailImg: ArrayList<Boolean> = arrayListOf()
    private var isChangeTitleImg: Boolean = false
    private var titlePath: String = ""
    private var titleImgId: String = ""
    private var arrDetailImg: ArrayList<String> = arrayListOf()
    private var arrDetailImgId: ArrayList<String> = arrayListOf()

    private val mNextInfo = object : KtMakeADDetailView.OnGetInfoData {
        override fun onGetInfoData(arrData: ArrayList<String>, arrCategory: String) {
            arrDetailData = arrData
            setTabSelected(isDetail = false)
        }
    }

    private val mNextClick = object : KtMakeADImgRegiView.OnGetData {
        override fun onGetData(
            arrIsChangeDetailImg: ArrayList<Boolean>,
            isChangeTitleImg: Boolean,
            titlePath: String,
            arrDetailImg: ArrayList<String>,
            titleImgId: String,
            arrDetailImgId: ArrayList<String>
        ) {
            this@KtMakeADMainActivity.arrIsChangeDetailImg = arrIsChangeDetailImg
            this@KtMakeADMainActivity.isChangeTitleImg = isChangeTitleImg
            this@KtMakeADMainActivity.titlePath = titlePath
            this@KtMakeADMainActivity.arrDetailImg = arrDetailImg
            this@KtMakeADMainActivity.titleImgId = titleImgId
            this@KtMakeADMainActivity.arrDetailImgId = arrDetailImgId

            openPreview()
        }
    }

    private val mDetailClick = object : KtMakeADImgRegiView.OnDetailImgClickListener {
        override fun onDetailImgClick(isImg: Boolean, kind: String) {
            strImgKind = kind
            ensureCameraThenShowChooser(isImg, kind)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.advertiser_make_ad_main_activity)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "상품 등록"
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back) // 너네 리소스에 맞게
        toolbar.setNavigationOnClickListener { finish() }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "상품 등록"


        llProgress = findViewById(R.id.ll_progress_circle)
        txtDetailInfo = findViewById(R.id.txt_ad_detail_info)
        txtRegiImg = findViewById(R.id.txt_regi_img)

        strADIdx = intent.getStringExtra("AD_IDX")
        strADStatus = intent.getStringExtra("AD_STATUS")

        makeADDetail = findViewById(R.id.make_ad_detail)
        makeADImgRegi = findViewById(R.id.make_ad_img_regi)

        makeADDetail.getOnInfoData(mNextInfo)
        makeADImgRegi.getOnData(mNextClick) // ✅ 원본 이름으로 맞춤
        makeADImgRegi.setOnDetailImgClickListener(mDetailClick)

        txtDetailInfo.setOnClickListener(this)
        txtRegiImg.setOnClickListener(this)

        // "이전" 버튼은 Activity에서 탭 전환 처리
        makeADImgRegi.findViewById<Button>(R.id.btn_make_ad_img_registration_pre).setOnClickListener(this)

        makeADDetail.setOnCategorySelectedListener(object : KtMakeADDetailView.OnCategorySelectedListener {
            override fun onCategorySelected(code: String) {
                lifecycleScope.launch {
                    try {
                        val sub = withContext(Dispatchers.IO) { appService.getSCodeList("R010610", code) }
                        makeADDetail.setSubCategoryList(sub)
                    } catch (_: Throwable) {
                        Toast.makeText(this@KtMakeADMainActivity, "카테고리 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        makeADDetail.setOnCitySelectedListener(object : KtMakeADDetailView.OnCitySelectedListener {
            override fun onCitySelected(code: String) {
                lifecycleScope.launch {
                    try {
                        val district = withContext(Dispatchers.IO) { appService.getSCodeList("R010070", code) }
                        makeADDetail.setDistrictList(district)
                    } catch (_: Throwable) {
                        Toast.makeText(this@KtMakeADMainActivity, "지역 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        bindCodes()

        if (!strADIdx.isNullOrBlank()) {
            loadProductDetailForModify(strADIdx!!)
        }

        setTabSelected(isDetail = true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_make_ad_img_registration_pre -> setTabSelected(isDetail = true)
        }
    }

    private fun setTabSelected(isDetail: Boolean) {

        val llDetailInfoUnder = findViewById<LinearLayout>(R.id.ll_ad_detail_info_under)
        val llRegiImgUnder = findViewById<LinearLayout>(R.id.ll_regi_img_under)

        if (isDetail) {
            // ===== 상세정보 탭 활성 =====
            txtDetailInfo.setBackgroundColor(
                ContextCompat.getColor(this, R.color.colorRAccent)
            )
            txtDetailInfo.setTextColor(
                ContextCompat.getColor(this, android.R.color.white)
            )

            txtRegiImg.setBackgroundColor(
                ContextCompat.getColor(this, android.R.color.white)
            )
            txtRegiImg.setTextColor(
                ContextCompat.getColor(this, android.R.color.black)
            )

            llDetailInfoUnder.visibility = View.VISIBLE
            llRegiImgUnder.visibility = View.GONE

            makeADDetail.visibility = View.VISIBLE
            makeADImgRegi.visibility = View.GONE

        } else {
            // ===== 이미지등록 탭 활성 =====
            txtRegiImg.setBackgroundColor(
                ContextCompat.getColor(this, R.color.colorRAccent)
            )
            txtRegiImg.setTextColor(
                ContextCompat.getColor(this, android.R.color.white)
            )

            txtDetailInfo.setBackgroundColor(
                ContextCompat.getColor(this, android.R.color.white)
            )
            txtDetailInfo.setTextColor(
                ContextCompat.getColor(this, android.R.color.black)
            )

            llDetailInfoUnder.visibility = View.GONE
            llRegiImgUnder.visibility = View.VISIBLE

            makeADDetail.visibility = View.GONE
            makeADImgRegi.visibility = View.VISIBLE
        }
    }

    private fun bindCodes() {
        llProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val category = withContext(Dispatchers.IO) { appService.getCodeList("R010610") }
                val unit = withContext(Dispatchers.IO) { appService.getCodeList("R010620") }
                val city = withContext(Dispatchers.IO) { appService.getCodeList("R010070") }

                makeADDetail.setCategoryList(category)
                makeADDetail.setUnitList(unit)
                makeADDetail.setCityList(city)

            } catch (_: Throwable) {
                Toast.makeText(this@KtMakeADMainActivity, "코드 리스트 불러오기 실패", Toast.LENGTH_SHORT).show()
            } finally {
                llProgress.visibility = View.GONE
            }
        }
    }

    private fun loadProductDetailForModify(productId: String) {
        llProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val userNoLong = LoginInfoUtil.getUserNo(this@KtMakeADMainActivity).toLongOrNull() ?: 0L
                val detail = withContext(Dispatchers.IO) { appService.getProductDetail(productId.toLong(), userNoLong) }

                val modifyInfo = KtModifyADInfo().apply {
                    this.productId = detail?.product?.productId
                    this.userNo = detail?.product?.userNo
                    this.title = detail?.product?.title
                    this.description = detail?.product?.description
                    this.price = detail?.product?.price
                    this.categoryGroup = detail?.product?.categoryGroup
                    this.categoryMid = detail?.product?.categoryMid
                    this.categoryScls = detail?.product?.categoryScls
                    this.saleStatus = detail?.product?.saleStatus
                    this.areaGroup = detail?.product?.areaGroup
                    this.areaMid = detail?.product?.areaMid
                    this.areaScls = detail?.product?.areaScls
                    this.quantity = detail?.product?.quantity
                    this.unitGroup = detail?.product?.unitGroup
                    this.unitCode = detail?.product?.unitCode
                    this.desiredShippingDate = detail?.product?.desiredShippingDate
                }

                val images: List<ProductImageVo> = detail?.imageMetas ?: emptyList()
                val titleImg = images.firstOrNull { it.represent == "1" }
                modifyInfo.strADTitleImgUrl = titleImg?.imageUrl
                modifyInfo.aDTitleimageId = titleImg?.imageId

                val sub = images.filter { it.represent == "0" }.mapNotNull { it.imageUrl }
                if (sub.isNotEmpty()) modifyInfo.strADDetailImgUrl1 = sub.getOrNull(0)
                if (sub.size > 1) modifyInfo.strADDetailImgUrl2 = sub.getOrNull(1)
                if (sub.size > 2) modifyInfo.strADDetailImgUrl3 = sub.getOrNull(2)

                makeADDetail.modifyData(modifyInfo)
                makeADImgRegi.modifyAD(modifyInfo) // ✅ 이제 컴파일 됨
            } catch (t: Throwable) {
                Toast.makeText(this@KtMakeADMainActivity, "상세 조회 실패", Toast.LENGTH_SHORT).show()
                Log.e("MakeAD", "getProductDetail failed", t)
            } finally {
                llProgress.visibility = View.GONE
            }
        }
    }

    private fun openPreview() {
        val intent = Intent(this, MakeADPreviewActivity::class.java)

        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_IDX, strADIdx)
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_NAME, arrDetailData.getOrNull(0).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_QUANTITY, arrDetailData.getOrNull(1).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_UNIT_CODE, arrDetailData.getOrNull(2).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_AMOUNT, arrDetailData.getOrNull(3).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_DESIRED_SHIPPING_DATE, arrDetailData.getOrNull(4).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_DETAIL, arrDetailData.getOrNull(5).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_CATEGORY_MID, arrDetailData.getOrNull(6).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_CATEGORY_SCLS, arrDetailData.getOrNull(7).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_AREA_MID, arrDetailData.getOrNull(8).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_AREA_SCLS, arrDetailData.getOrNull(9).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_UNIT_CODEMM, arrDetailData.getOrNull(10).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_CATEGORY_MIDNM, arrDetailData.getOrNull(11).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_CATEGORY_SCLSNM, arrDetailData.getOrNull(12).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_AREA_MIDNM, arrDetailData.getOrNull(13).orEmpty())
        intent.putExtra(KtMakeADDetailView.STR_PUT_AD_AREA_SCLSNM, arrDetailData.getOrNull(14).orEmpty())

        intent.putExtra("Title_Img", titlePath)
        intent.putExtra("Title_ImgId", titleImgId)
        intent.putExtra("ChangeTitleImg", isChangeTitleImg)

        intent.putStringArrayListExtra("Detail_Img", arrDetailImg)
        intent.putStringArrayListExtra("Detail_ImgId", arrDetailImgId)
        intent.putExtra("ChangeDetailImg", arrIsChangeDetailImg.toBooleanArray())

        startActivityForResult(intent, 999)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        // 원본: 삭제 플래그면 DelImg 호출
        val dlg = mSelDlg ?: return
        if (dlg.getDelImg()) {
            // 서버삭제 로직은 기존대로 붙이면 되고, 일단 UI삭제만:
            makeADImgRegi.DelImg(strImgKind)
        }
    }

    /**
     * ✅ 원본처럼 "촬영/앨범" 팝업(DlgSelImg)으로 복원
     */
    private fun ensureCameraThenShowChooser(isImg: Boolean, kind: String) {
        // 카메라만 권한 체크(원본 동일)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 2002)
            return
        }

        mSelDlg = DlgSelImg(this, isImg, false).apply {
            setonDismissListener(this@KtMakeADMainActivity)
            if (!isShowing) show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 권한 승인 후 다시 팝업
            mSelDlg = DlgSelImg(this, true, false).apply {
                setonDismissListener(this@KtMakeADMainActivity)
                if (!isShowing) show()
            }
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "CroppedImage_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(90)
            setToolbarTitle("이미지 크롭")
        }

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1080, 1080)
            .withOptions(options)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            DlgSelImg.PICK_FROM_CAMERA -> {
                val dlg = mSelDlg
                val uri = dlg?.getImageUri()
                if (uri != null) startCrop(uri)
            }

            DlgSelImg.PICK_FROM_GALLERY -> {
                val uri = data?.data
                if (uri != null) startCrop(uri)
            }

            UCrop.REQUEST_CROP -> {
                val cropped = UCrop.getOutput(data ?: return) ?: return
                // ✅ 원본처럼 View에 uri로 전달
                makeADImgRegi.setImg(cropped, strImgKind)
            }

            999 -> {
                val ok = data?.getBooleanExtra("register_result", false) ?: false
                if (ok) finish()
            }
        }
    }
}
