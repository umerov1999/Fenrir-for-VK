package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import dev.ragnarok.fenrir.model.database.Country;

public final class UserDetails implements Parcelable {

    public static final Creator<UserDetails> CREATOR = new Creator<UserDetails>() {
        @Override
        public UserDetails createFromParcel(Parcel in) {
            return new UserDetails(in);
        }

        @Override
        public UserDetails[] newArray(int size) {
            return new UserDetails[size];
        }
    };
    private IdPair photoId;
    private Audio statusAudio;
    private boolean isFavorite;
    private boolean isSubscribed;
    private int friendsCount;
    private int onlineFriendsCount;
    private int mutualFriendsCount;
    private int followersCount;
    private int groupsCount;
    private int photosCount;
    private int audiosCount;
    private int articlesCount;
    private int productsCount;
    private int videosCount;
    private int allWallCount;
    private int ownWallCount;
    private int postponedWallCount;
    private int GiftCount;
    private String bdate;
    private City city;
    private Country country;
    private String hometown;
    private String phone;
    private String homePhone;
    private String skype;
    private String instagram;
    private String twitter;
    private String facebook;
    private List<Career> careers;
    private List<Military> militaries;
    private List<University> universities;
    private List<School> schools;
    private List<Relative> relatives;
    private int relation;
    private Owner relationPartner;
    private String[] languages;
    private int political;
    private int peopleMain;
    private int lifeMain;
    private int smoking;
    private int alcohol;
    private String inspiredBy;
    private String religion;
    private String site;
    private String interests;
    private String music;
    private String activities;
    private String movies;
    private String tv;
    private String games;
    private String quotes;
    private String about;
    private String books;

    public UserDetails() {

    }

    private UserDetails(Parcel in) {
        photoId = in.readParcelable(IdPair.class.getClassLoader());
        statusAudio = in.readParcelable(Audio.class.getClassLoader());
        friendsCount = in.readInt();
        onlineFriendsCount = in.readInt();
        mutualFriendsCount = in.readInt();
        followersCount = in.readInt();
        groupsCount = in.readInt();
        photosCount = in.readInt();
        audiosCount = in.readInt();
        videosCount = in.readInt();
        articlesCount = in.readInt();
        productsCount = in.readInt();
        allWallCount = in.readInt();
        ownWallCount = in.readInt();
        postponedWallCount = in.readInt();
        GiftCount = in.readInt();
        bdate = in.readString();
        isFavorite = in.readByte() == 1;
        isSubscribed = in.readByte() == 1;
    }

    public String getInterests() {
        return interests;
    }

    public UserDetails setInterests(String interests) {
        this.interests = interests;
        return this;
    }

    public String getMusic() {
        return music;
    }

    public UserDetails setMusic(String music) {
        this.music = music;
        return this;
    }

    public boolean isSetFavorite() {
        return isFavorite;
    }

    public UserDetails setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    public boolean isSetSubscribed() {
        return isSubscribed;
    }

