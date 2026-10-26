/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.clipboard;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ClipboardSuggestion {
    static final long MAX_AGE_MS = TimeUnit.MINUTES.toMillis(3);
    private static final Pattern CODE_PATTERN = Pattern.compile("(?<!\\d)\\d{4,8}(?!\\d)");

    @Nonnull
    private final String mText;
    private final long mTimestamp;
    private final boolean mIsCode;
    private final boolean mIsSensitive;

    private ClipboardSuggestion(@Nonnull final String text, final long timestamp,
            final boolean isCode, final boolean isSensitive) {
        mText = text;
        mTimestamp = timestamp;
        mIsCode = isCode;
        mIsSensitive = isSensitive;
    }

    @Nullable
    public static ClipboardSuggestion create(@Nullable final CharSequence clipText,
            final long timestamp, final long now, final boolean isSensitive) {
        final long age = now - timestamp;
        if (clipText == null || age < 0 || age > MAX_AGE_MS) {
            return null;
        }
        final String text = clipText.toString().trim();
        if (text.isEmpty()) {
            return null;
        }
        final String code = findSingleCode(text);
        return code == null
                ? new ClipboardSuggestion(text, timestamp, false, isSensitive)
                : new ClipboardSuggestion(code, timestamp, true, isSensitive);
    }

    @Nullable
    private static String findSingleCode(@Nonnull final String text) {
        final Matcher matcher = CODE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        final String code = matcher.group();
        return matcher.find() ? null : code;
    }

    @Nonnull
    public String getText() {
        return mText;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public boolean isCode() {
        return mIsCode;
    }

    public boolean isSensitive() {
        return mIsSensitive;
    }
}
