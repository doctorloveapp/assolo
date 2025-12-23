package com.smartinstrument.app.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.smartinstrument.app.music.MusicalKey
import com.smartinstrument.app.music.Note
import com.smartinstrument.app.music.ScaleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.nio.ByteOrder
import kotlin.coroutines.resume
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * KeyDetector - Analyzes audio to detect the musical key
 * 
 * Uses MediaCodec with async callbacks to avoid blocking.
 */
class KeyDetector(private val context: Context) {
    
    companion object {
        private const val TAG = "KeyDetector"
        private const val A4_FREQ = 440.0
        private const val A4_MIDI = 69
        private const val MAX_SAMPLES_TO_ANALYZE = 44100 * 30 // 30 seconds worth
    }
    
    data class KeyDetectionResult(
        val key: MusicalKey,
        val confidence: Float,
        val noteHistogram: Map<Note, Int>
    )
    
    /**
     * Analyze audio file and detect the key using async MediaCodec
     */
    suspend fun detectKey(uri: Uri): KeyDetectionResult? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting key detection for: $uri")
            
            val pcmSamples = decodeAudioAsync(uri)
            
            if (pcmSamples.isEmpty()) {
                Log.e(TAG, "No audio samples decoded")
                return@withContext createDefaultResult()
            }
            
            Log.d(TAG, "Decoded ${pcmSamples.size} samples, analyzing pitches...")
            
            val noteHistogram = analyzePitches(pcmSamples)
            val totalNotes = noteHistogram.values.sum()
            
            Log.d(TAG, "Detected $totalNotes pitch events from histogram: $noteHistogram")
            
            if (totalNotes < 10) {
                Log.w(TAG, "Too few pitch events, using default")
                return@withContext createDefaultResult()
            }
            
