package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.Includes;
import dev.ragnarok.fenrir.domain.impl.AccountsInteractor;
import dev.ragnarok.fenrir.domain.impl.AudioInteractor;
import dev.ragnarok.fenrir.domain.impl.BoardInteractor;
import dev.ragnarok.fenrir.domain.impl.CommunitiesInteractor;
import dev.ragnarok.fenrir.domain.impl.DatabaseInteractor;
import dev.ragnarok.fenrir.domain.impl.DialogsInteractor;
import dev.ragnarok.fenrir.domain.impl.DocsInteractor;
import dev.ragnarok.fenrir.domain.impl.FaveInteractor;
import dev.ragnarok.fenrir.domain.impl.FeedInteractor;
import dev.ragnarok.fenrir.domain.impl.FeedbackInteractor;
import dev.ragnarok.fenrir.domain.impl.GroupSettingsInteractor;
import dev.ragnarok.fenrir.domain.impl.LikesInteractor;
import dev.ragnarok.fenrir.domain.impl.LocalServerInteractor;
import dev.ragnarok.fenrir.domain.impl.NewsfeedInteractor;
import dev.ragnarok.fenrir.domain.impl.PhotosInteractor;
import dev.ragnarok.fenrir.domain.impl.PollInteractor;
import dev.ragnarok.fenrir.domain.impl.RelationshipInteractor;
import dev.ragnarok.fenrir.domain.impl.StickersInteractor;
import dev.ragnarok.fenrir.domain.impl.UtilsInteractor;
import dev.ragnarok.fenrir.domain.impl.VideosInteractor;
import dev.ragnarok.fenrir.settings.Settings;

public class InteractorFactory {

    public static INewsfeedInteractor createNewsfeedInteractor() {
        return new NewsfeedInteractor(Includes.getNetworkInterfaces(), Repository.INSTANCE.getOwners());
    }

    public static IStickersInteractor createStickersInteractor() {
        return new StickersInteractor(Includes.getNetworkInterfaces(), Includes.getStores().stickers());
    }

    public static IPollInteractor createPollInteractor() {
        return new PollInteractor(Includes.getNetworkInterfaces());
    }

    public static IDocsInteractor createDocsInteractor() {
        return new DocsInteractor(Includes.getNetworkInterfaces(), Includes.getStores().docs());
    }

    public static ILikesInteractor createLikesInteractor() {
        return new LikesInteractor(Includes.getNetworkInterfaces());
    }

    public static IFeedbackInteractor createFeedbackInteractor() {
        return new FeedbackInteractor(Includes.getStores(), Includes.getNetworkInterfaces(), Repository.INSTANCE.getOwners());
    }

    public static IDatabaseInteractor createDatabaseInteractor() {
        return new DatabaseInteractor(Includes.getStores().database(), Includes.getNetworkInterfaces());
    }

    public static ICommunitiesInteractor createCommunitiesInteractor() {
        return new CommunitiesInteractor(Includes.getNetworkInterfaces(), Includes.getStores());
    }

    public static IBoardInteractor createBoardInteractor() {
        return new BoardInteractor(Includes.getNetworkInterfaces(), Includes.getStores(), Repository.INSTANCE.getOwners());
    }

    public static IUtilsInteractor createUtilsInteractor() {
        return new UtilsInteractor(Includes.getNetworkInterfaces(), Includes.getStores(), Repository.INSTANCE.getOwners());
    }

    public static IRelationshipInteractor createRelationshipInteractor() {
        return new RelationshipInteractor(Includes.getStores(), Includes.getNetworkInterfaces());
    }

    public static IFeedInteractor createFeedInteractor() {
        return new FeedInteractor(Includes.getNetworkInterfaces(), Includes.getStores(), Settings.get().other(), Repository.INSTANCE.getOwners());
    }

    public static IGroupSettingsInteractor createGroupSettingsInteractor() {
        return new GroupSettingsInteractor(Includes.getNetworkInterfaces(), Includes.getStores().owners(), Repository.INSTANCE.getOwners());
    }

    public static IDialogsInteractor createDialogsInteractor() {
        return new DialogsInteractor(Includes.getNetworkInterfaces(), Includes.getStores());
    }

    public static IVideosInteractor createVideosInteractor() {
        return new VideosInteractor(Includes.getNetworkInterfaces(), Includes.getStores());
    }

    public static IAccountsInteractor createAccountInteractor() {
        return new AccountsInteractor(
                Includes.getNetworkInterfaces(),
                Includes.getSettings().accounts(),
                Includes.getBlacklistRepository(),
                Repository.INSTANCE.getOwners()
        );
    }

    public static IPhotosInteractor createPhotosInteractor() {
        return new PhotosInteractor(Includes.getNetworkInterfaces(), Includes.getStores());
    }

    public static IFaveInteractor createFaveInteractor() {
        return new FaveInteractor(Includes.getNetworkInterfaces(), Includes.getStores(), Repository.INSTANCE.getOwners());
    }

    public static ILocalServerInteractor createLocalServerInteractor() {
        return new LocalServerInteractor(Includes.getNetworkInterfaces());
    }

    public static IAudioInteractor createAudioInteractor() {
        return new AudioInteractor(Includes.getNetworkInterfaces());
    }
}
