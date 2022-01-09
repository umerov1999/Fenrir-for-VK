package dev.ragnarok.fenrir.db.impl;

import static android.text.TextUtils.join;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.annotation.SuppressLint;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.FriendListsColumns;
import dev.ragnarok.fenrir.db.column.GroupColumns;
import dev.ragnarok.fenrir.db.column.GroupsDetColumns;
import dev.ragnarok.fenrir.db.column.UserColumns;
import dev.ragnarok.fenrir.db.column.UsersDetColumns;
import dev.ragnarok.fenrir.db.interfaces.IOwnersStorage;
import dev.ragnarok.fenrir.db.model.BanAction;
import dev.ragnarok.fenrir.db.model.UserPatch;
import dev.ragnarok.fenrir.db.model.entity.CommunityDetailsEntity;
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity;
import dev.ragnarok.fenrir.db.model.entity.FriendListEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.UserDetailsEntity;
import dev.ragnarok.fenrir.db.model.entity.UserEntity;
import dev.ragnarok.fenrir.fragment.UserInfoResolveUtil;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

class OwnersStorage extends AbsStorage implements IOwnersStorage {

    private final PublishSubject<BanAction> banActionsPublisher;
    private final PublishSubject<Pair<Integer, Manager>> managementActionsPublisher;

    OwnersStorage(@NonNull AppStorages context) {
        super(context);
        banActionsPublisher = PublishSubject.create();
        managementActionsPublisher = PublishSubject.create();
    }

    private static void appendUserInsertOperation(@NonNull List<ContentProviderOperation> operations, @NonNull Uri uri, UserEntity dbo) {
        operations.add(ContentProviderOperation.newInsert(uri)
                .withValues(createCv(dbo))
                .build());
    }

    private static void appendCommunityInsertOperation(@NonNull List<ContentProviderOperation> operations, @NonNull Uri uri, CommunityEntity dbo) {
        operations.add(ContentProviderOperation.newInsert(uri)
                .withValues(createCv(dbo))
                .build());
    }

    static void appendOwnersInsertOperations(@NonNull List<ContentProviderOperation> operations, int accountId, OwnerEntities ownerEntities) {
        appendUsersInsertOperation(operations, accountId, ownerEntities.getUserEntities());
        appendCommunitiesInsertOperation(operations, accountId, ownerEntities.getCommunityEntities());
    }

    static void appendUsersInsertOperation(@NonNull List<ContentProviderOperation> operations, int accouuntId, List<UserEntity> dbos) {
        Uri uri = MessengerContentProvider.getUserContentUriFor(accouuntId);
        for (UserEntity dbo : dbos) {
            appendUserInsertOperation(operations, uri, dbo);
        }
    }

    static void appendCommunitiesInsertOperation(@NonNull List<ContentProviderOperation> operations, int accouuntId, List<CommunityEntity> dbos) {
        Uri uri = MessengerContentProvider.getGroupsContentUriFor(accouuntId);
        for (CommunityEntity dbo : dbos) {
            appendCommunityInsertOperation(operations, uri, dbo);
        }
    }

    private static ContentValues createCv(CommunityEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(BaseColumns._ID, dbo.getId());
        cv.put(GroupColumns.NAME, dbo.getName());
        cv.put(GroupColumns.SCREEN_NAME, dbo.getScreenName());
        cv.put(GroupColumns.IS_CLOSED, dbo.getClosed());
        cv.put(GroupColumns.IS_VERIFIED, dbo.isVerified());
        cv.put(GroupColumns.IS_ADMIN, dbo.isAdmin());
        cv.put(GroupColumns.ADMIN_LEVEL, dbo.getAdminLevel());
        cv.put(GroupColumns.IS_MEMBER, dbo.isMember());
        cv.put(GroupColumns.MEMBER_STATUS, dbo.getMemberStatus());
        cv.put(GroupColumns.MEMBERS_COUNT, dbo.getMembersCount());
        cv.put(GroupColumns.TYPE, dbo.getType());
        cv.put(GroupColumns.PHOTO_50, dbo.getPhoto50());
        cv.put(GroupColumns.PHOTO_100, dbo.getPhoto100());
        cv.put(GroupColumns.PHOTO_200, dbo.getPhoto200());
        return cv;
    }

