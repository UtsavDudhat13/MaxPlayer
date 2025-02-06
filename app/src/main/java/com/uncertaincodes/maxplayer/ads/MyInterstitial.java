package com.uncertaincodes.maxplayer.ads;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.tencent.mmkv.MMKV;
import com.uncertaincodes.maxplayer.BaseActivity;
import com.uncertaincodes.maxplayer.dialogs.LoadingDialog;
import com.uncertaincodes.maxplayer.utils.Constants;

public class MyInterstitial extends IInterstitial {
    private static MyInterstitial mInterstitial = new MyInterstitial();
    private OnAdDismissListener listener;
    private com.google.android.gms.ads.interstitial.InterstitialAd gInterstitial = null;
    private com.facebook.ads.InterstitialAd fInterstitial = null;
    private STATE mState;
    private final String status;
    private final String type;
    private final int adCount;
    private final String googleAdUnitId;
    private final String fbInterstitialId;
    private int currentChance;
    private boolean isGoogle;

    private MyInterstitial() {
        MMKV mmkv = MMKV.defaultMMKV();
        currentChance = 0;
        isGoogle = true;

        this.googleAdUnitId = mmkv.getString(Constants.GOOGLE_INTERSTITIAL_ID, "/6499/example/interstitial");
        this.fbInterstitialId = mmkv.getString(Constants.FACEBOOK_INTERSTITIAL_ID, "YOUR_PLACEMENT_ID");
        this.status = mmkv.getString(Constants.INTERSTITIAL_STATUS, "true");
        this.type = mmkv.getString(Constants.INTERSTITIAL_TYPE, "facebook");
        this.adCount = Integer.parseInt(mmkv.getString(Constants.INTERSTITIAL_AD_COUNT, "3"));

        if (type.equals("google")) {
            MobileAds.initialize(BaseActivity.getContext().getApplicationContext());
        }
        if (type.equals("facebook")) {
            AudienceNetworkAds.initialize(BaseActivity.getContext().getApplicationContext());
        }
        if (type.equals("mix") || type.equals("mixNative")) {
            MobileAds.initialize(BaseActivity.getContext().getApplicationContext());
            AudienceNetworkAds.initialize(BaseActivity.getContext().getApplicationContext());
        }
    }

    public static MyInterstitial create() {
        if (mInterstitial == null) {
            mInterstitial = new MyInterstitial();
        }
        return mInterstitial;
    }

    @Override
    public void loadAd() {
        if (type.equals("google")) {
            loadGoogleAd();
        }
        if (type.equals("facebook")) {
            loadFacebookAd();
        }
        if (type.equals("mix")) {
            if (isGoogle) {
                loadGoogleAd();
            } else {
                loadFacebookAd();
            }
        }
        if (type.equals("mixNative")) {
            if (isGoogle) {
                loadGoogleAd();
            }
        }
    }

    @Override
    public void loadAndShow(Activity activity, OnAdDismissListener listener) {
        if (!status.equals("true")) {
            if (listener != null) {
                listener.OnAdDismiss();
            }
            return;
        }
        this.listener = listener;
        LoadingDialog dialog = new LoadingDialog(activity);
        dialog.show();
        if (type.equals("google")) {
            loadAndShowGoogleAd(activity, dialog);
        }
        if (type.equals("facebook")) {
            loadAndShowFacebookAd(activity, dialog);
        }
        if (type.equals("mix")) {
            if (isGoogle) {
                isGoogle = false;
                loadAndShowGoogleAd(activity, dialog);
            } else {
                isGoogle = true;
                loadAndShowFacebookAd(activity, dialog);
            }
        }
        if (type.equals("mixNative")) {
            if (isGoogle) {
                isGoogle = false;
                loadAndShowGoogleAd(activity, dialog);
            }
        }
    }

