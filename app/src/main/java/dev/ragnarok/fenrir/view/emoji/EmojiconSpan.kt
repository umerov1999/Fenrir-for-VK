package dev.ragnarok.fenrir.view.emoji

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.style.DynamicDrawableSpan
import androidx.appcompat.content.res.AppCompatResources

internal class EmojiconSpan(
    private val mContext: Context,
    private val mResourceId: Int,
    private val mSize: Int
) : DynamicDrawableSpan() {
    private var mDrawable: Drawable? = null
    override fun getDrawable(): Drawable {
        if (mDrawable == null) {
            try {
                mDrawable = AppCompatResources.getDrawable(mContext, mResourceId)
                mDrawable?.setBounds(0, 0, mSize, mSize)
            } catch (e: Exception) {
                // swallow
            }
        }
        return mDrawable!!
    }
}