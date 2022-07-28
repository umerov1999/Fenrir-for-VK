package dev.ragnarok.fenrir.model.feedback

import android.os.Parcel
import android.os.Parcelable

class ParcelableFeedbackWrapper : Parcelable {
    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableFeedbackWrapper> =
            object : Parcelable.Creator<ParcelableFeedbackWrapper> {
                override fun createFromParcel(`in`: Parcel): ParcelableFeedbackWrapper {
                    return ParcelableFeedbackWrapper(`in`)
                }

                override fun newArray(size: Int): Array<ParcelableFeedbackWrapper?> {
                    return arrayOfNulls(size)
                }
            }
        private val TYPES: MutableList<Class<*>> = ArrayList()

        init {
            TYPES.add(CommentFeedback::class.java)
            TYPES.add(CopyFeedback::class.java)
            TYPES.add(LikeCommentFeedback::class.java)
            TYPES.add(LikeFeedback::class.java)
            TYPES.add(MentionCommentFeedback::class.java)
            TYPES.add(MentionFeedback::class.java)
            TYPES.add(PostPublishFeedback::class.java)
            TYPES.add(ReplyCommentFeedback::class.java)
            TYPES.add(UsersFeedback::class.java)
        }
    }

    private val feedback: Feedback?

    constructor(feedback: Feedback?) {
        this.feedback = feedback
    }

    internal constructor(`in`: Parcel) {
        val index = `in`.readInt()
        val classLoader = TYPES[index].classLoader
        feedback = `in`.readParcelable(classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        val index = TYPES.indexOf((feedback ?: return).javaClass)
        if (index == -1) {
            throw UnsupportedOperationException("Unsupported class: " + feedback.javaClass)
        }
        dest.writeInt(index)
        dest.writeParcelable(feedback, flags)
    }

    fun get(): Feedback? {
        return feedback
    }
}