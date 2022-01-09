package dev.ragnarok.fenrir.media.record;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;

import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Logger;

public class AudioRecordWrapper {

    private static final String TEMP_FILE_NAME = "temp_recording";
    private static final String TAG = AudioRecordWrapper.class.getSimpleName();

    private final Context mContext;
    private final String mFileExt;
    private Recorder mRecorder;

    private AudioRecordWrapper(@NonNull Builder builder) {
        mContext = builder.mContext;
        mFileExt = builder.mFileExt;
    }

    public static File getRecordingDirectory(Context context) {
        return context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES);
    }

    public void doRecord() throws AudioRecordException {
        if (mRecorder == null) {
            File file = getTmpRecordFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    throw new AudioRecordException(AudioRecordException.Codes.UNABLE_TO_REMOVE_TMP_FILE);
                }
            }

            mRecorder = new Recorder(file.getAbsolutePath());

            try {
                mRecorder.prepare();
            } catch (IOException e) {
                mRecorder = null;
                throw new AudioRecordException(AudioRecordException.Codes.UNABLE_TO_PREPARE_RECORDER);
            }
        }

        if (mRecorder.getStatus() == Recorder.Status.RECORDING_NOW) {
            if (Recorder.isPauseSupported()) {
                mRecorder.pause();
            } else {
                mRecorder.stopAndRelease();
                mRecorder = null;
            }
        } else {
            try {
                mRecorder.start();
            } catch (IllegalStateException e) {
                CustomToast.CreateCustomToast(mContext).showToastError(e.getLocalizedMessage());
            }
        }
    }

    public boolean isPauseSupported() {
        return Recorder.isPauseSupported();
    }

    public long getCurrentRecordDuration() {
        return mRecorder == null ? 0 : mRecorder.getCurrentRecordDuration();
    }

    public int getCurrentMaxAmplitude() {
        return mRecorder == null ? 0 : mRecorder.getMaxAmplitude();
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public void pause() {
        if (mRecorder == null) {
            Logger.wtf(TAG, "Recorder in NULL");
            return;
        }

        if (mRecorder.getStatus() == Recorder.Status.RECORDING_NOW) {
            try {
                mRecorder.pause();
            } catch (IllegalStateException e) {
                CustomToast.CreateCustomToast(mContext).showToastError(e.getLocalizedMessage());
            }
        } else {
            Logger.wtf(TAG, "Recorder status is not RECORDING_NOW");
        }
    }

    public void stopRecording() {
        if (mRecorder == null) {
            throw new IllegalStateException("Recorder in NULL");
        }
        try {
            mRecorder.stopAndRelease();
        } catch (IllegalStateException e) {
            CustomToast.CreateCustomToast(mContext).showToastError(e.getLocalizedMessage());
        }
        mRecorder = null;
    }

    public File stopRecordingAndReceiveFile() throws AudioRecordException {
        if (mRecorder == null) {
            throw new IllegalStateException("Recorder in NULL");
        }

        int status = mRecorder.getStatus();
        if (status == Recorder.Status.RECORDING_NOW || status == Recorder.Status.PAUSED) {
            String filePath = mRecorder.getFilePath();

            mRecorder.stopAndRelease();
            mRecorder = null;

            long currentTime = System.currentTimeMillis();
            String destFileName = "record_" + currentTime + "." + mFileExt;

            File destFile = new File(getRecordingDirectory(mContext), destFileName);

            File file = new File(filePath);
            boolean renamed = file.renameTo(destFile);
            if (!renamed) {
                throw new AudioRecordException(AudioRecordException.Codes.UNABLE_TO_RENAME_TMP_FILE);
            }

            return destFile;
        } else {
            throw new AudioRecordException(AudioRecordException.Codes.INVALID_RECORDER_STATUS);
        }
    }

    public int getRecorderStatus() {
        return mRecorder == null ? Recorder.Status.NO_RECORD : mRecorder.getStatus();
    }

    public File getTmpRecordFile() {
        return new File(getRecordingDirectory(mContext), TEMP_FILE_NAME + "." + mFileExt);
    }

    public static final class Builder {

        private final Context mContext;
        private final String mFileExt = Recorder.isOpusSupported() ? "ogg" : "mp3";

        public Builder(Context context) {
            mContext = context;
        }

        public AudioRecordWrapper build() {
            return new AudioRecordWrapper(this);
        }
    }
}
