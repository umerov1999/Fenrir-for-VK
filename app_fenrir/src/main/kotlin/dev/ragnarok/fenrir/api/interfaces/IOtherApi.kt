package dev.ragnarok.fenrir.api.interfaces

import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody

interface IOtherApi {
    fun rawRequest(method: String, postParams: Map<String, String>): Flow<ResponseBody?>
}
