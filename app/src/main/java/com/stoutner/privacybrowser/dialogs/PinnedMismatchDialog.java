/*
 * Copyright © 2017-2019 Soren Stoutner <soren@stoutner.com>.
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

public class PinnedMismatchDialog extends AppCompatDialogFragment {
    // Instantiate the class variables.
    private PinnedMismatchListener pinnedMismatchListener;
    private LayoutInflater layoutInflater;
    private String currentSslIssuedToCName;
    private String currentSslIssuedToOName;
    private String currentSslIssuedToUName;
    private String currentSslIssuedByCName;
    private String currentSslIssuedByOName;
    private String currentSslIssuedByUName;
    private Date currentSslStartDate;
    private Date currentSslEndDate;
    private boolean pinnedSslCertificate;
    private boolean pinnedIpAddresses;

    // The public interface is used to send information back to the parent activity.
    public interface PinnedMismatchListener {
        void onPinnedMismatchBack();

        void onPinnedMismatchProceed();
    }

    // Check to make sure that the parent activity implements the listener.
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `PinnedSslCertificateMismatchListener` from the launching context.
        pinnedMismatchListener = (PinnedMismatchListener) context;
    }

    public static PinnedMismatchDialog displayDialog(boolean pinnedSslCertificate, boolean pinnedIpAddresses) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the variables in the bundle.
        argumentsBundle.putBoolean("Pinned_SSL_Certificate", pinnedSslCertificate);
        argumentsBundle.putBoolean("Pinned_IP_Addresses", pinnedIpAddresses);

        // Add the arguments bundle to this instance of `PinnedMismatchDialog`.
        PinnedMismatchDialog thisPinnedMismatchDialog = new PinnedMismatchDialog();
        thisPinnedMismatchDialog.setArguments(argumentsBundle);
        return thisPinnedMismatchDialog;
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
        } else {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Remove the incorrect lint warning below that `.getArguments.getBoolean()` might be null.
        assert getArguments() != null;

        // Get the variables from the bundle.
        pinnedSslCertificate = getArguments().getBoolean("Pinned_SSL_Certificate");
        pinnedIpAddresses = getArguments().getBoolean("Pinned_IP_Addresses");

        // Set the favorite icon as the dialog icon if it exists.
        if (MainWebViewActivity.favoriteIconBitmap.equals(MainWebViewActivity.favoriteIconDefaultBitmap)) {  // There is no favorite icon.
            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_dark);
            } else {
                dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_light);
            }
        } else {  // There is a favorite icon.
            // Create a drawable version of the favorite icon.
            Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), MainWebViewActivity.favoriteIconBitmap);

            // Set the icon.
            dialogBuilder.setIcon(favoriteIconDrawable);
        }

        // Setup the neutral button.
        dialogBuilder.setNeutralButton(R.string.update, (DialogInterface dialog, int which) -> {
            // Initialize the long date variables.  If the date is null, a long value of `0` will be stored in the Domains database entry.
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

            // Update the SSL certificate if it is pinned.
            if (pinnedSslCertificate) {
                // Update the pinned SSL certificate in the domain database.
                domainsDatabaseHelper.updatePinnedSslCertificate(MainWebViewActivity.domainSettingsDatabaseId, currentSslIssuedToCName, currentSslIssuedToOName, currentSslIssuedToUName,
                        currentSslIssuedByCName, currentSslIssuedByOName, currentSslIssuedByUName, currentSslStartDateLong, currentSslEndDateLong);

                // Update the pinned SSL certificate class variables to match the information that is now in the database.
                MainWebViewActivity.pinnedSslIssuedToCName = currentSslIssuedToCName;
                MainWebViewActivity.pinnedSslIssuedToOName = currentSslIssuedToOName;
                MainWebViewActivity.pinnedSslIssuedToUName = currentSslIssuedToUName;
                MainWebViewActivity.pinnedSslIssuedByCName = currentSslIssuedByCName;
                MainWebViewActivity.pinnedSslIssuedByOName = currentSslIssuedByOName;
                MainWebViewActivity.pinnedSslIssuedByUName = currentSslIssuedByUName;
                MainWebViewActivity.pinnedSslStartDate = currentSslStartDate;
                MainWebViewActivity.pinnedSslEndDate = currentSslEndDate;
            }

            // Update the IP addresses if they are pinned.
            if (pinnedIpAddresses) {
                // Update the pinned IP addresses in the domain database.
                domainsDatabaseHelper.updatePinnedIpAddresses(MainWebViewActivity.domainSettingsDatabaseId, MainWebViewActivity.currentHostIpAddresses);

                // Update the pinned IP addresses class variable to match the information that is now in the database.
                MainWebViewActivity.pinnedHostIpAddresses = MainWebViewActivity.currentHostIpAddresses;
            }
        });

        // Setup the negative button.
        dialogBuilder.setNegativeButton(R.string.back, (DialogInterface dialog, int which) -> {
            // Call the `onSslMismatchBack` public interface to send the `WebView` back one page.
            pinnedMismatchListener.onPinnedMismatchBack();
        });

        // Setup the positive button.
        dialogBuilder.setPositiveButton(R.string.proceed, (DialogInterface dialog, int which) -> {
            // Call the `onSslMismatchProceed` public interface.
            pinnedMismatchListener.onPinnedMismatchProceed();
        });

        // Set the title.
        dialogBuilder.setTitle(R.string.pinned_mismatch);

        // Set the layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.pinned_mismatch_linearlayout, null));

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
                return getString(R.string.current);
            } else {  // The pinned SSL certificate tab.
                return getString(R.string.pinned);
            }
        }

        @Override
        @NonNull
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            // Inflate the scroll view for this tab.
            ViewGroup tabViewGroup = (ViewGroup) layoutInflater.inflate(R.layout.pinned_mismatch_scrollview, container, false);

            // Get handles for the `TextViews`.
            TextView domainNameTextView = tabViewGroup.findViewById(R.id.domain_name);
            TextView ipAddressesTextView = tabViewGroup.findViewById(R.id.ip_addresses);
            TextView issuedToCNameTextView = tabViewGroup.findViewById(R.id.issued_to_cname);
            TextView issuedToONameTextView = tabViewGroup.findViewById(R.id.issued_to_oname);
            TextView issuedToUNameTextView = tabViewGroup.findViewById(R.id.issued_to_uname);
            TextView issuedByCNameTextView = tabViewGroup.findViewById(R.id.issued_by_cname);
            TextView issuedByONameTextView = tabViewGroup.findViewById(R.id.issued_by_oname);
            TextView issuedByUNameTextView = tabViewGroup.findViewById(R.id.issued_by_uname);
            TextView startDateTextView = tabViewGroup.findViewById(R.id.start_date);
            TextView endDateTextView = tabViewGroup.findViewById(R.id.end_date);

            // Setup the labels.
            String domainNameLabel = getString(R.string.domain_label) + "  ";
            String ipAddressesLabel = getString(R.string.ip_addresses) + "  ";
            String cNameLabel = getString(R.string.common_name) + "  ";
            String oNameLabel = getString(R.string.organization) + "  ";
            String uNameLabel = getString(R.string.organizational_unit) + "  ";
            String startDateLabel = getString(R.string.start_date) + "  ";
            String endDateLabel = getString(R.string.end_date) + "  ";

            // Get a URI for the URL.
            Uri currentUri = Uri.parse(MainWebViewActivity.formattedUrlString);

            // Get the current host from the URI.
            String domainName = currentUri.getHost();

            // Get the current website SSL certificate.
            SslCertificate sslCertificate = MainWebViewActivity.sslCertificate;

            // Extract the individual pieces of information from the current website SSL certificate if it is not null.
            if (sslCertificate != null) {
                currentSslIssuedToCName = sslCertificate.getIssuedTo().getCName();
                currentSslIssuedToOName = sslCertificate.getIssuedTo().getOName();
                currentSslIssuedToUName = sslCertificate.getIssuedTo().getUName();
                currentSslIssuedByCName = sslCertificate.getIssuedBy().getCName();
                currentSslIssuedByOName = sslCertificate.getIssuedBy().getOName();
                currentSslIssuedByUName = sslCertificate.getIssuedBy().getUName();
                currentSslStartDate = sslCertificate.getValidNotBeforeDate();
                currentSslEndDate = sslCertificate.getValidNotAfterDate();
            } else {
                // Initialize the current website SSL certificate variables with blank information.
                currentSslIssuedToCName = "";
                currentSslIssuedToOName = "";
                currentSslIssuedToUName = "";
                currentSslIssuedByCName = "";
                currentSslIssuedByOName = "";
                currentSslIssuedByUName = "";
            }

            // Setup the domain name spannable string builder.
            SpannableStringBuilder domainNameStringBuilder = new SpannableStringBuilder(domainNameLabel + domainName);

            // Initialize the spannable string builders.
            SpannableStringBuilder ipAddressesStringBuilder;
            SpannableStringBuilder issuedToCNameStringBuilder;
            SpannableStringBuilder issuedToONameStringBuilder;
            SpannableStringBuilder issuedToUNameStringBuilder;
            SpannableStringBuilder issuedByCNameStringBuilder;
            SpannableStringBuilder issuedByONameStringBuilder;
            SpannableStringBuilder issuedByUNameStringBuilder;
            SpannableStringBuilder startDateStringBuilder;
            SpannableStringBuilder endDateStringBuilder;

            // Setup the spannable string builders for each tab.
            if (position == 0) {  // Setup the current settings tab.
                // Create the string builders.
                ipAddressesStringBuilder = new SpannableStringBuilder(ipAddressesLabel + MainWebViewActivity.currentHostIpAddresses);
                issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + currentSslIssuedToCName);
                issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + currentSslIssuedToOName);
                issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + currentSslIssuedToUName);
                issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + currentSslIssuedByCName);
                issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + currentSslIssuedByOName);
                issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + currentSslIssuedByUName);

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
            } else {  // Setup the pinned settings tab.
                // Create the string builders.
                ipAddressesStringBuilder = new SpannableStringBuilder(ipAddressesLabel + MainWebViewActivity.pinnedHostIpAddresses);
                issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + MainWebViewActivity.pinnedSslIssuedToCName);
                issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + MainWebViewActivity.pinnedSslIssuedToOName);
                issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + MainWebViewActivity.pinnedSslIssuedToUName);
                issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + MainWebViewActivity.pinnedSslIssuedByCName);
                issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + MainWebViewActivity.pinnedSslIssuedByOName);
                issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + MainWebViewActivity.pinnedSslIssuedByUName);

                // Set the dates if they aren't `null`.
                if (MainWebViewActivity.pinnedSslStartDate == null) {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel);
                } else {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG)
                            .format(MainWebViewActivity.pinnedSslStartDate));
                }

                if (MainWebViewActivity.pinnedSslEndDate == null) {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel);
                } else {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(MainWebViewActivity.pinnedSslEndDate));
                }
            }

            // Create a red foreground color span.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
            @SuppressWarnings("deprecation") ForegroundColorSpan redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));

            // Create a blue foreground color span.
            ForegroundColorSpan blueColorSpan;

            // Set the blue color span according to the theme.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
            if (MainWebViewActivity.darkTheme) {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
            } else {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
            }

            // Set the domain name to be blue.
            domainNameStringBuilder.setSpan(blueColorSpan, domainNameLabel.length(), domainNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Color coordinate the IP addresses if they are pinned.
            if (pinnedIpAddresses) {
                if (MainWebViewActivity.currentHostIpAddresses.equals(MainWebViewActivity.pinnedHostIpAddresses)) {
                    ipAddressesStringBuilder.setSpan(blueColorSpan, ipAddressesLabel.length(), ipAddressesStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    ipAddressesStringBuilder.setSpan(redColorSpan, ipAddressesLabel.length(), ipAddressesStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            // Color coordinate the SSL certificate fields if they are pinned.
            if (pinnedSslCertificate) {
                if (currentSslIssuedToCName.equals(MainWebViewActivity.pinnedSslIssuedToCName)) {
                    issuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedToOName.equals(MainWebViewActivity.pinnedSslIssuedToOName)) {
                    issuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedToONameStringBuilder.setSpan(redColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedToUName.equals(MainWebViewActivity.pinnedSslIssuedToUName)) {
                    issuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedToUNameStringBuilder.setSpan(redColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedByCName.equals(MainWebViewActivity.pinnedSslIssuedByCName)) {
                    issuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedByCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedByOName.equals(MainWebViewActivity.pinnedSslIssuedByOName)) {
                    issuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedByONameStringBuilder.setSpan(redColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedByUName.equals(MainWebViewActivity.pinnedSslIssuedByUName)) {
                    issuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedByUNameStringBuilder.setSpan(redColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if ((currentSslStartDate != null) && currentSslStartDate.equals(MainWebViewActivity.pinnedSslStartDate)) {
                    startDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    startDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if ((currentSslEndDate != null) && currentSslEndDate.equals(MainWebViewActivity.pinnedSslEndDate)) {
                    endDateStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    endDateStringBuilder.setSpan(redColorSpan, endDateLabel.length(), endDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            // Display the strings.
            domainNameTextView.setText(domainNameStringBuilder);
            ipAddressesTextView.setText(ipAddressesStringBuilder);
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
