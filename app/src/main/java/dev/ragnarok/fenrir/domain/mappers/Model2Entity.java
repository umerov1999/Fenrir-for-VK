package dev.ragnarok.fenrir.domain.mappers;

import static dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll;
import static dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAndAdd;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.ArticleEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioArtistEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioMessageEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioPlaylistEntity;
import dev.ragnarok.fenrir.db.model.entity.CallEntity;
import dev.ragnarok.fenrir.db.model.entity.DialogEntity;
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EventEntity;
import dev.ragnarok.fenrir.db.model.entity.GiftItemEntity;
import dev.ragnarok.fenrir.db.model.entity.GraffitiEntity;
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity;
import dev.ragnarok.fenrir.db.model.entity.LinkEntity;
import dev.ragnarok.fenrir.db.model.entity.MarketAlbumEntity;
import dev.ragnarok.fenrir.db.model.entity.MarketEntity;
import dev.ragnarok.fenrir.db.model.entity.MessageEntity;
import dev.ragnarok.fenrir.db.model.entity.NotSupportedEntity;
import dev.ragnarok.fenrir.db.model.entity.PageEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoSizeEntity;
import dev.ragnarok.fenrir.db.model.entity.PollEntity;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.db.model.entity.PrivacyEntity;
import dev.ragnarok.fenrir.db.model.entity.SimpleDialogEntity;
import dev.ragnarok.fenrir.db.model.entity.StickerEntity;
import dev.ragnarok.fenrir.db.model.entity.StoryEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoEntity;
import dev.ragnarok.fenrir.db.model.entity.WallReplyEntity;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Attachments;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Call;
import dev.ragnarok.fenrir.model.Conversation;
import dev.ragnarok.fenrir.model.CryptStatus;
import dev.ragnarok.fenrir.model.Dialog;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Event;
import dev.ragnarok.fenrir.model.GiftItem;
import dev.ragnarok.fenrir.model.Graffiti;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.NotSupported;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.PhotoSizes;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.PostSource;
import dev.ragnarok.fenrir.model.SimplePrivacy;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.util.Utils;


public class Model2Entity {

    public static KeyboardEntity buildKeyboardEntity(Keyboard keyboard) {
        if (keyboard == null || Utils.isEmpty(keyboard.getButtons())) {
            return null;
        }
        List<List<KeyboardEntity.ButtonEntity>> buttons = new ArrayList<>(keyboard.getButtons().size());
        for (List<Keyboard.Button> i : keyboard.getButtons()) {
            List<KeyboardEntity.ButtonEntity> vt = new ArrayList<>(i.size());
            for (Keyboard.Button s : i) {
                vt.add(new KeyboardEntity.ButtonEntity().setType(s.getType()).setColor(s.getColor()).setLabel(s.getLabel()).setLink(s.getLink()).setPayload(s.getPayload()));
            }
            buttons.add(vt);
        }
        return new KeyboardEntity().setAuthor_id(
                keyboard.getAuthor_id()).setInline(keyboard.getInline())
                .setOne_time(keyboard.getOne_time()).setButtons(buttons);
    }

    public static SimpleDialogEntity buildSimpleDialog(Conversation entity) {
        return new SimpleDialogEntity(entity.getId())
                .setInRead(entity.getInRead())
                .setOutRead(entity.getOutRead())
                .setPhoto50(entity.getPhoto50())
                .setPhoto100(entity.getPhoto100())
                .setPhoto200(entity.getPhoto200())
                .setUnreadCount(entity.getUnreadCount())
                .setTitle(entity.getTitle())
                .setPinned(isNull(entity.getPinned()) ? null : buildMessageEntity(entity.getPinned()))
                .setAcl(entity.getAcl())
                .setGroupChannel(entity.isGroupChannel())
                .setCurrentKeyboard(buildKeyboardEntity(entity.getCurrentKeyboard()))
                .setMajor_id(entity.getMajor_id())
                .setMinor_id(entity.getMinor_id());
    }

