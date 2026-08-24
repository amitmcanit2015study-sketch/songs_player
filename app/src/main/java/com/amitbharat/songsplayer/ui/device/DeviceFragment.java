package com.amitbharat.songsplayer.ui.device;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.local.AppDatabase;
import com.amitbharat.songsplayer.data.mediastore.MediaStoreScanner;
import com.amitbharat.songsplayer.data.model.DeviceFolder;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.FragmentDeviceBinding;
import com.amitbharat.songsplayer.ui.adapter.DeviceFolderAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends Fragment implements DeviceFolderAdapter.OnFolderSongClickListener {

    private FragmentDeviceBinding binding;
    private MainViewModel mainViewModel;
    private MediaStoreScanner scanner;
    private DeviceFolderAdapter folderAdapter;

    private final List<DeviceFolder> allFolders = new ArrayList<>();
    private final List<DeviceFolder> displayedFolders = new ArrayList<>();
    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        scanner = new MediaStoreScanner(requireContext());

        folderAdapter = new DeviceFolderAdapter(this);
        binding.rvDeviceFolders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDeviceFolders.setAdapter(folderAdapter);

        setupSearch();

        binding.swipeRefreshDevice.setOnRefreshListener(this::loadDeviceFolders);
        binding.btnRescanDevice.setOnClickListener(v -> loadDeviceFolders());

        loadDeviceFolders();
    }

    private void setupSearch() {
        binding.etSearchDevice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim().toLowerCase();
                filterFolders();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDeviceFolders() {
        if (binding != null) {
            binding.swipeRefreshDevice.setRefreshing(true);
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<DeviceFolder> scanned = scanner.scanDeviceAudioFolders();
            if (getActivity() == null) return;

            getActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.swipeRefreshDevice.setRefreshing(false);

                allFolders.clear();
                if (scanned != null) {
                    allFolders.addAll(scanned);
                }
                filterFolders();
            });
        });
    }

    private void filterFolders() {
        displayedFolders.clear();

        for (DeviceFolder folder : allFolders) {
            if (currentSearch.isEmpty()) {
                displayedFolders.add(folder);
            } else {
                boolean matchesFolder = folder.getFolderName().toLowerCase().contains(currentSearch);
                List<Song> matchingSongs = new ArrayList<>();
                for (Song s : folder.getSongs()) {
                    if (s.getTitle().toLowerCase().contains(currentSearch) || s.getArtist().toLowerCase().contains(currentSearch)) {
                        matchingSongs.add(s);
                    }
                }

                if (matchesFolder || !matchingSongs.isEmpty()) {
                    DeviceFolder filteredFolder = new DeviceFolder(
                            folder.getFolderName(),
                            folder.getFolderPath(),
                            matchingSongs.isEmpty() ? folder.getSongs() : matchingSongs
                    );
                    filteredFolder.setExpanded(true);
                    displayedFolders.add(filteredFolder);
                }
            }
        }

        folderAdapter.setFolders(displayedFolders);
        binding.tvDeviceFoldersCount.setText(String.format("%d folders", displayedFolders.size()));

        if (displayedFolders.isEmpty()) {
            binding.layoutEmptyDevice.setVisibility(View.VISIBLE);
            binding.rvDeviceFolders.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyDevice.setVisibility(View.GONE);
            binding.rvDeviceFolders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSongClick(Song song, List<Song> folderSongs, int position) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).playSongList(folderSongs, position);
        }
    }

    @Override
    public void onSongFavorite(Song song) {
        mainViewModel.toggleFavorite(song);
    }

    @Override
    public void onSongMore(Song song, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.inflate(R.menu.song_item_menu);
        popup.getMenu().findItem(R.id.action_download).setVisible(false); // Already on device
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
            }
            return false;
        });
        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
