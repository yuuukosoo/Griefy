package com.naufal.griefy.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.naufal.griefy.data.local.DailyMoodDao
import com.naufal.griefy.data.local.DailyMoodEntity
import com.naufal.griefy.domain.model.DailyMood
import com.naufal.griefy.domain.repository.DailyMoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DailyMoodRepositoryImpl @Inject constructor(
    private val dao: DailyMoodDao,
    private val firestore: FirebaseFirestore
) : DailyMoodRepository {

    private val collectionName = "daily_moods"

    override fun getMoodsForMonth(yearMonth: String): Flow<List<DailyMood>> {
        return dao.getMoodsForMonth(yearMonth).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveMood(dailyMood: DailyMood) {
        val entity = dailyMood.toEntity()
        dao.insertMood(entity)

        if (dailyMood.userId != null) {
            try {
                val moodMap = hashMapOf(
                    "id" to dailyMood.id,
                    "dateString" to dailyMood.dateString,
                    "moodValue" to dailyMood.moodValue,
                    "userId" to dailyMood.userId
                )
                firestore.collection(collectionName)
                    .document(dailyMood.id)
                    .set(moodMap)
                    .await()
            } catch (e: Exception) {
                android.util.Log.e("DailyMoodRepository", "Failed to sync mood to Firebase: ${e.message}", e)
            }
        }
    }

    override suspend fun clearLocalMoods() {
        dao.clearAll()
    }

    override suspend fun deleteMood(id: String) {
        dao.deleteMoodById(id)
        try {
            firestore.collection(collectionName).document(id).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("DailyMoodRepository", "Failed to delete mood from Firebase: ${e.message}", e)
        }
    }

    private fun DailyMoodEntity.toDomain(): DailyMood {
        return DailyMood(
            id = id,
            dateString = dateString,
            moodValue = moodValue,
            userId = userId
        )
    }

    private fun DailyMood.toEntity(): DailyMoodEntity {
        return DailyMoodEntity(
            id = id,
            dateString = dateString,
            moodValue = moodValue,
            userId = userId
        )
    }
}
