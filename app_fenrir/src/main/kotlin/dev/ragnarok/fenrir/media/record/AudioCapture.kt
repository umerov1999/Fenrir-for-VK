package dev.ragnarok.fenrir.media.record

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.SystemClock
import android.util.Log
import dev.ragnarok.fenrir.module.encoder.AudioNoiseSuppressor
import dev.ragnarok.fenrir.module.encoder.AudioRecorderNative
import dev.ragnarok.fenrir.orZero
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.Volatile
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Captures microphone PCM and encodes it into one Ogg/Opus output.
 * 
 * 
 * Capture and encoding run on separate threads, so the microphone starts immediately while
 * the encoder is still re-encoding a retained prefix. Captured chunks queue up on the encoder
 * thread until it is ready.
 */
internal class AudioCapture(private val outputPath: String, private val callback: Callback) {
    internal interface Callback {
        /**
         * Called when the microphone is recording; the encoder may still be preparing.
         */
        fun onStarted()

        /**
         * Called with the raw microphone RMS amplitude at most every 32 ms.
         */
        fun onProgress(capturedSamples: Long, amplitude: Double)

        /**
         * Called when the output file has been flushed up to the given size.
         */
        fun onOutputCommitted(committedFileSize: Long)

        fun onStopped(cancel: Boolean)

        fun onPaused()

        fun onResumed()

        fun onError(error: Throwable)
    }

    private val captureThread: HandlerThread =
        HandlerThread("AudioRecorderCapture", Process.THREAD_PRIORITY_AUDIO)
    private val captureHandler: Handler
    private val encoderThread: HandlerThread
    private val encoderHandler: Handler
    private val chunkPool = ConcurrentLinkedQueue<ByteBuffer>()
    private val queuedChunks = AtomicInteger()

    @Volatile
    private var stopRequested = false

    @Volatile
    private var cancelRequested = false

    @Volatile
    private var captureStopped = false

    @Volatile
    private var isPaused = false

    // Capture thread.
    private var audioRecord: AudioRecord? = null
    private var capturedSamples: Long = 0
    private var lastProgressMs: Long = 0

    // Encoder thread.
    private val frameBuffer: ByteBuffer = ByteBuffer.allocateDirect(FRAME_BYTES)
        .order(ByteOrder.nativeOrder())
    private var encoder: AudioRecorderNative? = null
    private var noiseSuppressor: AudioNoiseSuppressor? = null
    private var encoderFailure: Throwable? = null
    private var encodingFinished = false
    private var lastFlushMs: Long = 0
    private var maximumQueuedChunks = 0
    private var noiseProcessingCount: Long = 0
    private var noiseProcessingTotalNs: Long = 0
    private var noiseProcessingMaxNs: Long = 0

    @Volatile
    private var recordingStartedAtMs: Long = 0L

    @Volatile
    private var totalPausedDurationMs: Long = 0L

    @Volatile
    private var pauseStartedAtMs: Long = 0L

    init {
        captureThread.start()
        captureHandler = Handler(captureThread.getLooper())
        encoderThread = HandlerThread("AudioRecorderEncoder", Process.THREAD_PRIORITY_DEFAULT)
        encoderThread.start()
        encoderHandler = Handler(encoderThread.getLooper())
    }

    fun getRecordingDurationMs(): Long {
        val nowMs = SystemClock.elapsedRealtime()
        val startedAtMs = recordingStartedAtMs

        if (startedAtMs == 0L) {
            return 0L
        }

        val currentPauseMs = if (isPaused && pauseStartedAtMs > 0L) {
            nowMs - pauseStartedAtMs
        } else {
            0L
        }

        return (nowMs - startedAtMs - totalPausedDurationMs - currentPauseMs)
            .coerceAtLeast(0L)
    }

    fun start() {
        if (stopRequested || cancelRequested) {
            return
        }
        encoderHandler.post { this.startEncoder() }
        captureHandler.post { this.startCapture() }
    }

    fun stop() {
        if (stopRequested || cancelRequested) {
            return
        }
        stopRequested = true
        if (!captureStopped) {
            captureHandler.postAtFrontOfQueue { stopCapture(null) }
        }
    }

    fun cancel() {
        if (stopRequested || cancelRequested) {
            return
        }
        cancelRequested = true
        if (!captureStopped) {
            captureHandler.postAtFrontOfQueue { stopCapture(null) }
        }
    }

    fun pause() {
        if (!stopRequested && !cancelRequested && !isPaused) {
            pauseStartedAtMs = SystemClock.elapsedRealtime()
            isPaused = true
            callback.onPaused()
        }
    }

