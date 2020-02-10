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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

import java.io.ByteArrayOutputStream;

public class CreateBookmarkFolderDialog extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface CreateBookmarkFolderListener {
        void onCreateBookmarkFolder(DialogFragment dialogFragment, Bitmap favoriteIconBitmap);
    }

    // `createBookmarkFolderListener` is used in `onAttach()` and `onCreateDialog`.
    private CreateBookmarkFolderListener createBookmarkFolderListener;

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Get a handle for `createBookmarkFolderListener` from the launching context.
        createBookmarkFolderListener = (CreateBookmarkFolderListener) context;
    }

    public static CreateBookmarkFolderDialog createBookmarkFolder(Bitmap favoriteIconBitmap) {
        // Create a favorite icon byte array output stream.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

        // Convert the byte array output stream to a byte array.
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the favorite icon in the bundle.
        argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray);

        // Create a new instance of the dialog.
        CreateBookmarkFolderDialog createBookmarkFolderDialog = new CreateBookmarkFolderDialog();

        // Add the bundle to the dialog.
        createBookmarkFolderDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return createBookmarkFolderDialog;
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

        // Get the favorite icon byte array.
        byte[] favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array");

        // Remove the incorrect lint warning below that the favorite icon byte array might be null.
        assert favoriteIconByteArray != null;

        // Convert the favorite icon byte array to a bitmap.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

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
        dialogBuilder.setTitle(R.string.create_folder);

        // Remove the warning below that `getLayoutInflater()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.create_bookmark_folder_dialog, null));

        // Set an `onClick()` listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The `AlertDialog` will close automatically.
        });

        // Set an `onClick()` listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.create, (DialogInterface dialog, int which) -> {
            // Return the `DialogFragment` to the parent activity on create.
            createBookmarkFolderListener.onCreateBookmarkFolder(this, favoriteIconBitmap);
        });


        // Create an alert dialog from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Get the alert dialog window.
        Window dialogWindow = alertDialog.getWindow();

        // Remove the incorrect lint warning below that the dialog window might be null.
        assert dialogWindow != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Display the keyboard.
        dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // The alert dialog must be shown before items in the alert dialog can be modified.
        alertDialog.show();

        // Get handles for the views in the dialog.
        final Button createButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        EditText folderNameEditText = alertDialog.findViewById(R.id.create_folder_name_edittext);
        ImageView webPageIconImageView = alertDialog.findViewById(R.id.create_folder_web_page_icon);

        // Initially disable the create button.
        createButton.setEnabled(false);

        // Initialize the database helper.  The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        final BookmarksDatabaseHelper bookmarksDatabaseHelper = new BookmarksDatabaseHelper(getContext(), null, null, 0);

        // Enable the create button if the new folder name is unique.
        folderNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Convert the current text to a string.
                String folderName = s.toString();

                // Check if a folder with the name already exists.
                Cursor folderExistsCursor = bookmarksDatabaseHelper.getFolder(folderName);

                // Enable the create button if the new folder name is not empty and doesn't already exist.
                createButton.setEnabled(!folderName.isEmpty() && (folderExistsCursor.getCount() == 0));
            }
        });

        // Set the enter key on the keyboard to create the folder from the edit text.
        folderNameEditText.setOnKeyListener((View view, int keyCode, KeyEvent keyEvent) -> {
            // If the key event is a key-down on the `enter` key create the bookmark folder.
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && createButton.isEnabled()) {  // The enter key was pressed and the create button is enabled.
                // Trigger the create bookmark folder listener and return the dialog fragment to the parent activity.
                createBookmarkFolderListener.onCreateBookmarkFolder(this, favoriteIconBitmap);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, or if the create button is currently disabled, do not consume the event.
                return false;
            }
        });

        // Display the current favorite icon.
        webPageIconImageView.setImageBitmap(favoriteIconBitmap);

        // Return the alert dialog.
        return alertDialog;
    }
}