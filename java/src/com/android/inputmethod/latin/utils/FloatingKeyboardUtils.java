/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.utils;

import android.content.SharedPreferences;
import android.graphics.Point;

import com.android.inputmethod.latin.settings.Settings;

import javax.annotation.Nonnull;

public final class FloatingKeyboardUtils {
    public static final float PANEL_WIDTH_RATIO = 0.8f;
    public static final int PANEL_MAX_WIDTH_DP = 370;
    public static final int PANEL_MIN_WIDTH_DP = 280;
    public static final float PANEL_MAX_SCREEN_RATIO = 0.95f;
    public static final int KEYBOARD_MIN_HEIGHT_DP = 140;
    public static final float KEYBOARD_MAX_SCREEN_RATIO = 0.6f;
    public static final float KEYBOARD_HEIGHT_TO_WIDTH_RATIO = 0.6f;
    public static final int DOCK_THRESHOLD_DP = 48;
    public static final int PANEL_PADDING_DP = 8;
    public static final int PANEL_CORNER_RADIUS_DP = 16;
    private static final float DEFAULT_X_FRACTION = 0.5f;
    private static final float DEFAULT_Y_FRACTION = 0.85f;
    private static final String PREF_POSITION_X = "pref_floating_keyboard_x_";
    private static final String PREF_POSITION_Y = "pref_floating_keyboard_y_";
    private static final String PREF_WIDTH_DP = "pref_floating_keyboard_width_dp_";
    private static final String PREF_HEIGHT_DP = "pref_floating_keyboard_height_dp_";
    private static final String LANDSCAPE = "land";
    private static final String PORTRAIT = "port";

    private FloatingKeyboardUtils() {
    }

    public static int getPanelWidth(final int screenWidth, final float density) {
        return Math.min(Math.round(screenWidth * PANEL_WIDTH_RATIO),
                Math.round(PANEL_MAX_WIDTH_DP * density));
    }

    public static int clampPanelWidth(final int width, final int screenWidth,
            final float density) {
        return Math.max(dpToPx(PANEL_MIN_WIDTH_DP, density),
                Math.min(width, Math.round(screenWidth * PANEL_MAX_SCREEN_RATIO)));
    }

    public static int loadPanelWidth(@Nonnull final SharedPreferences prefs,
            final boolean isLandscape, final int screenWidth, final float density) {
        final String key = PREF_WIDTH_DP + (isLandscape ? LANDSCAPE : PORTRAIT);
        if (!prefs.contains(key)) {
            return getPanelWidth(screenWidth, density);
        }
        return clampPanelWidth(Math.round(prefs.getFloat(key, 0) * density), screenWidth,
                density);
    }

    public static void savePanelWidth(@Nonnull final SharedPreferences prefs,
            final boolean isLandscape, final int width, final float density) {
        prefs.edit().putFloat(PREF_WIDTH_DP + (isLandscape ? LANDSCAPE : PORTRAIT),
                width / density).apply();
    }

    public static int clampKeyboardHeight(final int height, final int screenHeight,
            final float density) {
        return Math.max(dpToPx(KEYBOARD_MIN_HEIGHT_DP, density),
                Math.min(height, Math.round(screenHeight * KEYBOARD_MAX_SCREEN_RATIO)));
    }

    public static int loadKeyboardHeight(@Nonnull final SharedPreferences prefs,
            final boolean isLandscape, final int keyboardWidth, final int screenHeight,
            final float density) {
        final String key = PREF_HEIGHT_DP + (isLandscape ? LANDSCAPE : PORTRAIT);
        if (!prefs.contains(key)) {
            return getKeyboardHeight(keyboardWidth);
        }
        return clampKeyboardHeight(Math.round(prefs.getFloat(key, 0) * density), screenHeight,
                density);
    }

    public static void saveKeyboardHeight(@Nonnull final SharedPreferences prefs,
            final boolean isLandscape, final int height, final float density) {
        prefs.edit().putFloat(PREF_HEIGHT_DP + (isLandscape ? LANDSCAPE : PORTRAIT),
                height / density).apply();
    }

    public static int dpToPx(final int dp, final float density) {
        return Math.round(dp * density);
    }

    public static int getKeyboardHeight(final int keyboardWidth) {
        return Math.round(keyboardWidth * KEYBOARD_HEIGHT_TO_WIDTH_RATIO);
    }

    @Nonnull
    public static Point clampPosition(final int x, final int y, final int panelWidth,
            final int panelHeight, final int areaWidth, final int areaHeight) {
        return new Point(clamp(x, areaWidth - panelWidth), clamp(y, areaHeight - panelHeight));
    }

    public static boolean shouldDock(final int panelBottom, final int areaHeight,
            final float density) {
        return areaHeight - panelBottom <= Math.round(DOCK_THRESHOLD_DP * density);
    }

    @Nonnull
    public static Point loadPosition(@Nonnull final SharedPreferences prefs,
            final boolean isLandscape, final int panelWidth, final int panelHeight,
            final int areaWidth, final int areaHeight) {
        final String orientation = isLandscape ? LANDSCAPE : PORTRAIT;
        final float xFraction = prefs.getFloat(PREF_POSITION_X + orientation, DEFAULT_X_FRACTION);
        final float yFraction = prefs.getFloat(PREF_POSITION_Y + orientation, DEFAULT_Y_FRACTION);
        return clampPosition(Math.round(xFraction * (areaWidth - panelWidth)),
                Math.round(yFraction * (areaHeight - panelHeight)),
                panelWidth, panelHeight, areaWidth, areaHeight);
    }

    public static void savePosition(@Nonnull final SharedPreferences prefs,
            final boolean isLandscape, final int x, final int y, final int panelWidth,
            final int panelHeight, final int areaWidth, final int areaHeight) {
        final String orientation = isLandscape ? LANDSCAPE : PORTRAIT;
        prefs.edit()
                .putFloat(PREF_POSITION_X + orientation, fraction(x, areaWidth - panelWidth))
                .putFloat(PREF_POSITION_Y + orientation, fraction(y, areaHeight - panelHeight))
                .apply();
    }

    public static void resetPanel(@Nonnull final SharedPreferences prefs,
            final boolean isLandscape) {
        final String orientation = isLandscape ? LANDSCAPE : PORTRAIT;
        prefs.edit()
                .remove(PREF_POSITION_X + orientation)
                .remove(PREF_POSITION_Y + orientation)
                .remove(PREF_WIDTH_DP + orientation)
                .remove(PREF_HEIGHT_DP + orientation)
                .apply();
    }

    public static boolean toggleFloatingKeyboard(@Nonnull final SharedPreferences prefs) {
        final boolean enabled = !prefs.getBoolean(Settings.PREF_FLOATING_KEYBOARD, false);
        prefs.edit().putBoolean(Settings.PREF_FLOATING_KEYBOARD, enabled).apply();
        return enabled;
    }

    private static int clamp(final int value, final int max) {
        return Math.max(0, Math.min(value, Math.max(0, max)));
    }

    private static float fraction(final int value, final int range) {
        return range <= 0 ? 0f : (float) value / range;
    }
}
