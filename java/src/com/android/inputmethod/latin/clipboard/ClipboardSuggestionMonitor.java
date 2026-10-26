/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.clipboard;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.PersistableBundle;
import android.util.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ClipboardSuggestionMonitor
        implements ClipboardManager.OnPrimaryClipChangedListener {
    private static final String TAG = ClipboardSuggestionMonitor.class.getSimpleName();
    private static final long NO_TIMESTAMP = -1;

    public interface Listener {
        void onClipboardSuggestionChanged(@Nullable ClipboardSuggestion suggestion);
    }

    private final ClipboardManager mClipboardManager;
    private final Listener mListener;
    private long mConsumedTimestamp = NO_TIMESTAMP;
    private boolean mIsStarted;

    public ClipboardSuggestionMonitor(@Nonnull final Context context,
            @Nonnull final Listener listener) {
        mClipboardManager = context.getSystemService(ClipboardManager.class);
        mListener = listener;
    }

    public void start() {
        if (!mIsStarted) {
            mClipboardManager.addPrimaryClipChangedListener(this);
            mIsStarted = true;
        }
        mListener.onClipboardSuggestionChanged(readSuggestion());
    }

    public void stop() {
        if (mIsStarted) {
            mClipboardManager.removePrimaryClipChangedListener(this);
            mIsStarted = false;
        }
    }

    public void consume(@Nonnull final ClipboardSuggestion suggestion) {
        mConsumedTimestamp = suggestion.getTimestamp();
        mListener.onClipboardSuggestionChanged(null);
    }

    @Override
    public void onPrimaryClipChanged() {
        mListener.onClipboardSuggestionChanged(readSuggestion());
    }

    @Nullable
    private ClipboardSuggestion readSuggestion() {
        final ClipData clip = getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            return null;
        }
        final ClipDescription description = clip.getDescription();
        if (description.getTimestamp() == mConsumedTimestamp) {
            return null;
        }
        return ClipboardSuggestion.create(clip.getItemAt(0).getText(),
                description.getTimestamp(), System.currentTimeMillis(),
                isSensitive(description));
    }

    @Nullable
    private ClipData getPrimaryClip() {
        try {
            return mClipboardManager.getPrimaryClip();
        } catch (final SecurityException e) {
            Log.w(TAG, "Clipboard is not accessible", e);
            return null;
        }
    }

    private static boolean isSensitive(@Nonnull final ClipDescription description) {
        final PersistableBundle extras = description.getExtras();
        return extras != null && extras.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE);
    }
}
