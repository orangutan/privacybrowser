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
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

public class DownloadImageDialog extends DialogFragment {
    // `downloadImageListener` is used in `onAttach()` and `onCreateDialog()`.
    private DownloadImageListener downloadImageListener;

    // The public interface is used to send information back to the parent activity.
    public interface DownloadImageListener {
        void onDownloadImage(DialogFragment dialogFragment, String downloadUrl);
    }

    // Check to make sure tha the parent activity implements the listener.
    @Override
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `DownloadImageListener` from the launching context.
        downloadImageListener = (DownloadImageListener) context;
    }

    public static DownloadImageDialog imageUrl(String imageUrlString) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Create a variable for the image name string.
        String imageNameString;

        // Extract the image name string from the image URL.
        Uri imageUri = Uri.parse(imageUrlString);
        imageNameString = imageUri.getLastPathSegment();

        // Store the variables in the bundle.
        argumentsBundle.putString("URL", imageUrlString);
        argumentsBundle.putString("Image_Name", imageNameString);

        // Add the arguments bundle to this instance of `DownloadFileDialog`.
        DownloadImageDialog thisDownloadFileDialog = new DownloadImageDialog();
        thisDownloadFileDialog.setArguments(argumentsBundle);
        return thisDownloadFileDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the warning below that `getArguments()` might be null.
        assert getArguments() != null;

        // Get the strings from the bundle.
        String imageUrl = getArguments().getString("URL");
        String imageFileName = getArguments().getString("Image_Name");

        // Remove the warning below that `.getActivity()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use and alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.save_image_as);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.download_image_dialog, null));

        // Set an listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing if `Cancel` is clicked.
        });

        // Set an listener on the positive button
        dialogBuilder.setPositiveButton(R.string.download, (DialogInterface dialog, int which) -> {
            // trigger `onDownloadFile()` and return the `DialogFragment` and the download URL to the parent activity.
            downloadImageListener.onDownloadImage(DownloadImageDialog.this, imageUrl);
        });


        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when `alertDialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The alert dialog must be shown before the contents can be modified.
        alertDialog.show();

        // Set the text for `downloadImageNameTextView`.
        EditText downloadImageNameTextView = alertDialog.findViewById(R.id.download_image_name);
        downloadImageNameTextView.setText(imageFileName);

        // Allow the `enter` key on the keyboard to save the file from `downloadImageNameTextView`.
        downloadImageNameTextView.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // If the event is an `ACTION_DOWN` on the `enter` key, initiate the download.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // trigger `onDownloadImage()` and return the `DialogFragment` and the URL to the parent activity.
                downloadImageListener.onDownloadImage(DownloadImageDialog.this, imageUrl);

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