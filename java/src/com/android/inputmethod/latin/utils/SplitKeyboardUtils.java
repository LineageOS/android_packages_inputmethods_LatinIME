/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;

import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.define.ProductionFlags;
import com.android.inputmethod.latin.settings.Settings;

import javax.annotation.Nonnull;

public final class SplitKeyboardUtils {
    public static final float SPLIT_KEYBOARD_HEIGHT_RATIO = 0.78f;
    private static final int PHONE_SMALLEST_SCREEN_WIDTH_DP = 360;

    private SplitKeyboardUtils() {
    }

    public static boolean isSplitKeyboardAvailable(@Nonnull final Resources res) {
        return ProductionFlags.IS_SPLIT_KEYBOARD_SUPPORTED
                && !Constants.isPhone(Settings.readScreenMetrics(res))
                && res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Nonnull
    public static Context createPhoneLayoutContext(@Nonnull final Context context) {
        final Configuration config =
                new Configuration(context.getResources().getConfiguration());
        config.smallestScreenWidthDp = PHONE_SMALLEST_SCREEN_WIDTH_DP;
        final Context configContext = context.createConfigurationContext(config);
        final Resources.Theme theme = configContext.getResources().newTheme();
        theme.setTo(context.getTheme());
        return new ContextThemeWrapper(configContext, theme);
    }

    public static int getSplitKeyboardHeight(final int keyboardHeight) {
        return Math.round(keyboardHeight * SPLIT_KEYBOARD_HEIGHT_RATIO);
    }

    public static boolean toggleSplitKeyboard(@Nonnull final SharedPreferences prefs) {
        final boolean enabled = !prefs.getBoolean(Settings.PREF_ENABLE_SPLIT_KEYBOARD, false);
        prefs.edit().putBoolean(Settings.PREF_ENABLE_SPLIT_KEYBOARD, enabled).apply();
        return enabled;
    }
}
