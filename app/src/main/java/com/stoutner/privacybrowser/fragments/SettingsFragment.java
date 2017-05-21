/*
 * Copyright © 2016-2017 Soren Stoutner <soren@stoutner.com>.
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
        final Preference javaScriptPreference = findPreference("javascript_enabled");
        final Preference firstPartyCookiesPreference = findPreference("first_party_cookies_enabled");
        final Preference thirdPartyCookiesPreference = findPreference("third_party_cookies_enabled");
        final Preference domStoragePreference = findPreference("dom_storage_enabled");
        final Preference saveFormDataPreference = findPreference("save_form_data_enabled");
        final Preference userAgentPreference = findPreference("user_agent");
        final Preference customUserAgentPreference = findPreference("custom_user_agent");
        final Preference blockAdsPreference = findPreference("block_ads");
        final Preference incognitoModePreference = findPreference("incognito_mode");
        final Preference doNotTrackPreference = findPreference("do_not_track");
        final Preference proxyThroughOrbotPreference = findPreference("proxy_through_orbot");
        final Preference torHomepagePreference = findPreference("tor_homepage");
        final Preference torSearchPreference = findPreference("tor_search");
        final Preference torSearchCustomURLPreference = findPreference("tor_search_custom_url");
        final Preference searchPreference = findPreference("search");
        final Preference searchCustomURLPreference = findPreference("search_custom_url");
        final Preference fullScreenBrowsingModePreference = findPreference("full_screen_browsing_mode");
        final Preference hideSystemBarsPreference = findPreference("hide_system_bars");
        final Preference translucentNavigationBarPreference = findPreference("translucent_navigation_bar");
        final Preference homepagePreference = findPreference("homepage");
        final Preference defaultFontSizePreference = findPreference("default_font_size");
        final Preference swipeToRefreshPreference = findPreference("swipe_to_refresh");
        final Preference displayAdditionalAppBarIconsPreference = findPreference("display_additional_app_bar_icons");

        // Set dependencies.
        domStoragePreference.setDependency("javascript_enabled");
        torHomepagePreference.setDependency("proxy_through_orbot");
        torSearchPreference.setDependency("proxy_through_orbot");
        hideSystemBarsPreference.setDependency("full_screen_browsing_mode");

        // Get strings from the preferences.
        String torSearchString = savedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");
        String searchString = savedPreferences.getString("search", "https://duckduckgo.com/html/?q=");

        // Get booleans from the preferences.
        boolean javaScriptEnabledBoolean = savedPreferences.getBoolean("javascript_enabled", false);
        boolean firstPartyCookiesEnabledBoolean = savedPreferences.getBoolean("first_party_cookies_enabled", false);
        boolean thirdPartyCookiesEnabledBoolean = savedPreferences.getBoolean("third_party_cookies_enabled", false);
        boolean proxyThroughOrbotBoolean = savedPreferences.getBoolean("proxy_through_orbot", false);
        boolean fullScreenBrowsingModeBoolean = savedPreferences.getBoolean("full_screen_browsing_mode", false);
        boolean hideSystemBarsBoolean = savedPreferences.getBoolean("hide_system_bars", false);

        // Only enable `thirdPartyCookiesPreference` if `firstPartyCookiesEnabledBoolean` is `true` and API >= 21.
        thirdPartyCookiesPreference.setEnabled(firstPartyCookiesEnabledBoolean && (Build.VERSION.SDK_INT >= 21));

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

        // Set the summary text for "customUserAgentPreference" (the default is `PrivacyBrowser/1.0`) and enable it if `userAgentPreference` it set to `Custom user agent`.
        customUserAgentPreference.setSummary(savedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
        customUserAgentPreference.setEnabled(userAgentPreference.getSummary().equals("Custom user agent"));


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
        torSearchCustomURLPreference.setEnabled(proxyThroughOrbotBoolean && torSearchString.equals("Custom URL"));


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


        // Enable `translucentNavigationBarPreference` only if full screen browsing mode is enabled and `hide_system_bars` is disabled.
        translucentNavigationBarPreference.setEnabled(fullScreenBrowsingModeBoolean && !hideSystemBarsBoolean);


        // Set the homepage URL as the summary text for the `Homepage` preference when the preference screen is loaded.  The default is `https://duckduckgo.com`.
        homepagePreference.setSummary(savedPreferences.getString("homepage", "https://duckduckgo.com"));

        // Set the default font size as the summary text for the `Default Font Size` preference when the preference screen is loaded.  The default is `100`.
        defaultFontSizePreference.setSummary(savedPreferences.getString("default_font_size", "100") + "%%");


        // Set the `javaScriptPreference` icon.
        if (javaScriptEnabledBoolean) {
            javaScriptPreference.setIcon(R.drawable.javascript_enabled);
        } else {
            javaScriptPreference.setIcon(R.drawable.privacy_mode);
        }

        // Set the `firstPartyCookiesPreference` icon.
        if (firstPartyCookiesEnabledBoolean) {
            firstPartyCookiesPreference.setIcon(R.drawable.cookies_enabled);
        } else {
            firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled);
        }

        // Set the `thirdPartyCookiesPreference` icon.
        if (firstPartyCookiesEnabledBoolean && Build.VERSION.SDK_INT >= 21) {
            if (thirdPartyCookiesEnabledBoolean) {
                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_warning);
            } else {
                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled);
            }
        } else {
            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted);
        }

        // Set the `domStoragePreference` icon.
        if (javaScriptEnabledBoolean) {
            if (savedPreferences.getBoolean("dom_storage_enabled", false)) {
                domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
            } else {
                domStoragePreference.setIcon(R.drawable.dom_storage_disabled);
            }
        } else {
            domStoragePreference.setIcon(R.drawable.dom_storage_ghosted);
        }

        // Set the `saveFormDataPreference` icon.
        if (savedPreferences.getBoolean("save_form_data_enabled", false)) {
            saveFormDataPreference.setIcon(R.drawable.form_data_enabled);
        } else {
            saveFormDataPreference.setIcon(R.drawable.form_data_disabled);
        }

        // Set the `customUserAgentPreference` icon.
        if (customUserAgentPreference.isEnabled()) {
            customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled);
        } else {
            customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted);
        }

        // Set the `blockAdsPreference` icon.
        if (savedPreferences.getBoolean("block_ads", true)) {
            blockAdsPreference.setIcon(R.drawable.block_ads_enabled);
        } else {
            blockAdsPreference.setIcon(R.drawable.block_ads_disabled);
        }

        // Set the `incognitoModePreference` icon.
        if (savedPreferences.getBoolean("incognito_mode", false)) {
            incognitoModePreference.setIcon(R.drawable.incognito_mode_enabled);
        } else {
            incognitoModePreference.setIcon(R.drawable.incognito_mode_disabled);
        }

        // Set the `doNotTrackPreference` icon.
        if (savedPreferences.getBoolean("do_not_track", false)) {
            doNotTrackPreference.setIcon(R.drawable.do_not_track_enabled);
        } else {
            doNotTrackPreference.setIcon(R.drawable.do_not_track_disabled);
        }

        // Set the `proxyThroughOrbotPreference` icon.
        if (proxyThroughOrbotBoolean) {
            proxyThroughOrbotPreference.setIcon(R.drawable.orbot_enabled);
        } else {
            proxyThroughOrbotPreference.setIcon(R.drawable.orbot_disabled);
        }

        // Set the `torSearchPreference` and `torSearchCustomURLPreference` icons.
        if (proxyThroughOrbotBoolean) {
            // Set the `torHomepagePreference` and `torSearchPreference` icons.
            torHomepagePreference.setIcon(R.drawable.home_enabled);
            torSearchPreference.setIcon(R.drawable.search_enabled);

            // Set the `torSearchCustomURLPreference` icon.
            if (torSearchCustomURLPreference.isEnabled()) {
                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled);
            } else {
                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_disabled);
            }
        } else {  // Proxy through Orbot is disabled.
            torHomepagePreference.setIcon(R.drawable.home_ghosted);
            torSearchPreference.setIcon(R.drawable.search_ghosted);
            torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted);
        }

        // Set the `searchCustomURLPreference` icon.
        if (searchCustomURLPreference.isEnabled()) {
            searchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled);
        } else {
            searchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted);
        }

        // Set the full screen browsing mode icons.
        if (fullScreenBrowsingModeBoolean) {
            // Set the `fullScreenBrowsingModePreference` icon.
            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled);

            if (hideSystemBarsBoolean) {
                // Set `hideSystemBarsPreference` to use the enabled icon.
                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled);

                // Set `translucentNavigationBarPreference` to use the ghosted icon.
                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted);
            } else {  // `hideSystemBarsBoolean` is false.
                // Set `hideSystemBarsPreference` to use the disabled icon.
                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled);

                // Set the correct icon for `translucentNavigationBarPreference`.
                if (savedPreferences.getBoolean("translucent_navigation_bar", true)) {
                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled);
                } else {
                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled);
                }
            }
        } else {  // `fullScreenBrwosingModeBoolean` is false.
            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled);
            hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_ghosted);
            translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted);
        }

        // Set the `swipeToRefreshPreference` icon.
        if (savedPreferences.getBoolean("swipe_to_refresh", false)) {
            swipeToRefreshPreference.setIcon(R.drawable.refresh_enabled);
        } else {
            swipeToRefreshPreference.setIcon(R.drawable.refresh_disabled);
        }

        // Set the `displayAdditionalAppBarIconsPreference` icon.
        if (savedPreferences.getBoolean("display_additional_app_bar_icons", false)) {
            displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_enabled);
        } else {
            displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_disabled);
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
                            javaScriptPreference.setIcon(R.drawable.javascript_enabled);

                            // Update the icon for `dom_storage_enabled`.
                            if (sharedPreferences.getBoolean("dom_storage_enabled", false)) {
                                domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
                            } else {
                                domStoragePreference.setIcon(R.drawable.dom_storage_disabled);
                            }
                        } else {  // `javascript_enabled` is `false`.
                            // Update the icon for `javascript_enabled`.
                            javaScriptPreference.setIcon(R.drawable.privacy_mode);

                            // Set the icon for `dom_storage_disabled` to be ghosted.
                            domStoragePreference.setIcon(R.drawable.dom_storage_ghosted);
                        }
                        break;

                    case "first_party_cookies_enabled":
                        // Update the icons for `first_party_cookies_enabled` and `third_party_cookies_enabled`.
                        if (sharedPreferences.getBoolean("first_party_cookies_enabled", false)) {
                            // Set the icon for `first_party_cookies_enabled`.
                            firstPartyCookiesPreference.setIcon(R.drawable.cookies_enabled);

                            // Update the icon for `third_party_cookies_enabled`.
                            if (Build.VERSION.SDK_INT >= 21) {
                                if (sharedPreferences.getBoolean("third_party_cookies_enabled", false)) {
                                    thirdPartyCookiesPreference.setIcon(R.drawable.cookies_warning);
                                } else {
                                    thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled);
                                }
                            } else {
                                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted);
                            }
                        } else {  // `first_party_cookies_enabled` is `false`.
                            // Update the icon for `first_party_cookies_enabled`.
                            firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled);

                            // Set the icon for `third_party_cookies_enabled` to be ghosted.
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted);
                        }

                        // Enable `third_party_cookies_enabled` if `first_party_cookies_enabled` is `true` and API >= 21.
                        thirdPartyCookiesPreference.setEnabled(sharedPreferences.getBoolean("first_party_cookies_enabled", false) && (Build.VERSION.SDK_INT >= 21));
                        break;

                    case "third_party_cookies_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("third_party_cookies_enabled", false)) {
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_warning);
                        } else {
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled);
                        }
                        break;

                    case "dom_storage_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("dom_storage_enabled", false)) {
                            domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
                        } else {
                            domStoragePreference.setIcon(R.drawable.dom_storage_disabled);
                        }
                        break;

                    case "save_form_data_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("save_form_data_enabled", false)) {
                            saveFormDataPreference.setIcon(R.drawable.form_data_enabled);
                        } else {
                            saveFormDataPreference.setIcon(R.drawable.form_data_disabled);
                        }

                    case "user_agent":
                        String userAgentString = sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0");

                        switch (userAgentString) {
                            case "WebView default user agent":
                                // Display the user agent as the summary text for `userAgentPreference`.
                                userAgentPreference.setSummary(bareWebView.getSettings().getUserAgentString());

                                // Update `customUserAgentPreference`.
                                customUserAgentPreference.setEnabled(false);
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted);
                                break;

                            case "Custom user agent":
                                // Display `Custom user agent` as the summary text for `userAgentPreference`.
                                userAgentPreference.setSummary(R.string.custom_user_agent);

                                // Update `customUserAgentPreference`.
                                customUserAgentPreference.setEnabled(true);
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled);
                                break;

                            default:
                                // Display the user agent as the summary text for `userAgentPreference`.
                                userAgentPreference.setSummary(sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0"));

                                // Update `customUserAgentPreference`.
                                customUserAgentPreference.setEnabled(false);
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted);
                                break;
                        }
                        break;

                    case "custom_user_agent":
                        // Set the new custom user agent as the summary text for `custom_user_agent`.  The default is `PrivacyBrowser/1.0`.
                        customUserAgentPreference.setSummary(sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
                        break;

                    case "block_ads":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("block_ads", true)) {
                            blockAdsPreference.setIcon(R.drawable.block_ads_enabled);
                        } else {
                            blockAdsPreference.setIcon(R.drawable.block_ads_disabled);
                        }
                        break;

                    case "incognito_mode":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("incognito_mode", false)) {
                            incognitoModePreference.setIcon(R.drawable.incognito_mode_enabled);
                        } else {
                            incognitoModePreference.setIcon(R.drawable.incognito_mode_disabled);
                        }
                        break;

                    case "do_not_track":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("do_not_track", false)) {
                            doNotTrackPreference.setIcon(R.drawable.do_not_track_enabled);
                        } else {
                            doNotTrackPreference.setIcon(R.drawable.do_not_track_disabled);
                        }

                        break;

                    case "proxy_through_orbot":
                        // Get current settings.
                        boolean currentProxyThroughOrbot = sharedPreferences.getBoolean("proxy_through_orbot", false);
                        String currentTorSearchString = sharedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");

                        // Enable the Tor custom URL search option only if `currentProxyThroughOrbot` is true and the search is set to `Custom URL`.
                        torSearchCustomURLPreference.setEnabled(currentProxyThroughOrbot && currentTorSearchString.equals("Custom URL"));

                        // Update the icons.
                        if (currentProxyThroughOrbot) {
                            // Set the `proxyThroughOrbotPreference`, `torHomepagePreference`, and `torSearchPreference` icons.
                            proxyThroughOrbotPreference.setIcon(R.drawable.orbot_enabled);
                            torHomepagePreference.setIcon(R.drawable.home_enabled);
                            torSearchPreference.setIcon(R.drawable.search_enabled);

                            // Set the `torSearchCustomURLPreference` icon.
                            if (torSearchCustomURLPreference.isEnabled()) {
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled);
                            } else {
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_disabled);
                            }
                        } else {  // Proxy through Orbot is disabled.
                            proxyThroughOrbotPreference.setIcon(R.drawable.orbot_disabled);
                            torHomepagePreference.setIcon(R.drawable.home_ghosted);
                            torSearchPreference.setIcon(R.drawable.search_ghosted);
                            torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted);
                        }
                        break;

                    case "tor_homepage":
                        // Set the new tor homepage URL as the summary text for the `tor_homepage` preference.  The default is DuckDuckGo:  `https://3g2upl4pq6kufc4m.onion`.
                        torHomepagePreference.setSummary(sharedPreferences.getString("tor_homepage", "https://3g2upl4pq6kufc4m.onion"));
                        break;

                    case "tor_search":
                        // Get the present search string.
                        String presentTorSearchString = sharedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");

                        // Update the preferences.
                        if (presentTorSearchString.equals("Custom URL")) {
                            // Use `R.string.custom_url`, which is translated, as the summary instead of the array value, which isn't.
                            torSearchPreference.setSummary(R.string.custom_url);

                            // Update `torSearchCustomURLPreference`.
                            torSearchCustomURLPreference.setEnabled(true);
                            torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled);
                        } else {
                            // Set the array value as the summary text.
                            torSearchPreference.setSummary(presentTorSearchString);

                            // Update `torSearchCustomURLPreference`.
                            torSearchCustomURLPreference.setEnabled(false);
                            torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_disabled);
                        }
                        break;

                    case "tor_search_custom_url":
                        // Set the summary text for `tor_search_custom_url`.
                        torSearchCustomURLPreference.setSummary(sharedPreferences.getString("tor_search_custom_url", ""));
                        break;

                    case "search":
                        // Store the new search string.
                        String newSearchString = sharedPreferences.getString("search", "https://duckduckgo.com/html/?q=");

                        // Update `searchPreference` and `searchCustomURLPreference`.
                        if (newSearchString.equals("Custom URL")) {  // `Custom URL` is selected.
                            // Set the summary text to `R.string.custom_url`, which is translated.
                            searchPreference.setSummary(R.string.custom_url);

                            // Update `searchCustomURLPreference`.
                            searchCustomURLPreference.setEnabled(true);
                            searchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled);
                        } else {  // `Custom URL` is not selected.
                            // Set the summary text to `newSearchString`.
                            searchPreference.setSummary(newSearchString);

                            // Update `searchCustomURLPreference`.
                            searchCustomURLPreference.setEnabled(false);
                            searchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted);
                        }
                        break;

                    case "search_custom_url":
                        // Set the new custom search URL as the summary text for `search_custom_url`.  The default is `""`.
                        searchCustomURLPreference.setSummary(sharedPreferences.getString("search_custom_url", ""));
                        break;

                    case "full_screen_browsing_mode":
                        if (sharedPreferences.getBoolean("full_screen_browsing_mode", false)) {
                            // Set `fullScreenBrowsingModePreference` to use the enabled icon.
                            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled);

                            if (sharedPreferences.getBoolean("hide_system_bars", false)) {
                                // Set `hideSystemBarsPreference` to use the enabled icon.
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled);

                                // Update `translucentNavigationBarPreference`.
                                translucentNavigationBarPreference.setEnabled(false);
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted);
                            } else {  // `hide_system_bars` is false.
                                // Set `hideSystemBarsPreference` to use the disabled icon.
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled);

                                // Update `translucentNavigationBarPreference`.
                                translucentNavigationBarPreference.setEnabled(true);
                                if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled);
                                } else {
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled);
                                }
                            }
                        } else {  // `full_screen_browsing_mode` is false.
                            // Disable `translucentNavigationBarPreference`.
                            translucentNavigationBarPreference.setEnabled(false);

                            // Update the icons.
                            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled);
                            hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_ghosted);
                            translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted);
                        }
                        break;

                    case "hide_system_bars":
                        if (sharedPreferences.getBoolean("hide_system_bars", false)) {
                            // Set `hideSystemBarsPreference` to use the enabled icon.
                            hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled);

                            // Update `translucentNavigationBarPreference`.
                            translucentNavigationBarPreference.setEnabled(false);
                            translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted);
                        } else {  // `hide_system_bars` is false.
                            // Set `hideSystemBarsPreference` to use the disabled icon.
                            hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled);

                            // Update `translucentNavigationBarPreference`.
                            translucentNavigationBarPreference.setEnabled(true);
                            if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled);
                            } else {
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled);
                            }
                        }
                        break;

                    case "translucent_navigation_bar":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                            translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled);
                        } else {
                            translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled);
                        }
                        break;

                    case "homepage":
                        // Set the new homepage URL as the summary text for the Homepage preference.  The default is `https://www.duckduckgo.com`.
                        homepagePreference.setSummary(sharedPreferences.getString("homepage", "https://www.duckduckgo.com"));
                        break;

                    case "default_font_size":
                        // Update the summary text of `default_font_size`.
                        defaultFontSizePreference.setSummary(sharedPreferences.getString("default_font_size", "100") + "%%");
                        break;

                    case "swipe_to_refresh":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("swipe_to_refresh", false)) {
                            swipeToRefreshPreference.setIcon(R.drawable.refresh_enabled);
                        } else {
                            swipeToRefreshPreference.setIcon(R.drawable.refresh_disabled);
                        }
                        break;

                    case "display_additional_app_bar_icons":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("display_additional_app_bar_icons", false)) {
                            displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_enabled);
                        } else {
                            displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_disabled);
                        }
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
