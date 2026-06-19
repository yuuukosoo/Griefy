package com.naufal.griefy.di

import android.app.Application
import androidx.room.Room
import com.naufal.griefy.data.local.GriefyDatabase
import com.naufal.griefy.data.local.MemoryDao
import com.naufal.griefy.data.local.RemembranceDayDao
import com.naufal.griefy.data.remote.DeezerApi
import com.naufal.griefy.data.repository.MemoryRepositoryImpl
import com.naufal.griefy.data.repository.RemembranceRepositoryImpl
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.repository.RemembranceRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.naufal.griefy.data.repository.AuthRepositoryImpl
import com.naufal.griefy.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
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
    fun provideMemoryRepository(
        dao: MemoryDao,
        deezerApi: DeezerApi,
        app: Application,
        firestore: FirebaseFirestore
    ): MemoryRepository {
        return MemoryRepositoryImpl(dao, deezerApi, app, firestore)
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
    fun provideDeezerApi(): DeezerApi {
        return Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
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
        firebaseAuth: FirebaseAuth
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}