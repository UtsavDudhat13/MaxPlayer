package com.uncertaincodes.maxplayer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uncertaincodes.maxplayer.BaseActivity;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.adapters.AdapterItemClickListener;
import com.uncertaincodes.maxplayer.adapters.VideoAdapter;
import com.uncertaincodes.maxplayer.ads.MyInterstitial;
import com.uncertaincodes.maxplayer.ads.MyNativeBanner;
import com.uncertaincodes.maxplayer.models.VideoDetails;
import com.uncertaincodes.maxplayer.utils.Utils;
import com.uncertaincodes.maxplayer.videoUtils.OnEventListener;
import com.uncertaincodes.maxplayer.videoUtils.VideoDeletionHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.uncertaincodes.maxplayer.BaseActivity.context;

public class VideoFilesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, AdapterItemClickListener<VideoDetails> {

    private String key;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rvVideos;
    private VideoAdapter videoAdapter;
    private ArrayList<VideoDetails> videoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener(v -> {
            MyInterstitial.create().showIntervalAd(VideoFilesActivity.this, () -> {
                getOnBackPressedDispatcher().onBackPressed();
            });
        });

        LinearLayout nativeAdLayout = findViewById(R.id.ll_naive_ad);
        MyNativeBanner.getInstance(this).loadNativeAd(nativeAdLayout);

        Bundle bundle = getIntent().getBundleExtra("folderBundle");
        deleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        handleDeletionResult(result.getResultCode());
                    } else {
                        Toast.makeText(this, "Video deletion failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        if (bundle != null) {
            key = bundle.getString("key");
            String name = bundle.getString("name");

            toolbar.setTitle(name);

            refreshLayout = findViewById(R.id.refreshLayout);
            refreshLayout.setColorSchemeResources(R.color.colorPrimary);
            refreshLayout.setOnRefreshListener(this);
            rvVideos = findViewById(R.id.rv_videos);

            onRefresh();

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void showVideos() {
        BaseActivity.getVideoFetcher().getVideos(key, new OnEventListener<List<VideoDetails>>() {
            @Override
            public void onSuccess(List<VideoDetails> data) {
                super.onSuccess(data);
                videoList.clear();
                videoList.addAll(data);

                refreshLayout.setRefreshing(false);
            }
        });

        videoAdapter = new VideoAdapter(videoList,VideoFilesActivity.this);
        rvVideos.setLayoutManager(new LinearLayoutManager(this));
        rvVideos.setAdapter(videoAdapter);
        videoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);
        showVideos();
    }

    @Override
    public void onClicked(VideoDetails data, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putParcelableArrayList("videoArrayList", videoList);
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("VideoListBundle", bundle);
        startActivity(intent);
    }
    private ActivityResultLauncher<IntentSenderRequest> deleteLauncher;

    VideoDetails currentVideoToDelete;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onDelete(VideoDetails video) {
        currentVideoToDelete = video; // Store the video to be deleted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VideoDeletionHelper.deleteVideo(BaseActivity.getContext(), video.getPath(), deleteLauncher);
        } else {
            // Directly delete if on a lower API level
            VideoDeletionHelper.deleteVideo(BaseActivity.getContext(), video.getPath(), null);
        }
    }

    @Override
    public void onShare(VideoDetails video) {
        File videoFile = new File(video.getPath());
        Uri uriPath = Uri.parse(videoFile.getPath());

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
        shareIntent.setType("video/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "send"));
    }

    @Override
    public void onInfo(VideoDetails video) {
        LayoutInflater inflater = LayoutInflater.from(VideoFilesActivity.this);
        View dialogView = inflater.inflate(R.layout.video_info_dialog, null);

        TextView videoName = dialogView.findViewById(R.id.textView_video_name);
        TextView videoLocation = dialogView.findViewById(R.id.textView_video_location);
        TextView videoDate = dialogView.findViewById(R.id.textView_video_date);
        TextView videoSize = dialogView.findViewById(R.id.textView_video_size);
        TextView videoLength = dialogView.findViewById(R.id.textView_video_length);

        TextView videoResolution = dialogView.findViewById(R.id.textView_video_resolution);

        // Populate the dialog with video information
        videoName.setText(video.getDisplayName());
        videoLocation.setText(video.getPath());
        videoDate.setText(video.getDateAdded());
        videoSize.setText(Utils.timeConversion(video.getDuration()));
        videoLength.setText(String.format(Formatter.formatFileSize(context, video.getSize())));
        videoResolution.setText(video.getResolution());

        new MaterialAlertDialogBuilder(VideoFilesActivity.this)
                .setTitle("Video Information")
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }
    private void handleDeletionResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            // Continue with deletion if permission is granted
            if (currentVideoToDelete != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VideoDeletionHelper.deleteVideo(
                            BaseActivity.getContext(),
                            currentVideoToDelete.getPath(),
                            deleteLauncher
                    );
                }
            }
        } else {
            Toast.makeText(this, "Video deletion failed", Toast.LENGTH_SHORT).show();
        }
    }
}