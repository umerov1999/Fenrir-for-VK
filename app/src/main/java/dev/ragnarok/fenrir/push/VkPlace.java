package dev.ragnarok.fenrir.push;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VkPlace {

    //+ wall_comment26632922_4630
    //+ wall25651989_3738
    //+ photo25651989_415613803
    //+ wall_comment-88914001_50005
    //+ photo_comment246484771_456239032
    //+ video25651989_171388574

    private static final Pattern PATTERN_PHOTO = Pattern.compile("photo(-?\\d+)_(\\d+)");
    private static final Pattern PATTERN_PHOTO_COMMENT = Pattern.compile("photo_comment(-?\\d+)_(\\d+)");

    private static final Pattern PATTERN_VIDEO = Pattern.compile("video(-?\\d+)_(\\d+)");
    private static final Pattern PATTERN_VIDEO_COMMENT = Pattern.compile("video_comment(-?\\d+)_(\\d+)");

    private static final Pattern PATTERN_WALL = Pattern.compile("wall(-?\\d+)_(\\d+)");
    private static final Pattern PATTERN_WALL_COMMENT = Pattern.compile("wall_comment(-?\\d+)_(\\d+)");

    public static VkPlace parse(String object) {
        Matcher matcher = PATTERN_PHOTO.matcher(object);
        if (matcher.find()) {
            int ownerId = Integer.parseInt(matcher.group(1));
            int photoId = Integer.parseInt(matcher.group(2));
            return new Photo(ownerId, photoId);
        }

        matcher = PATTERN_PHOTO_COMMENT.matcher(object);
        if (matcher.find()) {
            int ownerId = Integer.parseInt(matcher.group(1));
            int photoId = Integer.parseInt(matcher.group(2));
            return new PhotoComment(ownerId, photoId);
        }

        matcher = PATTERN_WALL.matcher(object);
        if (matcher.find()) {
            int ownerId = Integer.parseInt(matcher.group(1));
            int postId = Integer.parseInt(matcher.group(2));
            return new WallPost(ownerId, postId);
        }

        matcher = PATTERN_WALL_COMMENT.matcher(object);
        if (matcher.find()) {
            int ownerId = Integer.parseInt(matcher.group(1));
            int postId = Integer.parseInt(matcher.group(2));
            return new WallComment(ownerId, postId);
        }

        matcher = PATTERN_VIDEO.matcher(object);
        if (matcher.find()) {
            int ownerId = Integer.parseInt(matcher.group(1));
            int videoId = Integer.parseInt(matcher.group(2));
            return new Video(ownerId, videoId);
        }

        matcher = PATTERN_VIDEO_COMMENT.matcher(object);
        if (matcher.find()) {
            int ownerId = Integer.parseInt(matcher.group(1));
            int videoId = Integer.parseInt(matcher.group(2));
            return new VideoComment(ownerId, videoId);
        }

        return null;
    }

    public static class Photo extends VkPlace {

        private final int ownerId;

        private final int photoId;

        public Photo(int ownerId, int photoId) {
            this.ownerId = ownerId;
            this.photoId = photoId;
        }

        @NonNull
        @Override
        public String toString() {
            return "Photo{" +
                    "ownerId=" + ownerId +
                    ", photoId=" + photoId +
                    '}';
        }

        public int getOwnerId() {
            return ownerId;
        }

        public int getPhotoId() {
            return photoId;
        }
    }

    public static class PhotoComment extends VkPlace {

        private final int ownerId;

        private final int photoId;

        public PhotoComment(int ownerId, int photoId) {
            this.ownerId = ownerId;
            this.photoId = photoId;
        }

        @NonNull
        @Override
        public String toString() {
            return "PhotoComment{" +
                    "ownerId=" + ownerId +
                    ", photoId=" + photoId +
                    '}';
        }

        public int getOwnerId() {
            return ownerId;
        }

        public int getPhotoId() {
            return photoId;
        }
    }

    public static class WallComment extends VkPlace {

        private final int ownerId;

        private final int postId;

        public WallComment(int ownerId, int postId) {
            this.ownerId = ownerId;
            this.postId = postId;
        }

        @NonNull
        @Override
        public String toString() {
            return "WallComment{" +
                    "ownerId=" + ownerId +
                    ", postId=" + postId +
                    '}';
        }

        public int getOwnerId() {
            return ownerId;
        }

        public int getPostId() {
            return postId;
        }
    }

    public static class WallPost extends VkPlace {

        private final int ownerId;

        private final int postId;

        public WallPost(int ownerId, int postId) {
            this.ownerId = ownerId;
            this.postId = postId;
        }

        @NonNull
        @Override
        public String toString() {
            return "WallPost{" +
                    "ownerId=" + ownerId +
                    ", postId=" + postId +
                    '}';
        }

        public int getOwnerId() {
            return ownerId;
        }

        public int getPostId() {
            return postId;
        }
    }

    public static class Video extends VkPlace {

        private final int ownerId;

        private final int videoId;

        public Video(int ownerId, int videoId) {
            this.ownerId = ownerId;
            this.videoId = videoId;
        }

        @NonNull
        @Override
        public String toString() {
            return "Video{" +
                    "ownerId=" + ownerId +
                    ", videoId=" + videoId +
                    '}';
        }

        public int getOwnerId() {
            return ownerId;
        }

        public int getVideoId() {
            return videoId;
        }
    }

    public static class VideoComment extends VkPlace {

        private final int ownerId;

        private final int videoId;

        public VideoComment(int ownerId, int videoId) {
            this.ownerId = ownerId;
            this.videoId = videoId;
        }

        @NonNull
        @Override
        public String toString() {
            return "VideoComment{" +
                    "ownerId=" + ownerId +
                    ", videoId=" + videoId +
                    '}';
        }

        public int getVideoId() {
            return videoId;
        }

        public int getOwnerId() {
            return ownerId;
        }
    }
}