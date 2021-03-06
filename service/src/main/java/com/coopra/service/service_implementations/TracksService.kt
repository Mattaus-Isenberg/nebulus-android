package com.coopra.service.service_implementations

import com.coopra.data.Track
import com.coopra.service.Constants
import com.coopra.service.RetrofitHelper
import com.coopra.service.interfaces.TracksInterface
import retrofit2.Callback

class TracksService {
    private val retrofitHelper = RetrofitHelper()

    fun getRandomTracks(callback: Callback<List<Track>>) {
        val service = retrofitHelper.createRetrofit().create(TracksInterface::class.java)
        val call = service.getRandomTracks(Constants.CLIENT_ID)
        call.enqueue(callback)
    }
}