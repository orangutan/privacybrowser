/*
 * Copyright © 2018 Soren Stoutner <soren@stoutner.com>.
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
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.helpers.AdHelper;

public class AdConsentDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and the icon according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
            dialogBuilder.setIcon(R.drawable.block_ads_enabled_dark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
            dialogBuilder.setIcon(R.drawable.block_ads_enabled_light);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.ad_consent);

        // Set the text.
        dialogBuilder.setMessage(R.string.ad_consent_text);

        // Get a handle for the consent information.
        ConsentInformation consentInformation = ConsentInformation.getInstance(getActivity().getApplicationContext());

        // Configure the close button.
        dialogBuilder.setNegativeButton(R.string.close_browser, (DialogInterface dialog, int which) -> {
            // Set the consent status to Unknown.
            consentInformation.setConsentStatus(ConsentStatus.UNKNOWN);

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
            // Set the consent status to Non-Personalized.
            consentInformation.setConsentStatus(ConsentStatus.NON_PERSONALIZED);

            // Indicate the user is under age, which disables personalized advertising and remarketing.  https://developers.google.com/admob/android/eu-consent
            consentInformation.setTagForUnderAgeOfConsent(true);

            // Load an ad.
            AdHelper.loadAd(getActivity().findViewById(R.id.adview), getActivity().getApplicationContext(), getString(R.string.ad_unit_id));
        });

        // Return the alert dialog.
        return dialogBuilder.create();
    }

    // Close Privacy Browser Free if the dialog is cancelled without selecting a button (by tapping on the background).
    @Override
    public void onCancel(DialogInterface dialogInterface) {
        // Set the consent status to Unknown.
        ConsentInformation.getInstance(getActivity().getApplicationContext()).setConsentStatus(ConsentStatus.UNKNOWN);

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