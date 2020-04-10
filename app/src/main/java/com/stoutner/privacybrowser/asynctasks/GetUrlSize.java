/*
 * Copyright © 2020 Soren Stoutner <soren@stoutner.com>.
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

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.ProxyHelper;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.text.NumberFormat;

public class GetUrlSize extends AsyncTask<String, Void, String> {
    // Define weak references for the calling context and alert dialog.
    private WeakReference<Context> contextWeakReference;
    private WeakReference<AlertDialog> alertDialogWeakReference;

    // Define the class variables.
    private String userAgent;
    private boolean cookiesEnabled;

    // The public constructor.
    public GetUrlSize(Context context, AlertDialog alertDialog, String userAgent, boolean cookiesEnabled) {
        // Populate the week references for the context and alert dialog.
        contextWeakReference = new WeakReference<>(context);
        alertDialogWeakReference = new WeakReference<>(alertDialog);

        // Store the class variables.
        this.userAgent = userAgent;
        this.cookiesEnabled = cookiesEnabled;
    }

    @Override
    protected String doInBackground(String... urlToSave) {
        // Get a handle for the context and the fragment.
        Context context = contextWeakReference.get();
        AlertDialog alertDialog = alertDialogWeakReference.get();

        // Abort if the fragment is gone.
        if (alertDialog == null) {
            return null;
        }

        // Initialize the formatted file size string.
        String formattedFileSize = context.getString(R.string.unknown_size);

        // Because everything relating to requesting data from a webserver can throw errors, the entire section must catch exceptions.
        try {
            // Get the URL from the calling fragment.
            URL url = new URL(urlToSave[0]);

            // Instantiate the proxy helper.
            ProxyHelper proxyHelper = new ProxyHelper();

            // Get the current proxy.
            Proxy proxy = proxyHelper.getCurrentProxy(context);

            // Open a connection to the URL.  No data is actually sent at this point.
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection(proxy);

            // Add the user agent to the header property.
            httpUrlConnection.setRequestProperty("User-Agent", userAgent);

            // Add the cookies if they are enabled.
            if (cookiesEnabled) {
                // Get the cookies for the current domain.
                String cookiesString = CookieManager.getInstance().getCookie(url.toString());

                // Only add the cookies if they are not null.
                if (cookiesString != null) {
                    // Add the cookies to the header property.
                    httpUrlConnection.setRequestProperty("Cookie", cookiesString);
                }
            }

            // The actual network request is in a `try` bracket so that `disconnect()` is run in the `finally` section even if an error is encountered in the main block.
            try {
                // Exit if the task has been cancelled.
                if (isCancelled()) {
                    // Disconnect the HTTP URL connection.
                    httpUrlConnection.disconnect();

                    // Return the formatted file size string.
                    return formattedFileSize;
                }

                // Get the status code.  This initiates a network connection.
                int responseCode = httpUrlConnection.getResponseCode();

                // Exit if the task has been cancelled.
                if (isCancelled()) {
                    // Disconnect the HTTP URL connection.
                    httpUrlConnection.disconnect();

                    // Return the formatted file size string.
                    return formattedFileSize;
                }

                // Check the response code.
                if (responseCode >= 400) {  // The response code is an error message.
                    // Set the formatted file size to indicate a bad URL.
                    formattedFileSize = context.getString(R.string.invalid_url);
                } else {  // The response code is not an error message.
                    // Get the content length header.
                    String contentLengthString = httpUrlConnection.getHeaderField("Content-Length");

                    // Only process the content length string if it isn't null.
                    if (contentLengthString != null) {
                        // Convert the content length string to a long.
                        long fileSize = Long.parseLong(contentLengthString);

                        // Format the file size.
                        formattedFileSize = NumberFormat.getInstance().format(fileSize) + " " + context.getString(R.string.bytes);
                    }
                }
            } finally {
                // Disconnect the HTTP URL connection.
                httpUrlConnection.disconnect();
            }
        } catch (Exception exception) {
            // Set the formatted file size to indicate a bad URL.
            formattedFileSize = context.getString(R.string.invalid_url);
        }

        // Return the formatted file size string.
        return formattedFileSize;
    }

    // `onPostExecute()` operates on the UI thread.
    @Override
    protected void onPostExecute(String fileSize) {
        // Get a handle for the alert dialog.
        AlertDialog alertDialog = alertDialogWeakReference.get();

        // Abort if the alert dialog is gone.
        if (alertDialog == null) {
            return;
        }

        // Get a handle for the file size text view.
        TextView fileSizeTextView = alertDialog.findViewById(R.id.file_size_textview);

        // Update the file size.
        fileSizeTextView.setText(fileSize);
    }
}