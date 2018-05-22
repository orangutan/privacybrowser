/*
 * Copyright © 2016-2018 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser.helpers;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.AdConsentDialog;

public class AdHelper {
    private static boolean initialized;

    public static void initializeAds (View view, Context applicationContext, FragmentManager fragmentManager, String adId) {
        if (!initialized) {  // This is the first run.
            // Initialize mobile ads.
            MobileAds.initialize(applicationContext, adId);

            // Store the publisher ID in a string array.
            String[] publisherIds = {"pub-5962503714887045"};

            // Check to see if consent is needed in Europe to comply with the GDPR.
            ConsentInformation consentInformation = ConsentInformation.getInstance(applicationContext);
            consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
                @Override
                public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                    if (consentStatus == ConsentStatus.UNKNOWN) {  // The user has not yet consented to ads.
                        // Display the ad consent dialog.
                        DialogFragment adConsentDialogFragment = new AdConsentDialog();
                        adConsentDialogFragment.show(fragmentManager, "Ad Consent");
                    } else {  // The user has consented to ads.
                        // Indicate the user is under age, which disables personalized advertising and remarketing.  https://developers.google.com/admob/android/eu-consent
                        consentInformation.setTagForUnderAgeOfConsent(true);

                        // Load an ad.
                        loadAd(view, applicationContext, adId);
                    }
                }

                @Override
                public void onFailedToUpdateConsentInfo(String reason) {  // The user is not in Europe.
                    // Indicate the user is under age, which disables personalized advertising and remarketing.  https://developers.google.com/admob/android/eu-consent
                    consentInformation.setTagForUnderAgeOfConsent(true);

                    // Load an ad.
                    loadAd(view, applicationContext, adId);
                }
            });

            // Set the initialized variable to true so this section doesn't run again.
            initialized = true;
        } else {  // Ads have previously been initialized.
            // Load an ad.
            loadAd(view, applicationContext, adId);
        }
    }

    public static void loadAd (View view, Context applicationContext, String adId) {
        // Cast the generic view to an AdView.
        AdView adView = (AdView) view;

        // Save the layout parameters.  They are used when programatically recreating the add below.
        RelativeLayout.LayoutParams adViewLayoutParameters = (RelativeLayout.LayoutParams) adView.getLayoutParams();

        // Remove the AdView.
        RelativeLayout adViewParentLayout = (RelativeLayout) adView.getParent();
        adViewParentLayout.removeView(adView);

        // Setup the new AdView.  This is necessary because the size of the banner ad can change on rotate.
        adView = new AdView(applicationContext);
        adView.setAdSize(AdSize.SMART_BANNER);
        adView.setAdUnitId(adId);
        adView.setId(R.id.adview);
        adView.setLayoutParams(adViewLayoutParameters);

        // Display the new AdView.
        adViewParentLayout.addView(adView);

        // Only request non-personalized ads.
        Bundle adSettingsBundle = new Bundle();
        adSettingsBundle.putString("npa", "1");

        // Request a new ad.
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, adSettingsBundle).build();
        adView.loadAd(adRequest);
    }

    public static void hideAd(View view) {
        // Cast the generic view to an AdView.
        AdView adView = (AdView) view;

        // Hide the ad.
        adView.setVisibility(View.GONE);
    }

    public static void pauseAd(View view) {
        // Cast The generic view to an AdView.
        AdView adView = (AdView) view;

        // Pause the AdView.
        adView.pause();
    }

    public static void resumeAd(View view) {
        // Cast the generic view to an AdView.
        AdView adView = (AdView) view;

        // Resume the AdView.
        adView.resume();
    }
}