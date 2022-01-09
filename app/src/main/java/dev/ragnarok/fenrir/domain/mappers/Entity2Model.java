package dev.ragnarok.fenrir.domain.mappers;

import static dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.ArticleEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioArtistEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioMessageEntity;
import dev.ragnarok.fenrir.db.model.entity.AudioPlaylistEntity;
import dev.ragnarok.fenrir.db.model.entity.CallEntity;
import dev.ragnarok.fenrir.db.model.entity.CareerEntity;
import dev.ragnarok.fenrir.db.model.entity.CityEntity;
import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.db.model.entity.CommunityDetailsEntity;
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity;
import dev.ragnarok.fenrir.db.model.entity.CountryEntity;
import dev.ragnarok.fenrir.db.model.entity.DialogEntity;
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.EventEntity;
import dev.ragnarok.fenrir.db.model.entity.FavePageEntity;
import dev.ragnarok.fenrir.db.model.entity.GiftItemEntity;
import dev.ragnarok.fenrir.db.model.entity.GraffitiEntity;
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity;
import dev.ragnarok.fenrir.db.model.entity.LinkEntity;
import dev.ragnarok.fenrir.db.model.entity.MarketAlbumEntity;
import dev.ragnarok.fenrir.db.model.entity.MarketEntity;
import dev.ragnarok.fenrir.db.model.entity.MessageEntity;
import dev.ragnarok.fenrir.db.model.entity.MilitaryEntity;
import dev.ragnarok.fenrir.db.model.entity.NewsEntity;
import dev.ragnarok.fenrir.db.model.entity.NotSupportedEntity;
import dev.ragnarok.fenrir.db.model.entity.PageEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoSizeEntity;
import dev.ragnarok.fenrir.db.model.entity.PollEntity;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.db.model.entity.PrivacyEntity;
import dev.ragnarok.fenrir.db.model.entity.SchoolEntity;
import dev.ragnarok.fenrir.db.model.entity.StickerEntity;
import dev.ragnarok.fenrir.db.model.entity.StickerSetEntity;
import dev.ragnarok.fenrir.db.model.entity.StickersKeywordsEntity;
import dev.ragnarok.fenrir.db.model.entity.StoryEntity;
import dev.ragnarok.fenrir.db.model.entity.TopicEntity;
import dev.ragnarok.fenrir.db.model.entity.UniversityEntity;
import dev.ragnarok.fenrir.db.model.entity.UserDetailsEntity;
import dev.ragnarok.fenrir.db.model.entity.UserEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoAlbumEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoEntity;
import dev.ragnarok.fenrir.db.model.entity.WallReplyEntity;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Attachments;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Call;
import dev.ragnarok.fenrir.model.Career;
import dev.ragnarok.fenrir.model.City;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.CommunityDetails;
import dev.ragnarok.fenrir.model.CryptStatus;
import dev.ragnarok.fenrir.model.Dialog;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Event;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.GiftItem;
import dev.ragnarok.fenrir.model.Graffiti;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.IdPair;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Military;
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
import dev.ragnarok.fenrir.model.School;
import dev.ragnarok.fenrir.model.SimplePrivacy;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.StickerSet;
import dev.ragnarok.fenrir.model.StickersKeywords;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.model.University;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserDetails;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VideoAlbum;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.model.database.Country;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.VKOwnIds;


public class Entity2Model {

    public static VideoAlbum buildVideoAlbumFromDbo(VideoAlbumEntity dbo) {
        return new VideoAlbum(dbo.getId(), dbo.getOwnerId())
                .setTitle(dbo.getTitle())
                .setCount(dbo.getCount())
                .setPrivacy(nonNull(dbo.getPrivacy()) ? mapSimplePrivacy(dbo.getPrivacy()) : null)
                .setImage(dbo.getImage())
                .setUpdatedTime(dbo.getUpdateTime());
    }

    public static Topic buildTopicFromDbo(TopicEntity dbo, IOwnersBundle owners) {
        Topic topic = new Topic(dbo.getId(), dbo.getOwnerId())
                .setTitle(dbo.getTitle())
                .setCreationTime(dbo.getCreatedTime())
                .setCreatedByOwnerId(dbo.getCreatorId())
                .setLastUpdateTime(dbo.getLastUpdateTime())
                .setUpdatedByOwnerId(dbo.getUpdatedBy())
                .setClosed(dbo.isClosed())
                .setFixed(dbo.isFixed())
                .setCommentsCount(dbo.getCommentsCount())
                .setFirstCommentBody(dbo.getFirstComment())
                .setLastCommentBody(dbo.getLastComment());

        if (dbo.getUpdatedBy() != 0) {
            topic.setUpdater(owners.getById(dbo.getUpdatedBy()));
        }

        if (dbo.getCreatorId() != 0) {
            topic.setCreator(owners.getById(dbo.getCreatorId()));
        }

        return topic;
    }

    public static List<Community> buildCommunitiesFromDbos(List<CommunityEntity> dbos) {
        List<Community> communities = new ArrayList<>(dbos.size());
        for (CommunityEntity dbo : dbos) {
            communities.add(buildCommunityFromDbo(dbo));
        }

        return communities;
    }

    public static Community buildCommunityFromDbo(CommunityEntity dbo) {
        return new Community(dbo.getId())
                .setName(dbo.getName())
                .setScreenName(dbo.getScreenName())
                .setClosed(dbo.getClosed())
                .setVerified(dbo.isVerified())
                .setAdmin(dbo.isAdmin())
                .setAdminLevel(dbo.getAdminLevel())
                .setMember(dbo.isMember())
                .setMemberStatus(dbo.getMemberStatus())
                .setType(dbo.getType())
                .setPhoto50(dbo.getPhoto50())
                .setPhoto100(dbo.getPhoto100())
                .setPhoto200(dbo.getPhoto200())
                .setMembersCount(dbo.getMembersCount());
    }

