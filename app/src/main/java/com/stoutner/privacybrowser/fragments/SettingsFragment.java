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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.stoutner.privacybrowser.R;

public class SettingsFragment extends PreferenceFragment {
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
    private SharedPreferences savedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Initialize savedPreferences.
        savedPreferences = getPreferenceScreen().getSharedPreferences();

        // Get handles for the preferences we need to modify.
        final Preference javaScriptEnabled = findPreference("javascript_enabled");
        final Preference firstPartyCookiesEnabled = findPreference("first_party_cookies_enabled");
        final Preference thirdPartyCookiesEnabled = findPreference("third_party_cookies_enabled");
        final Preference domStorageEnabled = findPreference("dom_storage_enabled");
        final Preference userAgentPreference = findPreference("user_agent");
        final Preference customUserAgent = findPreference("custom_user_agent");
        final Preference torHomepagePreference = findPreference("tor_homepage");
        final Preference torSearchPreference = findPreference("tor_search");
        final Preference torSearchCustomURLPreference = findPreference("tor_search_custom_url");
        final Preference searchPreference = findPreference("search");
        final Preference searchCustomURLPreference = findPreference("search_custom_url");
        final Preference hideSystemBarsPreference = findPreference("hide_system_bars");
        final Preference translucentNavigationBarPreference = findPreference("translucent_navigation_bar");
        final Preference homepagePreference = findPreference("homepage");
        final Preference defaultFontSizePreference = findPreference("default_font_size");

        // Set dependencies.
        domStorageEnabled.setDependency("javascript_enabled");
        torHomepagePreference.setDependency("proxy_through_orbot");
        torSearchPreference.setDependency("proxy_through_orbot");
        hideSystemBarsPreference.setDependency("enable_full_screen_browsing_mode");

        // Get strings from the preferences.
        String torSearchString = savedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");
        String searchString = savedPreferences.getString("search", "https://duckduckgo.com/html/?q=");

        // Get booleans from the preferences.
        boolean javaScriptEnabledBoolean = savedPreferences.getBoolean("javascript_enabled", false);
        boolean firstPartyCookiesEnabledBoolean = savedPreferences.getBoolean("first_party_cookies_enabled", false);
        boolean thirdPartyCookiesEnabledBoolean = savedPreferences.getBoolean("third_party_cookies_enabled", false);

        // Only enable `third_party_cookies_enabled` if `first_party_cookies_enabled` is `true` and API >= 21.
        thirdPartyCookiesEnabled.setEnabled(firstPartyCookiesEnabledBoolean && (Build.VERSION.SDK_INT >= 21));

        // We need to inflated a `WebView` to get the default user agent.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // `@SuppressLint("InflateParams")` removes the warning about using `null` as the `ViewGroup`, which in this case makes sense because we don't want to display `bare_webview` on the screen.  `false` does not attach the view to the root.
        @SuppressLint("InflateParams") View bareWebViewLayout = inflater.inflate(R.layout.bare_webview, null, false);
        final WebView bareWebView = (WebView) bareWebViewLayout.findViewById(R.id.bare_webview);

        // Set the current user-agent as the summary text for the "user_agent" preference when the preference screen is loaded.
        switch (savedPreferences.getString("user_agent", "PrivacyBrowser/1.0")) {
            case "WebView default user agent":
                // Get the user agent text from the webview (which changes based on the version of Android and WebView installed).
                userAgentPreference.setSummary(bareWebView.getSettings().getUserAgentString());
                break;

            case "Custom user agent":
                // We can't use the string from the array because it is referenced in code and can't be translated.
                userAgentPreference.setSummary(R.string.custom_user_agent);
                break;

            default:
                // Display the user agent from the preference as the summary text.
                userAgentPreference.setSummary(savedPreferences.getString("user_agent", "PrivacyBrowser/1.0"));
                break;
        }

