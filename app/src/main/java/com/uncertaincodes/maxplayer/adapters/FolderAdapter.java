package com.uncertaincodes.maxplayer.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.ads.MyInterstitial;
import com.uncertaincodes.maxplayer.models.FolderDetails;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class FolderAdapter  extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private List<FolderDetails> list;
    private final AdapterItemClickListener<FolderDetails> listener;

    public FolderAdapter(List<FolderDetails> list, AdapterItemClickListener<FolderDetails> listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = (LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_folder, viewGroup, false);
        return new FolderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull FolderAdapter.ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        FolderDetails data = list.get(position);

        holder.tv_folder_name.setText(data.getName());
        holder.tv_video_size.setText(String.format(Locale.US,"%d Videos", data.getSize()));

        holder.itemView.setOnClickListener(v -> {
            listener.onClicked(data, position);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv_video_size, tv_folder_name;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);


            tv_video_size = itemView.findViewById(R.id.tv_video_size);
            tv_folder_name = itemView.findViewById(R.id.tv_folder_name);

        }
    }
}
