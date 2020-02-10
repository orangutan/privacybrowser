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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

import java.io.ByteArrayOutputStream;

public class EditBookmarkFolderDialog extends DialogFragment {
    // Instantiate the class variable.
    private EditBookmarkFolderListener editBookmarkFolderListener;

    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkFolderListener {
        void onSaveBookmarkFolder(DialogFragment dialogFragment, int selectedFolderDatabaseId, Bitmap favoriteIconBitmap);
    }

    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `EditFolderListener` from the launching context.
        editBookmarkFolderListener = (EditBookmarkFolderListener) context;
    }

    // Store the database ID in the arguments bundle.
    public static EditBookmarkFolderDialog folderDatabaseId(int databaseId, Bitmap favoriteIconBitmap) {
        // Create a favorite icon byte array output stream.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

        // Convert the byte array output stream to a byte array.
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Create an arguments bundle
        Bundle argumentsBundle = new Bundle();

        // Store the variables in the bundle.
        argumentsBundle.putInt("database_id", databaseId);
        argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray);

        // Create a new instance of the dialog.
        EditBookmarkFolderDialog editBookmarkFolderDialog = new EditBookmarkFolderDialog();

        // Add the arguments bundle to the dialog.
        editBookmarkFolderDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return editBookmarkFolderDialog;
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

        // Store the folder database ID in the class variable.
        int selectedFolderDatabaseId = arguments.getInt("database_id");

        // Get the favorite icon byte array.
        byte[] favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array");

        // Remove the incorrect lint warning below that the favorite icon byte array might be null.
        assert favoriteIconByteArray != null;

        // Convert the favorite icon byte array to a bitmap.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

        // Initialize the database helper.  The two `nulls` do not specify the database name or a `CursorFactory`.  The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        BookmarksDatabaseHelper bookmarksDatabaseHelper = new BookmarksDatabaseHelper(getContext(), null, null, 0);

        // Get a cursor with the selected folder and move it to the first position.
        Cursor folderCursor = bookmarksDatabaseHelper.getBookmark(selectedFolderDatabaseId);
        folderCursor.moveToFirst();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Set the style according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.edit_folder);

        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_folder_dialog, null));

        // Set the listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The `AlertDialog` will close automatically.
        });

        // Set the listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
            // Return the `DialogFragment` to the parent activity on save.
            editBookmarkFolderListener.onSaveBookmarkFolder(this, selectedFolderDatabaseId, favoriteIconBitmap);
        });

        // Create an alert dialog from the alert dialog builder.
        AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the views in the alert dialog.
        RadioGroup iconRadioGroup = alertDialog.findViewById(R.id.edit_folder_icon_radio_group);
        RadioButton currentIconRadioButton = alertDialog.findViewById(R.id.edit_folder_current_icon_radiobutton);
        ImageView currentIconImageView = alertDialog.findViewById(R.id.edit_folder_current_icon_imageview);
        ImageView webPageFavoriteIconImageView = alertDialog.findViewById(R.id.edit_folder_web_page_favorite_icon_imageview);
        EditText folderNameEditText = alertDialog.findViewById(R.id.edit_folder_name_edittext);
        Button editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Initially disable the edit button.
        editButton.setEnabled(false);

        // Get the current favorite icon byte array from the Cursor.
        byte[] currentIconByteArray = folderCursor.getBlob(folderCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

        // Convert the byte array to a bitmap beginning at the first byte and ending at the last.
        Bitmap currentIconBitmap = BitmapFactory.decodeByteArray(currentIconByteArray, 0, currentIconByteArray.length);

        // Display the current icon bitmap.
        currentIconImageView.setImageBitmap(currentIconBitmap);

        // Set the new favorite icon bitmap.
        webPageFavoriteIconImageView.setImageBitmap(favoriteIconBitmap);

        // Get the current folder name.
        String currentFolderName = folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

        // Display the current folder name in `edit_folder_name_edittext`.
        folderNameEditText.setText(currentFolderName);

        // Update the status of the edit button when the folder name is changed.
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
                String newFolderName = s.toString();

                // Get a cursor for the new folder name if it exists.
                Cursor folderExistsCursor = bookmarksDatabaseHelper.getFolder(newFolderName);

                // Is the new folder name empty?
                boolean folderNameNotEmpty = !newFolderName.isEmpty();

                // Does the folder name already exist?
                boolean folderNameAlreadyExists = (!newFolderName.equals(currentFolderName) && (folderExistsCursor.getCount() > 0));

                // Has the folder been renamed?
                boolean folderRenamed = (!newFolderName.equals(currentFolderName) && !folderNameAlreadyExists);

                // Has the favorite icon changed?
                boolean iconChanged = (!currentIconRadioButton.isChecked() && !folderNameAlreadyExists);

                // Enable the create button if something has been edited and the new folder name is valid.
                editButton.setEnabled(folderNameNotEmpty && (folderRenamed || iconChanged));
            }
        });

        // Update the status of the edit button when the icon is changed.
        iconRadioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            // Get the new folder name.
            String newFolderName = folderNameEditText.getText().toString();

            // Get a cursor for the new folder name if it exists.
            Cursor folderExistsCursor = bookmarksDatabaseHelper.getFolder(newFolderName);

            // Is the new folder name empty?
            boolean folderNameEmpty = newFolderName.isEmpty();

            // Does the folder name already exist?
            boolean folderNameAlreadyExists = (!newFolderName.equals(currentFolderName) && (folderExistsCursor.getCount() > 0));

            // Has the folder been renamed?
            boolean folderRenamed = (!newFolderName.equals(currentFolderName) && !folderNameAlreadyExists);

            // Has the favorite icon changed?
            boolean iconChanged = (!currentIconRadioButton.isChecked() && !folderNameAlreadyExists);

            // Enable the create button if something has been edited and the new folder name is valid.
            editButton.setEnabled(!folderNameEmpty && (folderRenamed || iconChanged));
        });

        // Allow the `enter` key on the keyboard to save the bookmark from `edit_bookmark_name_edittext`.
        folderNameEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the "enter" button, select the PositiveButton `Save`.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger `editBookmarkListener` and return the DialogFragment to the parent activity.
                editBookmarkFolderListener.onSaveBookmarkFolder(this, selectedFolderDatabaseId, favoriteIconBitmap);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}