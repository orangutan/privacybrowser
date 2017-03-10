/*
 * Copyright 2016-2017 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.stoutner.privacybrowser.BuildConfig;
import com.stoutner.privacybrowser.R;

public class AboutTabFragment extends Fragment {
    private int tabNumber;

    // AboutTabFragment.createTab stores the tab number in the bundle arguments so it can be referenced from onCreate().
    public static AboutTabFragment createTab(int tab) {
        Bundle thisTabArguments = new Bundle();
        thisTabArguments.putInt("Tab", tab);

        AboutTabFragment thisTab = new AboutTabFragment();
        thisTab.setArguments(thisTabArguments);
        return thisTab;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the tab number in tabNumber.
        tabNumber = getArguments().getInt("Tab");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View tabLayout;

        // Load the tabs.  Tab numbers start at 0.
        if (tabNumber == 0) {  // Load the about tab.
            // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.
            // The fragment will take care of attaching the root automatically.
            tabLayout = inflater.inflate(R.layout.about_tab_version, container, false);

            // Get handles for the `TextViews`.
            TextView versionNumberTextView = (TextView) tabLayout.findViewById(R.id.about_version_number);
            TextView versionBrandTextView = (TextView) tabLayout.findViewById(R.id.about_version_brand);
            TextView versionManufacturerTextView = (TextView) tabLayout.findViewById(R.id.about_version_manufacturer);
            TextView versionModelTextView = (TextView) tabLayout.findViewById(R.id.about_version_model);
            TextView versionDeviceTextView = (TextView) tabLayout.findViewById(R.id.about_version_device);
            TextView versionBootloaderTextView = (TextView) tabLayout.findViewById(R.id.about_version_bootloader);
            TextView versionRadioTextView = (TextView) tabLayout.findViewById(R.id.about_version_radio);
            TextView versionAndroidTextView = (TextView) tabLayout.findViewById(R.id.about_version_android);
            TextView versionBuildTextView = (TextView) tabLayout.findViewById(R.id.about_version_build);
            TextView versionSecurityPatchTextView = (TextView) tabLayout.findViewById(R.id.about_version_securitypatch);
            TextView versionWebKitTextView = (TextView) tabLayout.findViewById(R.id.about_version_webkit);
            TextView versionChromeText = (TextView) tabLayout.findViewById(R.id.about_version_chrome);

            // Setup the labels.
            String version = getString(R.string.version) + " " + BuildConfig.VERSION_NAME + " (" + getString(R.string.version_code) + " " + Integer.toString(BuildConfig.VERSION_CODE) + ")";
            String brandLabel = getString(R.string.brand) + "  ";
            String manufacturerLabel = getString(R.string.manufacturer) + "  ";
            String modelLabel = getString(R.string.model) + "  ";
            String deviceLabel = getString(R.string.device) + "  ";
            String bootloaderLabel = getString(R.string.bootloader) + "  ";
            String androidLabel = getString(R.string.android) + "  ";
            String buildLabel = getString(R.string.build) + "  ";
            String webKitLabel = getString(R.string.webkit) + "  ";
            String chromeLabel = getString(R.string.chrome) + "  ";

            // `webViewLayout` is only used to get the default user agent from `bare_webview`.  It is not used to render content on the screen.
            View webViewLayout = inflater.inflate(R.layout.bare_webview, container, false);
            WebView tabLayoutWebView = (WebView) webViewLayout.findViewById(R.id.bare_webview);
            String userAgentString =  tabLayoutWebView.getSettings().getUserAgentString();

            // Get the device's information and store it in strings.
            String brand = Build.BRAND;
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            String device = Build.DEVICE;
            String bootloader = Build.BOOTLOADER;
            String radio = Build.getRadioVersion();
            String android = Build.VERSION.RELEASE + " (" + getString(R.string.api) + " " + Integer.toString(Build.VERSION.SDK_INT) + ")";
            String build = Build.DISPLAY;
            // Select the substring that begins after "Safari/" and goes to the end of the string.
            String webKit = userAgentString.substring(userAgentString.indexOf("Safari/") + 7);
            // Select the substring that begins after "Chrome/" and goes until the next " ".
            String chrome = userAgentString.substring(userAgentString.indexOf("Chrome/") + 7, userAgentString.indexOf(" ", userAgentString.indexOf("Chrome/")));

            // Create a `SpannableStringBuilder` for each `TextView` that needs multiple colors of text.
            SpannableStringBuilder brandStringBuilder = new SpannableStringBuilder(brandLabel + brand);
            SpannableStringBuilder manufacturerStringBuilder = new SpannableStringBuilder(manufacturerLabel + manufacturer);
            SpannableStringBuilder modelStringBuilder = new SpannableStringBuilder(modelLabel + model);
            SpannableStringBuilder deviceStringBuilder = new SpannableStringBuilder(deviceLabel + device);
            SpannableStringBuilder bootloaderStringBuilder = new SpannableStringBuilder(bootloaderLabel + bootloader);
            SpannableStringBuilder androidStringBuilder = new SpannableStringBuilder(androidLabel + android);
            SpannableStringBuilder buildStringBuilder = new SpannableStringBuilder(buildLabel + build);
            SpannableStringBuilder webKitStringBuilder = new SpannableStringBuilder(webKitLabel + webKit);
            SpannableStringBuilder chromeStringBuilder = new SpannableStringBuilder(chromeLabel + chrome);

            // Create a blue `ForegroundColorSpan`.  We have to use the deprecated `getColor` until API >= 23.
            @SuppressWarnings("deprecation") ForegroundColorSpan blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));

            // Setup the spans to display the device information in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            brandStringBuilder.setSpan(blueColorSpan, brandLabel.length(), brandStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            manufacturerStringBuilder.setSpan(blueColorSpan, manufacturerLabel.length(), manufacturerStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            modelStringBuilder.setSpan(blueColorSpan, modelLabel.length(), modelStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            deviceStringBuilder.setSpan(blueColorSpan, deviceLabel.length(), deviceStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            bootloaderStringBuilder.setSpan(blueColorSpan, bootloaderLabel.length(), bootloaderStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            androidStringBuilder.setSpan(blueColorSpan, androidLabel.length(), androidStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            buildStringBuilder.setSpan(blueColorSpan, buildLabel.length(), buildStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            webKitStringBuilder.setSpan(blueColorSpan, webKitLabel.length(), webKitStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            chromeStringBuilder.setSpan(blueColorSpan, chromeLabel.length(), chromeStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Display the strings.
            versionNumberTextView.setText(version);
            versionBrandTextView.setText(brandStringBuilder);
            versionManufacturerTextView.setText(manufacturerStringBuilder);
            versionModelTextView.setText(modelStringBuilder);
            versionDeviceTextView.setText(deviceStringBuilder);
            versionBootloaderTextView.setText(bootloaderStringBuilder);
            versionAndroidTextView.setText(androidStringBuilder);
            versionBuildTextView.setText(buildStringBuilder);
            versionWebKitTextView.setText(webKitStringBuilder);
            versionChromeText.setText(chromeStringBuilder);

            // Build.VERSION.SECURITY_PATCH is only available for SDK_INT >= 23.
            if (Build.VERSION.SDK_INT >= 23) {
                String securityPatchLabel = getString(R.string.security_patch) + "  ";
                String securityPatch = Build.VERSION.SECURITY_PATCH;
                SpannableStringBuilder securityPatchStringBuilder = new SpannableStringBuilder(securityPatchLabel + securityPatch);
                securityPatchStringBuilder.setSpan(blueColorSpan, securityPatchLabel.length(), securityPatchStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                versionSecurityPatchTextView.setText(securityPatchStringBuilder);
            } else { // Hide `versionSecurityPatchTextView`.
                versionSecurityPatchTextView.setVisibility(View.GONE);
            }

            // Only populate `versionRadioTextView` if there is a radio in the device.
            if (!radio.equals("")) {
                String radioLabel = getString(R.string.radio) + "  ";
                SpannableStringBuilder radioStringBuilder = new SpannableStringBuilder(radioLabel + radio);
                radioStringBuilder.setSpan(blueColorSpan, radioLabel.length(), radioStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                versionRadioTextView.setText(radioStringBuilder);
            } else { // Hide `versionRadioTextView`.
                versionRadioTextView.setVisibility(View.GONE);
            }
        } else { // load a WebView for all the other tabs.  Tab numbers start at 0.
            // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.
            // The fragment will take care of attaching the root automatically.
            tabLayout = inflater.inflate(R.layout.bare_webview, container, false);
            WebView tabWebView = (WebView) tabLayout;

            switch (tabNumber) {
                case 1:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_permissions.html");
                    break;

                case 2:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_privacy_policy.html");
                    break;

                case 3:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_changelog.html");
                    break;

                case 4:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_licenses.html");
                    break;

                case 5:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_contributors.html");
                    break;

                case 6:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_links.html");
                    break;

                default:
                    break;
            }
        }

        return tabLayout;
    }
}