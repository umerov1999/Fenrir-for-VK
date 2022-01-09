/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.picasso3;

import static com.squareup.picasso3.BitmapUtils.decodeStream;
import static com.squareup.picasso3.Picasso.LoadedFrom.DISK;
import static com.squareup.picasso3.Picasso.LoadedFrom.NETWORK;
import static com.squareup.picasso3.Utils.checkNotNull;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class NetworkRequestHandler extends RequestHandler {
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";

    private final Call.Factory callFactory;

    NetworkRequestHandler(Call.Factory callFactory) {
        this.callFactory = callFactory;
    }

    private static okhttp3.Request createRequest(Request request) {
        CacheControl cacheControl = null;
        int networkPolicy = request.networkPolicy;
        if (networkPolicy != 0) {
            if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
                cacheControl = CacheControl.FORCE_CACHE;
            } else {
                CacheControl.Builder builder = new CacheControl.Builder();
                if (!NetworkPolicy.shouldReadFromDiskCache(networkPolicy)) {
                    builder.noCache();
                }
                if (!NetworkPolicy.shouldWriteToDiskCache(networkPolicy)) {
                    builder.noStore();
                }
                cacheControl = builder.build();
            }
        }

        Uri uri = checkNotNull(request.uri, "request.uri == null");
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(uri.toString());
        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }
        return builder.build();
    }

    @Override
    public boolean canHandleRequest(@NonNull Request data) {
        Uri uri = data.uri;
        if (uri == null) return false;

        String scheme = uri.getScheme();
        return (SCHEME_HTTP.equalsIgnoreCase(scheme) || SCHEME_HTTPS.equalsIgnoreCase(scheme));
    }

    @Override
    public void load(@NonNull Picasso picasso, @NonNull Request request,
                     @NonNull Callback callback) {
        okhttp3.Request callRequest = createRequest(request);
        callFactory.newCall(callRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    callback.onError(new ResponseException(response.code(), request.networkPolicy));
                    return;
                }

                // Cache response is only null when the response comes fully from the network. Both
                // completely cached and conditionally cached responses will have a non-null cache response.
                Picasso.LoadedFrom loadedFrom = response.cacheResponse() == null ? NETWORK : DISK;

                // Sometimes response content length is zero when requests are being replayed. Haven't found
                // root cause to this but retrying the request seems safe to do so.
                ResponseBody body = response.body();
                if (loadedFrom == DISK && body.contentLength() == 0) {
                    body.close();
                    callback.onError(
                            new ContentLengthException("Received response with 0 content-length header."));
                    return;
                }
                if (loadedFrom == NETWORK && body.contentLength() > 0) {
                    picasso.downloadFinished(body.contentLength());
                }
                try {
                    Bitmap bitmap = decodeStream(body.source(), request);
                    callback.onSuccess(new Result.Bitmap(bitmap, loadedFrom));
                } catch (IOException e) {
                    body.close();
                    callback.onError(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }
        });
    }

    @Override
    public int getRetryCount() {
        return 2;
    }

    @Override
    public boolean shouldRetry(boolean networkAvailable) {
        return networkAvailable;
    }

    @Override
    public boolean supportsReplay() {
        return true;
    }

    static class ContentLengthException extends RuntimeException {
        ContentLengthException(String message) {
            super(message);
        }
    }

    static final class ResponseException extends RuntimeException {
        final int code;
        final int networkPolicy;

        ResponseException(int code, int networkPolicy) {
            super("Server error: HTTP " + code);
            this.code = code;
            this.networkPolicy = networkPolicy;
        }
    }
}
