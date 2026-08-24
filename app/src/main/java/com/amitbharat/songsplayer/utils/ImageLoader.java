package com.amitbharat.songsplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.amitbharat.songsplayer.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

public final class ImageLoader {

    private static final Uri ALBUM_ART_URI = Uri.parse("content://media/external/audio/albumart");

    private ImageLoader() {}

    /**
     * Loads artwork from local albumId or remote URL with placeholder and smooth crossfade
     */
    public static void loadAlbumArt(Context context, long albumId, String remoteArtUrl, ImageView target) {
        if (context == null || target == null) return;

        Uri artUri = null;
        if (remoteArtUrl != null && !remoteArtUrl.trim().isEmpty()) {
            artUri = Uri.parse(remoteArtUrl);
        } else if (albumId > 0) {
            artUri = ContentUris.withAppendedId(ALBUM_ART_URI, albumId);
        }

        Glide.with(context)
                .load(artUri)
                .placeholder(R.drawable.bg_album_placeholder)
                .error(R.drawable.bg_album_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(target);
    }

    /**
     * Loads image with custom fallback drawable
     */
    public static void loadImage(Context context, String url, @DrawableRes int fallbackRes, ImageView target) {
        if (context == null || target == null) return;

        Glide.with(context)
                .load(url)
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(target);
    }

    /**
     * Clears disk cache in background
     */
    public static void clearCache(Context context) {
        if (context == null) return;
        new Thread(() -> Glide.get(context.getApplicationContext()).clearDiskCache()).start();
        Glide.get(context.getApplicationContext()).clearMemory();
    }
}
