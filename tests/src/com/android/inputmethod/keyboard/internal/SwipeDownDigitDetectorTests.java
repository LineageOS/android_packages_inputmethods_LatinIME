/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.latin.common.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class SwipeDownDigitDetectorTests {
    private static final int KEY_HEIGHT = 100;
    private static final int THRESHOLD = 60;
    private static final int DOWN_X = 50;
    private static final int DOWN_Y = 20;
    private static final int START_Y = DOWN_Y + 10;

    private final Key mDigitKey = key("q", "1");
    private SwipeDownDigitDetector mDetector;

    private static Key key(final String label, final String hintLabel) {
        return new Key(label, KeyboardIconsSet.ICON_UNDEFINED, label.codePointAt(0),
                null /* outputText */, hintLabel, 0 /* labelFlags */,
                Key.BACKGROUND_TYPE_NORMAL, 0 /* x */, 0 /* y */, 100 /* width */,
                KEY_HEIGHT, 0 /* horizontalGap */, 0 /* verticalGap */);
    }

    @Before
    public void setUp() {
        mDetector = new SwipeDownDigitDetector();
        mDetector.setKeyHeight(KEY_HEIGHT);
    }

    private void startSwipeDown(final Key key) {
        mDetector.onDownEvent(key, DOWN_X, DOWN_Y);
        mDetector.onMoveEvent(key, DOWN_X, START_Y);
    }

    @Test
    public void testDigitCodeFromDigitHint() {
        assertEquals('1', SwipeDownDigitDetector.getDigitCode(mDigitKey));
        assertEquals('١', SwipeDownDigitDetector.getDigitCode(key("ض", "١")));
    }

    @Test
    public void testNoDigitCodeWithoutDigitHint() {
        assertEquals(Constants.NOT_A_CODE, SwipeDownDigitDetector.getDigitCode(null));
        assertEquals(Constants.NOT_A_CODE, SwipeDownDigitDetector.getDigitCode(key("a", null)));
        assertEquals(Constants.NOT_A_CODE, SwipeDownDigitDetector.getDigitCode(key("a", "@")));
        assertEquals(Constants.NOT_A_CODE, SwipeDownDigitDetector.getDigitCode(key("a", "12")));
    }

    @Test
    public void testSwipedDownPastThreshold() {
        startSwipeDown(mDigitKey);

        assertTrue(mDetector.hasSwipedDown(mDigitKey, DOWN_X + 5, DOWN_Y + THRESHOLD));
    }

    @Test
    public void testSwipingDownBelowThreshold() {
        startSwipeDown(mDigitKey);

        assertTrue(mDetector.isSwipingDown(mDigitKey, DOWN_X, DOWN_Y + THRESHOLD - 1));
        assertFalse(mDetector.hasSwipedDown(mDigitKey, DOWN_X, DOWN_Y + THRESHOLD - 1));
    }

    @Test
    public void testSwipedDownBeyondKey() {
        startSwipeDown(mDigitKey);

        assertTrue(mDetector.hasSwipedDown(mDigitKey, DOWN_X, DOWN_Y + 3 * KEY_HEIGHT));
    }

    @Test
    public void testNotSwipingDownWithoutMoveOnDownKey() {
        mDetector.onDownEvent(mDigitKey, DOWN_X, DOWN_Y);
        mDetector.onMoveEvent(key("a", "@"), DOWN_X, DOWN_Y + KEY_HEIGHT);

        assertFalse(mDetector.hasSwipedDown(mDigitKey, DOWN_X, DOWN_Y + KEY_HEIGHT));
    }

    @Test
    public void testNotSwipingDownWhenMovingUp() {
        startSwipeDown(mDigitKey);

        assertFalse(mDetector.isSwipingDown(mDigitKey, DOWN_X, DOWN_Y - THRESHOLD));
        assertFalse(mDetector.isSwipingDown(mDigitKey, DOWN_X, DOWN_Y));
    }

    @Test
    public void testNotSwipingDownWhenMovingSideways() {
        startSwipeDown(mDigitKey);

        assertFalse(mDetector.hasSwipedDown(mDigitKey, DOWN_X + THRESHOLD, DOWN_Y + THRESHOLD));
        assertFalse(mDetector.hasSwipedDown(mDigitKey, DOWN_X - THRESHOLD, DOWN_Y + THRESHOLD));
    }

    @Test
    public void testNotSwipingDownOnAnotherKey() {
        startSwipeDown(mDigitKey);

        assertFalse(mDetector.hasSwipedDown(key("w", "2"), DOWN_X, DOWN_Y + THRESHOLD));
        assertFalse(mDetector.hasSwipedDown(null, DOWN_X, DOWN_Y + THRESHOLD));
    }

    @Test
    public void testNotSwipingDownFromKeyWithoutDigitHint() {
        final Key letterKey = key("a", null);
        startSwipeDown(letterKey);

        assertFalse(mDetector.hasSwipedDown(letterKey, DOWN_X, DOWN_Y + THRESHOLD));
    }

    @Test
    public void testNotSwipingDownAfterNewDownEvent() {
        startSwipeDown(mDigitKey);
        mDetector.onDownEvent(mDigitKey, DOWN_X, DOWN_Y);

        assertFalse(mDetector.hasSwipedDown(mDigitKey, DOWN_X, DOWN_Y + THRESHOLD));
    }
}
