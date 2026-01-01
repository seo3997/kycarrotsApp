// DlgSelImg.kt
package com.whomade.kycarrots.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.content.FileProvider
import com.whomade.kycarrots.FileManager
import com.whomade.kycarrots.R
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 사진촬영, 앨범 선택 할 수 있는 팝업
 */
class DlgSelImg : Dialog, View.OnTouchListener {

    private val mContext: Context
    private lateinit var btnCancel: Button

    private val arrString = ArrayList<String>()
    private var isImg: Boolean = false
    private var isChar: Boolean = false

    private var strFilePath: String = "" // 폴더 경로
    private var strFileName: String = "" // 파일 name
    private var imageFilePath: String = ""

    // ✅ 원본처럼 "삭제 선택 여부"
    private var delSelected: Boolean = false

    // ✅ Activity에서 받는 dismiss listener
    private var mDismiss: DialogInterface.OnDismissListener? = null

    constructor(context: Context) : super(context) {
        mContext = context
    }

    constructor(context: Context, isImg: Boolean, isChar: Boolean) : super(context) {
        mContext = context
        this.isImg = isImg
        this.isChar = isChar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dlg_list_title)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        findViewById<TextView>(R.id.txt_dlg_title).text =
            mContext.resources.getString(R.string.str_img_title)

        findViewById<LinearLayout>(R.id.ll1).setOnTouchListener(this)
        btnCancel = findViewById(R.id.btn1)
        btnCancel.setOnTouchListener(this)

        val lvDlgMsg: ListView = findViewById(R.id.lv_dlg)

        arrString.clear()
        arrString.add(mContext.getString(R.string.str_camera))
        arrString.add(mContext.getString(R.string.str_album))
        if (isImg) arrString.add(mContext.getString(R.string.str_img_del))

        lvDlgMsg.adapter = DlgListAdapter(mContext, arrString)

        lvDlgMsg.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val selected = arrString[position]

                when (selected) {
                    mContext.getString(R.string.str_camera) -> openCamera()
                    mContext.getString(R.string.str_album) -> openAlbum()
                    mContext.getString(R.string.str_img_del) -> {
                        delSelected = true
                        mDismiss?.onDismiss(this@DlgSelImg)
                    }
                }
                dismiss()
            }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(mContext.packageManager) == null) return

        val photoFile = try {
            createImageFile()
        } catch (e: IOException) {
            Log.e("temp", "createImageFile error", e)
            null
        } ?: return

        // ✅ 카메라 결과 uri (Activity에서 getImageUri()로 꺼낼 것)
        mImageUri = FileProvider.getUriForFile(
            mContext,
            "${mContext.packageName}.fileprovider",
            photoFile
        )

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        (mContext as Activity).startActivityForResult(intent, PICK_FROM_CAMERA)
    }

    private fun openAlbum() {
        try {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).apply { type = "image/*" }

            (mContext as Activity).startActivityForResult(intent, PICK_FROM_GALLERY)
        } catch (e: Exception) {
            Log.d("temp", "ACTION_PICK[$e]")
            Toast.makeText(
                mContext,
                "This device doesn't support the pick action!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IOException("저장 디렉토리를 찾을 수 없습니다.")
        val file = File.createTempFile("CROP_$timeStamp", ".jpg", storageDir)
        imageFilePath = file.absolutePath
        return file
    }

    // ====== 원본처럼 Getter들 ======
    fun getDelImg(): Boolean = delSelected
    fun getFilePath(): String = strFilePath
    fun getFileName(): String = strFileName
    fun getImageFilePath(): String = imageFilePath

    // ✅ Activity에서 카메라 uri를 꺼내기 위한 함수
    fun getImageUri(): Uri? = mImageUri

    fun setFilePathName(filePath: String, fileName: String) {
        strFilePath = filePath
        strFileName = fileName
    }

    fun setonDismissListener(listener: DialogInterface.OnDismissListener) {
        mDismiss = listener
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (v.id == R.id.ll1 || v.id == R.id.btn1) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                @Suppress("DEPRECATION")
                btnCancel.background = mContext.resources.getDrawable(R.drawable.dlg_chk_press)
            }
            if (event.action == MotionEvent.ACTION_UP) {
                @Suppress("DEPRECATION")
                btnCancel.background = mContext.resources.getDrawable(R.drawable.dlg_chk)
                dismiss()
            }
            return true
        }
        return false
    }

    companion object {
        // ✅ 카메라 촬영용 uri
        @JvmStatic var mImageUri: Uri? = null

        const val CROP_FROM_CAMERA = 222
        const val PICK_FROM_GALLERY = 333
        const val PICK_FROM_CAMERA = 444
        const val CROP_FROM_GALLERY = 555

        const val STR_SECERT_FOLDER_NAME = "CashcukTemp"
        val STR_DIR: String =
            Environment.getExternalStorageDirectory().toString() + "/." + STR_SECERT_FOLDER_NAME
    }
}
