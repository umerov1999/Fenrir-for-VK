package ealvatag.audio.exceptions;

import ealvatag.BaseException;

/**
 * Thrown if when trying to read box id the length doesn't make any sense
 */
public class InvalidBoxHeaderException extends BaseException {
    public InvalidBoxHeaderException(String id, int length) {
        super("Unable to find next atom because identifier is invalid %s, length:%d", id, length);
    }
}
