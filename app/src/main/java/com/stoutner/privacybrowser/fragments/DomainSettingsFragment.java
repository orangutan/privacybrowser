/*
 * Copyright © 2017 Soren Stoutner <soren@stoutner.com>.
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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.http.SslCertificate;
import android.os.Build;
import android.os.Bundle;
// We have to use `android.support.v4.app.Fragment` until minimum API >= 23.  Otherwise we cannot call `getContext()`.
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class DomainSettingsFragment extends Fragment {
    // `DATABASE_ID` is used by activities calling this fragment.
    public static final String DATABASE_ID = "database_id";

    // `databaseId` is public static so it can be accessed from `DomainsActivity`. It is also used in `onCreate()` and `onCreateView()`.
    public static int databaseId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the database id in `databaseId`.
        databaseId = getArguments().getInt(DATABASE_ID);
    }

    // We have to use the deprecated `getDrawable()` until the minimum API >= 21.
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate `domain_settings_fragment`.  `false` does not attach it to the root `container`.
        View domainSettingsView = inflater.inflate(R.layout.domain_settings_fragment, container, false);

        // Get a handle for the `Context` and the `Resources`.
        Context context = getContext();
        final Resources resources = getResources();

        // Get a handle for the shared preference.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Store the default settings.
        final String defaultUserAgentString = sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0");
        final String defaultCustomUserAgentString = sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0");
        String defaultFontSizeString = sharedPreferences.getString("default_font_size", "100");
        final boolean defaultDisplayWebpageImagesBoolean = sharedPreferences.getBoolean("display_website_images", true);
        final boolean defaultNightModeBoolean = sharedPreferences.getBoolean("night_mode", false);

        // Get handles for the views in the fragment.
        final EditText domainNameEditText = domainSettingsView.findViewById(R.id.domain_settings_name_edittext);
        final Switch javaScriptEnabledSwitch = domainSettingsView.findViewById(R.id.domain_settings_javascript_switch);
        final ImageView javaScriptImageView = domainSettingsView.findViewById(R.id.domain_settings_javascript_imageview);
        Switch firstPartyCookiesEnabledSwitch = domainSettingsView.findViewById(R.id.domain_settings_first_party_cookies_switch);
        final ImageView firstPartyCookiesImageView = domainSettingsView.findViewById(R.id.domain_settings_first_party_cookies_imageview);
        LinearLayout thirdPartyCookiesLinearLayout = domainSettingsView.findViewById(R.id.domain_settings_third_party_cookies_linearlayout);
        final Switch thirdPartyCookiesEnabledSwitch = domainSettingsView.findViewById(R.id.domain_settings_third_party_cookies_switch);
        final ImageView thirdPartyCookiesImageView = domainSettingsView.findViewById(R.id.domain_settings_third_party_cookies_imageview);
        final Switch domStorageEnabledSwitch = domainSettingsView.findViewById(R.id.domain_settings_dom_storage_switch);
        final ImageView domStorageImageView = domainSettingsView.findViewById(R.id.domain_settings_dom_storage_imageview);
        Switch formDataEnabledSwitch = domainSettingsView.findViewById(R.id.domain_settings_form_data_switch);
        final ImageView formDataImageView = domainSettingsView.findViewById(R.id.domain_settings_form_data_imageview);
        final Spinner userAgentSpinner = domainSettingsView.findViewById(R.id.domain_settings_user_agent_spinner);
        final TextView userAgentTextView = domainSettingsView.findViewById(R.id.domain_settings_user_agent_textview);
        final EditText customUserAgentEditText = domainSettingsView.findViewById(R.id.domain_settings_custom_user_agent_edittext);
        final Spinner fontSizeSpinner = domainSettingsView.findViewById(R.id.domain_settings_font_size_spinner);
        final TextView fontSizeTextView = domainSettingsView.findViewById(R.id.domain_settings_font_size_textview);
        final ImageView displayWebpageImagesImageView = domainSettingsView.findViewById(R.id.domain_settings_display_webpage_images_imageview);
        final Spinner displayWebpageImagesSpinner = domainSettingsView.findViewById(R.id.domain_settings_display_webpage_images_spinner);
        final TextView displayImagesTextView = domainSettingsView.findViewById(R.id.domain_settings_display_webpage_images_textview);
        final ImageView nightModeImageView = domainSettingsView.findViewById(R.id.domain_settings_night_mode_imageview);
        final Spinner nightModeSpinner = domainSettingsView.findViewById(R.id.domain_settings_night_mode_spinner);
        final TextView nightModeTextView = domainSettingsView.findViewById(R.id.domain_settings_night_mode_textview);
        final ImageView pinnedSslCertificateImageView = domainSettingsView.findViewById(R.id.domain_settings_pinned_ssl_certificate_imageview);
        Switch pinnedSslCertificateSwitch = domainSettingsView.findViewById(R.id.domain_settings_pinned_ssl_certificate_switch);
        final LinearLayout savedSslCertificateLinearLayout = domainSettingsView.findViewById(R.id.saved_ssl_certificate_linearlayout);
        final RadioButton savedSslCertificateRadioButton = domainSettingsView.findViewById(R.id.saved_ssl_certificate_radiobutton);
        final TextView savedSslCertificateIssuedToCNameTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_issued_to_cname);
        TextView savedSslCertificateIssuedToONameTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_issued_to_oname);
        TextView savedSslCertificateIssuedToUNameTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_issued_to_uname);
        TextView savedSslCertificateIssuedByCNameTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_issued_by_cname);
        TextView savedSslCertificateIssuedByONameTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_issued_by_oname);
        TextView savedSslCertificateIssuedByUNameTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_issued_by_uname);
        TextView savedSslCertificateStartDateTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_start_date);
        TextView savedSslCertificateEndDateTextView = domainSettingsView.findViewById(R.id.saved_ssl_certificate_end_date);
        final LinearLayout currentWebsiteCertificateLinearLayout = domainSettingsView.findViewById(R.id.current_website_certificate_linearlayout);
        final RadioButton currentWebsiteCertificateRadioButton = domainSettingsView.findViewById(R.id.current_website_certificate_radiobutton);
        final TextView currentWebsiteCertificateIssuedToCNameTextView = domainSettingsView.findViewById(R.id.current_website_certificate_issued_to_cname);
        TextView currentWebsiteCertificateIssuedToONameTextView = domainSettingsView.findViewById(R.id.current_website_certificate_issued_to_oname);
        TextView currentWebsiteCertificateIssuedToUNameTextView = domainSettingsView.findViewById(R.id.current_website_certificate_issued_to_uname);
        TextView currentWebsiteCertificateIssuedByCNameTextView = domainSettingsView.findViewById(R.id.current_website_certificate_issued_by_cname);
        TextView currentWebsiteCertificateIssuedByONameTextView = domainSettingsView.findViewById(R.id.current_website_certificate_issued_by_oname);
        TextView currentWebsiteCertificateIssuedByUNameTextView = domainSettingsView.findViewById(R.id.current_website_certificate_issued_by_uname);
        TextView currentWebsiteCertificateStartDateTextView = domainSettingsView.findViewById(R.id.current_website_certificate_start_date);
        TextView currentWebsiteCertificateEndDateTextView = domainSettingsView.findViewById(R.id.current_website_certificate_end_date);
        final TextView noCurrentWebsiteCertificateTextView = domainSettingsView.findViewById(R.id.no_current_website_certificate);

        // Setup the SSL certificate labels.
        final String cNameLabel = getString(R.string.common_name) + "  ";
        String oNameLabel = getString(R.string.organization) + "  ";
        String uNameLabel = getString(R.string.organizational_unit) + "  ";
        String startDateLabel = getString(R.string.start_date) + "  ";
        String endDateLabel = getString(R.string.end_date) + "  ";

        // Get the current website SSL certificate
        final SslCertificate currentWebsiteSslCertificate = MainWebViewActivity.sslCertificate;

        // Initialize the database handler.  The two `nulls` do not specify the database name or a `CursorFactory`.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(context, null, null, 0);

        // Get the database `Cursor` for this ID and move it to the first row.
        Cursor domainCursor = domainsDatabaseHelper.getCursorForId(databaseId);
        domainCursor.moveToFirst();

        // Save the `Cursor` entries as variables.
        String domainNameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
        final int javaScriptEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_JAVASCRIPT));
        int firstPartyCookiesEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES));
        int thirdPartyCookiesEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES));
        final int domStorageEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_DOM_STORAGE));
        int formDataEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FORM_DATA));
        final String currentUserAgentString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.USER_AGENT));
        int fontSizeInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE));
        int displayImagesInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.DISPLAY_IMAGES));
        int nightModeInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.NIGHT_MODE));
        int pinnedSslCertificateInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.PINNED_SSL_CERTIFICATE));
        final String savedSslCertificateIssuedToCNameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_COMMON_NAME));
        String savedSslCertificateIssuedToONameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATION));
        String savedSslCertificateIssuedToUNameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATIONAL_UNIT));
        String savedSslCertificateIssuedByCNameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_COMMON_NAME));
        String savedSslCertificateIssuedByONameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATION));
        String savedSslCertificateIssuedByUNameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATIONAL_UNIT));

        // Initialize the saved SSL certificate date variables.
        Date savedSslCertificateStartDate = null;
        Date savedSslCertificateEndDate = null;

        // Only get the saved SSL certificate dates from the cursor if they are not set to `0`.
        if (domainCursor.getLong(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)) != 0) {
            savedSslCertificateStartDate = new Date(domainCursor.getLong(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)));
        }

        if (domainCursor.getLong(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)) != 0) {
            savedSslCertificateEndDate = new Date(domainCursor.getLong(domainCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)));
        }

        // Create `ArrayAdapters` for the `Spinners`and their `entry values`.
        ArrayAdapter<CharSequence> userAgentArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_user_agent_entries, R.layout.domain_settings_spinner_item);
        final ArrayAdapter<CharSequence> userAgentEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_user_agent_entry_values, R.layout.domain_settings_spinner_item);
        ArrayAdapter<CharSequence> fontSizeArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_font_size_entries, R.layout.domain_settings_spinner_item);
        ArrayAdapter<CharSequence> fontSizeEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_font_size_entry_values, R.layout.domain_settings_spinner_item);
        final ArrayAdapter<CharSequence> displayImagesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.display_webpage_images_array, R.layout.domain_settings_spinner_item);
        ArrayAdapter<CharSequence> nightModeArrayAdapter = ArrayAdapter.createFromResource(context, R.array.night_mode_array, R.layout.domain_settings_spinner_item);

        // Set the `DropDownViewResource` on the `Spinners`.
        userAgentArrayAdapter.setDropDownViewResource(R.layout.domain_settings_spinner_dropdown_item);
        fontSizeArrayAdapter.setDropDownViewResource(R.layout.domain_settings_spinner_dropdown_item);
        displayImagesArrayAdapter.setDropDownViewResource(R.layout.domain_settings_spinner_dropdown_item);
        nightModeArrayAdapter.setDropDownViewResource(R.layout.domain_settings_spinner_dropdown_item);

        // Set the `ArrayAdapters` for the `Spinners`.
        userAgentSpinner.setAdapter(userAgentArrayAdapter);
        fontSizeSpinner.setAdapter(fontSizeArrayAdapter);
        displayWebpageImagesSpinner.setAdapter(displayImagesArrayAdapter);
        nightModeSpinner.setAdapter(nightModeArrayAdapter);

        // Create a `SpannableStringBuilder` for each `TextView` that needs multiple colors of text.
        SpannableStringBuilder savedSslCertificateIssuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + savedSslCertificateIssuedToCNameString);
        SpannableStringBuilder savedSslCertificateIssuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + savedSslCertificateIssuedToONameString);
        SpannableStringBuilder savedSslCertificateIssuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + savedSslCertificateIssuedToUNameString);
        SpannableStringBuilder savedSslCertificateIssuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + savedSslCertificateIssuedByCNameString);
        SpannableStringBuilder savedSslCertificateIssuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + savedSslCertificateIssuedByONameString);
        SpannableStringBuilder savedSslCertificateIssuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + savedSslCertificateIssuedByUNameString);

        // Initialize the `SpannableStringBuilders` for the SSL certificate dates.
        SpannableStringBuilder savedSslCertificateStartDateStringBuilder;
        SpannableStringBuilder savedSslCertificateEndDateStringBuilder;

        // Leave the SSL certificate dates empty if they are `null`.
        if (savedSslCertificateStartDate == null) {
            savedSslCertificateStartDateStringBuilder = new SpannableStringBuilder(startDateLabel);
        } else {
            savedSslCertificateStartDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(savedSslCertificateStartDate));
        }

        if (savedSslCertificateEndDate == null) {
            savedSslCertificateEndDateStringBuilder = new SpannableStringBuilder(endDateLabel);
        } else {
            savedSslCertificateEndDateStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(savedSslCertificateEndDate));
        }

        // Create a red `ForegroundColorSpan`.  We have to use the deprecated `getColor` until API >= 23.
        final ForegroundColorSpan redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));

        // Create a blue `ForegroundColorSpan`.
        final ForegroundColorSpan blueColorSpan;

        // Set `blueColorSpan` according to the theme.  We have to use the deprecated `getColor()` until API >= 23.
        if (MainWebViewActivity.darkTheme) {
            //noinspection deprecation
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
        } else {
            //noinspection deprecation
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
        }

        // Set the domain name from the the database cursor.
        domainNameEditText.setText(domainNameString);

        // Update the certificates' `Common Name` color when the domain name text changes.
        domainNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Get the new domain name.
                String newDomainName = domainNameEditText.getText().toString();

                // Check the saved SSL certificate against the new domain name.
                boolean savedSslCertificateMatchesNewDomainName = checkDomainNameAgainstCertificate(newDomainName, savedSslCertificateIssuedToCNameString);

                // Create a `SpannableStringBuilder` for the saved certificate `Common Name`.
                SpannableStringBuilder savedSslCertificateCommonNameStringBuilder = new SpannableStringBuilder(cNameLabel + savedSslCertificateIssuedToCNameString);

                // Format the saved certificate `Common Name` color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
                if (savedSslCertificateMatchesNewDomainName) {
                    savedSslCertificateCommonNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), savedSslCertificateCommonNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {
                    savedSslCertificateCommonNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), savedSslCertificateCommonNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }

                // Update `savedSslCertificateIssuedToCNameTextView`.
                savedSslCertificateIssuedToCNameTextView.setText(savedSslCertificateCommonNameStringBuilder);

                // Update the current website certificate if it exists.
                if (currentWebsiteSslCertificate != null) {
                    // Get the current website certificate `Common Name`.
                    String currentWebsiteCertificateCommonName = currentWebsiteSslCertificate.getIssuedTo().getCName();

                    // Check the current website certificate against the new domain name.
                    boolean currentWebsiteCertificateMatchesNewDomainName = checkDomainNameAgainstCertificate(newDomainName, currentWebsiteCertificateCommonName);

                    // Create a `SpannableStringBuilder` for the current website certificate `Common Name`.
                    SpannableStringBuilder currentWebsiteCertificateCommonNameStringBuilder = new SpannableStringBuilder(cNameLabel + currentWebsiteCertificateCommonName);

                    // Format the current certificate `Common Name` color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
                    if (currentWebsiteCertificateMatchesNewDomainName) {
                        currentWebsiteCertificateCommonNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), currentWebsiteCertificateCommonNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    } else {
                        currentWebsiteCertificateCommonNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), currentWebsiteCertificateCommonNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }

                    // Update `currentWebsiteCertificateIssuedToCNameTextView`.
                    currentWebsiteCertificateIssuedToCNameTextView.setText(currentWebsiteCertificateCommonNameStringBuilder);
                }
            }
        });

        // Create a `boolean` to track if night mode is enabled.
        boolean nightModeEnabled = (nightModeInt == DomainsDatabaseHelper.NIGHT_MODE_ENABLED) || ((nightModeInt == DomainsDatabaseHelper.NIGHT_MODE_SYSTEM_DEFAULT) && defaultNightModeBoolean);

        // Disable the JavaScript `Switch` if night mode is enabled.
        if (nightModeEnabled) {
            javaScriptEnabledSwitch.setEnabled(false);
        } else {
            javaScriptEnabledSwitch.setEnabled(true);
        }

        // Set the JavaScript icon.
        if ((javaScriptEnabledInt == 1) || nightModeEnabled) {
            javaScriptImageView.setImageDrawable(resources.getDrawable(R.drawable.javascript_enabled));
        } else {
            javaScriptImageView.setImageDrawable(resources.getDrawable(R.drawable.privacy_mode));
        }

        // Set the JavaScript `Switch` status.
        if (javaScriptEnabledInt == 1) {  // JavaScript is enabled.
            javaScriptEnabledSwitch.setChecked(true);
        } else {  // JavaScript is disabled.
            javaScriptEnabledSwitch.setChecked(false);
        }

        // Set the first-party cookies status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
        if (firstPartyCookiesEnabledInt == 1) {  // First-party cookies are enabled.
            firstPartyCookiesEnabledSwitch.setChecked(true);
            firstPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_enabled));
        } else {  // First-party cookies are disabled.
            firstPartyCookiesEnabledSwitch.setChecked(false);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                firstPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_dark));
            } else {
                firstPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_light));
            }
        }

        // Only display third-party cookies if SDK_INT >= 21.
        if (Build.VERSION.SDK_INT >= 21) {  // Third-party cookies can be configured for API >= 21.
            // Only enable third-party-cookies if first-party cookies are enabled.
            if (firstPartyCookiesEnabledInt == 1) {  // First-party cookies are enabled.
                // Set the third-party cookies status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
                if (thirdPartyCookiesEnabledInt == 1) {  // Both first-party and third-party cookies are enabled.
                    thirdPartyCookiesEnabledSwitch.setChecked(true);
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_warning));
                } else {  // First party cookies are enabled but third-party cookies are disabled.
                    thirdPartyCookiesEnabledSwitch.setChecked(false);

                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_dark));
                    } else {
                        thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_light));
                    }
                }
            } else {  // First-party cookies are disabled.
                // Set the status of third-party cookies.
                if (thirdPartyCookiesEnabledInt == 1) {
                    thirdPartyCookiesEnabledSwitch.setChecked(true);
                } else {
                    thirdPartyCookiesEnabledSwitch.setChecked(false);
                }

                // Disable the third-party cookies switch.
                thirdPartyCookiesEnabledSwitch.setEnabled(false);

                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_ghosted_dark));
                } else {
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_ghosted_light));
                }
            }
        } else {  // Third-party cookies cannot be configured for API <= 21.
            // Hide the `LinearLayout` for third-party cookies.
            thirdPartyCookiesLinearLayout.setVisibility(View.GONE);
        }

        // Only enable DOM storage if JavaScript is enabled.
        if ((javaScriptEnabledInt == 1) || nightModeEnabled) {  // JavaScript is enabled.
            // Enable the DOM storage `Switch`.
            domStorageEnabledSwitch.setEnabled(true);

            // Set the DOM storage status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
            if (domStorageEnabledInt == 1) {  // Both JavaScript and DOM storage are enabled.
                domStorageEnabledSwitch.setChecked(true);
                domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_enabled));
            } else {  // JavaScript is enabled but DOM storage is disabled.
                // Set the DOM storage switch to off.
                domStorageEnabledSwitch.setChecked(false);

                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_dark));
                } else {
                    domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_light));
                }
            }
        } else {  // JavaScript is disabled.
            // Disable the DOM storage `Switch`.
            domStorageEnabledSwitch.setEnabled(false);

            // Set the checked status of DOM storage.
            if (domStorageEnabledInt == 1) {  // DOM storage is enabled but JavaScript is disabled.
                domStorageEnabledSwitch.setChecked(true);
            } else {  // Both JavaScript and DOM storage are disabled.
                domStorageEnabledSwitch.setChecked(false);
            }

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_ghosted_dark));
            } else {
                domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_ghosted_light));
            }
        }

        // Set the form data status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
        if (formDataEnabledInt == 1) {  // Form data is enabled.
            formDataEnabledSwitch.setChecked(true);
            formDataImageView.setImageDrawable(resources.getDrawable(R.drawable.form_data_enabled));
        } else {  // Form data is disabled.
            // Set the form data switch to off.
            formDataEnabledSwitch.setChecked(false);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                formDataImageView.setImageDrawable(resources.getDrawable(R.drawable.form_data_disabled_dark));
            } else {
                formDataImageView.setImageDrawable(resources.getDrawable(R.drawable.form_data_disabled_light));
            }
        }

        // We need to inflated a `WebView` to get the default user agent.
        // `@SuppressLint("InflateParams")` removes the warning about using `null` as the `ViewGroup`, which in this case makes sense because we don't want to display `bare_webview` on the screen.  `false` does not attach the view to the root.
        @SuppressLint("InflateParams") View bareWebViewLayout = inflater.inflate(R.layout.bare_webview, null, false);
        WebView bareWebView = bareWebViewLayout.findViewById(R.id.bare_webview);
        final String webViewDefaultUserAgentString = bareWebView.getSettings().getUserAgentString();

        // Get the position of the user agent in `userAgentEntryValuesArrayAdapter`.
        int userAgentArrayPosition = userAgentEntryValuesArrayAdapter.getPosition(currentUserAgentString);

        // Set the user agent.
        if (userAgentArrayPosition == -1) {  // We are using a custom `userAgentString`.
            // Set `userAgentSpinner` to `Custom`.
            userAgentSpinner.setSelection(userAgentEntryValuesArrayAdapter.getPosition("Custom user agent"));

            // Hide `userAgentTextView`.
            userAgentTextView.setVisibility(View.GONE);

            // Show `customUserAgentEditText` and set `userAgentString` as the text.
            customUserAgentEditText.setVisibility(View.VISIBLE);
            customUserAgentEditText.setText(currentUserAgentString);
        } else{  // We are using one of the preset user agents.
            // Set the `userAgentSpinner` selection.
            userAgentSpinner.setSelection(userAgentArrayPosition);

            // Show `userAgentTextView`.
            userAgentTextView.setVisibility(View.VISIBLE);

            // Hide `customUserAgentEditText`.
            customUserAgentEditText.setVisibility(View.GONE);

            // Set the user agent text.
            switch (currentUserAgentString) {
                case "System default user agent":
                    // Display the user agent text string.
                    switch (defaultUserAgentString) {
                        case "WebView default user agent":
                            // Display the `WebView` default user agent.
                            userAgentTextView.setText(webViewDefaultUserAgentString);
                            break;

                        case "Custom user agent":
                            // Display the custom user agent.
                            userAgentTextView.setText(defaultCustomUserAgentString);
                            break;

                        default:
                            // Display the text from `defaultUserAgentString`.
                            userAgentTextView.setText(defaultUserAgentString);
                    }
                    break;

                case "WebView default user agent":
                    // Display the `WebView` default user agent.
                    userAgentTextView.setText(webViewDefaultUserAgentString);
                    break;

                default:
                    // Display the text from `currentUserAgentString`.
                    userAgentTextView.setText(currentUserAgentString);
            }
        }

        // Open the user agent spinner when the `TextView` is clicked.
        userAgentTextView.setOnClickListener((View v) -> {
            // Open the user agent spinner.
            userAgentSpinner.performClick();
        });

        // Set the selected font size.
        int fontSizeArrayPosition = fontSizeEntryValuesArrayAdapter.getPosition(String.valueOf(fontSizeInt));
        fontSizeSpinner.setSelection(fontSizeArrayPosition);

        // Set the default font size text.
        int defaultFontSizeArrayPosition = fontSizeEntryValuesArrayAdapter.getPosition(defaultFontSizeString);
        fontSizeTextView.setText(fontSizeArrayAdapter.getItem(defaultFontSizeArrayPosition));

        // Set the display options for `fontSizeTextView`.
        if (fontSizeArrayPosition == 0) {  // System default font size is selected.  Display `fontSizeTextView`.
            fontSizeTextView.setVisibility(View.VISIBLE);
        } else {  // A custom font size is specified.  Hide `fontSizeTextView`.
            fontSizeTextView.setVisibility(View.GONE);
        }

        // Open the font size spinner when the `TextView` is clicked.
        fontSizeTextView.setOnClickListener((View v) -> {
            // Open the user agent spinner.
            fontSizeSpinner.performClick();
        });

        // Display the website images mode in the spinner.
        displayWebpageImagesSpinner.setSelection(displayImagesInt);

        // Set the default display images text.
        if (defaultDisplayWebpageImagesBoolean) {
            displayImagesTextView.setText(displayImagesArrayAdapter.getItem(DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_ENABLED));
        } else {
            displayImagesTextView.setText(displayImagesArrayAdapter.getItem(DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_DISABLED));
        }

        // Set the display website images icon and `TextView` settings.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
        switch (displayImagesInt) {
            case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_SYSTEM_DEFAULT:
                if (defaultDisplayWebpageImagesBoolean) {  // Display webpage images enabled by default.
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_dark));
                    } else {
                        displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_light));
                    }
                } else {  // Display webpage images disabled by default.
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_dark));
                    } else {
                        displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_light));
                    }
                }

                // Show `displayImagesTextView`.
                displayImagesTextView.setVisibility(View.VISIBLE);
                break;

            case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_ENABLED:
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_dark));
                } else {
                    displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_light));
                }

                // Hide `displayImagesTextView`.
                displayImagesTextView.setVisibility(View.GONE);
                break;

            case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_DISABLED:
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_dark));
                } else {
                    displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_light));
                }

                // Hide `displayImagesTextView`.
                displayImagesTextView.setVisibility(View.GONE);
                break;
        }

        // Open the display images spinner when the `TextView` is clicked.
        displayImagesTextView.setOnClickListener((View v) -> {
            // Open the user agent spinner.
            displayWebpageImagesSpinner.performClick();
        });

        // Display the night mode in the spinner.
        nightModeSpinner.setSelection(nightModeInt);

        // Set the default night mode text.
        if (defaultNightModeBoolean) {
            nightModeTextView.setText(nightModeArrayAdapter.getItem(DomainsDatabaseHelper.NIGHT_MODE_ENABLED));
        } else {
            nightModeTextView.setText(nightModeArrayAdapter.getItem(DomainsDatabaseHelper.NIGHT_MODE_DISABLED));
        }

        // Set the night mode icon and `TextView` settings.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
        switch (displayImagesInt) {
            case DomainsDatabaseHelper.NIGHT_MODE_SYSTEM_DEFAULT:
                if (defaultNightModeBoolean) {  // Night mode enabled by default.
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_dark));
                    } else {
                        nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_light));
                    }
                } else {  // Night mode disabled by default.
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_dark));
                    } else {
                        nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_light));
                    }
                }

                // Show `nightModeTextView`.
                nightModeTextView.setVisibility(View.VISIBLE);
                break;

            case DomainsDatabaseHelper.NIGHT_MODE_ENABLED:
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_dark));
                } else {
                    nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_light));
                }

                // Hide `nightModeTextView`.
                nightModeTextView.setVisibility(View.GONE);
                break;

            case DomainsDatabaseHelper.NIGHT_MODE_DISABLED:
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_dark));
                } else {
                    nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_light));
                }

                // Hide `nightModeTextView`.
                nightModeTextView.setVisibility(View.GONE);
                break;
        }

        // Open the night mode spinner when the `TextView` is clicked.
        nightModeTextView.setOnClickListener((View v) -> {
            // Open the user agent spinner.
            nightModeSpinner.performClick();
        });
        
        // Set the pinned SSL certificate icon.
        if (pinnedSslCertificateInt == 1) {  // Pinned SSL certificate is enabled.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
            // Check the switch.
            pinnedSslCertificateSwitch.setChecked(true);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_enabled_dark));
            } else {
                pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_enabled_light));
            }
        } else {  // Pinned SSL certificate is disabled.
            // Uncheck the switch.
            pinnedSslCertificateSwitch.setChecked(false);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_disabled_dark));
            } else {
                pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_disabled_light));
            }
        }

        // Store the current date.
        Date currentDate = Calendar.getInstance().getTime();

        // Setup the `StringBuilders` to display the general certificate information in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
        savedSslCertificateIssuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), savedSslCertificateIssuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        savedSslCertificateIssuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), savedSslCertificateIssuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        savedSslCertificateIssuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), savedSslCertificateIssuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        savedSslCertificateIssuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), savedSslCertificateIssuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        savedSslCertificateIssuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), savedSslCertificateIssuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // Check the certificate `Common Name` against the domain name.
        boolean savedSSlCertificateCommonNameMatchesDomainName = checkDomainNameAgainstCertificate(domainNameString, savedSslCertificateIssuedToCNameString);

        // Format the `issuedToCommonName` color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
        if (savedSSlCertificateCommonNameMatchesDomainName) {
            savedSslCertificateIssuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), savedSslCertificateIssuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else {
            savedSslCertificateIssuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), savedSslCertificateIssuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        //  Format the start date color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
        if ((savedSslCertificateStartDate != null) && savedSslCertificateStartDate.after(currentDate)) {  // The certificate start date is in the future.
            savedSslCertificateStartDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), savedSslCertificateStartDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else {  // The certificate start date is in the past.
            savedSslCertificateStartDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), savedSslCertificateStartDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // Format the end date color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
        if ((savedSslCertificateEndDate != null) && savedSslCertificateEndDate.before(currentDate)) {  // The certificate end date is in the past.
            savedSslCertificateEndDateStringBuilder.setSpan(redColorSpan, endDateLabel.length(), savedSslCertificateEndDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else {  // The certificate end date is in the future.
            savedSslCertificateEndDateStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), savedSslCertificateEndDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        // Display the current website SSL certificate strings.
        savedSslCertificateIssuedToCNameTextView.setText(savedSslCertificateIssuedToCNameStringBuilder);
        savedSslCertificateIssuedToONameTextView.setText(savedSslCertificateIssuedToONameStringBuilder);
        savedSslCertificateIssuedToUNameTextView.setText(savedSslCertificateIssuedToUNameStringBuilder);
        savedSslCertificateIssuedByCNameTextView.setText(savedSslCertificateIssuedByCNameStringBuilder);
        savedSslCertificateIssuedByONameTextView.setText(savedSslCertificateIssuedByONameStringBuilder);
        savedSslCertificateIssuedByUNameTextView.setText(savedSslCertificateIssuedByUNameStringBuilder);
        savedSslCertificateStartDateTextView.setText(savedSslCertificateStartDateStringBuilder);
        savedSslCertificateEndDateTextView.setText(savedSslCertificateEndDateStringBuilder);

        // Populate the current website SSL certificate if there is one.
        if (currentWebsiteSslCertificate != null) {
            // Get the strings from the SSL certificate.
            String currentWebsiteCertificateIssuedToCNameString = currentWebsiteSslCertificate.getIssuedTo().getCName();
            String currentWebsiteCertificateIssuedToONameString = currentWebsiteSslCertificate.getIssuedTo().getOName();
            String currentWebsiteCertificateIssuedToUNameString = currentWebsiteSslCertificate.getIssuedTo().getUName();
            String currentWebsiteCertificateIssuedByCNameString = currentWebsiteSslCertificate.getIssuedBy().getCName();
            String currentWebsiteCertificateIssuedByONameString = currentWebsiteSslCertificate.getIssuedBy().getOName();
            String currentWebsiteCertificateIssuedByUNameString = currentWebsiteSslCertificate.getIssuedBy().getUName();
            Date currentWebsiteCertificateStartDate = currentWebsiteSslCertificate.getValidNotBeforeDate();
            Date currentWebsiteCertificateEndDate = currentWebsiteSslCertificate.getValidNotAfterDate();

            // Create a `SpannableStringBuilder` for each `TextView` that needs multiple colors of text.
            SpannableStringBuilder currentWebsiteCertificateIssuedToCNameStringBuilder = new SpannableStringBuilder(cNameLabel + currentWebsiteCertificateIssuedToCNameString);
            SpannableStringBuilder currentWebsiteCertificateIssuedToONameStringBuilder = new SpannableStringBuilder(oNameLabel + currentWebsiteCertificateIssuedToONameString);
            SpannableStringBuilder currentWebsiteCertificateIssuedToUNameStringBuilder = new SpannableStringBuilder(uNameLabel + currentWebsiteCertificateIssuedToUNameString);
            SpannableStringBuilder currentWebsiteCertificateIssuedByCNameStringBuilder = new SpannableStringBuilder(cNameLabel + currentWebsiteCertificateIssuedByCNameString);
            SpannableStringBuilder currentWebsiteCertificateIssuedByONameStringBuilder = new SpannableStringBuilder(oNameLabel + currentWebsiteCertificateIssuedByONameString);
            SpannableStringBuilder currentWebsiteCertificateIssuedByUNameStringBuilder = new SpannableStringBuilder(uNameLabel + currentWebsiteCertificateIssuedByUNameString);
            SpannableStringBuilder currentWebsiteCertificateStartDateStringBuilder = new SpannableStringBuilder(startDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(currentWebsiteCertificateStartDate));
            SpannableStringBuilder currentWebsiteCertificateEndDateStringBuilder = new SpannableStringBuilder(endDateLabel + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG).format(currentWebsiteCertificateEndDate));

            // Setup the `StringBuilders` to display the general certificate information in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            currentWebsiteCertificateIssuedToONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), currentWebsiteCertificateIssuedToONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            currentWebsiteCertificateIssuedToUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), currentWebsiteCertificateIssuedToUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            currentWebsiteCertificateIssuedByCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), currentWebsiteCertificateIssuedByCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            currentWebsiteCertificateIssuedByONameStringBuilder.setSpan(blueColorSpan, oNameLabel.length(), currentWebsiteCertificateIssuedByONameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            currentWebsiteCertificateIssuedByUNameStringBuilder.setSpan(blueColorSpan, uNameLabel.length(), currentWebsiteCertificateIssuedByUNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // Check the certificate `Common Name` against the domain name.
            boolean currentWebsiteCertificateCommonNameMatchesDomainName = checkDomainNameAgainstCertificate(domainNameString, currentWebsiteCertificateIssuedToCNameString);

            // Format the `issuedToCommonName` color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            if (currentWebsiteCertificateCommonNameMatchesDomainName) {
                currentWebsiteCertificateIssuedToCNameStringBuilder.setSpan(blueColorSpan, cNameLabel.length(), currentWebsiteCertificateIssuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {
                currentWebsiteCertificateIssuedToCNameStringBuilder.setSpan(redColorSpan, cNameLabel.length(), currentWebsiteCertificateIssuedToCNameStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            //  Format the start date color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            if (currentWebsiteCertificateStartDate.after(currentDate)) {  // The certificate start date is in the future.
                currentWebsiteCertificateStartDateStringBuilder.setSpan(redColorSpan, startDateLabel.length(), currentWebsiteCertificateStartDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {  // The certificate start date is in the past.
                currentWebsiteCertificateStartDateStringBuilder.setSpan(blueColorSpan, startDateLabel.length(), currentWebsiteCertificateStartDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            // Format the end date color.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
            if (currentWebsiteCertificateEndDate.before(currentDate)) {  // The certificate end date is in the past.
                currentWebsiteCertificateEndDateStringBuilder.setSpan(redColorSpan, endDateLabel.length(), currentWebsiteCertificateEndDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {  // The certificate end date is in the future.
                currentWebsiteCertificateEndDateStringBuilder.setSpan(blueColorSpan, endDateLabel.length(), currentWebsiteCertificateEndDateStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }

            // Display the current website SSL certificate strings.
            currentWebsiteCertificateIssuedToCNameTextView.setText(currentWebsiteCertificateIssuedToCNameStringBuilder);
            currentWebsiteCertificateIssuedToONameTextView.setText(currentWebsiteCertificateIssuedToONameStringBuilder);
            currentWebsiteCertificateIssuedToUNameTextView.setText(currentWebsiteCertificateIssuedToUNameStringBuilder);
            currentWebsiteCertificateIssuedByCNameTextView.setText(currentWebsiteCertificateIssuedByCNameStringBuilder);
            currentWebsiteCertificateIssuedByONameTextView.setText(currentWebsiteCertificateIssuedByONameStringBuilder);
            currentWebsiteCertificateIssuedByUNameTextView.setText(currentWebsiteCertificateIssuedByUNameStringBuilder);
            currentWebsiteCertificateStartDateTextView.setText(currentWebsiteCertificateStartDateStringBuilder);
            currentWebsiteCertificateEndDateTextView.setText(currentWebsiteCertificateEndDateStringBuilder);
        }

        // Set the initial display status for the SSL certificates.
        if (pinnedSslCertificateSwitch.isChecked()) {
            // Set the visibility of the saved SSL certificate.
            if (savedSslCertificateIssuedToCNameString == null) {
                savedSslCertificateLinearLayout.setVisibility(View.GONE);
            } else {
                savedSslCertificateLinearLayout.setVisibility(View.VISIBLE);
            }

            // Set the visibility of the current website SSL certificate.
            if (currentWebsiteSslCertificate == null) {
                // Hide the SSL certificate.
                currentWebsiteCertificateLinearLayout.setVisibility(View.GONE);

                // Show the instruction.
                noCurrentWebsiteCertificateTextView.setVisibility(View.VISIBLE);
            } else {
                // Show the SSL certificate.
                currentWebsiteCertificateLinearLayout.setVisibility(View.VISIBLE);

                // Hide the instruction.
                noCurrentWebsiteCertificateTextView.setVisibility(View.GONE);
            }

            // Set the status of the radio buttons.
            if (savedSslCertificateLinearLayout.getVisibility() == View.VISIBLE) {  // The saved SSL certificate is displayed.
                savedSslCertificateRadioButton.setChecked(true);
                currentWebsiteCertificateRadioButton.setChecked(false);
            } else if (currentWebsiteCertificateLinearLayout.getVisibility() == View.VISIBLE) {  // The saved SSL certificate is hidden but the current website SSL certificate is visible.
                currentWebsiteCertificateRadioButton.setChecked(true);
                savedSslCertificateRadioButton.setChecked(false);
            } else {  // Neither SSL certificate is visible.
                savedSslCertificateRadioButton.setChecked(false);
                currentWebsiteCertificateRadioButton.setChecked(false);
            }
        } else {  // `pinnedSslCertificateSwitch` is not checked.
            // Hide the SSl certificates and instructions.
            savedSslCertificateLinearLayout.setVisibility(View.GONE);
            currentWebsiteCertificateLinearLayout.setVisibility(View.GONE);
            noCurrentWebsiteCertificateTextView.setVisibility(View.GONE);

            // Uncheck the radio buttons.
            savedSslCertificateRadioButton.setChecked(false);
            currentWebsiteCertificateRadioButton.setChecked(false);
        }


        // Set the `javaScriptEnabledSwitch` `OnCheckedChangeListener()`.
        javaScriptEnabledSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {  // JavaScript is enabled.
                // Update the JavaScript icon.
                javaScriptImageView.setImageDrawable(resources.getDrawable(R.drawable.javascript_enabled));

                // Enable the DOM storage `Switch`.
                domStorageEnabledSwitch.setEnabled(true);

                // Update the DOM storage icon.
                if (domStorageEnabledSwitch.isChecked()) {  // DOM storage is enabled.
                    domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_enabled));
                } else {  // DOM storage is disabled.
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_dark));
                    } else {
                        domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_light));
                    }
                }
            } else {  // JavaScript is disabled.
                // Update the JavaScript icon.
                javaScriptImageView.setImageDrawable(resources.getDrawable(R.drawable.privacy_mode));

                // Disable the DOM storage `Switch`.
                domStorageEnabledSwitch.setEnabled(false);

                // Set the DOM storage icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_ghosted_dark));
                } else {
                    domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_ghosted_light));
                }
            }
        });

        // Set the `firstPartyCookiesEnabledSwitch` `OnCheckedChangeListener()`.
        firstPartyCookiesEnabledSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked) {  // First-party cookies are enabled.
                // Update the first-party cookies icon.
                firstPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_enabled));

                // Enable the third-party cookies `Switch`.
                thirdPartyCookiesEnabledSwitch.setEnabled(true);

                // Update the third-party cookies icon.
                if (thirdPartyCookiesEnabledSwitch.isChecked()) {  // Third-party cookies are enabled.
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_warning));
                } else {  // Third-party cookies are disabled.
                    // Set the third-party cookies icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_dark));
                    } else {
                        thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_light));
                    }
                }
            } else {  // First-party cookies are disabled.
                // Update the first-party cookies icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    firstPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_dark));
                } else {
                    firstPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_light));
                }

                // Disable the third-party cookies `Switch`.
                thirdPartyCookiesEnabledSwitch.setEnabled(false);

                // Set the third-party cookies icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_ghosted_dark));
                } else {
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_ghosted_light));
                }
            }
        });

        // Set the `thirdPartyCookiesEnabledSwitch` `OnCheckedChangeListener()`.
        thirdPartyCookiesEnabledSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            // Update the icon.
            if (isChecked) {
                thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_warning));
            } else {
                // Update the third-party cookies icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_dark));
                } else {
                    thirdPartyCookiesImageView.setImageDrawable(resources.getDrawable(R.drawable.cookies_disabled_light));
                }
            }
        });

        // Set the `domStorageEnabledSwitch` `OnCheckedChangeListener()`.
        domStorageEnabledSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            // Update the icon.
            if (isChecked) {
                domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_enabled));
            } else {
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_dark));
                } else {
                    domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_light));
                }
            }
        });

        // Set the `formDataEnabledSwitch` `OnCheckedChangeListener()`.
        formDataEnabledSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            // Update the icon.
            if (isChecked) {
                formDataImageView.setImageDrawable(resources.getDrawable(R.drawable.form_data_enabled));
            } else {
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    formDataImageView.setImageDrawable(resources.getDrawable(R.drawable.form_data_disabled_dark));
                } else {
                    formDataImageView.setImageDrawable(resources.getDrawable(R.drawable.form_data_disabled_light));
                }
            }
        });

        // Set the `userAgentSpinner` `onItemClickListener()`.
        userAgentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the new user agent string.
                String newUserAgentString = resources.getStringArray(R.array.domain_settings_user_agent_entry_values)[position];

                // Set the new user agent.
                switch (newUserAgentString) {
                    case "System default user agent":
                        // Show `userAgentTextView`.
                        userAgentTextView.setVisibility(View.VISIBLE);

                        // Hide `customUserAgentEditText`.
                        customUserAgentEditText.setVisibility(View.GONE);

                        // Set the user text.
                        switch (defaultUserAgentString) {
                            case "WebView default user agent":
                                // Display the `WebView` default user agent.
                                userAgentTextView.setText(webViewDefaultUserAgentString);
                                break;

                            case "Custom user agent":
                                // Display the custom user agent.
                                userAgentTextView.setText(defaultCustomUserAgentString);
                                break;

                            default:
                                // Display the text from `defaultUserAgentString`.
                                userAgentTextView.setText(defaultUserAgentString);
                        }
                        break;

                    case "WebView default user agent":
                        // Show `userAgentTextView` and set the text.
                        userAgentTextView.setVisibility(View.VISIBLE);
                        userAgentTextView.setText(webViewDefaultUserAgentString);

                        // Hide `customUserAgentEditText`.
                        customUserAgentEditText.setVisibility(View.GONE);
                        break;

                    case "Custom user agent":
                        // Hide `userAgentTextView`.
                        userAgentTextView.setVisibility(View.GONE);

                        // Show `customUserAgentEditText` and set `userAgentString` as the text.
                        customUserAgentEditText.setVisibility(View.VISIBLE);
                        customUserAgentEditText.setText(currentUserAgentString);
                        break;

                    default:
                        // Show `userAgentTextView` and set the text.
                        userAgentTextView.setVisibility(View.VISIBLE);
                        userAgentTextView.setText(newUserAgentString);

                        // Hide `customUserAgentEditText`.
                        customUserAgentEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // Set the `fontSizeSpinner` `onItemSelectedListener()`.
        fontSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update the display options for `fontSizeTextView`.
                if (position == 0) {  // System default font size has been selected.  Display `fontSizeTextView`.
                    fontSizeTextView.setVisibility(View.VISIBLE);
                } else {  // A custom font size has been selected.  Hide `fontSizeTextView`.
                    fontSizeTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // Set the `displayWebpageImagesSpinner` `onItemSelectedListener()`.
        displayWebpageImagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update the icon and the visibility of `displayImagesTextView`.
                switch (position) {
                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_SYSTEM_DEFAULT:
                        if (defaultDisplayWebpageImagesBoolean) {
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_dark));
                            } else {
                                displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_light));
                            }
                        } else {
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_dark));
                            } else {
                                displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_light));
                            }
                        }

                        // Show `displayImagesTextView`.
                        displayImagesTextView.setVisibility(View.VISIBLE);
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_ENABLED:
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_dark));
                        } else {
                            displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_enabled_light));
                        }

                        // Hide `displayImagesTextView`.
                        displayImagesTextView.setVisibility(View.GONE);
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_DISABLED:
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_dark));
                        } else {
                            displayWebpageImagesImageView.setImageDrawable(resources.getDrawable(R.drawable.images_disabled_light));
                        }

                        // Hide `displayImagesTextView`.
                        displayImagesTextView.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // Set the `nightModeSpinner` `onItemSelectedListener()`.
        nightModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update the icon and the visibility of `nightModeTextView`.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
                switch (position) {
                    case DomainsDatabaseHelper.NIGHT_MODE_SYSTEM_DEFAULT:
                        if (defaultNightModeBoolean) {  // Night mode enabled by default.
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_dark));
                            } else {
                                nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_light));
                            }
                        } else {  // Night mode disabled by default.
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_dark));
                            } else {
                                nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_light));
                            }
                        }

                        // Show `nightModeTextView`.
                        nightModeTextView.setVisibility(View.VISIBLE);
                        break;

                    case DomainsDatabaseHelper.NIGHT_MODE_ENABLED:
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_dark));
                        } else {
                            nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_enabled_light));
                        }

                        // Hide `nightModeTextView`.
                        nightModeTextView.setVisibility(View.GONE);
                        break;

                    case DomainsDatabaseHelper.NIGHT_MODE_DISABLED:
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_dark));
                        } else {
                            nightModeImageView.setImageDrawable(resources.getDrawable(R.drawable.night_mode_disabled_light));
                        }

                        // Hide `nightModeTextView`.
                        nightModeTextView.setVisibility(View.GONE);
                        break;
                }

                // Create a `boolean` to store the current night mode setting.
                boolean currentNightModeEnabled = (position == DomainsDatabaseHelper.NIGHT_MODE_ENABLED) || ((position == DomainsDatabaseHelper.NIGHT_MODE_SYSTEM_DEFAULT) && defaultNightModeBoolean);

                // Disable the JavaScript `Switch` if night mode is enabled.
                if (currentNightModeEnabled) {
                    javaScriptEnabledSwitch.setEnabled(false);
                } else {
                    javaScriptEnabledSwitch.setEnabled(true);
                }

                // Update the JavaScript icon.
                if ((javaScriptEnabledInt == 1) || currentNightModeEnabled) {
                    javaScriptImageView.setImageDrawable(resources.getDrawable(R.drawable.javascript_enabled));
                } else {
                    javaScriptImageView.setImageDrawable(resources.getDrawable(R.drawable.privacy_mode));
                }

                // Update the DOM storage status.
                if ((javaScriptEnabledInt == 1) || currentNightModeEnabled) {  // JavaScript is enabled.
                    // Enable the DOM storage `Switch`.
                    domStorageEnabledSwitch.setEnabled(true);

                    // Set the DOM storage status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
                    if (domStorageEnabledInt == 1) {  // Both JavaScript and DOM storage are enabled.
                        domStorageEnabledSwitch.setChecked(true);
                        domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_enabled));
                    } else {  // JavaScript is enabled but DOM storage is disabled.
                        // Set the DOM storage switch to off.
                        domStorageEnabledSwitch.setChecked(false);

                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_dark));
                        } else {
                            domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_disabled_light));
                        }
                    }
                } else {  // JavaScript is disabled.
                    // Disable the DOM storage `Switch`.
                    domStorageEnabledSwitch.setEnabled(false);

                    // Set the checked status of DOM storage.
                    if (domStorageEnabledInt == 1) {  // DOM storage is enabled but JavaScript is disabled.
                        domStorageEnabledSwitch.setChecked(true);
                    } else {  // Both JavaScript and DOM storage are disabled.
                        domStorageEnabledSwitch.setChecked(false);
                    }

                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_ghosted_dark));
                    } else {
                        domStorageImageView.setImageDrawable(resources.getDrawable(R.drawable.dom_storage_ghosted_light));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });
        
        // Set the `pinnedSSLCertificateSwitch` `onCheckedChangeListener()`.
        pinnedSslCertificateSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            // Update the icon
            if (isChecked) {  // Pinned SSL certificate is enabled.
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_enabled_dark));
                } else {
                    pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_enabled_light));
                }

                // Update the visibility of the saved SSL certificate.
                if (savedSslCertificateIssuedToCNameString == null) {
                    savedSslCertificateLinearLayout.setVisibility(View.GONE);
                } else {
                    savedSslCertificateLinearLayout.setVisibility(View.VISIBLE);
                }

                // Update the visibility of the current website SSL certificate.
                if (currentWebsiteSslCertificate == null) {
                    // Hide the SSL certificate.
                    currentWebsiteCertificateLinearLayout.setVisibility(View.GONE);

                    // Show the instruction.
                    noCurrentWebsiteCertificateTextView.setVisibility(View.VISIBLE);
                } else {
                    // Show the SSL certificate.
                    currentWebsiteCertificateLinearLayout.setVisibility(View.VISIBLE);

                    // Hide the instruction.
                    noCurrentWebsiteCertificateTextView.setVisibility(View.GONE);
                }

                // Set the status of the radio buttons.
                if (savedSslCertificateLinearLayout.getVisibility() == View.VISIBLE) {  // The saved SSL certificate is displayed.
                    savedSslCertificateRadioButton.setChecked(true);
                    currentWebsiteCertificateRadioButton.setChecked(false);
                } else if (currentWebsiteCertificateLinearLayout.getVisibility() == View.VISIBLE) {  // The saved SSL certificate is hidden but the current website SSL certificate is visible.
                    currentWebsiteCertificateRadioButton.setChecked(true);
                    savedSslCertificateRadioButton.setChecked(false);
                } else {  // Neither SSL certificate is visible.
                    savedSslCertificateRadioButton.setChecked(false);
                    currentWebsiteCertificateRadioButton.setChecked(false);
                }
            } else {  // Pinned SSL certificate is disabled.
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_disabled_dark));
                } else {
                    pinnedSslCertificateImageView.setImageDrawable(resources.getDrawable(R.drawable.ssl_certificate_disabled_light));
                }

                // Hide the SSl certificates and instructions.
                savedSslCertificateLinearLayout.setVisibility(View.GONE);
                currentWebsiteCertificateLinearLayout.setVisibility(View.GONE);
                noCurrentWebsiteCertificateTextView.setVisibility(View.GONE);

                // Uncheck the radio buttons.
                savedSslCertificateRadioButton.setChecked(false);
                currentWebsiteCertificateRadioButton.setChecked(false);
            }
        });

        savedSslCertificateLinearLayout.setOnClickListener((View v) -> {
            savedSslCertificateRadioButton.setChecked(true);
            currentWebsiteCertificateRadioButton.setChecked(false);
        });

        savedSslCertificateRadioButton.setOnClickListener((View v) -> {
            savedSslCertificateRadioButton.setChecked(true);
            currentWebsiteCertificateRadioButton.setChecked(false);
        });

        currentWebsiteCertificateLinearLayout.setOnClickListener((View v) -> {
            currentWebsiteCertificateRadioButton.setChecked(true);
            savedSslCertificateRadioButton.setChecked(false);
        });

        currentWebsiteCertificateRadioButton.setOnClickListener((View v) -> {
            currentWebsiteCertificateRadioButton.setChecked(true);
            savedSslCertificateRadioButton.setChecked(false);
        });

        return domainSettingsView;
    }

    private boolean checkDomainNameAgainstCertificate(String domainName, String certificateCommonName) {
        // Initialize `domainNamesMatch`.
        boolean domainNamesMatch = false;

        // Check if the domains match.
        if (domainName.equals(certificateCommonName)) {
            domainNamesMatch = true;
        }

        // Check various wildcard permutations if `domainName` and `certificateCommonName` are not empty.  `noinspection ConstantCondition` removes Android Studio's incorrect lint warning that `domainName` can never be `null`.
        //noinspection ConstantConditions
        if ((domainName != null) && (certificateCommonName != null)) {
            // If `domainName` starts with a wildcard, check the base domain against all the subdomains of `certificateCommonName`.
            if (!domainNamesMatch && domainName.startsWith("*.") && (domainName.length() > 2)) {
                // Remove the initial `*.`.
                String baseDomainName = domainName.substring(2);

                // Setup a copy of `certificateCommonName` to test subdomains.
                String certificateCommonNameSubdomain = certificateCommonName;

                // Check all the subdomains in `certificateCommonNameSubdomain` against `baseDomainName`.
                while (!domainNamesMatch && certificateCommonNameSubdomain.contains(".")) {  // Stop checking if we know that `domainNamesMatch` is `true` or if we run out of  `.`.
                    // Test the `certificateCommonNameSubdomain` against `baseDomainName`.
                    if (certificateCommonNameSubdomain.equals(baseDomainName)) {
                        domainNamesMatch = true;
                    }

                    // Strip out the lowest subdomain of `certificateCommonNameSubdomain`.
                    try {
                        certificateCommonNameSubdomain = certificateCommonNameSubdomain.substring(certificateCommonNameSubdomain.indexOf(".") + 1);
                    } catch (IndexOutOfBoundsException e) {  // `certificateCommonNameSubdomain` ends with `.`.
                        certificateCommonNameSubdomain = "";
                    }
                }
            }

            // If `certificateCommonName` starts with a wildcard, check the base common name against all the subdomains of `domainName`.
            if (!domainNamesMatch && certificateCommonName.startsWith("*.") && (certificateCommonName.length() > 2)) {
                // Remove the initial `*.`.
                String baseCertificateCommonName = certificateCommonName.substring(2);

                // Setup a copy of `domainName` to test subdomains.
                String domainNameSubdomain = domainName;

                // Check all the subdomains in `domainNameSubdomain` against `baseCertificateCommonName`.
                while (!domainNamesMatch && domainNameSubdomain.contains(".") && (domainNameSubdomain.length() > 2)) {
                    // Test the `domainNameSubdomain` against `baseCertificateCommonName`.
                    if (domainNameSubdomain.equals(baseCertificateCommonName)) {
                        domainNamesMatch = true;
                    }

                    // Strip out the lowest subdomain of `domainNameSubdomain`.
                    try {
                        domainNameSubdomain = domainNameSubdomain.substring(domainNameSubdomain.indexOf(".") + 1);
                    } catch (IndexOutOfBoundsException e) { // `domainNameSubdomain` ends with `.`.
                        domainNameSubdomain = "";
                    }
                }
            }

            // If both names start with a wildcard, check if the root of one contains the root of the other.
            if (!domainNamesMatch && domainName.startsWith("*.") && (domainName.length() > 2) && certificateCommonName.startsWith("*.") && (certificateCommonName.length() > 2)) {
                // Remove the wildcards.
                String rootDomainName = domainName.substring(2);
                String rootCertificateCommonName = certificateCommonName.substring(2);

                // Check if one name ends with the contents of the other.  If so, there will be overlap in the their wildcard subdomains.
                if (rootDomainName.endsWith(rootCertificateCommonName) || rootCertificateCommonName.endsWith(rootDomainName)) {
                    domainNamesMatch = true;
                }
            }
        }

        return domainNamesMatch;
    }
}
