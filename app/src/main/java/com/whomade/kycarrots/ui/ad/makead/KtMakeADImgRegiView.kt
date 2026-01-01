package com.whomade.kycarrots.ui.ad.makead

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.whomade.kycarrots.R
import com.whomade.kycarrots.ui.common.ImageLoader
import com.whomade.kycarrots.ui.dialog.DlgBtnActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale

/**
 * MakeADImgRegi2.java -> Kotlin (원본 동작 유지)
 */
class KtMakeADImgRegiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), View.OnClickListener {

    interface OnDetailImgClickListener {
        fun onDetailImgClick(isImg: Boolean, strKind: String)
    }

    interface OnGetData {
        fun onGetData(
            arrIsChangeDetailImg: ArrayList<Boolean>,
            isChangeTitleImg: Boolean,
            strTitle: String,
            arrDetailImg: ArrayList<String>,
            titleImgId: String,
            arrDetailImgId: ArrayList<String>
        )
    }

    private var mClick: OnDetailImgClickListener? = null
    private var mGetData: OnGetData? = null

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    private lateinit var svDetailImg: ScrollView
    private lateinit var llMakeDetailImg: LinearLayout
    private lateinit var btnDetailImgAdd: Button

    private lateinit var llTitleImg: LinearLayout          // 타이틀 placeholder
    private lateinit var ivTitle: ImageView                // 타이틀 실제 이미지
    private lateinit var pbTitleImg: ProgressBar
    private lateinit var ivDetailTitle: ImageView          // + 아이콘
    private lateinit var txtDetailTitle: TextView          // "타이틀 이미지"

    private val DETAIL_IMG_ADD_MAX = 3
    private val STR_TITLE_IMG = "Title"
    private val STR_DETAIL_IMG = "Detail"

    private var imageFilePath: String? = null

    // 원본처럼 Map으로 관리
    private val viewsMap = LinkedHashMap<String, View>()
    private val isChangedMap = LinkedHashMap<String, Boolean>()
    private val imagePathMap = LinkedHashMap<String, String>()
    private val imageIdMap = LinkedHashMap<String, String>()
    private var imgCounter = 0

    // 현재 선택된 detail item
    private var selView: View? = null
    private var selKey: String? = null

    // 타이틀 이미지 상태
    private var strTitleImgPath: String = ""
    private var strTitleImgId: String = ""
    private var isChangeTitleImg: Boolean = false

    // ✅ Kotlin에서 "Variable must be initialized" 방지: View.OnClickListener로 명시
    private val mDetailImgClick = View.OnClickListener { v ->
        when (v.id) {
            R.id.ll_detail_title_img, R.id.iv_detail_title_img -> {
                // 원본: txtDetailTitle 보이면 아직 이미지 없음
                val isImg = !txtDetailTitle.isShown
                mClick?.onDetailImgClick(isImg, STR_TITLE_IMG)
            }

            else -> {
                val key = v.tag as? String ?: return@OnClickListener
                val ro = viewsMap[key] as? ViewGroup ?: return@OnClickListener

                selView = ro
                selKey = key

                // 원본: iv_img_ad_detail 가 안 보이면 이미지가 선택된 상태
                val isImg = !ro.findViewById<View>(R.id.iv_img_ad_detail).isShown
                mClick?.onDetailImgClick(isImg, STR_DETAIL_IMG)
            }
        }
    }

    init {
        orientation = VERTICAL
        inflater.inflate(R.layout.advertiser_make_ad_img_registration, this, true)

        btnDetailImgAdd = findViewById(R.id.btn_detail_img_add)
        btnDetailImgAdd.setOnClickListener(this)

        llMakeDetailImg = findViewById(R.id.ll_make_ad_detail_img)

        llTitleImg = findViewById(R.id.ll_detail_title_img)
        ivTitle = findViewById(R.id.iv_detail_title_img)
        pbTitleImg = findViewById(R.id.pb_title_img)
        ivDetailTitle = findViewById(R.id.iv_img_title)
        txtDetailTitle = findViewById(R.id.txt_title_img)

        svDetailImg = findViewById(R.id.sv_detail_img)

        // ✅ 타이틀 영역 클릭
        llTitleImg.isClickable = true
        llTitleImg.isFocusable = true
        llTitleImg.setOnClickListener(mDetailImgClick)

        // ✅ 서버에서 타이틀 이미지 로드 후에는 ivTitle이 보이므로 ivTitle도 클릭 걸어야 함
        ivTitle.isClickable = true
        ivTitle.isFocusable = true
        ivTitle.setOnClickListener(mDetailImgClick)

        // 다음 버튼은 View 내부에서 처리(원본과 동일)
        findViewById<Button>(R.id.btn_make_ad_img_registration_next).setOnClickListener {
            setData()
        }
    }

    fun setOnDetailImgClickListener(listener: OnDetailImgClickListener) {
        mClick = listener
    }