    public UserDetails setSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
        return this;
    }

    public String getActivities() {
        return activities;
    }

    public UserDetails setActivities(String activities) {
        this.activities = activities;
        return this;
    }

    public String getMovies() {
        return movies;
    }

    public UserDetails setMovies(String movies) {
        this.movies = movies;
        return this;
    }

    public String getTv() {
        return tv;
    }

    public UserDetails setTv(String tv) {
        this.tv = tv;
        return this;
    }

    public String getGames() {
        return games;
    }

    public UserDetails setGames(String games) {
        this.games = games;
        return this;
    }

    public String getQuotes() {
        return quotes;
    }

    public UserDetails setQuotes(String quotes) {
        this.quotes = quotes;
        return this;
    }

    public String getAbout() {
        return about;
    }

    public UserDetails setAbout(String about) {
        this.about = about;
        return this;
    }

    public String getBooks() {
        return books;
    }

    public UserDetails setBooks(String books) {
        this.books = books;
        return this;
    }

    public String getSite() {
        return site;
    }

    public UserDetails setSite(String site) {
        this.site = site;
        return this;
    }

    public int getAlcohol() {
        return alcohol;
    }

    public UserDetails setAlcohol(int alcohol) {
        this.alcohol = alcohol;
        return this;
    }

    public int getLifeMain() {
        return lifeMain;
    }

    public UserDetails setLifeMain(int lifeMain) {
        this.lifeMain = lifeMain;
        return this;
    }

    public int getPeopleMain() {
        return peopleMain;
    }

    public UserDetails setPeopleMain(int peopleMain) {
        this.peopleMain = peopleMain;
        return this;
    }

    public int getPolitical() {
        return political;
    }

    public UserDetails setPolitical(int political) {
        this.political = political;
        return this;
    }

    public int getSmoking() {
        return smoking;
    }

    public UserDetails setSmoking(int smoking) {
        this.smoking = smoking;
        return this;
    }

    public String getInspiredBy() {
        return inspiredBy;
    }

    public UserDetails setInspiredBy(String inspiredBy) {
        this.inspiredBy = inspiredBy;
        return this;
    }

    public String getReligion() {
        return religion;
    }

    public UserDetails setReligion(String religion) {
        this.religion = religion;
        return this;
    }

    public String[] getLanguages() {
        return languages;
    }

    public UserDetails setLanguages(String[] languages) {
        this.languages = languages;
        return this;
    }

    public int getRelation() {
        return relation;
    }

    public UserDetails setRelation(int relation) {
        this.relation = relation;
        return this;
    }

    public Owner getRelationPartner() {
        return relationPartner;
    }

    public UserDetails setRelationPartner(Owner relationPartner) {
        this.relationPartner = relationPartner;
        return this;
    }

    public List<Relative> getRelatives() {
        return relatives;
    }

    public UserDetails setRelatives(List<Relative> relatives) {
        this.relatives = relatives;
        return this;
    }

    public List<School> getSchools() {
        return schools;
    }

    public UserDetails setSchools(List<School> schools) {
        this.schools = schools;
        return this;
    }

    public List<University> getUniversities() {
        return universities;
    }

    public UserDetails setUniversities(List<University> universities) {
        this.universities = universities;
        return this;
    }

    public List<Military> getMilitaries() {
        return militaries;
    }

    public UserDetails setMilitaries(List<Military> militaries) {
        this.militaries = militaries;
        return this;
    }

    public List<Career> getCareers() {
        return careers;
    }

    public UserDetails setCareers(List<Career> careers) {
        this.careers = careers;
        return this;
    }

    public String getSkype() {
        return skype;
    }

    public UserDetails setSkype(String skype) {
        this.skype = skype;
        return this;
    }

    public String getInstagram() {
        return instagram;
    }

    public UserDetails setInstagram(String instagram) {
        this.instagram = instagram;
        return this;
    }

    public String getTwitter() {
        return twitter;
    }

    public UserDetails setTwitter(String twitter) {
        this.twitter = twitter;
        return this;
    }

    public String getFacebook() {
        return facebook;
    }

    public UserDetails setFacebook(String facebook) {
        this.facebook = facebook;
        return this;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public UserDetails setHomePhone(String homePhone) {
        this.homePhone = homePhone;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserDetails setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getHometown() {
        return hometown;
    }

    public UserDetails setHometown(String hometown) {
        this.hometown = hometown;
        return this;
    }

    public Country getCountry() {
        return country;
    }

    public UserDetails setCountry(Country country) {
        this.country = country;
        return this;
    }

    public City getCity() {
        return city;
    }

    public UserDetails setCity(City city) {
        this.city = city;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(photoId, i);
        parcel.writeParcelable(statusAudio, i);
        parcel.writeInt(friendsCount);
        parcel.writeInt(onlineFriendsCount);
        parcel.writeInt(mutualFriendsCount);
        parcel.writeInt(followersCount);
        parcel.writeInt(groupsCount);
        parcel.writeInt(photosCount);
        parcel.writeInt(audiosCount);
        parcel.writeInt(videosCount);
        parcel.writeInt(articlesCount);
        parcel.writeInt(productsCount);
        parcel.writeInt(allWallCount);
        parcel.writeInt(ownWallCount);
        parcel.writeInt(postponedWallCount);
        parcel.writeInt(GiftCount);
        parcel.writeString(bdate);
        parcel.writeByte(isFavorite ? (byte) 1 : (byte) 0);
        parcel.writeByte(isSubscribed ? (byte) 1 : (byte) 0);
    }

    public String getBdate() {
        return bdate;
    }

    public UserDetails setBdate(String bdate) {
        this.bdate = bdate;
        return this;
    }

    public IdPair getPhotoId() {
        return photoId;
    }

    public UserDetails setPhotoId(IdPair photoId) {
        this.photoId = photoId;
        return this;
    }

    public Audio getStatusAudio() {
        return statusAudio;
    }

    public UserDetails setStatusAudio(Audio statusAudio) {
        this.statusAudio = statusAudio;
        return this;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public UserDetails setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
        return this;
    }

    public int getOnlineFriendsCount() {
        return onlineFriendsCount;
    }

    public UserDetails setOnlineFriendsCount(int onlineFriendsCount) {
        this.onlineFriendsCount = onlineFriendsCount;
        return this;
    }

    public int getMutualFriendsCount() {
        return mutualFriendsCount;
    }

    public UserDetails setMutualFriendsCount(int mutualFriendsCount) {
        this.mutualFriendsCount = mutualFriendsCount;
        return this;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public UserDetails setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public int getGroupsCount() {
        return groupsCount;
    }

    public UserDetails setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
        return this;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public UserDetails setPhotosCount(int photosCount) {
        this.photosCount = photosCount;
        return this;
    }

    public int getAudiosCount() {
        return audiosCount;
    }

    public UserDetails setAudiosCount(int audiosCount) {
        this.audiosCount = audiosCount;
        return this;
    }

    public int getArticlesCount() {
        return articlesCount;
    }

    public UserDetails setArticlesCount(int articlesCount) {
        this.articlesCount = articlesCount;
        return this;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public UserDetails setProductsCount(int productsCount) {
        this.productsCount = productsCount;
        return this;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public UserDetails setVideosCount(int videosCount) {
        this.videosCount = videosCount;
        return this;
    }

    public int getAllWallCount() {
        return allWallCount;
    }

    public UserDetails setAllWallCount(int allWallCount) {
        this.allWallCount = allWallCount;
        return this;
    }

    public int getOwnWallCount() {
        return ownWallCount;
    }

    public UserDetails setOwnWallCount(int ownWallCount) {
        this.ownWallCount = ownWallCount;
        return this;
    }

    public int getPostponedWallCount() {
        return postponedWallCount;
    }

    public UserDetails setPostponedWallCount(int postponedWallCount) {
        this.postponedWallCount = postponedWallCount;
        return this;
    }

    public int getGiftCount() {
        return GiftCount;
    }

    public UserDetails setGiftCount(int GiftCount) {
        this.GiftCount = GiftCount;
        return this;
    }

    public static final class Relative {

        private User user;

        private String type;

        private String name;

        public String getName() {
            return name;
        }

        public Relative setName(String name) {
            this.name = name;
            return this;
        }

        public User getUser() {
            return user;
        }

        public Relative setUser(User user) {
            this.user = user;
            return this;
        }

        public String getType() {
            return type;
        }

        public Relative setType(String type) {
            this.type = type;
            return this;
        }
    }
}