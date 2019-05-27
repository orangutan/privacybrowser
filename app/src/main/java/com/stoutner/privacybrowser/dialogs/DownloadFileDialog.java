/*
 * Copyright © 2016-2019 Soren Stoutner <soren@stoutner.com>.
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;

import java.util.Locale;

public class DownloadFileDialog extends DialogFragment {
    // `downloadFileListener` is used in `onAttach()` and `onCreateDialog()`.
    private DownloadFileListener downloadFileListener;

    // The public interface is used to send information back to the parent activity.
    public interface DownloadFileListener {
        void onDownloadFile(DialogFragment dialogFragment, String downloadUrl);
    }

    @Override
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `DownloadFileListener` from the launching context.
        downloadFileListener = (DownloadFileListener) context;
    }

    public static DownloadFileDialog fromUrl(String urlString, String contentDisposition, long contentLength) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Create a variable for the file name string.
        String fileNameString;

        // Parse the filename from `contentDisposition`.
        if (contentDisposition.contains("filename=\"")) {  // The file name is contained in a string surrounded by `""`.
            fileNameString = contentDisposition.substring(contentDisposition.indexOf("filename=\"") + 10, contentDisposition.indexOf("\"", contentDisposition.indexOf("filename=\"") + 10));
        } else if (contentDisposition.contains("filename=") && ((contentDisposition.indexOf(";", contentDisposition.indexOf("filename=") + 9)) > 0 )) {
            // The file name is contained in a string beginning with `filename=` and ending with `;`.
            fileNameString = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9, contentDisposition.indexOf(";", contentDisposition.indexOf("filename=") + 9));
        } else if (contentDisposition.contains("filename=")) {  // The file name is contained in a string beginning with `filename=` and proceeding to the end of `contentDisposition`.
            fileNameString = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
        } else {  // `contentDisposition` does not contain the filename, so use the last path segment of the URL.
            Uri downloadUri = Uri.parse(urlString);
            fileNameString = downloadUri.getLastPathSegment();
        }

        // Store the variables in the bundle.
        argumentsBundle.putString("URL", urlString);
        argumentsBundle.putString("File_Name", fileNameString);
        argumentsBundle.putLong("File_Size", contentLength);

        // Add the arguments bundle to this instance of `DownloadFileDialog`.
        DownloadFileDialog thisDownloadFileDialog = new DownloadFileDialog();
        thisDownloadFileDialog.setArguments(argumentsBundle);
        return thisDownloadFileDialog;
    }

    @Override
    @NonNull
    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the warning below that `getArguments()` might be null.
        assert getArguments() != null;

        // Store the variables from the bundle.
        String downloadUrl = getArguments().getString("URL");
        String downloadFileName = getArguments().getString("File_Name");
        long fileSizeLong = getArguments().getLong("File_Size");

        // Initialize the file size string.
        String fileSize;

        // Convert `fileSizeLong` to a String.
        if (fileSizeLong == -1) {  // We don't know the file size.
            fileSize = getString(R.string.unknown_size);
        } else {  // Convert `fileSize` to MB and store it in `fileSizeString`.  `%.3g` displays the three most significant digits.
            fileSize = String.format(Locale.getDefault(), "%.3g", (float) fileSizeLong / 1048576) + " MB";
        }

        // Remove the warning below that `getActivity()` might be null;
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

        // Set the style according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.save_as);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.download_file_dialog, null));

        // Set an listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing if `Cancel` is clicked.  The `Dialog` will automatically close.
        });

        // Set an listener on the positive button
        dialogBuilder.setPositiveButton(R.string.download, (DialogInterface dialog, int which) -> {
            // trigger `onDownloadFile()` and return the `DialogFragment` and the download URL to the parent activity.
            downloadFileListener.onDownloadFile(DownloadFileDialog.this, downloadUrl);
        });

        // Create an alert dialog from the alert dialog builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when alert dialog is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // We need to show `alertDialog` before we can modify the contents.
        alertDialog.show();

        // Set the text for `downloadFileSizeTextView`.
        TextView downloadFileSizeTextView = alertDialog.findViewById(R.id.download_file_size);
        downloadFileSizeTextView.setText(fileSize);

        // Set the text for `downloadFileNameTextView`.
        EditText downloadFileNameTextView = alertDialog.findViewById(R.id.download_file_name);
        downloadFileNameTextView.setText(downloadFileName);

        // Allow the `enter` key on the keyboard to save the file from `downloadFileNameTextView`.
        downloadFileNameTextView.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // If the event is an `ACTION_DOWN` on the `enter` key, initiate the download.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // trigger `onDownloadFile()` and return the `DialogFragment` and the URL to the parent activity.
                downloadFileListener.onDownloadFile(DownloadFileDialog.this, downloadUrl);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}