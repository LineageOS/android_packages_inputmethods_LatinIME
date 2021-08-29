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

package com.android.inputmethod.keyboard.layout;

import com.android.inputmethod.keyboard.layout.customizer.LayoutCustomizer;
import com.android.inputmethod.keyboard.layout.expected.ExpectedKey;
import com.android.inputmethod.keyboard.layout.expected.ExpectedKeyboardBuilder;

public final class Goidelic extends LayoutBase {
    private static final String LAYOUT_NAME = "goidelic";

    public Goidelic(final LayoutCustomizer customizer) {
        super(customizer, Symbols.class, SymbolsShifted.class);
    }

    @Override
    public String getName() { return LAYOUT_NAME; }

    @Override
    ExpectedKey[][] getCommonAlphabetLayout(final boolean isPhone) { return ALPHABET_COMMON; }

    private static final ExpectedKey[][] ALPHABET_COMMON = new ExpectedKeyboardBuilder()
            .setKeysOfRow(1,
                    key("q", additionalMoreKey("1")),
                    key("w", additionalMoreKey("2")),
                    key("e", additionalMoreKey("3")),
                    key("r", additionalMoreKey("4")),
                    key("t", additionalMoreKey("5")),
                    key("y", additionalMoreKey("6")),
                    key("u", additionalMoreKey("7")),
                    key("i", additionalMoreKey("8")),
                    key("o", additionalMoreKey("9")),
                    key("p", additionalMoreKey("0")))
            .setKeysOfRow(2, "a", "s",
                    // U+00A3: "£" Pound Sign
                    key("d", additionalMoreKey("\u00A3")),
                    // U+1E1F: "ḟ" Latin Small Letter F with Dot Above
                    key("f", joinMoreKeys(additionalMoreKey("%"), "\u1E1F")),
                    // U+204A: "⁊" Tironian Sign Et
                    key("g", additionalMoreKey("\u204A")),
                    "h", "j", "k", "l")
            .setKeysOfRow(3, "z", "x", "c", "v", "b", "n", "m")
            .build();

    // Common symbols keyboard layout.
    // U+00A3: "£" POUND SIGN
    public static final ExpectedKey CURRENCY = key("\u00A3");
    public static final String DOUBLE_QUOTE = "DOUBLE_QUOTE";
    public static final String SINGLE_QUOTE = "SINGLE_QUOTE";
    private static final ExpectedKey[][] SYMBOLS_COMMON = new ExpectedKeyboardBuilder()
            .setKeysOfRow(1,
                    // U+00B9: "¹" SUPERSCRIPT ONE
                    // U+00BD: "½" VULGAR FRACTION ONE HALF
                    // U+2153: "⅓" VULGAR FRACTION ONE THIRD
                    // U+00BC: "¼" VULGAR FRACTION ONE QUARTER
                    // U+215B: "⅛" VULGAR FRACTION ONE EIGHTH
                    key("1", joinMoreKeys("\u00B9", "\u00BD", "\u2153", "\u00BC", "\u215B")),
                    // U+00B2: "²" SUPERSCRIPT TWO
                    // U+2154: "⅔" VULGAR FRACTION TWO THIRDS
                    key("2", joinMoreKeys("\u00B2", "\u2154")),
                    // U+00B3: "³" SUPERSCRIPT THREE
                    // U+00BE: "¾" VULGAR FRACTION THREE QUARTERS
                    // U+215C: "⅜" VULGAR FRACTION THREE EIGHTHS
                    key("3", joinMoreKeys("\u00B3", "\u00BE", "\u215C")),
                    // U+2074: "⁴" SUPERSCRIPT FOUR
                    key("4", moreKey("\u2074")),
                    // U+215D: "⅝" VULGAR FRACTION FIVE EIGHTHS
                    key("5", moreKey("\u215D")),
                    "6",
                    // U+215E: "⅞" VULGAR FRACTION SEVEN EIGHTHS
                    key("7", moreKey("\u215E")),
                    "8", "9",
                    // U+207F: "ⁿ" SUPERSCRIPT LATIN SMALL LETTER N
                    // U+2205: "∅" EMPTY SET
                    key("0", joinMoreKeys("\u207F", "\u2205")))
            .setKeysOfRow(2,
                    key("@"), key("#"), key(CURRENCY),
                    // U+2030: "‰" PER MILLE SIGN
                    key("%", moreKey("\u2030")),
                    "&",
                    // U+204A: "⁊" Tironian Sign Et
                    "\u204A",
                    // U+2013: "–" EN DASH
                    // U+2014: "—" EM DASH
                    // U+00B7: "·" MIDDLE DOT
                    key("-", joinMoreKeys("_", "\u2013", "\u2014", "\u00B7")),
                    // U+00B1: "±" PLUS-MINUS SIGN
                    key("+", moreKey("\u00B1")),
                    key("(", joinMoreKeys("<", "{", "[")),
                    key(")", joinMoreKeys(">", "}", "]")))
            .setKeysOfRow(3,
                    // U+2020: "†" DAGGER
                    // U+2021: "‡" DOUBLE DAGGER
                    // U+2605: "★" BLACK STAR
                    key("*", joinMoreKeys("\u2020", "\u2021", "\u2605")),
                    key(DOUBLE_QUOTE), key(SINGLE_QUOTE), key(":"), key(";"),
                    // U+00A1: "¡" INVERTED EXCLAMATION MARK
                    key("!", moreKey("\u00A1")),
                    // U+00BF: "¿" INVERTED QUESTION MARK
                    key("?", moreKey("\u00BF")))
            .setKeysOfRow(4,
                    key(","), key("_"), SPACE_KEY, key("/"),
                    // U+2026: "…" HORIZONTAL ELLIPSIS
                    key(".", moreKey("\u2026")))
            .build();
}
