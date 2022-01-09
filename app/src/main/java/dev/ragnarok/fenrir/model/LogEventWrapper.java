package dev.ragnarok.fenrir.model;


public class LogEventWrapper {

    private final LogEvent event;
    private boolean expanded;

    public LogEventWrapper(LogEvent event) {
        this.event = event;
    }

    public LogEvent getEvent() {
        return event;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
