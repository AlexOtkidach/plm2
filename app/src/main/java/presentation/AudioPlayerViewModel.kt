package presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.GetTracksUseCase
import kotlinx.coroutines.launch


class AudioPlayerViewModel(
    private val getTracksUseCase: GetTracksUseCase
): ViewModel() {

    fun getTracks() {
        viewModelScope.launch {
            val tracks = getTracksUseCase.execute()
            // Обновление UI с помощью полученных треков
        }
    }
}