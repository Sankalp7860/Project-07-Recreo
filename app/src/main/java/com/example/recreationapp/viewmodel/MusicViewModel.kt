package com.example.recreationapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.recreationapp.api.MusicCategory
import com.example.recreationapp.api.MusicItem
import com.example.recreationapp.api.YouTubeApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val _categories = MutableStateFlow(YouTubeApiClient.musicCategories)
    val categories: StateFlow<List<MusicCategory>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow<MusicCategory?>(null)
    val selectedCategory: StateFlow<MusicCategory?> = _selectedCategory.asStateFlow()

    private val _musicItems = MutableStateFlow<List<MusicItem>>(emptyList())
    val musicItems: StateFlow<List<MusicItem>> = _musicItems.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<MusicItem>>(emptyList())
    val recentlyPlayed: StateFlow<List<MusicItem>> = _recentlyPlayed.asStateFlow()

    init {
        // Load trending music by default
        selectCategory(categories.value.find { it.id == "trending" }!!)
    }

    fun selectCategory(category: MusicCategory) {
        _selectedCategory.value = category
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val items = YouTubeApiClient.getMusicByCategory(category.id)
                _musicItems.value = items.map { it.copy(isFavorite = _favorites.value.contains(it.id)) }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchMusic() {
        val query = _searchQuery.value
        if (query.isBlank()) return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val items = YouTubeApiClient.searchMusic(query)
                _musicItems.value = items.map { it.copy(isFavorite = _favorites.value.contains(it.id)) }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(musicItem: MusicItem) {
        val currentFavorites = _favorites.value.toMutableSet()

        if (currentFavorites.contains(musicItem.id)) {
            currentFavorites.remove(musicItem.id)
        } else {
            currentFavorites.add(musicItem.id)
        }

        _favorites.value = currentFavorites

        // Update the music items to reflect the changed favorite status
        _musicItems.value = _musicItems.value.map {
            if (it.id == musicItem.id) {
                it.copy(isFavorite = !it.isFavorite)
            } else {
                it
            }
        }
    }

    fun addToRecentlyPlayed(musicItem: MusicItem) {
        val current = _recentlyPlayed.value.toMutableList()

        // Remove if already exists to avoid duplicates
        current.removeIf { it.id == musicItem.id }

        // Add to the beginning of the list
        current.add(0, musicItem)

        // Keep only the latest 10 items
        _recentlyPlayed.value = current.take(10)
    }

    fun getFavorites(): List<MusicItem> {
        return _musicItems.value.filter { it.isFavorite }
    }
}