package dev.ragnarok.fenrir.util;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.api.model.CommentsDto;
import dev.ragnarok.fenrir.api.model.VKApiAttachment;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VKApiNews;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiStory;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.api.model.VkApiDialog;
import dev.ragnarok.fenrir.api.model.feedback.Copies;
import dev.ragnarok.fenrir.api.model.feedback.UserArray;
import dev.ragnarok.fenrir.api.model.feedback.VkApiUsersFeedback;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Peer;

public class VKOwnIds {

    private final Set<Integer> uids;
    private final Set<Integer> gids;

    public VKOwnIds() {
        uids = new HashSet<>();
        gids = new HashSet<>();
    }

    public static VKOwnIds fromPosts(@NonNull Collection<VKApiPost> posts) {
        VKOwnIds ids = new VKOwnIds();
        for (VKApiPost post : posts) {
            ids.append(post);
        }

        return ids;
    }

    public VKOwnIds append(UserArray userArray) {
        for (int id : userArray.ids) {
            append(id);
        }

        return this;
    }

    public VKOwnIds appendStory(VKApiStory story) {
        append(story.owner_id);
        return this;
    }

    public VKOwnIds append(@NonNull VkApiUsersFeedback dto) {
        append(dto.users);
        return this;
    }

    public VKOwnIds append(@NonNull VKApiTopic topic) {
        append(topic.created_by);
        append(topic.updated_by);
        return this;
    }

    public VKOwnIds append(@NonNull Copies copies) {
        for (Copies.IdPair pair : copies.pairs) {
            append(pair.owner_id);
        }
        return this;
    }

    public VKOwnIds append(CommentsDto commentsDto) {
        if (nonNull(commentsDto) && Utils.nonEmpty(commentsDto.list)) {
            for (VKApiComment comment : commentsDto.list) {
                append(comment);
            }
        }

        return this;
    }

    public VKOwnIds append(@NonNull VKApiComment comment) {
        if (comment.from_id != 0) {
            append(comment.from_id);
        }
        if (comment.attachments != null) {
            append(comment.attachments);
        }
        if (!Utils.isEmpty(comment.threads)) {
            for (VKApiComment i : comment.threads) {
                append(i);
            }
        }

        return this;
    }

    public VKOwnIds appendAttachmentDto(@NonNull VKApiAttachment attachment) {
        if (attachment instanceof VKApiPost) {
            append((VKApiPost) attachment);
        } else if (attachment instanceof VKApiStory) {
            appendStory((VKApiStory) attachment);
        }

        return this;
    }

    public VKOwnIds append(@NonNull VkApiAttachments attachments) {
        List<VkApiAttachments.Entry> entries = attachments.entryList();
        for (VkApiAttachments.Entry entry : entries) {
            appendAttachmentDto(entry.attachment);
        }

        return this;
    }

    @NonNull
    public Collection<Integer> getAll() {
        Collection<Integer> result = new HashSet<>(uids.size() + gids.size());
        result.addAll(uids);

        for (Integer gid : gids) {
            result.add(-Math.abs(gid));
        }

        return result;
    }

    public Set<Integer> getUids() {
        return uids;
    }

    public Set<Integer> getGids() {
        return gids;
    }

    public VKOwnIds appendAll(@NonNull Collection<Integer> ids) {
        for (Integer id : ids) {
            append(id);
        }

        return this;
    }

    public VKOwnIds append(Collection<VKApiMessage> messages) {
        if (messages != null) {
            for (VKApiMessage message : messages) {
                append(message);
            }
        }

        return this;
    }

    public VKOwnIds append(VkApiDialog dialog) {
        if (dialog.lastMessage != null) {
            append(dialog.lastMessage);
        }

        return this;
    }

    public VKOwnIds append(VkApiConversation conversation) {
        if (!Peer.isGroupChat(conversation.peer.id)) {
            append(conversation.peer.id);
        }
        return this;
    }

    public VKOwnIds append(VKApiMessage message) {
        append(message.from_id);
        append(message.action_mid);

        if (!message.isGroupChat()) {
            append(message.peer_id);
        }

        if (message.fwd_messages != null) {
            for (VKApiMessage fwd : message.fwd_messages) {
                append(fwd);
            }
        }

        if (nonNull(message.attachments)) {
            List<VkApiAttachments.Entry> entries = message.attachments.entryList();
            for (VkApiAttachments.Entry entry : entries) {
                if (entry.attachment instanceof VKApiPost) {
                    append((VKApiPost) entry.attachment);
                } else if (entry.attachment instanceof VKApiStory) {
                    appendStory((VKApiStory) entry.attachment);
                }
            }
        }

        return this;
    }

    public VKOwnIds append(@NonNull ArrayList<Message> messages) {
        for (Message message : messages) {
            append(message);
        }
        return this;
    }

    public VKOwnIds append(@NonNull Message message) {
        append(message.getSenderId());
        append(message.getActionMid()); // тут 100% пользователь, нюанс в том, что он может быть < 0, если email

        if (!Peer.isGroupChat(message.getPeerId())) {
            append(message.getPeerId());
        }

        if (nonNull(message.getFwd())) {
            List<Message> forwardMessages = message.getFwd();
            for (Message fwd : forwardMessages) {
                append(fwd);
            }
        }

        return this;
    }

    public VKOwnIds appendNews(@NonNull VKApiNews news) {
        append(news.source_id);
        append(news.copy_owner_id);

        if (news.hasCopyHistory()) {
            for (VKApiPost post : news.copy_history) {
                append(post);
            }
        }

        if (news.hasAttachments()) {
            append(news.attachments);
        }

        if (!Utils.isEmpty(news.friends)) {
            appendAll(news.friends);
        }

        return this;
    }


    public VKOwnIds append(VKApiPost post) {
        //append(post.owner_id);
        append(post.from_id);
        append(post.signer_id);
        append(post.created_by);

        if (post.copy_history != null) {
            for (VKApiPost copy : post.copy_history) {
                append(copy);
            }
        }

        return this;
    }

    public void append(int ownerId) {
        if (ownerId == 0) return;

        if (ownerId > 0) {
            appendUid(ownerId);
        } else {
            appendGid(ownerId);
        }
    }

    public void appendAll(int[] ownerIds) {
        if (ownerIds != null) {
            for (int id : ownerIds) {
                append(id);
            }
        }
    }

    public void appendUid(int uid) {
        uids.add(uid);
    }

    public void appendGid(int gid) {
        gids.add(Math.abs(gid));
    }

    public boolean constainsUids() {
        return !uids.isEmpty();
    }

    public boolean constainsGids() {
        return !gids.isEmpty();
    }

    public boolean isEmpty() {
        return !constainsUids() && !constainsGids();
    }

    public boolean nonEmpty() {
        return constainsGids() || constainsUids();
    }

    @NonNull
    @Override
    public String toString() {
        return "uids: " + uids + ", gids: " + gids;
    }
}