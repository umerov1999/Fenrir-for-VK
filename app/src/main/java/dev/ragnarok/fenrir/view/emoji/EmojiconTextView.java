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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.ClickableForegroundColorSpan;
import dev.ragnarok.fenrir.view.WrapWidthTextView;

public class EmojiconTextView extends WrapWidthTextView implements ClickableForegroundColorSpan.OnHashTagClickListener {

    private static final Pattern URL_VK_PATTERN = Pattern.compile("(((http|https|rstp)://)?(\\w+.)?vk\\.(com|me|cc)/\\S*)");
    private static final Pattern URL_NON_VK_PATTERN = Pattern.compile("((http|https|rstp)://(?!(\\w+.)?vk\\.(com|me|cc)/)\\S*)");
    private int mEmojiconSize;
    private int mTextStart;
    private int mTextLength = -1;
    private List<Character> mAdditionalHashTagChars;
    private OnHashTagClickListener mOnHashTagClickListener;
    private boolean mDisplayHashTags;
    private int mHashTagWordColor;

    public EmojiconTextView(Context context) {
        this(context, null);
    }

    public EmojiconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mAdditionalHashTagChars = new ArrayList<>(2);
        mAdditionalHashTagChars.add('_');
        mAdditionalHashTagChars.add('@');

        if (attrs == null) {
            mEmojiconSize = (int) getTextSize();
        } else {
            @SuppressLint("CustomViewStyleable") TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);

            try {
                mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
                mTextStart = a.getInteger(R.styleable.Emojicon_emojiconTextStart, 0);
                mTextLength = a.getInteger(R.styleable.Emojicon_emojiconTextLength, -1);
                mHashTagWordColor = a.getColor(R.styleable.Emojicon_hashTagColor, Color.BLUE);
                mDisplayHashTags = a.getBoolean(R.styleable.Emojicon_displayHashTags, false);
            } finally {
                a.recycle();
            }
        }

        setText(getText());
    }

    private void setColorsToAllHashTags(Spannable text) {
        int startIndexOfNextHashSign;

        int index = 0;
        while (index < text.length() - 1) {
            char sign = text.charAt(index);
            int nextNotLetterDigitCharIndex = index + 1; // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
            if (sign == '#') {
                startIndexOfNextHashSign = index;

                nextNotLetterDigitCharIndex = findNextValidHashTagChar(text, startIndexOfNextHashSign);

                setColorForHashTagToTheEnd(text, startIndexOfNextHashSign, nextNotLetterDigitCharIndex);
            }

            index = nextNotLetterDigitCharIndex;
        }
    }

    private int findNextValidHashTagChar(CharSequence text, int start) {
        int nonLetterDigitCharIndex = -1; // skip first sign '#"
        for (int index = start + 1; index < text.length(); index++) {
            char sign = text.charAt(index);
            boolean isValidSign = Character.isLetterOrDigit(sign) || mAdditionalHashTagChars.contains(sign);
            if (!isValidSign) {
                nonLetterDigitCharIndex = index;
                break;
            }
        }

        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length();
        }

        return nonLetterDigitCharIndex;
    }

    private void setColorForHashTagToTheEnd(Spannable s, int startIndex, int nextNotLetterDigitCharIndex) {
        CharacterStyle span;

        if (mOnHashTagClickListener != null) {
            span = new ClickableForegroundColorSpan(mHashTagWordColor, this);
        } else {
            // no need for clickable span because it is messing with selection when click
            span = new ForegroundColorSpan(mHashTagWordColor);
        }

        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    public void setText(CharSequence originalText, BufferType type) {
        if (originalText != null && originalText.length() > 0) {
            Spannable spannable = SpannableStringBuilder.valueOf(originalText);

            if (mDisplayHashTags) {
                setColorsToAllHashTags(spannable);
            }

            if (!Settings.get().ui().isSystemEmoji()) {
                EmojiconHandler.addEmojis(getContext(), spannable, mEmojiconSize, mTextStart, mTextLength);
            }
            int mode = Settings.get().main().isOpenUrlInternal();

            if (mode > 0) {
                linkifyVKUrl(spannable);
            }
            if (mode > 1) {
                linkifyNonVKUrl(spannable);
            }

            super.setText(spannable, type);
        } else {
            super.setText(originalText, type);
        }
    }

    public void linkifyVKUrl(Spannable spannable) {
        Matcher m = URL_VK_PATTERN.matcher(spannable);
        while (m.find()) {
            String url = spannable.toString().substring(m.start(), m.end());
            ClickableSpan urlSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    LinkHelper.openUrl((Activity) getContext(), Settings.get().accounts().getCurrent(), url);
                }
            };
            spannable.setSpan(urlSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void linkifyNonVKUrl(Spannable spannable) {
        Matcher m = URL_NON_VK_PATTERN.matcher(spannable);
        while (m.find()) {
            String url = spannable.toString().substring(m.start(), m.end());
            ClickableSpan urlSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    LinkHelper.openLinkInBrowser(getContext(), url);
                }
            };
            spannable.setSpan(urlSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;
    }

    private void eraseAndColorizeAllText(Spannable text) {
        if (getText() instanceof Spannable) {
            Spannable spannable = ((Spannable) getText());

            CharacterStyle[] spans = spannable.getSpans(0, text.length(), CharacterStyle.class);
            for (CharacterStyle span : spans) {
                spannable.removeSpan(span);
            }
        }

        setColorsToAllHashTags(text);
    }

    @Override
    public void onHashTagClicked(String hashTag) {
        mOnHashTagClickListener.onHashTagClicked(hashTag);
    }

    public void setOnHashTagClickListener(OnHashTagClickListener onHashTagClickListener) {
        mOnHashTagClickListener = onHashTagClickListener;
    }

    public void setAdditionalHashTagChars(List<Character> additionalHashTagChars) {
        mAdditionalHashTagChars = additionalHashTagChars;
    }

    public List<String> getAllHashTags(boolean withHashes) {
        String text = getText().toString();
        Spannable spannable = (Spannable) getText();

        // use set to exclude duplicates
        Set<String> hashTags = new LinkedHashSet<>();

        for (CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            hashTags.add(text.substring(!withHashes ? spannable.getSpanStart(span) + 1 :
                    spannable.getSpanStart(span), spannable.getSpanEnd(span)));
        }

        return new ArrayList<>(hashTags);
    }

    public List<String> getAllHashTags() {
        return getAllHashTags(false);
    }

    public interface OnHashTagClickListener {
        void onHashTagClicked(String hashTag);
    }
}
