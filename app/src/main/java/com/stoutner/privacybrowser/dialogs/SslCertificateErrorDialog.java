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
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
// `AppCompatDialogFragment` is used instead of `DialogFragment` to avoid an error on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;

public class SslCertificateErrorDialog extends AppCompatDialogFragment {
    // `sslCertificateErrorListener` is used in `onAttach` and `onCreateDialog`.
    private SslCertificateErrorListener sslCertificateErrorListener;

    // The public interface is used to send information back to the parent activity.
    public interface SslCertificateErrorListener {
        void onSslErrorCancel();

        void onSslErrorProceed();
    }

    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `SslCertificateErrorListener` from the launching context.
        sslCertificateErrorListener = (SslCertificateErrorListener) context;
    }

    public static SslCertificateErrorDialog displayDialog(SslError error) {
        // Get the various components of the SSL error message.
        int primaryErrorIntForBundle = error.getPrimaryError();
        String urlWithErrorForBundle = error.getUrl();
        SslCertificate sslCertificate = error.getCertificate();
        String issuedToCNameForBundle = sslCertificate.getIssuedTo().getCName();
        String issuedToONameForBundle = sslCertificate.getIssuedTo().getOName();
        String issuedToUNameForBundle = sslCertificate.getIssuedTo().getUName();
        String issuedByCNameForBundle = sslCertificate.getIssuedBy().getCName();
        String issuedByONameForBundle = sslCertificate.getIssuedBy().getOName();
        String issuedByUNameForBundle = sslCertificate.getIssuedBy().getUName();
        Date startDateForBundle = sslCertificate.getValidNotBeforeDate();
        Date endDateForBundle = sslCertificate.getValidNotAfterDate();

        // Store the SSL error message components in a `Bundle`.
        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putInt("PrimaryErrorInt", primaryErrorIntForBundle);
        argumentsBundle.putString("UrlWithError", urlWithErrorForBundle);
        argumentsBundle.putString("IssuedToCName", issuedToCNameForBundle);
        argumentsBundle.putString("IssuedToOName", issuedToONameForBundle);
        argumentsBundle.putString("IssuedToUName", issuedToUNameForBundle);
        argumentsBundle.putString("IssuedByCName", issuedByCNameForBundle);
        argumentsBundle.putString("IssuedByOName", issuedByONameForBundle);
        argumentsBundle.putString("IssuedByUName", issuedByUNameForBundle);
        argumentsBundle.putString("StartDate", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(startDateForBundle));
        argumentsBundle.putString("EndDate", DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(endDateForBundle));

        // Add `argumentsBundle` to this instance of `SslCertificateErrorDialog`.
        SslCertificateErrorDialog thisSslCertificateErrorDialog = new SslCertificateErrorDialog();
        thisSslCertificateErrorDialog.setArguments(argumentsBundle);
        return thisSslCertificateErrorDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @SuppressWarnings("deprecation")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warning that `getArguments()` might be null.
        assert getArguments() != null;

        // Get the components of the SSL error message from the bundle.
        int primaryErrorInt = getArguments().getInt("PrimaryErrorInt");
        String urlWithErrors = getArguments().getString("UrlWithError");
        String issuedToCName = getArguments().getString("IssuedToCName");
        String issuedToOName = getArguments().getString("IssuedToOName");
        String issuedToUName = getArguments().getString("IssuedToUName");
        String issuedByCName = getArguments().getString("IssuedByCName");
        String issuedByOName = getArguments().getString("IssuedByOName");
        String issuedByUName = getArguments().getString("IssuedByUName");
        String startDate = getArguments().getString("StartDate");
        String endDate = getArguments().getString("EndDate");

        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and icon according to the theme.
        if (MainWebViewActivity.darkTheme) {
            // Set the style.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_dark);
        } else {
            // Set the style.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_light);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.ssl_certificate_error);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.ssl_certificate_error, null));

        // Set a listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> sslCertificateErrorListener.onSslErrorCancel());

        // Set a listener on the positive button.
        dialogBuilder.setPositiveButton(R.string.proceed, (DialogInterface dialog, int which) -> sslCertificateErrorListener.onSslErrorProceed());


        // Create an alert dialog from the alert dialog builder.
        AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Get a URI for the URL with errors.
        Uri uriWithErrors = Uri.parse(urlWithErrors);

        // Get the IP addresses for the URI.
        new GetIpAddresses(getActivity(), alertDialog).execute(uriWithErrors.getHost());

        // The alert dialog must be shown before the contents can be modified.
        alertDialog.show();

        // Get handles for the `TextViews`
        TextView primaryErrorTextView = alertDialog.findViewById(R.id.primary_error);
        TextView urlTextView = alertDialog.findViewById(R.id.url);
        TextView issuedToCNameTextView = alertDialog.findViewById(R.id.issued_to_cname);
        TextView issuedToONameTextView = alertDialog.findViewById(R.id.issued_to_oname);
        TextView issuedToUNameTextView = alertDialog.findViewById(R.id.issued_to_uname);
        TextView issuedByTextView = alertDialog.findViewById(R.id.issued_by_textview);
        TextView issuedByCNameTextView = alertDialog.findViewById(R.id.issued_by_cname);
        TextView issuedByONameTextView = alertDialog.findViewById(R.id.issued_by_oname);
        TextView issuedByUNameTextView = alertDialog.findViewById(R.id.issued_by_uname);
        TextView validDatesTextView = alertDialog.findViewById(R.id.valid_dates_textview);
        TextView startDateTextView = alertDialog.findViewById(R.id.start_date);
        TextView endDateTextView = alertDialog.findViewById(R.id.end_date);

        // Setup the common strings.
        String urlLabel = getString(R.string.url_label) + "  ";
        String cNameLabel = getString(R.string.common_name) + "  ";
        String oNameLabel = getString(R.string.organization) + "  ";
        String uNameLabel = getString(R.string.organizational_unit) + "  ";
        String startDateLabel = getString(R.string.start_date) + "  ";
        String endDateLabel = getString(R.string.end_date) + "  ";

        // Create a spannable string builder for each text view that needs multiple colors of text.
        SpannableStringBuilder urlStringBuilder = new SpannableStringBuilder(urlLabel + urlWithErrors);
        SpannableStringBuilder issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedToCName);
        SpannableStringBuilder issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedToOName);
        SpannableStringBuilder issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedToUName);
        SpannableStringBuilder issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedByCName);
        SpannableStringBuilder issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedByOName);
        SpannableStringBuilder issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedByUName);
        SpannableStringBuilder startDateStringBuilder = new SpannableStringBuilder(startDateLabel + startDate);
        SpannableStringBuilder endDateStringBuilder = new SpannableStringBuilder((endDateLabel + endDate));

        // Create a red foreground color span.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
        @SuppressWarnings("deprecation") ForegroundColorSpan redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));

        // Create a blue `ForegroundColorSpan`.
        ForegroundColorSpan blueColorSpan;

        // Set a blue color span according to the theme.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
        if (MainWebViewActivity.darkTheme) {
            //noinspection deprecation
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
        } else {
            //noinspection deprecation
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
        }

        // Setup the spans to display the certificate information in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
        urlStringBuilder.setSpan(blueColorSpan, urlLabel.length(), urlStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        issuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        issuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        issuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        issuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        issuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        issuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        startDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        endDateStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // Initialize `primaryErrorString`.
        String primaryErrorString = "";

        // Highlight the primary error in red and store the primary error string in `primaryErrorString`.
        switch (primaryErrorInt) {
            case SslError.SSL_IDMISMATCH:
                // Change the URL span colors to red.
                urlStringBuilder.setSpan(redColorSpan, urlLabel.length(), urlStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                issuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // Store the primary error string.
                primaryErrorString = getString(R.string.cn_mismatch);
                break;

            case SslError.SSL_UNTRUSTED:
                // Change the issued by text view text to red.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
                issuedByTextView.setTextColor(getResources().getColor(R.color.red_a700));

                // Change the issued by span color to red.
                issuedByCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                issuedByONameStringBuilder.setSpan(redColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                issuedByUNameStringBuilder.setSpan(redColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // Store the primary error string.
                primaryErrorString = getString(R.string.untrusted);
                break;

            case SslError.SSL_DATE_INVALID:
                // Change the valid dates text view text to red.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
                validDatesTextView.setTextColor(getResources().getColor(R.color.red_a700));

                // Change the date span colors to red.
                startDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                endDateStringBuilder.setSpan(redColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // Store the primary error string.
                primaryErrorString = getString(R.string.invalid_date);
                break;

            case SslError.SSL_NOTYETVALID:
                // Change the start date span color to red.
                startDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // Store the primary error string.
                primaryErrorString = getString(R.string.future_certificate);
                break;

            case SslError.SSL_EXPIRED:
                // Change the end date span color to red.
                endDateStringBuilder.setSpan(redColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // Store the primary error string.
                primaryErrorString = getString(R.string.expired_certificate);
                break;

            case SslError.SSL_INVALID:
                // Store the primary error string.
                primaryErrorString = getString(R.string.invalid_certificate);
                break;
        }


        // Display the strings.
        primaryErrorTextView.setText(primaryErrorString);
        urlTextView.setText(urlStringBuilder);
        issuedToCNameTextView.setText(issuedToCNameStringBuilder);
        issuedToONameTextView.setText(issuedToONameStringBuilder);
        issuedToUNameTextView.setText(issuedToUNameStringBuilder);
        issuedByCNameTextView.setText(issuedByCNameStringBuilder);
        issuedByONameTextView.setText(issuedByONameStringBuilder);
        issuedByUNameTextView.setText(issuedByUNameStringBuilder);
        startDateTextView.setText(startDateStringBuilder);
        endDateTextView.setText(endDateStringBuilder);

        // `onCreateDialog` requires the return of an alert dialog.
        return alertDialog;
    }


    // This must run asynchronously because it involves a network request.  `String` declares the parameters.  `Void` does not declare progress units.  `SpannableStringBuilder` contains the results.
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
