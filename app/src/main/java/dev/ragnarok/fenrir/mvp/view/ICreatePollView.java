package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICreatePollView extends IAccountDependencyView, IMvpView, IProgressView, IErrorView {
    void displayQuestion(String question);

    void setAnonymous(boolean anomymous);

    void displayOptions(String[] options);

    void showQuestionError(@StringRes int message);

    void showOptionError(int index, @StringRes int message);

    void sendResultAndGoBack(@NonNull Poll poll);

    void setMultiply(boolean multiply);
}
