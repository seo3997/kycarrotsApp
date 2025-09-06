package com.whomade.kycarrots.ui.ad.makead

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.whomade.kycarrots.R
import com.whomade.kycarrots.common.AppServiceProvider
import com.whomade.kycarrots.data.model.ProductImageVo
import com.whomade.kycarrots.data.model.ProductVo
import com.whomade.kycarrots.domain.Helper.AppServiceHelper
import com.whomade.kycarrots.domain.service.AppService
import com.whomade.kycarrots.ui.common.ImageLoader
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import java.io.ByteArrayOutputStream
import java.io.File

class MakeADPreviewActivity : AppCompatActivity(), View.OnClickListener {

    // 로딩 오버레이
    private lateinit var llProgress: LinearLayout

    // 상단 대표 이미지 (CollapsingToolbar의 배경)
    private lateinit var ivBackdrop: ImageView

    // 프리뷰 텍스트들
    private lateinit var tvDescription: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvShippingDate: TextView
    private lateinit var tvQuantityUnit: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvRegion: TextView

    // 서브 이미지 3장 슬롯
    private lateinit var ivSub1: ImageView
    private lateinit var ivSub2: ImageView
    private lateinit var ivSub3: ImageView

    // 등록 버튼
    private lateinit var btnRegister: Button

    // 전달받는 데이터
    private var strADIdx: String? = null
    private var strADName: String? = null
    private var strADQuantity: String? = null
    private var strADUnitCode: String? = null
    private var strADUnitCodeNm: String? = null
    private var strADAmount: String? = null
    private var strADDesiredShippingDate: String? = null
    private var strADDetail: String? = null
    private var strADCategory: String? = null
    private var strADCategoryScls: String? = null
    private var strADAreaMid: String? = null
    private var strADAreaScls: String? = null
    private var strADCategoryNm: String? = null
    private var strADCategorySclsNm: String? = null
    private var strADAreaMidNm: String? = null
    private var strADAreaSclsNm: String? = null

    private var strTitleImgPath: String = ""
    private var strTitleImgId: String = ""
    private var isChangeTitleImg: Boolean = false

    private val arrDetailImg = ArrayList<String>()
    private val arrDetailImgId = ArrayList<String>()
    private var arrIsChangeDetailImg: BooleanArray = BooleanArray(0)
    private lateinit var imageCardView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: 레이아웃 파일명 확인. 필요시 변경
        setContentView(R.layout.acitivty_ad_preview)

