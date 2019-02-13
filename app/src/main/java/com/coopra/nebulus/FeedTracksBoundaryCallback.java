package com.coopra.nebulus;

import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagedList;
import androidx.annotation.NonNull;

import com.coopra.data.DashboardActivity;
import com.coopra.data.DashboardActivityEnvelope;
import com.coopra.database.entities.Track;
import com.coopra.database.entities.User;
import com.coopra.nebulus.enums.NetworkStates;
import com.coopra.service.service_implementations.ActivitiesService;
import com.coopra.service.service_implementations.TracksService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedTracksBoundaryCallback extends PagedList.BoundaryCallback<Track> {
    private TrackRepository mRepository;
    private String mToken;
    private boolean mIsLoading;
    private MutableLiveData<NetworkStates> mNetworkState;

    FeedTracksBoundaryCallback(TrackRepository repository, String token, MutableLiveData<NetworkStates> networkState) {
        mRepository = repository;
        mToken = token;
        mNetworkState = networkState;
    }

    /**
     * Requests initial data from the network, replacing all content currently in the database.
     */
    @Override
    public void onZeroItemsLoaded() {
        if (mIsLoading) {
            return;
        }

        mIsLoading = true;
        mNetworkState.postValue(NetworkStates.LOADING);

        ActivitiesService.getFeedTracks(mToken, new Callback<DashboardActivityEnvelope>() {
            @Override
            public void onResponse(@NonNull Call<DashboardActivityEnvelope> call, @NonNull Response<DashboardActivityEnvelope> response) {
                handleSuccessfulNetworkCall(response);
                mIsLoading = false;
                mNetworkState.postValue(NetworkStates.NORMAL);
            }

            @Override
            public void onFailure(@NonNull Call<DashboardActivityEnvelope> call, @NonNull Throwable t) {
                mIsLoading = false;
                mNetworkState.postValue(NetworkStates.NORMAL);
            }
        });
    }

    /**
     * Requests additional data from the network, appending the results to the end of the database's
     * existing data.
     */
    @Override
    public void onItemAtEndLoaded(@NonNull Track itemAtEnd) {
        if (mIsLoading) {
            return;
        }

        mIsLoading = true;
        mNetworkState.postValue(NetworkStates.LOADING);

        ActivitiesService.getNextTracks(mToken, itemAtEnd.nextToken, new Callback<DashboardActivityEnvelope>() {
            @Override
            public void onResponse(@NonNull Call<DashboardActivityEnvelope> call, @NonNull Response<DashboardActivityEnvelope> response) {
                handleSuccessfulNetworkCall(response);
                mIsLoading = false;
                mNetworkState.postValue(NetworkStates.NORMAL);
            }

            @Override
            public void onFailure(@NonNull Call<DashboardActivityEnvelope> call, @NonNull Throwable t) {
                mIsLoading = false;
                mNetworkState.postValue(NetworkStates.NORMAL);
            }
        });
    }

    private void handleSuccessfulNetworkCall(@NonNull Response<DashboardActivityEnvelope> response) {
        if (response.body() != null) {
            List<Track> tracks = new ArrayList<>();
            List<User> users = new ArrayList<>();

            for (DashboardActivity activity : response.body().collection) {
                if (activity.origin != null) {
                    tracks.add(new Track(activity.origin, response.body().next_href, activity.created_at));
                    users.add(new User(activity.origin.user));
                }
            }

            if (tracks.size() > 0) {
                mRepository.insertAll(new TrackRepository.TrackParameters(tracks, users));
            } else {
                getTrendingTracks();
            }
        }
    }

    private void getTrendingTracks() {
        TracksService.getRandomTracks(new Callback<List<com.coopra.data.Track>>() {
            @Override
            public void onResponse(@NonNull Call<List<com.coopra.data.Track>> call, @NonNull Response<List<com.coopra.data.Track>> response) {
                handleSuccessfulTrendingCall(response);
                mIsLoading = false;
                mNetworkState.postValue(NetworkStates.NORMAL);
            }

            @Override
            public void onFailure(@NonNull Call<List<com.coopra.data.Track>> call, @NonNull Throwable t) {
                mIsLoading = false;
                mNetworkState.postValue(NetworkStates.NORMAL);
            }
        });
    }

    private void handleSuccessfulTrendingCall(@NonNull Response<List<com.coopra.data.Track>> response) {
        if (response.body() != null) {
            List<Track> tracks = new ArrayList<>();
            List<User> users = new ArrayList<>();

            for (com.coopra.data.Track serverTrack : response.body()) {
                tracks.add(new Track(serverTrack));
                users.add(new User(serverTrack.user));
            }

            if (tracks.size() > 0) {
                mRepository.insertAll(new TrackRepository.TrackParameters(tracks, users));
            }
        }
    }
}
