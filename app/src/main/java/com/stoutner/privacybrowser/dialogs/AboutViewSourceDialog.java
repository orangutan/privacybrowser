/*
 * Copyright © 2018-2019 Soren Stoutner <soren@stoutner.com>.
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.stoutner.privacybrowser.R;

public class AboutViewSourceDialog extends DialogFragment {
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and the icon according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
            dialogBuilder.setIcon(R.drawable.about_dark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
            dialogBuilder.setIcon(R.drawable.about_light);
        }

        // Set an `onClick` listener on the negative button.  Using `null` as the listener closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.close, null);

        // Set the title.
        dialogBuilder.setTitle(R.string.about_view_source);

        // Set the text.
        dialogBuilder.setMessage(R.string.about_view_source_message);

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}
