package dev.ragnarok.fenrir.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;

public class LinkSpan extends ClickableSpan {

    private final boolean is_underline;
    private final Context context;
    private final String link;

    public LinkSpan(Context context, String str, boolean is_underline) {
        this.is_underline = is_underline;
        this.context = context;
        link = str;
    }

    @Override
    public void onClick(@NonNull View widget) {
        if (Settings.get().other().is_notification_force_link()) {
            LinkHelper.openUrl((Activity) context, Settings.get().accounts().getCurrent(), link);
            return;
        }
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        menus.add(new OptionRequest(R.id.button_ok, context.getString(R.string.open), R.drawable.web, true));
        menus.add(new OptionRequest(R.id.button_cancel, context.getString(R.string.copy_simple), R.drawable.content_copy, true));
        menus.show(((FragmentActivity) context).getSupportFragmentManager(), "left_options", option -> {
            if (option.getId() == R.id.button_ok) {
                LinkHelper.openUrl((Activity) context, Settings.get().accounts().getCurrent(), link);
            } else if (option.getId() == R.id.button_cancel) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("response", link);
                clipboard.setPrimaryClip(clip);
                CustomToast.CreateCustomToast(context).showToast(R.string.copied_to_clipboard);
            }
        });
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        super.updateDrawState(textPaint);
        if (is_underline)
            textPaint.setColor(CurrentTheme.getColorPrimary(context));
        else
            textPaint.setColor(CurrentTheme.getColorSecondary(context));
        textPaint.setUnderlineText(is_underline);
    }
}
