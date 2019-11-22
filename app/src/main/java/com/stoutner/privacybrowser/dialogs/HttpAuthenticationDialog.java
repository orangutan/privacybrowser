/*
 * Copyright © 2017-2019 Soren Stoutner <soren@stoutner.com>.
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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.HttpAuthHandler;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.fragments.WebViewTabFragment;
import com.stoutner.privacybrowser.views.NestedScrollWebView;

public class HttpAuthenticationDialog extends DialogFragment{
    // Define the class variables.
    private EditText usernameEditText;
    private EditText passwordEditText;

    public static HttpAuthenticationDialog displayDialog(String host, String realm, long webViewFragmentId) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the variables in the bundle.
        argumentsBundle.putString("host", host);
        argumentsBundle.putString("realm", realm);
        argumentsBundle.putLong("webview_fragment_id", webViewFragmentId);

        // Create a new instance of the HTTP authentication dialog.
        HttpAuthenticationDialog thisHttpAuthenticationDialog = new HttpAuthenticationDialog();

        // Add the arguments bundle to the new dialog.
        thisHttpAuthenticationDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return thisHttpAuthenticationDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning below that arguments might be null.
        assert arguments != null;

        // Get the variables from the bundle.
        String httpAuthHost = arguments.getString("host");
        String httpAuthRealm = arguments.getString("realm");
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

        // Get a handle for the HTTP authentication handler.
        HttpAuthHandler httpAuthHandler = nestedScrollWebView.getHttpAuthHandler();

        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Set the style according to the theme.
        if (darkTheme) {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.lock_dark);
        } else {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.lock_light);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.http_authentication);

        // Set the layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.http_authentication_dialog, null));

        // Setup the close button.
        dialogBuilder.setNegativeButton(R.string.close, (DialogInterface dialog, int which) -> {
            // Cancel the HTTP authentication request.
            httpAuthHandler.cancel();

            // Reset the HTTP authentication handler.
            nestedScrollWebView.resetHttpAuthHandler();
        });

        // Setup the proceed button.
        dialogBuilder.setPositiveButton(R.string.proceed, (DialogInterface dialog, int which) -> {
            // Send the login information
            login(httpAuthHandler);

            // Reset the HTTP authentication handler.
            nestedScrollWebView.resetHttpAuthHandler();
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Get the alert dialog window.
        Window dialogWindow = alertDialog.getWindow();

        // Remove the incorrect lint warning below that the dialog window might be null.
        assert dialogWindow != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Display the keyboard.
        dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // The alert dialog needs to be shown before the contents can be modified.
        alertDialog.show();

        // Get handles for the views.
        TextView realmTextView = alertDialog.findViewById(R.id.http_authentication_realm);
        TextView hostTextView = alertDialog.findViewById(R.id.http_authentication_host);
        usernameEditText = alertDialog.findViewById(R.id.http_authentication_username);
        passwordEditText = alertDialog.findViewById(R.id.http_authentication_password);

        // Set the realm text.
        realmTextView.setText(httpAuthRealm);

        // Set the realm text color according to the theme.  The deprecated `.getColor()` must be used until API >= 23.
        if (darkTheme) {
            realmTextView.setTextColor(getResources().getColor(R.color.gray_300));
        } else {
            realmTextView.setTextColor(getResources().getColor(R.color.black));
        }

        // Initialize the host label and the `SpannableStringBuilder`.
        String hostLabel = getString(R.string.host) + "  ";
        SpannableStringBuilder hostStringBuilder = new SpannableStringBuilder(hostLabel + httpAuthHost);

        // Create a blue `ForegroundColorSpan`.
        ForegroundColorSpan blueColorSpan;

        // Set `blueColorSpan` according to the theme.  The deprecated `getColor()` must be used until API >= 23.
        if (darkTheme) {
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
        } else {
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
        }

        // Setup the span to display the host name in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
        hostStringBuilder.setSpan(blueColorSpan, hostLabel.length(), hostStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // Set the host text.
        hostTextView.setText(hostStringBuilder);

        // Allow the `enter` key on the keyboard to trigger `onHttpAuthenticationProceed` from `usernameEditText`.
        usernameEditText.setOnKeyListener((View view, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the `enter` key, call `onHttpAuthenticationProceed()`.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Send the login information.
                login(httpAuthHandler);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // Allow the `enter` key on the keyboard to trigger `onHttpAuthenticationProceed()` from `passwordEditText`.
        passwordEditText.setOnKeyListener((View view, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the `enter` key, call `onHttpAuthenticationProceed()`.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Send the login information.
                login(httpAuthHandler);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // Return the alert dialog.
        return alertDialog;
    }

    private void login(HttpAuthHandler httpAuthHandler) {
        // Send the login information.
        httpAuthHandler.proceed(usernameEditText.getText().toString(), passwordEditText.getText().toString());
    }
}