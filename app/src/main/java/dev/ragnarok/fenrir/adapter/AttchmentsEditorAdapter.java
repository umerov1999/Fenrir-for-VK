package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.adapter.holder.IdentificableHolder;
import dev.ragnarok.fenrir.adapter.holder.SharedHolders;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Call;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Event;
import dev.ragnarok.fenrir.model.FwdMessages;
import dev.ragnarok.fenrir.model.Graffiti;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.NotSupported;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.view.CircleRoadProgress;

public class AttchmentsEditorAdapter extends RecyclerBindableAdapter<AttachmentEntry, AttchmentsEditorAdapter.ViewHolder> {

    private static final int ERROR_COLOR = Color.parseColor("#ff0000");
    private static int idGenerator;
    private final SharedHolders<ViewHolder> sharedHolders;
    private final Context context;
    private final Callback callback;

    public AttchmentsEditorAdapter(Context context, List<AttachmentEntry> items, Callback callback) {
        super(items);
        this.context = context;
        this.callback = callback;
        sharedHolders = new SharedHolders<>(false);
    }

    private static int generateNextHolderId() {
        idGenerator++;
        return idGenerator;
    }

    @Override
    protected void onBindItemViewHolder(@NonNull ViewHolder holder, int position, int type) {
        AttachmentEntry attachment = getItem(position);

        sharedHolders.put(attachment.getId(), holder);

        configView(attachment, holder);

        holder.vRemove.setOnClickListener(view -> {
            int dataposition = holder.getBindingAdapterPosition() - getHeadersCount();
            callback.onRemoveClick(dataposition, attachment);
        });

        holder.vTitleRoot.setOnClickListener(v -> {
            int dataposition = holder.getBindingAdapterPosition() - getHeadersCount();
            callback.onTitleClick(dataposition, attachment);
        });
    }

    public void cleanup() {
        sharedHolders.release();
    }

    @NonNull
    @Override
    protected ViewHolder viewHolder(View view, int type) {
        return new ViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_post_attachments;
    }

    public void updateEntityProgress(int attachmentId, int progress) {
        ViewHolder holder = sharedHolders.findOneByEntityId(attachmentId);

        if (nonNull(holder)) {
            bindProgress(holder, progress, true);
        }
    }

    private void bindProgress(ViewHolder holder, int progress, boolean smoothly) {
        String progressLine = progress + "%";
        holder.tvTitle.setText(progressLine);

        if (smoothly) {
            holder.pbProgress.changePercentageSmoothly(progress);
        } else {
            holder.pbProgress.changePercentage(progress);
        }
    }

