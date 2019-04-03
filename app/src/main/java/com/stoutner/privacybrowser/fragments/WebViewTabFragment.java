/*
 * Copyright © 2019 Soren Stoutner <soren@stoutner.com>.
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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.views.NestedScrollWebView;

import java.util.Calendar;

public class WebViewTabFragment extends Fragment {
    // Set a unique ID for this tab based on the time it was created.
    public long fragmentId = Calendar.getInstance().getTimeInMillis();

    // The public interface is used to send information back to the parent activity.
    public interface NewTabListener {
        void initializeWebView(NestedScrollWebView nestedScrollWebView, int pageNumber, ProgressBar progressBar);
    }

    // The new tab listener is used in `onAttach()` and `onCreateView()`.
    private NewTabListener newTabListener;

    @Override
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the new tab listener from the launching context.
        newTabListener = (NewTabListener) context;
    }

    public static WebViewTabFragment createPage(int pageNumber) {
        // Create a bundle.
        Bundle bundle = new Bundle();

        // Store the page number in the bundle.
        bundle.putInt("page_number", pageNumber);

        // Create a new instance of the WebView tab fragment.
        WebViewTabFragment webViewTabFragment = new WebViewTabFragment();

        // Add the bundle to the fragment.
        webViewTabFragment.setArguments(bundle);

        // Return the new fragment.
        return webViewTabFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning that the arguments might be null.
        assert arguments != null;

        // Get the variables from the arguments
        int pageNumber = arguments.getInt("page_number");

        // Inflate the tab's WebView.  Setting false at the end of inflater.inflate does not attach the inflated layout as a child of container.  The fragment will take care of attaching the root automatically.
        View newPageView = layoutInflater.inflate(R.layout.webview_framelayout, container, false);

        // Get handles for the views.
        NestedScrollWebView nestedScrollWebView = newPageView.findViewById(R.id.nestedscroll_webview);
        ProgressBar progressBar = newPageView.findViewById(R.id.progress_bar);

        // Store the WebView fragment ID in the nested scroll WebView.
        nestedScrollWebView.setWebViewFragmentId(fragmentId);

        // Request the main activity initialize the WebView.
        newTabListener.initializeWebView(nestedScrollWebView, pageNumber, progressBar);

        // Return the new page view.
        return newPageView;
    }
}