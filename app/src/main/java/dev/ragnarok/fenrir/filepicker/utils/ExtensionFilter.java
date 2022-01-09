package dev.ragnarok.fenrir.filepicker.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import dev.ragnarok.fenrir.filepicker.model.DialogConfigs;
import dev.ragnarok.fenrir.filepicker.model.DialogProperties;

/**
 * @author akshay sunil masram
 */
public class ExtensionFilter implements FileFilter {

    private final String[] validExtensions;
    private final DialogProperties properties;

    public ExtensionFilter(DialogProperties properties) {
        if (properties.extensions != null) {
            validExtensions = properties.extensions;
        } else {
            validExtensions = new String[]{""};
        }
        this.properties = properties;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory() && file.canRead()) {
            return true;
        } else if (properties.selection_type == DialogConfigs.DIR_SELECT) {
            return false;
        } else {
            String name = file.getName().toLowerCase(Locale.getDefault());
            for (String ext : validExtensions) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
        }
        return false;
    }
}