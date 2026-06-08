/*
 * Copyright (C) 2026, The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.latin;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.android.inputmethod.event.Event;

import javax.annotation.Nonnull;

/**
 * Tracks the physical Alt key as a symbol/number layer for hardware keyboards that print digits
 * and symbols on the Alt layer of each key. The layer can be reached three ways:
 * <ul>
 *   <li><b>One-shot</b>: tap Alt, then a key applies the layer to that one key.</li>
 *   <li><b>Latched</b>: double-tap Alt keeps the layer on until tapped off.</li>
 *   <li><b>Held</b>: hold Alt while pressing keys.</li>
 * </ul>
 * The symbol is resolved from the key character map via {@link KeyEvent#getUnicodeChar(int)} with
 * the Alt meta bit set, so no symbol table is bundled here.
 */
final class SymbolAltPhysicalKeyDetector {

    private enum AltState { OFF, ONE_SHOT, LATCHED }

    // A second Alt tap within this window (since the previous tap) latches the layer.
    private static final long DOUBLE_TAP_TIMEOUT_MS = 500;

    private AltState mState = AltState.OFF;
    private boolean mPhysicallyHeld;
    // True once a key was used while Alt was held, so the Alt key-up counts as a chord, not a tap.
    private boolean mChordSinceAltDown;
    private long mLastAltTapTime;

    static boolean isAltKey(final int keyCode) {
        return keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT;
    }

    /**
     * Feed an Alt key down/up event. Drives the one-shot / latch / held state machine.
     */
    void onAltKey(@Nonnull final KeyEvent keyEvent) {
        switch (keyEvent.getAction()) {
            case KeyEvent.ACTION_DOWN:
                if (keyEvent.getRepeatCount() == 0) {
                    mPhysicallyHeld = true;
                    mChordSinceAltDown = false;
                }
                break;
            case KeyEvent.ACTION_UP:
                mPhysicallyHeld = false;
                // A tap (no key used while Alt was down) toggles the sticky state.
                if (!mChordSinceAltDown) {
                    registerAltTap(keyEvent.getEventTime());
                }
                mChordSinceAltDown = false;
                break;
            default:
                break;
        }
    }

    private void registerAltTap(final long eventTime) {
        final boolean doubleTap = eventTime - mLastAltTapTime < DOUBLE_TAP_TIMEOUT_MS;
        mLastAltTapTime = eventTime;
        if (doubleTap) {
            mState = (mState == AltState.LATCHED) ? AltState.OFF : AltState.LATCHED;
        } else if (mState == AltState.OFF) {
            mState = AltState.ONE_SHOT;
        } else {
            // A single tap while ONE_SHOT or LATCHED turns the layer off.
            mState = AltState.OFF;
        }
    }

    /**
     * @return true if the next printing key should be resolved through the Alt symbol layer.
     */
    boolean isAltActive() {
        return mPhysicallyHeld || mState != AltState.OFF;
    }

    /**
     * Resolve the Alt-layer code point for a printing key from the device key character map.
     *
     * @return the mapped code point, or {@link Event#NOT_A_CODE_POINT} if the key has no
     *         usable Alt mapping (in which case the caller should not consume the event).
     */
    int resolveAltCodePoint(@Nonnull final KeyEvent keyEvent) {
        // Resolve against the Alt modifier only, so the layer is independent of Shift/Caps state.
        final int metaState = KeyEvent.normalizeMetaState(
                KeyEvent.META_ALT_ON | KeyEvent.META_ALT_LEFT_ON);
        final int codePointAndFlags = keyEvent.getUnicodeChar(metaState);
        if (codePointAndFlags == 0) {
            return Event.NOT_A_CODE_POINT;
        }
        if ((codePointAndFlags & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            // Dead key; leave it to normal handling.
            return Event.NOT_A_CODE_POINT;
        }
        final int codePoint = codePointAndFlags & KeyCharacterMap.COMBINING_ACCENT_MASK;
        // Skip private-use code points, which are function placeholders rather than symbols.
        if (codePoint >= 0xE000 && codePoint <= 0xF8FF) {
            return Event.NOT_A_CODE_POINT;
        }
        return codePoint;
    }

    /**
     * Notify that a printing key was just emitted through the layer. Consumes a one-shot, and
     * marks a held Alt as a chord so its key-up does not arm the layer.
     */
    void onAltCodePointEmitted() {
        if (mPhysicallyHeld) {
            mChordSinceAltDown = true;
        }
        if (mState == AltState.ONE_SHOT) {
            mState = AltState.OFF;
        }
    }
}
