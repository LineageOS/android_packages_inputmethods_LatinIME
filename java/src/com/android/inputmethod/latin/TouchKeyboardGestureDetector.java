/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin;

import android.content.Context;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.MotionEvent;

import javax.annotation.Nullable;

public final class TouchKeyboardGestureDetector extends GestureDetector.SimpleOnGestureListener {
    public interface Listener {
        void onSwipeUp(float horizontalPosition);

        void onSwipeDown();

        void onSwipeLeft();

        void onSwipeRight();
    }

    enum Swipe {
        NONE, UP, DOWN, LEFT, RIGHT
    }

    private static final float MIN_HORIZONTAL_SWIPE_DISTANCE_DP = 90f;
    private static final float MIN_VERTICAL_SWIPE_DISTANCE_DP = 25f;
    private static final float MIN_SWIPE_VELOCITY_DP_PER_SECOND = 425f;
    private static final float MAX_CROSS_AXIS_RATIO = 0.5f;
    private static final float MILLIMETERS_PER_DP = 25.4f / 160f;

    private final GestureDetector mGestureDetector;
    private final Listener mListener;
    private final float mDisplayDensity;
    private long mLastHardwareKeyTime;

    public TouchKeyboardGestureDetector(final Context context, final Listener listener) {
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setIsLongpressEnabled(false);
        mListener = listener;
        mDisplayDensity = context.getResources().getDisplayMetrics().density;
    }

    public static boolean isFromBuiltInTouchKeyboard(final MotionEvent event) {
        if (!event.isFromSource(InputDevice.SOURCE_TOUCHPAD)
                || event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
            return false;
        }
        final InputDevice device = event.getDevice();
        return device != null && !device.isExternal() && !device.isVirtual();
    }

    public void onHardwareKeyDown(final long eventTime) {
        mLastHardwareKeyTime = eventTime;
    }

    public void onMotionEvent(final MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onFling(@Nullable final MotionEvent down, final MotionEvent up,
            final float velocityX, final float velocityY) {
        if (down == null || down.getEventTime() < mLastHardwareKeyTime) {
            return false;
        }
        final InputDevice.MotionRange xRange = getXRange(up);
        if (xRange == null) {
            return false;
        }
        final Swipe swipe = classifySwipe(up.getX() - down.getX(), up.getY() - down.getY(),
                velocityX, velocityY, getUnitsPerDp(xRange));
        switch (swipe) {
        case UP:
            mListener.onSwipeUp((up.getX() - xRange.getMin()) / xRange.getRange());
            return true;
        case DOWN:
            mListener.onSwipeDown();
            return true;
        case LEFT:
            mListener.onSwipeLeft();
            return true;
        case RIGHT:
            mListener.onSwipeRight();
            return true;
        default:
            return false;
        }
    }

    static Swipe classifySwipe(final float deltaX, final float deltaY, final float velocityX,
            final float velocityY, final float unitsPerDp) {
        final float minVelocity = MIN_SWIPE_VELOCITY_DP_PER_SECOND * unitsPerDp;
        if (isAlongAxis(deltaX, deltaY)) {
            if (Math.abs(deltaX) < MIN_HORIZONTAL_SWIPE_DISTANCE_DP * unitsPerDp
                    || Math.abs(velocityX) < minVelocity) {
                return Swipe.NONE;
            }
            return deltaX < 0 ? Swipe.LEFT : Swipe.RIGHT;
        }
        if (isAlongAxis(deltaY, deltaX)) {
            if (Math.abs(deltaY) < MIN_VERTICAL_SWIPE_DISTANCE_DP * unitsPerDp
                    || Math.abs(velocityY) < minVelocity) {
                return Swipe.NONE;
            }
            return deltaY < 0 ? Swipe.UP : Swipe.DOWN;
        }
        return Swipe.NONE;
    }

    private static boolean isAlongAxis(final float primaryDelta, final float crossDelta) {
        return Math.abs(crossDelta) < MAX_CROSS_AXIS_RATIO * Math.abs(primaryDelta);
    }

    @Nullable
    private static InputDevice.MotionRange getXRange(final MotionEvent event) {
        final InputDevice device = event.getDevice();
        return device == null ? null : device.getMotionRange(MotionEvent.AXIS_X, event.getSource());
    }

    private float getUnitsPerDp(final InputDevice.MotionRange range) {
        return range.getResolution() > 0 ? range.getResolution() * MILLIMETERS_PER_DP
                : mDisplayDensity;
    }
}
