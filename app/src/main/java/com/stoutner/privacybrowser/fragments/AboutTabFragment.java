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

package com.stoutner.privacybrowser.fragments;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Date;

public class AboutTabFragment extends Fragment {
    private int tabNumber;

    // Store the tab number in the arguments bundle.
    public static AboutTabFragment createTab(int tab) {
        // Create a bundle.
        Bundle bundle = new Bundle();

        // Store the tab number in the bundle.
        bundle.putInt("Tab", tab);

        // Add the bundle to the fragment.
        AboutTabFragment aboutTabFragment = new AboutTabFragment();
        aboutTabFragment.setArguments(bundle);

        // Return the new fragment.
        return aboutTabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Remove the lint warning that `getArguments()` might be null.
        assert getArguments() != null;

        // Store the tab number in a class variable.
        tabNumber = getArguments().getInt("Tab");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View tabLayout;

        // Load the tabs.  Tab numbers start at 0.
        if (tabNumber == 0) {  // Load the about tab.
            // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.  The fragment will take care of attaching the root automatically.
            tabLayout = inflater.inflate(R.layout.about_tab_version, container, false);

            // Get handles for the `TextViews`.
            TextView versionNumberTextView = tabLayout.findViewById(R.id.about_version_number);
            TextView versionBrandTextView = tabLayout.findViewById(R.id.about_version_brand);
            TextView versionManufacturerTextView = tabLayout.findViewById(R.id.about_version_manufacturer);
            TextView versionModelTextView = tabLayout.findViewById(R.id.about_version_model);
            TextView versionDeviceTextView = tabLayout.findViewById(R.id.about_version_device);
            TextView versionBootloaderTextView = tabLayout.findViewById(R.id.about_version_bootloader);
            TextView versionRadioTextView = tabLayout.findViewById(R.id.about_version_radio);
            TextView versionAndroidTextView = tabLayout.findViewById(R.id.about_version_android);
            TextView versionSecurityPatchTextView = tabLayout.findViewById(R.id.about_version_securitypatch);
            TextView versionBuildTextView = tabLayout.findViewById(R.id.about_version_build);
            TextView versionWebKitTextView = tabLayout.findViewById(R.id.about_version_webkit);
            TextView versionChromeTextView = tabLayout.findViewById(R.id.about_version_chrome);
            TextView versionOrbotTextView = tabLayout.findViewById(R.id.about_version_orbot);
            TextView versionEasyListTextView = tabLayout.findViewById(R.id.about_version_easylist);
            TextView versionEasyPrivacyTextView = tabLayout.findViewById(R.id.about_version_easyprivacy);
            TextView versionFanboyAnnoyanceTextView = tabLayout.findViewById(R.id.about_version_fanboy_annoyance);
            TextView versionFanboySocialTextView = tabLayout.findViewById(R.id.about_version_fanboy_social);
            TextView certificateIssuerDNTextView = tabLayout.findViewById(R.id.about_version_certificate_issuer_dn);
            TextView certificateSubjectDNTextView = tabLayout.findViewById(R.id.about_version_certificate_subject_dn);
            TextView certificateStartDateTextView = tabLayout.findViewById(R.id.about_version_certificate_start_date);
            TextView certificateEndDateTextView = tabLayout.findViewById(R.id.about_version_certificate_end_date);
            TextView certificateVersionTextView = tabLayout.findViewById(R.id.about_version_certificate_version);
            TextView certificateSerialNumberTextView = tabLayout.findViewById(R.id.about_version_certificate_serial_number);
            TextView certificateSignatureAlgorithmTextView = tabLayout.findViewById(R.id.about_version_certificate_signature_algorithm);

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
            String easyListLabel = getString(R.string.easylist_label) + "  ";
            String easyPrivacyLabel = getString(R.string.easyprivacy_label) + "  ";
            String fanboyAnnoyanceLabel = getString(R.string.fanboy_annoyance_label) + "  ";
            String fanboySocialLabel = getString(R.string.fanboy_social_label) + "  ";
            String issuerDNLabel = getString(R.string.issuer_dn) + "  ";
            String subjectDNLabel = getString(R.string.subject_dn) + "  ";
            String startDateLabel = getString(R.string.start_date) + "  ";
            String endDateLabel = getString(R.string.end_date) + "  ";
            String certificateVersionLabel = getString(R.string.certificate_version) + "  ";
            String serialNumberLabel = getString(R.string.serial_number) + "  ";
            String signatureAlgorithmLabel = getString(R.string.signature_algorithm) + "  ";

            // `webViewLayout` is only used to get the default user agent from `bare_webview`.  It is not used to render content on the screen.
            View webViewLayout = inflater.inflate(R.layout.bare_webview, container, false);
            WebView tabLayoutWebView = webViewLayout.findViewById(R.id.bare_webview);
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

            // Get the Orbot version name if Orbot is installed.
            String orbot;
            try {
                // Remove the lint warning that `getContext()` might be null.
                assert getContext() != null;

                // Store the version name.
                orbot = getContext().getPackageManager().getPackageInfo("org.torproject.android", PackageManager.GET_CONFIGURATIONS).versionName;
            } catch (PackageManager.NameNotFoundException e) {  // Orbot is not installed.
                orbot = "";
            }

            // Create a `SpannableStringBuilder` for the hardware and software `TextViews` that needs multiple colors of text.
            SpannableStringBuilder brandStringBuilder = new SpannableStringBuilder(brandLabel + brand);
            SpannableStringBuilder manufacturerStringBuilder = new SpannableStringBuilder(manufacturerLabel + manufacturer);
            SpannableStringBuilder modelStringBuilder = new SpannableStringBuilder(modelLabel + model);
            SpannableStringBuilder deviceStringBuilder = new SpannableStringBuilder(deviceLabel + device);
            SpannableStringBuilder bootloaderStringBuilder = new SpannableStringBuilder(bootloaderLabel + bootloader);
            SpannableStringBuilder androidStringBuilder = new SpannableStringBuilder(androidLabel + android);
            SpannableStringBuilder buildStringBuilder = new SpannableStringBuilder(buildLabel + build);
            SpannableStringBuilder webKitStringBuilder = new SpannableStringBuilder(webKitLabel + webKit);
            SpannableStringBuilder chromeStringBuilder = new SpannableStringBuilder(chromeLabel + chrome);
            SpannableStringBuilder easyListStringBuilder = new SpannableStringBuilder(easyListLabel + MainWebViewActivity.easyListVersion);
            SpannableStringBuilder easyPrivacyStringBuilder = new SpannableStringBuilder(easyPrivacyLabel + MainWebViewActivity.easyPrivacyVersion);
            SpannableStringBuilder fanboyAnnoyanceStringBuilder = new SpannableStringBuilder(fanboyAnnoyanceLabel + MainWebViewActivity.fanboysAnnoyanceVersion);
            SpannableStringBuilder fanboySocialStringBuilder = new SpannableStringBuilder(fanboySocialLabel + MainWebViewActivity.fanboysSocialVersion);

            // Create the `blueColorSpan` variable.
            ForegroundColorSpan blueColorSpan;

            // Set `blueColorSpan` according to the theme.  We have to use the deprecated `getColor()` until API >= 23.
            if (MainWebViewActivity.darkTheme) {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
            } else {
                //noinspection deprecation
                blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
            }

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
            easyListStringBuilder.setSpan(blueColorSpan, easyListLabel.length(), easyListStringBuilder.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            easyPrivacyStringBuilder.setSpan(blueColorSpan, easyPrivacyLabel.length(), easyPrivacyStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            fanboyAnnoyanceStringBuilder.setSpan(blueColorSpan, fanboyAnnoyanceLabel.length(), fanboyAnnoyanceStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            fanboySocialStringBuilder.setSpan(blueColorSpan, fanboySocialLabel.length(), fanboySocialStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Display the strings in the text boxes.
            versionNumberTextView.setText(version);
            versionBrandTextView.setText(brandStringBuilder);
            versionManufacturerTextView.setText(manufacturerStringBuilder);
            versionModelTextView.setText(modelStringBuilder);
            versionDeviceTextView.setText(deviceStringBuilder);
            versionBootloaderTextView.setText(bootloaderStringBuilder);
            versionAndroidTextView.setText(androidStringBuilder);
            versionBuildTextView.setText(buildStringBuilder);
            versionWebKitTextView.setText(webKitStringBuilder);
            versionChromeTextView.setText(chromeStringBuilder);
            versionEasyListTextView.setText(easyListStringBuilder);
            versionEasyPrivacyTextView.setText(easyPrivacyStringBuilder);
            versionFanboyAnnoyanceTextView.setText(fanboyAnnoyanceStringBuilder);
            versionFanboySocialTextView.setText(fanboySocialStringBuilder);

            // Build.VERSION.SECURITY_PATCH is only available for SDK_INT >= 23.
            if (Build.VERSION.SDK_INT >= 23) {
                String securityPatchLabel = getString(R.string.security_patch) + "  ";
                String securityPatch = Build.VERSION.SECURITY_PATCH;
                SpannableStringBuilder securityPatchStringBuilder = new SpannableStringBuilder(securityPatchLabel + securityPatch);
                securityPatchStringBuilder.setSpan(blueColorSpan, securityPatchLabel.length(), securityPatchStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                versionSecurityPatchTextView.setText(securityPatchStringBuilder);
            } else {  // SDK_INT < 23, so `versionSecurityPatchTextView` should be hidden.
                versionSecurityPatchTextView.setVisibility(View.GONE);
            }

            // Only populate `versionRadioTextView` if there is a radio in the device.
            if (!radio.equals("")) {
                String radioLabel = getString(R.string.radio) + "  ";
                SpannableStringBuilder radioStringBuilder = new SpannableStringBuilder(radioLabel + radio);
                radioStringBuilder.setSpan(blueColorSpan, radioLabel.length(), radioStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                versionRadioTextView.setText(radioStringBuilder);
            } else {  // This device does not have a radio, so `versionRadioTextView` should be hidden.
                versionRadioTextView.setVisibility(View.GONE);
            }

            // Only populate `versionOrbotTextView` if Orbot is installed.
            if (!orbot.equals("")) {
                String orbotLabel = getString(R.string.orbot) + "  ";
                SpannableStringBuilder orbotStringBuilder = new SpannableStringBuilder(orbotLabel + orbot);
                orbotStringBuilder.setSpan(blueColorSpan, orbotLabel.length(), orbotStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                versionOrbotTextView.setText(orbotStringBuilder);
            } else {  // Orbot is not installed, so the `versionOrbotTextView` should be hidden.
                versionOrbotTextView.setVisibility(View.GONE);
            }

            // Display the package signature.
            try {
                // Get the first package signature.  Suppress the lint warning about the need to be careful in implementing comparison of certificates for security purposes.
                @SuppressLint("PackageManagerGetSignatures") Signature packageSignature = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_SIGNATURES).signatures[0];

                // Convert the signature to a `byte[]` `InputStream`.
                InputStream certificateByteArrayInputStream = new ByteArrayInputStream(packageSignature.toByteArray());

                // Display the certificate information on the screen.
                try {
                    // Instantiate a `CertificateFactory`.
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");

                    // Generate an `X509Certificate`.
                    X509Certificate x509Certificate = (X509Certificate) certificateFactory.generateCertificate(certificateByteArrayInputStream);

                    // Store the individual sections of the certificate that we are interested in.
                    Principal issuerDNPrincipal = x509Certificate.getIssuerDN();
                    Principal subjectDNPrincipal = x509Certificate.getSubjectDN();
                    Date startDate = x509Certificate.getNotBefore();
                    Date endDate = x509Certificate.getNotAfter();
                    int certificateVersion = x509Certificate.getVersion();
                    BigInteger serialNumberBigInteger = x509Certificate.getSerialNumber();
                    String signatureAlgorithmNameString = x509Certificate.getSigAlgName();

                    // Create a `SpannableStringBuilder` for each `TextView` that needs multiple colors of text.
                    SpannableStringBuilder issuerDNStringBuilder = new SpannableStringBuilder(issuerDNLabel + issuerDNPrincipal.toString());
                    SpannableStringBuilder subjectDNStringBuilder = new SpannableStringBuilder(subjectDNLabel + subjectDNPrincipal.toString());
                    SpannableStringBuilder startDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(startDate));
                    SpannableStringBuilder endDataStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(endDate));
                    SpannableStringBuilder certificateVersionStringBuilder = new SpannableStringBuilder(certificateVersionLabel + certificateVersion);
                    SpannableStringBuilder serialNumberStringBuilder = new SpannableStringBuilder(serialNumberLabel + serialNumberBigInteger);
                    SpannableStringBuilder signatureAlgorithmStringBuilder = new SpannableStringBuilder(signatureAlgorithmLabel + signatureAlgorithmNameString);

                    // Setup the spans to display the device information in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
                    issuerDNStringBuilder.setSpan(blueColorSpan, issuerDNLabel.length(), issuerDNStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    subjectDNStringBuilder.setSpan(blueColorSpan, subjectDNLabel.length(), subjectDNStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    startDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), startDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    endDataStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), endDataStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    certificateVersionStringBuilder.setSpan(blueColorSpan, certificateVersionLabel.length(), certificateVersionStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    serialNumberStringBuilder.setSpan(blueColorSpan, serialNumberLabel.length(), serialNumberStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    signatureAlgorithmStringBuilder.setSpan(blueColorSpan, signatureAlgorithmLabel.length(), signatureAlgorithmStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                    // Display the strings in the text boxes.
                    certificateIssuerDNTextView.setText(issuerDNStringBuilder);
                    certificateSubjectDNTextView.setText(subjectDNStringBuilder);
                    certificateStartDateTextView.setText(startDateStringBuilder);
                    certificateEndDateTextView.setText(endDataStringBuilder);
                    certificateVersionTextView.setText(certificateVersionStringBuilder);
                    certificateSerialNumberTextView.setText(serialNumberStringBuilder);
                    certificateSignatureAlgorithmTextView.setText(signatureAlgorithmStringBuilder);
                } catch (CertificateException e) {
                    // Do nothing if there is a certificate error.
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Do nothing if `PackageManager` says Privacy Browser isn't installed.
            }
        } else { // load a `WebView` for all the other tabs.  Tab numbers start at 0.
            // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.  The fragment will take care of attaching the root automatically.
            tabLayout = inflater.inflate(R.layout.bare_webview, container, false);

            // Get a handle for `tabWebView`.
            WebView tabWebView = (WebView) tabLayout;

            // Load the tabs according to the theme.
            if (MainWebViewActivity.darkTheme) {  // The dark theme is applied.
                // Set the background color.  We have to use the deprecated `.getColor()` until API >= 23.
                //noinspection deprecation
                tabWebView.setBackgroundColor(getResources().getColor(R.color.gray_850));

                switch (tabNumber) {
                    case 1:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_permissions_dark.html");
                        break;

                    case 2:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_privacy_policy_dark.html");
                        break;

                    case 3:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_changelog_dark.html");
                        break;

                    case 4:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_licenses_dark.html");
                        break;

                    case 5:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_contributors_dark.html");
                        break;

                    case 6:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_links_dark.html");
                        break;
                }
            } else {  // The light theme is applied.
                switch (tabNumber) {
                    case 1:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_permissions_light.html");
                        break;

                    case 2:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_privacy_policy_light.html");
                        break;

                    case 3:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_changelog_light.html");
                        break;

                    case 4:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_licenses_light.html");
                        break;

                    case 5:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_contributors_light.html");
                        break;

                    case 6:
                        tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/about_links_light.html");
                        break;
                }
            }
        }

        // Return the formatted `tabLayout`.
        return tabLayout;
    }
}