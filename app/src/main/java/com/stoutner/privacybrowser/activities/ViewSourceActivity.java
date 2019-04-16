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

package com.stoutner.privacybrowser.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;  // The AndroidX toolbar must be used until the minimum API is >= 21.
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.asynctasks.GetSource;
import com.stoutner.privacybrowser.dialogs.AboutViewSourceDialog;

public class ViewSourceActivity extends AppCompatActivity {
    // `activity` is used in `onCreate()` and `goBack()`.
    private Activity activity;

    // The color spans are used in `onCreate()` and `highlightUrlText()`.
    private ForegroundColorSpan redColorSpan;
    private ForegroundColorSpan initialGrayColorSpan;
    private ForegroundColorSpan finalGrayColorSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Get the screenshot and theme preferences.
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the theme.
        if (darkTheme) {
            setTheme(R.style.PrivacyBrowserDark);
        } else {
            setTheme(R.style.PrivacyBrowserLight);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Get the launching intent
        Intent intent = getIntent();

        // Get the information from the intent.
        String userAgent = intent.getStringExtra("user_agent");
        String currentUrl = intent.getStringExtra("current_url");

        // Store a handle for the current activity.
        activity = this;

        // Set the content view.
        setContentView(R.layout.view_source_coordinatorlayout);

        // The AndroidX toolbar must be used until the minimum API is >= 21.
        Toolbar toolbar = findViewById(R.id.view_source_toolbar);
        setSupportActionBar(toolbar);

        // Get a handle for the action bar.
        final ActionBar actionBar = getSupportActionBar();

        // Remove the incorrect lint warning that the action bar might be null.
        assert actionBar != null;

        // Add the custom layout to the action bar.
        actionBar.setCustomView(R.layout.view_source_app_bar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Get a handle for the url text box.
        EditText urlEditText = findViewById(R.id.url_edittext);

        // Populate the URL text box.
        urlEditText.setText(currentUrl);

        // Initialize the foreground color spans for highlighting the URLs.  We have to use the deprecated `getColor()` until API >= 23.
        redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));
        initialGrayColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.gray_500));
        finalGrayColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.gray_500));

        // Apply text highlighting to the URL.
        highlightUrlText();

        // Get a handle for the input method manager, which is used to hide the keyboard.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Remove the lint warning that the input method manager might be null.
        assert inputMethodManager != null;

        // Remove the formatting from the URL when the user is editing the text.
        urlEditText.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (hasFocus) {  // The user is editing `urlTextBox`.
                // Remove the highlighting.
                urlEditText.getText().removeSpan(redColorSpan);
                urlEditText.getText().removeSpan(initialGrayColorSpan);
                urlEditText.getText().removeSpan(finalGrayColorSpan);
            } else {  // The user has stopped editing `urlTextBox`.
                // Hide the soft keyboard.
                inputMethodManager.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);

                // Move to the beginning of the string.
                urlEditText.setSelection(0);

                // Reapply the highlighting.
                highlightUrlText();
            }
        });

        // Set the go button on the keyboard to request new source data.
        urlEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Request new source data if the enter key was pressed.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Hide the soft keyboard.
                inputMethodManager.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);

                // Remove the focus from the URL box.
                urlEditText.clearFocus();

                // Get the URL.
                String url = urlEditText.getText().toString();

                // Get new source data for the current URL if it beings with `http`.
                if (url.startsWith("http")) {
                    new GetSource(this, userAgent).execute(url);
                }

                // Consume the key press.
                return true;
            } else {
                // Do not consume the key press.
                return false;
            }
        });

        // Get a handle for the swipe refresh layout.
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.view_source_swiperefreshlayout);

        // Implement swipe to refresh.
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Get the URL.
            String url = urlEditText.getText().toString();

            // Get new source data for the URL if it begins with `http`.
            if (url.startsWith("http")) {
                new GetSource(this, userAgent).execute(url);
            } else {
                // Stop the refresh animation.
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Set the swipe to refresh color according to the theme.
        if (darkTheme) {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_600);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.gray_800);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_700);
        }

        // Get the source using an AsyncTask if the URL begins with `http`.
        if (currentUrl.startsWith("http")) {
            new GetSource(this, userAgent).execute(currentUrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.  This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_source_options_menu, menu);

        // Display the menu.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get a handle for the about alert dialog.
        DialogFragment aboutDialogFragment = new AboutViewSourceDialog();

        // Show the about alert dialog.
        aboutDialogFragment.show(getSupportFragmentManager(), getString(R.string.about));

        // Consume the event.
        return true;
    }

    public void goBack(View view) {
        // Go home.
        NavUtils.navigateUpFromSameTask(activity);
    }

    private void highlightUrlText() {
        // Get a handle for the URL EditText.
        EditText urlEditText = findViewById(R.id.url_edittext);

        // Get the URL string.
        String urlString = urlEditText.getText().toString();

        // Highlight the URL according to the protocol.
        if (urlString.startsWith("file://")) {  // This is a file URL.
            // De-emphasize only the protocol.
            urlEditText.getText().setSpan(initialGrayColorSpan, 0, 7, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else if (urlString.startsWith("content://")) {
            // De-emphasize only the protocol.
            urlEditText.getText().setSpan(initialGrayColorSpan, 0, 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        } else {  // This is a web URL.
            // Get the index of the `/` immediately after the domain name.
            int endOfDomainName = urlString.indexOf("/", (urlString.indexOf("//") + 2));

            // Create a base URL string.
            String baseUrl;

            // Get the base URL.
            if (endOfDomainName > 0) {  // There is at least one character after the base URL.
                // Get the base URL.
                baseUrl = urlString.substring(0, endOfDomainName);
            } else {  // There are no characters after the base URL.
                // Set the base URL to be the entire URL string.
                baseUrl = urlString;
            }

            // Get the index of the last `.` in the domain.
            int lastDotIndex = baseUrl.lastIndexOf(".");

            // Get the index of the penultimate `.` in the domain.
            int penultimateDotIndex = baseUrl.lastIndexOf(".", lastDotIndex - 1);

            // Markup the beginning of the URL.
            if (urlString.startsWith("http://")) {  // Highlight the protocol of connections that are not encrypted.
                urlEditText.getText().setSpan(redColorSpan, 0, 7, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                // De-emphasize subdomains.
                if (penultimateDotIndex > 0) {  // There is more than one subdomain in the domain name.
                    urlEditText.getText().setSpan(initialGrayColorSpan, 7, penultimateDotIndex + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            } else if (urlString.startsWith("https://")) {  // De-emphasize the protocol of connections that are encrypted.
                if (penultimateDotIndex > 0) {  // There is more than one subdomain in the domain name.
                    // De-emphasize the protocol and the additional subdomains.
                    urlEditText.getText().setSpan(initialGrayColorSpan, 0, penultimateDotIndex + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                } else {  // There is only one subdomain in the domain name.
                    // De-emphasize only the protocol.
                    urlEditText.getText().setSpan(initialGrayColorSpan, 0, 8, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }

            // De-emphasize the text after the domain name.
            if (endOfDomainName > 0) {
                urlEditText.getText().setSpan(finalGrayColorSpan, endOfDomainName, urlString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
    }
}