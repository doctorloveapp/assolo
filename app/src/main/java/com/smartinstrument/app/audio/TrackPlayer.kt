package com.smartinstrument.app.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * TrackPlayer - Manages playback of backing tracks using ExoPlayer (Media3)
 */
class TrackPlayer(private val context: Context) {
    
    private var exoPlayer: ExoPlayer? = null
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _isTrackLoaded = MutableStateFlow(false)
    val isTrackLoaded: StateFlow<Boolean> = _isTrackLoaded.asStateFlow()
    
    private val _trackName = MutableStateFlow<String?>(null)
    val trackName: StateFlow<String?> = _trackName.asStateFlow()
    
    private var _volume = 1.0f
    
    init {
        initializePlayer()
    }
    
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _duration.value = duration
                            _isTrackLoaded.value = true
                        }
                        Player.STATE_ENDED -> {
                            _isPlaying.value = false
                            seekTo(0)
                        }
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
            })
        }
    }
    
    /**
     * Load a track from URI
     */
    fun loadTrack(uri: Uri, fileName: String? = null) {
        exoPlayer?.let { player ->
            val mediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            _trackName.value = fileName ?: uri.lastPathSegment ?: "Unknown Track"
            _isTrackLoaded.value = false // Will be set to true when ready
        }
    }
    
    /**
     * Load a track from assets folder
     */
    fun loadAssetTrack(assetFileName: String) {
        exoPlayer?.let { player ->
            val assetUri = Uri.parse("asset:///tracks/$assetFileName")
            val mediaItem = MediaItem.fromUri(assetUri)
            player.setMediaItem(mediaItem)
            player.prepare()
            _trackName.value = assetFileName.removeSuffix(".mp3")
            _isTrackLoaded.value = false
        }
    }
    
    /**
     * Get list of available built-in tracks
     */
    fun getBuiltInTracks(): List<String> {
        return try {
            context.assets.list("tracks")?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Play the loaded track
     */
    fun play() {
        exoPlayer?.play()
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        exoPlayer?.pause()
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                pause()
            } else {
                play()
            }
        }
    }
    
    /**
     * Stop and reset to beginning
     */
    fun stop() {
        exoPlayer?.let { player ->
            player.pause()
            player.seekTo(0)
            _isPlaying.value = false
        }
    }
    
    /**
     * Seek to position in milliseconds
     */
    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }
    
    /**
     * Set volume (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        _volume = volume.coerceIn(0f, 1f)
        exoPlayer?.volume = _volume
    }
    
    /**
     * Get current volume
     */
    fun getVolume(): Float = _volume
    
    /**
     * Update current position (call from a coroutine loop)
     */
    fun updatePosition() {
        exoPlayer?.let { player ->
            _currentPosition.value = player.currentPosition
        }
    }
    
    /**
     * Get the URI of the currently loaded track for analysis
     */
    fun getCurrentTrackUri(): Uri? {
        return exoPlayer?.currentMediaItem?.localConfiguration?.uri
    }
    
    /**
     * Release resources
     */
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
