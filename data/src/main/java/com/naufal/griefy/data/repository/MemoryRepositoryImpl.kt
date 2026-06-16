package com.naufal.griefy.data.repository

import com.naufal.griefy.data.local.MemoryDao
import com.naufal.griefy.data.remote.SpotifyApi
import com.naufal.griefy.data.remote.SpotifyAuthApi
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.Song
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

    override suspend fun searchSongs(query: String): List<Song> {
        return try {

            val clientId = com.naufal.griefy.data.BuildConfig.SPOTIFY_CLIENT_ID
            val clientSecret = com.naufal.griefy.data.BuildConfig.SPOTIFY_CLIENT_SECRET


            val tokenResponse = authApi.getAccessToken(
                clientId = clientId,
                clientSecret = clientSecret
            )


            val bearerToken = "Bearer ${tokenResponse.access_token}"


            val response = spotifyApi.searchTracks(token = bearerToken, query = query)

            
            response.tracks.items.map { it.toSong() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
