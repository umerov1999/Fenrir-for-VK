package dev.ragnarok.fenrir.domain.mappers;

import static dev.ragnarok.fenrir.domain.mappers.MapUtil.calculateConversationAcl;
import static dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.api.model.FaveLinkDto;
import dev.ragnarok.fenrir.api.model.PhotoSizeDto;
import dev.ragnarok.fenrir.api.model.VKApiArticle;
import dev.ragnarok.fenrir.api.model.VKApiAttachment;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiAudioArtist;
import dev.ragnarok.fenrir.api.model.VKApiAudioCatalog;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.VKApiCall;
import dev.ragnarok.fenrir.api.model.VKApiCatalogLink;
import dev.ragnarok.fenrir.api.model.VKApiChat;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiGift;
import dev.ragnarok.fenrir.api.model.VKApiGiftItem;
import dev.ragnarok.fenrir.api.model.VKApiGraffiti;
import dev.ragnarok.fenrir.api.model.VKApiGroupChats;
import dev.ragnarok.fenrir.api.model.VKApiLink;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VKApiNews;
import dev.ragnarok.fenrir.api.model.VKApiNotSupported;
import dev.ragnarok.fenrir.api.model.VKApiOwner;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.api.model.VKApiPoll;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiShortLink;
import dev.ragnarok.fenrir.api.model.VKApiSticker;
import dev.ragnarok.fenrir.api.model.VKApiStory;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VKApiWallReply;
import dev.ragnarok.fenrir.api.model.VKApiWikiPage;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.VkApiAudioMessage;
import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.api.model.VkApiCover;
import dev.ragnarok.fenrir.api.model.VkApiDialog;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.api.model.VkApiEvent;
import dev.ragnarok.fenrir.api.model.VkApiFriendList;
import dev.ragnarok.fenrir.api.model.VkApiMarket;
import dev.ragnarok.fenrir.api.model.VkApiMarketAlbum;
import dev.ragnarok.fenrir.api.model.VkApiPrivacy;
import dev.ragnarok.fenrir.api.model.feedback.Copies;
import dev.ragnarok.fenrir.api.model.feedback.UserArray;
import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate;
import dev.ragnarok.fenrir.api.model.response.CatalogResponse;
import dev.ragnarok.fenrir.api.model.response.FavePageResponse;
import dev.ragnarok.fenrir.api.util.VKStringUtils;
import dev.ragnarok.fenrir.crypt.CryptHelper;
import dev.ragnarok.fenrir.crypt.MessageType;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Attachments;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioCatalog;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Call;
import dev.ragnarok.fenrir.model.CatalogBlock;
import dev.ragnarok.fenrir.model.Chat;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.CommunityDetails;
import dev.ragnarok.fenrir.model.Conversation;
import dev.ragnarok.fenrir.model.CryptStatus;
import dev.ragnarok.fenrir.model.Dialog;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Event;
import dev.ragnarok.fenrir.model.FaveLink;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.FavePageType;
import dev.ragnarok.fenrir.model.FriendList;
import dev.ragnarok.fenrir.model.Gift;
import dev.ragnarok.fenrir.model.GiftItem;
import dev.ragnarok.fenrir.model.Graffiti;
import dev.ragnarok.fenrir.model.GroupChats;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.MessageStatus;
import dev.ragnarok.fenrir.model.News;
import dev.ragnarok.fenrir.model.NotSupported;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.OwnerType;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoSizes;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.PostSource;
import dev.ragnarok.fenrir.model.Privacy;
import dev.ragnarok.fenrir.model.ShortLink;
import dev.ragnarok.fenrir.model.SimplePrivacy;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.util.Utils;

public class Dto2Model {

    public static FriendList transform(VkApiFriendList dto) {
        return new FriendList(dto.id, dto.name);
    }

    public static PhotoAlbum transform(VKApiPhotoAlbum dto) {
        return new PhotoAlbum(dto.id, dto.owner_id)
                .setSize(dto.size)
                .setTitle(dto.title)
                .setDescription(dto.description)
                .setCanUpload(dto.can_upload)
                .setUpdatedTime(dto.updated)
                .setCreatedTime(dto.created)
                .setSizes(nonNull(dto.photo) ? transform(dto.photo) : PhotoSizes.empty())
                .setUploadByAdminsOnly(dto.upload_by_admins_only)
                .setCommentsDisabled(dto.comments_disabled)
                .setPrivacyView(nonNull(dto.privacy_view) ? transform(dto.privacy_view) : null)
                .setPrivacyComment(nonNull(dto.privacy_comment) ? transform(dto.privacy_comment) : null);
    }

    public static Chat transform(VKApiChat chat) {
        return new Chat(chat.id)
                .setPhoto50(chat.photo_50)
                .setPhoto100(chat.photo_100)
                .setPhoto200(chat.photo_200)
                .setTitle(chat.title);
    }

    public static AudioCatalog.ArtistBlock transform(VKApiAudioCatalog.VKApiArtistBlock block) {
        if (block == null)
            return null;
        String url = null;
        if (!Utils.isEmpty(block.images)) {
            int def = 0;
            for (VKApiAudioCatalog.Image i : block.images) {
                if (i.width * i.height > def) {
                    def = i.width * i.height;
                    url = i.url;
                }
            }
        }
        return new AudioCatalog.ArtistBlock()
                .setName(block.name)
                .setPhoto(url);
    }

    public static Sticker.Image transformStickerImage(VKApiSticker.Image dto) {
        return new Sticker.Image(dto.url, dto.width, dto.height);
    }

    public static Sticker.Animation transformStickerAnimation(VKApiSticker.VKApiAnimation dto) {
        return new Sticker.Animation(dto.url, dto.type);
    }

    public static Sticker transformSticker(VKApiSticker sticker) {
        return new Sticker(sticker.sticker_id)
                .setImages(mapAll(sticker.images, Dto2Model::transformStickerImage))
                .setImagesWithBackground(mapAll(sticker.images_with_background, Dto2Model::transformStickerImage))
                .setAnimations(mapAll(sticker.animations, Dto2Model::transformStickerAnimation))
                .setAnimationUrl(sticker.animation_url);
    }

    public static List<Sticker> transformStickers(List<VKApiSticker> dto) {
        return mapAll(dto, Dto2Model::transformSticker);
    }

    public static List<Audio> transformAudios(List<VKApiAudio> dto) {
        return mapAll(dto, Dto2Model::transform);
    }

    public static List<AudioPlaylist> transformAudioPlaylists(List<VKApiAudioPlaylist> dto) {
        return mapAll(dto, Dto2Model::transform);
    }

    public static List<Link> transformCatalogLinks(List<VKApiCatalogLink> dto) {
        return mapAll(dto, Dto2Model::transform);
    }

