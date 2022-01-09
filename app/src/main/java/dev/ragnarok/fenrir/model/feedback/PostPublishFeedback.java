package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;

import dev.ragnarok.fenrir.model.Post;

public final class PostPublishFeedback extends Feedback {

    public static final Creator<PostPublishFeedback> CREATOR = new Creator<PostPublishFeedback>() {
        @Override
        public PostPublishFeedback createFromParcel(Parcel in) {
            return new PostPublishFeedback(in);
        }

        @Override
        public PostPublishFeedback[] newArray(int size) {
            return new PostPublishFeedback[size];
        }
    };
    private Post post;

    public PostPublishFeedback(@FeedbackType int type) {
        super(type);
    }

    private PostPublishFeedback(Parcel in) {
        super(in);
        post = in.readParcelable(Post.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(post, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Post getPost() {
        return post;
    }

    public PostPublishFeedback setPost(Post post) {
        this.post = post;
        return this;
    }
}