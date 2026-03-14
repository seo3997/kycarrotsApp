package com.whomade.kycarrots.ui.ad.adreview

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
    private lateinit var ivReviewImage: ImageView
    private lateinit var ivDeleteImage: ImageView
    
    private var productId: Long = 0
    private var reviewId: String? = null
    private var selectedImageFile: File? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = getFileFromUri(it)
            if (file != null) {
                selectedImageFile = file
                ivDeleteImage.visibility = View.VISIBLE
                Glide.with(this).load(file).into(ivReviewImage)
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
        ivReviewImage = findViewById(R.id.iv_review_image)
        ivDeleteImage = findViewById(R.id.iv_delete_image)

        if (reviewId != null) {
            ratingBar.rating = intent.getFloatExtra("rating", 0f)
            etContents.setText(intent.getStringExtra("contents"))
            btnSubmit.text = "수정하기"
            // For editing, we might not allow changing the image path easily without server-side support for multipart update
            ivReviewImage.visibility = View.GONE 
        }

        ivReviewImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        ivDeleteImage.setOnClickListener {
            selectedImageFile = null
            ivReviewImage.setImageResource(android.R.drawable.ic_menu_camera)
            ivDeleteImage.visibility = View.GONE
        }

        btnSubmit.setOnClickListener {
            submitReview()
        }
    }

    private fun submitReview() {
        val contents = etContents.text.toString().trim()
        val rating = ratingBar.rating.toDouble()
        
        if (contents.isEmpty()) {
            Toast.makeText(this, "리뷰 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = LoginInfoUtil.getUserId(this)
        val branchId = LoginInfoUtil.getBranchId(this)

        lifecycleScope.launch {
            try {
                val success = if (reviewId != null) {
                    AppServiceProvider.getService().updateReview(
                        reviewId!!,
                        rating.toInt(),
                        contents,
                        TokenUtil.getToken(this@AdReviewWriteActivity),
                        branchId
                    )
                } else {
                    AppServiceProvider.getService().insertReview(
                        productId,
                        rating.toInt(),
                        contents,
                        TokenUtil.getToken(this@AdReviewWriteActivity),
                        branchId,
                        selectedImageFile
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
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val context = applicationContext
        val fileName = getFileName(context, uri) ?: "temp_image.jpg"
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