    public static Owner transformOwner(VKApiOwner owner) {
        return owner instanceof VKApiUser ? transformUser((VKApiUser) owner) : transformCommunity((VKApiCommunity) owner);
    }

    public static List<Owner> transformOwners(Collection<VKApiUser> users, Collection<VKApiCommunity> communities) {
        List<Owner> owners = new ArrayList<>(safeCountOf(users) + safeCountOf(communities));

        if (nonNull(users)) {
            for (VKApiUser user : users) {
                owners.add(transformUser(user));
            }
        }

        if (nonNull(communities)) {
            for (VKApiCommunity community : communities) {
                owners.add(transformCommunity(community));
            }
        }

        return owners;
    }

    public static CommunityDetails transformCommunityDetails(VKApiCommunity dto) {
        CommunityDetails details = new CommunityDetails()
                .setCanMessage(dto.can_message)
                .setStatus(dto.status)
                .setStatusAudio(nonNull(dto.status_audio) ? transform(dto.status_audio) : null)
                .setFavorite(dto.is_favorite)
                .setSubscribed(dto.is_subscribed);

        if (nonNull(dto.counters)) {
            details.setAllWallCount(dto.counters.all_wall)
                    .setOwnerWallCount(dto.counters.owner_wall)
                    .setPostponedWallCount(dto.counters.postponed_wall)
                    .setSuggestedWallCount(dto.counters.suggest_wall)
                    .setTopicsCount(dto.counters.topics)
                    .setDocsCount(dto.counters.docs)
                    .setPhotosCount(dto.counters.photos)
                    .setAudiosCount(dto.counters.audios)
                    .setVideosCount(dto.counters.videos)
                    .setProductsCount(dto.counters.market)
                    .setArticlesCount(dto.counters.articles)
                    .setChatsCount(dto.counters.chats);
        }

        if (nonNull(dto.cover)) {
            CommunityDetails.Cover cover = new CommunityDetails.Cover()
                    .setEnabled(dto.cover.enabled)
                    .setImages(new ArrayList<>(safeCountOf(dto.cover.images)));

            if (nonNull(dto.cover.images)) {
                for (VkApiCover.Image imageDto : dto.cover.images) {
                    cover.getImages().add(new CommunityDetails.CoverImage(imageDto.url, imageDto.height, imageDto.width));
                }
            }

            details.setCover(cover);
        } else {
            details.setCover(new CommunityDetails.Cover().setEnabled(false));
        }
        details.setDescription(dto.description);

        return details;
    }

    public static GroupChats transformGroupChat(VKApiGroupChats chats) {
        return new GroupChats(chats.id)
                .setInvite_link(chats.invite_link)
                .setIs_closed(chats.is_closed)
                .setPhoto(chats.photo)
                .setTitle(chats.title)
                .setMembers_count(chats.members_count)
                .setLastUpdateTime(chats.last_message_date);
    }

    public static Community transformCommunity(VKApiCommunity community) {
        return new Community(community.id)
                .setName(community.name)
                .setScreenName(community.screen_name)
                .setClosed(community.is_closed)
                .setVerified(community.verified)
                .setAdmin(community.is_admin)
                .setAdminLevel(community.admin_level)
                .setMember(community.is_member)
                .setMemberStatus(community.member_status)
                .setType(community.type)
                .setPhoto50(community.photo_50)
                .setPhoto100(community.photo_100)
                .setPhoto200(community.photo_200)
                .setMembersCount(community.members_count);
    }

    public static GiftItem transform(VKApiGiftItem dto) {
        return new GiftItem(dto.id)
                .setThumb48(dto.thumb_48)
                .setThumb96(dto.thumb_96)
                .setThumb256(dto.thumb_256);
    }

    public static Gift transform(VKApiGift dto) {
        return new Gift(dto.id)
                .setFromId(dto.from_id)
                .setMessage(dto.message)
                .setDate(dto.date)
                .setGiftItem(nonNull(dto.gift) ? transform(dto.gift) : null)
                .setPrivacy(dto.privacy);
    }

    public static List<Community> transformCommunities(List<VKApiCommunity> dtos) {
        return mapAll(dtos, Dto2Model::transformCommunity);
    }

    public static List<GroupChats> transformGroupChats(List<VKApiGroupChats> dtos) {
        return mapAll(dtos, Dto2Model::transformGroupChat);
    }

    public static List<Gift> transformGifts(List<VKApiGift> dtos) {
        return mapAll(dtos, Dto2Model::transform);
    }

    public static List<MarketAlbum> transformMarketAlbums(List<VkApiMarketAlbum> dtos) {
        return mapAll(dtos, Dto2Model::transform);
    }

    public static List<AudioArtist> transformAudioArtist(List<VKApiAudioArtist> dtos) {
        return mapAll(dtos, Dto2Model::transform);
    }

    public static List<Market> transformMarket(List<VkApiMarket> dtos) {
        return mapAll(dtos, Dto2Model::transform);
    }

    public static List<User> transformUsers(List<VKApiUser> dtos) {
        return mapAll(dtos, Dto2Model::transformUser);
    }

    public static List<Video> transformVideos(List<VKApiVideo> dtos) {
        return mapAll(dtos, Dto2Model::transform);
    }

    public static FavePage transformFaveUser(FavePageResponse favePage) {
        int id = 0;
        switch (favePage.type) {
            case FavePageType.USER:
                id = favePage.user.id;
                break;
            case FavePageType.COMMUNITY:
                id = favePage.group.id;
                break;
        }

        FavePage page = new FavePage(id)
                .setDescription(favePage.description)
                .setFaveType(favePage.type)
                .setUpdatedDate(favePage.updated_date);

        if (favePage.user != null) {
            page.setUser(transformUser(favePage.user));
        }

        if (favePage.group != null) {
            page.setGroup(transformCommunity(favePage.group));
        }

        return page;
    }

    public static User transformUser(VKApiUser user) {
        return new User(user.id)
                .setFirstName(user.first_name)
                .setLastName(user.last_name)
                .setOnline(user.online)
                .setOnlineMobile(user.online_mobile)
                .setOnlineApp(user.online_app)
                .setPhoto50(user.photo_50)
                .setPhoto100(user.photo_100)
                .setPhoto200(user.photo_200)
                .setPhotoMax(user.photo_max_orig)
                .setLastSeen(user.last_seen)
                .setPlatform(user.platform)
                .setStatus(user.status)
                .setSex(user.sex)
                .setDomain(user.domain)
                .setFriend(user.is_friend)
                .setFriendStatus(user.friend_status)
                .setCanWritePrivateMessage(user.can_write_private_message)
                .setBlacklisted(user.blacklisted)
                .setBlacklisted_by_me(user.blacklisted_by_me)
                .setCan_access_closed(user.can_access_closed)
                .setVerified(user.verified)
                .setMaiden_name(user.maiden_name);
    }

