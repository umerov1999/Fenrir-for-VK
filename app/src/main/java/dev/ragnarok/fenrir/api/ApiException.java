package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.api.model.Error;


public class ApiException extends Exception {

    private final Error error;

    public ApiException(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
