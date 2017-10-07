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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

public class GuideTabFragment extends Fragment {
    // `tabNumber` is used in `onCreate()` and `onCreateView()`.
    private int tabNumber;

    // Store the tab number in the arguments bundle.
    public static GuideTabFragment createTab (int tab) {
        // Create a bundle.
        Bundle bundle = new Bundle();

        // Store the tab number in the bundle.
        bundle.putInt("Tab", tab);

        // Add the bundle to the fragment.
        GuideTabFragment guideTabFragment = new GuideTabFragment();
        guideTabFragment.setArguments(bundle);

        // Return the new fragment.
        return guideTabFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Store the tab number in a class variable.
        tabNumber = getArguments().getInt("Tab");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.  The fragment will take care of attaching the root automatically.
        View tabLayout = inflater.inflate(R.layout.bare_webview, container, false);

        // Get a handle for `tabWebView`.
        WebView tabWebView = (WebView) tabLayout;

        // Load the tabs according to the theme.
        if (MainWebViewActivity.darkTheme) {  // The dark theme is applied.
            // Set the background color.  We have to use the deprecated `.getColor()` until API >= 23.
            //noinspection deprecation
            tabWebView.setBackgroundColor(getResources().getColor(R.color.gray_850));

            // Tab numbers start at 0.
            switch (tabNumber) {
                case 0:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_overview_dark.html");
                    break;

                case 1:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_javascript_dark.html");
                    break;

                case 2:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_local_storage_dark.html");
                    break;

                case 3:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_user_agent_dark.html");
                    break;

                case 4:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_domain_settings_dark.html");
                    break;

                case 5:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_ssl_certificates_dark.html");
                    break;

                case 6:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_tor_dark.html");
                    break;

                case 7:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_tracking_ids_dark.html");
                    break;
            }
        } else {  // The light theme is applied.
            // Tab numbers start at 0.
            switch (tabNumber) {
                case 0:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_overview_light.html");
                    break;

                case 1:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_javascript_light.html");
                    break;

                case 2:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_local_storage_light.html");
                    break;

                case 3:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_user_agent_light.html");
                    break;

                case 4:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_domain_settings_light.html");
                    break;

                case 5:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_ssl_certificates_light.html");
                    break;

                case 6:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_tor_light.html");
                    break;

                case 7:
                    tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_tracking_ids_light.html");
                    break;
            }
        }

        // Return the formatted `tabLayout`.
        return tabLayout;
    }
}
