package com.amitbharat.songsplayer.ui.local;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.amitbharat.songsplayer.R;
import com.amitbharat.songsplayer.databinding.FragmentLocalMusicBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class LocalMusicFragment extends Fragment {

    private FragmentLocalMusicBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLocalMusicBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LocalPagerAdapter pagerAdapter = new LocalPagerAdapter(this);
        binding.localViewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.localTabLayout, binding.localViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.tab_tracks);
                    break;
                case 1:
                    tab.setText(R.string.tab_albums);
                    break;
                case 2:
                    tab.setText(R.string.tab_artists);
                    break;
            }
        }).attach();
    }

    private static class LocalPagerAdapter extends FragmentStateAdapter {
        public LocalPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return new AlbumsTabFragment();
                case 2:
                    return new ArtistsTabFragment();
                default:
                    return new TracksTabFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
