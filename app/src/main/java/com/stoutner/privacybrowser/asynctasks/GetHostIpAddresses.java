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

package com.stoutner.privacybrowser.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;

// This must run asynchronously because it involves a network request.  `String` declares the parameters.  `Void` does not declare progress units.  `String` contains the results.
public class GetHostIpAddresses extends AsyncTask<String, Void, String> {
    // The weak references are used to determine if the activity have disappeared while the AsyncTask is running.
    private final WeakReference<Activity> activityWeakReference;
    private final int domainSettingsDatabaseId;

    public GetHostIpAddresses(Activity activity, int domainSettingsDatabaseId) {
        // Populate the weak activity reference.
        activityWeakReference = new WeakReference<>(activity);

        // Populate the domain settings database ID.
        this.domainSettingsDatabaseId = domainSettingsDatabaseId;
    }

    // `onPreExecute()` operates on the UI thread.
    @Override
    protected void onPreExecute() {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            return;
        }

        // Set the getting IP addresses tracker.
        MainWebViewActivity.gettingIpAddresses = true;
    }


    @Override
    protected String doInBackground(String... domainName) {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            // Return an empty spannable string builder.
            return "";
        }

        // Initialize an IP address string builder.
        StringBuilder ipAddresses = new StringBuilder();

        // Get an array with the IP addresses for the host.
        try {
            // Get an array with all the IP addresses for the domain.
            InetAddress[] inetAddressesArray = InetAddress.getAllByName(domainName[0]);

            // Add each IP address to the string builder.
            for (InetAddress inetAddress : inetAddressesArray) {
                if (ipAddresses.length() == 0) {  // This is the first IP address.
                    // Add the IP address to the string builder.
                    ipAddresses.append(inetAddress.getHostAddress());
                } else {  // This is not the first IP address.
                    // Add a line break to the string builder first.
                    ipAddresses.append("\n");

                    // Add the IP address to the string builder.
                    ipAddresses.append(inetAddress.getHostAddress());
                }
            }
        } catch (UnknownHostException exception) {
            // Do nothing.
        }

        // Return the string.
        return ipAddresses.toString();
    }

    // `onPostExecute()` operates on the UI thread.
    @Override
    protected void onPostExecute(String ipAddresses) {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            return;
        }

        // Store the IP addresses.
        MainWebViewActivity.currentHostIpAddresses = ipAddresses;

        if (!MainWebViewActivity.urlIsLoading) {
            MainWebViewActivity.checkPinnedMismatch(domainSettingsDatabaseId);
        }

        // Reset the getting IP addresses tracker.
        MainWebViewActivity.gettingIpAddresses = false;
    }
}