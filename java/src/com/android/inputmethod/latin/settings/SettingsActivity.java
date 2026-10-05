/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

import com.android.inputmethod.latin.permissions.PermissionsManager;
import com.android.inputmethod.latin.utils.StatsUtils;

public final class SettingsActivity extends SettingsBaseActivity {
    public static final String EXTRA_SHOW_HOME_AS_UP = "show_home_as_up";
    public static final String EXTRA_ENTRY_KEY = "entry";
    public static final String EXTRA_ENTRY_VALUE_LONG_PRESS_COMMA = "long_press_comma";
    public static final String EXTRA_ENTRY_VALUE_APP_ICON = "app_icon";
    public static final String EXTRA_ENTRY_VALUE_NOTICE_DIALOG = "important_notice";
    public static final String EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS = "system_settings";

    @Override
    protected Fragment createDefaultFragment() {
        return new SettingsFragment();
    }

    @Override
    protected void onCreate(final Bundle savedState) {
        super.onCreate(savedState);
        final ActionBar actionBar = getSupportActionBar();
        final Intent intent = getIntent();
        if (actionBar != null && !intent.getBooleanExtra(EXTRA_SHOW_HOME_AS_UP, true)) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        StatsUtils.onSettingsActivity(
                intent.hasExtra(EXTRA_ENTRY_KEY) ? intent.getStringExtra(EXTRA_ENTRY_KEY)
                        : EXTRA_ENTRY_VALUE_SYSTEM_SETTINGS);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
            @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsManager.get(this).onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }
}
