package dev.ragnarok.fenrir.fragment.audio.catalog_v2.sections

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Animatable
import android.view.ContextMenu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso3.Transformation
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity
import dev.ragnarok.fenrir.api.exceptions.ApiException
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.fragment.audio.catalog_v2.sections.holders.IViewHolder
import dev.ragnarok.fenrir.fragment.base.AttachmentsViewBinder
import dev.ragnarok.fenrir.fragment.base.RecyclerBindableAdapter
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.media.music.MusicPlaybackController
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.currentAudio
import dev.ragnarok.fenrir.media.music.MusicPlaybackService
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.AbsModelType
import dev.ragnarok.fenrir.model.Article
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.AudioArtist
import dev.ragnarok.fenrir.model.AudioPlaylist
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.model.Market
import dev.ragnarok.fenrir.model.MarketAlbum
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.Narratives
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoAlbum
import dev.ragnarok.fenrir.model.Poll
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.model.Story
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.model.WallReply
import dev.ragnarok.fenrir.model.WikiPage
import dev.ragnarok.fenrir.model.catalog_v2_audio.CatalogV2Block
import dev.ragnarok.fenrir.model.catalog_v2_audio.CatalogV2Layout
import dev.ragnarok.fenrir.model.catalog_v2_audio.CatalogV2Layout.CATALOG_V2_HOLDER
import dev.ragnarok.fenrir.model.catalog_v2_audio.CatalogV2RecommendationPlaylist
import dev.ragnarok.fenrir.model.menu.options.AudioOption
import dev.ragnarok.fenrir.module.StringHash
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.picasso.transforms.ImageHelper
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toColor
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.AppTextUtils
import dev.ragnarok.fenrir.util.DownloadWorkUtils
import dev.ragnarok.fenrir.util.Mp3InfoHelper
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.hls.M3U8
import dev.ragnarok.fenrir.util.toast.CustomSnackbars
import dev.ragnarok.fenrir.util.toast.CustomToast
import dev.ragnarok.fenrir.view.AudioContainer
import dev.ragnarok.fenrir.view.VP2NestedRecyclerView
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter
import dev.ragnarok.fenrir.view.natives.animation.ThorVGLottieView
import java.lang.ref.WeakReference

