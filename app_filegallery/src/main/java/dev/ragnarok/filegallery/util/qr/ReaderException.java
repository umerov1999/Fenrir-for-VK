package dev.ragnarok.filegallery.util.qr;

import androidx.annotation.NonNull;

/**
 * The general exception class throw when something goes wrong during decoding of a barcode.
 * This includes, but is not limited to, failing checksums / error correction algorithms, being
 * unable to locate finder timing patterns, and so on.
 *
 * @author Sean Owen
 */
public abstract class ReaderException extends Exception {

    protected static final StackTraceElement[] NO_TRACE = new StackTraceElement[0];
    // disable stack traces when not running inside test units
    protected static boolean isStackTrace =
            System.getProperty("surefire.test.class.path") != null;

    ReaderException() {
        // do nothing
    }

    ReaderException(Throwable cause) {
        super(cause);
    }

    /**
     * For testing only. Controls whether library exception classes include stack traces or not.
     * Defaults to false, unless running in the project's unit testing harness.
     *
     * @param enabled if true, enables stack traces in library exception classes
     * @since 3.5.0
     */
    public static void setStackTrace(boolean enabled) {
        isStackTrace = enabled;
    }

    // Prevent stack traces from being taken
    @NonNull
    @Override
    public final synchronized Throwable fillInStackTrace() {
        return new Throwable();
    }

}
