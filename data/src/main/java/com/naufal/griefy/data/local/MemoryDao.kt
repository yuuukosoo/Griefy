package com.naufal.griefy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {


    @Query("SELECT * FROM memory_table WHERE isTrashed = 0 ORDER BY createdAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>


    @Query("SELECT * FROM memory_table WHERE id = :id")
    suspend fun getMemoryById(id: Int): MemoryEntity?

    @Query("SELECT * FROM memory_table WHERE id = :id")
    fun getMemoryByIdAsFlow(id: Int): Flow<MemoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)


    @Update
    suspend fun updateMemory(memory: MemoryEntity)


    @Query("UPDATE memory_table SET isTrashed = 1 WHERE id = :id")
    suspend fun moveToTrash(id: Int)


    @Query("SELECT * FROM memory_table WHERE isTrashed = 1 ORDER BY createdAt DESC")
    fun getTrashedMemories(): Flow<List<MemoryEntity>>


    @Query("UPDATE memory_table SET isTrashed = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: Int)


    @Query("DELETE FROM memory_table WHERE id = :id")
    suspend fun deletePermanently(id: Int)
}