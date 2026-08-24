package com.amitbharat.songsplayer.ui.local;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.model.Song;
import com.amitbharat.songsplayer.ui.adapter.SongAdapter;
import com.amitbharat.songsplayer.ui.main.MainActivity;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TracksTabFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private SongAdapter songAdapter;
    private MainViewModel viewModel;
    private List<Song> localSongsList = new ArrayList<>();

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

        songAdapter = new SongAdapter(this);
        ((RecyclerView) view).setAdapter(songAdapter);

        viewModel.getAllSongs().observe(getViewLifecycleOwner(), songs -> {
            if (songs != null) {
                localSongsList = new ArrayList<>();
                for (Song s : songs) {
                    if (!s.isOnline()) {
                        localSongsList.add(s);
                    }
                }
                songAdapter.submitList(localSongsList);
            }
        });
    }

    @Override
    public void onSongClick(Song song, int position) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).playSongList(localSongsList, position);
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
            }
            return false;
        });
        popup.show();
    }
}
