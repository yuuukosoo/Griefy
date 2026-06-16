package com.naufal.griefy.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface SpotifyApi {


    @GET("v1/search?type=track&limit=15")
    suspend fun searchTracks(
        @Header("Authorization") token: String,
        @Query("q") query: String,
    ): SpotifySearchResponse

}