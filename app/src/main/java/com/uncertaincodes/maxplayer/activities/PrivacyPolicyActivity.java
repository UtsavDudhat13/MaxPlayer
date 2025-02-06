package com.uncertaincodes.maxplayer.activities;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.ads.MyInterstitial;
import com.uncertaincodes.maxplayer.ads.MyNativeBanner;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        LinearLayout nativeAdLayout = findViewById(R.id.ll_naive_ad);
        MyNativeBanner.getInstance(this).loadNativeAd(nativeAdLayout);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener(v -> {
            MyInterstitial.create().showIntervalAd(PrivacyPolicyActivity.this,() -> {
                getOnBackPressedDispatcher().onBackPressed();
            });
        });

        WebView webView = findViewById(R.id.webview);
        webView.loadUrl("file:///android_asset/privacy.html");

    }
}