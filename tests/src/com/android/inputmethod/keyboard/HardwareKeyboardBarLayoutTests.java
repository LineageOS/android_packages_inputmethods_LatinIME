/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard;

import android.text.InputType;
import android.view.KeyEvent;
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
                .setHardwareKeyboardLayoutEnabled(true)
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
        assertTrue(keyboard.mId.mIsHardwareKeyboardLayout);
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
        assertTrue(keyboard.mId.mIsHardwareKeyboardLayout);
        assertTrue(hasKey(keyboard, Constants.CODE_SHIFT));
    }

    public void testPasswordBarAddsNumberRow() {
        final Keyboard keyboard = buildLayoutSet(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertEquals(getKeyboardHeight() / 2, keyboard.mId.mHeight);
        assertTrue(hasKey(keyboard, '1'));
        assertTrue(hasKey(keyboard, Constants.CODE_SHIFT));
    }

    public void testUrlBarHasSlash() {
        final Keyboard keyboard = buildLayoutSet(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertTrue(hasKey(keyboard, '/'));
        assertFalse(hasKey(keyboard, '!'));
    }

    public void testEmailBarHasAtSign() {
        final Keyboard keyboard = buildLayoutSet(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertTrue(hasKey(keyboard, '@'));
    }

    public void testSymbolsKeyboardIsCompactPanel() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_TEXT)
                .getKeyboard(KeyboardId.ELEMENT_SYMBOLS);
        assertTrue(keyboard.mId.mIsHardwareKeyboardLayout);
        assertFalse(keyboard.mId.mIsSplitLayout);
        assertEquals(getKeyboardHeight(), keyboard.mId.mHeight);
        assertTrue(hasKey(keyboard, '1'));
        assertTrue(hasKey(keyboard, Constants.CODE_SWITCH_ALPHA_SYMBOL));
    }

    public void testSymbolsKeyboardMapsDigitsToTopLetterRow() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_TEXT)
                .getKeyboard(KeyboardId.ELEMENT_SYMBOLS);
        assertEquals('1', findMappedKey(keyboard, KeyEvent.KEYCODE_Q).getCode());
        assertEquals('0', findMappedKey(keyboard, KeyEvent.KEYCODE_P).getCode());
        assertEquals('=', findMappedKey(keyboard, KeyEvent.KEYCODE_A).getCode());
        assertEquals('-', findMappedKey(keyboard, KeyEvent.KEYCODE_Z).getCode());
    }

    public void testShiftedSymbolsKeyboardMapsEveryLetter() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_TEXT)
                .getKeyboard(KeyboardId.ELEMENT_SYMBOLS_SHIFTED);
        assertTrue(keyboard.mId.mIsHardwareKeyboardLayout);
        for (int keyCode = KeyEvent.KEYCODE_A; keyCode <= KeyEvent.KEYCODE_Z; keyCode++) {
            assertNotNull(KeyEvent.keyCodeToString(keyCode), findMappedKey(keyboard, keyCode));
        }
    }

    public void testAlphabetBarDoesNotMapHardwareKeys() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_TEXT)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertNull(findMappedKey(keyboard, KeyEvent.KEYCODE_Q));
    }

    public void testNumberFieldUsesHardwareNumberPad() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_NUMBER)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertTrue(keyboard.mId.mIsHardwareKeyboardLayout);
        assertEquals(KeyboardId.ELEMENT_NUMBER, keyboard.mId.mElementId);
        assertEquals(getKeyboardHeight() * 3 / 4, keyboard.mId.mHeight);
        assertTrue(hasKey(keyboard, '0'));
        assertTrue(hasKey(keyboard, '9'));
    }

    public void testNumberPadMapsDigitsToLetterKeys() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_NUMBER)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertEquals('1', findMappedKey(keyboard, KeyEvent.KEYCODE_W).getCode());
        assertEquals('9', findMappedKey(keyboard, KeyEvent.KEYCODE_V).getCode());
    }

    public void testPhoneFieldKeepsPhoneKeyboard() {
        final Keyboard keyboard = buildLayoutSet(InputType.TYPE_CLASS_PHONE)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertFalse(keyboard.mId.mIsHardwareKeyboardLayout);
        assertEquals(KeyboardId.ELEMENT_PHONE, keyboard.mId.mElementId);
    }

    private static Key findMappedKey(final Keyboard keyboard, final int keyCode) {
        return HardwareKeyboardLayouts.findKeyMappedToHardwareKey(keyboard,
                new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
    }

    public void testOnlySymbolsPanelsCloseOnSwipeDown() {
        final KeyboardLayoutSet layoutSet = buildLayoutSet(InputType.TYPE_CLASS_TEXT);
        assertTrue(HardwareKeyboardLayouts.isSymbolsPanel(
                layoutSet.getKeyboard(KeyboardId.ELEMENT_SYMBOLS)));
        assertTrue(HardwareKeyboardLayouts.isSymbolsPanel(
                layoutSet.getKeyboard(KeyboardId.ELEMENT_SYMBOLS_SHIFTED)));
        assertFalse(HardwareKeyboardLayouts.isSymbolsPanel(
                layoutSet.getKeyboard(KeyboardId.ELEMENT_ALPHABET)));
        assertFalse(HardwareKeyboardLayouts.isSymbolsPanel(
                buildLayoutSet(InputType.TYPE_CLASS_NUMBER)
                        .getKeyboard(KeyboardId.ELEMENT_ALPHABET)));
    }
}
