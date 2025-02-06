package com.uncertaincodes.maxplayer.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.ads.MyNativeBanner;
import com.uncertaincodes.maxplayer.utils.Constants;
import com.tencent.mmkv.MMKV;

public class PermissionActivity extends AppCompatActivity {

    private TextView tv_permission_title;
    private TextView tv_permission_description;

    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_PERMISSION_SETTINGS = 10;
    private Button btn_allow_permission;
    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        LinearLayout nativeAdLayout = findViewById(R.id.ll_naive_ad);
        MyNativeBanner.getInstance(this).loadNativeAd(nativeAdLayout);

        tv_permission_title = findViewById(R.id.tv_permission_title);
        tv_permission_description = findViewById(R.id.tv_permission_description);
        btn_allow_permission = findViewById(R.id.btn_allow_permission);

        tv_permission_title.setText(String.format(getString(R.string.strPermissionTitle), getString(R.string.app_name)));
        tv_permission_description.setText(String.format(getString(R.string.strPermissionDesc2), getString(R.string.app_name)));

        if (Build.VERSION.SDK_INT >= 33) {
            storagePermissions = new String[]{android.Manifest.permission.READ_MEDIA_VIDEO, android.Manifest.permission.POST_NOTIFICATIONS};
        } else {
            if (Build.VERSION.SDK_INT >= 30) {
                storagePermissions = new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE};
            } else {
                storagePermissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
        }

        MMKV mmkv = MMKV.defaultMMKV();
        if (mmkv.decodeBool(Constants.FLAG_PERMISSION, false)) {
            btn_allow_permission.setText("Go To Settings");
            btn_allow_permission.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
            });
        } else {
            btn_allow_permission.setOnClickListener(v -> requestStoragePermission());
        }

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(PermissionActivity.this, storagePermissions, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            MMKV mmkv = MMKV.defaultMMKV();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mmkv.encode(Constants.IS_PERMISSION_GRANTED, true);
                startActivity(new Intent(PermissionActivity.this, SplashActivity.class));
                finish();
                return;
            }
            mmkv.encode(Constants.FLAG_PERMISSION, true);
            mmkv.encode(Constants.IS_PERMISSION_GRANTED, false);

            btn_allow_permission.setText("Go To Settings");
            btn_allow_permission.setOnClickListener(v -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTINGS) {
            startActivity(new Intent(PermissionActivity.this, SplashActivity.class));
            finish();
        }
    }
}