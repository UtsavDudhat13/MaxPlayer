package com.example.videoplayer.adapters;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.videoplayer.R;
import com.example.videoplayer.models.FolderDetails;
import com.example.videoplayer.models.VideoDetails;
import com.example.videoplayer.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {

    private List<VideoDetails> videoList;
    private final AdapterItemClickListener<VideoDetails> listener;
    public VideoAdapter(List<VideoDetails> videoList,AdapterItemClickListener<VideoDetails> listener) {
        this.videoList = videoList;
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public VideoAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_video, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull VideoAdapter.ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        VideoDetails data = videoList.get(position);

        Glide.with(holder.img_thumbnail)
                .asBitmap()
                .encodeQuality(60)
                .placeholder(R.color.colorSecondaryText)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .load(data.getPath())
                .into(holder.img_thumbnail);

        holder.tv_name.setText(data.getDisplayName());

        String str = context.getString(R.string.videoSizeDuration);
        String duration = Utils.timeConversion(data.getDuration());
        holder.tv_size.setText(String.format(str, duration, Formatter.formatFileSize(context, data.getSize())));

        holder.videoitem_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClicked(data,position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }
    public void updateData(List<VideoDetails> newList) {
        this.videoList = newList;
        notifyDataSetChanged(); // Notify adapter about data changes
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img_thumbnail,img_more;
        TextView tv_name, tv_size;
        LinearLayout videoitem_ll;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            img_thumbnail = itemView.findViewById(R.id.img_thumbnail);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_size = itemView.findViewById(R.id.tv_size);
            videoitem_ll = itemView.findViewById(R.id.videoitem_ll);
            img_more = itemView.findViewById(R.id.btn_video_more);

        }
    }
}
