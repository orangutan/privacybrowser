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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.AdConsentDatabaseHelper;
import com.stoutner.privacybrowser.helpers.AdHelper;

public class AdConsentDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and the icon according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
            dialogBuilder.setIcon(R.drawable.block_ads_enabled_dark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
            dialogBuilder.setIcon(R.drawable.block_ads_enabled_light);
        }

        // Remove the incorrect lint warning below that `getApplicationContext()` might be null.
        assert getActivity() != null;

        // Initialize the bookmarks database helper.  The `0` specifies a database version, but that is ignored and set instead using a constant in `AdConsentDatabaseHelper`.
        // `getContext()` can be used instead of `getActivity.getApplicationContext()` when the minimum API >= 23.
        AdConsentDatabaseHelper adConsentDatabaseHelper = new AdConsentDatabaseHelper(getActivity().getApplicationContext(), null, null, 0);

        // Set the title.
        dialogBuilder.setTitle(R.string.ad_consent);

        // Set the text.
        dialogBuilder.setMessage(R.string.ad_consent_text);

        // Configure the close button.
        dialogBuilder.setNegativeButton(R.string.close_browser, (DialogInterface dialog, int which) -> {
            // Update the ad consent database.
            adConsentDatabaseHelper.updateAdConsent(false);

            // Close the browser.  `finishAndRemoveTask` also removes Privacy Browser from the recent app list.
            if (Build.VERSION.SDK_INT >= 21) {
                getActivity().finishAndRemoveTask();
            } else {
                getActivity().finish();
            }

            // Remove the terminated program from RAM.  The status code is `0`.
            System.exit(0);
        });

        // Configure the accept button.
        dialogBuilder.setPositiveButton(R.string.accept_ads, (DialogInterface dialog, int which) -> {
            // Update the ad consent database.
            adConsentDatabaseHelper.updateAdConsent(true);

            // Load an ad.  `getContext()` can be used instead of `getActivity.getApplicationContext()` on the minimum API >= 23.
            AdHelper.loadAd(getActivity().findViewById(R.id.adview), getActivity().getApplicationContext(), getString(R.string.ad_unit_id));
        });

        // Create an alert dialog from the alert dialog builder.
        AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Return the alert dialog.
        return alertDialog;
    }

    // Close Privacy Browser Free if the dialog is cancelled without selecting a button (by tapping on the background).
    @Override
    public void onCancel(DialogInterface dialogInterface) {
        // Remove the incorrect lint warning below that `getApplicationContext()` might be null.
        assert getActivity() != null;

        // Initialize the bookmarks database helper.  The `0` specifies a database version, but that is ignored and set instead using a constant in `AdConsentDatabaseHelper`.
        // `getContext()` can be used instead of `getActivity.getApplicationContext()` when the minimum API >= 23.
        AdConsentDatabaseHelper adConsentDatabaseHelper = new AdConsentDatabaseHelper(getActivity().getApplicationContext(), null, null, 0);

        // Update the ad consent database.
        adConsentDatabaseHelper.updateAdConsent(false);

        // Close the browser.  `finishAndRemoveTask` also removes Privacy Browser from the recent app list.
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().finishAndRemoveTask();
        } else {
            getActivity().finish();
        }

        // Remove the terminated program from RAM.  The status code is `0`.
        System.exit(0);
    }
}