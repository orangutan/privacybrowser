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

public class EditBookmarkFolderDialog extends AppCompatDialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkFolderListener {
        void onEditBookmarkFolder(AppCompatDialogFragment dialogFragment);
    }

    // `editFolderListener` is used in `onAttach()` and `onCreateDialog`.
    private EditBookmarkFolderListener editBookmarkFolderListener;

    public void onAttach(Context context) {
        super.onAttach(context);

        // Get a handle for `EditFolderListener` from `parentActivity`.
        try {
            editBookmarkFolderListener = (EditBookmarkFolderListener) context;
        } catch(ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement EditBookmarkFolderListener.");
        }
    }

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
        dialogBuilder.setTitle(R.string.edit_folder);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_folder_dialog, null));

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
                editBookmarkFolderListener.onEditBookmarkFolder(EditBookmarkFolderDialog.this);
            }
        });


        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `setSoftInputMode` might produce `java.lang.NullPointerException`.
        assert alertDialog.getWindow() != null;

        // Show the keyboard when the `Dialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The `AlertDialog` must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for layout items in the `AlertDialog`.
        final Button editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        final RadioButton currentIconRadioButton = (RadioButton) alertDialog.findViewById(R.id.edit_folder_current_icon_radiobutton);
        RadioGroup iconRadioGroup = (RadioGroup) alertDialog.findViewById(R.id.edit_folder_icon_radio_group);

        // Initially disable the edit button.
        editButton.setEnabled(false);

        // Get the current favorite icon byte array from the `Cursor`.
        byte[] currentIconByteArray = bookmarkCursor.getBlob(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));
        // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
        Bitmap currentIconBitmap = BitmapFactory.decodeByteArray(currentIconByteArray, 0, currentIconByteArray.length);
        // Display `currentIconBitmap` in `edit_folder_current_icon`.
        ImageView currentIconImageView = (ImageView) alertDialog.findViewById(R.id.edit_folder_current_icon);
        currentIconImageView.setImageBitmap(currentIconBitmap);

        // Get a `Bitmap` of the favorite icon from `MainWebViewActivity` and display it in `edit_folder_web_page_favorite_icon`.
        ImageView webPageFavoriteIconImageView = (ImageView) alertDialog.findViewById(R.id.edit_folder_web_page_favorite_icon);
        webPageFavoriteIconImageView.setImageBitmap(MainWebViewActivity.favoriteIconBitmap);

        // Get the current folder name.
        final String currentFolderName = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

        // Display the current folder name in `edit_folder_name_edittext`.
        final EditText folderNameEditText = (EditText) alertDialog.findViewById(R.id.edit_folder_name_edittext);
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
                Cursor folderExistsCursor = BookmarksActivity.bookmarksDatabaseHelper.getFolderCursor(newFolderName);

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
            }
        });

        // Update the status of the edit button when the icon is changed.
        iconRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // Get the new folder name.
                String newFolderName = folderNameEditText.getText().toString();

                // Get a cursor for the new folder name if it exists.
                Cursor folderExistsCursor = BookmarksActivity.bookmarksDatabaseHelper.getFolderCursor(newFolderName);

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
            }
        });

        // Allow the `enter` key on the keyboard to save the bookmark from `edit_bookmark_name_edittext`.
        folderNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the "enter" button, select the PositiveButton `Save`.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                    // Trigger `editBookmarkListener` and return the DialogFragment to the parent activity.
                    editBookmarkFolderListener.onEditBookmarkFolder(EditBookmarkFolderDialog.this);
                    // Manually dismiss the `AlertDialog`.
                    alertDialog.dismiss();
                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                    return false;
                }
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}