package dev.ragnarok.fenrir.service

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.Repository.messages
import dev.ragnarok.fenrir.ifNonNullNoEmpty
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.SaveMessageBuilder
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.IntentService
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.syncSingleSafe
import java.io.File

class NotificationIntentService : IntentService(NotificationIntentService::class.java.name) {
    override fun onHandleIntent(intent: Intent?) {
        intent ?: return

        if (intent.action == ACTION_RETRY_UPLOAD) {
            Includes.uploadManager.retryAll()
            return
        }

        val extras = intent.extras ?: return
        when {
            ACTION_ADD_MESSAGE == intent.action -> {
                val accountId = extras.getLong(Extra.ACCOUNT_ID)
                val peerId = extras.getLong(Extra.PEER_ID)
                val msg = RemoteInput.getResultsFromIntent(intent)
                msg?.getCharSequence(Extra.BODY).ifNonNullNoEmpty({
                    addMessage(accountId, peerId, it.toString())
                }, {})
            }

            ACTION_MARK_AS_READ == intent.action -> {
                val accountId = extras.getLong(Extra.ACCOUNT_ID)
                val peerId = extras.getLong(Extra.PEER_ID)
                val msgId = extras.getInt(Extra.MESSAGE_ID)
                messages.markAsRead(accountId, peerId, msgId).syncSingleSafe()
            }

            ACTION_DELETE_FILE == intent.action -> {
                if (AppPerms.hasNotificationPermissionSimple(this)) {
                    NotificationManagerCompat.from(this).cancel(
                        extras.getString(Extra.TYPE), extras.getInt(Extra.ID)
                    )
                }
                File(extras.getString(Extra.DOC) ?: return).delete()
            }

            ACTION_VALIDATE == intent.action -> {
                val accountId = extras.getLong(Extra.ACCOUNT_ID)
                val hash = extras.getString(Extra.HASH)
                hash.ifNonNullNoEmpty({
                    InteractorFactory.createAccountInteractor()
                        .validateAction(accountId, true, it).syncSingleSafe()
                }, {})
            }
        }
    }

    private fun addMessage(accountId: Long, peerId: Long, text: String) {
        val messagesInteractor = messages
        val builder = SaveMessageBuilder(accountId, peerId).setText(text)
        messagesInteractor.put(builder).syncSingleSafe()
        messages.runSendingQueue()
    }

    companion object {
        const val ACTION_ADD_MESSAGE = "NotificationIntentService.ACTION_ADD_MESSAGE"
        const val ACTION_MARK_AS_READ = "NotificationIntentService.ACTION_MARK_AS_READ"
        const val ACTION_DELETE_FILE = "NotificationIntentService.ACTION_DELETE_FILE"
        const val ACTION_VALIDATE = "NotificationIntentService.ACTION_VALIDATE"
        const val ACTION_RETRY_UPLOAD = "NotificationIntentService.ACTION_RETRY_UPLOAD"

        fun intentForAddMessage(
            context: Context,
            accountId: Long,
            peerId: Long,
            msg: Message
        ): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_ADD_MESSAGE
            intent.putExtra(Extra.ACCOUNT_ID, accountId)
            intent.putExtra(Extra.PEER_ID, peerId)
            intent.putExtra(Extra.MESSAGE, msg)
            return intent
        }

        fun intentForDeleteFile(
            context: Context,
            path: String,
            notificationId: Int,
            notificationTag: String
        ): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_DELETE_FILE
            intent.putExtra(Extra.DOC, path)
            intent.putExtra(Extra.ID, notificationId)
            intent.putExtra(Extra.TYPE, notificationTag)
            return intent
        }

        fun intentForReadMessage(
            context: Context,
            accountId: Long,
            peerId: Long,
            msgId: Int
        ): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_MARK_AS_READ
            intent.putExtra(Extra.ACCOUNT_ID, accountId)
            intent.putExtra(Extra.PEER_ID, peerId)
            intent.putExtra(Extra.MESSAGE_ID, msgId)
            return intent
        }

        fun intentForAccountValidate(
            context: Context,
            accountId: Long,
            hash: String
        ): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_VALIDATE
            intent.putExtra(Extra.ACCOUNT_ID, accountId)
            intent.putExtra(Extra.HASH, hash)
            return intent
        }

        fun intentForRetryUpload(
            context: Context
        ): Intent {
            val intent = Intent(context, NotificationIntentService::class.java)
            intent.action = ACTION_RETRY_UPLOAD
            return intent
        }
    }
}