    // 원본: getOnData()
    fun getOnData(getData: OnGetData) {
        mGetData = getData
    }

    fun getTitleImgId(): String = strTitleImgId
    fun getSelectedDetailImgId(): String = selKey?.let { imageIdMap[it].orEmpty() }.orEmpty()

    override fun onClick(v: View?) {
        if (v?.id == R.id.btn_detail_img_add) {
            detailAdd("", "")
        }
    }

    /**
     * 수정 모드(서버 이미지) 세팅
     */
    fun modifyAD(data: KtModifyADInfo) {
        // 타이틀
        strTitleImgPath = (data.strADTitleImgUrl ?: "").replace("\\", "//")
        strTitleImgId = data.aDTitleimageId.orEmpty()
        isChangeTitleImg = false

        if (strTitleImgPath.isNotBlank()) {
            llTitleImg.visibility = View.GONE
            ivTitle.visibility = View.VISIBLE
            pbTitleImg.visibility = View.VISIBLE

            // ✅ RequestListener 시그니처 문제 회피: DrawableImageViewTarget 사용
            Glide.with(context)
                .load(strTitleImgPath)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(object : DrawableImageViewTarget(ivTitle) {
                    override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        pbTitleImg.visibility = View.GONE
                    }

                    override fun onResourceReady(
                        resource: android.graphics.drawable.Drawable,
                        transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                    ) {
                        super.onResourceReady(resource, transition)
                        pbTitleImg.visibility = View.GONE
                    }
                })
        }

        // 상세 1~3
        val urls = listOf(data.strADDetailImgUrl1, data.strADDetailImgUrl2, data.strADDetailImgUrl3)
        val ids = listOf(data.aDDetailimageId1, data.aDDetailimageId2, data.aDDetailimageId3)

