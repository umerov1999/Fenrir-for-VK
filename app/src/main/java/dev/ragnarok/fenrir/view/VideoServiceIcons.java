package dev.ragnarok.fenrir.view;

import androidx.annotation.DrawableRes;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.VideoPlatform;

public class VideoServiceIcons {

    @DrawableRes
    public static Integer getIconByType(String platform) {
        if (platform == null) {
            return null;
        }

        switch (platform) {
            default:
                return null;
            case VideoPlatform.COUB:
                return R.drawable.ic_coub;
            case VideoPlatform.VIMEO:
                return R.drawable.ic_vimeo;
            case VideoPlatform.YOUTUBE:
                return R.drawable.ic_youtube;
            case VideoPlatform.RUTUBE:
                return R.drawable.ic_rutube;
        }
    }
}