/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard;

import android.view.inputmethod.EditorInfo;

import androidx.test.filters.SmallTest;

import com.android.inputmethod.latin.RichInputMethodSubtype;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.utils.ResourceUtils;

import java.util.Locale;

@SmallTest
public class FloatingKeyboardLayoutTests extends KeyboardLayoutSetTestsBase {
    private static final int PANEL_WIDTH = 800;

    @Override
    protected int getKeyboardThemeForTests() {
        return KeyboardTheme.THEME_ID_LXX_LIGHT;
    }

    private Keyboard buildFloating(final boolean splitLayoutEnabled) {
        final KeyboardLayoutSet.Builder builder =
                new KeyboardLayoutSet.Builder(getContext(), new EditorInfo());
        builder.setKeyboardGeometry(PANEL_WIDTH,
                        ResourceUtils.getDefaultKeyboardHeight(getContext().getResources()))
                .setSubtype(RichInputMethodSubtype.getRichInputMethodSubtype(
                        getSubtype(Locale.US, "qwerty")))
                .setSplitLayoutEnabledByUser(splitLayoutEnabled)
                .setFloatingKeyboard(true);
        return builder.build().getKeyboard(KeyboardId.ELEMENT_ALPHABET);
    }

    public void testFloatingKeyboardUsesPanelWidth() {
        final Keyboard keyboard = buildFloating(false);
        assertTrue(keyboard.mId.mIsFloating);
        assertEquals(PANEL_WIDTH, keyboard.mOccupiedWidth);
    }

    public void testFloatingKeyboardUsesPhoneLayout() {
        final Keyboard keyboard = buildFloating(false);
        int topRow = -1;
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.getCode() == 'q') topRow = key.getY();
        }
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.getY() == topRow) {
                assertTrue("top row " + key, key.getCode() != Constants.CODE_DELETE);
            }
        }
    }

    public void testFloatingKeyboardIsNeverSplit() {
        final Keyboard keyboard = buildFloating(true);
        assertFalse(keyboard.mId.mIsSplitLayout);
    }
}
