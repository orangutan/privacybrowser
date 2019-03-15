/*
 * Copyright © 2016-2019 Soren Stoutner <soren@stoutner.com>.
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

public class SettingsFragment extends PreferenceFragment {
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;
    private SharedPreferences savedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Load the preferences from the XML file.
        addPreferencesFromResource(R.xml.preferences);

        // Get a handle for the context.
        Context context = getActivity().getApplicationContext();

        // Initialize savedPreferences.
        savedPreferences = getPreferenceScreen().getSharedPreferences();

        // Get handles for the preferences.
        final Preference javaScriptPreference = findPreference("javascript");
        final Preference firstPartyCookiesPreference = findPreference("first_party_cookies");
        final Preference thirdPartyCookiesPreference = findPreference("third_party_cookies");
        final Preference domStoragePreference = findPreference("dom_storage");
        final Preference saveFormDataPreference = findPreference("save_form_data");  // The form data preference can be removed once the minimum API >= 26.
        final Preference userAgentPreference = findPreference("user_agent");
        final Preference customUserAgentPreference = findPreference("custom_user_agent");
        final Preference incognitoModePreference = findPreference("incognito_mode");
        final Preference doNotTrackPreference = findPreference("do_not_track");
        final Preference allowScreenshotsPreference = findPreference("allow_screenshots");
        final Preference easyListPreference = findPreference("easylist");
        final Preference easyPrivacyPreference = findPreference("easyprivacy");
        final Preference fanboyAnnoyanceListPreference = findPreference("fanboys_annoyance_list");
        final Preference fanboySocialBlockingListPreference = findPreference("fanboys_social_blocking_list");
        final Preference ultraPrivacyPreference = findPreference("ultraprivacy");
        final Preference blockAllThirdPartyRequestsPreference = findPreference("block_all_third_party_requests");
        final Preference proxyThroughOrbotPreference = findPreference("proxy_through_orbot");
        final Preference torHomepagePreference = findPreference("tor_homepage");
        final Preference torSearchPreference = findPreference("tor_search");
        final Preference torSearchCustomURLPreference = findPreference("tor_search_custom_url");
        final Preference searchPreference = findPreference("search");
        final Preference searchCustomURLPreference = findPreference("search_custom_url");
        final Preference fullScreenBrowsingModePreference = findPreference("full_screen_browsing_mode");
        final Preference hideAppBarPreference = findPreference("hide_app_bar");
        final Preference clearEverythingPreference = findPreference("clear_everything");
        final Preference clearCookiesPreference = findPreference("clear_cookies");
        final Preference clearDomStoragePreference = findPreference("clear_dom_storage");
        final Preference clearFormDataPreference = findPreference("clear_form_data");  // The clear form data preference can be removed once the minimum API >= 26.
        final Preference clearCachePreference = findPreference("clear_cache");
        final Preference homepagePreference = findPreference("homepage");
        final Preference fontSizePreference = findPreference("font_size");
        final Preference swipeToRefreshPreference = findPreference("swipe_to_refresh");
        final Preference scrollAppBarPreference = findPreference("scroll_app_bar");
        final Preference displayAdditionalAppBarIconsPreference = findPreference("display_additional_app_bar_icons");
        final Preference downloadWithExternalAppPreference = findPreference("download_with_external_app");
        final Preference darkThemePreference = findPreference("dark_theme");
        final Preference nightModePreference = findPreference("night_mode");
        final Preference displayWebpageImagesPreference = findPreference("display_webpage_images");

        // Set dependencies.
        torHomepagePreference.setDependency("proxy_through_orbot");
        torSearchPreference.setDependency("proxy_through_orbot");
        hideAppBarPreference.setDependency("full_screen_browsing_mode");

        // Get strings from the preferences.
        String torSearchString = savedPreferences.getString("tor_search", getString(R.string.tor_search_default_value));
        String searchString = savedPreferences.getString("search", getString(R.string.search_default_value));

        // Get booleans that are used in multiple places from the preferences.
        final boolean javaScriptEnabled = savedPreferences.getBoolean("javascript", false);
        boolean firstPartyCookiesEnabled = savedPreferences.getBoolean("first_party_cookies", false);
        boolean thirdPartyCookiesEnabled = savedPreferences.getBoolean("third_party_cookies", false);
        boolean fanboyAnnoyanceListEnabled = savedPreferences.getBoolean("fanboys_annoyance_list", true);
        boolean fanboySocialBlockingEnabled = savedPreferences.getBoolean("fanboys_social_blocking_list", true);
        boolean proxyThroughOrbot = savedPreferences.getBoolean("proxy_through_orbot", false);
        boolean fullScreenBrowsingMode = savedPreferences.getBoolean("full_screen_browsing_mode", false);
        boolean clearEverything = savedPreferences.getBoolean("clear_everything", true);
        final boolean nightMode = savedPreferences.getBoolean("night_mode", false);

        // Only enable the third-party cookies preference if first-party cookies are enabled and API >= 21.
        thirdPartyCookiesPreference.setEnabled(firstPartyCookiesEnabled && (Build.VERSION.SDK_INT >= 21));

        // Only enable the DOM storage preference if either JavaScript or Night Mode is enabled.
        domStoragePreference.setEnabled(javaScriptEnabled || nightMode);

        // Remove the form data preferences if the API is >= 26 as they no longer do anything.
        if (Build.VERSION.SDK_INT >= 26) {
            // Get the categories.
            PreferenceCategory privacyCategory = (PreferenceCategory) findPreference("privacy");
            PreferenceCategory clearAndExitCategory = (PreferenceCategory) findPreference("clear_and_exit");

            // Remove the form data preferences.
            privacyCategory.removePreference(saveFormDataPreference);
            clearAndExitCategory.removePreference(clearFormDataPreference);
        }

        // Only enable Fanboy's social blocking list preference if Fanboy's annoyance list is disabled.
        fanboySocialBlockingListPreference.setEnabled(!fanboyAnnoyanceListEnabled);

        // Inflate a WebView to get the default user agent.
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // `@SuppressLint("InflateParams")` removes the warning about using `null` as the `ViewGroup`, which in this case makes sense because the `bare_webview` will not be displayed.
        @SuppressLint("InflateParams") View bareWebViewLayout = inflater.inflate(R.layout.bare_webview, null, false);
        final WebView bareWebView = bareWebViewLayout.findViewById(R.id.bare_webview);

        // Get the user agent arrays.
        ArrayAdapter<CharSequence> userAgentNamesArray = ArrayAdapter.createFromResource(context, R.array.user_agent_names, R.layout.spinner_item);
        String[] translatedUserAgentNamesArray = getResources().getStringArray(R.array.translated_user_agent_names);
        String[] userAgentDataArray = getResources().getStringArray(R.array.user_agent_data);

        // Get the current user agent name from the preference.
        String userAgentName = savedPreferences.getString("user_agent", getString(R.string.user_agent_default_value));

        // Get the array position of the user agent name.
        int userAgentArrayPosition = userAgentNamesArray.getPosition(userAgentName);

        // Populate the user agent summary.
        switch (userAgentArrayPosition) {
            case MainWebViewActivity.UNRECOGNIZED_USER_AGENT:  // The user agent name is not on the canonical list.
                // This is probably because it was set in an older version of Privacy Browser before the switch to persistent user agent names.  Use the current user agent entry name as the summary.
                userAgentPreference.setSummary(userAgentName);
                break;

            case MainWebViewActivity.SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                // Get the user agent text from the webview (which changes based on the version of Android and WebView installed).
                userAgentPreference.setSummary(translatedUserAgentNamesArray[userAgentArrayPosition] + ":\n" + bareWebView.getSettings().getUserAgentString());
                break;

            case MainWebViewActivity.SETTINGS_CUSTOM_USER_AGENT:
                // Set the summary text.
                userAgentPreference.setSummary(R.string.custom_user_agent);
                break;

            default:
                // Get the user agent summary from the user agent data array.
                userAgentPreference.setSummary(translatedUserAgentNamesArray[userAgentArrayPosition] + ":\n" + userAgentDataArray[userAgentArrayPosition]);
        }

        // Set the summary text for the custom user agent preference and enable it if user agent preference is set to custom.
        customUserAgentPreference.setSummary(savedPreferences.getString("custom_user_agent", getString(R.string.custom_user_agent_default_value)));
        customUserAgentPreference.setEnabled(userAgentPreference.getSummary().equals(getString(R.string.custom_user_agent)));


        // Set the Tor homepage URL as the summary text for the `tor_homepage` preference when the preference screen is loaded.  The default is Searx: `http://ulrn6sryqaifefld.onion/`.
        torHomepagePreference.setSummary(savedPreferences.getString("tor_homepage", getString(R.string.tor_homepage_default_value)));


        // Set the Tor search URL as the summary text for the Tor preference when the preference screen is loaded.
        if (torSearchString.equals("Custom URL")) {
            // Use R.string.custom_url, which will be translated, instead of the array value, which will not.
            torSearchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            torSearchPreference.setSummary(torSearchString);
        }

        // Set the summary text for `tor_search_custom_url`.  The default is `""`.
        torSearchCustomURLPreference.setSummary(savedPreferences.getString("tor_search_custom_url", getString(R.string.tor_search_custom_url_default_value)));

        // Enable the Tor custom URL search options only if proxying through Orbot and the search is set to `Custom URL`.
        torSearchCustomURLPreference.setEnabled(proxyThroughOrbot && torSearchString.equals("Custom URL"));


        // Set the search URL as the summary text for the search preference when the preference screen is loaded.
        if (searchString.equals("Custom URL")) {
            // Use R.string.custom_url, which will be translated, instead of the array value, which will not.
            searchPreference.setSummary(R.string.custom_url);
        } else {
            // Set the array value as the summary text.
            searchPreference.setSummary(searchString);
        }

        // Set the summary text for `search_custom_url` (the default is `""`) and enable it if `search` is set to `Custom URL`.
        searchCustomURLPreference.setSummary(savedPreferences.getString("search_custom_url", getString(R.string.search_custom_url_default_value)));
        searchCustomURLPreference.setEnabled(searchString.equals("Custom URL"));

        // Set the status of the Clear and Exit preferences.
        clearCookiesPreference.setEnabled(!clearEverything);
        clearDomStoragePreference.setEnabled(!clearEverything);
        clearFormDataPreference.setEnabled(!clearEverything);  // The form data line can be removed once the minimum API is >= 26.
        clearCachePreference.setEnabled(!clearEverything);

        // Set the homepage URL as the summary text for the homepage preference.
        homepagePreference.setSummary(savedPreferences.getString("homepage", getString(R.string.homepage_default_value)));

        // Set the font size as the summary text for the preference.
        fontSizePreference.setSummary(savedPreferences.getString("font_size", getString(R.string.font_size_default_value)) + "%%");

        // Disable the JavaScript preference if Night Mode is enabled.  JavaScript will be enabled for all web pages.
        javaScriptPreference.setEnabled(!nightMode);

        // Set the JavaScript icon.
        if (javaScriptEnabled || nightMode) {
            javaScriptPreference.setIcon(R.drawable.javascript_enabled);
        } else {
            javaScriptPreference.setIcon(R.drawable.privacy_mode);
        }

        // Set the first-party cookies icon.
        if (firstPartyCookiesEnabled) {
            firstPartyCookiesPreference.setIcon(R.drawable.cookies_enabled);
        } else {
            if (MainWebViewActivity.darkTheme) {
                firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
            } else {
                firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
            }
        }

        // Set the third party cookies icon.
        if (firstPartyCookiesEnabled && Build.VERSION.SDK_INT >= 21) {
            if (thirdPartyCookiesEnabled) {
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

        // Set the DOM storage icon.
        if (javaScriptEnabled || nightMode) {  // The preference is enabled.
            if (savedPreferences.getBoolean("dom_storage", false)) {  // DOM storage is enabled.
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

        // Set the save form data icon if API < 26.  Save form data has no effect on API >= 26.
        if (Build.VERSION.SDK_INT < 26) {
            if (savedPreferences.getBoolean("save_form_data", false)) {
                saveFormDataPreference.setIcon(R.drawable.form_data_enabled);
            } else {
                if (MainWebViewActivity.darkTheme) {
                    saveFormDataPreference.setIcon(R.drawable.form_data_disabled_dark);
                } else {
                    saveFormDataPreference.setIcon(R.drawable.form_data_disabled_light);
                }
            }
        }

        // Set the custom user agent icon.
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

        // Set the incognito mode icon.
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

        // Set the Do Not Track icon.
        if (savedPreferences.getBoolean("do_not_track", false)) {
            if (MainWebViewActivity.darkTheme) {
                doNotTrackPreference.setIcon(R.drawable.block_tracking_enabled_dark);
            } else {
                doNotTrackPreference.setIcon(R.drawable.block_tracking_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                doNotTrackPreference.setIcon(R.drawable.block_tracking_disabled_dark);
            } else {
                doNotTrackPreference.setIcon(R.drawable.block_tracking_disabled_light);
            }
        }

        // Set the allow screenshots icon.
        if (savedPreferences.getBoolean("allow_screenshots", false)) {
            if (MainWebViewActivity.darkTheme) {
                allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_enabled_dark);
            } else {
                allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_disabled_dark);
            } else {
                allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_disabled_light);
            }
        }

        // Set the EasyList icon.
        if (savedPreferences.getBoolean("easylist", true)) {
            if (MainWebViewActivity.darkTheme) {
                easyListPreference.setIcon(R.drawable.block_ads_enabled_dark);
            } else {
                easyListPreference.setIcon(R.drawable.block_ads_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                easyListPreference.setIcon(R.drawable.block_ads_disabled_dark);
            } else {
                easyListPreference.setIcon(R.drawable.block_ads_disabled_light);
            }
        }

        // Set the EasyPrivacy icon.
        if (savedPreferences.getBoolean("easyprivacy", true)) {
            if (MainWebViewActivity.darkTheme) {
                easyPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_dark);
            } else {
                easyPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                easyPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_dark);
            } else {
                easyPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_light);
            }
        }

        // Set the Fanboy lists icons.
        if (fanboyAnnoyanceListEnabled) {
            if (MainWebViewActivity.darkTheme) {
                // Set the Fanboy annoyance list icon.
                fanboyAnnoyanceListPreference.setIcon(R.drawable.social_media_enabled_dark);

                // Set the Fanboy social blocking list icon.
                fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_ghosted_dark);
            } else {
                // Set the Fanboy annoyance list icon.
                fanboyAnnoyanceListPreference.setIcon(R.drawable.social_media_enabled_light);

                // Set the Fanboy social blocking list icon.
                fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_ghosted_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                // Set the Fanboy annoyance list icon.
                fanboyAnnoyanceListPreference.setIcon(R.drawable.social_media_disabled_dark);

                // Set the Fanboy social blocking list icon.
                if (fanboySocialBlockingEnabled) {
                    fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_enabled_dark);
                } else {
                    fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_disabled_dark);
                }
            } else {
                // Set the Fanboy annoyance list icon.
                fanboyAnnoyanceListPreference.setIcon(R.drawable.block_ads_disabled_light);

                // Set the Fanboy social blocking list icon.
                if (fanboySocialBlockingEnabled) {
                    fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_enabled_light);
                } else {
                    fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_disabled_light);
                }
            }
        }

        // Set the UltraPrivacy icon.
        if (savedPreferences.getBoolean("ultraprivacy", true)) {
            if (MainWebViewActivity.darkTheme) {
                ultraPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_dark);
            } else {
                ultraPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                ultraPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_dark);
            } else {
                ultraPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_light);
            }
        }

        // Set the block all third-party requests icon.
        if (savedPreferences.getBoolean("block_all_third_party_requests", false)) {
            if (MainWebViewActivity.darkTheme) {
                blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_enabled_dark);
            } else {
                blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_disabled_dark);
            } else {
                blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_disabled_light);
            }
        }

        // Set the Tor icons according to the theme.
        if (proxyThroughOrbot) {  // Proxying is enabled.
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

        // Set the search custom URL icon.
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
        if (fullScreenBrowsingMode) {  // Full screen browsing mode is enabled.
            // Set the `fullScreenBrowsingModePreference` icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_dark);
            } else {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_light);
            }

            // Set the hide app bar icon.
            if (savedPreferences.getBoolean("hide_app_bar", true)) {  // Hide app bar is enabled.
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    hideAppBarPreference.setIcon(R.drawable.app_bar_enabled_dark);
                } else {
                    hideAppBarPreference.setIcon(R.drawable.app_bar_enabled_light);
                }
            } else {  // Hide app bar is disabled.
                // Set the icon according to the theme.
                if (MainWebViewActivity.darkTheme) {
                    hideAppBarPreference.setIcon(R.drawable.app_bar_disabled_dark);
                } else {
                    hideAppBarPreference.setIcon(R.drawable.app_bar_disabled_light);
                }
            }
        } else {  // Full screen browsing mode is disabled.
            // Set the icons according to the theme.
            if (MainWebViewActivity.darkTheme) {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_dark);
                hideAppBarPreference.setIcon(R.drawable.app_bar_ghosted_dark);
            } else {
                fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_light);
                hideAppBarPreference.setIcon(R.drawable.app_bar_ghosted_light);
            }
        }

        // Set the clear everything preference icon.
        if (clearEverything) {
            if (MainWebViewActivity.darkTheme) {
                clearEverythingPreference.setIcon(R.drawable.clear_everything_enabled_dark);
            } else {
                clearEverythingPreference.setIcon(R.drawable.clear_everything_enabled_light);
            }
        } else {
            clearEverythingPreference.setIcon(R.drawable.clear_everything_disabled);
        }

        // Set the clear cookies preference icon.
        if (clearEverything || savedPreferences.getBoolean("clear_cookies", true)) {
            if (MainWebViewActivity.darkTheme) {
                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_dark);
            } else {
                clearCookiesPreference.setIcon(R.drawable.cookies_cleared_light);
            }
        } else {
            clearCookiesPreference.setIcon(R.drawable.cookies_warning);
        }

        // Set the clear DOM storage preference icon.
        if (clearEverything || savedPreferences.getBoolean("clear_dom_storage", true)) {
            if (MainWebViewActivity.darkTheme) {
                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_dark);
            } else {
                clearDomStoragePreference.setIcon(R.drawable.dom_storage_cleared_light);
            }
        } else {
            clearDomStoragePreference.setIcon(R.drawable.dom_storage_warning);
        }

        // Set the clear form data preference icon if the API < 26.  It has no effect on newer versions of Android.
        if (Build.VERSION.SDK_INT < 26) {
            if (clearEverything || savedPreferences.getBoolean("clear_form_data", true)) {
                if (MainWebViewActivity.darkTheme) {
                    clearFormDataPreference.setIcon(R.drawable.form_data_cleared_dark);
                } else {
                    clearFormDataPreference.setIcon(R.drawable.form_data_cleared_light);
                }
            } else {
                clearFormDataPreference.setIcon(R.drawable.form_data_warning);
            }
        }

        // Set the clear cache preference icon.
        if (clearEverything || savedPreferences.getBoolean("clear_cache", true)) {
            if (MainWebViewActivity.darkTheme) {
                clearCachePreference.setIcon(R.drawable.cache_cleared_dark);
            } else {
                clearCachePreference.setIcon(R.drawable.cache_cleared_light);
            }
        } else {
            clearCachePreference.setIcon(R.drawable.cache_warning);
        }

        // Set the swipe to refresh preference icon.
        if (savedPreferences.getBoolean("swipe_to_refresh", true)) {
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

        // Set the scroll app bar preference icon.
        if (savedPreferences.getBoolean("scroll_app_bar", true)) {
            if (MainWebViewActivity.darkTheme) {
                scrollAppBarPreference.setIcon(R.drawable.app_bar_enabled_dark);
            } else {
                scrollAppBarPreference.setIcon(R.drawable.app_bar_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                scrollAppBarPreference.setIcon(R.drawable.app_bar_disabled_dark);
            } else {
                scrollAppBarPreference.setIcon(R.drawable.app_bar_disabled_light);
            }
        }

        // Set the display additional app bar icons preference icon.
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

        // Set the download with external app preference icon.
        if (savedPreferences.getBoolean("download_with_external_app", false)) {
            if (MainWebViewActivity.darkTheme) {
                downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_enabled_dark);
            } else {
                downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_enabled_light);
            }
        } else {
            if (MainWebViewActivity.darkTheme) {
                downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_disabled_dark);
            } else {
                downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_disabled_light);
            }
        }

        // Set the dark theme preference icon.
        if (savedPreferences.getBoolean("dark_theme", false)) {
            darkThemePreference.setIcon(R.drawable.theme_dark);
        } else {
            darkThemePreference.setIcon(R.drawable.theme_light);
        }

        // Set the night mode preference icon.
        if (nightMode) {
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

        // Set the display webpage images preference icon.
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
        preferencesListener = (SharedPreferences sharedPreferences, String key) -> {
            switch (key) {
                case "javascript":
                    // Update the icons and the DOM storage preference status.
                    if (sharedPreferences.getBoolean("javascript", false)) {  // The JavaScript preference is enabled.
                        // Update the icon for the JavaScript preference.
                        javaScriptPreference.setIcon(R.drawable.javascript_enabled);

                        // Update the status of the DOM storage preference.
                        domStoragePreference.setEnabled(true);

                        // Update the icon for the DOM storage preference.
                        if (sharedPreferences.getBoolean("dom_storage", false)) {
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

                case "first_party_cookies":
                    // Update the icons for `first_party_cookies` and `third_party_cookies`.
                    if (sharedPreferences.getBoolean("first_party_cookies", false)) {
                        // Set the icon for `first_party_cookies`.
                        firstPartyCookiesPreference.setIcon(R.drawable.cookies_enabled);

                        // Update the icon for `third_party_cookies`.
                        if (Build.VERSION.SDK_INT >= 21) {
                            if (sharedPreferences.getBoolean("third_party_cookies", false)) {
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
                    } else {  // `first_party_cookies` is `false`.
                        // Update the icon for `first_party_cookies`.
                        if (MainWebViewActivity.darkTheme) {
                            firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
                        } else {
                            firstPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
                        }

                        // Set the icon for `third_party_cookies` to be ghosted.
                        if (MainWebViewActivity.darkTheme) {
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_dark);
                        } else {
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_ghosted_light);
                        }
                    }

                    // Enable `third_party_cookies` if `first_party_cookies` is `true` and API >= 21.
                    thirdPartyCookiesPreference.setEnabled(sharedPreferences.getBoolean("first_party_cookies", false) && (Build.VERSION.SDK_INT >= 21));
                    break;

                case "third_party_cookies":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("third_party_cookies", false)) {
                        thirdPartyCookiesPreference.setIcon(R.drawable.cookies_warning);
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_dark);
                        } else {
                            thirdPartyCookiesPreference.setIcon(R.drawable.cookies_disabled_light);
                        }
                    }
                    break;

                case "dom_storage":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("dom_storage", false)) {
                        domStoragePreference.setIcon(R.drawable.dom_storage_enabled);
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            domStoragePreference.setIcon(R.drawable.dom_storage_disabled_dark);
                        } else {
                            domStoragePreference.setIcon(R.drawable.dom_storage_disabled_light);
                        }
                    }
                    break;

                // Save form data can be removed once the minimum API >= 26.
                case "save_form_data":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("save_form_data", false)) {
                        saveFormDataPreference.setIcon(R.drawable.form_data_enabled);
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            saveFormDataPreference.setIcon(R.drawable.form_data_disabled_dark);
                        } else {
                            saveFormDataPreference.setIcon(R.drawable.form_data_disabled_light);
                        }
                    }

                case "user_agent":
                    // Get the new user agent name.
                    String newUserAgentName = sharedPreferences.getString("user_agent", getString(R.string.user_agent_default_value));

                    // Get the array position for the new user agent name.
                    int newUserAgentArrayPosition = userAgentNamesArray.getPosition(newUserAgentName);

                    // Get the translated new user agent name.
                    String translatedNewUserAgentName = translatedUserAgentNamesArray[newUserAgentArrayPosition];

                    // Populate the user agent summary.
                    switch (newUserAgentArrayPosition) {
                        case MainWebViewActivity.SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                            // Get the user agent text from the webview (which changes based on the version of Android and WebView installed).
                            userAgentPreference.setSummary(translatedNewUserAgentName + ":\n" + bareWebView.getSettings().getUserAgentString());

                            // Disable the custom user agent preference.
                            customUserAgentPreference.setEnabled(false);

                            // Set the custom user agent preference icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_dark);
                            } else {
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_light);
                            }
                            break;

                        case MainWebViewActivity.SETTINGS_CUSTOM_USER_AGENT:
                            // Set the summary text.
                            userAgentPreference.setSummary(R.string.custom_user_agent);

                            // Enable the custom user agent preference.
                            customUserAgentPreference.setEnabled(true);

                            // Set the custom user agent preference icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled_dark);
                            } else {
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_enabled_light);
                            }
                            break;

                        default:
                            // Get the user agent summary from the user agent data array.
                            userAgentPreference.setSummary(translatedNewUserAgentName + ":\n" + userAgentDataArray[newUserAgentArrayPosition]);

                            // Disable the custom user agent preference.
                            customUserAgentPreference.setEnabled(false);

                            // Set the custom user agent preference icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_dark);
                            } else {
                                customUserAgentPreference.setIcon(R.drawable.custom_user_agent_ghosted_light);
                            }
                    }
                    break;

                case "custom_user_agent":
                    // Set the new custom user agent as the summary text for the preference.
                    customUserAgentPreference.setSummary(sharedPreferences.getString("custom_user_agent", getString(R.string.custom_user_agent_default_value)));
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
                            doNotTrackPreference.setIcon(R.drawable.block_tracking_enabled_dark);
                        } else {
                            doNotTrackPreference.setIcon(R.drawable.block_tracking_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            doNotTrackPreference.setIcon(R.drawable.block_tracking_disabled_dark);
                        } else {
                            doNotTrackPreference.setIcon(R.drawable.block_tracking_disabled_light);
                        }
                    }

                    break;

                case "allow_screenshots":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("allow_screenshots", false)) {
                        if (MainWebViewActivity.darkTheme) {
                            allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_enabled_dark);
                        } else {
                            allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_disabled_dark);
                        } else {
                            allowScreenshotsPreference.setIcon(R.drawable.allow_screenshots_disabled_light);
                        }
                    }

                    // Create an intent to restart Privacy Browser.
                    Intent allowScreenshotsRestartIntent = getActivity().getParentActivityIntent();

                    // Assert that the intent is not null to remove the lint error below.
                    assert allowScreenshotsRestartIntent != null;

                    // `Intent.FLAG_ACTIVITY_CLEAR_TASK` removes all activities from the stack.  It requires `Intent.FLAG_ACTIVITY_NEW_TASK`.
                    allowScreenshotsRestartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // Make it so.
                    startActivity(allowScreenshotsRestartIntent);
                    break;

                case "easylist":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("easylist", true)) {
                        if (MainWebViewActivity.darkTheme) {
                            easyListPreference.setIcon(R.drawable.block_ads_enabled_dark);
                        } else {
                            easyListPreference.setIcon(R.drawable.block_ads_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            easyListPreference.setIcon(R.drawable.block_ads_disabled_dark);
                        } else {
                            easyListPreference.setIcon(R.drawable.block_ads_disabled_light);
                        }
                    }
                    break;

                case "easyprivacy":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("easyprivacy", true)) {
                        if (MainWebViewActivity.darkTheme) {
                            easyPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_dark);
                        } else {
                            easyPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            easyPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_dark);
                        } else {
                            easyPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_light);
                        }
                    }
                    break;

                case "fanboys_annoyance_list":
                    boolean currentFanboyAnnoyanceList = sharedPreferences.getBoolean("fanboys_annoyance_list", true);
                    boolean currentFanboySocialBlockingList = sharedPreferences.getBoolean("fanboys_social_blocking_list", true);

                    // Update the Fanboy icons.
                    if (currentFanboyAnnoyanceList) {  // Fanboy's annoyance list is enabled.
                        if (MainWebViewActivity.darkTheme) {
                            // Update the Fanboy's annoyance list icon.
                            fanboyAnnoyanceListPreference.setIcon(R.drawable.social_media_enabled_dark);

                            // Update the Fanboy's social blocking list icon.
                            fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_ghosted_dark);
                        } else {
                            // Update the Fanboy's annoyance list icon.
                            fanboyAnnoyanceListPreference.setIcon(R.drawable.social_media_enabled_light);

                            // Update the Fanboy's social blocking list icon.
                            fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_ghosted_light);
                        }
                    } else {  // Fanboy's annoyance list is disabled.
                        if (MainWebViewActivity.darkTheme) {
                            // Update the Fanboy's annoyance list icon.
                            fanboyAnnoyanceListPreference.setIcon(R.drawable.social_media_disabled_dark);

                            // Update the Fanboy's social blocking list icon.
                            if (currentFanboySocialBlockingList) {
                                fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_enabled_dark);
                            } else {
                                fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_disabled_dark);
                            }
                        } else {
                            // Update the Fanboy's annoyance list icon.
                            fanboyAnnoyanceListPreference.setIcon(R.drawable.social_media_disabled_light);

                            // Update the Fanboy's social blocking list icon.
                            if (currentFanboySocialBlockingList) {
                                fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_enabled_light);
                            } else {
                                fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_disabled_light);
                            }
                        }
                    }

                    // Only enable Fanboy's social blocking list preference if Fanboy's annoyance list preference is disabled.
                    fanboySocialBlockingListPreference.setEnabled(!currentFanboyAnnoyanceList);
                    break;

                case "fanboys_social_blocking_list":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("fanboys_social_blocking_list", true)) {
                        if (MainWebViewActivity.darkTheme) {
                            fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_enabled_dark);
                        } else {
                            fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_disabled_dark);
                        } else {
                            fanboySocialBlockingListPreference.setIcon(R.drawable.social_media_disabled_light);
                        }
                    }
                    break;

                case "ultraprivacy":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("ultraprivacy", true)) {
                        if (MainWebViewActivity.darkTheme) {
                            ultraPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_dark);
                        } else {
                            ultraPrivacyPreference.setIcon(R.drawable.block_tracking_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            ultraPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_dark);
                        } else {
                            ultraPrivacyPreference.setIcon(R.drawable.block_tracking_disabled_light);
                        }
                    }
                    break;

                case "block_all_third_party_requests":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("block_all_third_party_requests", false)) {
                        if (MainWebViewActivity.darkTheme) {
                            blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_enabled_dark);
                        } else {
                            blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_disabled_dark);
                        } else {
                            blockAllThirdPartyRequestsPreference.setIcon(R.drawable.block_all_third_party_requests_disabled_light);
                        }
                    }
                    break;

                case "proxy_through_orbot":
                    // Get current settings.
                    boolean currentProxyThroughOrbot = sharedPreferences.getBoolean("proxy_through_orbot", false);
                    String currentTorSearchString = sharedPreferences.getString("tor_search", getString(R.string.tor_search_default_value));

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
                    // Set the new tor homepage URL as the summary text for the `tor_homepage` preference.  The default is Searx:  `http://ulrn6sryqaifefld.onion/`.
                    torHomepagePreference.setSummary(sharedPreferences.getString("tor_homepage", getString(R.string.tor_homepage_default_value)));
                    break;

                case "tor_search":
                    // Get the present search string.
                    String presentTorSearchString = sharedPreferences.getString("tor_search", getString(R.string.tor_search_default_value));

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
                    torSearchCustomURLPreference.setSummary(sharedPreferences.getString("tor_search_custom_url", getString(R.string.tor_search_custom_url_default_value)));
                    break;

                case "search":
                    // Store the new search string.
                    String newSearchString = sharedPreferences.getString("search", getString(R.string.search_default_value));

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
                    searchCustomURLPreference.setSummary(sharedPreferences.getString("search_custom_url", getString(R.string.search_custom_url_default_value)));
                    break;

                case "full_screen_browsing_mode":
                    if (sharedPreferences.getBoolean("full_screen_browsing_mode", false)) {  // Full screen browsing is enabled.
                        // Set the full screen browsing mode preference icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_dark);
                        } else {
                            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_enabled_light);
                        }

                        // Set the hide app bar preference icon.
                        if (sharedPreferences.getBoolean("hide_app_bar", true)) {  //  Hide app bar is enabled.
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                hideAppBarPreference.setIcon(R.drawable.app_bar_enabled_dark);
                            } else {
                                hideAppBarPreference.setIcon(R.drawable.app_bar_enabled_light);
                            }
                        } else {  // Hide app bar is disabled.
                            // Set the icon according to the theme.
                            if (MainWebViewActivity.darkTheme) {
                                hideAppBarPreference.setIcon(R.drawable.app_bar_disabled_dark);
                            } else {
                                hideAppBarPreference.setIcon(R.drawable.app_bar_disabled_light);
                            }
                        }
                    } else {  // Full screen browsing is disabled.
                        // Update the icons according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_dark);
                            hideAppBarPreference.setIcon(R.drawable.app_bar_ghosted_dark);
                        } else {
                            fullScreenBrowsingModePreference.setIcon(R.drawable.full_screen_disabled_light);
                            hideAppBarPreference.setIcon(R.drawable.app_bar_ghosted_light);
                        }
                    }
                    break;

                case "hide_app_bar":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("hide_app_bar", true)) {  // Hide app bar is enabled.
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            hideAppBarPreference.setIcon(R.drawable.app_bar_enabled_dark);
                        } else {
                            hideAppBarPreference.setIcon(R.drawable.app_bar_enabled_light);
                        }
                    } else {  // Hide app bar is disabled.
                        // Set the icon according to the theme.
                        if (MainWebViewActivity.darkTheme) {
                            hideAppBarPreference.setIcon(R.drawable.app_bar_disabled_dark);
                        } else {
                            hideAppBarPreference.setIcon(R.drawable.app_bar_disabled_light);
                        }
                    }
                    break;

                case "clear_everything":
                    // Store the new `clear_everything` status
                    boolean newClearEverythingBoolean = sharedPreferences.getBoolean("clear_everything", true);

                    // Update the status of the `Clear and Exit` preferences.
                    clearCookiesPreference.setEnabled(!newClearEverythingBoolean);
                    clearDomStoragePreference.setEnabled(!newClearEverythingBoolean);
                    clearFormDataPreference.setEnabled(!newClearEverythingBoolean);  // This line can be removed once the minimum API >= 26.
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

                    // Update the clear form data preference icon if the API < 26.
                    if (Build.VERSION.SDK_INT < 26) {
                        if (newClearEverythingBoolean || sharedPreferences.getBoolean("clear_form_data", true)) {
                            if (MainWebViewActivity.darkTheme) {
                                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_dark);
                            } else {
                                clearFormDataPreference.setIcon(R.drawable.form_data_cleared_light);
                            }
                        } else {
                            clearFormDataPreference.setIcon(R.drawable.form_data_warning);
                        }
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

                // This section can be removed once the minimum API >= 26.
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
                    // Set the new homepage URL as the summary text for the Homepage preference.
                    homepagePreference.setSummary(sharedPreferences.getString("homepage", getString(R.string.homepage_default_value)));
                    break;

                case "font_size":
                    // Update the font size summary text.
                    fontSizePreference.setSummary(sharedPreferences.getString("font_size", getString(R.string.font_size_default_value)) + "%%");
                    break;

                case "swipe_to_refresh":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("swipe_to_refresh", true)) {
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

                case "scroll_app_bar":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("scroll_app_bar", true)) {
                        if (MainWebViewActivity.darkTheme) {
                            scrollAppBarPreference.setIcon(R.drawable.app_bar_enabled_dark);
                        } else {
                            scrollAppBarPreference.setIcon(R.drawable.app_bar_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            scrollAppBarPreference.setIcon(R.drawable.app_bar_disabled_dark);
                        } else {
                            scrollAppBarPreference.setIcon(R.drawable.app_bar_disabled_light);
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

                case "download_with_external_app":
                    // Update the icon.
                    if (sharedPreferences.getBoolean("download_with_external_app", false)) {
                        if (MainWebViewActivity.darkTheme) {
                            downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_enabled_dark);
                        } else {
                            downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_enabled_light);
                        }
                    } else {
                        if (MainWebViewActivity.darkTheme) {
                            downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_disabled_dark);
                        } else {
                            downloadWithExternalAppPreference.setIcon(R.drawable.open_with_external_app_disabled_light);
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

                    // Create an intent to restart Privacy Browser.
                    Intent changeThemeRestartIntent = getActivity().getParentActivityIntent();

                    // Assert that the intent is not null to remove the lint error below.
                    assert changeThemeRestartIntent != null;

                    // `Intent.FLAG_ACTIVITY_CLEAR_TASK` removes all activities from the stack.  It requires `Intent.FLAG_ACTIVITY_NEW_TASK`.
                    changeThemeRestartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // Make it so.
                    startActivity(changeThemeRestartIntent);
                    break;

                case "night_mode":
                    // Set the URL to be reloaded on restart to apply the new night mode setting.
                    MainWebViewActivity.loadUrlOnRestart = true;

                    // Store the current night mode status.
                    boolean currentNightModeBoolean = sharedPreferences.getBoolean("night_mode", false);
                    boolean currentJavaScriptBoolean = sharedPreferences.getBoolean("javascript", false);

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

                    // Update the DOM storage preference icon.
                    if (currentNightModeBoolean || currentJavaScriptBoolean) {  // The preference is enabled.
                        if (sharedPreferences.getBoolean("dom_storage", false)) {  // DOM storage is enabled.
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
        };

        // Register the listener.
        savedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener);
    }

    // It is necessary to re-register the listener on every resume or it will randomly stop working because apps can be paused and resumed at any time, even while running in the foreground.
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
