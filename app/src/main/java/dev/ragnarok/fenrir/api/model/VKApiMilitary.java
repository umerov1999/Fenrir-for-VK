package dev.ragnarok.fenrir.api.model;

/**
 * A school object describes a school.
 */
public class VKApiMilitary {
    /**
     * номер части
     */
    public String unit;
    /**
     * идентификатор части в базе данных
     */
    public int unit_id;
    /**
     * идентификатор страны, в которой находится часть
     */
    public int country_id;
    /**
     * год начала службы
     */
    public int from;
    /**
     * год окончания службы
     */
    public int until;

    /**
     * Creates empty School instance.
     */
    public VKApiMilitary() {

    }
}