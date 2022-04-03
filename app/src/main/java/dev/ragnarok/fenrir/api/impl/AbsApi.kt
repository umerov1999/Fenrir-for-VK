package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.api.ApiException
import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.TokenType
import dev.ragnarok.fenrir.api.model.IAttachmentToken
import dev.ragnarok.fenrir.api.model.response.BaseResponse
import dev.ragnarok.fenrir.api.model.response.BlockResponse
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.nullOrEmpty
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.exceptions.Exceptions
import io.reactivex.rxjava3.functions.Function

internal open class AbsApi(val accountId: Int, private val retrofitProvider: IServiceProvider) {
    fun <T : Any> provideService(serviceClass: Class<T>, vararg tokenTypes: Int): Single<T> {
        var pTokenTypes: IntArray = tokenTypes
        if (pTokenTypes.nullOrEmpty()) {
            pTokenTypes = intArrayOf(TokenType.USER) // user by default
        }
        return retrofitProvider.provideService(accountId, serviceClass, *pTokenTypes)
    }

    companion object {

        fun <T : Any> extractResponseWithErrorHandling(): Function<BaseResponse<T>, T> {
            return Function { response: BaseResponse<T> ->
                if (response.error != null) {
                    throw Exceptions.propagate(ApiException(response.error))
                }
                response.response
            }
        }


        fun <T : Any> extractBlockResponseWithErrorHandling(): Function<BaseResponse<BlockResponse<T>>, T> {
            return Function { response: BaseResponse<BlockResponse<T>> ->
                if (response.error != null) {
                    throw Exceptions.propagate(ApiException(response.error))
                }
                response.response.block
            }
        }


        fun <T> handleExecuteErrors(vararg expectedMethods: String): Function<BaseResponse<T>, BaseResponse<T>> {
            require(expectedMethods.isNotEmpty()) { "No expected methods found" }
            return Function { response: BaseResponse<T> ->
                if (response.executeErrors.nonNullNoEmpty()) {
                    for (error in response.executeErrors) {
                        for (expectedMethod in expectedMethods) {
                            if (expectedMethod.equals(error.method, ignoreCase = true)) {
                                throw Exceptions.propagate(ApiException(error))
                            }
                        }
                    }
                }
                response
            }
        }

        inline fun <T> join(
            tokens: Iterable<T>?,
            delimiter: String?,
            crossinline function: (T) -> String
        ): String? {
            if (tokens == null) {
                return null
            }
            val sb = StringBuilder()
            var firstTime = true
            for (token in tokens) {
                if (firstTime) {
                    firstTime = false
                } else {
                    sb.append(delimiter)
                }
                sb.append(function.invoke(token))
            }
            return sb.toString()
        }

        fun join(tokens: Iterable<*>?, delimiter: String): String? {
            if (tokens == null) {
                return null
            }
            val sb = StringBuilder()
            var firstTime = true
            for (token in tokens) {
                if (firstTime) {
                    firstTime = false
                } else {
                    sb.append(delimiter)
                }
                sb.append(token)
            }
            return sb.toString()
        }


        fun formatAttachmentToken(token: IAttachmentToken): String {
            return token.format()
        }


        fun toQuotes(word: String?): String? {
            return if (word == null) {
                null
            } else "\"" + word + "\""
        }


        fun integerFromBoolean(value: Boolean?): Int? {
            return if (value == null) null else if (value) 1 else 0
        }
    }
}