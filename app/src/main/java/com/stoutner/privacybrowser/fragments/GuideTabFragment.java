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

import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
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

    // GuideTabFragment.createTab stores the tab number in the bundle arguments so it can be referenced from onCreate().
    public static GuideTabFragment createTab (int tab) {
        Bundle thisTabArguments = new Bundle();
        thisTabArguments.putInt("Tab", tab);

        GuideTabFragment thisTab = new GuideTabFragment();
        thisTab.setArguments(thisTabArguments);
        return thisTab;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the tab number in `tabNumber`.
        tabNumber = getArguments().getInt("Tab");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.  The fragment will take care of attaching the root automatically.
        View tabLayout = inflater.inflate(R.layout.bare_webview, container, false);

        // Get a handle for `tabWebView`.
        WebView tabWebView = (WebView) tabLayout;

        // Filter the colors if `darkTheme` is `true`.
        if (MainWebViewActivity.darkTheme) {
            // Initialize `darkPaint`.
            Paint darkPaint = new Paint();

            // Setup a float array that inverts and tempers the colors (no hard whites or blacks).
            float[] darkFilterFloatArray = {
                    -.8f, 0, 0, 0, 255,  // Red.
                    0, -.8f, 0, 0, 255,  // Green.
                    0, 0, -.8f, 0, 255,  // Blue.
                    0, 0, 0, .8f, 0      // Alpha.
            };

            // Set `darkPaint` to use `darkFilterFloatArray`.
            darkPaint.setColorFilter(new ColorMatrixColorFilter(darkFilterFloatArray));

            // Apply `darkPaint` to `tabWebView`.
            tabWebView.setLayerType(View.LAYER_TYPE_HARDWARE, darkPaint);
        } else {
            // Reset `tabWebView` to use the normal colors.
            tabWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        // Tab numbers start at 0.
        switch (tabNumber) {
            case 0:
                tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_overview.html");
                break;

            case 1:
                tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_javascript.html");
                break;

            case 2:
                tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_local_storage.html");
                break;

            case 3:
                tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_user_agent.html");
                break;

            case 4:
                tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_domain_settings.html");
                break;

            case 5:
                tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_tor.html");
                break;

            case 6:
                tabWebView.loadUrl("file:///android_asset/" + getString(R.string.android_asset_path) + "/guide_tracking_ids.html");
                break;
        }

        return tabLayout;
    }
}
