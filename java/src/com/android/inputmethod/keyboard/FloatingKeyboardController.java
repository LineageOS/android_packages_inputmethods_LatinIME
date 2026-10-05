/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.inputmethod.latin.R;
import com.android.inputmethod.latin.utils.FloatingKeyboardUtils;

import javax.annotation.Nonnull;

public final class FloatingKeyboardController {
    public interface Listener {
        void onFloatingKeyboardDocked();

        void onFloatingKeyboardResized();

        void onFloatingKeyboardHideRequested();

        void onFloatingKeyboardSwitchImeRequested();
    }

    private static final int CORNER_HANDLE_SIZE_DP = 24;
    private static final int DOCK_HIGHLIGHT_HEIGHT_DP = 96;
    private static final int DOCK_HIGHLIGHT_ALPHA = 0x66;
    private static final long DOCK_HIGHLIGHT_FADE_MS = 150;
    private static final int RESIZE_OUTLINE_STROKE_DP = 2;
    private static final long RESIZE_MODE_TIMEOUT_MS = 4000;
    private static final long RESIZE_MODE_FADE_MS = 200;

    private final ViewGroup mInputView;
    private final View mPanel;
    private final View mDockedOnlyView;
    private final FrameLayout.LayoutParams mDockedLayoutParams;
    private final View mDragHandle;
    private final ImageView[] mCornerHandles = new ImageView[4];
    private final View mDockHighlight;
    private final View mResizeOutline;
    private final TextView mResetButton;
    private final View mSwitchImeButton;
    private final Runnable mExitResizeMode = () -> setResizeMode(false);
    private final int mTouchSlop;
    private final Drawable mPanelBackground;
    private final int mPanelPadding;
    private final int mCornerHandleSize;
    private final float mCornerRadius;
    private final float mDensity;
    private final SharedPreferences mPrefs;
    private final Listener mListener;
    private boolean mIsFloating;
    private boolean mIsDragging;
    private boolean mIsInDockZone;
    private boolean mIsResizeMode;
    private int mPanelWidth;
    private float mDownRawX;
    private float mDownRawY;
    private float mDownX;
    private float mDownY;
    private int mDownWidth;
    private int mDownHeight;
    private int mDownKeyboardHeight;
    private int mResizedWidth;
    private int mResizedKeyboardHeight;
    private int mResizedX;
    private int mResizedY;

