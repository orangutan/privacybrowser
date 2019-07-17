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

package com.stoutner.privacybrowser.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.adapters.HistoryArrayAdapter;
import com.stoutner.privacybrowser.definitions.History;
import com.stoutner.privacybrowser.fragments.WebViewTabFragment;
import com.stoutner.privacybrowser.views.NestedScrollWebView;

import java.util.ArrayList;

public class UrlHistoryDialog extends DialogFragment{
    // The public interface is used to send information back to the parent activity.
    public interface NavigateHistoryListener {
        void navigateHistory(String url, int steps);
    }

    // The navigate history listener is used in `onAttach()` and `onCreateDialog()`.
    private NavigateHistoryListener navigateHistoryListener;

    @Override
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the listener from the launching context.
        navigateHistoryListener = (NavigateHistoryListener) context;
    }

    public static UrlHistoryDialog loadBackForwardList(long webViewFragmentId) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the WebView fragment ID in the bundle.
        argumentsBundle.putLong("webview_fragment_id", webViewFragmentId);

        // Create a new instance of the URL history dialog.
        UrlHistoryDialog urlHistoryDialog = new UrlHistoryDialog();

        // Add the arguments bundle to this instance.
        urlHistoryDialog.setArguments(argumentsBundle);

        // Return the new URL history dialog.
        return urlHistoryDialog;
    }

    @Override
    @NonNull
    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint error that arguments might be null.
        assert arguments != null;

        // Get the WebView fragment ID from the arguments.
        long webViewFragmentId = arguments.getLong("webview_fragment_id");

        // Get the current position of this WebView fragment.
        int webViewPosition = MainWebViewActivity.webViewPagerAdapter.getPositionForId(webViewFragmentId);

        // Get the WebView tab fragment.
        WebViewTabFragment webViewTabFragment = MainWebViewActivity.webViewPagerAdapter.getPageFragment(webViewPosition);

        // Get the fragment view.
        View fragmentView = webViewTabFragment.getView();

        // Remove the incorrect lint warning below that the fragment view might be null.
        assert fragmentView != null;

        // Get a handle for the current WebView.
        NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

        // Get the web back forward list from the WebView.
        WebBackForwardList webBackForwardList = nestedScrollWebView.copyBackForwardList();

        // Store the current page index.
        int currentPageIndex = webBackForwardList.getCurrentIndex();

        // Remove the lint warning below that `getContext()` might be null.
        assert getContext() != null;

        // Get the default favorite icon drawable.  `ContextCompat` must be used until the minimum API >= 21.
        Drawable defaultFavoriteIconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.world);

        // Convert the default favorite icon drawable to a `BitmapDrawable`.
        BitmapDrawable defaultFavoriteIconBitmapDrawable = (BitmapDrawable) defaultFavoriteIconDrawable;

        // Remove the incorrect lint error that `getBitmap()` might be null.
        assert defaultFavoriteIconBitmapDrawable != null;

        // Extract a bitmap from the default favorite icon bitmap drawable.
        Bitmap defaultFavoriteIcon = defaultFavoriteIconBitmapDrawable.getBitmap();

        // Create a history array list.
        ArrayList<History> historyArrayList = new ArrayList<>();

        // Populate the history array list, descending from the end of the list so that the newest entries are at the top.  `-1` is needed because the history array list is zero-based.
        for (int i=webBackForwardList.getSize() -1; i >= 0; i--) {
            // Create a variable to store the favorite icon bitmap.
            Bitmap favoriteIconBitmap;

            // Determine the favorite icon bitmap
            if (webBackForwardList.getItemAtIndex(i).getFavicon() == null) {
                // If the web back forward list does not have a favorite icon, use Privacy Browser's default world icon.
                favoriteIconBitmap = defaultFavoriteIcon;
            } else {  // Use the icon from the web back forward list.
                favoriteIconBitmap = webBackForwardList.getItemAtIndex(i).getFavicon();
            }

            // Store the favorite icon and the URL in history entry.
            History historyEntry = new History(favoriteIconBitmap, webBackForwardList.getItemAtIndex(i).getUrl());

            // Add this history entry to the history array list.
            historyArrayList.add(historyEntry);
        }

        // Subtract the original current page ID from the array size because the order of the array is reversed so that the newest entries are at the top.  `-1` is needed because the array is zero-based.
        int currentPageId = webBackForwardList.getSize() - 1 - currentPageIndex;

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Set the style according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.history);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.url_history_dialog, null));

        // Setup the clear history button.
        dialogBuilder.setNegativeButton(R.string.clear_history, (DialogInterface dialog, int which) -> {
            // Clear the history.
            nestedScrollWebView.clearHistory();
        });

        // Set an `onClick()` listener on the positive button.
        dialogBuilder.setPositiveButton(R.string.close, (DialogInterface dialog, int which) -> {
            // Do nothing if `Close` is clicked.  The `Dialog` will automatically close.
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        //The alert dialog must be shown before the contents can be modified.
        alertDialog.show();

        // Instantiate a history array adapter.
        HistoryArrayAdapter historyArrayAdapter = new HistoryArrayAdapter(getContext(), historyArrayList, currentPageId);

        // Get a handle for the list view.
        ListView listView = alertDialog.findViewById(R.id.history_listview);

        // Set the list view adapter.
        listView.setAdapter(historyArrayAdapter);

        // Listen for clicks on entries in the list view.
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Convert the long ID to an int.
            int itemId = (int) id;

            // Only consume the click if it is not on the `currentPageId`.
            if (itemId != currentPageId) {
                // Get a handle for the URL text view.
                TextView urlTextView = view.findViewById(R.id.history_url_textview);

                // Get the URL.
                String url = urlTextView.getText().toString();

                // Invoke the navigate history listener in the calling activity.  These commands cannot be run here because they need access to `applyDomainSettings()`.
                navigateHistoryListener.navigateHistory(url, currentPageId - itemId);

                // Dismiss the alert dialog.
                alertDialog.dismiss();
            }
        });

        // Return the alert dialog.
        return alertDialog;
    }
}