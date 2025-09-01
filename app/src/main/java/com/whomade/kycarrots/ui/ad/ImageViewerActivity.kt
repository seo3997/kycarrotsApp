package com.whomade.kycarrots.ui.ad

import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.whomade.kycarrots.R
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.DataSource
import android.graphics.drawable.Drawable

class ImageViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        // 상태바/내비바 어둡게
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        val url = intent.getStringExtra("url")
        val photoView = findViewById<PhotoView>(R.id.photoView)
        val progress  = findViewById<ProgressBar>(R.id.progress)

        // 탭하면 닫히게
        photoView.setOnClickListener { finish() }

        progress?.visibility = android.view.View.VISIBLE
        Glide.with(this)
            .load(url)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable>, isFirst: Boolean
                ): Boolean {
                    progress?.visibility = android.view.View.GONE
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirst: Boolean
                ): Boolean {
                    progress?.visibility = android.view.View.GONE
                    return false
                }
            })
            .into(photoView)
    }

}