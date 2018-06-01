/*
 * Copyright © 2015-2018 Soren Stoutner <soren@stoutner.com>.
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
import android.support.annotation.NonNull;
// We have to use `AppCompatDialogFragment` instead of `DialogFragment` or an error is produced on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.R;

public class CreateHomeScreenShortcutDialog extends AppCompatDialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface CreateHomeScreenShortcutListener {
        void onCreateHomeScreenShortcut(AppCompatDialogFragment dialogFragment);
    }

    //`createHomeScreenShortcutListener` is used in `onAttach()` and `onCreateDialog()`.
    private CreateHomeScreenShortcutListener createHomeScreenShortcutListener;

    // Check to make sure that the parent activity implements the listener.
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            createHomeScreenShortcutListener = (CreateHomeScreenShortcutListener) context;
        } catch(ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement CreateHomeScreenShortcutListener.");
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warning below that `getLayoutInflater()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

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
        dialogBuilder.setTitle(R.string.create_shortcut);
        dialogBuilder.setIcon(favoriteIconDrawable);

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.create_home_screen_shortcut_dialog, null));

        // Setup the negative button.  Using null closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.cancel, null);

        // Set an `onClick` listener on the positive button.
        dialogBuilder.setPositiveButton(R.string.create, (DialogInterface dialog, int which) -> createHomeScreenShortcutListener.onCreateHomeScreenShortcut(CreateHomeScreenShortcutDialog.this));

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when the Dialog is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // We need to show the alert dialog before we can call `setOnKeyListener()` below.
        alertDialog.show();

        // Get a handle for `shortcut_name_edittext`.
        EditText shortcutNameEditText = alertDialog.findViewById(R.id.shortcut_name_edittext);

        // Set the current `WebView` title as the text for `shortcutNameEditText`.
        shortcutNameEditText.setText(MainWebViewActivity.webViewTitle);

        // Allow the "enter" key on the keyboard to create the shortcut.
        shortcutNameEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the "enter" button, select the PositiveButton "Create".
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Trigger the create listener.
                createHomeScreenShortcutListener.onCreateHomeScreenShortcut(CreateHomeScreenShortcutDialog.this);

                // Manually dismiss `alertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}