package com.uncertaincodes.maxplayer.videoUtils;

import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.RequiresApi;

public class VideoDeletionHelper {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static void deleteVideo(Context context, String videoPath, ActivityResultLauncher<IntentSenderRequest> launcher) {
        Uri videoUri = getVideoUriFromPath(context, videoPath);

        if (videoUri != null) {
            try {
                int rowsDeleted = context.getContentResolver().delete(videoUri, null, null);
                if (rowsDeleted > 0) {
                    Toast.makeText(context, "Video deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete video", Toast.LENGTH_SHORT).show();
                }
            } catch (RecoverableSecurityException e) {
                // Handle permission request for deletion on Android 10+
                IntentSenderRequest request = new IntentSenderRequest.Builder(e.getUserAction().getActionIntent().getIntentSender()).build();
                launcher.launch(request);
            } catch (SecurityException e) {
                Toast.makeText(context, "Permission denied to delete this video", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Video file not found in MediaStore", Toast.LENGTH_SHORT).show();
        }
    }


    private static Uri getVideoUriFromPath(Context context, String videoPath) {
        Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.Media._ID};

        // Querying MediaStore to get the content URI for the video file
        Cursor cursor = context.getContentResolver().query(
                contentUri,
                projection,
                MediaStore.Video.Media.DATA + "=?",
                new String[]{videoPath},
                null
        );

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    return ContentUris.withAppendedId(contentUri, id);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }
}
