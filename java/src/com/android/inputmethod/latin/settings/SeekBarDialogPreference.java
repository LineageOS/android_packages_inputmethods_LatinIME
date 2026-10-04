/*
 * Copyright (C) 2013 The Android Open Source Project
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

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.android.inputmethod.latin.R;
import com.google.android.material.slider.Slider;

public final class SeekBarDialogPreference extends DialogPreference {
    public interface ValueProxy {
        public int readValue(final String key);
        public int readDefaultValue(final String key);
        public void writeValue(final int value, final String key);
        public void writeDefaultValue(final String key);
        public String getValueText(final int value);
        public void feedbackValue(final int value);
    }

    private final int mMaxValue;
    private final int mMinValue;
    private final int mStepValue;

    private ValueProxy mValueProxy;

    public SeekBarDialogPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.SeekBarDialogPreference, 0, 0);
        mMaxValue = a.getInt(R.styleable.SeekBarDialogPreference_maxValue, 0);
        mMinValue = a.getInt(R.styleable.SeekBarDialogPreference_minValue, 0);
        mStepValue = a.getInt(R.styleable.SeekBarDialogPreference_stepValue, 0);
        a.recycle();
        setDialogLayoutResource(R.layout.seek_bar_dialog);
    }

    public void setInterface(final ValueProxy proxy) {
        mValueProxy = proxy;
        final int value = mValueProxy.readValue(getKey());
        setSummary(mValueProxy.getValueText(value));
    }

    private int clipValue(final int value) {
        final int clippedValue = Math.min(mMaxValue, Math.max(mMinValue, value));
        if (mStepValue <= 1) {
            return clippedValue;
        }
        return clippedValue - (clippedValue % mStepValue);
    }

    private void writeValue(final int value) {
        setSummary(mValueProxy.getValueText(value));
        mValueProxy.writeValue(value, getKey());
    }

    private void writeDefaultValue() {
        final String key = getKey();
        setSummary(mValueProxy.getValueText(mValueProxy.readDefaultValue(key)));
        mValueProxy.writeDefaultValue(key);
    }

    public static final class SeekBarDialogFragment extends PreferenceDialogFragmentCompat
            implements Slider.OnChangeListener, Slider.OnSliderTouchListener {
        private TextView mValueView;
        private Slider mSlider;

        public static SeekBarDialogFragment newInstance(final String key) {
            final SeekBarDialogFragment fragment = new SeekBarDialogFragment();
            final Bundle args = new Bundle(1);
            args.putString(ARG_KEY, key);
            fragment.setArguments(args);
            return fragment;
        }

        private SeekBarDialogPreference getSeekBarPreference() {
            return (SeekBarDialogPreference) getPreference();
        }

        @Override
        protected void onBindDialogView(@NonNull final View view) {
            super.onBindDialogView(view);
            final SeekBarDialogPreference pref = getSeekBarPreference();
            mSlider = (Slider) view.findViewById(R.id.seek_bar_dialog_bar);
            mSlider.setValueFrom(pref.mMinValue);
            mSlider.setValueTo(pref.mMaxValue);
            mValueView = (TextView) view.findViewById(R.id.seek_bar_dialog_value);
            final int value = pref.clipValue(pref.mValueProxy.readValue(pref.getKey()));
            mValueView.setText(pref.mValueProxy.getValueText(value));
            mSlider.setValue(value);
            mSlider.addOnChangeListener(this);
            mSlider.addOnSliderTouchListener(this);
        }

        @Override
        protected void onPrepareDialogBuilder(@NonNull final AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            builder.setNeutralButton(R.string.button_default, this);
        }

        @Override
        public void onClick(@NonNull final DialogInterface dialog, final int which) {
            super.onClick(dialog, which);
            if (which == DialogInterface.BUTTON_NEUTRAL) {
                getSeekBarPreference().writeDefaultValue();
            }
        }

        @Override
        public void onDialogClosed(final boolean positiveResult) {
            if (positiveResult) {
                final SeekBarDialogPreference pref = getSeekBarPreference();
                pref.writeValue(pref.clipValue((int) mSlider.getValue()));
            }
        }

        @Override
        public void onValueChange(@NonNull final Slider slider, final float value,
                final boolean fromUser) {
            final SeekBarDialogPreference pref = getSeekBarPreference();
            final int clippedValue = pref.clipValue((int) value);
            mValueView.setText(pref.mValueProxy.getValueText(clippedValue));
            if (clippedValue != (int) value) {
                slider.setValue(clippedValue);
            }
        }

        @Override
        public void onStartTrackingTouch(@NonNull final Slider slider) {}

        @Override
        public void onStopTrackingTouch(@NonNull final Slider slider) {
            final SeekBarDialogPreference pref = getSeekBarPreference();
            pref.mValueProxy.feedbackValue(pref.clipValue((int) slider.getValue()));
        }
    }
}
