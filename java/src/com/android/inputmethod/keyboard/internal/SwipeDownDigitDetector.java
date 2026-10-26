/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard.internal;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.latin.common.Constants;
import com.android.inputmethod.latin.common.StringUtils;

import javax.annotation.Nullable;

public final class SwipeDownDigitDetector {
    private static final float SWIPE_DOWN_THRESHOLD_KEY_HEIGHT_RATIO = 0.6f;

    private int mSwipeDownThreshold;
    @Nullable
    private Key mDownKey;
    private int mDownX;
    private int mDownY;
    private boolean mIsTracking;

    public static int getDigitCode(@Nullable final Key key) {
        if (key == null) {
            return Constants.NOT_A_CODE;
        }
        final String hintLabel = key.getHintLabel();
        if (hintLabel == null || StringUtils.codePointCount(hintLabel) != 1) {
            return Constants.NOT_A_CODE;
        }
        final int code = hintLabel.codePointAt(0);
        return Character.isDigit(code) ? code : Constants.NOT_A_CODE;
    }

    public void setKeyHeight(final int keyHeight) {
        mSwipeDownThreshold = (int)(keyHeight * SWIPE_DOWN_THRESHOLD_KEY_HEIGHT_RATIO);
    }

    public void onDownEvent(@Nullable final Key key, final int x, final int y) {
        mDownKey = getDigitCode(key) == Constants.NOT_A_CODE ? null : key;
        mDownX = x;
        mDownY = y;
        mIsTracking = false;
    }

    public void onMoveEvent(@Nullable final Key hitKey, final int x, final int y) {
        if (hitKey != null && hitKey == mDownKey && isMovingDown(x, y)) {
            mIsTracking = true;
        }
    }

    public boolean isSwipingDown(@Nullable final Key key, final int x, final int y) {
        return mIsTracking && key != null && key == mDownKey && isMovingDown(x, y);
    }

    public boolean hasSwipedDown(@Nullable final Key key, final int x, final int y) {
        return isSwipingDown(key, x, y) && y - mDownY >= mSwipeDownThreshold;
    }

    private boolean isMovingDown(final int x, final int y) {
        return y - mDownY > Math.abs(x - mDownX);
    }
}
