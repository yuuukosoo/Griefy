package com.naufal.griefy

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.naufal.griefy.worker.TrashCleanupWorker
import java.util.concurrent.TimeUnit
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GriefyApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        
        val trashCleanupWorkRequest = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
            1, TimeUnit.DAYS 
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TrashCleanupWork",
            ExistingPeriodicWorkPolicy.KEEP,
            trashCleanupWorkRequest
        )
    }


    @Inject
    lateinit var workerFactory: HiltWorkerFactory


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}