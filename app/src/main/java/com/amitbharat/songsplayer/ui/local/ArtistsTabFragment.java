package com.amitbharat.songsplayer.ui.local;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.data.model.Artist;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.ui.adapter.ArtistAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class ArtistsTabFragment extends Fragment implements ArtistAdapter.OnArtistClickListener {

    private ArtistAdapter artistAdapter;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setClipToPadding(false);
        rv.setPadding(16, 16, 16, 100);
        return rv;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        artistAdapter = new ArtistAdapter(this);
        ((RecyclerView) view).setAdapter(artistAdapter);

        viewModel.getLocalArtists().observe(getViewLifecycleOwner(), artists -> {
            if (artists != null) {
                artistAdapter.setArtists(artists);
            }
        });
    }

    @Override
    public void onArtistClick(Artist artist) {
        List<Song> all = viewModel.getAllSongs().getValue();
        if (all != null) {
            List<Song> artistSongs = new ArrayList<>();
            for (Song s : all) {
                if (s.getArtist().equalsIgnoreCase(artist.getName())) {
                    artistSongs.add(s);
                }
            }
            if (!artistSongs.isEmpty() && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playSongList(artistSongs, 0);
            }
        }
    }
}
