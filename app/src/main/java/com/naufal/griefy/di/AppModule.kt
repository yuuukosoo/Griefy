package com.naufal.griefy.di

import android.app.Application
import androidx.room.Room
import com.naufal.griefy.data.local.GriefyDatabase
import com.naufal.griefy.data.local.memory.MemoryDao
import com.naufal.griefy.data.local.reminder.RemembranceDayDao
import com.naufal.griefy.data.remote.DeezerApi
import com.naufal.griefy.data.repository.MemoryRepositoryImpl
import com.naufal.griefy.data.repository.RemembranceRepositoryImpl
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.repository.RemembranceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.naufal.griefy.data.repository.AuthRepositoryImpl
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.data.repository.NetworkRepositoryImpl
import com.naufal.griefy.domain.repository.NetworkRepository
import com.naufal.griefy.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
object AppModule {


    @Provides
    @Singleton
    fun provideGriefyDatabase(app: Application): GriefyDatabase {
        return Room.databaseBuilder(
            app,
            GriefyDatabase::class.java,
            GriefyDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }


    @Provides
    @Singleton
    fun provideMemoryDao(database: GriefyDatabase): MemoryDao {
        return database.memoryDao
    }

    @Provides
    @Singleton
    fun provideRemembranceDayDao(database: GriefyDatabase): RemembranceDayDao {
        return database.remembranceDayDao
    }

    @Provides
    @Singleton
    fun provideDailyMoodDao(database: GriefyDatabase): com.naufal.griefy.data.local.mood.DailyMoodDao {
        return database.dailyMoodDao
    }

    @Provides
    @Singleton
    fun provideMemoryRepository(
        dao: MemoryDao,
        deezerApi: DeezerApi,
        app: Application,
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        cloudinaryUploader: com.naufal.griefy.data.remote.CloudinaryUploader
    ): MemoryRepository {
        return MemoryRepositoryImpl(dao, deezerApi, app, firestore, firebaseAuth, cloudinaryUploader)
    }

    @Provides
    @Singleton
    fun provideRemembranceRepository(
        dao: RemembranceDayDao
    ): RemembranceRepository {
        return RemembranceRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideDailyMoodRepository(
        dao: com.naufal.griefy.data.local.mood.DailyMoodDao,
        firestore: FirebaseFirestore
    ): com.naufal.griefy.domain.repository.DailyMoodRepository {
        return com.naufal.griefy.data.repository.DailyMoodRepositoryImpl(dao, firestore)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .protocols(listOf(okhttp3.Protocol.HTTP_1_1))
            .build()
    }

    @Provides
    @Singleton
    fun provideDeezerApi(okHttpClient: OkHttpClient): DeezerApi {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.DEEZER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeezerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        okHttpClient: OkHttpClient,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore, okHttpClient, context)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): com.naufal.griefy.domain.repository.ReminderScheduler {
        return com.naufal.griefy.receiver.ReminderScheduler(context)
    }

    @Provides
    @Singleton
    fun provideAudioPlayer(): com.naufal.griefy.domain.repository.AudioPlayer {
        return com.naufal.griefy.data.repository.AndroidAudioPlayer()
    }

    @Provides
    @Singleton
    fun provideNetworkRepository(
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): NetworkRepository {
        return NetworkRepositoryImpl(context)
    }
}
