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
 * Open Source Free Music Catalog Engine (Audius Decentralized Audio Protocol).
 * Delivers direct, 100% Ad-Free MP3 audio streams for native ExoPlayer playback.
 */
public class OpenMusicCatalogEngine {

    public interface MusicCallback {
        void onSongsLoaded(List<Song> songs);
    }

    private static final String APP_NAME = "SongsPlayer";
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    private static final String[] DISCOVERY_NODES = {
            "https://api.audius.co",
            "https://discoveryprovider.audius.co",
            "https://audius-discovery-1.cultur3stake.com",
            "https://discovery-us-01.audius.openplayer.org"
    };

    /**
     * Searches Open Music Catalog by keyword/genre
     */
    public static void searchMusic(String query, MusicCallback callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) callback.onSongsLoaded(new ArrayList<>());
            return;
        }

        try {
            String encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString());
            fetchTracksWithFallback(0, "/v1/tracks/search?query=" + encoded + "&app_name=" + APP_NAME + "&limit=30", callback);
        } catch (Exception e) {
            if (callback != null) callback.onSongsLoaded(new ArrayList<>());
        }
    }

    /**
     * Fetches Global Trending Tracks from Open Music Catalog
     */
    public static void fetchTrendingMusic(MusicCallback callback) {
        fetchTracksWithFallback(0, "/v1/tracks/trending?app_name=" + APP_NAME + "&limit=30", callback);
    }

    private static void fetchTracksWithFallback(int nodeIndex, String path, MusicCallback callback) {
        if (nodeIndex >= DISCOVERY_NODES.length) {
            mainHandler.post(() -> {
                if (callback != null) callback.onSongsLoaded(new ArrayList<>());
            });
            return;
        }

        String node = DISCOVERY_NODES[nodeIndex];
        String fullUrl = node + path;

        Request request = new Request.Builder()
                .url(fullUrl)
                .header("User-Agent", "SongsPlayer/1.0 (Android; OpenCatalog)")
                .header("Accept", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Try next discovery node on failure
                fetchTracksWithFallback(nodeIndex + 1, path, callback);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                List<Song> songs = new ArrayList<>();
                Set<Long> seenIds = new HashSet<>();

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JsonElement rootElement = new JsonParser().parse(json);
                        if (rootElement.isJsonObject() && rootElement.getAsJsonObject().has("data")) {
                            JsonArray dataArray = rootElement.getAsJsonObject().getAsJsonArray("data");
                            for (int i = 0; i < dataArray.size(); i++) {
                                JsonObject track = dataArray.get(i).getAsJsonObject();

                                String trackId = track.has("id") ? track.get("id").getAsString() : "";
                                String title = track.has("title") ? track.get("title").getAsString() : "Music Track";
                                
                                String artist = "Open Artist";
                                if (track.has("user") && track.getAsJsonObject("user").has("name")) {
                                    artist = track.getAsJsonObject("user").get("name").getAsString();
                                }

                                String genre = track.has("genre") && !track.get("genre").isJsonNull() ? track.get("genre").getAsString() : "Open Music";
                                
                                long durationMs = 210000;
                                if (track.has("duration") && !track.get("duration").isJsonNull()) {
                                    durationMs = track.get("duration").getAsLong() * 1000;
                                }

                                // High quality album artwork
                                String artwork = null;
                                if (track.has("artwork") && !track.get("artwork").isJsonNull() && track.get("artwork").isJsonObject()) {
                                    JsonObject artObj = track.getAsJsonObject("artwork");
                                    if (artObj.has("480x480")) {
                                        artwork = artObj.get("480x480").getAsString();
                                    } else if (artObj.has("150x150")) {
                                        artwork = artObj.get("150x150").getAsString();
                                    }
                                }
                                if (artwork == null) {
                                    artwork = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=60";
                                }

                                // Direct MP3 Stream URL (Plays natively in ExoPlayer)
                                String streamUrl = node + "/v1/tracks/" + trackId + "/stream?app_name=" + APP_NAME;
                                long songId = Math.abs(trackId.hashCode());

                                if (seenIds.add(songId) && !trackId.isEmpty()) {
                                    songs.add(new Song(
                                            songId,
                                            title,
                                            artist,
                                            genre,
                                            0,
                                            durationMs,
                                            null,
                                            true,
                                            streamUrl,
                                            artwork,
                                            false,
                                            250000 + (int)(Math.random() * 500000),
                                            System.currentTimeMillis(),
                                            (durationMs / 1000) * 40000
                                    ));
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                }

                if (songs.isEmpty() && nodeIndex + 1 < DISCOVERY_NODES.length && !response.isSuccessful()) {
                    fetchTracksWithFallback(nodeIndex + 1, path, callback);
                } else {
                    mainHandler.post(() -> {
                        if (callback != null) callback.onSongsLoaded(songs);
                    });
                }
            }
        });
    }
}
