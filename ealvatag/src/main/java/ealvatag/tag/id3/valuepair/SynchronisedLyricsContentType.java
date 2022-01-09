package ealvatag.tag.id3.valuepair;

import com.google.common.base.Strings;

/**
 * Content Type used by Sysnchronised Lyrics Frame (SYLT)
 */
public class SynchronisedLyricsContentType implements SimpleIntStringMap {
    public static final int MAX_SYNCHRONIZED_LYRICS_ID = 0x08;
    public static final int CONTENT_KEY_FIELD_SIZE = 1;

    private static volatile SynchronisedLyricsContentType instance;
    private final String[] values;

    private SynchronisedLyricsContentType() {
        values = new String[MAX_SYNCHRONIZED_LYRICS_ID + 1];
        values[0x00] = "other";
        values[0x01] = "lyrics";
        values[0x02] = "text transcription";
        values[0x03] = "movement/part name";
        values[0x04] = "events";
        values[0x05] = "chord";
        values[0x06] = "trivia";
        values[0x07] = "URLs to webpages";
        values[0x08] = "URLs to images";
    }

    public static SynchronisedLyricsContentType getInstanceOf() {
        if (instance == null) {
            synchronized (SynchronisedLyricsContentType.class) {
                if (instance == null) {
                    instance = new SynchronisedLyricsContentType();
                }
            }
        }
        return instance;
    }

    @Override
    public boolean containsKey(int key) {
        return key >= 0 && key <= MAX_SYNCHRONIZED_LYRICS_ID;
    }

    @Override
    public String getValue(int key) {
        if (!containsKey(key)) {
            return "";
        }
        return Strings.nullToEmpty(values[key]);
    }
}
