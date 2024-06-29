package com.example.plm2.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plm2.domain.interactor.TrackInteractor
import com.example.plm2.domain.model.Track
import kotlinx.coroutines.launch

class SearchViewModel(private val trackInteractor: TrackInteractor) : ViewModel() {
    private val _searchResults = MutableLiveData<List<Track>>()
    val searchResults: LiveData<List<Track>> = _searchResults

    fun searchTracks(query: String) {
        viewModelScope.launch {
            val results = trackInteractor.searchTracks(query)
            _searchResults.postValue(results)
        }
    }
}
