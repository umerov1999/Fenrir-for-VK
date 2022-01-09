package dev.ragnarok.fenrir.db.model.entity;


public class CommunityEntity {

    private final int id;

    private String name;

    private String screenName;

    private int closed;

    private boolean admin;

    private int adminLevel;

    private boolean member;

    private int membersCount;

    private int memberStatus;

    private int type;

    private String photo50;

    private String photo100;

    private String photo200;

    private boolean verified;

    public CommunityEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CommunityEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getScreenName() {
        return screenName;
    }

    public CommunityEntity setScreenName(String screenName) {
        this.screenName = screenName;
        return this;
    }

    public int getClosed() {
        return closed;
    }

    public CommunityEntity setClosed(int closed) {
        this.closed = closed;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public CommunityEntity setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public int getAdminLevel() {
        return adminLevel;
    }

    public CommunityEntity setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
        return this;
    }

    public boolean isMember() {
        return member;
    }

    public CommunityEntity setMember(boolean member) {
        this.member = member;
        return this;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public CommunityEntity setMembersCount(int membersCount) {
        this.membersCount = membersCount;
        return this;
    }

    public int getMemberStatus() {
        return memberStatus;
    }

    public CommunityEntity setMemberStatus(int memberStatus) {
        this.memberStatus = memberStatus;
        return this;
    }

    public int getType() {
        return type;
    }

    public CommunityEntity setType(int type) {
        this.type = type;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public CommunityEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public CommunityEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public CommunityEntity setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public boolean isVerified() {
        return verified;
    }

    public CommunityEntity setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}