package domain

interface GetTracksUseCase {
    suspend fun execute(): List<Track>
}