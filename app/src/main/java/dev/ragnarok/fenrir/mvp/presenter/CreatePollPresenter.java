package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IPollInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ICreatePollView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.util.RxUtils;


public class CreatePollPresenter extends AccountDependencyPresenter<ICreatePollView> {

    private final IPollInteractor pollInteractor;
    private final int mOwnerId;
    private String mQuestion;
    private String[] mOptions;
    private boolean mAnonymous;
    private boolean mMultiply;
    private boolean creationNow;

    public CreatePollPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        mOwnerId = ownerId;
        pollInteractor = InteractorFactory.createPollInteractor();

        if (isNull(savedInstanceState)) {
            mOptions = new String[10];
        }
    }

    @Override
    public void onGuiCreated(@NonNull ICreatePollView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayQuestion(mQuestion);
        viewHost.setAnonymous(mAnonymous);
        viewHost.setMultiply(mMultiply);
        viewHost.displayOptions(mOptions);

        resolveProgressDialog();
    }

    private void setCreationNow(boolean creationNow) {
        this.creationNow = creationNow;
        resolveProgressDialog();
    }

    private void create() {
        if (safeIsEmpty(mQuestion)) {
            callView(v -> v.showQuestionError(R.string.field_is_required));
            return;
        }

        List<String> nonEmptyOptions = new ArrayList<>();
        for (String o : mOptions) {
            if (!safeIsEmpty(o)) {
                nonEmptyOptions.add("\"" + o + "\"");
            }
        }

        if (nonEmptyOptions.isEmpty()) {
            callView(v -> v.showOptionError(0, R.string.field_is_required));
            return;
        }

        setCreationNow(true);
        int accountId = getAccountId();

        appendDisposable(pollInteractor.createPoll(accountId, mQuestion, mAnonymous, mMultiply, mOwnerId, nonEmptyOptions)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollCreated, this::onPollCreateError));
    }

    private void onPollCreateError(Throwable t) {
        setCreationNow(false);
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onPollCreated(Poll poll) {
        setCreationNow(false);
        callView(view -> view.sendResultAndGoBack(poll));
    }

    private void resolveProgressDialog() {
        if (creationNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.publication, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    public void fireQuestionEdited(CharSequence text) {
        mQuestion = isNull(text) ? null : text.toString();
    }

    public void fireOptionEdited(int index, CharSequence s) {
        mOptions[index] = isNull(s) ? null : s.toString();
    }

    public void fireAnonyamousChecked(boolean b) {
        mAnonymous = b;
    }

    public void fireDoneClick() {
        create();
    }

    public void fireMultiplyChecked(boolean isChecked) {
        mMultiply = isChecked;
    }
}