package com.example.videoplayer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.videoplayer.BaseActivity;
import com.example.videoplayer.R;
import com.example.videoplayer.adapters.AdapterItemClickListener;
import com.example.videoplayer.adapters.VideoAdapter;
import com.example.videoplayer.models.VideoDetails;
import com.example.videoplayer.utils.Utils;
import com.example.videoplayer.videoUtils.OnEventListener;

import com.example.videoplayer.videoUtils.VideoDeletionHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static com.example.videoplayer.BaseActivity.context;

public class AllVideoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterItemClickListener<VideoDetails> {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rvVideos;
    private TextView tvTotalVideos;
    private VideoAdapter videoAdapter;
    private ArrayList<VideoDetails> videoList = new ArrayList<>();
    private ImageView btn_short;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_video, container, false);

        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(this);
        rvVideos = view.findViewById(R.id.rv_videos);
        tvTotalVideos = view.findViewById(R.id.tv_total_video);
        btn_short = view.findViewById(R.id.shorting_btn);

        onRefresh();
        deleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        handleDeletionResult(result.getResultCode());
                        Toast.makeText(getContext(), "Video deletion completed", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Video deletion failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btn_short.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSortingDialog();
            }
        });

        return view;
    }

    private void showSortingDialog() {


        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_sort_option, null);

        // Reference to RadioGroup
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup_sortBy);


        // Build and display the dialog
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Sort Videos")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle sorting logic based on selected option
                    int selectedId = radioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = dialogView.findViewById(selectedId);

                    if (selectedId != -1) {  // Ensure a button is selected
                        if (selectedId == R.id.radioButton_sizeAsc) {
                            sortVideosBySize(true);  // Small to High
                        } else if (selectedId == R.id.radioButton_sizeDesc) {
                            sortVideosBySize(false); // High to Small
                        } else if (selectedId == R.id.radioButton_nameAsc) {
                            sortVideosByName(true);  // A to Z
                        } else if (selectedId == R.id.radioButton_nameDesc) {
                            sortVideosByName(false); // Z to A
                        } else {

                        }
                    } else {

                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sortVideosBySize(boolean ascending) {
        Collections.sort(videoList, new Comparator<VideoDetails>() {
            @Override
            public int compare(VideoDetails video1, VideoDetails video2) {
                if (ascending) {
                    return Long.compare(video1.getSize(), video2.getSize()); // Small to High
                } else {
                    return Long.compare(video2.getSize(), video1.getSize()); // High to Small
                }
            }
        });

        videoAdapter.notifyDataSetChanged();
        updateTotalVideoCount();
    }

    private void sortVideosByName(boolean ascending) {
        Collections.sort(videoList, new Comparator<VideoDetails>() {
            @Override
            public int compare(VideoDetails video1, VideoDetails video2) {
                if (ascending) {
                    return video1.getDisplayName().compareToIgnoreCase(video2.getDisplayName());  // A to Z
                } else {
                    return video2.getDisplayName().compareToIgnoreCase(video1.getDisplayName());  // Z to A
                }
            }
        });

        videoAdapter.notifyDataSetChanged();
        updateTotalVideoCount();
    }
    @SuppressLint("NotifyDataSetChanged")
    private void showVideos() {
        BaseActivity.getVideoFetcher().fetchAllVideos(new OnEventListener<List<VideoDetails>>() {
            @Override
            public void onSuccess(List<VideoDetails> data) {
                super.onSuccess(data);
                videoList.clear();
                videoList.addAll(data);

                refreshLayout.setRefreshing(false);
            }
        });
        videoAdapter = new VideoAdapter(videoList,AllVideoFragment.this);
        rvVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVideos.setAdapter(videoAdapter);
        videoAdapter.notifyDataSetChanged();
        updateTotalVideoCount();
    }

    private void updateTotalVideoCount() {
        String totalVideosText = String.format(getString(R.string.totalVideos),String.valueOf(videoList.size()));
        tvTotalVideos.setText(totalVideosText);
    }

    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);

        showVideos();
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onClicked(VideoDetails data, int position) {
//        Intent intent = new Intent(getContext(), PlayerActivity.class);
//        intent.putParcelableArrayListExtra("videoList", videoList);
//        intent.putExtra("position",position);
//        getContext().startActivity(intent);

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
        LayoutInflater inflater = LayoutInflater.from(getContext());
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

        new MaterialAlertDialogBuilder(getContext())
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
            Toast.makeText(getContext(), "Video deletion failed", Toast.LENGTH_SHORT).show();
        }
    }

}
