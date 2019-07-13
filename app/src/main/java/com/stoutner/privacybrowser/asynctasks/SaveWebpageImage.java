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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;

import com.google.android.material.snackbar.Snackbar;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.views.NestedScrollWebView;
import com.stoutner.privacybrowser.views.NoSwipeViewPager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class SaveWebpageImage extends AsyncTask<String, Void, String> {
    // Define the weak references.
    private WeakReference<Activity> activityWeakReference;
    private WeakReference<NestedScrollWebView> nestedScrollWebViewWeakReference;

    // Define a success string constant.
    private final String SUCCESS = "Success";

    // Define the saving image snackbar and the webpage bitmap.
    private Snackbar savingImageSnackbar;
    private Bitmap webpageBitmap;

    // The public constructor.
    public SaveWebpageImage(Activity activity, NestedScrollWebView nestedScrollWebView) {
        // Populate the weak references.
        activityWeakReference = new WeakReference<>(activity);
        nestedScrollWebViewWeakReference = new WeakReference<>(nestedScrollWebView);
    }

    // `onPreExecute()` operates on the UI thread.
    @Override
    protected void onPreExecute() {
        // Get a handle for the activity and the nested scroll WebView.
        Activity activity = activityWeakReference.get();
        NestedScrollWebView nestedScrollWebView = nestedScrollWebViewWeakReference.get();

        // Abort if the activity or the nested scroll WebView is gone.
        if ((activity == null) || activity.isFinishing() || nestedScrollWebView == null) {
            return;
        }

        // Create a saving image snackbar.
        savingImageSnackbar = Snackbar.make(nestedScrollWebView, R.string.saving_image, Snackbar.LENGTH_INDEFINITE);

        // Display the saving image snackbar.
        savingImageSnackbar.show();

        // Create a webpage bitmap.  Once the Minimum API >= 26 Bitmap.Config.RBGA_F16 can be used instead of ARGB_8888.  The nested scroll WebView commands must be run on the UI thread.
        webpageBitmap = Bitmap.createBitmap(nestedScrollWebView.getHorizontalScrollRange(), nestedScrollWebView.getVerticalScrollRange(), Bitmap.Config.ARGB_8888);

        // Create a canvas.
        Canvas webpageCanvas = new Canvas(webpageBitmap);

        // Draw the current webpage onto the bitmap.  The nested scroll WebView commands must be run on the UI thread.
        nestedScrollWebView.draw(webpageCanvas);
    }

    @Override
    protected String doInBackground(String... fileName) {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            return "";
        }

        // Create a webpage PNG byte array output stream.
        ByteArrayOutputStream webpageByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the bitmap to a PNG.  `0` is for lossless compression (the only option for a PNG).  This compression takes a long time.
        webpageBitmap.compress(Bitmap.CompressFormat.PNG, 0, webpageByteArrayOutputStream);

        // Get a file for the image.
        File imageFile = new File(fileName[0]);

        // Delete the current file if it exists.
        if (imageFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            imageFile.delete();
        }

        // Create a file creation disposition string.
        String fileCreationDisposition = SUCCESS;

        try {
            // Create an image file output stream.
            FileOutputStream imageFileOutputStream = new FileOutputStream(imageFile);

            // Write the webpage image to the image file.
            webpageByteArrayOutputStream.writeTo(imageFileOutputStream);
        } catch (Exception exception) {
            // Store the error in the file creation disposition string.
            fileCreationDisposition = exception.toString();
        }

        // Return the file creation disposition string.
        return fileCreationDisposition;
    }

    // `onPostExecute()` operates on the UI thread.
    @Override
    protected void onPostExecute(String fileCreationDisposition) {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            return;
        }

        // Get a handle for the no swipe view pager.
        NoSwipeViewPager noSwipeViewPager = activity.findViewById(R.id.webviewpager);

        // Dismiss the saving image snackbar.
        savingImageSnackbar.dismiss();

        // Display a file creation disposition snackbar.
        if (fileCreationDisposition.equals(SUCCESS)) {
            Snackbar.make(noSwipeViewPager, R.string.image_saved, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(noSwipeViewPager, activity.getString(R.string.error_saving_image) + "  " + fileCreationDisposition, Snackbar.LENGTH_INDEFINITE).show();
        }
    }
}