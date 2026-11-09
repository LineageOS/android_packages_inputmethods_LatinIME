/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethod;

final class HardwareKeyboardSoftInputRequest {
    private boolean mIsRequested;

    boolean isRequested() {
        return mIsRequested;
    }

    void onShowInputRequested(final int flags) {
        if ((flags & InputMethod.SHOW_EXPLICIT) != 0) {
            mIsRequested = true;
        }
    }

    void reset() {
        mIsRequested = false;
    }

    static boolean isFromHardwareKeyboard(final KeyEvent keyEvent) {
        if (keyEvent.isSystem()) {
            return false;
        }
        final InputDevice device = keyEvent.getDevice();
        return device != null && !device.isVirtual()
                && device.getKeyboardType() == InputDevice.KEYBOARD_TYPE_ALPHABETIC;
    }
}
