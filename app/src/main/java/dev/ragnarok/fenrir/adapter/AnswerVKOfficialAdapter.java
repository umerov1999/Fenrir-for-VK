package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.feedback.FeedbackPhotosAdapter;
import dev.ragnarok.fenrir.model.AnswerVKOfficial;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.LinkParser;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class AnswerVKOfficialAdapter extends RecyclerView.Adapter<AnswerVKOfficialAdapter.Holder> {

    private static final int DIV_DISABLE = 0;
    private static final int DIV_TODAY = 1;
    private static final int DIV_YESTERDAY = 2;
    private static final int DIV_THIS_WEEK = 3;
    private static final int DIV_OLD = 4;
    private final Context context;
    private final Transformation transformation;
    private final long mStartOfToday;
    private AnswerVKOfficialList data;
    private ClickListener clickListener;

    public AnswerVKOfficialAdapter(AnswerVKOfficialList data, Context context) {
        this.data = data;
        this.context = context;
        transformation = CurrentTheme.createTransformationForAvatar();
        mStartOfToday = Utils.startOfTodayMillis();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_answer_official, parent, false));
    }

    private void LoadIcon(@NonNull Holder holder, AnswerVKOfficial Page, boolean isSmall) {
        if (!isSmall) {
            holder.avatar.setOnClickListener(v -> {
            });
        }
        Integer IconRes = GetIconResByType(Page.iconType);

        if (IconRes == null && Page.iconURL == null) {
            if (isSmall) {
                holder.small.setVisibility(View.VISIBLE);
                holder.small.setImageResource(R.drawable.client_round);
                Utils.setColorFilter(holder.small, CurrentTheme.getColorPrimary(context));
            } else {
                holder.small.setVisibility(View.INVISIBLE);
                holder.avatar.setImageResource(R.drawable.client_round);
                Utils.setColorFilter(holder.avatar, CurrentTheme.getColorPrimary(context));
            }
            return;
        }
        holder.avatar.clearColorFilter();
        holder.small.clearColorFilter();
        if (IconRes == null) {
            if (isSmall) {
                holder.small.setVisibility(View.VISIBLE);
                ViewUtils.displayAvatar(holder.small, transformation, Page.iconURL, Constants.PICASSO_TAG);
            } else {
                holder.small.setVisibility(View.INVISIBLE);
                ViewUtils.displayAvatar(holder.avatar, transformation, Page.iconURL, Constants.PICASSO_TAG);
            }
            return;
        }
        if (isSmall) {
            holder.small.setVisibility(View.VISIBLE);
            holder.small.setImageResource(IconRes);
        } else {
            holder.small.setVisibility(View.INVISIBLE);
            holder.avatar.setImageResource(IconRes);
        }
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private int getDivided(long messageDateJavaTime, Long previousMessageDateJavaTime) {
        int stCurrent = getStatus(messageDateJavaTime);
        if (previousMessageDateJavaTime == null) {
            return stCurrent;
        } else {
            int stPrevious = getStatus(previousMessageDateJavaTime);
            if (stCurrent == stPrevious) {
                return DIV_DISABLE;
            } else {
                return stCurrent;
            }
        }
    }

    private int getStatus(long time) {
        if (time >= mStartOfToday) {
            return DIV_TODAY;
        }

        if (time >= mStartOfToday - 86400000) {
            return DIV_YESTERDAY;
        }

        if (time >= mStartOfToday - 864000000) {
            return DIV_THIS_WEEK;
        }

        return DIV_OLD;
    }

    @SuppressWarnings("deprecation")
    private CharSequence fromHtml(@NonNull String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AnswerVKOfficial Page = data.items.get(position);
        AnswerVKOfficial previous = position == 0 ? null : data.items.get(position - 1);

        long lastMessageJavaTime = Page.time * 1000;
        int headerStatus = getDivided(lastMessageJavaTime, previous == null ? null : previous.time * 1000);

        switch (headerStatus) {
            case DIV_DISABLE:
                holder.mHeaderTitle.setVisibility(View.GONE);
                break;
            case DIV_OLD:
                holder.mHeaderTitle.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_older);
                break;
            case DIV_TODAY:
                holder.mHeaderTitle.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_today);
                break;
            case DIV_YESTERDAY:
                holder.mHeaderTitle.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_yesterday);
                break;
            case DIV_THIS_WEEK:
                holder.mHeaderTitle.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_ten_days);
                break;
        }

        holder.small.setVisibility(View.INVISIBLE);
        if (!Utils.isEmpty(Page.header)) {
            holder.name.setVisibility(View.VISIBLE);
            SpannableStringBuilder replace = new SpannableStringBuilder(fromHtml(Page.header));
            holder.name.setText(LinkParser.parseLinks(context, replace), TextView.BufferType.SPANNABLE);

            Matcher matcher = LinkParser.MENTIONS_AVATAR_PATTERN.matcher(Page.header);
            if (matcher.find()) {
                String Type = matcher.group(1);
                int Id = Integer.parseInt(Objects.requireNonNull(matcher.group(2)));
                assert Type != null;
                if (Type.equals("event") || Type.equals("club") || Type.equals("public"))
                    Id *= -1;
                String icn = data.getAvatar(Id);
                if (icn != null) {
                    PicassoInstance.with()
                            .load(icn)
                            .tag(Constants.PICASSO_TAG)
                            .placeholder(R.drawable.background_gray)
                            .transform(transformation)
                            .into(holder.avatar);
                    int finalId = Id;
                    holder.avatar.setOnClickListener(v -> clickListener.openOwnerWall(finalId));
                    LoadIcon(holder, Page, true);
                } else {
                    PicassoInstance.with().cancelRequest(holder.avatar);
                    LoadIcon(holder, Page, false);
                }
            } else {
                PicassoInstance.with().cancelRequest(holder.avatar);
                LoadIcon(holder, Page, false);
            }
        } else {
            holder.name.setVisibility(View.GONE);
            LoadIcon(holder, Page, false);
        }
        if (!Utils.isEmpty(Page.text)) {
            holder.description.setVisibility(View.VISIBLE);
            SpannableStringBuilder replace = new SpannableStringBuilder(fromHtml(Page.text));
            holder.description.setText(LinkParser.parseLinks(context, replace), TextView.BufferType.SPANNABLE);
        } else
            holder.description.setVisibility(View.GONE);

        if (!Utils.isEmpty(Page.footer)) {
            holder.footer.setVisibility(View.VISIBLE);
            SpannableStringBuilder replace = new SpannableStringBuilder(fromHtml(Page.footer));
            holder.footer.setText(LinkParser.parseLinks(context, replace), TextView.BufferType.SPANNABLE);
        } else
            holder.footer.setVisibility(View.GONE);
        holder.time.setText(AppTextUtils.getDateFromUnixTime(context, Page.time));
        AnswerVKOfficial.ImageAdditional Img = Page.getImage(256);
        if (Img == null) {
            holder.additional.setVisibility(View.GONE);
            PicassoInstance.with().cancelRequest(holder.additional);
        } else {
            holder.additional.setVisibility(View.VISIBLE);
            PicassoInstance.with()
                    .load(Img.url)
                    .tag(Constants.PICASSO_TAG)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.additional);
        }
        if (Utils.isEmpty(Page.attachments)) {
            PicassoInstance.with().cancelRequest(holder.photo_image);
            holder.photo_image.setVisibility(View.GONE);
            holder.attachments.setVisibility(View.GONE);
            holder.attachments.setAdapter(null);
        } else if (Page.attachments.size() == 1) {
            holder.photo_image.setVisibility(View.VISIBLE);
            holder.attachments.setVisibility(View.GONE);
            holder.attachments.setAdapter(null);
            PicassoInstance.with()
                    .load(Page.attachments.get(0).getUrlForSize(PhotoSize.X, false))
                    .tag(Constants.PICASSO_TAG)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.photo_image);
            holder.photo_image.setOnClickListener(v -> PlaceFactory.getSimpleGalleryPlace(Settings.get().accounts().getCurrent(), new ArrayList<>(Collections.singletonList(Page.attachments.get(0))), 0, true).tryOpenWith(context));
        } else {
            PicassoInstance.with().cancelRequest(holder.photo_image);
            holder.photo_image.setVisibility(View.GONE);
            holder.attachments.setVisibility(View.VISIBLE);
            holder.attachments.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
            FeedbackPhotosAdapter adapter = new FeedbackPhotosAdapter(context, Page.attachments);
            adapter.setPhotoSelectionListener((position1, photo) -> PlaceFactory.getSimpleGalleryPlace(Settings.get().accounts().getCurrent(), new ArrayList<>(Page.attachments), position1, true).tryOpenWith(context));
            holder.attachments.setAdapter(adapter);
        }
        if (nonNull(Page.action)) {
            if (Page.action.getType() == AnswerVKOfficial.Action_Types.URL) {
                holder.actionButton.setText(R.string.more_info);
            } else {
                holder.actionButton.setText(R.string.open);
            }
            holder.actionButton.setVisibility(View.VISIBLE);
        } else {
            holder.actionButton.setVisibility(View.GONE);
        }
        holder.actionButton.setOnClickListener(v -> {
            if (nonNull(Page.action)) {
                clickListener.openAction(Page.action);
            }
        });
    }

    private @Nullable
    Integer GetIconResByType(String IconType) {
        if (IconType == null)
            return null;
        if (IconType.equals("suggested_post_published")) {
            return R.drawable.ic_feedback_suggested_post_published;
        }
        if (IconType.equals("transfer_money_cancelled")) {
            return R.drawable.ic_feedback_transfer_money_cancelled;
        }
        if (IconType.equals("invite_game")) {
            return R.drawable.ic_feedback_invite_app;
        }
        if (IconType.equals("cancel")) {
            return R.drawable.ic_feedback_cancel;
        }
        if (IconType.equals("follow")) {
            return R.drawable.ic_feedback_follow;
        }
        if (IconType.equals("repost")) {
            return R.drawable.ic_feedback_repost;
        }
        if (IconType.equals("story_reply")) {
            return R.drawable.ic_feedback_story_reply;
        }
        if (IconType.equals("photo_tag")) {
            return R.drawable.ic_feedback_photo_tag;
        }
        if (IconType.equals("invite_group_accepted")) {
            return R.drawable.ic_feedback_friend_accepted;
        }
        if (IconType.equals("ads")) {
            return R.drawable.ic_feedback_ads;
        }
        if (IconType.equals("like")) {
            return R.drawable.ic_feedback_like;
        }
        if (IconType.equals("live")) {
            return R.drawable.ic_feedback_live;
        }
        if (IconType.equals("poll")) {
            return R.drawable.ic_feedback_poll;
        }
        if (IconType.equals("wall")) {
            return R.drawable.ic_feedback_wall;
        }
        if (IconType.equals("friend_found")) {
            return R.drawable.ic_feedback_add;
        }
        if (IconType.equals("event")) {
            return R.drawable.ic_feedback_event;
        }
        if (IconType.equals("reply")) {
            return R.drawable.ic_feedback_reply;
        }
        if (IconType.equals("gift")) {
            return R.drawable.ic_feedback_gift;
        }
        if (IconType.equals("friend_suggest")) {
            return R.drawable.ic_feedback_follow;
        }
        if (IconType.equals("invite_group")) {
            return R.drawable.ic_feedback_invite_group;
        }
        if (IconType.equals("friend_accepted")) {
            return R.drawable.ic_feedback_friend_accepted;
        }
        if (IconType.equals("mention")) {
            return R.drawable.ic_feedback_mention;
        }
        if (IconType.equals("comment")) {
            return R.drawable.ic_feedback_comment;
        }
        if (IconType.equals("message")) {
            return R.drawable.ic_feedback_message;
        }
        if (IconType.equals("private_post")) {
            return R.drawable.ic_feedback_private_post;
        }
        if (IconType.equals("birthday")) {
            return R.drawable.ic_feedback_birthday;
        }
        if (IconType.equals("invite_app")) {
            return R.drawable.ic_feedback_invite_app;
        }
        if (IconType.equals("new_post")) {
            return R.drawable.ic_feedback_new_post;
        }
        if (IconType.equals("interesting")) {
            return R.drawable.ic_feedback_interesting;
        }
        if (IconType.equals("transfer_money")) {
            return R.drawable.ic_feedback_transfer_money;
        }
        if (IconType.equals("transfer_votes")) {
            return R.drawable.ic_feedback_transfer_votes;
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (data == null || data.items == null)
            return 0;
        return data.items.size();
    }

    public void setData(AnswerVKOfficialList data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void openOwnerWall(int owner_id);

        void openAction(@NonNull AnswerVKOfficial.Action action);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name;
        final TextView description;
        final TextView footer;
        final TextView time;
        final ImageView small;
        final TextView mHeaderTitle;
        final ShapeableImageView additional;
        final ShapeableImageView photo_image;
        final RecyclerView attachments;
        final MaterialButton actionButton;

        public Holder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.item_friend_avatar);
            name = itemView.findViewById(R.id.item_friend_name);
            name.setMovementMethod(LinkMovementMethod.getInstance());
            description = itemView.findViewById(R.id.item_additional_info);
            description.setMovementMethod(LinkMovementMethod.getInstance());
            footer = itemView.findViewById(R.id.item_friend_footer);
            footer.setMovementMethod(LinkMovementMethod.getInstance());
            time = itemView.findViewById(R.id.item_friend_time);
            small = itemView.findViewById(R.id.item_icon);
            mHeaderTitle = itemView.findViewById(R.id.header_title);
            additional = itemView.findViewById(R.id.additional_image);
            attachments = itemView.findViewById(R.id.attachments);
            photo_image = itemView.findViewById(R.id.photo_image);
            actionButton = itemView.findViewById(R.id.action_button);
        }
    }
}
