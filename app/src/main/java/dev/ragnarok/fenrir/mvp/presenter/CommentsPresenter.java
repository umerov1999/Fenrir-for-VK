package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.dummy;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.trimmedNonEmpty;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.domain.ICommentsInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IStickersInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.impl.CommentsInteractor;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.CommentIntent;
import dev.ragnarok.fenrir.model.CommentUpdate;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.model.CommentsBundle;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.DraftComment;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommentsView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Analytics;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.DisposableHolder;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CommentsPresenter extends PlaceSupportPresenter<ICommentsView> {
    private static final int COUNT = 20;
    private static final String REPLY_PATTERN = "[post%s|%s], ";
    private final Commented commented;
    private final IOwnersRepository ownersRepository;
    private final ICommentsInteractor interactor;
    private final IStickersInteractor stickersInteractor;
    private final List<Comment> data;
    private final Integer CommentThread;
    private final Context context;
    private final DisposableHolder<Void> stickersWordsDisplayDisposable = new DisposableHolder<>();
    private final CompositeDisposable actualLoadingDisposable = new CompositeDisposable();
    private final DisposableHolder<Void> deepLookingHolder = new DisposableHolder<>();
    private final CompositeDisposable cacheLoadingDisposable = new CompositeDisposable();
    private Integer focusToComment;
    private CommentedState commentedState;
    private int authorId;
    private Owner author;
    private boolean directionDesc;
    private int loadingState;
    private int adminLevel;
    private String draftCommentBody;
    private int draftCommentAttachmentsCount;
    private Integer draftCommentId;
    private Comment replyTo;
    private boolean sendingNow;
    private Poll topicPoll;
    private boolean loadingAvailableAuthorsNow;

    public CommentsPresenter(int accountId, Commented commented, Integer focusToComment, Context context, Integer CommentThread, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        authorId = accountId;
        ownersRepository = Repository.INSTANCE.getOwners();
        interactor = new CommentsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Repository.INSTANCE.getOwners());
        stickersInteractor = InteractorFactory.createStickersInteractor();
        this.commented = commented;
        this.focusToComment = focusToComment;
        this.context = context;
        directionDesc = Settings.get().other().isCommentsDesc();
        this.CommentThread = CommentThread;
        data = new ArrayList<>();

        if (isNull(focusToComment) && isNull(CommentThread)) {
            // если надо сфокусироваться на каком-то комментарии - не грузим из кэша
            loadCachedData();
        }

        IAttachmentsRepository attachmentsRepository = Injection.provideAttachmentsRepository();

        appendDisposable(attachmentsRepository
                .observeAdding()
                .filter(this::filterAttachmentEvent)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAttchmentAddEvent));

        appendDisposable(attachmentsRepository
                .observeRemoving()
                .filter(this::filterAttachmentEvent)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAttchmentRemoveEvent));

        appendDisposable(Injection.provideStores()
                .comments()
                .observeMinorUpdates()
                .filter(update -> update.getCommented().equals(commented))
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onCommentMinorUpdate, Analytics::logUnexpectedError));

        restoreDraftCommentSync();
        requestInitialData();
        loadAuthorData();
    }

    private static String buildReplyTextFor(Comment comment) {
        String name = comment.getFromId() > 0 ? ((User) comment.getAuthor()).getFirstName() : ((Community) comment.getAuthor()).getName();
        return String.format(REPLY_PATTERN, comment.getId(), name);
    }

    private void loadAuthorData() {
        int accountId = getAccountId();

        appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, authorId, IOwnersRepository.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAuthorDataReceived, this::onAuthorDataGetError));
    }

    private void resolveAuthorAvatarView() {
        String avatarUrl = nonNull(author) ? (author instanceof User ? ((User) author).getPhoto50() : ((Community) author).getPhoto50()) : null;
        callView(v -> v.displayAuthorAvatar(avatarUrl));
    }

    private void onAuthorDataGetError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onAuthorDataReceived(Owner owner) {
        author = owner;
        resolveAuthorAvatarView();
    }

    private void onCommentMinorUpdate(CommentUpdate update) {
        for (int i = 0; i < data.size(); i++) {
            Comment comment = data.get(i);
            if (comment.getId() == update.getCommentId()) {
                applyUpdate(comment, update);
                int finalI = i;
                callView(v -> v.notifyItemChanged(finalI));
                return;
            } else if (comment.hasThreads()) {
                for (int s = 0; s < comment.getThreads().size(); s++) {
                    Comment thread = comment.getThreads().get(s);
                    if (thread.getId() == update.getCommentId()) {
                        applyUpdate(thread, update);
                        int finalI = i;
                        callView(v -> v.notifyItemChanged(finalI));
                        return;
                    }
                }
            }
        }
    }

    private void applyUpdate(Comment comment, CommentUpdate update) {
        if (update.hasLikesUpdate()) {
            comment.setLikesCount(update.getLikeUpdate().getCount());
            comment.setUserLikes(update.getLikeUpdate().isUserLikes());
        }

        if (update.hasDeleteUpdate()) {
            comment.setDeleted(update.getDeleteUpdate().isDeleted());
        }
    }

    public void resetDraftMessage() {
        draftCommentAttachmentsCount = 0;
        draftCommentBody = null;
        draftCommentId = null;
        replyTo = null;

        resolveAttachmentsCounter();
        resolveBodyView();
        resolveReplyViews();
        resolveSendButtonAvailability();
        resolveEmptyTextVisibility();
    }

    public void fireTextEdited(String s) {
        if (!Settings.get().other().isHint_stickers()) {
            return;
        }
        stickersWordsDisplayDisposable.dispose();
        if (Utils.isEmpty(s)) {
            callView(view -> view.updateStickers(Collections.emptyList()));
            return;
        }
        stickersWordsDisplayDisposable.append(stickersInteractor.getKeywordsStickers(getAccountId(), s.trim())
                .delay(500, TimeUnit.MILLISECONDS)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(stickers -> callView(view -> view.updateStickers(stickers)), u -> callView(v -> showError(v, u))));
    }

    @SuppressWarnings("unused")
    private void onAttchmentRemoveEvent(IAttachmentsRepository.IRemoveEvent event) {
        draftCommentAttachmentsCount--;
        onAttachmentCountChanged();
    }

    private void onAttachmentCountChanged() {
        resolveSendButtonAvailability();
        resolveAttachmentsCounter();
    }

    private void onAttchmentAddEvent(IAttachmentsRepository.IAddEvent event) {
        draftCommentAttachmentsCount = draftCommentAttachmentsCount + event.getAttachments().size();
        onAttachmentCountChanged();
    }

    private boolean filterAttachmentEvent(IAttachmentsRepository.IBaseEvent event) {
        return nonNull(draftCommentId)
                && event.getAttachToType() == AttachToType.COMMENT
                && event.getAccountId() == getAccountId()
                && event.getAttachToId() == draftCommentId;
    }

    private void restoreDraftCommentSync() {
        DraftComment draft = interactor.restoreDraftComment(getAccountId(), commented)
                .blockingGet();

        if (nonNull(draft)) {
            draftCommentBody = draft.getBody();
            draftCommentAttachmentsCount = draft.getAttachmentsCount();
            draftCommentId = draft.getId();
        }
    }

    private void requestInitialData() {
        int accountId = getAccountId();

        Single<CommentsBundle> single;
        if (nonNull(focusToComment)) {
            single = interactor.getCommentsPortion(accountId, commented, -10, COUNT, focusToComment, CommentThread, true, "asc");
        } else if (directionDesc) {
            single = interactor.getCommentsPortion(accountId, commented, 0, COUNT, null, CommentThread, true, "desc");
        } else {
            single = interactor.getCommentsPortion(accountId, commented, 0, COUNT, null, CommentThread, true, "asc");
        }

        setLoadingState(LoadingState.INITIAL);
        actualLoadingDisposable.add(single
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitialDataReceived, this::onInitialDataError));
    }

    private void onInitialDataError(Throwable throwable) {
        setLoadingState(LoadingState.NO);
        callView(v -> showError(v, getCauseIfRuntime(throwable)));
    }

    private void loadUp() {
        if (loadingState != LoadingState.NO) return;

        Comment first = getFirstCommentInList();
        if (isNull(first)) return;

        int accountId = getAccountId();

        setLoadingState(LoadingState.UP);
        actualLoadingDisposable.add(interactor.getCommentsPortion(accountId, commented, 1, COUNT, first.getId(), CommentThread, false, "desc")
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCommentsPortionPortionReceived,
                        throwable -> onCommentPortionError(getCauseIfRuntime(throwable))));
    }

    private void loadDown() {
        if (loadingState != LoadingState.NO) return;

        Comment last = getLastCommentInList();
        if (isNull(last)) return;

        int accountId = getAccountId();

        setLoadingState(LoadingState.DOWN);
        actualLoadingDisposable.add(interactor.getCommentsPortion(accountId, commented, 0, COUNT, last.getId(), CommentThread, false, "asc")
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCommentsPortionPortionReceived,
                        throwable -> onCommentPortionError(getCauseIfRuntime(throwable))));
    }

    private void onCommentPortionError(Throwable throwable) {
        setLoadingState(LoadingState.NO);
        callView(v -> showError(v, throwable));
    }

    private void onCommentsPortionPortionReceived(CommentsBundle bundle) {
        cacheLoadingDisposable.clear();

        List<Comment> comments = bundle.getComments();

        switch (loadingState) {
            case LoadingState.UP:
                data.addAll(comments);
                callView(view -> view.notifyDataAddedToTop(comments.size()));
                break;

            case LoadingState.DOWN:
                if (nonEmpty(comments)) {
                    comments.remove(comments.size() - 1); // последним комментарием приходит комментарий к кодом, который был передан в startCommentId
                }

                if (nonEmpty(comments)) {
                    data.addAll(0, comments);
                    callView(view -> view.notifyDataAddedToBottom(comments.size()));
                }
                break;
        }

        commentedState = new CommentedState(bundle.getFirstCommentId(), bundle.getLastCommentId());
        updateAdminLevel(nonNull(bundle.getAdminLevel()) ? bundle.getAdminLevel() : 0);

        setLoadingState(LoadingState.NO);
    }

    private void updateAdminLevel(int newValue) {
        adminLevel = newValue;
        resolveCanSendAsAdminView();
    }

    private boolean canDelete(Comment comment) {
        int currentSessionUserId = getAccountId();

        Owner author = comment.getAuthor();

        // если комментарий от имени сообщества и я админ или модератор, то могу удалить
        if (author instanceof Community && ((Community) author).getAdminLevel() >= VKApiCommunity.AdminLevel.MODERATOR) {
            return true;
        }

        return comment.getFromId() == currentSessionUserId
                || commented.getSourceOwnerId() == currentSessionUserId
                || adminLevel >= VKApiCommunity.AdminLevel.MODERATOR;
    }

    private boolean canEdit(Comment comment) {
        // нельзя редактировать комментарий со стикером
        return !comment.hasStickerOnly() && comment.isCanEdit();

        /*int myUserId = getAccountId();

        if (isTopic()) {
            // если я одмен или автор коммента в топике - я могу
            // редактировать в любое время
            return myUserId == comment.getFromId() || adminLevel == VKApiCommunity.AdminLevel.ADMIN;
        } else {
            // в обратном случае у меня есть только 24 часа
            // и я должен быть автором либо админом
            boolean canEditAsAdmin = ownerIsCommunity() && comment.getFromId() == commented.getSourceOwnerId() && adminLevel == VKApiCommunity.AdminLevel.ADMIN;
            boolean canPotencialEdit = myUserId == comment.getFromId() || canEditAsAdmin;

            long currentUnixtime = new Date().getTime() / 1000;
            long max24 = 24 * 60 * 60;
            return canPotencialEdit && (currentUnixtime - comment.getDate()) < max24;
        }*/
    }

    private void setLoadingState(int loadingState) {
        this.loadingState = loadingState;

        resolveEmptyTextVisibility();
        resolveHeaderFooterViews();
        resolveCenterProgressView();
    }

    private void resolveEmptyTextVisibility() {
        callView(v -> v.setEpmtyTextVisible(loadingState == LoadingState.NO && data.isEmpty()));
    }

    private void resolveHeaderFooterViews() {

        if (data.isEmpty()) {
            // если комментариев к этому обьекту нет, то делать хидеры невидимыми
            callView(v -> v.setupLoadUpHeader(LoadMoreState.INVISIBLE));
            callView(v -> v.setupLoadDownFooter(LoadMoreState.INVISIBLE));
            return;
        }

        boolean lastResponseAvailable = nonNull(commentedState);

        if (!lastResponseAvailable) {
            // если мы еще не получили с сервера информацию о количестве комеентов, то делать хидеры невидимыми
            callView(v -> v.setupLoadUpHeader(LoadMoreState.END_OF_LIST));
            callView(v -> v.setupLoadDownFooter(LoadMoreState.END_OF_LIST));
            return;
        }

        switch (loadingState) {
            case LoadingState.NO:
                callView(v -> v.setupLoadUpHeader(isCommentsAvailableUp() ? LoadMoreState.CAN_LOAD_MORE : LoadMoreState.END_OF_LIST));
                callView(v -> v.setupLoadDownFooter(isCommentsAvailableDown() ? LoadMoreState.CAN_LOAD_MORE : LoadMoreState.END_OF_LIST));
                break;

            case LoadingState.DOWN:
                callView(v -> v.setupLoadDownFooter(LoadMoreState.LOADING));
                callView(v -> v.setupLoadUpHeader(LoadMoreState.END_OF_LIST));
                break;

            case LoadingState.UP:
                callView(v -> v.setupLoadDownFooter(LoadMoreState.END_OF_LIST));
                callView(v -> v.setupLoadUpHeader(LoadMoreState.LOADING));
                break;

            case LoadingState.INITIAL:
                callView(v -> v.setupLoadDownFooter(LoadMoreState.END_OF_LIST));
                callView(v -> v.setupLoadUpHeader(LoadMoreState.END_OF_LIST));
                break;
        }
    }

    private boolean isCommentsAvailableUp() {
        if (isNull(commentedState) || isNull(commentedState.firstCommentId)) {
            return false;
        }

        Comment fisrt = getFirstCommentInList();
        return nonNull(fisrt) && fisrt.getId() > commentedState.firstCommentId;

    }

    private boolean isCommentsAvailableDown() {
        if (isNull(commentedState) || isNull(commentedState.lastCommentId)) {
            return false;
        }

        Comment last = getLastCommentInList();
        return nonNull(last) && last.getId() < commentedState.lastCommentId;
    }

    @Nullable
    private Comment getFirstCommentInList() {
        return nonEmpty(data) ? data.get(data.size() - 1) : null;
    }

    private void resolveCenterProgressView() {
        callView(v -> v.setCenterProgressVisible(loadingState == LoadingState.INITIAL && data.isEmpty()));
    }

    @Nullable
    private Comment getLastCommentInList() {
        return nonEmpty(data) ? data.get(0) : null;
    }

    private void resolveBodyView() {
        callView(v -> v.displayBody(draftCommentBody));
    }

    private boolean canSendComment() {
        return draftCommentAttachmentsCount > 0 || trimmedNonEmpty(draftCommentBody);
    }

    private void resolveSendButtonAvailability() {
        callView(v -> v.setButtonSendAvailable(canSendComment()));
    }

    private Single<Integer> saveSingle() {
        int accountId = getAccountId();
        int replyToComment = nonNull(replyTo) ? replyTo.getId() : 0;
        int replyToUser = nonNull(replyTo) ? replyTo.getFromId() : 0;
        return interactor.safeDraftComment(accountId, commented, draftCommentBody, replyToComment, replyToUser);
    }

    private Integer saveDraftSync() {
        return saveSingle().blockingGet();
    }

    private void resolveAttachmentsCounter() {
        callView(v -> v.displayAttachmentsCount(draftCommentAttachmentsCount));
    }

    public void fireInputTextChanged(String s) {
        boolean canSend = canSendComment();

        draftCommentBody = s;

        if (canSend != canSendComment()) {
            resolveSendButtonAvailability();
        }
    }

    public void fireReplyToOwnerClick(int commentId) {
        for (int y = 0; y < data.size(); y++) {
            Comment comment = data.get(y);
            if (comment.getId() == commentId) {
                comment.setAnimationNow(true);

                int finalY = y;
                callView(v -> v.notifyItemChanged(finalY));
                callView(v -> v.moveFocusTo(finalY, true));
                return;
            } else if (comment.hasThreads()) {
                for (int s = 0; s < comment.getThreads().size(); s++) {
                    Comment thread = comment.getThreads().get(s);
                    if (thread.getId() == commentId) {
                        thread.setAnimationNow(true);
                        int finalY = y;
                        callView(v -> v.notifyItemChanged(finalY));
                        callView(v -> v.moveFocusTo(finalY, true));
                        return;
                    }
                }
            }
        }

        //safeShowToast(getView(), R.string.the_comment_is_not_in_the_list, false);

        startDeepCommentFinding(commentId);
    }

    private void startDeepCommentFinding(int commentId) {
        if (loadingState != LoadingState.NO) {
            // не грузить, если сейчас что-то грузится
            return;
        }

        Comment older = getFirstCommentInList();
        AssertUtils.requireNonNull(older);

        int accountId = getAccountId();

        callView(ICommentsView::displayDeepLookingCommentProgress);

        deepLookingHolder.append(interactor.getAllCommentsRange(accountId, commented, older.getId(), commentId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(comments -> onDeepCommentLoadingResponse(commentId, comments), this::onDeepCommentLoadingError));
    }

    private void onDeepCommentLoadingError(Throwable throwable) {
        callView(ICommentsView::dismissDeepLookingCommentProgress);

        if (throwable instanceof NotFoundException) {
            callView(v -> v.showToast(R.string.the_comment_is_not_in_the_list, false));
        } else {
            callView(v -> showError(v, throwable));
        }
    }

    @Override
    public void onGuiDestroyed() {
        deepLookingHolder.dispose();
        super.onGuiDestroyed();
    }

    public void fireDeepLookingCancelledByUser() {
        deepLookingHolder.dispose();
    }

    private void onDeepCommentLoadingResponse(int commentId, List<Comment> comments) {
        callView(ICommentsView::dismissDeepLookingCommentProgress);

        data.addAll(comments);

        int index = -1;
        for (int i = 0; i < data.size(); i++) {
            Comment comment = data.get(i);

            if (comment.getId() == commentId) {
                index = i;
                comment.setAnimationNow(true);
                break;
            }
        }

        if (index == -1) {
            return;
        }

        callView(view -> view.notifyDataAddedToTop(comments.size()));

        int finalIndex = index;
        callView(view -> view.moveFocusTo(finalIndex, false));
    }

    public void fireAttachClick() {
        if (isNull(draftCommentId)) {
            draftCommentId = saveDraftSync();
        }

        int accountId = getAccountId();
        callView(v -> v.openAttachmentsManager(accountId, draftCommentId, commented.getSourceOwnerId(), draftCommentBody));
    }

    public void fireEditBodyResult(String newBody) {
        draftCommentBody = newBody;
        resolveSendButtonAvailability();
        resolveBodyView();
    }

    public void fireReplyToCommentClick(Comment comment) {
        if (commented.getSourceType() == CommentedType.TOPIC) {
            // в топиках механизм ответа отличается
            String replyText = buildReplyTextFor(comment);
            callView(v -> v.replaceBodySelectionTextTo(replyText));
        } else {
            replyTo = comment;
            resolveReplyViews();
        }
    }

    public void fireReplyToCommentClick(int index) {
        if (data.size() <= index || index < 0) {
            return;
        }
        Comment comment = data.get(index);
        if (commented.getSourceType() == CommentedType.TOPIC) {
            // в топиках механизм ответа отличается
            String replyText = buildReplyTextFor(comment);
            callView(v -> v.replaceBodySelectionTextTo(replyText));
        } else {
            replyTo = comment;
            resolveReplyViews();
        }
    }

    public void fireReport(Comment comment) {
        CharSequence[] items = {"Спам", "Детская порнография", "Экстремизм", "Насилие", "Пропаганда наркотиков", "Материал для взрослых", "Оскорбление", "Призывы к суициду"};
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.report)
                .setItems(items, (dialog, item) -> {
                    appendDisposable(interactor.reportComment(getAccountId(), comment.getFromId(), comment.getId(), item)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(p -> {
                                if (p == 1)
                                    callView(v -> v.getCustomToast().showToast(R.string.success));
                                else
                                    callView(v -> v.getCustomToast().showToast(R.string.error));
                            }, t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
                    dialog.dismiss();
                })
                .show();
    }

    public void fireWhoLikesClick(Comment comment) {
        callView(v -> v.goToLikes(getAccountId(), getApiCommentType(comment), commented.getSourceOwnerId(), comment.getId()));
    }

    public void fireReplyToChat(Comment comment) {
        SendAttachmentsActivity.startForSendAttachments(context, getAccountId(), new WallReply().buildFromComment(comment, commented));
    }

    private String getApiCommentType(Comment comment) {
        switch (comment.getCommented().getSourceType()) {
            case CommentedType.PHOTO:
                return "photo_comment";
            case CommentedType.POST:
                return "comment";
            case CommentedType.VIDEO:
                return "video_comment";
            case CommentedType.TOPIC:
                return "topic_comment";
            default:
                throw new IllegalArgumentException();
        }
    }

    public void fireSendClick() {
        sendNormalComment();
    }

    private void sendNormalComment() {
        setSendingNow(true);

        int accountId = getAccountId();
        CommentIntent intent = createCommentIntent();
        if (intent.getReplyToComment() == null && CommentThread != null)
            intent.setReplyToComment(CommentThread);

        appendDisposable(interactor.send(accountId, commented, CommentThread, intent)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onNormalSendResponse, this::onSendError));
    }

    private void sendQuickComment(CommentIntent intent) {
        setSendingNow(true);

        if (intent.getReplyToComment() == null && CommentThread != null)
            intent.setReplyToComment(CommentThread);

        int accountId = getAccountId();
        appendDisposable(interactor.send(accountId, commented, CommentThread, intent)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onQuickSendResponse, this::onSendError));
    }

    private void onSendError(Throwable t) {
        setSendingNow(false);
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onQuickSendResponse(Comment comment) {
        setSendingNow(false);

        handleCommentAdded();

        replyTo = null;

        resolveReplyViews();
        resolveEmptyTextVisibility();
    }

    private void handleCommentAdded() {
        callView(ICommentsView::showCommentSentToast);
        fireRefreshClick();
    }

    private void handleCommentAddedNotFixable(Comment comment) {
        boolean canAdd;

        if (isNull(commentedState)) {
            canAdd = false;
        } else if (data.isEmpty()) {
            canAdd = true;
        } else if (isNull(commentedState.lastCommentId)) {
            canAdd = true;
        } else {
            Comment last = data.get(0);
            canAdd = last.getId() == commentedState.lastCommentId;
        }

        if (canAdd) {
            data.add(0, comment);
            commentedState.lastCommentId = comment.getId();
            callView(view -> view.notifyDataAddedToBottom(1));
        } else {
            callView(ICommentsView::showCommentSentToast);
        }
    }

    private void onNormalSendResponse(Comment comment) {
        setSendingNow(false);

        handleCommentAdded();

        draftCommentAttachmentsCount = 0;
        draftCommentBody = null;
        draftCommentId = null;
        replyTo = null;

        resolveAttachmentsCounter();
        resolveBodyView();
        resolveReplyViews();
        resolveSendButtonAvailability();
        resolveEmptyTextVisibility();
    }

    private CommentIntent createCommentIntent() {
        Integer replyToComment = isNull(replyTo) ? null : replyTo.getId();
        String body = draftCommentBody;

        return new CommentIntent(authorId)
                .setMessage(body)
                .setReplyToComment(replyToComment)
                .setDraftMessageId(draftCommentId);
    }

    private void setSendingNow(boolean sendingNow) {
        this.sendingNow = sendingNow;
        resolveProgressDialog();
    }

    private void resolveProgressDialog() {
        if (sendingNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.publication, false));
        } else if (loadingAvailableAuthorsNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.getting_list_loading_message, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    public void fireCommentContextViewCreated(ICommentsView.ICommentContextView view, Comment comment) {
        view.setCanDelete(canDelete(comment));
        view.setCanEdit(canEdit(comment));
        view.setCanBan(canBanAuthor(comment));
    }

    private boolean canBanAuthor(Comment comment) {
        return comment.getFromId() > 0 // только пользователей
                && comment.getFromId() != getAccountId() // не блокируем себя
                && adminLevel >= VKApiCommunity.AdminLevel.MODERATOR; // только если я модератор и выше
    }

    public void fireCommentDeleteClick(Comment comment) {
        deleteRestoreInternal(comment.getId(), true);
    }

    private void deleteRestoreInternal(int commentId, boolean delete) {
        int accountId = getAccountId();
        appendDisposable(interactor.deleteRestore(accountId, commented, commentId, delete)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(dummy(), t -> callView(v -> showError(v, t))));
    }

    public void fireCommentEditClick(Comment comment) {
        int accountId = getAccountId();
        callView(v -> v.goToCommentEdit(accountId, comment, CommentThread));
    }

    public void fireCommentLikeClick(Comment comment, boolean add) {
        likeInternal(add, comment);
    }

    private void likeInternal(boolean add, Comment comment) {
        int accountId = getAccountId();

        appendDisposable(interactor.like(accountId, comment.getCommented(), comment.getId(), add)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(dummy(), t -> callView(v -> showError(v, t))));
    }

    public void fireCommentRestoreClick(int commentId) {
        deleteRestoreInternal(commentId, false);
    }

    public void fireStickerClick(Sticker sticker) {
        CommentIntent intent = new CommentIntent(authorId)
                .setReplyToComment(isNull(replyTo) ? null : replyTo.getId())
                .setStickerId(sticker.getId());

        sendQuickComment(intent);
    }

    public void fireGotoSourceClick() {
        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                Photo photo = new Photo()
                        .setOwnerId(commented.getSourceOwnerId())
                        .setId(commented.getSourceId())
                        .setAccessKey(commented.getAccessKey());

                firePhotoClick(Utils.singletonArrayList(photo), 0, true);
                break;

            case CommentedType.POST:
                callView(v -> v.goToWallPost(getAccountId(), commented.getSourceId(), commented.getSourceOwnerId()));
                break;

            case CommentedType.VIDEO:
                callView(v -> v.goToVideoPreview(getAccountId(), commented.getSourceId(), commented.getSourceOwnerId()));
                break;

            case CommentedType.TOPIC:
                // not supported
                break;
        }
    }

    public void fireTopicPollClick() {
        firePollClick(topicPoll);
    }

    public void fireRefreshClick() {
        if (loadingState != LoadingState.INITIAL) {
            actualLoadingDisposable.clear();
            requestInitialData();
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveOptionMenu();
    }

    private void resolveOptionMenu() {
        boolean hasPoll = nonNull(topicPoll);
        boolean hasGotoSource = commented.getSourceType() != CommentedType.TOPIC;

        @StringRes
        Integer gotoSourceText = null;
        if (hasGotoSource) {
            switch (commented.getSourceType()) {
                case CommentedType.PHOTO:
                    gotoSourceText = R.string.go_to_photo;
                    break;
                case CommentedType.VIDEO:
                    gotoSourceText = R.string.go_to_video;
                    break;
                case CommentedType.POST:
                    gotoSourceText = R.string.go_to_post;
                    break;
                case CommentedType.TOPIC:
                    // not supported
                    break;
            }
        }

        Integer finalGotoSourceText = gotoSourceText;
        callResumedView(v -> v.setupOptionMenu(hasPoll, hasGotoSource, finalGotoSourceText));
    }

    public void fireCommentEditResult(Comment comment) {
        if (commented.equals(comment.getCommented())) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getId() == comment.getId()) {
                    data.set(i, comment);
                    int finalI = i;
                    callView(v -> v.notifyItemChanged(finalI));
                    break;
                }
            }
        }
    }

    public void fireBanClick(Comment comment) {
        User user = (User) comment.getAuthor();
        int groupId = Math.abs(commented.getSourceOwnerId());

        callView(v -> v.banUser(getAccountId(), groupId, user));
    }

    private void setLoadingAvailableAuthorsNow(boolean loadingAvailableAuthorsNow) {
        this.loadingAvailableAuthorsNow = loadingAvailableAuthorsNow;
        resolveProgressDialog();
    }

    public void fireSendLongClick() {
        setLoadingAvailableAuthorsNow(true);

        int accountId = getAccountId();

        boolean canSendFromAnyGroup = commented.getSourceType() == CommentedType.POST;

        Single<List<Owner>> single;

        if (canSendFromAnyGroup) {
            single = interactor.getAvailableAuthors(accountId);
        } else {
            Set<Integer> ids = new HashSet<>();
            ids.add(accountId);
            ids.add(commented.getSourceOwnerId());
            single = ownersRepository.findBaseOwnersDataAsList(accountId, ids, IOwnersRepository.MODE_ANY);
        }

        appendDisposable(single
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onAvailableAuthorsReceived, throwable -> onAvailableAuthorsGetError(getCauseIfRuntime(throwable))));
    }

    private void onAvailableAuthorsGetError(Throwable throwable) {
        setLoadingAvailableAuthorsNow(false);
    }

    private void onAvailableAuthorsReceived(List<Owner> owners) {
        setLoadingAvailableAuthorsNow(false);
        callView(view -> view.showAuthorSelectDialog(owners));
    }

    public void fireAuthorSelected(Owner owner) {
        author = owner;
        authorId = owner.getOwnerId();
        resolveAuthorAvatarView();
    }

    public void fireDirectionChanged() {
        data.clear();
        callView(ICommentsView::notifyDataSetChanged);

        directionDesc = Settings.get().other().isCommentsDesc();
        requestInitialData();
    }

    private void checkFocusToCommentDone() {
        if (nonNull(focusToComment)) {
            for (int i = 0; i < data.size(); i++) {
                Comment comment = data.get(i);
                if (comment.getId() == focusToComment) {
                    comment.setAnimationNow(true);
                    focusToComment = null;
                    int finalI = i;
                    callView(v -> v.moveFocusTo(finalI, false));
                    break;
                }
            }
        }
    }

    private void onInitialDataReceived(CommentsBundle bundle) {
        // отменяем загрузку из БД если активна
        cacheLoadingDisposable.clear();

        data.clear();
        data.addAll(bundle.getComments());

        commentedState = new CommentedState(bundle.getFirstCommentId(), bundle.getLastCommentId());
        updateAdminLevel(nonNull(bundle.getAdminLevel()) ? bundle.getAdminLevel() : 0);

        // init poll once
        topicPoll = bundle.getTopicPoll();

        setLoadingState(LoadingState.NO);
        callView(ICommentsView::notifyDataSetChanged);

        if (nonNull(focusToComment)) {
            checkFocusToCommentDone();
        } else if (!directionDesc) {
            callView(view -> view.scrollToPosition(data.size() - 1));
        }

        resolveOptionMenu();
        resolveHeaderFooterViews();
    }

    private void loadCachedData() {
        int accountId = getAccountId();
        cacheLoadingDisposable.add(interactor.getAllCachedData(accountId, commented)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, ignore()));
    }

    private void resolveCanSendAsAdminView() {
        callView(v -> v.setCanSendSelectAuthor(commented.getSourceType() == CommentedType.POST || adminLevel >= VKApiCommunity.AdminLevel.MODERATOR));
    }

    @Override
    public void onGuiCreated(@NonNull ICommentsView view) {
        super.onGuiCreated(view);

        view.displayData(data);
        view.setToolbarTitle(getString(R.string.comments));

        switch (commented.getSourceType()) {
            case CommentedType.POST:
                view.setToolbarSubtitle(getString(R.string.for_wall_post));
                break;
            case CommentedType.PHOTO:
                view.setToolbarSubtitle(getString(R.string.for_photo));
                break;
            case CommentedType.VIDEO:
                view.setToolbarSubtitle(getString(R.string.for_video));
                break;
            case CommentedType.TOPIC:
                view.setToolbarSubtitle(getString(R.string.for_topic));
                break;
        }

        resolveCanSendAsAdminView();
        resolveReplyViews();
        checkFocusToCommentDone();
        resolveEmptyTextVisibility();
        resolveProgressDialog();
        resolveAttachmentsCounter();
        resolveSendButtonAvailability();
        resolveAuthorAvatarView();
        resolveBodyView();
        resolveHeaderFooterViews();
        resolveCenterProgressView();
    }

    private void onCachedDataReceived(List<Comment> comments) {
        data.clear();
        data.addAll(comments);

        resolveHeaderFooterViews();
        resolveEmptyTextVisibility();
        resolveCenterProgressView();
        callView(ICommentsView::notifyDataSetChanged);
        if (!directionDesc) {
            callView(v -> v.scrollToPosition(data.size() - 1));
        }
    }

    @Override
    public void onDestroyed() {
        cacheLoadingDisposable.dispose();
        actualLoadingDisposable.dispose();
        deepLookingHolder.dispose();
        stickersWordsDisplayDisposable.dispose();

        // save draft async
        saveSingle().subscribeOn(Schedulers.io()).subscribe(ignore(), ignore());
        super.onDestroyed();
    }

    private void resolveReplyViews() {
        callView(v -> v.setupReplyViews(nonNull(replyTo) ? replyTo.getFullAuthorName() : null));
    }

    public void fireReplyCancelClick() {
        replyTo = null;
        resolveReplyViews();
    }

    public void fireUpLoadMoreClick() {
        loadUp();
    }

    public void fireDownLoadMoreClick() {
        loadDown();
    }

    public void fireScrollToTop() {
        if (isCommentsAvailableUp()) {
            loadUp();
        }
    }

    private static class CommentedState {

        final Integer firstCommentId;
        Integer lastCommentId;

        CommentedState(Integer firstCommentId, Integer lastCommentId) {
            this.firstCommentId = firstCommentId;
            this.lastCommentId = lastCommentId;
        }
    }

    private static final class LoadingState {
        static final int NO = 0;
        static final int INITIAL = 1;
        static final int UP = 2;
        static final int DOWN = 3;
    }
}