    private void configUploadObject(Upload upload, ViewHolder holder) {
        holder.pbProgress.setVisibility(upload.getStatus() == Upload.STATUS_UPLOADING ? View.VISIBLE : View.GONE);
        holder.vTint.setVisibility(View.VISIBLE);

        int nonErrorTextColor = holder.tvTitle.getTextColors().getDefaultColor();
        switch (upload.getStatus()) {
            case Upload.STATUS_ERROR:
                holder.tvTitle.setText(R.string.error);
                holder.tvTitle.setTextColor(ERROR_COLOR);
                break;
            case Upload.STATUS_QUEUE:
                holder.tvTitle.setText(R.string.in_order);
                holder.tvTitle.setTextColor(nonErrorTextColor);
                break;
            case Upload.STATUS_CANCELLING:
                holder.tvTitle.setText(R.string.cancelling);
                holder.tvTitle.setTextColor(nonErrorTextColor);
                break;
            default:
                holder.tvTitle.setTextColor(nonErrorTextColor);
                String progressLine = upload.getProgress() + "%";
                holder.tvTitle.setText(progressLine);
                break;
        }

        holder.pbProgress.changePercentage(upload.getProgress());

        if (upload.hasThumbnail()) {
            PicassoInstance.with()
                    .load(upload.buildThumnailUri())
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindLink(ViewHolder holder, Link link) {
        holder.tvTitle.setText(R.string.link);

        String photoLink = nonNull(link.getPhoto()) ? link.getPhoto().getUrlForSize(PhotoSize.X, false) : null;

        if (nonEmpty(photoLink)) {
            PicassoInstance.with()
                    .load(photoLink)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindArticle(ViewHolder holder, Article link) {
        holder.tvTitle.setText(R.string.article);

        String photoLink = nonNull(link.getPhoto()) ? link.getPhoto().getUrlForSize(PhotoSize.X, false) : null;

        if (nonEmpty(photoLink)) {
            PicassoInstance.with()
                    .load(photoLink)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindStory(ViewHolder holder, Story story) {
        holder.tvTitle.setText(R.string.story);

        String photoLink = nonNull(story.getOwner()) ? story.getOwner().getMaxSquareAvatar() : null;

        if (nonEmpty(photoLink)) {
            PicassoInstance.with()
                    .load(photoLink)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindPhotoAlbum(ViewHolder holder, PhotoAlbum album) {
        holder.tvTitle.setText(R.string.photo_album);

        String photoLink = nonNull(album.getSizes()) ? album.getSizes().getUrlForSize(PhotoSize.X, false) : null;

        if (nonEmpty(photoLink)) {
            PicassoInstance.with()
                    .load(photoLink)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindAudioPlaylist(ViewHolder holder, AudioPlaylist playlist) {
        holder.tvTitle.setText(playlist.getTitle());

        String photoLink = playlist.getThumb_image();

        if (nonEmpty(photoLink)) {
            PicassoInstance.with()
                    .load(photoLink)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindGraffiti(ViewHolder holder, Graffiti graffiti) {
        holder.tvTitle.setText(R.string.graffity);

        String photoLink = graffiti.getUrl();

        if (nonEmpty(photoLink)) {
            PicassoInstance.with()
                    .load(photoLink)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(R.drawable.background_gray);
        }
    }

    private void bindCall(ViewHolder holder) {
        holder.tvTitle.setText(R.string.call);
        PicassoInstance.with().cancelRequest(holder.photoImageView);
        holder.photoImageView.setImageResource(R.drawable.phone_call_color);
    }

    private void bindEvent(ViewHolder holder, Event event) {
        holder.tvTitle.setText(event.getButton_text());
        PicassoInstance.with().cancelRequest(holder.photoImageView);
    }

    private void bindMarket(ViewHolder holder, Market market) {
        holder.tvTitle.setText(market.getTitle());
        if (isEmpty(market.getThumb_photo())) {
            PicassoInstance.with().cancelRequest(holder.photoImageView);

        } else {
            PicassoInstance.with()
                    .load(market.getThumb_photo())
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        }
    }

    private void bindMarketAlbum(ViewHolder holder, MarketAlbum market_album) {
        holder.tvTitle.setText(market_album.getTitle());
        if (market_album.getPhoto() == null) {
            PicassoInstance.with().cancelRequest(holder.photoImageView);

        } else {
            PicassoInstance.with()
                    .load(market_album.getPhoto().getUrlForSize(PhotoSize.X, false))
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        }
    }

    private void bindAudioArtist(ViewHolder holder, AudioArtist artist) {
        holder.tvTitle.setText(artist.getName());
        if (artist.getMaxPhoto() == null) {
            PicassoInstance.with().cancelRequest(holder.photoImageView);

        } else {
            PicassoInstance.with()
                    .load(artist.getMaxPhoto())
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        }
    }

    private void bindNotSupported(ViewHolder holder) {
        holder.tvTitle.setText(R.string.not_supported);
        PicassoInstance.with().cancelRequest(holder.photoImageView);
        holder.photoImageView.setImageResource(R.drawable.not_supported);
    }

    private void bindPhoto(ViewHolder holder, Photo photo) {
        holder.tvTitle.setText(R.string.photo);

        PicassoInstance.with()
                .load(photo.getUrlForSize(PhotoSize.X, false))
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        holder.photoImageView.setOnClickListener(null);
    }

    private void bindVideo(ViewHolder holder, Video video) {
        holder.tvTitle.setText(video.getTitle());

        PicassoInstance.with()
                .load(video.getImage())
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        holder.photoImageView.setOnClickListener(null);
    }

    private void bindAudio(ViewHolder holder, Audio audio) {
        if (isEmpty(audio.getThumb_image_big())) {
            PicassoInstance.with().cancelRequest(holder.photoImageView);
            holder.photoImageView.setImageResource(Settings.get().ui().isDarkModeEnabled(context) ? R.drawable.generic_audio_nowplaying_dark : R.drawable.generic_audio_nowplaying_light);
        } else {
            PicassoInstance.with()
                    .load(audio.getThumb_image_big())
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photoImageView);
        }

        String audiostr = audio.getArtist() + " - " + audio.getTitle();
        holder.tvTitle.setText(audiostr);
        holder.photoImageView.setOnClickListener(null);
    }

    private void bindPoll(ViewHolder holder, Poll poll) {
        PicassoInstance.with()
                .load(R.drawable.background_gray)
                .into(holder.photoImageView);

        holder.tvTitle.setText(poll.getQuestion());
        holder.photoImageView.setOnClickListener(null);
    }

    private void bindPost(ViewHolder holder, Post post) {
        String postImgUrl = post.findFirstImageCopiesInclude();

        if (TextUtils.isEmpty(postImgUrl)) {
            PicassoInstance.with()
                    .load(R.drawable.background_gray)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with()
                    .load(postImgUrl)
                    .into(holder.photoImageView);
        }

        holder.tvTitle.setText(R.string.attachment_wall_post);
        holder.photoImageView.setOnClickListener(null);
    }

    private void bindDoc(ViewHolder holder, Document document) {
        String previewUrl = document.getPreviewWithSize(PhotoSize.X, false);

        if (nonEmpty(previewUrl)) {
            PicassoInstance.with()
                    .load(previewUrl)
                    .into(holder.photoImageView);
        } else {
            PicassoInstance.with()
                    .load(R.drawable.background_gray)
                    .into(holder.photoImageView);
        }

        holder.photoImageView.setOnClickListener(null);
        holder.tvTitle.setText(document.getTitle());
    }

    @SuppressWarnings("unused")
    private void bindFwdMessages(ViewHolder holder, FwdMessages messages) {
        PicassoInstance.with()
                .load(R.drawable.background_gray)
                .into(holder.photoImageView);
        holder.tvTitle.setText(context.getString(R.string.title_messages));
    }

    private void bindWallReplies(ViewHolder holder) {
        PicassoInstance.with()
                .load(R.drawable.background_gray)
                .into(holder.photoImageView);
        holder.tvTitle.setText(context.getString(R.string.comments));
    }

    private void configView(AttachmentEntry item, ViewHolder holder) {
        holder.vRemove.setVisibility(item.isCanDelete() ? View.VISIBLE : View.GONE);

        AbsModel model = item.getAttachment();

        holder.pbProgress.setVisibility(View.GONE);
        holder.vTint.setVisibility(View.GONE);

        if (model instanceof Photo) {
            bindPhoto(holder, (Photo) model);
        } else if (model instanceof Video) {
            bindVideo(holder, (Video) model);
        } else if (model instanceof Audio) {
            bindAudio(holder, (Audio) model);
        } else if (model instanceof Poll) {
            bindPoll(holder, (Poll) model);
        } else if (model instanceof Post) {
            bindPost(holder, (Post) model);
        } else if (model instanceof Document) {
            bindDoc(holder, (Document) model);
        } else if (model instanceof FwdMessages) {
            bindFwdMessages(holder, (FwdMessages) model);
        } else if (model instanceof Upload) {
            configUploadObject((Upload) model, holder);
        } else if (model instanceof Link) {
            bindLink(holder, (Link) model);
        } else if (model instanceof Article) {
            bindArticle(holder, (Article) model);
        } else if (model instanceof Story) {
            bindStory(holder, (Story) model);
        } else if (model instanceof Call) {
            bindCall(holder);
        } else if (model instanceof NotSupported) {
            bindNotSupported(holder);
        } else if (model instanceof Event) {
            bindEvent(holder, (Event) model);
        } else if (model instanceof Market) {
            bindMarket(holder, (Market) model);
        } else if (model instanceof MarketAlbum) {
            bindMarketAlbum(holder, (MarketAlbum) model);
        } else if (model instanceof AudioArtist) {
            bindAudioArtist(holder, (AudioArtist) model);
        } else if (model instanceof AudioPlaylist) {
            bindAudioPlaylist(holder, (AudioPlaylist) model);
        } else if (model instanceof Graffiti) {
            bindGraffiti(holder, (Graffiti) model);
        } else if (model instanceof PhotoAlbum) {
            bindPhotoAlbum(holder, (PhotoAlbum) model);
        } else if (model instanceof WallReply) {
            bindWallReplies(holder);
        } else {
            throw new UnsupportedOperationException("Type " + model.getClass() + " in not supported");
        }
    }

    public interface Callback {
        void onRemoveClick(int dataposition, @NonNull AttachmentEntry entry);

        void onTitleClick(int dataposition, @NonNull AttachmentEntry entry);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements IdentificableHolder {

        final ImageView photoImageView;
        final TextView tvTitle;
        final View vRemove;
        final CircleRoadProgress pbProgress;
        final View vTint;
        final View vTitleRoot;

        ViewHolder(View itemView) {
            super(itemView);

            photoImageView = itemView.findViewById(R.id.item_attachment_image);
            tvTitle = itemView.findViewById(R.id.item_attachment_title);
            vRemove = itemView.findViewById(R.id.item_attachment_progress_root);
            pbProgress = itemView.findViewById(R.id.item_attachment_progress);
            vTint = itemView.findViewById(R.id.item_attachment_tint);
            vTitleRoot = itemView.findViewById(R.id.item_attachment_title_root);

            itemView.setTag(generateNextHolderId());
        }

        @Override
        public int getHolderId() {
            return (int) itemView.getTag();
        }
    }
}
