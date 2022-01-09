package dev.ragnarok.fenrir.media.record;

public class AudioRecordException extends Exception {

    private final int code;

    public AudioRecordException(int code) {
        this.code = code;
    }

    public AudioRecordException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static final class Codes {
        public static final int UNABLE_TO_REMOVE_TMP_FILE = 10;
        public static final int UNABLE_TO_RENAME_TMP_FILE = 12;
        public static final int UNABLE_TO_PREPARE_RECORDER = 11;
        public static final int INVALID_RECORDER_STATUS = 13;
    }
}
