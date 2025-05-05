package com.whomade.kycarrots.ui.common;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

public class ImageLoader {

    public static void loadImage(
            Activity activity,
            String imageUrl,
            ImageView imageView,
            @Nullable ProgressBar progressBar
    ) {
        Glide.with(activity)
                .load(imageUrl)
                .centerCrop()
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        return false; // Glide가 오류를 처리하도록 false 반환
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        return false; // Glide가 리소스를 처리하도록 false 반환
                    }
                })
                .into(imageView);
    }
}