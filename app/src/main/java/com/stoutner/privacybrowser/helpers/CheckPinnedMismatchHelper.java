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

package com.stoutner.privacybrowser.helpers;

import android.net.http.SslCertificate;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.stoutner.privacybrowser.dialogs.PinnedMismatchDialog;
import com.stoutner.privacybrowser.views.NestedScrollWebView;

import java.util.ArrayList;
import java.util.Date;

public class CheckPinnedMismatchHelper {
    public static void checkPinnedMismatch(FragmentManager fragmentManager, NestedScrollWebView nestedScrollWebView) {
        // Initialize the current SSL certificate variables.
        String currentWebsiteIssuedToCName = "";
        String currentWebsiteIssuedToOName = "";
        String currentWebsiteIssuedToUName = "";
        String currentWebsiteIssuedByCName = "";
        String currentWebsiteIssuedByOName = "";
        String currentWebsiteIssuedByUName = "";
        Date currentWebsiteSslStartDate = null;
        Date currentWebsiteSslEndDate = null;

        // Initialize the pinned SSL certificate variables.
        String pinnedSslIssuedToCName = "";
        String pinnedSslIssuedToOName = "";
        String pinnedSslIssuedToUName = "";
        String pinnedSslIssuedByCName = "";
        String pinnedSslIssuedByOName = "";
        String pinnedSslIssuedByUName = "";
        Date pinnedSslStartDate = null;
        Date pinnedSslEndDate = null;

        // Get the current website SSL certificate.
        SslCertificate currentWebsiteSslCertificate = nestedScrollWebView.getCertificate();

        // Extract the individual pieces of information from the current website SSL certificate if it is not null.
        if (currentWebsiteSslCertificate != null) {
            currentWebsiteIssuedToCName = currentWebsiteSslCertificate.getIssuedTo().getCName();
            currentWebsiteIssuedToOName = currentWebsiteSslCertificate.getIssuedTo().getOName();
            currentWebsiteIssuedToUName = currentWebsiteSslCertificate.getIssuedTo().getUName();
            currentWebsiteIssuedByCName = currentWebsiteSslCertificate.getIssuedBy().getCName();
            currentWebsiteIssuedByOName = currentWebsiteSslCertificate.getIssuedBy().getOName();
            currentWebsiteIssuedByUName = currentWebsiteSslCertificate.getIssuedBy().getUName();
            currentWebsiteSslStartDate = currentWebsiteSslCertificate.getValidNotBeforeDate();
            currentWebsiteSslEndDate = currentWebsiteSslCertificate.getValidNotAfterDate();
        }

        // Get the pinned SSL certificate information if it exists.
        if (nestedScrollWebView.hasPinnedSslCertificate()) {
            // Get the pinned SSL certificate.
            ArrayList<Object> pinnedSslCertificateArrayList = nestedScrollWebView.getPinnedSslCertificate();

            // Extract the arrays from the array list.
            String[] pinnedSslCertificateStringArray = (String[]) pinnedSslCertificateArrayList.get(0);
            Date[] pinnedSslCertificateDateArray = (Date[]) pinnedSslCertificateArrayList.get(1);

            // Populate the pinned SSL certificate string variables.
            pinnedSslIssuedToCName = pinnedSslCertificateStringArray[0];
            pinnedSslIssuedToOName = pinnedSslCertificateStringArray[1];
            pinnedSslIssuedToUName = pinnedSslCertificateStringArray[2];
            pinnedSslIssuedByCName = pinnedSslCertificateStringArray[3];
            pinnedSslIssuedByOName = pinnedSslCertificateStringArray[4];
            pinnedSslIssuedByUName = pinnedSslCertificateStringArray[5];

            // Populate the pinned SSL certificate date variables.
            pinnedSslStartDate = pinnedSslCertificateDateArray[0];
            pinnedSslEndDate = pinnedSslCertificateDateArray[1];
        }

        // Initialize string variables to store the SSL certificate dates.  Strings are needed to compare the values below, which doesn't work with dates if the first one is null.
        String currentWebsiteSslStartDateString = "";
        String currentWebsiteSslEndDateString = "";
        String pinnedSslStartDateString = "";
        String pinnedSslEndDateString = "";

        // Convert the dates to strings if they are not null.
        if (currentWebsiteSslStartDate != null) {
            currentWebsiteSslStartDateString = currentWebsiteSslStartDate.toString();
        }

        if (currentWebsiteSslEndDate != null) {
            currentWebsiteSslEndDateString = currentWebsiteSslEndDate.toString();
        }

        if (pinnedSslStartDate != null) {
            pinnedSslStartDateString = pinnedSslStartDate.toString();
        }

        if (pinnedSslEndDate != null) {
            pinnedSslEndDateString = pinnedSslEndDate.toString();
        }

        // Check to see if the pinned information matches the current information.
        if ((nestedScrollWebView.hasPinnedIpAddresses() && !nestedScrollWebView.getCurrentIpAddresses().equals(nestedScrollWebView.getPinnedIpAddresses())) ||
                (nestedScrollWebView.hasPinnedSslCertificate() && (!currentWebsiteIssuedToCName.equals(pinnedSslIssuedToCName) ||
                !currentWebsiteIssuedToOName.equals(pinnedSslIssuedToOName) || !currentWebsiteIssuedToUName.equals(pinnedSslIssuedToUName) ||
                !currentWebsiteIssuedByCName.equals(pinnedSslIssuedByCName) || !currentWebsiteIssuedByOName.equals(pinnedSslIssuedByOName) ||
                !currentWebsiteIssuedByUName.equals(pinnedSslIssuedByUName) || !currentWebsiteSslStartDateString.equals(pinnedSslStartDateString) ||
                !currentWebsiteSslEndDateString.equals(pinnedSslEndDateString)))) {

            // Get a handle for the pinned mismatch alert dialog.
            DialogFragment pinnedMismatchDialogFragment = PinnedMismatchDialog.displayDialog(nestedScrollWebView.getWebViewFragmentId());

            // Show the pinned mismatch alert dialog.
            pinnedMismatchDialogFragment.show(fragmentManager, "Pinned Mismatch");
        }
    }
}