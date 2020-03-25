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

package com.stoutner.privacybrowser.helpers;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.stoutner.privacybrowser.R;

import java.io.File;

public class DownloadLocationHelper {
    public String getDownloadLocation(Context context) {
        // Get the download location entry values string array.
        String[] downloadLocationEntryValuesStringArray = context.getResources().getStringArray(R.array.download_location_entry_values);

        // Get the two standard download directories.
        File publicDownloadDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File publicAppFilesDirectory = context.getExternalFilesDir(null);

        // Remove the incorrect lint warning below that the public app files directory might be null.
        assert publicAppFilesDirectory != null;

        // Convert the download directories to strings.
        String publicDownloadDirectoryString = publicDownloadDirectory.toString();
        String publicAppFilesDirectoryString = publicAppFilesDirectory.toString();

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the download location strings from the preferences.
        String downloadLocationString = sharedPreferences.getString("download_location", context.getString(R.string.download_location_default_value));
        String downloadCustomLocationString = sharedPreferences.getString("download_custom_location", context.getString(R.string.download_custom_location_default_value));

        // Define a string for the default file path.
        String defaultFilePath;

        // Set the default file path according to the download location.
        if (downloadLocationString.equals(downloadLocationEntryValuesStringArray[0])) {  // the download location is set to auto.
            // Set the download location summary text according to the storage permission status.
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // The storage permission has been granted.
                // Use the public download directory.
                defaultFilePath = publicDownloadDirectoryString;
            } else {  // The storage permission has not been granted.
                // Use the public app files directory.
                defaultFilePath = publicAppFilesDirectoryString;
            }
        } else if (downloadLocationString.equals(downloadLocationEntryValuesStringArray[1])) {  // The download location is set to the app directory.
            // Use the public app files directory.
            defaultFilePath = publicAppFilesDirectoryString;
        } else if (downloadLocationString.equals(downloadLocationEntryValuesStringArray[2])) {  // The download location is set to the public directory.
            // Use the public download directory.
            defaultFilePath = publicDownloadDirectoryString;
        } else {  // The download location is set to custom.
            // Use the download custom location.
            defaultFilePath = downloadCustomLocationString;
        }

        // Return the default file path.
        return defaultFilePath;
    }
}