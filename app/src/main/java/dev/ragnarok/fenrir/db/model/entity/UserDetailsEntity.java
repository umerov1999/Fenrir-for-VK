package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

import java.util.List;

import dev.ragnarok.fenrir.db.model.IdPairEntity;

@Keep
public class UserDetailsEntity {

    private IdPairEntity photoId;

    private AudioEntity statusAudio;

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

    private CityEntity city;

    private CountryEntity country;

    private String homeTown;

    private String phone;

    private String homePhone;

    private String skype;

    private String instagram;

    private String twitter;

    private String facebook;

    private List<CareerEntity> careers;

    private List<MilitaryEntity> militaries;

    private List<UniversityEntity> universities;

    private List<SchoolEntity> schools;

    private List<RelativeEntity> relatives;

    private int relation;

    private int relationPartnerId;

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

    public String getInterests() {
        return interests;
    }

    public UserDetailsEntity setInterests(String interests) {
        this.interests = interests;
        return this;
    }

    public String getMusic() {
        return music;
    }

    public UserDetailsEntity setMusic(String music) {
        this.music = music;
        return this;
    }

    public String getActivities() {
        return activities;
    }

    public UserDetailsEntity setActivities(String activities) {
        this.activities = activities;
        return this;
    }

    public boolean isSetFavorite() {
        return isFavorite;
    }

