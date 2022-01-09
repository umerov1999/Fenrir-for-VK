package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCheckedLink;
import dev.ragnarok.fenrir.api.model.VKApiShortLink;
import dev.ragnarok.fenrir.api.model.response.ResolveDomailResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiChatResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiLinkResponse;
import io.reactivex.rxjava3.core.Single;

public interface IUtilsApi {

    @CheckResult
    Single<ResolveDomailResponse> resolveScreenName(String screenName);

    @CheckResult
    Single<VKApiShortLink> getShortLink(String url, Integer t_private);

    @CheckResult
    Single<Items<VKApiShortLink>> getLastShortenedLinks(Integer count, Integer offset);

    @CheckResult
    Single<Integer> deleteFromLastShortened(String key);

    @CheckResult
    Single<VKApiCheckedLink> checkLink(String url);

    @CheckResult
    Single<VkApiChatResponse> joinChatByInviteLink(String link);

    @CheckResult
    Single<VkApiLinkResponse> getInviteLink(Integer peer_id, Integer reset);

    @CheckResult
    Single<Integer> customScript(String code);
}
