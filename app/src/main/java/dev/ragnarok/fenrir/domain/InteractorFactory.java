package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.Injection;
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
        return new NewsfeedInteractor(Injection.provideNetworkInterfaces(), Repository.INSTANCE.getOwners());
    }

    public static IStickersInteractor createStickersInteractor() {
        return new StickersInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().stickers());
    }

    public static IPollInteractor createPollInteractor() {
        return new PollInteractor(Injection.provideNetworkInterfaces());
    }

    public static IDocsInteractor createDocsInteractor() {
        return new DocsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().docs());
    }

    public static ILikesInteractor createLikesInteractor() {
        return new LikesInteractor(Injection.provideNetworkInterfaces());
    }

    public static IFeedbackInteractor createFeedbackInteractor() {
        return new FeedbackInteractor(Injection.provideStores(), Injection.provideNetworkInterfaces(), Repository.INSTANCE.getOwners());
    }

    public static IDatabaseInteractor createDatabaseInteractor() {
        return new DatabaseInteractor(Injection.provideStores().database(), Injection.provideNetworkInterfaces());
    }

    public static ICommunitiesInteractor createCommunitiesInteractor() {
        return new CommunitiesInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IBoardInteractor createBoardInteractor() {
        return new BoardInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
    }

    public static IUtilsInteractor createUtilsInteractor() {
        return new UtilsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
    }

    public static IRelationshipInteractor createRelationshipInteractor() {
        return new RelationshipInteractor(Injection.provideStores(), Injection.provideNetworkInterfaces());
    }

    public static IFeedInteractor createFeedInteractor() {
        return new FeedInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Settings.get().other(), Repository.INSTANCE.getOwners());
    }

    public static IGroupSettingsInteractor createGroupSettingsInteractor() {
        return new GroupSettingsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().owners(), Repository.INSTANCE.getOwners());
    }

    public static IDialogsInteractor createDialogsInteractor() {
        return new DialogsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IVideosInteractor createVideosInteractor() {
        return new VideosInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IAccountsInteractor createAccountInteractor() {
        return new AccountsInteractor(
                Injection.provideNetworkInterfaces(),
                Injection.provideSettings().accounts(),
                Injection.provideBlacklistRepository(),
                Repository.INSTANCE.getOwners()
        );
    }

    public static IPhotosInteractor createPhotosInteractor() {
        return new PhotosInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IFaveInteractor createFaveInteractor() {
        return new FaveInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
    }

    public static ILocalServerInteractor createLocalServerInteractor() {
        return new LocalServerInteractor(Injection.provideNetworkInterfaces());
    }

    public static IAudioInteractor createAudioInteractor() {
        return new AudioInteractor(Injection.provideNetworkInterfaces());
    }
}