    public UserDetailsEntity setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        return this;
    }

    public boolean isSetSubscribed() {
        return isSubscribed;
    }

    public UserDetailsEntity setSubscribed(boolean isSubscribed) {
        this.isSubscribed = isSubscribed;
        return this;
    }

    public String getMovies() {
        return movies;
    }

    public UserDetailsEntity setMovies(String movies) {
        this.movies = movies;
        return this;
    }

    public String getTv() {
        return tv;
    }

    public UserDetailsEntity setTv(String tv) {
        this.tv = tv;
        return this;
    }

    public String getGames() {
        return games;
    }

    public UserDetailsEntity setGames(String games) {
        this.games = games;
        return this;
    }

    public String getQuotes() {
        return quotes;
    }

    public UserDetailsEntity setQuotes(String quotes) {
        this.quotes = quotes;
        return this;
    }

    public String getAbout() {
        return about;
    }

    public UserDetailsEntity setAbout(String about) {
        this.about = about;
        return this;
    }

    public String getBooks() {
        return books;
    }

    public UserDetailsEntity setBooks(String books) {
        this.books = books;
        return this;
    }

    public String getSite() {
        return site;
    }

    public UserDetailsEntity setSite(String site) {
        this.site = site;
        return this;
    }

    public int getAlcohol() {
        return alcohol;
    }

    public UserDetailsEntity setAlcohol(int alcohol) {
        this.alcohol = alcohol;
        return this;
    }

    public int getLifeMain() {
        return lifeMain;
    }

    public UserDetailsEntity setLifeMain(int lifeMain) {
        this.lifeMain = lifeMain;
        return this;
    }

    public int getPeopleMain() {
        return peopleMain;
    }

    public UserDetailsEntity setPeopleMain(int peopleMain) {
        this.peopleMain = peopleMain;
        return this;
    }

    public int getPolitical() {
        return political;
    }

    public UserDetailsEntity setPolitical(int political) {
        this.political = political;
        return this;
    }

    public int getSmoking() {
        return smoking;
    }

    public UserDetailsEntity setSmoking(int smoking) {
        this.smoking = smoking;
        return this;
    }

    public String getInspiredBy() {
        return inspiredBy;
    }

    public UserDetailsEntity setInspiredBy(String inspiredBy) {
        this.inspiredBy = inspiredBy;
        return this;
    }

    public String getReligion() {
        return religion;
    }

    public UserDetailsEntity setReligion(String religion) {
        this.religion = religion;
        return this;
    }

    public String[] getLanguages() {
        return languages;
    }

    public UserDetailsEntity setLanguages(String[] languages) {
        this.languages = languages;
        return this;
    }

    public int getRelation() {
        return relation;
    }

    public UserDetailsEntity setRelation(int relation) {
        this.relation = relation;
        return this;
    }

    public int getRelationPartnerId() {
        return relationPartnerId;
    }

    public UserDetailsEntity setRelationPartnerId(int relationPartnerId) {
        this.relationPartnerId = relationPartnerId;
        return this;
    }

    public List<RelativeEntity> getRelatives() {
        return relatives;
    }

    public UserDetailsEntity setRelatives(List<RelativeEntity> relatives) {
        this.relatives = relatives;
        return this;
    }

    public List<SchoolEntity> getSchools() {
        return schools;
    }

    public UserDetailsEntity setSchools(List<SchoolEntity> schools) {
        this.schools = schools;
        return this;
    }

    public List<UniversityEntity> getUniversities() {
        return universities;
    }

    public UserDetailsEntity setUniversities(List<UniversityEntity> universities) {
        this.universities = universities;
        return this;
    }

    public List<MilitaryEntity> getMilitaries() {
        return militaries;
    }

    public UserDetailsEntity setMilitaries(List<MilitaryEntity> militaries) {
        this.militaries = militaries;
        return this;
    }

    public List<CareerEntity> getCareers() {
        return careers;
    }

    public UserDetailsEntity setCareers(List<CareerEntity> careers) {
        this.careers = careers;
        return this;
    }

    public String getSkype() {
        return skype;
    }

    public UserDetailsEntity setSkype(String skype) {
        this.skype = skype;
        return this;
    }

    public String getTwitter() {
        return twitter;
    }

    public UserDetailsEntity setTwitter(String twitter) {
        this.twitter = twitter;
        return this;
    }

    public String getInstagram() {
        return instagram;
    }

    public UserDetailsEntity setInstagram(String instagram) {
        this.instagram = instagram;
        return this;
    }

    public String getFacebook() {
        return facebook;
    }

    public UserDetailsEntity setFacebook(String facebook) {
        this.facebook = facebook;
        return this;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public UserDetailsEntity setHomePhone(String homePhone) {
        this.homePhone = homePhone;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserDetailsEntity setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public UserDetailsEntity setHomeTown(String homeTown) {
        this.homeTown = homeTown;
        return this;
    }

    public CountryEntity getCountry() {
        return country;
    }

    public UserDetailsEntity setCountry(CountryEntity country) {
        this.country = country;
        return this;
    }

    public CityEntity getCity() {
        return city;
    }

    public UserDetailsEntity setCity(CityEntity city) {
        this.city = city;
        return this;
    }

    public String getBdate() {
        return bdate;
    }

    public UserDetailsEntity setBdate(String bdate) {
        this.bdate = bdate;
        return this;
    }

    public IdPairEntity getPhotoId() {
        return photoId;
    }

    public UserDetailsEntity setPhotoId(IdPairEntity photoId) {
        this.photoId = photoId;
        return this;
    }

    public AudioEntity getStatusAudio() {
        return statusAudio;
    }

    public UserDetailsEntity setStatusAudio(AudioEntity statusAudio) {
        this.statusAudio = statusAudio;
        return this;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public UserDetailsEntity setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
        return this;
    }

    public int getOnlineFriendsCount() {
        return onlineFriendsCount;
    }

    public UserDetailsEntity setOnlineFriendsCount(int onlineFriendsCount) {
        this.onlineFriendsCount = onlineFriendsCount;
        return this;
    }

    public int getMutualFriendsCount() {
        return mutualFriendsCount;
    }

    public UserDetailsEntity setMutualFriendsCount(int mutualFriendsCount) {
        this.mutualFriendsCount = mutualFriendsCount;
        return this;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public UserDetailsEntity setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
        return this;
    }

    public int getGroupsCount() {
        return groupsCount;
    }

    public UserDetailsEntity setGroupsCount(int groupsCount) {
        this.groupsCount = groupsCount;
        return this;
    }

    public int getPhotosCount() {
        return photosCount;
    }

    public UserDetailsEntity setPhotosCount(int photosCount) {
        this.photosCount = photosCount;
        return this;
    }

    public int getAudiosCount() {
        return audiosCount;
    }

    public UserDetailsEntity setAudiosCount(int audiosCount) {
        this.audiosCount = audiosCount;
        return this;
    }

    public int getArticlesCount() {
        return articlesCount;
    }

    public UserDetailsEntity setArticlesCount(int articlesCount) {
        this.articlesCount = articlesCount;
        return this;
    }

    public int getProductsCount() {
        return productsCount;
    }

    public UserDetailsEntity setProductsCount(int productsCount) {
        this.productsCount = productsCount;
        return this;
    }

    public int getVideosCount() {
        return videosCount;
    }

    public UserDetailsEntity setVideosCount(int videosCount) {
        this.videosCount = videosCount;
        return this;
    }

    public int getAllWallCount() {
        return allWallCount;
    }

    public UserDetailsEntity setAllWallCount(int allWallCount) {
        this.allWallCount = allWallCount;
        return this;
    }

    public int getOwnWallCount() {
        return ownWallCount;
    }

    public UserDetailsEntity setOwnWallCount(int ownWallCount) {
        this.ownWallCount = ownWallCount;
        return this;
    }

    public int getPostponedWallCount() {
        return postponedWallCount;
    }

    public UserDetailsEntity setPostponedWallCount(int postponedWallCount) {
        this.postponedWallCount = postponedWallCount;
        return this;
    }

    public int getGiftCount() {
        return GiftCount;
    }

    public UserDetailsEntity setGiftCount(int GiftCount) {
        this.GiftCount = GiftCount;
        return this;
    }

    @Keep
    public static final class RelativeEntity {

        private int id;

        private String type;

        private String name;

        public String getName() {
            return name;
        }

        public RelativeEntity setName(String name) {
            this.name = name;
            return this;
        }

        public int getId() {
            return id;
        }

        public RelativeEntity setId(int id) {
            this.id = id;
            return this;
        }

        public String getType() {
            return type;
        }

        public RelativeEntity setType(String type) {
            this.type = type;
            return this;
        }
    }
}