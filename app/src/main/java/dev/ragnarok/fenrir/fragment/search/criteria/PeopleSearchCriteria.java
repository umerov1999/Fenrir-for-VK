package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.search.options.DatabaseOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleBooleanOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleNumberOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleTextOption;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.util.ParcelUtils;

public final class PeopleSearchCriteria extends BaseSearchCriteria {

    public static final int KEY_SORT = 1;
    public static final int KEY_AGE_FROM = 2;
    public static final int KEY_AGE_TO = 3;
    public static final int KEY_RELATIONSHIP = 4;
    public static final int KEY_SEX = 5;
    public static final int KEY_ONLINE_ONLY = 6;
    public static final int KEY_WITH_PHOTO_ONLY = 7;
    public static final int KEY_COUNTRY = 8;
    public static final int KEY_CITY = 9;
    public static final int KEY_HOMETOWN = 10;

    public static final int KEY_BIRTHDAY_DAY = 13;
    public static final int KEY_BIRTHDAY_MONTH = 15;
    public static final int KEY_BIRTHDAY_YEAR = 16;

    public static final int KEY_UNIVERSITY_COUNTRY = 17;
    public static final int KEY_UNIVERSITY = 18;
    public static final int KEY_UNIVERSITY_YEAR = 19;
    public static final int KEY_UNIVERSITY_FACULTY = 20;
    public static final int KEY_UNIVERSITY_CHAIR = 21;

    public static final int KEY_SCHOOL_COUNTRY = 22;
    public static final int KEY_SCHOOL_CITY = 23;
    public static final int KEY_SCHOOL = 24;
    public static final int KEY_SCHOOL_CLASS = 25;
    public static final int KEY_SCHOOL_YEAR = 26;

    public static final int KEY_RELIGION = 27;
    public static final int KEY_INTERESTS = 28;
    public static final int KEY_COMPANY = 11;
    public static final int KEY_POSITION = 12;
    public static final int KEY_FROM_LIST = 29;
    public static final Creator<PeopleSearchCriteria> CREATOR = new Creator<PeopleSearchCriteria>() {
        @Override
        public PeopleSearchCriteria createFromParcel(Parcel in) {
            return new PeopleSearchCriteria(in);
        }

        @Override
        public PeopleSearchCriteria[] newArray(int size) {
            return new PeopleSearchCriteria[size];
        }
    };
    private Integer groupId;

