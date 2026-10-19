package dev.ragnarok.filegallery.api.interfaces.providers

import dev.ragnarok.filegallery.api.rest.SimplePostHttp
import kotlinx.coroutines.flow.Flow

interface IUploadRestProvider {
    fun provideUploadRest(): Flow<SimplePostHttp>
}