    public static DialogEntity buildDialog(Dialog model) {
        return new DialogEntity(model.getPeerId())
                .setUnreadCount(model.getUnreadCount())
                .setInRead(model.getInRead())
                .setOutRead(model.getOutRead())
                .setMessage(buildMessageEntity(model.getMessage()))
                .setLastMessageId(model.getLastMessageId())
                .setTitle(model.getTitle())
                .setGroupChannel(model.isGroupChannel())
                .setPhoto50(model.getPhoto50())
                .setPhoto100(model.getPhoto100())
                .setPhoto200(model.getPhoto200())
                .setMajor_id(model.getMajor_id())
                .setMinor_id(model.getMinor_id());
    }

    public static MessageEntity buildMessageEntity(Message message) {
        return new MessageEntity().set(message.getId(), message.getPeerId(), message.getSenderId())
                .setDate(message.getDate())
                .setOut(message.isOut())
                .setBody(message.getBody())
                .setEncrypted(message.getCryptStatus() != CryptStatus.NO_ENCRYPTION)
                .setImportant(message.isImportant())
                .setDeleted(message.isDeleted())
                .setDeletedForAll(message.isDeletedForAll())
                .setForwardCount(message.getForwardMessagesCount())
                .setHasAttachmens(message.isHasAttachments())
                .setStatus(message.getStatus())
                .setOriginalId(message.getOriginalId())
                .setAction(message.getAction())
                .setActionMemberId(message.getActionMid())
                .setActionEmail(message.getActionEmail())
                .setActionText(message.getActionText())
                .setPhoto50(message.getPhoto50())
                .setPhoto100(message.getPhoto100())
                .setPhoto200(message.getPhoto200())
                .setRandomId(message.getRandomId())
                .setExtras(message.getExtras())
                .setAttachments(nonNull(message.getAttachments()) ? buildEntityAttachments(message.getAttachments()) : null)
                .setForwardMessages(mapAll(message.getFwd(), Model2Entity::buildMessageEntity, false))
                .setUpdateTime(message.getUpdateTime())
                .setPayload(message.getPayload())
                .setKeyboard(buildKeyboardEntity(message.getKeyboard()));
    }

    public static List<Entity> buildEntityAttachments(Attachments attachments) {
        List<Entity> entities = new ArrayList<>(attachments.size());
        mapAndAdd(attachments.getAudios(), Model2Entity::buildAudioEntity, entities);
        mapAndAdd(attachments.getStickers(), Model2Entity::buildStickerEntity, entities);
        mapAndAdd(attachments.getPhotos(), Model2Entity::buildPhotoEntity, entities);
        mapAndAdd(attachments.getDocs(), Model2Entity::buildDocumentDbo, entities);
        mapAndAdd(attachments.getVoiceMessages(), Model2Entity::mapAudio, entities);
        mapAndAdd(attachments.getVideos(), Model2Entity::buildVideoDbo, entities);
        mapAndAdd(attachments.getPosts(), Model2Entity::buildPostDbo, entities);
        mapAndAdd(attachments.getLinks(), Model2Entity::buildLinkDbo, entities);
        mapAndAdd(attachments.getArticles(), Model2Entity::buildArticleDbo, entities);
        mapAndAdd(attachments.getStories(), Model2Entity::buildStoryDbo, entities);
        mapAndAdd(attachments.getCalls(), Model2Entity::buildCallDbo, entities);
        mapAndAdd(attachments.getWallReplies(), Model2Entity::buildWallReplyDbo, entities);
        mapAndAdd(attachments.getNotSupported(), Model2Entity::buildNotSupportedDbo, entities);
        mapAndAdd(attachments.getEvents(), Model2Entity::buildEventDbo, entities);
        mapAndAdd(attachments.getMarkets(), Model2Entity::buildMarketDbo, entities);
        mapAndAdd(attachments.getMarketAlbums(), Model2Entity::buildMarketAlbumDbo, entities);
        mapAndAdd(attachments.getAudioArtists(), Model2Entity::buildAudioArtistDbo, entities);
        mapAndAdd(attachments.getGraffity(), Model2Entity::buildGraffityDbo, entities);
        mapAndAdd(attachments.getAudioPlaylists(), Model2Entity::buildAudioPlaylistEntity, entities);
        mapAndAdd(attachments.getPolls(), Model2Entity::buildPollDbo, entities);
        mapAndAdd(attachments.getPages(), Model2Entity::buildPageEntity, entities);
        mapAndAdd(attachments.getPhotoAlbums(), Model2Entity::buildPhotoAlbumEntity, entities);
        mapAndAdd(attachments.getGifts(), Model2Entity::buildGiftItemEntity, entities);
        return entities;
    }

