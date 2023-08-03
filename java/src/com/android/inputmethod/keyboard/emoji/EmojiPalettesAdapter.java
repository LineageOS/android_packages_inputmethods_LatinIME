/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.inputmethod.keyboard.emoji;

import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.keyboard.Keyboard;
import com.android.inputmethod.keyboard.KeyboardView;
import com.android.inputmethod.latin.R;

final class EmojiPalettesAdapter extends RecyclerView.Adapter<EmojiPalettesAdapter.ViewHolder> {
    private static final String TAG = EmojiPalettesAdapter.class.getSimpleName();
    private static final boolean DEBUG_PAGER = false;

    private final EmojiPageKeyboardView.OnKeyEventListener mListener;
    private final DynamicGridKeyboard mRecentsKeyboard;
    private final SparseArray<EmojiPageKeyboardView> mActiveKeyboardViews = new SparseArray<>();
    private final EmojiCategory mEmojiCategory;
    private int mActivePosition = 0;

    public EmojiPalettesAdapter(final EmojiCategory emojiCategory,
            final EmojiPageKeyboardView.OnKeyEventListener listener) {
        mEmojiCategory = emojiCategory;
        mListener = listener;
        mRecentsKeyboard = mEmojiCategory.getKeyboard(EmojiCategory.ID_RECENTS, 0);
    }

    public void flushPendingRecentKeys() {
        mRecentsKeyboard.flushPendingRecentKeys();
        final KeyboardView recentKeyboardView =
                mActiveKeyboardViews.get(mEmojiCategory.getRecentTabId());
        if (recentKeyboardView != null) {
            recentKeyboardView.invalidateAllKeys();
        }
    }

    public void addRecentKey(final Key key) {
        if (mEmojiCategory.isInRecentTab()) {
            mRecentsKeyboard.addPendingKey(key);
            return;
        }
        mRecentsKeyboard.addKeyFirst(key);
        final KeyboardView recentKeyboardView =
                mActiveKeyboardViews.get(mEmojiCategory.getRecentTabId());
        if (recentKeyboardView != null) {
            recentKeyboardView.invalidateAllKeys();
        }
    }

    public void onPageScrolled() {
        releaseCurrentKey(false /* withKeyRegistering */);
    }

    public void releaseCurrentKey(final boolean withKeyRegistering) {
        // Make sure the delayed key-down event (highlight effect and haptic feedback) will be
        // canceled.
        final EmojiPageKeyboardView currentKeyboardView =
                mActiveKeyboardViews.get(mActivePosition);
        if (currentKeyboardView == null) {
            return;
        }
        currentKeyboardView.releaseCurrentKey(withKeyRegistering);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.emoji_keyboard_page, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EmojiPalettesAdapter.ViewHolder holder, int position) {
        if (DEBUG_PAGER) {
            Log.d(TAG, "instantiate item: " + position);
        }
        final EmojiPageKeyboardView oldKeyboardView = mActiveKeyboardViews.get(position);
        if (oldKeyboardView != null) {
            oldKeyboardView.deallocateMemory();
            // This may be redundant but wanted to be safer..
            mActiveKeyboardViews.remove(position);
        }
        final Keyboard keyboard =
                mEmojiCategory.getKeyboardFromPagePosition(position);
        holder.getKeyboardView().setKeyboard(keyboard);
        holder.getKeyboardView().setOnKeyEventListener(mListener);
        mActiveKeyboardViews.put(position, holder.getKeyboardView());
    }

    @Override
    public int getItemCount() {
        return mEmojiCategory.getTotalPageCountOfAllCategories();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private EmojiPageKeyboardView customView;

        public ViewHolder(View view) {
            super(view);
            customView = view.findViewById(R.id.emoji_keyboard_page);
        }

        public EmojiPageKeyboardView getKeyboardView() {
            return customView;
        }
    }
}
