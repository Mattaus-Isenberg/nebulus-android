package com.coopra.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.coopra.database.data_access_objects.TrackDao;
import com.coopra.database.entities.Track;

@Database(entities = {Track.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "nebulus_database")
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract TrackDao trackDao();
}
