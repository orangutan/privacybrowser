/*
 * Copyright © 2016-2017 Soren Stoutner <soren@stoutner.com>.
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
import android.support.annotation.NonNull;
// We have to use `AppCompatDialogFragment` instead of `DialogFragment` or an error is produced on API <= 22.
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

// `android.support.v7.app.AlertDialog` uses more of the horizontal screen real estate versus `android.app.AlertDialog's` smaller width.
// We have to use `AppCompatDialogFragment` instead of `DialogFragment` or an error is produced on API <=22.
public class DownloadImageDialog extends AppCompatDialogFragment {

    private String imageUrl;
    private String imageFileName;

    public static DownloadImageDialog imageUrl(String imageUrlString) {
        // Create `argumentsBundle`.
        Bundle argumentsBundle = new Bundle();

        String imageNameString;

        Uri imageUri = Uri.parse(imageUrlString);
        imageNameString = imageUri.getLastPathSegment();

        // Store the variables in the `Bundle`.
        argumentsBundle.putString("URL", imageUrlString);
        argumentsBundle.putString("Image_Name", imageNameString);

        // Add `argumentsBundle` to this instance of `DownloadFileDialog`.
        DownloadImageDialog thisDownloadFileDialog = new DownloadImageDialog();
        thisDownloadFileDialog.setArguments(argumentsBundle);
        return thisDownloadFileDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the strings in the local class variables.
        imageUrl = getArguments().getString("URL");
        imageFileName = getArguments().getString("Image_Name");
    }

    // The public interface is used to send information back to the parent activity.
    public interface DownloadImageListener {
        void onDownloadImage(AppCompatDialogFragment dialogFragment, String downloadUrl);
    }

    // `downloadImageListener` is used in `onAttach()` and `onCreateDialog()`.
    private DownloadImageListener downloadImageListener;

    // Check to make sure tha the parent activity implements the listener.
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            downloadImageListener = (DownloadImageListener) context;
        } catch (ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement DownloadImageListener.");
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use `AlertDialog.Builder` to create the `AlertDialog`.
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
                downloadImageListener.onDownloadImage(DownloadImageDialog.this, imageUrl);
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

        // Set the text for `downloadImageNameTextView`.
        EditText downloadImageNameTextView = (EditText) alertDialog.findViewById(R.id.download_image_name);
        downloadImageNameTextView.setText(imageFileName);

        // Allow the `enter` key on the keyboard to save the file from `downloadImageNameTextView`.
        downloadImageNameTextView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey (View v, int keyCode, KeyEvent event) {
                // If the event is an `ACTION_DOWN` on the `enter` key, initiate the download.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // trigger `onDownloadImage()` and return the `DialogFragment` and the URL to the parent activity.
                    downloadImageListener.onDownloadImage(DownloadImageDialog.this, imageUrl);
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