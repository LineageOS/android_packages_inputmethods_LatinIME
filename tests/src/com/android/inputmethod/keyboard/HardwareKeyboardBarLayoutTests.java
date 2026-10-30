/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard;

import android.text.InputType;
import android.view.inputmethod.EditorInfo;

import androidx.test.filters.SmallTest;

import com.android.inputmethod.latin.RichInputMethodSubtype;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.utils.ResourceUtils;

import java.util.Locale;

@SmallTest
public class HardwareKeyboardBarLayoutTests extends KeyboardLayoutSetTestsBase {
    private static final int KEYBOARD_WIDTH = 1080;

    @Override
    protected int getKeyboardThemeForTests() {
        return KeyboardTheme.THEME_ID_LXX_LIGHT;
    }

    private int getKeyboardHeight() {
        return ResourceUtils.getDefaultKeyboardHeight(getContext().getResources());
    }

    private KeyboardLayoutSet buildLayoutSet(final int inputType) {
        final EditorInfo editorInfo = new EditorInfo();
        editorInfo.inputType = inputType;
        return new KeyboardLayoutSet.Builder(getContext(), editorInfo)
                .setKeyboardGeometry(KEYBOARD_WIDTH, getKeyboardHeight())
                .setSubtype(RichInputMethodSubtype.getRichInputMethodSubtype(
                        getSubtype(Locale.US, "qwerty")))
                .setSplitLayoutEnabledByUser(true)
                .setHardwareKeyboardBarEnabled(true)
                .build();
    }

    private static boolean hasKey(final Keyboard keyboard, final int code) {
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.getCode() == code) {
                return true;
            }
        }
        return false;
    }

    public void testAlphabetKeyboardIsSingleRowBar() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_TEXT)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertTrue(keyboard.mId.mIsHardwareKeyboardBar);
        assertFalse(keyboard.mId.mIsSplitLayout);
        assertTrue(keyboard.mOccupiedHeight < getKeyboardHeight());
        assertTrue(hasKey(keyboard, Constants.CODE_SHIFT));
        assertTrue(hasKey(keyboard, Constants.CODE_SWITCH_ALPHA_SYMBOL));
        assertFalse(hasKey(keyboard, 'q'));
        final int rowY = keyboard.getSortedKeys().get(0).getY();
        for (final Key key : keyboard.getSortedKeys()) {
            assertEquals("row of " + key, rowY, key.getY());
        }
    }

    public void testShiftedAlphabetKeyboardIsBar() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_TEXT)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED);
        assertTrue(keyboard.mId.mIsHardwareKeyboardBar);
        assertTrue(hasKey(keyboard, Constants.CODE_SHIFT));
    }

    public void testSymbolsKeyboardIsFullLayout() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_TEXT)
                .getKeyboard(KeyboardId.ELEMENT_SYMBOLS);
        assertFalse(keyboard.mId.mIsHardwareKeyboardBar);
        assertEquals(getKeyboardHeight(), keyboard.mOccupiedHeight);
        assertTrue(hasKey(keyboard, '1'));
    }

    public void testNumberFieldUsesNumberKeyboard() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_NUMBER)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertFalse(keyboard.mId.mIsHardwareKeyboardBar);
        assertEquals(KeyboardId.ELEMENT_NUMBER, keyboard.mId.mElementId);
    }
}
