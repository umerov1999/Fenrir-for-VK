package dev.ragnarok.fenrir.adapter.horizontal

import android.content.Context

interface Entry {
    fun getTitle(context: Context): String?
    val isActive: Boolean
    val isCustom: Boolean
}