/*
 * @author : Paul Taylor
 * <p>
 * Version @version:$Id$ Date :${DATE}
 * <p>
 * Jaikoz Copyright Copyright (C) 2003 -2005 JThink Ltd
 */
package ealvatag.audio.exceptions;

import java.util.Locale;

/**
 * Thrown if portion of file thought to be an AudioFrame is found to not be.
 */
public class InvalidAudioFrameException extends Exception {
    public InvalidAudioFrameException(String message) {
        super(message);
    }

    public InvalidAudioFrameException(String message, Object... formatArgs) {
        super(String.format(Locale.getDefault(), message, formatArgs));
    }

}
