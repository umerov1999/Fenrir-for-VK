package dev.ragnarok.filegallery.util.qr;

/**
 * Thrown when a barcode was not found in the image. It might have been
 * partially detected but could not be confirmed.
 *
 * @author Sean Owen
 */
public final class NotFoundException extends ReaderException {

    private static final NotFoundException INSTANCE = new NotFoundException();

    static {
        INSTANCE.setStackTrace(NO_TRACE); // since it's meaningless
    }

    private NotFoundException() {
        // do nothing
    }

    public static NotFoundException getNotFoundInstance() {
        return isStackTrace ? new NotFoundException() : INSTANCE;
    }

}