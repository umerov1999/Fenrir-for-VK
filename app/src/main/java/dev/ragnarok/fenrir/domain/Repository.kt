package dev.ragnarok.fenrir.domain

import dev.ragnarok.fenrir.Injection
import dev.ragnarok.fenrir.db.Stores
import dev.ragnarok.fenrir.domain.impl.MessagesRepository
import dev.ragnarok.fenrir.domain.impl.OwnersRepository
import dev.ragnarok.fenrir.domain.impl.WallsRepository
import dev.ragnarok.fenrir.settings.Settings

object Repository {
    val owners: IOwnersRepository by lazy {
        OwnersRepository(Injection.provideNetworkInterfaces(), Stores.getInstance().owners())
    }

    val walls: IWallsRepository by lazy {
        WallsRepository(Injection.provideNetworkInterfaces(), Stores.getInstance(), owners)
    }

    val messages: IMessagesRepository by lazy {
        MessagesRepository(
            Settings.get().accounts(),
            Injection.provideNetworkInterfaces(),
            owners,
            Injection.provideStores(),
            Injection.provideUploadManager()
        )
    }
}