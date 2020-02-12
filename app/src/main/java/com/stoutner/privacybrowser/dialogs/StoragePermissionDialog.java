/*
 * Copyright © 2018-2020 Soren Stoutner <soren@stoutner.com>.
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.stoutner.privacybrowser.R;

public class StoragePermissionDialog extends DialogFragment {
    // Define the save type constants.
    public static final int OPEN = 0;
    public static final int SAVE = 1;
    public static final int SAVE_AS_ARCHIVE = 2;
    public static final int SAVE_AS_IMAGE = 3;

    // The listener is used in `onAttach()` and `onCreateDialog()`.
    private StoragePermissionDialogListener storagePermissionDialogListener;

    // The public interface is used to send information back to the parent activity.
    public interface StoragePermissionDialogListener {
        void onCloseStoragePermissionDialog(int requestType);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the listener from the launching context.
        storagePermissionDialogListener = (StoragePermissionDialogListener) context;
    }

    public static StoragePermissionDialog displayDialog(int requestType) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the save type in the bundle.
        argumentsBundle.putInt("request_type", requestType);

        // Create a new instance of the storage permission dialog.
        StoragePermissionDialog storagePermissionDialog = new StoragePermissionDialog();

        // Add the arguments bundle to the new dialog.
        storagePermissionDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return storagePermissionDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning that the arguments might be null.
        assert arguments != null;

        // Get the save type.
        int requestType = arguments.getInt("request_type");

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and the icon according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
            dialogBuilder.setIcon(R.drawable.import_export_dark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
            dialogBuilder.setIcon(R.drawable.import_export_light);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.storage_permission);

        // Set the text.
        dialogBuilder.setMessage(R.string.storage_permission_message);

        // Set an listener on the OK button.
        dialogBuilder.setNegativeButton(R.string.ok, (DialogInterface dialog, int which) -> {
            // Inform the parent activity that the dialog was closed.
            storagePermissionDialogListener.onCloseStoragePermissionDialog(requestType);
        });

        // Create an alert dialog from the builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Return the alert dialog.
        return alertDialog;
    }
}