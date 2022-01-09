package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.EventListener;
import java.util.List;

import dev.ragnarok.fenrir.R;
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
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.NotSupported;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.view.CircleRoadProgress;

public class AttachmentsBottomSheetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ERROR_COLOR = Color.parseColor("#ff0000");
    private static final int VTYPE_BUTTON = 0;
    private static final int VTYPE_ENTRY = 1;

    private final List<AttachmentEntry> data;
    private final ActionListener actionListener;
    private final SharedHolders<EntryHolder> holders;
    private int nextHolderId;

    public AttachmentsBottomSheetAdapter(List<AttachmentEntry> data, ActionListener actionListener) {
        this.data = data;
        this.actionListener = actionListener;
        holders = new SharedHolders<>(false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VTYPE_BUTTON) {
            return new ImagesButtonHolder(inflater.inflate(R.layout.button_add_photo, parent, false));
        }

        EntryHolder holder = new EntryHolder(inflater.inflate(R.layout.message_attachments_entry, parent, false));
        holder.attachId(generateHolderId());
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VTYPE_BUTTON:
                bindAddPhotoButton((ImagesButtonHolder) holder);
                break;
            case VTYPE_ENTRY:
                bindEntryHolder((EntryHolder) holder, position);
                break;
        }
    }

    private void bindEntryHolder(EntryHolder holder, int position) {
        int dataPosition = position - 1;
        holder.image.setBackgroundResource(R.drawable.background_unknown_image);

        AttachmentEntry entry = data.get(dataPosition);
        holders.put(entry.getId(), holder);
        AbsModel model = entry.getAttachment();

        if (model instanceof Photo) {
            bindImageHolder(holder, (Photo) model);
        } else if (model instanceof Upload) {
            bindUploading(holder, (Upload) model);
        } else if (model instanceof Post) {
            bindPost(holder, (Post) model);
        } else if (model instanceof Video) {
            bindVideo(holder, (Video) model);
        } else if (model instanceof FwdMessages) {
            bindMessages(holder, (FwdMessages) model);
        } else if (model instanceof WallReply) {
            bindWallReplies(holder);
        } else if (model instanceof Document) {
            bindDoc(holder, (Document) model);
        } else if (model instanceof Audio) {
            bindAudio(holder, (Audio) model);
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
        }

        holder.buttomRemove.setOnClickListener(v -> actionListener.onButtonRemoveClick(entry));
        holder.Retry.setOnClickListener(v -> actionListener.onButtonRetryClick(entry));
    }

    @SuppressWarnings("unused")
    private void bindMessages(EntryHolder holder, FwdMessages messages) {
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        holder.image.setBackgroundResource(R.drawable.background_emails);

        if (!isEmpty(messages.fwds) && messages.fwds.size() == 1 && !isEmpty(messages.fwds.get(0).getBody())) {
            holder.title.setText(AppTextUtils.reduceText(messages.fwds.get(0).getBody(), 20));
        } else {
            holder.title.setText(R.string.messages);
        }

        bindImageView(holder, null);
    }

    private void bindWallReplies(EntryHolder holder) {
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        holder.image.setBackgroundResource(R.drawable.background_emails);
        holder.title.setText(R.string.comment);

        bindImageView(holder, null);
    }

    private void bindImageView(EntryHolder holder, String url) {
        if (isEmpty(url)) {
            PicassoInstance.with().cancelRequest(holder.image);
            holder.image.setImageResource(R.drawable.background_gray);
        } else {
            PicassoInstance.with()
                    .load(url)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.image);
        }
    }

    private void bindPhotoAlbum(EntryHolder holder, PhotoAlbum album) {
        holder.title.setText(R.string.photo_album);
        String photoLink = nonNull(album.getSizes()) ? album.getSizes().getUrlForSize(PhotoSize.X, false) : null;
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        bindImageView(holder, photoLink);
    }

    private void bindGraffiti(EntryHolder holder, Graffiti graffiti) {
        holder.title.setText(R.string.graffity);
        String photoLink = graffiti.getUrl();
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        bindImageView(holder, photoLink);
    }

    private void bindArticle(EntryHolder holder, Article article) {
        holder.title.setText(R.string.article);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        String photoLink = nonNull(article.getPhoto()) ? article.getPhoto().getUrlForSize(PhotoSize.X, false) : null;
        bindImageView(holder, photoLink);
    }

    private void bindMarket(EntryHolder holder, Market market) {
        holder.title.setText(market.getTitle());
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        bindImageView(holder, market.getThumb_photo());
    }

    private void bindMarketAlbum(EntryHolder holder, MarketAlbum market_album) {
        holder.title.setText(market_album.getTitle());
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        String photoLink = nonNull(market_album.getPhoto()) ? market_album.getPhoto().getUrlForSize(PhotoSize.X, false) : null;
        bindImageView(holder, photoLink);
    }

    private void bindAudioArtist(EntryHolder holder, AudioArtist artist) {
        holder.title.setText(artist.getName());
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        String photoLink = artist.getMaxPhoto();
        bindImageView(holder, photoLink);
    }

    private void bindAudioPlaylist(EntryHolder holder, AudioPlaylist link) {
        holder.title.setText(link.getTitle());
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        String photoLink = link.getThumb_image();
        bindImageView(holder, photoLink);
    }

    private void bindStory(EntryHolder holder, Story story) {
        holder.title.setText(R.string.story);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        String photoLink = nonNull(story.getOwner()) ? story.getOwner().getMaxSquareAvatar() : null;
        bindImageView(holder, photoLink);
    }

    private void bindCall(EntryHolder holder) {
        holder.title.setText(R.string.call);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        PicassoInstance.with().cancelRequest(holder.image);
        holder.image.setImageResource(R.drawable.phone_call_color);
    }

    private void bindNotSupported(EntryHolder holder) {
        holder.title.setText(R.string.not_supported);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        PicassoInstance.with().cancelRequest(holder.image);
        holder.image.setImageResource(R.drawable.not_supported);
    }

    private void bindEvent(EntryHolder holder, Event event) {
        holder.title.setText(event.getButton_text());
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        PicassoInstance.with().cancelRequest(holder.image);
    }

    private void bindAudio(EntryHolder holder, Audio audio) {
        String audiostr = audio.getArtist() + " - " + audio.getTitle();
        holder.title.setText(audiostr);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        holder.image.setBackgroundResource(R.drawable.background_unknown_song);
        bindImageView(holder, audio.getThumb_image_big());
    }

    private void bindVideo(EntryHolder holder, Video video) {
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        holder.title.setText(video.getTitle());

        bindImageView(holder, video.getImage());
    }

    private void bindDoc(EntryHolder holder, Document doc) {
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);
        holder.title.setText(doc.getTitle());

        String imgUrl = doc.getPreviewWithSize(PhotoSize.Q, false);

        bindImageView(holder, imgUrl);
    }

    private void bindPost(EntryHolder holder, Post post) {
        holder.progress.setVisibility(View.INVISIBLE);
        holder.Retry.setVisibility(View.GONE);
        holder.tintView.setVisibility(View.GONE);

        String title = post.getTextCopiesInclude();
        if (isEmpty(title)) {
            holder.title.setText(R.string.attachment_wall_post);
        } else {
            holder.title.setText(title);
        }

        String imgUrl = post.findFirstImageCopiesInclude(PhotoSize.Q, false);
        bindImageView(holder, imgUrl);
    }

    private void bindUploading(EntryHolder holder, Upload upload) {
        holder.tintView.setVisibility(View.VISIBLE);

        boolean inProgress = upload.getStatus() == Upload.STATUS_UPLOADING;
        holder.progress.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);
        if (inProgress) {
            holder.progress.changePercentage(upload.getProgress());
        } else {
            holder.progress.changePercentage(0);
        }

        @ColorInt
        int titleColor = holder.title.getTextColors().getDefaultColor();

        holder.Retry.setVisibility(View.GONE);
        switch (upload.getStatus()) {
            case Upload.STATUS_UPLOADING:
                String precentText = upload.getProgress() + "%";
                holder.title.setText(precentText);
                break;
            case Upload.STATUS_CANCELLING:
                holder.title.setText(R.string.cancelling);
                break;
            case Upload.STATUS_QUEUE:
                holder.title.setText(R.string.in_order);
                break;
            case Upload.STATUS_ERROR:
                holder.title.setText(R.string.error);
                titleColor = ERROR_COLOR;
                holder.Retry.setVisibility(View.VISIBLE);
                break;
        }

        holder.title.setTextColor(titleColor);

        if (upload.hasThumbnail()) {
            PicassoInstance.with()
                    .load(upload.buildThumnailUri())
                    .placeholder(R.drawable.background_gray)
                    .into(holder.image);
        } else {
            PicassoInstance.with().cancelRequest(holder.image);
            holder.image.setImageResource(R.drawable.background_gray);
        }
    }

    public void changeUploadProgress(int id, int progress, boolean smoothly) {
        EntryHolder holder = holders.findOneByEntityId(id);
        if (nonNull(holder)) {
            String precentText = progress + "%";
            holder.title.setText(precentText);

            if (smoothly) {
                holder.progress.changePercentageSmoothly(progress);
            } else {
                holder.progress.changePercentage(progress);
            }
        }
    }

    private void bindImageHolder(EntryHolder holder, Photo photo) {
        String url = photo.getUrlForSize(PhotoSize.Q, false);

        holder.Retry.setVisibility(View.GONE);
        holder.progress.setVisibility(View.INVISIBLE);
        holder.tintView.setVisibility(View.GONE);
        holder.title.setText(R.string.photo);

        bindImageView(holder, url);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VTYPE_BUTTON : VTYPE_ENTRY;
    }

    private void bindAddPhotoButton(ImagesButtonHolder holder) {
        holder.button.setOnClickListener(v -> actionListener.onAddPhotoButtonClick());
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    private int generateHolderId() {
        nextHolderId++;
        return nextHolderId;
    }

    public interface ActionListener extends EventListener {
        void onAddPhotoButtonClick();

        void onButtonRemoveClick(AttachmentEntry entry);

        void onButtonRetryClick(AttachmentEntry entry);
    }

    private static class ImagesButtonHolder extends RecyclerView.ViewHolder {

        final View button;

        ImagesButtonHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.content_root);
        }
    }

    private class EntryHolder extends RecyclerView.ViewHolder implements IdentificableHolder {

        final ImageView image;
        final TextView title;
        final ViewGroup buttomRemove;
        final CircleRoadProgress progress;
        final ImageView Retry;
        final View tintView;

        EntryHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            buttomRemove = itemView.findViewById(R.id.progress_root);
            progress = itemView.findViewById(R.id.progress_view);
            tintView = itemView.findViewById(R.id.tint_view);
            Retry = itemView.findViewById(R.id.retry_upload);
            itemView.setTag(generateHolderId());
        }

        @Override
        public int getHolderId() {
            return (int) tintView.getTag();
        }

        void attachId(int id) {
            tintView.setTag(id);
        }
    }
}
