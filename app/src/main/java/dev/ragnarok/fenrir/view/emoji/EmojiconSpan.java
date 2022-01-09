/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ragnarok.fenrir.view.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import androidx.appcompat.content.res.AppCompatResources;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
class EmojiconSpan extends DynamicDrawableSpan {
    private final Context mContext;
    private final int mResourceId;
    private final int mSize;
    private Drawable mDrawable;

    public EmojiconSpan(Context context, int resourceId, int size) {
        mContext = context;
        mResourceId = resourceId;
        mSize = size;
    }

    public Drawable getDrawable() {
        if (mDrawable == null) {
            try {
                mDrawable = AppCompatResources.getDrawable(mContext, mResourceId);
                if (mDrawable != null)
                    mDrawable.setBounds(0, 0, mSize, mSize);
            } catch (Exception e) {
                // swallow
            }
        }
        return mDrawable;
    }
}
