package com.whomade.kycarrots.ui.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.DataSource;

public class ImageLoader {

    public static void loadImage(
            Context context,
            String imageUrl,
            ImageView imageView,
            @Nullable ProgressBar progressBar
    ) {
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .listener(new RequestListener<Drawable>() {
                    private int retryCount = 0;
                    private final int maxRetries = 3;

                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource
                    ) {
                        if (retryCount < maxRetries) {
                            retryCount++;
                            imageView.postDelayed(() ->
                                Glide.with(context)
                                    .load(imageUrl)
                                    .centerCrop()
                                    .listener(this)
                                    .into(imageView)
                            , 1000);
                            return true;
                        }
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        return false;
                    }
                })
                .into(imageView);
    }
}
