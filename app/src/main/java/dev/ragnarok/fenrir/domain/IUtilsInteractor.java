package dev.ragnarok.fenrir.domain;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.api.model.VKApiCheckedLink;
import dev.ragnarok.fenrir.api.model.response.VkApiChatResponse;
import dev.ragnarok.fenrir.api.model.response.VkApiLinkResponse;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Privacy;
import dev.ragnarok.fenrir.model.ShortLink;
import dev.ragnarok.fenrir.model.SimplePrivacy;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Single;

public interface IUtilsInteractor {
    Single<Map<Integer, Privacy>> createFullPrivacies(int accountId, @NonNull Map<Integer, SimplePrivacy> orig);

    Single<Optional<Owner>> resolveDomain(int accountId, String domain);

    Single<ShortLink> getShortLink(int accountId, String url, Integer t_private);

    Single<List<ShortLink>> getLastShortenedLinks(int accountId, Integer count, Integer offset);

    Single<Integer> deleteFromLastShortened(int accountId, String key);

    Single<VKApiCheckedLink> checkLink(int accountId, String url);

    Single<VkApiChatResponse> joinChatByInviteLink(int accountId, String link);

    Single<VkApiLinkResponse> getInviteLink(int accountId, Integer peer_id, Integer reset);

    Single<Integer> customScript(int accountId, String code);
}
