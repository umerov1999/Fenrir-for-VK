package dev.ragnarok.fenrir.fragment.wallattachments;

import androidx.fragment.app.Fragment;

import dev.ragnarok.fenrir.util.FindAttachmentType;

public class WallAttachmentsFragmentFactory {

    public static Fragment newInstance(int accountId, int ownerId, String type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cant bee null");
        }

        Fragment fragment = null;
        switch (type) {
            case FindAttachmentType.TYPE_PHOTO:
                fragment = WallPhotosAttachmentsFragment.newInstance(accountId, ownerId);
                break;
            case FindAttachmentType.TYPE_VIDEO:
                fragment = WallVideosAttachmentsFragment.newInstance(accountId, ownerId);
                break;
            case FindAttachmentType.TYPE_DOC:
                fragment = WallDocsAttachmentsFragment.newInstance(accountId, ownerId);
                break;
            case FindAttachmentType.TYPE_LINK:
                fragment = WallLinksAttachmentsFragment.newInstance(accountId, ownerId);
                break;
            case FindAttachmentType.TYPE_AUDIO:
                fragment = WallAudiosAttachmentsFragment.newInstance(accountId, ownerId);
                break;
            case FindAttachmentType.TYPE_POST_WITH_COMMENT:
                fragment = WallPostCommentAttachmentsFragment.newInstance(accountId, ownerId);
                break;
            case FindAttachmentType.TYPE_ALBUM:
                fragment = WallPhotoAlbumAttachmentsFragment.newInstance(accountId, ownerId);
                break;
            case FindAttachmentType.TYPE_POST_WITH_QUERY:
                fragment = WallPostQueryAttachmentsFragment.newInstance(accountId, ownerId);
                break;
        }

        return fragment;
    }
}
