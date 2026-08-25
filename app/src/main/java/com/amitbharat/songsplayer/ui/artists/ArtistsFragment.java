package com.amitbharat.songsplayer.ui.artists;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Artist;
import com.amitbharat.songsplayer.databinding.FragmentArtistsBinding;
import com.amitbharat.songsplayer.ui.adapter.ArtistAdapter;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArtistsFragment extends Fragment implements ArtistAdapter.OnArtistClickListener {

    private FragmentArtistsBinding binding;
    private ArtistAdapter artistAdapter;
    private MainViewModel viewModel;
    private final List<Artist> allArtistsList = new ArrayList<>();
    private final List<Artist> displayedArtistsList = new ArrayList<>();
    private String currentSearchQuery = "";
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentArtistsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupChips();
        loadArtists();
    }

    private void setupRecyclerView() {
        artistAdapter = new ArtistAdapter(this);
        binding.rvArtists.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvArtists.setAdapter(artistAdapter);

        binding.swipeRefreshArtists.setOnRefreshListener(() -> {
            loadArtists();
            binding.swipeRefreshArtists.setRefreshing(false);
        });
    }

    private void setupSearch() {
        binding.etSearchArtists.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                binding.btnClearArtistSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                filterArtists();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearArtistSearch.setOnClickListener(v -> binding.etSearchArtists.setText(""));
    }

    private void setupChips() {
        binding.chipGroupArtists.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_all)) selectedCategory = "All";
            else if (checkedIds.contains(R.id.chip_bollywood)) selectedCategory = "Bollywood";
            else if (checkedIds.contains(R.id.chip_punjabi)) selectedCategory = "Punjabi";
            else if (checkedIds.contains(R.id.chip_classics)) selectedCategory = "Classics";
            else if (checkedIds.contains(R.id.chip_global)) selectedCategory = "Global";
            else if (checkedIds.contains(R.id.chip_device)) selectedCategory = "Device";
            filterArtists();
        });
    }

    private void loadArtists() {
        allArtistsList.clear();
        allArtistsList.addAll(getCuratedArtists());

        viewModel.getLocalArtists().observe(getViewLifecycleOwner(), localArtists -> {
            if (localArtists != null && !localArtists.isEmpty()) {
                for (Artist la : localArtists) {
                    boolean exists = false;
                    for (Artist ca : allArtistsList) {
                        if (ca.getName().equalsIgnoreCase(la.getName())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        la.setGenre("Device");
                        allArtistsList.add(la);
                    }
                }
            }
            filterArtists();
        });

        filterArtists();
    }

    private void filterArtists() {
        displayedArtistsList.clear();

        for (Artist a : allArtistsList) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    a.getName().toLowerCase().contains(currentSearchQuery) ||
                    (a.getGenre() != null && a.getGenre().toLowerCase().contains(currentSearchQuery));

            boolean matchesCategory = "All".equals(selectedCategory) ||
                    (a.getGenre() != null && a.getGenre().equalsIgnoreCase(selectedCategory));

            if (matchesSearch && matchesCategory) {
                displayedArtistsList.add(a);
            }
        }

        artistAdapter.setArtists(displayedArtistsList);
        binding.tvEmptyArtists.setVisibility(displayedArtistsList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<Artist> getCuratedArtists() {
        List<Artist> list = new ArrayList<>();
        list.add(new Artist(101L, "Arijit Singh", 50, 10, "https://c.saavncdn.com/artists/Arijit_Singh_002_20230323062147_500x500.jpg", "Bollywood"));
        list.add(new Artist(102L, "Shreya Ghoshal", 45, 8, "https://c.saavncdn.com/artists/Shreya_Ghoshal_004_20230614081023_500x500.jpg", "Bollywood"));
        list.add(new Artist(103L, "Diljit Dosanjh", 40, 6, "https://c.saavncdn.com/artists/Diljit_Dosanjh_004_20221006184540_500x500.jpg", "Punjabi"));
        list.add(new Artist(104L, "Atif Aslam", 38, 7, "https://c.saavncdn.com/artists/Atif_Aslam_004_20200806082728_500x500.jpg", "Bollywood"));
        list.add(new Artist(105L, "Sonu Nigam", 42, 9, "https://c.saavncdn.com/artists/Sonu_Nigam_003_20200806082718_500x500.jpg", "Bollywood"));
        list.add(new Artist(106L, "Neha Kakkar", 35, 5, "https://c.saavncdn.com/artists/Neha_Kakkar_006_20200820065306_500x500.jpg", "Bollywood"));
        list.add(new Artist(107L, "Kishore Kumar", 60, 15, "https://c.saavncdn.com/artists/Kishore_Kumar_500x500.jpg", "Classics"));
        list.add(new Artist(108L, "Lata Mangeshkar", 55, 14, "https://c.saavncdn.com/artists/Lata_Mangeshkar_500x500.jpg", "Classics"));
        list.add(new Artist(109L, "A.R. Rahman", 50, 12, "https://c.saavncdn.com/artists/A.R._Rahman_002_20210219092446_500x500.jpg", "Bollywood"));
        list.add(new Artist(110L, "Anirudh Ravichander", 36, 6, "https://c.saavncdn.com/artists/Anirudh_Ravichander_004_20230807085718_500x500.jpg", "Global"));
        list.add(new Artist(111L, "KK", 35, 6, "https://c.saavncdn.com/artists/KK_500x500.jpg", "Bollywood"));
        list.add(new Artist(112L, "Badshah", 30, 4, "https://c.saavncdn.com/artists/Badshah_005_20230608083047_500x500.jpg", "Punjabi"));
        list.add(new Artist(113L, "Jubin Nautiyal", 32, 5, "https://c.saavncdn.com/artists/Jubin_Nautiyal_003_20200820065318_500x500.jpg", "Bollywood"));
        list.add(new Artist(114L, "Alka Yagnik", 48, 11, "https://c.saavncdn.com/artists/Alka_Yagnik_002_20200806082708_500x500.jpg", "Classics"));
        list.add(new Artist(115L, "Kumar Sanu", 46, 10, "https://c.saavncdn.com/artists/Kumar_Sanu_002_20200806082740_500x500.jpg", "Classics"));
        list.add(new Artist(116L, "Imagine Dragons", 25, 4, "https://c.saavncdn.com/artists/Imagine_Dragons_500x500.jpg", "Global"));
        list.add(new Artist(117L, "Ed Sheeran", 28, 5, "https://c.saavncdn.com/artists/Ed_Sheeran_500x500.jpg", "Global"));
        list.add(new Artist(118L, "Taylor Swift", 30, 6, "https://c.saavncdn.com/artists/Taylor_Swift_500x500.jpg", "Global"));
        return list;
    }

    @Override
    public void onArtistClick(Artist artist) {
        Intent intent = new Intent(requireContext(), ArtistDetailActivity.class);
        intent.putExtra("artist", artist);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
