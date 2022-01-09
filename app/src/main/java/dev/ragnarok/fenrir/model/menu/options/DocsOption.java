package dev.ragnarok.fenrir.model.menu.options;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({DocsOption.add_item_doc,
        DocsOption.delete_item_doc,
        DocsOption.share_item_doc,
        DocsOption.open_item_doc,
        DocsOption.go_to_owner_doc})
@Retention(RetentionPolicy.SOURCE)
public @interface DocsOption {
    int add_item_doc = 1;
    int delete_item_doc = 2;
    int share_item_doc = 3;
    int open_item_doc = 4;
    int go_to_owner_doc = 5;
}

