package dev.ragnarok.fenrir.db.column

import android.provider.BaseColumns

object StickerSetsCustomColumns : BaseColumns {
    const val TABLENAME = "sticker_sets_custom"
    const val ACCOUNT_ID = "account_id"
    const val POSITION = "position"
    const val TITLE = "title"
    const val ICON = "icon"
    const val PURCHASED = "purchased"
    const val PROMOTED = "promoted"
    const val ACTIVE = "active"
    const val STICKERS = "stickers"
}
