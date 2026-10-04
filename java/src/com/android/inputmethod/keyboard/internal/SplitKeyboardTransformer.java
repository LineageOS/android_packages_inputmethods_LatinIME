/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard.internal;

import android.graphics.Rect;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.latin.common.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public final class SplitKeyboardTransformer {
    public static final float SPLIT_HALF_WIDTH_RATIO = 0.26f;
    public static final float SPLIT_OUTER_MARGIN_RATIO = 0.02f;
    private static final int MIN_KEYS_FOR_ROW_MIDDLE = 4;

    private static final int SIDE_LEFT = 0;
    private static final int SIDE_RIGHT = 1;
    private static final int SIDE_SPACE = 2;

    private final int mLeft;
    private final int mRight;
    private final float mMid;
    private final float mHalfWidth;
    private final float mOuterLeft;
    private final float mOuterRight;
    private final float mInnerLeft;
    private final float mInnerRight;
    private final float mMaxScale;
    private final boolean mDuplicateCenterKeys;

    public SplitKeyboardTransformer(final int left, final int right,
            final boolean duplicateCenterKeys) {
        mLeft = left;
        mRight = right;
        mMid = (left + right) / 2.0f;
        mHalfWidth = (right - left) * SPLIT_HALF_WIDTH_RATIO;
        final float margin = (right - left) * SPLIT_OUTER_MARGIN_RATIO;
        mOuterLeft = left + margin;
        mOuterRight = right - margin;
        mInnerLeft = mOuterLeft + mHalfWidth;
        mInnerRight = mOuterRight - mHalfWidth;
        mMaxScale = mHalfWidth / (mMid - mOuterLeft);
        mDuplicateCenterKeys = duplicateCenterKeys;
    }

    @Nonnull
    public List<Key> transform(@Nonnull final Collection<Key> keys) {
        final Map<Integer, Float> rowMiddles = computeRowMiddles(keys);
        final Map<Integer, RowHalves> rows = new HashMap<>();
        final ArrayList<Placement> placements = new ArrayList<>(keys.size());
        for (final Key key : keys) {
            if (key.isSpacer()) {
                continue;
            }
            final float slotLeft = getSlotLeft(key);
            final float slotRight = getSlotRight(key);
            RowHalves row = rows.get(key.getY());
            if (row == null) {
                row = new RowHalves();
                rows.put(key.getY(), row);
            }
            if (key.getCode() == Constants.CODE_SPACE && slotLeft < mMid && slotRight > mMid) {
                placements.add(new Placement(key, SIDE_SPACE));
                row.mIsSpaceRow = true;
                continue;
            }
            final Float rowMiddle = rowMiddles.get(key.getY());
            final float middle = (rowMiddle != null) ? rowMiddle : mMid;
            final boolean straddles = slotLeft < middle && slotRight > middle;
            final boolean mostlyLeft = (middle - slotLeft) >= (slotRight - middle);
            final boolean duplicate = straddles && mDuplicateCenterKeys;
            if (mostlyLeft || duplicate) {
                placements.add(new Placement(key, SIDE_LEFT));
                row.addLeft(slotLeft, slotRight);
            }
            if (!mostlyLeft || duplicate) {
                placements.add(new Placement(key, SIDE_RIGHT));
                row.addRight(slotLeft, slotRight);
            }
        }
        final float scale = computeScale(rows.values());
        final ArrayList<Key> result = new ArrayList<>(placements.size());
        for (final Placement placement : placements) {
            final Key key = placement.mKey;
            final float slotLeft = getSlotLeft(key);
            final float slotRight = getSlotRight(key);
            final RowHalves row = rows.get(key.getY());
            switch (placement.mSide) {
            case SIDE_LEFT:
                result.add(copy(key, toLeftHalf(row, slotLeft, scale),
                        toLeftHalf(row, slotRight, scale)));
                break;
            case SIDE_RIGHT:
                result.add(copy(key, toRightHalf(row, slotLeft, scale),
                        toRightHalf(row, slotRight, scale)));
                break;
            default:
                result.add(copy(key, toLeftHalf(row, slotLeft, scale),
                        toRightHalf(row, slotRight, scale)));
                break;
            }
        }
        return result;
    }

    private float toLeftHalf(final RowHalves row, final float x, final float scale) {
        if (row.mIsSpaceRow || !row.hasLeft()) {
            return mOuterLeft + (x - mLeft) * scale;
        }
        return mInnerLeft - (row.mLeftAnchor - x) * scale;
    }

    private float toRightHalf(final RowHalves row, final float x, final float scale) {
        if (row.mIsSpaceRow || !row.hasRight()) {
            return mOuterRight - (mRight - x) * scale;
        }
        return mInnerRight + (x - row.mRightAnchor) * scale;
    }

    private float computeScale(@Nonnull final Collection<RowHalves> rows) {
        float widestHalf = 0;
        for (final RowHalves row : rows) {
            if (row.hasLeft()) {
                widestHalf = Math.max(widestHalf, row.mLeftAnchor - row.mLeftMin);
            }
            if (row.hasRight()) {
                widestHalf = Math.max(widestHalf, row.mRightMax - row.mRightAnchor);
            }
        }
        return widestHalf > 0 ? Math.min(mMaxScale, mHalfWidth / widestHalf) : mMaxScale;
    }

    private static final class RowHalves {
        float mLeftMin = Float.POSITIVE_INFINITY;
        float mLeftAnchor = Float.NEGATIVE_INFINITY;
        float mRightAnchor = Float.POSITIVE_INFINITY;
        float mRightMax = Float.NEGATIVE_INFINITY;
        boolean mIsSpaceRow;

        void addLeft(final float slotLeft, final float slotRight) {
            mLeftMin = Math.min(mLeftMin, slotLeft);
            mLeftAnchor = Math.max(mLeftAnchor, slotRight);
        }

        void addRight(final float slotLeft, final float slotRight) {
            mRightAnchor = Math.min(mRightAnchor, slotLeft);
            mRightMax = Math.max(mRightMax, slotRight);
        }

        boolean hasLeft() {
            return mLeftAnchor != Float.NEGATIVE_INFINITY;
        }

        boolean hasRight() {
            return mRightAnchor != Float.POSITIVE_INFINITY;
        }
    }

    private static final class Placement {
        final Key mKey;
        final int mSide;

        Placement(final Key key, final int side) {
            mKey = key;
            mSide = side;
        }
    }

    @Nonnull
    private static Map<Integer, Float> computeRowMiddles(@Nonnull final Collection<Key> keys) {
        final Map<Integer, float[]> rows = new HashMap<>();
        for (final Key key : keys) {
            if (key.isSpacer() || key.getCode() <= Constants.CODE_SPACE) {
                continue;
            }
            final float[] row = rows.get(key.getY());
            if (row == null) {
                rows.put(key.getY(), new float[] { getSlotLeft(key), getSlotRight(key), 1 });
            } else {
                rows.put(key.getY(), new float[] { Math.min(row[0], getSlotLeft(key)),
                        Math.max(row[1], getSlotRight(key)), row[2] + 1 });
            }
        }
        final Map<Integer, Float> middles = new HashMap<>();
        for (final Map.Entry<Integer, float[]> entry : rows.entrySet()) {
            final float[] row = entry.getValue();
            if (row[2] >= MIN_KEYS_FOR_ROW_MIDDLE) {
                middles.put(entry.getKey(), (row[0] + row[1]) / 2.0f);
            }
        }
        return middles;
    }

    private static float getSlotLeft(@Nonnull final Key key) {
        return key.getX() - key.getHorizontalGap() / 2.0f;
    }

    private static float getSlotRight(@Nonnull final Key key) {
        return getSlotLeft(key) + key.getWidth() + key.getHorizontalGap();
    }

    @Nonnull
    private Key copy(@Nonnull final Key key, final float left, final float right) {
        final int slotLeft = Math.round(left);
        final int slotRight = Math.round(right);
        final Rect oldHitBox = key.getHitBox();
        final int hitLeft = oldHitBox.left <= mLeft ? oldHitBox.left : slotLeft;
        final int hitRight = oldHitBox.right >= mRight ? oldHitBox.right : slotRight + 1;
        final Rect hitBox = new Rect(hitLeft, oldHitBox.top, hitRight, oldHitBox.bottom);
        return Key.copyWithGeometry(key, slotLeft, slotRight, hitBox);
    }
}
