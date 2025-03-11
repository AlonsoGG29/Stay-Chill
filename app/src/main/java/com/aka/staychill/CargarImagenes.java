package com.aka.staychill;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class CargarImagenes {
    private static CargarImagenes instance;
    private final SharedPreferences versionsPrefs;
    private static final String PREFS_NAME = "image_versions";

    // Configuración común para Glide
    private final RequestOptions glideOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .circleCrop()
            .placeholder(R.drawable.img_default)
            .error(R.drawable.img_default);

    private CargarImagenes(Context context) {
        versionsPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized CargarImagenes getInstance(Context context) {
        if (instance == null) {
            instance = new CargarImagenes(context.getApplicationContext());
        }
        return instance;
    }

    public void loadProfileImage(String imageUrl, ImageView imageView, Context context) {
        String cacheBusterUrl = getCacheBustedUrl(imageUrl, "profile_" + getUserId(context));
        Glide.with(context)
                .load(cacheBusterUrl)
                .apply(glideOptions.circleCrop())
                .into(imageView);
    }

    public void updateImageVersion(String imageKey) {
        int currentVersion = versionsPrefs.getInt(imageKey, 0);
        versionsPrefs.edit()
                .putInt(imageKey, currentVersion + 1)
                .apply();
    }

    private String getCacheBustedUrl(String originalUrl, String imageKey) {
        int version = versionsPrefs.getInt(imageKey, 0);
        return originalUrl + (originalUrl.contains("?") ? "&" : "?") + "v=" + version;
    }

    private String getUserId(Context context) {
        return new SessionManager(context).getUserIdString();
    }
}