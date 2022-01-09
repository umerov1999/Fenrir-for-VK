package ealvatag.audio.exceptions;

import java.io.IOException;
import java.util.Locale;

/**
 * Should be thrown when unable to modify a file when it is expected it should be modifiable. For example because
 * you dont have permission to modify files in the folder that it is in.
 */
public class UnableToModifyFileException extends IOException {
    public UnableToModifyFileException(String message) {
        super(message);
    }

    public UnableToModifyFileException(String message, Object... formatArgs) {
        super(String.format(Locale.getDefault(), message, formatArgs));
    }

}
