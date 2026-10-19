package dev.ragnarok.fenrir.api.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.api.model.AccessIdPair
import dev.ragnarok.fenrir.api.model.ArtistInfo
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiArtist
import dev.ragnarok.fenrir.api.model.VKApiAudio
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist
import dev.ragnarok.fenrir.api.model.VKApiLyrics
import dev.ragnarok.fenrir.api.model.catalog_v2_audio.VKApiCatalogV2BlockResponse
import dev.ragnarok.fenrir.api.model.catalog_v2_audio.VKApiCatalogV2ListResponse
import dev.ragnarok.fenrir.api.model.catalog_v2_audio.VKApiCatalogV2SectionResponse
import dev.ragnarok.fenrir.api.model.response.AddToPlaylistResponse
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse
import dev.ragnarok.fenrir.api.model.server.VKApiAudioUploadServer
import dev.ragnarok.fenrir.model.Audio
import kotlinx.coroutines.flow.Flow

interface IAudioApi {
    @CheckResult
    fun setBroadcast(audio: AccessIdPair, targetIds: Collection<Long>): Flow<List<Int>>

    @CheckResult
    fun search(
        query: String?, autoComplete: Boolean?, lyrics: Boolean?,
        performerOnly: Boolean?, sort: Int?, searchOwn: Boolean?,
        offset: Int?, count: Int?
    ): Flow<Items<VKApiAudio>>

    @CheckResult
    fun searchArtists(query: String?, offset: Int?, count: Int?): Flow<Items<VKApiArtist>>

    @CheckResult
    fun searchPlaylists(
        query: String?,
        offset: Int?,
        count: Int?
    ): Flow<Items<VKApiAudioPlaylist>>

    @CheckResult
    fun restore(audioId: Int, ownerId: Long?): Flow<VKApiAudio>

    @CheckResult
    fun delete(audioId: Int, ownerId: Long): Flow<Boolean>

    @CheckResult
    fun edit(
        ownerId: Long,
        audioId: Int,
        artist: String?,
        title: String?
    ): Flow<Int>

    @CheckResult
    fun add(audioId: Int, ownerId: Long, groupId: Long?, accessKey: String?): Flow<Int>

    @CheckResult
    fun createPlaylist(
        ownerId: Long,
        title: String?,
        description: String?
    ): Flow<VKApiAudioPlaylist>

    @CheckResult
    fun editPlaylist(
        ownerId: Long,
        playlistId: Int,
        title: String?,
        description: String?
    ): Flow<Int>

    @CheckResult
    fun removeFromPlaylist(
        ownerId: Long,
        playlistId: Int,
        audioIds: Collection<AccessIdPair>
    ): Flow<Int>

    @CheckResult
    fun addToPlaylist(
        ownerId: Long,
        playlistId: Int,
        audioIds: Collection<AccessIdPair>
    ): Flow<List<AddToPlaylistResponse>>

    @CheckResult
    fun reorder(ownerId: Long, audioId: Int, before: Int?, after: Int?): Flow<Int>

    @CheckResult
    fun trackEvents(events: String?): Flow<Int>

    @CheckResult
    fun get(
        playlistId: Int?, ownerId: Long?,
        offset: Int?, count: Int?, accessKey: String?
    ): Flow<Items<VKApiAudio>>

    @CheckResult
    fun getAudiosByArtist(
        artistId: String?,
        offset: Int?,
        count: Int?
    ): Flow<Items<VKApiAudio>>

    @CheckResult
    fun getPopular(
        foreign: Int?,
        genre: Int?, count: Int?
    ): Flow<List<VKApiAudio>>

    @CheckResult
    fun deletePlaylist(playlistId: Int, ownerId: Long): Flow<Int>

    @CheckResult
    fun followPlaylist(
        playlistId: Int,
        ownerId: Long,
        accessKey: String?
    ): Flow<VKApiAudioPlaylist>

    @CheckResult
    fun clonePlaylist(playlistId: Int, ownerId: Long): Flow<VKApiAudioPlaylist>

    @CheckResult
    fun getPlaylistById(
        playlistId: Int,
        ownerId: Long,
        accessKey: String?
    ): Flow<VKApiAudioPlaylist>

    @CheckResult
    fun getRecommendations(audioOwnerId: Long?, count: Int?): Flow<Items<VKApiAudio>>

    @CheckResult
    fun getRecommendationsByAudio(audio: String?, count: Int?): Flow<Items<VKApiAudio>>

    @CheckResult
    fun getById(audios: List<Audio>): Flow<List<VKApiAudio>>

    @CheckResult
    fun getByIdOld(audios: List<Audio>): Flow<List<VKApiAudio>>

    @CheckResult
    fun getLyrics(audio: Audio): Flow<VKApiLyrics>

    @CheckResult
    fun getPlaylists(ownerId: Long, offset: Int, count: Int): Flow<Items<VKApiAudioPlaylist>>

    @CheckResult
    fun getPlaylistsCustom(code: String?): Flow<ServicePlaylistResponse>

    @get:CheckResult
    val uploadServer: Flow<VKApiAudioUploadServer>

    @CheckResult
    fun save(
        server: String?,
        audio: String?,
        hash: String?,
        artist: String?,
        title: String?
    ): Flow<VKApiAudio>

    @CheckResult
    fun getCatalogV2Sections(
        ownerId: Long, artistId: String?, url: String?, query: String?, context: String?
    ): Flow<VKApiCatalogV2ListResponse>

    @CheckResult
    fun getCatalogV2Section(
        sectionId: String,
        startFrom: String?
    ): Flow<VKApiCatalogV2SectionResponse>

    @CheckResult
    fun getCatalogV2BlockItems(
        blockId: String, startFrom: String?
    ): Flow<VKApiCatalogV2BlockResponse>

    @CheckResult
    fun getArtistById(
        artistId: String
    ): Flow<ArtistInfo>
}