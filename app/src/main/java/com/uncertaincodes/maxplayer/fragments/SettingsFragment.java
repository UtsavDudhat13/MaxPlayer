package com.uncertaincodes.maxplayer.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.activities.PrivacyPolicyActivity;
import com.uncertaincodes.maxplayer.ads.MyInterstitial;
import com.uncertaincodes.maxplayer.ads.MyNativeBanner;
import com.uncertaincodes.maxplayer.dialogs.RateUsDialog;
import com.uncertaincodes.maxplayer.utils.Utils;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        view.findViewById(R.id.privacyPolicy).setOnClickListener(view1 -> {
            MyInterstitial.create().loadAndShow(getActivity(), () -> {
                startActivity(new Intent(getContext(), PrivacyPolicyActivity.class));
            });
        });

        view.findViewById(R.id.rateUs).setOnClickListener(view1 -> {
            RateUsDialog dialog = new RateUsDialog(getContext());
            dialog.setListener(new RateUsDialog.RateUsListener() {
                @Override
                public void onRateUs(int star) {
                    if (star > 3) {
                        try {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("market://details?id=");
                            stringBuilder.append(requireActivity().getPackageName());
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(stringBuilder.toString())));
                            dialog.dismiss();
                        } catch (ActivityNotFoundException activityNotFoundException) {
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("https://play.google.com/store/apps/details?id=");
                            stringBuilder.append(requireActivity().getPackageName());
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(stringBuilder.toString())));
                            dialog.dismiss();
                        }
                    } else {
                        Toast.makeText(getContext(), "Thank you for supporting us.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }

                @Override
                public void onCancel() {
                }
            });
            dialog.show();
        });

        view.findViewById(R.id.shareApp).setOnClickListener(view1 -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "Check out this HD Video Player:\n https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share App"));
        });

        view.findViewById(R.id.exitApp).setOnClickListener(view1 -> {
            Utils.alertDialog(requireActivity(), R.layout.dialog_exit, true, dialog -> {
                dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
                dialog.findViewById(R.id.btnExit).setOnClickListener(v -> requireActivity().finishAffinity());
            });
        });

        return view;
    }
}