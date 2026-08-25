package com.amitbharat.songsplayer.ui.settings;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.amitbharat.songsplayer.data.local.AppDatabase;
import com.amitbharat.songsplayer.databinding.ActivityAboutBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbarAbout.setNavigationOnClickListener(v -> finish());

        // Share App Button - Share Actual APK File
        binding.btnShareApp.setOnClickListener(v -> shareApplicationApk());

        // Send Feedback Button
        binding.btnSendFeedback.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:rooyssofttech2020@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Songs Player Feedback & Inquiry");
            try {
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
            } catch (Exception ignored) {}
        });
    }

    private void shareApplicationApk() {
        Toast.makeText(this, "Preparing Songs Player APK to share...", Toast.LENGTH_SHORT).show();

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                ApplicationInfo appInfo = getApplicationContext().getApplicationInfo();
                String sourceApkPath = appInfo.sourceDir;
                File originalApk = new File(sourceApkPath);

                if (!originalApk.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "APK file not found on device", Toast.LENGTH_SHORT).show());
                    return;
                }

                File shareFolder = new File(getCacheDir(), "shared_apk");
                if (!shareFolder.exists()) {
                    shareFolder.mkdirs();
                }

                File sharedApk = new File(shareFolder, "SongsPlayer.apk");

                // Copy APK bytes to cache folder
                try (FileInputStream in = new FileInputStream(originalApk);
                     FileOutputStream out = new FileOutputStream(sharedApk)) {
                    byte[] buffer = new byte[16384];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }

                Uri apkUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        sharedApk
                );

                runOnUiThread(() -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("application/vnd.android.package-archive");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Songs Player APK");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is the Songs Player app APK by Amit Bharat. Install and enjoy unlimited online & offline music!");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share Songs Player APK via"));
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to share APK: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
