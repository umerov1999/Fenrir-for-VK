package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class UniversityEntity {

    private int id;
    private int countryId;
    private int cityId;
    private String name;
    private int facultyId;
    private String facultyName;
    private int chairId;
    private String chairName;
    private int graduationYear;
    private String form;
    private String status;

    public int getId() {
        return id;
    }

    public UniversityEntity setId(int id) {
        this.id = id;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public UniversityEntity setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getCityId() {
        return cityId;
    }

    public UniversityEntity setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public String getName() {
        return name;
    }

    public UniversityEntity setName(String name) {
        this.name = name;
        return this;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public UniversityEntity setFacultyId(int facultyId) {
        this.facultyId = facultyId;
        return this;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public UniversityEntity setFacultyName(String facultyName) {
        this.facultyName = facultyName;
        return this;
    }

    public int getChairId() {
        return chairId;
    }

    public UniversityEntity setChairId(int chairId) {
        this.chairId = chairId;
        return this;
    }

    public String getChairName() {
        return chairName;
    }

    public UniversityEntity setChairName(String chairName) {
        this.chairName = chairName;
        return this;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public UniversityEntity setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
        return this;
    }

    public String getForm() {
        return form;
    }

    public UniversityEntity setForm(String form) {
        this.form = form;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UniversityEntity setStatus(String status) {
        this.status = status;
        return this;
    }
}