package com.uncertaincodes.maxplayer.models;


import android.os.Parcel;
import android.os.Parcelable;

public class VideoDetails implements Parcelable {
    private String displayName;
    private String path;
    private long size;
    private long duration;
    private String modifiedDate;
    private String folderName;
    private String resolution;
    private String dateAdded;

    // Constructor
    public VideoDetails(String displayName, String path, long size, long duration, String modifiedDate,
                        String folderName, String resolution, String dateAdded) {
        this.displayName = displayName;
        this.path = path;
        this.size = size;
        this.duration = duration;
        this.modifiedDate = modifiedDate;
        this.folderName = folderName;
        this.resolution = resolution;
        this.dateAdded = dateAdded;
    }

    // Parcelable implementation
    protected VideoDetails(Parcel in) {
        displayName = in.readString();
        path = in.readString();
        size = in.readLong();
        duration = in.readLong();
        modifiedDate = in.readString();
        folderName = in.readString();
        resolution = in.readString();
        dateAdded = in.readString();
    }

    public static final Creator<VideoDetails> CREATOR = new Creator<VideoDetails>() {
        @Override
        public VideoDetails createFromParcel(Parcel in) {
            return new VideoDetails(in);
        }

        @Override
        public VideoDetails[] newArray(int size) {
            return new VideoDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeLong(duration);
        dest.writeString(modifiedDate);
        dest.writeString(folderName);
        dest.writeString(resolution);
        dest.writeString(dateAdded);
    }

    // Getters (same as before)
    public String getDisplayName() {
        return displayName;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getDuration() {
        return duration;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getResolution() {
        return resolution;
    }

    public String getDateAdded() {
        return dateAdded;
    }
}

