package dev.ragnarok.fenrir.upload;

import android.net.Uri;

public class UploadIntent {

    private final int accountId;

    /* Идентификатор обьекта, к которому прикрепляется файл
       (локальный код сообщения, поста, комментария) */
    private final UploadDestination destination;

    /* Локальный путь к файлу */
    private Uri fileUri;

    /* Размер изображения (только для изображений)*/
    private int size;

    /**
     * Дополнительные данные
     */
    private Long fileId;

    private boolean autoCommit;

    public UploadIntent(int accountId, UploadDestination destination) {
        this.accountId = accountId;
        this.destination = destination;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public UploadIntent setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    public UploadDestination getDestination() {
        return destination;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public UploadIntent setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
        return this;
    }

    public int getSize() {
        return size;
    }

    public UploadIntent setSize(int size) {
        this.size = size;
        return this;
    }

    public Long getFileId() {
        return fileId;
    }

    public UploadIntent setFileId(Long fileId) {
        this.fileId = fileId;
        return this;
    }
}
