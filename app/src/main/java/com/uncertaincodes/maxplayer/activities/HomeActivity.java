package com.uncertaincodes.maxplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.uncertaincodes.maxplayer.BaseActivity;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.ads.MyInterstitial;
import com.uncertaincodes.maxplayer.ads.MyNativeBanner;
import com.uncertaincodes.maxplayer.fragments.AllVideoFragment;
import com.uncertaincodes.maxplayer.fragments.FolderFragment;
import com.uncertaincodes.maxplayer.fragments.SettingsFragment;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private AllVideoFragment allVideoFragment = new AllVideoFragment();
    private FolderFragment folderFragment = new FolderFragment();
    private SettingsFragment settingsFragment = new SettingsFragment();
    Fragment activeFragment = allVideoFragment;

    private TextView tvTitle;
    private ImageView homeIcon, folderIcon, settingIcon,btn_search;
    private TextView tv_home, tv_folder, tv_setting;
    private LinearLayout homeLayout, folderLayout, settingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BaseActivity.getVideoFetcher().fetchAllFolders();

        configureSystemUI();

        initViews();

        LinearLayout nativeAdLayout = findViewById(R.id.ll_naive_ad);
        MyNativeBanner.getInstance(this).loadNativeAd(nativeAdLayout);

        setCustomTitle();

        homeLayout.setOnClickListener(this);
        folderLayout.setOnClickListener(this);
        settingLayout.setOnClickListener(this);

        loadFragment(allVideoFragment);
        updateTabIcons(0);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.container_frame, allVideoFragment, "allVideoFragment");
        ft.add(R.id.container_frame, folderFragment, "folderFragment").hide(folderFragment);
        ft.add(R.id.container_frame, settingsFragment, "settingsFragment").hide(settingsFragment);
        ft.commit();

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyInterstitial.create().showIntervalAd(HomeActivity.this, () -> {
                    startActivity(new Intent(HomeActivity.this, SearchActivity.class));
                });
            }
        });

    }

    public void loadFragment(Fragment fragment) {
        if (fragment == activeFragment) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(activeFragment);
        ft.show(fragment);
        ft.commit();
        activeFragment = fragment;
    }

    private void configureSystemUI() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        homeIcon = findViewById(R.id.home);
        folderIcon = findViewById(R.id.folder);
        settingIcon = findViewById(R.id.setting);
        homeLayout = findViewById(R.id.homell);
        folderLayout = findViewById(R.id.folderll);
        settingLayout = findViewById(R.id.settingll);
        tv_home = findViewById(R.id.tv_home);
        tv_folder = findViewById(R.id.tv_folder);
        tv_setting = findViewById(R.id.tv_setting);
        btn_search = findViewById(R.id.btn_search);
    }

    private void setCustomTitle() {
        String text = getString(R.string.app_name);
        SpannableString spannableString = new SpannableString(text);

        int firstSpaceIndex = text.indexOf(" ");
        if (firstSpaceIndex != -1) {
            spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimary)), 0, firstSpaceIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimaryText)), firstSpaceIndex, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimary)), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvTitle.setText(spannableString);
    }

    private void updateTabIcons(int position) {
        int selectedColor = getColor(R.color.colorPrimary);
        int defaultColor = getColor(R.color.colorSecondaryText);

        homeIcon.setColorFilter(position == 0 ? selectedColor : defaultColor);
        tv_home.setTextColor(position == 0 ? selectedColor : defaultColor);

        folderIcon.setColorFilter(position == 1 ? selectedColor : defaultColor);
        tv_folder.setTextColor(position == 1 ? selectedColor : defaultColor);

        settingIcon.setColorFilter(position == 2 ? selectedColor : defaultColor);
        tv_setting.setTextColor(position == 2 ? selectedColor : defaultColor);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.homell) {
            updateTabIcons(0);
            loadFragment(allVideoFragment);
        }
        if (id == R.id.folderll) {
            updateTabIcons(1);
            loadFragment(folderFragment);
        }
        if (id == R.id.settingll) {
            updateTabIcons(2);
            loadFragment(settingsFragment);
        }
    }
}
