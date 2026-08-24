package com.amitbharat.songsplayer.ui.playlist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.data.local.entity.PlaylistEntity;
import com.amitbharat.songsplayer.databinding.FragmentPlaylistBinding;
import com.amitbharat.songsplayer.ui.adapter.PlaylistAdapter;
import com.amitbharat.songsplayer.ui.viewmodel.MainViewModel;
import com.amitbharat.songsplayer.ui.viewmodel.PlaylistViewModel;
import com.amitbharat.songsplayer.utils.Constants;

public class PlaylistFragment extends Fragment implements PlaylistAdapter.OnPlaylistClickListener {

    private FragmentPlaylistBinding binding;
    private PlaylistViewModel playlistViewModel;
    private MainViewModel mainViewModel;
    private PlaylistAdapter playlistAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlaylistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        playlistAdapter = new PlaylistAdapter(this);
        binding.rvPlaylists.setAdapter(playlistAdapter);

        setupSmartPlaylists();
        setupObservers();

        binding.fabCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
    }

    private void setupSmartPlaylists() {
        binding.cardSmartFavorites.setOnClickListener(v -> openSmartPlaylist("Favorites", -1));
        binding.cardSmartRecentlyPlayed.setOnClickListener(v -> openSmartPlaylist("Recently Played", -2));
        binding.cardSmartMostPlayed.setOnClickListener(v -> openSmartPlaylist("Most Played", -3));
        binding.cardSmartRecentlyAdded.setOnClickListener(v -> openSmartPlaylist("Recently Added", -4));
    }

    private void setupObservers() {
        playlistViewModel.getPlaylists().observe(getViewLifecycleOwner(), playlists -> {
            if (playlists != null) {
                playlistAdapter.setPlaylists(playlists);
            }
        });

        mainViewModel.getFavoriteSongs().observe(getViewLifecycleOwner(), list -> {
            binding.tvFavoritesCount.setText(String.format("%d tracks", list != null ? list.size() : 0));
        });

        mainViewModel.getRecentlyPlayedSongs().observe(getViewLifecycleOwner(), list -> {
            binding.tvRecentCount.setText(String.format("%d tracks", list != null ? list.size() : 0));
        });

        mainViewModel.getMostPlayedSongs().observe(getViewLifecycleOwner(), list -> {
            binding.tvMostPlayedCount.setText(String.format("%d tracks", list != null ? list.size() : 0));
        });

        mainViewModel.getRecentlyAddedSongs().observe(getViewLifecycleOwner(), list -> {
            binding.tvRecentlyAddedCount.setText(String.format("%d tracks", list != null ? list.size() : 0));
        });
    }

    private void openSmartPlaylist(String title, long smartId) {
        Intent intent = new Intent(requireContext(), PlaylistDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PLAYLIST_ID, smartId);
        intent.putExtra(Constants.EXTRA_PLAYLIST_NAME, title);
        startActivity(intent);
    }

    private void showCreatePlaylistDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null);
        EditText etName = dialogView.findViewById(R.id.et_playlist_name);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel_playlist).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save_playlist).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                playlistViewModel.createPlaylist(name);
                Toast.makeText(requireContext(), R.string.playlist_created, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onPlaylistClick(PlaylistEntity playlist) {
        Intent intent = new Intent(requireContext(), PlaylistDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PLAYLIST_ID, playlist.id);
        intent.putExtra(Constants.EXTRA_PLAYLIST_NAME, playlist.name);
        startActivity(intent);
    }

    @Override
    public void onPlaylistMoreClick(PlaylistEntity playlist, View anchorView) {
        PopupMenu popup = new PopupMenu(requireContext(), anchorView);
        popup.getMenu().add(0, 1, 0, R.string.rename_playlist);
        popup.getMenu().add(0, 2, 1, R.string.delete_playlist);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                showRenameDialog(playlist);
                return true;
            } else if (item.getItemId() == 2) {
                playlistViewModel.deletePlaylist(playlist.id);
                Toast.makeText(requireContext(), R.string.playlist_deleted, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showRenameDialog(PlaylistEntity playlist) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null);
        EditText etName = dialogView.findViewById(R.id.et_playlist_name);
        etName.setText(playlist.name);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_cancel_playlist).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_save_playlist).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                playlistViewModel.renamePlaylist(playlist.id, name);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
