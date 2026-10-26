/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.clipboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.inputmethod.latin.clipboard.ClipboardSuggestion.Match;
import com.android.inputmethod.latin.clipboard.ClipboardSuggestion.Type;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class ClipboardSuggestionTests {
    private static final long COPY_TIME = TimeUnit.HOURS.toMillis(1);
    private static final long NOW = COPY_TIME + TimeUnit.SECONDS.toMillis(10);

    private static ClipboardSuggestion create(final CharSequence text) {
        return ClipboardSuggestion.create(text, COPY_TIME, NOW, false /* isSensitive */);
    }

    private static void assertMatch(final Match match, final String text, final Type type) {
        assertEquals(text, match.getText());
        assertEquals(type, match.getType());
    }

    @Test
    public void testPlainTextSuggestion() {
        final ClipboardSuggestion suggestion = create("  hello world ");

        assertEquals(1, suggestion.getMatches().size());
        assertMatch(suggestion.getMatches().get(0), "hello world", Type.TEXT);
        assertEquals(COPY_TIME, suggestion.getTimestamp());
    }

    @Test
    public void testCodeOnlySuggestion() {
        final ClipboardSuggestion suggestion = create("482913");

        assertEquals(1, suggestion.getMatches().size());
        assertMatch(suggestion.getMatches().get(0), "482913", Type.CODE);
    }

    @Test
    public void testCodeAndTextFromMessage() {
        final String message = "Your verification code is 4829. Do not share it.";

        final List<Match> matches = create(message).getMatches();

        assertEquals(2, matches.size());
        assertMatch(matches.get(0), "4829", Type.CODE);
        assertMatch(matches.get(1), message, Type.TEXT);
    }

    @Test
    public void testOnlyTextWhenMessageHasSeveralNumbers() {
        final String message = "Meet at 1530 in room 2041";

        final List<Match> matches = create(message).getMatches();

        assertEquals(1, matches.size());
        assertMatch(matches.get(0), message, Type.TEXT);
    }

    @Test
    public void testOnlyTextForShortOrLongNumbers() {
        assertMatch(create("123").getMatches().get(0), "123", Type.TEXT);
        assertMatch(create("9876543210").getMatches().get(0), "9876543210", Type.TEXT);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMatchesAreUnmodifiable() {
        create("hello").getMatches().clear();
    }

    @Test
    public void testSensitiveSuggestion() {
        final ClipboardSuggestion suggestion =
                ClipboardSuggestion.create("hunter2", COPY_TIME, NOW, true /* isSensitive */);

        assertTrue(suggestion.isSensitive());
    }

    @Test
    public void testNoSuggestionForEmptyText() {
        assertNull(create(null));
        assertNull(create(""));
        assertNull(create("   "));
    }

    @Test
    public void testNoSuggestionForExpiredClip() {
        final long expiredNow = COPY_TIME + ClipboardSuggestion.MAX_AGE_MS + 1;

        assertNull(ClipboardSuggestion.create("hello", COPY_TIME, expiredNow, false));
    }

    @Test
    public void testNoSuggestionForClipFromTheFuture() {
        assertNull(ClipboardSuggestion.create("hello", NOW + 1, NOW, false));
    }
}
