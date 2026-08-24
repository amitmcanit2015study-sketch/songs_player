package com.amitbharat.songsplayer.ui.local;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.data.model.Album;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.ui.adapter.AlbumAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class AlbumsTabFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener {

    private AlbumAdapter albumAdapter;
    private MainViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView rv = new RecyclerView(requireContext());
        rv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rv.setClipToPadding(false);
        rv.setPadding(16, 16, 16, 100);
        return rv;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        albumAdapter = new AlbumAdapter(this);
        ((RecyclerView) view).setAdapter(albumAdapter);

        viewModel.getLocalAlbums().observe(getViewLifecycleOwner(), albums -> {
            if (albums != null) {
                albumAdapter.setAlbums(albums);
            }
        });
    }

    @Override
    public void onAlbumClick(Album album) {
        // Play songs from clicked album
        List<Song> all = viewModel.getAllSongs().getValue();
        if (all != null) {
            List<Song> albumSongs = new ArrayList<>();
            for (Song s : all) {
                if (s.getAlbumId() == album.getId() || s.getAlbum().equalsIgnoreCase(album.getTitle())) {
                    albumSongs.add(s);
                }
            }
            if (!albumSongs.isEmpty() && getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).playSongList(albumSongs, 0);
            }
        }
    }
}
