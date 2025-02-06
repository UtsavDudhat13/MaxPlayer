package com.uncertaincodes.maxplayer.videoUtils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.uncertaincodes.maxplayer.BaseActivity;
import com.uncertaincodes.maxplayer.models.FolderDetails;
import com.uncertaincodes.maxplayer.models.VideoDetails;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class VideoFetcher {

    protected HashMap<String, FolderDetails> backupList = new HashMap<>();
    protected List<String> keys = new ArrayList<>();
    // Singleton instance
    private static VideoFetcher instance;

    private VideoFetcher() {
    }

    public static synchronized VideoFetcher getInstance() {
        if (instance == null) {
            instance = new VideoFetcher();
        }
        return instance;
    }

    // Fetch all videos with details
    public void fetchAllVideos(OnEventListener<List<VideoDetails>> listener) {
        List<VideoDetails> videoDetailsList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = BaseActivity.getContext().getContentResolver().query(uri, projection, null, null, sortOrder);
        if (cursor != null) {
            int displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
            int folderNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH);
            int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while (cursor.moveToNext()) {
                String displayName = cursor.getString(displayNameColumn);
                String path = cursor.getString(dataColumn);
                long size = cursor.getLong(sizeColumn);
                long duration = cursor.getLong(durationColumn);
                String modifiedDate = sdf.format(new Date(cursor.getLong(dateModifiedColumn) * 1000L));
                String folderName = cursor.getString(folderNameColumn);
                String resolution = cursor.getInt(widthColumn) + "x" + cursor.getInt(heightColumn);
                String dateAdded = sdf.format(new Date(cursor.getLong(dateAddedColumn) * 1000L));

                VideoDetails videoDetails = new VideoDetails(displayName, path, size, duration, modifiedDate, folderName, resolution, dateAdded);
                videoDetailsList.add(videoDetails);
            }
            cursor.close();
        }
        listener.onSuccess(videoDetailsList);
    }


    public void fetchAllFolders() {
        ContentResolver contentResolver = BaseActivity.getContext().getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DATE_ADDED
        };

        Cursor cursor = contentResolver.query(uri, projection, null, null, "datetaken DESC");

        if (cursor != null) {

            int displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
            int folderNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
            int widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH);
            int heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            backupList.clear();
            keys.clear();
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(displayNameColumn);
                String path = cursor.getString(dataColumn);
                long size = cursor.getLong(sizeColumn);
                long duration = cursor.getLong(durationColumn);
                String modifiedDate = sdf.format(new Date(cursor.getLong(dateModifiedColumn) * 1000L));
                String folderName = cursor.getString(folderNameColumn);
                String resolution = cursor.getInt(widthColumn) + "x" + cursor.getInt(heightColumn);
                String dateAdded = sdf.format(new Date(cursor.getLong(dateAddedColumn) * 1000L));

                VideoDetails videoDetails =new VideoDetails(displayName, path, size, duration, modifiedDate, folderName, resolution, dateAdded);

                String bucket_id = getBucketPath(path);
                String bucket_name = getBucketName(bucket_id);

                if (new File(path).exists()) {
                    if (backupList.containsKey(bucket_id)) {
                        FolderDetails bucket = backupList.get(bucket_id);
                        if (bucket != null) {
                            bucket.addVideo(videoDetails);
                        }
                    } else {
                        FolderDetails bucket = new FolderDetails(bucket_id, bucket_name);
                        bucket.addVideo(videoDetails);
                        backupList.put(bucket_id, bucket);
                        keys.add(bucket_id);
                    }
                }
            }
            cursor.close();
        }
    }

    public void getVideos(String key, OnEventListener<List<VideoDetails>> listener) {
        if (backupList.containsKey(key)) {
            FolderDetails folder = backupList.get(key);
            if (folder != null) {
                listener.onSuccess(folder.getVideos());
                return;
            }
        }
        listener.onFailure();
    }

    public void getFolder(OnEventListener<List<FolderDetails>> listener) {
        if (!backupList.isEmpty()) {
            List<FolderDetails> folders = new ArrayList<>();
            for (String key : keys) {
                folders.add(backupList.get(key));
            }
            listener.onSuccess(folders);
            return;
        }
        listener.onFailure();
    }

    public String getBucketName(String path) {
        if (path.lastIndexOf('/') > 0) {
            return path.substring(path.lastIndexOf('/') + 1);
        } else {
            return "";
        }
    }

    public String getBucketPath(String path) {
        if (path.lastIndexOf('/') > 0) {
            return path.substring(0, path.lastIndexOf('/'));
        } else {
            return "";
        }
    }
}
