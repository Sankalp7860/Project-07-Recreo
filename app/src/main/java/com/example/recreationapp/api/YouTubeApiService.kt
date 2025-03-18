package com.example.recreationapp.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("videoCategoryId") videoCategoryId: String = "10", // 10 is for Music
        @Query("maxResults") maxResults: Int = 20,
        @Query("q") query: String,
        @Query("key") apiKey: String
    ): Response<YouTubeSearchResponse>

    @GET("videos")
    suspend fun getVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("chart") chart: String = "mostPopular",
        @Query("videoCategoryId") videoCategoryId: String = "10", // 10 is for Music
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): Response<YouTubeVideosResponse>

    @GET("videoCategories")
    suspend fun getVideoCategories(
        @Query("part") part: String = "snippet",
        @Query("regionCode") regionCode: String = "US",
        @Query("key") apiKey: String
    ): Response<VideoCategoriesResponse>
}

data class YouTubeSearchResponse(
    val kind: String,
    val etag: String,
    val nextPageToken: String?,
    val prevPageToken: String?,
    val regionCode: String?,
    val pageInfo: PageInfo,
    val items: List<YouTubeSearchItem>
)

data class YouTubeVideosResponse(
    val kind: String,
    val etag: String,
    val items: List<YouTubeVideoItem>,
    val nextPageToken: String?,
    val pageInfo: PageInfo
)

data class VideoCategoriesResponse(
    val kind: String,
    val etag: String,
    val items: List<CategoryItem>
)

data class CategoryItem(
    val id: String,
    val snippet: CategorySnippet
)

data class CategorySnippet(
    val title: String
)

data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int
)

data class YouTubeSearchItem(
    val kind: String,
    val etag: String,
    val id: VideoId,
    val snippet: VideoSnippet
)

data class YouTubeVideoItem(
    val kind: String,
    val etag: String,
    val id: String,
    val snippet: VideoSnippet,
    val contentDetails: ContentDetails?
)

data class VideoId(
    val kind: String,
    val videoId: String
)

data class VideoSnippet(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val channelTitle: String,
    val liveBroadcastContent: String?
)

data class ContentDetails(
    val duration: String,
    val dimension: String,
    val definition: String
)

data class Thumbnails(
    val default: Thumbnail?,
    val medium: Thumbnail?,
    val high: Thumbnail?,
    val standard: Thumbnail?,
    val maxres: Thumbnail?
)

data class Thumbnail(
    val url: String,
    val width: Int?,
    val height: Int?
)

data class MusicCategory(
    val id: String,
    val title: String
)

data class MusicItem(
    val id: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: String?,
    val isFavorite: Boolean = false
)

object YouTubeApiClient {
    private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    private const val API_KEY = "AIzaSyAWUBzgPnvzvMjBi-IjeW-YCfTE97Cm4Nc"

    // Music categories - predefined since YouTube API doesn't have specific music categories
    val musicCategories = listOf(
        MusicCategory("pop", "Pop"),
        MusicCategory("rock", "Rock"),
        MusicCategory("hiphop", "Hip Hop"),
        MusicCategory("jazz", "Jazz"),
        MusicCategory("electronic", "Electronic"),
        MusicCategory("classical", "Classical"),
        MusicCategory("country", "Country"),
        MusicCategory("trending", "Trending")
    )

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: YouTubeApiService = retrofit.create(YouTubeApiService::class.java)

    suspend fun searchMusic(query: String): List<MusicItem> = withContext(Dispatchers.IO) {
        try {
            val response = service.searchVideos(query = query, apiKey = API_KEY)
            if (response.isSuccessful) {
                return@withContext response.body()?.items?.map { video ->
                    MusicItem(
                        id = video.id.videoId,
                        title = video.snippet.title,
                        artist = video.snippet.channelTitle,
                        thumbnailUrl = video.snippet.thumbnails.high?.url ?: video.snippet.thumbnails.medium?.url ?: "",
                        duration = null
                    )
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }

    suspend fun getMusicByCategory(category: String): List<MusicItem> = withContext(Dispatchers.IO) {
        try {
            // For trending, use the videoCategoryId=10 (Music)
            if (category == "trending") {
                val response = service.getVideos(apiKey = API_KEY)
                if (response.isSuccessful) {
                    return@withContext response.body()?.items?.map { video ->
                        MusicItem(
                            id = video.id,
                            title = video.snippet.title,
                            artist = video.snippet.channelTitle,
                            thumbnailUrl = video.snippet.thumbnails.high?.url ?: video.snippet.thumbnails.medium?.url ?: "",
                            duration = video.contentDetails?.duration?.let { parseDuration(it) }
                        )
                    } ?: emptyList()
                }
            } else {
                // For other categories, use search with the category name as query
                val response = service.searchVideos(query = category, apiKey = API_KEY)
                if (response.isSuccessful) {
                    return@withContext response.body()?.items?.map { video ->
                        MusicItem(
                            id = video.id.videoId,
                            title = video.snippet.title,
                            artist = video.snippet.channelTitle,
                            thumbnailUrl = video.snippet.thumbnails.high?.url ?: video.snippet.thumbnails.medium?.url ?: "",
                            duration = null
                        )
                    } ?: emptyList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }

    // Parse ISO 8601 duration to human-readable format
    private fun parseDuration(isoDuration: String): String {
        val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
        val matchResult = regex.find(isoDuration) ?: return "00:00"

        val (hours, minutes, seconds) = matchResult.destructured

        val h = hours.toIntOrNull() ?: 0
        val m = minutes.toIntOrNull() ?: 0
        val s = seconds.toIntOrNull() ?: 0

        return if (h > 0) {
            String.format("%d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }
}