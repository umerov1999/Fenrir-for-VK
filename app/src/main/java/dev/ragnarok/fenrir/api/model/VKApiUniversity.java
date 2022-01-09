package dev.ragnarok.fenrir.api.model;

/**
 * An university object describes an university.
 */
public class VKApiUniversity implements IUserActivityPoint {

    /**
     * University ID, positive number
     */
    public int id;

    /**
     * ID of the country the university is located in, positive number
     */
    public int country_id;

    /**
     * ID of the city the university is located in, positive number
     */
    public int city_id;

    /**
     * University name
     */
    public String name;

    /**
     * Faculty ID
     */
    public int faculty;

    /**
     * Faculty name
     */
    public String faculty_name;

    /**
     * University chair ID;
     */
    public int chair;

    /**
     * Chair name
     */
    public String chair_name;

    /**
     * Graduation year
     */
    public int graduation;

    /**
     * Form of education
     */
    public String education_form;

    /**
     * Status of education
     */
    public String education_status;
}