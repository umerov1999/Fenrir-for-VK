package dev.ragnarok.fenrir.api.model;

/**
 * A school object describes a school.
 */
public class VKApiCareer implements IUserActivityPoint {

    public int group_id;
    public String company;
    public int country_id;
    public int city_id;
    public String city_name;
    public int from;
    public int until;
    public String position;

    /**
     * Creates empty School instance.
     */
    public VKApiCareer() {

    }
}