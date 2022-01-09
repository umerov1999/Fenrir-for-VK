package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.NewsfeedComment;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public interface INewsfeedInteractor {

    Single<Pair<List<NewsfeedComment>, String>> getNewsfeedComments(int accountId, int count, String startFrom, String filter);

    Single<Pair<List<NewsfeedComment>, String>> getMentions(int accountId, Integer owner_id, Integer count, Integer offset, Long startTime, Long endTime);

}
