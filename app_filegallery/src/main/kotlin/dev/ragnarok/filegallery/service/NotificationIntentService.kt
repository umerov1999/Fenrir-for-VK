package dev.ragnarok.filegallery.service

import android.content.Context
import android.content.Intent
import dev.ragnarok.filegallery.Includes
import dev.ragnarok.filegallery.util.IntentService

class NotificationIntentService : IntentService(NotificationIntentService::class.java.name) {
    override fun onHandleIntent(intent: Intent?) {
        intent ?: return

        if (intent.action == ACTION_RETRY_UPLOAD) {
            Includes.uploadManager.retryAll()
            return
        }
    }

    companion object {
        const val ACTION_RETRY_UPLOAD = "NotificationIntentService.ACTION_RETRY_UPLOAD"

        fun intentForRetryUpload(
            context: Context
        ): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_RETRY_UPLOAD
            return intent
        }
    }
}