        urls.forEachIndexed { idx, url ->
            val u = url.orEmpty()
            if (u.isNotBlank()) {
                detailAdd(u.replace("\\", "//"), ids[idx].orEmpty())
            }
        }
    }

    /**
     * 광고 세부이미지 추가 (원본 DetailAdd)
     */
    private fun detailAdd(strUrl: String, imageId: String) {
        val view = inflater.inflate(R.layout.advertiser_make_ad_detail_img_add, llMakeDetailImg, false)

        val imgKey = "IMG_${imgCounter++}"
        view.tag = imgKey

        // ✅ "클릭이 안 먹는" 문제 대부분이 여기서 발생
        // - 자식뷰가 터치를 먹으면 parent click이 안 들어옴
        // - 그래서 root + 주요 child에도 동일 리스너를 건다
        view.isClickable = true
        view.isFocusable = true
        view.setOnClickListener(mDetailImgClick)

        // child들도 tag 공유 + 클릭 연결
        view.findViewById<View>(R.id.ll_detail_img)?.apply {
            tag = imgKey
            isClickable = true
            isFocusable = true
            setOnClickListener(mDetailImgClick)
        }
        view.findViewById<View>(R.id.iv_detail_img)?.apply {
            tag = imgKey
            isClickable = true
            isFocusable = true
            setOnClickListener(mDetailImgClick)
        }
        view.findViewById<View>(R.id.iv_img_ad_detail)?.apply {
            tag = imgKey
            isClickable = true
            isFocusable = true
            setOnClickListener(mDetailImgClick)
        }

        val llDetailImg = view.findViewById<LinearLayout>(R.id.ll_detail_img)

        if (strUrl.isNotBlank()) {
            llDetailImg.visibility = View.GONE

            val ivDetailImg = view.findViewById<ImageView>(R.id.iv_detail_img)
            ivDetailImg.visibility = View.VISIBLE

            val pbDetailImg = view.findViewById<ProgressBar>(R.id.pb_detail_img)
            pbDetailImg?.visibility = View.VISIBLE

            ImageLoader.loadImage(context, strUrl, ivDetailImg, pbDetailImg)
        }

        llMakeDetailImg.addView(view)

        viewsMap[imgKey] = view
        imagePathMap[imgKey] = strUrl
        isChangedMap[imgKey] = false
        imageIdMap[imgKey] = imageId

        if (viewsMap.size >= DETAIL_IMG_ADD_MAX) {
            btnDetailImgAdd.visibility = View.GONE
        }

        svDetailImg.invalidate()
        svDetailImg.requestLayout()
    }

    /**
     * 원본 setImg(Uri, kind) 공개 API (MainActivity에서 UCrop 결과로 호출)
     */
    fun setImg(fileUri: Uri, strKind: String) {
        val bmp = uriToScaledBitmap(fileUri) ?: return
        val file = try {
            createImageFile()
        } catch (e: IOException) {
            return
        }

        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }

        setImgPath(file.absolutePath, strKind)
    }

    /**
     * 원본 DelImg(kind)
     */
    fun DelImg(strKind: String) {
        if (strKind == STR_TITLE_IMG) {
            // 배경 원복
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llTitleImg.background = resources.getDrawable(R.drawable.img, null)
            } else {
                @Suppress("DEPRECATION")
                llTitleImg.setBackgroundDrawable(resources.getDrawable(R.drawable.img))
            }

            strTitleImgPath = ""
            isChangeTitleImg = false

            llTitleImg.visibility = View.VISIBLE
            ivTitle.visibility = View.GONE
            ivDetailTitle.visibility = View.VISIBLE
            txtDetailTitle.visibility = View.VISIBLE
        } else if (strKind == STR_DETAIL_IMG) {
            val key = selKey ?: return
            val viewToRemove = viewsMap[key] ?: return

            llMakeDetailImg.removeView(viewToRemove)

            viewsMap.remove(key)
            imagePathMap.remove(key)
            isChangedMap.remove(key)
            imageIdMap.remove(key)

            selKey = null
            selView = null

            if (viewsMap.size < DETAIL_IMG_ADD_MAX) {
                btnDetailImgAdd.visibility = View.VISIBLE
            }

            svDetailImg.invalidate()
            svDetailImg.requestLayout()
        }
    }

    /**
     * 원본 setData()
     */
    fun setData() {
        if (strTitleImgPath.isBlank()) {
            context.startActivity(Intent(context, DlgBtnActivity::class.java).apply {
                putExtra("BtnDlgMsg", resources.getString(R.string.str_ad_title_img_err))
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
            return
        }

        val arrDetail = ArrayList<String>()
        val arrIsChangeDetail = ArrayList<Boolean>()
        val arrDetailImgId = ArrayList<String>()

        for ((key, view) in viewsMap) {
            val ro = view as ViewGroup
            if (!ro.findViewById<View>(R.id.iv_img_ad_detail).isShown) {
                arrDetail.add(imagePathMap[key].orEmpty())
                arrIsChangeDetail.add(isChangedMap[key] == true)
                arrDetailImgId.add(imageIdMap[key].orEmpty())
            }
        }

        mGetData?.onGetData(
            arrIsChangeDetail,
            isChangeTitleImg,
            strTitleImgPath,
            arrDetail,
            strTitleImgId,
            arrDetailImgId
        )
    }

    /**
     * 내부: path 적용 (원본 setImg(String path, kind))
     */
    private fun setImgPath(path: String, kind: String) {
        val bmp = decodeScaledBitmapFromPath(
            path,
            resources.getString(R.string.str_ad_char_w).toInt(),
            resources.getString(R.string.str_ad_h).toInt()
        )

        if (kind == STR_TITLE_IMG) {
            ivDetailTitle.visibility = View.GONE
            txtDetailTitle.visibility = View.GONE
            ivTitle.visibility = View.GONE
            llTitleImg.visibility = View.VISIBLE

            strTitleImgPath = path
            isChangeTitleImg = true

            val bg = BitmapDrawable(resources, bmp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llTitleImg.background = bg
            } else {
                @Suppress("DEPRECATION")
                llTitleImg.setBackgroundDrawable(bg)
            }
        } else if (kind == STR_DETAIL_IMG) {
            val selected = selView ?: return
            val key = selKey ?: return

            val llDetailImg = selected.findViewById<LinearLayout>(R.id.ll_detail_img)
            llDetailImg.visibility = View.VISIBLE

            selected.findViewById<View>(R.id.iv_detail_img).visibility = View.GONE
            selected.findViewById<View>(R.id.iv_img_ad_detail).visibility = View.GONE
            selected.findViewById<View>(R.id.txt_detail).visibility = View.GONE

            isChangedMap[key] = true
            imagePathMap[key] = path

            val bg = BitmapDrawable(resources, bmp)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                llDetailImg.background = bg
            } else {
                @Suppress("DEPRECATION")
                llDetailImg.setBackgroundDrawable(bg)
            }
        }
    }

    private fun uriToScaledBitmap(uri: Uri): Bitmap? {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            val decoded = input.use { BitmapFactory.decodeStream(it) } ?: return null
            Bitmap.createScaledBitmap(
                decoded,
                resources.getString(R.string.str_ad_char_w).toInt(),
                resources.getString(R.string.str_ad_h).toInt(),
                false
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeScaledBitmapFromPath(path: String, reqWidth: Int, reqHeight: Int): Bitmap {
        val opt = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opt)

        opt.inJustDecodeBounds = false
        var src = BitmapFactory.decodeFile(path, opt)
        src = Bitmap.createScaledBitmap(src, reqWidth, reqHeight, false)

        ByteArrayOutputStream().use { bos ->
            src.compress(Bitmap.CompressFormat.PNG, 100, bos)
        }
        return src
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IOException("저장 디렉토리를 찾을 수 없습니다.")
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        imageFilePath = image.absolutePath
        return image
    }
}
