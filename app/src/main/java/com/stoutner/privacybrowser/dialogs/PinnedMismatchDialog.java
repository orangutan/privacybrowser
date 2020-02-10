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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.fragments.WebViewTabFragment;
import com.stoutner.privacybrowser.views.NestedScrollWebView;
import com.stoutner.privacybrowser.views.WrapVerticalContentViewPager;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.
import androidx.viewpager.widget.PagerAdapter;

public class PinnedMismatchDialog extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface PinnedMismatchListener {
        void pinnedErrorGoBack();
    }

    // Declare the class variables.
    private PinnedMismatchListener pinnedMismatchListener;
    private NestedScrollWebView nestedScrollWebView;
    private String currentSslIssuedToCName;
    private String currentSslIssuedToOName;
    private String currentSslIssuedToUName;
    private String currentSslIssuedByCName;
    private String currentSslIssuedByOName;
    private String currentSslIssuedByUName;
    private Date currentSslStartDate;
    private Date currentSslEndDate;

    @Override
    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the listener from the launching context.
        pinnedMismatchListener = (PinnedMismatchListener) context;
    }

    public static PinnedMismatchDialog displayDialog(long webViewFragmentId) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the WebView fragment ID in the bundle.
        argumentsBundle.putLong("webview_fragment_id", webViewFragmentId);

        // Create a new instance of the pinned mismatch dialog.
        PinnedMismatchDialog pinnedMismatchDialog = new PinnedMismatchDialog();

        // Add the arguments bundle to the new instance.
        pinnedMismatchDialog.setArguments(argumentsBundle);

        // Make it so.
        return pinnedMismatchDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning below that `.getArguments().getInt()` might be null.
        assert arguments != null;

        // Get the current position of this WebView fragment.
        int webViewPosition = MainWebViewActivity.webViewPagerAdapter.getPositionForId(arguments.getLong("webview_fragment_id"));

        // Get the WebView tab fragment.
        WebViewTabFragment webViewTabFragment = MainWebViewActivity.webViewPagerAdapter.getPageFragment(webViewPosition);

        // Get the fragment view.
        View fragmentView = webViewTabFragment.getView();

        // Remove the incorrect lint warning below that the fragment view might be null.
        assert fragmentView != null;

        // Get a handle for the current WebView.
        nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Set the style according to the theme.
        if (darkTheme) {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Get the context.
        Context context = getContext();

        // Remove the incorrect lint warning below that the context might be null.
        assert context != null;

        // Get the favorite icon.
        Bitmap favoriteIconBitmap = nestedScrollWebView.getFavoriteOrDefaultIcon();

        // Get the default favorite icon drawable.  `ContextCompat` must be used until API >= 21.
        Drawable defaultFavoriteIconDrawable = ContextCompat.getDrawable(context, R.drawable.world);

        // Cast the favorite icon drawable to a bitmap drawable.
        BitmapDrawable defaultFavoriteIconBitmapDrawable = (BitmapDrawable) defaultFavoriteIconDrawable;

        // Remove the incorrect warning below that the favorite icon bitmap drawable might be null.
        assert defaultFavoriteIconBitmapDrawable != null;

        // Store the default icon bitmap.
        Bitmap defaultFavoriteIconBitmap = defaultFavoriteIconBitmapDrawable.getBitmap();

        // Set the favorite icon as the dialog icon if it exists.
        if (favoriteIconBitmap.sameAs(defaultFavoriteIconBitmap)) {  // There is no website favorite icon.
            // Set the icon according to the theme.
            if (darkTheme) {
                dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_dark);
            } else {
                dialogBuilder.setIcon(R.drawable.ssl_certificate_enabled_light);
            }
        } else {  // There is a favorite icon.
            // Create a drawable version of the favorite icon.
            Drawable favoriteIconDrawable = new BitmapDrawable(getResources(), favoriteIconBitmap);

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
            DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(context, null, null, 0);

            // Update the SSL certificate if it is pinned.
            if (nestedScrollWebView.hasPinnedSslCertificate()) {
                // Update the pinned SSL certificate in the domain database.
                domainsDatabaseHelper.updatePinnedSslCertificate(nestedScrollWebView.getDomainSettingsDatabaseId(), currentSslIssuedToCName, currentSslIssuedToOName, currentSslIssuedToUName,
                        currentSslIssuedByCName, currentSslIssuedByOName, currentSslIssuedByUName, currentSslStartDateLong, currentSslEndDateLong);

                // Update the pinned SSL certificate in the nested scroll WebView.
                nestedScrollWebView.setPinnedSslCertificate(currentSslIssuedToCName, currentSslIssuedToOName, currentSslIssuedToUName, currentSslIssuedByCName, currentSslIssuedByOName, currentSslIssuedByUName,
                        currentSslStartDate, currentSslEndDate);
            }

            // Update the IP addresses if they are pinned.
            if (nestedScrollWebView.hasPinnedIpAddresses()) {
                // Update the pinned IP addresses in the domain database.
                domainsDatabaseHelper.updatePinnedIpAddresses(nestedScrollWebView.getDomainSettingsDatabaseId(), nestedScrollWebView.getCurrentIpAddresses());

                // Update the pinned IP addresses in the nested scroll WebView.
                nestedScrollWebView.setPinnedIpAddresses(nestedScrollWebView.getCurrentIpAddresses());
            }
        });

        // Setup the back button.
        dialogBuilder.setNegativeButton(R.string.back, (DialogInterface dialog, int which) -> {
            if (nestedScrollWebView.canGoBack()) {  // There is a back page in the history.
                // Invoke the navigate history listener in the calling activity.  These commands cannot be run here because they need access to `applyDomainSettings()`.
                pinnedMismatchListener.pinnedErrorGoBack();
            } else {  // There are no pages to go back to.
                // Load a blank page
                nestedScrollWebView.loadUrl("");
            }
        });

        // Setup the proceed button.
        dialogBuilder.setPositiveButton(R.string.proceed, (DialogInterface dialog, int which) -> {
            // Do not check the pinned information for this domain again until the domain changes.
            nestedScrollWebView.setIgnorePinnedDomainInformation(true);
        });

        // Set the title.
        dialogBuilder.setTitle(R.string.pinned_mismatch);

        // Remove the incorrect lint warning below that `getLayoutInflater()` might be null.
        assert getActivity() != null;

        // Set the layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
        // For some reason, `getLayoutInflater()` without `getActivity()` produces an endless loop (probably a bug that will be fixed at some point in the future).
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.pinned_mismatch_linearlayout, null));

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
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
            ViewGroup tabViewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.pinned_mismatch_scrollview, container, false);

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

            // Convert the URL to a URI.
            Uri currentUri = Uri.parse(nestedScrollWebView.getUrl());

            // Get the current host from the URI.
            String domainName = currentUri.getHost();

            // Get the current website SSL certificate.
            SslCertificate sslCertificate = nestedScrollWebView.getCertificate();

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

            // Get the pinned SSL certificate.
            ArrayList<Object> pinnedSslCertificateArrayList = nestedScrollWebView.getPinnedSslCertificate();

            // Extract the arrays from the array list.
            String[] pinnedSslCertificateStringArray = (String[]) pinnedSslCertificateArrayList.get(0);
            Date[] pinnedSslCertificateDateArray = (Date[]) pinnedSslCertificateArrayList.get(1);

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
                ipAddressesStringBuilder = new SpannableStringBuilder(ipAddressesLabel + nestedScrollWebView.getCurrentIpAddresses());
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
                ipAddressesStringBuilder = new SpannableStringBuilder(ipAddressesLabel + nestedScrollWebView.getPinnedIpAddresses());
                issuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + pinnedSslCertificateStringArray[0]);
                issuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + pinnedSslCertificateStringArray[1]);
                issuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + pinnedSslCertificateStringArray[2]);
                issuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + pinnedSslCertificateStringArray[3]);
                issuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + pinnedSslCertificateStringArray[4]);
                issuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + pinnedSslCertificateStringArray[5]);

                // Set the dates if they aren't `null`.
                if (pinnedSslCertificateDateArray[0] == null) {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel);
                } else {
                    startDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(pinnedSslCertificateDateArray[0]));
                }

                if (pinnedSslCertificateDateArray[1] == null) {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel);
                } else {
                    endDateStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(pinnedSslCertificateDateArray[1]));
                }
            }

            // Get a handle for the shared preferences.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            // Get the screenshot and theme preferences.
            boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

            // Create a red foreground color span.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
            ForegroundColorSpan redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));

            // Create a blue foreground color span.
            ForegroundColorSpan blueColorSpan;

            // Set the blue color span according to the theme.  The deprecated `getResources().getColor` must be used until the minimum API >= 23.
            if (darkTheme) {
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
            } else {
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
            }

            // Set the domain name to be blue.
            domainNameStringBuilder.setSpan(blueColorSpan, domainNameLabel.length(), domainNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Color coordinate the IP addresses if they are pinned.
            if (nestedScrollWebView.hasPinnedIpAddresses()) {
                if (nestedScrollWebView.getCurrentIpAddresses().equals(nestedScrollWebView.getPinnedIpAddresses())) {
                    ipAddressesStringBuilder.setSpan(blueColorSpan, ipAddressesLabel.length(), ipAddressesStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    ipAddressesStringBuilder.setSpan(redColorSpan, ipAddressesLabel.length(), ipAddressesStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            // Color coordinate the SSL certificate fields if they are pinned.
            if (nestedScrollWebView.hasPinnedSslCertificate()) {
                if (currentSslIssuedToCName.equals(pinnedSslCertificateStringArray[0])) {
                    issuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedToOName.equals(pinnedSslCertificateStringArray[1])) {
                    issuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedToONameStringBuilder.setSpan(redColorSpan, oNameLabel.length(), issuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedToUName.equals(pinnedSslCertificateStringArray[2])) {
                    issuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedToUNameStringBuilder.setSpan(redColorSpan, uNameLabel.length(), issuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedByCName.equals(pinnedSslCertificateStringArray[3])) {
                    issuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedByCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), issuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedByOName.equals(pinnedSslCertificateStringArray[4])) {
                    issuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedByONameStringBuilder.setSpan(redColorSpan, oNameLabel.length(), issuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if (currentSslIssuedByUName.equals(pinnedSslCertificateStringArray[5])) {
                    issuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    issuedByUNameStringBuilder.setSpan(redColorSpan, uNameLabel.length(), issuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if ((currentSslStartDate != null) && currentSslStartDate.equals(pinnedSslCertificateDateArray[0])) {
                    startDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    startDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                if ((currentSslEndDate != null) && currentSslEndDate.equals(pinnedSslCertificateDateArray[1])) {
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