    @NonNull
    public static VKApiMessage transform(int accountUid, @NonNull AddMessageUpdate update) {
        VKApiMessage message = new VKApiMessage();
        message.id = update.message_id;
        message.out = update.outbox;
        message.important = update.important;
        message.deleted = update.deleted;
        //message.read_state = !update.unread;
        message.peer_id = update.peer_id;
        message.from_id = message.out ? accountUid : (Peer.isGroupChat(update.peer_id) ? update.from : update.peer_id);
        message.body = VKStringUtils.unescape(update.text);
        //message.title = update.subject;
        message.date = update.timestamp;
        message.action_mid = update.sourceMid;
        message.action_text = update.sourceText;
        message.action = update.sourceAct;
        message.random_id = update.random_id;
        message.keyboard = update.keyboard;
        message.payload = update.payload;
        message.update_time = update.edit_time;
        return message;
    }

    public static List<Dialog> transform(int accountId, @NonNull List<VkApiDialog> dtos, @NonNull IOwnersBundle owners) {
        List<Dialog> data = new ArrayList<>(dtos.size());
        for (VkApiDialog dto : dtos) {
            data.add(transform(accountId, dto, owners));
        }

        return data;
    }

    public static Keyboard mapKeyboard(VkApiConversation.CurrentKeyboard keyboard) {
        if (keyboard == null || Utils.isEmpty(keyboard.buttons)) {
            return null;
        }
        List<List<Keyboard.Button>> buttons = new ArrayList<>();
        for (List<VkApiConversation.ButtonElement> i : keyboard.buttons) {
            List<Keyboard.Button> v = new ArrayList<>();
            for (VkApiConversation.ButtonElement s : i) {
                if (isNull(s.action) || (!"text".equals(s.action.type) && !"open_link".equals(s.action.type))) {
                    continue;
                }
                v.add(new Keyboard.Button().setType(s.action.type).setColor(s.color).setLabel(s.action.label).setLink(s.action.link).setPayload(s.action.payload));
            }
            buttons.add(v);
        }
        if (!Utils.isEmpty(buttons)) {
            return new Keyboard().setAuthor_id(keyboard.author_id)
                    .setInline(keyboard.inline)
                    .setOne_time(keyboard.one_time)
                    .setButtons(buttons);
        }
        return null;
    }

    public static Conversation transform(int accountId, @NonNull VkApiConversation dto, @NonNull IOwnersBundle bundle) {
        Conversation entity = new Conversation(dto.peer.id)
                .setInRead(dto.inRead)
                .setOutRead(dto.outRead)
                .setUnreadCount(dto.unreadCount)
                .setAcl(calculateConversationAcl(dto));

        if (!Peer.isGroupChat(dto.peer.id)) {
            Owner own = bundle.getById(dto.peer.id);
            entity.setTitle(own.getFullName());
            entity.setPhoto50(own.get100photoOrSmaller());
            entity.setPhoto100(own.get100photoOrSmaller());
            entity.setPhoto200(own.getMaxSquareAvatar());
        }

        if (nonNull(dto.settings)) {
            entity.setTitle(dto.settings.title);

            if (nonNull(dto.settings.pinnedMesage)) {
                entity.setPinned(transform(accountId, dto.settings.pinnedMesage, bundle));
            }

            if (nonNull(dto.settings.photo)) {
                entity.setPhoto50(dto.settings.photo.photo50)
                        .setPhoto100(dto.settings.photo.photo100)
                        .setPhoto200(dto.settings.photo.photo200);
            }
        }

        if (nonNull(dto.sort_id)) {
            entity.setMajor_id(dto.sort_id.major_id);
            entity.setMinor_id(dto.sort_id.minor_id);
        }
        entity.setCurrentKeyboard(mapKeyboard(dto.current_keyboard));
        return entity;
    }

    public static Dialog transform(int accountId, @NonNull VkApiDialog dto, @NonNull IOwnersBundle bundle) {
        VKApiMessage message = dto.lastMessage;

        Owner interlocutor;
        if (Peer.isGroup(message.peer_id) || Peer.isUser(message.peer_id)) {
            interlocutor = bundle.getById(message.peer_id);
        } else {
            interlocutor = bundle.getById(message.from_id);
        }

        Dialog dialog = new Dialog()
                .setPeerId(message.peer_id)
                .setUnreadCount(dto.conversation.unreadCount)
                .setInRead(dto.conversation.inRead)
                .setOutRead(dto.conversation.outRead)
                .setMessage(transform(accountId, message, bundle))
                .setLastMessageId(message.id)
                .setInterlocutor(interlocutor);

        if (nonNull(dto.conversation.settings)) {
            dialog.setTitle(dto.conversation.settings.title);
            dialog.setGroupChannel(dto.conversation.settings.is_group_channel);

            if (nonNull(dto.conversation.settings.photo)) {
                dialog.setPhoto50(dto.conversation.settings.photo.photo50)
                        .setPhoto100(dto.conversation.settings.photo.photo100)
                        .setPhoto200(dto.conversation.settings.photo.photo200);
            }
        }
        if (nonNull(dto.conversation.sort_id)) {
            dialog.setMajor_id(dto.conversation.sort_id.major_id);
            dialog.setMinor_id(dto.conversation.sort_id.minor_id);
        }

        return dialog;
    }

    public static List<Message> transformMessages(int aid, List<VKApiMessage> dtos, @NonNull IOwnersBundle owners) {
        List<Message> data = new ArrayList<>(dtos.size());
        for (VKApiMessage dto : dtos) {
            data.add(transform(aid, dto, owners));
        }
        return data;
    }

    public static Keyboard transformKeyboard(VkApiConversation.CurrentKeyboard keyboard) {
        if (keyboard == null || Utils.isEmpty(keyboard.buttons)) {
            return null;
        }
        List<List<Keyboard.Button>> buttons = new ArrayList<>();
        for (List<VkApiConversation.ButtonElement> i : keyboard.buttons) {
            List<Keyboard.Button> v = new ArrayList<>();
            for (VkApiConversation.ButtonElement s : i) {
                if (isNull(s.action) || (!"text".equals(s.action.type) && !"open_link".equals(s.action.type))) {
                    continue;
                }
                v.add(new Keyboard.Button().setType(s.action.type).setColor(s.color).setLabel(s.action.label).setLink(s.action.link).setPayload(s.action.payload));
            }
            buttons.add(v);
        }
        if (!Utils.isEmpty(buttons)) {
            return new Keyboard().setAuthor_id(keyboard.author_id)
                    .setInline(keyboard.inline)
                    .setOne_time(keyboard.one_time)
                    .setButtons(buttons);
        }
        return null;
    }

