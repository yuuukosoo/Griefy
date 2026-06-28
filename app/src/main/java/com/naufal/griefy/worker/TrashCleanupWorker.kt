package com.naufal.griefy.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class TrashCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: MemoryRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            android.util.Log.d("WORKER_CEK", "Petugas Kebersihan mulai bekerja...")


            val trashedMemories = repository.getTrashedMemories().first()


            trashedMemories.forEach { memory ->
                repository.deletePermanently(memory.id)
            }

            android.util.Log.d("WORKER_CEK", "Trash berhasil dibersihkan!")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("WORKER_ERROR", "Gagal membersihkan trash", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            Result.retry()
        }
    }
}