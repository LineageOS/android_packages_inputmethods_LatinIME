/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.android.inputmethod.latin.TouchKeyboardGestureDetector;
import com.android.inputmethod.latin.TouchKeyboardGestureDetector.Swipe;

import javax.annotation.Nullable;

final class HardwareKeyboardPanelSwipeDetector extends GestureDetector.SimpleOnGestureListener {
    private final GestureDetector mGestureDetector;
    private final float mDisplayDensity;

    HardwareKeyboardPanelSwipeDetector(final Context context) {
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(false);
        mDisplayDensity = context.getResources().getDisplayMetrics().density;
    }

    boolean isSwipeDownOnSymbolsPanel(@Nullable final Keyboard keyboard,
            final MotionEvent event) {
        return HardwareKeyboardLayouts.isSymbolsPanel(keyboard)
                && mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(@Nullable final MotionEvent down, final MotionEvent up,
            final float velocityX, final float velocityY) {
        return down != null && TouchKeyboardGestureDetector.classifySwipe(
                up.getX() - down.getX(), up.getY() - down.getY(), velocityX, velocityY,
                mDisplayDensity) == Swipe.DOWN;
    }
}
