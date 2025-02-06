package com.uncertaincodes.maxplayer.models;

import java.util.ArrayList;
import java.util.List;

public class FolderDetails {
    private final String key;
    private final String name;
    private final List<VideoDetails> videos;

    public FolderDetails(String key, String name) {
        this.key = key;
        this.name = name;
        this.videos = new ArrayList<>();
    }
    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<VideoDetails> getVideos() {
        return videos;
    }

    public int getSize(){
        return videos.size();
    }

    public void setVideos(List<VideoDetails> videos) {
        this.videos.clear();
        this.videos.addAll(videos);
    }

    public void addVideo(VideoDetails video){
        this.videos.add(video);
    }

    public void remove(int index){
        this.videos.remove(index);
    }
}
