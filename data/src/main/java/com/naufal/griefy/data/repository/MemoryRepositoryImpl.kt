package com.naufal.griefy.data.repository

import com.naufal.griefy.data.local.MemoryDao
import com.naufal.griefy.data.remote.SpotifyApi
import com.naufal.griefy.data.remote.SpotifyAuthApi
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class MemoryRepositoryImpl @Inject constructor(
    private val dao: MemoryDao,
    private val spotifyApi: SpotifyApi,
    private val authApi: SpotifyAuthApi
) : MemoryRepository {

    override fun getAllMemories(): Flow<List<Memory>> {

        return dao.getAllMemories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPublicMemories(): Flow<List<Memory>> {

        return dao.getAllMemories().map { entities ->
            entities.filter { it.isPublic }.map { it.toDomain() }
        }
    }

    override suspend fun getMemoryById(id: Int): Memory? {
        return dao.getMemoryById(id)?.toDomain()
    }


    override fun getMemoryByIdAsFlow(id: Int): Flow<Memory?> {
        return dao.getMemoryByIdAsFlow(id).map { it?.toDomain() }
    }

    override suspend fun addMemory(memory: Memory) {
        dao.insertMemory(memory.toEntity())
    }

    override suspend fun updateMemory(memory: Memory) {
        dao.updateMemory(memory.toEntity())
    }

    override suspend fun moveToTrash(id: Int) {
        dao.moveToTrash(id)
    }

    override fun getTrashedMemories(): Flow<List<Memory>> {
        return dao.getTrashedMemories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun restoreFromTrash(id: Int) {
        dao.restoreFromTrash(id)
    }

    override suspend fun deletePermanently(id: Int) {
        dao.deletePermanently(id)
    }

    override suspend fun searchSongs(query: String): List<com.naufal.griefy.domain.model.Song> {
        return try {

            val rawClientId = com.naufal.griefy.data.BuildConfig.SPOTIFY_CLIENT_ID
            val rawClientSecret = com.naufal.griefy.data.BuildConfig.SPOTIFY_CLIENT_SECRET

            val cleanClientId = rawClientId.replace("\"", "").trim()
            val cleanClientSecret = rawClientSecret.replace("\"", "").trim()

            android.util.Log.d("SPOTIFY_CEK", "Kunci Bersih: $cleanClientId")


            val tokenResponse = authApi.getAccessToken(
                clientId = cleanClientId,
                clientSecret = cleanClientSecret
            )


            val bearerToken = "Bearer ${tokenResponse.access_token}"
            val response = spotifyApi.searchTracks(token = bearerToken, query = query)


            response.tracks.items.map { trackDto ->
                com.naufal.griefy.domain.model.Song(
                    trackId = trackDto.id,
                    title = trackDto.name,
                    artistName = trackDto.artists.firstOrNull()?.name ?: "Unknown",
                    imageUrl = trackDto.album.images.firstOrNull()?.url ?: "",
                    previewUrl = trackDto.preview_url
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("SPOTIFY_ERROR", "Gagal ambil lagu: ${e.message}", e)
            emptyList()
        }
    }
}
