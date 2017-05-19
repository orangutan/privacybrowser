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
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
// We have to use `android.support.v4.app.Fragment` until minimum API >= 23.  Otherwise we cannot call `getContext()`.
import android.support.v4.app.Fragment;
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
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class DomainSettingsFragment extends Fragment {
    // `DATABASE_ID` is used by activities calling this fragment.
    public static final String DATABASE_ID = "database_id";

    // `databaseId` is used in `onCreate()` and `onCreateView()`.
    private int databaseId;

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
        // Inflate `domain_settings`.  `false` does not attach it to the root `container`.
        View domainSettingsView = inflater.inflate(R.layout.domain_settings, container, false);

        // Get a handle for the `Context`.
        Context context = getContext();

        // Get handles for the views in the fragment.
        EditText domainNameEditText = (EditText) domainSettingsView.findViewById(R.id.domain_settings_name_edittext);
        Switch javaScriptEnabledSwitch = (Switch) domainSettingsView.findViewById(R.id.domain_settings_javascript_switch);
        final ImageView javaScriptImageView = (ImageView) domainSettingsView.findViewById(R.id.domain_settings_javascript_imageview);
        Switch firstPartyCookiesEnabledSwitch = (Switch) domainSettingsView.findViewById(R.id.domain_settings_first_party_cookies_switch);
        final ImageView firstPartyCookiesImageView = (ImageView) domainSettingsView.findViewById(R.id.domain_settings_first_party_cookies_imageview);
        LinearLayout thirdPartyCookiesLinearLayout = (LinearLayout) domainSettingsView.findViewById(R.id.domain_settings_third_party_cookies_linearlayout);
        final Switch thirdPartyCookiesEnabledSwitch = (Switch) domainSettingsView.findViewById(R.id.domain_settings_third_party_cookies_switch);
        final ImageView thirdPartyCookiesImageView = (ImageView) domainSettingsView.findViewById(R.id.domain_settings_third_party_cookies_imageview);
        final Switch domStorageEnabledSwitch = (Switch) domainSettingsView.findViewById(R.id.domain_settings_dom_storage_switch);
        final ImageView domStorageImageView = (ImageView) domainSettingsView.findViewById(R.id.domain_settings_dom_storage_imageview);
        Switch formDataEnabledSwitch = (Switch) domainSettingsView.findViewById(R.id.domain_settings_form_data_switch);
        final ImageView formDataImageView = (ImageView) domainSettingsView.findViewById(R.id.domain_settings_form_data_imageview);
        Spinner userAgentSpinner = (Spinner) domainSettingsView.findViewById(R.id.domain_settings_user_agent_spinner);
        final TextView userAgentTextView = (TextView) domainSettingsView.findViewById(R.id.domain_settings_user_agent_textview);
        final EditText customUserAgentEditText = (EditText) domainSettingsView.findViewById(R.id.domain_settings_custom_user_agent_edittext);
        Spinner fontSizeSpinner = (Spinner) domainSettingsView.findViewById(R.id.domain_settings_font_size_spinner);

        // Initialize the database handler.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
        // The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getContext(), null, null, 0);

        // Get the database `Cursor` for this ID and move it to the first row.
        Cursor domainCursor = domainsDatabaseHelper.getCursorForId(databaseId);
        domainCursor.moveToFirst();

        // Save the `Cursor` entries as variables.
        String domainNameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
        int javaScriptEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_JAVASCRIPT));
        int firstPartyCookiesEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES));
        int thirdPartyCookiesEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES));
        int domStorageEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_DOM_STORAGE));
        int formDataEnabledInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FORM_DATA));
        final String currentUserAgentString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.USER_AGENT));
        int fontSizeInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE));

        // Create `ArrayAdapters` for the `Spinners`and their `entry values`.
        ArrayAdapter<CharSequence> userAgentArrayAdapter = ArrayAdapter.createFromResource(context, R.array.user_agent_entries, android.R.layout.simple_spinner_item);
        final ArrayAdapter<CharSequence> userAgentEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.user_agent_entry_values, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> fontSizeArrayAdapter = ArrayAdapter.createFromResource(context, R.array.default_font_size_entries, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> fontSizeEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.default_font_size_entry_values, android.R.layout.simple_spinner_item);

        // Set the drop down style for the `ArrayAdapters`.
        userAgentArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSizeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the `ArrayAdapters` for the `Spinners`.
        userAgentSpinner.setAdapter(userAgentArrayAdapter);
        fontSizeSpinner.setAdapter(fontSizeArrayAdapter);

        // Set the domain name from the the database cursor.
        domainNameEditText.setText(domainNameString);

        // Set the JavaScript status.
        if (javaScriptEnabledInt == 1) {  // JavaScript is enabled.
            javaScriptEnabledSwitch.setChecked(true);
            javaScriptImageView.setImageDrawable(getResources().getDrawable(R.drawable.javascript_enabled));
        } else {  // JavaScript is disabled.
            javaScriptEnabledSwitch.setChecked(false);
            javaScriptImageView.setImageDrawable(getResources().getDrawable(R.drawable.privacy_mode));
        }

        // Set the first-party cookies status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
        if (firstPartyCookiesEnabledInt == 1) {  // First-party cookies are enabled.
            firstPartyCookiesEnabledSwitch.setChecked(true);
            firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_enabled));
        } else {  // First-party cookies are disabled.
            firstPartyCookiesEnabledSwitch.setChecked(false);
            firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled));
        }

        // Only display third-party cookies if SDK_INT >= 21.
        if (Build.VERSION.SDK_INT >= 21) {  // Third-party cookies can be configured for API >= 21.
            // Only enable third-party-cookies if first-party cookies are enabled.
            if (firstPartyCookiesEnabledInt == 1) {  // First-party cookies are enabled.
                // Set the third-party cookies status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
                if (thirdPartyCookiesEnabledInt == 1) {  // Both first-party and third-party cookies are enabled.
                    thirdPartyCookiesEnabledSwitch.setChecked(true);
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_warning));
                } else {  // First party cookies are enabled but third-party cookies are disabled.
                    thirdPartyCookiesEnabledSwitch.setChecked(false);
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled));
                }
            } else {  // First-party cookies are disabled.
                // Set the status of third-party cookies, but disable it.
                if (thirdPartyCookiesEnabledInt == 1) {  // Third-party cookies are enabled but first-party cookies are disabled.
                    thirdPartyCookiesEnabledSwitch.setChecked(true);
                    thirdPartyCookiesEnabledSwitch.setEnabled(false);
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_ghosted));
                } else {  // Both first party and third-party cookies are disabled.
                    thirdPartyCookiesEnabledSwitch.setChecked(false);
                    thirdPartyCookiesEnabledSwitch.setEnabled(false);
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_ghosted));
                }
            }
        } else {  // Third-party cookies cannot be configured for API <= 21.
            // Hide the `LinearLayout` for third-party cookies.
            thirdPartyCookiesLinearLayout.setVisibility(View.GONE);
        }

        // Only enable DOM storage if JavaScript is enabled.
        if (javaScriptEnabledInt == 1) {  // JavaScript is enabled.
            // Set the DOM storage status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
            if (domStorageEnabledInt == 1) {  // Both JavaScript and DOM storage are enabled.
                domStorageEnabledSwitch.setChecked(true);
                domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_enabled));
            } else {  // JavaScript is enabled but DOM storage is disabled.
                domStorageEnabledSwitch.setChecked(false);
                domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled));
            }
        } else {  // JavaScript is disabled.
            // Set the status of DOM storage, but disable it.
            if (domStorageEnabledInt == 1) {  // DOM storage is enabled but JavaScript is disabled.
                domStorageEnabledSwitch.setChecked(true);
                domStorageEnabledSwitch.setEnabled(false);
                domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_ghosted));
            } else {  // Both JavaScript and DOM storage are disabled.
                domStorageEnabledSwitch.setChecked(false);
                domStorageEnabledSwitch.setEnabled(false);
                domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_ghosted));
            }
        }

        // Set the form data status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
        if (formDataEnabledInt == 1) {  // Form data is enabled.
            formDataEnabledSwitch.setChecked(true);
            formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_enabled));
        } else {  // Form data is disabled.
            formDataEnabledSwitch.setChecked(false);
            formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_disabled));
        }

        // We need to inflated a `WebView` to get the default user agent.
        // `@SuppressLint("InflateParams")` removes the warning about using `null` as the `ViewGroup`, which in this case makes sense because we don't want to display `bare_webview` on the screen.  `false` does not attach the view to the root.
        @SuppressLint("InflateParams") View bareWebViewLayout = inflater.inflate(R.layout.bare_webview, null, false);
        WebView bareWebView = (WebView) bareWebViewLayout.findViewById(R.id.bare_webview);
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
        } else if (currentUserAgentString.equals("WebView default user agent")) {  // We are using the `WebView` default user agent.
            // Set the `userAgentSpinner` selection.
            userAgentSpinner.setSelection(userAgentArrayPosition);

            // Show `userAgentTextView` and set the text.
            userAgentTextView.setVisibility(View.VISIBLE);
            userAgentTextView.setText(webViewDefaultUserAgentString);

            // Hide `customUserAgentEditText`.
            customUserAgentEditText.setVisibility(View.GONE);
        } else {  // We are using a standard user agent.
            // Set the `userAgentSpinner` selection.
            userAgentSpinner.setSelection(userAgentArrayPosition);

            // Show `userAgentTextView` and set the text.
            userAgentTextView.setVisibility(View.VISIBLE);
            userAgentTextView.setText(currentUserAgentString);

            // Hide `customUserAgentEditText`.
            customUserAgentEditText.setVisibility(View.GONE);
        }

        // Set the selected font size.
        int fontSizeArrayPosition = fontSizeEntryValuesArrayAdapter.getPosition(String.valueOf(fontSizeInt));
        fontSizeSpinner.setSelection(fontSizeArrayPosition);

        // Set the `javaScriptEnabledSwitch` `OnCheckedChangeListener()`.
        javaScriptEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {  // JavaScript is enabled.
                    // Update the JavaScript icon.
                    javaScriptImageView.setImageDrawable(getResources().getDrawable(R.drawable.javascript_enabled));

                    // Enable the DOM storage `Switch`.
                    domStorageEnabledSwitch.setEnabled(true);

                    // Update the DOM storage icon.
                    if (domStorageEnabledSwitch.isChecked()) {  // DOM storage is enabled.
                        domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_enabled));
                    } else {  // DOM storage is disabled.
                        domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled));
                    }
                } else {  // JavaScript is disabled.
                    // Update the JavaScript icon.
                    javaScriptImageView.setImageDrawable(getResources().getDrawable(R.drawable.privacy_mode));

                    // Disable the DOM storage `Switch`.
                    domStorageEnabledSwitch.setEnabled(false);

                    // Set the DOM storage icon to be ghosted.
                    domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_ghosted));
                }
            }
        });

        // Set the `firstPartyCookiesEnabledSwitch` `OnCheckedChangeListener()`.
        firstPartyCookiesEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {  // First-party cookies are enabled.
                    // Update the first-party cookies icon.
                    firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_enabled));

                    // Enable the third-party cookies `Switch`.
                    thirdPartyCookiesEnabledSwitch.setEnabled(true);

                    // Update the third-party cookies icon.
                    if (thirdPartyCookiesEnabledSwitch.isChecked()) {  // Third-party cookies are enabled.
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_warning));
                    } else {  // Third-party cookies are disabled.
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled));
                    }
                } else {  // First-party cookies are disabled.
                    // Update the first-party cookies icon.
                    firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled));

                    // Disable the third-party cookies `Switch`.
                    thirdPartyCookiesEnabledSwitch.setEnabled(false);

                    // Set the third-party cookies icon to be ghosted.
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_ghosted));
                }
            }
        });

        // Set the `thirdPartyCookiesEnabledSwitch` `OnCheckedChangeListener()`.
        thirdPartyCookiesEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the icon.
                if (isChecked) {
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_warning));
                } else {
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled));
                }
            }
        });

        // Set the `domStorageEnabledSwitch` `OnCheckedChangeListener()`.
        domStorageEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the icon.
                if (isChecked) {
                    domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_enabled));
                } else {
                    domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled));
                }
            }
        });

        // Set the `formDataEnabledSwitch` `OnCheckedChangeListener()`.
        formDataEnabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the icon.
                if (isChecked) {
                    formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_enabled));
                } else {
                    formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_disabled));
                }
            }
        });

        // Set the `userAgentSpinner` `onItemClickListener()`.
        userAgentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the new user agent string.
                String newUserAgentString = getResources().getStringArray(R.array.user_agent_entry_values)[position];

                // Set the new user agent.
                switch (newUserAgentString) {
                    case "Custom user agent":
                        // Hide `userAgentTextView`.
                        userAgentTextView.setVisibility(View.GONE);

                        // Show `customUserAgentEditText` and set `userAgentString` as the text.
                        customUserAgentEditText.setVisibility(View.VISIBLE);
                        customUserAgentEditText.setText(currentUserAgentString);
                        break;

                    case "WebView default user agent":
                        // Show `userAgentTextView` and set the text.
                        userAgentTextView.setVisibility(View.VISIBLE);
                        userAgentTextView.setText(webViewDefaultUserAgentString);

                        // Hide `customUserAgentEditText`.
                        customUserAgentEditText.setVisibility(View.GONE);
                        break;

                    default:
                        // Show `userAgentTextView` and set the text.
                        userAgentTextView.setVisibility(View.VISIBLE);
                        userAgentTextView.setText(getResources().getStringArray(R.array.user_agent_entry_values)[position]);

                        // Hide `customUserAgentEditText`.
                        customUserAgentEditText.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        return domainSettingsView;
    }
}