        // Set the summary text for "custom_user_agent" (the default is "PrivacyBrowser/1.0") and enable it if "user_agent" it set to "Custom user agent".
        customUserAgent.setSummary(savedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
        customUserAgent.setEnabled(userAgentPreference.getSummary().equals("Custom user agent"));


        // Set the Tor homepage URL as the summary text for the `tor_homepage` preference when the preference screen is loaded.  The default is DuckDuckGo: `https://3g2upl4pq6kufc4m.onion`.
        torHomepagePreference.setSummary(savedPreferences.getString("tor_homepage", "https://3g2upl4pq6kufc4m.onion"));


        // Set the Tor search URL as the summary text for the Tor preference when the preference screen is loaded.  The default is `https://3g2upl4pq6kufc4m.onion/html/?q=`
        if (torSearchString.equals("Custom URL")) {
            // Use R.string.custom_url, which will be translated, instead of the array value, which will not.
            torSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            torSearchPreference.setSummary(torSearchString);
        }

        // Set the summary text for `tor_search_custom_url`.  The default is `""`.
        torSearchCustomURLPreference.setSummary(savedPreferences.getString("tor_search_custom_url", ""));

        // Enable the Tor custom URL search options only if proxying through Orbot and the search is set to `Custom URL`.
        torSearchCustomURLPreference.setEnabled(savedPreferences.getBoolean("proxy_through_orbot", false) && torSearchString.equals("Custom URL"));


        // Set the search URL as the summary text for the search preference when the preference screen is loaded.  The default is `https://duckduckgo.com/html/?q=`.
        if (searchString.equals("Custom URL")) {
            // Use R.string.custom_url, which will be translated, instead of the array value, which will not.
            searchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            searchPreference.setSummary(searchString);
        }

        // Set the summary text for `search_custom_url` (the default is `""`) and enable it if `search` is set to `Custom URL`.
        searchCustomURLPreference.setSummary(savedPreferences.getString("search_custom_url", ""));
        searchCustomURLPreference.setEnabled(searchString.equals("Custom URL"));


        // Enable `transparent_navigation_bar` only if full screen browsing mode is enabled and `hide_system_bars` is disabled.
        translucentNavigationBarPreference.setEnabled(savedPreferences.getBoolean("enable_full_screen_browsing_mode", false) && !savedPreferences.getBoolean("hide_system_bars", false));


        // Set the homepage URL as the summary text for the `Homepage` preference when the preference screen is loaded.  The default is `https://duckduckgo.com`.
        homepagePreference.setSummary(savedPreferences.getString("homepage", "https://duckduckgo.com"));

        // Set the default font size as the summary text for the `Default Font Size` preference when the preference screen is loaded.  The default is `100`.
        defaultFontSizePreference.setSummary(savedPreferences.getString("default_font_size", "100") + "%%");


        // Set the `javascript_enabled` icon.
        if (javaScriptEnabledBoolean) {
            javaScriptEnabled.setIcon(R.drawable.javascript_enabled);
        } else {
            javaScriptEnabled.setIcon(R.drawable.privacy_mode);
        }

        // Set the `first_party_cookies_enabled` icon.
        if (firstPartyCookiesEnabledBoolean) {
            firstPartyCookiesEnabled.setIcon(R.drawable.cookies_enabled);
        } else {
            firstPartyCookiesEnabled.setIcon(R.drawable.cookies_disabled);
        }

        // Set the `third_party_cookies_enabled` icon.
        if (firstPartyCookiesEnabledBoolean && Build.VERSION.SDK_INT >= 21) {
            if (thirdPartyCookiesEnabledBoolean) {
                thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_warning);
            } else {
                thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_disabled);
            }
        } else {
            thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_ghosted);
        }

        // Set the `dom_storage_enabled` icon.
        if (javaScriptEnabledBoolean) {
            if (savedPreferences.getBoolean("dom_storage_enabled", false)) {
                domStorageEnabled.setIcon(R.drawable.dom_storage_enabled);
            } else {
                domStorageEnabled.setIcon(R.drawable.dom_storage_disabled);
            }
        } else {
            domStorageEnabled.setIcon(R.drawable.dom_storage_ghosted);
        }

        // Listen for preference changes.
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  We know.
            @SuppressLint("SetJavaScriptEnabled")
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {
                    case "javascript_enabled":
                        // Update the icons.
                        if (sharedPreferences.getBoolean("javascript_enabled", false)) {
                            // Update the icon for `javascript_enabled`.
                            javaScriptEnabled.setIcon(R.drawable.javascript_enabled);

                            // Update the icon for `dom_storage_enabled`.
                            if (sharedPreferences.getBoolean("dom_storage_enabled", false)) {
                                domStorageEnabled.setIcon(R.drawable.dom_storage_enabled);
                            } else {
                                domStorageEnabled.setIcon(R.drawable.dom_storage_disabled);
                            }
                        } else {  // `javascript_enabled` is `false`.
                            // Update the icon for `javascript_enabled`.
                            javaScriptEnabled.setIcon(R.drawable.privacy_mode);

                            // Set the icon for `dom_storage_disabled` to be ghosted.
                            domStorageEnabled.setIcon(R.drawable.dom_storage_ghosted);
                        }
                        break;

                    case "first_party_cookies_enabled":
                        // Update the icons for `first_party_cookies_enabled` and `third_party_cookies_enabled`.
                        if (sharedPreferences.getBoolean("first_party_cookies_enabled", false)) {
                            // Set the icon for `first_party_cookies_enabled`.
                            firstPartyCookiesEnabled.setIcon(R.drawable.cookies_enabled);

                            // Update the icon for `third_party_cookies_enabled`.
                            if (Build.VERSION.SDK_INT >= 21) {
                                if (sharedPreferences.getBoolean("third_party_cookies_enabled", false)) {
                                    thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_warning);
                                } else {
                                    thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_disabled);
                                }
                            } else {
                                thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_ghosted);
                            }
                        } else {  // `first_party_cookies_enabled` is `false`.
                            // Update the icon for `first_party_cookies_enabled`.
                            firstPartyCookiesEnabled.setIcon(R.drawable.cookies_disabled);

                            // Set the icon for `third_party_cookies_enabled` to be ghosted.
                            thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_ghosted);
                        }

                        // Enable `third_party_cookies_enabled` if `first_party_cookies_enabled` is `true` and API >= 21.
                        thirdPartyCookiesEnabled.setEnabled(sharedPreferences.getBoolean("first_party_cookies_enabled", false) && (Build.VERSION.SDK_INT >= 21));
                        break;

                    case "third_party_cookies_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("third_party_cookies_enabled", false)) {
                            thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_warning);
                        } else {
                            thirdPartyCookiesEnabled.setIcon(R.drawable.cookies_disabled);
                        }
                        break;

                    case "dom_storage_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("dom_storage_enabled", false)) {
                            domStorageEnabled.setIcon(R.drawable.dom_storage_enabled);
                        } else {
                            domStorageEnabled.setIcon(R.drawable.dom_storage_disabled);
                        }
                        break;

                    case "user_agent":
                        String userAgentString = sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0");

                        switch (userAgentString) {
                            case "WebView default user agent":
                                // Display the user agent as the summary text for `userAgentPreference`, and disable `customUserAgent`.
                                userAgentPreference.setSummary(bareWebView.getSettings().getUserAgentString());
                                customUserAgent.setEnabled(false);
                                break;

                            case "Custom user agent":
                                // Display `Custom user agent` as the summary text for `userAgentPreference`, and enable `customUserAgent`.
                                userAgentPreference.setSummary(R.string.custom_user_agent);
                                customUserAgent.setEnabled(true);
                                break;

                            default:
                                // Display the user agent as the summary text for `userAgentPreference`, and disable `customUserAgent`.
                                userAgentPreference.setSummary(sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0"));
                                customUserAgent.setEnabled(false);
                                break;
                        }
                        break;

                    case "custom_user_agent":
                        // Set the new custom user agent as the summary text for `custom_user_agent`.  The default is `PrivacyBrowser/1.0`.
                        customUserAgent.setSummary(sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
                        break;

                    case "proxy_through_orbot":
                        // Get current settings.
                        boolean currentProxyThroughOrbot = sharedPreferences.getBoolean("proxy_through_orbot", false);
                        String currentTorSearchString = sharedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");

                        // Enable the Tor custom URL search option only if `currentProxyThroughOrbot` is true and the search is set to `Custom URL`.
                        torSearchCustomURLPreference.setEnabled(currentProxyThroughOrbot && currentTorSearchString.equals("Custom URL"));
                        break;

                    case "tor_homepage":
                        // Set the new tor homepage URL as the summary text for the `tor_homepage` preference.  The default is DuckDuckGo:  `https://3g2upl4pq6kufc4m.onion`.
                        torHomepagePreference.setSummary(sharedPreferences.getString("tor_homepage", "https://3g2upl4pq6kufc4m.onion"));
                        break;

                    case "tor_search":
                        // Get the present search string.
                        String presentTorSearchString = sharedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");

                        // Set the summary text for `tor_search`.
                        if (presentTorSearchString.equals("Custom URL")) {
                            // Use R.string.custom_url, which is translated, instead of the array value, which isn't.
                            torSearchPreference.setSummary(R.string.custom_url);
                        } else {
                            // Set the array value as the summary text.
                            torSearchPreference.setSummary(presentTorSearchString);
                        }

                        // Set the status of `torJavaScriptDisabledSearchCustomURLPreference`.
                        torSearchCustomURLPreference.setEnabled(presentTorSearchString.equals("Custom URL"));
                        break;

                    case "tor_search_custom_url":
                        // Set the summary text for `tor_search_custom_url`.
                        torSearchCustomURLPreference.setSummary(sharedPreferences.getString("tor_search_custom_url", ""));
                        break;

                    case "search":
                        String newSearchString = sharedPreferences.getString("search", "https://duckduckgo.com/html/?q=");
                        if (newSearchString.equals("Custom URL")) {  // Set the summary text to `R.string.custom_url`, which is translated.
                            searchPreference.setSummary(R.string.custom_url);
                        } else {  // Set the new search URL as the summary text for the JavaScript-disabled search preference.
                            searchPreference.setSummary(newSearchString);
                        }

                        // Enable or disable `searchCustomURLPreference`.
                        searchCustomURLPreference.setEnabled(newSearchString.equals("Custom URL"));
                        break;

                    case "search_custom_url":
                        // Set the new custom search URL as the summary text for `search_custom_url`.  The default is `""`.
                        searchCustomURLPreference.setSummary(sharedPreferences.getString("search_custom_url", ""));
                        break;

                    case "enable_full_screen_browsing_mode":
                        // Enable `transparent_navigation_bar` only if full screen browsing mode is enabled and `hide_system_bars` is disabled.
                        translucentNavigationBarPreference.setEnabled(sharedPreferences.getBoolean("enable_full_screen_browsing_mode", false) && !sharedPreferences.getBoolean("hide_system_bars", false));
                        break;

                    case "hide_system_bars":
                        // Enable `translucentNavigationBarPreference` if `hide_system_bars` is disabled.
                        translucentNavigationBarPreference.setEnabled(!sharedPreferences.getBoolean("hide_system_bars", false));
                        break;

                    case "homepage":
                        // Set the new homepage URL as the summary text for the Homepage preference.  The default is `https://www.duckduckgo.com`.
                        homepagePreference.setSummary(sharedPreferences.getString("homepage", "https://www.duckduckgo.com"));
                        break;

                    case "default_font_size":
                        // Update the summary text of `default_font_size`.
                        defaultFontSizePreference.setSummary(sharedPreferences.getString("default_font_size", "100") + "%%");
                        break;

                    default:
                        // If no match, do nothing.
                        break;
                }
            }
        };

        // Register the listener.
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    // It is necessary to re-register the listener on every resume or it will randomly stop working because apps can be paused and resumed at any time
    // even while running in the foreground.
    @Override
    public void onPause() {
        super.onPause();
        savedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }
}
