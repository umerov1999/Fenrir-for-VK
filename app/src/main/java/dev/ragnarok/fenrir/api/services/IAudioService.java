package dev.ragnarok.fenrir.api.services;

import java.util.List;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiAudioCatalog;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.VkApiArtist;
import dev.ragnarok.fenrir.api.model.VkApiLyrics;
import dev.ragnarok.fenrir.api.model.response.AddToPlaylistResponse;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.BlockResponse;
import dev.ragnarok.fenrir.api.model.response.CatalogResponse;
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse;
import dev.ragnarok.fenrir.api.model.server.VkApiAudioUploadServer;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface IAudioService {

    @FormUrlEncoded
    @POST("audio.setBroadcast")
    Single<BaseResponse<int[]>> setBroadcast(@Field("audio") String audio,
                                             @Field("target_ids") String targetIds);

    //https://vk.com/dev/audio.search
    @FormUrlEncoded
    @POST("audio.search")
    Single<BaseResponse<Items<VKApiAudio>>> search(@Field("q") String query,
                                                   @Field("auto_complete") Integer autoComplete,
                                                   @Field("lyrics") Integer lyrics,
                                                   @Field("performer_only") Integer performerOnly,
                                                   @Field("sort") Integer sort,
                                                   @Field("search_own") Integer searchOwn,
                                                   @Field("offset") Integer offset,
                                                   @Field("count") Integer count);

    //https://vk.com/dev/audio.searchArtists
    @FormUrlEncoded
    @POST("audio.searchArtists")
    Single<BaseResponse<Items<VkApiArtist>>> searchArtists(@Field("q") String query,
                                                           @Field("offset") Integer offset,
                                                           @Field("count") Integer count);

    //https://vk.com/dev/audio.searchPlaylists
    @FormUrlEncoded
    @POST("audio.searchPlaylists")
    Single<BaseResponse<Items<VKApiAudioPlaylist>>> searchPlaylists(@Field("q") String query,
                                                                    @Field("offset") Integer offset,
                                                                    @Field("count") Integer count);

    //https://vk.com/dev/audio.restore
    @FormUrlEncoded
    @POST("audio.restore")
    Single<BaseResponse<VKApiAudio>> restore(@Field("audio_id") int audioId,
                                             @Field("owner_id") Integer ownerId);

    //https://vk.com/dev/audio.delete
    @FormUrlEncoded
    @POST("audio.delete")
    Single<BaseResponse<Integer>> delete(@Field("audio_id") int audioId,
                                         @Field("owner_id") int ownerId);

    //https://vk.com/dev/audio.add
    @FormUrlEncoded
    @POST("audio.add")
    Single<BaseResponse<Integer>> add(@Field("audio_id") int audioId,
                                      @Field("owner_id") int ownerId,
                                      @Field("group_id") Integer groupId,
                                      @Field("access_key") String accessKey);

    /**
     * Returns a list of audio files of a user or community.
     *
     * @param ownerId ID of the user or community that owns the audio file.
     *                Use a negative value to designate a community ID.
     *                Current user id is used by default
     * @param offset  Offset needed to return a specific subset of audio files.
     * @param count   Number of audio files to return.
     * @return Returns the total results number in count field and an array of objects describing audio in items field.
     */
    //https://vk.com/dev/audio.get
    @FormUrlEncoded
    @POST("audio.get")
    Single<BaseResponse<Items<VKApiAudio>>> get(@Field("playlist_id") Integer playlist_id,
                                                @Field("owner_id") Integer ownerId,
                                                @Field("offset") Integer offset,
                                                @Field("count") Integer count,
                                                @Field("access_key") String accessKey);

    //https://vk.com/dev/audio.getAudiosByArtist
    @FormUrlEncoded
    @POST("audio.getAudiosByArtist")
    Single<BaseResponse<Items<VKApiAudio>>> getAudiosByArtist(@Field("artist_id") String artist_id,
                                                              @Field("offset") Integer offset,
                                                              @Field("count") Integer count);

    @FormUrlEncoded
    @POST("audio.getPopular")
    Single<BaseResponse<List<VKApiAudio>>> getPopular(@Field("only_eng") Integer foreign,
                                                      @Field("genre_id") Integer genre,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("audio.getRecommendations")
    Single<BaseResponse<Items<VKApiAudio>>> getRecommendations(@Field("user_id") Integer user_id,
                                                               @Field("count") Integer count);

    @FormUrlEncoded
    @POST("audio.getRecommendations")
    Single<BaseResponse<Items<VKApiAudio>>> getRecommendationsByAudio(@Field("target_audio") String audio,
                                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("audio.getById")
    Single<BaseResponse<List<VKApiAudio>>> getById(@Field("audios") String audios);

    @FormUrlEncoded
    @POST("audio.getById")
    Single<BaseResponse<List<VKApiAudio>>> getByIdVersioned(@Field("audios") String audios, @Field("v") String version);

    @FormUrlEncoded
    @POST("audio.getLyrics")
    Single<BaseResponse<VkApiLyrics>> getLyrics(@Field("lyrics_id") int lyrics_id);

    @FormUrlEncoded
    @POST("audio.getPlaylists")
    Single<BaseResponse<Items<VKApiAudioPlaylist>>> getPlaylists(@Field("owner_id") int owner_id,
                                                                 @Field("offset") int offset,
                                                                 @Field("count") int count);

    @FormUrlEncoded
    @POST("execute")
    Single<ServicePlaylistResponse> getPlaylistsCustom(@Field("code") String code);

    @FormUrlEncoded
    @POST("audio.deletePlaylist")
    Single<BaseResponse<Integer>> deletePlaylist(@Field("playlist_id") int playlist_id,
                                                 @Field("owner_id") int ownerId);

    @FormUrlEncoded
    @POST("audio.followPlaylist")
    Single<BaseResponse<VKApiAudioPlaylist>> followPlaylist(@Field("playlist_id") int playlist_id,
                                                            @Field("owner_id") int ownerId,
                                                            @Field("access_key") String accessKey);

    @FormUrlEncoded
    @POST("audio.savePlaylistAsCopy")
    Single<BaseResponse<VKApiAudioPlaylist>> clonePlaylist(@Field("playlist_id") int playlist_id,
                                                           @Field("owner_id") int ownerId);

    @FormUrlEncoded
    @POST("audio.getPlaylistById")
    Single<BaseResponse<VKApiAudioPlaylist>> getPlaylistById(@Field("playlist_id") int playlist_id,
                                                             @Field("owner_id") int ownerId,
                                                             @Field("access_key") String accessKey);

    @FormUrlEncoded
    @POST("audio.getCatalog")
    Single<BaseResponse<Items<VKApiAudioCatalog>>> getCatalog(@Field("artist_id") String artist_id,
                                                              @Field("query") String query);

    @FormUrlEncoded
    @POST("audio.getCatalogBlockById")
    Single<BaseResponse<BlockResponse<CatalogResponse>>> getCatalogBlockById(@Field("block_id") String block_id,
                                                                             @Field("start_from") String start_from);

    @POST("audio.getUploadServer")
    Single<BaseResponse<VkApiAudioUploadServer>> getUploadServer();

    @FormUrlEncoded
    @POST("audio.save")
    Single<BaseResponse<VKApiAudio>> save(@Field("server") String server,
                                          @Field("audio") String audio,
                                          @Field("hash") String hash,
                                          @Field("artist") String artist,
                                          @Field("title") String title);

    @FormUrlEncoded
    @POST("audio.edit")
    Single<BaseResponse<Integer>> edit(@Field("owner_id") int ownerId,
                                       @Field("audio_id") int audioId,
                                       @Field("artist") String artist,
                                       @Field("title") String title,
                                       @Field("text") String text);

    @FormUrlEncoded
    @POST("audio.createPlaylist")
    Single<BaseResponse<VKApiAudioPlaylist>> createPlaylist(@Field("owner_id") int ownerId,
                                                            @Field("title") String title,
                                                            @Field("description") String description);

    @FormUrlEncoded
    @POST("audio.editPlaylist")
    Single<BaseResponse<Integer>> editPlaylist(@Field("owner_id") int ownerId,
                                               @Field("playlist_id") int playlist_id,
                                               @Field("title") String title,
                                               @Field("description") String description);

    @FormUrlEncoded
    @POST("audio.removeFromPlaylist")
    Single<BaseResponse<Integer>> removeFromPlaylist(@Field("owner_id") int ownerId,
                                                     @Field("playlist_id") int playlist_id,
                                                     @Field("audio_ids") String audio_ids);

    @FormUrlEncoded
    @POST("audio.addToPlaylist")
    Single<BaseResponse<List<AddToPlaylistResponse>>> addToPlaylist(@Field("owner_id") int ownerId,
                                                                    @Field("playlist_id") int playlist_id,
                                                                    @Field("audio_ids") String audio_ids);

    @FormUrlEncoded
    @POST("audio.reorder")
    Single<BaseResponse<Integer>> reorder(@Field("owner_id") int ownerId,
                                          @Field("audio_id") int audio_id,
                                          @Field("before") Integer before,
                                          @Field("after") Integer after);

    @FormUrlEncoded
    @POST("stats.trackEvents")
    Single<BaseResponse<Integer>> trackEvents(@Field("events") String events);
}
