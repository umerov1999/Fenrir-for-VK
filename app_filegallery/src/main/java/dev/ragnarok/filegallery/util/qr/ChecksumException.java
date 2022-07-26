package dev.ragnarok.filegallery.util.qr;

/**
 * Thrown when a barcode was successfully detected and decoded, but
 * was not returned because its checksum feature failed.
 *
 * @author Sean Owen
 */
public final class ChecksumException extends ReaderException {

    private static final ChecksumException INSTANCE = new ChecksumException();

    static {
        INSTANCE.setStackTrace(NO_TRACE); // since it's meaningless
    }

    private ChecksumException() {
        // do nothing
    }

    private ChecksumException(Throwable cause) {
        super(cause);
    }

    public static ChecksumException getChecksumInstance() {
        return isStackTrace ? new ChecksumException() : INSTANCE;
    }

    public static ChecksumException getChecksumInstance(Throwable cause) {
        return isStackTrace ? new ChecksumException(cause) : INSTANCE;
    }
}