            val result = determineKeyFromHistogram(noteHistogram)
            Log.d(TAG, "Detected key: ${result.key.displayName} (confidence: ${result.confidence})")
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting key: ${e.message}", e)
            createDefaultResult()
        }
    }
    
    private fun createDefaultResult(): KeyDetectionResult {
        return KeyDetectionResult(
            key = MusicalKey(Note.A, ScaleType.MINOR),
            confidence = 0f,
            noteHistogram = emptyMap()
        )
    }
    
    /**
     * Decode audio using MediaCodec with async callbacks
     */
    private suspend fun decodeAudioAsync(uri: Uri): FloatArray = suspendCancellableCoroutine { continuation ->
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        val handlerThread = HandlerThread("AudioDecoder")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        
        val pcmSamples = mutableListOf<Float>()
        var sampleRate = 44100
        var channelCount = 2
        var completed = false
        
        try {
            // Setup extractor
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                extractor.setDataSource(pfd.fileDescriptor)
                pfd.close()
            } else {
                extractor.setDataSource(context, uri, null)
            }
            
            // Find audio track
            var audioFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
                if (mime.startsWith("audio/")) {
                    audioFormat = format
                    extractor.selectTrack(i)
                    break
                }
            }
            
            if (audioFormat == null) {
                Log.e(TAG, "No audio track found")
                continuation.resume(floatArrayOf())
                return@suspendCancellableCoroutine
            }
            
            val mime = audioFormat.getString(MediaFormat.KEY_MIME)!!
            sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            
            Log.d(TAG, "Audio: $mime, rate=$sampleRate, channels=$channelCount")
            
            codec = MediaCodec.createDecoderByType(mime)
            
            val callback = object : MediaCodec.Callback() {
                override fun onInputBufferAvailable(mc: MediaCodec, index: Int) {
                    if (completed || pcmSamples.size >= MAX_SAMPLES_TO_ANALYZE) return
                    
                    try {
                        val inputBuffer = mc.getInputBuffer(index) ?: return
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        
                        if (sampleSize < 0) {
                            mc.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        } else {
                            mc.queueInputBuffer(index, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Input error: ${e.message}")
                    }
                }
                
                override fun onOutputBufferAvailable(mc: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                    if (completed) return
                    
                    try {
                        if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            finishDecoding()
                            return
                        }
                        
                        val outputBuffer = mc.getOutputBuffer(index)
                        if (outputBuffer != null && info.size > 0) {
                            outputBuffer.position(info.offset)
                            outputBuffer.limit(info.offset + info.size)
                            
                            val shortBuffer = outputBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                            
                            // Downsample to ~22050 Hz and convert to mono
                            val downsampleRatio = (sampleRate / 22050).coerceAtLeast(1)
                            var sampleIndex = 0
                            
                            while (shortBuffer.hasRemaining() && pcmSamples.size < MAX_SAMPLES_TO_ANALYZE) {
                                var sample = 0f
                                repeat(channelCount) {
                                    if (shortBuffer.hasRemaining()) {
                                        sample += shortBuffer.get() / 32768f
                                    }
                                }
                                sample /= channelCount
                                
                                if (sampleIndex % downsampleRatio == 0) {
                                    pcmSamples.add(sample)
                                }
                                sampleIndex++
                            }
                            
                            // Check if we have enough samples
                            if (pcmSamples.size >= MAX_SAMPLES_TO_ANALYZE) {
                                finishDecoding()
                                return
                            }
                        }
                        
                        mc.releaseOutputBuffer(index, false)
                    } catch (e: Exception) {
                        Log.e(TAG, "Output error: ${e.message}")
                    }
                }
                
                override fun onError(mc: MediaCodec, e: MediaCodec.CodecException) {
                    Log.e(TAG, "Codec error: ${e.message}")
                    finishDecoding()
                }
                
                override fun onOutputFormatChanged(mc: MediaCodec, format: MediaFormat) {
                    Log.d(TAG, "Output format changed: $format")
                }
                
                private fun finishDecoding() {
                    if (completed) return
                    completed = true
                    
                    try {
                        codec?.stop()
                        codec?.release()
                        extractor.release()
                        handlerThread.quitSafely()
                    } catch (e: Exception) {
                        Log.e(TAG, "Cleanup error: ${e.message}")
                    }
                    
                    Log.d(TAG, "Decoding finished with ${pcmSamples.size} samples")
                    continuation.resume(pcmSamples.toFloatArray())
                }
            }
            
            codec.setCallback(callback, handler)
            codec.configure(audioFormat, null, null, 0)
            codec.start()
            
            // Safety timeout - if decoding takes too long, return what we have
            handler.postDelayed({
                if (!completed) {
                    Log.w(TAG, "Decoding timeout, returning ${pcmSamples.size} samples")
                    completed = true
                    try {
                        codec?.stop()
                        codec?.release()
                        extractor.release()
                        handlerThread.quitSafely()
                    } catch (e: Exception) { }
                    continuation.resume(pcmSamples.toFloatArray())
                }
            }, 8000) // 8 second timeout
            
        } catch (e: Exception) {
            Log.e(TAG, "Setup error: ${e.message}", e)
            try {
                codec?.release()
                extractor.release()
                handlerThread.quitSafely()
            } catch (ex: Exception) { }
            continuation.resume(floatArrayOf())
        }
        
        continuation.invokeOnCancellation {
            completed = true
            try {
                codec?.release()
                extractor.release()
                handlerThread.quitSafely()
            } catch (e: Exception) { }
        }
    }
    
    /**
     * Simple low-pass filter to isolate bass frequencies
     */
    private fun applyLowPassFilter(samples: FloatArray, sampleRate: Int, cutoffHz: Float): FloatArray {
        val filtered = FloatArray(samples.size)
        val rc = 1.0f / (2.0f * kotlin.math.PI.toFloat() * cutoffHz)
        val dt = 1.0f / sampleRate
        val alpha = dt / (rc + dt)
        
        filtered[0] = samples[0]
        for (i in 1 until samples.size) {
            filtered[i] = filtered[i - 1] + alpha * (samples[i] - filtered[i - 1])
        }
        return filtered
    }
    
    /**
     * Analyze pitches using autocorrelation - focus on bass frequencies
     */
    private fun analyzePitches(samples: FloatArray): MutableMap<Note, Int> {
        val noteHistogram = mutableMapOf<Note, Int>()
        Note.entries.forEach { noteHistogram[it] = 0 }
        
        val windowSize = 4096  // Larger window for better bass resolution
        val hopSize = 2048
        val effectiveSampleRate = 22050 // We downsampled to this
        
        // Apply low-pass filter to focus on bass (< 300 Hz)
        val bassFiltered = applyLowPassFilter(samples, effectiveSampleRate, 300f)
        
        var position = 0
        var windowsAnalyzed = 0
        
        while (position + windowSize < bassFiltered.size) {
            val window = bassFiltered.sliceArray(position until position + windowSize)
            
            // Check energy (skip silence)
            val energy = window.sumOf { (it * it).toDouble() } / window.size
            if (energy > 0.0005) { // Lower threshold for filtered signal
                val pitch = detectPitchAutocorrelation(window, effectiveSampleRate)
                if (pitch > 0) {
                    val note = frequencyToNote(pitch.toDouble())
                    noteHistogram[note] = (noteHistogram[note] ?: 0) + 1
                    windowsAnalyzed++
                }
            }
            
            position += hopSize
        }
        
        Log.d(TAG, "Analyzed $windowsAnalyzed windows with pitch (bass-filtered)")
        return noteHistogram
    }
    
    /**
     * Detect pitch using autocorrelation - optimized for bass
     */
    private fun detectPitchAutocorrelation(buffer: FloatArray, sampleRate: Int): Float {
        val minFreq = 40f   // Lowest bass note (E1 ~ 41 Hz)
        val maxFreq = 300f  // Upper limit for bass
        
        val minPeriod = (sampleRate / maxFreq).toInt()
        val maxPeriod = (sampleRate / minFreq).toInt().coerceAtMost(buffer.size / 2)
        
        // Compute autocorrelation
        var bestPeriod = 0
        var bestCorrelation = 0f
        
        // Normalize by first autocorrelation value
        var r0 = 0f
        for (i in 0 until buffer.size / 2) {
            r0 += buffer[i] * buffer[i]
        }
        if (r0 < 0.0001f) return -1f // Too quiet
        
        for (lag in minPeriod..maxPeriod) {
            var correlation = 0f
            for (i in 0 until buffer.size / 2) {
                if (i + lag < buffer.size) {
                    correlation += buffer[i] * buffer[i + lag]
                }
            }
            correlation /= r0
            
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation
                bestPeriod = lag
            }
        }
        
        // Need decent correlation for valid pitch
        if (bestCorrelation < 0.4f || bestPeriod == 0) return -1f
        
        // Parabolic interpolation for better accuracy
        if (bestPeriod > minPeriod && bestPeriod < maxPeriod - 1) {
            val y0 = autocorrelationAt(buffer, bestPeriod - 1)
            val y1 = autocorrelationAt(buffer, bestPeriod)
            val y2 = autocorrelationAt(buffer, bestPeriod + 1)
            
            val shift = (y0 - y2) / (2 * (y0 - 2 * y1 + y2))
            if (shift.isFinite() && kotlin.math.abs(shift) < 1f) {
                val refinedPeriod = bestPeriod + shift
                return sampleRate / refinedPeriod
            }
        }
        
        return sampleRate.toFloat() / bestPeriod
    }
    
    private fun autocorrelationAt(buffer: FloatArray, lag: Int): Float {
        var sum = 0f
        for (i in 0 until buffer.size / 2) {
            if (i + lag < buffer.size) {
                sum += buffer[i] * buffer[i + lag]
            }
        }
        return sum
    }
    
    private fun frequencyToNote(frequency: Double): Note {
        val semitones = 12 * ln(frequency / A4_FREQ) / ln(2.0)
        val midiNote = (A4_MIDI + semitones).roundToInt()
        val noteIndex = ((midiNote % 12) + 12) % 12
        return Note.entries[noteIndex]
    }
    
    /**
     * Determine key using Krumhansl-Schmuckler algorithm
     */
    private fun determineKeyFromHistogram(histogram: Map<Note, Int>): KeyDetectionResult {
        // Krumhansl-Kessler key profiles
        val majorProfile = doubleArrayOf(6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88)
        val minorProfile = doubleArrayOf(6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17)
        
        // Build note distribution
        val distribution = DoubleArray(12) { histogram[Note.entries[it]]?.toDouble() ?: 0.0 }
        val sum = distribution.sum()
        if (sum > 0) {
            for (i in distribution.indices) distribution[i] /= sum
        }
        
        Log.d(TAG, "Note distribution: ${Note.entries.zip(distribution.toList()).joinToString { "${it.first.name}=${String.format("%.2f", it.second)}" }}")
        
        var bestKey = MusicalKey(Note.A, ScaleType.MINOR)
        var bestCorr = -2.0
        
        // Try all 24 possible keys
        for (rootIndex in 0 until 12) {
            val rotated = DoubleArray(12) { distribution[(it + rootIndex) % 12] }
            
            val majorCorr = correlate(rotated, majorProfile)
            if (majorCorr > bestCorr) {
                bestCorr = majorCorr
                bestKey = MusicalKey(Note.entries[rootIndex], ScaleType.MAJOR)
            }
            
            val minorCorr = correlate(rotated, minorProfile)
            if (minorCorr > bestCorr) {
                bestCorr = minorCorr
                bestKey = MusicalKey(Note.entries[rootIndex], ScaleType.MINOR)
            }
        }
        
        val confidence = ((bestCorr + 1) / 2).toFloat().coerceIn(0f, 1f)
        return KeyDetectionResult(bestKey, confidence, histogram)
    }
    
    private fun correlate(a: DoubleArray, b: DoubleArray): Double {
        val meanA = a.average()
        val meanB = b.average()
        var num = 0.0
        var denA = 0.0
        var denB = 0.0
        
        for (i in a.indices) {
            val dA = a[i] - meanA
            val dB = b[i] - meanB
            num += dA * dB
            denA += dA * dA
            denB += dB * dB
        }
        
        val den = sqrt(denA) * sqrt(denB)
        return if (den > 0) num / den else 0.0
    }
}
