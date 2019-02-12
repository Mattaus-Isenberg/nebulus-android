package com.coopra.nebulus;

import android.app.Application;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import android.os.AsyncTask;

import com.coopra.database.AppDatabase;
import com.coopra.database.data_access_objects.TrackDao;
import com.coopra.database.data_access_objects.UserDao;
import com.coopra.database.entities.Track;
import com.coopra.database.entities.User;
import com.coopra.nebulus.enums.NetworkStates;

import java.util.ArrayList;
import java.util.List;

public class TrackRepository {
    private TrackDao mTrackDao;
    private UserDao mUserDao;
    private LiveData<PagedList<Track>> mAllTracks;

    public TrackRepository(Application application, MutableLiveData<NetworkStates> networkState) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mTrackDao = db.trackDao();
        mUserDao = db.userDao();

        PagedList.Config myPagingConfig = new PagedList.Config.Builder()
                .setPageSize(20)
                .setEnablePlaceholders(false)
                .build();

        mAllTracks = new LivePagedListBuilder<>(mTrackDao.getAll(), myPagingConfig)
                .setBoundaryCallback(new FeedTracksBoundaryCallback(this, TokenHandler.getToken(application), networkState))
                .build();
    }

    public LiveData<PagedList<Track>> getAll() {
        return mAllTracks;
    }

    public void insertAll(TrackParameters parameters) {
        new insertAllAsyncTask(mTrackDao, mUserDao).execute(parameters);
    }

    @WorkerThread
    public static List<com.coopra.data.Track> alternativeGetTracks() {
        List<com.coopra.data.Track> tracks = new ArrayList<>();
        com.coopra.data.Track track1 = new com.coopra.data.Track();
        track1.title = "journey";
        track1.user = new com.coopra.data.User();
        track1.user.username = "otxhello";
        track1.playback_count = 7410;
        track1.genre = "";
        track1.artwork_url = "https://i1.sndcdn.com/artworks-000485572158-2fhfxz-large.jpg";

        tracks.add(track1);

        return tracks;
    }

    private static class insertAllAsyncTask extends AsyncTask<TrackParameters, Void, Void> {
        private TrackDao mAsyncTrackDao;
        private UserDao mAsyncUserDao;

        insertAllAsyncTask(TrackDao trackDao, UserDao userDao) {
            mAsyncTrackDao = trackDao;
            mAsyncUserDao = userDao;
        }

        @Override
        protected Void doInBackground(final TrackParameters... params) {
            mAsyncUserDao.insertAll(params[0].users.toArray(new User[0]));
            mAsyncTrackDao.insertAll(params[0].tracks.toArray(new Track[0]));
            return null;
        }
    }

    public static class TrackParameters {
        List<Track> tracks;
        List<User> users;

        public TrackParameters(List<Track> track, List<User> user) {
            this.tracks = track;
            this.users = user;
        }
    }
}
