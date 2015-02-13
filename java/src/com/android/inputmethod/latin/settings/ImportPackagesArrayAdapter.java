/*
 * Copyright (c) 2015 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.android.inputmethod.latin.settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import android.widget.ImageView;
import android.widget.TextView;
import com.android.inputmethod.latin.R;

public class ImportPackagesArrayAdapter extends ArrayAdapter<ApplicationInfo> {
    private PackageManager mPackageManager;

    private static class ViewHolder {
        ImageView packageIcon;
        TextView packageTitle;
    }

    public ImportPackagesArrayAdapter(Context context,
                   List<ApplicationInfo> objects) {
        super(context, 0, objects);
        mPackageManager = context.getPackageManager();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ApplicationInfo applicationInfo = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(
                    getContext()).inflate(R.layout.import_item_view, parent, false);
            viewHolder.packageIcon = (ImageView) convertView.findViewById(R.id.dictionary_icon);
            viewHolder.packageTitle = (TextView) convertView.findViewById(R.id.dictionary_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.packageIcon.setImageDrawable(applicationInfo.loadIcon(mPackageManager));
        viewHolder.packageTitle.setText(applicationInfo.loadLabel(mPackageManager));
        return convertView;
    }
}
