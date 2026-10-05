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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

public abstract class CustomPreferenceFragment extends PreferenceFragmentCompat {
    private static final String DIALOG_FRAGMENT_TAG = "CustomPreferenceFragment.DIALOG";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final AppBarLayout appBarLayout = getAppBarLayout();
        if (appBarLayout != null) {
            appBarLayout.setExpanded(true, false);
            appBarLayout.setLiftOnScrollTargetView(getListView());
        }
    }

    @Override
    public void onDestroyView() {
        final AppBarLayout appBarLayout = getAppBarLayout();
        if (appBarLayout != null) {
            appBarLayout.setLiftOnScrollTargetView(null);
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public RecyclerView onCreateRecyclerView(@NonNull final LayoutInflater inflater,
            @NonNull final ViewGroup parent, final Bundle savedInstanceState) {
        final RecyclerView recyclerView =
                super.onCreateRecyclerView(inflater, parent, savedInstanceState);
        recyclerView.setClipToPadding(false);
        recyclerView.setVerticalScrollBarEnabled(false);
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, windowInsets) -> {
            final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(insets.left, v.getPaddingTop(), insets.right, insets.bottom);
            return windowInsets;
        });
        return recyclerView;
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

    private AppBarLayout getAppBarLayout() {
        return getActivity() instanceof SettingsBaseActivity
                ? ((SettingsBaseActivity) getActivity()).getAppBarLayout() : null;
    }
}
