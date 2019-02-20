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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.R;

public class CreateBookmarkDialog extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface CreateBookmarkListener {
        void onCreateBookmark(DialogFragment dialogFragment);
    }

    // `createBookmarkListener` is used in `onAttach()` and `onCreateDialog()`
    private CreateBookmarkListener createBookmarkListener;


    public void onAttach(Context context) {
        super.onAttach(context);

        // Get a handle for `CreateBookmarkListener` from the launching context.
        createBookmarkListener = (CreateBookmarkListener) context;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a drawable version of the favorite icon.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), MainWebViewActivity.favoriteIconBitmap);

        // Use `AlertDialog.Builder` to create the `AlertDialog`.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
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
            createBookmarkListener.onCreateBookmark(CreateBookmarkDialog.this);
        });

        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when the `AlertDialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The `AlertDialog` needs to be shown before `setOnKeyListener()` can be called.
        alertDialog.show();

        // Get a handle for `create_bookmark_name_edittext`.
        EditText createBookmarkNameEditText = alertDialog.findViewById(R.id.create_bookmark_name_edittext);

        // Set the current `WebView` title as the text for `create_bookmark_name_edittext`.
        createBookmarkNameEditText.setText(MainWebViewActivity.webViewTitle);

        // Allow the `enter` key on the keyboard to create the bookmark from `create_bookmark_name_edittext`.
        createBookmarkNameEditText.setOnKeyListener((View view, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the `enter` key, select the `PositiveButton` `Create`.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger `createBookmarkListener` and return the `DialogFragment` to the parent activity.
                createBookmarkListener.onCreateBookmark(CreateBookmarkDialog.this);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // Set the formattedUrlString as the initial text of `create_bookmark_url_edittext`.
        EditText createBookmarkUrlEditText = alertDialog.findViewById(R.id.create_bookmark_url_edittext);
        createBookmarkUrlEditText.setText(MainWebViewActivity.formattedUrlString);

        // Allow the `enter` key on the keyboard to create the bookmark from `create_bookmark_url_edittext`.
        createBookmarkUrlEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the "enter" key, select the PositiveButton "Create".
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger `createBookmarkListener` and return the DialogFragment to the parent activity.
                createBookmarkListener.onCreateBookmark(CreateBookmarkDialog.this);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else { // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}