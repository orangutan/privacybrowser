/*
 * Copyright © 2019 Soren Stoutner <soren@stoutner.com>.
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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.stoutner.privacybrowser.R;

public class FontSizeDialog extends DialogFragment {
    // Define the update font size listener.
    private UpdateFontSizeListener updateFontSizeListener;

    // The public interface is used to send information back to the parent activity.
    public interface UpdateFontSizeListener {
        void onApplyNewFontSize(DialogFragment dialogFragment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the update font size listener from the launching context.
        updateFontSizeListener = (UpdateFontSizeListener) context;
    }

    public static FontSizeDialog displayDialog(int fontSize) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the font size in the bundle.
        argumentsBundle.putInt("font_size", fontSize);

        // Create a new instance of the dialog.
        FontSizeDialog fontSizeDialog = new FontSizeDialog();

        // Add the bundle to the dialog.
        fontSizeDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return fontSizeDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using null as the parent view group when inflating the alert dialog.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the activity and the context.
        Activity activity = getActivity();
        Context context = getContext();

        // Remove the incorrect lint warnings below that the activity and context might be null.
        assert activity != null;
        assert context != null;

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning below that `getInt()` might be null.
        assert arguments != null;

        // Get the current font size.
        int currentFontSize = arguments.getInt("font_size");

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Set the style and icon according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogDark);
            dialogBuilder.setIcon(R.drawable.font_size_dark);
        } else {
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogLight);
            dialogBuilder.setIcon(R.drawable.font_size_light);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.font_size);

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(activity.getLayoutInflater().inflate(R.layout.font_size_dialog, null));

        // Set the close button listener.  Using `null` as the listener closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.close, null);

        // Set the apply button listener.
        dialogBuilder.setPositiveButton(R.string.apply, (DialogInterface dialog, int which) -> {
            // Return the dialog fragment to the parent activity.
            updateFontSizeListener.onApplyNewFontSize(this);
        });

        // Create an alert dialog from the builder.
        AlertDialog alertDialog = dialogBuilder.create();

        // Get the alert dialog window.
        Window dialogWindow = alertDialog.getWindow();

        // Remove the incorrect lint warning below that the dialog window might be null.
        assert dialogWindow != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Display the keyboard.
        dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get a handle for the font size edit text.
        EditText fontSizeEditText = alertDialog.findViewById(R.id.font_size_edittext);

        // Display the current font size.
        fontSizeEditText.setText(String.valueOf(currentFontSize));

        // Request focus on the font size edit text.
        fontSizeEditText.requestFocus();

        // Set the enter key on the keyboard to update the font size.
        fontSizeEditText.setOnKeyListener((View view, int keyCode, KeyEvent keyEvent) -> {
            // If the key event is a key-down on the `enter` key apply the new font size.
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {  // The enter key was pressed.
                // Trigger the update font size listener and return the dialog fragment to the parent activity.
                updateFontSizeListener.onApplyNewFontSize((this));

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                //Consume the event.
                return true;
            } else {  // If any other key was pressed do not consume the event.
                return false;
            }
        });

        // Return the alert dialog.
        return alertDialog;
    }
}
