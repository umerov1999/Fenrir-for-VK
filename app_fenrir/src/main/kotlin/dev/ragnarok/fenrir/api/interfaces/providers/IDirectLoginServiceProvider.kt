package dev.ragnarok.fenrir.api.interfaces.providers

import dev.ragnarok.fenrir.api.services.IAuthService
import kotlinx.coroutines.flow.Flow

interface IDirectLoginServiceProvider {
    fun provideAuthService(): Flow<IAuthService>
}