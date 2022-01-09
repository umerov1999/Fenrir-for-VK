package dev.ragnarok.fenrir.api.impl;

import androidx.annotation.NonNull;

import java.io.InputStream;

import dev.ragnarok.fenrir.api.IUploadRetrofitProvider;
import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.IUploadApi;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.upload.UploadAudioDto;
import dev.ragnarok.fenrir.api.model.upload.UploadChatPhotoDto;
import dev.ragnarok.fenrir.api.model.upload.UploadDocDto;
import dev.ragnarok.fenrir.api.model.upload.UploadOwnerPhotoDto;
import dev.ragnarok.fenrir.api.model.upload.UploadPhotoToAlbumDto;
import dev.ragnarok.fenrir.api.model.upload.UploadPhotoToMessageDto;
import dev.ragnarok.fenrir.api.model.upload.UploadPhotoToWallDto;
import dev.ragnarok.fenrir.api.model.upload.UploadStoryDto;
import dev.ragnarok.fenrir.api.model.upload.UploadVideoDto;
import dev.ragnarok.fenrir.api.services.IUploadService;
import dev.ragnarok.fenrir.api.util.ProgressRequestBody;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MediaType;
import okhttp3.MultipartBody;


public class UploadApi implements IUploadApi {

    private final IUploadRetrofitProvider provider;

    UploadApi(IUploadRetrofitProvider provider) {
        this.provider = provider;
    }

    private static ProgressRequestBody.UploadCallbacks wrapPercentageListener(PercentagePublisher listener) {
        return percentage -> {
            if (Objects.nonNull(listener)) {
                listener.onProgressChanged(percentage);
            }
        };
    }

    private IUploadService service() {
        return provider.provideUploadRetrofit().blockingGet().create(IUploadService.class);
    }

    @Override
    public Single<UploadDocDto> uploadDocumentRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("*/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, body);
        return service().uploadDocumentRx(server, part);
    }

    @Override
    public Single<UploadAudioDto> uploadAudioRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("*/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, body);
        return service().uploadAudioRx(server, part);
    }

    @Override
    public Single<BaseResponse<Integer>> remotePlayAudioRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("*/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("audio", filename, body);
        return service().remotePlayAudioRx(server, part);
    }

    @Override
    public Single<BaseResponse<UploadStoryDto>> uploadStoryRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener, boolean isVideo) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("*/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData(!isVideo ? "photo" : "video_file", filename, body);
        return service().uploadStoryRx(server, part);
    }

    @Override
    public Single<UploadVideoDto> uploadVideoRx(String server, String filename, @NonNull InputStream video, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(video, wrapPercentageListener(listener), MediaType.parse("*/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", filename, body);
        return service().uploadVideoRx(server, part);
    }

    @Override
    public Single<UploadOwnerPhotoDto> uploadOwnerPhotoRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "photo.jpg", body);
        return service().uploadOwnerPhotoRx(server, part);
    }

    @Override
    public Single<UploadChatPhotoDto> uploadChatPhotoRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "photo.jpg", body);
        return service().uploadChatPhotoRx(server, part);
    }

    @Override
    public Single<UploadPhotoToWallDto> uploadPhotoToWallRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "photo.jpg", body);
        return service().uploadPhotoToWallRx(server, part);
    }

    @Override
    public Single<UploadPhotoToMessageDto> uploadPhotoToMessageRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("photo", "photo.jpg", body);
        return service().uploadPhotoToMessageRx(server, part);
    }

    @Override
    public Single<UploadPhotoToAlbumDto> uploadPhotoToAlbumRx(String server, @NonNull InputStream is, PercentagePublisher listener) {
        ProgressRequestBody body = new ProgressRequestBody(is, wrapPercentageListener(listener), MediaType.parse("image/*"));
        MultipartBody.Part part = MultipartBody.Part.createFormData("file1", "photo.jpg", body);
        return service().uploadPhotoToAlbumRx(server, part);
    }
}