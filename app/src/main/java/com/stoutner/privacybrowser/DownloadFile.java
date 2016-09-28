/**
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
// `android.support.v7.app.AlertDialog` uses more of the horizontal screen real estate versus `android.app.AlertDialog's` smaller width.
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class DownloadFile extends DialogFragment {

    private String downloadUrl;
    private String downloadFileName;
    private String fileSize;

    public static DownloadFile fromUrl(String urlString, String contentDisposition, long contentLength) {
        // Create `argumentsBundle`.
        Bundle argumentsBundle = new Bundle();

        String fileNameString;
        if (!contentDisposition.isEmpty()) {  // Extract `fileNameString` from `contentDisposition` using the substring beginning after `filename="` and ending one character before the end of `contentDisposition`.
            fileNameString = contentDisposition.substring(contentDisposition.indexOf("filename=\"") + 10, contentDisposition.length() - 1);
        } else {  // `contentDisposition` is empty, so use the last path segment of the URL as the file name.
            Uri downloadUri = Uri.parse(urlString);
            fileNameString = downloadUri.getLastPathSegment();
        }

        // Store the variables in the `Bundle`.
        argumentsBundle.putString("URL", urlString);
        argumentsBundle.putString("File_Name", fileNameString);
        argumentsBundle.putLong("File_Size", contentLength);

        // Add `argumentsBundle` to this instance of `DownloadFile`.
        DownloadFile thisDownloadFileDialog = new DownloadFile();
        thisDownloadFileDialog.setArguments(argumentsBundle);
        return thisDownloadFileDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the strings in the local class variables.
        downloadUrl = getArguments().getString("URL");
        downloadFileName = getArguments().getString("File_Name");

        // Get the `File_Size`.
        long fileSizeLong = getArguments().getLong("File_Size");

        // Convert `fileSizeLong` to a String.
        if (fileSizeLong == -1) {  // We don't know the file size.
            fileSize = getString(R.string.unknown_size);
        } else {  // Convert `fileSize` to MB and store it in `fileSizeString`.  `%.3g` displays the three most significant digits.
            fileSize = String.format(Locale.getDefault(), "%.3g", (float) fileSizeLong / 1048576) + " MB";
        }
    }

    // The public interface is used to send information back to the parent activity.
    public interface DownloadFileListener {
        void onDownloadFile(DialogFragment dialogFragment, String downloadUrl);
    }

    // `downloadFileListener` is used in `onAttach()` and `onCreateDialog()`.
    private DownloadFileListener downloadFileListener;

    // Check to make sure tha the parent activity implements the listener.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            downloadFileListener = (DownloadFileListener) context;
        } catch (ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement DownloadFileListener.");
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use `AlertDialog.Builder` to create the `AlertDialog`.  `R.style.lightAlertDialog` formats the color of the button text.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        dialogBuilder.setTitle(R.string.save_as);
        // The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.download_file_dialog, null));

        // Set an `onClick()` listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing if `Cancel` is clicked.
            }
        });

        // Set an `onClick()` listener on the positive button
        dialogBuilder.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // trigger `onDownloadFile()` and return the `DialogFragment` and the download URL to the parent activity.
                downloadFileListener.onDownloadFile(DownloadFile.this, downloadUrl);
            }
        });


        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `setSoftInputMode` might produce `java.lang.NullPointerException`.
        assert alertDialog.getWindow() != null;

        // Show the keyboard when `alertDialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // We need to show `alertDialog` before we can modify the contents.
        alertDialog.show();

        // Set the text for `downloadFileSizeTextView`.
        TextView downloadFileSizeTextView = (TextView) alertDialog.findViewById(R.id.download_file_size);
        assert downloadFileSizeTextView != null;  // Remove the warning on the following line that `downloadFileSizeTextView` might be `null`.
        downloadFileSizeTextView.setText(fileSize);

        // Set the text for `downloadFileNameTextView`.
        EditText downloadFileNameTextView = (EditText) alertDialog.findViewById(R.id.download_file_name);
        assert downloadFileNameTextView != null;  // Remove the warning on the following line that `downloadFileNameTextView` might be `null`.
        downloadFileNameTextView.setText(downloadFileName);

        // Allow the `enter` key on the keyboard to save the file from `downloadFileNameTextView`.
        downloadFileNameTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey (View v, int keyCode, KeyEvent event) {
                // If the event is an `ACTION_DOWN` on the `enter` key, initiate the download.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // trigger `onDownloadFile()` and return the `DialogFragment` and the URL to the parent activity.
                    downloadFileListener.onDownloadFile(DownloadFile.this, downloadUrl);
                    // Manually dismiss `alertDialog`.
                    alertDialog.dismiss();
                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });


        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}