/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.suggestions;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InlineSuggestion;
import android.view.inputmethod.InlineSuggestionsRequest;
import android.widget.LinearLayout;
import android.widget.inline.InlineContentView;
import android.widget.inline.InlinePresentationSpec;

import androidx.autofill.inline.UiVersions;
import androidx.autofill.inline.common.TextViewStyle;
import androidx.autofill.inline.common.ViewStyle;
import androidx.autofill.inline.v1.InlineSuggestionUi;
import androidx.core.graphics.ColorUtils;

import com.android.inputmethod.latin.R;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class InlineSuggestionsStrip extends LinearLayout {
    private static final int CHIP_HEIGHT_DP = 32;
    private static final int CHIP_MIN_WIDTH_DP = 48;
    private static final int CHIP_MAX_WIDTH_DP = 240;
    private static final int CHIP_HORIZONTAL_PADDING_DP = 12;
    private static final int CHIP_SPACING_DP = 4;
    private static final int CHIP_BACKGROUND_ALPHA = 0x1F;
    private static final int SUBTITLE_ALPHA = 0x99;
    private static final float TITLE_TEXT_SIZE_SP = 16f;
    private static final float SUBTITLE_TEXT_SIZE_SP = 12f;
    private static final Size WRAP_CONTENT_SIZE =
            new Size(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private final ViewGroup mScrollableSuggestions;
    private final ViewGroup mPinnedSuggestions;
    private int mGeneration;
    private boolean mHasSuggestions;

    public InlineSuggestionsStrip(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.inline_suggestions_strip, this);
        mScrollableSuggestions = findViewById(R.id.inline_suggestions_scrollable);
        mPinnedSuggestions = findViewById(R.id.inline_suggestions_pinned);
    }

    @Nullable
    public InlineSuggestionsRequest createRequest(@Nonnull final Bundle uiExtras,
            final int textColor) {
        if (!UiVersions.getVersions(uiExtras).contains(UiVersions.INLINE_UI_VERSION_1)) {
            return null;
        }
        final Bundle styles = UiVersions.newStylesBuilder()
                .addStyle(createStyle(textColor))
                .build();
        final int chipHeight = dpToPx(CHIP_HEIGHT_DP);
        final InlinePresentationSpec spec = new InlinePresentationSpec.Builder(
                new Size(dpToPx(CHIP_MIN_WIDTH_DP), chipHeight),
                new Size(dpToPx(CHIP_MAX_WIDTH_DP), chipHeight))
                .setStyle(styles)
                .build();
        return new InlineSuggestionsRequest.Builder(Collections.singletonList(spec)).build();
    }

    public void setSuggestions(@Nonnull final List<InlineSuggestion> suggestions) {
        mGeneration++;
        mHasSuggestions = !suggestions.isEmpty();
        if (!mHasSuggestions) {
            removeAllChips();
            return;
        }
        final InflationBatch batch = new InflationBatch(mGeneration, suggestions);
        for (int i = 0; i < suggestions.size(); i++) {
            final int index = i;
            suggestions.get(i).inflate(getContext(), WRAP_CONTENT_SIZE,
                    getContext().getMainExecutor(), view -> onChipInflated(batch, index, view));
        }
    }

    static int getChipBackgroundColor(final int textColor) {
        return ColorUtils.setAlphaComponent(textColor, CHIP_BACKGROUND_ALPHA);
    }

    public boolean hasSuggestions() {
        return mHasSuggestions;
    }

    private void onChipInflated(@Nonnull final InflationBatch batch, final int index,
            @Nullable final InlineContentView view) {
        if (batch.mGeneration != mGeneration) {
            return;
        }
        batch.mViews[index] = view;
        batch.mPendingCount--;
        if (batch.mPendingCount == 0) {
            showChips(batch);
        }
    }

    private void showChips(@Nonnull final InflationBatch batch) {
        removeAllChips();
        for (int i = 0; i < batch.mViews.length; i++) {
            final View view = batch.mViews[i];
            if (view == null) {
                continue;
            }
            final ViewGroup container = batch.mSuggestions.get(i).getInfo().isPinned()
                    ? mPinnedSuggestions : mScrollableSuggestions;
            container.addView(view, createChipLayoutParams());
        }
    }

    private void removeAllChips() {
        mScrollableSuggestions.removeAllViews();
        mPinnedSuggestions.removeAllViews();
    }

    private LayoutParams createChipLayoutParams() {
        final LayoutParams params =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMarginEnd(dpToPx(CHIP_SPACING_DP));
        return params;
    }

    @Nonnull
    private InlineSuggestionUi.Style createStyle(final int textColor) {
        final Icon background = Icon.createWithResource(getContext(),
                R.drawable.inline_suggestion_chip)
                .setTint(getChipBackgroundColor(textColor));
        final int padding = dpToPx(CHIP_HORIZONTAL_PADDING_DP);
        return InlineSuggestionUi.newStyleBuilder()
                .setSingleIconChipStyle(new ViewStyle.Builder()
                        .setBackground(background)
                        .setPadding(0, 0, 0, 0)
                        .build())
                .setChipStyle(new ViewStyle.Builder()
                        .setBackground(background)
                        .setPadding(padding, 0, padding, 0)
                        .build())
                .setTitleStyle(new TextViewStyle.Builder()
                        .setTextColor(textColor)
                        .setTextSize(TITLE_TEXT_SIZE_SP)
                        .build())
                .setSubtitleStyle(new TextViewStyle.Builder()
                        .setTextColor(ColorUtils.setAlphaComponent(textColor, SUBTITLE_ALPHA))
                        .setTextSize(SUBTITLE_TEXT_SIZE_SP)
                        .build())
                .build();
    }

    private int dpToPx(final int dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics()));
    }

    private static final class InflationBatch {
        final int mGeneration;
        final List<InlineSuggestion> mSuggestions;
        final View[] mViews;
        int mPendingCount;

        InflationBatch(final int generation, @Nonnull final List<InlineSuggestion> suggestions) {
            mGeneration = generation;
            mSuggestions = suggestions;
            mViews = new View[suggestions.size()];
            mPendingCount = suggestions.size();
        }
    }
}
