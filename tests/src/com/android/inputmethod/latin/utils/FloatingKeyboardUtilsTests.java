/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.inputmethod.latin.settings.Settings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class FloatingKeyboardUtilsTests {
    private SharedPreferences mPrefs;

    @Before
    public void setUp() {
        mPrefs = InstrumentationRegistry.getTargetContext().getSharedPreferences(
                "floating_keyboard_utils_tests", Context.MODE_PRIVATE);
        mPrefs.edit().clear().commit();
    }

    @Test
    public void testPanelWidthIsCappedOnLargeScreens() {
        assertEquals(740, FloatingKeyboardUtils.getPanelWidth(2000, 2.0f));
    }

    @Test
    public void testPanelWidthIsFractionOfSmallScreens() {
        assertEquals(720, FloatingKeyboardUtils.getPanelWidth(900, 2.625f));
    }

    @Test
    public void testKeyboardHeightFollowsPanelWidth() {
        assertEquals(600, FloatingKeyboardUtils.getKeyboardHeight(1000));
    }

    @Test
    public void testClampKeepsPanelOnScreen() {
        assertEquals(new Point(0, 0),
                FloatingKeyboardUtils.clampPosition(-50, -20, 800, 400, 2000, 1000));
        assertEquals(new Point(1200, 600),
                FloatingKeyboardUtils.clampPosition(1900, 900, 800, 400, 2000, 1000));
        assertEquals(new Point(300, 200),
                FloatingKeyboardUtils.clampPosition(300, 200, 800, 400, 2000, 1000));
    }

    @Test
    public void testDocksOnlyNearBottomEdge() {
        assertTrue(FloatingKeyboardUtils.shouldDock(950, 1000, 2.0f));
        assertFalse(FloatingKeyboardUtils.shouldDock(800, 1000, 2.0f));
    }

    @Test
    public void testDefaultPositionIsCenteredNearBottom() {
        final Point position =
                FloatingKeyboardUtils.loadPosition(mPrefs, true, 800, 400, 2000, 1000);
        assertEquals(600, position.x);
        assertEquals(510, position.y);
    }

    @Test
    public void testPositionRoundTripsAndScalesWithScreen() {
        FloatingKeyboardUtils.savePosition(mPrefs, true, 300, 200, 800, 400, 2000, 1000);
        assertEquals(new Point(300, 200),
                FloatingKeyboardUtils.loadPosition(mPrefs, true, 800, 400, 2000, 1000));
        assertEquals(new Point(250, 200),
                FloatingKeyboardUtils.loadPosition(mPrefs, true, 800, 400, 1800, 1000));
    }

    @Test
    public void testPositionsArePerOrientation() {
        FloatingKeyboardUtils.savePosition(mPrefs, true, 0, 0, 800, 400, 2000, 1000);
        final Point portrait =
                FloatingKeyboardUtils.loadPosition(mPrefs, false, 800, 400, 2000, 1000);
        assertEquals(600, portrait.x);
        assertEquals(510, portrait.y);
    }

    @Test
    public void testPanelWidthDefaultsWhenNotResized() {
        assertEquals(740, FloatingKeyboardUtils.loadPanelWidth(mPrefs, true, 2000, 2.0f));
    }

    @Test
    public void testResizedPanelWidthIsRestoredPerOrientation() {
        FloatingKeyboardUtils.savePanelWidth(mPrefs, true, 1000, 2.0f);
        assertEquals(1000, FloatingKeyboardUtils.loadPanelWidth(mPrefs, true, 2000, 2.0f));
        assertEquals(740, FloatingKeyboardUtils.loadPanelWidth(mPrefs, false, 2000, 2.0f));
    }

    @Test
    public void testPanelWidthIsClampedToScreen() {
        assertEquals(560, FloatingKeyboardUtils.clampPanelWidth(100, 2000, 2.0f));
        assertEquals(1900, FloatingKeyboardUtils.clampPanelWidth(5000, 2000, 2.0f));
        FloatingKeyboardUtils.savePanelWidth(mPrefs, true, 1800, 2.0f);
        assertEquals(1520, FloatingKeyboardUtils.loadPanelWidth(mPrefs, true, 1600, 2.0f));
    }

    @Test
    public void testKeyboardHeightDefaultsToWidthRatio() {
        assertEquals(600, FloatingKeyboardUtils.loadKeyboardHeight(mPrefs, true, 1000, 1600, 2.0f));
    }

    @Test
    public void testResizedKeyboardHeightIsRestoredPerOrientation() {
        FloatingKeyboardUtils.saveKeyboardHeight(mPrefs, true, 500, 2.0f);
        assertEquals(500, FloatingKeyboardUtils.loadKeyboardHeight(mPrefs, true, 1000, 1600, 2.0f));
        assertEquals(600,
                FloatingKeyboardUtils.loadKeyboardHeight(mPrefs, false, 1000, 1600, 2.0f));
    }

    @Test
    public void testKeyboardHeightIsClamped() {
        assertEquals(280, FloatingKeyboardUtils.clampKeyboardHeight(100, 1600, 2.0f));
        assertEquals(960, FloatingKeyboardUtils.clampKeyboardHeight(2000, 1600, 2.0f));
    }

    @Test
    public void testResetRestoresDefaultsForOrientationOnly() {
        FloatingKeyboardUtils.savePanelWidth(mPrefs, true, 1000, 2.0f);
        FloatingKeyboardUtils.saveKeyboardHeight(mPrefs, true, 500, 2.0f);
        FloatingKeyboardUtils.savePosition(mPrefs, true, 0, 0, 800, 400, 2000, 1000);
        FloatingKeyboardUtils.savePanelWidth(mPrefs, false, 1000, 2.0f);
        FloatingKeyboardUtils.resetPanel(mPrefs, true);
        assertEquals(740, FloatingKeyboardUtils.loadPanelWidth(mPrefs, true, 2000, 2.0f));
        assertEquals(600, FloatingKeyboardUtils.loadKeyboardHeight(mPrefs, true, 1000, 1600, 2.0f));
        assertEquals(new Point(600, 510),
                FloatingKeyboardUtils.loadPosition(mPrefs, true, 800, 400, 2000, 1000));
        assertEquals(1000, FloatingKeyboardUtils.loadPanelWidth(mPrefs, false, 2000, 2.0f));
    }

    @Test
    public void testToggleFlipsFloatingPreference() {
        assertTrue(FloatingKeyboardUtils.toggleFloatingKeyboard(mPrefs));
        assertTrue(mPrefs.getBoolean(Settings.PREF_FLOATING_KEYBOARD, false));
        assertFalse(FloatingKeyboardUtils.toggleFloatingKeyboard(mPrefs));
        assertFalse(mPrefs.getBoolean(Settings.PREF_FLOATING_KEYBOARD, true));
    }
}
