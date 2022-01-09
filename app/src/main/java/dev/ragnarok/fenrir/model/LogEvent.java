package dev.ragnarok.fenrir.model;


public class LogEvent {

    private final int id;

    private long date;

    private int type;

    private String tag;

    private String body;

    public LogEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public LogEvent setDate(long date) {
        this.date = date;
        return this;
    }

    public int getType() {
        return type;
    }

    public LogEvent setType(int type) {
        this.type = type;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public LogEvent setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public String getBody() {
        return body;
    }

    public LogEvent setBody(String body) {
        this.body = body;
        return this;
    }

    public static final class Type {

        public static final int ERROR = 1;

    }
}
