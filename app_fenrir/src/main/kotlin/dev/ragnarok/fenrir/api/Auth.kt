package dev.ragnarok.fenrir.api

import android.util.Log
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.api.util.VKStringUtils.extractPattern
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object Auth {
    private const val TAG = "Fenrir.Auth"

    fun getRedirectUrl(): String {
        return "https://${Settings.get().main().authWebDomain}/blank.html"
    }

    @Throws(UnsupportedEncodingException::class)
    fun getUrl(api_id: String, scope: String, groupIds: String?): String {
        var url = "https://${Settings.get().main().authWebDomain}/authorize?client_id=$api_id"
        url = (url + "&display=mobile&scope="
                + scope + "&redirect_uri=" + URLEncoder.encode(
            getRedirectUrl(),
            "utf-8"
        ) + "&response_type=token"
                + "&v=" + URLEncoder.encode(
            Constants.API_VERSION,
            "utf-8"
        ) + "&lang=" + URLEncoder.encode(
            Constants.DEVICE_COUNTRY_CODE, "utf-8"
        ) + "&device_id=" + URLEncoder.encode(
            Utils.getDeviceId(
                Constants.DEFAULT_ACCOUNT_TYPE,
                provideApplicationContext()
            ), "utf-8"
        )) + "&https=1"
        if (groupIds.nonNullNoEmpty()) {
            url = "$url&group_ids=$groupIds"
        }
        return url
    }

    //https://vk.ru/dev/permission
    //return "notify,friends,photos,audio,video,stories,pages,status,notes,messages,wall,offline,docs,groups,notifications,stats,email,market";

    val scope: String
        get() =//https://vk.ru/dev/permission
            //return "notify,friends,photos,audio,video,stories,pages,status,notes,messages,wall,offline,docs,groups,notifications,stats,email,market";
            if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
                "notify,friends,photos,audio,video,docs,status,notes,pages,wall,groups,messages,offline,notifications,stories"
            } else {
                "all"
            }

    val groupScope: String
        get() = "messages,manage,photos,docs,wall,stories"

    @Throws(Exception::class)
    fun parseRedirectUrl(url: String): Array<String> {
        //url is something like http://api.vkontakte.ru/blank.html#access_token=66e8f7a266af0dd477fcd3916366b17436e66af77ac352aeb270be99df7deeb&expires_in=0&user_id=7657164
        val access_token = extractPattern(url, "access_token=(.*?)&")
        Log.i(TAG, "access_token=$access_token")
        val user_id = extractPattern(url, "user_id=(\\d*)")
        Log.i(TAG, "user_id=$user_id")
        if (user_id.isNullOrEmpty() || access_token.isNullOrEmpty()) {
            throw Exception("Failed to parse redirect url $url")
        }
        return arrayOf(access_token, user_id)
    }
}
