/*
 * Copyright © 2017-2018 Soren Stoutner <soren@stoutner.com>.
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
// `AppCompatDialogFragment` is used instead of `DialogFragment` to avoid an error on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.definitions.WrapVerticalContentViewPager;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

import java.text.DateFormat;
import java.util.Date;

public class PinnedSslCertificateMismatchDialog extends AppCompatDialogFragment {
    // Instantiate the class variables.
    private PinnedSslCertificateMismatchListener pinnedSslCertificateMismatchListener;
    private LayoutInflater layoutInflater;
    private String currentSslIssuedToCNameString;
    private String currentSslIssuedToONameString;
    private String currentSslIssuedToUNameString;
    private String currentSslIssuedByCNameString;
    private String currentSslIssuedByONameString;
    private String currentSslIssuedByUNameString;
    private Date currentSslStartDate;
    private Date currentSslEndDate;

    // The public interface is used to send information back to the parent activity.
    public interface PinnedSslCertificateMismatchListener {
        void onSslMismatchBack();

        void onSslMismatchProceed();
    }

    // Check to make sure that the parent activity implements the listener.
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `PinnedSslCertificateMismatchListener` from the launching context.
        pinnedSslCertificateMismatchListener = (PinnedSslCertificateMismatchListener) context;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        layoutInflater = getActivity().getLayoutInflater();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_dark);
        } else {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_light);
        }

        // Setup the neutral button.
        dialogBuilder.setNeutralButton(R.string.update_ssl, (DialogInterface dialog, int which) -> {
            // Initialize the `long` date variables.  If the date is `null`, a long value of `0` will be stored in the Domains database entry.
            long currentSslStartDateLong = 0;
            long currentSslEndDateLong = 0;

            // Convert the `Dates` into `longs`.
            if (currentSslStartDate != null) {
                currentSslStartDateLong = currentSslStartDate.getTime();
            }

            if (currentSslEndDate != null) {
                currentSslEndDateLong = currentSslEndDate.getTime();
            }

            // Initialize the database handler.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
            DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getContext(), null, null, 0);

            // Update the pinned SSL certificate for this domain.
            domainsDatabaseHelper.updateCertificate(MainWebViewActivity.domainSettingsDatabaseId, currentSslIssuedToCNameString, currentSslIssuedToONameString, currentSslIssuedToUNameString,
                    currentSslIssuedByCNameString, currentSslIssuedByONameString, currentSslIssuedByUNameString, currentSslStartDateLong, currentSslEndDateLong);

            // Update the pinned SSL certificate global variables to match the information that is now in the database.
            MainWebViewActivity.pinnedDomainSslIssuedToCNameString = currentSslIssuedToCNameString;
            MainWebViewActivity.pinnedDomainSslIssuedToONameString = currentSslIssuedToONameString;
            MainWebViewActivity.pinnedDomainSslIssuedToUNameString = currentSslIssuedToUNameString;
            MainWebViewActivity.pinnedDomainSslIssuedByCNameString = currentSslIssuedByCNameString;
            MainWebViewActivity.pinnedDomainSslIssuedByONameString = currentSslIssuedByONameString;
            MainWebViewActivity.pinnedDomainSslIssuedByUNameString = currentSslIssuedByUNameString;
            MainWebViewActivity.pinnedDomainSslStartDate = currentSslStartDate;
            MainWebViewActivity.pinnedDomainSslEndDate = currentSslEndDate;
        });

        // Setup the negative button.
        dialogBuilder.setNegativeButton(R.string.back, (DialogInterface dialog, int which) -> {
            // Call the `onSslMismatchBack` public interface to send the `WebView` back one page.
            pinnedSslCertificateMismatchListener.onSslMismatchBack();
        });

        // Setup the positive button.
        dialogBuilder.setPositiveButton(R.string.proceed, (DialogInterface dialog, int which) -> {
            // Call the `onSslMismatchProceed` public interface.
            pinnedSslCertificateMismatchListener.onSslMismatchProceed();
        });

        // Set the title.
        dialogBuilder.setTitle(R.string.ssl_certificate_mismatch);

        // Set the layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.pinned_ssl_certificate_mismatch_linearlayout, null));

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the alert dialog so the items in the layout can be modified.
        alertDialog.show();

        //  Setup the view pager.
        WrapVerticalContentViewPager wrapVerticalContentViewPager = alertDialog.findViewById(R.id.pinned_ssl_certificate_mismatch_viewpager);
        wrapVerticalContentViewPager.setAdapter(new pagerAdapter());

        // Setup the tab layout and connect it to the view pager.
        TabLayout tabLayout = alertDialog.findViewById(R.id.pinned_ssl_certificate_mismatch_tablayout);
        tabLayout.setupWithViewPager(wrapVerticalContentViewPager);

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return alertDialog;
    }

    private class pagerAdapter extends PagerAdapter {
        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            // Check to see if the `View` and the `Object` are the same.
            return (view == object);
        }

        @Override
        public int getCount() {
            // There are two tabs.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Return the current tab title.
            if (position == 0) {  // The current SSL certificate tab.
                return getString(R.string.current_ssl);
            } else {  // The pinned SSL certificate tab.
                return getString(R.string.pinned_ssl);
            }
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            // Inflate the `ScrollView` for this tab.
            ViewGroup tabViewGroup = (ViewGroup) layoutInflater.inflate(R.layout.pinned_ssl_certificate_mismatch_scrollview, container, false);

            // Get handles for the `TextViews`.
            TextView issuedToCNameTextView = tabViewGroup.findViewById(R.id.issued_to_cname);
            TextView issuedToONameTextView = tabViewGroup.findViewById(R.id.issued_to_oname);
            TextView issuedToUNameTextView = tabViewGroup.findViewById(R.id.issued_to_uname);
            TextView issuedByCNameTextView = tabViewGroup.findViewById(R.id.issued_by_cname);
            TextView issuedByONameTextView = tabViewGroup.findViewById(R.id.issued_by_oname);
            TextView issuedByUNameTextView = tabViewGroup.findViewById(R.id.issued_by_uname);
            TextView startDateTextView = tabViewGroup.findViewById(R.id.start_date);
            TextView endDateTextView = tabViewGroup.findViewById(R.id.end_date);

            // Setup the labels.
            String cNameLabel = getString(R.string.common_name) + "  ";
            String oNameLabel = getString(R.string.organization) + "  ";
            String uNameLabel = getString(R.string.organizational_unit) + "  ";
            String startDateLabel = getString(R.string.start_date) + "  ";
            String endDateLabel = getString(R.string.end_date) + "  ";

            // Get the current website SSL certificate.
            SslCertificate sslCertificate = MainWebViewActivity.sslCertificate;

            // Extract the individual pieces of information from the current website SSL certificate if it is not null.
            if (sslCertificate != null) {
                currentSslIssuedToCNameString = sslCertificate.getIssuedTo().getCName();
                currentSslIssuedToONameString = sslCertificate.getIssuedTo().getOName();
                currentSslIssuedToUNameString = sslCertificate.getIssuedTo().getUName();
                currentSslIssuedByCNameString = sslCertificate.getIssuedBy().getCName();
                currentSslIssuedByONameString = sslCertificate.getIssuedBy().getOName();
                currentSslIssuedByUNameString = sslCertificate.getIssuedBy().getUName();
                currentSslStartDate = sslCertificate.getValidNotBeforeDate();
                currentSslEndDate = sslCertificate.getValidNotAfterDate();
            } else {
                // Initialize the current website SSL certificate variables with blank information.
                currentSslIssuedToCNameString = "";
                currentSslIssuedToONameString = "";
                currentSslIssuedToUNameString = "";
                currentSslIssuedByCNameString = "";
                currentSslIssuedByONameString = "";
                currentSslIssuedByUNameString = "";
            }

            // Initialize the `SpannableStringBuilders`.
            SpannableStringBuilder issuedToCNameStringBuilder;
            SpannableStringBuilder issuedToONameStringBuilder;
            SpannableStringBuilder issuedToUNameStringBuilder;
            SpannableStringBuilder issuedByCNameStringBuilder;
            SpannableStringBuilder issuedByONameStringBuilder;
            SpannableStringBuilder issuedByUNameStringBuilder;
            SpannableStringBuilder startDateStringBuilder;
            SpannableStringBuilder endDateStringBuilder;

            // Setup the `SpannableStringBuilders` for each tab.
            if (position == 0) {  // Setup the current SSL certificate tab.
                issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + currentSslIssuedToCNameString);
                issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + currentSslIssuedToONameString);
                issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + currentSslIssuedToUNameString);
                issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + currentSslIssuedByCNameString);
                issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + currentSslIssuedByONameString);
                issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + currentSslIssuedByUNameString);

                // Set the dates if they aren't `null`.
                if (currentSslStartDate == null) {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel);
                } else {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(currentSslStartDate));
                }

                if (currentSslEndDate == null) {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel);
                } else {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(currentSslEndDate));
                }
            } else {  // Setup the pinned SSL certificate tab.
                issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + MainWebViewActivity.pinnedDomainSslIssuedToCNameString);
                issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + MainWebViewActivity.pinnedDomainSslIssuedToONameString);
                issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + MainWebViewActivity.pinnedDomainSslIssuedToUNameString);
                issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + MainWebViewActivity.pinnedDomainSslIssuedByCNameString);
                issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + MainWebViewActivity.pinnedDomainSslIssuedByONameString);
                issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + MainWebViewActivity.pinnedDomainSslIssuedByUNameString);

                // Set the dates if they aren't `null`.
                if (MainWebViewActivity.pinnedDomainSslStartDate == null) {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel);
                } else {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG)
                            .format(MainWebViewActivity.pinnedDomainSslStartDate));
                }

                if (MainWebViewActivity.pinnedDomainSslEndDate == null) {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel);
                } else {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(MainWebViewActivity.pinnedDomainSslEndDate));
                }
            }

            // Create a red `ForegroundColorSpan`.  We have to use the deprecated `getColor` until API >= 23.
            @SuppressWarnings("deprecation") ForegroundColorSpan redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));

            // Create a blue `ForegroundColorSpan`.
            ForegroundColorSpan blueColorSpan;

            // Set `blueColorSpan` according to the theme.  We have to use the deprecated `getColor()` until API >= 23.
            if (MainWebViewActivity.darkTheme) {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
            } else {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
            }

            // Configure the spans to display conflicting information in red.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            if (currentSslIssuedToCNameString.equals(MainWebViewActivity.pinnedDomainSslIssuedToCNameString)) {
                issuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                issuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            if (currentSslIssuedToONameString.equals(MainWebViewActivity.pinnedDomainSslIssuedToONameString)) {
                issuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                issuedToONameStringBuilder.setSpan(redColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            if (currentSslIssuedToUNameString.equals(MainWebViewActivity.pinnedDomainSslIssuedToUNameString)) {
                issuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                issuedToUNameStringBuilder.setSpan(redColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            if (currentSslIssuedByCNameString.equals(MainWebViewActivity.pinnedDomainSslIssuedByCNameString)) {
                issuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                issuedByCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            if (currentSslIssuedByONameString.equals(MainWebViewActivity.pinnedDomainSslIssuedByONameString)) {
                issuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                issuedByONameStringBuilder.setSpan(redColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            if (currentSslIssuedByUNameString.equals(MainWebViewActivity.pinnedDomainSslIssuedByUNameString)) {
                issuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                issuedByUNameStringBuilder.setSpan(redColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            if ((currentSslStartDate != null) && (MainWebViewActivity.pinnedDomainSslStartDate != null) && currentSslStartDate.equals(MainWebViewActivity.pinnedDomainSslStartDate)) {
                startDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                startDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            if ((currentSslEndDate != null) && (MainWebViewActivity.pinnedDomainSslEndDate != null) && currentSslEndDate.equals(MainWebViewActivity.pinnedDomainSslEndDate)) {
                endDateStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                endDateStringBuilder.setSpan(redColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            // Display the strings.
            issuedToCNameTextView.setText(issuedToCNameStringBuilder);
            issuedToONameTextView.setText(issuedToONameStringBuilder);
            issuedToUNameTextView.setText(issuedToUNameStringBuilder);
            issuedByCNameTextView.setText(issuedByCNameStringBuilder);
            issuedByONameTextView.setText(issuedByONameStringBuilder);
            issuedByUNameTextView.setText(issuedByUNameStringBuilder);
            startDateTextView.setText(startDateStringBuilder);
            endDateTextView.setText(endDateStringBuilder);

            // Display the tab.
            container.addView(tabViewGroup);

            // Make it so.
            return tabViewGroup;
        }
    }
}
