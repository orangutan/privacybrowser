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

package com.stoutner.privacybrowser.asynctasks;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.stoutner.privacybrowser.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

// This must run asynchronously because it involves a network request.  `String` declares the parameters.  `Void` does not declare progress units.  `SpannableStringBuilder[]` contains the results.
public class GetSource extends AsyncTask<String, Void, SpannableStringBuilder[]> {
    // Declare a weak reference to the calling activity.
    private WeakReference<Activity> activityWeakReference;

    // Store the user agent.
    private String userAgent;

    public GetSource(Activity activity, String userAgent) {
        // Populate the weak reference to the calling activity.
        activityWeakReference = new WeakReference<>(activity);

        // Store the user agent.
        this.userAgent = userAgent;
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

            // Define the variables necessary to build the request headers.
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
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  keep-alive");


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
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  1");


            // Set the `User-Agent` header property.
            httpUrlConnection.setRequestProperty("User-Agent", userAgent);

            // Add the `User-Agent` header to the string builder and format the text.
            requestHeadersBuilder.append(System.getProperty("line.separator"));
            if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                requestHeadersBuilder.append("User-Agent", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {  // Older versions not so much.
                oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.append("User-Agent");
                newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  ");
            requestHeadersBuilder.append(userAgent);


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
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  ");


            // Set the `Sec-Fetch-Site` header property.
            httpUrlConnection.setRequestProperty("Sec-Fetch-Site", "none");

            // Add the `Sec-Fetch-Site` header to the string builder and format the text.
            requestHeadersBuilder.append(System.getProperty("line.separator"));
            if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                requestHeadersBuilder.append("Sec-Fetch-Site", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {  // Older versions not so much.
                oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.append("Sec-Fetch-Site");
                newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  none");


            // Set the `Sec-Fetch-Mode` header property.
            httpUrlConnection.setRequestProperty("Sec-Fetch-Mode", "navigate");

            // Add the `Sec-Fetch-Mode` header to the string builder and format the text.
            requestHeadersBuilder.append(System.getProperty("line.separator"));
            if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                requestHeadersBuilder.append("Sec-Fetch-Mode", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {  // Older versions not so much.
                oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.append("Sec-Fetch-Mode");
                newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  navigate");


            // Set the `Sec-Fetch-User` header property.
            httpUrlConnection.setRequestProperty("Sec-Fetch-User", "?1");

            // Add the `Sec-Fetch-User` header to the string builder and format the text.
            requestHeadersBuilder.append(System.getProperty("line.separator"));
            if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                requestHeadersBuilder.append("Sec-Fetch-User", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {  // Older versions not so much.
                oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.append("Sec-Fetch-User");
                newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  ?1");


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
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                requestHeadersBuilder.append(":  1");
            }


            // Set the `Accept` header property.
            httpUrlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");

            // Add the `Accept` header to the string builder and format the text.
            requestHeadersBuilder.append(System.getProperty("line.separator"));
            if (Build.VERSION.SDK_INT >= 21) {  // Newer versions of Android are so smart.
                requestHeadersBuilder.append("Accept", new StyleSpan(Typeface.BOLD), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {  // Older versions not so much.
                oldRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.append("Accept");
                newRequestHeadersBuilderLength = requestHeadersBuilder.length();
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            requestHeadersBuilder.append(":  ");
            requestHeadersBuilder.append("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");


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

                    // Get the locale from the list.
                    Locale locale = localeList.get(i);

                    // Add the locale to the string.  `locale` by default displays as `en_US`, but WebView uses the `en-US` format.
                    localesStringBuilder.append(locale.getLanguage());
                    localesStringBuilder.append("-");
                    localesStringBuilder.append(locale.getCountry());

                    // If not the first locale, append `;q=0.x`, which drops by .1 for each removal from the main locale until q=0.1.
                    if (q < 10) {
                        localesStringBuilder.append(";q=0.");
                        localesStringBuilder.append(q);
                    }

                    // Decrement `q` if it is greater than 1.
                    if (q > 1) {
                        q--;
                    }

                    // Add a second entry for the language only portion of the locale.
                    localesStringBuilder.append(",");
                    localesStringBuilder.append(locale.getLanguage());

                    // Append `1;q=0.x`, which drops by .1 for each removal form the main locale until q=0.1.
                    localesStringBuilder.append(";q=0.");
                    localesStringBuilder.append(q);

                    // Decrement `q` if it is greater than 1.
                    if (q > 1) {
                        q--;
                    }
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
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                    requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                requestHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldRequestHeadersBuilderLength, newRequestHeadersBuilderLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                        responseHeadersBuilder.setSpan(new StyleSpan(Typeface.BOLD), oldLength, newLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        // Get a handle for the activity.
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