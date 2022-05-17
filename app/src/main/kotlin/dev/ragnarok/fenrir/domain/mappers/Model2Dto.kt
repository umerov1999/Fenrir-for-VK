package dev.ragnarok.fenrir.domain.mappers

import dev.ragnarok.fenrir.api.model.AttachmentsTokenCreator
import dev.ragnarok.fenrir.api.model.IAttachmentToken
import dev.ragnarok.fenrir.model.*

object Model2Dto {
    /*public static List<IAttachmentToken> createTokens(Attachments attachments){
        List<IAttachmentToken> tokens = new ArrayList<>(attachments != null ? attachments.size() : 0);

        if(attachments != null){
            tokens.addAll(createTokens(attachments.getAudios()));
            tokens.addAll(createTokens(attachments.getPhotos()));
            tokens.addAll(createTokens(attachments.getDocs()));
            tokens.addAll(createTokens(attachments.getVideos()));
            tokens.addAll(createTokens(attachments.getPosts()));
            tokens.addAll(createTokens(attachments.getLinks()));
            tokens.addAll(createTokens(attachments.getPolls()));
            tokens.addAll(createTokens(attachments.getPages()));
            tokens.addAll(createTokens(attachments.getVoiceMessages()));
        }

        return tokens;
    }*/

    fun createTokens(models: Collection<AbsModel>?): List<IAttachmentToken> {
        return MapUtil.mapAll(models) { createToken(it) }
    }

    private fun createToken(model: AbsModel): IAttachmentToken {
        if (model is Document) {
            return AttachmentsTokenCreator.ofDocument(
                model.id,
                model.ownerId,
                model.accessKey
            )
        }
        if (model is Audio) {
            return AttachmentsTokenCreator.ofAudio(model.id, model.ownerId, model.accessKey)
        }
        if (model is Link) {
            return AttachmentsTokenCreator.ofLink(model.url)
        }
        if (model is Photo) {
            return AttachmentsTokenCreator.ofPhoto(
                model.getObjectId(),
                model.ownerId,
                model.accessKey
            )
        }
        if (model is Poll) {
            return AttachmentsTokenCreator.ofPoll(model.id, model.ownerId)
        }
        if (model is Post) {
            return AttachmentsTokenCreator.ofPost(model.vkid, model.ownerId)
        }
        if (model is Video) {
            return AttachmentsTokenCreator.ofVideo(model.id, model.ownerId, model.accessKey)
        }
        if (model is Article) {
            return AttachmentsTokenCreator.ofArticle(model.id, model.ownerId, model.accessKey)
        }
        if (model is Story) {
            return AttachmentsTokenCreator.ofStory(model.id, model.ownerId, model.accessKey)
        }
        if (model is Graffiti) {
            return AttachmentsTokenCreator.ofGraffity(
                model.id,
                model.owner_id,
                model.access_key
            )
        }
        if (model is PhotoAlbum) {
            return AttachmentsTokenCreator.ofPhotoAlbum(model.getObjectId(), model.ownerId)
        }
        if (model is Call) {
            return AttachmentsTokenCreator.ofCall(
                model.initiator_id,
                model.receiver_id,
                model.state,
                model.time
            )
        }
        if (model is AudioArtist) {
            return AttachmentsTokenCreator.ofArtist(model.getId())
        }
        if (model is WallReply) {
            return AttachmentsTokenCreator.ofWallReply(model.getObjectId(), model.ownerId)
        }
        if (model is NotSupported) {
            return AttachmentsTokenCreator.ofError(model.getType(), model.getBody())
        }
        if (model is Event) {
            return AttachmentsTokenCreator.ofEvent(model.id, model.button_text)
        }
        if (model is Market) {
            return AttachmentsTokenCreator.ofMarket(model.id, model.owner_id, model.access_key)
        }
        if (model is MarketAlbum) {
            return AttachmentsTokenCreator.ofMarketAlbum(
                model.getId(),
                model.getOwner_id(),
                model.getAccess_key()
            )
        }
        if (model is AudioPlaylist) {
            return if (model.getOriginal_access_key()
                    .isNullOrEmpty() || model.getOriginal_id() == 0 || model.getOriginal_owner_id() == 0
            ) {
                AttachmentsTokenCreator.ofAudioPlaylist(
                    model.getId(),
                    model.getOwnerId(),
                    model.getAccess_key()
                )
            } else {
                AttachmentsTokenCreator.ofAudioPlaylist(
                    model.getOriginal_id(),
                    model.getOriginal_owner_id(),
                    model.getOriginal_access_key()
                )
            }
        }
        throw UnsupportedOperationException("Token for class " + model.javaClass + " not supported")
    }
}