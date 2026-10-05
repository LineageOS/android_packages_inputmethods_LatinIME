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
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.inputmethod.latin.settings.Settings;

import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class SplitKeyboardUtilsTests {
    private static final int TABLET_SMALLEST_WIDTH_DP = 800;
    private static final int PHONE_SMALLEST_WIDTH_DP = 400;

    private static Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    private static Resources resourcesFor(final int smallestWidthDp, final int orientation) {
        final Configuration config =
                new Configuration(getContext().getResources().getConfiguration());
        config.smallestScreenWidthDp = smallestWidthDp;
        config.orientation = orientation;
        return getContext().createConfigurationContext(config).getResources();
    }

    @Test
    public void testAvailableOnTabletInLandscape() {
        assertTrue(SplitKeyboardUtils.isSplitKeyboardAvailable(resourcesFor(
                TABLET_SMALLEST_WIDTH_DP, Configuration.ORIENTATION_LANDSCAPE)));
    }

    @Test
    public void testUnavailableOnTabletInPortrait() {
        assertFalse(SplitKeyboardUtils.isSplitKeyboardAvailable(resourcesFor(
                TABLET_SMALLEST_WIDTH_DP, Configuration.ORIENTATION_PORTRAIT)));
    }

    @Test
    public void testUnavailableOnPhoneInLandscape() {
        assertFalse(SplitKeyboardUtils.isSplitKeyboardAvailable(resourcesFor(
                PHONE_SMALLEST_WIDTH_DP, Configuration.ORIENTATION_LANDSCAPE)));
    }

    @Test
    public void testUnavailableOnLargePhonesInLandscape() {
        for (final int smallestWidthDp : new int[] { 430, 599 }) {
            assertFalse("sw" + smallestWidthDp, SplitKeyboardUtils.isSplitKeyboardAvailable(
                    resourcesFor(smallestWidthDp, Configuration.ORIENTATION_LANDSCAPE)));
        }
    }

    @Test
    public void testAvailableOnAllTabletSizesInLandscape() {
        for (final int smallestWidthDp : new int[] { 600, 768 }) {
            assertTrue("sw" + smallestWidthDp, SplitKeyboardUtils.isSplitKeyboardAvailable(
                    resourcesFor(smallestWidthDp, Configuration.ORIENTATION_LANDSCAPE)));
        }
    }

    @Test
    public void testSplitKeyboardHeightIsReduced() {
        assertEquals(780, SplitKeyboardUtils.getSplitKeyboardHeight(1000));
        assertEquals(0, SplitKeyboardUtils.getSplitKeyboardHeight(0));
    }

    @Test
    public void testToggleFlipsSplitPreference() {
        final SharedPreferences prefs = getContext().getSharedPreferences(
                "split_keyboard_utils_tests", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();

        assertTrue(SplitKeyboardUtils.toggleSplitKeyboard(prefs));
        assertTrue(prefs.getBoolean(Settings.PREF_ENABLE_SPLIT_KEYBOARD, false));

        assertFalse(SplitKeyboardUtils.toggleSplitKeyboard(prefs));
        assertFalse(prefs.getBoolean(Settings.PREF_ENABLE_SPLIT_KEYBOARD, true));
    }
}
