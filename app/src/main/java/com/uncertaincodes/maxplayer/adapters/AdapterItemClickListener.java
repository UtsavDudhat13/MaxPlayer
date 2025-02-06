package com.uncertaincodes.maxplayer.adapters;

import com.uncertaincodes.maxplayer.models.VideoDetails;

public interface AdapterItemClickListener<T> {
    void onClicked(T data, int position);
    void onDelete(VideoDetails video);
    void onShare(VideoDetails video);
    void onInfo(VideoDetails video);
}
