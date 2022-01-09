package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.dpToPx;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeLenghtOf;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso3.Transformation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.holder.IdentificableHolder;
import dev.ragnarok.fenrir.adapter.holder.SharedHolders;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Attachments;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.CryptStatus;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Event;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Types;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.WaveFormView;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class AttachmentsViewBinder {

    private static final int PREFFERED_STICKER_SIZE = 120;
    private static final byte[] DEFAUL_WAVEFORM = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static int sHolderIdCounter;
    private final PhotosViewHelper photosViewHelper;
    private final Transformation mAvatarTransformation;
    private final int mActiveWaveFormColor;
    private final int mNoactiveWaveFormColor;
    private final SharedHolders<VoiceHolder> mVoiceSharedHolders;
    private final OnAttachmentsActionCallback mAttachmentsActionCallback;
    private final Context mContext;
    private final boolean isNightStiker;
    private final boolean expandVoiceTranscript;
    private VoiceActionListener mVoiceActionListener;
    private EmojiconTextView.OnHashTagClickListener mOnHashTagClickListener;

    public AttachmentsViewBinder(Context context, @NonNull OnAttachmentsActionCallback attachmentsActionCallback) {
        mContext = context;
        mVoiceSharedHolders = new SharedHolders<>(true);
        mAvatarTransformation = CurrentTheme.createTransformationForAvatar();
        photosViewHelper = new PhotosViewHelper(context, attachmentsActionCallback);
        mAttachmentsActionCallback = attachmentsActionCallback;
        mActiveWaveFormColor = CurrentTheme.getColorPrimary(context);
        mNoactiveWaveFormColor = Utils.adjustAlpha(mActiveWaveFormColor, 0.5f);
        isNightStiker = Settings.get().ui().isStickers_by_theme() && Settings.get().ui().isDarkModeEnabled(context);
        expandVoiceTranscript = Settings.get().main().isExpand_voice_transcript();
    }

    private static void safeSetVisibitity(@Nullable View view, int visibility) {
        if (view != null) view.setVisibility(visibility);
    }

    private static int generateHolderId() {
        sHolderIdCounter++;
        return sHolderIdCounter;
    }

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        mOnHashTagClickListener = onHashTagClickListener;
    }

    public void displayAttachments(Attachments attachments, AttachmentsHolder containers, boolean postsAsLinks, Integer messageId) {
        if (attachments == null) {
            safeSetVisibitity(containers.getVgAudios(), View.GONE);
            safeSetVisibitity(containers.getVgVideos(), View.GONE);
            safeSetVisibitity(containers.getVgArticles(), View.GONE);
            safeSetVisibitity(containers.getVgDocs(), View.GONE);
            safeSetVisibitity(containers.getVgPhotos(), View.GONE);
            safeSetVisibitity(containers.getVgPosts(), View.GONE);
            safeSetVisibitity(containers.getVgStickers(), View.GONE);
            safeSetVisibitity(containers.getVoiceMessageRoot(), View.GONE);
            safeSetVisibitity(containers.getVgFriends(), View.GONE);
            photosViewHelper.removeZoomable(containers.getVgPhotos());
            containers.getVgAudios().dispose();
        } else {
            displayArticles(attachments.getArticles(), containers.getVgArticles());
            containers.getVgAudios().displayAudios(attachments.getAudios(), mAttachmentsActionCallback);
            displayVoiceMessages(attachments.getVoiceMessages(), containers.getVoiceMessageRoot(), messageId);
            displayDocs(attachments.getDocLinks(postsAsLinks, true), containers.getVgDocs());

            if (containers.getVgStickers() != null) {
                displayStickers(attachments.getStickers(), containers.getVgStickers());
            }

            photosViewHelper.displayPhotos(attachments.getPostImages(), containers.getVgPhotos());
            photosViewHelper.displayVideos(attachments.getPostImagesVideos(), containers.getVgVideos());
        }
    }

    private void displayVoiceMessages(ArrayList<VoiceMessage> voices, ViewGroup container, Integer messageId) {
        if (isNull(container)) return;

        boolean empty = safeIsEmpty(voices);
        container.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }

        int i = voices.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            container.addView(LayoutInflater.from(mContext).inflate(R.layout.item_voice_message, container, false));
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup root = (ViewGroup) container.getChildAt(g);

            if (g < voices.size()) {
                VoiceHolder holder = (VoiceHolder) root.getTag();
                if (holder == null) {
                    holder = new VoiceHolder(root);
                    root.setTag(holder);
                }

                VoiceMessage voice = voices.get(g);
                bindVoiceHolder(holder, voice, messageId);

                root.setVisibility(View.VISIBLE);
            } else {
                root.setVisibility(View.GONE);
            }
        }
    }

    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin, boolean speed) {
        VoiceHolder holder = mVoiceSharedHolders.findHolderByHolderId(holderId);
        if (nonNull(holder)) {
            bindVoiceHolderPlayState(holder, play, paused, progress, amin, speed);
        }
    }

    private void bindVoiceHolderPlayState(VoiceHolder holder, boolean play, boolean paused, float progress, boolean anim, boolean speed) {
        @DrawableRes
        int icon = play && !paused ? R.drawable.pause : R.drawable.play;

        holder.mButtonPlay.setImageResource(icon);
        holder.mWaveFormView.setCurrentActiveProgress(play ? progress : 1.0f, anim);
        Utils.setTint(holder.mSpeed, speed ? CurrentTheme.getColorPrimary(mContext) : CurrentTheme.getColorOnSurface(mContext));
        holder.mSpeed.setVisibility(play ? View.VISIBLE : View.GONE);
    }

    public void configNowVoiceMessagePlaying(int voiceMessageId, float progress, boolean paused, boolean amin, boolean speed) {
        SparseArray<Set<WeakReference<VoiceHolder>>> holders = mVoiceSharedHolders.getCache();
        for (int i = 0; i < holders.size(); i++) {
            int key = holders.keyAt(i);

            boolean play = key == voiceMessageId;

            Set<WeakReference<VoiceHolder>> set = holders.get(key);
            for (WeakReference<VoiceHolder> reference : set) {
                VoiceHolder holder = reference.get();
                if (nonNull(holder)) {
                    bindVoiceHolderPlayState(holder, play, paused, progress, amin, speed);
                }
            }
        }
    }

    public void setVoiceActionListener(VoiceActionListener voiceActionListener) {
        mVoiceActionListener = voiceActionListener;
    }

    public void disableVoiceMessagePlaying() {
        SparseArray<Set<WeakReference<VoiceHolder>>> holders = mVoiceSharedHolders.getCache();
        for (int i = 0; i < holders.size(); i++) {
            int key = holders.keyAt(i);
            Set<WeakReference<VoiceHolder>> set = holders.get(key);
            for (WeakReference<VoiceHolder> reference : set) {
                VoiceHolder holder = reference.get();
                if (nonNull(holder)) {
                    bindVoiceHolderPlayState(holder, false, false, 0f, false, false);
                }
            }
        }
    }

    private void bindVoiceHolder(@NonNull VoiceHolder holder, @NonNull VoiceMessage voice, Integer messageId) {
        int voiceMessageId = voice.getId();
        mVoiceSharedHolders.put(voiceMessageId, holder);

        holder.mDurationText.setText(AppTextUtils.getDurationString(voice.getDuration()));

        // can bee NULL/empty
        if (nonNull(voice.getWaveform()) && voice.getWaveform().length > 0) {
            holder.mWaveFormView.setWaveForm(voice.getWaveform());
        } else {
            holder.mWaveFormView.setWaveForm(DEFAUL_WAVEFORM);
        }

        if (isEmpty(voice.getTranscript())) {
            holder.TranscriptText.setVisibility(View.GONE);
            if (messageId == null) {
                holder.mDoTranscript.setVisibility(View.GONE);
            } else {
                holder.mDoTranscript.setVisibility(View.VISIBLE);
                holder.mDoTranscript.setOnClickListener(v -> {
                    if (nonNull(mVoiceActionListener)) {
                        mVoiceActionListener.onTranscript(voice.getOwnerId() + "_" + voice.getId(), messageId);
                        holder.mDoTranscript.setVisibility(View.GONE);
                    }
                });
            }
        } else {
            if (voice.isShowTranscript() || expandVoiceTranscript) {
                holder.TranscriptText.setVisibility(View.VISIBLE);
                holder.TranscriptText.setText(voice.getTranscript());
                holder.mDoTranscript.setVisibility(View.GONE);
            } else {
                holder.TranscriptText.setVisibility(View.GONE);
                holder.mDoTranscript.setVisibility(View.VISIBLE);
                holder.mDoTranscript.setOnClickListener(v -> {
                    voice.setShowTranscript(true);
                    holder.TranscriptText.setVisibility(View.VISIBLE);
                    holder.TranscriptText.setText(voice.getTranscript());
                    holder.mDoTranscript.setVisibility(View.GONE);
                });
            }
        }

        holder.mWaveFormView.setOnLongClickListener(v -> {
            if (!AppPerms.hasReadWriteStoragePermission(mContext)) {
                if (mAttachmentsActionCallback != null) {
                    mAttachmentsActionCallback.onRequestWritePermissions();
                }
                return true;
            }
            DownloadWorkUtils.doDownloadVoice(mContext, voice);

            return true;
        });

        holder.mButtonPlay.setOnClickListener(v -> {
            if (nonNull(mVoiceActionListener)) {
                mVoiceActionListener.onVoicePlayButtonClick(holder.getHolderId(), voiceMessageId, voice);
            }
        });

        holder.mSpeed.setOnClickListener(v -> {
            if (nonNull(mVoiceActionListener)) {
                mVoiceActionListener.onVoiceTogglePlaybackSpeed();
            }
        });

        if (nonNull(mVoiceActionListener)) {
            mVoiceActionListener.onVoiceHolderBinded(voiceMessageId, holder.getHolderId());
        }
    }

    private void displayStickers(List<Sticker> stickers, ViewGroup stickersContainer) {
        stickersContainer.setVisibility(safeIsEmpty(stickers) ? View.GONE : View.VISIBLE);
        if (isEmpty(stickers)) {
            return;
        }

        if (stickersContainer.getChildCount() == 0) {
            RLottieImageView localView = new RLottieImageView(mContext);
            stickersContainer.addView(localView);
        }

        RLottieImageView imageView = (RLottieImageView) stickersContainer.getChildAt(0);
        Sticker sticker = stickers.get(0);

        int prefferedStickerSize = (int) dpToPx(PREFFERED_STICKER_SIZE, mContext);
        Sticker.Image image = sticker.getImage(256, isNightStiker);

        boolean horisontal = image.getHeight() < image.getWidth();
        double proporsion = (double) image.getWidth() / (double) image.getHeight();

        float finalWidth;
        float finalHeihgt;

        if (horisontal) {
            finalWidth = prefferedStickerSize;
            finalHeihgt = (float) (finalWidth / proporsion);
        } else {
            finalHeihgt = prefferedStickerSize;
            finalWidth = (float) (finalHeihgt * proporsion);
        }

        imageView.getLayoutParams().height = (int) finalHeihgt;
        imageView.getLayoutParams().width = (int) finalWidth;

        if (sticker.isAnimated()) {
            imageView.fromNet(sticker.getAnimationByType(isNightStiker ? "dark" : "light"), Utils.createOkHttp(5), (int) finalWidth, (int) finalHeihgt);
            stickersContainer.setOnLongClickListener(e -> {
                imageView.replayAnimation();
                return true;
            });
        } else {
            PicassoInstance.with()
                    .load(image.getUrl())
                    .tag(Constants.PICASSO_TAG)
                    .into(imageView);
        }
    }

    public void displayCopyHistory(List<Post> posts, ViewGroup container, boolean reduce, int layout) {
        if (container != null) {
            container.setVisibility(safeIsEmpty(posts) ? View.GONE : View.VISIBLE);
        }

        if (safeIsEmpty(posts) || container == null) {
            return;
        }

        int i = posts.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            View itemView = LayoutInflater.from(container.getContext()).inflate(layout, container, false);
            CopyHolder holder = new CopyHolder((ViewGroup) itemView, mAttachmentsActionCallback);
            itemView.setTag(holder);

            if (!reduce) {
                holder.bodyView.setAutoLinkMask(Linkify.WEB_URLS);
                holder.bodyView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            container.addView(itemView);
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup postViewGroup = (ViewGroup) container.getChildAt(g);

            if (g < posts.size()) {
                CopyHolder check = (CopyHolder) postViewGroup.getTag();
                if (check == null) {
                    check = new CopyHolder(postViewGroup, mAttachmentsActionCallback);
                    postViewGroup.setTag(check);
                }
                CopyHolder holder = check;
                Post copy = posts.get(g);

                if (isNull(copy)) {
                    postViewGroup.setVisibility(View.GONE);
                    return;
                }

                postViewGroup.setVisibility(View.VISIBLE);

                String text = reduce ? AppTextUtils.reduceStringForPost(copy.getText()) : copy.getText();

                holder.bodyView.setVisibility(isEmpty(copy.getText()) ? View.GONE : View.VISIBLE);
                holder.bodyView.setOnHashTagClickListener(mOnHashTagClickListener);
                holder.bodyView.setText(OwnerLinkSpanFactory.withSpans(text, true, false, new LinkActionAdapter() {
                    @Override
                    public void onOwnerClick(int ownerId) {
                        mAttachmentsActionCallback.onOpenOwner(ownerId);
                    }
                }));

                holder.ivAvatar.setOnClickListener(v -> mAttachmentsActionCallback.onOpenOwner(copy.getAuthorId()));
                ViewUtils.displayAvatar(holder.ivAvatar, mAvatarTransformation, copy.getAuthorPhoto(), Constants.PICASSO_TAG);

                holder.tvShowMore.setVisibility(reduce && safeLenghtOf(copy.getText()) > 400 ? View.VISIBLE : View.GONE);
                holder.ownerName.setText(copy.getAuthorName());
                holder.buttonDots.setTag(copy);

                displayAttachments(copy.getAttachments(), holder.attachmentsHolder, false, null);
            } else {
                postViewGroup.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void displayFriendsPost(List<User> users, ViewGroup container, int layout) {
        if (container != null) {
            container.setVisibility(safeIsEmpty(users) ? View.GONE : View.VISIBLE);
        }

        if (safeIsEmpty(users) || container == null) {
            return;
        }

        int i = users.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            View itemView = LayoutInflater.from(container.getContext()).inflate(layout, container, false);
            FriendsPostViewHolder holder = new FriendsPostViewHolder(itemView, mAttachmentsActionCallback);
            itemView.setTag(holder);

            container.addView(itemView);
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup postViewGroup = (ViewGroup) container.getChildAt(g);

            if (g < users.size()) {
                FriendsPostViewHolder holder = (FriendsPostViewHolder) postViewGroup.getTag();
                User user = users.get(g);

                if (isNull(user)) {
                    postViewGroup.setVisibility(View.GONE);
                    return;
                }

                postViewGroup.setVisibility(View.VISIBLE);

                if (isEmpty(user.getFullName()))
                    holder.tvTitle.setVisibility(View.INVISIBLE);
                else {
                    holder.tvTitle.setVisibility(View.VISIBLE);
                    holder.tvTitle.setText(user.getFullName());
                }
                if (isEmpty(user.getDomain()))
                    holder.tvDescription.setVisibility(View.INVISIBLE);
                else {
                    holder.tvDescription.setVisibility(View.VISIBLE);
                    holder.tvDescription.setText("@" + user.getDomain());
                }

                String imageUrl = user.getMaxSquareAvatar();
                if (imageUrl != null) {
                    ViewUtils.displayAvatar(holder.ivImage, mAvatarTransformation, imageUrl, Constants.PICASSO_TAG);
                } else {
                    PicassoInstance.with().cancelRequest(holder.ivImage);
                    holder.ivImage.setImageResource(R.drawable.ic_avatar_unknown);
                }

                holder.ivImage.setOnClickListener(v -> mAttachmentsActionCallback.onOpenOwner(user.getOwnerId()));

            } else {
                postViewGroup.setVisibility(View.GONE);
            }
        }
    }

    public void displayForwards(List<Message> fwds, ViewGroup fwdContainer, Context context, boolean postsAsLinks) {
        fwdContainer.setVisibility(safeIsEmpty(fwds) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(fwds)) {
            return;
        }

        int i = fwds.size() - fwdContainer.getChildCount();
        for (int j = 0; j < i; j++) {
            View localView = LayoutInflater.from(context).inflate(R.layout.item_forward_message, fwdContainer, false);
            fwdContainer.addView(localView);
        }

        for (int g = 0; g < fwdContainer.getChildCount(); g++) {
            ViewGroup itemView = (ViewGroup) fwdContainer.getChildAt(g);
            if (g < fwds.size()) {
                Message message = fwds.get(g);
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(message);

                if (Settings.get().other().isDeveloper_mode()) {
                    itemView.findViewById(R.id.item_message_bubble).setOnLongClickListener(v -> {
                        mAttachmentsActionCallback.onGoToMessagesLookup(message);
                        return true;
                    });
                }

                TextView tvBody = itemView.findViewById(R.id.item_fwd_message_text);
                tvBody.setText(OwnerLinkSpanFactory.withSpans(message.getCryptStatus() == CryptStatus.DECRYPTED ? message.getDecryptedBody() : message.getBody(), true, false, new LinkActionAdapter() {
                    @Override
                    public void onOwnerClick(int ownerId) {
                        mAttachmentsActionCallback.onOpenOwner(ownerId);
                    }
                }));
                tvBody.setVisibility(message.getBody() == null || message.getBody().length() == 0 ? View.GONE : View.VISIBLE);

                ((TextView) itemView.findViewById(R.id.item_fwd_message_username)).setText(message.getSender().getFullName());
                ((TextView) itemView.findViewById(R.id.item_fwd_message_time)).setText(AppTextUtils.getDateFromUnixTime(message.getDate()));
                MaterialButton tvFwds = itemView.findViewById(R.id.item_forward_message_fwds);
                tvFwds.setVisibility(message.getForwardMessagesCount() > 0 ? View.VISIBLE : View.GONE);

                tvFwds.setOnClickListener(v -> mAttachmentsActionCallback.onForwardMessagesOpen(message.getFwd()));

                ImageView ivAvatar = itemView.findViewById(R.id.item_fwd_message_avatar);

                String senderPhotoUrl = message.getSender() == null ? null : message.getSender().getMaxSquareAvatar();
                ViewUtils.displayAvatar(ivAvatar, mAvatarTransformation, senderPhotoUrl, Constants.PICASSO_TAG);

                ivAvatar.setOnClickListener(v -> mAttachmentsActionCallback.onOpenOwner(message.getSenderId()));

                AttachmentsHolder attachmentContainers = new AttachmentsHolder();
                attachmentContainers.setVgAudios(itemView.findViewById(R.id.audio_attachments)).
                        setVgVideos(itemView.findViewById(R.id.video_attachments)).
                        setVgDocs(itemView.findViewById(R.id.docs_attachments)).
                        setVgArticles(itemView.findViewById(R.id.articles_attachments)).
                        setVgPhotos(itemView.findViewById(R.id.photo_attachments)).
                        setVgPosts(itemView.findViewById(R.id.posts_attachments)).
                        setVgStickers(itemView.findViewById(R.id.stickers_attachments)).
                        setVoiceMessageRoot(itemView.findViewById(R.id.voice_message_attachments));

                displayAttachments(message.getAttachments(), attachmentContainers, postsAsLinks, message.getId());
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setTag(null);
            }
        }
    }

    private void displayDocs(List<DocLink> docs, ViewGroup root) {
        root.setVisibility(safeIsEmpty(docs) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(docs)) {
            return;
        }

        int i = docs.size() - root.getChildCount();
        for (int j = 0; j < i; j++) {
            root.addView(LayoutInflater.from(mContext).inflate(R.layout.item_document, root, false));
        }

        for (int g = 0; g < root.getChildCount(); g++) {
            ViewGroup itemView = (ViewGroup) root.getChildAt(g);
            if (g < docs.size()) {
                DocLink doc = docs.get(g);
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(doc);

                TextView tvTitle = itemView.findViewById(R.id.item_document_title);
                TextView tvDetails = itemView.findViewById(R.id.item_document_ext_size);
                EmojiconTextView tvPostText = itemView.findViewById(R.id.item_message_text);
                ShapeableImageView ivPhotoT = itemView.findViewById(R.id.item_document_image);
                ImageView ivGraffity = itemView.findViewById(R.id.item_document_graffity);
                ImageView ivPhoto_Post = itemView.findViewById(R.id.item_post_avatar_image);
                ImageView ivType = itemView.findViewById(R.id.item_document_type);
                TextView tvShowMore = itemView.findViewById(R.id.item_post_show_more);

                String title = doc.getTitle(mContext);
                String details = doc.getSecondaryText(mContext);

                String imageUrl = doc.getImageUrl();
                String ext = doc.getExt(mContext) == null ? "" : doc.getExt(mContext) + ", ";

                String subtitle = firstNonEmptyString(ext, " ") + firstNonEmptyString(details, " ");

                if (isEmpty(title)) {
                    tvTitle.setVisibility(View.GONE);
                } else {
                    tvTitle.setText(title);
                    tvTitle.setVisibility(View.VISIBLE);
                }
                if (doc.getType() == Types.POST) {
                    tvShowMore.setVisibility(subtitle.length() > 400 ? View.VISIBLE : View.GONE);
                    tvDetails.setVisibility(View.GONE);
                    tvPostText.setVisibility(View.VISIBLE);
                    tvPostText.setText(OwnerLinkSpanFactory.withSpans(AppTextUtils.reduceStringForPost(subtitle), true, false, new LinkActionAdapter() {
                        @Override
                        public void onOwnerClick(int ownerId) {
                            mAttachmentsActionCallback.onOpenOwner(ownerId);
                        }
                    }));
                } else if (doc.getType() == Types.WALL_REPLY) {
                    tvShowMore.setVisibility(subtitle.length() > 400 ? View.VISIBLE : View.GONE);
                    tvDetails.setVisibility(View.GONE);
                    tvPostText.setVisibility(View.VISIBLE);
                    tvPostText.setText(OwnerLinkSpanFactory.withSpans(AppTextUtils.reduceStringForPost(subtitle), true, false, new LinkActionAdapter() {
                        @Override
                        public void onOwnerClick(int ownerId) {
                            mAttachmentsActionCallback.onOpenOwner(ownerId);
                        }
                    }));
                } else if (doc.getType() == Types.EVENT) {
                    tvShowMore.setVisibility(View.GONE);
                    tvDetails.setVisibility(View.GONE);
                    tvPostText.setVisibility(View.VISIBLE);
                    tvPostText.setText(OwnerLinkSpanFactory.withSpans(AppTextUtils.reduceStringForPost(subtitle), true, false, new LinkActionAdapter() {
                        @Override
                        public void onOwnerClick(int ownerId) {
                            mAttachmentsActionCallback.onOpenOwner(ownerId);
                        }
                    }));
                } else if (doc.getType() == Types.NOT_SUPPORTED) {
                    tvShowMore.setVisibility(View.GONE);
                    tvDetails.setVisibility(View.GONE);
                    tvPostText.setVisibility(View.VISIBLE);
                    tvPostText.setText(AppTextUtils.reduceStringForPost(subtitle));
                } else {
                    tvDetails.setVisibility(View.VISIBLE);
                    tvPostText.setVisibility(View.GONE);
                    tvShowMore.setVisibility(View.GONE);

                    if (isEmpty(subtitle)) {
                        tvDetails.setVisibility(View.GONE);
                    } else {
                        tvDetails.setText(subtitle);
                        tvDetails.setVisibility(View.VISIBLE);
                    }
                }

                View attachmentsRoot = itemView.findViewById(R.id.item_message_attachment_container);
                AttachmentsHolder attachmentsHolder = new AttachmentsHolder();
                attachmentsHolder.setVgAudios(attachmentsRoot.findViewById(R.id.audio_attachments))
                        .setVgVideos(attachmentsRoot.findViewById(R.id.video_attachments))
                        .setVgDocs(attachmentsRoot.findViewById(R.id.docs_attachments))
                        .setVgArticles(attachmentsRoot.findViewById(R.id.articles_attachments))
                        .setVgPhotos(attachmentsRoot.findViewById(R.id.photo_attachments))
                        .setVgPosts(attachmentsRoot.findViewById(R.id.posts_attachments))
                        .setVoiceMessageRoot(attachmentsRoot.findViewById(R.id.voice_message_attachments));
                attachmentsRoot.setVisibility(View.GONE);

                itemView.setOnClickListener(v -> openDocLink(doc));
                ivPhoto_Post.setVisibility(View.GONE);
                ivGraffity.setVisibility(View.GONE);

                switch (doc.getType()) {
                    case Types.DOC:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.GONE);
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, imageUrl, Constants.PICASSO_TAG);
                        } else {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhotoT.setVisibility(View.GONE);
                            ivType.setImageResource(R.drawable.file);
                        }
                        break;
                    case Types.GRAFFITY:
                        ivPhotoT.setVisibility(View.GONE);
                        if (imageUrl != null) {
                            ivType.setVisibility(View.GONE);
                            ivGraffity.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivGraffity, null, imageUrl, Constants.PICASSO_TAG);
                        } else {
                            ivType.setVisibility(View.VISIBLE);
                            ivType.setImageResource(R.drawable.counter);
                        }
                        break;
                    case Types.AUDIO_PLAYLIST:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, imageUrl, Constants.PICASSO_TAG);
                            ivType.setImageResource(R.drawable.audio_player);
                        } else {
                            ivPhotoT.setVisibility(View.GONE);
                        }
                        break;
                    case Types.ALBUM:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, imageUrl, Constants.PICASSO_TAG);
                            ivType.setImageResource(R.drawable.album_photo);
                        } else {
                            ivPhotoT.setVisibility(View.GONE);
                        }
                        break;
                    case Types.MARKET_ALBUM:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, imageUrl, Constants.PICASSO_TAG);
                            ivType.setImageResource(R.drawable.ic_market_stack);
                        } else {
                            ivPhotoT.setVisibility(View.GONE);
                        }
                        break;
                    case Types.ARTIST:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, imageUrl, Constants.PICASSO_TAG);
                            ivType.setImageResource(R.drawable.artist_icon);
                        } else {
                            ivPhotoT.setVisibility(View.GONE);
                        }
                        break;
                    case Types.MARKET:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, imageUrl, Constants.PICASSO_TAG);
                            ivType.setImageResource(R.drawable.ic_market_outline);
                        } else {
                            ivPhotoT.setVisibility(View.GONE);
                        }
                        break;
                    case Types.STORY:
                        ivPhotoT.setVisibility(View.GONE);
                        ivType.setVisibility(View.GONE);
                        if (imageUrl != null) {
                            ivPhoto_Post.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhoto_Post, mAvatarTransformation, imageUrl, Constants.PICASSO_TAG);
                        } else {
                            ivPhoto_Post.setVisibility(View.GONE);
                        }
                        Story st = (Story) doc.attachment;
                        String prw = st.getPhoto() != null ? st.getPhoto().getUrlForSize(PhotoSize.X, true) : (st.getVideo() != null ? st.getVideo().getImage() : null);
                        if (prw != null) {
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, prw, Constants.PICASSO_TAG);
                        } else {
                            ivPhotoT.setVisibility(View.GONE);
                        }
                        break;
                    case Types.POST:
                        ivPhotoT.setVisibility(View.GONE);
                        ivType.setVisibility(View.GONE);
                        if (imageUrl != null) {
                            ivPhoto_Post.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhoto_Post, mAvatarTransformation, imageUrl, Constants.PICASSO_TAG);
                        } else {
                            ivPhoto_Post.setVisibility(View.GONE);
                        }
                        Post post = (Post) doc.attachment;
                        boolean hasAttachments = (nonNull(post.getAttachments()) && post.getAttachments().size() > 0);
                        attachmentsRoot.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);
                        if (hasAttachments)
                            displayAttachments(post.getAttachments(), attachmentsHolder, false, null);
                        break;
                    case Types.WALL_REPLY:
                        ivPhotoT.setVisibility(View.GONE);
                        ivType.setVisibility(View.VISIBLE);
                        ivType.setImageResource(R.drawable.comment);
                        if (imageUrl != null) {
                            ivPhoto_Post.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhoto_Post, mAvatarTransformation, imageUrl, Constants.PICASSO_TAG);
                        } else {
                            ivPhoto_Post.setVisibility(View.GONE);
                        }
                        WallReply comment = (WallReply) doc.attachment;
                        boolean hasCommentAttachments = (nonNull(comment.getAttachments()) && comment.getAttachments().size() > 0);
                        attachmentsRoot.setVisibility(hasCommentAttachments ? View.VISIBLE : View.GONE);
                        if (hasCommentAttachments)
                            displayAttachments(comment.getAttachments(), attachmentsHolder, false, null);
                        break;
                    case Types.EVENT:
                        ivPhotoT.setVisibility(View.GONE);
                        ivType.setVisibility(View.VISIBLE);
                        ivType.setImageResource(R.drawable.feed);
                        if (imageUrl != null) {
                            ivPhoto_Post.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhoto_Post, mAvatarTransformation, imageUrl, Constants.PICASSO_TAG);
                        } else {
                            ivPhoto_Post.setVisibility(View.GONE);
                        }
                        break;
                    case Types.LINK:
                    case Types.WIKI_PAGE:
                        ivType.setVisibility(View.VISIBLE);
                        if (imageUrl != null) {
                            ivPhotoT.setVisibility(View.VISIBLE);
                            ViewUtils.displayAvatar(ivPhotoT, null, imageUrl, Constants.PICASSO_TAG);
                        } else {
                            ivPhotoT.setVisibility(View.GONE);
                        }
                        ivType.setImageResource(R.drawable.attachment);
                        break;
                    case Types.NOT_SUPPORTED:
                        ivType.setVisibility(View.GONE);
                        ivPhotoT.setVisibility(View.VISIBLE);
                        ivPhotoT.setImageResource(R.drawable.not_supported);
                        break;
                    case Types.POLL:
                        ivType.setVisibility(View.VISIBLE);
                        ivPhotoT.setVisibility(View.GONE);
                        ivType.setImageResource(R.drawable.chart_bar);
                        break;
                    case Types.CALL:
                        ivType.setVisibility(View.VISIBLE);
                        ivPhotoT.setVisibility(View.GONE);
                        ivType.setImageResource(R.drawable.phone_call);
                        break;
                    default:
                        ivType.setVisibility(View.GONE);
                        ivPhotoT.setVisibility(View.GONE);
                        break;
                }

            } else {
                itemView.setVisibility(View.GONE);
                itemView.setTag(null);
            }
        }
    }

    private void displayArticles(List<Article> articles, ViewGroup root) {
        root.setVisibility(safeIsEmpty(articles) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(articles)) {
            return;
        }

        int i = articles.size() - root.getChildCount();
        for (int j = 0; j < i; j++) {
            root.addView(LayoutInflater.from(mContext).inflate(R.layout.item_article, root, false));
        }

        for (int g = 0; g < root.getChildCount(); g++) {
            ViewGroup itemView = (ViewGroup) root.getChildAt(g);
            if (g < articles.size()) {
                Article article = articles.get(g);
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(article);

                ImageView ivPhoto = itemView.findViewById(R.id.item_article_image);
                TextView ivSubTitle = itemView.findViewById(R.id.item_article_subtitle);

                TextView ivTitle = itemView.findViewById(R.id.item_article_title);
                TextView ivName = itemView.findViewById(R.id.item_article_name);

                ImageView btFave = itemView.findViewById(R.id.item_article_to_fave);
                ImageView btShare = itemView.findViewById(R.id.item_article_share);
                Button ivButton = itemView.findViewById(R.id.item_article_read);
                if (article.getURL() != null) {
                    btFave.setVisibility(View.VISIBLE);
                    btFave.setImageResource(article.getIsFavorite() ? R.drawable.favorite : R.drawable.star);
                    btFave.setOnClickListener(v -> {
                        mAttachmentsActionCallback.onFaveArticle(article);
                        article.setIsFavorite(!article.getIsFavorite());
                        btFave.setImageResource(article.getIsFavorite() ? R.drawable.favorite : R.drawable.star);
                    });
                    ivButton.setVisibility(View.VISIBLE);
                    ivButton.setOnClickListener(v -> mAttachmentsActionCallback.onUrlOpen(article.getURL()));
                } else {
                    ivButton.setVisibility(View.GONE);
                    btFave.setVisibility(View.GONE);
                }

                btShare.setOnClickListener(v -> mAttachmentsActionCallback.onShareArticle(article));

                String photo_url = null;
                if (article.getPhoto() != null) {
                    photo_url = article.getPhoto().getUrlForSize(Settings.get().main().getPrefPreviewImageSize(), false);
                }

                if (photo_url != null) {
                    ivPhoto.setVisibility(View.VISIBLE);
                    ViewUtils.displayAvatar(ivPhoto, null, photo_url, Constants.PICASSO_TAG);
                    ivPhoto.setOnLongClickListener(v -> {
                        ArrayList<Photo> temp = new ArrayList<>(Collections.singletonList(article.getPhoto()));
                        mAttachmentsActionCallback.onPhotosOpen(temp, 0, false);
                        return true;
                    });
                } else
                    ivPhoto.setVisibility(View.GONE);

                if (article.getSubTitle() != null) {
                    ivSubTitle.setVisibility(View.VISIBLE);
                    ivSubTitle.setText(article.getSubTitle());
                } else
                    ivSubTitle.setVisibility(View.GONE);

                if (article.getTitle() != null) {
                    ivTitle.setVisibility(View.VISIBLE);
                    ivTitle.setText(article.getTitle());
                } else
                    ivTitle.setVisibility(View.GONE);

                if (article.getOwnerName() != null) {
                    ivName.setVisibility(View.VISIBLE);
                    ivName.setText(article.getOwnerName());
                } else
                    ivName.setVisibility(View.GONE);

            } else {
                itemView.setVisibility(View.GONE);
                itemView.setTag(null);
            }
        }
    }

    private void openDocLink(DocLink link) {
        switch (link.getType()) {
            case Types.DOC:
                mAttachmentsActionCallback.onDocPreviewOpen((Document) link.attachment);
                break;
            case Types.POST:
                mAttachmentsActionCallback.onPostOpen((Post) link.attachment);
                break;
            case Types.LINK:
                mAttachmentsActionCallback.onLinkOpen((Link) link.attachment);
                break;
            case Types.POLL:
                mAttachmentsActionCallback.onPollOpen((Poll) link.attachment);
                break;
            case Types.WIKI_PAGE:
                mAttachmentsActionCallback.onWikiPageOpen((WikiPage) link.attachment);
                break;
            case Types.STORY:
                mAttachmentsActionCallback.onStoryOpen((Story) link.attachment);
                break;
            case Types.AUDIO_PLAYLIST:
                mAttachmentsActionCallback.onAudioPlaylistOpen((AudioPlaylist) link.attachment);
                break;
            case Types.WALL_REPLY:
                mAttachmentsActionCallback.onWallReplyOpen((WallReply) link.attachment);
                break;
            case Types.EVENT:
                mAttachmentsActionCallback.onOpenOwner(-1 * Math.abs(((Event) link.attachment).getId()));
                break;
            case Types.ALBUM:
                mAttachmentsActionCallback.onPhotoAlbumOpen((PhotoAlbum) link.attachment);
                break;
            case Types.MARKET_ALBUM:
                mAttachmentsActionCallback.onMarketAlbumOpen((MarketAlbum) link.attachment);
                break;
            case Types.MARKET:
                mAttachmentsActionCallback.onMarketOpen((Market) link.attachment);
                break;
            case Types.ARTIST:
                mAttachmentsActionCallback.onArtistOpen((AudioArtist) link.attachment);
                break;
        }
    }

    public interface VoiceActionListener extends EventListener {
        void onVoiceHolderBinded(int voiceMessageId, int voiceHolderId);

        void onVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage);

        void onVoiceTogglePlaybackSpeed();

        void onTranscript(String voiceMessageId, int messageId);
    }

    public interface OnAttachmentsActionCallback {
        void onPollOpen(@NonNull Poll poll);

        void onVideoPlay(@NonNull Video video);

        void onAudioPlay(int position, @NonNull ArrayList<Audio> audios);

        void onForwardMessagesOpen(@NonNull ArrayList<Message> messages);

        void onOpenOwner(int userId);

        void onGoToMessagesLookup(@NonNull Message message);

        void onDocPreviewOpen(@NonNull Document document);

        void onPostOpen(@NonNull Post post);

        void onLinkOpen(@NonNull Link link);

        void onUrlOpen(@NonNull String url);

        void onFaveArticle(@NonNull Article article);

        void onShareArticle(@NonNull Article article);

        void onWikiPageOpen(@NonNull WikiPage page);

        void onPhotosOpen(@NonNull ArrayList<Photo> photos, int index, boolean refresh);

        void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix);

        void onStoryOpen(@NonNull Story story);

        void onWallReplyOpen(@NonNull WallReply reply);

        void onAudioPlaylistOpen(@NonNull AudioPlaylist playlist);

        void onPhotoAlbumOpen(@NonNull PhotoAlbum album);

        void onMarketAlbumOpen(@NonNull MarketAlbum market_album);

        void onMarketOpen(@NonNull Market market);

        void onArtistOpen(@NonNull AudioArtist artist);

        void onRequestWritePermissions();
    }

    private static final class CopyHolder {
        final ViewGroup itemView;
        final ImageView ivAvatar;
        final TextView ownerName;
        final EmojiconTextView bodyView;
        final View tvShowMore;
        final View buttonDots;
        final AttachmentsHolder attachmentsHolder;
        final OnAttachmentsActionCallback callback;

        CopyHolder(ViewGroup itemView, OnAttachmentsActionCallback callback) {
            this.itemView = itemView;
            bodyView = itemView.findViewById(R.id.item_post_copy_text);
            ivAvatar = itemView.findViewById(R.id.item_copy_history_post_avatar);
            tvShowMore = itemView.findViewById(R.id.item_post_copy_show_more);
            ownerName = itemView.findViewById(R.id.item_post_copy_owner_name);
            buttonDots = itemView.findViewById(R.id.item_copy_history_post_dots);
            attachmentsHolder = AttachmentsHolder.forCopyPost(itemView);
            this.callback = callback;

            buttonDots.setOnClickListener(v -> showDotsMenu());
        }

        void showDotsMenu() {
            PopupMenu menu = new PopupMenu(itemView.getContext(), buttonDots);
            menu.getMenu().add(R.string.open_post).setOnMenuItemClickListener(item -> {
                Post copy = (Post) buttonDots.getTag();
                callback.onPostOpen(copy);
                return true;
            });

            menu.show();
        }
    }

    private static class FriendsPostViewHolder implements IdentificableHolder {
        final OnAttachmentsActionCallback callback;
        final ImageView ivImage;
        final TextView tvTitle;
        final TextView tvDescription;

        private FriendsPostViewHolder(View root, OnAttachmentsActionCallback callback) {
            ivImage = root.findViewById(R.id.item_link_pic);
            tvTitle = root.findViewById(R.id.item_link_name);
            tvDescription = root.findViewById(R.id.item_link_description);
            ivImage.setTag(generateHolderId());
            this.callback = callback;
        }

        @Override
        public int getHolderId() {
            return (int) ivImage.getTag();
        }
    }

    private class VoiceHolder implements IdentificableHolder {

        final WaveFormView mWaveFormView;
        final ImageView mButtonPlay;
        final TextView mDurationText;
        final TextView TranscriptText;
        final TextView mDoTranscript;
        final ImageView mSpeed;

        VoiceHolder(View itemView) {
            mWaveFormView = itemView.findViewById(R.id.item_voice_wave_form_view);
            mWaveFormView.setActiveColor(mActiveWaveFormColor);
            mWaveFormView.setNoactiveColor(mNoactiveWaveFormColor);
            mWaveFormView.setSectionCount(Utils.isLandscape(itemView.getContext()) ? 128 : 64);
            mWaveFormView.setTag(generateHolderId());
            mButtonPlay = itemView.findViewById(R.id.item_voice_button_play);
            mDurationText = itemView.findViewById(R.id.item_voice_duration);

            TranscriptText = itemView.findViewById(R.id.transcription_text);
            mDoTranscript = itemView.findViewById(R.id.item_voice_translate);
            mSpeed = itemView.findViewById(R.id.item_voice_speed);
        }

        @Override
        public int getHolderId() {
            return (int) mWaveFormView.getTag();
        }
    }
}
