/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.graphics.Rect;

import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.android.inputmethod.keyboard.Key;
import com.android.inputmethod.latin.common.Constants;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class SplitKeyboardTransformerTests {
    private static final int LEFT = 0;
    private static final int RIGHT = 1000;
    private static final int Y = 0;
    private static final int HEIGHT = 50;

    private static Key key(final String label, final int code, final int x, final int width) {
        return key(label, code, x, Y, width);
    }

    private static Key key(final String label, final int code, final int x, final int y,
            final int width) {
        return new Key(label, KeyboardIconsSet.ICON_UNDEFINED, code, null /* outputText */,
                null /* hintLabel */, 0 /* labelFlags */, Key.BACKGROUND_TYPE_NORMAL, x, y,
                width, HEIGHT, 0 /* horizontalGap */, 0 /* verticalGap */);
    }

    private static Key letter(final String label, final int x) {
        return key(label, label.charAt(0), x, 100);
    }

    private static Key[] fullRow() {
        final String letters = "qwertyuiop";
        final Key[] keys = new Key[letters.length()];
        for (int i = 0; i < letters.length(); i++) {
            keys[i] = letter(letters.substring(i, i + 1), i * 100);
        }
        return keys;
    }

    private static List<Key> split(final boolean duplicate, final Key... keys) {
        return new SplitKeyboardTransformer(LEFT, RIGHT, duplicate).transform(
                Arrays.asList(keys));
    }

    private static Key find(final List<Key> keys, final String label) {
        for (final Key k : keys) {
            if (label.equals(k.getLabel())) return k;
        }
        throw new AssertionError("no key " + label);
    }

    private static void assertSlot(final Key key, final int left, final int right) {
        assertEquals(key.getLabel() + " left", left, key.getX());
        assertEquals(key.getLabel() + " right", right, key.getX() + key.getWidth());
    }

    @Test
    public void testLeftHalfKeysAreCompressedTowardLeftEdge() {
        final List<Key> out = split(false, fullRow());
        assertEquals(10, out.size());
        assertSlot(find(out, "q"), 20, 72);
        assertSlot(find(out, "t"), 228, 280);
    }

    @Test
    public void testRightHalfKeysAreCompressedTowardRightEdge() {
        final List<Key> out = split(false, fullRow());
        assertSlot(find(out, "y"), 720, 772);
        assertSlot(find(out, "p"), 928, 980);
    }

    @Test
    public void testSpacebarSpansTheGap() {
        final List<Key> out = split(false, key(" ", Constants.CODE_SPACE, 200, 600));
        assertEquals(1, out.size());
        assertSlot(out.get(0), 128, 872);
    }

    @Test
    public void testSpaceRowIsAlignedToOuterEdges() {
        final List<Key> out = split(false, key("?123", Constants.CODE_SWITCH_ALPHA_SYMBOL, 0, 150),
                key(",", ',', 150, 100), key(" ", Constants.CODE_SPACE, 250, 500),
                key(".", '.', 750, 100), key("enter", Constants.CODE_ENTER, 850, 150));
        assertEquals(5, out.size());
        assertSlot(out.get(0), 20, 101);
        assertSlot(out.get(1), 101, 155);
        assertSlot(out.get(2), 155, 845);
        assertSlot(out.get(3), 845, 899);
        assertSlot(out.get(4), 899, 980);
    }

    @Test
    public void testSpacebarIsNeverDuplicated() {
        final List<Key> out = split(true, key(" ", Constants.CODE_SPACE, 200, 600));
        assertEquals(1, out.size());
    }

    @Test
    public void testStraddlerGoesToLargerOverlapSide() {
        final List<Key> out = split(false, letter("a", 0), letter("h", 460), letter("l", 900));
        assertEquals(3, out.size());
        assertSlot(find(out, "h"), 720, 768);
    }

    @Test
    public void testStraddlerTieGoesLeft() {
        final List<Key> out = split(false, letter("a", 0), letter("g", 450), letter("l", 900));
        assertEquals(3, out.size());
        assertSlot(find(out, "g"), 233, 280);
    }

    @Test
    public void testStraddlerDuplicatedOnBothSides() {
        final List<Key> out = split(true, letter("a", 0), letter("g", 450), letter("l", 900));
        assertEquals(4, out.size());
        assertSlot(out.get(1), 233, 280);
        assertSlot(out.get(2), 720, 767);
        assertEquals("g", out.get(2).getLabel());
    }

    @Test
    public void testDuplicatesDoNotOverlapNeighbors() {
        final List<Key> out = new ArrayList<>(split(true, letter("a", 0), letter("f", 350),
                letter("g", 450), letter("h", 550), letter("l", 900)));
        Collections.sort(out, new Comparator<Key>() {
            @Override
            public int compare(final Key lhs, final Key rhs) {
                return lhs.getX() - rhs.getX();
            }
        });
        assertEquals(6, out.size());
        for (int i = 1; i < out.size(); i++) {
            final Key prev = out.get(i - 1);
            final Key cur = out.get(i);
            assertTrue(prev.getLabel() + "/" + cur.getLabel() + " overlap",
                    prev.getX() + prev.getWidth() <= cur.getX());
        }
    }

    @Test
    public void testKeysSplitAtRowMiddleNotKeyboardMiddle() {
        final ArrayList<Key> row = new ArrayList<>();
        final String letters = "qwertyuiop";
        for (int i = 0; i < letters.length(); i++) {
            row.add(key(letters.substring(i, i + 1), letters.charAt(i), i * 90, 90));
        }
        row.add(key("del", Constants.CODE_DELETE, 900, 100));
        final List<Key> out = split(false, row.toArray(new Key[0]));
        assertEquals(11, out.size());
        final Key t = find(out, "t");
        final Key y = find(out, "y");
        assertTrue("t on left: " + t, t.getX() + t.getWidth() <= 500);
        assertTrue("y on right: " + y, y.getX() >= 500);
    }

    @Test
    public void testInnerEdgesAlignedAcrossRows() {
        final ArrayList<Key> keys = new ArrayList<>(Arrays.asList(fullRow()));
        final String home = "asdfghjkl";
        for (int i = 0; i < home.length(); i++) {
            keys.add(key(home.substring(i, i + 1), home.charAt(i), 50 + i * 100, HEIGHT, 100));
        }
        final List<Key> out = split(false, keys.toArray(new Key[0]));
        assertEquals(19, out.size());
        for (final int y : new int[] { Y, HEIGHT }) {
            int leftInnerEdge = Integer.MIN_VALUE;
            int rightInnerEdge = Integer.MAX_VALUE;
            for (final Key k : out) {
                if (k.getY() != y) continue;
                if (k.getX() < 500) {
                    leftInnerEdge = Math.max(leftInnerEdge, k.getX() + k.getWidth());
                } else {
                    rightInnerEdge = Math.min(rightInnerEdge, k.getX());
                }
            }
            assertEquals("row " + y + " left", 280, leftInnerEdge);
            assertEquals("row " + y + " right", 720, rightInnerEdge);
        }
    }

    @Test
    public void testInnerHitBoxesClampedOuterPreserved() {
        final List<Key> out = split(false, fullRow());
        assertEquals(0, find(out, "q").getHitBox().left);
        assertEquals(1001, find(out, "p").getHitBox().right);
        assertEquals(281, find(out, "t").getHitBox().right);
        assertEquals(720, find(out, "y").getHitBox().left);
        for (final Key k : out) {
            assertTrue(k.getLabel() + " hit in gap", !k.isOnKey(500, Y + 10));
        }
    }

    @Test
    public void testVerticalGeometryUnchanged() {
        final Key q = letter("q", 0);
        final Rect before = new Rect(q.getHitBox());
        final Key out = split(false, q).get(0);
        assertEquals(q.getY(), out.getY());
        assertEquals(q.getHeight(), out.getHeight());
        assertEquals(before.top, out.getHitBox().top);
        assertEquals(before.bottom, out.getHitBox().bottom);
    }

    @Test
    public void testInputKeysAreNotMutated() {
        final Key[] row = fullRow();
        final Key t = row[4];
        split(true, row);
        assertSlot(t, 400, 500);
        assertEquals(new Rect(400, Y, 501, Y + HEIGHT), t.getHitBox());
    }

    @Test
    public void testKeepsLabelCodeAndHorizontalGap() {
        final Key k = new Key("a", KeyboardIconsSet.ICON_UNDEFINED, 'a', null, null, 0,
                Key.BACKGROUND_TYPE_NORMAL, 0, Y, 100, HEIGHT, 10 /* hGap */, 0);
        final Key out = split(false, k).get(0);
        assertEquals("a", out.getLabel());
        assertEquals('a', out.getCode());
        assertEquals(10, out.getHorizontalGap());
        assertEquals(231, out.getX());
        assertEquals(44, out.getWidth());
    }
}
