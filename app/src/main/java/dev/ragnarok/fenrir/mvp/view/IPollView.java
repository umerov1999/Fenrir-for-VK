package dev.ragnarok.fenrir.mvp.view;

import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IPollView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayQuestion(String title);

    void displayPhoto(String photo_url);

    void displayType(boolean anonymous);

    void displayCreationTime(long unixtime);

    void displayVoteCount(int count);

    void displayVotesList(List<Poll.Answer> answers, boolean canCheck, boolean multiply, Set<Integer> checked);

    void displayLoading(boolean loading);

    void setupButton(boolean voted);
}