    fun resume() {
        if (!stopRequested && !cancelRequested && isPaused) {
            val nowMs = SystemClock.elapsedRealtime()
            val pausedAtMs = pauseStartedAtMs

            if (pausedAtMs > 0L) {
                totalPausedDurationMs += nowMs - pausedAtMs
            }

            pauseStartedAtMs = 0L
            isPaused = false
            lastProgressMs = nowMs
            callback.onResumed()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCapture() {
        try {
            if (stopRequested || cancelRequested) {
                stopCapture(null)
                return
            }
            var minimumBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minimumBufferSize <= 0) {
                minimumBufferSize = FRAME_BYTES * 4
            }
            val audioRecordBufferSize = max(minimumBufferSize * 2, FRAME_BYTES * 4)
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioRecordBufferSize
            )
            check(audioRecord?.state == AudioRecord.STATE_INITIALIZED) { "Unable to initialize AudioRecord" }
            audioRecord?.startRecording()
            check(audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) { "Unable to start AudioRecord" }
            Log.i(
                "AudioRecord", ("started: minBufferBytes=" + minimumBufferSize
                        + ", bufferBytes=" + audioRecordBufferSize
                        + ", sessionId=" + audioRecord?.audioSessionId)
            )
            lastProgressMs = SystemClock.elapsedRealtime()

            recordingStartedAtMs = lastProgressMs
            totalPausedDurationMs = 0L
            pauseStartedAtMs = 0L

            callback.onStarted()
            captureHandler.post { this.readOnce() }
        } catch (error: Throwable) {
            stopCapture(error)
        }
    }

    private fun readOnce() {
        if (captureStopped) return
        if (stopRequested || cancelRequested) {
            stopCapture(null)
            return
        }
        if (isPaused) {
            captureHandler.post { this.readOnce() }
            return
        }
        val chunk = obtainChunk()
        var queued = false
        try {
            val bytesRead = audioRecord?.read(chunk, chunk.capacity()).orZero()
            check(bytesRead > 0) { "AudioRecord read failed: $bytesRead" }
            chunk.limit(bytesRead)
            var sum = 0L
            val sampleCount = bytesRead / 2
            var offset = 0
            while (offset + 1 < bytesRead) {
                val sample = chunk.getShort(offset).toInt()
                sum += sample.toLong() * sample
                offset += 2
            }
            capturedSamples += sampleCount.toLong()
            queuedChunks.incrementAndGet()
            encoderHandler.post { encodeChunk(chunk) }
            queued = true

            val nowMs = SystemClock.elapsedRealtime()
            if (nowMs - lastProgressMs >= PROGRESS_INTERVAL_MS) {
                val amplitude = if (sampleCount == 0) 0.0 else sqrt(sum.toDouble() / sampleCount)
                callback.onProgress(capturedSamples, amplitude)
                lastProgressMs = nowMs
            }
            captureHandler.post { this.readOnce() }
        } catch (error: Throwable) {
            if (!queued) recycleChunk(chunk)
            stopCapture(if (stopRequested || cancelRequested) null else error)
        }
    }

