package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IGroupSettingsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.BlockReason;
import dev.ragnarok.fenrir.model.IdOption;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityBanEditView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class CommunityBanEditPresenter extends AccountDependencyPresenter<ICommunityBanEditView> {

    private static final String TAG = CommunityBanEditPresenter.class.getSimpleName();
    private static final int BLOCK_FOR_UNCHANGED = -1;
    private static final int REQUEST_CODE_BLOCK_FOR = 1;
    private static final int REQUEST_CODE_REASON = 2;
    private final int groupId;
    private final Banned banned;
    private final ArrayList<Owner> users;
    private final IGroupSettingsInteractor interactor;
    private int index;
    private BlockFor blockFor;
    private int reason;
    private String comment;
    private boolean showCommentToUser;
    private boolean requestNow;

    public CommunityBanEditPresenter(int accountId, int groupId, Banned banned, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
        this.banned = banned;
        users = Utils.singletonArrayList(banned.getBanned());

        Banned.Info info = banned.getInfo();

        blockFor = new BlockFor(info.getEndDate());
        reason = info.getReason();
        comment = info.getComment();
        showCommentToUser = info.isCommentVisible();
        index = 0;
        interactor = InteractorFactory.createGroupSettingsInteractor();
    }

    public CommunityBanEditPresenter(int accountId, int groupId, ArrayList<Owner> users, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
        banned = null;
        this.users = users;
        index = 0;
        blockFor = new BlockFor(BlockFor.FOREVER); // by default
        reason = BlockReason.OTHER;
        interactor = InteractorFactory.createGroupSettingsInteractor();
    }

    private Owner currentBanned() {
        return users.get(index);
    }

    private void resolveCommentViews() {
        callView(v -> v.diplayComment(comment));
        callView(v -> v.setShowCommentChecked(showCommentToUser));
    }

    private void resolveBanStatusView() {
        if (nonNull(banned)) {
            callView(v -> v.displayBanStatus(banned.getAdmin().getId(), banned.getAdmin().getFullName(), banned.getInfo().getEndDate()));
        }
    }

    private void resolveUserInfoViews() {
        callView(v -> v.displayUserInfo(currentBanned()));
    }

    private void resolveBlockForView() {

        String blockForText;

        switch (blockFor.type) {
            case BlockFor.FOREVER:
                blockForText = getString(R.string.block_for_forever);
                break;

            case BlockFor.YEAR:
                blockForText = getString(R.string.block_for_year);
                break;

            case BlockFor.MONTH:
                blockForText = getString(R.string.block_for_month);
                break;

            case BlockFor.WEEK:
                blockForText = getString(R.string.block_for_week);
                break;

            case BlockFor.DAY:
                blockForText = getString(R.string.block_for_day);
                break;

            case BlockFor.HOUR:
                blockForText = getString(R.string.block_for_hour);
                break;

            case BlockFor.CUSTOM:
                blockForText = formatBlockFor();
                break;

            default:
                throw new IllegalStateException();
        }

        callView(v -> v.displayBlockFor(blockForText));
    }

    private void resolveReasonView() {
        switch (reason) {
            case BlockReason.SPAM:
                callView(v -> v.displayReason(getString(R.string.reason_spam)));
                break;
            case BlockReason.IRRELEVANT_MESSAGES:
                callView(v -> v.displayReason(getString(R.string.reason_irrelevant_messages)));
                break;
            case BlockReason.STRONG_LANGUAGE:
                callView(v -> v.displayReason(getString(R.string.reason_strong_language)));
                break;
            case BlockReason.VERBAL_ABUSE:
                callView(v -> v.displayReason(getString(R.string.reason_verbal_abuse)));
                break;
            default:
                callView(v -> v.displayReason(getString(R.string.reason_other)));
                break;
        }
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveProgressView();
    }

    private void resolveProgressView() {
        if (requestNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.saving, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityBanEditView view) {
        super.onGuiCreated(view);

        resolveUserInfoViews();
        resolveProgressView();
        resolveReasonView();
        resolveBlockForView();
        resolveBanStatusView();
        resolveCommentViews();
    }

    public void fireButtonSaveClick() {
        setRequestNow(true);

        int accountId = getAccountId();
        int ownerId = currentBanned().getOwnerId();
        Date endDate = blockFor.getUnblockingDate();

        Long endDateUnixtime = nonNull(endDate) ? endDate.getTime() / 1000 : null;

        appendDisposable(interactor.ban(accountId, groupId, ownerId, endDateUnixtime, reason, comment, showCommentToUser)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onAddBanComplete, throwable -> onAddBanError(getCauseIfRuntime(throwable))));
    }

    private void onAddBanComplete() {
        setRequestNow(false);
        callView(v -> v.showToast(R.string.success, false));

        if (index == users.size() - 1) {
            callView(ICommunityBanEditView::goBack);
        } else {
            // switch to next user
            index++;

            resolveUserInfoViews();
        }
    }

    private void onAddBanError(Throwable throwable) {
        setRequestNow(false);
        throwable.printStackTrace();
        callView(v -> showError(v, throwable));
    }

    public void fireShowCommentCheck(boolean isChecked) {
        showCommentToUser = isChecked;
    }

    public void fireCommentEdit(CharSequence s) {
        comment = s.toString();
    }

    public void fireBlockForClick() {
        List<IdOption> options = new ArrayList<>();
        if (blockFor.type == BlockFor.CUSTOM) {
            options.add(new IdOption(BLOCK_FOR_UNCHANGED, formatBlockFor()));
        }

        options.add(new IdOption(BlockFor.FOREVER, getString(R.string.block_for_forever)));
        options.add(new IdOption(BlockFor.YEAR, getString(R.string.block_for_year)));
        options.add(new IdOption(BlockFor.MONTH, getString(R.string.block_for_month)));
        options.add(new IdOption(BlockFor.WEEK, getString(R.string.block_for_week)));
        options.add(new IdOption(BlockFor.DAY, getString(R.string.block_for_day)));
        options.add(new IdOption(BlockFor.HOUR, getString(R.string.block_for_hour)));

        callView(v -> v.displaySelectOptionDialog(REQUEST_CODE_BLOCK_FOR, options));
    }

    public void fireOptionSelected(int requestCode, IdOption idOption) {
        switch (requestCode) {
            case REQUEST_CODE_BLOCK_FOR:
                if (idOption.getId() != BLOCK_FOR_UNCHANGED) {
                    blockFor = new BlockFor(idOption.getId());
                    resolveBlockForView();
                } //else not changed
                break;

            case REQUEST_CODE_REASON:
                reason = idOption.getId();
                resolveReasonView();
                break;
        }
    }

    private String formatBlockFor() {
        Date date = blockFor.getUnblockingDate();
        if (isNull(date)) {
            Logger.wtf(TAG, "formatBlockFor, date-is-null???");
            return "NULL";
        }

        String formattedDate = DateFormat.getDateInstance().format(date);
        String formattedTime = DateFormat.getTimeInstance().format(date);
        return getString(R.string.until_date_time, formattedDate, formattedTime);
    }

    public void fireResonClick() {
        List<IdOption> options = new ArrayList<>();

        options.add(new IdOption(BlockReason.SPAM, getString(R.string.reason_spam)));
        options.add(new IdOption(BlockReason.IRRELEVANT_MESSAGES, getString(R.string.reason_irrelevant_messages)));
        options.add(new IdOption(BlockReason.STRONG_LANGUAGE, getString(R.string.reason_strong_language)));
        options.add(new IdOption(BlockReason.VERBAL_ABUSE, getString(R.string.reason_verbal_abuse)));
        options.add(new IdOption(BlockReason.OTHER, getString(R.string.reason_other)));

        callView(v -> v.displaySelectOptionDialog(REQUEST_CODE_REASON, options));
    }

    public void fireAvatarClick() {
        callView(v -> v.openProfile(getAccountId(), currentBanned()));
    }

    private static final class BlockFor {

        static final int FOREVER = 0;
        static final int YEAR = 1;
        static final int MONTH = 2;
        static final int WEEK = 3;
        static final int DAY = 4;
        static final int HOUR = 5;
        static final int CUSTOM = 6;
        final int type;
        final long customDate;

        BlockFor(int type) {
            this.type = type;
            customDate = 0;
        }

        BlockFor(long customDate) {
            this.customDate = customDate;
            type = customDate > 0 ? CUSTOM : FOREVER;
        }

        Date getUnblockingDate() {
            if (type == CUSTOM) {
                return new Date(customDate * 1000);
            }

            Calendar calendar = Calendar.getInstance();
            switch (type) {
                case YEAR:
                    calendar.add(Calendar.YEAR, 1);
                    break;
                case MONTH:
                    calendar.add(Calendar.MONTH, 1);
                    break;
                case WEEK:
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
                    break;
                case DAY:
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    break;
                case HOUR:
                    calendar.add(Calendar.HOUR, 1);
                    break;

                case FOREVER:
                    return null;
            }

            return calendar.getTime();
        }
    }
}