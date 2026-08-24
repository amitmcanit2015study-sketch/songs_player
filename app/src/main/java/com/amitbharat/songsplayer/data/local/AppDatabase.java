package com.amitbharat.songsplayer.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.amitbharat.songsplayer.data.local.dao.DownloadDao;
import com.amitbharat.songsplayer.data.local.dao.HistoryDao;
import com.amitbharat.songsplayer.data.local.dao.PlaylistDao;
import com.amitbharat.songsplayer.data.local.dao.SongDao;
import com.amitbharat.songsplayer.data.local.entity.DownloadEntity;
import com.amitbharat.songsplayer.data.local.entity.HistoryEntity;
import com.amitbharat.songsplayer.data.local.entity.PlaylistEntity;
import com.amitbharat.songsplayer.data.local.entity.PlaylistSongCrossRef;
import com.amitbharat.songsplayer.data.local.entity.SongEntity;
import com.amitbharat.songsplayer.utils.Constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        SongEntity.class,
        PlaylistEntity.class,
        PlaylistSongCrossRef.class,
        HistoryEntity.class,
        DownloadEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract SongDao songDao();
    public abstract PlaylistDao playlistDao();
    public abstract HistoryDao historyDao();
    public abstract DownloadDao downloadDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            Constants.DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
