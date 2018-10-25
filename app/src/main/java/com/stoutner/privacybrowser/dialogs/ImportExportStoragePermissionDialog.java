/*
 * Copyright © 2018 Soren Stoutner <soren@stoutner.com>.
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
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

public class ImportExportStoragePermissionDialog extends DialogFragment {
    // The constants are used to differentiate between the two commands.
    public static final int EXPORT_SETTINGS = 1;
    public static final int IMPORT_SETTINGS = 2;

    // The listener is used in `onAttach()` and `onCreateDialog()`.
    private ImportExportStoragePermissionDialogListener importExportStoragePermissionDialogListener;

    // The public interface is used to send information back to the parent activity.
    public interface ImportExportStoragePermissionDialogListener {
        void onCloseImportExportStoragePermissionDialog(int type);
    }

    @Override
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the listener from the launching context.
        importExportStoragePermissionDialogListener = (ImportExportStoragePermissionDialogListener) context;
    }

    public static ImportExportStoragePermissionDialog type(int type) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the download type in the bundle.
        argumentsBundle.putInt("type", type);

        // Add the arguments bundle to this instance of the dialog.
        ImportExportStoragePermissionDialog thisImportExportStoragePermissionDialog = new ImportExportStoragePermissionDialog();
        thisImportExportStoragePermissionDialog.setArguments(argumentsBundle);
        return thisImportExportStoragePermissionDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Store the download type in a local variable.
        int type = getArguments().getInt("type");

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and the icon according to the theme.
        if (MainWebViewActivity.darkTheme) {
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

        // Set an `onClick` listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.ok, (DialogInterface dialog, int which) -> {
            // Inform the parent activity that the dialog was closed.
            importExportStoragePermissionDialogListener.onCloseImportExportStoragePermissionDialog(type);
        });

        // Create an alert dialog from the builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}
