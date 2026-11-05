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

package com.android.inputmethod.latin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

/**
 * Cache for dictionary facilitators of multiple locales.
 * This class automatically creates and releases up to 3 facilitator instances using LRU policy.
 */
public class DictionaryFacilitatorLruCache {
    private static final String TAG = "DictionaryFacilitatorLruCache";
    private static final int WAIT_FOR_LOADING_MAIN_DICT_IN_MILLISECONDS = 1000;
    private static final int MAX_RETRY_COUNT_FOR_WAITING_FOR_LOADING_DICT = 5;

    private final Context mContext;
    private final String mDictionaryNamePrefix;
    private final Object mLock = new Object();
    private final DictionaryFacilitator mDictionaryFacilitator;
    private boolean mUseContactsDictionary;
    private boolean mMultilingualSpellCheck;
    private List<Locale> mSecondaryLocales = Collections.emptyList();
    private Locale mLocale;

    public DictionaryFacilitatorLruCache(final Context context, final String dictionaryNamePrefix) {
        mContext = context;
        mDictionaryNamePrefix = dictionaryNamePrefix;
        mDictionaryFacilitator = DictionaryFacilitatorProvider.getDictionaryFacilitator(
                true /* isNeededForSpellChecking */);
    }

    private static void waitForLoadingMainDictionary(
            final DictionaryFacilitator dictionaryFacilitator) {
        for (int i = 0; i < MAX_RETRY_COUNT_FOR_WAITING_FOR_LOADING_DICT; i++) {
            try {
                dictionaryFacilitator.waitForLoadingMainDictionaries(
                        WAIT_FOR_LOADING_MAIN_DICT_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
                return;
            } catch (final InterruptedException e) {
                Log.i(TAG, "Interrupted during waiting for loading main dictionary.", e);
                if (i < MAX_RETRY_COUNT_FOR_WAITING_FOR_LOADING_DICT - 1) {
                    Log.i(TAG, "Retry", e);
                } else {
                    Log.w(TAG, "Give up retrying. Retried "
                            + MAX_RETRY_COUNT_FOR_WAITING_FOR_LOADING_DICT + " times.", e);
                }
            }
        }
    }

    private void resetDictionariesForLocaleLocked() {
        // Nothing to do if the locale is null.  This would be the case before any get() calls.
        if (mLocale != null) {
          mSecondaryLocales = mMultilingualSpellCheck
              ? getSecondaryLocalesLocked(mLocale) : Collections.<Locale>emptyList();
          // Note: Given that personalized dictionaries are not used here; we can pass null account.
          mDictionaryFacilitator.resetDictionaries(mContext, mLocale, mSecondaryLocales,
              mUseContactsDictionary, false /* usePersonalizedDicts */,
              false /* forceReloadMainDictionary */, null /* account */,
              mDictionaryNamePrefix, null /* listener */);
        }
    }

    /**
     * Returns the locales of the other enabled keyboard languages (excluding the primary one), so
     * their dictionaries can be consulted when checking spelling in a multilingual setup.
     */
    private List<Locale> getSecondaryLocalesLocked(final Locale primaryLocale) {
        final InputMethodManager imm =
                (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return Collections.emptyList();
        }
        final String packageName = mContext.getPackageName();
        final ArrayList<Locale> secondaryLocales = new ArrayList<>();
        for (final InputMethodInfo imi : imm.getInputMethodList()) {
            if (!packageName.equals(imi.getPackageName())) {
                continue;
            }
            for (final InputMethodSubtype subtype
                    : imm.getEnabledInputMethodSubtypeList(imi, true /* allowsImplicit */)) {
                // Resolve the locale the same way as the keyboard so the primary is reliably
                // excluded (RichInputMethodSubtype may remap it).
                final RichInputMethodSubtype richSubtype =
                        RichInputMethodSubtype.getRichInputMethodSubtype(subtype);
                if (richSubtype.isNoLanguage()) {
                    continue;
                }
                final Locale locale = richSubtype.getLocale();
                if (locale != null && !locale.equals(primaryLocale)
                        && !secondaryLocales.contains(locale)) {
                    secondaryLocales.add(locale);
                }
            }
            break;
        }
        return secondaryLocales;
    }

    public void setUseContactsDictionary(final boolean useContactsDictionary) {
        synchronized (mLock) {
            if (mUseContactsDictionary == useContactsDictionary) {
                // The value has not been changed.
                return;
            }
            mUseContactsDictionary = useContactsDictionary;
            resetDictionariesForLocaleLocked();
            waitForLoadingMainDictionary(mDictionaryFacilitator);
        }
    }

    public void setMultilingualSpellCheck(final boolean multilingualSpellCheck) {
        synchronized (mLock) {
            if (mMultilingualSpellCheck == multilingualSpellCheck) {
                // The value has not been changed.
                return;
            }
            mMultilingualSpellCheck = multilingualSpellCheck;
            resetDictionariesForLocaleLocked();
            waitForLoadingMainDictionary(mDictionaryFacilitator);
        }
    }

    public DictionaryFacilitator get(final Locale locale) {
        synchronized (mLock) {
            if (!mDictionaryFacilitator.isForLocales(locale, mSecondaryLocales)) {
                mLocale = locale;
                resetDictionariesForLocaleLocked();
            }
            waitForLoadingMainDictionary(mDictionaryFacilitator);
            return mDictionaryFacilitator;
        }
    }

    public void closeDictionaries() {
        synchronized (mLock) {
            mDictionaryFacilitator.closeDictionaries();
        }
    }
}
