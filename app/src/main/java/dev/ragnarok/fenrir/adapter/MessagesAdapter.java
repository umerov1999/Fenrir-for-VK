package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.AppTextUtils.getDateFromUnixTime;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.CryptStatus;
import dev.ragnarok.fenrir.model.GiftItem;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.MessageStatus;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.MessageView;
import dev.ragnarok.fenrir.view.OnlineView;
import dev.ragnarok.fenrir.view.emoji.BotKeyboardView;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class MessagesAdapter extends RecyclerBindableAdapter<Message, RecyclerView.ViewHolder> {
    private static final int TYPE_MY_MESSAGE = 1;
    private static final int TYPE_FRIEND_MESSAGE = 2;
    private static final int TYPE_SERVICE = 3;
    private static final int TYPE_STICKER_MY = 4;
    private static final int TYPE_STICKER_FRIEND = 5;
    private static final int TYPE_GIFT_MY = 6;
    private static final int TYPE_GIFT_FRIEND = 7;
    private static final int TYPE_GRAFFITY_MY = 8;
    private static final int TYPE_GRAFFITY_FRIEND = 9;
    private static final Date DATE = new Date();
    private final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm", Utils.getAppLocale());
    private final Context context;
    private final AttachmentsViewBinder attachmentsViewBinder;
    private final Transformation avatarTransformation;
    private final ShapeDrawable selectedDrawable;
    private final int unreadColor;
    private final boolean disable_read;
    private final boolean isNightStiker;
    private final AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback;
    private final OwnerLinkSpanFactory.ActionListener ownerLinkAdapter = new LinkActionAdapter() {
        @Override
        public void onOwnerClick(int ownerId) {
            if (nonNull(attachmentsActionCallback)) {
                attachmentsActionCallback.onOpenOwner(ownerId);
            }
        }
    };
    private EmojiconTextView.OnHashTagClickListener onHashTagClickListener;
    private OnMessageActionListener onMessageActionListener;
    private LastReadId lastReadId;

    public MessagesAdapter(Context context, List<Message> items, AttachmentsViewBinder.OnAttachmentsActionCallback callback, boolean disable_read) {
        this(context, items, new LastReadId(0, 0), callback, disable_read);
    }

    public MessagesAdapter(Context context, List<Message> items, LastReadId lastReadId, AttachmentsViewBinder.OnAttachmentsActionCallback callback, boolean disable_read) {
        super(items);
        this.context = context;
        this.lastReadId = lastReadId;
        attachmentsActionCallback = callback;
        attachmentsViewBinder = new AttachmentsViewBinder(context, callback);
        avatarTransformation = CurrentTheme.createTransformationForAvatar();
        selectedDrawable = new ShapeDrawable(new OvalShape());
        selectedDrawable.getPaint().setColor(CurrentTheme.getColorPrimary(context));
        unreadColor = CurrentTheme.getMessageUnreadColor(context);
        this.disable_read = disable_read;
        isNightStiker = Settings.get().ui().isStickers_by_theme() && Settings.get().ui().isDarkModeEnabled(context);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        Message message = getItem(position);
        switch (type) {
            case TYPE_SERVICE:
                bindServiceHolder((ServiceMessageHolder) viewHolder, message, position);
                break;
            case TYPE_GRAFFITY_FRIEND:
            case TYPE_GRAFFITY_MY:
            case TYPE_MY_MESSAGE:
            case TYPE_FRIEND_MESSAGE:
                bindNormalMessage((MessageHolder) viewHolder, message, position);
                break;
            case TYPE_STICKER_FRIEND:
            case TYPE_STICKER_MY:
                bindStickerHolder((StickerMessageHolder) viewHolder, message, position);
                break;
            case TYPE_GIFT_FRIEND:
            case TYPE_GIFT_MY:
                bindGiftHolder((GiftMessageHolder) viewHolder, message, position);
                break;
        }
    }

    private void bindStickerHolder(StickerMessageHolder holder, Message message, int position) {
        bindBaseMessageHolder(holder, message, position);

        if (message.isDeleted()) {
            holder.root.setAlpha(0.6f);
            holder.Restore.setVisibility(View.VISIBLE);
            holder.Restore.setOnClickListener(v -> {
                if (onMessageActionListener != null) {
                    onMessageActionListener.onRestoreClick(message, holder.getBindingAdapterPosition());
                }
            });
        } else {
            holder.root.setAlpha(1);
            holder.Restore.setVisibility(View.GONE);
        }

        Sticker sticker = message.getAttachments().getStickers().get(0);
        if (sticker.isAnimated()) {
            holder.sticker.fromNet(sticker.getAnimationByType(isNightStiker ? "dark" : "light"), Utils.createOkHttp(5), Utils.dp(128), Utils.dp(128));
        } else {
            Sticker.Image image = sticker.getImage(256, isNightStiker);

            PicassoInstance.with()
                    .load(image.getUrl())
                    .into(holder.sticker);
        }

        holder.sticker.setOnLongClickListener(v -> {
            if (!AppPerms.hasReadWriteStoragePermission(context)) {
                if (attachmentsActionCallback != null) {
                    attachmentsActionCallback.onRequestWritePermissions();
                }
            } else {
                DownloadWorkUtils.doDownloadSticker(context, sticker);
            }
            return true;
        });

        boolean hasAttachments = Utils.nonEmpty(message.getFwd()) || (nonNull(message.getAttachments()) && message.getAttachments().size_no_stickers() > 0);
        holder.attachmentsRoot.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);

        if (hasAttachments) {
            attachmentsViewBinder.displayAttachments(message.getAttachments(), holder.attachmentsHolder, true, message.getId());
            attachmentsViewBinder.displayForwards(message.getFwd(), holder.forwardMessagesRoot, context, true);
        }
    }

    public void setItems(List<Message> messages, LastReadId lastReadId) {
        this.lastReadId = lastReadId;
        setItems(messages);
    }

    private void bindGiftHolder(GiftMessageHolder holder, Message message, int position) {
        bindBaseMessageHolder(holder, message, position);

        if (message.isDeleted()) {
            holder.root.setAlpha(0.6f);
            holder.Restore.setVisibility(View.VISIBLE);
            holder.Restore.setOnClickListener(v -> {
                if (onMessageActionListener != null) {
                    onMessageActionListener.onRestoreClick(message, holder.getBindingAdapterPosition());
                }
            });
        } else {
            holder.root.setAlpha(1);
            holder.Restore.setVisibility(View.GONE);
        }

        holder.message.setVisibility(TextUtils.isEmpty(message.getBody()) ? View.GONE : View.VISIBLE);
        holder.message.setText(OwnerLinkSpanFactory.withSpans(message.getBody(), true, false, ownerLinkAdapter));
        GiftItem giftItem = message.getAttachments().getGifts().get(0);

        PicassoInstance.with()
                .load(giftItem.getThumb256())
                .into(holder.gift);
    }

    private void bindStatusText(TextView textView, int status, long time, long updateTime) {
        switch (status) {
            case MessageStatus.SENDING:
                textView.setText(context.getString(R.string.sending));
                textView.setTextColor(ContextCompat.getColor(context, R.color.default_message_status));
                break;
            case MessageStatus.QUEUE:
                textView.setText(context.getString(R.string.in_order));
                textView.setTextColor(ContextCompat.getColor(context, R.color.default_message_status));
                break;
            case MessageStatus.ERROR:
                textView.setText(context.getString(R.string.error));
                textView.setTextColor(Color.RED);
                break;
            case MessageStatus.WAITING_FOR_UPLOAD:
                textView.setText(R.string.waiting_for_upload);
                textView.setTextColor(ContextCompat.getColor(context, R.color.default_message_status));
                break;
            default:
                String text = getDateFromUnixTime(time);

                if (updateTime != 0) {
                    DATE.setTime(updateTime * 1000);
                    text = text + " " + context.getString(R.string.message_edited_at, df.format(DATE));
                }

                textView.setText(text);
                textView.setTextColor(CurrentTheme.getSecondaryTextColorCode(context));
                break;
        }
    }

    private void bindReadState(View root, boolean read) {
        root.setBackgroundColor(read ? Color.TRANSPARENT : unreadColor);
        root.getBackground().setAlpha(60);
    }

    private void bindBaseMessageHolder(BaseMessageHolder holder, Message message, int position) {
        holder.important.setVisibility(message.isImportant() ? View.VISIBLE : View.GONE);

        bindStatusText(holder.status, message.getStatus(), message.getDate(), message.getUpdateTime());

        boolean read = message.isOut() ? lastReadId.getOutgoing() >= message.getId() : lastReadId.getIncoming() >= message.getId();
        if (disable_read)
            read = true;

        bindReadState(holder.itemView, message.getStatus() == MessageStatus.SENT && read);

        if (message.isSelected()) {
            holder.itemView.setBackgroundColor(CurrentTheme.getColorSecondary(context));
            holder.itemView.getBackground().setAlpha(80);
            holder.avatar.setBackground(selectedDrawable);
            holder.avatar.setImageResource(R.drawable.ic_message_check_vector);
        } else {
            String avaurl = message.getSender() != null ? message.getSender().getMaxSquareAvatar() : null;
            ViewUtils.displayAvatar(holder.avatar, avatarTransformation, avaurl, Constants.PICASSO_TAG);

            holder.avatar.setBackgroundColor(Color.TRANSPARENT);
        }
        if (holder.user != null)
            holder.user.setText(message.getSender().getFullName());

        holder.avatar.setOnClickListener(v -> {
            if (nonNull(onMessageActionListener)) {
                onMessageActionListener.onAvatarClick(message, message.getSenderId(), position);
            }
        });

        holder.avatar.setOnLongClickListener(v -> {
            if (nonNull(onMessageActionListener)) {
                onMessageActionListener.onLongAvatarClick(message, message.getSenderId(), position);
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (nonNull(onMessageActionListener)) {
                onMessageActionListener.onMessageClicked(message, position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> nonNull(onMessageActionListener)
                && onMessageActionListener.onMessageLongClick(message, position));
    }

    private void bindNormalMessage(MessageHolder holder, Message message, int position) {
        bindBaseMessageHolder(holder, message, position);

        if (nonNull(holder.botKeyboardView)) {
            if (nonNull(message.getKeyboard()) && message.getKeyboard().getInline() && message.getKeyboard().getButtons().size() > 0) {
                holder.botKeyboardView.setVisibility(View.VISIBLE);
                holder.botKeyboardView.setButtons(message.getKeyboard().getButtons(), false);
            } else {
                holder.botKeyboardView.setVisibility(View.GONE);
            }

            holder.botKeyboardView.setDelegate((button, needClose) -> {
                if (onMessageActionListener != null) {
                    onMessageActionListener.onBotKeyboardClick(button);
                }
            });
        }

        if (message.isDeleted()) {
            holder.root.setAlpha(0.6f);
            holder.Restore.setVisibility(View.VISIBLE);
            holder.Restore.setOnClickListener(v -> {
                if (onMessageActionListener != null) {
                    onMessageActionListener.onRestoreClick(message, holder.getBindingAdapterPosition());
                }
            });
        } else {
            holder.root.setAlpha(1);
            holder.Restore.setVisibility(View.GONE);
        }

        holder.body.setVisibility(TextUtils.isEmpty(message.getBody()) ? View.GONE : View.VISIBLE);
        String displayedBody = null;

        switch (message.getCryptStatus()) {
            case CryptStatus.NO_ENCRYPTION:
            case CryptStatus.ENCRYPTED:
            case CryptStatus.DECRYPT_FAILED:
                displayedBody = message.getBody();
                break;
            case CryptStatus.DECRYPTED:
                displayedBody = message.getDecryptedBody();
                break;
        }

        if (!message.isGraffity()) {
            switch (message.getCryptStatus()) {
                case CryptStatus.ENCRYPTED:
                case CryptStatus.DECRYPT_FAILED:
                    holder.bubble.setNonGradientColor(Color.parseColor("#D4ff0000"));
                    break;
                case CryptStatus.NO_ENCRYPTION:
                case CryptStatus.DECRYPTED:
                    if (message.isOut()) {
                        if (Settings.get().other().isCustom_MyMessage())
                            holder.bubble.setGradientColor(Settings.get().other().getColorMyMessage(), Settings.get().other().getSecondColorMyMessage());
                        else {
                            if (Settings.get().main().isMy_message_no_color())
                                holder.bubble.setNonGradientColor(CurrentTheme.getColorFromAttrs(R.attr.message_bubble_color, context, "#D4ff0000"));
                            else {
                                holder.bubble.setGradientColor(CurrentTheme.getColorFromAttrs(R.attr.my_messages_bubble_color, context, "#D4ff0000"),
                                        CurrentTheme.getColorFromAttrs(R.attr.my_messages_secondary_bubble_color, context, "#D4ff0000"));
                            }
                        }
                    } else
                        holder.bubble.setNonGradientColor(CurrentTheme.getColorFromAttrs(R.attr.message_bubble_color, context, "#D4ff0000"));
                    break;
            }
        }

        holder.body.setText(OwnerLinkSpanFactory.withSpans(displayedBody, true, false, ownerLinkAdapter));
        holder.encryptedView.setVisibility(message.getCryptStatus() == CryptStatus.NO_ENCRYPTION ? View.GONE : View.VISIBLE);

        boolean hasAttachments = Utils.nonEmpty(message.getFwd()) || (nonNull(message.getAttachments()) && message.getAttachments().size() > 0);
        holder.attachmentsRoot.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);

        if (hasAttachments) {
            attachmentsViewBinder.displayAttachments(message.getAttachments(), holder.attachmentsHolder, true, message.getId());
            attachmentsViewBinder.displayForwards(message.getFwd(), holder.forwardMessagesRoot, context, true);
        }
    }

    private void bindServiceHolder(ServiceMessageHolder holder, Message message, int position) {
        if (message.isDeleted()) {
            holder.root.setAlpha(0.6f);
            holder.Restore.setVisibility(View.VISIBLE);
            holder.Restore.setOnClickListener(v -> {
                if (onMessageActionListener != null) {
                    onMessageActionListener.onRestoreClick(message, holder.getBindingAdapterPosition());
                }
            });
        } else {
            holder.root.setAlpha(1);
            holder.Restore.setVisibility(View.GONE);
        }

        if (nonNull(holder.botKeyboardView)) {
            if (nonNull(message.getKeyboard()) && message.getKeyboard().getInline() && message.getKeyboard().getButtons().size() > 0) {
                holder.botKeyboardView.setVisibility(View.VISIBLE);
                holder.botKeyboardView.setButtons(message.getKeyboard().getButtons(), false);
            } else {
                holder.botKeyboardView.setVisibility(View.GONE);
            }

            holder.botKeyboardView.setDelegate((button, need_close) -> {
                if (onMessageActionListener != null) {
                    onMessageActionListener.onBotKeyboardClick(button);
                }
            });
        }

        boolean read = message.isOut() ? lastReadId.getOutgoing() >= message.getId() : lastReadId.getIncoming() >= message.getId();
        if (disable_read)
            read = true;
        bindReadState(holder.itemView, message.getStatus() == MessageStatus.SENT && read);
        holder.tvAction.setText(message.getServiceText(context));
        holder.itemView.setOnClickListener(v -> {
            if (nonNull(onMessageActionListener)) {
                onMessageActionListener.onMessageClicked(message, position);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (nonNull(onMessageActionListener)) {
                onMessageActionListener.onMessageDelete(message);
            }
            return true;
        });
        boolean hasAttachments = Utils.nonEmpty(message.getFwd()) || (nonNull(message.getAttachments()) && message.getAttachments().size() > 0);
        holder.attachmentsRoot.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);
        attachmentsViewBinder.displayAttachments(message.getAttachments(), holder.mAttachmentsHolder, true, message.getId());
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type) {
            case TYPE_GRAFFITY_FRIEND:
            case TYPE_GRAFFITY_MY:
            case TYPE_MY_MESSAGE:
            case TYPE_FRIEND_MESSAGE:
                return new MessageHolder(view);
            case TYPE_SERVICE:
                return new ServiceMessageHolder(view);
            case TYPE_STICKER_FRIEND:
            case TYPE_STICKER_MY:
                return new StickerMessageHolder(view);
            case TYPE_GIFT_FRIEND:
            case TYPE_GIFT_MY:
                return new GiftMessageHolder(view);
        }

        return null;
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case TYPE_MY_MESSAGE:
                return R.layout.item_message_my;
            case TYPE_FRIEND_MESSAGE:
                return R.layout.item_message_friend;
            case TYPE_GRAFFITY_MY:
                return R.layout.item_message_graffity_my;
            case TYPE_GRAFFITY_FRIEND:
                return R.layout.item_message_graffity_friend;
            case TYPE_SERVICE:
                return R.layout.item_service_message;
            case TYPE_STICKER_FRIEND:
                return R.layout.item_message_friend_sticker;
            case TYPE_STICKER_MY:
                return R.layout.item_message_my_sticker;
            case TYPE_GIFT_FRIEND:
                return R.layout.item_message_friend_gift;
            case TYPE_GIFT_MY:
                return R.layout.item_message_my_gift;
        }

        throw new IllegalArgumentException();
    }

    @Override
    protected int getItemType(int position) {
        Message m = getItem(position - getHeadersCount());
        if (m.isServiseMessage()) {
            return TYPE_SERVICE;
        }

        if (m.isSticker()) {
            return m.isOut() ? TYPE_STICKER_MY : TYPE_STICKER_FRIEND;
        }

        if (m.isGraffity()) {
            return m.isOut() ? TYPE_GRAFFITY_MY : TYPE_GRAFFITY_FRIEND;
        }

        if (m.isGift()) {
            return m.isOut() ? TYPE_GIFT_MY : TYPE_GIFT_FRIEND;
        }

        return m.isOut() ? TYPE_MY_MESSAGE : TYPE_FRIEND_MESSAGE;
    }

    public void setVoiceActionListener(AttachmentsViewBinder.VoiceActionListener voiceActionListener) {
        attachmentsViewBinder.setVoiceActionListener(voiceActionListener);
    }

    public void configNowVoiceMessagePlaying(int voiceId, float progress, boolean paused, boolean amin, boolean speed) {
        attachmentsViewBinder.configNowVoiceMessagePlaying(voiceId, progress, paused, amin, speed);
    }

    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin, boolean speed) {
        attachmentsViewBinder.bindVoiceHolderById(holderId, play, paused, progress, amin, speed);
    }

    public void disableVoiceMessagePlaying() {
        attachmentsViewBinder.disableVoiceMessagePlaying();
    }

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        this.onHashTagClickListener = onHashTagClickListener;
    }

    public void setOnMessageActionListener(OnMessageActionListener onMessageActionListener) {
        this.onMessageActionListener = onMessageActionListener;
    }

    public interface OnMessageActionListener {
        void onAvatarClick(@NonNull Message message, int userId, int position);

        void onLongAvatarClick(@NonNull Message message, int userId, int position);

        void onRestoreClick(@NonNull Message message, int position);

        void onBotKeyboardClick(@NonNull Keyboard.Button button);

        boolean onMessageLongClick(@NonNull Message message, int position);

        void onMessageClicked(@NonNull Message message, int position);

        void onMessageDelete(@NonNull Message message);
    }

    private static class ServiceMessageHolder extends RecyclerView.ViewHolder {

        final View root;
        final TextView tvAction;
        final View attachmentsRoot;
        final AttachmentsHolder mAttachmentsHolder;
        final BotKeyboardView botKeyboardView;
        final Button Restore;

        ServiceMessageHolder(View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.item_service_message_text);
            root = itemView.findViewById(R.id.item_message_bubble);
            Restore = itemView.findViewById(R.id.item_message_restore);
            botKeyboardView = itemView.findViewById(R.id.input_keyboard_container);

            attachmentsRoot = itemView.findViewById(R.id.item_message_attachment_container);
            mAttachmentsHolder = new AttachmentsHolder();
            mAttachmentsHolder.setVgAudios(itemView.findViewById(R.id.audio_attachments)).
                    setVgVideos(itemView.findViewById(R.id.video_attachments)).
                    setVgDocs(itemView.findViewById(R.id.docs_attachments)).
                    setVgArticles(itemView.findViewById(R.id.articles_attachments)).
                    setVgPhotos(itemView.findViewById(R.id.photo_attachments)).
                    setVgPosts(itemView.findViewById(R.id.posts_attachments)).
                    setVgStickers(itemView.findViewById(R.id.stickers_attachments));
        }
    }

    private static class StickerMessageHolder extends BaseMessageHolder {

        final RLottieImageView sticker;

        final View attachmentsRoot;
        final AttachmentsHolder attachmentsHolder;
        final ViewGroup forwardMessagesRoot;

        StickerMessageHolder(View itemView) {
            super(itemView);
            sticker = itemView.findViewById(R.id.sticker);
            forwardMessagesRoot = itemView.findViewById(R.id.forward_messages);
            attachmentsRoot = itemView.findViewById(R.id.item_message_attachment_container);
            attachmentsHolder = new AttachmentsHolder();
            attachmentsHolder.setVgAudios(attachmentsRoot.findViewById(R.id.audio_attachments))
                    .setVgVideos(attachmentsRoot.findViewById(R.id.video_attachments))
                    .setVgDocs(attachmentsRoot.findViewById(R.id.docs_attachments))
                    .setVgArticles(attachmentsRoot.findViewById(R.id.articles_attachments))
                    .setVgPhotos(attachmentsRoot.findViewById(R.id.photo_attachments))
                    .setVgPosts(attachmentsRoot.findViewById(R.id.posts_attachments))
                    .setVoiceMessageRoot(attachmentsRoot.findViewById(R.id.voice_message_attachments));
        }
    }

    private abstract static class BaseMessageHolder extends RecyclerView.ViewHolder {

        final View root;
        final EmojiconTextView user;
        final TextView status;
        final ImageView avatar;
        final OnlineView important;
        final Button Restore;

        BaseMessageHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.message_container);
            Restore = itemView.findViewById(R.id.item_message_restore);
            user = itemView.findViewById(R.id.item_message_user);
            status = itemView.findViewById(R.id.item_message_status_text);
            important = itemView.findViewById(R.id.item_message_important);
            avatar = itemView.findViewById(R.id.item_message_avatar);
        }
    }

    private class GiftMessageHolder extends BaseMessageHolder {
        final ImageView gift;
        final EmojiconTextView message;

        GiftMessageHolder(View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.item_message_text);
            message.setMovementMethod(LinkMovementMethod.getInstance());
            message.setOnHashTagClickListener(onHashTagClickListener);
            message.setOnLongClickListener(v -> this.itemView.performLongClick());
            message.setOnClickListener(v -> this.itemView.performClick());
            gift = itemView.findViewById(R.id.gift);
        }
    }

    private class MessageHolder extends BaseMessageHolder {
        final EmojiconTextView body;
        final ViewGroup forwardMessagesRoot;
        final MessageView bubble;
        final View attachmentsRoot;
        final AttachmentsHolder attachmentsHolder;
        final View encryptedView;
        final BotKeyboardView botKeyboardView;

        MessageHolder(View itemView) {
            super(itemView);
            encryptedView = itemView.findViewById(R.id.item_message_encrypted);
            botKeyboardView = itemView.findViewById(R.id.input_keyboard_container);

            body = itemView.findViewById(R.id.item_message_text);
            body.setMovementMethod(LinkMovementMethod.getInstance());
            body.setOnHashTagClickListener(onHashTagClickListener);
            body.setOnLongClickListener(v -> this.itemView.performLongClick());
            body.setOnClickListener(v -> this.itemView.performClick());

            forwardMessagesRoot = itemView.findViewById(R.id.forward_messages);
            bubble = itemView.findViewById(R.id.item_message_bubble);

            attachmentsRoot = itemView.findViewById(R.id.item_message_attachment_container);
            attachmentsHolder = new AttachmentsHolder();
            attachmentsHolder.setVgAudios(attachmentsRoot.findViewById(R.id.audio_attachments))
                    .setVgVideos(attachmentsRoot.findViewById(R.id.video_attachments))
                    .setVgDocs(attachmentsRoot.findViewById(R.id.docs_attachments))
                    .setVgArticles(attachmentsRoot.findViewById(R.id.articles_attachments))
                    .setVgPhotos(attachmentsRoot.findViewById(R.id.photo_attachments))
                    .setVgPosts(attachmentsRoot.findViewById(R.id.posts_attachments))
                    .setVoiceMessageRoot(attachmentsRoot.findViewById(R.id.voice_message_attachments));
        }
    }
}
