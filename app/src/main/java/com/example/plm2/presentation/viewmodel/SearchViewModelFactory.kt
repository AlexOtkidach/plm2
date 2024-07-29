package com.example.plm2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.domain.interactor.TrackInteractor
import com.example.plm2.data.local.SearchHistory

class SearchViewModelFactory(
    private val trackInteractor: TrackInteractor,
    private val searchHistory: SearchHistory
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(trackInteractor, searchHistory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
