package com.amitbharat.songsplayer.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.amitbharat.songsplayer.data.local.AppDatabase;
import com.amitbharat.songsplayer.data.local.dao.DownloadDao;
import com.amitbharat.songsplayer.data.model.DownloadItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadWorker extends Worker {

    private static final OkHttpClient downloadClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build();

    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        long songId = getInputData().getLong("song_id", 0);
        String streamUrl = getInputData().getString("stream_url");
        String targetPath = getInputData().getString("target_path");

        if (songId == 0 || streamUrl == null || targetPath == null) {
            return Result.failure();
        }

        DownloadDao downloadDao = AppDatabase.getDatabase(getApplicationContext()).downloadDao();

        InputStream input = null;
        FileOutputStream output = null;

        try {
            downloadDao.updateProgress(songId, 0, 0, DownloadItem.Status.DOWNLOADING.name());

            Request request = new Request.Builder()
                    .url(streamUrl)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36")
                    .header("Accept", "*/*")
                    .build();

            Response response = downloadClient.newCall(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                downloadDao.updateProgress(songId, 0, 0, DownloadItem.Status.FAILED.name());
                return Result.failure();
            }

            ResponseBody body = response.body();
            long fileLength = body.contentLength();
            input = body.byteStream();

            File targetFile = new File(targetPath);
            if (targetFile.getParentFile() != null && !targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }

            output = new FileOutputStream(targetFile);

            byte[] data = new byte[8192];
            long total = 0;
            int count;
            long lastUpdateTime = System.currentTimeMillis();

            while ((count = input.read(data)) != -1) {
                if (isStopped()) {
                    downloadDao.updateProgress(songId, 0, total, DownloadItem.Status.FAILED.name());
                    return Result.failure();
                }

                total += count;
                output.write(data, 0, count);

                if (fileLength > 0 && System.currentTimeMillis() - lastUpdateTime > 400) {
                    int progress = (int) (total * 100 / fileLength);
                    downloadDao.updateProgress(songId, progress, total, DownloadItem.Status.DOWNLOADING.name());
                    lastUpdateTime = System.currentTimeMillis();
                }
            }

            output.flush();
            downloadDao.updateProgress(songId, 100, total, DownloadItem.Status.COMPLETED.name());
            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            downloadDao.updateProgress(songId, 0, 0, DownloadItem.Status.FAILED.name());
            return Result.failure();
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (Exception ignored) {}
        }
    }
}
