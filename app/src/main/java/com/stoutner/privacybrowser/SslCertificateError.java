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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.util.Date;

public class SslCertificateError extends DialogFragment{

    private String primaryError;
    private String urlWithError;
    private String issuedToCName;
    private String issuedToOName;
    private String issuedToUName;
    private String issuedByCName;
    private String issuedByOName;
    private String issuedByUName;
    private String startDate;
    private String endDate;

    public static SslCertificateError displayDialog(SslError error) {
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
        argumentsBundle.putString("StartDate", startDateForBundle.toString());
        argumentsBundle.putString("EndDate", endDateForBundle.toString());

        // Add `argumentsBundle` to this instance of `SslCertificateError`.
        SslCertificateError thisSslCertificateErrorDialog = new SslCertificateError();
        thisSslCertificateErrorDialog.setArguments(argumentsBundle);
        return thisSslCertificateErrorDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Save the components of the SSL error message in class variables.
        urlWithError = getArguments().getString("UrlWithError");
        issuedToCName = getArguments().getString("IssuedToCName");
        issuedToOName = getArguments().getString("IssuedToOName");
        issuedToUName = getArguments().getString("IssuedToUName");
        issuedByCName = getArguments().getString("IssuedByCName");
        issuedByOName = getArguments().getString("IssuedByOName");
        issuedByUName = getArguments().getString("IssuedByUName");
        startDate = getArguments().getString("StartDate");
        endDate = getArguments().getString("EndDate");

        // Get the appropriate string for `primaryError.
        int primaryErrorInt = getArguments().getInt("PrimaryErrorInt");
        switch (primaryErrorInt) {
            case SslError.SSL_NOTYETVALID:
                primaryError = getString(R.string.future_certificate);
                break;

            case SslError.SSL_EXPIRED:
                primaryError = getString(R.string.expired_certificate);
                break;

            case SslError.SSL_IDMISMATCH:
                primaryError = getString(R.string.cn_mismatch);
                break;

            case SslError.SSL_UNTRUSTED:
                primaryError = getString(R.string.untrusted);
                break;

            case SslError.SSL_DATE_INVALID:
                primaryError = getString(R.string.invalid_date);
                break;

            case SslError.SSL_INVALID:
                primaryError = getString(R.string.invalid_certificate);
                break;
        }
    }

    // The public interface is used to send information back to the parent activity.
    public interface SslCertificateErrorListener {
        void onSslErrorCancel();

        void onSslErrorProceed();
    }

    // `sslCertificateErrorListener` is used in `onAttach` and `onCreateDialog`.
    private SslCertificateErrorListener sslCertificateErrorListener;

    // Check to make sure that the parent activity implements the listener.
    public void onAttach(Activity parentActivity) {
        super.onAttach(parentActivity);

        try {
            sslCertificateErrorListener = (SslCertificateErrorListener) parentActivity;
        } catch(ClassCastException exception) {
            throw new ClassCastException(parentActivity.toString() + " must implement SslCertificateErrorListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use `AlertDialog.Builder` to create the `AlertDialog`.  `R.style.LightAlertDialog` formats the color of the button text.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);
        dialogBuilder.setTitle(R.string.ssl_certificate_error);
        // The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.ssl_certificate_error, null));

        // Set an `onClick` listener on the negative button.  `null` doesn't do anything extra when the button is pressed.  The `Dialog` will automatically close.
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sslCertificateErrorListener.onSslErrorCancel();
            }
        });

        // Set an `onClick` listener on the positive button.
        dialogBuilder.setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sslCertificateErrorListener.onSslErrorProceed();
            }
        });


        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        AlertDialog alertDialog = dialogBuilder.create();

        // We have to show the `AlertDialog` before we can modify the content.
        alertDialog.show();

        // Get handles for the `TextViews`
        TextView primaryErrorTextView = (TextView) alertDialog.findViewById(R.id.primary_error);
        TextView urlTextView = (TextView) alertDialog.findViewById(R.id.url_error_dialog);
        TextView issuedToCNameTextView = (TextView) alertDialog.findViewById(R.id.issued_to_cname_error_dialog);
        TextView issuedToONameTextView = (TextView) alertDialog.findViewById(R.id.issued_to_oname_error_dialog);
        TextView issuedToUNameTextView = (TextView) alertDialog.findViewById(R.id.issued_to_uname_error_dialog);
        TextView issuedByCNameTextView = (TextView) alertDialog.findViewById(R.id.issued_by_cname_error_dialog);
        TextView issuedByONameTextView = (TextView) alertDialog.findViewById(R.id.issued_by_oname_error_dialog);
        TextView issuedByUNameTextView = (TextView) alertDialog.findViewById(R.id.issued_by_uname_error_dialog);
        TextView startDateTextView = (TextView) alertDialog.findViewById(R.id.start_date_error_dialog);
        TextView endDateTextView = (TextView) alertDialog.findViewById(R.id.end_date_error_dialog);

        // Setup the common strings.
        String urlLabel = getString(R.string.url_label) + "  ";
        String cNameLabel = getString(R.string.common_name) + "  ";
        String oNameLabel = getString(R.string.organization) + "  ";
        String uNameLabel = getString(R.string.organizational_unit) + "  ";
        String startDateLabel = getString(R.string.start_date) + "  ";
        String endDateLabel = getString(R.string.end_date) + "  ";

        // Create a `SpannableStringBuilder` for each `TextView` that needs multiple colors of text.
        SpannableStringBuilder urlStringBuilder = new SpannableStringBuilder(urlLabel + urlWithError);
        SpannableStringBuilder issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedToCName);
        SpannableStringBuilder issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedToOName);
        SpannableStringBuilder issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedToUName);
        SpannableStringBuilder issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + issuedByCName);
        SpannableStringBuilder issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + issuedByOName);
        SpannableStringBuilder issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + issuedByUName);
        SpannableStringBuilder startDateStringBuilder = new SpannableStringBuilder(startDateLabel + startDate);
        SpannableStringBuilder endDateStringBuilder = new SpannableStringBuilder((endDateLabel + endDate));

        // Create a blue `ForegroundColorSpan`.  We have to use the deprecated `getColor` until API >= 23.
        ForegroundColorSpan blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue));

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


        // Display the strings.
        primaryErrorTextView.setText(primaryError);
        urlTextView.setText(urlStringBuilder);
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
