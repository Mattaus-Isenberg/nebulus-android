package com.coopra.database.data_access_objects;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.coopra.database.entities.Track;

import java.util.List;

@Dao
public interface TrackDao {
    @Query("SELECT * FROM track_table")
    LiveData<List<Track>> getAll();

    @Insert
    void insertAll(Track... tracks);

    @Insert
    void insert(Track track);

    @Query("DELETE FROM track_table")
    void deleteAll();
}