class CatalogV2SectionAdapter(
    data: MutableList<AbsModel>,
    private val accountId: Long,
    context: Context
) : RecyclerBindableAdapter<AbsModel, IViewHolder>(data) {
    private var clickListener: ClickListener? = null
    private var disposable = CompositeJob()
    private var mPlayerDisposable = CancelableJob()
    private val mAudioInteractor: IAudioInteractor = InteractorFactory.audio

    private val isLongPressDownload: Boolean = Settings.get().main().isUse_long_click_download
    private val isAudioRoundIcon: Boolean = Settings.get().main().isAudio_round_icon
    private var currAudio: Audio? = currentAudio
    private val isDark: Boolean = Settings.get().ui().isDarkModeEnabled(context)
    private var recyclerView: RecyclerView? = null

    private var currentListContentType: String? = null

    fun updateContentType(listContentType: String?) {
        currentListContentType = listContentType
    }

    companion object {
        val poolCatalogV2Section: RecyclerView.RecycledViewPool by lazy {
            RecyclerView.RecycledViewPool()
        }
    }

    override fun viewHolder(view: View, type: Int): IViewHolder {
        return when (type) {
            CATALOG_V2_HOLDER.TYPE_CATALOG_SLIDER, CATALOG_V2_HOLDER.TYPE_CATALOG_LIST, CATALOG_V2_HOLDER.TYPE_CATALOG_TRIPLE_STACKED_SLIDER -> {
                ItemViewHolder(
                    view,
                    type
                )
            }

            AbsModelType.MODEL_AUDIO -> {
                AudioHolder(view)
            }

            AbsModelType.MODEL_AUDIO_PLAYLIST -> {
                PlaylistHolder(view)
            }

            AbsModelType.MODEL_CATALOG_V2_RECOMMENDATION_PLAYLIST -> {
                RecommendationPlaylistHolder(view)
            }

            else -> CatalogV2Layout.createHolder(type).create(view)
        }
    }

    override fun layoutId(type: Int): Int {
        return when (type) {
            CATALOG_V2_HOLDER.TYPE_CATALOG_SLIDER, CATALOG_V2_HOLDER.TYPE_CATALOG_LIST, CATALOG_V2_HOLDER.TYPE_CATALOG_TRIPLE_STACKED_SLIDER -> {
                R.layout.item_catalog_v2_content_horizontal
            }

            AbsModelType.MODEL_AUDIO -> {
                R.layout.item_audio
            }

            AbsModelType.MODEL_AUDIO_PLAYLIST -> {
                R.layout.item_catalog_v2_audio_playlist
            }

            AbsModelType.MODEL_CATALOG_V2_RECOMMENDATION_PLAYLIST -> {
                R.layout.item_catalog_v2_playlist_recommended
            }

            else -> CatalogV2Layout.createHolder(type).getLayout()
        }
    }

    override fun getItemType(position: Int): Int {
        if (getItem(position).getModelType() == AbsModelType.MODEL_CATALOG_V2_BLOCK) {
            return (getItem(position) as CatalogV2Block).layout.getViewHolderType()
        }
        return getItem(position).getModelType()
    }

    override fun onBindItemViewHolder(
        viewHolder: IViewHolder,
        position: Int,
        type: Int
    ) {
        viewHolder.bind(position, getItem(position), currentListContentType)
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    interface ClickListener {
        fun onAddPlayList(index: Int, album: AudioPlaylist)
        fun onRequestWritePermissions()
        fun onNext(loading: Boolean)
        fun onError(throwable: Throwable)
    }

    inner class ItemViewHolder(
        itemView: View,
        @param:CATALOG_V2_HOLDER private val type: Int
    ) : IViewHolder(itemView) {
        val list: VP2NestedRecyclerView = itemView.findViewById(R.id.list)

        private inline fun <reified T, reified VH : RecyclerView.ViewHolder> configureAdapterBindable(
            itemDataHolder: CatalogV2Block,
            list: RecyclerView,
            adapter: WeakReference<RecyclerBindableAdapter<T, VH>>
        ) {
            list.addOnScrollListener(object : EndlessRecyclerOnScrollListener(4, 1000, false) {
                override fun onScrollToLastElement() {
                    if (itemDataHolder.next_from.nonNullNoEmpty()) {
                        clickListener?.onNext(true)
                        disposable += mAudioInteractor.getCatalogV2BlockItems(
                            accountId,
                            itemDataHolder.id.orEmpty(),
                            itemDataHolder.next_from
                        )
                            .fromIOToMain({ data ->
                                val s = itemDataHolder.items?.size.orZero()
                                itemDataHolder.update(data)
                                adapter.get()?.notifyItemBindableRangeInserted(
                                    s,
                                    itemDataHolder.items?.size.orZero() - s
                                )
                                clickListener?.onNext(false)
                            }) {
                                if (it.cause !is ApiException || "Not found" != (it.cause as ApiException).error.errorMsg || it !is ApiException || "Not found" != it.error.errorMsg) {
                                    clickListener?.onError(it)
                                } else {
                                    clickListener?.onNext(false)
                                }
                            }
                    }
                }
            })
        }

        private fun createLayoutManager(itemDataHolder: CatalogV2Block) {
            list.visibility = View.VISIBLE
            list.layoutManager = when (type) {
                CATALOG_V2_HOLDER.TYPE_CATALOG_TRIPLE_STACKED_SLIDER -> StaggeredGridLayoutManager(
                    3,
                    StaggeredGridLayoutManager.HORIZONTAL
                )

                CATALOG_V2_HOLDER.TYPE_CATALOG_LIST, CATALOG_V2_HOLDER.TYPE_CATALOG_SLIDER -> LinearLayoutManager(
                    itemView.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

                else -> {
                    throw UnsupportedOperationException()
                }
            }
            PicassoPauseOnScrollListener.addListener(list)
            list.updateUid(itemDataHolder.id?.let { it1 -> StringHash.calculateCRC32(it1) })
        }

        private fun findScrollable(items: ArrayList<AbsModel>): Int {
            for ((i, element) in items.withIndex()) {
                if (element.getModelType() == AbsModelType.MODEL_AUDIO) {
                    val audio = element as Audio
                    if (audio.isAnimationNow) {
                        return i
                    }
                } else if (element.getModelType() == AbsModelType.MODEL_CATALOG_V2_BLOCK) {
                    val block = element as CatalogV2Block
                    if (block.isScroll()) {
                        return i
                    }
                } else if (element.getModelType() == AbsModelType.MODEL_CATALOG_V2_RECOMMENDATION_PLAYLIST) {
                    val block = element as CatalogV2RecommendationPlaylist
                    if (block.isScroll()) {
                        return i
                    }
                }
            }
            return -1
        }

        override fun bind(position: Int, itemDataHolder: AbsModel, listContentType: String?) {
            if (itemDataHolder !is CatalogV2Block) {
                list.visibility = View.GONE
                list.clearOnScrollListeners()
                list.adapter = null
                list.updateUid(-1)
                return
            }
            list.visibility = View.GONE
            list.clearOnScrollListeners()
            list.adapter = null
            list.updateUid(-1)
            itemDataHolder.items?.let {
                val op = findScrollable(it)
                createLayoutManager(itemDataHolder)
                //list.setRecycledViewPool(poolCatalogV2Section)
                val adapter = CatalogV2SectionAdapter(it, accountId, list.context)
                adapter.clickListener = clickListener
                list.adapter = adapter
                configureAdapterBindable(itemDataHolder, list, WeakReference(adapter))
                if (op >= 0) {
                    list.scrollToPosition(
                        op + adapter.headersCount
                    )
                }
            }
        }
    }

    ///////////////////////////////////////SUB_HOLDERS////////////////////////////////////////////////

    ///////////////////////////////////////PLAYLIST_HOLDER////////////////////////////////////////////
    internal fun onDelete(context: Context, index: Int, album: AudioPlaylist) {
        disposable += mAudioInteractor.deletePlaylist(accountId, album.id, album.owner_id)
            .fromIOToMain({
                getItems().removeAt(index)
                notifyItemBindableRemoved(index)
                CustomToast.createCustomToast(context, null)?.showToast(R.string.deleted)
            }) { throwable ->
                clickListener?.onError(
                    throwable
                )
            }
    }

    internal fun onAddPlaylist(context: Context, album: AudioPlaylist) {
        disposable += mAudioInteractor.followPlaylist(
            accountId,
            album.id,
            album.owner_id,
            album.access_key
        )
            .fromIOToMain({
                CustomToast.createCustomToast(context, null)?.showToast(R.string.success)
            }) {
                clickListener?.onError(
                    it
                )
            }
    }

    inner class PlaylistHolder(itemView: View) : IViewHolder(itemView),
        View.OnCreateContextMenuListener {
        val thumbRoot: FrameLayout
        val thumb: ImageView
        val title: TextView
        val year: TextView
        val artist: TextView
        val item_additional_info: ViewGroup
        val item_badge: ViewGroup
        val item_badge_icon: AppCompatImageView
        val item_title_badge: TextView
        val item_subtitle_badge: TextView
        private val playlist_container: View
        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val position = recyclerView?.getChildAdapterPosition(v).orZero()
            val playlist = getItem(position) as AudioPlaylist
            if (accountId == playlist.owner_id) {
                menu.add(0, v.id, 0, R.string.delete)
                    .setOnMenuItemClickListener {
                        onDelete(v.context, position, playlist)
                        true
                    }
            } else {
                menu.add(0, v.id, 0, R.string.save).setOnMenuItemClickListener {
                    onAddPlaylist(v.context, playlist)
                    true
                }
            }
        }

        init {
            itemView.setOnCreateContextMenuListener(this)
            thumbRoot = itemView.findViewById(R.id.item_thumb_root)
            thumb = itemView.findViewById(R.id.item_thumb)
            title = itemView.findViewById(R.id.item_title)
            playlist_container = itemView.findViewById(R.id.playlist_container)
            year = itemView.findViewById(R.id.item_year)
            artist = itemView.findViewById(R.id.item_artist)
            item_additional_info = itemView.findViewById(R.id.item_additional_info)
            item_badge = itemView.findViewById(R.id.item_badge)
            item_badge_icon = itemView.findViewById(R.id.item_badge_icon)
            item_title_badge = itemView.findViewById(R.id.item_title_badge)
            item_subtitle_badge = itemView.findViewById(R.id.item_subtitle_badge)
        }

        override fun bind(position: Int, itemDataHolder: AbsModel, listContentType: String?) {
            if (listContentType == "music_playlists") {
                thumbRoot.layoutParams.width = Utils.dp(95f)
                thumbRoot.layoutParams.height = Utils.dp(95f)
            } else {
                thumbRoot.layoutParams.width = Utils.dp(130f)
                thumbRoot.layoutParams.height = Utils.dp(130f)
            }
            val playlist = itemDataHolder as AudioPlaylist
            if (playlist.thumb_image.nonNullNoEmpty()) ViewUtils.displayAvatar(
                thumb,
                PolyTransformation(),
                playlist.thumb_image,
                Constants.PICASSO_TAG
            ) else thumb.setImageBitmap(
                ImageHelper.getEllipseBitmap(
                    BitmapFactory.decodeResource(
                        thumb.context.resources,
                        if (isDark) R.drawable.generic_audio_nowplaying_dark else R.drawable.generic_audio_nowplaying_light
                    ), 0.1f
                )
            )
            if (playlist.subtitle_badge) {
                item_additional_info.visibility = View.GONE
                item_badge.visibility = View.VISIBLE
                item_badge_icon.visibility = View.VISIBLE
            } else {
                item_additional_info.visibility = View.VISIBLE
                item_badge.visibility = View.GONE
                item_badge_icon.visibility = View.GONE
            }
            item_title_badge.text = playlist.title
            if (playlist.subtitle.isNullOrEmpty()) item_subtitle_badge.visibility =
                View.GONE else {
                item_subtitle_badge.visibility = View.VISIBLE
                item_subtitle_badge.text = playlist.subtitle
            }
            title.text = playlist.title
            if (playlist.artist_name.isNullOrEmpty()) artist.visibility = View.GONE else {
                artist.visibility = View.VISIBLE
                artist.text = playlist.artist_name
            }
            if (playlist.year == 0) year.visibility = View.GONE else {
                year.visibility = View.VISIBLE
                year.text = String.format(Utils.appLocale, "%d", playlist.year)
            }
            playlist_container.setOnClickListener {
                if (playlist.original_access_key
                        .isNullOrEmpty() || playlist.original_id == 0 || playlist.original_owner_id == 0L
                ) PlaceFactory.getAudiosInAlbumPlace(
                    accountId, playlist.owner_id, playlist.id, playlist.access_key
                ).tryOpenWith(playlist_container.context) else PlaceFactory.getAudiosInAlbumPlace(
                    accountId,
                    playlist.original_owner_id,
                    playlist.original_id,
                    playlist.original_access_key
                ).tryOpenWith(playlist_container.context)
            }
        }
    }

    inner class RecommendationPlaylistHolder(itemView: View) : IViewHolder(itemView),
        View.OnCreateContextMenuListener {
        val contentRoot: MaterialCardView = itemView.findViewById(R.id.root_container)
        val percentage: TextView = itemView.findViewById(R.id.percentage)
        private val percentageTitle: TextView = itemView.findViewById(R.id.percentageTitle)
        val title: TextView
        val subtitle: TextView
        val audios: AudioContainer
        private val playlist_container: View = itemView.findViewById(R.id.playlist_container)
        override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val position = recyclerView?.getChildAdapterPosition(itemView).orZero()
            val recPlaylist = getItem(position) as CatalogV2RecommendationPlaylist
            if (accountId == recPlaylist.owner_id) {
                menu.add(0, itemView.id, 0, R.string.delete)
                    .setOnMenuItemClickListener {
                        onDelete(v.context, position, recPlaylist.getPlaylist())
                        true
                    }
            } else {
                menu.add(0, itemView.id, 0, R.string.save).setOnMenuItemClickListener {
                    onAddPlaylist(v.context, recPlaylist.getPlaylist())
                    true
                }

                menu.add(0, itemView.id, 0, R.string.goto_user).setOnMenuItemClickListener {
                    recPlaylist.owner
                        ?.let { it1 ->
                            PlaceFactory.getOwnerWallPlace(accountId, it1).tryOpenWith(v.context)
                        }
                    true
                }
            }
        }

        init {
            playlist_container.setOnCreateContextMenuListener(this)
            title = itemView.findViewById(R.id.title)
            subtitle = itemView.findViewById(R.id.subtitle)
            audios = itemView.findViewById(R.id.audio_container)
        }

        @SuppressLint("SetTextI18n")
        override fun bind(position: Int, itemDataHolder: AbsModel, listContentType: String?) {
            if (listContentType == "music_recommended_playlists") {
                contentRoot.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                if (contentRoot.layoutParams is RecyclerView.LayoutParams) {
                    (contentRoot.layoutParams as RecyclerView.LayoutParams).setMargins(
                        Utils.dp(16f),
                        Utils.dp(8f),
                        Utils.dp(16f),
                        Utils.dp(8f)
                    )
                }
            } else {
                contentRoot.layoutParams.width = Utils.dp(286f)
                if (contentRoot.layoutParams is RecyclerView.LayoutParams) {
                    (contentRoot.layoutParams as RecyclerView.LayoutParams).setMargins(
                        Utils.dp(4.5f),
                        Utils.dp(4.5f),
                        Utils.dp(4.5f),
                        Utils.dp(4.5f)
                    )
                }
            }

            val recPlaylist = itemDataHolder as CatalogV2RecommendationPlaylist
            val playlist = recPlaylist.getPlaylist()
            playlist_container.setOnClickListener {
                if (playlist.original_access_key
                        .isNullOrEmpty() || playlist.original_id == 0 || playlist.original_owner_id == 0L
                ) PlaceFactory.getAudiosInAlbumPlace(
                    accountId, playlist.owner_id, playlist.id, playlist.access_key
                ).tryOpenWith(playlist_container.context) else PlaceFactory.getAudiosInAlbumPlace(
                    accountId,
                    playlist.original_owner_id,
                    playlist.original_id,
                    playlist.original_access_key
                ).tryOpenWith(playlist_container.context)
            }
            percentage.text = ((recPlaylist.percentage * 100).toInt()).toString() + "%"
            percentageTitle.text = recPlaylist.percentage_title
            title.text = playlist.title
            title.setTextColor(recPlaylist.color)
            subtitle.text = recPlaylist.owner?.fullName
            audios.displayAudios(recPlaylist.audios, object :
                AttachmentsViewBinder.OnAttachmentsActionCallback {
                override fun onPollOpen(poll: Poll) {

                }

                override fun onVideoOpen(video: Video) {

                }

                override fun onVideoPlay(video: Video) {

                }

                override fun onAudioPlay(
                    position: Int,
                    audios: ArrayList<Audio>,
                    holderPosition: Int?
                ) {
                    MusicPlaybackService.startForPlayList(
                        contentRoot.context,
                        audios,
                        position
                    )
                    if (!Settings.get().main().isShow_mini_player) PlaceFactory.getPlayerPlace(
                        accountId
                    ).tryOpenWith(contentRoot.context)
                }

                override fun onForwardMessagesOpen(messages: ArrayList<Message>) {}

                override fun onOpenOwner(ownerId: Long) {}

                override fun onGoToMessagesLookup(message: Message) {}

                override fun onDocPreviewOpen(document: Document) {}

                override fun onPostOpen(post: Post) {}

                override fun onLinkOpen(link: Link) {}

                override fun onUrlOpen(url: String) {}

                override fun onFaveArticle(article: Article) {}

                override fun onShareArticle(article: Article) {}

                override fun onWikiPageOpen(page: WikiPage) {}

                override fun onPhotosOpen(photos: ArrayList<Photo>, index: Int, refresh: Boolean) {}

                override fun onUrlPhotoOpen(url: String, prefix: String, photo_prefix: String) {
                    PlaceFactory.getSingleURLPhotoPlace(url, prefix, photo_prefix)
                        .tryOpenWith(contentRoot.context)
                }

                override fun onStoryOpen(story: Story) {}

                override fun onNarrativeOpen(narratives: Narratives) {}

                override fun onWallReplyOpen(reply: WallReply) {}

                override fun onAudioPlaylistOpen(playlist: AudioPlaylist) {}

                override fun onPhotoAlbumOpen(album: PhotoAlbum) {}

                override fun onMarketAlbumOpen(market_album: MarketAlbum) {}

                override fun onMarketOpen(market: Market) {}

                override fun onArtistOpen(artist: AudioArtist) {}

                override fun onRequestWritePermissions() {
                    clickListener?.onRequestWritePermissions()
                }

            }, getItemRawPosition(bindingAdapterPosition))
        }
    }

    ///////////////////////////////////////AUDIO_HOLDER////////////////////////////////////////////////
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        mPlayerDisposable += MusicPlaybackController.observeServiceBinding()
            .sharedFlowToMain { onServiceBindEvent(it) }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
        disposable.cancel()
        clickListener?.onNext(false)
        mPlayerDisposable.cancel()
    }

    private fun deleteTrack(context: Context, accountId: Long, audio: Audio, position: Int) {
        disposable += mAudioInteractor.delete(accountId, audio.id, audio.ownerId)
            .fromIOToMain(
                {
                    getItems().removeAt(position)
                    notifyItemBindableRemoved(position)
                    CustomToast.createCustomToast(context, null)?.showToast(R.string.deleted)
                }) { t -> CustomToast.createCustomToast(context, null)?.showToastThrowable(t) }
    }

    private fun addTrack(context: Context, accountId: Long, audio: Audio) {
        disposable += mAudioInteractor.add(accountId, audio, null).fromIOToMain(
            { CustomToast.createCustomToast(context, null)?.showToast(R.string.added) }) { t ->
            CustomToast.createCustomToast(context, null)?.showToastThrowable(t)
        }
    }

    private fun getMp3AndBitrate(context: Context, accountId: Long, audio: Audio) {
        if (audio.needRefresh()) {
            disposable += mAudioInteractor.getById(
                accountId,
                listOf(audio)
            ).fromIOToMain({ t -> getBitrate(context, t[0]) }) {
                getBitrate(
                    context,
                    audio
                )
            }
        } else {
            getBitrate(context, audio)
        }
    }

    private fun getBitrate(context: Context, audio: Audio) {
        val pUrl = audio.url
        if (pUrl.isNullOrEmpty()) {
            return
        }
        disposable += if (audio.isHLS) {
            M3U8(pUrl).length.fromIOToMain(
                { r ->
                    CustomToast.createCustomToast(context, null)?.showToast(
                        Mp3InfoHelper.getBitrate(
                            context,
                            audio.duration,
                            r
                        )
                    )
                }
            ) { e -> CustomToast.createCustomToast(context, null)?.showToastThrowable(e) }
        } else {
            Mp3InfoHelper.getLength(pUrl).fromIOToMain(
                { r ->
                    CustomToast.createCustomToast(context, null)?.showToast(
                        Mp3InfoHelper.getBitrate(
                            context,
                            audio.duration,
                            r
                        )
                    )
                }
            ) { e -> CustomToast.createCustomToast(context, null)?.showToastThrowable(e) }
        }
    }

    private fun getLyrics(context: Context, audio: Audio) {
        disposable += mAudioInteractor.getLyrics(accountId, audio)
            .fromIOToMain({ t ->
                onAudioLyricsReceived(
                    context,
                    t,
                    audio
                )
            }) { t -> CustomToast.createCustomToast(context, null)?.showToastThrowable(t) }
    }

    private fun onAudioLyricsReceived(context: Context, lyricText: String, audio: Audio) {
        val title = audio.artistAndTitle
        MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.dir_song)
            .setMessage(lyricText)
            .setTitle(title)
            .setPositiveButton(R.string.button_ok, null)
            .setNeutralButton(R.string.copy_text) { _, _ ->
                val clipboard = context.getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager?
                val clip = ClipData.newPlainText("response", lyricText)
                clipboard?.setPrimaryClip(clip)
                CustomToast.createCustomToast(context, null)
                    ?.showToast(R.string.copied_to_clipboard)
            }
            .setCancelable(true)
            .show()
    }

    private fun onServiceBindEvent(@PlayerStatus status: Int) {
        when (status) {
            PlayerStatus.UPDATE_METADATA, PlayerStatus.SERVICE_KILLED, PlayerStatus.UPDATE_PLAY_PAUSE -> {
                val tmpCurrAudio = currAudio
                currAudio = currentAudio

                if (status != PlayerStatus.UPDATE_METADATA || tmpCurrAudio != currAudio) {
                    updateAudio(tmpCurrAudio)
                    updateAudio(currAudio)
                }
            }

            PlayerStatus.REPEAT_MODE_CHANGED, PlayerStatus.SHUFFLE_MODE_CHANGED, PlayerStatus.UPDATE_PLAY_LIST, PlayerStatus.UPDATE_TRACK_INFO -> {}
        }
    }

    private fun updateAudio(audio: Audio?) {
        audio ?: return
        var pos = indexOf(0, audio)
        while (pos != -1) {
            notifyItemBindableRangeChanged(pos, 1)
            pos = indexOf(pos + 1, audio)
        }
    }

    @get:DrawableRes
    internal val audioCoverSimple: Int
        get() = if (isAudioRoundIcon) R.drawable.audio_button else R.drawable.audio_button_material

    internal val transformCover: Transformation by lazy {
        if (isAudioRoundIcon) RoundTransformation() else PolyTransformation()
    }

    internal fun updateAudioStatus(holder: AudioHolder, audio: Audio) {
        if (audio != currAudio && (holder.visual.drawable is Animatable || holder.visual.drawable == null)) {
            holder.visual.setImageResource(audio.songIcon)
            holder.playCover.clearColorFilter()
            return
        }
        when (MusicPlaybackController.playerStatus()) {
            1 -> {
                Utils.doWavesLottie(holder.visual, true)
                holder.playCover.setColorFilter("#44000000".toColor())
            }

            2 -> {
                Utils.doWavesLottie(holder.visual, false)
                holder.playCover.setColorFilter("#44000000".toColor())
            }
        }
    }

    internal fun updateDownloadState(holder: AudioHolder, audio: Audio) {
        if (audio.downloadIndicator == 2) {
            holder.saved.setImageResource(R.drawable.remote_cloud)
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorSecondary(holder.saved.context))
        } else {
            holder.saved.setImageResource(R.drawable.save)
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(holder.saved.context))
        }
        holder.saved.visibility =
            if (audio.downloadIndicator != 0) View.VISIBLE else View.GONE
    }

    private fun fireEditTrack(context: Context, position: Int, audio: Audio) {
        val root = View.inflate(context, R.layout.entry_audio_info, null)
        root.findViewById<TextInputEditText>(R.id.edit_artist).setText(audio.artist)
        root.findViewById<TextInputEditText>(R.id.edit_title).setText(audio.title)
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.enter_audio_info)
            .setCancelable(true)
            .setView(root)
            .setPositiveButton(R.string.button_ok) { _, _ ->
                val artist =
                    root.findViewById<TextInputEditText>(R.id.edit_artist).text?.toString()
                val title =
                    root.findViewById<TextInputEditText>(R.id.edit_title).text?.toString()
                disposable += mAudioInteractor.edit(
                    accountId,
                    audio.ownerId,
                    audio.id,
                    artist,
                    title
                ).fromIOToMain({
                    audio.setTitle(title).setArtist(artist)
                    notifyItemBindableChanged(position)
                }) { t ->
                    clickListener?.onError(t)
                }
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    internal fun doMenu(holder: AudioHolder, position: Int, view: View, audio: Audio) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                AudioOption.play_item_audio,
                holder.play.context.getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        if (MusicPlaybackController.canPlayAfterCurrent()) {
            menus.add(
                OptionRequest(
                    AudioOption.play_item_after_current_audio,
                    holder.play.context.getString(R.string.play_audio_after_current),
                    R.drawable.play_next,
                    false
                )
            )
        }
        if (audio.ownerId != accountId) {
            menus.add(
                OptionRequest(
                    AudioOption.add_item_audio,
                    holder.play.context.getString(R.string.action_add),
                    R.drawable.list_add,
                    true
                )
            )
            menus.add(
                OptionRequest(
                    AudioOption.add_and_download_button,
                    holder.play.context.getString(R.string.add_and_download_button),
                    R.drawable.add_download,
                    false
                )
            )
        } else {
            menus.add(
                OptionRequest(
                    AudioOption.add_item_audio,
                    holder.play.context.getString(R.string.delete),
                    R.drawable.ic_outline_delete,
                    true
                )
            )
            menus.add(
                OptionRequest(
                    AudioOption.edit_track,
                    holder.play.context.getString(R.string.edit),
                    R.drawable.about_writed,
                    true
                )
            )
        }
        menus.add(
            OptionRequest(
                AudioOption.share_button,
                holder.play.context.getString(R.string.share),
                R.drawable.ic_outline_share,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.save_item_audio,
                holder.play.context.getString(R.string.save),
                R.drawable.save,
                true
            )
        )
        if (audio.albumId != 0) menus.add(
            OptionRequest(
                AudioOption.open_album,
                holder.play.context.getString(R.string.open_album),
                R.drawable.audio_album,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.get_recommendation_by_audio,
                holder.play.context.getString(R.string.get_recommendation_by_audio),
                R.drawable.music_mic,
                false
            )
        )
        if (audio.main_artists.nonNullNoEmpty()) menus.add(
            OptionRequest(
                AudioOption.goto_artist,
                holder.play.context.getString(R.string.audio_goto_artist),
                R.drawable.artist_icon,
                false
            )
        )
        if (audio.lyricsId != 0) menus.add(
            OptionRequest(
                AudioOption.get_lyrics_menu,
                holder.play.context.getString(R.string.get_lyrics_menu),
                R.drawable.lyric,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.bitrate_item_audio,
                holder.play.context.getString(R.string.get_bitrate),
                R.drawable.high_quality,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.search_by_artist,
                holder.play.context.getString(R.string.search_by_artist),
                R.drawable.magnify,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.copy_url,
                holder.play.context.getString(R.string.copy_url),
                R.drawable.content_copy,
                false
            )
        )
        menus.header(
            Utils.firstNonEmptyString(audio.artist, " ") + " - " + audio.title,
            R.drawable.song,
            audio.thumb_image_little
        )
        menus.columns(2)
        menus.show(
            (holder.play.context as FragmentActivity).supportFragmentManager,
            "audio_options"
        ) { _, option ->
            when (option.id) {
                AudioOption.play_item_audio -> {
                    doPlay(holder.play.context, position, audio)
                }

                AudioOption.play_item_after_current_audio -> MusicPlaybackController.playAfterCurrent(
                    audio
                )

                AudioOption.edit_track -> {
                    fireEditTrack(holder.play.context, position, audio)
                }

                AudioOption.share_button -> SendAttachmentsActivity.startForSendAttachments(
                    holder.play.context,
                    accountId,
                    audio
                )

                AudioOption.search_by_artist -> PlaceFactory.getSingleTabSearchPlace(
                    accountId,
                    SearchContentType.AUDIOS,
                    AudioSearchCriteria(
                        audio.artist, by_artist = true, in_main_page = false
                    )
                ).tryOpenWith(holder.play.context)

                AudioOption.get_lyrics_menu -> getLyrics(holder.play.context, audio)
                AudioOption.get_recommendation_by_audio -> PlaceFactory.SearchByAudioPlace(
                    accountId, audio.ownerId, audio.id
                ).tryOpenWith(holder.play.context)

                AudioOption.open_album -> PlaceFactory.getAudiosInAlbumPlace(
                    accountId,
                    audio.album_owner_id,
                    audio.albumId,
                    audio.album_access_key
                ).tryOpenWith(holder.play.context)

                AudioOption.copy_url -> {
                    val clipboard =
                        holder.play.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("response", audio.url)
                    clipboard?.setPrimaryClip(clip)
                    CustomToast.createCustomToast(holder.play.context, null)
                        ?.showToast(R.string.copied)
                }

                AudioOption.add_item_audio -> {
                    val myAudio = audio.ownerId == accountId
                    if (myAudio) {
                        deleteTrack(holder.play.context, accountId, audio, position)
                    } else {
                        addTrack(holder.play.context, accountId, audio)
                    }
                }

                AudioOption.add_and_download_button -> {
                    if (audio.ownerId != accountId) {
                        addTrack(holder.play.context, accountId, audio)
                    }
                    if (!AppPerms.hasReadWriteStoragePermission(holder.play.context)) {
                        clickListener?.onRequestWritePermissions()
                        return@show
                    }
                    audio.downloadIndicator = 1
                    updateDownloadState(holder, audio)
                    val ret = DownloadWorkUtils.doDownloadAudio(
                        holder.play.context,
                        audio,
                        accountId,
                        Force = false,
                        isLocal = false
                    )
                    when (ret) {
                        0 -> CustomToast.createCustomToast(holder.play.context, null)
                            ?.showToastBottom(R.string.saved_audio)

                        1, 2 -> {
                            CustomSnackbars.createCustomSnackbars(view)
                                ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                                ?.themedSnack(if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc)
                                ?.setAction(
                                    R.string.button_yes
                                ) {
                                    DownloadWorkUtils.doDownloadAudio(
                                        holder.play.context,
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
                            updateDownloadState(holder, audio)
                            CustomToast.createCustomToast(holder.play.context, null)
                                ?.showToastBottom(R.string.error_audio)
                        }
                    }
                }

                AudioOption.save_item_audio -> {
                    if (!AppPerms.hasReadWriteStoragePermission(holder.play.context)) {
                        clickListener?.onRequestWritePermissions()
                        return@show
                    }
                    audio.downloadIndicator = 1
                    updateDownloadState(holder, audio)
                    val ret = DownloadWorkUtils.doDownloadAudio(
                        holder.play.context,
                        audio,
                        accountId,
                        Force = false,
                        isLocal = false
                    )
                    when (ret) {
                        0 -> CustomToast.createCustomToast(holder.play.context, null)
                            ?.showToastBottom(R.string.saved_audio)

                        1, 2 -> {
                            CustomSnackbars.createCustomSnackbars(view)
                                ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                                ?.themedSnack(if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc)
                                ?.setAction(
                                    R.string.button_yes
                                ) {
                                    DownloadWorkUtils.doDownloadAudio(
                                        holder.play.context,
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
                            updateDownloadState(holder, audio)
                            CustomToast.createCustomToast(holder.play.context, null)
                                ?.showToastBottom(R.string.error_audio)
                        }
                    }
                }

                AudioOption.bitrate_item_audio -> getMp3AndBitrate(
                    holder.play.context,
                    accountId, audio
                )

                AudioOption.goto_artist -> {
                    val artists = Utils.getArrayFromHash(
                        audio.main_artists
                    )
                    if (audio.main_artists?.keys?.size.orZero() > 1) {
                        MaterialAlertDialogBuilder(holder.play.context)
                            .setItems(artists[1]) { _, which ->
                                PlaceFactory.getArtistPlace(
                                    accountId,
                                    artists[0][which]
                                ).tryOpenWith(holder.play.context)
                            }
                            .show()
                    } else {
                        PlaceFactory.getArtistPlace(
                            accountId,
                            artists[0][0]
                        ).tryOpenWith(holder.play.context)
                    }
                }
            }
        }
    }

    internal fun doPlay(context: Context, position: Int, audio: Audio) {
        if (MusicPlaybackController.isNowPlayingOrPreparingOrPaused(audio)) {
            if (!Settings.get().main().isUse_stop_audio) {
                MusicPlaybackController.playOrPause()
            } else {
                MusicPlaybackController.stop()
            }
        } else {
            val op = ArrayList<Audio>(MusicPlaybackService.MAX_QUEUE_SIZE)
            val half = MusicPlaybackService.MAX_QUEUE_SIZE / 2
            val st = (position - half).coerceAtLeast(0)
            val en = (position + half).coerceAtMost(realItemCount)
            for (i in st until en) {
                if (getItem(i).getModelType() == AbsModelType.MODEL_AUDIO) {
                    op.add(getItem(i) as Audio)
                }
            }
            MusicPlaybackService.startForPlayList(
                context, op, op.indexOf(audio)
            )
            if (!Settings.get().main().isShow_mini_player) PlaceFactory.getPlayerPlace(
                accountId
            ).tryOpenWith(context)
        }
    }

    inner class AudioHolder(itemView: View) : IViewHolder(itemView) {
        val artist: TextView = itemView.findViewById(R.id.dialog_title)
        val title: TextView = itemView.findViewById(R.id.dialog_message)
        val play: View = itemView.findViewById(R.id.item_audio_play)
        val playCover: ImageView = itemView.findViewById(R.id.item_audio_play_cover)
        val visual: ThorVGLottieView = itemView.findViewById(R.id.item_audio_visual)
        val time: TextView = itemView.findViewById(R.id.item_audio_time)
        val saved: ImageView = itemView.findViewById(R.id.saved)
        private val lyric: ImageView = itemView.findViewById(R.id.lyric)
        val my: ImageView = itemView.findViewById(R.id.my)
        private val quality: ImageView = itemView.findViewById(R.id.quality)
        private val trackOption: View = itemView.findViewById(R.id.track_option)
        val selectionView: MaterialCardView = itemView.findViewById(R.id.item_audio_selection)
        private val isSelectedView: MaterialCardView =
            itemView.findViewById(R.id.item_audio_select_add)
        private val animationAdapter: Animator.AnimatorListener =
            object : WeakViewAnimatorAdapter<View>(selectionView) {
                override fun onAnimationEnd(view: View) {
                    view.visibility = View.GONE
                }

                override fun onAnimationStart(view: View) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(view: View) {
                    view.visibility = View.GONE
                }
            }
        var animator: ObjectAnimator? = null
        private fun startSelectionAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorPrimary(selectionView.context))
            selectionView.alpha = 0.5f
            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f)
            animator?.duration = 1500
            animator?.addListener(animationAdapter)
            animator?.start()
        }

        private fun startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(selectionView.context))
            selectionView.alpha = 0.5f
            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f)
            animator?.duration = 500
            animator?.addListener(animationAdapter)
            animator?.start()
        }

        private fun cancelSelectionAnimation() {
            animator?.cancel()
            animator = null
            selectionView.visibility = View.INVISIBLE
        }

        override fun bind(position: Int, itemDataHolder: AbsModel, listContentType: String?) {
            val audio = itemDataHolder as Audio
            cancelSelectionAnimation()
            if (audio.isAnimationNow) {
                startSelectionAnimation()
                audio.isAnimationNow = false
            }
            artist.text = audio.artist
            if (!audio.isLocal && !audio.isLocalServer && Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID && !audio.isHLS) {
                quality.visibility = View.VISIBLE
                if (audio.isHq) {
                    quality.setImageResource(R.drawable.high_quality)
                } else {
                    quality.setImageResource(R.drawable.low_quality)
                }
            } else {
                quality.visibility = View.GONE
            }
            title.text = audio.title
            if (audio.duration <= 0) time.visibility = View.INVISIBLE else {
                time.visibility = View.VISIBLE
                time.text = AppTextUtils.getDurationString(audio.duration)
            }
            lyric.visibility = if (audio.lyricsId != 0) View.VISIBLE else View.GONE
            isSelectedView.visibility = if (audio.isSelected) View.VISIBLE else View.GONE
            if (audio.isSelected) {
                when {
                    audio.url.isNullOrEmpty() -> {
                        isSelectedView.setCardBackgroundColor("#ff0000".toColor())
                    }

                    DownloadWorkUtils.TrackIsDownloaded(audio) != 0 -> {
                        isSelectedView.setCardBackgroundColor("#00aa00".toColor())
                    }

                    else -> {
                        isSelectedView.setCardBackgroundColor(
                            CurrentTheme.getColorPrimary(
                                isSelectedView.context
                            )
                        )
                    }
                }
            }
            my.visibility =
                if (audio.ownerId == accountId) View.VISIBLE else View.GONE
            saved.visibility = View.GONE
            updateDownloadState(this, audio)
            updateAudioStatus(this, audio)
            if (audio.thumb_image_little.nonNullNoEmpty()) {
                PicassoInstance.with()
                    .load(audio.thumb_image_little)
                    .placeholder(
                        ResourcesCompat.getDrawable(
                            playCover.context.resources,
                            audioCoverSimple,
                            playCover.context.theme
                        ) ?: return
                    )
                    .transform(transformCover)
                    .tag(Constants.PICASSO_TAG)
                    .into(playCover)
            } else {
                PicassoInstance.with().cancelRequest(playCover)
                playCover.setImageResource(audioCoverSimple)
            }
            play.setOnLongClickListener {
                if ((audio.thumb_image_very_big.nonNullNoEmpty()
                            || audio.thumb_image_big.nonNullNoEmpty() || audio.thumb_image_little.nonNullNoEmpty()) && audio.artist.nonNullNoEmpty() && audio.title.nonNullNoEmpty()
                ) {
                    Utils.firstNonEmptyString(
                        audio.thumb_image_very_big,
                        audio.thumb_image_big, audio.thumb_image_little
                    )?.let {
                        audio.artist?.let { it1 ->
                            audio.title?.let { it2 ->
                                PlaceFactory.getSingleURLPhotoPlace(it, it1, it2)
                                    .tryOpenWith(play.context)
                            }
                        }
                    }
                }
                true
            }
            play.setOnClickListener {
                if (Settings.get().main().isRevert_play_audio) {
                    doMenu(this, getItemRawPosition(bindingAdapterPosition), it, audio)
                } else {
                    doPlay(play.context, getItemRawPosition(bindingAdapterPosition), audio)
                }
            }
            if (isLongPressDownload) {
                trackOption.setOnLongClickListener { v: View? ->
                    if (!AppPerms.hasReadWriteStoragePermission(trackOption.context)) {
                        clickListener?.onRequestWritePermissions()
                        return@setOnLongClickListener false
                    }
                    audio.downloadIndicator = 1
                    updateDownloadState(this, audio)
                    val ret = DownloadWorkUtils.doDownloadAudio(
                        trackOption.context,
                        audio,
                        accountId,
                        Force = false,
                        isLocal = false
                    )
                    when (ret) {
                        0 -> CustomToast.createCustomToast(trackOption.context, null)
                            ?.showToastBottom(R.string.saved_audio)

                        1, 2 -> {
                            v?.let {
                                CustomSnackbars.createCustomSnackbars(it)
                                    ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                                    ?.themedSnack(if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc)
                                    ?.setAction(
                                        R.string.button_yes
                                    ) {
                                        DownloadWorkUtils.doDownloadAudio(
                                            trackOption.context,
                                            audio,
                                            accountId,
                                            Force = true,
                                            isLocal = false
                                        )
                                    }
                                    ?.show()
                            }
                        }

                        else -> {
                            audio.downloadIndicator = 0
                            updateDownloadState(this, audio)
                            CustomToast.createCustomToast(trackOption.context, null)
                                ?.showToastBottom(R.string.error_audio)
                        }
                    }
                    true
                }
            }
            trackOption.setOnClickListener {
                cancelSelectionAnimation()
                startSomeAnimation()
                if (Settings.get().main().isRevert_play_audio) {
                    doPlay(trackOption.context, getItemRawPosition(bindingAdapterPosition), audio)
                } else {
                    doMenu(this, getItemRawPosition(bindingAdapterPosition), it, audio)
                }
            }
        }
    }
}