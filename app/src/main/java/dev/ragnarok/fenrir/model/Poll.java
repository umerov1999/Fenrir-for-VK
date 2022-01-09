package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import java.util.List;


public class Poll extends AbsModel {

    public static final Creator<Poll> CREATOR = new Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel in) {
            return new Poll(in);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };
    private final int id;
    private final int ownerId;
    private long creationTime;
    private String question;
    private int voteCount;
    private int[] myAnswerIds;
    private boolean anonymous;
    private List<Answer> answers;
    private boolean board;
    private boolean closed;
    private int authorId;
    private boolean canVote;
    private boolean canEdit;
    private boolean canReport;
    private boolean canShare;
    private long endDate;
    private boolean multiple;
    private String photo;

    protected Poll(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        creationTime = in.readLong();
        question = in.readString();
        voteCount = in.readInt();
        myAnswerIds = in.createIntArray();
        anonymous = in.readByte() != 0;
        answers = in.createTypedArrayList(Answer.CREATOR);
        board = in.readByte() != 0;
        closed = in.readByte() != 0;
        authorId = in.readInt();
        canVote = in.readByte() != 0;
        canEdit = in.readByte() != 0;
        canReport = in.readByte() != 0;
        canShare = in.readByte() != 0;
        endDate = in.readLong();
        multiple = in.readByte() != 0;
        photo = in.readString();
    }

    public Poll(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeLong(creationTime);
        dest.writeString(question);
        dest.writeInt(voteCount);
        dest.writeIntArray(myAnswerIds);
        dest.writeByte((byte) (anonymous ? 1 : 0));
        dest.writeTypedList(answers);
        dest.writeByte((byte) (board ? 1 : 0));
        dest.writeByte((byte) (closed ? 1 : 0));
        dest.writeInt(authorId);
        dest.writeByte((byte) (canVote ? 1 : 0));
        dest.writeByte((byte) (canEdit ? 1 : 0));
        dest.writeByte((byte) (canReport ? 1 : 0));
        dest.writeByte((byte) (canShare ? 1 : 0));
        dest.writeLong(endDate);
        dest.writeByte((byte) (multiple ? 1 : 0));
        dest.writeString(photo);
    }

    public boolean isClosed() {
        return closed;
    }

    public Poll setClosed(boolean closed) {
        this.closed = closed;
        return this;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Poll setAuthorId(int authorId) {
        this.authorId = authorId;
        return this;
    }

    public boolean isCanVote() {
        return canVote;
    }

    public Poll setCanVote(boolean canVote) {
        this.canVote = canVote;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public Poll setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isCanReport() {
        return canReport;
    }

    public Poll setCanReport(boolean canReport) {
        this.canReport = canReport;
        return this;
    }

    public boolean isCanShare() {
        return canShare;
    }

    public Poll setCanShare(boolean canShare) {
        this.canShare = canShare;
        return this;
    }

    public long getEndDate() {
        return endDate;
    }

    public Poll setEndDate(long endDate) {
        this.endDate = endDate;
        return this;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public Poll setMultiple(boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Poll setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getQuestion() {
        return question;
    }

    public Poll setQuestion(String question) {
        this.question = question;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public Poll setPhoto(String photo) {
        this.photo = photo;
        return this;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public Poll setVoteCount(int voteCount) {
        this.voteCount = voteCount;
        return this;
    }

    public int[] getMyAnswerIds() {
        return myAnswerIds;
    }

    public Poll setMyAnswerIds(int[] myAnswerIds) {
        this.myAnswerIds = myAnswerIds;
        return this;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public Poll setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public Poll setAnswers(List<Answer> answers) {
        this.answers = answers;
        return this;
    }

    public boolean isBoard() {
        return board;
    }

    public Poll setBoard(boolean board) {
        this.board = board;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class Answer extends AbsModel {

        public static final Creator<Answer> CREATOR = new Creator<Answer>() {
            @Override
            public Answer createFromParcel(Parcel in) {
                return new Answer(in);
            }

            @Override
            public Answer[] newArray(int size) {
                return new Answer[size];
            }
        };
        private final int id;
        private String text;
        private int voteCount;
        private double rate;

        public Answer(int id) {
            this.id = id;
        }

        protected Answer(Parcel in) {
            super(in);
            id = in.readInt();
            text = in.readString();
            voteCount = in.readInt();
            rate = in.readDouble();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(id);
            dest.writeString(text);
            dest.writeInt(voteCount);
            dest.writeDouble(rate);
        }

        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public Answer setText(String text) {
            this.text = text;
            return this;
        }

        public int getVoteCount() {
            return voteCount;
        }

        public Answer setVoteCount(int voteCount) {
            this.voteCount = voteCount;
            return this;
        }

        public double getRate() {
            return rate;
        }

        public Answer setRate(double rate) {
            this.rate = rate;
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}