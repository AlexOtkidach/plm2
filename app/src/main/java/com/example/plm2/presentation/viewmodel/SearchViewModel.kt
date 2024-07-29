package com.example.plm2.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plm2.domain.interactor.TrackInteractorImpl
import com.example.plm2.domain.model.Track
import kotlinx.coroutines.launch

class SearchViewModel(
    private val trackInteractor: TrackInteractorImpl
) : ViewModel() {

    private val _searchResults = MutableLiveData<List<Track>>()
    val searchResults: LiveData<List<Track>> get() = _searchResults

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _searchHistory = MutableLiveData<List<Track>>()
    val searchHistoryLiveData: LiveData<List<Track>> get() = _searchHistory

    fun searchTracks(query: String) {
        viewModelScope.launch {
            try {
                val results = trackInteractor.searchTracks(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch search results: ${e.message}"
            }
        }
    }

    fun loadSearchHistory() {
        _searchHistory.value = trackInteractor.loadSearchHistory()
    }

    fun addTrackToHistory(track: Track) {
        trackInteractor.addTrackToHistory(track)
        loadSearchHistory() // Обновить историю после добавления трека
    }

    fun clearSearchHistory() {
        trackInteractor.clearSearchHistory()
        loadSearchHistory() // Обновить историю после очистки
    }
}
