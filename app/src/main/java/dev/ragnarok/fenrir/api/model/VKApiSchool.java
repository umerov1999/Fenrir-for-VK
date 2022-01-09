package dev.ragnarok.fenrir.api.model;

/**
 * A school object describes a school.
 */
public class VKApiSchool implements IUserActivityPoint {

    /**
     * School ID, positive number
     */
    public int id;

    /**
     * ID of the country the school is located in, positive number
     */
    public int country_id;

    /**
     * ID of the city the school is located in, positive number
     */
    public int city_id;

    /**
     * School name
     */
    public String name;

    /**
     * Year the user started to study
     */
    public int year_from;

    /**
     * Year the user finished to study
     */
    public int year_to;

    /**
     * Graduation year
     */
    public int year_graduated;

    /**
     * School class letter
     */
    public String clazz;

    /**
     * Speciality
     */
    public String speciality;


    /**
     * идентификатор типа
     */
    public int type;

    /**
     * название типа
     */
    public String type_str;

    private String fullName;
}