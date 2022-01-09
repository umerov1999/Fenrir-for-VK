package dev.ragnarok.fenrir.util;

import android.content.Context;
import android.text.Spannable;

import java.text.DateFormat;
import java.util.Date;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;


public class FormatUtil {

    public static Spannable formatCommunityBanInfo(Context context, int adminId, String adminName,
                                                   long endDate, OwnerLinkSpanFactory.ActionListener adminClickListener) {
        String endDateString;
        if (endDate == 0) {
            endDateString = context.getString(R.string.forever).toLowerCase();
        } else {
            Date date = new Date(endDate * 1000);
            String formattedDate = DateFormat.getDateInstance().format(date);
            String formattedTime = DateFormat.getTimeInstance().format(date);
            endDateString = context.getString(R.string.until_date_time, formattedDate, formattedTime);
        }

        String adminLink = OwnerLinkSpanFactory.genOwnerLink(adminId, adminName);

        String fullInfoText = context.getString(R.string.ban_admin_and_date_text, adminLink, endDateString);
        return OwnerLinkSpanFactory.withSpans(fullInfoText, true, false, adminClickListener);
    }
}