        bindViews()
        pullIntentExtrasCompat()
        bindDataToViews()
        renderImages()
    }

    private fun bindViews() {
        llProgress = findViewById(R.id.ll_progress_circle)

        ivBackdrop = findViewById(R.id.backdrop)

        tvDescription = findViewById(R.id.product_description)
        tvPrice = findViewById(R.id.product_price)
        tvShippingDate = findViewById(R.id.product_shipping_date)
        tvQuantityUnit = findViewById(R.id.product_quantity_unit)
        tvCategory = findViewById(R.id.product_category_name)
        tvRegion = findViewById(R.id.product_region_name)
        // NEW: 서브 이미지 카드 루트
        imageCardView = findViewById(R.id.image_card_view)
        ivSub1 = findViewById(R.id.image_sub_1)
        ivSub2 = findViewById(R.id.image_sub_2)
        ivSub3 = findViewById(R.id.image_sub_3)

        btnRegister = findViewById(R.id.btn_edit_product) // XML에서 "상품 등록"
        btnRegister.setOnClickListener(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()   // 또는 finish()
        }
    }

    /**
     * 인텐트 파라미터 호환 처리
     */
    private fun pullIntentExtrasCompat() {
        intent?.extras?.let { b ->
            strADIdx = b.getString(MakeADDetail1.STR_PUT_AD_IDX)
            strADName = b.getString(MakeADDetail1.STR_PUT_AD_NAME)
            strADQuantity = b.getString(MakeADDetail1.STR_PUT_AD_QUANTITY)
            strADUnitCode = b.getString(MakeADDetail1.STR_PUT_AD_UNIT_CODE)
            strADUnitCodeNm = b.getString(MakeADDetail1.STR_PUT_AD_UNIT_CODEMM)
            strADAmount = b.getString(MakeADDetail1.STR_PUT_AD_AMOUNT)
            strADDesiredShippingDate = b.getString(MakeADDetail1.STR_PUT_AD_DESIRED_SHIPPING_DATE)
            strADDetail = b.getString(MakeADDetail1.STR_PUT_AD_DETAIL)
            strADCategory = b.getString(MakeADDetail1.STR_PUT_AD_CATEGORY_MID)
            strADCategoryScls = b.getString(MakeADDetail1.STR_PUT_AD_CATEGORY_SCLS)
            strADAreaMid = b.getString(MakeADDetail1.STR_PUT_AD_AREA_MID)
            strADAreaScls = b.getString(MakeADDetail1.STR_PUT_AD_AREA_SCLS)
            strADCategoryNm = b.getString(MakeADDetail1.STR_PUT_AD_CATEGORY_MIDNM)
            strADCategorySclsNm = b.getString(MakeADDetail1.STR_PUT_AD_CATEGORY_SCLSNM)
            strADAreaMidNm = b.getString(MakeADDetail1.STR_PUT_AD_AREA_MIDNM)
            strADAreaSclsNm = b.getString(MakeADDetail1.STR_PUT_AD_AREA_SCLSNM)

            // 타이틀 이미지
            strTitleImgPath = b.getString("Title_Img") ?: b.getString("TITLE_IMG") ?: ""
            strTitleImgId = b.getString("Title_ImgId") ?: b.getString("TITLE_IMGID") ?: ""
            isChangeTitleImg = if (b.containsKey("ChangeTitleImg")) {
                b.getBoolean("ChangeTitleImg")
            } else {
                b.getBoolean("CHANGE_TITLE_IMG", false)
            }

            // 상세 이미지
            (b.getStringArrayList("Detail_Img") ?: b.getStringArrayList("DTAIL_IMG"))?.let {
                arrDetailImg.addAll(it)
            }
            (b.getStringArrayList("Detail_ImgId") ?: b.getStringArrayList("DTAIL_IMGID"))?.let {
                arrDetailImgId.addAll(it)
            }

            // 변경 플래그(boolean[] 또는 ArrayList<Boolean>)
            arrIsChangeDetailImg =
                if (b.containsKey("ChangeDetailImg")) {
                    toBooleanArray(b.get("ChangeDetailImg"))
                } else {
                    toBooleanArray(b.get("CHANGE_DTAIL_IMG"))
                } ?: BooleanArray(arrDetailImg.size) { false }
        }
    }

    private fun toBooleanArray(any: Any?): BooleanArray? {
        return when (any) {
            is BooleanArray -> any
            is ArrayList<*> -> {
                val out = BooleanArray(any.size)
                for (i in any.indices) {
                    out[i] = (any[i] as? Boolean) ?: false
                }
                out
            }
            else -> null
        }
    }

    private fun bindDataToViews() {
        // 설명/가격/출하일/수량·단위/카테고리/지역 표시
        val collapsingToolbar: CollapsingToolbarLayout = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = strADName


        tvDescription.text = strADDetail.orEmpty()
        val priceLong = strADAmount?.toDoubleOrNull()?.toLong() ?: 0L
        val formattedPrice = String.format("%,d원", priceLong)
        tvPrice.text = "가격: $formattedPrice"

        tvShippingDate.text = "희망출하일: ${strADDesiredShippingDate.orEmpty()}"
        tvQuantityUnit.text = "수량: ${strADQuantity.orEmpty()} ${strADUnitCodeNm.orEmpty()}"


        tvCategory.text = "카테고리: ${strADCategoryNm.orEmpty()} / ${strADCategorySclsNm.orEmpty()}"
        tvRegion.text = "지역: ${strADAreaMidNm.orEmpty()} / ${strADAreaSclsNm.orEmpty()}"
    }

    private fun renderImages() {
        // 대표 이미지
        strTitleImgPath = strTitleImgPath.replace("\\", "//")
        if (isChangeTitleImg && strTitleImgPath.isNotBlank()) {
            val bmp = decodeSampledPreviewBitmapFromPath(
                strTitleImgPath,
                resources.getString(R.string.str_ad_char_w).toInt(),
                resources.getString(R.string.str_ad_h).toInt()
            )
            ivBackdrop.setImageBitmap(bmp)
        } else {
            // 원격 URL 등은 로더 사용
            llProgress.isVisible = true
            try {
                // ImageLoader 시그니처가 (ctx, url, imageView, progressBar) 라면 아래처럼:
                ImageLoader.loadImage(this, strTitleImgPath, ivBackdrop, null)
            } catch (e: Throwable) {
                // 오버로드가 다르면 progress 없는 버전 시도
                try {
                    val m = ImageLoader::class.java.getDeclaredMethods()
                        .firstOrNull { it.name == "loadImage" && it.parameterTypes.size == 3 }
                    if (m != null) {
                        @Suppress("UNCHECKED_CAST")
                        m.invoke(null, this, strTitleImgPath, ivBackdrop)
                    } else {
                        Log.w("MakeADPreview", "ImageLoader.loadImage 오버로드를 찾지 못했음")
                    }
                } catch (e2: Throwable) {
                    Log.e("MakeADPreview", "대표 이미지 로딩 실패", e2)
                }
            } finally {
                llProgress.isVisible = false
            }
        }

        // --- 서브 이미지: 없으면 카드 숨김 ---
        imageCardView.visibility = View.GONE
        ivSub1.visibility = View.GONE
        ivSub2.visibility = View.GONE
        ivSub3.visibility = View.GONE

        if (arrDetailImg.isEmpty()) return  // 하나도 없으면 그대로 종료

        val slots = listOf(ivSub1, ivSub2, ivSub3)
        val max = minOf(arrDetailImg.size, slots.size)

        var shownCount = 0
        for (i in 0 until max) {
            val target = slots[i]
            val isLocalChanged = arrIsChangeDetailImg.getOrNull(i) == true
            val pathOrUrl = arrDetailImg[i].replace("\\", "//")

            try {
                if (isLocalChanged) {
                    val bmp = decodeSampledPreviewBitmapFromPath(
                        pathOrUrl,
                        resources.getString(R.string.str_ad_char_w).toInt(),
                        resources.getString(R.string.str_ad_h).toInt()
                    )
                    target.setImageBitmap(bmp)
                } else {
                    ImageLoader.loadImage(this, pathOrUrl, target, null)
                }
                target.visibility = View.VISIBLE
                shownCount++
            } catch (t: Throwable) {
                Log.e("MakeADPreview", "상세 이미지 로딩 실패 idx=$i", t)
                target.visibility = View.GONE
            }
        }

        // 최소 1장이라도 성공했을 때만 카드 보이기
        imageCardView.visibility = if (shownCount > 0) View.VISIBLE else View.GONE
    }

    /** 등록 버튼 */
    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_edit_product) {
            dataRequest()
        }
    }

    private fun dataRequest() {
        llProgress.isVisible = true

        val userNo = LoginInfoUtil.getUserNo(this)
        val SYSTEM_TYPE = com.whomade.kycarrots.common.Constants.SYSTEM_TYPE
        val saleStatus = if (SYSTEM_TYPE == 1) "1" else "0" // 1: 판매중, 0: 승인대기

        val productVo = ProductVo(
            strADIdx,                         // productId
            userNo,                           // userNo
            strADName.orEmpty(),              // title
            strADDetail.orEmpty(),            // description
            strADAmount.orEmpty(),            // price
            "R010610",          // categoryGroup
            strADCategory.orEmpty(),          // categoryMid
            strADCategoryScls.orEmpty(),      // categoryScls
            saleStatus,                       // saleStatus
            "R010070",             // cityGroup
            strADAreaMid.orEmpty(),           // areaMid
            strADAreaScls.orEmpty(),          // areaScls
            strADQuantity.orEmpty(),          // quantity
            "R010620",              // unitGroup
            strADUnitCode.orEmpty(),          // unitCode
            strADDesiredShippingDate.orEmpty(), // desiredShippingDate
            userNo, "", userNo, "", ""        // registerNo, registDt, updusrNo, updtDt, imageUrl
        )

        // 이미지 메타 + 파일
        val detailFiles = ArrayList<File>()
        val imageMetaList = ArrayList<ProductImageVo>()

        // 상세 이미지
        for (i in arrDetailImg.indices) {
            if (arrIsChangeDetailImg.getOrNull(i) == true) {
                val file = File(arrDetailImg[i])
                if (file.exists()) {
                    detailFiles.add(file)
                }
                val imageId = arrDetailImgId.getOrNull(i) ?: ""
                imageMetaList.add(
                    ProductImageVo(
                        imageId,    // imageId (수정 모드면 값 있을 수 있음)
                        null,       // productId
                        "1",        // imageCd
                        null,       // imageUrl
                        null,       // imageName
                        "0",        // represent (상세는 0)
                        null, null, null,
                        userNo, null, userNo, null
                    )
                )
            }
        }

        // 타이틀 이미지 (대표=1)
        if (isChangeTitleImg && strTitleImgPath.isNotBlank()) {
            val titleFile = File(strTitleImgPath)
            if (titleFile.exists()) {
                detailFiles.add(0, titleFile)
                imageMetaList.add(
                    0,
                    ProductImageVo(
                        strTitleImgId, // imageId (수정시)
                        null,
                        "1",
                        null,
                        null,
                        "1",          // 대표
                        null, null, null,
                        userNo, null, userNo, null
                    )
                )
            }
        }

        val appService: AppService = AppServiceProvider.instance

        val onFinally: () -> Unit = {
            llProgress.isVisible = false
        }

        if (productVo.productId.isNullOrEmpty()) {
            // 신규 등록
            AppServiceHelper.registerAdvertise(
                appService,
                productVo,
                imageMetaList,
                detailFiles,
                {
                    onFinally()
                    Toast.makeText(this, getString(R.string.str_ad_regi_success), Toast.LENGTH_SHORT).show()
                    finishAdd()
                    null
                },
                { t ->
                    onFinally()
                    Toast.makeText(this, getString(R.string.str_http_error), Toast.LENGTH_SHORT).show()
                    Log.e("registerAdvertise", "등록 실패", t)
                    null
                }
            )
        } else {
            // 수정
            AppServiceHelper.updateAdvertise(
                appService,
                productVo,
                imageMetaList,
                detailFiles,
                {
                    onFinally()
                    Toast.makeText(this, "광고 수정 성공", Toast.LENGTH_SHORT).show()
                    finishAdd()
                    null
                },
                { t ->
                    onFinally()
                    Toast.makeText(this, "광고 수정 실패", Toast.LENGTH_SHORT).show()
                    Log.e("updateAdvertise", "수정 실패", t)
                    null
                }
            )
        }
    }

    private fun finishAdd() {
        val result = Intent().putExtra("register_result", true)
        setResult(RESULT_OK, result)
        finish()
    }

    // ====== 이미지 유틸 ======
    private fun decodeSampledPreviewBitmapFromPath(path: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val opt = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opt)

        opt.inJustDecodeBounds = false
        var src = BitmapFactory.decodeFile(path, opt)
        src = Bitmap.createScaledBitmap(src, reqWidth, reqHeight, false)

        val bos = ByteArrayOutputStream()
        src.compress(Bitmap.CompressFormat.PNG, 100, bos)
        return src
    }


}
