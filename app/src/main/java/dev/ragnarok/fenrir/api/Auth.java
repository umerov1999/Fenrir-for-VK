package dev.ragnarok.fenrir.api;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.util.VKStringUtils;
import dev.ragnarok.fenrir.util.Utils;

public class Auth {

    public static final String redirect_url = "https://oauth.vk.com/blank.html";
    private static final String TAG = "Fenrir.Auth";

    public static String getUrl(String api_id, String scope, String groupIds) throws UnsupportedEncodingException {
        String url = "https://oauth.vk.com/authorize?client_id=" + api_id;

        url = url + "&display=mobile&scope="
                + scope + "&redirect_uri=" + URLEncoder.encode(redirect_url, "utf-8") + "&response_type=token"
                + "&v=" + URLEncoder.encode(Constants.API_VERSION, "utf-8") + "&lang=" + URLEncoder.encode(Constants.DEVICE_COUNTRY_CODE, "utf-8") + "&device_id=" + URLEncoder.encode(Utils.getDeviceId(Injection.provideApplicationContext()), "utf-8");

        if (Utils.nonEmpty(groupIds)) {
            url = url + "&group_ids=" + groupIds;
        }
        return url;
    }

    public static String getScope() {
        //http://vk.com/dev/permission
        //return "notify,friends,photos,audio,video,stories,pages,status,notes,messages,wall,offline,docs,groups,notifications,stats,email,market";
        if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
            return "notify,friends,photos,audio,video,docs,status,notes,pages,wall,groups,messages,offline,notifications,stories";
        } else {
            return "all";
        }
    }

    public static String[] parseRedirectUrl(String url) throws Exception {
        //url is something like http://api.vkontakte.ru/blank.html#access_token=66e8f7a266af0dd477fcd3916366b17436e66af77ac352aeb270be99df7deeb&expires_in=0&user_id=7657164
        String access_token = VKStringUtils.extractPattern(url, "access_token=(.*?)&");
        Log.i(TAG, "access_token=" + access_token);
        String user_id = VKStringUtils.extractPattern(url, "user_id=(\\d*)");
        Log.i(TAG, "user_id=" + user_id);

        if (user_id == null || user_id.length() == 0 || access_token == null || access_token.length() == 0) {
            throw new Exception("Failed to parse redirect url " + url);
        }

        return new String[]{access_token, user_id};
    }
}
