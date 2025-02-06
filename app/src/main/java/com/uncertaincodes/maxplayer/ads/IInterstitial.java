package com.uncertaincodes.maxplayer.ads;

import android.app.Activity;

public abstract class IInterstitial {
    protected abstract void loadAd();
    protected abstract void loadAndShow(Activity activity, OnAdDismissListener listener);
    protected abstract void showIntervalAd(Activity activity, OnAdDismissListener listener);
}
