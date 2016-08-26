/**
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Date;

public class ViewSslCertificate extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the activity's layout inflater.
        LayoutInflater layoutInflater   = getActivity().getLayoutInflater();

        // Create a drawable version of the favorite icon.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), MainWebViewActivity.favoriteIcon);

        // Use `AlertDialog.Builder` to create the `AlertDialog`.  `R.style.LightAlertDialog` formats the color of the button text.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        dialogBuilder.setIcon(favoriteIconDrawable);

        // Set an `onClick` listener on the negative button.  Using `null` closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.close, null);

        // Check to see if the website is encrypted.
        if (MainWebViewActivity.mainWebView.getCertificate() == null) {  // The website is not encrypted.
            // Set the title.
            dialogBuilder.setTitle(R.string.unencrypted_website);

            // Set the Layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
            dialogBuilder.setView(layoutInflater.inflate(R.layout.unencrypted_website, null));

            // Create an `AlertDialog` from the `AlertDialog.Builder`
            final AlertDialog alertDialog = dialogBuilder.create();

            // Show `alertDialog`.
            alertDialog.show();

            // `onCreateDialog` requires the return of an `AlertDialog`.
            return alertDialog;

        } else {  // Display the SSL certificate information
            // Set the title.
            dialogBuilder.setTitle(R.string.ssl_certificate);

            // Set the layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
            dialogBuilder.setView(layoutInflater.inflate(R.layout.view_ssl_certificate, null));

            // Create an `AlertDialog` from the `AlertDialog.Builder`
            final AlertDialog alertDialog = dialogBuilder.create();

            // We need to show the `AlertDialog` before we can modify items in the layout.
            alertDialog.show();

            // Get handles for the `TextViews`.
            TextView issuedToCNameTextView = (TextView) alertDialog.findViewById(R.id.issued_to_cname);
            TextView issuedToONameTextView = (TextView) alertDialog.findViewById(R.id.issued_to_oname);
            TextView issuedToUNameTextView = (TextView) alertDialog.findViewById(R.id.issued_to_uname);
            TextView issuedByCNameTextView = (TextView) alertDialog.findViewById(R.id.issued_by_cname);
            TextView issuedByONameTextView = (TextView) alertDialog.findViewById(R.id.issued_by_oname);
            TextView issuedByUNameTextView = (TextView) alertDialog.findViewById(R.id.issued_by_uname);
            TextView startDateTextView = (TextView) alertDialog.findViewById(R.id.start_date);
            TextView endDateTextView = (TextView) alertDialog.findViewById(R.id.end_date);

            // Setup the labels.
            String cNameLabel = getString(R.string.common_name) + "  ";
            String oNameLabel = getString(R.string.organization) + "  ";
            String uNameLabel = getString(R.string.organizational_unit) + "  ";
            String startDateLabel = getString(R.string.start_date) + "  ";
            String endDateLabel = getString(R.string.end_date) + "  ";

            // Get the SSL certificate.
            SslCertificate sslCertificate = MainWebViewActivity.mainWebView.getCertificate();

            // Get the strings from the SSL certificate.
            String issuedToCNameString = sslCertificate.getIssuedTo().getCName();
            String issuedToONameString = sslCertificate.getIssuedTo().getOName();
            String issuedToUNameString = sslCertificate.getIssuedTo().getUName();
            String issuedByCNameString = sslCertificate.getIssuedBy().getCName();
            String issuedByONameString = sslCertificate.getIssuedBy().getOName();
            String issuedByUNameString = sslCertificate.getIssuedBy().getUName();
            Date startDate = sslCertificate.getValidNotBeforeDate();
            Date endDate = sslCertificate.getValidNotAfterDate();

            // Create a `SpannableStringBuilder` for each item.
            SpannableStringBuilder issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedToCNameString);
            SpannableStringBuilder issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedToONameString);
            SpannableStringBuilder issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedToUNameString);
            SpannableStringBuilder issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedByCNameString);
            SpannableStringBuilder issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedByONameString);
            SpannableStringBuilder issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedByUNameString);
            SpannableStringBuilder startDateStringBuilder = new SpannableStringBuilder(startDateLabel + startDate.toString());
            SpannableStringBuilder endDateStringBuilder = new SpannableStringBuilder(endDateLabel + endDate.toString());

            // Create a blue `ForegroundColorSpan`.  We have to use the deprecated `getColor` until API >= 23.
            ForegroundColorSpan blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue));

            // Setup the spans to display the certificate information in blue.
            // `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            issuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            startDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            endDateStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Display the strings.
            issuedToCNameTextView.setText(issuedToCNameStringBuilder);
            issuedToONameTextView.setText(issuedToONameStringBuilder);
            issuedToUNameTextView.setText(issuedToUNameStringBuilder);
            issuedByCNameTextView.setText(issuedByCNameStringBuilder);
            issuedByONameTextView.setText(issuedByONameStringBuilder);
            issuedByUNameTextView.setText(issuedByUNameStringBuilder);
            startDateTextView.setText(startDateStringBuilder);
            endDateTextView.setText(endDateStringBuilder);

            // `onCreateDialog` requires the return of an `AlertDialog`.
            return alertDialog;
        }
    }
}
