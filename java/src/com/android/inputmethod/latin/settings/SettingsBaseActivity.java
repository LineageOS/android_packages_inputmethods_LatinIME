/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.inputmethod.latin.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.inputmethod.latin.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.color.DynamicColors;

public abstract class SettingsBaseActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private CharSequence mInitialTitle;

    protected abstract Fragment createDefaultFragment();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(findViewById(R.id.toolbar));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mInitialTitle = getTitle();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this::updateTitle);
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.settings, createDefaultFragment())
                    .commit();
        } else {
            updateTitle();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull final PreferenceFragmentCompat caller,
            @NonNull final Preference pref) {
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory()
                .instantiate(getClassLoader(), pref.getFragment());
        fragment.setArguments(pref.getExtras());
        startFragment(fragment, pref.getTitle());
        return true;
    }

    public void startFragment(final Fragment fragment, final CharSequence title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(TextUtils.isEmpty(title) ? null : title.toString())
                .commit();
    }

    AppBarLayout getAppBarLayout() {
        return findViewById(R.id.appBarLayout);
    }

    private void updateTitle() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final int count = fragmentManager.getBackStackEntryCount();
        final String name = count == 0 ? null
                : fragmentManager.getBackStackEntryAt(count - 1).getName();
        setTitle(name != null ? name : mInitialTitle);
    }
}