    public static CommunityDetails buildCommunityDetailsFromDbo(CommunityDetailsEntity dbo) {
        CommunityDetails details = new CommunityDetails()
                .setCanMessage(dbo.isCanMessage())
                .setFavorite(dbo.isSetFavorite())
                .setSubscribed(dbo.isSetSubscribed())
                .setStatus(dbo.getStatus())
                .setStatusAudio(nonNull(dbo.getStatusAudio()) ? buildAudioFromDbo(dbo.getStatusAudio()) : null)
                .setAllWallCount(dbo.getAllWallCount())
                .setOwnerWallCount(dbo.getOwnerWallCount())
                .setPostponedWallCount(dbo.getPostponedWallCount())
                .setSuggestedWallCount(dbo.getSuggestedWallCount())
                .setTopicsCount(dbo.getTopicsCount())
                .setDocsCount(dbo.getDocsCount())
                .setPhotosCount(dbo.getPhotosCount())
                .setAudiosCount(dbo.getAudiosCount())
                .setVideosCount(dbo.getVideosCount())
                .setProductsCount(dbo.getProductsCount())
                .setArticlesCount(dbo.getArticlesCount())
                .setChatsCount(dbo.getChatsCount());

        if (nonNull(dbo.getCover())) {
            CommunityDetails.Cover cover = new CommunityDetails.Cover()
                    .setEnabled(dbo.getCover().isEnabled())
                    .setImages(new ArrayList<>(safeCountOf(dbo.getCover().getImages())));

            if (nonNull(dbo.getCover().getImages())) {
                for (CommunityDetailsEntity.CoverImage imageDto : dbo.getCover().getImages()) {
                    cover.getImages().add(new CommunityDetails.CoverImage(imageDto.getUrl(), imageDto.getHeight(), imageDto.getWidth()));
                }
            }

            details.setCover(cover);
        } else {
            details.setCover(new CommunityDetails.Cover().setEnabled(false));
        }
        details.setDescription(dbo.getDescription());

        return details;
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

    public static List<User> buildUsersFromDbo(List<UserEntity> dbos) {
        List<User> users = new ArrayList<>(dbos.size());
        for (UserEntity dbo : dbos) {
            users.add(map(dbo));
        }

        return users;
    }

    public static List<FavePage> buildFaveUsersFromDbo(List<FavePageEntity> dbos) {
        List<FavePage> users = new ArrayList<>(dbos.size());
        for (FavePageEntity dbo : dbos) {
            users.add(map(dbo));
        }

        return users;
    }

    public static UserDetails buildUserDetailsFromDbo(UserDetailsEntity dbo, IOwnersBundle owners) {
        UserDetails details = new UserDetails()
                .setPhotoId(nonNull(dbo.getPhotoId()) ? new IdPair(dbo.getPhotoId().getId(), dbo.getPhotoId().getOwnerId()) : null)
                .setStatusAudio(nonNull(dbo.getStatusAudio()) ? buildAudioFromDbo(dbo.getStatusAudio()) : null)
                .setFriendsCount(dbo.getFriendsCount())
                .setOnlineFriendsCount(dbo.getOnlineFriendsCount())
                .setMutualFriendsCount(dbo.getMutualFriendsCount())
                .setFollowersCount(dbo.getFollowersCount())
                .setGroupsCount(dbo.getGroupsCount())
                .setPhotosCount(dbo.getPhotosCount())
                .setAudiosCount(dbo.getAudiosCount())
                .setVideosCount(dbo.getVideosCount())
                .setArticlesCount(dbo.getArticlesCount())
                .setProductsCount(dbo.getProductsCount())
                .setGiftCount(dbo.getGiftCount())
                .setAllWallCount(dbo.getAllWallCount())
                .setOwnWallCount(dbo.getOwnWallCount())
                .setPostponedWallCount(dbo.getPostponedWallCount())
                .setBdate(dbo.getBdate())
                .setCity(isNull(dbo.getCity()) ? null : map(dbo.getCity()))
                .setCountry(isNull(dbo.getCountry()) ? null : map(dbo.getCountry()))
                .setHometown(dbo.getHomeTown())
                .setPhone(dbo.getPhone())
                .setHomePhone(dbo.getHomePhone())
                .setSkype(dbo.getSkype())
                .setInstagram(dbo.getInstagram())
                .setTwitter(dbo.getTwitter())
                .setFacebook(dbo.getFacebook());

        details.setMilitaries(mapAll(dbo.getMilitaries(), Entity2Model::map));
        details.setCareers(mapAll(dbo.getCareers(), orig -> map(orig, owners)));
        details.setUniversities(mapAll(dbo.getUniversities(), Entity2Model::map));
        details.setSchools(mapAll(dbo.getSchools(), Entity2Model::map));
        details.setRelatives(mapAll(dbo.getRelatives(), orig -> map(orig, owners)));

        details.setRelation(dbo.getRelation());
        details.setRelationPartner(dbo.getRelationPartnerId() != 0 ? owners.getById(dbo.getRelationPartnerId()) : null);
        details.setLanguages(dbo.getLanguages());

        details.setPolitical(dbo.getPolitical());
        details.setPeopleMain(dbo.getPeopleMain());
        details.setLifeMain(dbo.getLifeMain());
        details.setSmoking(dbo.getSmoking());
        details.setAlcohol(dbo.getAlcohol());
        details.setInspiredBy(dbo.getInspiredBy());
        details.setReligion(dbo.getReligion());
        details.setSite(dbo.getSite());
        details.setInterests(dbo.getInterests());
        details.setMusic(dbo.getMusic());
        details.setActivities(dbo.getActivities());
        details.setMovies(dbo.getMovies());
        details.setTv(dbo.getTv());
        details.setGames(dbo.getGames());
        details.setQuotes(dbo.getQuotes());
        details.setAbout(dbo.getAbout());
        details.setBooks(dbo.getBooks());
        details.setFavorite(dbo.isSetFavorite());
        details.setSubscribed(dbo.isSetSubscribed());
        return details;
    }

    public static UserDetails.Relative map(UserDetailsEntity.RelativeEntity entity, IOwnersBundle owners) {
        return new UserDetails.Relative()
                .setUser(entity.getId() > 0 ? (User) owners.getById(entity.getId()) : null)
                .setName(entity.getName())
                .setType(entity.getType());
    }

    public static School map(SchoolEntity entity) {
        return new School()
                .setCityId(entity.getCityId())
                .setCountryId(entity.getCountryId())
                .setId(entity.getId())
                .setClazz(entity.getClazz())
                .setName(entity.getName())
                .setTo(entity.getTo())
                .setFrom(entity.getFrom())
                .setYearGraduated(entity.getYearGraduated());
    }

    public static University map(UniversityEntity entity) {
        return new University()
                .setName(entity.getName())
                .setCityId(entity.getCityId())
                .setCountryId(entity.getCountryId())
                .setStatus(entity.getStatus())
                .setGraduationYear(entity.getGraduationYear())
                .setId(entity.getId())
                .setFacultyId(entity.getFacultyId())
                .setFacultyName(entity.getFacultyName())
                .setChairId(entity.getChairId())
                .setChairName(entity.getChairName())
                .setForm(entity.getForm());
    }

    public static Military map(MilitaryEntity entity) {
        return new Military()
                .setCountryId(entity.getCountryId())
                .setFrom(entity.getFrom())
                .setUnit(entity.getUnit())
                .setUntil(entity.getUntil())
                .setUnitId(entity.getUnitId());
    }

    public static Career map(CareerEntity entity, IOwnersBundle bundle) {
        return new Career()
                .setCityId(entity.getCityId())
                .setCompany(entity.getCompany())
                .setCountryId(entity.getCountryId())
                .setFrom(entity.getFrom())
                .setUntil(entity.getUntil())
                .setPosition(entity.getPosition())
                .setGroup(entity.getGroupId() == 0 ? null : (Community) bundle.getById(-entity.getGroupId()));
    }

    public static Country map(CountryEntity entity) {
        return new Country(entity.getId(), entity.getTitle());
    }

    public static City map(CityEntity entity) {
        return new City(entity.getId(), entity.getTitle())
                .setArea(entity.getArea())
                .setImportant(entity.isImportant())
                .setRegion(entity.getRegion());
    }

    public static User map(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(entity.getId())
                .setFirstName(entity.getFirstName())
                .setLastName(entity.getLastName())
                .setOnline(entity.isOnline())
                .setOnlineMobile(entity.isOnlineMobile())
                .setOnlineApp(entity.getOnlineApp())
                .setPhoto50(entity.getPhoto50())
                .setPhoto100(entity.getPhoto100())
                .setPhoto200(entity.getPhoto200())
                .setPhotoMax(entity.getPhotoMax())
                .setLastSeen(entity.getLastSeen())
                .setPlatform(entity.getPlatform())
                .setStatus(entity.getStatus())
                .setSex(entity.getSex())
                .setDomain(entity.getDomain())
                .setFriend(entity.isFriend())
                .setFriendStatus(entity.getFriendStatus())
                .setCanWritePrivateMessage(entity.getCanWritePrivateMessage())
                .setBlacklisted(entity.getBlacklisted())
                .setBlacklisted_by_me(entity.getBlacklisted_by_me())
                .setVerified(entity.isVerified())
                .setCan_access_closed(entity.isCan_access_closed())
                .setMaiden_name(entity.getMaiden_name());
    }

    public static FavePage map(FavePageEntity entity) {
        return new FavePage(entity.getId())
                .setDescription(entity.getDescription())
                .setUpdatedDate(entity.getUpdateDate())
                .setFaveType(entity.getFaveType())
                .setUser(nonNull(entity.getUser()) ? map(entity.getUser()) : null)
                .setGroup(nonNull(entity.getGroup()) ? map(entity.getGroup()) : null);
    }

    public static Community map(CommunityEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Community(entity.getId())
                .setName(entity.getName())
                .setScreenName(entity.getScreenName())
                .setPhoto50(entity.getPhoto50())
                .setPhoto100(entity.getPhoto100())
                .setPhoto200(entity.getPhoto200())
                .setAdmin(entity.isAdmin())
                .setAdminLevel(entity.getAdminLevel())
                .setClosed(entity.getClosed())
                .setVerified(entity.isVerified())
                .setMember(entity.isMember())
                .setMemberStatus(entity.getMemberStatus())
                .setMembersCount(entity.getMembersCount())
                .setType(entity.getType());
    }

    public static PhotoAlbum mapPhotoAlbum(PhotoAlbumEntity entity) {
        return new PhotoAlbum(entity.getId(), entity.getOwnerId())
                .setSize(entity.getSize())
                .setTitle(entity.getTitle())
                .setDescription(entity.getDescription())
                .setCanUpload(entity.isCanUpload())
                .setUpdatedTime(entity.getUpdatedTime())
                .setCreatedTime(entity.getCreatedTime())
                .setSizes(nonNull(entity.getSizes()) ? buildPhotoSizesFromDbo(entity.getSizes()) : PhotoSizes.empty())
                .setPrivacyView(nonNull(entity.getPrivacyView()) ? mapSimplePrivacy(entity.getPrivacyView()) : null)
                .setPrivacyComment(nonNull(entity.getPrivacyComment()) ? mapSimplePrivacy(entity.getPrivacyComment()) : null)
                .setUploadByAdminsOnly(entity.isUploadByAdminsOnly())
                .setCommentsDisabled(entity.isCommentsDisabled());
    }

    public static Comment buildCommentFromDbo(CommentEntity dbo, IOwnersBundle owners) {
        Attachments attachments = Utils.isEmpty(dbo.getAttachments()) ? null : buildAttachmentsFromDbos(dbo.getAttachments(), owners);

        return new Comment(new Commented(dbo.getSourceId(), dbo.getSourceOwnerId(), dbo.getSourceType(), dbo.getSourceAccessKey()))
                .setId(dbo.getId())
                .setFromId(dbo.getFromId())
                .setDate(dbo.getDate())
                .setText(dbo.getText())
                .setReplyToUser(dbo.getReplyToUserId())
                .setReplyToComment(dbo.getReplyToComment())
                .setLikesCount(dbo.getLikesCount())
                .setUserLikes(dbo.isUserLikes())
                .setCanLike(dbo.isCanLike())
                .setCanEdit(dbo.isCanEdit())
                .setAttachments(attachments)
                .setAuthor(owners.getById(dbo.getFromId()))
                .setThreadsCount(dbo.getThreadsCount())
                .setThreads(buildCommentsFromDbo(dbo.getThreads(), owners))
                .setPid(dbo.getPid())
                .setDeleted(dbo.isDeleted());
    }

    public static List<Comment> buildCommentsFromDbo(List<CommentEntity> dbos, IOwnersBundle owners) {
        if (Utils.isEmpty(dbos)) {
            return null;
        }
        List<Comment> o = new ArrayList<>();
        for (CommentEntity i : dbos) {
            Comment u = buildCommentFromDbo(i, owners);
            if (nonNull(u)) {
                o.add(u);
            }
        }
        return o;
    }

    public static Dialog buildDialogFromDbo(int accountId, DialogEntity entity, IOwnersBundle owners) {
        Message message = message(accountId, entity.getMessage(), owners);

        Dialog dialog = new Dialog()
                .setLastMessageId(entity.getLastMessageId())
                .setPeerId(entity.getPeerId())
                .setPhoto50(entity.getPhoto50())
                .setPhoto100(entity.getPhoto100())
                .setPhoto200(entity.getPhoto200())
                .setTitle(entity.getTitle())
                .setMessage(message)
                .setUnreadCount(entity.getUnreadCount())
                .setOutRead(entity.getOutRead())
                .setInRead(entity.getInRead())
                .setGroupChannel(entity.isGroupChannel())
                .setMajor_id(entity.getMajor_id())
                .setMinor_id(entity.getMinor_id());

        switch (Peer.getType(entity.getPeerId())) {
            case Peer.GROUP:
            case Peer.USER:
                dialog.setInterlocutor(owners.getById(dialog.getPeerId()));
                break;
            case Peer.CHAT:
                dialog.setInterlocutor(owners.getById(message.getSenderId()));
                break;
            default:
                throw new IllegalArgumentException("Invalid peer_id");
        }

        return dialog;
    }

    public static Keyboard buildKeyboardFromDbo(KeyboardEntity keyboard) {
        if (keyboard == null || Utils.isEmpty(keyboard.getButtons())) {
            return null;
        }
        List<List<Keyboard.Button>> buttons = new ArrayList<>(keyboard.getButtons().size());
        for (List<KeyboardEntity.ButtonEntity> i : keyboard.getButtons()) {
            List<Keyboard.Button> vt = new ArrayList<>(i.size());
            for (KeyboardEntity.ButtonEntity s : i) {
                vt.add(new Keyboard.Button().setType(s.getType()).setColor(s.getColor()).setLabel(s.getLabel()).setLink(s.getLink()).setPayload(s.getPayload()));
            }
            buttons.add(vt);
        }
        return new Keyboard().setAuthor_id(
                keyboard.getAuthor_id()).setInline(keyboard.getInline())
                .setOne_time(keyboard.getOne_time()).setButtons(buttons);
    }

    public static Message message(int accountId, MessageEntity dbo, IOwnersBundle owners) {
        Message message = new Message(dbo.getId())
                .setAccountId(accountId)
                .setBody(dbo.getBody())
                .setPeerId(dbo.getPeerId())
                .setSenderId(dbo.getFromId())
                .setOut(dbo.isOut())
                .setStatus(dbo.getStatus())
                .setDate(dbo.getDate())
                .setHasAttachments(dbo.isHasAttachmens())
                .setForwardMessagesCount(dbo.getForwardCount())
                .setDeleted(dbo.isDeleted())
                .setDeletedForAll(dbo.isDeletedForAll())
                .setOriginalId(dbo.getOriginalId())
                .setCryptStatus(dbo.isEncrypted() ? CryptStatus.ENCRYPTED : CryptStatus.NO_ENCRYPTION)
                .setImportant(dbo.isImportant())
                .setAction(dbo.getAction())
                .setActionMid(dbo.getActionMemberId())
                .setActionEmail(dbo.getActionEmail())
                .setActionText(dbo.getActionText())
                .setPhoto50(dbo.getPhoto50())
                .setPhoto100(dbo.getPhoto100())
                .setPhoto200(dbo.getPhoto200())
                .setSender(owners.getById(dbo.getFromId()))
                .setRandomId(dbo.getRandomId())
                .setUpdateTime(dbo.getUpdateTime())
                .setPayload(dbo.getPayload())
                .setKeyboard(buildKeyboardFromDbo(dbo.getKeyboard()));

        if (dbo.getActionMemberId() != 0) {
            message.setActionUser(owners.getById(dbo.getActionMemberId()));
        }

        if (nonEmpty(dbo.getAttachments())) {
            message.setAttachments(buildAttachmentsFromDbos(dbo.getAttachments(), owners));
        }

        if (nonEmpty(dbo.getForwardMessages())) {
            for (MessageEntity fwdDbo : dbo.getForwardMessages()) {
                message.prepareFwd(dbo.getForwardMessages().size()).add(message(accountId, fwdDbo, owners));
            }
        }

        return message;
    }

    public static Attachments buildAttachmentsFromDbos(List<Entity> entities, IOwnersBundle owners) {
        Attachments attachments = new Attachments();

        for (Entity entity : entities) {
            attachments.add(buildAttachmentFromDbo(entity, owners));
        }

        return attachments;
    }

    public static AbsModel buildAttachmentFromDbo(Entity entity, IOwnersBundle owners) {
        if (entity instanceof PhotoEntity) {
            return map((PhotoEntity) entity);
        }

        if (entity instanceof VideoEntity) {
            return buildVideoFromDbo((VideoEntity) entity);
        }

        if (entity instanceof PostEntity) {
            return buildPostFromDbo((PostEntity) entity, owners);
        }

        if (entity instanceof LinkEntity) {
            return buildLinkFromDbo((LinkEntity) entity);
        }

        if (entity instanceof ArticleEntity) {
            return buildArticleFromDbo((ArticleEntity) entity);
        }

        if (entity instanceof StoryEntity) {
            return buildStoryFromDbo((StoryEntity) entity, owners);
        }

        if (entity instanceof PhotoAlbumEntity) {
            return mapPhotoAlbum((PhotoAlbumEntity) entity);
        }

        if (entity instanceof GraffitiEntity) {
            return buildGraffityFromDbo((GraffitiEntity) entity);
        }

        if (entity instanceof AudioPlaylistEntity) {
            return buildAudioPlaylistFromDbo((AudioPlaylistEntity) entity);
        }

        if (entity instanceof CallEntity) {
            return buildCallFromDbo((CallEntity) entity);
        }

        if (entity instanceof WallReplyEntity) {
            return buildWallReplyDbo((WallReplyEntity) entity, owners);
        }

        if (entity instanceof NotSupportedEntity) {
            return buildNotSupportedFromDbo((NotSupportedEntity) entity);
        }

        if (entity instanceof EventEntity) {
            return buildEventFromDbo((EventEntity) entity, owners);
        }

        if (entity instanceof MarketEntity) {
            return buildMarketFromDbo((MarketEntity) entity);
        }

        if (entity instanceof MarketAlbumEntity) {
            return buildMarketAlbumFromDbo((MarketAlbumEntity) entity);
        }

        if (entity instanceof AudioArtistEntity) {
            return buildAudioArtistFromDbo((AudioArtistEntity) entity);
        }

        if (entity instanceof PollEntity) {
            return buildPollFromDbo((PollEntity) entity);
        }

        if (entity instanceof DocumentEntity) {
            return buildDocumentFromDbo((DocumentEntity) entity);
        }

        if (entity instanceof PageEntity) {
            return buildWikiPageFromDbo((PageEntity) entity);
        }

        if (entity instanceof StickerEntity) {
            return buildStickerFromDbo((StickerEntity) entity);
        }

        if (entity instanceof AudioEntity) {
            return buildAudioFromDbo((AudioEntity) entity);
        }

        if (entity instanceof TopicEntity) {
            return buildTopicFromDbo((TopicEntity) entity, owners);
        }

        if (entity instanceof AudioMessageEntity) {
            return map((AudioMessageEntity) entity);
        }

        if (entity instanceof GiftItemEntity) {
            return buildGiftItemFromDbo((GiftItemEntity) entity);
        }

        throw new UnsupportedOperationException("Unsupported DBO class: " + entity.getClass());
    }

    public static Audio buildAudioFromDbo(AudioEntity dbo) {
        return new Audio()
                .setAccessKey(dbo.getAccessKey())
                .setAlbumId(dbo.getAlbumId())
                .setAlbum_owner_id(dbo.getAlbum_owner_id())
                .setAlbum_access_key(dbo.getAlbum_access_key())
                .setArtist(dbo.getArtist())
                .setDeleted(dbo.isDeleted())
                .setDuration(dbo.getDuration())
                .setUrl(dbo.getUrl())
                .setId(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setLyricsId(dbo.getLyricsId())
                .setTitle(dbo.getTitle())
                .setGenre(dbo.getGenre())
                .setAlbum_title(dbo.getAlbum_title())
                .setThumb_image_big(dbo.getThumb_image_big())
                .setThumb_image_little(dbo.getThumb_image_little())
                .setThumb_image_very_big(dbo.getThumb_image_very_big())
                .setIsHq(dbo.getIsHq())
                .setMain_artists(dbo.getMain_artists()).updateDownloadIndicator();
    }

    public static AudioPlaylist buildAudioPlaylistFromDbo(AudioPlaylistEntity dto) {
        return new AudioPlaylist()
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

    public static GiftItem buildGiftItemFromDbo(GiftItemEntity entity) {
        return new GiftItem(entity.getId())
                .setThumb48(entity.getThumb48())
                .setThumb96(entity.getThumb96())
                .setThumb256(entity.getThumb256());
    }

    public static Sticker buildStickerFromDbo(StickerEntity entity) {
        return new Sticker(entity.getId())
                .setImages(mapAll(entity.getImages(), Entity2Model::map))
                .setImagesWithBackground(mapAll(entity.getImagesWithBackground(), Entity2Model::map))
                .setAnimations(mapAll(entity.getAnimations(), Entity2Model::mapStickerAnimation))
                .setAnimationUrl(entity.getAnimationUrl());
    }

    public static Sticker.Animation mapStickerAnimation(StickerEntity.AnimationEntity entity) {
        return new Sticker.Animation(entity.getUrl(), entity.getType());
    }

    public static StickerSet map(StickerSetEntity entity) {
        return new StickerSet(mapAll(entity.getIcon(), Entity2Model::map), mapAll(entity.getStickers(), Entity2Model::buildStickerFromDbo), entity.getTitle());
    }

    public static StickersKeywords map(StickersKeywordsEntity entity) {
        return new StickersKeywords(entity.getKeywords(), mapAll(entity.getStickers(), Entity2Model::buildStickerFromDbo));
    }

    public static Sticker.Image map(StickerEntity.Img entity) {
        return new Sticker.Image(entity.getUrl(), entity.getWidth(), entity.getHeight());
    }

    public static StickerSet.Image map(StickerSetEntity.Img entity) {
        return new StickerSet.Image(entity.getUrl(), entity.getWidth(), entity.getHeight());
    }

    public static WikiPage buildWikiPageFromDbo(PageEntity dbo) {
        return new WikiPage(dbo.getId(), dbo.getOwnerId())
                .setCreatorId(dbo.getCreatorId())
                .setTitle(dbo.getTitle())
                .setSource(dbo.getSource())
                .setEditionTime(dbo.getEditionTime())
                .setCreationTime(dbo.getCreationTime())
                .setParent(dbo.getParent())
                .setParent2(dbo.getParent2())
                .setViews(dbo.getViews())
                .setViewUrl(dbo.getViewUrl());
    }

    public static VoiceMessage map(AudioMessageEntity entity) {
        return new VoiceMessage(entity.getId(), entity.getOwnerId())
                .setAccessKey(entity.getAccessKey())
                .setDuration(entity.getDuration())
                .setLinkMp3(entity.getLinkMp3())
                .setLinkOgg(entity.getLinkOgg())
                .setWaveform(entity.getWaveform())
                .setTranscript(entity.getTranscript());
    }

    public static Document buildDocumentFromDbo(DocumentEntity dbo) {
        Document document = new Document(dbo.getId(), dbo.getOwnerId());

        document.setTitle(dbo.getTitle())
                .setSize(dbo.getSize())
                .setExt(dbo.getExt())
                .setUrl(dbo.getUrl())
                .setAccessKey(dbo.getAccessKey())
                .setDate(dbo.getDate())
                .setType(dbo.getType());

        if (nonNull(dbo.getPhoto())) {
            document.setPhotoPreview(buildPhotoSizesFromDbo(dbo.getPhoto()));
        }

        if (nonNull(dbo.getVideo())) {
            document.setVideoPreview(new Document.VideoPreview()
                    .setWidth(dbo.getVideo().getWidth())
                    .setHeight(dbo.getVideo().getHeight())
                    .setSrc(dbo.getVideo().getSrc()));
        }

        if (nonNull(dbo.getGraffiti())) {
            document.setGraffiti(new Document.Graffiti()
                    .setHeight(dbo.getGraffiti().getHeight())
                    .setWidth(dbo.getGraffiti().getWidth())
                    .setSrc(dbo.getGraffiti().getSrc()));
        }

        return document;
    }

    public static Poll.Answer map(PollEntity.Answer entity) {
        return new Poll.Answer(entity.getId())
                .setRate(entity.getRate())
                .setText(entity.getText())
                .setVoteCount(entity.getVoteCount());
    }

    public static Poll buildPollFromDbo(PollEntity entity) {
        return new Poll(entity.getId(), entity.getOwnerId())
                .setAnonymous(entity.isAnonymous())
                .setAnswers(mapAll(entity.getAnswers(), Entity2Model::map))
                .setBoard(entity.isBoard())
                .setCreationTime(entity.getCreationTime())
                .setMyAnswerIds(entity.getMyAnswerIds())
                .setQuestion(entity.getQuestion())
                .setVoteCount(entity.getVoteCount())
                .setClosed(entity.closed)
                .setAuthorId(entity.authorId)
                .setCanVote(entity.canVote)
                .setCanEdit(entity.canEdit)
                .setCanReport(entity.canReport)
                .setCanShare(entity.canShare)
                .setEndDate(entity.endDate)
                .setMultiple(entity.multiple)
                .setPhoto(entity.getPhoto());
    }

    public static Link buildLinkFromDbo(LinkEntity dbo) {
        return new Link()
                .setUrl(dbo.getUrl())
                .setTitle(dbo.getTitle())
                .setCaption(dbo.getCaption())
                .setDescription(dbo.getDescription())
                .setPreviewPhoto(dbo.getPreviewPhoto())
                .setPhoto(nonNull(dbo.getPhoto()) ? map(dbo.getPhoto()) : null);
    }

    public static Article buildArticleFromDbo(ArticleEntity dbo) {
        return new Article(dbo.getId(), dbo.getOwnerId())
                .setAccessKey(dbo.getAccessKey())
                .setOwnerName(dbo.getOwnerName())
                .setPhoto(nonNull(dbo.getPhoto()) ? map(dbo.getPhoto()) : null)
                .setTitle(dbo.getTitle())
                .setSubTitle(dbo.getSubTitle())
                .setURL(dbo.getURL())
                .setIsFavorite(dbo.getIsFavorite());
    }

    public static Call buildCallFromDbo(CallEntity dbo) {
        return new Call().setInitiator_id(dbo.getInitiator_id())
                .setReceiver_id(dbo.getReceiver_id())
                .setState(dbo.getState())
                .setTime(dbo.getTime());
    }

    public static WallReply buildWallReplyDbo(@NonNull WallReplyEntity dbo, @NonNull IOwnersBundle owners) {
        WallReply comment = new WallReply().setId(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setFromId(dbo.getFromId())
                .setPostId(dbo.getPostId())
                .setText(dbo.getText())
                .setAuthor(owners.getById(dbo.getFromId()));

        Attachments attachments = nonEmpty(dbo.getAttachments()) ? buildAttachmentsFromDbos(dbo.getAttachments(), owners) : null;
        comment.setAttachments(attachments);
        return comment;
    }

    public static NotSupported buildNotSupportedFromDbo(NotSupportedEntity dbo) {
        return new NotSupported().setType(dbo.getType()).setBody(dbo.getBody());
    }

    public static Event buildEventFromDbo(EventEntity dbo, IOwnersBundle owners) {
        return new Event(dbo.getId()).setButton_text(dbo.getButton_text()).setText(dbo.getText()).setSubject(owners.getById(dbo.getId() >= 0 ? -dbo.getId() : dbo.getId()));
    }

    public static Market buildMarketFromDbo(@NonNull MarketEntity dbo) {
        return new Market(dbo.getId(), dbo.getOwner_id())
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

    public static MarketAlbum buildMarketAlbumFromDbo(@NonNull MarketAlbumEntity dbo) {
        return new MarketAlbum(dbo.getId(), dbo.getOwner_id())
                .setAccess_key(dbo.getAccess_key())
                .setCount(dbo.getCount())
                .setTitle(dbo.getTitle())
                .setUpdated_time(dbo.getUpdated_time())
                .setPhoto(dbo.getPhoto() != null ? map(dbo.getPhoto()) : null);
    }

    public static AudioArtist.AudioArtistImage mapArtistImage(AudioArtistEntity.AudioArtistImageEntity dbo) {
        return new AudioArtist.AudioArtistImage(dbo.getUrl(), dbo.getWidth(), dbo.getHeight());
    }

    public static AudioArtist buildAudioArtistFromDbo(AudioArtistEntity dbo) {
        return new AudioArtist(dbo.getId())
                .setName(dbo.getName())
                .setPhoto(mapAll(dbo.getPhoto(), Entity2Model::mapArtistImage));
    }

    public static Story buildStoryFromDbo(StoryEntity dbo, IOwnersBundle owners) {
        return new Story().setId(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setDate(dbo.getDate())
                .setExpires(dbo.getExpires())
                .setIs_expired(dbo.isIs_expired())
                .setAccessKey(dbo.getAccessKey())
                .setTarget_url(dbo.getTarget_url())
                .setOwner(owners.getById(dbo.getOwnerId()))
                .setPhoto(nonNull(dbo.getPhoto()) ? map(dbo.getPhoto()) : null)
                .setVideo(dbo.getVideo() != null ? buildVideoFromDbo(dbo.getVideo()) : null);
    }

    public static Graffiti buildGraffityFromDbo(GraffitiEntity dto) {
        return new Graffiti().setId(dto.getId())
                .setOwner_id(dto.getOwner_id())
                .setAccess_key(dto.getAccess_key())
                .setHeight(dto.getHeight())
                .setWidth(dto.getWidth())
                .setUrl(dto.getUrl());
    }

    public static News buildNewsFromDbo(NewsEntity dbo, IOwnersBundle owners) {
        News news = new News()
                .setType(dbo.getType())
                .setSourceId(dbo.getSourceId())
                .setSource(owners.getById(dbo.getSourceId()))
                .setPostType(dbo.getPostType())
                .setFinalPost(dbo.isFinalPost())
                .setCopyOwnerId(dbo.getCopyOwnerId())
                .setCopyPostId(dbo.getCopyPostId())
                .setCopyPostDate(dbo.getCopyPostDate())
                .setDate(dbo.getDate())
                .setPostId(dbo.getPostId())
                .setText(dbo.getText())
                .setCanEdit(dbo.isCanEdit())
                .setCanDelete(dbo.isCanDelete())
                .setCommentCount(dbo.getCommentCount())
                .setCommentCanPost(dbo.isCanPostComment())
                .setLikeCount(dbo.getLikesCount())
                .setUserLike(dbo.isUserLikes())
                .setCanLike(dbo.isCanLike())
                .setCanPublish(dbo.isCanPublish())
                .setRepostsCount(dbo.getRepostCount())
                .setUserReposted(dbo.isUserReposted())
                .setFriends(dbo.getFriendsTags() == null ? null : buildUserArray(dbo.getFriendsTags(), owners))
                .setViewCount(dbo.getViews());

        if (nonEmpty(dbo.getAttachments())) {
            news.setAttachments(buildAttachmentsFromDbos(dbo.getAttachments(), owners));
        } else {
            news.setAttachments(new Attachments());
        }

        if (nonEmpty(dbo.getCopyHistory())) {
            List<Post> copies = new ArrayList<>(dbo.getCopyHistory().size());
            for (PostEntity copyDbo : dbo.getCopyHistory()) {
                copies.add(buildPostFromDbo(copyDbo, owners));
            }

            news.setCopyHistory(copies);
        } else {
            news.setCopyHistory(Collections.emptyList());
        }

        return news;
    }

    public static Post buildPostFromDbo(PostEntity dbo, IOwnersBundle owners) {
        Post post = new Post()
                .setDbid(dbo.getDbid())
                .setVkid(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setAuthorId(dbo.getFromId())
                .setDate(dbo.getDate())
                .setText(dbo.getText())
                .setReplyOwnerId(dbo.getReplyOwnerId())
                .setReplyPostId(dbo.getReplyPostId())
                .setFriendsOnly(dbo.isFriendsOnly())
                .setCommentsCount(dbo.getCommentsCount())
                .setCanPostComment(dbo.isCanPostComment())
                .setLikesCount(dbo.getLikesCount())
                .setUserLikes(dbo.isUserLikes())
                .setCanLike(dbo.isCanLike())
                .setCanRepost(dbo.isCanPublish())
                .setRepostCount(dbo.getRepostCount())
                .setUserReposted(dbo.isUserReposted())
                .setPostType(dbo.getPostType())
                .setSignerId(dbo.getSignedId())
                .setCreatorId(dbo.getCreatedBy())
                .setCanEdit(dbo.isCanEdit())
                .setCanPin(dbo.isCanPin())
                .setPinned(dbo.isPinned())
                .setViewCount(dbo.getViews());

        PostEntity.SourceDbo sourceDbo = dbo.getSource();
        if (nonNull(sourceDbo)) {
            post.setSource(new PostSource(sourceDbo.getType(), sourceDbo.getPlatform(), sourceDbo.getData(), sourceDbo.getUrl()));
        }

        if (nonEmpty(dbo.getAttachments())) {
            post.setAttachments(buildAttachmentsFromDbos(dbo.getAttachments(), owners));
        }

        if (nonEmpty(dbo.getCopyHierarchy())) {
            int copyCount = safeCountOf(dbo.getCopyHierarchy());

            for (PostEntity copyDbo : dbo.getCopyHierarchy()) {
                post.prepareCopyHierarchy(copyCount).add(buildPostFromDbo(copyDbo, owners));
            }
        }

        Dto2Model.fillPostOwners(post, owners);

        if (post.hasCopyHierarchy()) {
            for (Post copy : post.getCopyHierarchy()) {
                Dto2Model.fillPostOwners(copy, owners);
            }
        }

        return post;
    }

    public static SimplePrivacy mapSimplePrivacy(PrivacyEntity dbo) {
        return new SimplePrivacy(dbo.getType(), mapAll(dbo.getEntries(), orig -> new SimplePrivacy.Entry(orig.getType(), orig.getId(), orig.isAllowed())));
    }

    public static Video buildVideoFromDbo(VideoEntity entity) {
        return new Video()
                .setId(entity.getId())
                .setOwnerId(entity.getOwnerId())
                .setAlbumId(entity.getAlbumId())
                .setTitle(entity.getTitle())
                .setDescription(entity.getDescription())
                .setDuration(entity.getDuration())
                .setLink(entity.getLink())
                .setDate(entity.getDate())
                .setAddingDate(entity.getAddingDate())
                .setViews(entity.getViews())
                .setPlayer(entity.getPlayer())
                .setImage(entity.getImage())
                .setAccessKey(entity.getAccessKey())
                .setCommentsCount(entity.getCommentsCount())
                .setCanComment(entity.isCanComment())
                .setCanRepost(entity.isCanRepost())
                .setUserLikes(entity.isUserLikes())
                .setRepeat(entity.isRepeat())
                .setLikesCount(entity.getLikesCount())
                .setPrivacyView(nonNull(entity.getPrivacyView()) ? mapSimplePrivacy(entity.getPrivacyView()) : null)
                .setPrivacyComment(nonNull(entity.getPrivacyComment()) ? mapSimplePrivacy(entity.getPrivacyComment()) : null)
                .setMp4link240(entity.getMp4link240())
                .setMp4link360(entity.getMp4link360())
                .setMp4link480(entity.getMp4link480())
                .setMp4link720(entity.getMp4link720())
                .setMp4link1080(entity.getMp4link1080())
                .setExternalLink(entity.getExternalLink())
                .setHls(entity.getHls())
                .setLive(entity.getLive())
                .setPlatform(entity.getPlatform())
                .setCanEdit(entity.isCanEdit())
                .setCanAdd(entity.isCanAdd())
                .setPrivate(entity.getPrivate());
    }

    public static Photo map(PhotoEntity dbo) {
        return new Photo()
                .setId(dbo.getId())
                .setAlbumId(dbo.getAlbumId())
                .setOwnerId(dbo.getOwnerId())
                .setWidth(dbo.getWidth())
                .setHeight(dbo.getHeight())
                .setText(dbo.getText())
                .setDate(dbo.getDate())
                .setUserLikes(dbo.isUserLikes())
                .setCanComment(dbo.isCanComment())
                .setLikesCount(dbo.getLikesCount())
                .setCommentsCount(dbo.getCommentsCount())
                .setTagsCount(dbo.getTagsCount())
                .setAccessKey(dbo.getAccessKey())
                .setDeleted(dbo.isDeleted())
                .setPostId(dbo.getPostId())
                .setSizes(nonNull(dbo.getSizes()) ? buildPhotoSizesFromDbo(dbo.getSizes()) : new PhotoSizes());
    }

    private static PhotoSizes.Size entity2modelNullable(@Nullable PhotoSizeEntity.Size size) {
        if (nonNull(size)) {
            return new PhotoSizes.Size(size.getW(), size.getH(), size.getUrl());
        }
        return null;
    }

    public static PhotoSizes buildPhotoSizesFromDbo(PhotoSizeEntity dbo) {
        return new PhotoSizes()
                .setS(entity2modelNullable(dbo.getS()))
                .setM(entity2modelNullable(dbo.getM()))
                .setX(entity2modelNullable(dbo.getX()))
                .setO(entity2modelNullable(dbo.getO()))
                .setP(entity2modelNullable(dbo.getP()))
                .setQ(entity2modelNullable(dbo.getQ()))
                .setR(entity2modelNullable(dbo.getR()))
                .setY(entity2modelNullable(dbo.getY()))
                .setZ(entity2modelNullable(dbo.getZ()))
                .setW(entity2modelNullable(dbo.getW()));
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, @Nullable List<? extends Entity> dbos) {
        if (nonNull(dbos)) {
            for (Entity entity : dbos) {
                fillOwnerIds(ids, entity);
            }
        }
    }

    public static void fillPostOwnerIds(@NonNull VKOwnIds ids, @Nullable PostEntity dbo) {
        if (nonNull(dbo)) {
            ids.append(dbo.getFromId());
            ids.append(dbo.getSignedId());
            ids.append(dbo.getCreatedBy());

            fillOwnerIds(ids, dbo.getAttachments());
            fillOwnerIds(ids, dbo.getCopyHierarchy());
        }
    }

    public static void fillStoryOwnerIds(@NonNull VKOwnIds ids, @Nullable StoryEntity dbo) {
        if (nonNull(dbo)) {
            ids.append(dbo.getOwnerId());
        }
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, CommentEntity entity) {
        fillCommentOwnerIds(ids, entity);
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, @Nullable Entity entity) {
        if (entity instanceof MessageEntity) {
            fillMessageOwnerIds(ids, (MessageEntity) entity);
        } else if (entity instanceof PostEntity) {
            fillPostOwnerIds(ids, (PostEntity) entity);
        } else if (entity instanceof StoryEntity) {
            fillStoryOwnerIds(ids, (StoryEntity) entity);
        } else if (entity instanceof WallReplyEntity) {
            fillWallReplyOwnerIds(ids, (WallReplyEntity) entity);
        } else if (entity instanceof EventEntity) {
            fillEventIds(ids, (EventEntity) entity);
        }
    }

    public static void fillWallReplyOwnerIds(@NonNull VKOwnIds ids, @Nullable WallReplyEntity dbo) {
        if (nonNull(dbo)) {
            ids.append(dbo.getFromId());

            if (nonNull(dbo.getAttachments())) {
                fillOwnerIds(ids, dbo.getAttachments());
            }
        }
    }

    public static void fillEventIds(@NonNull VKOwnIds ids, @Nullable EventEntity dbo) {
        if (nonNull(dbo)) {
            ids.append(dbo.getId() >= 0 ? -dbo.getId() : dbo.getId());
        }
    }

    public static void fillCommentOwnerIds(@NonNull VKOwnIds ids, @Nullable CommentEntity dbo) {
        if (nonNull(dbo)) {
            if (dbo.getFromId() != 0) {
                ids.append(dbo.getFromId());
            }
            if (dbo.getReplyToUserId() != 0) {
                ids.append(dbo.getReplyToUserId());
            }

            if (nonNull(dbo.getAttachments())) {
                fillOwnerIds(ids, dbo.getAttachments());
            }
            if (!Utils.isEmpty(dbo.getThreads())) {
                for (CommentEntity i : dbo.getThreads()) {
                    fillCommentOwnerIds(ids, i);
                }
            }
        }
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, @Nullable NewsEntity dbo) {
        if (nonNull(dbo)) {
            ids.append(dbo.getSourceId());

            fillOwnerIds(ids, dbo.getAttachments());
            fillOwnerIds(ids, dbo.getCopyHistory());

            if (!Utils.isEmpty(dbo.getFriendsTags())) {
                ids.appendAll(dbo.getFriendsTags());
            }
        }
    }

    public static void fillMessageOwnerIds(@NonNull VKOwnIds ids, @Nullable MessageEntity dbo) {
        if (isNull(dbo)) {
            return;
        }

        ids.append(dbo.getFromId());
        ids.append(dbo.getActionMemberId()); // тут 100% пользователь, нюанс в том, что он может быть < 0, если email

        if (!Peer.isGroupChat(dbo.getPeerId())) {
            ids.append(dbo.getPeerId());
        }

        if (nonEmpty(dbo.getForwardMessages())) {
            for (MessageEntity fwd : dbo.getForwardMessages()) {
                fillMessageOwnerIds(ids, fwd);
            }
        }

        if (nonEmpty(dbo.getAttachments())) {
            for (Entity attachmentEntity : dbo.getAttachments()) {
                fillOwnerIds(ids, attachmentEntity);
            }
        }
    }
}
