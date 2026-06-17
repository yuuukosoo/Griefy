package com.naufal.griefy.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApi {

    @GET("search")
    suspend fun searchTracks(
        @Query("q") query: String
    ): DeezerSearchResponse

    @GET("track/{id}")
    suspend fun getTrack(
        @Path("id") trackId: Long
    ): DeezerTrackDto
}
