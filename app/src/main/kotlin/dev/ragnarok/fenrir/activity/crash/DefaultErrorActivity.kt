package dev.ragnarok.fenrir.activity.crash

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.slidr.Slidr
import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig
import dev.ragnarok.fenrir.activity.slidr.model.SlidrListener
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.util.Utils

class DefaultErrorActivity : AppCompatActivity() {
    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.App_CrashError)
        super.onCreate(savedInstanceState)
        Slidr.attach(
            this,
            SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this))
                .listener(object : SlidrListener {
                    override fun onSlideStateChanged(state: Int) {
                    }

                    override fun onSlideChange(percent: Float) {
                    }

                    override fun onSlideOpened() {
                    }

                    override fun onSlideClosed(): Boolean {
                        CrashUtils.closeApplication(this@DefaultErrorActivity)
                        return true
                    }
                }).build()
        )
        setContentView(R.layout.crash_error_activity)
        findViewById<MaterialButton>(R.id.crash_error_activity_restart_button).setOnClickListener {
            CrashUtils.restartApplication(
                this
            )
        }
        findViewById<FloatingActionButton>(R.id.crash_error_activity_mail_to_button)
            .visibility =
            if (Utils.compareFingerprintHashForPackage(this)) View.VISIBLE else View.GONE

        findViewById<FloatingActionButton>(R.id.crash_error_activity_mail_to_button).setOnClickListener {
            val msgIntent = Intent(Intent.ACTION_SEND)
            msgIntent.type = "message/rfc822"
            msgIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("fenrir.logs@list.ru"))
            msgIntent.putExtra(Intent.EXTRA_SUBJECT, "Fenrir Crash/Warning Log")
            msgIntent.putExtra(
                Intent.EXTRA_TEXT,
                CrashUtils.getAllErrorDetailsFromIntent(this, intent)
            )
            msgIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(msgIntent)
        }

        findViewById<MaterialButton>(R.id.crash_error_activity_more_info_button).setOnClickListener {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.crash_error_activity_error_details_title)
                .setMessage(CrashUtils.getAllErrorDetailsFromIntent(this, intent))
                .setPositiveButton(R.string.crash_error_activity_error_details_close, null)
                .setNeutralButton(
                    R.string.crash_error_activity_error_details_copy
                ) { _: DialogInterface?, _: Int -> copyErrorToClipboard() }
                .show()
            val textView = dialog.findViewById<TextView>(android.R.id.message)
            textView?.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                resources.getDimension(R.dimen.crash_error_activity_error_details_text_size)
            )
        }
    }

    private fun copyErrorToClipboard() {
        val errorInformation = CrashUtils.getAllErrorDetailsFromIntent(this, intent)
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        if (clipboard != null) {
            val clip = ClipData.newPlainText(
                getString(R.string.crash_error_activity_error_details_clipboard_label),
                errorInformation
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(
                this,
                R.string.crash_error_activity_error_details_copied,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}