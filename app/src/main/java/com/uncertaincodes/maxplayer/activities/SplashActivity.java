package com.uncertaincodes.maxplayer.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tencent.mmkv.MMKV;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.ads.MyInterstitial;
import com.uncertaincodes.maxplayer.dialogs.InternetDialog;
import com.uncertaincodes.maxplayer.utils.Constants;
import com.uncertaincodes.maxplayer.utils.Utils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        storagePermissions = Build.VERSION.SDK_INT >= 33 ? new String[]{"android.permission.READ_MEDIA_VIDEO"} : (Build.VERSION.SDK_INT >= 30 ? new String[]{"android.permission.READ_EXTERNAL_STORAGE"} : new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"});

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (!Utils.isConnected(this)) {
            new InternetDialog(this, this::init).show();
        } else {
            init();
        }

    }


    private void init() {

        FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(15)
                .build();
        config.setConfigSettingsAsync(configSettings);
        config.setDefaultsAsync(R.xml.remote_config_defaults);
        config.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                MMKV mmkv = MMKV.defaultMMKV();

                String str_ad_count = config.getString("interstitial_ad_count");

                String str_app_open_status = config.getString("show_app_open");
                String str_interstitial_status = config.getString("show_interstitial");
                String str_native_status = config.getString("show_native");

                String str_native_type = config.getString("native_type");
                String str_interstitial_type = config.getString("interstitial_type");

                String str_google_native_id = config.getString("google_native_id");
                String str_google_interstitial_id = config.getString("google_interstitial_id");
                String str_google_app_open_id = config.getString("google_app_open_id");
                String str_fb_native_id = config.getString("fb_native_id");
                String str_fb_native_banner_id = config.getString("fb_native_banner_id");
                String str_fb_interstitial_id = config.getString("fb_interstitial_id");


                mmkv.putString(Constants.INTERSTITIAL_AD_COUNT, str_ad_count);
                mmkv.putString(Constants.APP_OPEN_STATUS, str_app_open_status);
                mmkv.putString(Constants.INTERSTITIAL_STATUS, str_interstitial_status);
                mmkv.putString(Constants.NATIVE_STATUS, str_native_status);
                mmkv.putString(Constants.NATIVE_TYPE, str_native_type);
                mmkv.putString(Constants.INTERSTITIAL_TYPE, str_interstitial_type);

                mmkv.putString(Constants.GOOGLE_NATIVE_ID, str_google_native_id);
                mmkv.putString(Constants.GOOGLE_INTERSTITIAL_ID, str_google_interstitial_id);
                mmkv.putString(Constants.GOOGLE_APP_OPEN_ID, str_google_app_open_id);
                mmkv.putString(Constants.FACEBOOK_NATIVE_ID, str_fb_native_id);
                mmkv.putString(Constants.FACEBOOK_NATIVE_BANNER_ID, str_fb_native_banner_id);
                mmkv.putString(Constants.FACEBOOK_INTERSTITIAL_ID, str_fb_interstitial_id);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MyInterstitial.create().loadAd();
                        MMKV mmkv = MMKV.defaultMMKV();
                        if (!checkPermissions() && !mmkv.decodeBool(Constants.IS_PERMISSION_GRANTED, false)) {
                            startActivity(new Intent(SplashActivity.this, PermissionActivity.class));
                            finish();
                            return;
                        }
                        if (!mmkv.decodeBool(Constants.IS_ONBOARDING_SHOWED, false)) {
                            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                        } else {
                            startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        }
                        finish();
                    }
                }, 2000);

            }
        });
    }

    private boolean checkPermissions() {
        MMKV mmkv = MMKV.defaultMMKV();
        for (String permission : storagePermissions) {
            if (ContextCompat.checkSelfPermission(SplashActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                mmkv.encode(Constants.IS_PERMISSION_GRANTED,false);
                return false;
            }
        }
        if (!mmkv.decodeBool(Constants.IS_PERMISSION_GRANTED, false)) {
            mmkv.encode(Constants.IS_PERMISSION_GRANTED,true);
            return true;
        }
        return true;
    }
}