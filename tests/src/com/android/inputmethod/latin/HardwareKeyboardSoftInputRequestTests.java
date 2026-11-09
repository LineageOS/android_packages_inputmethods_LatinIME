/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethod;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class HardwareKeyboardSoftInputRequestTests {
    private final HardwareKeyboardSoftInputRequest mRequest =
            new HardwareKeyboardSoftInputRequest();

    @Test
    public void testExplicitShowRequestsSoftInput() {
        mRequest.onShowInputRequested(InputMethod.SHOW_EXPLICIT);

        assertTrue(mRequest.isRequested());
    }

    @Test
    public void testImplicitShowDoesNotRequestSoftInput() {
        mRequest.onShowInputRequested(0);

        assertFalse(mRequest.isRequested());
    }

    @Test
    public void testImplicitShowKeepsExplicitRequest() {
        mRequest.onShowInputRequested(InputMethod.SHOW_EXPLICIT);

        mRequest.onShowInputRequested(0);

        assertTrue(mRequest.isRequested());
    }

    @Test
    public void testResetDropsRequest() {
        mRequest.onShowInputRequested(InputMethod.SHOW_EXPLICIT);

        mRequest.reset();

        assertFalse(mRequest.isRequested());
    }

    @Test
    public void testVirtualKeyIsNotFromHardwareKeyboard() {
        final KeyEvent event = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0);

        assertFalse(HardwareKeyboardSoftInputRequest.isFromHardwareKeyboard(event));
    }

    @Test
    public void testSystemKeyIsNotFromHardwareKeyboard() {
        final KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP);

        assertFalse(HardwareKeyboardSoftInputRequest.isFromHardwareKeyboard(event));
    }
}
