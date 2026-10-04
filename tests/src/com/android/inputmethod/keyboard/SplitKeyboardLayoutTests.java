/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.keyboard;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;

import androidx.test.filters.SmallTest;

import com.android.inputmethod.latin.common.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SmallTest
public class SplitKeyboardLayoutTests extends KeyboardLayoutSetTestsBase {
    @Override
    protected int getKeyboardThemeForTests() {
        return KeyboardTheme.THEME_ID_LXX_LIGHT;
    }

    private Keyboard build(final Locale locale, final String layout, final int elementId,
            final boolean split) {
        final InputMethodSubtype subtype = getSubtype(locale, layout);
        final EditorInfo editorInfo = new EditorInfo();
        final KeyboardLayoutSet layoutSet = createKeyboardLayoutSet(subtype, editorInfo,
                false /* voiceInputKeyEnabled */, false /* languageSwitchKeyEnabled */, split);
        return layoutSet.getKeyboard(elementId);
    }

    private static List<String> describeKeys(final Keyboard keyboard) {
        final ArrayList<String> keys = new ArrayList<>();
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.isSpacer()) continue;
            keys.add(key.getCode() + ":" + key.getLabel() + ":" + key.getIconId());
        }
        Collections.sort(keys);
        return keys;
    }

    private void assertSplit(final Locale locale, final String layout, final int elementId) {
        final Keyboard plain = build(locale, layout, elementId, false);
        final Keyboard split = build(locale, layout, elementId, true);
        final String name = layout + "/" + elementId;
        assertTrue(name + " id", split.mId.mIsSplitLayout);
        assertFalse(name + " plain id", plain.mId.mIsSplitLayout);
        assertSameKeys(name, plain, split);
        assertHalves(name, split);
        assertNoOverlapInRows(name, split);
    }

    private static void assertSameKeys(final String name, final Keyboard plain,
            final Keyboard split) {
        assertEquals(name + " keys", describeKeys(plain), describeKeys(split));
    }

    private static void assertHalves(final String name, final Keyboard keyboard) {
        final float mid = keyboard.mOccupiedWidth / 2.0f;
        int spaceKeys = 0;
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.isSpacer()) continue;
            final int left = key.getX();
            final int right = key.getX() + key.getWidth();
            if (key.getCode() == Constants.CODE_SPACE) {
                spaceKeys++;
                assertTrue(name + " space spans mid", left < mid && right > mid);
                continue;
            }
            assertTrue(name + " key " + key + " crosses mid", right <= mid || left >= mid);
        }
        assertEquals(name + " space key count", 1, spaceKeys);
    }

    private static void assertNoOverlapInRows(final String name, final Keyboard keyboard) {
        Key prev = null;
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.isSpacer()) continue;
            if (prev != null && prev.getY() == key.getY()) {
                assertTrue(name + " overlap " + prev + " / " + key,
                        prev.getX() + prev.getWidth() <= key.getX());
            }
            prev = key;
        }
    }

    private void assertInnerEdgesAligned(final Locale locale, final String layout,
            final int elementId) {
        final Keyboard keyboard = build(locale, layout, elementId, true);
        final float mid = keyboard.mOccupiedWidth / 2.0f;
        final HashSet<Integer> spaceRows = new HashSet<>();
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.getCode() == Constants.CODE_SPACE) spaceRows.add(key.getY());
        }
        final Map<Integer, Integer> leftEdges = new HashMap<>();
        final Map<Integer, Integer> rightEdges = new HashMap<>();
        for (final Key key : keyboard.getSortedKeys()) {
            if (key.isSpacer() || spaceRows.contains(key.getY())) continue;
            final int y = key.getY();
            if (key.getX() + key.getWidth() <= mid) {
                final Integer edge = leftEdges.get(y);
                leftEdges.put(y, Math.max(edge == null ? 0 : edge, key.getX() + key.getWidth()));
            } else {
                final Integer edge = rightEdges.get(y);
                rightEdges.put(y, Math.min(edge == null ? Integer.MAX_VALUE : edge, key.getX()));
            }
        }
        final String name = layout + "/" + elementId;
        final int left = Collections.min(leftEdges.values());
        final int right = Collections.min(rightEdges.values());
        for (final int edge : leftEdges.values()) {
            assertTrue(name + " left inner edges " + leftEdges, Math.abs(edge - left) <= 1);
        }
        for (final int edge : rightEdges.values()) {
            assertTrue(name + " right inner edges " + rightEdges, Math.abs(edge - right) <= 1);
        }
    }

    public void testInnerEdgesAligned() {
        assertInnerEdgesAligned(Locale.US, "qwerty", KeyboardId.ELEMENT_ALPHABET);
        assertInnerEdgesAligned(Locale.FRENCH, "azerty", KeyboardId.ELEMENT_ALPHABET);
        assertInnerEdgesAligned(Locale.US, "qwerty", KeyboardId.ELEMENT_SYMBOLS);
    }

    public void testQwertySplit() {
        assertSplit(Locale.US, "qwerty", KeyboardId.ELEMENT_ALPHABET);
        assertSplit(Locale.US, "qwerty", KeyboardId.ELEMENT_ALPHABET_MANUAL_SHIFTED);
    }

    public void testAzertySplit() {
        assertSplit(Locale.FRENCH, "azerty", KeyboardId.ELEMENT_ALPHABET);
    }

    public void testQwertzSplit() {
        assertSplit(Locale.GERMAN, "qwertz", KeyboardId.ELEMENT_ALPHABET);
    }

    public void testDvorakSplit() {
        assertSplit(Locale.US, "dvorak", KeyboardId.ELEMENT_ALPHABET);
    }

    public void testSymbolsSplit() {
        assertSplit(Locale.US, "qwerty", KeyboardId.ELEMENT_SYMBOLS);
        assertSplit(Locale.US, "qwerty", KeyboardId.ELEMENT_SYMBOLS_SHIFTED);
    }

    public void testNumberPadNeverSplit() {
        final InputMethodSubtype subtype = getSubtype(Locale.US, "qwerty");
        final EditorInfo editorInfo = new EditorInfo();
        editorInfo.inputType = android.text.InputType.TYPE_CLASS_NUMBER;
        final Keyboard keyboard = createKeyboardLayoutSet(subtype, editorInfo, false, false,
                true /* splitLayoutEnabled */).getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertFalse(keyboard.mId.mIsSplitLayout);
    }

    public void testSplitAndUnsplitKeyboardIdsDiffer() {
        final Keyboard plain = build(Locale.US, "qwerty", KeyboardId.ELEMENT_ALPHABET, false);
        final Keyboard split = build(Locale.US, "qwerty", KeyboardId.ELEMENT_ALPHABET, true);
        assertFalse(plain.mId.equals(split.mId));
        assertNotSame(plain, split);
    }

    public void testAltCodeKeysWhileTypingContainOnlySplitKeys() {
        final Keyboard split = createKeyboardLayoutSet(getSubtype(Locale.US, "qwerty"),
                new EditorInfo(), false /* voiceInputKeyEnabled */,
                true /* languageSwitchKeyEnabled */, true /* splitLayoutEnabled */)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertFalse("no alt-code keys", split.mAltCodeKeysWhileTyping.isEmpty());
        final List<Key> sorted = split.getSortedKeys();
        for (final Key key : split.mAltCodeKeysWhileTyping) {
            assertTrue("stale alt-code key " + key, sorted.contains(key));
        }
    }

    private Keyboard buildDuplicate(final boolean duplicate) {
        final InputMethodSubtype subtype = getSubtype(Locale.US, "qwerty");
        return createKeyboardLayoutSet(subtype, new EditorInfo(), false, false,
                true /* split */, duplicate).getKeyboard(KeyboardId.ELEMENT_ALPHABET);
    }

    public void testDuplicateFlagChangesKeyboardId() {
        final Keyboard off = buildDuplicate(false);
        final Keyboard on = buildDuplicate(true);
        assertFalse(off.mId.mIsSplitDuplicateKeys);
        assertTrue(on.mId.mIsSplitDuplicateKeys);
        assertFalse(off.mId.equals(on.mId));
    }

    public void testDuplicateFlagIgnoredWhenNotSplit() {
        final InputMethodSubtype subtype = getSubtype(Locale.US, "qwerty");
        final Keyboard keyboard = createKeyboardLayoutSet(subtype, new EditorInfo(), false,
                false, false /* split */, true /* duplicate */)
                .getKeyboard(KeyboardId.ELEMENT_ALPHABET);
        assertFalse(keyboard.mId.mIsSplitDuplicateKeys);
    }

    public void testDuplicatedKeysAppearOncePerSide() {
        final Keyboard off = buildDuplicate(false);
        final Keyboard on = buildDuplicate(true);
        assertEquals(new HashSet<>(describeKeys(off)), new HashSet<>(describeKeys(on)));
        assertTrue(on.getSortedKeys().size() >= off.getSortedKeys().size());

        final float mid = on.mOccupiedWidth / 2.0f;
        final Map<String, Integer> leftCount = new HashMap<>();
        final Map<String, Integer> rightCount = new HashMap<>();
        for (final Key key : on.getSortedKeys()) {
            if (key.isSpacer() || key.getCode() == Constants.CODE_SPACE) continue;
            final String id = key.getY() + ":" + key.getCode() + ":" + key.getLabel();
            final Map<String, Integer> side = key.getX() < mid ? leftCount : rightCount;
            side.put(id, side.containsKey(id) ? side.get(id) + 1 : 1);
        }
        for (final Map.Entry<String, Integer> e : leftCount.entrySet()) {
            assertEquals(e.getKey() + " left", 1, (int) e.getValue());
        }
        for (final Map.Entry<String, Integer> e : rightCount.entrySet()) {
            assertEquals(e.getKey() + " right", 1, (int) e.getValue());
        }
        assertHalves("duplicate", on);
        assertNoOverlapInRows("duplicate", on);
    }

    public void testDuplicateTapsMapOntoScoredCopy() {
        final Keyboard on = buildDuplicate(true);
        final Map<Integer, List<Key>> keysByCode = new HashMap<>();
        for (final Key key : on.getSortedKeys()) {
            if (key.isSpacer()) continue;
            if (!keysByCode.containsKey(key.getCode())) {
                keysByCode.put(key.getCode(), new ArrayList<Key>());
            }
            keysByCode.get(key.getCode()).add(key);
        }
        int duplicatedCodes = 0;
        for (final List<Key> copies : keysByCode.values()) {
            final Key scored = copies.get(copies.size() - 1);
            assertEquals(scored.toString(), 0, on.getSplitDuplicateOffsetX(scored));
            if (copies.size() < 2) continue;
            duplicatedCodes++;
            final Key other = copies.get(0);
            assertEquals(other.toString(), scored.getX() - other.getX(),
                    on.getSplitDuplicateOffsetX(other));
        }
        assertTrue("no duplicated keys", duplicatedCodes > 0);
    }

    public void testNoDuplicateOffsetWithoutDuplicates() {
        final Keyboard off = buildDuplicate(false);
        for (final Key key : off.getSortedKeys()) {
            assertEquals(key.toString(), 0, off.getSplitDuplicateOffsetX(key));
        }
    }
}
