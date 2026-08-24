package com.amitbharat.songsplayer.ui.offline;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.DownloadItem;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.FragmentOfflineBinding;
import com.amitbharat.songsplayer.ui.adapter.SongAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.DownloadViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;
import com.amitbharat.songsplayer.utils.FormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OfflineFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private FragmentOfflineBinding binding;
    private MainViewModel mainViewModel;
    private DownloadViewModel downloadViewModel;
    private SongAdapter songAdapter;

    private final List<Song> allOfflineSongs = new ArrayList<>();
    private final List<Song> displayedSongs = new ArrayList<>();
    private String currentQuery = "";
    private int currentFilter = R.id.chip_offline_all;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOfflineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        downloadViewModel = new ViewModelProvider(requireActivity()).get(DownloadViewModel.class);

        songAdapter = new SongAdapter(this);
        binding.rvOfflineSongs.setAdapter(songAdapter);

        setupSearch();
        setupFilterChips();
        setupObservers();

        // Refresh triggers storage rescan
        binding.swipeRefreshOffline.setOnRefreshListener(() -> {
            mainViewModel.scanLocalMedia();
        });

        binding.btnRescanMedia.setOnClickListener(v -> mainViewModel.scanLocalMedia());
    }

    private void setupSearch() {
        binding.etSearchOffline.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim().toLowerCase();
                filterSongs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        binding.chipGroupOfflineFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_offline_downloads)) {
                currentFilter = R.id.chip_offline_downloads;
            } else if (checkedIds.contains(R.id.chip_offline_favorites)) {
                currentFilter = R.id.chip_offline_favorites;
            } else {
                currentFilter = R.id.chip_offline_all;
            }
            filterSongs();
        });
    }

    private void setupObservers() {
        mainViewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
            binding.swipeRefreshOffline.setRefreshing(isScanning != null && isScanning);
        });

        // Combine local scanned songs and completed downloads
        mainViewModel.getAllSongs().observe(getViewLifecycleOwner(), localSongs -> {
            updateCombinedOfflineList();
        });

        downloadViewModel.getCompletedDownloads().observe(getViewLifecycleOwner(), completedDownloads -> {
            updateCombinedOfflineList();
        });

        // Observe active downloads to show progress percentage
        downloadViewModel.getAllDownloads().observe(getViewLifecycleOwner(), allDownloads -> {
            if (allDownloads != null) {
                DownloadItem activeItem = null;
                for (DownloadItem item : allDownloads) {
                    if (item.getStatus() == DownloadItem.Status.DOWNLOADING) {
                        activeItem = item;
                        break;
                    }
                }

                if (activeItem != null) {
                    binding.cardActiveDownload.setVisibility(View.VISIBLE);
                    binding.tvActiveDownloadTitle.setText(String.format("Downloading: %s", activeItem.getTitle()));
                    binding.tvActiveDownloadPercent.setText(String.format("%d%%", activeItem.getProgress()));
                    binding.tvActiveDownloadPercent.setTextColor(0xFFFF9800); // Orange mark for downloading
                    binding.progressActiveDownload.setProgress(activeItem.getProgress());
                    binding.progressActiveDownload.setIndicatorColor(0xFFFF9800); // Orange indicator

                    String sizeStr = FormatUtils.formatFileSize(activeItem.getTotalBytes());
                    binding.tvActiveDownloadStatus.setText(String.format("Downloading in background • %s", sizeStr));
                } else {
                    binding.cardActiveDownload.setVisibility(View.GONE);
                }
            } else {
                binding.cardActiveDownload.setVisibility(View.GONE);
            }
        });
    }

    private void updateCombinedOfflineList() {
        allOfflineSongs.clear();

        List<Song> local = mainViewModel.getAllSongs().getValue();
        if (local != null) {
            for (Song s : local) {
                if (!s.isOnline() && !allOfflineSongs.contains(s)) {
                    allOfflineSongs.add(s);
                }
            }
        }

        List<DownloadItem> downloads = downloadViewModel.getCompletedDownloads().getValue();
        if (downloads != null) {
            for (DownloadItem item : downloads) {
                if (item.getLocalPath() != null && new File(item.getLocalPath()).exists()) {
                    Song downloadedSong = new Song(
                            item.getSongId(),
                            item.getTitle(),
                            item.getArtist(),
                            "Downloaded",
                            0,
                            0,
                            item.getLocalPath(),
                            false,
                            null,
                            item.getArtUrl(),
                            false,
                            0,
                            item.getDownloadDate(),
                            item.getTotalBytes()
                    );
                    if (!allOfflineSongs.contains(downloadedSong)) {
                        allOfflineSongs.add(downloadedSong);
                    }
                }
            }
        }

        filterSongs();
    }

    private void filterSongs() {
        displayedSongs.clear();

        for (Song s : allOfflineSongs) {
            if (currentFilter == R.id.chip_offline_downloads) {
                if (!"Downloaded".equalsIgnoreCase(s.getAlbum())) {
                    continue;
                }
            } else if (currentFilter == R.id.chip_offline_favorites) {
                if (!s.isFavorite()) {
                    continue;
                }
            }

            if (!currentQuery.isEmpty()) {
                boolean matchesTitle = s.getTitle() != null && s.getTitle().toLowerCase().contains(currentQuery);
                boolean matchesArtist = s.getArtist() != null && s.getArtist().toLowerCase().contains(currentQuery);
                boolean matchesAlbum = s.getAlbum() != null && s.getAlbum().toLowerCase().contains(currentQuery);
                if (!matchesTitle && !matchesArtist && !matchesAlbum) {
                    continue;
                }
            }

            displayedSongs.add(s);
        }

        songAdapter.submitList(new ArrayList<>(displayedSongs));
        binding.tvOfflineCount.setText(String.format("%d tracks", displayedSongs.size()));

        if (displayedSongs.isEmpty()) {
            binding.layoutEmptyOffline.setVisibility(View.VISIBLE);
            binding.rvOfflineSongs.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyOffline.setVisibility(View.GONE);
            binding.rvOfflineSongs.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSongClick(Song song, int position) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).playSongList(displayedSongs, position);
        }
    }

    @Override
    public void onFavoriteClick(Song song, int position) {
        mainViewModel.toggleFavorite(song);
    }

    @Override
    public void onMoreClick(Song song, View anchorView, int position) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.inflate(R.menu.song_item_menu);
        popup.getMenu().findItem(R.id.action_download).setVisible(false); // Already downloaded / offline
        popup.getMenu().findItem(R.id.action_delete).setVisible(true);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_play_next) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity act = (MainActivity) getActivity();
                    if (act.getPlaybackService() != null) {
                        act.getPlaybackService().playNextInQueue(song);
                        Toast.makeText(requireContext(), R.string.added_to_queue, Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            } else if (id == R.id.action_add_to_queue) {
                if (getActivity() instanceof MainActivity) {
                    MainActivity act = (MainActivity) getActivity();
                    if (act.getPlaybackService() != null) {
                        act.getPlaybackService().addToQueue(song);
                        Toast.makeText(requireContext(), R.string.added_to_queue, Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            } else if (id == R.id.action_favorite) {
                mainViewModel.toggleFavorite(song);
                return true;
            } else if (id == R.id.action_delete) {
                deleteSongFromDevice(song);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void deleteSongFromDevice(Song song) {
        com.amitbharat.songsplayer.data.local.AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Delete physical file from device storage
                if (song.getPlayableUri() != null) {
                    File file = new File(song.getPlayableUri());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                // Delete from Room Database
                com.amitbharat.songsplayer.data.local.AppDatabase db = com.amitbharat.songsplayer.data.local.AppDatabase.getDatabase(requireContext());
                db.downloadDao().deleteDownload(song.getId());
                db.songDao().deleteSongById(song.getId());

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Deleted \"" + song.getTitle() + "\" from device", Toast.LENGTH_SHORT).show();
                        updateCombinedOfflineList();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
