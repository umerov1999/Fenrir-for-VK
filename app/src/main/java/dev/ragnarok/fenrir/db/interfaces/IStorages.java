package dev.ragnarok.fenrir.db.interfaces;

import dev.ragnarok.fenrir.crypt.KeyLocationPolicy;

public interface IStorages {

    ITempDataStorage tempStore();

    ISearchRequestHelperStorage searchQueriesStore();

    IVideoAlbumsStorage videoAlbums();

    IVideoStorage videos();

    IAttachmentsStorage attachments();

    IKeysStorage keys(@KeyLocationPolicy int policy);

    ILocalMediaStorage localMedia();

    IFeedbackStorage notifications();

    IDialogsStorage dialogs();

    IMessagesStorage messages();

    IWallStorage wall();

    IFaveStorage fave();

    IPhotosStorage photos();

    IRelativeshipStorage relativeship();

    IFeedStorage feed();

    IOwnersStorage owners();

    ICommentsStorage comments();

    IPhotoAlbumsStorage photoAlbums();

    ITopicsStore topics();

    IDocsStorage docs();

    IStickersStorage stickers();

    IDatabaseStore database();
}