package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.NonNull;

import java.io.InputStream;

import dev.ragnarok.fenrir.api.PercentagePublisher;
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
import io.reactivex.rxjava3.core.Single;

public interface IUploadApi {
    Single<UploadDocDto> uploadDocumentRx(String server, String filename, @NonNull InputStream doc, PercentagePublisher listener);

    Single<UploadAudioDto> uploadAudioRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener);

    Single<BaseResponse<Integer>> remotePlayAudioRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener);

    Single<BaseResponse<UploadStoryDto>> uploadStoryRx(String server, String filename, @NonNull InputStream is, PercentagePublisher listener, boolean isVideo);

    Single<UploadVideoDto> uploadVideoRx(String server, String filename, @NonNull InputStream video, PercentagePublisher listener);

    Single<UploadOwnerPhotoDto> uploadOwnerPhotoRx(String server, @NonNull InputStream photo, PercentagePublisher listener);

    Single<UploadChatPhotoDto> uploadChatPhotoRx(String server, @NonNull InputStream photo, PercentagePublisher listener);

    Single<UploadPhotoToWallDto> uploadPhotoToWallRx(String server, @NonNull InputStream photo, PercentagePublisher listener);

    Single<UploadPhotoToMessageDto> uploadPhotoToMessageRx(String server, @NonNull InputStream is, PercentagePublisher listener);

    Single<UploadPhotoToAlbumDto> uploadPhotoToAlbumRx(String server, @NonNull InputStream file1, PercentagePublisher listener);
}