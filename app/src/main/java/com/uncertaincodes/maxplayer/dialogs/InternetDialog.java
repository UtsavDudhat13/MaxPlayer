package com.uncertaincodes.maxplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.utils.Utils;

public class InternetDialog extends Dialog {
    private final OnDialogDismissListener listener;
    public InternetDialog(@NonNull Context context, OnDialogDismissListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_no_internet);

        findViewById(R.id.apply).setOnClickListener(view -> {
            if (Utils.isConnected(getContext())){
                listener.onDismiss();
                dismiss();
            }
        });
    }
}