    public static List<Entity> buildDboAttachments(List<? extends AbsModel> models) {
        List<Entity> entities = new ArrayList<>(models.size());

        for (AbsModel model : models) {
            if (model instanceof Audio) {
                entities.add(buildAudioEntity((Audio) model));
            } else if (model instanceof Sticker) {
                entities.add(buildStickerEntity((Sticker) model));
            } else if (model instanceof Photo) {
                entities.add(buildPhotoEntity((Photo) model));
            } else if (model instanceof Document) {
                entities.add(buildDocumentDbo((Document) model));
            } else if (model instanceof Video) {
                entities.add(buildVideoDbo((Video) model));
            } else if (model instanceof Post) {
                entities.add(buildPostDbo((Post) model));
            } else if (model instanceof Link) {
                entities.add(buildLinkDbo((Link) model));
            } else if (model instanceof Article) {
                entities.add(buildArticleDbo((Article) model));
            } else if (model instanceof PhotoAlbum) {
                entities.add(buildPhotoAlbumEntity((PhotoAlbum) model));
            } else if (model instanceof Story) {
                entities.add(buildStoryDbo((Story) model));
            } else if (model instanceof AudioPlaylist) {
                entities.add(buildAudioPlaylistEntity((AudioPlaylist) model));
            } else if (model instanceof Call) {
                entities.add(buildCallDbo((Call) model));
            } else if (model instanceof NotSupported) {
                entities.add(buildNotSupportedDbo((NotSupported) model));
            } else if (model instanceof Event) {
                entities.add(buildEventDbo((Event) model));
            } else if (model instanceof Market) {
                entities.add(buildMarketDbo((Market) model));
            } else if (model instanceof MarketAlbum) {
                entities.add(buildMarketAlbumDbo((MarketAlbum) model));
            } else if (model instanceof AudioArtist) {
                entities.add(buildAudioArtistDbo((AudioArtist) model));
            } else if (model instanceof WallReply) {
                entities.add(buildWallReplyDbo((WallReply) model));
            } else if (model instanceof Graffiti) {
                entities.add(buildGraffityDbo((Graffiti) model));
            } else if (model instanceof Poll) {
                entities.add(buildPollDbo((Poll) model));
            } else if (model instanceof WikiPage) {
                entities.add(buildPageEntity((WikiPage) model));
            } else if (model instanceof GiftItem) {
                entities.add(buildGiftItemEntity((GiftItem) model));
            } else {
                throw new UnsupportedOperationException("Unsupported model");
            }
        }

        return entities;
    }

    public static GiftItemEntity buildGiftItemEntity(GiftItem giftItem) {
        return new GiftItemEntity().setId(giftItem.getId())
                .setThumb256(giftItem.getThumb256())
                .setThumb96(giftItem.getThumb96())
                .setThumb48(giftItem.getThumb48());
    }

    public static PageEntity buildPageEntity(WikiPage page) {
        return new PageEntity().set(page.getId(), page.getOwnerId())
                .setViewUrl(page.getViewUrl())
                .setViews(page.getViews())
                .setParent2(page.getParent2())
                .setParent(page.getParent())
                .setCreationTime(page.getCreationTime())
                .setEditionTime(page.getEditionTime())
                .setCreatorId(page.getCreatorId())
                .setSource(page.getSource());
    }

    public static PollEntity.Answer mapAnswer(Poll.Answer answer) {
        return new PollEntity.Answer().set(answer.getId(), answer.getText(), answer.getVoteCount(), answer.getRate());
    }

