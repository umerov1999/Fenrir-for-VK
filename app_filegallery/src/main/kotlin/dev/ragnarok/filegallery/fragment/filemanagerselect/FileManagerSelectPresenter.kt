package dev.ragnarok.filegallery.fragment.filemanagerselect

import android.os.Environment
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManagerSavedState
import dev.ragnarok.filegallery.fragment.base.RxSupportPresenter
import dev.ragnarok.filegallery.model.FileItemSelect
import dev.ragnarok.filegallery.settings.Settings
import dev.ragnarok.filegallery.util.Objects.safeEquals
import dev.ragnarok.filegallery.util.Utils
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.fromIOToMain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FilenameFilter

class FileManagerSelectPresenter(
    private var path: File,
    private val ext: String?
) : RxSupportPresenter<IFileManagerSelectView>() {
    private val fileList: ArrayList<FileItemSelect> = ArrayList()
    private val fileListSearch: ArrayList<FileItemSelect> = ArrayList()
    private var isLoading = false
    private val directoryScrollPositions = HashMap<String, Pair<Parcelable, Int>>()
    private var q: String? = null

    private val filter: FilenameFilter = FilenameFilter { dir, filename ->
        val sel = File(dir, filename)
        if (sel.absolutePath == File(
                Environment.getExternalStorageDirectory(),
                "Android"
            ).absolutePath
        ) {
            return@FilenameFilter false
        }
        val ret = !sel.isHidden && sel.canRead()
        if ("dirs" == ext && sel.isDirectory && ret) {
            return@FilenameFilter true
        } else if (ext == null && ret) {
            return@FilenameFilter true
        } else if ((!sel.isDirectory && ext?.let {
                sel.extension.contains(
                    it,
                    true
                )
            } == true || sel.isDirectory) && ret) {
            return@FilenameFilter true
        }
        return@FilenameFilter false
    }

    fun getCurrentDir(): String {
        return path.absolutePath
    }

    fun doSearch(str: String?) {
        if (isLoading) {
            return
        }
        var query: String? = str?.trim()
        if (query.isNullOrEmpty()) {
            query = null
        }
        if (safeEquals(query, this.q)) {
            return
        }
        q = query
        val locale = Utils.appLocale
        if (query == null) {
            fileListSearch.clear()
            view?.resolveEmptyText(fileList.isEmpty())
            view?.displayData(fileList)
            view?.updatePathString(path.absolutePath)
        } else {
            fileListSearch.clear()
            for (i in fileList) {
                if (i.file_name.isNullOrEmpty()) {
                    continue
                }
                if (i.file_name.lowercase(locale)
                        .contains(query.lowercase(locale))
                ) {
                    fileListSearch.add(i)
                }
            }
            view?.resolveEmptyText(fileListSearch.isEmpty())
            view?.displayData(fileListSearch)
            view?.updatePathString(query)
        }
    }

    override fun onGuiCreated(viewHost: IFileManagerSelectView) {
        super.onGuiCreated(viewHost)
        (if (q == null) path.absolutePath else q)?.let { viewHost.updatePathString(it) }
        viewHost.displayData(if (q == null) fileList else fileListSearch)
        viewHost.updatePathString(path.absolutePath)
        viewHost.resolveEmptyText(fileList.isEmpty())
        viewHost.resolveLoading(isLoading)
        viewHost.updateSelectVisibility(ext == "dirs")
        viewHost.updateHeader(ext)
    }

    fun backupDirectoryScroll(scroll: Parcelable, appVerticalOffset: Int) {
        directoryScrollPositions[path.absolutePath] = Pair(scroll, appVerticalOffset)
    }

    fun loadUp() {
        if (isLoading) {
            return
        }
        val parent = path.parentFile
        if (parent != null && parent.canRead()) {
            path = parent
            view?.updatePathString(path.absolutePath)
            loadFiles()
        }
    }

    fun setCurrent(file: File) {
        if (isLoading) {
            return
        }
        if (file.canRead()) {
            view?.displayData(fileList)
            q = null
            path = file
            view?.updatePathString(path.absolutePath)
            loadFiles()
        }
    }

    fun canLoadUp(): Boolean {
        val parent = path.parentFile
        return parent != null && parent.canRead()
    }

    private fun loadFiles() {
        if (isLoading) {
            return
        }
        isLoading = true
        view?.resolveEmptyText(false)
        view?.resolveLoading(isLoading)
        appendJob(rxLoadFileList().fromIOToMain({
            fileList.clear()
            fileList.addAll(it)
            isLoading = false
            view?.resolveEmptyText(fileList.isEmpty())
            view?.resolveLoading(isLoading)
            view?.notifyAllChanged()
            directoryScrollPositions.remove(path.absolutePath)?.let { scroll ->
                view?.restoreScroll(scroll.first, scroll.second)
            } ?: view?.restoreScroll(LinearLayoutManagerSavedState(), 0)
        }, {
            view?.showThrowable(it)
            isLoading = false
            view?.resolveLoading(isLoading)
        }))
    }

    private fun getFolderFilesCount(file: File): Long {
        if (!Settings.get().main().isEnable_dirs_files_count) {
            return -1
        }
        return file.list()?.size?.toLong() ?: -1
    }

    private fun rxLoadFileList(): Flow<ArrayList<FileItemSelect>> {
        return flow {
            val fileListTmp = ArrayList<FileItemSelect>()
            if (path.exists() && path.canRead()) {
                val fList = path.list(filter)
                if (fList != null) {
                    for (i in fList.indices) {
                        // Convert into file path
                        val file = File(path, fList[i])
                        val mod = file.lastModified()

                        fileListTmp.add(
                            i,
                            FileItemSelect(
                                file.isDirectory,
                                fList[i],
                                file.absolutePath,
                                path.name,
                                path.absolutePath,
                                mod,
                                if (file.isDirectory) getFolderFilesCount(file) else file.length(),
                            )
                        )
                    }
                    val dirsList = ArrayList<FileItemSelect>()
                    val flsList = ArrayList<FileItemSelect>()
                    for (i in fileListTmp) {
                        if (i.isDir) dirsList.add(i) else flsList.add(i)
                    }
                    dirsList.sortByDescending { it.modification }
                    flsList.sortByDescending { it.modification }
                    fileListTmp.clear()
                    fileListTmp.addAll(dirsList)
                    fileListTmp.addAll(flsList)
                }
            }
            emit(fileListTmp)
        }
    }

    init {
        loadFiles()
    }
}
