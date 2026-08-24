package com.amitbharat.songsplayer.data.remote;

import android.os.Handler;
import android.os.Looper;

import com.amitbharat.songsplayer.data.model.Song;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Universal High-Speed Music Engine.
 * Connects to full 80M+ catalog (Bollywood, Indian Classical, 90s Hits, International, Ghazal, EDM, Pop)
 * with direct 320kbps CDNs and automatic fallback to decentralized audio protocol.
 */
public class UniversalStreamEngine {

    public interface MusicCallback {
        void onSongsLoaded(List<Song> songs);
    }

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    /**
     * Search songs across the massive catalog by query and page
     */
    public static void searchMusic(String query, int page, MusicCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) callback.onSongsLoaded(new ArrayList<>());
            return;
        }

        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString());
            String url = "https://www.jiosaavn.com/api.php?__call=search.getResults&q=" + encoded
                    + "&_format=json&_marker=0&api_version=4&ctx=android&n=30&p=" + Math.max(1, page);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36")
                    .header("Accept", "application/json")
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Fallback to Open Catalog on network error
                    OpenMusicCatalogEngine.searchMusic(query, songs -> {
                        if (callback != null) callback.onSongsLoaded(songs);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    List<Song> songs = new ArrayList<>();
                    Set<Long> seenIds = new HashSet<>();

                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            JsonElement root = new JsonParser().parse(json);
                            if (root.isJsonObject() && root.getAsJsonObject().has("results")) {
                                JsonArray results = root.getAsJsonObject().getAsJsonArray("results");
                                for (int i = 0; i < results.size(); i++) {
                                    JsonObject item = results.get(i).getAsJsonObject();

                                    String title = item.has("title") ? cleanHtml(item.get("title").getAsString()) : "Song";
                                    String idStr = item.has("id") ? item.get("id").getAsString() : String.valueOf(System.nanoTime());
                                    long songId = Math.abs(idStr.hashCode());

                                    // Extract Artist & Album info
                                    String artist = "Artist";
                                    String album = "Single";
                                    long durationMs = 210000;
                                    String streamUrl = null;

                                    if (item.has("more_info") && item.get("more_info").isJsonObject()) {
                                        JsonObject moreInfo = item.getAsJsonObject("more_info");

                                        if (moreInfo.has("album") && !moreInfo.get("album").isJsonNull()) {
                                            album = cleanHtml(moreInfo.get("album").getAsString());
                                        }

                                        if (moreInfo.has("duration") && !moreInfo.get("duration").isJsonNull()) {
                                            try {
                                                durationMs = moreInfo.get("duration").getAsLong() * 1000;
                                            } catch (Exception ignored) {}
                                        }

                                        // Extract artists from artistMap
                                        if (moreInfo.has("artistMap") && moreInfo.get("artistMap").isJsonObject()) {
                                            JsonObject artistMap = moreInfo.getAsJsonObject("artistMap");
                                            if (artistMap.has("primary_artists") && artistMap.get("primary_artists").isJsonArray()) {
                                                JsonArray primary = artistMap.getAsJsonArray("primary_artists");
                                                StringBuilder sb = new StringBuilder();
                                                for (int p = 0; p < primary.size(); p++) {
                                                    JsonObject pa = primary.get(p).getAsJsonObject();
                                                    if (pa.has("name")) {
                                                        if (sb.length() > 0) sb.append(", ");
                                                        sb.append(pa.get("name").getAsString());
                                                    }
                                                }
                                                if (sb.length() > 0) {
                                                    artist = sb.toString();
                                                }
                                            }
                                        }

                                        if (artist.equals("Artist") && item.has("subtitle") && !item.get("subtitle").isJsonNull()) {
                                            artist = cleanHtml(item.get("subtitle").getAsString());
                                        }

                                        // Decrypt 320kbps Master Audio Stream URL
                                        if (moreInfo.has("encrypted_media_url") && !moreInfo.get("encrypted_media_url").isJsonNull()) {
                                            String encrypted = moreInfo.get("encrypted_media_url").getAsString();
                                            streamUrl = SaavnAudioDecoder.decryptMediaUrl(encrypted);
                                        }
                                    }

                                    // High-Definition Album Artwork (500x500)
                                    String image = null;
                                    if (item.has("image") && !item.get("image").isJsonNull()) {
                                        image = item.get("image").getAsString()
                                                .replace("150x150", "500x500")
                                                .replace("50x50", "500x500");
                                    }
                                    if (image == null || image.isEmpty()) {
                                        image = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=60";
                                    }

                                    if (streamUrl != null && !streamUrl.isEmpty() && seenIds.add(songId)) {
                                        long sizeBytes = (durationMs / 1000) * 40000; // ~320kbps calculation
                                        songs.add(new Song(
                                                songId,
                                                title,
                                                artist,
                                                album,
                                                0,
                                                durationMs,
                                                null,
                                                true,
                                                streamUrl,
                                                image,
                                                false,
                                                450000 + (int)(Math.random() * 500000),
                                                System.currentTimeMillis(),
                                                sizeBytes
                                        ));
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }

                    if (songs.isEmpty() && page == 1) {
                        // Fallback to Audius if no songs were returned
                        OpenMusicCatalogEngine.searchMusic(query, more -> {
                            if (callback != null) callback.onSongsLoaded(more);
                        });
                    } else {
                        mainHandler.post(() -> {
                            if (callback != null) callback.onSongsLoaded(songs);
                        });
                    }
                }
            });
        } catch (Exception e) {
            OpenMusicCatalogEngine.searchMusic(query, songs -> {
                if (callback != null) callback.onSongsLoaded(songs);
            });
        }
    }

    /**
     * Fetch Top Trending Hindi & Global Songs for initial screen
     */
    public static void fetchTrendingMusic(MusicCallback callback) {
        searchMusic("Top Hindi Songs", 1, songs -> {
            if (songs != null && !songs.isEmpty()) {
                if (callback != null) callback.onSongsLoaded(songs);
            } else {
                OpenMusicCatalogEngine.fetchTrendingMusic(more -> {
                    if (callback != null) callback.onSongsLoaded(more);
                });
            }
        });
    }

    private static String cleanHtml(String text) {
        if (text == null) return "";
        return text.replace("&quot;", "\"")
                .replace("&#039;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }
}
