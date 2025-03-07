package com.example.recreationapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MusicRepository {
    private const val BASE_URL = "https://freemusicarchive.org/api/"
    private const val API_KEY = "YOUR_API_KEY_HERE" // Replace with FMA API key

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(MusicApiService::class.java)

    suspend fun getTracks(): List<MusicApiService.Track> {
        val response = service.getTracks(API_KEY)
        return if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
    }
}