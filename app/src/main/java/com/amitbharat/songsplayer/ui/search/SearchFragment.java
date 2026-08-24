package com.amitbharat.songsplayer.ui.search;

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
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.databinding.FragmentSearchBinding;
import com.amitbharat.songsplayer.ui.adapter.SongAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.DownloadViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.SearchViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private FragmentSearchBinding binding;
    private SearchViewModel searchViewModel;
    private MainViewModel mainViewModel;
    private DownloadViewModel downloadViewModel;
    private SongAdapter songAdapter;
    private final List<Song> displayedSongs = new ArrayList<>();
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        downloadViewModel = new ViewModelProvider(requireActivity()).get(DownloadViewModel.class);

        songAdapter = new SongAdapter(this);
        binding.rvSearchResults.setAdapter(songAdapter);

        setupSearchInput();
        setupFilterChips();
        setupGenreChips();
        setupObservers();
    }

    private void setupSearchInput() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                searchViewModel.setSearchQuery(currentQuery);
                updateHeader();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_online)) {
                searchViewModel.setFilterType(SearchViewModel.FilterType.ONLINE);
            } else if (checkedIds.contains(R.id.chip_local)) {
                searchViewModel.setFilterType(SearchViewModel.FilterType.LOCAL);
            } else {
                searchViewModel.setFilterType(SearchViewModel.FilterType.ALL);
            }
            updateHeader();
            updateDisplayedList();
        });
    }

    private void setupGenreChips() {
        binding.chipGenreTrending.setOnClickListener(v -> searchKeyword("Trending"));
        binding.chipGenreLofi.setOnClickListener(v -> searchKeyword("Lofi"));
        binding.chipGenrePop.setOnClickListener(v -> searchKeyword("Pop"));
        binding.chipGenreRock.setOnClickListener(v -> searchKeyword("Rock"));
        binding.chipGenreElectronic.setOnClickListener(v -> searchKeyword("Electronic"));
        binding.chipGenreAcoustic.setOnClickListener(v -> searchKeyword("Acoustic"));
        binding.chipGenreAmbient.setOnClickListener(v -> searchKeyword("Ambient"));
    }

    private void searchKeyword(String keyword) {
        binding.etSearch.setText(keyword);
        binding.etSearch.setSelection(keyword.length());
    }

    private void updateHeader() {
        SearchViewModel.FilterType filter = searchViewModel.getFilterType().getValue();
        if (filter == null) filter = SearchViewModel.FilterType.ALL;

        if (currentQuery.isEmpty()) {
            if (filter == SearchViewModel.FilterType.ONLINE) {
                binding.tvSearchHeader.setText("Suggested Online Songs");
            } else if (filter == SearchViewModel.FilterType.LOCAL) {
                binding.tvSearchHeader.setText("Local Songs");
            } else {
                binding.tvSearchHeader.setText("Suggested & Discover Tracks");
            }
        } else {
            binding.tvSearchHeader.setText(String.format("Search Results for \"%s\"", currentQuery));
        }
    }

    private void setupObservers() {
        searchViewModel.getLocalSearchResults().observe(getViewLifecycleOwner(), localSongs -> {
            updateDisplayedList();
        });

        searchViewModel.getOnlineSearchResults().observe(getViewLifecycleOwner(), onlineSongs -> {
            updateDisplayedList();
        });
    }

    private void updateDisplayedList() {
        displayedSongs.clear();
        SearchViewModel.FilterType filter = searchViewModel.getFilterType().getValue();
        if (filter == null) filter = SearchViewModel.FilterType.ALL;

        List<Song> local = searchViewModel.getLocalSearchResults().getValue();
        List<Song> online = searchViewModel.getOnlineSearchResults().getValue();

        if (filter == SearchViewModel.FilterType.ONLINE) {
            if (online != null) displayedSongs.addAll(online);
        } else if (filter == SearchViewModel.FilterType.LOCAL) {
            if (local != null) {
                for (Song s : local) {
                    if (!s.isOnline()) displayedSongs.add(s);
                }
            }
        } else {
            // ALL: Online suggestions + Local tracks
            if (online != null) displayedSongs.addAll(online);
            if (local != null) {
                for (Song s : local) {
                    if (!s.isOnline() && !displayedSongs.contains(s)) {
                        displayedSongs.add(s);
                    }
                }
            }
        }

        songAdapter.submitList(new ArrayList<>(displayedSongs));

        if (displayedSongs.isEmpty()) {
            binding.tvEmptySearch.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptySearch.setVisibility(View.GONE);
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
