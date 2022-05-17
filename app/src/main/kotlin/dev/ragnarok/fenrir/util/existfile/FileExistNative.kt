package dev.ragnarok.fenrir.util.existfile

import android.content.Context
import com.google.gson.stream.JsonReader
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper
import dev.ragnarok.fenrir.module.StringExist
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadStoragePermissionSimple
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.abs

class FileExistNative : AbsFileExist {
    private val CachedAudios = StringExist(true)
    private val RemoteAudios = StringExist(true)
    private val CachedPhotos = StringExist(true)

    @Throws(IOException::class)
    private fun findRemoteAudios(context: Context, requestPermission: Boolean) {
        if (requestPermission) {
            if (!hasReadStoragePermissionSimple(context)) return
        }
        RemoteAudios.clear()
        val audios = File(Settings.get().other().musicDir, "local_server_audio_list.json")
        if (!audios.exists()) {
            return
        }
        val reader = JsonReader(InputStreamReader(FileInputStream(audios), StandardCharsets.UTF_8))
        reader.beginArray()
        while (reader.hasNext()) {
            RemoteAudios.insert(reader.nextString().lowercase(Locale.getDefault()))
        }
    }

    @Throws(IOException::class)
    override fun findRemoteAudios(context: Context) {
        findRemoteAudios(context, true)
    }

    private fun transform_owner(owner_id: Int): String {
        return if (owner_id < 0) "club" + abs(owner_id) else "id$owner_id"
    }

    private fun existPhoto(photo: Photo): Boolean {
        return CachedPhotos.contains(transform_owner(photo.ownerId) + "_" + photo.getObjectId())
    }

    private fun loadDownloadPath(Path: String) {
        val temp = File(Path)
        if (!temp.exists()) return
        val file_list = temp.listFiles()
        if (file_list == null || file_list.isEmpty()) return
        for (u in file_list) {
            if (u.isFile) CachedPhotos.insert(u.name) else if (u.isDirectory) {
                loadDownloadPath(u.absolutePath)
            }
        }
    }

    override fun findLocalImages(photos: List<SelectablePhotoWrapper>): Completable {
        return Completable.create { t: CompletableEmitter ->
            val temp = File(Settings.get().other().photoDir)
            if (!temp.exists()) {
                t.onComplete()
                return@create
            }
            val file_list = temp.listFiles()
            if (file_list == null || file_list.isEmpty()) {
                t.onComplete()
                return@create
            }
            CachedPhotos.clear()
            for (u in file_list) {
                if (u.isFile) CachedPhotos.insert(u.name) else if (u.isDirectory) {
                    loadDownloadPath(u.absolutePath)
                }
            }
            for (i in photos) {
                i.setDownloaded(existPhoto(i.photo))
            }
            t.onComplete()
        }
    }

    override fun addAudio(file: String) {
        CachedAudios.insert(file.lowercase(Locale.getDefault()))
    }

    override fun findAllAudios(context: Context): Completable {
        return if (!hasReadStoragePermissionSimple(context)) Completable.complete() else Completable.create { t: CompletableEmitter ->
            findRemoteAudios(context, false)
            val temp = File(Settings.get().other().musicDir)
            if (!temp.exists()) {
                t.onComplete()
                return@create
            }
            val file_list = temp.listFiles()
            if (file_list == null || file_list.isEmpty()) {
                t.onComplete()
                return@create
            }
            CachedAudios.clear()
            for (u in file_list) {
                if (u.isFile) CachedAudios.insert(u.name.lowercase(Locale.getDefault()))
            }
        }
    }

    override fun markExistPhotos(photos: List<SelectablePhotoWrapper>) {
        if (photos.isEmpty()) {
            return
        }
        for (i in photos) {
            i.setDownloaded(existPhoto(i.photo))
        }
    }

    override fun isExistRemoteAudio(file: String): Boolean {
        return RemoteAudios.has(file.lowercase(Locale.getDefault()))
    }

    override fun isExistAllAudio(file: String): Int {
        val res = file.lowercase(Locale.getDefault())
        if (CachedAudios.has(res)) {
            return 1
        }
        return if (RemoteAudios.has(res)) {
            2
        } else 0
    }
}