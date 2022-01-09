package dev.ragnarok.fenrir.api.util;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {

    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private final InputStream stream;
    private final UploadCallbacks listener;
    private final MediaType mediaType;

    public ProgressRequestBody(InputStream file, UploadCallbacks listener, MediaType mediaType) {
        stream = file;
        this.listener = listener;
        this.mediaType = mediaType;
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public long contentLength() throws IOException {
        return stream.available();
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength = stream.available();

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long uploaded = 0;

        try {
            int read;
            while ((read = stream.read(buffer)) != -1) {
                if (listener != null) {
                    listener.onProgressUpdate((int) (100 * uploaded / fileLength));
                }

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw e;
            } else {
                throw new IOException(e);
            }
        } finally {
            stream.close();
        }
    }

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
    }
}