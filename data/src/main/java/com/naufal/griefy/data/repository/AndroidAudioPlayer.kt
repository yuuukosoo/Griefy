package com.naufal.griefy.data.repository

import android.media.MediaPlayer
import com.naufal.griefy.domain.repository.AudioPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAudioPlayer @Inject constructor() : AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrackId = MutableStateFlow<String?>(null)
    override val currentTrackId: StateFlow<String?> = _currentTrackId.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    override val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    override val duration: StateFlow<Long> = _duration.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private fun getOrCreatePlayer(): MediaPlayer {
        val player = mediaPlayer ?: MediaPlayer().also {
            mediaPlayer = it
        }
        return player
    }

    override fun play(trackId: String, url: String) {
        if (url.isEmpty()) return

        stop()

        val player = getOrCreatePlayer()
        try {
            player.setDataSource(url)
            player.isLooping = true
            player.setOnPreparedListener { mp ->
                try {
                    mp.start()
                    _isPlaying.value = true
                    _currentTrackId.value = trackId
                    _duration.value = mp.duration.toLong()
                    startProgressTracker()
                } catch (e: Exception) {
                    android.util.Log.e("AUDIO_PLAYER", "Gagal memulai playback setelah prepared", e)
                    resetStates()
                }
            }
            player.setOnCompletionListener { mp ->
                try {
                    mp.seekTo(0)
                    mp.start()
                    _isPlaying.value = true
                } catch (e: Exception) {
                    android.util.Log.e("AUDIO_PLAYER", "Gagal memutar ulang track", e)
                    resetStates()
                }
            }
            player.setOnErrorListener { _, what, extra ->
                android.util.Log.e("AUDIO_PLAYER", "MediaPlayer error: what=$what, extra=$extra")
                resetStates()
                true
            }
            player.prepareAsync()
        } catch (e: Exception) {
            android.util.Log.e("AUDIO_PLAYER", "Gagal memutar track: $trackId", e)
            resetStates()
        }
    }

    override fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _isPlaying.value = false
                    stopProgressTracker()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AUDIO_PLAYER", "Gagal menjeda playback", e)
        }
    }

    override fun resume() {
        try {
            mediaPlayer?.let { player ->
                player.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        } catch (e: Exception) {
            android.util.Log.e("AUDIO_PLAYER", "Gagal melanjutkan playback", e)
        }
    }

    override fun stop() {
        try {
            stopProgressTracker()
            val player = mediaPlayer
            if (player != null) {
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                mediaPlayer = null
                scope.launch(Dispatchers.IO) {
                    try {
                        if (player.isPlaying) {
                            player.stop()
                        }
                        player.reset()
                        player.release()
                    } catch (e: Exception) {
                        android.util.Log.e("AUDIO_PLAYER", "Gagal menghentikan/me-release player di background", e)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AUDIO_PLAYER", "Gagal menghentikan playback", e)
        } finally {
            resetStates()
        }
    }

    override fun release() {
        stop()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                try {
                    mediaPlayer?.let { player ->
                        _currentPosition.value = player.currentPosition.toLong()
                        _duration.value = player.duration.toLong()
                    }
                } catch (_: Exception) {

                }
                delay(1000)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun resetStates() {
        _isPlaying.value = false
        _currentTrackId.value = null
        _currentPosition.value = 0L
        _duration.value = 0L
    }
}
