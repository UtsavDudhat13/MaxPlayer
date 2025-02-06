package com.uncertaincodes.maxplayer.ads;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.facebook.ads.*;
import com.tencent.mmkv.MMKV;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.dialogs.LoadingDialog;
import com.uncertaincodes.maxplayer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FbNativeInterstitial extends AppCompatActivity {
    private NativeAd fNativeInterstitial = null;
    private String fbNativeInterstitialId;
    public static NativeInterstitialListener nativeInterstitialListener;

    private NativeAdLayout nativeAdLayout;
    private LinearLayout adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_native_interstitial);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        nativeAdLayout = findViewById(R.id.native_ad_container);
        adView = findViewById(R.id.ad_unit);

        LoadingDialog dialog = new LoadingDialog(this);
        dialog.show();

        MMKV mmkv = MMKV.defaultMMKV();
        fbNativeInterstitialId = mmkv.getString(Constants.FACEBOOK_NATIVE_ID, "YOUR_PLACEMENT_ID");

        fNativeInterstitial = new NativeAd(this, fbNativeInterstitialId);
        fNativeInterstitial.loadAd(fNativeInterstitial.buildLoadAdConfig().withAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.d("TAG___", "onError: " + adError.getErrorMessage());
                if (nativeInterstitialListener != null) {
                    nativeInterstitialListener.OnNativeAdDismiss();
                    finish();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (fNativeInterstitial == null || fNativeInterstitial != ad) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(fNativeInterstitial);
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        }).build());
    }

    private void inflateAd(NativeAd nativeAd) {
        nativeAd.unregisterView();

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(this, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = findViewById(R.id.native_ad_call_to_action);
        ImageView btnClose = findViewById(R.id.native_ad_close);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView, nativeAdMedia, nativeAdIcon, clickableViews);

        btnClose.setOnClickListener(v -> {
            if (nativeInterstitialListener != null) nativeInterstitialListener.OnNativeAdDismiss();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nativeInterstitialListener = null;
    }
}