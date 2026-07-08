package dev.ragnarok.filegallery.upload

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.api.PercentagePublisher
import dev.ragnarok.filegallery.api.interfaces.INetworker
import dev.ragnarok.filegallery.media.music.NotificationHelper
import dev.ragnarok.filegallery.service.NotificationIntentService.Companion.intentForRetryUpload
import dev.ragnarok.filegallery.upload.IUploadManager.IProgressUpdate
import dev.ragnarok.filegallery.upload.impl.RemoteAudioPlayUploadable
import dev.ragnarok.filegallery.util.AppPerms
import dev.ragnarok.filegallery.util.Optional
import dev.ragnarok.filegallery.util.Optional.Companion.wrap
import dev.ragnarok.filegallery.util.Pair
import dev.ragnarok.filegallery.util.Pair.Companion.create
import dev.ragnarok.filegallery.util.Utils.firstNonEmptyString
import dev.ragnarok.filegallery.util.Utils.getCauseIfRuntime
import dev.ragnarok.filegallery.util.Utils.makeImmutablePendingIntent
import dev.ragnarok.filegallery.util.coroutines.CompositeJob
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.createPublishSubject
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.inMainThread
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.isActive
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.myEmit
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.filegallery.util.toast.CustomToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.milliseconds

class UploadManagerImpl(
    context: Context,
    private val networker: INetworker
) : IUploadManager {
    private val context: Context = context.applicationContext
    private val queue: MutableList<Upload> = ArrayList()
    private val scheduler =
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())

    private val addingProcessor = createPublishSubject<List<Upload>>()
    private val deletingProcessor = createPublishSubject<IntArray>()
    private val completeProcessor = createPublishSubject<Pair<Upload, UploadResult<*>>>()
    private val statusProcessor = createPublishSubject<Upload>()
    private val lock = Any()

    private val timer: Flow<IProgressUpdate?> = flow {
        while (isActive()) {
            delay(PROGRESS_LOOKUP_DELAY.milliseconds)
            val ret: IProgressUpdate?
            synchronized(lock) {
                val pCurrent = current
                ret = if (pCurrent == null) {
                    null
                } else {
                    ProgressUpdate(pCurrent.id, pCurrent.progress)
                }
            }
            emit(ret)
        }
    }
    private val notificationUpdateDisposable = CompositeJob()
    private val compositeDisposable = CompositeJob()

    @Volatile
    private var current: Upload? = null
    private var needCreateChannel = true
    override fun get(destination: UploadDestination): Flow<List<Upload>> {
        return flow { emit(getByDestination(destination)) }
    }

    private fun getByDestination(destination: UploadDestination): List<Upload> {
        val data: MutableList<Upload> = ArrayList()
        synchronized(lock) {
            for (upload in queue) {
                if (destination.compareTo(upload.destination)) {
                    data.add(upload)
                }
            }
        }
        return data
    }

    private fun startWithNotification() {
        updateNotification(null)
        notificationUpdateDisposable.add(
            observeProgress().sharedFlowToMain {
                updateNotification(it)
            }
        )
    }

    private fun buildErrorUploadNotification(message: String?) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
                ?: return
        if (needCreateChannel) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.files_uploading_notification_title),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
            needCreateChannel = false
        }
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_upload)
                .setContentTitle(context.getString(R.string.files_uploading_error_notification_title))
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
        builder.priority = NotificationCompat.PRIORITY_HIGH

        val retryUploadIntent =
            intentForRetryUpload(context)
        val retryUploadPendingIntent = PendingIntent.getService(
            context,
            NotificationHelper.NOTIFICATION_UPLOAD_FAIL,
            retryUploadIntent,
            makeImmutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT)
        )
        val actionRetryUpload =
            NotificationCompat.Action.Builder(
                R.drawable.ic_notification_upload,
                context.resources.getString(R.string.retry), retryUploadPendingIntent
            ).build()
        builder.addAction(actionRetryUpload)

        if (AppPerms.hasNotificationPermissionSimple(context)) {
            notificationManager.notify(
                NotificationHelper.NOTIFICATION_UPLOAD_FAIL,
                builder.build()
            )
        }
    }

    private fun updateNotification(updates: IProgressUpdate?) {
        updates?.let {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager?
                    ?: return
            if (needCreateChannel) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.files_uploading_notification_title),
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
                needCreateChannel = false
            }
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(context.getString(R.string.files_uploading_notification_title) + " " + it.progress.toString() + "%")
                    .setSmallIcon(R.drawable.ic_notification_upload)
                    .setOngoing(true)
                    .setProgress(100, it.progress, false)
            if (AppPerms.hasNotificationPermissionSimple(context)) {
                notificationManager.notify(NotificationHelper.NOTIFICATION_UPLOAD, builder.build())
            }
        }
    }

    private fun stopNotification() {
        notificationUpdateDisposable.clear()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (AppPerms.hasNotificationPermissionSimple(context)) {
            notificationManager?.cancel(NotificationHelper.NOTIFICATION_UPLOAD)
        }
    }

    override fun enqueue(intents: List<UploadIntent>) {
        val all: MutableList<Upload> = ArrayList(intents.size)
        synchronized(lock) {
            for (intent in intents) {
                val upload = intent2Upload(intent)
                all.add(upload)
                queue.add(upload)
            }
        }
        addingProcessor.myEmit(all)
        startIfNotStarted()
    }

    private fun startIfNotStarted() {
        scheduler.launch {
            startIfNotStartedInternal()
        }
    }

    private fun findFirstQueue(): Upload? {
        var first: Upload? = null
        for (u in queue) {
            if (u.status == Upload.STATUS_QUEUE) {
                first = u
                break
            }
        }
        return first
    }

    private fun doUpload(uploadable: IUploadable<*>, upload: Upload) {
        compositeDisposable.add(
            scheduler.launch {
                uploadable.doUpload(
                    upload,
                    WeakProgressPublisher(upload)
                ).catch {
                    if (isActive()) {
                        onUploadFail(upload, it)
                    }
                }.collect {
                    if (isActive()) {
                        onUploadComplete(
                            upload,
                            it
                        )
                    }
                }
            }
        )
    }

    private fun startIfNotStartedInternal() {
        synchronized(lock) {
            if (current != null) {
                return
            }
            val first = findFirstQueue()
            if (first == null) {
                stopNotification()
                return
            }
            startWithNotification()
            current = first
            first.setStatus(Upload.STATUS_UPLOADING).errorText = null
            statusProcessor.myEmit(first)
            val uploadable = createUploadable(first)
            doUpload(uploadable, first)
        }
    }

    private fun onUploadComplete(upload: Upload, result: UploadResult<*>) {
        synchronized(lock) {
            queue.remove(upload)
            if (current === upload) {
                current = null
            }

            completeProcessor.myEmit(create(upload, result))
        }
        startIfNotStartedInternal()
    }

    private fun onUploadFail(upload: Upload, t: Throwable) {
        synchronized(lock) {
            if (current === upload) {
                current = null
                val cause = getCauseIfRuntime(t)
                val message: String? = firstNonEmptyString(cause.message, cause.toString())
                t.printStackTrace()
                compositeDisposable.add(inMainThread {
                    CustomToast.createCustomToast(context, null)
                        ?.setDuration(Toast.LENGTH_SHORT)
                        ?.showToastError(message)
                })
                buildErrorUploadNotification(message)
            }
            val errorMessage = firstNonEmptyString(t.message, t.toString())
            upload.setStatus(Upload.STATUS_ERROR).errorText = errorMessage
            statusProcessor.myEmit(upload)
        }
        startIfNotStartedInternal()
    }

    private fun findIndexById(data: List<Upload?>?, id: Int): Int {
        data ?: return -1
        for (i in data.indices) {
            if (data[i]?.id == id) {
                return i
            }
        }
        return -1
    }

    override fun cancel(id: Int) {
        synchronized(lock) {
            if (current?.id == id) {
                compositeDisposable.clear()
                current = null
            }
            val index = findIndexById(queue, id)
            if (index != -1) {
                queue.removeAt(index)
                deletingProcessor.myEmit(intArrayOf(id))
            }
        }
        startIfNotStarted()
    }

    override fun retry(id: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (AppPerms.hasNotificationPermissionSimple(context)) {
            notificationManager?.cancel(NotificationHelper.NOTIFICATION_UPLOAD_FAIL)
        }
        synchronized(lock) {
            val index = findIndexById(queue, id)
            if (index != -1) {
                val upload = queue[index]
                upload.setStatus(Upload.STATUS_QUEUE).errorText = null
                statusProcessor.myEmit(upload)
            }
        }
        startIfNotStarted()
    }

    override fun retryAll() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        if (AppPerms.hasNotificationPermissionSimple(context)) {
            notificationManager?.cancel(NotificationHelper.NOTIFICATION_UPLOAD_FAIL)
        }
        synchronized(lock) {
            for (i in queue) {
                i.setStatus(Upload.STATUS_QUEUE).errorText = null
                statusProcessor.myEmit(i)
            }
        }
        startIfNotStarted()
    }

    override fun cancelAll(destination: UploadDestination) {
        synchronized(lock) {
            if (current != null && destination.compareTo(
                    current?.destination
                )
            ) {
                compositeDisposable.clear()
                current = null
            }
            val target: MutableList<Upload> = ArrayList()
            val iterator = queue.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (destination.compareTo(next.destination)) {
                    iterator.remove()
                    target.add(next)
                }
            }
            if (target.isNotEmpty()) {
                val ids = IntArray(target.size)
                for (i in target.indices) {
                    ids[i] = target[i].id
                }
                deletingProcessor.myEmit(ids)
            }
        }
        startIfNotStarted()
    }

    override fun getCurrent(): Optional<Upload> {
        synchronized(lock) { return wrap(current) }
    }

    override fun observeDeleting(includeCompleted: Boolean): Flow<IntArray> {
        if (includeCompleted) {
            return merge(
                completeProcessor
                    .map { intArrayOf(it.first.id) }, deletingProcessor
            )
        }
        return deletingProcessor
    }

    override fun observeAdding(): SharedFlow<List<Upload>> {
        return addingProcessor
    }

    override fun observeStatus(): SharedFlow<Upload> {
        return statusProcessor
    }

    override fun observeResults(): SharedFlow<Pair<Upload, UploadResult<*>>> {
        return completeProcessor
    }

    override fun observeProgress(): Flow<IProgressUpdate?> {
        return timer
    }

    private fun createUploadable(upload: Upload): IUploadable<*> {
        val destination = upload.destination
        when (destination.method) {
            Method.REMOTE_PLAY_AUDIO -> return RemoteAudioPlayUploadable(
                context,
                networker
            )
        }
        throw UnsupportedOperationException()
    }

    class WeakProgressPublisher(upload: Upload) :
        PercentagePublisher {
        private val reference: WeakReference<Upload> = WeakReference(upload)
        override fun onProgressChanged(percentage: Int) {
            val upload = reference.get()
            if (upload != null) {
                upload.progress = percentage
            }
        }

    }

    class ProgressUpdate(override val id: Int, override val progress: Int) : IProgressUpdate
    companion object {
        const val PROGRESS_LOOKUP_DELAY = 500
        const val NOTIFICATION_CHANNEL_ID = "upload_files"
        fun intent2Upload(intent: UploadIntent): Upload {
            return Upload()
                .setAutoCommit(intent.pAutoCommit)
                .setDestination(intent.destination)
                .setFileId(intent.fileId)
                .setFileUri(intent.pFileUri)
                .setStatus(Upload.STATUS_QUEUE)
                .setSize(intent.size)
        }
    }
}
