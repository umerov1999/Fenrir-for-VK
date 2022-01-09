package dev.ragnarok.fenrir.crypt;

public class InvalidSessionStateException extends Exception {

    public InvalidSessionStateException() {
    }

    public InvalidSessionStateException(String message) {
        super(message);
    }
}
