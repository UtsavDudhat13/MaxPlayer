package com.uncertaincodes.maxplayer.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uncertaincodes.maxplayer.R;

public class OnboardAdapter  extends RecyclerView.Adapter<OnboardAdapter.SliderViewHolder>{
    private final int[] slideImages;
    private final String[] slideTexts;
    private final String[] slideSubtitle;

    public OnboardAdapter(int[] slideImages, String[] slideTexts,String[] slideSubtitle) {
        this.slideImages = slideImages;
        this.slideTexts = slideTexts;
        this.slideSubtitle = slideSubtitle;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboard, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        holder.imageView.setImageResource(slideImages[position]);
        holder.textView.setText(slideTexts[position]);
        holder.subtitle.setText(slideSubtitle[position]);
    }

    @Override
    public int getItemCount() {
        return slideImages.length;
    }
    public class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView subtitle;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.textView);
            subtitle = itemView.findViewById(R.id.subtitle);
        }
    }
}
