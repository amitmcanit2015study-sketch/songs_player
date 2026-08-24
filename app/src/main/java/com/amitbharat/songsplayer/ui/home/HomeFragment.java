package com.amitbharat.songsplayer.ui.home;

import android.os.Bundle;
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
import com.amitbharat.songsplayer.data.model.Album;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.FragmentHomeBinding;
import com.amitbharat.songsplayer.ui.adapter.AlbumAdapter;
import com.amitbharat.songsplayer.ui.adapter.SongAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.DownloadViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements SongAdapter.OnSongClickListener, AlbumAdapter.OnAlbumClickListener {

    private FragmentHomeBinding binding;
    private MainViewModel viewModel;
    private DownloadViewModel downloadViewModel;
    private SongAdapter allSongsAdapter;
    private AlbumAdapter trendingAlbumAdapter;
    private List<Song> currentSongList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        downloadViewModel = new ViewModelProvider(requireActivity()).get(DownloadViewModel.class);

        setupAdapters();
        setupObservers();

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.scanLocalMedia();
            viewModel.fetchOnlineTrending();
        });

        // Trigger initial online fetch
        viewModel.fetchOnlineTrending();

        binding.heroBannerCard.setOnClickListener(v -> {
            List<Song> onlineSongs = viewModel.getOnlineTrendingSongs().getValue();
            if (onlineSongs != null && !onlineSongs.isEmpty() && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playSongList(onlineSongs, 0);
            }
        });

        binding.btnSeeAllTrending.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity act = (MainActivity) getActivity();
                // Navigate to search screen with online filter
                View onlineNav = act.findViewById(R.id.nav_online);
                if (onlineNav != null) onlineNav.performClick();
            }
        });
    }

    private void setupAdapters() {
        allSongsAdapter = new SongAdapter(this);
        binding.rvAllSongs.setAdapter(allSongsAdapter);

        trendingAlbumAdapter = new AlbumAdapter(this);
        binding.rvTrending.setAdapter(trendingAlbumAdapter);
    }

    private void setupObservers() {
        viewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
            binding.swipeRefresh.setRefreshing(isScanning != null && isScanning);
        });

        viewModel.getAllSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                currentSongList = songs;
                allSongsAdapter.submitList(songs);
            }
        });

        viewModel.getOnlineTrendingSongs().observe(getViewLifecycleOwner(), onlineSongs -> {
            if (onlineSongs != null && !onlineSongs.isEmpty()) {
                List<Album> trendingCards = new ArrayList<>();
                for (Song song : onlineSongs) {
                    trendingCards.add(new Album(
                            song.getId(),
                            song.getTitle(),
                            song.getArtist(),
                            song.getArtUrl(),
                            1,
                            2024
                    ));
                }
                trendingAlbumAdapter.setAlbums(trendingCards);
            }
        });
    }

    @Override
    public void onSongClick(Song song, int position) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).playSongList(currentSongList, position);
        }
    }

    @Override
    public void onFavoriteClick(Song song, int position) {
        viewModel.toggleFavorite(song);
    }

    @Override
    public void onMoreClick(Song song, View anchorView, int position) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.inflate(R.menu.song_item_menu);
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
            } else if (id == R.id.action_download) {
                if (song.isOnline()) {
                    downloadViewModel.startDownload(song);
                    Toast.makeText(requireContext(), R.string.downloading, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Song is already available offline", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (id == R.id.action_favorite) {
                viewModel.toggleFavorite(song);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public void onAlbumClick(Album album) {
        // Play selected trending online track
        List<Song> onlineSongs = viewModel.getOnlineTrendingSongs().getValue();
        if (onlineSongs != null) {
            for (int i = 0; i < onlineSongs.size(); i++) {
                if (onlineSongs.get(i).getId() == album.getId()) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).playSongList(onlineSongs, i);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
