package dev.ragnarok.fenrir.api.interfaces.providers

import dev.ragnarok.fenrir.api.services.ILocalServerService
import kotlinx.coroutines.flow.Flow

interface ILocalServerServiceProvider {
    fun provideLocalServerService(): Flow<ILocalServerService>
}