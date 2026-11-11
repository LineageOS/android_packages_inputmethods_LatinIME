/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard;

import android.view.KeyEvent;

import com.android.inputmethod.latin.R;

import javax.annotation.Nullable;

public final class HardwareKeyboardLayouts {
    private static final int FULL_KEYBOARD_ROW_COUNT = 4;
    private static final int BAR_ROW_COUNT = 1;
    private static final int PASSWORD_BAR_ROW_COUNT = 2;
    private static final int NUMBER_PAD_ROW_COUNT = 3;

    private HardwareKeyboardLayouts() {
    }

    static int getKeyboardXmlId(final int elementId) {
        switch (elementId) {
        case KeyboardId.ELEMENT_ALPHABET:
        case KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED:
        case KeyboardId.ELEMENT_ALPHABET_AUTOMATIC_SHIFTED:
        case KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCKED:
        case KeyboardId.ELEMENT_ALPHABET_SHIFT_LOCK_SHIFTED:
            return R.xml.kbd_hardware_keyboard_bar;
        case KeyboardId.ELEMENT_SYMBOLS:
            return R.xml.kbd_hardware_keyboard_symbols;
        case KeyboardId.ELEMENT_SYMBOLS_SHIFTED:
            return R.xml.kbd_hardware_keyboard_symbols_shift;
        case KeyboardId.ELEMENT_NUMBER:
            return R.xml.kbd_hardware_keyboard_number;
        default:
            return 0;
        }
    }

    static int getKeyboardHeight(final int elementId, final boolean isPasswordInput,
            final int fullKeyboardHeight) {
        return fullKeyboardHeight * getRowCount(elementId, isPasswordInput)
                / FULL_KEYBOARD_ROW_COUNT;
    }

    private static int getRowCount(final int elementId, final boolean isPasswordInput) {
        if (elementId == KeyboardId.ELEMENT_NUMBER) {
            return NUMBER_PAD_ROW_COUNT;
        }
        if (elementId < KeyboardId.ELEMENT_SYMBOLS) {
            return isPasswordInput ? PASSWORD_BAR_ROW_COUNT : BAR_ROW_COUNT;
        }
        return FULL_KEYBOARD_ROW_COUNT;
    }

    @Nullable
    public static Key findKeyMappedToHardwareKey(@Nullable final Keyboard keyboard,
            final KeyEvent keyEvent) {
        if (keyboard == null || !keyboard.mId.mIsHardwareKeyboardLayout
                || keyboard.mId.isAlphabetKeyboard()) {
            return null;
        }
        final int codePoint = keyEvent.getUnicodeChar(0);
        if (codePoint == 0) {
            return null;
        }
        final String keyLabel = new String(Character.toChars(codePoint));
        for (final Key key : keyboard.getSortedKeys()) {
            if (keyLabel.equalsIgnoreCase(key.getHintLabel())) {
                return key;
            }
        }
        return null;
    }
}
