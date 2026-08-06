package dev.ragnarok.fenrir.link.internal

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.fragment.app.FragmentActivity
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.link.LinkHelper
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.toast.CustomToast.Companion.createCustomToast

class LinkSpan(
    private val context: Context,
    private val link: String,
    private val is_underline: Boolean
) : ClickableSpan() {
    override fun onClick(widget: View) {
        if (Settings.get().main().is_notification_force_link) {
            LinkHelper.openUrl(context as Activity, Settings.get().accounts().current, link)
            return
        }
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                0,
                context.getString(
                    if (link.contains("tel:")) {
                        R.string.call
                    } else {
                        R.string.open
                    }
                ),
                R.drawable.web,
                true
            )
        )
        menus.add(
            OptionRequest(
                1,
                context.getString(R.string.copy_simple),
                R.drawable.content_copy,
                true
            )
        )
        menus.show(
            (context as FragmentActivity).supportFragmentManager,
            "left_options"
        ) { _, option ->
            when (option.id) {
                0 -> {
                    LinkHelper.openUrl(
                        context as Activity,
                        Settings.get().accounts().current,
                        link
                    )
                }

                1 -> {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    var tmpLink = link
                    if (tmpLink.contains("tel:")) {
                        tmpLink = tmpLink.replace("tel:", "")
                    } else if (tmpLink.contains("mailto:")) {
                        tmpLink = tmpLink.replace("mailto:", "")
                    }
                    val clip = ClipData.newPlainText("response", tmpLink)
                    clipboard?.setPrimaryClip(clip)
                    createCustomToast(context, null)?.showToast(R.string.copied_to_clipboard)
                }
            }
        }
    }

    override fun updateDrawState(textPaint: TextPaint) {
        super.updateDrawState(textPaint)
        textPaint.color = if (is_underline) {
            CurrentTheme.getColorPrimary(context)
        } else {
            CurrentTheme.getColorSecondary(
                context
            )
        }
        textPaint.isUnderlineText = is_underline
    }
}