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
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.AboutViewSourceDialog;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class ViewSourceActivity extends AppCompatActivity {
    // `activity` is used in `onCreate()` and `goBack()`.
    private Activity activity;

    // The color spans are used in `onCreate()` and `highlightUrlText()`.
    private ForegroundColorSpan redColorSpan;
    private ForegroundColorSpan initialGrayColorSpan;
    private ForegroundColorSpan finalGrayColorSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the theme.
        if (MainWebViewActivity.darkTheme) {
            setTheme(R.style.PrivacyBrowserDark);
        } else {
            setTheme(R.style.PrivacyBrowserLight);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Store a handle for the current activity.
        activity = this;

        // Set the content view.
        setContentView(R.layout.view_source_coordinatorlayout);

        // `SupportActionBar` from `android.support.v7.app.ActionBar` must be used until the minimum API is >= 21.
        Toolbar viewSourceAppBar = findViewById(R.id.view_source_toolbar);
        setSupportActionBar(viewSourceAppBar);

        // Setup the app bar.
        final ActionBar appBar = getSupportActionBar();

        // Remove the incorrect warning in Android Studio that appBar might be null.
        assert appBar != null;

        // Add the custom layout to the app bar.
        appBar.setCustomView(R.layout.view_source_app_bar);
        appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Get a handle for the url text box.
        EditText urlEditText = findViewById(R.id.url_edittext);

        // Get the formatted URL string from the main activity.
        String formattedUrlString = MainWebViewActivity.formattedUrlString;

        // Populate the URL text box.
        urlEditText.setText(formattedUrlString);

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
                    new GetSource(this).execute(url);
                }

                // Consume the key press.
                return true;
            } else {
                // Do not consume the key press.
                return false;
            }
        });

        // Implement swipe to refresh.
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.view_source_swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Get the URL.
            String url = urlEditText.getText().toString();

            // Get new source data for the URL if it begins with `http`.
            if (url.startsWith("http")) {
                new GetSource(this).execute(url);
            } else {
                // Stop the refresh animation.
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Set the swipe to refresh color according to the theme.
        if (MainWebViewActivity.darkTheme) {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_600);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.gray_800);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_700);
        }

        // Get the source using an AsyncTask if the URL begins with `http`.
        if (formattedUrlString.startsWith("http")) {
            new GetSource(this).execute(formattedUrlString);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_source_options_menu, menu);

        // Display the menu.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get a handle for the about alert dialog.
        DialogFragment aboutDialogFragment = new AboutViewSourceDialog();

        // Show the about alert dialog.
        aboutDialogFragment.show(getFragmentManager(), getString(R.string.about));

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

    // `String` declares the parameters.  `Void` does not declare progress units.  `String[]` contains the results.
    private static class GetSource extends AsyncTask<String, Void, SpannableStringBuilder[]> {
        // Create a weak reference to the calling activity.
        private WeakReference<Activity> activityWeakReference;

        // Populate the weak reference to the calling activity.
        GetSource(Activity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        // `onPreExecute()` operates on the UI thread.
        @Override
        protected void onPreExecute() {
            // Get a handle for the activity.
            Activity viewSourceActivity = activityWeakReference.get();

            // Abort if the activity is gone.
            if ((viewSourceActivity == null) || viewSourceActivity.isFinishing()) {
                return;
            }

            // Get a handle for the progress bar.
            ProgressBar progressBar = viewSourceActivity.findViewById(R.id.progress_bar);

            // Make the progress bar visible.
            progressBar.setVisibility(View.VISIBLE);

            // Set the progress bar to be indeterminate.
            progressBar.setIndeterminate(true);
        }

        @Override
        protected SpannableStringBuilder[] doInBackground(String... formattedUrlString) {
            // Initialize the response body String.
            SpannableStringBuilder requestHeadersBuilder = new SpannableStringBuilder();
            SpannableStringBuilder responseMessageBuilder = new SpannableStringBuilder();
            SpannableStringBuilder responseHeadersBuilder = new SpannableStringBuilder();
            SpannableStringBuilder responseBodyBuilder = new SpannableStringBuilder();

            // Get a handle for the activity.
            Activity activity = activityWeakReference.get();

            // Abort if the activity is gone.
            if ((activity == null) || activity.isFinishing()) {
                return new SpannableStringBuilder[] {requestHeadersBuilder, responseMessageBuilder, responseHeadersBuilder, responseBodyBuilder};
            }

            // Because everything relating to requesting data from a webserver can throw errors, the entire section must catch `IOExceptions`.
            try {
                // Get the current URL from the main activity.
                URL url = new URL(formattedUrlString[0]);

                // Open a connection to the URL.  No data is actually sent at this point.
                HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();

                // Instantiate the variables necessary to build the request headers.
                requestHeadersBuilder = new SpannableStringBuilder();
                int oldRequestHeadersBuilderLength;
                int newRequestHeadersBuilderLength;


                // Set the `Host` header property.
                httpUrlConnection.setRequestProperty("Host", url.getHost());

                // Add the `Host` header to the string builder and format the text.
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("Host", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("Host");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  ");
                requestHeadersBuilder.append(url.getHost());


                // Set the `Connection` header property.
                httpUrlConnection.setRequestProperty("Connection", "keep-alive");

                // Add the `Connection` header to the string builder and format the text.
                requestHeadersBuilder.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("Connection", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("Connection");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  keep-alive");


                // Get the current `User-Agent` string.
                String userAgentString = MainWebViewActivity.appliedUserAgentString;

                // Set the `User-Agent` header property.
                httpUrlConnection.setRequestProperty("User-Agent", userAgentString);

                // Add the `User-Agent` header to the string builder and format the text.
                requestHeadersBuilder.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("User-Agent", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("User-Agent");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  ");
                requestHeadersBuilder.append(userAgentString);


                // Set the `Upgrade-Insecure-Requests` header property.
                httpUrlConnection.setRequestProperty("Upgrade-Insecure-Requests", "1");

                // Add the `Upgrade-Insecure-Requests` header to the string builder and format the text.
                requestHeadersBuilder.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("Upgrade-Insecure-Requests", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("Upgrade-Insecure_Requests");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  1");


                // Set the `x-requested-with` header property.
                httpUrlConnection.setRequestProperty("x-requested-with", "");

                // Add the `x-requested-with` header to the string builder and format the text.
                requestHeadersBuilder.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("x-requested-with", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("x-requested-with");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  ");


                // Get a handle for the shared preferences.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());

                // Only populate `Do Not Track` if it is enabled.
                if (sharedPreferences.getBoolean("do_not_track", false)) {
                    // Set the `dnt` header property.
                    httpUrlConnection.setRequestProperty("dnt", "1");

                    // Add the `dnt` header to the string builder and format the text.
                    requestHeadersBuilder.append(System.getProperty("line.separator"));
                    if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                        requestHeadersBuilder.append("dnt", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {  // Older versions not so much.
                        oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                        requestHeadersBuilder.append("dnt");
                        newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                        requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    requestHeadersBuilder.append(":  1");
                }


                // Set the `Accept` header property.
                httpUrlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");

                // Add the `Accept` header to the string builder and format the text.
                requestHeadersBuilder.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("Accept", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("Accept");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  ");
                requestHeadersBuilder.append("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");


                // Instantiate a locale string.
                String localeString;

                // Populate the locale string.
                if (Build.VERSION.SDK_INT >= 24) {  // SDK >= 24 has a list of locales.
                    // Get the list of locales.
                    LocaleList localeList = activity.getResources().getConfiguration().getLocales();

                    // Initialize a string builder to extract the locales from the list.
                    StringBuilder localesStringBuilder = new StringBuilder();

                    // Initialize a `q` value, which is used by `WebView` to indicate the order of importance of the languages.
                    int q = 10;

                    // Populate the string builder with the contents of the locales list.
                    for (int i = 0; i < localeList.size(); i++) {
                        // Append a comma if there is already an item in the string builder.
                        if (i > 0) {
                            localesStringBuilder.append(",");
                        }

                        // Get the indicated locale from the list.
                        localesStringBuilder.append(localeList.get(i));

                        // If not the first locale, append `;q=0.i`, which drops by .1 for each removal from the main locale.
                        if (q < 10) {
                            localesStringBuilder.append(";q=0.");
                            localesStringBuilder.append(q);
                        }

                        // Decrement `q`.
                        q--;
                    }

                    // Store the populated string builder in the locale string.
                    localeString = localesStringBuilder.toString();
                } else {  // SDK < 24 only has a primary locale.
                    // Store the locale in the locale string.
                    localeString = Locale.getDefault().toString();
                }

                // Set the `Accept-Language` header property.
                httpUrlConnection.setRequestProperty("Accept-Language", localeString);

                // Add the `Accept-Language` header to the string builder and format the text.
                requestHeadersBuilder.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("Accept-Language", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("Accept-Language");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  ");
                requestHeadersBuilder.append(localeString);


                // Get the cookies for the current domain.
                String cookiesString = CookieManager.getInstance().getCookie(url.toString());

                // Only process the cookies if they are not null.
                if (cookiesString != null) {
                    // Set the `Cookie` header property.
                    httpUrlConnection.setRequestProperty("Cookie", cookiesString);

                    // Add the `Cookie` header to the string builder and format the text.
                    requestHeadersBuilder.append(System.getProperty("line.separator"));
                    if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                        requestHeadersBuilder.append("Cookie", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {  // Older versions not so much.
                        oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                        requestHeadersBuilder.append("Cookie");
                        newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                        requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    requestHeadersBuilder.append(":  ");
                    requestHeadersBuilder.append(cookiesString);
                }


                // `HttpUrlConnection` sets `Accept-Encoding` to be `gzip` by default.  If the property is manually set, than `HttpUrlConnection` does not process the decoding.
                // Add the `Accept-Encoding` header to the string builder and format the text.
                requestHeadersBuilder.append(System.getProperty("line.separator"));
                if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                    requestHeadersBuilder.append("Accept-Encoding", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {  // Older versions not so much.
                    oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.append("Accept-Encoding");
                    newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength + 1, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  gzip");


                // The actual network request is in a `try` bracket so that `disconnect()` is run in the `finally` section even if an error is encountered in the main block.
                try {
                    // Initialize the string builders.
                    responseMessageBuilder = new SpannableStringBuilder();
                    responseHeadersBuilder = new SpannableStringBuilder();

                    // Get the response code, which causes the connection to the server to be made.
                    int responseCode = httpUrlConnection.getResponseCode();

                    // Populate the response message string builder.
                    if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                        responseMessageBuilder.append(String.valueOf(responseCode), new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {  // Older versions not so much.
                        responseMessageBuilder.append(String.valueOf(responseCode));
                        int newLength = responseMessageBuilder.length();
                        responseMessageBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, newLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    responseMessageBuilder.append(":  ");
                    responseMessageBuilder.append(httpUrlConnection.getResponseMessage());

                    // Initialize the iteration variable.
                    int i = 0;

                    // Iterate through the received header fields.
                    while (httpUrlConnection.getHeaderField(i) != null) {
                        // Add a new line if there is already information in the string builder.
                        if (i > 0) {
                            responseHeadersBuilder.append(System.getProperty("line.separator"));
                        }

                        // Add the header to the string builder and format the text.
                        if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                            responseHeadersBuilder.append(httpUrlConnection.getHeaderFieldKey(i), new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {  // Older versions not so much.
                            int oldLength = responseHeadersBuilder.length();
                            responseHeadersBuilder.append(httpUrlConnection.getHeaderFieldKey(i));
                            int newLength = responseHeadersBuilder.length();
                            responseHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldLength + 1, newLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        responseHeadersBuilder.append(":  ");
                        responseHeadersBuilder.append(httpUrlConnection.getHeaderField(i));

                        // Increment the iteration variable.
                        i++;
                    }

                    // Instantiate an input stream for the response body.
                    InputStream inputStream;

                    // Get the correct input stream based on the response code.
                    if (responseCode == 404) {  // Get the error stream.
                        inputStream = new BufferedInputStream(httpUrlConnection.getErrorStream());
                    } else {  // Get the response body stream.
                        inputStream = new BufferedInputStream(httpUrlConnection.getInputStream());
                    }

                    // Initialize the byte array output stream and the conversion buffer byte array.
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] conversionBufferByteArray = new byte[1024];

                    // Instantiate the variable to track the buffer length.
                    int bufferLength;

                    try {
                        // Attempt to read data from the input stream and store it in the conversion buffer byte array.  Also store the amount of data transferred in the buffer length variable.
                        while ((bufferLength = inputStream.read(conversionBufferByteArray)) > 0) {  // Proceed while the amount of data stored in the buffer is > 0.
                            // Write the contents of the conversion buffer to the byte array output stream.
                            byteArrayOutputStream.write(conversionBufferByteArray, 0, bufferLength);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Close the input stream.
                    inputStream.close();

                    // Populate the response body string with the contents of the byte array output stream.
                    responseBodyBuilder.append(byteArrayOutputStream.toString());
                } finally {
                    // Disconnect `httpUrlConnection`.
                    httpUrlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Return the response body string as the result.
            return new SpannableStringBuilder[] {requestHeadersBuilder, responseMessageBuilder, responseHeadersBuilder, responseBodyBuilder};
        }

        // `onPostExecute()` operates on the UI thread.
        @Override
        protected void onPostExecute(SpannableStringBuilder[] viewSourceStringArray){
            // Get a handle the activity.
            Activity activity = activityWeakReference.get();

            // Abort if the activity is gone.
            if ((activity == null) || activity.isFinishing()) {
                return;
            }

            // Get handles for the text views.
            TextView requestHeadersTextView = activity.findViewById(R.id.request_headers);
            TextView responseMessageTextView = activity.findViewById(R.id.response_message);
            TextView responseHeadersTextView = activity.findViewById(R.id.response_headers);
            TextView responseBodyTextView = activity.findViewById(R.id.response_body);
            ProgressBar progressBar = activity.findViewById(R.id.progress_bar);
            SwipeRefreshLayout swipeRefreshLayout = activity.findViewById(R.id.view_source_swiperefreshlayout);

            // Populate the text views.  This can take a long time, and freeze the user interface, if the response body is particularly large.
            requestHeadersTextView.setText(viewSourceStringArray[0]);
            responseMessageTextView.setText(viewSourceStringArray[1]);
            responseHeadersTextView.setText(viewSourceStringArray[2]);
            responseBodyTextView.setText(viewSourceStringArray[3]);

            // Hide the progress bar.
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.GONE);

            //Stop the swipe to refresh indicator if it is running
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}