    public PeopleSearchCriteria(String query) {
        super(query);

        SpinnerOption sort = new SpinnerOption(KEY_SORT, R.string.sorting, true);
        sort.available = new ArrayList<>(2);
        sort.available.add(new SpinnerOption.Entry(1, R.string.search_option_by_date_registered));
        sort.available.add(new SpinnerOption.Entry(0, R.string.search_option_by_rating));
        appendOption(sort);

        SimpleNumberOption ageFrom = new SimpleNumberOption(KEY_AGE_FROM, R.string.age_from, true);
        appendOption(ageFrom);

        SimpleNumberOption ageTo = new SimpleNumberOption(KEY_AGE_TO, R.string.age_to, true);
        appendOption(ageTo);

        SpinnerOption status = new SpinnerOption(KEY_RELATIONSHIP, R.string.relationship, true);
        status.available = new ArrayList<>(7);
        status.available.add(new SpinnerOption.Entry(1, R.string.search_option_not_married));
        status.available.add(new SpinnerOption.Entry(2, R.string.search_option_in_relationship));
        status.available.add(new SpinnerOption.Entry(3, R.string.search_option_engaged));
        status.available.add(new SpinnerOption.Entry(4, R.string.search_option_married));
        status.available.add(new SpinnerOption.Entry(5, R.string.search_option_its_complicated));
        status.available.add(new SpinnerOption.Entry(6, R.string.search_option_actively_searching));
        status.available.add(new SpinnerOption.Entry(7, R.string.search_option_in_love));
        appendOption(status);

        SpinnerOption sex = new SpinnerOption(KEY_SEX, R.string.sex, true);
        sex.available = new ArrayList<>(2);
        sex.available.add(new SpinnerOption.Entry(1, R.string.female));
        sex.available.add(new SpinnerOption.Entry(2, R.string.male));
        appendOption(sex);

        SimpleBooleanOption onlyOnline = new SimpleBooleanOption(KEY_ONLINE_ONLY, R.string.online_only, true);
        appendOption(onlyOnline);

        SimpleBooleanOption withPhoto = new SimpleBooleanOption(KEY_WITH_PHOTO_ONLY, R.string.with_photo_only, true);
        appendOption(withPhoto);

        DatabaseOption country = new DatabaseOption(KEY_COUNTRY, R.string.country, true, DatabaseOption.TYPE_COUNTRY);
        country.setChildDependencies(KEY_CITY);
        appendOption(country);

        DatabaseOption city = new DatabaseOption(KEY_CITY, R.string.city, true, DatabaseOption.TYPE_CITY);
        city.setDependencyOf(KEY_COUNTRY);
        appendOption(city);

        appendOption(new SimpleTextOption(KEY_HOMETOWN, R.string.hometown, true));

        appendOption(new SimpleNumberOption(KEY_BIRTHDAY_DAY, R.string.birthday_day, true));

        SpinnerOption birthdayMonth = new SpinnerOption(KEY_BIRTHDAY_MONTH, R.string.birthday_month, true);
        birthdayMonth.available = new ArrayList<>(12);
        birthdayMonth.available.add(new SpinnerOption.Entry(1, R.string.january));
        birthdayMonth.available.add(new SpinnerOption.Entry(2, R.string.february));
        birthdayMonth.available.add(new SpinnerOption.Entry(3, R.string.march));
        birthdayMonth.available.add(new SpinnerOption.Entry(4, R.string.april));
        birthdayMonth.available.add(new SpinnerOption.Entry(5, R.string.may));
        birthdayMonth.available.add(new SpinnerOption.Entry(6, R.string.june));
        birthdayMonth.available.add(new SpinnerOption.Entry(7, R.string.july));
        birthdayMonth.available.add(new SpinnerOption.Entry(8, R.string.august));
        birthdayMonth.available.add(new SpinnerOption.Entry(9, R.string.september));
        birthdayMonth.available.add(new SpinnerOption.Entry(10, R.string.october));
        birthdayMonth.available.add(new SpinnerOption.Entry(11, R.string.november));
        birthdayMonth.available.add(new SpinnerOption.Entry(12, R.string.december));
        appendOption(birthdayMonth);

        appendOption(new SimpleNumberOption(KEY_BIRTHDAY_YEAR, R.string.birthday_year, true));

        DatabaseOption universityCountry = new DatabaseOption(KEY_UNIVERSITY_COUNTRY, R.string.university_country, true, DatabaseOption.TYPE_COUNTRY);
        universityCountry.setChildDependencies(KEY_UNIVERSITY, KEY_UNIVERSITY_FACULTY, KEY_UNIVERSITY_CHAIR);
        appendOption(universityCountry);

        DatabaseOption university = new DatabaseOption(KEY_UNIVERSITY, R.string.college_or_university, true, DatabaseOption.TYPE_UNIVERSITY);
        university.setDependencyOf(KEY_UNIVERSITY_COUNTRY);
        university.setChildDependencies(KEY_UNIVERSITY_FACULTY, KEY_UNIVERSITY_CHAIR);
        appendOption(university);

        appendOption(new SimpleNumberOption(KEY_UNIVERSITY_YEAR, R.string.year_of_graduation, true));

        DatabaseOption faculty = new DatabaseOption(KEY_UNIVERSITY_FACULTY, R.string.faculty, true, DatabaseOption.TYPE_FACULTY);
        faculty.setDependencyOf(KEY_UNIVERSITY);
        faculty.setChildDependencies(KEY_UNIVERSITY_CHAIR);
        appendOption(faculty);

        DatabaseOption chair = new DatabaseOption(KEY_UNIVERSITY_CHAIR, R.string.chair, true, DatabaseOption.TYPE_CHAIR);
        chair.setDependencyOf(KEY_UNIVERSITY_FACULTY);
        appendOption(chair);

        DatabaseOption schoolCountry = new DatabaseOption(KEY_SCHOOL_COUNTRY, R.string.school_country, true, DatabaseOption.TYPE_COUNTRY);
        schoolCountry.setChildDependencies(KEY_SCHOOL_CITY, KEY_SCHOOL, KEY_SCHOOL_CLASS);
        appendOption(schoolCountry);

        DatabaseOption shoolCity = new DatabaseOption(KEY_SCHOOL_CITY, R.string.school_city, true, DatabaseOption.TYPE_CITY);
        shoolCity.setChildDependencies(KEY_SCHOOL);
        shoolCity.setDependencyOf(KEY_SCHOOL_COUNTRY);
        appendOption(shoolCity);

        DatabaseOption school = new DatabaseOption(KEY_SCHOOL, R.string.school, true, DatabaseOption.TYPE_SCHOOL);
        school.setDependencyOf(KEY_SCHOOL_CITY);
        appendOption(school);

        appendOption(new SimpleNumberOption(KEY_SCHOOL_YEAR, R.string.year_of_graduation, true));

        DatabaseOption schoolClass = new DatabaseOption(KEY_SCHOOL_CLASS, R.string.school_class, true, DatabaseOption.TYPE_SCHOOL_CLASS);
        schoolClass.setDependencyOf(KEY_SCHOOL_COUNTRY);
        appendOption(schoolClass);

        appendOption(new SimpleTextOption(KEY_RELIGION, R.string.religious_affiliation, true));
        appendOption(new SimpleTextOption(KEY_INTERESTS, R.string.interests, true));
        appendOption(new SimpleTextOption(KEY_COMPANY, R.string.company, true));
        appendOption(new SimpleTextOption(KEY_POSITION, R.string.position, true));

        SpinnerOption fromListOption = new SpinnerOption(KEY_FROM_LIST, R.string.from_list, true);
        fromListOption.available = new ArrayList<>(2);
        fromListOption.available.add(new SpinnerOption.Entry(FromList.FRIENDS, R.string.friends));
        fromListOption.available.add(new SpinnerOption.Entry(FromList.SUBSCRIPTIONS, R.string.subscriptions));
        appendOption(fromListOption);
    }

    private PeopleSearchCriteria(Parcel in) {
        super(in);
        groupId = ParcelUtils.readObjectInteger(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelUtils.writeObjectInteger(dest, groupId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public PeopleSearchCriteria clone() throws CloneNotSupportedException {
        return (PeopleSearchCriteria) super.clone();
    }

    public Integer getGroupId() {
        return groupId;
    }

    public PeopleSearchCriteria setGroupId(Integer groupId) {
        this.groupId = groupId;
        return this;
    }

    public static class FromList {
        public static final int FRIENDS = 1;
        public static final int SUBSCRIPTIONS = 2;
    }
}