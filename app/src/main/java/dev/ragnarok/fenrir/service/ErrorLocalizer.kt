package dev.ragnarok.fenrir.service

import android.content.Context
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.ApiException
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.util.Utils
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorLocalizer {
    private val sApiLocalizer = ApiLocalizer()

    @JvmStatic
    fun localizeThrowable(context: Context, throwable: Throwable?): String {
        throwable ?: return "null"
        if (throwable is ApiException) {
            val error = throwable.error
            return api().getMessage(context, error.errorCode, error.errorMsg) ?: "null"
        }
        if (throwable is SocketTimeoutException) {
            return context.getString(R.string.error_timeout_message)
        }
        if (throwable is UnknownHostException) {
            return context.getString(R.string.error_unknown_host)
        }
        if (throwable is NotFoundException) {
            return context.getString(R.string.error_not_found_message)
        }
        return if (Utils.nonEmpty(throwable.message)) throwable.message!! else throwable.toString()
    }

    fun api(): Localizer {
        return sApiLocalizer
    }

    interface Localizer {
        fun getMessage(
            context: Context,
            code: Int,
            ifUnknown: String?,
            vararg params: Any?
        ): String?
    }

    open class BaseLocazer : Localizer {
        override fun getMessage(
            context: Context,
            code: Int,
            ifUnknown: String?,
            vararg params: Any?
        ): String? {
            return if (Utils.isEmpty(ifUnknown)) context.getString(R.string.unknown_error) else ifUnknown
        }
    }

    class ApiLocalizer : BaseLocazer() {
        override fun getMessage(
            context: Context,
            code: Int,
            ifUnknown: String?,
            vararg params: Any?
        ): String? {
            when (code) {
                -1 -> return context.getString(R.string.api_error_internal_1)
                1 -> return context.getString(R.string.api_error_1)
                2 -> return context.getString(R.string.api_error_2)
                3 -> return context.getString(R.string.api_error_3)
                4 -> return context.getString(R.string.api_error_4)
                ApiErrorCodes.USER_AUTHORIZATION_FAILED -> return context.getString(R.string.api_error_5)
                6 -> return context.getString(R.string.api_error_6)
                7 -> return context.getString(R.string.api_error_7)
                8 -> return context.getString(R.string.api_error_8)
                9 -> return context.getString(R.string.api_error_9)
                10 -> return context.getString(R.string.api_error_10)
                11 -> return context.getString(R.string.api_error_11)
                ApiErrorCodes.CAPTCHA_NEED -> return context.getString(R.string.api_error_14)
                ApiErrorCodes.ACCESS_DENIED -> return context.getString(R.string.api_error_15)
                16 -> return context.getString(R.string.api_error_16)
                17 -> return context.getString(R.string.api_error_17)
                ApiErrorCodes.PAGE_HAS_BEEN_REMOVED_OR_BLOCKED -> return context.getString(R.string.api_error_18)
                19 -> return context.getString(R.string.api_error_19)
                20 -> return context.getString(R.string.api_error_20)
                21 -> return context.getString(R.string.api_error_21)
                23 -> return context.getString(R.string.api_error_23)
                24 -> return context.getString(R.string.api_error_24)
                25 -> return context.getString(R.string.api_error_25)
                27 -> return context.getString(R.string.api_error_27)
                28 -> return context.getString(R.string.api_error_28)
                29 -> return context.getString(R.string.api_error_29)
                30 -> return context.getString(R.string.api_error_30)
                34 -> return context.getString(R.string.api_error_34)
                100 -> return context.getString(R.string.api_error_100)
                101 -> return context.getString(R.string.api_error_101)
                103 -> return context.getString(R.string.api_error_103)
                105 -> return context.getString(R.string.api_error_105)
                113 -> return context.getString(R.string.api_error_113)
                114 -> return context.getString(R.string.api_error_114)
                118 -> return context.getString(R.string.api_error_118)
                121 -> return context.getString(R.string.api_error_121)
                150 -> return context.getString(R.string.api_error_150)
                174 -> return context.getString(R.string.api_error_174)
                175 -> return context.getString(R.string.api_error_175)
                176 -> return context.getString(R.string.api_error_176)
                200 -> return context.getString(R.string.api_error_200)
                201 -> return context.getString(R.string.api_error_201)
                203 -> return context.getString(R.string.api_error_203)
                210 -> return context.getString(R.string.api_error_210)
                214 -> return context.getString(R.string.api_error_214)
                219 -> return context.getString(R.string.api_error_219)
                220 -> return context.getString(R.string.api_error_220)
                221 -> return context.getString(R.string.api_error_221)
                300 -> return context.getString(R.string.api_error_300)
                500 -> return context.getString(R.string.api_error_500)
                600 -> return context.getString(R.string.api_error_600)
                603 -> return context.getString(R.string.api_error_603)
                701 -> return context.getString(R.string.api_error_701)
                800 -> return context.getString(R.string.api_error_800)
                900 -> return context.getString(R.string.api_error_900)
                901 -> return context.getString(R.string.api_error_901)
                902 -> return context.getString(R.string.api_error_902)
                909 -> return context.getString(R.string.api_error_909)
                914 -> return context.getString(R.string.api_error_914)
                913 -> return context.getString(R.string.api_error_913)
                921 -> return context.getString(R.string.api_error_921)
                1150 -> return context.getString(R.string.api_error_1150)
                1151 -> return context.getString(R.string.api_error_1151)
                1170 -> return context.getString(R.string.api_error_1170)
            }
            return super.getMessage(context, code, ifUnknown, *params)
        }
    }
}