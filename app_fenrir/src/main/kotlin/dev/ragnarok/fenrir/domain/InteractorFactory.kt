package dev.ragnarok.fenrir.domain

import dev.ragnarok.fenrir.Includes.blacklistRepository
import dev.ragnarok.fenrir.Includes.networkInterfaces
import dev.ragnarok.fenrir.Includes.settings
import dev.ragnarok.fenrir.Includes.stores
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.domain.impl.AccountsInteractor
import dev.ragnarok.fenrir.domain.impl.AudioInteractor
import dev.ragnarok.fenrir.domain.impl.BoardInteractor
import dev.ragnarok.fenrir.domain.impl.CommunitiesInteractor
import dev.ragnarok.fenrir.domain.impl.DatabaseInteractor
import dev.ragnarok.fenrir.domain.impl.DocsInteractor
import dev.ragnarok.fenrir.domain.impl.FaveInteractor
import dev.ragnarok.fenrir.domain.impl.FeedInteractor
import dev.ragnarok.fenrir.domain.impl.FeedbackInteractor
import dev.ragnarok.fenrir.domain.impl.GroupSettingsInteractor
import dev.ragnarok.fenrir.domain.impl.LikesInteractor
import dev.ragnarok.fenrir.domain.impl.LocalServerInteractor
import dev.ragnarok.fenrir.domain.impl.PhotosInteractor
import dev.ragnarok.fenrir.domain.impl.PollInteractor
import dev.ragnarok.fenrir.domain.impl.RelationshipInteractor
import dev.ragnarok.fenrir.domain.impl.StickersInteractor
import dev.ragnarok.fenrir.domain.impl.StoriesShortVideosInteractor
import dev.ragnarok.fenrir.domain.impl.UtilsInteractor
import dev.ragnarok.fenrir.domain.impl.VideosInteractor
import dev.ragnarok.fenrir.domain.interfaces.IAccountsInteractor
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.domain.interfaces.IBoardInteractor
import dev.ragnarok.fenrir.domain.interfaces.ICommunitiesInteractor
import dev.ragnarok.fenrir.domain.interfaces.IDatabaseInteractor
import dev.ragnarok.fenrir.domain.interfaces.IDocsInteractor
import dev.ragnarok.fenrir.domain.interfaces.IFaveInteractor
import dev.ragnarok.fenrir.domain.interfaces.IFeedInteractor
import dev.ragnarok.fenrir.domain.interfaces.IFeedbackInteractor
import dev.ragnarok.fenrir.domain.interfaces.IGroupSettingsInteractor
import dev.ragnarok.fenrir.domain.interfaces.ILikesInteractor
import dev.ragnarok.fenrir.domain.interfaces.ILocalServerInteractor
import dev.ragnarok.fenrir.domain.interfaces.IPhotosInteractor
import dev.ragnarok.fenrir.domain.interfaces.IPollInteractor
import dev.ragnarok.fenrir.domain.interfaces.IRelationshipInteractor
import dev.ragnarok.fenrir.domain.interfaces.IStickersInteractor
import dev.ragnarok.fenrir.domain.interfaces.IStoriesShortVideosInteractor
import dev.ragnarok.fenrir.domain.interfaces.IUtilsInteractor
import dev.ragnarok.fenrir.domain.interfaces.IVideosInteractor
import dev.ragnarok.fenrir.settings.Settings

object InteractorFactory {
    val sticker: IStickersInteractor by lazy {
        StickersInteractor(networkInterfaces, stores.stickers())
    }

    val poll: IPollInteractor by lazy {
        PollInteractor(networkInterfaces)
    }

    val doc: IDocsInteractor by lazy {
        DocsInteractor(networkInterfaces, stores.docs())
    }

    val like: ILikesInteractor by lazy {
        LikesInteractor(networkInterfaces)
    }

    val feedback: IFeedbackInteractor by lazy {
        FeedbackInteractor(stores, networkInterfaces, owners)
    }

    val database: IDatabaseInteractor by lazy {
        DatabaseInteractor(stores.database(), networkInterfaces)
    }

    val community: ICommunitiesInteractor by lazy {
        CommunitiesInteractor(networkInterfaces, stores)
    }

    val board: IBoardInteractor by lazy {
        BoardInteractor(networkInterfaces, stores, owners)
    }

    val utils: IUtilsInteractor by lazy {
        UtilsInteractor(networkInterfaces, stores, owners)
    }

    val relationship: IRelationshipInteractor by lazy {
        RelationshipInteractor(stores, networkInterfaces)
    }

    val feed: IFeedInteractor by lazy {
        FeedInteractor(networkInterfaces, stores, Settings.get().main(), owners)
    }

    val groupSettings: IGroupSettingsInteractor by lazy {
        GroupSettingsInteractor(networkInterfaces, stores.owners(), owners)
    }

    val video: IVideosInteractor by lazy {
        VideosInteractor(networkInterfaces, stores)
    }

    val account: IAccountsInteractor by lazy {
        AccountsInteractor(
            networkInterfaces,
            settings.accounts(),
            blacklistRepository,
            owners
        )
    }

    val photo: IPhotosInteractor by lazy {
        PhotosInteractor(networkInterfaces, stores)
    }

    val fave: IFaveInteractor by lazy {
        FaveInteractor(networkInterfaces, stores, owners)
    }

    val localServer: ILocalServerInteractor by lazy {
        LocalServerInteractor(networkInterfaces)
    }

    val audio: IAudioInteractor by lazy {
        AudioInteractor(networkInterfaces)
    }

    val story: IStoriesShortVideosInteractor by lazy {
        StoriesShortVideosInteractor(networkInterfaces, owners)
    }
}