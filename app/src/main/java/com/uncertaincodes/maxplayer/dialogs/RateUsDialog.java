package com.uncertaincodes.maxplayer.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.uncertaincodes.maxplayer.R;

public class RateUsDialog extends Dialog {
    private RateUsListener listener;

    public RateUsDialog(@NonNull Context context) {
        super(context);
    }

    public void setListener(RateUsListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_rateus);


        for (int i = 0; i < ids.length; i++) {
            int finalI = i;
            findViewById(ids[i]).setOnClickListener(v -> setStart(finalI + 1));
        }

        findViewById(R.id.btnCancelRate).setOnClickListener(v -> {
            listener.onCancel();
            dismiss();
        });
        findViewById(R.id.btnRateUs).setOnClickListener(v -> {
            listener.onRateUs(selectedStar);
            dismiss();
        });
    }

    private int selectedStar = 0;
    private final int[] ids = new int[]{R.id.imageStar1, R.id.imageStar2, R.id.imageStar3, R.id.imageStar4, R.id.imageStar5};

    void setStart(int size) {
        selectedStar = size;
        for (int i = 0; i < ids.length; i++) {
            if (i < size) {
                ((ImageView) findViewById(ids[i])).setImageResource(R.drawable.ic_star_filled);
            } else {
                ((ImageView) findViewById(ids[i])).setImageResource(R.drawable.ic_star);
            }
        }
    }

    public interface RateUsListener {
        void onRateUs(int star);
        void onCancel();
    }
}