    @Override
    public void showIntervalAd(Activity activity, OnAdDismissListener listener) {
        if (!status.equals("true")) {
            if (listener != null) {
                listener.OnAdDismiss();
            }
            return;
        }
        currentChance++;
        if (currentChance >= adCount) {
            this.listener = listener;
//            if (!isGoogle && type.equals("mixNative")) {
//                isGoogle = true;
//                activity.startActivity(new Intent(activity, FbNativeInterstitial.class));
//                FbNativeInterstitial.nativeInterstitialListener = new NativeInterstitialListener() {
//                    @Override
//                    public void OnNativeAdDismiss() {
//                        loadAd();
//                        listener.OnAdDismiss();
//                    }
//                };
//                return;
//            }
            if (mState == STATE.FAILED || mState == STATE.NULL) {
                loadAndShow(activity, listener);
                return;
            }
            if (mState == STATE.LOADING) {
                if (type.equals("google")) {
                    currentChance = 0;
                    if (listener != null) {
                        listener.OnAdDismiss();
                    }
                }
                return;
            }
            if (mState == STATE.LOADED) {
                if ((type.equals("mixNative") && isGoogle && gInterstitial != null) || (type.equals("mix") ? isGoogle ? gInterstitial != null : fInterstitial != null : type.equals("google") ? gInterstitial != null : fInterstitial != null)) {
                    if (type.equals("google")) {
                        showGoogleIntervalAd();
                        gInterstitial.show(activity);
                    }
                    if (type.equals("facebook")) {
                        fInterstitial.show();
                    }
                    if (type.equals("mix")) {
                        if (isGoogle) {
                            isGoogle = false;
                            showGoogleIntervalAd();
                            gInterstitial.show(activity);
                        } else {
                            isGoogle = true;
                            fInterstitial.show();
                        }
                    }
//                    if (type.equals("mixNative") && gInterstitial != null) {
//                        isGoogle = false;
//                        showGoogleIntervalAd();
//                        gInterstitial.show(activity);
//                    }
                    return;
                }
            } else {
                if (listener != null) {
                    listener.OnAdDismiss();
                }
            }
            return;
        }
        if (mState == STATE.NULL) {
            loadAd();
            if (listener != null) {
                listener.OnAdDismiss();
            }
            return;
        }
        if (mState == STATE.FAILED) {
            loadAd();
            if (listener != null) {
                listener.OnAdDismiss();
            }
            return;
        }
        if (listener != null) {
            listener.OnAdDismiss();
        }
    }


    private void loadGoogleAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                BaseActivity.getContext().getApplicationContext(),
                googleAdUnitId,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        gInterstitial = interstitialAd;
                        mState = STATE.LOADED;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        mState = STATE.FAILED;
                    }
                });
    }

    private void loadFacebookAd() {
        fInterstitial = new com.facebook.ads.InterstitialAd(BaseActivity.getContext().getApplicationContext(), fbInterstitialId);
        fInterstitial.loadAd(fInterstitial.buildLoadAdConfig().withAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                loadAd();
                currentChance = 0;
                if (listener != null) {
                    listener.OnAdDismiss();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                mState = STATE.FAILED;
            }

            @Override
            public void onAdLoaded(Ad ad) {
                mState = STATE.LOADED;
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        }).build());
    }

    private void loadAndShowGoogleAd(Activity activity, LoadingDialog dialog) {
        InterstitialAd.load(
                BaseActivity.getContext().getApplicationContext(),
                googleAdUnitId,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                super.onAdFailedToShowFullScreenContent(adError);
                                currentChance = 0;
                                loadAd();
                                if (listener != null) {
                                    listener.OnAdDismiss();
                                }
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent();
                                currentChance = 0;
                                loadAd();
                                if (listener != null) {
                                    listener.OnAdDismiss();
                                }
                            }

                            @Override
                            public void onAdImpression() {
                                super.onAdImpression();
                            }

                            @Override
                            public void onAdClicked() {
                                super.onAdClicked();
                            }
                        });
                        interstitialAd.show(activity);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        loadAd();
                        currentChance = 0;
                        if (listener != null) {
                            listener.OnAdDismiss();
                        }
                    }
                });
    }

    private void loadAndShowFacebookAd(Activity activity, LoadingDialog dialog) {
        com.facebook.ads.InterstitialAd fInter;
        fInter = new com.facebook.ads.InterstitialAd(
                BaseActivity.getContext().getApplicationContext(),
                fbInterstitialId
        );
        fInter.loadAd(fInter.buildLoadAdConfig().withAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                loadAd();
                currentChance = 0;
                if (listener != null) {
                    listener.OnAdDismiss();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                loadAd();
                currentChance = 0;
                if (listener != null) {
                    listener.OnAdDismiss();
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                currentChance = 0;
                fInter.show();
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        }).build());
    }

    private void showGoogleIntervalAd() {
        gInterstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                super.onAdFailedToShowFullScreenContent(adError);
                currentChance = 0;
                loadAd();
                if (listener != null) {
                    listener.OnAdDismiss();
                }
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent();
                currentChance = 0;
                loadAd();
                if (listener != null) {
                    listener.OnAdDismiss();
                }
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }
        });
    }
}
