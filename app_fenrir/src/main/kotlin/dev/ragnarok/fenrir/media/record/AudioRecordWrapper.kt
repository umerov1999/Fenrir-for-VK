package dev.ragnarok.fenrir.media.record

import android.content.Context
import android.os.Environment
import dev.ragnarok.fenrir.orZero
import java.io.File

class AudioRecordWrapper(
    context: Context,
    val onReceiveFile: (file: File) -> Unit,
    val onErrorRecord: (throwable: Throwable) -> Unit,
    val onStartRecord: () -> Unit,
    val onPauseResume: (isPaused: Boolean) -> Unit,
    val onCancelled: () -> Unit,
) {
    private val tempRecordFile = File(getRecordingDirectory(context), "$TEMP_FILE_NAME.ogg")
    private var mRecorder: AudioCapture? = null

    @Volatile
    var isRecordingProcess = false
        private set

    @Volatile
    var isPaused = false
        private set


    fun doRecord() {
        if (mRecorder == null) {
            val file = tempRecordFile
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) {
                    onErrorRecord(IllegalStateException("Couldn't delete old temporary recorded audio"))
                    return
                }
            }
            mRecorder = AudioCapture(file.absolutePath, object : AudioCapture.Callback {
                override fun onStarted() {
                    isRecordingProcess = true
                    onStartRecord()
                }

                override fun onProgress(capturedSamples: Long, amplitude: Double) {}

                override fun onOutputCommitted(committedFileSize: Long) {}

                override fun onStopped(cancel: Boolean) {
                    isRecordingProcess = false
                    isPaused = false
                    if (cancel) {
                        val file = tempRecordFile
                        if (file.exists()) {
                            file.delete()
                        }
                        onCancelled()
                    } else {
                        onReceiveFile(file)
                    }
                    mRecorder = null
                }

                override fun onPaused() {
                    isPaused = true
                    onPauseResume(true)
                }

                override fun onResumed() {
                    isPaused = false
                    onPauseResume(false)
                }

                override fun onError(error: Throwable) {
                    isRecordingProcess = false
                    isPaused = false
                    val file = tempRecordFile
                    if (file.exists()) {
                        file.delete()
                    }
                    onErrorRecord(error)
                    mRecorder = null
                }

            })
        }
        if (!isRecordingProcess) {
            mRecorder?.start()
        }
    }

    val currentRecordDuration: Long
        get() = mRecorder?.getRecordingDurationMs().orZero()

    fun pauseResume() {
        if (!isPaused) {
            mRecorder?.pause()
        } else {
            mRecorder?.resume()
        }
    }

    fun cancelRecording() {
        mRecorder?.cancel()
    }

    fun stopAndCommit() {
        mRecorder?.stop()
    }

    companion object {
        private const val TEMP_FILE_NAME = "temp_recording"
        fun getRecordingDirectory(context: Context): File? {
            return context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES)
        }
    }

}