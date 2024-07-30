package com.example.plm2.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.domain.interactor.TrackInteractorImpl

class SearchViewModelFactory(private val trackInteractor: TrackInteractorImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(trackInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
