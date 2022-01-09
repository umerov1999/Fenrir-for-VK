package dev.ragnarok.fenrir.api.model;

/**
 * A place object describes a location.
 */
@SuppressWarnings("unused")
public class VKApiPlace {

    /**
     * Location ID.
     */
    public int id;

    /**
     * Location title.
     */
    public String title;

    /**
     * Geographical latitude, in degrees (from -90 to 90).
     */
    public double latitude;

    /**
     * Geographical longitude, in degrees (from -180 to 180)
     */
    public double longitude;

    /**
     * Date (in Unix time) when the location was added
     */
    public long created;

    /**
     * Numbers of checkins in this place
     */
    public int checkins;

    /**
     * Date (in Unix time) when the location was last time updated
     */
    public long updated;

    /**
     * ID of the country the place is located in, positive number
     */
    public int country_id;

    /**
     * ID of the city the place is located in, positive number
     */
    public int city_id;

    /**
     * Location address.
     */
    public String address;

    /**
     * Creates empty Place instance.
     */
    public VKApiPlace() {

    }
}