    public static PollEntity buildPollDbo(Poll poll) {
        return new PollEntity().set(poll.getId(), poll.getOwnerId())
                .setAnswers(mapAll(poll.getAnswers(), Model2Entity::mapAnswer, false))
                .setQuestion(poll.getQuestion())
                .setVoteCount(poll.getVoteCount())
                .setMyAnswerIds(poll.getMyAnswerIds())
                .setCreationTime(poll.getCreationTime())
                .setAnonymous(poll.isAnonymous())
                .setBoard(poll.isBoard())
                .setClosed(poll.isClosed())
                .setAuthorId(poll.getAuthorId())
                .setCanVote(poll.isCanVote())
                .setCanEdit(poll.isCanEdit())
                .setCanReport(poll.isCanReport())
                .setCanShare(poll.isCanShare())
                .setEndDate(poll.getEndDate())
                .setMultiple(poll.isMultiple())
                .setPhoto(poll.getPhoto());
    }

    public static LinkEntity buildLinkDbo(Link link) {
        return new LinkEntity().setUrl(link.getUrl())
                .setPhoto(isNull(link.getPhoto()) ? null : buildPhotoEntity(link.getPhoto()))
                .setTitle(link.getTitle())
                .setDescription(link.getDescription())
                .setCaption(link.getCaption())
                .setPreviewPhoto(link.getPreviewPhoto());
    }

    public static ArticleEntity buildArticleDbo(Article dbo) {
        return new ArticleEntity().set(dbo.getId(), dbo.getOwnerId())
                .setAccessKey(dbo.getAccessKey())
                .setOwnerName(dbo.getOwnerName())
                .setPhoto(isNull(dbo.getPhoto()) ? null : buildPhotoEntity(dbo.getPhoto()))
                .setTitle(dbo.getTitle())
                .setSubTitle(dbo.getSubTitle())
                .setURL(dbo.getURL())
                .setIsFavorite(dbo.getIsFavorite());
    }

