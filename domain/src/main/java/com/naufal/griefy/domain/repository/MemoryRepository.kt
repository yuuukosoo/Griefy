package com.naufal.griefy.domain.repository

import com.naufal.griefy.domain.model.Memory
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {

    fun getAllMemories(): Flow<List<Memory>>

    fun getPublicMemories(): Flow<List<Memory>>

    suspend fun getMemoryById(id: Int): Memory?

    suspend fun addMemory(memory: Memory)

    suspend fun updateMemory(memory: Memory)

    suspend fun moveToTrash(id: Int)



    fun getTrashedMemories(): Flow<List<Memory>>
    suspend fun restoreFromTrash(id: Int)
    suspend fun deletePermanently(id: Int)

    suspend fun searchSongs(query: String): List<com.naufal.griefy.domain.model.Song>
}