package com.naufal.griefy.di

import android.app.Application
import androidx.room.Room
import com.naufal.griefy.data.local.GriefyDatabase
import com.naufal.griefy.data.local.MemoryDao
import com.naufal.griefy.data.local.RemembranceDayDao
import com.naufal.griefy.data.remote.SpotifyApi
import com.naufal.griefy.data.remote.SpotifyAuthApi
import com.naufal.griefy.data.repository.MemoryRepositoryImpl
import com.naufal.griefy.data.repository.RemembranceRepositoryImpl
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.repository.RemembranceRepository
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
        spotifyApi: SpotifyApi,
        authApi: SpotifyAuthApi
    ): MemoryRepository {
        return MemoryRepositoryImpl(dao, spotifyApi, authApi)
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
    fun provideSpotifyApi(): SpotifyApi {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyAuthApi(): SpotifyAuthApi {
        return Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyAuthApi::class.java)
    }
}