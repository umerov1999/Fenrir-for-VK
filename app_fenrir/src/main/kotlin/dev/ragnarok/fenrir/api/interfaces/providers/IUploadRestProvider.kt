package dev.ragnarok.fenrir.api.interfaces.providers

import dev.ragnarok.fenrir.api.rest.SimplePostHttp
import kotlinx.coroutines.flow.Flow

interface IUploadRestProvider {
    fun provideUploadRest(): Flow<SimplePostHttp>
}