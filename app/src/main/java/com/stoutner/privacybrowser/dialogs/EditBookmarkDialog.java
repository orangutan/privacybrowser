/*
 * Copyright © 2016-2018 Soren Stoutner <soren@stoutner.com>.
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
// `AppCompatDialogFragment` is required instead of `DialogFragment` or an error is produced on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
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

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

public class EditBookmarkDialog extends AppCompatDialogFragment {
    // Instantiate the class variables.
    private EditBookmarkListener editBookmarkListener;
    private EditText nameEditText;
    private EditText urlEditText;
    private RadioButton newIconRadioButton;
    private Button editButton;
    private String currentName;
    private String currentUrl;

    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkListener {
        void onSaveBookmark(AppCompatDialogFragment dialogFragment, int selectedBookmarkDatabaseId);
    }

    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `EditBookmarkListener` from the launching context.
        editBookmarkListener = (EditBookmarkListener) context;
    }

    // Store the database ID in the arguments bundle.
    public static EditBookmarkDialog bookmarkDatabaseId(int databaseId) {
        // Create a bundle.
        Bundle bundle = new Bundle();

        // Store the bookmark database ID in the bundle.
        bundle.putInt("Database ID", databaseId);

        // Add the bundle to the dialog.
        EditBookmarkDialog editBookmarkDialog = new EditBookmarkDialog();
        editBookmarkDialog.setArguments(bundle);

        // Return the new dialog.
        return editBookmarkDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warning that `getInt()` might be null.
        assert getArguments() != null;

        // Store the bookmark database ID in the class variable.
        int selectedBookmarkDatabaseId = getArguments().getInt("Database ID");

        // Initialize the database helper.  The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        BookmarksDatabaseHelper bookmarksDatabaseHelper = new BookmarksDatabaseHelper(getContext(), null, null, 0);

        // Get a `Cursor` with the selected bookmark and move it to the first position.
        Cursor bookmarkCursor = bookmarksDatabaseHelper.getBookmarkCursor(selectedBookmarkDatabaseId);
        bookmarkCursor.moveToFirst();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.edit_bookmark);

        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_dialog, null));

        // Set the listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The `AlertDialog` will close automatically.
        });

        // Set the listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
            // Return the `DialogFragment` to the parent activity on save.
            editBookmarkListener.onSaveBookmark(EditBookmarkDialog.this, selectedBookmarkDatabaseId);
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when the alert dialog is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the layout items.
        RadioGroup iconRadioGroup = alertDialog.findViewById(R.id.edit_bookmark_icon_radiogroup);
        ImageView currentIconImageView = alertDialog.findViewById(R.id.edit_bookmark_current_icon);
        ImageView newFavoriteIconImageView = alertDialog.findViewById(R.id.edit_bookmark_webpage_favorite_icon);
        newIconRadioButton = alertDialog.findViewById(R.id.edit_bookmark_webpage_favorite_icon_radiobutton);
        nameEditText = alertDialog.findViewById(R.id.edit_bookmark_name_edittext);
        urlEditText = alertDialog.findViewById(R.id.edit_bookmark_url_edittext);
        editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Get the current favorite icon byte array from the `Cursor`.
        byte[] currentIconByteArray = bookmarkCursor.getBlob(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

        // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
        Bitmap currentIconBitmap = BitmapFactory.decodeByteArray(currentIconByteArray, 0, currentIconByteArray.length);

        // Display `currentIconBitmap` in `edit_bookmark_current_icon`.
        currentIconImageView.setImageBitmap(currentIconBitmap);

        // Get a `Bitmap` of the favorite icon from `MainWebViewActivity` and display it in `edit_bookmark_web_page_favorite_icon`.
        newFavoriteIconImageView.setImageBitmap(MainWebViewActivity.favoriteIconBitmap);

        // Store the current bookmark name and URL.
        currentName = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
        currentUrl = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL));

        // Populate the `EditTexts`.
        nameEditText.setText(currentName);
        urlEditText.setText(currentUrl);

        // Initially disable the edit button.
        editButton.setEnabled(false);

        // Update the edit button if the icon selection changes.
        iconRadioGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
            // Update the edit button.
            updateEditButton();
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
                updateEditButton();
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
                updateEditButton();
            }
        });

        // Allow the `enter` key on the keyboard to save the bookmark from the bookmark name `EditText`.
        nameEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the `DialogFragment` to the parent activity.
                editBookmarkListener.onSaveBookmark(EditBookmarkDialog.this, selectedBookmarkDatabaseId);

                // Manually dismiss `alertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // Allow the "enter" key on the keyboard to save the bookmark from the URL `EditText`.
        urlEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the DialogFragment to the parent activity.
                editBookmarkListener.onSaveBookmark(EditBookmarkDialog.this, selectedBookmarkDatabaseId);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else { // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }

    private void updateEditButton() {
        // Get the text from the `EditTexts`.
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