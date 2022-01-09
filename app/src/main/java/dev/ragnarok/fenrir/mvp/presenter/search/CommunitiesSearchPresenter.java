package dev.ragnarok.fenrir.mvp.presenter.search;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.domain.ICommunitiesInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.criteria.GroupSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.mvp.view.search.ICommunitiesSearchView;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public class CommunitiesSearchPresenter extends AbsSearchPresenter<ICommunitiesSearchView,
        GroupSearchCriteria, Community, IntNextFrom> {

    private final ICommunitiesInteractor communitiesInteractor;

    public CommunitiesSearchPresenter(int accountId, @Nullable GroupSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        communitiesInteractor = InteractorFactory.createCommunitiesInteractor();
    }

    private static String extractTypeFromCriteria(GroupSearchCriteria criteria) {
        SpinnerOption option = criteria.findOptionByKey(GroupSearchCriteria.KEY_TYPE);
        if (option != null && option.value != null) {
            switch (option.value.id) {
                case GroupSearchCriteria.TYPE_PAGE:
                    return "page";
                case GroupSearchCriteria.TYPE_GROUP:
                    return "group";
                case GroupSearchCriteria.TYPE_EVENT:
                    return "event";
            }
        }

        return null;
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Community>, IntNextFrom>> doSearch(int accountId, GroupSearchCriteria criteria, IntNextFrom startFrom) {
        String type = extractTypeFromCriteria(criteria);

        Integer countryId = criteria.extractDatabaseEntryValueId(GroupSearchCriteria.KEY_COUNTRY);
        Integer cityId = criteria.extractDatabaseEntryValueId(GroupSearchCriteria.KEY_CITY);
        Boolean future = criteria.extractBoleanValueFromOption(GroupSearchCriteria.KEY_FUTURE_ONLY);

        SpinnerOption sortOption = criteria.findOptionByKey(GroupSearchCriteria.KEY_SORT);
        Integer sort = (sortOption == null || sortOption.value == null) ? null : sortOption.value.id;

        int offset = startFrom.getOffset();
        IntNextFrom nextFrom = new IntNextFrom(offset + 50);

        return communitiesInteractor.search(accountId, criteria.getQuery(), type, countryId, cityId, future, sort, 50, offset)
                .map(communities -> Pair.Companion.create(communities, nextFrom));
    }

    @Override
    GroupSearchCriteria instantiateEmptyCriteria() {
        return new GroupSearchCriteria("");
    }

    @Override
    boolean canSearch(GroupSearchCriteria criteria) {
        return nonEmpty(criteria.getQuery());
    }

    public void fireCommunityClick(Community community) {
        callView(v -> v.openCommunityWall(getAccountId(), community));
    }
}