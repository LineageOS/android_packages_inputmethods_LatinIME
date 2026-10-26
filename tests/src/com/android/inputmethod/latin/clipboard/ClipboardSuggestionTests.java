/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.clipboard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class ClipboardSuggestionTests {
    private static final long COPY_TIME = TimeUnit.HOURS.toMillis(1);
    private static final long NOW = COPY_TIME + TimeUnit.SECONDS.toMillis(10);

    private static ClipboardSuggestion create(final CharSequence text) {
        return ClipboardSuggestion.create(text, COPY_TIME, NOW, false /* isSensitive */);
    }

    @Test
    public void testPlainTextSuggestion() {
        final ClipboardSuggestion suggestion = create("  hello world ");

        assertEquals("hello world", suggestion.getText());
        assertFalse(suggestion.isCode());
        assertEquals(COPY_TIME, suggestion.getTimestamp());
    }

    @Test
    public void testCodeOnlySuggestion() {
        final ClipboardSuggestion suggestion = create("482913");

        assertEquals("482913", suggestion.getText());
        assertTrue(suggestion.isCode());
    }

    @Test
    public void testCodeExtractedFromMessage() {
        final ClipboardSuggestion suggestion =
                create("Your verification code is 4829. Do not share it.");

        assertEquals("4829", suggestion.getText());
        assertTrue(suggestion.isCode());
    }

    @Test
    public void testNoCodeWhenMessageHasSeveralNumbers() {
        final String message = "Meet at 1530 in room 2041";

        final ClipboardSuggestion suggestion = create(message);

        assertEquals(message, suggestion.getText());
        assertFalse(suggestion.isCode());
    }

    @Test
    public void testNoCodeForShortOrLongNumbers() {
        assertFalse(create("123").isCode());
        assertFalse(create("9876543210").isCode());
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
