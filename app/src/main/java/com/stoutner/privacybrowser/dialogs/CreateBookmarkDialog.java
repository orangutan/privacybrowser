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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;

import java.io.ByteArrayOutputStream;

public class CreateBookmarkDialog extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface CreateBookmarkListener {
        void onCreateBookmark(DialogFragment dialogFragment, Bitmap favoriteIconBitmap);
    }

    // The create bookmark listener is initialized in `onAttach()` and used in `onCreateDialog()`.
    private CreateBookmarkListener createBookmarkListener;

    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the create bookmark listener from the launching context.
        createBookmarkListener = (CreateBookmarkListener) context;
    }

    public static CreateBookmarkDialog createBookmark(String url, String title, Bitmap favoriteIconBitmap) {
        // Create a favorite icon byte array output stream.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

        // Convert the byte array output stream to a byte array.
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the variables in the bundle.
        argumentsBundle.putString("url", url);
        argumentsBundle.putString("title", title);
        argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray);

        // Create a new instance of the dialog.
        CreateBookmarkDialog createBookmarkDialog = new CreateBookmarkDialog();

        // Add the bundle to the dialog.
        createBookmarkDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return createBookmarkDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning below that the arguments might be null.
        assert arguments != null;

        // Get the strings from the arguments.
        String url = arguments.getString("url");
        String title = arguments.getString("title");

        // Get the favorite icon byte array.
        byte[] favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array");

        // Remove the incorrect lint warning below that the favorite icon byte array might be null.
        assert favoriteIconByteArray != null;

        // Convert the favorite icon byte array to a bitmap.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the theme and screenshot preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Create a drawable version of the favorite icon.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), favoriteIconBitmap);

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title and icon.
        dialogBuilder.setTitle(R.string.create_bookmark);
        dialogBuilder.setIcon(favoriteIconDrawable);

        // Remove the warning below that `getLayoutInflater()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is `null` because it will be assigned by the `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.create_bookmark_dialog, null));

        // Set an `onClick()` listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The `AlertDialog` will close automatically.
        });

        // Set an `onClick()` listener for the positive button.
        dialogBuilder.setPositiveButton(R.string.create, (DialogInterface dialog, int which) -> {
            // Return the `DialogFragment` to the parent activity.
            createBookmarkListener.onCreateBookmark(this, favoriteIconBitmap);
        });

        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when the `AlertDialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The alert dialog needs to be shown before `setOnKeyListener()` can be called.
        alertDialog.show();

        // Get a handle for `create_bookmark_name_edittext`.
        EditText createBookmarkNameEditText = alertDialog.findViewById(R.id.create_bookmark_name_edittext);

        // Set the current `WebView` title as the text for `create_bookmark_name_edittext`.
        createBookmarkNameEditText.setText(title);

        // Allow the `enter` key on the keyboard to create the bookmark from the create bookmark name edittext`.
        createBookmarkNameEditText.setOnKeyListener((View view, int keyCode, KeyEvent keyEvent) -> {
            // If the event is a key-down on the `enter` key, select the create button.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger the create bookmark listener and return the dialog fragment and the favorite icon bitmap to the parent activity.
                createBookmarkListener.onCreateBookmark(this, favoriteIconBitmap);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // Some other key was pressed.
                // Do not consume the event.
                return false;
            }
        });

        // Set the formatted URL string as the initial text of the create bookmark URL edit text.
        EditText createBookmarkUrlEditText = alertDialog.findViewById(R.id.create_bookmark_url_edittext);
        createBookmarkUrlEditText.setText(url);

        // Allow the enter key on the keyboard to create the bookmark from create bookmark URL edit text.
        createBookmarkUrlEditText.setOnKeyListener((View v, int keyCode, KeyEvent keyEvent) -> {
            // If the event is a key-down on the `enter` key, select the create button.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (keyEvent.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger the create bookmark listener and return the dialog fragment and the favorite icon bitmap to the parent activity.
                createBookmarkListener.onCreateBookmark(this, favoriteIconBitmap);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else { // Some other key was pressed.
                // Do not consume the event.
                return false;
            }
        });

        // Return the alert dialog.
        return alertDialog;
    }
}