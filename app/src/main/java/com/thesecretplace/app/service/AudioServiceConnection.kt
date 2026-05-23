package com.thesecretplace.app.service

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class AudioState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val nowPlayingTitle: String = "",
    val nowPlayingMeditationId: String = "",
    val isDownloading: Boolean = false,
    val didFinish: Boolean = false
)

@Singleton
class AudioServiceConnection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controller: MediaController? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionUpdateJob: Job? = null

    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private var looping = false

    fun connect() {
        if (controller != null) return

        val sessionToken = SessionToken(context, ComponentName(context, AudioService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                controller = controllerFuture.get()
                controller?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _audioState.value = _audioState.value.copy(isPlaying = isPlaying)
                        if (isPlaying) startPositionUpdates() else stopPositionUpdates()
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            if (looping) {
                                controller?.seekTo(0)
                                controller?.play()
                            } else {
                                _audioState.value = _audioState.value.copy(
                                    isPlaying = false,
                                    didFinish = true
                                )
                                stopPositionUpdates()
                                println("✅ Playback finished")
                            }
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        println("❌ Player error: ${error.message}")
                        _audioState.value = _audioState.value.copy(
                            isPlaying = false,
                            isDownloading = false
                        )
                        stopPositionUpdates()
                    }
                })
                println("✅ MediaController connected to AudioService")
            } catch (e: Exception) {
                println("❌ MediaController connection failed: ${e.message}")
            }
        }, MoreExecutors.directExecutor())
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                controller?.let { ctrl ->
                    _audioState.value = _audioState.value.copy(
                        currentPosition = ctrl.currentPosition.coerceAtLeast(0),
                        duration = ctrl.duration.coerceAtLeast(0)
                    )
                }
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
    }

    // Play bundled audio from res/raw
    @androidx.annotation.OptIn(UnstableApi::class)
    fun playAudio(
        fileName: String,
        title: String = "",
        meditationId: String = "",
        loop: Boolean = false
    ) {
        val snakeName = fileName.replace(Regex("([a-z])([A-Z])")) {
            "${it.groupValues[1]}_${it.groupValues[2]}"
        }.lowercase()
        val resId = context.resources.getIdentifier(snakeName, "raw", context.packageName)
        if (resId == 0) {
            println("❌ Audio file $fileName ($snakeName) not found in res/raw")
            return
        }
        val uri = RawResourceDataSource.buildRawResourceUri(resId)
        playUri(uri, title, meditationId, loop)
    }

    // Play from a URI (local file or remote)
    fun playUri(uri: Uri, title: String, meditationId: String, loop: Boolean = false) {
        val ctrl = controller ?: run {
            println("⏳ MediaController not ready — queuing playback")
            connect()
            // Retry after connection establishes
            scope.launch {
                var attempts = 0
                while (controller == null && attempts < 20) {
                    delay(100)
                    attempts++
                }
                controller?.let { playUri(uri, title, meditationId, loop) }
                    ?: println("❌ MediaController failed to connect after retries")
            }
            return
        }

        looping = loop
        _audioState.value = _audioState.value.copy(
            didFinish = false,
            nowPlayingTitle = title,
            nowPlayingMeditationId = meditationId,
            isDownloading = false
        )

        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist("The Secret Place")
                    .build()
            )
            .build()

        ctrl.setMediaItem(mediaItem)
        ctrl.prepare()
        ctrl.play()
        println("✅ Playing: $title (uri: $uri)")
    }

    // Download remote audio, cache, then play
    suspend fun downloadAndPlay(
        remoteUrl: String,
        title: String,
        meditationId: String
    ) {
        val cacheDir = File(context.cacheDir, "meditations")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val cacheFile = File(cacheDir, "$meditationId.mp3")

        if (cacheFile.exists()) {
            playUri(Uri.fromFile(cacheFile), title, meditationId)
            return
        }

        _audioState.value = _audioState.value.copy(isDownloading = true)
        withContext(Dispatchers.IO) {
            try {
                val url = java.net.URL(remoteUrl)
                val connection = url.openConnection()
                connection.connect()
                connection.getInputStream().use { input ->
                    cacheFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                println("✅ Remote audio cached: ${cacheFile.name}")
            } catch (e: Exception) {
                println("❌ Download failed: ${e.message}")
                _audioState.value = _audioState.value.copy(isDownloading = false)
                return@withContext
            }
        }
        _audioState.value = _audioState.value.copy(isDownloading = false)
        playUri(Uri.fromFile(cacheFile), title, meditationId)
    }

    fun hasCachedAudio(meditationId: String): Boolean {
        val cacheFile = File(File(context.cacheDir, "meditations"), "$meditationId.mp3")
        return cacheFile.exists()
    }

    fun playCached(meditationId: String, title: String) {
        val cacheFile = File(File(context.cacheDir, "meditations"), "$meditationId.mp3")
        if (cacheFile.exists()) {
            playUri(Uri.fromFile(cacheFile), title, meditationId)
        } else {
            println("❌ No cached audio for $meditationId")
        }
    }

    fun pause() {
        controller?.pause()
    }

    fun resume() {
        controller?.play()
    }

    fun stop() {
        controller?.stop()
        controller?.clearMediaItems()
        _audioState.value = AudioState()
        stopPositionUpdates()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _audioState.value = _audioState.value.copy(currentPosition = positionMs)
    }

    fun setLooping(loop: Boolean) {
        looping = loop
    }

    fun consumeFinishEvent() {
        _audioState.value = _audioState.value.copy(
            didFinish = false,
            nowPlayingMeditationId = ""
        )
    }

    fun disconnect() {
        scope.cancel()
        stopPositionUpdates()
        controller?.release()
        controller = null
    }
}
