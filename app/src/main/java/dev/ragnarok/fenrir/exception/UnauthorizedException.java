package dev.ragnarok.fenrir.exception;

import java.io.IOException;

public class UnauthorizedException extends IOException {

    public UnauthorizedException(String message) {
        super(message);
    }
}