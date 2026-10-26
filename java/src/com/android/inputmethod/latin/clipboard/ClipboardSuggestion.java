/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.clipboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ClipboardSuggestion {
    static final long MAX_AGE_MS = TimeUnit.MINUTES.toMillis(3);
    private static final Pattern CODE_PATTERN = Pattern.compile("(?<!\\d)\\d{4,8}(?!\\d)");

    public enum Type {
        CODE,
        TEXT
    }

    public static final class Match {
        @Nonnull
        private final String mText;
        @Nonnull
        private final Type mType;

        Match(@Nonnull final String text, @Nonnull final Type type) {
            mText = text;
            mType = type;
        }

        @Nonnull
        public String getText() {
            return mText;
        }

        @Nonnull
        public Type getType() {
            return mType;
        }
    }

    @Nonnull
    private final List<Match> mMatches;
    private final long mTimestamp;
    private final boolean mIsSensitive;

    private ClipboardSuggestion(@Nonnull final List<Match> matches, final long timestamp,
            final boolean isSensitive) {
        mMatches = Collections.unmodifiableList(matches);
        mTimestamp = timestamp;
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
        return new ClipboardSuggestion(findMatches(text), timestamp, isSensitive);
    }

    @Nonnull
    private static List<Match> findMatches(@Nonnull final String text) {
        final List<Match> matches = new ArrayList<>();
        final String code = findSingleCode(text);
        if (code != null) {
            matches.add(new Match(code, Type.CODE));
        }
        if (!text.equals(code)) {
            matches.add(new Match(text, Type.TEXT));
        }
        return matches;
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
    public List<Match> getMatches() {
        return mMatches;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public boolean isSensitive() {
        return mIsSensitive;
    }
}
