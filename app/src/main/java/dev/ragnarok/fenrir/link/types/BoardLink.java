package dev.ragnarok.fenrir.link.types;

public class BoardLink extends AbsLink {

    private final int groupId;

    public BoardLink(int groupId) {
        super(BOARD);
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }
}