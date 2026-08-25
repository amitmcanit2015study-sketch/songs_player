package com.amitbharat.songsplayer.ui.online;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.data.remote.OpenMusicCatalogEngine;
import com.amitbharat.songsplayer.data.repository.MusicRepository;
import com.amitbharat.songsplayer.databinding.FragmentOnlineBinding;
import com.amitbharat.songsplayer.ui.adapter.SongAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.DownloadViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.SearchViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnlineFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private FragmentOnlineBinding binding;
    private MainViewModel mainViewModel;
    private SearchViewModel searchViewModel;
    private DownloadViewModel downloadViewModel;
    private SongAdapter songAdapter;
    private LinearLayoutManager layoutManager;

    private final List<Song> displayedSongs = new ArrayList<>();
    private final Set<Long> loadedSongIds = new HashSet<>();
    private String currentQuery = "";
    private String selectedCategory = "All";

    private boolean isLoadingMore = false;
    private int paginationPage = 1;

    // Endless suggestion keywords to fetch continuously as user scrolls
    private static final String[] ENDLESS_SUGGESTIONS = {
            "Trending Bollywood Songs 2026",
            "Arijit Singh Best Songs Mashup",
            "90s Hindi Romantic Melodies",
            "Indian Classical Sitar and Flute",
            "Best Sufi Ghazal Nusrat Rahat",
            "Deep House Club Mix 2026",
            "Lofi Hip Hop Chill Beats Study",
            "Top Global Hits Billboard 2026",
            "Coke Studio All Time Hits",
            "Acoustic Guitar Relaxing Hindi English",
            "Nonstop Bollywood Dance Megamix",
            "Sufi Rock Qawwali Collection",
            "Midnight Ambient Piano Meditation",
            "Punjabi Trending Superhits 2026",
            "Old Is Gold Lata Kishore Mukesh Hits"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOnlineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        downloadViewModel = new ViewModelProvider(requireActivity()).get(DownloadViewModel.class);

        layoutManager = new LinearLayoutManager(requireContext());
        binding.rvOnlineSongs.setLayoutManager(layoutManager);

        songAdapter = new SongAdapter(this);
        binding.rvOnlineSongs.setAdapter(songAdapter);

        // Preload curated tracks immediately so UI is never blank
        addSongs(MusicRepository.getCuratedOnlineTracks());

        setupSearchInput();
        setupCategoryChips();
        setupObservers();
        setupInfiniteScroll();

        // Refresh triggers live YouTube fetch
        binding.swipeRefreshOnline.setOnRefreshListener(() -> {
            paginationPage = 1;
            if (currentQuery.isEmpty() && "All".equals(selectedCategory)) {
                mainViewModel.fetchOnlineTrending();
            } else {
                String searchTarget = !currentQuery.isEmpty() ? currentQuery : selectedCategory;
                searchViewModel.setSearchQuery(searchTarget);
            }
        });

        // Trigger initial trending fetch on open
        mainViewModel.fetchOnlineTrending();
    }

    private void setupInfiniteScroll() {
        binding.rvOnlineSongs.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0 && !isLoadingMore) { // Scrolled downwards
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3 && totalItemCount > 0) {
                        loadMoreSongs();
                    }
                }
            }
        });
    }

    private void loadMoreSongs() {
        if (isLoadingMore) return;
        isLoadingMore = true;
        if (binding != null) {
            binding.progressLoadingMore.setVisibility(View.VISIBLE);
        }

        String targetQuery;
        if (!currentQuery.isEmpty()) {
            targetQuery = currentQuery;
        } else if (!"All".equalsIgnoreCase(selectedCategory)) {
            targetQuery = selectedCategory;
        } else {
            targetQuery = "Top Hindi Songs";
        }

        int pageToFetch = ++paginationPage;

        com.amitbharat.songsplayer.data.remote.UniversalStreamEngine.searchMusic(targetQuery, pageToFetch, moreSongs -> {
            if (binding == null) return;
            isLoadingMore = false;
            binding.progressLoadingMore.setVisibility(View.GONE);

            if (moreSongs != null && !moreSongs.isEmpty()) {
                addSongs(moreSongs);
            }
        });
    }

    private void addSongs(List<Song> newSongs) {
        if (newSongs == null) return;
        boolean added = false;
        for (Song s : newSongs) {
            if (loadedSongIds.add(s.getId())) {
                displayedSongs.add(s);
                added = true;
            }
        }
        if (added) {
            songAdapter.submitList(new ArrayList<>(displayedSongs));
            if (binding != null) {
                binding.tvEmptyOnline.setVisibility(View.GONE);
            }
        }
    }

    private void resetAndSetSongs(List<Song> newSongs) {
        displayedSongs.clear();
        loadedSongIds.clear();
        addSongs(newSongs);
    }

    private void setupSearchInput() {
        binding.etSearchOnline.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                paginationPage = 1;
                if (!currentQuery.isEmpty()) {
                    binding.tvOnlineHeader.setText(String.format("Online Results for \"%s\"", currentQuery));
                    searchViewModel.setSearchQuery(currentQuery);
                } else {
                    binding.tvOnlineHeader.setText(selectedCategory.equals("All") ? "Suggested Online Songs" : selectedCategory);
                    loadCategory(selectedCategory);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.etSearchOnline.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String q = binding.etSearchOnline.getText() != null ? binding.etSearchOnline.getText().toString().trim() : "";
                if (!q.isEmpty()) {
                    searchViewModel.setSearchQuery(q);
                }
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(binding.etSearchOnline.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupGenres.setOnCheckedStateChangeListener((group, checkedIds) -> {
            paginationPage = 1;
            if (checkedIds.contains(R.id.chip_indian_classical)) {
                selectedCategory = "Indian Classical";
            } else if (checkedIds.contains(R.id.chip_90s_hits)) {
                selectedCategory = "90s Hindi Hits";
            } else if (checkedIds.contains(R.id.chip_sufi_ghazal)) {
                selectedCategory = "Sufi Ghazal";
            } else if (checkedIds.contains(R.id.chip_deep_house)) {
                selectedCategory = "Deep House";
            } else if (checkedIds.contains(R.id.chip_lofi)) {
                selectedCategory = "Lofi Hindi";
            } else if (checkedIds.contains(R.id.chip_focus_work)) {
                selectedCategory = "Instrumental Relaxing";
            } else if (checkedIds.contains(R.id.chip_bollywood)) {
                selectedCategory = "Bollywood Mashup";
            } else {
                selectedCategory = "All";
            }

            if (currentQuery.isEmpty()) {
                binding.tvOnlineHeader.setText(selectedCategory.equals("All") ? "Suggested Online Songs" : selectedCategory);
                loadCategory(selectedCategory);
            }
        });
    }

    private void loadCategory(String category) {
        if ("All".equalsIgnoreCase(category)) {
            List<Song> trending = mainViewModel.getOnlineTrendingSongs().getValue();
            if (trending != null && !trending.isEmpty()) {
                resetAndSetSongs(trending);
            } else {
                resetAndSetSongs(MusicRepository.getCuratedOnlineTracks());
                mainViewModel.fetchOnlineTrending();
            }
        } else {
            searchViewModel.setSearchQuery(category);
        }
    }

    private void setupObservers() {
        mainViewModel.getOnlineTrendingSongs().observe(getViewLifecycleOwner(), songs -> {
            binding.swipeRefreshOnline.setRefreshing(false);
            if (songs != null && !songs.isEmpty() && currentQuery.isEmpty() && "All".equals(selectedCategory)) {
                resetAndSetSongs(songs);
            }
        });

        searchViewModel.getOnlineSearchResults().observe(getViewLifecycleOwner(), liveSongs -> {
            binding.swipeRefreshOnline.setRefreshing(false);
            if (liveSongs != null && !liveSongs.isEmpty()) {
                resetAndSetSongs(liveSongs);
            } else if (!currentQuery.isEmpty()) {
                displayedSongs.clear();
                loadedSongIds.clear();
                songAdapter.submitList(new ArrayList<>());
                binding.tvEmptyOnline.setVisibility(View.VISIBLE);
            }
        });

        // Observe Current Song & Playing state to highlight active track
        com.amitbharat.songsplayer.service.PlaybackService.getCurrentSongLive().observe(getViewLifecycleOwner(), song -> {
            Boolean isPlaying = com.amitbharat.songsplayer.service.PlaybackService.getIsPlayingLive().getValue();
            songAdapter.setPlayingState(song, isPlaying != null && isPlaying);
        });

        com.amitbharat.songsplayer.service.PlaybackService.getIsPlayingLive().observe(getViewLifecycleOwner(), isPlaying -> {
            Song song = com.amitbharat.songsplayer.service.PlaybackService.getCurrentSongLive().getValue();
            songAdapter.setPlayingState(song, isPlaying != null && isPlaying);
        });
    }

    @Override
    public void onSongClick(Song song, int position) {
        if (getActivity() instanceof MainActivity) {
            MainActivity act = (MainActivity) getActivity();
            act.playSongList(displayedSongs, position);
            if (song.isOnline()) {
                Intent intent = new Intent(requireContext(), com.amitbharat.songsplayer.ui.player.FullPlayerActivity.class);
                startActivity(intent);
            }
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
                com.amitbharat.songsplayer.ui.dialog.DownloadFormatDialog dialog = new com.amitbharat.songsplayer.ui.dialog.DownloadFormatDialog(song, (selectedSong, isVideo) -> {
                    downloadViewModel.startDownload(selectedSong, isVideo);
                    Toast.makeText(requireContext(), isVideo ? "Downloading video (.mp4)..." : "Downloading audio (.mp3)...", Toast.LENGTH_SHORT).show();
                });
                dialog.show(getParentFragmentManager(), "DownloadFormatDialog");
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
