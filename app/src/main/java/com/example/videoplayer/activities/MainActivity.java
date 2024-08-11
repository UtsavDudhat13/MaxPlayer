package com.example.videoplayer.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.videoplayer.R;
import com.example.videoplayer.utils.Constants;
import com.tencent.mmkv.MMKV;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String[] storagePermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MMKV mmkv = MMKV.defaultMMKV();

        storagePermissions = Build.VERSION.SDK_INT >= 33 ? new String[]{"android.permission.READ_MEDIA_VIDEO"} : (Build.VERSION.SDK_INT >= 30 ? new String[]{"android.permission.READ_EXTERNAL_STORAGE"} : new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"});

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.windowBackground));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!checkPermissions() && !mmkv.decodeBool(Constants.IS_PERMISSION_GRANTED, false)) {
                    startActivity(new Intent(MainActivity.this, PermissionActivity.class));
                } else {
                   startActivity(new Intent(MainActivity.this, HomeActivity.class));
                }
                finish();
            }
        }, 2000);
    }

    private boolean checkPermissions() {
        for (String permission : storagePermissions) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}