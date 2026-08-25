package com.amitbharat.songsplayer.ui.downloads;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amitbharat.songsplayer.data.model.DownloadItem;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.FragmentDownloadsBinding;
import com.amitbharat.songsplayer.ui.adapter.DownloadAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.DownloadViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadsFragment extends Fragment implements DownloadAdapter.OnDownloadItemClickListener {

    private FragmentDownloadsBinding binding;
    private DownloadViewModel viewModel;
    private DownloadAdapter adapter;
    private final List<DownloadItem> currentDownloads = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDownloadsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DownloadViewModel.class);

        adapter = new DownloadAdapter(this);
        binding.rvDownloads.setAdapter(adapter);

        setupBatchActions();
        setupObservers();
    }

    private void setupBatchActions() {
        binding.cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
        });

        binding.btnDeleteSelected.setOnClickListener(v -> {
            List<DownloadItem> selected = adapter.getSelectedItems();
            if (!selected.isEmpty()) {
                viewModel.deleteSelectedDownloads(selected);
                adapter.setSelectionMode(false);
                binding.batchToolbar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Deleted " + selected.size() + " downloads", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupObservers() {
        viewModel.getAllDownloads().observe(getViewLifecycleOwner(), items -> {
            currentDownloads.clear();
            if (items != null) {
                currentDownloads.addAll(items);
            }
            adapter.setDownloads(new ArrayList<>(currentDownloads));

            if (currentDownloads.isEmpty()) {
                binding.layoutEmptyDownloads.setVisibility(View.VISIBLE);
                binding.rvDownloads.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyDownloads.setVisibility(View.GONE);
                binding.rvDownloads.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDownloadClick(DownloadItem item) {
        if (item.getStatus() == DownloadItem.Status.COMPLETED && item.getLocalPath() != null) {
            List<DownloadItem> allDownloads = viewModel.getAllDownloads().getValue();
            List<Song> downloadedSongs = new ArrayList<>();
            int targetIndex = 0;

            if (allDownloads != null) {
                for (DownloadItem di : allDownloads) {
                    if (di.getStatus() == DownloadItem.Status.COMPLETED && di.getLocalPath() != null) {
                        File f = new File(di.getLocalPath());
                        if (f.exists()) {
                            Song s = new Song(
                                    di.getSongId(),
                                    di.getTitle(),
                                    di.getArtist(),
                                    "Downloaded",
                                    0,
                                    0,
                                    di.getLocalPath(),
                                    false,
                                    null,
                                    di.getArtUrl(),
                                    false,
                                    0,
                                    di.getDownloadDate(),
                                    di.getTotalBytes()
                            );
                            if (di.getSongId() == item.getSongId()) {
                                targetIndex = downloadedSongs.size();
                            }
                            downloadedSongs.add(s);
                        }
                    }
                }
            }

            if (!downloadedSongs.isEmpty() && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playSongList(downloadedSongs, targetIndex);
            }
        }
    }

    @Override
    public void onDownloadActionClick(DownloadItem item) {
        viewModel.deleteDownload(item);
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (selectedCount > 0) {
            binding.batchToolbar.setVisibility(View.VISIBLE);
        } else if (!adapter.isSelectionMode()) {
            binding.batchToolbar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
