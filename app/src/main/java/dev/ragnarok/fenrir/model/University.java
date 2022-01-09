package dev.ragnarok.fenrir.model;

public class University {

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

    public University setId(int id) {
        this.id = id;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public University setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getCityId() {
        return cityId;
    }

    public University setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public String getName() {
        return name;
    }

    public University setName(String name) {
        this.name = name;
        return this;
    }

    public int getFacultyId() {
        return facultyId;
    }

    public University setFacultyId(int facultyId) {
        this.facultyId = facultyId;
        return this;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public University setFacultyName(String facultyName) {
        this.facultyName = facultyName;
        return this;
    }

    public int getChairId() {
        return chairId;
    }

    public University setChairId(int chairId) {
        this.chairId = chairId;
        return this;
    }

    public String getChairName() {
        return chairName;
    }

    public University setChairName(String chairName) {
        this.chairName = chairName;
        return this;
    }

    public int getGraduationYear() {
        return graduationYear;
    }

    public University setGraduationYear(int graduationYear) {
        this.graduationYear = graduationYear;
        return this;
    }

    public String getForm() {
        return form;
    }

    public University setForm(String form) {
        this.form = form;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public University setStatus(String status) {
        this.status = status;
        return this;
    }
}