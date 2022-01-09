package dev.ragnarok.fenrir.domain;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.CommentIntent;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentsBundle;
import dev.ragnarok.fenrir.model.DraftComment;
import dev.ragnarok.fenrir.model.Owner;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public interface ICommentsInteractor {

    Single<List<Comment>> getAllCachedData(int accountId, @NonNull Commented commented);

    Single<CommentsBundle> getCommentsPortion(int accountId, @NonNull Commented commented, int offset,
                                              int count, Integer startCommentId, Integer threadComment, boolean invalidateCache, String sort);

    Single<List<Comment>> getCommentsNoCache(int accountId, int ownerId, int postId, int offset);

    Maybe<DraftComment> restoreDraftComment(int accountId, @NonNull Commented commented);

    Single<Integer> safeDraftComment(int accountId, @NonNull Commented commented, String body, int replyToCommentId, int replyToUserId);

    Completable like(int accountId, Commented commented, int commentId, boolean add);

    Single<Integer> checkAndAddLike(int accountId, Commented commented, int commentId);

    Single<Boolean> isLiked(int accountId, Commented commented, int commentId);

    Completable deleteRestore(int accountId, Commented commented, int commentId, boolean delete);

    Single<Comment> send(int accountId, Commented commented, Integer commentThread, CommentIntent intent);

    Single<List<Comment>> getAllCommentsRange(int accountId, Commented commented, int startFromCommentId, int continueToCommentId);

    Single<List<Owner>> getAvailableAuthors(int accountId);

    Single<Comment> edit(int accountId, Commented commented, int commentId, String body, Integer commentThread, List<AbsModel> attachments);

    Single<Integer> reportComment(int accountId, int owner_id, int post_id, int reason);
}