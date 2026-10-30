package dev.ragnarok.fenrir.db.impl

import android.content.Context
import android.content.ContextWrapper
import dev.ragnarok.fenrir.db.interfaces.IAttachmentsStorage
import dev.ragnarok.fenrir.db.interfaces.ICommentsStorage
import dev.ragnarok.fenrir.db.interfaces.IDatabaseStore
import dev.ragnarok.fenrir.db.interfaces.IDialogsStorage
import dev.ragnarok.fenrir.db.interfaces.IDocsStorage
import dev.ragnarok.fenrir.db.interfaces.IFaveStorage
import dev.ragnarok.fenrir.db.interfaces.IFeedStorage
import dev.ragnarok.fenrir.db.interfaces.IFeedbackStorage
import dev.ragnarok.fenrir.db.interfaces.ILocalMediaStorage
import dev.ragnarok.fenrir.db.interfaces.IMessagesStorage
import dev.ragnarok.fenrir.db.interfaces.IOwnersStorage
import dev.ragnarok.fenrir.db.interfaces.IPhotoAlbumsStorage
import dev.ragnarok.fenrir.db.interfaces.IPhotosStorage
import dev.ragnarok.fenrir.db.interfaces.IRelativeshipStorage
import dev.ragnarok.fenrir.db.interfaces.IStickersStorage
import dev.ragnarok.fenrir.db.interfaces.IStorages
import dev.ragnarok.fenrir.db.interfaces.ITempDataStorage
import dev.ragnarok.fenrir.db.interfaces.ITopicsStore
import dev.ragnarok.fenrir.db.interfaces.IVideoAlbumsStorage
import dev.ragnarok.fenrir.db.interfaces.IVideoStorage
import dev.ragnarok.fenrir.db.interfaces.IWallStorage

class AppStorages(base: Context) : ContextWrapper(base), IStorages {
    private val _tempData: ITempDataStorage by lazy {
        TempDataStorage(this)
    }
    private val _owners: IOwnersStorage by lazy {
        OwnersStorage(this)
    }
    private val _feed: IFeedStorage by lazy {
        FeedStorage(this)
    }
    private val _relativeShip: IRelativeshipStorage by lazy {
        RelativeshipStorage(this)
    }
    private val _photos: IPhotosStorage by lazy {
        PhotosStorage(this)
    }
    private val _fave: IFaveStorage by lazy {
        FaveStorage(this)
    }
    private val _wall: IWallStorage by lazy {
        WallStorage(this)
    }
    private val _messages: IMessagesStorage by lazy {
        MessagesStorage(this)
    }
    private val _dialogs: IDialogsStorage by lazy {
        DialogsStorage(this)
    }
    private val _feedback: IFeedbackStorage by lazy {
        FeedbackStorage(this)
    }
    private val _localMedia: ILocalMediaStorage by lazy {
        LocalMediaStorage(this)
    }
    private val _attachments: IAttachmentsStorage by lazy {
        AttachmentsStorage(this)
    }

    private val _video: IVideoStorage by lazy {
        VideoStorage(this)
    }

    private val _videoAlbums: IVideoAlbumsStorage by lazy {
        VideoAlbumsStorage(this)
    }

    private val _comments: ICommentsStorage by lazy {
        CommentsStorage(this)
    }

    private val _photoAlbums: IPhotoAlbumsStorage by lazy {
        PhotoAlbumsStorage(this)
    }

    private val _topics: ITopicsStore by lazy {
        TopicsStorage(this)
    }

    private val _docs: IDocsStorage by lazy {
        DocsStorage(this)
    }

    private val _stickers: IStickersStorage by lazy {
        StickersStorage(this)
    }
    private val _database: IDatabaseStore by lazy {
        DatabaseStorage(this)
    }


    override fun comments(): ICommentsStorage {
        return _comments
    }

    override fun photoAlbums(): IPhotoAlbumsStorage {
        return _photoAlbums
    }

    override fun topics(): ITopicsStore {
        return _topics
    }

    override fun docs(): IDocsStorage {
        return _docs
    }

    override fun stickers(): IStickersStorage {
        return _stickers
    }

    override fun database(): IDatabaseStore {
        return _database
    }

    override fun tempStore(): ITempDataStorage {
        return _tempData
    }

    override fun videoAlbums(): IVideoAlbumsStorage {
        return _videoAlbums
    }

    override fun videos(): IVideoStorage {
        return _video
    }

    override fun attachments(): IAttachmentsStorage {
        return _attachments
    }

    override fun localMedia(): ILocalMediaStorage {
        return _localMedia
    }

    override fun notifications(): IFeedbackStorage {
        return _feedback
    }

    override fun dialogs(): IDialogsStorage {
        return _dialogs
    }

    override fun messages(): IMessagesStorage {
        return _messages
    }

    override fun wall(): IWallStorage {
        return _wall
    }

    override fun fave(): IFaveStorage {
        return _fave
    }

    override fun photos(): IPhotosStorage {
        return _photos
    }

    override fun relativeship(): IRelativeshipStorage {
        return _relativeShip
    }

    override fun feed(): IFeedStorage {
        return _feed
    }

    override fun owners(): IOwnersStorage {
        return _owners
    }
}