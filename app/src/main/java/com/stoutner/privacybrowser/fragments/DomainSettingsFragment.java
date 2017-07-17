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
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
// We have to use `android.support.v4.app.Fragment` until minimum API >= 23.  Otherwise we cannot call `getContext()`.
import android.preference.PreferenceManager;
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
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
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
        // Inflate `domain_settings_fragment`.  `false` does not attach it to the root `container`.
        View domainSettingsView = inflater.inflate(R.layout.domain_settings_fragment, container, false);

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
        final ImageView displayWebpageImagesImageView = (ImageView) domainSettingsView.findViewById(R.id.domain_settings_display_webpage_images_imageview);
        Spinner displayWebpageImagesSpinner = (Spinner) domainSettingsView.findViewById(R.id.domain_settings_display_webpage_images_spinner);

        // Initialize the database handler.  The two `nulls` do not specify the database name or a `CursorFactory`.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(context, null, null, 0);

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
        int displayImagesInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.DISPLAY_IMAGES));

        // Create `ArrayAdapters` for the `Spinners`and their `entry values`.
        ArrayAdapter<CharSequence> userAgentArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_user_agent_entries, R.layout.spinner_item);
        final ArrayAdapter<CharSequence> userAgentEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_user_agent_entry_values, R.layout.spinner_item);
        ArrayAdapter<CharSequence> fontSizeArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_font_size_entries, R.layout.spinner_item);
        ArrayAdapter<CharSequence> fontSizeEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.domain_settings_font_size_entry_values, R.layout.spinner_item);
        final ArrayAdapter<CharSequence> displayImagesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.display_website_images_array, R.layout.spinner_item);

        // Set the `DropDownViewResource` on the `Spinners`.
        userAgentArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        fontSizeArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        displayImagesArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Set the `ArrayAdapters` for the `Spinners`.
        userAgentSpinner.setAdapter(userAgentArrayAdapter);
        fontSizeSpinner.setAdapter(fontSizeArrayAdapter);
        displayWebpageImagesSpinner.setAdapter(displayImagesArrayAdapter);

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

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_dark));
            } else {
                firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_light));
            }
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

                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_dark));
                    } else {
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_light));
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
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_ghosted_dark));
                } else {
                    thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_ghosted_light));
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
                // Set the DOM storage switch to off.
                domStorageEnabledSwitch.setChecked(false);

                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled_dark));
                } else {
                    domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled_light));
                }
            }
        } else {  // JavaScript is disabled.
            // Set the checked status of DOM storage.
            if (domStorageEnabledInt == 1) {  // DOM storage is enabled but JavaScript is disabled.
                domStorageEnabledSwitch.setChecked(true);
            } else {  // Both JavaScript and DOM storage are disabled.
                domStorageEnabledSwitch.setChecked(false);
            }

            // Disable `domStorageEnabledSwitch`.
            domStorageEnabledSwitch.setEnabled(false);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_ghosted_dark));
            } else {
                domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_ghosted_light));
            }
        }

        // Set the form data status.  Once minimum API >= 21 we can use a selector as the tint mode instead of specifying different icons.
        if (formDataEnabledInt == 1) {  // Form data is enabled.
            formDataEnabledSwitch.setChecked(true);
            formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_enabled));
        } else {  // Form data is disabled.
            // Set the form data switch to off.
            formDataEnabledSwitch.setChecked(false);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_disabled_dark));
            } else {
                formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_disabled_light));
            }
        }

        // We need to inflated a `WebView` to get the default user agent.
        // `@SuppressLint("InflateParams")` removes the warning about using `null` as the `ViewGroup`, which in this case makes sense because we don't want to display `bare_webview` on the screen.  `false` does not attach the view to the root.
        @SuppressLint("InflateParams") View bareWebViewLayout = inflater.inflate(R.layout.bare_webview, null, false);
        WebView bareWebView = (WebView) bareWebViewLayout.findViewById(R.id.bare_webview);
        final String webViewDefaultUserAgentString = bareWebView.getSettings().getUserAgentString();

        // Get a handle for the shared preference.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Store the default user agent string values.
        final String defaultUserAgentString = sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0");
        final String defaultCustomUserAgentString = sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0");

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

        // Set the selected font size.
        int fontSizeArrayPosition = fontSizeEntryValuesArrayAdapter.getPosition(String.valueOf(fontSizeInt));
        fontSizeSpinner.setSelection(fontSizeArrayPosition);

        // Set the selected display website images mode.
        displayWebpageImagesSpinner.setSelection(displayImagesInt);

        // Set the display website images icon.
        switch (displayImagesInt) {
            case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_SYSTEM_DEFAULT:
                if (MainWebViewActivity.displayWebpageImagesBoolean) {
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_dark));
                    } else {
                        displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_light));
                    }
                } else {
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_dark));
                    } else {
                        displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_light));
                    }
                }
                break;

            case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_ENABLED:
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_dark));
                } else {
                    displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_light));
                }
                break;

            case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_DISABLED:
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_dark));
                } else {
                    displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_light));
                }
                break;
        }


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
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled_dark));
                        } else {
                            domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled_light));
                        }
                    }
                } else {  // JavaScript is disabled.
                    // Update the JavaScript icon.
                    javaScriptImageView.setImageDrawable(getResources().getDrawable(R.drawable.privacy_mode));

                    // Disable the DOM storage `Switch`.
                    domStorageEnabledSwitch.setEnabled(false);

                    // Set the DOM storage icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_ghosted_dark));
                    } else {
                        domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_ghosted_light));
                    }
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
                        // Set the third-party cookies icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_dark));
                        } else {
                            thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_light));
                        }
                    }
                } else {  // First-party cookies are disabled.
                    // Update the first-party cookies icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_dark));
                    } else {
                        firstPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_light));
                    }

                    // Disable the third-party cookies `Switch`.
                    thirdPartyCookiesEnabledSwitch.setEnabled(false);

                    // Set the third-party cookies icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_ghosted_dark));
                    } else {
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_ghosted_light));
                    }
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
                    // Update the third-party cookies icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_dark));
                    } else {
                        thirdPartyCookiesImageView.setImageDrawable(getResources().getDrawable(R.drawable.cookies_disabled_light));
                    }
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
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled_dark));
                    } else {
                        domStorageImageView.setImageDrawable(getResources().getDrawable(R.drawable.dom_storage_disabled_light));
                    }
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
                    // Set the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_disabled_dark));
                    } else {
                        formDataImageView.setImageDrawable(getResources().getDrawable(R.drawable.form_data_disabled_light));
                    }
                }
            }
        });

        // Set the `userAgentSpinner` `onItemClickListener()`.
        userAgentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the new user agent string.
                String newUserAgentString = getResources().getStringArray(R.array.domain_settings_user_agent_entry_values)[position];

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

        // Set the `displayImagesSwitch` `onItemClickListener()`.
        displayWebpageImagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update the icon.
                switch (position) {
                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_SYSTEM_DEFAULT:
                        if (MainWebViewActivity.displayWebpageImagesBoolean) {
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_dark));
                            } else {
                                displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_light));
                            }
                        } else {
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_dark));
                            } else {
                                displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_light));
                            }
                        }
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_ENABLED:
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_dark));
                        } else {
                            displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_enabled_light));
                        }
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_DISABLED:
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_dark));
                        } else {
                            displayWebpageImagesImageView.setImageDrawable(getResources().getDrawable(R.drawable.images_disabled_light));
                        }
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
