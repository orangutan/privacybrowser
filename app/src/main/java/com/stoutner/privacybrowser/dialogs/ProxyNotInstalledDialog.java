/*
 * Copyright © 2019 Soren Stoutner <soren@stoutner.com>.
 *
 * This file is part of Privacy Browser <https://www.stoutner.com/privacy-browser>.
 *
 * Privacy Browser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Browser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Browser.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stoutner.privacybrowser.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;


import com.stoutner.privacybrowser.helpers.ProxyHelper;
import com.stoutner.privacybrowser.R;

public class ProxyNotInstalledDialog extends DialogFragment {
    public static ProxyNotInstalledDialog displayDialog(String proxyMode) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the proxy mode in the bundle.
        argumentsBundle.putString("proxy_mode", proxyMode);

        // Create a new instance of the dialog.
        ProxyNotInstalledDialog proxyNotInstalledDialog = new ProxyNotInstalledDialog();

        // Add the bundle to the dialog.
        proxyNotInstalledDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return proxyNotInstalledDialog;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the context and the activity.
        Context context = getContext();
        Activity activity = getActivity();

        // Remove the incorrect lint warnings below that the context or the activity might be null.
        assert context != null;
        assert activity != null;

        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning below that the arguments might be null.
        assert arguments != null;

        // Get the proxy mode from the arguments.
        String proxyMode = arguments.getString("proxy_mode");

        // Remove the incorrect lint warning below tha tth eproxy mode migth be null.
        assert proxyMode != null;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the screenshot and the theme preferences.
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and the icon according to the theme.
        if (darkTheme) {
            // Set the style.
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogDark);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.proxy_enabled_dark);
        } else {
            // Set the style.
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogLight);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.proxy_enabled_light);
        }

        // Set the title and the message according to the proxy mode.
        switch (proxyMode) {
            case ProxyHelper.TOR:
                // Set the title.
                dialogBuilder.setTitle(R.string.orbot_not_installed_title);

                // Set the message.
                dialogBuilder.setMessage(R.string.orbot_not_installed_message);
                break;

            case ProxyHelper.I2P:
                // Set the title.
                dialogBuilder.setTitle(R.string.i2p_not_installed_title);

                // Set the message.
                dialogBuilder.setMessage(R.string.i2p_not_installed_message);
                break;
        }

        // Set the positive button.
        dialogBuilder.setPositiveButton(R.string.close, (DialogInterface dialog, int which) -> {
            // Do nothing.  The alert dialog will close automatically.
        });

        // Create an alert dialog from the alert dialog builder.
        AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            // Remove the warning below that `getWindows()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags((WindowManager.LayoutParams.FLAG_SECURE));
        }

        // Return the alert dialog.
        return alertDialog;
    }
}