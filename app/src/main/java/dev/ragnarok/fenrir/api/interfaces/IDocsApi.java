package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.api.model.server.VkApiDocsUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiVideosUploadServer;
import io.reactivex.rxjava3.core.Single;


public interface IDocsApi {

    @CheckResult
    Single<Boolean> delete(Integer ownerId, int docId);

    @CheckResult
    Single<Integer> add(int ownerId, int docId, String accessKey);

    @CheckResult
    Single<List<VkApiDoc>> getById(Collection<AccessIdPair> pairs);

    @CheckResult
    Single<Items<VkApiDoc>> search(String query, Integer count, Integer offset);

    @CheckResult
    Single<VkApiDoc.Entry> save(String file, String title, String tags);

    @CheckResult
    Single<VkApiDocsUploadServer> getUploadServer(Integer groupId);

    @CheckResult
    Single<VkApiDocsUploadServer> getMessagesUploadServer(Integer peerId, String type);

    @CheckResult
    Single<VkApiVideosUploadServer> getVideoServer(Integer isPrivate, Integer group_id, String name);

    @CheckResult
    Single<Items<VkApiDoc>> get(Integer ownerId, Integer count, Integer offset, Integer type);
}
