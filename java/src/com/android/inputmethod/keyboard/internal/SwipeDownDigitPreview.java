/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard.internal;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.latin.utils.TypefaceUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class SwipeDownDigitPreview {
    private static final long ANIMATION_DURATION_MS = 100;
    private static final float SLIDE_DISTANCE_KEY_HEIGHT_RATIO = 0.3f;
    private static final float HIDDEN = 0f;
    private static final float SHOWN = 1f;

    private final KeyboardView mKeyboardView;
    private final ValueAnimator mAnimator = new ValueAnimator();
    @Nullable
    private Key mKey;
    private float mProgress = HIDDEN;
    private float mTargetProgress = HIDDEN;

    public SwipeDownDigitPreview(@Nonnull final KeyboardView keyboardView) {
        mKeyboardView = keyboardView;
        mAnimator.setDuration(ANIMATION_DURATION_MS);
        mAnimator.addUpdateListener(animator -> {
            mProgress = (float)animator.getAnimatedValue();
            mKeyboardView.invalidateKey(mKey);
        });
    }

    public void show(@Nonnull final Key key) {
        if (key != mKey) {
            final Key previousKey = mKey;
            mAnimator.cancel();
            mKey = key;
            mProgress = HIDDEN;
            mTargetProgress = HIDDEN;
            mKeyboardView.invalidateKey(previousKey);
        }
        animateTo(SHOWN);
    }

    public void dismiss(@Nonnull final Key key) {
        if (key == mKey) {
            animateTo(HIDDEN);
        }
    }

    public void slideOutKeyTop(@Nonnull final Key key, @Nonnull final Canvas canvas,
            @Nonnull final KeyDrawParams params) {
        final float progress = getProgress(key);
        canvas.translate(0f, getSlideDistance(key) * progress);
        params.mAnimAlpha = Math.round(params.mAnimAlpha * (SHOWN - progress));
    }

    public void slideInDigit(@Nonnull final Key key, @Nonnull final Canvas canvas,
            @Nonnull final Paint paint, @Nonnull final KeyDrawParams params) {
        final float progress = getProgress(key);
        final String digit = key.getHintLabel();
        if (progress == HIDDEN || digit == null) {
            return;
        }
        paint.setTypeface(key.selectTypeface(params));
        paint.setTextSize(key.selectTextSize(params));
        paint.setTextAlign(Align.CENTER);
        paint.setColor(key.selectTextColor(params));
        paint.setAlpha(Math.round(paint.getAlpha() * progress));
        final float centerBaseline =
                (key.getHeight() + TypefaceUtils.getReferenceCharHeight(paint)) / 2f;
        final float baseline = centerBaseline - getSlideDistance(key) * (SHOWN - progress);
        canvas.drawText(digit, key.getDrawWidth() / 2f, baseline, paint);
    }

    private static float getSlideDistance(@Nonnull final Key key) {
        return key.getHeight() * SLIDE_DISTANCE_KEY_HEIGHT_RATIO;
    }

    private float getProgress(@Nonnull final Key key) {
        return key == mKey ? mProgress : HIDDEN;
    }

    private void animateTo(final float targetProgress) {
        if (targetProgress == mTargetProgress) {
            return;
        }
        mTargetProgress = targetProgress;
        mAnimator.cancel();
        mAnimator.setFloatValues(mProgress, targetProgress);
        mAnimator.start();
    }
}
