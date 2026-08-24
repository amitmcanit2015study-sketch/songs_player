package com.amitbharat.songsplayer.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.amitbharat.songsplayer.databinding.ActivitySplashBinding;
import com.amitbharat.songsplayer.ui.main.MainActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Animate content slightly
        binding.centerContent.setAlpha(0f);
        binding.centerContent.animate().alpha(1f).setDuration(800).start();

        binding.tvSplashDeveloper.setAlpha(0f);
        binding.tvSplashDeveloper.animate().alpha(1f).setDuration(1000).start();

        // Navigate to MainActivity after splash delay
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
