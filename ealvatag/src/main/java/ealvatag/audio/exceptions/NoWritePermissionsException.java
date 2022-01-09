package ealvatag.audio.exceptions;

/**
 * Use this exception instead of the more general CannotWriteException if unable to write file because of a permissions
 * problem
 */
public class NoWritePermissionsException extends CannotWriteException {

    public NoWritePermissionsException(Throwable ex) {
        super(ex);
    }

    public NoWritePermissionsException(Throwable cause, String message) {
        super(cause, message);
    }

    public NoWritePermissionsException(Throwable cause, String message, Object... formatArgs) {
        super(cause, message, formatArgs);
    }

}