    private static ContentValues createCv(UserEntity dbo) {
        ContentValues cv = new ContentValues();
        cv.put(BaseColumns._ID, dbo.getId());
        cv.put(UserColumns.FIRST_NAME, dbo.getFirstName());
        cv.put(UserColumns.LAST_NAME, dbo.getLastName());
        cv.put(UserColumns.ONLINE, dbo.isOnline());
        cv.put(UserColumns.ONLINE_MOBILE, dbo.isOnlineMobile());
        cv.put(UserColumns.ONLINE_APP, dbo.getOnlineApp());
        cv.put(UserColumns.PHOTO_50, dbo.getPhoto50());
        cv.put(UserColumns.PHOTO_100, dbo.getPhoto100());
        cv.put(UserColumns.PHOTO_200, dbo.getPhoto200());
        cv.put(UserColumns.PHOTO_MAX, dbo.getPhotoMax());
        cv.put(UserColumns.LAST_SEEN, dbo.getLastSeen());
        cv.put(UserColumns.PLATFORM, dbo.getPlatform());
        cv.put(UserColumns.USER_STATUS, dbo.getStatus());
        cv.put(UserColumns.SEX, dbo.getSex());
        cv.put(UserColumns.DOMAIN, dbo.getDomain());
        cv.put(UserColumns.IS_FRIEND, dbo.isFriend());
        cv.put(UserColumns.FRIEND_STATUS, dbo.getFriendStatus());
        cv.put(UserColumns.WRITE_MESSAGE_STATUS, dbo.getCanWritePrivateMessage());
        cv.put(UserColumns.IS_USER_BLACK_LIST, dbo.getBlacklisted_by_me());
        cv.put(UserColumns.IS_BLACK_LISTED, dbo.getBlacklisted());
        cv.put(UserColumns.IS_VERIFIED, dbo.isVerified());
        cv.put(UserColumns.IS_CAN_ACCESS_CLOSED, dbo.isCan_access_closed());
        cv.put(UserColumns.MAIDEN_NAME, dbo.getMaiden_name());
        return cv;
    }