    private fun stopCapture(failure: Throwable?) {
        var tmpFailure = failure
        if (captureStopped) return
        captureStopped = true
        stopRequested = true
        if (tmpFailure != null) {
            Log.e("AudioCapture", "capture failed", tmpFailure)
        }
        if (audioRecord != null) {
            try {
                if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord?.stop()
                }
            } catch (error: Throwable) {
                if (tmpFailure == null) tmpFailure = error
            } finally {
                audioRecord?.release()
                audioRecord = null
            }
        }
        val samples = capturedSamples
        encoderHandler.post { finishEncoding(samples, tmpFailure) }
        captureThread.quitSafely()
        isPaused = false
    }

    private fun startEncoder() {
        try {
            val startedAtMs = SystemClock.elapsedRealtime()
            encoder = AudioRecorderNative()
            check(
                encoder?.create(
                    outputPath,
                    SAMPLE_RATE
                ) == true
            ) { "Unable to initialize native Opus recorder" }
            noiseSuppressor = AudioNoiseSuppressor()
            if (noiseSuppressor?.create() == false) {
                noiseSuppressor = null
                Log.e("AudioCapture", "RNNoise unavailable; recording unprocessed PCM")
            } else {
                Log.i(
                    "AudioCapture", ("RNNoise started: sampleRate=" + SAMPLE_RATE
                            + ", captureFrameSamples=" + FRAME_SAMPLES)
                )
            }
            val initialFileSize = encoder?.flush().orZero()
            Log.i(
                "AudioCapture", ("Opus encoder started: prefix="
                        + ", initialBytes=" + initialFileSize
                        + ", queuedChunks=" + queuedChunks.get()
                        + ", elapsedMs=" + (SystemClock.elapsedRealtime() - startedAtMs))
            )
            lastFlushMs = SystemClock.elapsedRealtime()
            if (initialFileSize > 0L) {
                callback.onOutputCommitted(initialFileSize)
            }
        } catch (error: Throwable) {
            failEncoder(error)
        }
    }

    private fun encodeChunk(chunk: ByteBuffer) {
        val queued = queuedChunks.getAndDecrement()
        try {
            if (encoderFailure != null || encodingFinished) return
            maximumQueuedChunks = max(maximumQueuedChunks, queued)
            copyAndEncode(chunk)
            val nowMs = SystemClock.elapsedRealtime()
            if (nowMs - lastFlushMs >= OUTPUT_FLUSH_INTERVAL_MS) {
                val committedFileSize = encoder?.flush().orZero()
                lastFlushMs = nowMs
                if (committedFileSize >= 0L) {
                    callback.onOutputCommitted(committedFileSize)
                }
            }
        } catch (error: Throwable) {
            failEncoder(error)
        } finally {
            recycleChunk(chunk)
        }
    }

    private fun failEncoder(error: Throwable) {
        if (encoderFailure != null) return
        Log.e("AudioCapture", "encoding failed", error)
        encoderFailure = error
        stopRequested = true
    }

    private fun copyAndEncode(chunk: ByteBuffer) {
        chunk.position(0)
        while (chunk.hasRemaining()) {
            val byteCount = min(chunk.remaining(), frameBuffer.remaining())
            val previousLimit = chunk.limit()
            chunk.limit(chunk.position() + byteCount)
            frameBuffer.put(chunk)
            chunk.limit(previousLimit)
            if (!frameBuffer.hasRemaining()) {
                encodeFrame(FRAME_BYTES)
                frameBuffer.clear()
            }
        }
    }

    private fun encodeFrame(byteCount: Int) {
        frameBuffer.position(0)
        frameBuffer.limit(byteCount)
        if (noiseSuppressor != null) {
            val startedAtNs = System.nanoTime()
            val processed = noiseSuppressor?.process(frameBuffer, byteCount / 2) == true
            val elapsedNs = System.nanoTime() - startedAtNs
            noiseProcessingCount++
            noiseProcessingTotalNs += elapsedNs
            noiseProcessingMaxNs = max(noiseProcessingMaxNs, elapsedNs)
            if (!processed) {
                Log.e("AudioCapture", "RNNoise processing failed; recording unprocessed PCM")
                noiseSuppressor?.recycle()
                noiseSuppressor = null
            } else if (noiseProcessingCount % 100L == 0L) {
                Log.e(
                    "AudioCapture", ("RNNoise processing: averageMs="
                            + nsToMs(noiseProcessingTotalNs / noiseProcessingCount)
                            + ", maxMs=" + nsToMs(noiseProcessingMaxNs))
                )
            }
            frameBuffer.position(0)
            frameBuffer.limit(byteCount)
        }
        check(
            !(encoder == null || encoder?.writeFrame(frameBuffer, byteCount) == false)
        ) { "Native Opus encoding failed" }
    }

    private fun finishEncoding(samples: Long, captureFailure: Throwable?) {
        if (encodingFinished) return
        encodingFinished = true
        var failure = if (encoderFailure != null) encoderFailure else captureFailure
        try {
            if (failure == null && encoder != null && frameBuffer.position() > 0) {
                encodeFrame(frameBuffer.position())
                frameBuffer.clear()
            }
        } catch (error: Throwable) {
            failure = error
        } finally {
            if (noiseSuppressor != null) {
                noiseSuppressor?.recycle()
                noiseSuppressor = null
            }
            if (encoder != null) {
                try {
                    if (encoder?.finish() != true && failure == null) {
                        failure = IllegalStateException("Unable to finalize native Opus recorder")
                    }
                } catch (error: Throwable) {
                    if (failure == null) failure = error
                }
                encoder?.recycle()
                encoder = null
            }
        }

        if (noiseProcessingCount > 0L) {
            Log.i(
                "AudioCapture", ("RNNoise summary: frames=" + noiseProcessingCount
                        + ", averageMs=" + nsToMs(noiseProcessingTotalNs / noiseProcessingCount)
                        + ", maxMs=" + nsToMs(noiseProcessingMaxNs))
            )
        }
        Log.i(
            "AudioCapture", ("capture stopped: samples=" + samples
                    + ", durationMs=" + samplesToMs(samples)
                    + ", maxQueuedChunks=" + maximumQueuedChunks)
        )
        if (failure == null) {
            callback.onStopped(cancelRequested)
        } else {
            callback.onError(failure)
        }
        encoderThread.quitSafely()
        chunkPool.clear()
    }

    private fun obtainChunk(): ByteBuffer {
        var chunk = chunkPool.poll()
        if (chunk == null) {
            chunk = ByteBuffer.allocateDirect(FRAME_BYTES).order(ByteOrder.nativeOrder())
        }
        chunk.clear()
        return chunk
    }

    private fun recycleChunk(chunk: ByteBuffer) {
        if (!encodingFinished) {
            chunkPool.offer(chunk)
        }
    }

    companion object {
        const val SAMPLE_RATE: Int = 48000
        const val FRAME_SAMPLES: Int = 960
        const val FRAME_BYTES: Int = FRAME_SAMPLES * 2

        private const val OUTPUT_FLUSH_INTERVAL_MS = 100L
        private const val PROGRESS_INTERVAL_MS = 32L

        internal fun samplesToMs(samples: Long): Long {
            return samples * 1000L / SAMPLE_RATE
        }

        internal fun nsToMs(durationNs: Long): Double {
            return durationNs / 1000000.0
        }
    }
}