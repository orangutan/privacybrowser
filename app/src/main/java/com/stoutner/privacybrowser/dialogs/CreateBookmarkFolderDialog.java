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
import android.os.Bundle;
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

import com.stoutner.privacybrowser.activities.BookmarksActivity;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.R;

public class CreateBookmarkFolderDialog extends AppCompatDialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface CreateBookmarkFolderListener {
        void onCreateBookmarkFolder(AppCompatDialogFragment dialogFragment);
    }

    // `createBookmarkFolderListener` is used in `onAttach()` and `onCreateDialog`.
    private CreateBookmarkFolderListener createBookmarkFolderListener;

    public void onAttach(Context context) {
        super.onAttach(context);

        // Get a handle for `createBookmarkFolderListener` from `context`.
        try {
            createBookmarkFolderListener = (CreateBookmarkFolderListener) context;
        } catch(ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement CreateBookmarkFolderListener.");
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use `AlertDialog.Builder` to create the `AlertDialog`.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.create_folder);

        // Set the view.  The parent view is `null` because it will be assigned by the `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.create_bookmark_folder_dialog, null));

        // Set an `onClick()` listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.  The `AlertDialog` will close automatically.
            }
        });

        // Set an `onClick()` listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on create.
                createBookmarkFolderListener.onCreateBookmarkFolder(CreateBookmarkFolderDialog.this);
            }
        });


        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `setSoftInputMode` might produce `java.lang.NullPointerException`.
        assert alertDialog.getWindow() != null;

        // Show the keyboard when the `Dialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The `AlertDialog` must be shown before items in the alert dialog can be modified.
        alertDialog.show();

        // Get a handle for the create button.
        final Button createButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        EditText folderNameEditText = (EditText) alertDialog.findViewById(R.id.create_folder_name_edittext);

        // Initially disable the create button.
        createButton.setEnabled(false);

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
                Cursor folderExistsCursor = BookmarksActivity.bookmarksDatabaseHelper.getFolderCursor(folderName);

                // Enable the create button if the new folder name is not empty and doesn't already exist.
                createButton.setEnabled(!folderName.isEmpty() && (folderExistsCursor.getCount() == 0));
            }
        });

        // Allow the `enter` key on the keyboard to create the folder from `create_folder_name_edittext`.
        folderNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the `enter` key, select the `PositiveButton` `Create`.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && createButton.isEnabled()) {  // The enter key was pressed and the create button is enabled.
                    // Trigger `createBookmarkFolderListener` and return the `DialogFragment` to the parent activity.
                    createBookmarkFolderListener.onCreateBookmarkFolder(CreateBookmarkFolderDialog.this);
                    // Manually dismiss the `AlertDialog`.
                    alertDialog.dismiss();
                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed, or if the create button is currently disabled, do not consume the event.
                    return false;
                }
            }
        });

        // Display the current favorite icon.
        ImageView webPageIconImageView = (ImageView) alertDialog.findViewById(R.id.create_folder_web_page_icon);
        webPageIconImageView.setImageBitmap(MainWebViewActivity.favoriteIconBitmap);

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}