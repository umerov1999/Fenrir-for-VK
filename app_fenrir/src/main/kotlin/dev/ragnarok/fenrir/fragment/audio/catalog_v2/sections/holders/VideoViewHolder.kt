package dev.ragnarok.fenrir.fragment.audio.catalog_v2.sections.holders

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.place.PlaceFactory.getVideoPreviewPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppTextUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.toast.CustomToast
import dev.ragnarok.fenrir.view.VideoServiceIcons.getIconByType
import dev.ragnarok.fenrir.view.natives.animation.AnimatedShapeableImageView
import dev.ragnarok.fenrir.view.natives.animation.AspectRatioAnimatedShapeableImageView

class VideoViewHolder(itemView: View) : IViewHolder(itemView) {
    val card: View = itemView.findViewById(R.id.card_view)
    val image: AspectRatioAnimatedShapeableImageView = itemView.findViewById(R.id.video_image)
    val videoLenght: TextView = itemView.findViewById(R.id.video_lenght)
    val videoService: ImageView = itemView.findViewById(R.id.video_service)
    val title: TextView = itemView.findViewById(R.id.title)
    val viewsCount: TextView = itemView.findViewById(R.id.view_count)

    private val isAutoPlayVideo = Settings.get().main().isAutoplay_video_on_posts

    private fun doAutoplayVideo(context: Context, video: Video) {
        InteractorFactory.video
            .getById(
                Settings.get().accounts().current,
                video.ownerId,
                video.id,
                video.accessKey,
                false
            )
            .fromIOToMain({
                Utils.doAutoPlayVideo(context, CustomToast.createCustomToast(context, null), video)
            }) {
                getVideoPreviewPlace(
                    Settings.get().accounts().current, video
                ).tryOpenWith(itemView.context)
            }
    }

    override fun bind(position: Int, itemDataHolder: AbsModel, listContentType: String?) {
        val video = itemDataHolder as Video
        viewsCount.text = String.format(Utils.appLocale, "%d", video.views)
        title.text = video.title
        videoLenght.text = AppTextUtils.getDurationString(video.duration)
        val photoUrl = video.image
        val trailerUrl = video.trailer
        if (isAutoPlayVideo == 1 && trailerUrl.nonNullNoEmpty() || isAutoPlayVideo == 2) {
            PicassoInstance.with().cancelRequest(image)
            image.setDecoderCallback(object :
                AnimatedShapeableImageView.OnDecoderInit {
                override fun onLoaded(success: Boolean) {
                    if (!success) {
                        if (photoUrl.nonNullNoEmpty()) {
                            PicassoInstance.with()
                                .load(photoUrl)
                                .placeholder(R.drawable.background_gray)
                                .tag(Constants.PICASSO_TAG)
                                .into(image)
                        } else {
                            PicassoInstance.with().cancelRequest(image)
                        }
                    }
                }
            })
            if (isAutoPlayVideo == 2) {
                image.fromVKVideo(
                    video, true
                )
            } else if (trailerUrl.nonNullNoEmpty()) {
                image.fromNet(
                    (video.ownerId.toString() + "_" + video.id.toString()),
                    trailerUrl,
                    true
                )
            }
        } else if (photoUrl.nonNullNoEmpty()) {
            PicassoInstance.with()
                .load(photoUrl)
                .tag(Constants.PICASSO_TAG)
                .into(image)
        } else {
            PicassoInstance.with().cancelRequest(image)
        }
        val serviceIcon = getIconByType(video.platform)
        if (serviceIcon != null) {
            videoService.visibility = View.VISIBLE
            videoService.setImageResource(serviceIcon)
        } else {
            videoService.visibility = View.GONE
        }
        card.setOnClickListener {
            if (Settings.get().main().isDo_auto_play_video) {
                doAutoplayVideo(itemView.context, video)
            } else {
                getVideoPreviewPlace(
                    Settings.get().accounts().current, video
                ).tryOpenWith(itemView.context)
            }
        }
        card.setOnLongClickListener {
            if (Settings.get().main().isDo_auto_play_video) {
                getVideoPreviewPlace(
                    Settings.get().accounts().current, video
                ).tryOpenWith(itemView.context)
            } else {
                doAutoplayVideo(itemView.context, video)
            }
            true
        }
    }

    class Fabric : ViewHolderFabric {
        override fun create(view: View): IViewHolder {
            return VideoViewHolder(
                view
            )
        }

        override fun getLayout(): Int {
            return R.layout.item_catalog_v2_video
        }
    }
}
