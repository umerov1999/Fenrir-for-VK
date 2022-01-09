package ealvatag.audio;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ealvatag.audio.real.RealTag;
import ealvatag.tag.Tag;
import ealvatag.tag.TagOptionSingleton;

/**
 * Files formats currently supported by Library.
 * Each enum value is associated with a file suffix (extension).
 */
public enum SupportedFileFormat {
    MP3("mp3") {
        @Override
        public Tag makeDefaultTag() throws UnsupportedFileType {
            return TagOptionSingleton.createDefaultID3Tag();
        }
    },
    RA("ra") {
        @Override
        public Tag makeDefaultTag() throws UnsupportedFileType {
            return new RealTag();
        }
    },
    RM("rm") {
        @Override
        public Tag makeDefaultTag() throws UnsupportedFileType {
            return new RealTag();
        }
    },
    /**
     * This type is used when the format cannot be determined. Such as via file extension.
     */
    UNKNOWN("") {
        @Override
        public Tag makeDefaultTag() throws UnsupportedFileType {
            throw new UnsupportedFileType("Unable to create default tag for this file format:" + name());
        }
    };

    private static final Map<String, SupportedFileFormat> extensionMap;

    static {
        SupportedFileFormat[] values = values();
        extensionMap = new HashMap<>(values.length);
        for (SupportedFileFormat format : values) {
            extensionMap.put(format.fileSuffix, format);
        }
    }

    private final String fileSuffix;

    /**
     * Constructor for internal use by this enum.
     */
    SupportedFileFormat(String fileSuffix) {
        this.fileSuffix = fileSuffix.toLowerCase(Locale.ROOT);  // ensure lowercase
    }

    /**
     * Get the format from the file extension.
     *
     * @param fileExtension file extension
     * @return the format for the extension or UNKNOWN if the extension not recognized
     */
    public static SupportedFileFormat fromExtension(String fileExtension) {
        if (fileExtension == null) {
            return UNKNOWN;
        }
        SupportedFileFormat format = extensionMap.get(fileExtension.toLowerCase(Locale.ROOT));
        return format == null ? UNKNOWN : format;
    }

    /**
     * Returns the file suffix (lower case without initial .) associated with the format.
     */
    public String getFileSuffix() {
        return fileSuffix;
    }

    /**
     * Create for this format
     *
     * @return the default tag for the given type
     * @throws UnsupportedFileType if can't create the default tag
     */
    public abstract Tag makeDefaultTag() throws UnsupportedFileType;
}
