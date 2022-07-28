package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable

class Poll : AbsModel {
    val id: Int
    val ownerId: Int
    var creationTime: Long = 0
        private set
    var question: String? = null
        private set
    var voteCount = 0
        private set
    var myAnswerIds: IntArray? = null
        private set
    var isAnonymous = false
        private set
    var answers: MutableList<Answer>? = null
        private set
    var isBoard = false
        private set
    var isClosed = false
        private set
    var authorId = 0
        private set
    var isCanVote = false
        private set
    var isCanEdit = false
        private set
    var isCanReport = false
        private set
    var isCanShare = false
        private set
    var endDate: Long = 0
        private set
    var isMultiple = false
        private set
    var photo: String? = null
        private set

    internal constructor(`in`: Parcel) : super(`in`) {
        id = `in`.readInt()
        ownerId = `in`.readInt()
        creationTime = `in`.readLong()
        question = `in`.readString()
        voteCount = `in`.readInt()
        myAnswerIds = `in`.createIntArray()
        isAnonymous = `in`.readByte().toInt() != 0
        answers = `in`.createTypedArrayList(Answer.CREATOR)
        isBoard = `in`.readByte().toInt() != 0
        isClosed = `in`.readByte().toInt() != 0
        authorId = `in`.readInt()
        isCanVote = `in`.readByte().toInt() != 0
        isCanEdit = `in`.readByte().toInt() != 0
        isCanReport = `in`.readByte().toInt() != 0
        isCanShare = `in`.readByte().toInt() != 0
        endDate = `in`.readLong()
        isMultiple = `in`.readByte().toInt() != 0
        photo = `in`.readString()
    }

    constructor(id: Int, ownerId: Int) {
        this.id = id
        this.ownerId = ownerId
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        super.writeToParcel(parcel, i)
        parcel.writeInt(id)
        parcel.writeInt(ownerId)
        parcel.writeLong(creationTime)
        parcel.writeString(question)
        parcel.writeInt(voteCount)
        parcel.writeIntArray(myAnswerIds)
        parcel.writeByte((if (isAnonymous) 1 else 0).toByte())
        parcel.writeTypedList(answers)
        parcel.writeByte((if (isBoard) 1 else 0).toByte())
        parcel.writeByte((if (isClosed) 1 else 0).toByte())
        parcel.writeInt(authorId)
        parcel.writeByte((if (isCanVote) 1 else 0).toByte())
        parcel.writeByte((if (isCanEdit) 1 else 0).toByte())
        parcel.writeByte((if (isCanReport) 1 else 0).toByte())
        parcel.writeByte((if (isCanShare) 1 else 0).toByte())
        parcel.writeLong(endDate)
        parcel.writeByte((if (isMultiple) 1 else 0).toByte())
        parcel.writeString(photo)
    }

    fun setClosed(closed: Boolean): Poll {
        isClosed = closed
        return this
    }

    fun setAuthorId(authorId: Int): Poll {
        this.authorId = authorId
        return this
    }

    fun setCanVote(canVote: Boolean): Poll {
        isCanVote = canVote
        return this
    }

    fun setCanEdit(canEdit: Boolean): Poll {
        isCanEdit = canEdit
        return this
    }

    fun setCanReport(canReport: Boolean): Poll {
        isCanReport = canReport
        return this
    }

    fun setCanShare(canShare: Boolean): Poll {
        isCanShare = canShare
        return this
    }

    fun setEndDate(endDate: Long): Poll {
        this.endDate = endDate
        return this
    }

    fun setMultiple(multiple: Boolean): Poll {
        isMultiple = multiple
        return this
    }

    fun setCreationTime(creationTime: Long): Poll {
        this.creationTime = creationTime
        return this
    }

    fun setQuestion(question: String?): Poll {
        this.question = question
        return this
    }

    fun setPhoto(photo: String?): Poll {
        this.photo = photo
        return this
    }

    fun setVoteCount(voteCount: Int): Poll {
        this.voteCount = voteCount
        return this
    }

    fun setMyAnswerIds(myAnswerIds: IntArray?): Poll {
        this.myAnswerIds = myAnswerIds
        return this
    }

    fun setAnonymous(anonymous: Boolean): Poll {
        isAnonymous = anonymous
        return this
    }

    fun setAnswers(answers: MutableList<Answer>?): Poll {
        this.answers = answers
        return this
    }

    fun setBoard(board: Boolean): Poll {
        isBoard = board
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    class Answer : AbsModel {
        val id: Int
        var text: String? = null
            private set
        var voteCount = 0
            private set
        var rate = 0.0
            private set

        constructor(id: Int) {
            this.id = id
        }

        internal constructor(`in`: Parcel) : super(`in`) {
            id = `in`.readInt()
            text = `in`.readString()
            voteCount = `in`.readInt()
            rate = `in`.readDouble()
        }

        override fun writeToParcel(parcel: Parcel, i: Int) {
            super.writeToParcel(parcel, i)
            parcel.writeInt(id)
            parcel.writeString(text)
            parcel.writeInt(voteCount)
            parcel.writeDouble(rate)
        }

        fun setText(text: String?): Answer {
            this.text = text
            return this
        }

        fun setVoteCount(voteCount: Int): Answer {
            this.voteCount = voteCount
            return this
        }

        fun setRate(rate: Double): Answer {
            this.rate = rate
            return this
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Answer> {
            override fun createFromParcel(parcel: Parcel): Answer {
                return Answer(parcel)
            }

            override fun newArray(size: Int): Array<Answer?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object CREATOR : Parcelable.Creator<Poll> {
        override fun createFromParcel(parcel: Parcel): Poll {
            return Poll(parcel)
        }

        override fun newArray(size: Int): Array<Poll?> {
            return arrayOfNulls(size)
        }
    }
}