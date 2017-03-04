/*
 * Copyright 2016-2017 Soren Stoutner <soren@stoutner.com>.
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
import android.os.Bundle;
import android.support.annotation.NonNull;
// We have to use `AppCompatDialogFragment` instead of `DialogFragment` or an error is produced on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.stoutner.privacybrowser.activities.MainWebView;
import com.stoutner.privacybrowser.R;

public class CreateBookmarkFolder extends AppCompatDialogFragment {
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
        // Use `AlertDialog.Builder` to create the `AlertDialog`.  The style formats the color of the button text.
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        dialogBuilder.setTitle(R.string.create_folder);
        // The parent view is `null` because it will be assigned by the `AlertDialog`.
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
                createBookmarkFolderListener.onCreateBookmarkFolder(CreateBookmarkFolder.this);
            }
        });


        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `setSoftInputMode` might produce `java.lang.NullPointerException`.
        assert alertDialog.getWindow() != null;

        // Show the keyboard when the `Dialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // We need to show the `AlertDialog` before we can call `setOnKeyListener()` below.
        alertDialog.show();

        // Allow the `enter` key on the keyboard to create the folder from `create_folder_name_edittext`.
        EditText createFolderNameEditText = (EditText) alertDialog.findViewById(R.id.create_folder_name_edittext);
        assert createFolderNameEditText != null;  // Remove the warning below that `createFolderNameEditText` might be `null`.
        createFolderNameEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down on the `enter` key, select the `PositiveButton` `Create`.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Trigger `createBookmarkFolderListener` and return the `DialogFragment` to the parent activity.
                    createBookmarkFolderListener.onCreateBookmarkFolder(CreateBookmarkFolder.this);
                    // Manually dismiss the `AlertDialog`.
                    alertDialog.dismiss();
                    // Consume the event.
                    return true;
                } else {  // If any other key was pressed do not consume the event.
                    return false;
                }
            }
        });

        // Display the current favorite icon.
        ImageView webPageIconImageView = (ImageView) alertDialog.findViewById(R.id.create_folder_web_page_icon);
        assert webPageIconImageView != null;  // Remove the warning that `webPageIconImageView` may be null.
        webPageIconImageView.setImageBitmap(MainWebView.favoriteIcon);

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}