package dev.ragnarok.fenrir.api.services;

import java.util.List;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.server.VkApiDocsUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiVideosUploadServer;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface IDocsService {

    //https://vk.com/dev/docs.delete
    @FormUrlEncoded
    @POST("docs.delete")
    Single<BaseResponse<Integer>> delete(@Field("owner_id") Integer ownerId,
                                         @Field("doc_id") int docId);

    /**
     * Copies a document to a user's or community's document list.
     *
     * @param ownerId   ID of the user or community that owns the document. Use a negative value to designate a community ID.
     * @param docId     Document ID.
     * @param accessKey Access key. This parameter is required if access_key was returned with the document's data.
     * @return the ID of the created document.
     */
    @FormUrlEncoded
    @POST("docs.add")
    Single<BaseResponse<Integer>> add(@Field("owner_id") int ownerId,
                                      @Field("doc_id") int docId,
                                      @Field("access_key") String accessKey);

    /**
     * Returns information about documents by their IDs.
     *
     * @param ids Document IDs. Example: 66748_91488,66748_91455.
     *            List of comma-separated words, required parameter
     * @return an array of objects describing documents
     */
    @FormUrlEncoded
    @POST("docs.getById")
    Single<BaseResponse<List<VkApiDoc>>> getById(@Field("docs") String ids);

    /**
     * Returns a list of documents matching the search criteria.
     *
     * @param query  Search query string.
     * @param count  Number of results to return.
     * @param offset Offset needed to return a specific subset of results.
     * @return Returns the total results number in count field and an array of objects describing documents in items field
     */
    @FormUrlEncoded
    @POST("docs.search")
    Single<BaseResponse<Items<VkApiDoc>>> search(@Field("q") String query,
                                                 @Field("count") Integer count,
                                                 @Field("offset") Integer offset);

    /**
     * Saves a document after uploading it to a server.
     *
     * @param file  This parameter is returned when the file is uploaded to the server
     * @param title Document title
     * @param tags  Document tags.
     * @return Returns an array of uploaded document objects.
     */
    @FormUrlEncoded
    @POST("docs.save")
    Single<BaseResponse<VkApiDoc.Entry>> save(@Field("file") String file,
                                              @Field("title") String title,
                                              @Field("tags") String tags);

    /**
     * Returns the server address for document upload.
     *
     * @param peer_id Peer ID (if the document will be uploaded to the chat).
     * @param type    type of document, null or "audio_message" (undocumented option)
     * @return an object with an upload_url field. After the document is uploaded, use the {@link #save} method.
     */
    @FormUrlEncoded
    @POST("docs.getMessagesUploadServer")
    Single<BaseResponse<VkApiDocsUploadServer>> getMessagesUploadServer(@Field("peer_id") Integer peer_id,
                                                                        @Field("type") String type);

    @FormUrlEncoded
    @POST("docs.getUploadServer")
    Single<BaseResponse<VkApiDocsUploadServer>> getUploadServer(@Field("group_id") Integer groupId);

    @FormUrlEncoded
    @POST("video.save")
    Single<BaseResponse<VkApiVideosUploadServer>> getVideoServer(@Field("is_private") Integer is_private,
                                                                 @Field("group_id") Integer group_id,
                                                                 @Field("name") String name);

    /**
     * Returns detailed information about user or community documents.
     *
     * @param ownerId ID of the user or community that owns the documents.
     *                Use a negative value to designate a community ID.
     *                Current user id is used by default
     * @param count   Number of documents to return. By default, all documents.
     * @param offset  Offset needed to return a specific subset of documents.
     * @param type    Document type. Possible values:
     *                1 — text documents;
     *                2 — archives;
     *                3 — gif;
     *                4 — images;
     *                5 — audio;
     *                6 — video;
     *                7 — e-books;
     *                8 — unknown.
     * @return Returns the total results number in count field and an array of objects describing documents in items field
     */
    @FormUrlEncoded
    @POST("docs.get")
    Single<BaseResponse<Items<VkApiDoc>>> get(@Field("owner_id") Integer ownerId,
                                              @Field("count") Integer count,
                                              @Field("offset") Integer offset,
                                              @Field("type") Integer type);

}
