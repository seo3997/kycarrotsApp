package com.whomade.kycarrots.ui.ad.adreview

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.whomade.kycarrots.R
import com.whomade.kycarrots.data.model.ReviewVo
import com.whomade.kycarrots.domain.service.AppServiceProvider
import com.whomade.kycarrots.ui.common.LoginInfoUtil
import com.whomade.kycarrots.ui.common.TokenUtil
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AdReviewWriteActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var etContents: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var ivAddImage: ImageView
    private lateinit var llImageList: LinearLayout
    private lateinit var loadingLayout: View
    
    private var productId: Long = 0
    private var reviewId: String? = null
    data class ReviewImage(val file: File? = null, val url: String? = null)
    private val imageList = mutableListOf<ReviewImage>()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (imageList.size >= 3) {
                Toast.makeText(this, "사진은 최대 3개까지만 첨부 가능합니다.", Toast.LENGTH_SHORT).show()
                return@let
            }
            val file = getFileFromUri(it)
            if (file != null) {
                imageList.add(ReviewImage(file = file))
                refreshImageInterface()
            } else {
                Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_review_write)

        productId = intent.getLongExtra("productId", 0L)
        reviewId = intent.getStringExtra("reviewId")
        val existingFilePaths = intent.getStringExtra("filePaths")
        
        if (productId == 0L) {
            Toast.makeText(this, "상품 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (reviewId != null) "상품 리뷰 수정" else "상품 리뷰 작성"

        ratingBar = findViewById(R.id.rating_bar)
        etContents = findViewById(R.id.et_contents)
        btnSubmit = findViewById(R.id.btn_submit)
        ivAddImage = findViewById(R.id.iv_add_image)
        llImageList = findViewById(R.id.ll_image_list)
        loadingLayout = findViewById(R.id.loading_layout)

        if (reviewId != null) {
            ratingBar.rating = intent.getFloatExtra("rating", 0f)
            etContents.setText(intent.getStringExtra("contents"))
            btnSubmit.text = "수정하기"
            
            if (!existingFilePaths.isNullOrBlank()) {
                val paths = existingFilePaths.split(",")
                paths.forEach { path ->
                    if (path.isNotBlank()) {
                        imageList.add(ReviewImage(url = path.trim()))
                    }
                }
                refreshImageInterface()
            }
        }

        ivAddImage.setOnClickListener {
            if (imageList.size < 3) {
                pickImageLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "사진은 최대 3개까지만 첨부 가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubmit.setOnClickListener {
            submitReview()
        }
    }

    private fun refreshImageInterface() {
        // Remove all except the add button
        val childCount = llImageList.childCount
        if (childCount > 1) {
            llImageList.removeViews(1, childCount - 1)
        }

        imageList.forEachIndexed { index, item ->
            val itemLayout = layoutInflater.inflate(R.layout.item_review_image_upload, llImageList, false)
            val ivThumb = itemLayout.findViewById<ImageView>(R.id.iv_thumb)
            val ivDelete = itemLayout.findViewById<ImageView>(R.id.iv_delete)

            if (item.file != null) {
                Glide.with(this).load(item.file).into(ivThumb)
            } else if (item.url != null) {
                Glide.with(this).load(item.url).into(ivThumb)
            }

            ivDelete.setOnClickListener {
                imageList.removeAt(index)
                refreshImageInterface()
            }
            llImageList.addView(itemLayout)
        }

        ivAddImage.visibility = if (imageList.size < 3) View.VISIBLE else View.GONE
    }

    private fun submitReview() {
        val contents = etContents.text.toString().trim()
        val rating = ratingBar.rating.toDouble()
        
        if (contents.isEmpty()) {
            Toast.makeText(this, "리뷰 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val branchId = LoginInfoUtil.getBranchId(this)
        val selectedFiles = imageList.mapNotNull { it.file }

        loadingLayout.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val success = if (reviewId != null) {
                    AppServiceProvider.getService().updateReview(
                        reviewId!!,
                        rating.toInt(),
                        contents,
                        TokenUtil.getToken(this@AdReviewWriteActivity),
                        branchId,
                        if (selectedFiles.isNotEmpty()) selectedFiles else null
                    )
                } else {
                    AppServiceProvider.getService().insertReview(
                        productId,
                        rating.toInt(),
                        contents,
                        TokenUtil.getToken(this@AdReviewWriteActivity),
                        branchId,
                        if (selectedFiles.isNotEmpty()) selectedFiles else null
                    )
                }
                
                if (success) {
                    val msg = if (reviewId != null) "리뷰가 수정되었습니다." else "리뷰가 등록되었습니다."
                    Toast.makeText(this@AdReviewWriteActivity, msg, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@AdReviewWriteActivity, "실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdReviewWriteActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            } finally {
                loadingLayout.visibility = View.GONE
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val context = applicationContext
        val fileName = getFileName(context, uri) ?: "temp_review_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(tempFile)
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            return tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