    private static CommunityEntity mapCommunityDbo(Cursor cursor) {
        return new CommunityEntity(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                .setName(cursor.getString(cursor.getColumnIndex(GroupColumns.NAME)))
                .setScreenName(cursor.getString(cursor.getColumnIndex(GroupColumns.SCREEN_NAME)))
                .setClosed(cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_CLOSED)))
                .setVerified(cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_VERIFIED)) == 1)
                .setAdmin(cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_ADMIN)) == 1)
                .setAdminLevel(cursor.getInt(cursor.getColumnIndex(GroupColumns.ADMIN_LEVEL)))
                .setMember(cursor.getInt(cursor.getColumnIndex(GroupColumns.IS_MEMBER)) == 1)
                .setMemberStatus(cursor.getInt(cursor.getColumnIndex(GroupColumns.MEMBER_STATUS)))
                .setMembersCount(cursor.getInt(cursor.getColumnIndex(GroupColumns.MEMBERS_COUNT)))
                .setType(cursor.getInt(cursor.getColumnIndex(GroupColumns.TYPE)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(GroupColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(GroupColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(GroupColumns.PHOTO_200)));
    }

    private static UserEntity mapUserDbo(Cursor cursor) {
        return new UserEntity(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                .setFirstName(cursor.getString(cursor.getColumnIndex(UserColumns.FIRST_NAME)))
                .setLastName(cursor.getString(cursor.getColumnIndex(UserColumns.LAST_NAME)))
                .setOnline(cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE)) == 1)
                .setOnlineMobile(cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE_MOBILE)) == 1)
                .setOnlineApp(cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE_APP)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(UserColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(UserColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(UserColumns.PHOTO_200)))
                .setPhotoMax(cursor.getString(cursor.getColumnIndex(UserColumns.PHOTO_MAX)))
                .setLastSeen(cursor.getLong(cursor.getColumnIndex(UserColumns.LAST_SEEN)))
                .setPlatform(cursor.getInt(cursor.getColumnIndex(UserColumns.PLATFORM)))
                .setStatus(cursor.getString(cursor.getColumnIndex(UserColumns.USER_STATUS)))
                .setSex(cursor.getInt(cursor.getColumnIndex(UserColumns.SEX)))
                .setDomain(cursor.getString(cursor.getColumnIndex(UserColumns.DOMAIN)))
                .setFriend(cursor.getInt(cursor.getColumnIndex(UserColumns.IS_FRIEND)) == 1)
                .setFriendStatus(cursor.getInt(cursor.getColumnIndex(UserColumns.FRIEND_STATUS)))
                .setCanWritePrivateMessage(cursor.getInt(cursor.getColumnIndex(UserColumns.WRITE_MESSAGE_STATUS)) == 1)
                .setBlacklisted_by_me(cursor.getInt(cursor.getColumnIndex(UserColumns.IS_USER_BLACK_LIST)) == 1)
                .setBlacklisted(cursor.getInt(cursor.getColumnIndex(UserColumns.IS_BLACK_LISTED)) == 1)
                .setVerified(cursor.getInt(cursor.getColumnIndex(UserColumns.IS_VERIFIED)) == 1)
                .setCan_access_closed(cursor.getInt(cursor.getColumnIndex(UserColumns.IS_CAN_ACCESS_CLOSED)) == 1)
                .setMaiden_name(cursor.getString(cursor.getColumnIndex(UserColumns.MAIDEN_NAME)));
    }

    @Override
    public Completable fireBanAction(BanAction action) {
        return Completable.fromAction(() -> banActionsPublisher.onNext(action));
    }

    @Override
    public Observable<BanAction> observeBanActions() {
        return banActionsPublisher;
    }

    @Override
    public Completable fireManagementChangeAction(Pair<Integer, Manager> manager) {
        return Completable.fromAction(() -> managementActionsPublisher.onNext(manager));
    }

    @Override
    public Observable<Pair<Integer, Manager>> observeManagementChanges() {
        return managementActionsPublisher;
    }

    @Override
    public Single<Optional<UserDetailsEntity>> getUserDetails(int accountId, int userId) {
        return Single.fromCallable(() -> {
            Uri uri = MessengerContentProvider.getUserDetContentUriFor(accountId);
            final String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(userId)};

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);
            UserDetailsEntity details = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    String json = cursor.getString(cursor.getColumnIndex(UsersDetColumns.DATA));
                    if (nonEmpty(json)) {
                        details = GSON.fromJson(json, UserDetailsEntity.class);
                    }
                }

                cursor.close();
            }

            return Optional.wrap(details);
        });
    }

    @Override
    public Single<Optional<CommunityDetailsEntity>> getGroupsDetails(int accountId, int groupId) {
        return Single.fromCallable(() -> {
            Uri uri = MessengerContentProvider.getGroupsDetContentUriFor(accountId);
            final String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(groupId)};

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);
            CommunityDetailsEntity details = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    String json = cursor.getString(cursor.getColumnIndex(GroupsDetColumns.DATA));
                    if (nonEmpty(json)) {
                        details = GSON.fromJson(json, CommunityDetailsEntity.class);
                    }
                }

                cursor.close();
            }

            return Optional.wrap(details);
        });
    }

    @Override
    public Completable storeGroupsDetails(int accountId, int groupId, CommunityDetailsEntity dbo) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();
            cv.put(BaseColumns._ID, groupId);
            cv.put(GroupsDetColumns.DATA, GSON.toJson(dbo));

            Uri uri = MessengerContentProvider.getGroupsDetContentUriFor(accountId);

            getContentResolver().insert(uri, cv);
        });
    }

    @Override
    public Completable storeUserDetails(int accountId, int userId, UserDetailsEntity dbo) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();
            cv.put(BaseColumns._ID, userId);
            cv.put(UsersDetColumns.DATA, GSON.toJson(dbo));

            Uri uri = MessengerContentProvider.getUserDetContentUriFor(accountId);

            getContentResolver().insert(uri, cv);
        });
    }

    @Override
    public Completable applyPathes(int accountId, @NonNull List<UserPatch> patches) {
        if (patches.isEmpty()) {
            return Completable.complete();
        }

        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(patches.size());

            for (UserPatch patch : patches) {
                ContentValues cv = new ContentValues();

                if (nonNull(patch.getStatus())) {
                    cv.put(UserColumns.USER_STATUS, patch.getStatus().getStatus());
                }

                if (nonNull(patch.getOnline())) {
                    UserPatch.Online online = patch.getOnline();
                    cv.put(UserColumns.ONLINE, online.isOnline());
                    cv.put(UserColumns.LAST_SEEN, online.getLastSeen());
                    cv.put(UserColumns.PLATFORM, online.getPlatform());
                }

                if (cv.size() > 0) {
                    operations.add(ContentProviderOperation.newUpdate(uri)
                            .withValues(cv)
                            .withSelection(BaseColumns._ID + " = ?", new String[]{String.valueOf(patch.getUserId())})
                            .build());
                }
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<Map<Integer, FriendListEntity>> findFriendsListsByIds(int accountId, int userId, Collection<Integer> ids) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getFriendListsContentUriFor(accountId);

            String where = FriendListsColumns.USER_ID + " = ? " + " AND " + FriendListsColumns.LIST_ID + " IN(" + join(",", ids) + ")";
            String[] args = {String.valueOf(userId)};

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            @SuppressLint("UseSparseArrays")
            Map<Integer, FriendListEntity> map = new HashMap<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    FriendListEntity dbo = mapFriendsList(cursor);
                    map.put(dbo.getId(), dbo);
                }

                cursor.close();
            }

            emitter.onSuccess(map);
        });
    }

    @Override
    public Maybe<String> getLocalizedUserActivity(int accountId, int userId) {
        return Maybe.create(e -> {
            String[] uProjection = {UserColumns.LAST_SEEN, UserColumns.ONLINE, UserColumns.SEX};
            Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);
            String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(userId)};
            Cursor cursor = getContext().getContentResolver().query(uri, uProjection, where, args, null);

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    boolean online = cursor.getInt(cursor.getColumnIndex(UserColumns.ONLINE)) == 1;
                    long lastSeen = cursor.getLong(cursor.getColumnIndex(UserColumns.LAST_SEEN));
                    int sex = cursor.getInt(cursor.getColumnIndex(UserColumns.SEX));
                    String userActivityLine = UserInfoResolveUtil.getUserActivityLine(getContext(), lastSeen, online, sex, false);

                    if (nonNull(userActivityLine)) {
                        e.onSuccess(userActivityLine);
                    }
                }

                cursor.close();
            }

            e.onComplete();
        });
    }

    @Override
    public Single<Optional<UserEntity>> findUserDboById(int accountId, int ownerId) {
        return Single.create(emitter -> {
            final String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(ownerId)};
            Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            UserEntity dbo = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    dbo = mapUserDbo(cursor);
                }

                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(dbo));
        });
    }

    @Override
    public Single<Optional<CommunityEntity>> findCommunityDboById(int accountId, int ownerId) {
        return Single.create(emitter -> {
            final String where = BaseColumns._ID + " = ?";
            String[] args = {String.valueOf(ownerId)};
            Uri uri = MessengerContentProvider.getGroupsContentUriFor(accountId);

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            CommunityEntity dbo = null;

            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    dbo = mapCommunityDbo(cursor);
                }

                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(dbo));
        });
    }

    @Override
    public Single<Optional<UserEntity>> findUserByDomain(int accountId, String domain) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);
            String where = UserColumns.DOMAIN + " LIKE ?";
            String[] args = {domain};
            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            UserEntity entity = null;
            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    entity = mapUserDbo(cursor);
                }
                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(entity));
        });
    }

    @Override
    public Single<Optional<CommunityEntity>> findCommunityByDomain(int accountId, String domain) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getGroupsContentUriFor(accountId);
            String where = GroupColumns.SCREEN_NAME + " LIKE ?";
            String[] args = {domain};

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);

            CommunityEntity entity = null;
            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    entity = mapCommunityDbo(cursor);
                }
                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(entity));
        });
    }

    @Override
    public Single<List<UserEntity>> findUserDbosByIds(int accountId, List<Integer> ids) {
        if (ids.isEmpty()) {
            return Single.just(Collections.emptyList());
        }

        return Single.create(emitter -> {
            String where;
            String[] args;
            Uri uri = MessengerContentProvider.getUserContentUriFor(accountId);

            if (ids.size() == 1) {
                where = BaseColumns._ID + " = ?";
                args = new String[]{String.valueOf(ids.get(0))};
            } else {
                where = BaseColumns._ID + " IN (" + join(",", ids) + ")";
                args = null;
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null, null);

            List<UserEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapUserDbo(cursor));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Single<List<CommunityEntity>> findCommunityDbosByIds(int accountId, List<Integer> ids) {
        if (ids.isEmpty()) {
            return Single.just(Collections.emptyList());
        }

        return Single.create(emitter -> {
            String where;
            String[] args;
            Uri uri = MessengerContentProvider.getGroupsContentUriFor(accountId);

            if (ids.size() == 1) {
                where = BaseColumns._ID + " = ?";
                args = new String[]{String.valueOf(ids.get(0))};
            } else {
                where = BaseColumns._ID + " IN (" + join(",", ids) + ")";
                args = null;
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null, null);

            List<CommunityEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapCommunityDbo(cursor));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Completable storeUserDbos(int accountId, List<UserEntity> users) {
        return Completable.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(users.size());
            appendUsersInsertOperation(operations, accountId, users);
            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Completable storeOwnerEntities(int accountId, OwnerEntities entities) {
        return Completable.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(entities.size());
            appendUsersInsertOperation(operations, accountId, entities.getUserEntities());
            appendCommunitiesInsertOperation(operations, accountId, entities.getCommunityEntities());
            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Completable storeCommunityDbos(int accountId, List<CommunityEntity> communityEntities) {
        return Completable.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(communityEntities.size());
            appendCommunitiesInsertOperation(operations, accountId, communityEntities);
            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<Collection<Integer>> getMissingUserIds(int accountId, @NonNull Collection<Integer> ids) {
        return Single.create(e -> {
            if (ids.isEmpty()) {
                e.onSuccess(Collections.emptyList());
                return;
            }

            Set<Integer> copy = new HashSet<>(ids);
            String[] projection = {BaseColumns._ID};
            Cursor cursor = getContentResolver().query(MessengerContentProvider.getUserContentUriFor(accountId),
                    projection, BaseColumns._ID + " IN ( " + join(",", copy) + ")", null, null);

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    copy.remove(id);
                }

                cursor.close();
            }

            e.onSuccess(copy);
        });
    }

    @Override
    public Single<Collection<Integer>> getMissingCommunityIds(int accountId, @NonNull Collection<Integer> ids) {
        return Single.create(e -> {
            if (ids.isEmpty()) {
                e.onSuccess(Collections.emptyList());
                return;
            }

            Set<Integer> copy = new HashSet<>(ids);
            String[] projection = {BaseColumns._ID};
            Cursor cursor = getContentResolver().query(MessengerContentProvider.getGroupsContentUriFor(accountId),
                    projection, BaseColumns._ID + " IN ( " + join(",", copy) + ")", null, null);

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    copy.remove(id);
                }

                cursor.close();
            }

            e.onSuccess(copy);
        });
    }

    private FriendListEntity mapFriendsList(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(FriendListsColumns.LIST_ID));
        String name = cursor.getString(cursor.getColumnIndex(FriendListsColumns.NAME));
        return new FriendListEntity(id, name);
    }
}