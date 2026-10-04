/*
 * Copyright (C) 2020 The LineageOS Project
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

package com.android.inputmethod.latin.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.android.settingslib.widget.SettingsBasePreferenceFragment;

public abstract class CustomPreferenceFragment extends SettingsBasePreferenceFragment {
    private static final String DIALOG_FRAGMENT_TAG = "CustomPreferenceFragment.DIALOG";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onDisplayPreferenceDialog(@NonNull final Preference preference) {
        if (preference instanceof SeekBarDialogPreference) {
            showPreferenceDialog(SeekBarDialogPreference.SeekBarDialogFragment.newInstance(
                    preference.getKey()));
        } else if (preference instanceof CustomInputStylePreference) {
            showPreferenceDialog(CustomInputStylePreference.CustomInputStyleDialogFragment
                    .newInstance(preference.getKey()));
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @SuppressWarnings("deprecation")
    private void showPreferenceDialog(final PreferenceDialogFragmentCompat dialogFragment) {
        if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }
        dialogFragment.setTargetFragment(this, 0);
        dialogFragment.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
    }
}
