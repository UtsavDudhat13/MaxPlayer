package com.uncertaincodes.maxplayer;

import android.app.Application;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.uncertaincodes.maxplayer.models.FolderDetails;
import com.uncertaincodes.maxplayer.videoUtils.VideoFetcher;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseActivity extends Application {

    public static BaseActivity context;
    private static VideoFetcher videoFetcher;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        MMKV.initialize(this);
        videoFetcher = VideoFetcher.getInstance();

        AdSettings.addTestDevice("81919e85-a9bf-433b-a30e-320a605b6ed8");

        MobileAds.initialize(getApplicationContext());
        AudienceNetworkAds.initialize(getApplicationContext());

    }

    public static BaseActivity getContext() {
        return context;
    }

    public static VideoFetcher getVideoFetcher() {
        return videoFetcher;
    }

}
