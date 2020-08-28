/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.app.AlertDialog;
import android.app.ActivityManager.MemoryInfo;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class HardwareInfoDialogFragment extends InstrumentedDialogFragment {

    public static final String TAG = "HardwareInfo";

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.DIALOG_SETTINGS_HARDWARE_INFO;
    }

    public static HardwareInfoDialogFragment newInstance() {
        final HardwareInfoDialogFragment fragment = new HardwareInfoDialogFragment();
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.hardware_info)
                .setPositiveButton(android.R.string.ok, null);
        final View content = LayoutInflater.from(builder.getContext())
                .inflate(R.layout.dialog_hardware_info, null /* parent */);
        // Model
        setText(content, R.id.model_label, R.id.model_value,
                DeviceModelPreferenceController.getDeviceModel());

        // Serial number
        if (SystemProperties.get("ro.welcome.custom.serial").toString().equals("")) {
            setText(content, R.id.serial_number_label, R.id.serial_number_value, getSerialNumber());
        }
        else {
            setText(content, R.id.serial_number_label, R.id.serial_number_value, SystemProperties.get("ro.welcome.custom.serial"));
        }

        // Hardware rev
        setText(content, R.id.hardware_rev_label, R.id.hardware_rev_value,
                SystemProperties.get("ro.boot.hardware.revision"));

        // Total RAM
        if (SystemProperties.get("ro.welcome.fake.ram").toString().equals("")) {
            MemoryInfo mi = new MemoryInfo();
            ActivityManager activityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            long availableMegs = mi.availMem / 1048576L;
            setText(content, R.id.ram_total_title, R.id.ram_total_value,
                String.valueOf(availableMegs).concat("MB"));
        }
        else {
            setText(content, R.id.ram_total_title, R.id.ram_total_value,
                SystemProperties.get("ro.welcome.fake.ram"));
        }

        // CPU Cores
        if (SystemProperties.get("ro.welcome.fake.cpu_cores").toString().equals("")) {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            int numberOfFiles = dir.length;
            setText(content, R.id.cpu_cores_title, R.id.cpu_cores_value,
                String.valueOf(numberOfFiles));
        }
        else {
            setText(content, R.id.cpu_cores_title, R.id.cpu_cores_value,
                SystemProperties.get("ro.welcome.fake.cpu_cores"));
        }

        return builder.setView(content).create();
    }

    @VisibleForTesting
    void setText(View content, int labelViewId, int valueViewId, String value) {
        if (content == null) {
            return;
        }
        final View labelView = content.findViewById(labelViewId);
        final TextView valueView = content.findViewById(valueViewId);
        if (!TextUtils.isEmpty(value)) {
            labelView.setVisibility(View.VISIBLE);
            valueView.setVisibility(View.VISIBLE);
            valueView.setText(value);
        } else {
            labelView.setVisibility(View.GONE);
            valueView.setVisibility(View.GONE);
        }
    }

    @VisibleForTesting
    String getSerialNumber() {
        return Build.getSerial();
    }
}
