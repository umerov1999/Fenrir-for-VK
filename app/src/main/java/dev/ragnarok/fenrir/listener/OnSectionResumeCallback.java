package dev.ragnarok.fenrir.listener;

import dev.ragnarok.fenrir.model.drawer.SectionMenuItem;

public interface OnSectionResumeCallback {
    void onSectionResume(SectionMenuItem sectionDrawerItem);

    void onChatResume(int accountId, int peerId, String title, String imgUrl);

    void onClearSelection();

    void readAllNotifications();
}
