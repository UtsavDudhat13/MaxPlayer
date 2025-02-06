package com.uncertaincodes.maxplayer.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.uncertaincodes.maxplayer.BaseActivity;
import com.uncertaincodes.maxplayer.R;
import com.uncertaincodes.maxplayer.activities.VideoFilesActivity;
import com.uncertaincodes.maxplayer.adapters.AdapterItemClickListener;
import com.uncertaincodes.maxplayer.adapters.FolderAdapter;
import com.uncertaincodes.maxplayer.models.FolderDetails;
import com.uncertaincodes.maxplayer.models.VideoDetails;
import com.uncertaincodes.maxplayer.videoUtils.OnEventListener;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterItemClickListener<FolderDetails> {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rv_folders;
    private TextView tv_total_folder;
    private final List<FolderDetails> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, container, false);

        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary);
        refreshLayout.setOnRefreshListener(this);
        rv_folders = view.findViewById(R.id.rv_folders);
        tv_total_folder = view.findViewById(R.id.tv_total_folder);

        onRefresh();

        return view;
    }


    @Override
    public void onRefresh() {
        refreshLayout.setRefreshing(true);

        list.clear();
        BaseActivity.getVideoFetcher().getFolder(new OnEventListener<List<FolderDetails>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<FolderDetails> data) {
                super.onSuccess(data);
                list.addAll(data);

                FolderAdapter adapter = new FolderAdapter(list,FolderFragment.this);
                rv_folders.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                updateTotalFolderCount();
                refreshLayout.setRefreshing(false);
            }
        });

    }

    private void updateTotalFolderCount() {
        String totalVideosText = String.format(getString(R.string.totalFolders), String.valueOf(list.size()));
        tv_total_folder.setText(totalVideosText);
    }

    @Override
    public void onClicked(FolderDetails data, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("key", data.getKey());
        bundle.putString("name", data.getName());
        Intent intent = new Intent(getContext(), VideoFilesActivity.class);
        intent.putExtra("folderBundle", bundle);
        startActivity(intent);
    }

    @Override
    public void onDelete(VideoDetails video) {

    }

    @Override
    public void onShare(VideoDetails video) {

    }

    @Override
    public void onInfo(VideoDetails video) {

    }
}
