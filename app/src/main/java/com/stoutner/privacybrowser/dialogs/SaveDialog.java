/*
 * Copyright © 2019-2020 Soren Stoutner <soren@stoutner.com>.
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.asynctasks.GetUrlSize;

import java.io.File;

public class SaveDialog extends DialogFragment {
    // Define the save webpage listener.
    private SaveWebpageListener saveWebpageListener;

    // The public interface is used to send information back to the parent activity.
    public interface SaveWebpageListener {
        void onSaveWebpage(int saveType, DialogFragment dialogFragment);
    }

    // Define the get URL size AsyncTask.  This allows previous instances of the task to be cancelled if a new one is run.
    private AsyncTask getUrlSize;

    @Override
    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the save webpage listener from the launching context.
        saveWebpageListener = (SaveWebpageListener) context;
    }

    public static SaveDialog saveUrl(int saveType, String url, String userAgent, boolean cookiesEnabled) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the arguments in the bundle.
        argumentsBundle.putInt("save_type", saveType);
        argumentsBundle.putString("url", url);
        argumentsBundle.putString("user_agent", userAgent);
        argumentsBundle.putBoolean("cookies_enabled", cookiesEnabled);

        // Create a new instance of the save webpage dialog.
        SaveDialog saveWebpageDialog = new SaveDialog();

        // Add the arguments bundle to the new dialog.
        saveWebpageDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return saveWebpageDialog;
    }

    // `@SuppressLint("InflateParams")` removes the warning about using null as the parent view group when inflating the alert dialog.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning that the arguments might be null.
        assert arguments != null;

        // Get the arguments from the bundle.
        int saveType = arguments.getInt("save_type");
        String url = arguments.getString("url");
        String userAgent = arguments.getString("user_agent");
        boolean cookiesEnabled = arguments.getBoolean("cookies_enabled");

        // Get a handle for the activity and the context.
        Activity activity = getActivity();
        Context context = getContext();

        // Remove the incorrect lint warnings below that the activity and context might be null.
        assert activity != null;
        assert context != null;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and icon according to the theme.
        if (darkTheme) {
            // Set the style.
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogDark);

            // Set the icon according to the save type.
            switch (saveType) {
                case StoragePermissionDialog.SAVE_URL:
                    dialogBuilder.setIcon(R.drawable.copy_enabled_dark);
                    break;

                case StoragePermissionDialog.SAVE_AS_ARCHIVE:
                    dialogBuilder.setIcon(R.drawable.dom_storage_cleared_dark);
                    break;

                case StoragePermissionDialog.SAVE_AS_IMAGE:
                    dialogBuilder.setIcon(R.drawable.images_enabled_dark);
                    break;
            }
        } else {
            // Set the style.
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogLight);

            // Set the icon according to the save type.
            switch (saveType) {
                case StoragePermissionDialog.SAVE_URL:
                    dialogBuilder.setIcon(R.drawable.copy_enabled_light);
                    break;

                case StoragePermissionDialog.SAVE_AS_ARCHIVE:
                    dialogBuilder.setIcon(R.drawable.dom_storage_cleared_light);
                    break;

                case StoragePermissionDialog.SAVE_AS_IMAGE:
                    dialogBuilder.setIcon(R.drawable.images_enabled_light);
                    break;
            }
        }

        // Set the title according to the type.
        switch (saveType) {
            case StoragePermissionDialog.SAVE_URL:
                dialogBuilder.setTitle(R.string.save);
                break;

            case StoragePermissionDialog.SAVE_AS_ARCHIVE:
                dialogBuilder.setTitle(R.string.save_archive);
                break;

            case StoragePermissionDialog.SAVE_AS_IMAGE:
                dialogBuilder.setTitle(R.string.save_image);
                break;
        }

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(activity.getLayoutInflater().inflate(R.layout.save_dialog, null));

        // Set the cancel button listener.  Using `null` as the listener closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.cancel, null);

        // Set the save button listener.
        dialogBuilder.setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
            // Return the dialog fragment to the parent activity.
            saveWebpageListener.onSaveWebpage(saveType, this);
        });

        // Create an alert dialog from the builder.
        AlertDialog alertDialog = dialogBuilder.create();

        // Remove the incorrect lint warning below that the window might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the layout items.
        EditText urlEditText = alertDialog.findViewById(R.id.url_edittext);
        EditText fileNameEditText = alertDialog.findViewById(R.id.file_name_edittext);
        Button browseButton = alertDialog.findViewById(R.id.browse_button);
        TextView fileSizeTextView = alertDialog.findViewById(R.id.file_size_textview);
        TextView fileExistsWarningTextView = alertDialog.findViewById(R.id.file_exists_warning_textview);
        TextView storagePermissionTextView = alertDialog.findViewById(R.id.storage_permission_textview);
        Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Update the status of the save button whe the URL changes.
        urlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Cancel the get URL size AsyncTask if it is running.
                if ((getUrlSize != null)) {
                    getUrlSize.cancel(true);
                }

                // Get the current URL to save.
                String urlToSave = urlEditText.getText().toString();

                // Wipe the file size text view.
                fileSizeTextView.setText("");

                // Get the file size for the current URL.
                getUrlSize = new GetUrlSize(context, alertDialog, userAgent, cookiesEnabled).execute(urlToSave);

                // Enable the save button if the URL and file name are populated.
                saveButton.setEnabled(!urlToSave.isEmpty() && !fileNameEditText.getText().toString().isEmpty());
            }
        });

        // Update the status of the save button when the file name changes.
        fileNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Get the current file name.
                String fileNameString = fileNameEditText.getText().toString();

                // Convert the file name string to a file.
                File file = new File(fileNameString);

                // Check to see if the file exists.
                if (file.exists()) {
                    // Show the file exists warning.
                    fileExistsWarningTextView.setVisibility(View.VISIBLE);
                } else {
                    // Hide the file exists warning.
                    fileExistsWarningTextView.setVisibility(View.GONE);
                }

                // Enable the save button if the file name is populated.
                saveButton.setEnabled(!fileNameString.isEmpty() && !urlEditText.getText().toString().isEmpty());
            }
        });

        // Create a file name string.
        String fileName = "";

        // Set the file name according to the type.
        switch (saveType) {
            case StoragePermissionDialog.SAVE_URL:
                // Convert the URL to a URI.
                Uri uri = Uri.parse(url);

                // Get the last path segment.
                String lastPathSegment = uri.getLastPathSegment();

                // Use a default file name if the last path segment is null.
                if (lastPathSegment == null) {
                    lastPathSegment = getString(R.string.file);
                }

                // Use the last path segment as the file name.
                fileName = lastPathSegment;
                break;

            case StoragePermissionDialog.SAVE_AS_ARCHIVE:
                fileName = getString(R.string.webpage_mht);
                break;

            case StoragePermissionDialog.SAVE_AS_IMAGE:
                fileName = getString(R.string.webpage_png);
                break;
        }

        // Save the file name as the default file name.  This must be final to be used in the lambda below.
        final String defaultFileName = fileName;

        // Create a string for the default file path.
        String defaultFilePath;

        // Set the default file path according to the storage permission state.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // The storage permission has been granted.
            // Set the default file path to use the external public directory.
            defaultFilePath = Environment.getExternalStorageDirectory() + "/" + defaultFileName;

            // Hide the storage permission text view.
            storagePermissionTextView.setVisibility(View.GONE);
        } else {  // The storage permission has not been granted.
            // Set the default file path to use the external private directory.
            defaultFilePath = context.getExternalFilesDir(null) + "/" + defaultFileName;
        }

        // Populate the edit texts.
        urlEditText.setText(url);
        fileNameEditText.setText(defaultFilePath);

        // Move the cursor to the end of the default file path.
        fileNameEditText.setSelection(defaultFilePath.length());

        // Handle clicks on the browse button.
        browseButton.setOnClickListener((View view) -> {
            // Create the file picker intent.
            Intent browseIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // Set the intent MIME type to include all files so that everything is visible.
            browseIntent.setType("*/*");

            // Set the initial file name according to the type.
            browseIntent.putExtra(Intent.EXTRA_TITLE, defaultFileName);

            // Set the initial directory if the minimum API >= 26.
            if (Build.VERSION.SDK_INT >= 26) {
                browseIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
            }

            // Request a file that can be opened.
            browseIntent.addCategory(Intent.CATEGORY_OPENABLE);

            // Start the file picker.  This must be started under `activity` so that the request code is returned correctly.
            activity.startActivityForResult(browseIntent, MainWebViewActivity.BROWSE_SAVE_WEBPAGE_REQUEST_CODE);
        });

        // Return the alert dialog.
        return alertDialog;
    }
}