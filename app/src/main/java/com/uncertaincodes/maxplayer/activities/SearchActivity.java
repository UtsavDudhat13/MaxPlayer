package com.uncertaincodes.maxplayer.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class SearchActivity extends AppCompatActivity implements AdapterItemClickListener<VideoDetails> {
    private RecyclerView rvVideos;
    private VideoAdapter videoAdapter;
    private EditText editText;
    private ImageView closeBtn,back_btn;
    private TextView tvNoVideos;  // Reference to "No Videos" TextView
    private ArrayList<VideoDetails> videoList = new ArrayList<>();
    private ArrayList<VideoDetails> filteredList = new ArrayList<>(); // For filtered data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.white));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        backPressed();

        LinearLayout nativeAdLayout = findViewById(R.id.ll_naive_ad);
        MyNativeBanner.getInstance(this).loadNativeAd(nativeAdLayout);

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

        rvVideos = findViewById(R.id.rv_videos_search);
        editText = findViewById(R.id.editText);
        closeBtn = findViewById(R.id.close_btn);
        tvNoVideos = findViewById(R.id.tv_no_videos);
        back_btn = findViewById(R.id.back_arrow);

        rvVideos.setLayoutManager(new LinearLayoutManager(this));
        videoAdapter = new VideoAdapter(videoList, SearchActivity.this);
        rvVideos.setAdapter(videoAdapter);

        editText.requestFocus();
        closeBtn.setVisibility(View.INVISIBLE);
        tvNoVideos.setVisibility(View.GONE); // Hide "No Videos" message by default

        BaseActivity.getVideoFetcher().fetchAllVideos(new OnEventListener<List<VideoDetails>>() {
            @Override
            public void onSuccess(List<VideoDetails> data) {
                super.onSuccess(data);
                videoList.clear();
                videoList.addAll(data);
            }
        });

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyInterstitial.create().showIntervalAd(SearchActivity.this, () -> {
                    getOnBackPressedDispatcher().onBackPressed();
                });
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    closeBtn.setVisibility(View.VISIBLE);
                    filterVideos(charSequence.toString());
                } else {
                    closeBtn.setVisibility(View.INVISIBLE);
                    resetSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        closeBtn.setOnClickListener(view -> {
            editText.setText("");
            resetSearch();
        });

    }

    // Filter videos based on the search text
    private void filterVideos(String query) {
        filteredList.clear();
        for (VideoDetails video : videoList) {
            if (video.getDisplayName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(video);
            }
        }

        // Check if there are any results after filtering
        if (filteredList.isEmpty()) {
            rvVideos.setVisibility(View.GONE);  // Hide RecyclerView
            tvNoVideos.setVisibility(View.VISIBLE);  // Show No Videos message
        } else {
            rvVideos.setVisibility(View.VISIBLE);  // Show RecyclerView
            tvNoVideos.setVisibility(View.GONE);  // Hide No Videos message
        }

        // Update the adapter with the filtered list
        videoAdapter.updateData(filteredList);
    }

    // Reset search and show all videos
    private void resetSearch() {
        videoAdapter.updateData(videoList); // Restore the original video list
        // Check if the video list is empty
        if (videoList.isEmpty()) {
            rvVideos.setVisibility(View.GONE);  // Hide RecyclerView
            tvNoVideos.setVisibility(View.VISIBLE);  // Show No Videos message
        } else {
            rvVideos.setVisibility(View.VISIBLE);  // Show RecyclerView
            tvNoVideos.setVisibility(View.GONE);  // Hide No Videos message
        }
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
        LayoutInflater inflater = LayoutInflater.from(SearchActivity.this);
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

        new MaterialAlertDialogBuilder(SearchActivity.this)
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

    private void backPressed() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                MyInterstitial.create().showIntervalAd(SearchActivity.this, () -> {
                    finish();
                });
            }
        });
    }

}
