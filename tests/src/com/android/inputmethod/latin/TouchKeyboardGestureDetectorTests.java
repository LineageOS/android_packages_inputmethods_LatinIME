/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin;

import static org.junit.Assert.assertEquals;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.inputmethod.latin.TouchKeyboardGestureDetector.Swipe;

import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class TouchKeyboardGestureDetectorTests {
    private static final float UNITS_PER_DP = 2f;
    private static final float FAST = 2000f;

    @Test
    public void testFastUpwardSwipeIsSwipeUp() {
        assertEquals(Swipe.UP, TouchKeyboardGestureDetector.classifySwipe(
                0f, -100f, 0f, -FAST, UNITS_PER_DP));
    }

    @Test
    public void testFastDownwardSwipeIsSwipeDown() {
        assertEquals(Swipe.DOWN, TouchKeyboardGestureDetector.classifySwipe(
                10f, 100f, 0f, FAST, UNITS_PER_DP));
    }

    @Test
    public void testLongLeftwardSwipeIsSwipeLeft() {
        assertEquals(Swipe.LEFT, TouchKeyboardGestureDetector.classifySwipe(
                -200f, 20f, -FAST, 0f, UNITS_PER_DP));
    }

    @Test
    public void testLongRightwardSwipeIsSwipeRight() {
        assertEquals(Swipe.RIGHT, TouchKeyboardGestureDetector.classifySwipe(
                200f, 0f, FAST, 0f, UNITS_PER_DP));
    }

    @Test
    public void testShortHorizontalSwipeIsIgnored() {
        assertEquals(Swipe.NONE, TouchKeyboardGestureDetector.classifySwipe(
                -100f, 0f, -FAST, 0f, UNITS_PER_DP));
    }

    @Test
    public void testSlowSwipeIsIgnored() {
        assertEquals(Swipe.NONE, TouchKeyboardGestureDetector.classifySwipe(
                0f, -100f, 0f, -500f, UNITS_PER_DP));
    }

    @Test
    public void testDiagonalSwipeIsIgnored() {
        assertEquals(Swipe.NONE, TouchKeyboardGestureDetector.classifySwipe(
                200f, 150f, FAST, FAST, UNITS_PER_DP));
    }
}
