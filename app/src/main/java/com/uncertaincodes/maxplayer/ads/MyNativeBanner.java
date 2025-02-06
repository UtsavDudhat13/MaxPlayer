package com.uncertaincodes.maxplayer.ads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.facebook.ads.*;
import com.tencent.mmkv.MMKV;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MyNativeBanner {

    private final String placementId;
    private static MyNativeBanner instance;
    private NativeBannerAd nativeAd;
    private Context context;
    private String status;

    private MyNativeBanner(Context context) {
        MMKV mmkv = MMKV.defaultMMKV();
        this.placementId = mmkv.getString(Constants.FACEBOOK_NATIVE_BANNER_ID, "YOUR_PLACEMENT_ID");
        this.context = context.getApplicationContext();
        this.status = mmkv.getString(Constants.NATIVE_STATUS, "true");
    }

    public static synchronized MyNativeBanner getInstance(Context context) {
        if (instance == null) {
            instance = new MyNativeBanner(context);
        }
        return instance;
    }

    public void loadNativeAd(LinearLayout adLayout) {
        if (!status.equals("true")) {
            return;
        }

        nativeAd = new NativeBannerAd(context, placementId);
        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
            }

            @Override
            public void onError(Ad ad, AdError adError) {
            }

            @Override
            public void onAdLoaded(Ad ad) {
                adLayout.setVisibility(View.VISIBLE);
                if (nativeAd != null && nativeAd == ad) {
                    inflateAd(nativeAd, adLayout);
                }
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        }).build());

    }

    private void inflateAd(NativeBannerAd nativeBannerAd, LinearLayout adLayout) {
        nativeBannerAd.unregisterView();

        // Add the Ad view into the ad container
        LayoutInflater inflater = LayoutInflater.from(context);
        NativeAdLayout nativeAdLayout = (NativeAdLayout) inflater.inflate(R.layout.layout_fb_native_banner_ad, adLayout, false);
        adLayout.removeAllViews();
        adLayout.addView(nativeAdLayout);

        // Inflate the Ad view.  The layout referenced is the one you created in the last step.
        LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.layout_fb_native_banner, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Set the Native Ad view
        RelativeLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(context, nativeBannerAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        MediaView nativeAdIconView = adView.findViewById(R.id.native_icon_view);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdCallToAction.setText(nativeBannerAd.getAdCallToAction());
        nativeAdCallToAction.setVisibility(nativeBannerAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdTitle.setText(nativeBannerAd.getAdvertiserName());
        nativeAdSocialContext.setText(nativeBannerAd.getAdSocialContext());
        sponsoredLabel.setText(nativeBannerAd.getSponsoredTranslation());

        // Register the Title and CTA button to listen for clicks.
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        nativeBannerAd.registerViewForInteraction(adView, nativeAdIconView, clickableViews);
    }

    public void destroy() {
        if (nativeAd != null) {
            nativeAd.destroy();
            nativeAd = null;
        }
    }
}
