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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

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
        final Preference clearEverythingPreference = findPreference("clear_everything");
        final Preference clearCookiesPreference = findPreference("clear_cookies");
        final Preference clearDomStoragePreference = findPreference("clear_dom_storage");
        final Preference clearFormDataPreference = findPreference("clear_form_data");
        final Preference clearCachePreference = findPreference("clear_cache");
        final Preference homepagePreference = findPreference("homepage");
        final Preference defaultFontSizePreference = findPreference("default_font_size");
        final Preference swipeToRefreshPreference = findPreference("swipe_to_refresh");
        final Preference displayAdditionalAppBarIconsPreference = findPreference("display_additional_app_bar_icons");
        final Preference darkThemePreference = findPreference("dark_theme");
        final Preference nightModePreference = findPreference("night_mode");
        final Preference displayWebpageImagesPreference = findPreference("display_webpage_images");

        // Set dependencies.
        torHomepagePreference.setDependency("proxy_through_orbot");
        torSearchPreference.setDependency("proxy_through_orbot");
        hideSystemBarsPreference.setDependency("full_screen_browsing_mode");

        // Get Strings from the preferences.
        String torSearchString = savedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");
        String searchString = savedPreferences.getString("search", "https://duckduckgo.com/html/?q=");

        // Get booleans from the preferences.
        final boolean javaScriptEnabledBoolean = savedPreferences.getBoolean("javascript_enabled", false);
        boolean firstPartyCookiesEnabledBoolean = savedPreferences.getBoolean("first_party_cookies_enabled", false);
        boolean thirdPartyCookiesEnabledBoolean = savedPreferences.getBoolean("third_party_cookies_enabled", false);
        boolean proxyThroughOrbotBoolean = savedPreferences.getBoolean("proxy_through_orbot", false);
        boolean fullScreenBrowsingModeBoolean = savedPreferences.getBoolean("full_screen_browsing_mode", false);
        boolean hideSystemBarsBoolean = savedPreferences.getBoolean("hide_system_bars", false);
        boolean clearEverythingBoolean = savedPreferences.getBoolean("clear_everything", true);
        final boolean nightModeBoolean = savedPreferences.getBoolean("night_mode", false);

        // Only enable `thirdPartyCookiesPreference` if `firstPartyCookiesEnabledBoolean` is `true` and API >= 21.
        thirdPartyCookiesPreference.setEnabled(firstPartyCookiesEnabledBoolean && (Build.VERSION.SDK_INT >= 21));

        // Only enable `domStoragePreference` if either `javaScriptEnabledBoolean` or `nightModeBoolean` is true.
        domStoragePreference.setEnabled(javaScriptEnabledBoolean || nightModeBoolean);

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

        // Set the status of the `Clear and Exit` preferences.
        clearCookiesPreference.setEnabled(!clearEverythingBoolean);
        clearDomStoragePreference.setEnabled(!clearEverythingBoolean);
        clearFormDataPreference.setEnabled(!clearEverythingBoolean);
        clearCachePreference.setEnabled(!clearEverythingBoolean);

        // Set the homepage URL as the summary text for the `Homepage` preference when the preference screen is loaded.  The default is `https://duckduckgo.com`.
        homepagePreference.setSummary(savedPreferences.getString("homepage", "https://duckduckgo.com"));

        // Set the default font size as the summary text for the `Default Font Size` preference when the preference screen is loaded.  The default is `100`.
        defaultFontSizePreference.setSummary(savedPreferences.getString("default_font_size", "100") + "%%");

        // Disable `javaScriptPreference` if `nightModeBoolean` is true.  JavaScript will be enabled for all web pages.
        javaScriptPreference.setEnabled(!nightModeBoolean);

        // Set the `javaScriptPreference` icon.
        if (javaScriptEnabledBoolean || nightModeBoolean) {
            javaScriptPreference.setIcon(R.drawable.javascript_enabled);
        } else {
            javaScriptPreference.setIcon(R.drawable.privacy_mode);
        }

        // Set the `firstPartyCookiesPreference` icon.
        if (firstPartyCookiesEnabledBoolean) {
            firstPartyCookiesPreference.setIcon(R.drawable.cookies_enabled);
        } else {
            if (MainWebViewActivity.darkTheme) {
                firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
            } else {
                firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
            }
        }

        // Set the `thirdPartyCookiesPreference` icon.
        if (firstPartyCookiesEnabledBoolean && Build.VERSION.SDK_INT >= 21) {
            if (thirdPartyCookiesEnabledBoolean) {
                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_warning);
            } else {
                if (MainWebViewActivity.darkTheme) {
                    thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
                } else {
                    thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
                }
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_dark);
            } else {
                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_light);
            }
        }

        // Set the `domStoragePreference` icon.
        if (javaScriptEnabledBoolean || nightModeBoolean) {  // The preference is enabled.
            if (savedPreferences.getBoolean("dom_storage_enabled", false)) {  // DOM storage is enabled.
                domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
            } else {  // DOM storage is disabled.
                if (MainWebViewActivity.darkTheme) {
                    domStoragePreference.setIcon(R.drawable.dom_storage_disabled_dark);
                } else {
                    domStoragePreference.setIcon(R.drawable.dom_storage_disabled_light);
                }
            }
        } else {  // The preference is disabled.  The icon should be ghosted.
            if (MainWebViewActivity.darkTheme) {
                domStoragePreference.setIcon(R.drawable.dom_storage_ghosted_dark);
            } else {
                domStoragePreference.setIcon(R.drawable.dom_storage_ghosted_light);
            }
        }

        // Set the `saveFormDataPreference` icon.
        if (savedPreferences.getBoolean("save_form_data_enabled", false)) {
            saveFormDataPreference.setIcon(R.drawable.form_data_enabled);
        } else {
            if (MainWebViewActivity.darkTheme) {
                saveFormDataPreference.setIcon(R.drawable.form_data_disabled_dark);
            } else {
                saveFormDataPreference.setIcon(R.drawable.form_data_disabled_light);
            }
        }

        // Set the `customUserAgentPreference` icon.
        if (customUserAgentPreference.isEnabled()) {
            if (MainWebViewActivity.darkTheme) {
                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled_dark);
            } else {
                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_dark);
            } else {
                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_light);
            }
        }

        // Set the `blockAdsPreference` icon.
        if (savedPreferences.getBoolean("block_ads", true)) {
            if (MainWebViewActivity.darkTheme) {
                blockAdsPreference.setIcon(R.drawable.block_ads_enabled_dark);
            } else {
                blockAdsPreference.setIcon(R.drawable.block_ads_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                blockAdsPreference.setIcon(R.drawable.block_ads_disabled_dark);
            } else {
                blockAdsPreference.setIcon(R.drawable.block_ads_disabled_light);
            }
        }

        // Set the `incognitoModePreference` icon.
        if (savedPreferences.getBoolean("incognito_mode", false)) {
            if (MainWebViewActivity.darkTheme) {
                incognitoModePreference.setIcon(R.drawable.incognito_mode_enabled_dark);
            } else {
                incognitoModePreference.setIcon(R.drawable.incognito_mode_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                incognitoModePreference.setIcon(R.drawable.incognito_mode_disabled_dark);
            } else {
                incognitoModePreference.setIcon(R.drawable.incognito_mode_disabled_light);
            }
        }

        // Set the `doNotTrackPreference` icon.
        if (savedPreferences.getBoolean("do_not_track", false)) {
            if (MainWebViewActivity.darkTheme) {
                doNotTrackPreference.setIcon(R.drawable.do_not_track_enabled_dark);
            } else {
                doNotTrackPreference.setIcon(R.drawable.do_not_track_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                doNotTrackPreference.setIcon(R.drawable.do_not_track_disabled_dark);
            } else {
                doNotTrackPreference.setIcon(R.drawable.do_not_track_disabled_light);
            }
        }

        // Set the Tor icons according to the theme.
        if (proxyThroughOrbotBoolean) {  // Proxying is enabled.
            if (MainWebViewActivity.darkTheme) {
                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_enabled_dark);
                torHomepagePreference.setIcon(R.drawable.home_enabled_dark);
                torSearchPreference.setIcon(R.drawable.search_enabled_dark);

                // Set the custom search icon.
                if (torSearchCustomURLPreference.isEnabled()) {
                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_dark);
                } else {
                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_dark);
                }
            } else {
                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_enabled_light);
                torHomepagePreference.setIcon(R.drawable.home_enabled_light);
                torSearchPreference.setIcon(R.drawable.search_enabled_light);

                // Set the custom search icon.
                if (torSearchCustomURLPreference.isEnabled()) {
                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_light);
                } else {
                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_light);
                }
            }
        } else {  // Proxying is disabled.
            if (MainWebViewActivity.darkTheme) {
                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_disabled_dark);
                torHomepagePreference.setIcon(R.drawable.home_ghosted_dark);
                torSearchPreference.setIcon(R.drawable.search_ghosted_dark);
                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_dark);
            } else {
                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_disabled_light);
                torHomepagePreference.setIcon(R.drawable.home_ghosted_light);
                torSearchPreference.setIcon(R.drawable.search_ghosted_light);
                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_light);
            }
        }

        // Set the `searchCustomURLPreference` icon.
        if (searchCustomURLPreference.isEnabled()) {
            if (MainWebViewActivity.darkTheme) {
                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_dark);
            } else {
                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_dark);
            } else {
                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_light);
            }
        }

        // Set the full screen browsing mode icons.
        if (fullScreenBrowsingModeBoolean) {  // `fullScreenBrowsingModeBoolean` is `true`.
            // Set the `fullScreenBrowsingModePreference` icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_dark);
            } else {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_light);
            }

            if (hideSystemBarsBoolean) {  // `hideSystemBarsBoolean` is `true`.
                // Set the icons according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled_dark);
                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_dark);
                } else {
                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled_light);
                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_light);
                }
            } else {  // `hideSystemBarsBoolean` is `false`.
                // Set the `hideSystemBarsPreference` icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    // Set the icon for `hideSystemBarsPreference`.
                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled_dark);

                    // Set the icon for `translucentNavigationBarPreference`.
                    if (savedPreferences.getBoolean("translucent_navigation_bar", true)) {
                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_dark);
                    } else {
                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_dark);
                    }
                } else {
                    // Set the icon for `hideSystemBarsPreference`.
                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled_light);

                    // Set the icon for `translucentNavigationBarPreference`.
                    if (savedPreferences.getBoolean("translucent_navigation_bar", true)) {
                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_light);
                    } else {
                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_light);
                    }
                }
            }
        } else {  // `fullScreenBrowsingModeBoolean` is `false`.
            // Set the icons according to the theme.
            if (MainWebViewActivity.darkTheme) {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_dark);
                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_ghosted_dark);
                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_dark);
            } else {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_light);
                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_ghosted_light);
                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_light);
            }
        }

        // Set the `clearEverythingPreference` icon.
        if (clearEverythingBoolean) {
            if (MainWebViewActivity.darkTheme) {
                clearEverythingPreference.setIcon(R.drawable.clear_everything_enabled_dark);
            } else {
                clearEverythingPreference.setIcon(R.drawable.clear_everything_enabled_light);
            }
        } else {
            clearEverythingPreference.setIcon(R.drawable.clear_everything_disabled);
        }

        // Set the `clearCookiesPreference` icon.
        if (clearEverythingBoolean || savedPreferences.getBoolean("clear_cookies", true)) {
            if (MainWebViewActivity.darkTheme) {
                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_dark);
            } else {
                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_light);
            }
        } else {
            clearCookiesPreference.setIcon(R.drawable.cookies_warning);
        }

        // Set the `clearDomStoragePreference` icon.
        if (clearEverythingBoolean || savedPreferences.getBoolean("clear_dom_storage", true)) {
            if (MainWebViewActivity.darkTheme) {
                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_dark);
            } else {
                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_light);
            }
        } else {
            clearDomStoragePreference.setIcon(R.drawable.dom_storage_warning);
        }

        // Set the `clearFormDataPreference` icon.
        if (clearEverythingBoolean || savedPreferences.getBoolean("clear_form_data", true)) {
            if (MainWebViewActivity.darkTheme) {
                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_dark);
            } else {
                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_light);
            }
        } else {
            clearFormDataPreference.setIcon(R.drawable.form_data_warning);
        }

        // Set the `clearCachePreference` icon.
        if (clearEverythingBoolean || savedPreferences.getBoolean("clear_cache", true)) {
            if (MainWebViewActivity.darkTheme) {
                clearCachePreference.setIcon(R.drawable.cache_cleared_dark);
            } else {
                clearCachePreference.setIcon(R.drawable.cache_cleared_light);
            }
        } else {
            clearCachePreference.setIcon(R.drawable.cache_warning);
        }

        // Set the `swipeToRefreshPreference` icon.
        if (savedPreferences.getBoolean("swipe_to_refresh", false)) {
            if (MainWebViewActivity.darkTheme) {
                swipeToRefreshPreference.setIcon(R.drawable.refresh_enabled_dark);
            } else {
                swipeToRefreshPreference.setIcon(R.drawable.refresh_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                swipeToRefreshPreference.setIcon(R.drawable.refresh_disabled_dark);
            } else {
                swipeToRefreshPreference.setIcon(R.drawable.refresh_disabled_light);
            }
        }

        // Set the `displayAdditionalAppBarIconsPreference` icon.
        if (savedPreferences.getBoolean("display_additional_app_bar_icons", false)) {
            if (MainWebViewActivity.darkTheme) {
                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_enabled_dark);
            } else {
                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_disabled_dark);
            } else {
                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_disabled_light);
            }
        }

        // Set the `darkThemePreference` icon.
        if (savedPreferences.getBoolean("dark_theme", false)) {
            darkThemePreference.setIcon(R.drawable.theme_dark);
        } else {
            darkThemePreference.setIcon(R.drawable.theme_light);
        }

        // Set the `nightModePreference` icon.
        if (nightModeBoolean) {
            if (MainWebViewActivity.darkTheme) {
                nightModePreference.setIcon(R.drawable.night_mode_enabled_dark);
            } else {
                nightModePreference.setIcon(R.drawable.night_mode_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                nightModePreference.setIcon(R.drawable.night_mode_disabled_dark);
            } else {
                nightModePreference.setIcon(R.drawable.night_mode_disabled_light);
            }
        }

        // Set the `displayWebpageImagesPreference` icon.
        if (savedPreferences.getBoolean("display_webpage_images", true)) {
            if (MainWebViewActivity.darkTheme) {
                displayWebpageImagesPreference.setIcon(R.drawable.images_enabled_dark);
            } else {
                displayWebpageImagesPreference.setIcon(R.drawable.images_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                displayWebpageImagesPreference.setIcon(R.drawable.images_disabled_dark);
            } else {
                displayWebpageImagesPreference.setIcon(R.drawable.images_disabled_light);
            }
        }


        // Listen for preference changes.
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  We know.
            @SuppressLint("SetJavaScriptEnabled")
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                switch (key) {
                    case "javascript_enabled":
                        // Update the icons and the DOM storage preference status.
                        if (sharedPreferences.getBoolean("javascript_enabled", false)) {  // The JavaScript preference is enabled.
                            // Update the icon for the JavaScript preference.
                            javaScriptPreference.setIcon(R.drawable.javascript_enabled);

                            // Update the status of the DOM storage preference.
                            domStoragePreference.setEnabled(true);

                            // Update the icon for the DOM storage preference.
                            if (sharedPreferences.getBoolean("dom_storage_enabled", false)) {
                                domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
                            } else {
                                if (MainWebViewActivity.darkTheme) {
                                    domStoragePreference.setIcon(R.drawable.dom_storage_disabled_dark);
                                } else {
                                    domStoragePreference.setIcon(R.drawable.dom_storage_disabled_light);
                                }
                            }
                        } else {  // The JavaScript preference is disabled.
                            // Update the icon for the JavaScript preference.
                            javaScriptPreference.setIcon(R.drawable.privacy_mode);

                            // Update the status of the DOM storage preference.
                            domStoragePreference.setEnabled(false);

                            // Set the icon for DOM storage preference to be ghosted.
                            if (MainWebViewActivity.darkTheme) {
                                domStoragePreference.setIcon(R.drawable.dom_storage_ghosted_dark);
                            } else {
                                domStoragePreference.setIcon(R.drawable.dom_storage_ghosted_light);
                            }
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
                                    if (MainWebViewActivity.darkTheme) {
                                        thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
                                    } else {
                                        thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
                                    }
                                }
                            } else {
                                if (MainWebViewActivity.darkTheme) {
                                    thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_dark);
                                } else {
                                    thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_light);
                                }
                            }
                        } else {  // `first_party_cookies_enabled` is `false`.
                            // Update the icon for `first_party_cookies_enabled`.
                            if (MainWebViewActivity.darkTheme) {
                                firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
                            } else {
                                firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
                            }

                            // Set the icon for `third_party_cookies_enabled` to be ghosted.
                            if (MainWebViewActivity.darkTheme) {
                                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_dark);
                            } else {
                                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_light);
                            }
                        }

                        // Enable `third_party_cookies_enabled` if `first_party_cookies_enabled` is `true` and API >= 21.
                        thirdPartyCookiesPreference.setEnabled(sharedPreferences.getBoolean("first_party_cookies_enabled", false) && (Build.VERSION.SDK_INT >= 21));
                        break;

                    case "third_party_cookies_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("third_party_cookies_enabled", false)) {
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_warning);
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
                            } else {
                                thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
                            }
                        }
                        break;

                    case "dom_storage_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("dom_storage_enabled", false)) {
                            domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                domStoragePreference.setIcon(R.drawable.dom_storage_disabled_dark);
                            } else {
                                domStoragePreference.setIcon(R.drawable.dom_storage_disabled_light);
                            }
                        }
                        break;

                    case "save_form_data_enabled":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("save_form_data_enabled", false)) {
                            saveFormDataPreference.setIcon(R.drawable.form_data_enabled);
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                saveFormDataPreference.setIcon(R.drawable.form_data_disabled_dark);
                            } else {
                                saveFormDataPreference.setIcon(R.drawable.form_data_disabled_light);
                            }
                        }

                    case "user_agent":
                        String userAgentString = sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0");

                        switch (userAgentString) {
                            case "WebView default user agent":
                                // Display the user agent as the summary text for `userAgentPreference`.
                                userAgentPreference.setSummary(bareWebView.getSettings().getUserAgentString());

                                // Disable `customUserAgentPreference`.
                                customUserAgentPreference.setEnabled(false);

                                // Set the `customUserAgentPreference` icon according to the theme.
                                if (MainWebViewActivity.darkTheme) {
                                    customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_dark);
                                } else {
                                    customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_light);
                                }
                                break;

                            case "Custom user agent":
                                // Display `Custom user agent` as the summary text for `userAgentPreference`.
                                userAgentPreference.setSummary(R.string.custom_user_agent);

                                // Enable `customUserAgentPreference`.
                                customUserAgentPreference.setEnabled(true);

                                // Set the `customUserAgentPreference` icon according to the theme.
                                if (MainWebViewActivity.darkTheme) {
                                    customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled_dark);
                                } else {
                                    customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled_light);
                                }
                                break;

                            default:
                                // Display the user agent as the summary text for `userAgentPreference`.
                                userAgentPreference.setSummary(sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0"));

                                // Disable `customUserAgentPreference`.
                                customUserAgentPreference.setEnabled(false);

                                // Set the `customUserAgentPreference` icon according to the theme.
                                if (MainWebViewActivity.darkTheme) {
                                    customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_dark);
                                } else {
                                    customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_light);
                                }
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
                            if (MainWebViewActivity.darkTheme) {
                                blockAdsPreference.setIcon(R.drawable.block_ads_enabled_dark);
                            } else {
                                blockAdsPreference.setIcon(R.drawable.block_ads_enabled_light);
                            }
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                blockAdsPreference.setIcon(R.drawable.block_ads_disabled_dark);
                            } else {
                                blockAdsPreference.setIcon(R.drawable.block_ads_disabled_light);
                            }
                        }
                        break;

                    case "incognito_mode":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("incognito_mode", false)) {
                            if (MainWebViewActivity.darkTheme) {
                                incognitoModePreference.setIcon(R.drawable.incognito_mode_enabled_dark);
                            } else {
                                incognitoModePreference.setIcon(R.drawable.incognito_mode_enabled_light);
                            }
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                incognitoModePreference.setIcon(R.drawable.incognito_mode_disabled_dark);
                            } else {
                                incognitoModePreference.setIcon(R.drawable.incognito_mode_disabled_light);
                            }
                        }
                        break;

                    case "do_not_track":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("do_not_track", false)) {
                            if (MainWebViewActivity.darkTheme) {
                                doNotTrackPreference.setIcon(R.drawable.do_not_track_enabled_dark);
                            } else {
                                doNotTrackPreference.setIcon(R.drawable.do_not_track_enabled_light);
                            }
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                doNotTrackPreference.setIcon(R.drawable.do_not_track_disabled_dark);
                            } else {
                                doNotTrackPreference.setIcon(R.drawable.do_not_track_disabled_light);
                            }
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
                            // Set the Tor icons according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_enabled_dark);
                                torHomepagePreference.setIcon(R.drawable.home_enabled_dark);
                                torSearchPreference.setIcon(R.drawable.search_enabled_dark);

                                // Set the `torSearchCustomURLPreference` icon.
                                if (torSearchCustomURLPreference.isEnabled()) {
                                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_dark);
                                } else {
                                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_dark);
                                }
                            } else {
                                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_enabled_light);
                                torHomepagePreference.setIcon(R.drawable.home_enabled_light);
                                torSearchPreference.setIcon(R.drawable.search_enabled_light);

                                // Set the `torSearchCustomURLPreference` icon.
                                if (torSearchCustomURLPreference.isEnabled()) {
                                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_light);
                                } else {
                                    torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_light);
                                }
                            }
                        } else {  // Proxy through Orbot is disabled.
                            if (MainWebViewActivity.darkTheme) {
                                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_disabled_dark);
                                torHomepagePreference.setIcon(R.drawable.home_ghosted_dark);
                                torSearchPreference.setIcon(R.drawable.search_ghosted_dark);
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_dark);
                            } else {
                                proxyThroughOrbotPreference.setIcon(R.drawable.orbot_disabled_light);
                                torHomepagePreference.setIcon(R.drawable.home_ghosted_light);
                                torSearchPreference.setIcon(R.drawable.search_ghosted_light);
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_light);
                            }
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

                            // Enable `torSearchCustomURLPreference`.
                            torSearchCustomURLPreference.setEnabled(true);

                            // Update the `torSearchCustomURLPreference` icon.
                            if (MainWebViewActivity.darkTheme) {
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_dark);
                            } else {
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_light);
                            }
                        } else {
                            // Set the array value as the summary text.
                            torSearchPreference.setSummary(presentTorSearchString);

                            // Disable `torSearchCustomURLPreference`.
                            torSearchCustomURLPreference.setEnabled(false);

                            // Update the `torSearchCustomURLPreference` icon.
                            if (MainWebViewActivity.darkTheme) {
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_dark);
                            } else {
                                torSearchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_light);
                            }
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

                            // Enable `searchCustomURLPreference`.
                            searchCustomURLPreference.setEnabled(true);

                            // Set the `searchCustomURLPreference` according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_dark);
                            } else {
                                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_enabled_light);
                            }
                        } else {  // `Custom URL` is not selected.
                            // Set the summary text to `newSearchString`.
                            searchPreference.setSummary(newSearchString);

                            // Disable `searchCustomURLPreference`.
                            searchCustomURLPreference.setEnabled(false);

                            // Set the `searchCustomURLPreference` according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_dark);
                            } else {
                                searchCustomURLPreference.setIcon(R.drawable.search_custom_url_ghosted_light);
                            }
                        }
                        break;

                    case "search_custom_url":
                        // Set the new custom search URL as the summary text for `search_custom_url`.  The default is `""`.
                        searchCustomURLPreference.setSummary(sharedPreferences.getString("search_custom_url", ""));
                        break;

                    case "full_screen_browsing_mode":
                        if (sharedPreferences.getBoolean("full_screen_browsing_mode", false)) {
                            // Set the `fullScreenBrowsingModePreference` icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_dark);
                            } else {
                                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_light);
                            }

                            if (sharedPreferences.getBoolean("hide_system_bars", false)) {  // `hide_system_bars` is `true`.
                                // Disable `translucentNavigationBarPreference`.
                                translucentNavigationBarPreference.setEnabled(false);

                                // Set the icons according to the theme.
                                if (MainWebViewActivity.darkTheme) {
                                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled_dark);
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_dark);
                                } else {
                                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled_light);
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_light);
                                }
                            } else {  // `hide_system_bars` is `false`.
                                // Enable `translucentNavigationBarPreference`.
                                translucentNavigationBarPreference.setEnabled(true);

                                // Set the icons according to the theme.
                                if (MainWebViewActivity.darkTheme) {  // Use the dark theme.
                                    // Set the `hideSystemBarsPreference` icon.
                                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled_dark);

                                    // Set the `translucentNavigationBarPreference` icon.
                                    if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_dark);
                                    } else {
                                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_dark);
                                    }
                                } else {  // Use the light theme.
                                    // Set the `hideSystemBarsPreference` icon.
                                    hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled_light);

                                    // Set the `translucentNavigationBarPreference` icon.
                                    if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_light);
                                    } else {
                                        translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_light);
                                    }
                                }
                            }
                        } else {  // `full_screen_browsing_mode` is false.
                            // Disable `translucentNavigationBarPreference`.
                            translucentNavigationBarPreference.setEnabled(false);

                            // Update the icons according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_dark);
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_ghosted_dark);
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_dark);
                            } else {
                                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_light);
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_ghosted_light);
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_light);
                            }
                        }
                        break;

                    case "hide_system_bars":
                        if (sharedPreferences.getBoolean("hide_system_bars", false)) {
                            // Disable `translucentNavigationBarPreference`.
                            translucentNavigationBarPreference.setEnabled(false);

                            // Set the icons according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled_dark);
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_dark);
                            } else {
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_enabled_light);
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_ghosted_light);
                            }
                        } else {  // `hide_system_bars` is false.
                            // Enable `translucentNavigationBarPreference`.
                            translucentNavigationBarPreference.setEnabled(true);

                            // Set the icons according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                // Set the `hideSystemBarsPreference` icon.
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled_dark);

                                // Set the `translucentNavigationBarPreference` icon.
                                if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_dark);
                                } else {
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_dark);
                                }
                            } else {
                                // Set the `hideSystemBarsPreference` icon.
                                hideSystemBarsPreference.setIcon(R.drawable.hide_system_bars_disabled_light);

                                // Set the `translucentNavigationBarPreference` icon.
                                if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_light);
                                } else {
                                    translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_light);
                                }
                            }
                        }
                        break;

                    case "translucent_navigation_bar":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("translucent_navigation_bar", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_dark);
                            } else {
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_enabled_light);
                            }
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_dark);
                            } else {
                                translucentNavigationBarPreference.setIcon(R.drawable.translucent_bar_disabled_light);
                            }
                        }
                        break;

                    case "clear_everything":
                        // Store the new `clear_everything` status
                        boolean newClearEverythingBoolean = sharedPreferences.getBoolean("clear_everything", true);

                        // Update the status of the `Clear and Exit` preferences.
                        clearCookiesPreference.setEnabled(!newClearEverythingBoolean);
                        clearDomStoragePreference.setEnabled(!newClearEverythingBoolean);
                        clearFormDataPreference.setEnabled(!newClearEverythingBoolean);
                        clearCachePreference.setEnabled(!newClearEverythingBoolean);

                        // Update the `clearEverythingPreference` icon.
                        if (newClearEverythingBoolean) {
                            if (MainWebViewActivity.darkTheme) {
                                clearEverythingPreference.setIcon(R.drawable.clear_everything_enabled_dark);
                            } else {
                                clearEverythingPreference.setIcon(R.drawable.clear_everything_enabled_light);
                            }
                        } else {
                            clearEverythingPreference.setIcon(R.drawable.clear_everything_disabled);
                        }

                        // Update the `clearCookiesPreference` icon.
                        if (newClearEverythingBoolean || sharedPreferences.getBoolean("clear_cookies", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_dark);
                            } else {
                                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_light);
                            }
                        } else {
                            clearCookiesPreference.setIcon(R.drawable.cookies_warning);
                        }

                        // Update the `clearDomStoragePreference` icon.
                        if (newClearEverythingBoolean || sharedPreferences.getBoolean("clear_dom_storage", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_dark);
                            } else {
                                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_light);
                            }
                        } else {
                            clearDomStoragePreference.setIcon(R.drawable.dom_storage_warning);
                        }

                        // Update the `clearFormDataPreference` icon.
                        if (newClearEverythingBoolean || sharedPreferences.getBoolean("clear_form_data", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_dark);
                            } else {
                                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_light);
                            }
                        } else {
                            clearFormDataPreference.setIcon(R.drawable.form_data_warning);
                        }

                        // Update the `clearCachePreference` icon.
                        if (newClearEverythingBoolean || sharedPreferences.getBoolean("clear_cache", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearCachePreference.setIcon(R.drawable.cache_cleared_dark);
                            } else {
                                clearCachePreference.setIcon(R.drawable.cache_cleared_light);
                            }
                        } else {
                            clearCachePreference.setIcon(R.drawable.cache_warning);
                        }
                        break;

                    case "clear_cookies":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("clear_cookies", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_dark);
                            } else {
                                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_light);
                            }
                        } else {
                            clearCookiesPreference.setIcon(R.drawable.cookies_warning);
                        }
                        break;

                    case "clear_dom_storage":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("clear_dom_storage", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_dark);
                            } else {
                                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_light);
                            }
                        } else {
                            clearDomStoragePreference.setIcon(R.drawable.dom_storage_warning);
                        }
                        break;

                    case "clear_form_data":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("clear_form_data", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_dark);
                            } else {
                                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_light);
                            }
                        } else {
                            clearFormDataPreference.setIcon(R.drawable.form_data_warning);
                        }
                        break;

                    case "clear_cache":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("clear_cache", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearCachePreference.setIcon(R.drawable.cache_cleared_dark);
                            } else {
                                clearCachePreference.setIcon(R.drawable.cache_cleared_light);
                            }
                        } else {
                            clearCachePreference.setIcon(R.drawable.cache_warning);
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
                            if (MainWebViewActivity.darkTheme) {
                                swipeToRefreshPreference.setIcon(R.drawable.refresh_enabled_dark);
                            } else {
                                swipeToRefreshPreference.setIcon(R.drawable.refresh_enabled_light);
                            }
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                swipeToRefreshPreference.setIcon(R.drawable.refresh_disabled_dark);
                            } else {
                                swipeToRefreshPreference.setIcon(R.drawable.refresh_disabled_light);
                            }
                        }
                        break;

                    case "display_additional_app_bar_icons":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("display_additional_app_bar_icons", false)) {
                            if (MainWebViewActivity.darkTheme) {
                                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_enabled_dark);
                            } else {
                                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_enabled_light);
                            }
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_disabled_dark);
                            } else {
                                displayAdditionalAppBarIconsPreference.setIcon(R.drawable.more_disabled_light);
                            }
                        }
                        break;

                    case "dark_theme":
                        // Update the icon.
                        if (sharedPreferences.getBoolean("dark_theme", false)) {
                            darkThemePreference.setIcon(R.drawable.theme_dark);
                        } else {
                            darkThemePreference.setIcon(R.drawable.theme_light);
                        }

                        // Create an `Intent` to restart Privacy Browser.
                        Intent intent = getActivity().getParentActivityIntent();

                        // Assert that `intent` is not `null` to remove the lint error below.
                        assert intent != null;

                        // `Intent.FLAG_ACTIVITY_CLEAR_TASK` removes all activities from the stack.  It requires `Intent.FLAG_ACTIVITY_NEW_TASK`.
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        // Make it so.
                        startActivity(intent);
                        break;

                    case "night_mode":
                        // Set the URL to be reloaded on restart to apply the new night mode setting.
                        MainWebViewActivity.loadUrlOnRestart = true;

                        // Store the current night mode status.
                        boolean currentNightModeBoolean = sharedPreferences.getBoolean("night_mode", false);
                        boolean currentJavaScriptBoolean = sharedPreferences.getBoolean("javascript_enabled", false);

                        // Update the icon.
                        if (currentNightModeBoolean) {
                            if (MainWebViewActivity.darkTheme) {
                                nightModePreference.setIcon(R.drawable.night_mode_enabled_dark);
                            } else {
                                nightModePreference.setIcon(R.drawable.night_mode_enabled_light);
                            }
                        } else {
                            if (MainWebViewActivity.darkTheme) {
                                nightModePreference.setIcon(R.drawable.night_mode_disabled_dark);
                            } else {
                                nightModePreference.setIcon(R.drawable.night_mode_disabled_light);
                            }
                        }

                        // Update the status of `javaScriptPreference` and `domStoragePreference`.
                        javaScriptPreference.setEnabled(!currentNightModeBoolean);
                        domStoragePreference.setEnabled(currentNightModeBoolean || currentJavaScriptBoolean);

                        // Update the `javaScriptPreference` icon.
                        if (currentNightModeBoolean || currentJavaScriptBoolean) {
                            javaScriptPreference.setIcon(R.drawable.javascript_enabled);
                        } else {
                            javaScriptPreference.setIcon(R.drawable.privacy_mode);
                        }

                        // Update the `domStoragePreference` icon.
                        if (currentNightModeBoolean || currentJavaScriptBoolean) {  // The preference is enabled.
                            if (sharedPreferences.getBoolean("dom_storage_enabled", false)) {  // DOM storage is enabled.
                                domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
                            } else {  // DOM storage is disabled.
                                if (MainWebViewActivity.darkTheme) {
                                    domStoragePreference.setIcon(R.drawable.dom_storage_disabled_dark);
                                } else {
                                    domStoragePreference.setIcon(R.drawable.dom_storage_disabled_light);
                                }
                            }
                        } else {  // The preference is disabled.  The icon should be ghosted.
                            if (MainWebViewActivity.darkTheme) {
                                domStoragePreference.setIcon(R.drawable.dom_storage_ghosted_dark);
                            } else {
                                domStoragePreference.setIcon(R.drawable.dom_storage_ghosted_light);
                            }
                        }
                        break;

                    case "display_webpage_images":
                        if (sharedPreferences.getBoolean("display_webpage_images", true)) {
                            // Update the icon.
                            if (MainWebViewActivity.darkTheme) {
                                displayWebpageImagesPreference.setIcon(R.drawable.images_enabled_dark);
                            } else {
                                displayWebpageImagesPreference.setIcon(R.drawable.images_enabled_light);
                            }

                            // `mainWebView` does not need to be reloaded because unloaded images will load automatically.
                            MainWebViewActivity.reloadOnRestart = false;
                        } else {
                            // Update the icon.
                            if (MainWebViewActivity.darkTheme) {
                                displayWebpageImagesPreference.setIcon(R.drawable.images_disabled_dark);
                            } else {
                                displayWebpageImagesPreference.setIcon(R.drawable.images_disabled_light);
                            }

                            // Set `mainWebView` to reload on restart to remove the current images.
                            MainWebViewActivity.reloadOnRestart = true;
                        }
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
