package dev.ragnarok.filegallery.api.interfaces.providers

import dev.ragnarok.filegallery.api.services.ILocalServerService
import kotlinx.coroutines.flow.Flow

interface ILocalServerServiceProvider {
    fun provideLocalServerService(): Flow<ILocalServerService>
}