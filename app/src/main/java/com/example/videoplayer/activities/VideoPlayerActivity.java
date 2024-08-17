package com.example.videoplayer.activities;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.SimpleExoPlayer;
import androidx.media3.ui.PlayerView;
import com.example.videoplayer.R;
import com.example.videoplayer.models.VideoDetails;

import java.util.List;

import static com.example.videoplayer.BaseActivity.context;

public class VideoPlayerActivity extends AppCompatActivity {
    private List<VideoDetails> videoList;
    private int position;
    ExoPlayer player;
    PlayerView playerview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);

        hideBottomBar();

        playerview = findViewById(R.id.player);

        videoList = getIntent().getParcelableArrayListExtra("videoList");
        position = getIntent().getIntExtra("position", -1);
        player = new ExoPlayer.Builder(context).build();

        playerview.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(videoList.get(position).getPath());
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();

    }

    public void hideBottomBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View decodeView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decodeView.setSystemUiVisibility(uiOptions);
        }
    }


}