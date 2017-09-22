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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
// We have to use `AppCompatDialogFragment` instead of `DialogFragment` or an error is produced on API <=22.
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

import com.stoutner.privacybrowser.activities.BookmarksActivity;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

public class EditBookmarkDialog extends AppCompatDialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkListener {
        void onSaveEditBookmark(AppCompatDialogFragment dialogFragment);
    }

    // `editBookmarkListener` is used in `onAttach()` and `onCreateDialog()`
    private EditBookmarkListener editBookmarkListener;

    public void onAttach(Context context) {
        super.onAttach(context);

        // Get a handle for `EditBookmarkListener` from `context`.
        try {
            editBookmarkListener = (EditBookmarkListener) context;
        } catch(ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement EditBookmarkListener.");
        }
    }

    // Instantiate the class variables.
    EditText nameEditText;
    EditText urlEditText;
    RadioButton newIconRadioButton;
    Button editButton;
    String currentName;
    String currentUrl;

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a long array with the the databaseId of the selected bookmark and convert it to an `int`.
        long[] selectedBookmarkLongArray = BookmarksActivity.checkedItemIds;
        int selectedBookmarkDatabaseId = (int) selectedBookmarkLongArray[0];

        // Get a `Cursor` with the specified bookmark and move it to the first position.
        Cursor bookmarkCursor = BookmarksActivity.bookmarksDatabaseHelper.getBookmarkCursor(selectedBookmarkDatabaseId);
        bookmarkCursor.moveToFirst();

        // Use `AlertDialog.Builder` to create the `AlertDialog`.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.edit_bookmark);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_dialog, null));

        // Set an `onClick()` listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.  The `AlertDialog` will close automatically.
            }
        });

        // Set the `onClick()` listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.save, new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on save.
                editBookmarkListener.onSaveEditBookmark(EditBookmarkDialog.this);
            }
        });

        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `setSoftInputMode` might produce `java.lang.NullPointerException`.
        assert alertDialog.getWindow() != null;

        // Show the keyboard when `alertDialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The `AlertDialog` must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the layout items.
        RadioGroup iconRadioGroup = (RadioGroup) alertDialog.findViewById(R.id.edit_bookmark_icon_radiogroup);
        ImageView currentIconImageView = (ImageView) alertDialog.findViewById(R.id.edit_bookmark_current_icon);
        ImageView newFavoriteIconImageView = (ImageView) alertDialog.findViewById(R.id.edit_bookmark_web_page_favorite_icon);
        newIconRadioButton = (RadioButton) alertDialog.findViewById(R.id.edit_bookmark_web_page_favorite_icon_radiobutton);
        nameEditText = (EditText) alertDialog.findViewById(R.id.edit_bookmark_name_edittext);
        urlEditText = (EditText) alertDialog.findViewById(R.id.edit_bookmark_url_edittext);
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
        iconRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // Update the edit button.
                updateEditButton();
            }
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

        // Allow the `enter` key on the keyboard to save the bookmark from `edit_bookmark_name_edittext`.
        nameEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is an `ACTION_DOWN` on the `enter` key, save the bookmark.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                    // Trigger `onSaveEditBookmark()` and return the `DialogFragment` to the parent activity.
                    editBookmarkListener.onSaveEditBookmark(EditBookmarkDialog.this);
                    // Manually dismiss `alertDialog`.
                    alertDialog.dismiss();
                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                    return false;
                }
            }
        });

        // Allow the "enter" key on the keyboard to save the bookmark from `edit_bookmark_url_edittext`.
        urlEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the `enter` button, select the PositiveButton `Save`.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                    // Trigger `editBookmarkListener` and return the DialogFragment to the parent activity.
                    editBookmarkListener.onSaveEditBookmark(EditBookmarkDialog.this);
                    // Manually dismiss the `AlertDialog`.
                    alertDialog.dismiss();
                    // Consume the event.
                    return true;
                } else { // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                    return false;
                }
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

        editButton.setEnabled(iconChanged || nameChanged || urlChanged);
    }
}