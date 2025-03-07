package com.example.recreationapp.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response

interface MusicApiService {
    @GET("tracks")
    suspend fun getTracks(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 10
    ): Response<List<Track>>

    data class Track(
        val title: String,
        val artist: String,
        val streamUrl: String?
    )
}