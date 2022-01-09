package ealvatag.tag.reference;

import ealvatag.tag.id3.AbstractID3v2Tag;
import ealvatag.tag.id3.ID3v22Tag;
import ealvatag.tag.id3.ID3v23Tag;
import ealvatag.tag.id3.ID3v24Tag;

/**
 * Defines ID3V2 Versions
 */
public enum ID3V2Version {
    ID3_V22 {
        @Override
        public AbstractID3v2Tag makeTag() {
            return new ID3v22Tag();
        }
    },
    ID3_V23 {
        @Override
        public AbstractID3v2Tag makeTag() {
            return new ID3v23Tag();
        }
    },
    ID3_V24 {
        @Override
        public AbstractID3v2Tag makeTag() {
            return new ID3v24Tag();
        }
    };

    public abstract AbstractID3v2Tag makeTag();
}
