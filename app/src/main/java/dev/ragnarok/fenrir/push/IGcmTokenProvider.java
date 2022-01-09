package dev.ragnarok.fenrir.push;

import java.io.IOException;

public interface IGcmTokenProvider {
    String getToken() throws IOException;
}