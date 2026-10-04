/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.settings;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.inputmethod.latin.utils.FragmentUtils;
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.android.settingslib.collapsingtoolbar.R;

public abstract class SettingsBaseActivity extends CollapsingToolbarBaseActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private CharSequence mInitialTitle;

    protected abstract String getDefaultFragment();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInitialTitle = getTitle();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this::updateTitle);
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, instantiate(getDefaultFragment(), null))
                    .commit();
        } else {
            updateTitle();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull final PreferenceFragmentCompat caller,
            @NonNull final Preference pref) {
        startFragment(pref.getFragment(), pref.getExtras(), pref.getTitle());
        return true;
    }

    public void startFragment(final String fragmentName, final Bundle args,
            final CharSequence title) {
        final String backStackName = TextUtils.isEmpty(title) ? null : title.toString();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, instantiate(fragmentName, args))
                .addToBackStack(backStackName)
                .commit();
    }

    private Fragment instantiate(final String fragmentName, final Bundle args) {
        if (!FragmentUtils.isValidFragment(fragmentName)) {
            throw new IllegalArgumentException("Invalid fragment: " + fragmentName);
        }
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory()
                .instantiate(getClassLoader(), fragmentName);
        fragment.setArguments(args);
        return fragment;
    }

    private void updateTitle() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final int count = fragmentManager.getBackStackEntryCount();
        final String name = count == 0 ? null
                : fragmentManager.getBackStackEntryAt(count - 1).getName();
        setTitle(name != null ? name : mInitialTitle);
    }
}
