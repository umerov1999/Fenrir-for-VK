package dev.ragnarok.fenrir.fragment.localserver.filemanagerremote

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.StubAnimatorListener
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.BackPressCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.listener.UpdatableNavigation
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.media.music.MusicPlaybackService
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.FileRemote
import dev.ragnarok.fenrir.model.FileType
import dev.ragnarok.fenrir.model.InternalVideoSize
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.model.menu.options.FileLocalServerOption
import dev.ragnarok.fenrir.module.parcel.ParcelNative
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.service.ErrorLocalizer
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toColor
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.fenrir.util.ViewUtils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toMain
import dev.ragnarok.fenrir.util.toast.CustomSnackbars
import dev.ragnarok.fenrir.view.MySearchView
import dev.ragnarok.fenrir.view.natives.animation.ThorVGLottieView

class FileManagerRemoteFragment :
    BaseMvpFragment<FileManagerRemotePresenter, IFileManagerRemoteView>(),
    IFileManagerRemoteView, FileManagerRemoteAdapter.ClickListener, BackPressCallback {
    private var mRecyclerView: RecyclerView? = null
    private var mLayoutManager: GridLayoutManager? = null
    private var empty: TextView? = null
    private var loading: ThorVGLottieView? = null
    private var tvCurrentDir: TextView? = null
    private var mAdapter: FileManagerRemoteAdapter? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private var animationDispose = CancelableJob()
    private var mAnimationLoaded = false
    private var animLoad: ObjectAnimator? = null
    private var mySearchView: MySearchView? = null
    private var musicButton: FloatingActionButton? = null
    private var appBarView: AppBarLayout? = null
    private var appVerticalOffset = 0

    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        presenter?.continueDownload()
    }

    override fun onBackPressed(): Boolean {
        if (presenter?.canLoadUp() == true) {
            mLayoutManager?.onSaveInstanceState()
                ?.let { presenter?.backupDirectoryScroll(it, appVerticalOffset) }
            presenter?.loadUp()
            mySearchView?.clear()
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        animationDispose.cancel()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        FileManagerRemotePresenter(requireArguments().getLong(Extra.ACCOUNT_ID), saveInstanceState)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_file_remote_explorer, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        appBarView = root.findViewById(R.id.actionbar)
        appBarView?.addOnOffsetChangedListener { _, i ->
            appVerticalOffset = i
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        mRecyclerView = root.findViewById(R.id.list)
        empty = root.findViewById(R.id.empty)

        mySearchView = root.findViewById(R.id.searchview)
        mySearchView?.setRightButtonVisibility(false)
        mySearchView?.setLeftIcon(R.drawable.magnify)
        mySearchView?.setOnBackButtonClickListener(object : MySearchView.OnBackButtonClickListener {
            override fun onBackButtonClick() {
                presenter?.doSearch(mySearchView?.text?.toString())
            }
        })

        mySearchView?.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                presenter?.doSearch(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                presenter?.doSearch(newText)
                return false
            }
        })

        val columns = resources.getInteger(R.integer.files_column_count)
        mLayoutManager = GridLayoutManager(requireActivity(), columns, RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = mLayoutManager
        PicassoPauseOnScrollListener.addListener(mRecyclerView)
        tvCurrentDir = root.findViewById(R.id.current_path)
        loading = root.findViewById(R.id.loading)

        animLoad = ObjectAnimator.ofFloat(loading, View.ALPHA, 0.0f).setDuration(1000)
        animLoad?.addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                loading?.releaseAnimation()
                loading?.visibility = View.GONE
                loading?.alpha = 1f
            }

            override fun onAnimationCancel(animation: Animator) {
                loading?.releaseAnimation()
                loading?.visibility = View.GONE
                loading?.alpha = 1f
            }
        })

        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout?.setOnRefreshListener {
            mSwipeRefreshLayout?.isRefreshing = false
            if (presenter?.canRefresh() == true) {
                mLayoutManager?.onSaveInstanceState()
                    ?.let { presenter?.backupDirectoryScroll(it, appVerticalOffset) }
                presenter?.loadFiles()
            }
        }
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)

        mAdapter = FileManagerRemoteAdapter(requireActivity(), emptyList())
        mAdapter?.setClickListener(this)
        mRecyclerView?.adapter = mAdapter

        musicButton = root.findViewById(R.id.music_button)
        musicButton?.setOnLongClickListener {
            val curr = MusicPlaybackController.currentAudio
            if (curr != null) {
                getPlayerPlace(requireArguments().getLong(Extra.ACCOUNT_ID))
                    .tryOpenWith(requireActivity())
            } else customToast?.showToastError(R.string.null_audio)
            false
        }
        musicButton?.setOnClickListener {
            val curr = MusicPlaybackController.currentAudio
            if (curr != null) {
                if (presenter?.scrollTo(curr.id, curr.ownerId) != true) {
                    customToast?.showToastError(R.string.audio_not_found)
                }
            } else customToast?.showToastError(R.string.null_audio)
        }
        return root
    }

    override fun onClick(position: Int, item: FileRemote) {
        if (item.type == FileType.folder) {
            mLayoutManager?.onSaveInstanceState()
                ?.let { presenter?.backupDirectoryScroll(it, appVerticalOffset) }
            presenter?.goFolder(item)
        } else {
            presenter?.onClickFile(item)
        }
    }

    override fun onOpenAudioMenu(
        position: Int,
        audio: FileRemote
    ) {
        presenter?.fireOpenAudioMenu(position, audio)
    }

    override fun onOpenFileMenu(position: Int, file: FileRemote) {
        presenter?.fireOpenFileMenu(position, file)
    }

    override fun displayData(items: ArrayList<FileRemote>) {
        mAdapter?.setItems(items)
    }

    override fun resolveEmptyText(visible: Boolean) {
        empty?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun resolveLoading(visible: Boolean) {
        animationDispose.cancel()
        if (mAnimationLoaded && !visible) {
            mAnimationLoaded = false
            animLoad?.start()
        } else if (!mAnimationLoaded && visible) {
            animLoad?.end()
            animationDispose += delayTaskFlow(300).toMain {
                mAnimationLoaded = true
                loading?.visibility = View.VISIBLE
                loading?.alpha = 1f
                loading?.fromRes(
                    dev.ragnarok.fenrir_common.R.raw.s_loading,
                    intArrayOf(
                        0x333333,
                        CurrentTheme.getColorPrimary(activity),
                        0x777777,
                        CurrentTheme.getColorSecondary(activity)
                    )
                )
                loading?.startAnimation()
            }
        }
    }

    override fun onError(throwable: Throwable) {
        CustomSnackbars.createCustomSnackbars(mRecyclerView)?.setDurationSnack(Snackbar.LENGTH_LONG)
            ?.coloredSnack(
                ErrorLocalizer.localizeThrowable(requireActivity(), throwable),
                Color.RED, false
            )
            ?.show()
    }

    override fun showMessage(@StringRes res: Int) {
        CustomSnackbars.createCustomSnackbars(mRecyclerView)?.setDurationSnack(Snackbar.LENGTH_LONG)
            ?.defaultSnack(res, false)?.show()
    }

    private val requestPhotoUpdate = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && (result.data
                ?: return@registerForActivityResult)
                .extras != null
        ) {
            lazyPresenter {
                result.data?.extras?.let {
                    val p = ParcelNative.fromNative(it.getLong(Extra.PTR))
                        .readParcelableList(Photo.NativeCreator)
                        ?.get(
                            it.getInt(
                                Extra.POSITION
                            )
                        ) ?: return@lazyPresenter

                    scrollTo(p.getObjectId(), p.ownerId)
                } ?: return@lazyPresenter
            }
        }
    }

    override fun displayGalleryUnSafe(
        accountId: Long,
        parcelNativePointer: Long,
        position: Int,
        reversed: Boolean
    ) {
        PlaceFactory.getPhotoAlbumGalleryPlace(
            accountId,
            -311,
            accountId,
            parcelNativePointer,
            position,
            true,
            reversed
        )
            .setActivityResultLauncher(
                requestPhotoUpdate
            ).tryOpenWith(requireActivity())
    }

    override fun displayVideo(video: Video) {
        PlaceFactory.getVkInternalPlayerPlace(video, InternalVideoSize.SIZE_720, true)
            .tryOpenWith(requireActivity())
    }

    override fun startPlayAudios(accountId: Long, audios: ArrayList<Audio>, position: Int) {
        MusicPlaybackService.startForPlayList(requireActivity(), audios, position)
        if (!Settings.get().main().isShow_mini_player)
            getPlayerPlace(accountId)
                .tryOpenWith(requireActivity())
    }

    override fun notifyAllChanged() {
        musicButton?.show()
        mAdapter?.notifyDataSetChanged()
    }

    override fun notifyItemRemoved(index: Int) {
        mAdapter?.notifyItemRemoved(index)
    }

    override fun updatePathString(file: String?) {
        if (file.isNullOrEmpty()) {
            tvCurrentDir?.setText(R.string.root_dir)
        } else {
            tvCurrentDir?.text = file
        }
        if (requireActivity() is UpdatableNavigation) {
            (requireActivity() as UpdatableNavigation).onUpdateNavigation()
        }
    }

    override fun restoreScroll(scroll: Parcelable, appVerticalOffset: Int) {
        mLayoutManager?.onRestoreInstanceState(scroll)
        val layoutParams = appBarView?.layoutParams as? CoordinatorLayout.LayoutParams
        val behavior = layoutParams?.behavior as? AppBarLayout.Behavior
        behavior?.setTopAndBottomOffset(
            appVerticalOffset
        )
    }

    override fun onScrollTo(pos: Int) {
        mLayoutManager?.scrollToPosition(pos)
    }

    override fun notifyItemChanged(pos: Int) {
        mAdapter?.notifyItemChanged(pos)
    }

    override fun onDownloadAudioNextStage() {
        if (!hasReadWriteStoragePermission(requireActivity())) {
            requestWritePermission.launch()
            return
        }
        presenter?.continueDownload()
    }

    override fun onStartAudioDownload(
        accountId: Long,
        position: Int,
        audio: Audio
    ) {
        audio.downloadIndicator = 1
        notifyItemChanged(position)
        val ret = doDownloadAudio(
            requireActivity(),
            audio,
            accountId,
            Force = false,
            isLocal = false
        )
        when (ret) {
            0 -> customToast?.showToastBottom(R.string.saved_audio)

            1, 2 -> {
                CustomSnackbars.createCustomSnackbars(view)
                    ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                    ?.themedSnack(if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc)
                    ?.setAction(
                        R.string.button_yes
                    ) {
                        doDownloadAudio(
                            requireActivity(),
                            audio,
                            accountId,
                            Force = true,
                            isLocal = false
                        )
                    }
                    ?.show()
            }

            else -> {
                audio.downloadIndicator = 0
                notifyItemChanged(position)
                customToast?.showToastError(R.string.error_audio)
            }
        }
    }

    override fun showSuccess() {
        CustomSnackbars.createCustomSnackbars(view)
            ?.setDurationSnack(Snackbar.LENGTH_LONG)
            ?.coloredSnack(
                R.string.success,
                "#48BE2D".toColor(), false
            )
            ?.show()
    }

    override fun displayEditFileNameDialog(
        hash: String,
        fileName: String
    ) {
        val root =
            View.inflate(requireActivity(), R.layout.entry_file_name, null)
        root.findViewById<TextInputEditText>(R.id.edit_file_name).setText(fileName)
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.change_name)
            .setCancelable(true)
            .setView(root)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                val newFileName =
                    root.findViewById<TextInputEditText>(R.id.edit_file_name).text?.toString()
                        ?.trim()
                if (newFileName.isNullOrEmpty()) {
                    return@setPositiveButton
                }
                presenter?.firEditFileName(hash, newFileName)
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    override fun doAudioMenu(accountId: Long, position: Int, audio: FileRemote) {
        val t = Audio()
        t.setId(audio.id)
        t.setOwnerId(audio.owner_Id)
        t.setUrl(audio.url)
        t.setThumb_image_big(audio.preview_url)
        t.setThumb_image_very_big(audio.preview_url)
        t.setThumb_image_little(audio.preview_url)
        t.setDuration(audio.size.toInt())

        var TrackName: String = audio.file_name?.replace(".mp3", "") ?: ""
        val Artist: String
        val arr = TrackName.split(Regex(" - ")).toTypedArray()
        if (arr.size > 1) {
            Artist = arr[0]
            TrackName = TrackName.replace("$Artist - ", "")
        } else {
            Artist = ""
        }
        t.setIsLocal()
        t.setArtist(Artist)
        t.setTitle(TrackName)
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                FileLocalServerOption.play_item_audio,
                getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        if (MusicPlaybackController.canPlayAfterCurrent()) {
            menus.add(
                OptionRequest(
                    FileLocalServerOption.play_item_after_current_audio,
                    getString(R.string.play_audio_after_current),
                    R.drawable.play_next,
                    false
                )
            )
        }
        menus.add(
            OptionRequest(
                FileLocalServerOption.save_item,
                getString(R.string.download),
                R.drawable.save,
                true
            )
        )
        menus.add(
            OptionRequest(
                FileLocalServerOption.update_time_item,
                getString(R.string.update_time),
                R.drawable.ic_recent,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileLocalServerOption.edit_item,
                getString(R.string.edit),
                R.drawable.about_writed,
                true
            )
        )
        menus.add(
            OptionRequest(
                FileLocalServerOption.delete_item,
                getString(R.string.delete),
                R.drawable.ic_outline_delete,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileLocalServerOption.upload_item_audio,
                getString(R.string.upload),
                R.drawable.web,
                true
            )
        )
        menus.header(
            audio.file_name,
            R.drawable.song,
            t.thumb_image_big
        )
        menus.columns(2)
        menus.show(
            childFragmentManager,
            "audio_options", { _, option ->
                notifyItemChanged(position)
                when (option.id) {
                    FileLocalServerOption.play_item_after_current_audio -> MusicPlaybackController.playAfterCurrent(
                        t
                    )

                    FileLocalServerOption.save_item -> {
                        presenter?.firePrepareDownload(position, t)
                    }

                    FileLocalServerOption.upload_item_audio -> {
                        presenter?.fireUploadAudio(t)
                    }

                    FileLocalServerOption.delete_item -> {
                        MaterialAlertDialogBuilder(
                            requireActivity()
                        )
                            .setMessage(R.string.do_delete)
                            .setTitle(R.string.confirmation)
                            .setCancelable(true)
                            .setPositiveButton(R.string.button_yes) { _, _ ->
                                presenter?.fireDeleteFile(position, audio)
                            }
                            .setNegativeButton(R.string.button_cancel, null)
                            .show()
                    }

                    FileLocalServerOption.update_time_item -> {
                        presenter?.fireUpdateTime(audio)
                    }

                    FileLocalServerOption.edit_item -> {
                        presenter?.firePrepareEditFileName(audio)
                    }

                    FileLocalServerOption.play_item_audio -> {
                        presenter?.onClickFile(audio)
                    }

                    else -> {}
                }
            }, {
                notifyItemChanged(position)
            })
    }

    override fun doFileMenu(accountId: Long, position: Int, file: FileRemote) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                FileLocalServerOption.update_time_item,
                getString(R.string.update_time),
                R.drawable.ic_recent,
                false
            )
        )
        menus.add(
            OptionRequest(
                FileLocalServerOption.edit_item,
                getString(R.string.edit),
                R.drawable.about_writed,
                true
            )
        )
        menus.add(
            OptionRequest(
                FileLocalServerOption.delete_item,
                getString(R.string.delete),
                R.drawable.ic_outline_delete,
                false
            )
        )
        menus.header(
            file.file_name,
            R.drawable.file,
            file.preview_url
        )
        menus.columns(2)
        menus.show(
            childFragmentManager,
            "file_options", { _, option ->
                notifyItemChanged(position)
                when (option.id) {
                    FileLocalServerOption.delete_item -> {
                        MaterialAlertDialogBuilder(
                            requireActivity()
                        )
                            .setMessage(R.string.do_delete)
                            .setTitle(R.string.confirmation)
                            .setCancelable(true)
                            .setPositiveButton(R.string.button_yes) { _, _ ->
                                presenter?.fireDeleteFile(position, file)
                            }
                            .setNegativeButton(R.string.button_cancel, null)
                            .show()
                    }

                    FileLocalServerOption.update_time_item -> {
                        presenter?.fireUpdateTime(file)
                    }

                    FileLocalServerOption.edit_item -> {
                        presenter?.firePrepareEditFileName(file)
                    }

                    else -> {}
                }
            }, {
                notifyItemChanged(position)
            })
    }

    override fun onServiceBindEvent(@PlayerStatus status: Int) {
        mAdapter?.onServiceBindEvent(status)
    }

    override fun onResume() {
        super.onResume()
        val actionBar = ActivityUtils.supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.files_tab_title)
            actionBar.subtitle = null
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .build()
            .apply(requireActivity())
    }

    companion object {
        fun newInstance(accountId: Long): FileManagerRemoteFragment {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            val fragment = FileManagerRemoteFragment()
            fragment.arguments = args
            return fragment
        }
    }
}