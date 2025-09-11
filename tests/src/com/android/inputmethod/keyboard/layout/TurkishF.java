/*
 * Copyright (C) 2025 The Android Open Source Project
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

public final class TurkishF extends LayoutBase {
    private static final String LAYOUT_NAME = "turkish_f";

    public TurkishF(final LayoutCustomizer customizer) {
        super(customizer, Symbols.class, SymbolsShifted.class);
    }

    @Override
    public String getName() { return LAYOUT_NAME; }

    @Override
    ExpectedKey[][] getCommonAlphabetLayout(final boolean isPhone) { return ALPHABET_COMMON; }

    public static final String ROW1_3 = "ROW1_3";
    public static final String ROW1_4 = "ROW1_4";
    public static final String ROW2_5 = "ROW2_5";
    public static final String ROW2_11 = "ROW2_11";
    public static final String ROW3_2 = "ROW3_2";
    public static final String ROW3_5 = "ROW3_5";

    private static final ExpectedKey[][] ALPHABET_COMMON = new ExpectedKeyboardBuilder()
            .setKeysOfRow(1,
                    key("f", additionalMoreKey("1")),
                    key("g", additionalMoreKey("2")),
                    key(ROW1_3, additionalMoreKey("3")),
                    key(ROW1_4, additionalMoreKey("4")),
                    key("o", additionalMoreKey("5")),
                    key("d", additionalMoreKey("6")),
                    key("r", additionalMoreKey("7")),
                    key("n", additionalMoreKey("8")),
                    key("h", additionalMoreKey("9")),
                    key("p", additionalMoreKey("0")),
                    "q",
                    "w")
            .setKeysOfRow(2, "u", "i", "e", "a", ROW2_5, "t", "k", "m", "l", "y", ROW2_11, "x")
            .setKeysOfRow(3, "j", ROW3_2, "v", "c", ROW3_5, "z", "s", "b")
            .build();
}
