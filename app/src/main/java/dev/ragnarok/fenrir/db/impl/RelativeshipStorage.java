package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.FriendListsColumns;
import dev.ragnarok.fenrir.db.column.RelationshipColumns;
import dev.ragnarok.fenrir.db.interfaces.IRelativeshipStorage;
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity;
import dev.ragnarok.fenrir.db.model.entity.FriendListEntity;
import dev.ragnarok.fenrir.db.model.entity.UserEntity;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

class RelativeshipStorage extends AbsStorage implements IRelativeshipStorage {

    RelativeshipStorage(@NonNull AppStorages base) {
        super(base);
    }

    private static CommunityEntity mapCommunity(Cursor cursor) {
        return new CommunityEntity(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.SUBJECT_ID)))
                .setName(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_NAME)))
                .setScreenName(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_SCREEN_NAME)))
                .setClosed(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_CLOSED)))
                .setVerified(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_VERIFIED)) == 1)
                .setAdmin(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_ADMIN)) == 1)
                .setAdminLevel(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_ADMIN_LEVEL)))
                .setMember(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_MEMBER)) == 1)
                .setMemberStatus(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBER_STATUS)))
                .setMembersCount(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBERS_COUNT)))
                .setType(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_TYPE)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_200)));
    }

    private static UserEntity mapDbo(Cursor cursor) {
        int gid = Math.abs(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.SUBJECT_ID)));
        return new UserEntity(gid)
                .setFirstName(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_FIRST_NAME)))
                .setLastName(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_NAME)))
                .setOnline(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE)) == 1)
                .setOnlineMobile(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE)) == 1)
                .setOnlineApp(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE_APP)))
                .setPhoto50(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_200)))
                .setPhotoMax(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_MAX)))
                .setLastSeen(cursor.getLong(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_SEEN)))
                .setPlatform(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_PLATFORM)))
                .setStatus(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_STATUS)))
                .setSex(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_SEX)))
                .setFriend(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_IS_FRIEND)) == 1)
                .setFriendStatus(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_USER_FRIEND_STATUS)))
                .setCanWritePrivateMessage(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_WRITE_MESSAGE_STATUS)) == 1)
                .setBlacklisted_by_me(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_IS_USER_BLACK_LIST)) == 1)
                .setBlacklisted(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_IS_BLACK_LISTED)) == 1)
                .setCan_access_closed(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_IS_CAN_ACCESS_CLOSED)) == 1)
                .setVerified(cursor.getInt(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_IS_VERIFIED)) == 1)
                .setMaiden_name(cursor.getString(cursor.getColumnIndex(RelationshipColumns.FOREIGN_SUBJECT_MAIDEN_NAME)));
    }

    @Override
    public Completable storeFriendsList(int accountId, int userId, @NonNull Collection<FriendListEntity> data) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getFriendListsContentUriFor(accountId);
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(data.size());

            operations.add(ContentProviderOperation.newDelete(uri)
                    .withSelection(FriendListsColumns.FULL_USER_ID + " = ?", new String[]{String.valueOf(userId)})
                    .build());

            for (FriendListEntity item : data) {
                ContentValues cv = new ContentValues();
                cv.put(FriendListsColumns.LIST_ID, item.getId());
                cv.put(FriendListsColumns.NAME, item.getName());

                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }

    @Override
    public Completable storeFriends(int accountId, @NonNull List<UserEntity> users, int objectId, boolean clearBeforeStore) {
        return completableStoreForType(accountId, users, objectId, RelationshipColumns.TYPE_FRIEND, clearBeforeStore);
    }

    private Completable completableStoreForType(int accountId, List<UserEntity> userEntities, int objectId, int relationType, boolean clear) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getRelativeshipContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clear) {
                operations.add(clearOperationFor(accountId, objectId, relationType));
            }

            appendInsertHeaders(uri, operations, objectId, userEntities, relationType);
            OwnersStorage.appendUsersInsertOperation(operations, accountId, userEntities);

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Completable storeFollowers(int accountId, @NonNull List<UserEntity> users, int objectId, boolean clearBeforeStore) {
        return completableStoreForType(accountId, users, objectId, RelationshipColumns.TYPE_FOLLOWER, clearBeforeStore);
    }

    @Override
    public Completable storeRequests(int accountId, @NonNull List<UserEntity> users, int objectId, boolean clearBeforeStore) {
        return completableStoreForType(accountId, users, objectId, RelationshipColumns.TYPE_REQUESTS, clearBeforeStore);
    }

    @Override
    public Single<List<UserEntity>> getFriends(int accountId, int objectId) {
        return getUsersForType(accountId, objectId, RelationshipColumns.TYPE_FRIEND);
    }

    @Override
    public Single<List<UserEntity>> getFollowers(int accountId, int objectId) {
        return getUsersForType(accountId, objectId, RelationshipColumns.TYPE_FOLLOWER);
    }

    @Override
    public Single<List<UserEntity>> getRequests(int accountId) {
        return getUsersForType(accountId, accountId, RelationshipColumns.TYPE_REQUESTS);
    }

    @Override
    public Single<List<CommunityEntity>> getCommunities(int accountId, int ownerId) {
        return Single.create(emitter -> {
            Cursor cursor = getCursorForType(accountId, ownerId, RelationshipColumns.TYPE_MEMBER);

            List<CommunityEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (Objects.nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapCommunity(cursor));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Completable storeComminities(int accountId, List<CommunityEntity> communities, int userId, boolean invalidateBefore) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getRelativeshipContentUriFor(accountId);
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(communities.size() * 2 + 1);

            if (invalidateBefore) {
                operations.add(clearOperationFor(accountId, userId, RelationshipColumns.TYPE_MEMBER));
            }

            for (CommunityEntity dbo : communities) {
                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(RelationshipColumns.getCV(userId, -dbo.getId(), RelationshipColumns.TYPE_MEMBER))
                        .build());
            }

            OwnersStorage.appendCommunitiesInsertOperation(operations, accountId, communities);
            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    private Cursor getCursorForType(int accountId, int objectId, int relationType) {
        Uri uri = MessengerContentProvider.getRelativeshipContentUriFor(accountId);

        String where = RelationshipColumns.FULL_TYPE + " = ? AND " + RelationshipColumns.OBJECT_ID + " = ?";
        String[] args = {String.valueOf(relationType), String.valueOf(objectId)};

        return getContentResolver().query(uri, null, where, args, null);
    }

    private Single<List<UserEntity>> getUsersForType(int accountId, int objectId, int relationType) {
        return Single.create(emitter -> {

            Cursor cursor = getCursorForType(accountId, objectId, relationType);
            List<UserEntity> dbos = new ArrayList<>(safeCountOf(cursor));

            if (Objects.nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    dbos.add(mapDbo(cursor));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    private void appendInsertHeaders(Uri uri, List<ContentProviderOperation> operations, int objectId, List<UserEntity> dbos, int type) {
        for (UserEntity dbo : dbos) {
            operations.add(ContentProviderOperation
                    .newInsert(uri)
                    .withValues(RelationshipColumns.getCV(objectId, dbo.getId(), type))
                    .build());
        }
    }

    private ContentProviderOperation clearOperationFor(int accountId, int objectId, int type) {
        Uri uri = MessengerContentProvider.getRelativeshipContentUriFor(accountId);

        String clearWhere = RelationshipColumns.OBJECT_ID + " = ? AND " + RelationshipColumns.TYPE + " = ?";
        String[] clearWhereArgs = {
                String.valueOf(objectId),
                String.valueOf(type)
        };

        return ContentProviderOperation
                .newDelete(uri)
                .withSelection(clearWhere, clearWhereArgs)
                .build();
    }
}