package dev.ragnarok.fenrir.filepicker.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import dev.ragnarok.fenrir.filepicker.model.FileListItem;

/**
 * @author akshay sunil masram
 */
public class Utility {

    public static ArrayList<FileListItem>
    prepareFileListEntries(ArrayList<FileListItem> internalList, File inter,
                           ExtensionFilter filter, boolean show_hidden_files) {
        try {
            for (File name : Objects.requireNonNull(inter.listFiles(filter))) {
                if (name.canRead()) {
                    if (name.getName().startsWith(".") && !show_hidden_files) continue;
                    FileListItem item = new FileListItem();
                    item.setFilename(name.getName());
                    item.setDirectory(name.isDirectory());
                    item.setLocation(name.getAbsolutePath());
                    item.setTime(name.lastModified());
                    internalList.add(item);
                }
            }
            Collections.sort(internalList);
        } catch (NullPointerException e) {
            e.printStackTrace();
            internalList = new ArrayList<>();
        }
        return internalList;
    }
}
