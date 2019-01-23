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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;

import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.R;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

// `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
@SuppressLint("InflateParams")
public class ViewSslCertificateDialog extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the activity's layout inflater.
        LayoutInflater layoutInflater   = getActivity().getLayoutInflater();

        // Create a drawable version of the favorite icon.
        Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), MainWebViewActivity.favoriteIconBitmap);

        // Use a builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the icon.
        dialogBuilder.setIcon(favoriteIconDrawable);

        // Set a listener on the negative button.  Using `null` as the listener closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.close, null);

        // Check to see if the website is encrypted.
        if (MainWebViewActivity.sslCertificate == null) {  // The website is not encrypted.
            // Set the title.
            dialogBuilder.setTitle(R.string.unencrypted_website);

            // Set the Layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
            dialogBuilder.setView(layoutInflater.inflate(R.layout.unencrypted_website, null));

            // Create an alert dialog from the alert dialog builder.
            final AlertDialog alertDialog = dialogBuilder.create();

            // Disable screenshots if not allowed.
            if (!MainWebViewActivity.allowScreenshots) {
                // Remove the warning below that `getWindow()` might be null.
                assert alertDialog.getWindow() != null;

                // Disable screenshots.
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }

            // `onCreateDialog` requires the return of an `AlertDialog`.
            return alertDialog;

        } else {  // Display the SSL certificate information
            // Set the title.
            dialogBuilder.setTitle(R.string.ssl_certificate);

            // Set the layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
            dialogBuilder.setView(layoutInflater.inflate(R.layout.view_ssl_certificate, null));

            // Create an alert dialog from the builder.
            final AlertDialog alertDialog = dialogBuilder.create();

            // Disable screenshots if not allowed.
            if (!MainWebViewActivity.allowScreenshots) {
                // Remove the warning below that `getWindow()` might be null.
                assert alertDialog.getWindow() != null;

                // Disable screenshots.
                alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }

            // The alert dialog must be shown before items in the layout can be modified.
            alertDialog.show();

            // Get handles for the text views.
            TextView domainTextView = alertDialog.findViewById(R.id.domain);
            TextView ipAddressesTextView = alertDialog.findViewById(R.id.ip_addresses);
            TextView issuedToCNameTextView = alertDialog.findViewById(R.id.issued_to_cname);
            TextView issuedToONameTextView = alertDialog.findViewById(R.id.issued_to_oname);
            TextView issuedToUNameTextView = alertDialog.findViewById(R.id.issued_to_uname);
            TextView issuedByCNameTextView = alertDialog.findViewById(R.id.issued_by_cname);
            TextView issuedByONameTextView = alertDialog.findViewById(R.id.issued_by_oname);
            TextView issuedByUNameTextView = alertDialog.findViewById(R.id.issued_by_uname);
            TextView startDateTextView = alertDialog.findViewById(R.id.start_date);
            TextView endDateTextView = alertDialog.findViewById(R.id.end_date);

            // Setup the labels.
            String domainLabel = getString(R.string.domain_label) + "  ";
            String cNameLabel = getString(R.string.common_name) + "  ";
            String oNameLabel = getString(R.string.organization) + "  ";
            String uNameLabel = getString(R.string.organizational_unit) + "  ";
            String startDateLabel = getString(R.string.start_date) + "  ";
            String endDateLabel = getString(R.string.end_date) + "  ";

            // Convert the formatted URL string to a URI.
            Uri uri = Uri.parse(MainWebViewActivity.formattedUrlString);

            // Extract the domain name from the URI.
            String domainString = uri.getHost();

            // Get the IP addresses.
            new GetIpAddresses(getActivity(), alertDialog).execute(domainString);

            // Get the SSL certificate.
            SslCertificate sslCertificate = MainWebViewActivity.sslCertificate;

            // Get the strings from the SSL certificate.
            String issuedToCName = sslCertificate.getIssuedTo().getCName();
            String issuedToOName = sslCertificate.getIssuedTo().getOName();
            String issuedToUName = sslCertificate.getIssuedTo().getUName();
            String issuedByCName = sslCertificate.getIssuedBy().getCName();
            String issuedByOName = sslCertificate.getIssuedBy().getOName();
            String issuedByUName = sslCertificate.getIssuedBy().getUName();
            Date startDate = sslCertificate.getValidNotBeforeDate();
            Date endDate = sslCertificate.getValidNotAfterDate();

            // Create spannable string builders for each text view that needs multiple colors of text.
            SpannableStringBuilder domainStringBuilder = new SpannableStringBuilder(domainLabel + domainString);
            SpannableStringBuilder issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedToCName);
            SpannableStringBuilder issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedToOName);
            SpannableStringBuilder issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedToUName);
            SpannableStringBuilder issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedByCName);
            SpannableStringBuilder issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedByOName);
            SpannableStringBuilder issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedByUName);
            SpannableStringBuilder startDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(startDate));
            SpannableStringBuilder endDateStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(endDate));

            // Create a red foreground color span.  The deprecated `getColor` must be used until the minimum API >= 23.
            @SuppressWarnings("deprecation") ForegroundColorSpan redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));

            // Create a blue foreground color span.
            ForegroundColorSpan blueColorSpan;

            // Set the blue color span according to the theme.  The deprecated `getColor()` must be used until the minimum API >= 23.
            if (MainWebViewActivity.darkTheme) {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
            } else {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
            }

            // Remove the incorrect lint error that `.equals` might produce a NullPointerException.
            assert domainString != null;

            // Formet the `domainString` and `issuedToCName` colors.
            if (domainString.equals(issuedToCName)) {  // `domainString` and `issuedToCName` match.
                // Set the strings to be blue.
                domainStringBuilder.setSpan(blueColorSpan, domainLabel.length(), domainStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                issuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else if(issuedToCName.startsWith("*.")){  // `issuedToCName` begins with a wildcard.
                // Remove the initial `*.`.
                String baseCertificateDomain = issuedToCName.substring(2);

                // Setup a copy of `domainString` to test subdomains.
                String domainStringSubdomain = domainString;

                // Initialize `domainNamesMatch`.
                boolean domainNamesMatch = false;

                // Check all the subdomains in `domainStringSubdomain` against `baseCertificateDomain`.
                while (!domainNamesMatch && domainStringSubdomain.contains(".")) {  // Stop checking if we know that `domainNamesMatch` is `true` or if we run out of  `.`.
                    // Test the `domainStringSubdomain` against `baseCertificateDomain`.
                    if (domainStringSubdomain.equals(baseCertificateDomain)) {
                        domainNamesMatch = true;
                    }

                    // Strip out the lowest subdomain of `certificateCommonNameSubdomain`.
                    domainStringSubdomain = domainStringSubdomain.substring(domainStringSubdomain.indexOf(".") + 1);
                }

                // Format the domain and issued to Common Name according to `domainNamesMatch`.
                if (domainNamesMatch) {  // `domainString` is a subdomain of the wildcard `issuedToCNameString`.
                    // Set the strings to be blue.
                    domainStringBuilder.setSpan(blueColorSpan, domainLabel.length(), domainStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    issuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {  // `domainString` is not a subdomain of the wildcard `issuedToCNameString`.
                    // Set the string to be red.
                    domainStringBuilder.setSpan(redColorSpan, domainLabel.length(), domainStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    issuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            } else {  // The strings do not match and `issuedToCNameString` does not begin with a wildcard.
                // Set the strings to be red.
                domainStringBuilder.setSpan(redColorSpan, domainLabel.length(), domainStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                issuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            // Set the issued to and issued by spans to display the certificate information in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            issuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            issuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            Date currentDate = Calendar.getInstance().getTime();

            //  Format the start date color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            if (startDate.after(currentDate)) {  // The certificate start date is in the future.
                startDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {  // The certificate start date is in the past.
                startDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            // Format the end date color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            if (endDate.before(currentDate)) {  // The certificate end date is in the past.
                endDateStringBuilder.setSpan(redColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {  // The certificate end date is in the future.
                endDateStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            // Display the strings.
            domainTextView.setText(domainStringBuilder);
            ipAddressesTextView.setText(getString(R.string.ip_addresses));
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

    // This must run asynchronously because it involves a network request.  `String` declares the parameters.  `Void` does not declare progress units.  `String` contains the results.
    private static class GetIpAddresses extends AsyncTask<String, Void, SpannableStringBuilder> {
        // The weak references are used to determine if the activity or the alert dialog have disappeared while the AsyncTask is running.
        private WeakReference<Activity> activityWeakReference;
        private WeakReference<AlertDialog> alertDialogWeakReference;

        GetIpAddresses(Activity activity, AlertDialog alertDialog) {
            // Populate the weak references.
            activityWeakReference = new WeakReference<>(activity);
            alertDialogWeakReference = new WeakReference<>(alertDialog);
        }

        @Override
        protected SpannableStringBuilder doInBackground(String... domainName) {
            // Get handles for the activity and the alert dialog.
            Activity activity = activityWeakReference.get();
            AlertDialog alertDialog = alertDialogWeakReference.get();

            // Abort if the activity or the dialog is gone.
            if ((activity == null) || (activity.isFinishing()) || (alertDialog == null)) {
                return new SpannableStringBuilder();
            }

            // Initialize an IP address string builder.
            StringBuilder ipAddresses = new StringBuilder();

            // Get an array with the IP addresses for the host.
            try {
                // Get an array with all the IP addresses for the domain.
                InetAddress[] inetAddressesArray = InetAddress.getAllByName(domainName[0]);

                // Add each IP address to the string builder.
                for (InetAddress inetAddress : inetAddressesArray) {
                    if (ipAddresses.length() == 0) {  // This is the first IP address.
                        // Add the IP Address to the string builder.
                        ipAddresses.append(inetAddress.getHostAddress());
                    } else {  // This is not the first IP address.
                        // Add a line break to the string builder first.
                        ipAddresses.append("\n");

                        // Add the IP address to the string builder.
                        ipAddresses.append(inetAddress.getHostAddress());
                    }
                }
            } catch (UnknownHostException exception) {
                // Do nothing.
            }

            // Set the label.
            String ipAddressesLabel = activity.getString(R.string.ip_addresses) + "  ";

            // Create a spannable string builder.
            SpannableStringBuilder ipAddressesStringBuilder = new SpannableStringBuilder(ipAddressesLabel + ipAddresses);

            // Create a blue foreground color span.
            ForegroundColorSpan blueColorSpan;

            // Set the blue color span according to the theme.  The deprecated `getColor()` must be used until the minimum API >= 23.
            if (MainWebViewActivity.darkTheme) {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(activity.getResources().getColor(R.color.blue_400));
            } else {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(activity.getResources().getColor(R.color.blue_700));
            }

            // Set the string builder to display the certificate information in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            ipAddressesStringBuilder.setSpan(blueColorSpan, ipAddressesLabel.length(), ipAddressesStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Return the formatted string.
            return ipAddressesStringBuilder;
        }

        // `onPostExecute()` operates on the UI thread.
        @Override
        protected void onPostExecute(SpannableStringBuilder ipAddresses) {
            // Get handles for the activity and the alert dialog.
            Activity activity = activityWeakReference.get();
            AlertDialog alertDialog = alertDialogWeakReference.get();

            // Abort if the activity or the alert dialog is gone.
            if ((activity == null) || (activity.isFinishing()) || (alertDialog == null)) {
                return;
            }

            // Get a handle for the IP addresses text view.
            TextView ipAddressesTextView = alertDialog.findViewById(R.id.ip_addresses);

            // Populate the IP addresses text view.
            ipAddressesTextView.setText(ipAddresses);
        }
    }
}