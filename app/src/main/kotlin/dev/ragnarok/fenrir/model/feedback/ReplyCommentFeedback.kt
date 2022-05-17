package dev.ragnarok.fenrir.model.feedback

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.Comment
import dev.ragnarok.fenrir.model.ParcelableModelWrapper.Companion.readModel
import dev.ragnarok.fenrir.model.ParcelableModelWrapper.Companion.writeModel

class ReplyCommentFeedback : Feedback {
    var commentsOf: AbsModel? = null
        private set
    var ownComment: Comment? = null
        private set
    var feedbackComment: Comment? = null
        private set

    constructor(@FeedbackType type: Int) : super(type)
    private constructor(`in`: Parcel) : super(`in`) {
        commentsOf = readModel(`in`)
        ownComment = `in`.readParcelable(Comment::class.java.classLoader)
        feedbackComment = `in`.readParcelable(Comment::class.java.classLoader)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        writeModel(dest, flags, commentsOf)
        dest.writeParcelable(ownComment, flags)
        dest.writeParcelable(feedbackComment, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun setCommentsOf(commentsOf: AbsModel?): ReplyCommentFeedback {
        this.commentsOf = commentsOf
        return this
    }

    fun setOwnComment(ownComment: Comment?): ReplyCommentFeedback {
        this.ownComment = ownComment
        return this
    }

    fun setFeedbackComment(feedbackComment: Comment?): ReplyCommentFeedback {
        this.feedbackComment = feedbackComment
        return this
    }

    companion object CREATOR : Parcelable.Creator<ReplyCommentFeedback> {
        override fun createFromParcel(parcel: Parcel): ReplyCommentFeedback {
            return ReplyCommentFeedback(parcel)
        }

        override fun newArray(size: Int): Array<ReplyCommentFeedback?> {
            return arrayOfNulls(size)
        }
    }
}