    public static Message transform(int aid, @NonNull VKApiMessage message, @NonNull IOwnersBundle owners) {
        boolean encrypted = CryptHelper.analizeMessageBody(message.body) == MessageType.CRYPTED;
        Message appMessage = new Message(message.id)
                .setAccountId(aid)
                .setBody(message.body)
                //.setTitle(message.title)
                .setPeerId(message.peer_id)
                .setSenderId(message.from_id)
                //.setRead(message.read_state)
                .setOut(message.out)
                .setStatus(MessageStatus.SENT)
                .setDate(message.date)
                .setHasAttachments(nonNull(message.attachments) && message.attachments.nonEmpty())
                .setForwardMessagesCount(safeCountOf(message.fwd_messages))
                .setDeleted(message.deleted)
                .setDeletedForAll(false) // cant be deleted from api?
                .setOriginalId(message.id)
                .setCryptStatus(encrypted ? CryptStatus.ENCRYPTED : CryptStatus.NO_ENCRYPTION)
                .setImportant(message.important)
                .setAction(Message.fromApiChatAction(message.action))
                .setActionMid(message.action_mid)
                .setActionEmail(message.action_email)
                .setActionText(message.action_text)
                .setPhoto50(message.action_photo_50)
                .setPhoto100(message.action_photo_100)
                .setPhoto200(message.action_photo_200)
                .setSender(owners.getById(message.from_id))
                .setPayload(message.payload)
                .setKeyboard(transformKeyboard(message.keyboard));

        if (message.action_mid != 0) {
            appMessage.setActionUser(owners.getById(message.action_mid));
        }

        if (nonNull(message.attachments) && !message.attachments.isEmpty()) {
            appMessage.setAttachments(buildAttachments(message.attachments, owners));
        }

        if (nonEmpty(message.fwd_messages)) {
            for (VKApiMessage fwd : message.fwd_messages) {
                appMessage.prepareFwd(message.fwd_messages.size()).add(transform(aid, fwd, owners));
            }
        }

        if (nonEmpty(message.random_id)) {
            try {
                appMessage.setRandomId(Integer.parseInt(message.random_id));
            } catch (NumberFormatException ignored) {
            }
        }

        return appMessage;
    }

    public static Document.Graffiti transform(VkApiDoc.Graffiti dto) {
        return new Document.Graffiti()
                .setWidth(dto.width)
                .setHeight(dto.height)
                .setSrc(dto.src);
    }

    public static Document.VideoPreview transform(VkApiDoc.Video dto) {
        return new Document.VideoPreview()
                .setHeight(dto.height)
                .setSrc(dto.src)
                .setWidth(dto.width);
    }

    private static PhotoSizes.Size dto2model(PhotoSizeDto dto) {
        return new PhotoSizes.Size(dto.width, dto.height, dto.url);
    }