    public static StoryEntity buildStoryDbo(Story dbo) {
        return new StoryEntity().setId(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setDate(dbo.getDate())
                .setExpires(dbo.getExpires())
                .setIs_expired(dbo.isIs_expired())
                .setAccessKey(dbo.getAccessKey())
                .setTarget_url(dbo.getTarget_url())
                .setPhoto(isNull(dbo.getPhoto()) ? null : buildPhotoEntity(dbo.getPhoto()))
                .setVideo(dbo.getVideo() != null ? buildVideoDbo(dbo.getVideo()) : null);
    }

    public static CallEntity buildCallDbo(Call dbo) {
        return new CallEntity().setInitiator_id(dbo.getInitiator_id())
                .setReceiver_id(dbo.getReceiver_id())
                .setState(dbo.getState())
                .setTime(dbo.getTime());
    }

    public static WallReplyEntity buildWallReplyDbo(@NonNull WallReply dbo) {
        WallReplyEntity comment = new WallReplyEntity().setId(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setFromId(dbo.getFromId())
                .setPostId(dbo.getPostId())
                .setText(dbo.getText());

        if (nonNull(dbo.getAttachments())) {
            comment.setAttachments(buildEntityAttachments(dbo.getAttachments()));
        } else {
            comment.setAttachments(null);
        }
        return comment;
    }

    public static NotSupportedEntity buildNotSupportedDbo(NotSupported dbo) {
        return new NotSupportedEntity().setType(dbo.getType()).setBody(dbo.getBody());
    }

    public static EventEntity buildEventDbo(Event dbo) {
        return new EventEntity().setId(dbo.getId()).setButton_text(dbo.getButton_text()).setText(dbo.getText());
    }

    public static MarketEntity buildMarketDbo(@NonNull Market dbo) {
        return new MarketEntity().set(dbo.getId(), dbo.getOwner_id())
                .setAccess_key(dbo.getAccess_key())
                .setIs_favorite(dbo.isIs_favorite())
                .setAvailability(dbo.getAvailability())
                .setDate(dbo.getDate())
                .setDescription(dbo.getDescription())
                .setDimensions(dbo.getDimensions())
                .setPrice(dbo.getPrice())
                .setSku(dbo.getSku())
                .setTitle(dbo.getTitle())
                .setWeight(dbo.getWeight())
                .setThumb_photo(dbo.getThumb_photo());
    }

    public static MarketAlbumEntity buildMarketAlbumDbo(@NonNull MarketAlbum dbo) {
        return new MarketAlbumEntity().set(dbo.getId(), dbo.getOwner_id())
                .setAccess_key(dbo.getAccess_key())
                .setCount(dbo.getCount())
                .setTitle(dbo.getTitle())
                .setUpdated_time(dbo.getUpdated_time())
                .setPhoto(dbo.getPhoto() != null ? buildPhotoEntity(dbo.getPhoto()) : null);
    }

    public static AudioArtistEntity.AudioArtistImageEntity mapArtistImage(@NonNull AudioArtist.AudioArtistImage dbo) {
        return new AudioArtistEntity.AudioArtistImageEntity().set(dbo.getUrl(), dbo.getWidth(), dbo.getHeight());
    }

    public static AudioArtistEntity buildAudioArtistDbo(@NonNull AudioArtist dbo) {
        return new AudioArtistEntity()
                .setId(dbo.getId())
                .setName(dbo.getName())
                .setPhoto(mapAll(dbo.getPhoto(), Model2Entity::mapArtistImage));
    }

    public static GraffitiEntity buildGraffityDbo(Graffiti dbo) {
        return new GraffitiEntity().setId(dbo.getId())
                .setOwner_id(dbo.getOwner_id())
                .setAccess_key(dbo.getAccess_key())
                .setHeight(dbo.getHeight())
                .setWidth(dbo.getWidth())
                .setUrl(dbo.getUrl());
    }

    public static PostEntity buildPostDbo(Post post) {
        PostEntity dbo = new PostEntity().set(post.getVkid(), post.getOwnerId())
                .setFromId(post.getAuthorId())
                .setDate(post.getDate())
                .setText(post.getText())
                .setReplyOwnerId(post.getReplyOwnerId())
                .setReplyPostId(post.getReplyPostId())
                .setFriendsOnly(post.isFriendsOnly())
                .setCommentsCount(post.getCommentsCount())
                .setCanPostComment(post.isCanPostComment())
                .setLikesCount(post.getLikesCount())
                .setUserLikes(post.isUserLikes())
                .setCanLike(post.isCanLike())
                .setCanEdit(post.isCanEdit())
                .setCanPublish(post.isCanRepost())
                .setRepostCount(post.getRepostCount())
                .setUserReposted(post.isUserReposted())
                .setPostType(post.getPostType())
                .setAttachmentsCount(nonNull(post.getAttachments()) ? post.getAttachments().size() : 0)
                .setSignedId(post.getSignerId())
                .setCreatedBy(post.getCreatorId())
                .setCanPin(post.isCanPin())
                .setPinned(post.isPinned())
                .setDeleted(post.isDeleted())
                .setViews(post.getViewCount())
                .setDbid(post.getDbid());

        PostSource source = post.getSource();
        if (nonNull(source)) {
            dbo.setSource(new PostEntity.SourceDbo().set(source.getType(), source.getPlatform(), source.getData(), source.getUrl()));
        }

        if (nonNull(post.getAttachments())) {
            dbo.setAttachments(buildEntityAttachments(post.getAttachments()));
        } else {
            dbo.setAttachments(null);
        }

        dbo.setCopyHierarchy(mapAll(post.getCopyHierarchy(), Model2Entity::buildPostDbo, false));
        return dbo;
    }

    public static VideoEntity buildVideoDbo(Video video) {
        return new VideoEntity().set(video.getId(), video.getOwnerId())
                .setAlbumId(video.getAlbumId())
                .setTitle(video.getTitle())
                .setDescription(video.getDescription())
                .setLink(video.getLink())
                .setDate(video.getDate())
                .setAddingDate(video.getAddingDate())
                .setViews(video.getViews())
                .setPlayer(video.getPlayer())
                .setImage(video.getImage())
                .setAccessKey(video.getAccessKey())
                .setCommentsCount(video.getCommentsCount())
                .setUserLikes(video.isUserLikes())
                .setLikesCount(video.getLikesCount())
                .setMp4link240(video.getMp4link240())
                .setMp4link360(video.getMp4link360())
                .setMp4link480(video.getMp4link480())
                .setMp4link720(video.getMp4link720())
                .setMp4link1080(video.getMp4link1080())
                .setExternalLink(video.getExternalLink())
                .setPlatform(video.getPlatform())
                .setRepeat(video.isRepeat())
                .setDuration(video.getDuration())
                .setPrivacyView(isNull(video.getPrivacyView()) ? null : mapPrivacy(video.getPrivacyView()))
                .setPrivacyComment(isNull(video.getPrivacyComment()) ? null : mapPrivacy(video.getPrivacyComment()))
                .setCanEdit(video.isCanEdit())
                .setCanAdd(video.isCanAdd())
                .setCanComment(video.isCanComment())
                .setCanRepost(video.isCanRepost())
                .setPrivate(video.getPrivate());
    }

    public static PrivacyEntity mapPrivacy(SimplePrivacy privacy) {
        return new PrivacyEntity().set(privacy.getType(), mapAll(privacy.getEntries(), orig -> new PrivacyEntity.Entry().set(orig.getType(), orig.getId(), orig.isAllowed())));
    }

    public static AudioMessageEntity mapAudio(VoiceMessage message) {
        return new AudioMessageEntity().set(message.getId(), message.getOwnerId())
                .setWaveform(message.getWaveform())
                .setLinkOgg(message.getLinkOgg())
                .setLinkMp3(message.getLinkMp3())
                .setDuration(message.getDuration())
                .setAccessKey(message.getAccessKey())
                .setTranscript(message.getTranscript());
    }

    public static DocumentEntity buildDocumentDbo(Document document) {
        DocumentEntity dbo = new DocumentEntity().set(document.getId(), document.getOwnerId())
                .setTitle(document.getTitle())
                .setSize(document.getSize())
                .setExt(document.getExt())
                .setUrl(document.getUrl())
                .setDate(document.getDate())
                .setType(document.getType())
                .setAccessKey(document.getAccessKey());

        if (nonNull(document.getGraffiti())) {
            Document.Graffiti graffiti = document.getGraffiti();
            dbo.setGraffiti(new DocumentEntity.GraffitiDbo().set(graffiti.getSrc(), graffiti.getWidth(), graffiti.getHeight()));
        }

        if (nonNull(document.getVideoPreview())) {
            Document.VideoPreview video = document.getVideoPreview();
            dbo.setVideo(new DocumentEntity.VideoPreviewDbo().set(video.getSrc(), video.getWidth(), video.getHeight(), video.getFileSize()));
        }

        return dbo;
    }

    public static StickerEntity buildStickerEntity(Sticker sticker) {
        return new StickerEntity().setId(sticker.getId())
                .setImagesWithBackground(mapAll(sticker.getImagesWithBackground(), Model2Entity::map))
                .setImages(mapAll(sticker.getImages(), Model2Entity::map))
                .setAnimations(mapAll(sticker.getAnimations(), Model2Entity::mapStickerAnimation))
                .setAnimationUrl(sticker.getAnimationUrl());
    }

    public static StickerEntity.Img map(Sticker.Image image) {
        return new StickerEntity.Img().set(image.getUrl(), image.getWidth(), image.getHeight());
    }

    public static StickerEntity.AnimationEntity mapStickerAnimation(Sticker.Animation dto) {
        return new StickerEntity.AnimationEntity().set(dto.getUrl(), dto.getType());
    }

    public static AudioEntity buildAudioEntity(Audio audio) {
        return new AudioEntity().set(audio.getId(), audio.getOwnerId())
                .setArtist(audio.getArtist())
                .setTitle(audio.getTitle())
                .setDuration(audio.getDuration())
                .setUrl(audio.getUrl())
                .setLyricsId(audio.getLyricsId())
                .setAlbumId(audio.getAlbumId())
                .setAlbum_owner_id(audio.getAlbum_owner_id())
                .setAlbum_access_key(audio.getAlbum_access_key())
                .setGenre(audio.getGenre())
                .setAccessKey(audio.getAccessKey())
                .setAlbum_title(audio.getAlbum_title())
                .setThumb_image_big(audio.getThumb_image_big())
                .setThumb_image_little(audio.getThumb_image_little())
                .setThumb_image_very_big(audio.getThumb_image_very_big())
                .setIsHq(audio.getIsHq())
                .setMain_artists(audio.getMain_artists());
    }

    public static AudioPlaylistEntity buildAudioPlaylistEntity(AudioPlaylist dto) {
        return new AudioPlaylistEntity()
                .setId(dto.getId())
                .setOwnerId(dto.getOwnerId())
                .setAccess_key(dto.getAccess_key())
                .setArtist_name(dto.getArtist_name())
                .setCount(dto.getCount())
                .setDescription(dto.getDescription())
                .setGenre(dto.getGenre())
                .setYear(dto.getYear())
                .setTitle(dto.getTitle())
                .setThumb_image(dto.getThumb_image())
                .setUpdate_time(dto.getUpdate_time())
                .setOriginal_access_key(dto.getOriginal_access_key())
                .setOriginal_id(dto.getOriginal_id())
                .setOriginal_owner_id(dto.getOriginal_owner_id());
    }

    public static PhotoEntity buildPhotoEntity(Photo photo) {
        return new PhotoEntity().set(photo.getId(), photo.getOwnerId())
                .setAlbumId(photo.getAlbumId())
                .setWidth(photo.getWidth())
                .setHeight(photo.getHeight())
                .setText(photo.getText())
                .setDate(photo.getDate())
                .setUserLikes(photo.isUserLikes())
                .setCanComment(photo.isCanComment())
                .setLikesCount(photo.getLikesCount())
                .setCommentsCount(photo.getCommentsCount())
                .setTagsCount(photo.getTagsCount())
                .setAccessKey(photo.getAccessKey())
                .setPostId(photo.getPostId())
                .setDeleted(photo.isDeleted())
                .setSizes(isNull(photo.getSizes()) ? null : buildPhotoSizeEntity(photo.getSizes()));
    }

    public static PhotoAlbumEntity buildPhotoAlbumEntity(PhotoAlbum album) {
        return new PhotoAlbumEntity().set(album.getId(), album.getOwnerId())
                .setSize(album.getSize())
                .setTitle(album.getTitle())
                .setDescription(album.getDescription())
                .setCanUpload(album.isCanUpload())
                .setUpdatedTime(album.getUpdatedTime())
                .setCreatedTime(album.getCreatedTime())
                .setSizes(nonNull(album.getSizes()) ? buildPhotoSizeEntity(album.getSizes()) : null)
                .setPrivacyView(nonNull(album.getPrivacyView()) ? mapPrivacy(album.getPrivacyView()) : null)
                .setPrivacyComment(nonNull(album.getPrivacyComment()) ? mapPrivacy(album.getPrivacyComment()) : null)
                .setUploadByAdminsOnly(album.isUploadByAdminsOnly())
                .setCommentsDisabled(album.isCommentsDisabled());
    }

    private static PhotoSizeEntity.Size model2entityNullable(@Nullable PhotoSizes.Size size) {
        if (nonNull(size)) {
            return new PhotoSizeEntity.Size()
                    .setUrl(size.getUrl())
                    .setW(size.getW())
                    .setH(size.getH());
        }
        return null;
    }

    public static PhotoSizeEntity buildPhotoSizeEntity(PhotoSizes sizes) {
        return new PhotoSizeEntity()
                .setS(model2entityNullable(sizes.getS()))
                .setM(model2entityNullable(sizes.getM()))
                .setX(model2entityNullable(sizes.getX()))
                .setO(model2entityNullable(sizes.getO()))
                .setP(model2entityNullable(sizes.getP()))
                .setQ(model2entityNullable(sizes.getQ()))
                .setR(model2entityNullable(sizes.getR()))
                .setY(model2entityNullable(sizes.getY()))
                .setZ(model2entityNullable(sizes.getZ()))
                .setW(model2entityNullable(sizes.getW()));
    }
}