    public FloatingKeyboardController(@Nonnull final ViewGroup inputView,
            @Nonnull final View panel, @Nonnull final View dockedOnlyView,
            @Nonnull final View dragHandle, final Drawable keyboardBackground,
            @Nonnull final ColorStateList accentTint, @Nonnull final SharedPreferences prefs,
            @Nonnull final Listener listener) {
        mInputView = inputView;
        mPanel = panel;
        mDockedOnlyView = dockedOnlyView;
        mDockedLayoutParams =
                new FrameLayout.LayoutParams((FrameLayout.LayoutParams) panel.getLayoutParams());
        mDragHandle = dragHandle;
        mPanelBackground = keyboardBackground == null ? null
                : keyboardBackground.getConstantState().newDrawable();
        mDensity = inputView.getResources().getDisplayMetrics().density;
        mPanelPadding = FloatingKeyboardUtils.dpToPx(
                FloatingKeyboardUtils.PANEL_PADDING_DP, mDensity);
        mCornerHandleSize = FloatingKeyboardUtils.dpToPx(CORNER_HANDLE_SIZE_DP, mDensity);
        mCornerRadius = FloatingKeyboardUtils.dpToPx(
                FloatingKeyboardUtils.PANEL_CORNER_RADIUS_DP, mDensity);
        mPrefs = prefs;
        mListener = listener;
        mPanel.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(final View view, final Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mCornerRadius);
            }
        });
        mDragHandle.setOnTouchListener(this::onMoveTouch);
        mDockHighlight = createDockHighlight(accentTint);
        mResizeOutline = createResizeOutline(accentTint);
        mTouchSlop = ViewConfiguration.get(inputView.getContext()).getScaledTouchSlop();
        mResetButton = dragHandle.findViewById(R.id.floating_reset_button);
        mResetButton.setTextColor(accentTint);
        mResetButton.setCompoundDrawableTintList(accentTint);
        mResetButton.setOnClickListener(v -> resetPanel());
        mSwitchImeButton = dragHandle.findViewById(R.id.floating_switch_ime_button);
        ((ImageView) mSwitchImeButton).setImageTintList(accentTint);
        mSwitchImeButton.setOnClickListener(
                v -> mListener.onFloatingKeyboardSwitchImeRequested());
        final ImageView hideButton = dragHandle.findViewById(R.id.floating_hide_button);
        hideButton.setImageTintList(accentTint);
        hideButton.setOnClickListener(v -> mListener.onFloatingKeyboardHideRequested());
        for (int corner = 0; corner < mCornerHandles.length; corner++) {
            mCornerHandles[corner] = createCornerHandle(corner, accentTint);
        }
        mInputView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop,
                oldRight, oldBottom) -> applySavedPosition());
    }

    public boolean isFloating() {
        return mIsFloating;
    }

    public void setFloating(final boolean floating, final int keyboardWidth) {
        if (floating == mIsFloating && keyboardWidth == mPanelWidth) {
            return;
        }
        mIsFloating = floating;
        mPanelWidth = keyboardWidth;
        if (floating) {
            mPanel.setLayoutParams(new FrameLayout.LayoutParams(
                    keyboardWidth + 2 * mPanelPadding, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP | Gravity.LEFT));
            mPanel.setPadding(mPanelPadding, mPanelPadding, mPanelPadding, 0);
            mPanel.setBackground(mPanelBackground);
            mPanel.setClipToOutline(true);
        } else {
            mPanel.setLayoutParams(new FrameLayout.LayoutParams(mDockedLayoutParams));
            mPanel.setPadding(0, 0, 0, 0);
            mPanel.setBackground(null);
            mPanel.setClipToOutline(false);
            mPanel.setTranslationX(0);
            mPanel.setTranslationY(0);
        }
        if (!floating) {
            setResizeMode(false);
        }
        mDragHandle.setVisibility(floating ? View.VISIBLE : View.GONE);
        mInputView.requestLayout();
    }

    public void getPanelRect(@Nonnull final Rect outRect) {
        if (mDockedOnlyView.getVisibility() == View.VISIBLE) {
            outRect.set(mDockedOnlyView.getLeft(), mDockedOnlyView.getTop(),
                    mDockedOnlyView.getRight(), mDockedOnlyView.getBottom());
            return;
        }
        final int x = Math.round(mPanel.getTranslationX());
        final int y = Math.round(mPanel.getTranslationY());
        final int inset = mIsResizeMode ? mCornerHandleSize / 2 : 0;
        outRect.set(x - inset, y - inset, x + mPanel.getWidth() + inset,
                y + mPanel.getHeight() + inset);
    }

    private boolean onMoveTouch(final View view, final MotionEvent event) {
        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            mIsDragging = true;
            mDownRawX = event.getRawX();
            mDownRawY = event.getRawY();
            mDownX = mPanel.getTranslationX();
            mDownY = mPanel.getTranslationY();
            return true;
        case MotionEvent.ACTION_MOVE:
            final Point position = FloatingKeyboardUtils.clampPosition(
                    Math.round(mDownX + event.getRawX() - mDownRawX),
                    Math.round(mDownY + event.getRawY() - mDownRawY),
                    mPanel.getWidth(), mPanel.getHeight(),
                    mInputView.getWidth(), mInputView.getHeight());
            setPanelPosition(position.x, position.y);
            setInDockZone(FloatingKeyboardUtils.shouldDock(position.y + mPanel.getHeight(),
                    mInputView.getHeight(), mDensity));
            return true;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mIsDragging = false;
            final boolean dock = mIsInDockZone;
            setInDockZone(false);
            if (Math.abs(event.getRawX() - mDownRawX) < mTouchSlop
                    && Math.abs(event.getRawY() - mDownRawY) < mTouchSlop) {
                setResizeMode(!mIsResizeMode);
            } else if (dock) {
                mListener.onFloatingKeyboardDocked();
            } else {
                FloatingKeyboardUtils.savePosition(mPrefs, isLandscape(),
                        Math.round(mPanel.getTranslationX()),
                        Math.round(mPanel.getTranslationY()),
                        mPanel.getWidth(), mPanel.getHeight(),
                        mInputView.getWidth(), mInputView.getHeight());
            }
            return true;
        default:
            return false;
        }
    }

    private boolean onCornerTouch(final int corner, final MotionEvent event) {
        final boolean isLeft = corner == 0 || corner == 2;
        final boolean isTop = corner == 0 || corner == 1;
        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
            mInputView.removeCallbacks(mExitResizeMode);
            mIsDragging = true;
            mDownRawX = event.getRawX();
            mDownRawY = event.getRawY();
            mDownX = mPanel.getTranslationX();
            mDownY = mPanel.getTranslationY();
            mDownWidth = mPanel.getWidth();
            mDownHeight = mPanel.getHeight();
            mDownKeyboardHeight = mPanel.findViewById(R.id.keyboard_view).getHeight();
            mResizedWidth = mDownWidth;
            mResizedKeyboardHeight = mDownKeyboardHeight;
            return true;
        case MotionEvent.ACTION_MOVE:
            final float dx = event.getRawX() - mDownRawX;
            final float dy = event.getRawY() - mDownRawY;
            mResizedWidth = FloatingKeyboardUtils.clampPanelWidth(
                    Math.round(mDownWidth + (isLeft ? -dx : dx)),
                    mInputView.getWidth(), mDensity);
            mResizedKeyboardHeight = FloatingKeyboardUtils.clampKeyboardHeight(
                    Math.round(mDownKeyboardHeight + (isTop ? -dy : dy)),
                    mInputView.getHeight(), mDensity);
            final int height = mDownHeight + mResizedKeyboardHeight - mDownKeyboardHeight;
            mResizedX = Math.round(isLeft ? mDownX + mDownWidth - mResizedWidth : mDownX);
            mResizedY = Math.round(isTop ? mDownY + mDownHeight - height : mDownY);
            showResizeOutline(mResizedX, mResizedY, mResizedWidth, height);
            return true;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mIsDragging = false;
            mResizeOutline.setVisibility(View.GONE);
            mInputView.postDelayed(mExitResizeMode, RESIZE_MODE_TIMEOUT_MS);
            if (mResizedWidth != mDownWidth || mResizedKeyboardHeight != mDownKeyboardHeight) {
                final int finalHeight =
                        mDownHeight + mResizedKeyboardHeight - mDownKeyboardHeight;
                FloatingKeyboardUtils.savePanelWidth(mPrefs, isLandscape(), mResizedWidth,
                        mDensity);
                FloatingKeyboardUtils.saveKeyboardHeight(mPrefs, isLandscape(),
                        mResizedKeyboardHeight, mDensity);
                FloatingKeyboardUtils.savePosition(mPrefs, isLandscape(), mResizedX, mResizedY,
                        mResizedWidth, finalHeight,
                        mInputView.getWidth(), mInputView.getHeight());
                mListener.onFloatingKeyboardResized();
            } else {
                setPanelPosition(Math.round(mDownX), Math.round(mDownY));
            }
            return true;
        default:
            return false;
        }
    }

    private void showResizeOutline(final int x, final int y, final int width,
            final int height) {
        final ViewGroup.LayoutParams params = mResizeOutline.getLayoutParams();
        params.width = width;
        params.height = height;
        mResizeOutline.setLayoutParams(params);
        mResizeOutline.setTranslationX(x);
        mResizeOutline.setTranslationY(y);
        mResizeOutline.setVisibility(View.VISIBLE);
        positionCornerHandles(x, y, width, height);
    }

    private void setResizeMode(final boolean resizeMode) {
        mInputView.removeCallbacks(mExitResizeMode);
        final boolean show = resizeMode && mIsFloating;
        if (show == mIsResizeMode) {
            return;
        }
        mIsResizeMode = show;
        for (final View handle : mCornerHandles) {
            fade(handle, show);
        }
        fade(mResetButton, show);
        fade(mSwitchImeButton, !show);
        mInputView.requestLayout();
        if (show) {
            mInputView.postDelayed(mExitResizeMode, RESIZE_MODE_TIMEOUT_MS);
        }
    }

    private static void fade(final View view, final boolean in) {
        view.animate().cancel();
        if (in) {
            if (view.getVisibility() != View.VISIBLE) {
                view.setAlpha(0f);
                view.setVisibility(View.VISIBLE);
            }
            view.animate().alpha(1f).setDuration(RESIZE_MODE_FADE_MS);
        } else if (view.getVisibility() == View.VISIBLE) {
            view.animate().alpha(0f).setDuration(RESIZE_MODE_FADE_MS)
                    .withEndAction(() -> view.setVisibility(View.GONE));
        }
    }

    private void resetPanel() {
        FloatingKeyboardUtils.resetPanel(mPrefs, isLandscape());
        setResizeMode(false);
        mListener.onFloatingKeyboardResized();
        mInputView.post(this::applySavedPosition);
    }

    private void applySavedPosition() {
        if (!mIsFloating || mIsDragging) {
            return;
        }
        final Point position = FloatingKeyboardUtils.loadPosition(mPrefs, isLandscape(),
                mPanel.getWidth(), mPanel.getHeight(),
                mInputView.getWidth(), mInputView.getHeight());
        setPanelPosition(position.x, position.y);
    }

    private void setPanelPosition(final int x, final int y) {
        mPanel.setTranslationX(x);
        mPanel.setTranslationY(y);
        positionCornerHandles(x, y, mPanel.getWidth(), mPanel.getHeight());
    }

    private void positionCornerHandles(final int x, final int y, final int width,
            final int height) {
        final float half = mCornerHandleSize / 2f;
        for (int corner = 0; corner < mCornerHandles.length; corner++) {
            final boolean isLeft = corner == 0 || corner == 2;
            final boolean isTop = corner == 0 || corner == 1;
            mCornerHandles[corner].setTranslationX(x - half + (isLeft ? 0 : width));
            mCornerHandles[corner].setTranslationY(y - half + (isTop ? 0 : height));
        }
    }

    private void setInDockZone(final boolean inDockZone) {
        if (inDockZone == mIsInDockZone) {
            return;
        }
        mIsInDockZone = inDockZone;
        mDockHighlight.animate().cancel();
        if (inDockZone) {
            mDockHighlight.setVisibility(View.VISIBLE);
            mDockHighlight.animate().alpha(1f).setDuration(DOCK_HIGHLIGHT_FADE_MS);
        } else {
            mDockHighlight.animate().alpha(0f).setDuration(DOCK_HIGHLIGHT_FADE_MS)
                    .withEndAction(() -> mDockHighlight.setVisibility(View.GONE));
        }
    }

    private ImageView createCornerHandle(final int corner, final ColorStateList tint) {
        final ImageView handle = new ImageView(mInputView.getContext());
        handle.setImageResource(R.drawable.ic_floating_resize);
        handle.setImageTintList(tint);
        handle.setScaleX(corner == 0 || corner == 2 ? -1f : 1f);
        handle.setScaleY(corner == 0 || corner == 1 ? -1f : 1f);
        handle.setContentDescription(
                mInputView.getResources().getString(R.string.floating_keyboard_resize_handle));
        handle.setVisibility(View.GONE);
        handle.setOnTouchListener((v, event) -> onCornerTouch(corner, event));
        mInputView.addView(handle, new FrameLayout.LayoutParams(mCornerHandleSize,
                mCornerHandleSize, Gravity.TOP | Gravity.LEFT));
        return handle;
    }

    private View createDockHighlight(final ColorStateList tint) {
        final int color = tint.getDefaultColor();
        final GradientDrawable glow = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP, new int[] {
                        Color.argb(DOCK_HIGHLIGHT_ALPHA, Color.red(color), Color.green(color),
                                Color.blue(color)),
                        Color.TRANSPARENT });
        final View highlight = new View(mInputView.getContext());
        highlight.setBackground(glow);
        highlight.setAlpha(0f);
        highlight.setVisibility(View.GONE);
        mInputView.addView(highlight, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                FloatingKeyboardUtils.dpToPx(DOCK_HIGHLIGHT_HEIGHT_DP, mDensity),
                Gravity.BOTTOM));
        return highlight;
    }

    private View createResizeOutline(final ColorStateList tint) {
        final GradientDrawable outline = new GradientDrawable();
        outline.setCornerRadius(mCornerRadius);
        outline.setStroke(FloatingKeyboardUtils.dpToPx(RESIZE_OUTLINE_STROKE_DP, mDensity),
                tint.getDefaultColor());
        final View view = new View(mInputView.getContext());
        view.setBackground(outline);
        view.setVisibility(View.GONE);
        mInputView.addView(view, new FrameLayout.LayoutParams(0, 0, Gravity.TOP | Gravity.LEFT));
        return view;
    }

    private boolean isLandscape() {
        return mInputView.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }
}