    public static PhotoSizes transform(List<PhotoSizeDto> dtos) {
        PhotoSizes sizes = new PhotoSizes();
        if (nonNull(dtos)) {
            for (PhotoSizeDto dto : dtos) {
                switch (dto.type) {
                    case PhotoSizeDto.Type.S:
                        sizes.setS(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.M:
                        sizes.setM(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.X:
                        sizes.setX(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.Y:
                        sizes.setY(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.Z:
                        sizes.setZ(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.W:
                        sizes.setW(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.O:
                        sizes.setO(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.P:
                        sizes.setP(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.Q:
                        sizes.setQ(dto2model(dto));
                        break;

                    case PhotoSizeDto.Type.R:
                        sizes.setR(dto2model(dto));
                        break;
                }
            }
        }
        return sizes;
    }

    public static SimplePrivacy transform(@NonNull VkApiPrivacy orig) {
        ArrayList<SimplePrivacy.Entry> entries = new ArrayList<>(safeCountOf(orig.entries));

        if (nonNull(orig.entries)) {
            for (VkApiPrivacy.Entry entry : orig.entries) {
                entries.add(new SimplePrivacy.Entry(entry.type, entry.id, entry.allowed));
            }
        }

        return new SimplePrivacy(orig.category, entries);
    }

    public static Privacy transform(@NonNull SimplePrivacy simplePrivacy, @NonNull IOwnersBundle owners, @NonNull Map<Integer, FriendList> friendListMap) {
        Privacy privacy = new Privacy();
        privacy.setType(simplePrivacy.getType());

        for (SimplePrivacy.Entry entry : simplePrivacy.getEntries()) {
            switch (entry.getType()) {
                case VkApiPrivacy.Entry.TYPE_FRIENDS_LIST:
                    if (entry.isAllowed()) {
                        privacy.allowFor(friendListMap.get(entry.getId()));
                    } else {
                        privacy.disallowFor(friendListMap.get(entry.getId()));
                    }
                    break;
                case VkApiPrivacy.Entry.TYPE_OWNER:
                    if (entry.isAllowed()) {
                        privacy.allowFor((User) owners.getById(entry.getId()));
                    } else {
                        privacy.disallowFor((User) owners.getById(entry.getId()));
                    }
                    break;
            }
        }

        return privacy;
    }

    @NonNull
    public static List<Owner> buildUserArray(@NonNull Copies copies, @NonNull IOwnersBundle owners) {
        List<Owner> data = new ArrayList<>(safeCountOf(copies.pairs));
        if (nonNull(copies.pairs)) {
            for (Copies.IdPair pair : copies.pairs) {
                data.add(owners.getById(pair.owner_id));
            }
        }

        return data;
    }

    @NonNull
    public static List<User> buildUserArray(@NonNull List<Integer> users, @NonNull IOwnersBundle owners) {
        List<User> data = new ArrayList<>(safeCountOf(users));
        for (Integer pair : users) {
            Owner dt = owners.getById(pair);
            if (dt.getOwnerType() == OwnerType.USER)
                data.add((User) owners.getById(pair));
        }

        return data;
    }

    @NonNull
    public static List<Owner> buildUserArray(@NonNull UserArray original, @NonNull IOwnersBundle owners) {
        List<Owner> data = new ArrayList<>(original.ids == null ? 0 : original.ids.length);
        if (original.ids != null) {
            for (int id : original.ids) {
                data.add(owners.getById(id));
            }
        }

        return data;
    }

    public static Comment buildComment(@NonNull Commented commented, @NonNull VKApiComment dto, @NonNull IOwnersBundle owners) {
        Comment comment = new Comment(commented)
                .setId(dto.id)
                .setFromId(dto.from_id)
                .setDate(dto.date)
                .setText(dto.text)
                .setReplyToComment(dto.reply_to_comment)
                .setReplyToUser(dto.reply_to_user)
                .setLikesCount(dto.likes)
                .setUserLikes(dto.user_likes)
                .setCanLike(dto.can_like)
                .setCanEdit(dto.can_edit)
                .setThreadsCount(dto.threads_count)
                .setThreads(buildComments(commented, dto.threads, owners))
                .setPid(dto.pid);

        if (dto.from_id != 0) {
            comment.setAuthor(owners.getById(dto.from_id));
        }

        if (dto.attachments != null) {
            comment.setAttachments(buildAttachments(dto.attachments, owners));
            //comment.setHasAttachmens(comment.getAttachments().count());
        }

        return comment;
    }

    public static List<Comment> buildComments(@NonNull Commented commented, @NonNull List<VKApiComment> dtos, @NonNull IOwnersBundle owners) {
        if (Utils.isEmpty(dtos)) {
            return null;
        }
        List<Comment> o = new ArrayList<>();
        for (VKApiComment i : dtos) {
            Comment u = buildComment(commented, i, owners);
            if (nonNull(u)) {
                o.add(u);
            }
        }
        return o;
    }

    public static Topic transform(@NonNull VKApiTopic dto, @NonNull IOwnersBundle owners) {
        Topic topic = new Topic(dto.id, dto.owner_id)
                .setTitle(dto.title)
                .setCreationTime(dto.created)
                .setCreatedByOwnerId(dto.created_by)
                .setLastUpdateTime(dto.updated)
                .setUpdatedByOwnerId(dto.updated_by)
                .setClosed(dto.is_closed)
                .setFixed(dto.is_fixed)
                .setCommentsCount(isNull(dto.comments) ? 0 : dto.comments.count)
                .setFirstCommentBody(dto.first_comment)
                .setLastCommentBody(dto.last_comment);

        if (dto.updated_by != 0) {
            topic.setUpdater(owners.getById(dto.updated_by));
        }

        if (dto.created_by != 0) {
            topic.setCreator(owners.getById(dto.created_by));
        }

        return topic;
    }

    private static String buildPollPhoto(VKApiPoll.Photo photo) {
        String url = null;
        if (photo != null && !Utils.isEmpty(photo.images)) {
            int def = 0;
            for (VKApiPoll.Image i : photo.images) {
                if (i.width * i.height > def) {
                    def = i.width * i.height;
                    url = i.url;
                }
            }
        }
        return url;
    }

    public static Poll transform(@NonNull VKApiPoll dto) {
        List<Poll.Answer> answers = new ArrayList<>(safeCountOf(dto.answers));
        if (nonNull(dto.answers)) {

            for (VKApiPoll.Answer answer : dto.answers) {
                answers.add(new Poll.Answer(answer.id)
                        .setRate(answer.rate)
                        .setText(answer.text)
                        .setVoteCount(answer.votes));
            }
        }

        return new Poll(dto.id, dto.owner_id)
                .setAnonymous(dto.anonymous)
                .setAnswers(answers)
                .setBoard(dto.is_board)
                .setCreationTime(dto.created)
                .setMyAnswerIds(dto.answer_ids)
                .setQuestion(dto.question)
                .setVoteCount(dto.votes)
                .setClosed(dto.closed)
                .setAuthorId(dto.author_id)
                .setCanVote(dto.can_vote)
                .setCanEdit(dto.can_edit)
                .setCanReport(dto.can_report)
                .setCanShare(dto.can_share)
                .setEndDate(dto.end_date)
                .setMultiple(dto.multiple)
                .setPhoto(buildPollPhoto(dto.photo));
    }

    public static Story transformStory(@NonNull VKApiStory dto, @NonNull IOwnersBundle owners) {
        return new Story().setId(dto.id)
                .setOwnerId(dto.owner_id)
                .setDate(dto.date)
                .setExpires(dto.expires_at)
                .setIs_expired(dto.is_expired)
                .setAccessKey(dto.access_key)
                .setTarget_url(dto.target_url)
                .setPhoto(dto.photo != null ? transform(dto.photo) : null)
                .setVideo(dto.video != null ? transform(dto.video) : null)
                .setOwner(owners.getById(dto.owner_id));
    }

    public static Call transform(@NonNull VKApiCall dto) {
        return new Call().setInitiator_id(dto.initiator_id)
                .setReceiver_id(dto.receiver_id)
                .setState(dto.state)
                .setTime(dto.time);
    }

    public static WallReply transform(@NonNull VKApiWallReply dto, @NonNull IOwnersBundle owners) {
        WallReply comment = new WallReply().setId(dto.id)
                .setOwnerId(dto.owner_id)
                .setFromId(dto.from_id)
                .setPostId(dto.post_id)
                .setText(dto.text)
                .setAuthor(owners.getById(dto.from_id));

        if (dto.attachments != null) {
            comment.setAttachments(buildAttachments(dto.attachments, owners));
            //comment.setHasAttachmens(comment.getAttachments().count());
        }
        return comment;
    }

    public static NotSupported transform(@NonNull VKApiNotSupported dto) {
        return new NotSupported().setType(dto.type).setBody(dto.body);
    }

    public static Event transformEvent(VkApiEvent dto, @NonNull IOwnersBundle owners) {
        return new Event(dto.id).setButton_text(dto.button_text).setText(dto.text)
                .setSubject(owners.getById(dto.id >= 0 ? -dto.id : dto.id));
    }

    public static Market transform(@NonNull VkApiMarket dto) {
        return new Market(dto.id, dto.owner_id)
                .setAccess_key(dto.access_key)
                .setIs_favorite(dto.is_favorite)
                .setAvailability(dto.availability)
                .setDate(dto.date)
                .setDescription(dto.description)
                .setDimensions(dto.dimensions)
                .setPrice(dto.price)
                .setSku(dto.sku)
                .setTitle(dto.title)
                .setWeight(dto.weight)
                .setThumb_photo(dto.thumb_photo);
    }

    public static MarketAlbum transform(@NonNull VkApiMarketAlbum dto) {
        return new MarketAlbum(dto.id, dto.owner_id)
                .setAccess_key(dto.access_key)
                .setCount(dto.count)
                .setTitle(dto.title)
                .setUpdated_time(dto.updated_time)
                .setPhoto(dto.photo != null ? transform(dto.photo) : null);
    }

    public static Graffiti transform(@NonNull VKApiGraffiti dto) {
        return new Graffiti().setId(dto.id)
                .setOwner_id(dto.owner_id)
                .setAccess_key(dto.access_key)
                .setHeight(dto.height)
                .setWidth(dto.width)
                .setUrl(dto.url);
    }

    public static ShortLink transform(@NonNull VKApiShortLink dto) {
        return new ShortLink().setKey(dto.key)
                .setShort_url(dto.short_url)
                .setUrl(dto.url)
                .setAccess_key(dto.access_key)
                .setViews(dto.views)
                .setTimestamp(dto.timestamp);
    }

    public static Photo transform(@NonNull VKApiPhoto dto) {
        return new Photo()
                .setId(dto.id)
                .setAlbumId(dto.album_id)
                .setOwnerId(dto.owner_id)
                .setWidth(dto.width)
                .setHeight(dto.height)
                .setText(dto.text)
                .setDate(dto.date)
                .setUserLikes(dto.user_likes)
                .setCanComment(dto.can_comment)
                .setLikesCount(dto.likes)
                .setCommentsCount(isNull(dto.comments) ? 0 : dto.comments.count)
                .setTagsCount(dto.tags)
                .setAccessKey(dto.access_key)
                .setDeleted(false)
                .setPostId(dto.post_id)
                .setSizes(isNull(dto.sizes) ? null : transform(dto.sizes));
    }

    public static Audio transform(@NonNull VKApiAudio dto) {
        return new Audio()
                .setId(dto.id)
                .setOwnerId(dto.owner_id)
                .setArtist(dto.artist)
                .setTitle(dto.title)
                .setDuration(dto.duration)
                .setUrl(dto.url)
                .setLyricsId(dto.lyrics_id)
                .setAlbumId(dto.album_id)
                .setAlbum_owner_id(dto.album_owner_id)
                .setAlbum_access_key(dto.album_access_key)
                .setGenre(dto.genre_id)
                .setAccessKey(dto.access_key)
                .setDeleted(false)
                .setAlbum_title(dto.album_title)
                .setThumb_image_big(dto.thumb_image_big)
                .setThumb_image_little(dto.thumb_image_little)
                .setThumb_image_very_big(dto.thumb_image_very_big)
                .setIsHq(dto.isHq)
                .setMain_artists(dto.main_artists).updateDownloadIndicator();
    }

    public static AudioPlaylist transform(@NonNull VKApiAudioPlaylist dto) {
        return new AudioPlaylist()
                .setId(dto.id)
                .setOwnerId(dto.owner_id)
                .setAccess_key(dto.access_key)
                .setArtist_name(dto.artist_name)
                .setCount(dto.count)
                .setDescription(dto.description)
                .setGenre(dto.genre)
                .setYear(dto.Year)
                .setTitle(dto.title)
                .setThumb_image(dto.thumb_image)
                .setUpdate_time(dto.update_time)
                .setOriginal_access_key(dto.original_access_key)
                .setOriginal_id(dto.original_id)
                .setOriginal_owner_id(dto.original_owner_id);
    }

    public static CatalogBlock transform(@NonNull CatalogResponse dto) {
        return new CatalogBlock()
                .setAudios(transformAudios(dto.audios))
                .setPlaylists(transformAudioPlaylists(dto.playlists))
                .setVideos(transformVideos(dto.videos))
                .setLinks(transformCatalogLinks(dto.items))
                .setNext_from(dto.nextFrom);
    }

    public static AudioCatalog transform(@NonNull VKApiAudioCatalog dto) {
        List<AudioPlaylist> playlists = transformAudioPlaylists(dto.playlists);
        if (nonNull(dto.playlist)) {
            if (!nonNull(playlists)) {
                playlists = new ArrayList<>();
            }
            playlists.add(transform(dto.playlist));
        }
        return new AudioCatalog()
                .setId(dto.id)
                .setSource(dto.source)
                .setNext_from(dto.next_from)
                .setSubtitle(dto.subtitle)
                .setTitle(dto.title)
                .setType(dto.type)
                .setCount(dto.count)
                .setAudios(transformAudios(dto.audios))
                .setPlaylists(playlists)
                .setVideos(transformVideos(dto.videos))
                .setLinks(transformCatalogLinks(dto.items))
                .setArtist(dto.artist != null ? transform(dto.artist) : null);
    }

    public static Link transform(@NonNull VKApiLink link) {
        return new Link()
                .setUrl(link.url)
                .setTitle(link.title)
                .setCaption(link.caption)
                .setDescription(link.description)
                .setPreviewPhoto(link.preview_photo)
                .setPhoto(isNull(link.photo) ? null : transform(link.photo));
    }

    public static Link transform(@NonNull VKApiCatalogLink link) {
        return new Link()
                .setUrl(link.url)
                .setTitle(link.title)
                .setDescription(link.subtitle)
                .setPreviewPhoto(link.preview_photo)
                .setPhoto(null);
    }

    public static Article transform(@NonNull VKApiArticle article) {
        return new Article(article.id, article.owner_id)
                .setAccessKey(article.access_key)
                .setOwnerName(article.owner_name)
                .setPhoto(isNull(article.photo) ? null : transform(article.photo))
                .setTitle(article.title)
                .setSubTitle(article.subtitle)
                .setURL(article.url)
                .setIsFavorite(article.is_favorite);
    }

    public static AudioArtist.AudioArtistImage map(VKApiAudioArtist.Image dto) {
        return new AudioArtist.AudioArtistImage(dto.url, dto.width, dto.height);
    }

    public static AudioArtist transform(VKApiAudioArtist dto) {
        return new AudioArtist(dto.id)
                .setName(dto.name)
                .setPhoto(mapAll(dto.photo, Dto2Model::map));
    }

    public static Sticker.Image map(VKApiSticker.Image dto) {
        return new Sticker.Image(dto.url, dto.width, dto.height);
    }

    public static Sticker transform(@NonNull VKApiSticker dto) {
        return new Sticker(dto.sticker_id)
                .setImages(mapAll(dto.images, Dto2Model::map))
                .setImagesWithBackground(mapAll(dto.images_with_background, Dto2Model::map))
                .setAnimationUrl(dto.animation_url);
    }

    public static FaveLink transform(@NonNull FaveLinkDto dto) {
        return new FaveLink(dto.id)
                .setUrl(dto.url)
                .setTitle(dto.title)
                .setDescription(dto.description)
                .setPhoto(nonNull(dto.photo) ? transform(dto.photo) : null);
    }

    public static VoiceMessage transform(VkApiAudioMessage dto) {
        return new VoiceMessage(dto.id, dto.owner_id)
                .setDuration(dto.duration)
                .setWaveform(dto.waveform)
                .setLinkOgg(dto.linkOgg)
                .setLinkMp3(dto.linkMp3)
                .setAccessKey(dto.access_key)
                .setTranscript(dto.transcript);
    }

    public static Document transform(@NonNull VkApiDoc dto) {
        Document document = new Document(dto.id, dto.ownerId);

        document.setTitle(dto.title)
                .setSize(dto.size)
                .setExt(dto.ext)
                .setUrl(dto.url)
                .setAccessKey(dto.accessKey)
                .setDate(dto.date)
                .setType(dto.type);

        if (nonNull(dto.preview)) {
            if (nonNull(dto.preview.photo) && nonNull(dto.preview.photo.sizes)) {
                document.setPhotoPreview(transform(dto.preview.photo.sizes));
            }

            if (nonNull(dto.preview.video)) {
                document.setVideoPreview(new Document.VideoPreview()
                        .setWidth(dto.preview.video.width)
                        .setHeight(dto.preview.video.height)
                        .setSrc(dto.preview.video.src));
            }

            if (nonNull(dto.preview.graffiti)) {
                document.setGraffiti(new Document.Graffiti()
                        .setHeight(dto.preview.graffiti.height)
                        .setWidth(dto.preview.graffiti.width)
                        .setSrc(dto.preview.graffiti.src));
            }
        }

        return document;
    }

    /*public static Document transform(@NonNull VKApiDocument dto) {
        Document document = dto.isVoiceMessage() ? new VoiceMessage(dto.id, dto.owner_id)
                : new Document(dto.id, dto.owner_id);

        document.setTitle(dto.title)
                .setSize(dto.size)
                .setExt(dto.ext)
                .setUrl(dto.url)
                .setAccessKey(dto.access_key)
                .setDate(dto.date)
                .setType(dto.type);

        if (document instanceof VoiceMessage) {
            ((VoiceMessage) document)
                    .setDuration(dto.preview.audio_msg.duration)
                    .setWaveform(dto.preview.audio_msg.waveform)
                    .setLinkOgg(dto.preview.audio_msg.link_ogg)
                    .setLinkMp3(dto.preview.audio_msg.link_mp3);
        }

        if (nonNull(dto.preview)) {
            if (nonNull(dto.preview.photo_sizes)) {
                document.setPhotoPreview(transform(dto.preview.photo_sizes));
            }

            if (nonNull(dto.preview.video_preview)) {
                document.setVideoPreview(new Document.VideoPreview()
                        .setWidth(dto.preview.video_preview.width)
                        .setHeight(dto.preview.video_preview.height)
                        .setSrc(dto.preview.video_preview.src));
            }

            if (nonNull(dto.preview.graffiti)) {
                document.setGraffiti(new Document.Graffiti()
                        .setHeight(dto.preview.graffiti.height)
                        .setWidth(dto.preview.graffiti.width)
                        .setSrc(dto.preview.graffiti.src));
            }
        }

        return document;
    }*/

    public static Video transform(@NonNull VKApiVideo dto) {
        return new Video()
                .setId(dto.id)
                .setOwnerId(dto.owner_id)
                .setAlbumId(dto.album_id)
                .setTitle(dto.title)
                .setDescription(dto.description)
                .setDuration(dto.duration)
                .setLink(dto.link)
                .setDate(dto.date)
                .setAddingDate(dto.adding_date)
                .setViews(dto.views)
                .setPlayer(dto.player)
                .setImage(dto.image)
                .setAccessKey(dto.access_key)
                .setCommentsCount(isNull(dto.comments) ? 0 : dto.comments.count)
                .setCanComment(dto.can_comment)
                .setCanRepost(dto.can_repost)
                .setUserLikes(dto.user_likes)
                .setRepeat(dto.repeat)
                .setLikesCount(dto.likes)
                .setPrivacyView(isNull(dto.privacy_view) ? null : transform(dto.privacy_view))
                .setPrivacyComment(isNull(dto.privacy_comment) ? null : transform(dto.privacy_comment))
                .setMp4link240(dto.mp4_240)
                .setMp4link360(dto.mp4_360)
                .setMp4link480(dto.mp4_480)
                .setMp4link720(dto.mp4_720)
                .setMp4link1080(dto.mp4_1080)
                .setExternalLink(dto.external)
                .setHls(dto.hls)
                .setLive(dto.live)
                .setPlatform(dto.platform)
                .setCanEdit(dto.can_edit)
                .setCanAdd(dto.can_add)
                .setPrivate(dto.is_private);
    }

    public static WikiPage transform(@NonNull VKApiWikiPage dto) {
        return new WikiPage(dto.id, dto.owner_id)
                .setCreatorId(dto.creator_id)
                .setTitle(dto.title)
                .setSource(dto.source)
                .setEditionTime(dto.edited)
                .setCreationTime(dto.created)
                .setParent(dto.parent)
                .setParent2(dto.parent2)
                .setViews(dto.views)
                .setViewUrl(dto.view_url);
    }

    @NonNull
    public static Attachments buildAttachments(@NonNull VkApiAttachments apiAttachments, @NonNull IOwnersBundle owners) {
        Attachments attachments = new Attachments();

        List<VkApiAttachments.Entry> entries = apiAttachments.entryList();

        for (VkApiAttachments.Entry entry : entries) {
            VKApiAttachment attachment = entry.attachment;

            switch (attachment.getType()) {
                case VKApiAttachment.TYPE_AUDIO:
                    attachments.prepareAudios().add(transform((VKApiAudio) attachment));
                    break;
                case VKApiAttachment.TYPE_STICKER:
                    attachments.prepareStickers().add(transform((VKApiSticker) attachment));
                    break;
                case VKApiAttachment.TYPE_PHOTO:
                    attachments.preparePhotos().add(transform((VKApiPhoto) attachment));
                    break;
                case VKApiAttachment.TYPE_DOC:
                    attachments.prepareDocs().add(transform((VkApiDoc) attachment));
                    break;
                case VKApiAttachment.TYPE_AUDIO_MESSAGE:
                    attachments.prepareVoiceMessages().add(transform((VkApiAudioMessage) attachment));
                    break;
                case VKApiAttachment.TYPE_VIDEO:
                    attachments.prepareVideos().add(transform((VKApiVideo) attachment));
                    break;
                case VKApiAttachment.TYPE_LINK:
                    attachments.prepareLinks().add(transform((VKApiLink) attachment));
                    break;
                case VKApiAttachment.TYPE_ARTICLE:
                    attachments.prepareArticles().add(transform((VKApiArticle) attachment));
                    break;
                case VKApiAttachment.TYPE_STORY:
                    attachments.prepareStories().add(transformStory((VKApiStory) attachment, owners));
                    break;
                case VKApiAttachment.TYPE_ALBUM:
                    attachments.preparePhotoAlbums().add(transformPhotoAlbum((VKApiPhotoAlbum) attachment));
                    break;
                case VKApiAttachment.TYPE_CALL:
                    attachments.prepareCalls().add(transform((VKApiCall) attachment));
                    break;
                case VKApiAttachment.TYPE_WALL_REPLY:
                    attachments.prepareWallReply().add(transform((VKApiWallReply) attachment, owners));
                    break;
                case VKApiAttachment.TYPE_NOT_SUPPORT:
                    attachments.prepareNotSupporteds().add(transform((VKApiNotSupported) attachment));
                    break;
                case VKApiAttachment.TYPE_EVENT:
                    attachments.prepareEvents().add(transformEvent((VkApiEvent) attachment, owners));
                    break;
                case VKApiAttachment.TYPE_MARKET:
                    attachments.prepareMarkets().add(transform((VkApiMarket) attachment));
                    break;
                case VKApiAttachment.TYPE_MARKET_ALBUM:
                    attachments.prepareMarketAlbums().add(transform((VkApiMarketAlbum) attachment));
                    break;
                case VKApiAttachment.TYPE_ARTIST:
                    attachments.prepareAudioArtist().add(transform((VKApiAudioArtist) attachment));
                    break;
                case VKApiAttachment.TYPE_AUDIO_PLAYLIST:
                    attachments.prepareAudioPlaylists().add(transform((VKApiAudioPlaylist) attachment));
                    break;
                case VKApiAttachment.TYPE_GRAFFITI:
                    attachments.prepareGraffity().add(transform((VKApiGraffiti) attachment));
                    break;
                case VKApiAttachment.TYPE_POLL:
                    attachments.preparePolls().add(transform((VKApiPoll) attachment));
                    break;
                case VKApiAttachment.TYPE_WIKI_PAGE:
                    attachments.prepareWikiPages().add(transform((VKApiWikiPage) attachment));
                    break;
                case VKApiAttachment.TYPE_POST:
                    attachments.preparePosts().add(transform((VKApiPost) attachment, owners));
                    break;
            }
        }

        return attachments;
    }

    @NonNull
    public static List<Post> transformPosts(Collection<VKApiPost> dtos, IOwnersBundle bundle) {
        List<Post> posts = new ArrayList<>(safeCountOf(dtos));
        for (VKApiPost dto : dtos) {
            posts.add(transform(dto, bundle));
        }
        return posts;
    }

    @NonNull
    public static List<Post> transformAttachmentsPosts(Collection<VkApiAttachments.Entry> dtos, IOwnersBundle bundle) {
        List<Post> posts = new ArrayList<>(safeCountOf(dtos));
        for (VkApiAttachments.Entry dto : dtos) {
            if (dto.attachment instanceof VKApiPost)
                posts.add(transform((VKApiPost) dto.attachment, bundle));
        }
        return posts;
    }

    @NonNull
    public static Post transform(@NonNull VKApiPost dto, @NonNull IOwnersBundle owners) {
        Post post = new Post()
                .setDbid(Post.NO_STORED)
                .setVkid(dto.id)
                .setOwnerId(dto.owner_id)
                .setAuthorId(dto.from_id)
                .setDate(dto.date)
                .setText(dto.text)
                .setReplyOwnerId(dto.reply_owner_id)
                .setReplyPostId(dto.reply_post_id)
                .setFriendsOnly(dto.friends_only)
                .setCommentsCount(isNull(dto.comments) ? 0 : dto.comments.count)
                .setCanPostComment(nonNull(dto.comments) && dto.comments.canPost)
                .setLikesCount(dto.likes_count)
                .setUserLikes(dto.user_likes)
                .setCanLike(dto.can_like)
                .setCanRepost(dto.can_publish)
                .setRepostCount(dto.reposts_count)
                .setUserReposted(dto.user_reposted)
                .setPostType(dto.post_type)
                .setSignerId(dto.signer_id)
                .setCreatorId(dto.created_by)
                .setCanEdit(dto.can_edit)
                .setCanPin(dto.can_pin)
                .setPinned(dto.is_pinned)
                .setViewCount(dto.views);

        if (nonNull(dto.post_source)) {
            post.setSource(new PostSource(dto.post_source.type, dto.post_source.platform, dto.post_source.data, dto.post_source.url));
        }

        if (dto.hasAttachments()) {
            post.setAttachments(buildAttachments(dto.attachments, owners));
        }

        if (dto.hasCopyHistory()) {
            int copyCount = safeCountOf(dto.copy_history);

            for (VKApiPost copy : dto.copy_history) {
                post.prepareCopyHierarchy(copyCount).add(transform(copy, owners));
            }
        }

        fillPostOwners(post, owners);

        if (post.hasCopyHierarchy()) {
            for (Post copy : post.getCopyHierarchy()) {
                fillPostOwners(copy, owners);
            }
        }

        return post;
    }

    @NonNull
    public static News buildNews(@NonNull VKApiNews original, @NonNull IOwnersBundle owners) {
        News news = new News()
                .setType(original.type)
                .setSourceId(original.source_id)
                .setPostType(original.post_type)
                .setFinalPost(original.final_post)
                .setCopyOwnerId(original.copy_owner_id)
                .setCopyPostId(original.copy_post_id)
                .setCopyPostDate(original.copy_post_date)
                .setDate(original.date)
                .setPostId(original.post_id)
                .setText(original.text)
                .setCanEdit(original.can_edit)
                .setCanDelete(original.can_delete)
                .setCommentCount(original.comment_count)
                .setCommentCanPost(original.comment_can_post)
                .setLikeCount(original.like_count)
                .setUserLike(original.user_like)
                .setCanLike(original.can_like)
                .setCanPublish(original.can_publish)
                .setRepostsCount(original.reposts_count)
                .setUserReposted(original.user_reposted)
                .setFriends(original.friends == null ? null : buildUserArray(original.friends, owners))
                .setSource(owners.getById(original.source_id))
                .setViewCount(original.views);

        if (original.hasCopyHistory()) {
            ArrayList<Post> copies = new ArrayList<>(original.copy_history.size());
            for (VKApiPost copy : original.copy_history) {
                copies.add(transform(copy, owners));
            }

            news.setCopyHistory(copies);
        }

        if (original.hasAttachments()) {
            news.setAttachments(buildAttachments(original.attachments, owners));
        }

        return news;
    }

    public static void fillPostOwners(@NonNull Post post, @NonNull IOwnersBundle owners) {
        if (post.getAuthorId() != 0) {
            post.setAuthor(owners.getById(post.getAuthorId()));
        }

        if (post.getSignerId() != 0) {
            post.setCreator((User) owners.getById(post.getSignerId()));
        } else if (post.getCreatorId() != 0) {
            post.setCreator((User) owners.getById(post.getCreatorId()));
        }
    }

    public static PhotoAlbum transformPhotoAlbum(VKApiPhotoAlbum album) {
        return new PhotoAlbum(album.id, album.owner_id)
                .setTitle(album.title)
                .setSize(album.size)
                .setDescription(album.description)
                .setCanUpload(album.can_upload)
                .setUpdatedTime(album.updated)
                .setCreatedTime(album.created)
                .setSizes(nonNull(album.photo) ? transform(album.photo) : null)
                .setCommentsDisabled(album.comments_disabled)
                .setUploadByAdminsOnly(album.upload_by_admins_only)
                .setPrivacyView(nonNull(album.privacy_view) ? transform(album.privacy_view) : null)
                .setPrivacyComment(nonNull(album.privacy_comment) ? transform(album.privacy_comment) : null);
    }
}
