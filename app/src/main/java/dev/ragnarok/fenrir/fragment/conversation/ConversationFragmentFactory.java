package dev.ragnarok.fenrir.fragment.conversation;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.util.FindAttachmentType;

public class ConversationFragmentFactory {

    public static Fragment newInstance(Bundle args) {
        String type = args.getString(Extra.TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Type cant bee null");
        }

        Fragment fragment = null;
        switch (type) {
            case FindAttachmentType.TYPE_PHOTO:
                fragment = new ConversationPhotosFragment();
                break;
            case FindAttachmentType.TYPE_VIDEO:
                fragment = new ConversationVideosFragment();
                break;
            case FindAttachmentType.TYPE_DOC:
                fragment = new ConversationDocsFragment();
                break;
            case FindAttachmentType.TYPE_AUDIO:
                fragment = new ConversationAudiosFragment();
                break;
            case FindAttachmentType.TYPE_LINK:
                fragment = new ConversationLinksFragment();
                break;
            case FindAttachmentType.TYPE_POST:
                fragment = new ConversationPostsFragment();
                break;
        }

        if (fragment != null) {
            fragment.setArguments(args);
        }

        return fragment;
    }

    public static Bundle buildArgs(int accountId, int peerId, String type) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putInt(Extra.PEER_ID, peerId);
        bundle.putString(Extra.TYPE, type);
        return bundle;
    }
}
