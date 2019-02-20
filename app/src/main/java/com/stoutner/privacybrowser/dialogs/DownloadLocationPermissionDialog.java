/*
 * Copyright © 2018-2019 Soren Stoutner <soren@stoutner.com>.
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
import android.os.Bundle;
import android.view.WindowManager;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

public class DownloadLocationPermissionDialog extends DialogFragment {
    // The constants are used to differentiate between the two download types.
    public static final int DOWNLOAD_FILE = 1;
    public static final int DOWNLOAD_IMAGE = 2;

    // The listener is used in `onAttach()` and `onCreateDialog()`.
    private DownloadLocationPermissionDialogListener downloadLocationPermissionDialogListener;

    // The public interface is used to send information back to the parent activity.
    public interface DownloadLocationPermissionDialogListener {
        void onCloseDownloadLocationPermissionDialog(int downloadType);
    }

    @Override
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the listener from the launching context.
        downloadLocationPermissionDialogListener = (DownloadLocationPermissionDialogListener) context;
    }

    public static DownloadLocationPermissionDialog downloadType(int type) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the download type in the bundle.
        argumentsBundle.putInt("download_type", type);

        // Add the arguments bundle to this instance of the dialog.
        DownloadLocationPermissionDialog thisDownloadLocationPermissionDialog = new DownloadLocationPermissionDialog();
        thisDownloadLocationPermissionDialog.setArguments(argumentsBundle);
        return thisDownloadLocationPermissionDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warning below that `getArguments().getInt()` might be null.
        assert getArguments() != null;

        // Store the download type in a local variable.
        int downloadType = getArguments().getInt("download_type");

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and the icon according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
            dialogBuilder.setIcon(R.drawable.downloads_dark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
            dialogBuilder.setIcon(R.drawable.downloads_light);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.download_location);

        // Set the text.
        dialogBuilder.setMessage(R.string.download_location_message);

        // Set an `onClick` listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.ok, (DialogInterface dialog, int which) -> {
            // Inform the parent activity that the dialog was closed.
            downloadLocationPermissionDialogListener.onCloseDownloadLocationPermissionDialog(downloadType);
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