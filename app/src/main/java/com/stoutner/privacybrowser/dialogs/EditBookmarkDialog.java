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

public class EditBookmarkDialog extends DialogFragment {
    // Define the edit bookmark listener.
    private EditBookmarkListener editBookmarkListener;

    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkListener {
        void onSaveBookmark(DialogFragment dialogFragment, int selectedBookmarkDatabaseId, Bitmap favoriteIconBitmap);
    }

    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `EditBookmarkListener` from the launching context.
        editBookmarkListener = (EditBookmarkListener) context;
    }

    // Store the database ID in the arguments bundle.
    public static EditBookmarkDialog bookmarkDatabaseId(int databaseId, Bitmap favoriteIconBitmap) {
        // Create a favorite icon byte array output stream.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

        // Convert the byte array output stream to a byte array.
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the variables in the bundle.
        argumentsBundle.putInt("database_id", databaseId);
        argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray);

        // Create a new instance of the dialog.
        EditBookmarkDialog editBookmarkDialog = new EditBookmarkDialog();

        // Add the arguments bundle to the dialog.
        editBookmarkDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return editBookmarkDialog;
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

        // Store the bookmark database ID in the class variable.
        int selectedBookmarkDatabaseId = arguments.getInt("database_id");

        // Get the favorite icon byte array.
        byte[] favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array");

        // Remove the incorrect lint warning below that the favorite icon byte array might be null.
        assert favoriteIconByteArray != null;

        // Convert the favorite icon byte array to a bitmap.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

        // Initialize the database helper.  The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        BookmarksDatabaseHelper bookmarksDatabaseHelper = new BookmarksDatabaseHelper(getContext(), null, null, 0);

        // Get a `Cursor` with the selected bookmark and move it to the first position.
        Cursor bookmarkCursor = bookmarksDatabaseHelper.getBookmark(selectedBookmarkDatabaseId);
        bookmarkCursor.moveToFirst();

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
        dialogBuilder.setTitle(R.string.edit_bookmark);

        // Remove the incorrect lint warning that `getActivity().getLayoutInflater()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_dialog, null));

        // Set the cancel button listener.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The alert dialog will close automatically.
        });

        // Set the save button listener.
        dialogBuilder.setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
            // Return the dialog fragment to the parent activity.
            editBookmarkListener.onSaveBookmark(this, selectedBookmarkDatabaseId, favoriteIconBitmap);
        });

        // Create an alert dialog from the builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // remove the incorrect lint warning below that `getWindow().addFlags()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the layout items.
        RadioGroup iconRadioGroup = alertDialog.findViewById(R.id.edit_bookmark_icon_radiogroup);
        ImageView currentIconImageView = alertDialog.findViewById(R.id.edit_bookmark_current_icon);
        ImageView newFavoriteIconImageView = alertDialog.findViewById(R.id.edit_bookmark_webpage_favorite_icon);
        EditText nameEditText = alertDialog.findViewById(R.id.edit_bookmark_name_edittext);
        EditText urlEditText = alertDialog.findViewById(R.id.edit_bookmark_url_edittext);
        Button editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Get the current favorite icon byte array from the cursor.
        byte[] currentIconByteArray = bookmarkCursor.getBlob(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

        // Convert the byte array to a bitmap beginning at the first byte and ending at the last.
        Bitmap currentIconBitmap = BitmapFactory.decodeByteArray(currentIconByteArray, 0, currentIconByteArray.length);

        // Display the current icon bitmap.
        currentIconImageView.setImageBitmap(currentIconBitmap);

        // Set the new favorite icon bitmap.
        newFavoriteIconImageView.setImageBitmap(favoriteIconBitmap);

        // Store the current bookmark name and URL.
        String currentName = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
        String currentUrl = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL));

        // Populate the edit texts.
        nameEditText.setText(currentName);
        urlEditText.setText(currentUrl);

        // Initially disable the edit button.
        editButton.setEnabled(false);

        // Update the edit button if the icon selection changes.
        iconRadioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            // Update the edit button.
            updateEditButton(alertDialog, currentName, currentUrl);
        });

        // Update the edit button if the bookmark name changes.
        nameEditText.addTextChangedListener(new TextWatcher() {
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
                // Update the edit button.
                updateEditButton(alertDialog, currentName, currentUrl);
            }
        });

        // Update the edit button if the URL changes.
        urlEditText.addTextChangedListener(new TextWatcher() {
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
                // Update the edit button.
                updateEditButton(alertDialog, currentName, currentUrl);
            }
        });

        // Allow the enter key on the keyboard to save the bookmark from the bookmark name edit text.
        nameEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the `DialogFragment` to the parent activity.
                editBookmarkListener.onSaveBookmark(this, selectedBookmarkDatabaseId, favoriteIconBitmap);

                // Manually dismiss `alertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // Allow the enter key on the keyboard to save the bookmark from the URL edit text.
        urlEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the DialogFragment to the parent activity.
                editBookmarkListener.onSaveBookmark(this, selectedBookmarkDatabaseId, favoriteIconBitmap);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else { // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // Return the alert dialog.
        return alertDialog;
    }

    private void updateEditButton(AlertDialog alertdialog, String currentName, String currentUrl) {
        // Get handles for the views.
        EditText nameEditText = alertdialog.findViewById(R.id.edit_bookmark_name_edittext);
        EditText urlEditText = alertdialog.findViewById(R.id.edit_bookmark_url_edittext);
        RadioButton newIconRadioButton = alertdialog.findViewById(R.id.edit_bookmark_webpage_favorite_icon_radiobutton);
        Button editButton = alertdialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Get the text from the edit texts.
        String newName = nameEditText.getText().toString();
        String newUrl = urlEditText.getText().toString();

        // Has the favorite icon changed?
        boolean iconChanged = newIconRadioButton.isChecked();

        // Has the name changed?
        boolean nameChanged = !newName.equals(currentName);

        // Has the URL changed?
        boolean urlChanged = !newUrl.equals(currentUrl);

        // Update the enabled status of the edit button.
        editButton.setEnabled(iconChanged || nameChanged || urlChanged);
    }
}