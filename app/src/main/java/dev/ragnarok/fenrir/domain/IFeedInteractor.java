package dev.ragnarok.fenrir.domain;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria;
import dev.ragnarok.fenrir.model.FeedList;
import dev.ragnarok.fenrir.model.News;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public interface IFeedInteractor {
    Single<List<News>> getCachedFeed(int accountId);

    Single<Pair<List<News>, String>> getActualFeed(int accountId, int count, String nextFrom, String filters, Integer maxPhotos, String sourceIds);

    Single<Pair<List<Post>, String>> search(int accountId, NewsFeedCriteria criteria, int count, String startFrom);

    Single<List<FeedList>> getCachedFeedLists(int accountId);

    Single<List<FeedList>> getActualFeedLists(int accountId);

    Single<Integer> saveList(int accountId, String title, Collection<Integer> listIds);

    Single<Integer> addBan(int accountId, Collection<Integer> listIds);

    Single<Integer> deleteList(int accountId, Integer list_id);

    Single<Integer> ignoreItem(int accountId, String type, Integer owner_id, Integer item_id);
}