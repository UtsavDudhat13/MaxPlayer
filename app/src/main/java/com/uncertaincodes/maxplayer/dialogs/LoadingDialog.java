package com.uncertaincodes.maxplayer.dialogs;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import com.uncertaincodes.maxplayer.R;

public class LoadingDialog extends AlertDialog {

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        setCancelable(false);
        setContentView(R.layout.dialog_loading);
    }
}