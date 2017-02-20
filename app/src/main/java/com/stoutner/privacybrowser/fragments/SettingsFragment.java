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

package com.stoutner.privacybrowser.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
        final Preference domStorageEnabled = findPreference("dom_storage_enabled");
        final Preference thirdPartyCookiesEnabled = findPreference("third_party_cookies_enabled");
        final Preference userAgentPreference = findPreference("user_agent");
        final Preference customUserAgent = findPreference("custom_user_agent");
        final Preference javaScriptDisabledSearchPreference = findPreference("javascript_disabled_search");
        final Preference javaScriptDisabledSearchCustomURLPreference = findPreference("javascript_disabled_search_custom_url");
        final Preference javaScriptEnabledSearchPreference = findPreference("javascript_enabled_search");
        final Preference javaScriptEnabledSearchCustomURLPreference = findPreference("javascript_enabled_search_custom_url");
        final Preference hideSystemBarsPreference = findPreference("hide_system_bars");
        final Preference translucentNavigationBarPreference = findPreference("translucent_navigation_bar");
        final Preference torHomepagePreference = findPreference("tor_homepage");
        final Preference torJavaScriptDisabledSearchPreference = findPreference("tor_javascript_disabled_search");
        final Preference torJavaScriptDisabledSearchCustomURLPreference = findPreference("tor_javascript_disabled_search_custom_url");
        final Preference torJavaScriptEnabledSearchPreference = findPreference("tor_javascript_enabled_search");
        final Preference torJavaScriptEnabledSearchCustomURLPreference = findPreference("tor_javascript_enabled_search_custom_url");
        final Preference homepagePreference = findPreference("homepage");
        final Preference defaultFontSizePreference = findPreference("default_font_size");

        // Get booleans from the preferences.
        final boolean fullScreenBrowsingModeEnabled = savedPreferences.getBoolean("enable_full_screen_browsing_mode", false);
        final boolean proxyThroughOrbot = savedPreferences.getBoolean("proxy_through_orbot", false);

        // Get strings from the preferences.
        String javaScriptDisabledSearchString = savedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q=");
        String javaScriptEnabledSearchString = savedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q=");
        String torJavaScriptDisabledSearchString = savedPreferences.getString("tor_javascript_disabled_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");
        String torJavaScriptEnabledSearchString = savedPreferences.getString("tor_javascript_enabled_search", "https://3g2upl4pq6kufc4m.onion/?q=");
        String defaultFontSizeString = savedPreferences.getString("default_font_size", "100");

        // Allow the user to access "dom_storage_enabled" if "javascript_enabled" is enabled.  The default is false.
        domStorageEnabled.setEnabled(savedPreferences.getBoolean("javascript_enabled", false));

        // Allow the user to access "third_party_cookies_enabled" if "first_party_cookies_enabled" is enabled.  The default is false.
        thirdPartyCookiesEnabled.setEnabled(savedPreferences.getBoolean("first_party_cookies_enabled", false));


        // We need an inflated `WebView` to get the default user agent.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // `@SuppressLint("InflateParams")` removes the warning about using `null` as the `ViewGroup`, which in this case makes sense because we don't want to display `bare_webview` on the screen.
        // `false` does not attach the view to the root.
        @SuppressLint("InflateParams") View bareWebViewLayout = inflater.inflate(R.layout.bare_webview, null, false);
        final WebView bareWebView = (WebView) bareWebViewLayout.findViewById(R.id.bare_webview);

        // Set the current user-agent as the summary text for the "user_agent" preference when the preference screen is loaded.
        switch (savedPreferences.getString("user_agent", "PrivacyBrowser/1.0")) {
            case "Default user agent":
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


        // Set the JavaScript-disabled search URL as the summary text for the JavaScript-disabled search preference when the preference screen is loaded.  The default is `https://duckduckgo.com/html/?q=`.
        if (javaScriptDisabledSearchString.equals("Custom URL")) {
            // Use R.string.custom_url, which will be translated, instead of the array value, which will not.
            javaScriptDisabledSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            javaScriptDisabledSearchPreference.setSummary(javaScriptDisabledSearchString);
        }

        // Set the summary text for `javascript_disabled_search_custom_url` (the default is `""`) and enable it if `javascript_disabled_search` is set to `Custom URL`.
        javaScriptDisabledSearchCustomURLPreference.setSummary(savedPreferences.getString("javascript_disabled_search_custom_url", ""));
        javaScriptDisabledSearchCustomURLPreference.setEnabled(javaScriptDisabledSearchString.equals("Custom URL"));


        // Set the JavaScript-enabled search URL as the summary text for the JavaScript-enabled search preference when the preference screen is loaded.  The default is `https://duckduckgo.com/?q=`.
        if (javaScriptEnabledSearchString.equals("Custom URL")) {
            // If set to "Custom URL", use R.string.custom_url, which will be translated, instead of the array value, which will not.
            javaScriptEnabledSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            javaScriptEnabledSearchPreference.setSummary(javaScriptEnabledSearchString);
        }

        // Set the summary text for `javascript_enabled_search_custom_url` (the default is `""`) and enable it if `javascript_enabled_search` is set to `Custom URL`.
        javaScriptEnabledSearchCustomURLPreference.setSummary(savedPreferences.getString("javascript_enabled_search_custom_url", ""));
        javaScriptEnabledSearchCustomURLPreference.setEnabled(javaScriptEnabledSearchString.equals("Custom URL"));


        // Enable the full screen options if full screen browsing mode is enabled.
        if (!fullScreenBrowsingModeEnabled) {
            // Disable the full screen options.
            hideSystemBarsPreference.setEnabled(false);
            translucentNavigationBarPreference.setEnabled(false);
        } else {
            // Disable `transparent_navigation_bar` if `hide_system_bars` is `true`.
            translucentNavigationBarPreference.setEnabled(!savedPreferences.getBoolean("hide_system_bars", false));
        }


        // Set the Tor homepage URL as the summary text for the `tor_homepage` preference when the preference screen is loaded.  The default is DuckDuckGo: `https://3g2upl4pq6kufc4m.onion`.
        torHomepagePreference.setSummary(savedPreferences.getString("tor_homepage", "https://3g2upl4pq6kufc4m.onion"));


        // Set the Tor JavaScript-disabled search URL as the summary text for the Tor JavaScript-disabled search preference when the preference screen is loaded.  The default is `https://3g2upl4pq6kufc4m.onion/html/?q=`
        if (torJavaScriptDisabledSearchString.equals("Custom URL")) {
            // Use R.string.custom_url, which will be translated, instead of the array value, which will not.
            torJavaScriptDisabledSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            torJavaScriptDisabledSearchPreference.setSummary(torJavaScriptDisabledSearchString);
        }

        // Set the summary text for `tor_javascript_disabled_search_custom_url`.  The default is `""`.
        torJavaScriptDisabledSearchCustomURLPreference.setSummary(savedPreferences.getString("tor_javascript_disabled_search_custom_url", ""));


        // Set the Tor JavaScript-enabled search URL as the summary text for the Tor Javascript-enabled search preference when the preference screen is loaded.  The default is `https://3g2upl4pq6kufc4m.onion/?q=`.
        if (torJavaScriptEnabledSearchString.equals("Custom URL")) {
            // Use R.string.custom_url, which will be translated, instead of the array value, which will not.
            torJavaScriptEnabledSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            torJavaScriptEnabledSearchPreference.setSummary(torJavaScriptEnabledSearchString);
        }

        // Set the summary text for `tor_javascript_enabled_search_custom_url`.  The default is `""`.
        torJavaScriptEnabledSearchCustomURLPreference.setSummary(savedPreferences.getString("tor_javascript_enabled_search_custom_url", ""));


        // Enable the Tor preferences only if `proxy_through_orbot` is enabled.  The default is `false`.
        torHomepagePreference.setEnabled(proxyThroughOrbot);
        torJavaScriptDisabledSearchPreference.setEnabled(proxyThroughOrbot);
        torJavaScriptEnabledSearchPreference.setEnabled(proxyThroughOrbot);

        // Enable the Tor custom URL search options only if `proxyThroughOrbot` is true and the search is set to `Custom URL`.
        torJavaScriptDisabledSearchCustomURLPreference.setEnabled(proxyThroughOrbot && torJavaScriptDisabledSearchString.equals("Custom URL"));
        torJavaScriptEnabledSearchCustomURLPreference.setEnabled(proxyThroughOrbot && torJavaScriptEnabledSearchString.equals("Custom URL"));


        // Set the homepage URL as the summary text for the `Homepage` preference when the preference screen is loaded.  The default is `https://www.duckduckgo.com`.
        homepagePreference.setSummary(savedPreferences.getString("homepage", "https://www.duckduckgo.com"));

        // Set the default font size as the summary text for the `Default Font Size` preference when the preference screen is loaded.  The default is `100`.
        defaultFontSizePreference.setSummary(defaultFontSizeString + "%%");


        // Listen for preference changes.
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  We know.
            @SuppressLint("SetJavaScriptEnabled")
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {
                    case "javascript_enabled":
                        // Toggle the state of the `dom_storage_enabled` preference.  The default is `false`.
                        final Preference domStorageEnabled = findPreference("dom_storage_enabled");
                        domStorageEnabled.setEnabled(sharedPreferences.getBoolean("javascript_enabled", false));
                        break;

                    case "first_party_cookies_enabled":
                        // Toggle the state of the `third_party_cookies_enabled` preference.  The default is `false`.
                        final Preference thirdPartyCookiesEnabled = findPreference("third_party_cookies_enabled");
                        thirdPartyCookiesEnabled.setEnabled(sharedPreferences.getBoolean("first_party_cookies_enabled", false));
                        break;

                    case "user_agent":
                        String userAgentString = sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0");

                        switch (userAgentString) {
                            case "Default user agent":
                                // Display the user agent as the summary text for `userAgentPreference`, and disable `customUserAgent`.
                                userAgentPreference.setSummary(bareWebView.getSettings().getUserAgentString());
                                customUserAgent.setEnabled(false);
                                break;

                            case "Custom user agent":
                                // Display "Custom user agent" as the summary text for userAgentPreference, and enable customUserAgent.
                                userAgentPreference.setSummary(R.string.custom_user_agent);
                                customUserAgent.setEnabled(true);
                                break;

                            default:
                                // Display the user agent as the summary text for userAgentPreference, and disable customUserAgent.
                                userAgentPreference.setSummary(sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0"));
                                customUserAgent.setEnabled(false);
                                break;
                        }
                        break;

                    case "custom_user_agent":
                        // Set the new custom user agent as the summary text for `custom_user_agent`.  The default is `PrivacyBrowser/1.0`.
                        customUserAgent.setSummary(sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
                        break;

                    case "javascript_disabled_search":
                        String newJavaScriptDisabledSearchString = sharedPreferences.getString("javascript_disabled_search", "https://duckduckgo.com/html/?q=");
                        if (newJavaScriptDisabledSearchString.equals("Custom URL")) {  // Set the summary text to `R.string.custom_url`, which is translated.
                            javaScriptDisabledSearchPreference.setSummary(R.string.custom_url);
                        } else {  // Set the new search URL as the summary text for the JavaScript-disabled search preference.
                            javaScriptDisabledSearchPreference.setSummary(newJavaScriptDisabledSearchString);
                        }

                        // Enable or disable javaScriptDisabledSearchCustomURLPreference.
                        javaScriptDisabledSearchCustomURLPreference.setEnabled(newJavaScriptDisabledSearchString.equals("Custom URL"));
                        break;

                    case "javascript_disabled_search_custom_url":
                        // Set the new custom search URL as the summary text for `javascript_disabled_search_custom_url`.  The default is `""`.
                        javaScriptDisabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("javascript_disabled_search_custom_url", ""));
                        break;

                    case "javascript_enabled_search":
                        String newJavaScriptEnabledSearchString = sharedPreferences.getString("javascript_enabled_search", "https://duckduckgo.com/?q=");
                        if (newJavaScriptEnabledSearchString.equals("Custom URL")) {  // Set the summary text to `R.string.custom_url`, which is translated.
                            javaScriptEnabledSearchPreference.setSummary(R.string.custom_url);
                        } else {  // Set the new search URL as the summary text for the JavaScript-enabled search preference..
                            javaScriptEnabledSearchPreference.setSummary(newJavaScriptEnabledSearchString);
                        }

                        // Enable or disable javaScriptEnabledSearchCustomURLPreference.
                        javaScriptEnabledSearchCustomURLPreference.setEnabled(newJavaScriptEnabledSearchString.equals("Custom URL"));
                        break;

                    case "javascript_enabled_search_custom_url":
                        // Set the new custom search URL as the summary text for `javascript_enabled_search_custom_url`.  The default is `""`.
                        javaScriptEnabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("javascript_enabled_search_custom_url", ""));
                        break;

                    case "enable_full_screen_browsing_mode":
                        boolean newFullScreenBrowsingModeEnabled = sharedPreferences.getBoolean("enable_full_screen_browsing_mode", false);
                        if (newFullScreenBrowsingModeEnabled) {
                            // Enable `hideSystemBarsPreference`.
                            hideSystemBarsPreference.setEnabled(true);

                            // Only enable `transparent_navigation_bar` if `hide_system_bars` is `false`.
                            translucentNavigationBarPreference.setEnabled(!sharedPreferences.getBoolean("hide_system_bars", false));
                        } else {
                            // Disable the full screen options.
                            hideSystemBarsPreference.setEnabled(false);
                            translucentNavigationBarPreference.setEnabled(false);
                        }
                        break;

                    case "proxy_through_orbot":
                        // Get current settings.
                        boolean currentProxyThroughOrbot = sharedPreferences.getBoolean("proxy_through_orbot", false);
                        String currentTorJavaScriptDisabledSearchString = sharedPreferences.getString("tor_javascript_disabled_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");
                        String currentTorJavaScriptEnabledSearchString = sharedPreferences.getString("tor_javascript_enabled_search", "https://3g2upl4pq6kufc4m.onion/?q=");

                        // Enable the Tor preferences only if `proxy_through_orbot` is enabled.  The default is `false`.
                        torHomepagePreference.setEnabled(currentProxyThroughOrbot);
                        torJavaScriptDisabledSearchPreference.setEnabled(currentProxyThroughOrbot);
                        torJavaScriptEnabledSearchPreference.setEnabled(currentProxyThroughOrbot);

                        // Enable the Tor custom URL search options only if `currentProxyThroughOrbot` is true and the search is set to `Custom URL`.
                        torJavaScriptDisabledSearchCustomURLPreference.setEnabled(currentProxyThroughOrbot && currentTorJavaScriptDisabledSearchString.equals("Custom URL"));
                        torJavaScriptEnabledSearchCustomURLPreference.setEnabled(currentProxyThroughOrbot && currentTorJavaScriptEnabledSearchString.equals("Custom URL"));
                        break;

                    case "tor_homepage":
                        // Set the new tor homepage URL as the summary text for the `tor_homepage` preference.  The default is DuckDuckGo:  `https://3g2upl4pq6kufc4m.onion`.
                        torHomepagePreference.setSummary(sharedPreferences.getString("tor_homepage", "https://3g2upl4pq6kufc4m.onion"));
                        break;

                    case "tor_javascript_disabled_search":
                        // Get the present search string.
                        String presentTorJavaScriptDisabledSearchString = sharedPreferences.getString("tor_javascript_disabled_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");

                        // Set the summary text for `tor_javascript_disabled_search`.
                        if (presentTorJavaScriptDisabledSearchString.equals("Custom URL")) {
                            // Use R.string.custom_url, which is translated, instead of the array value, which isn't.
                            torJavaScriptDisabledSearchPreference.setSummary(R.string.custom_url);
                        } else {
                            // Set the array value as the summary text.
                            torJavaScriptDisabledSearchPreference.setSummary(presentTorJavaScriptDisabledSearchString);
                        }

                        // Set the status of `torJavaScriptDisabledSearchCustomURLPreference`.
                        torJavaScriptDisabledSearchCustomURLPreference.setEnabled(presentTorJavaScriptDisabledSearchString.equals("Custom URL"));
                        break;

                    case "tor_javascript_disabled_search_custom_url":
                        // Set the summary text for `tor_javascript_disabled_search_custom_url`.
                        torJavaScriptDisabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("tor_javascript_disabled_search_custom_url", ""));
                        break;

                    case "tor_javascript_enabled_search":
                        // Get the present search string.
                        String presentTorJavaScriptEnabledSearchString = sharedPreferences.getString("tor_javascript_enabled_search", "https://3g2upl4pq6kufc4m.onion/?q=");

                        // Set the summary text for `tor_javascript_enabled_search`.
                        if (presentTorJavaScriptEnabledSearchString.equals("Custom URL")) {
                            // Use R.string.custom_url, which is translated, instead of the array value, which isn't.
                            torJavaScriptEnabledSearchPreference.setSummary(R.string.custom_url);
                        } else {
                            // Set the array value as the summary text.
                            torJavaScriptEnabledSearchPreference.setSummary(presentTorJavaScriptEnabledSearchString);
                        }

                        // Set the status of `torJavaScriptEnabledSearchCustomURLPreference`.
                        torJavaScriptEnabledSearchCustomURLPreference.setEnabled(presentTorJavaScriptEnabledSearchString.equals("Custom URL"));
                        break;

                    case "tor_javascript_enabled_search_custom_url":
                        // Set the summary text for `tor_javascript_enabled_search_custom_url`.
                        torJavaScriptEnabledSearchCustomURLPreference.setSummary(sharedPreferences.getString("tor_javascript_enabled_search_custom_url", ""));
                        break;

                    case "homepage":
                        // Set the new homepage URL as the summary text for the Homepage preference.  The default is `https://www.duckduckgo.com`.
                        homepagePreference.setSummary(sharedPreferences.getString("homepage", "https://www.duckduckgo.com"));
                        break;

                    case "default_font_size":
                        // Get the default font size as a string.  The default is `100`.
                        String newDefaultFontSizeString = sharedPreferences.getString("default_font_size", "100");

                        // Update the summary text of `default_font_size`.
                        defaultFontSizePreference.setSummary(newDefaultFontSizeString + "%%");
                        break;

                    case "hide_system_bars":
                        // Enable `translucentNavigationBarPreference` if `hide_system_bars` is disabled.
                        translucentNavigationBarPreference.setEnabled(!sharedPreferences.getBoolean("hide_system_bars", false));
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
