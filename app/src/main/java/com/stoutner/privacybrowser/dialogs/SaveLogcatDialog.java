/*
 * Copyright © 2016-2020 Soren Stoutner <soren@stoutner.com>.
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment is required or an error is produced on API <=22.  It is also required for the browse button to work correctly.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.DownloadLocationHelper;

import java.io.File;

public class SaveLogcatDialog extends DialogFragment {
    // Define the save logcat listener.
    private SaveLogcatListener saveLogcatListener;

    // The public interface is used to send information back to the parent activity.
    public interface SaveLogcatListener {
        void onSaveLogcat(DialogFragment dialogFragment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for save logcat listener from the launching context.
        saveLogcatListener = (SaveLogcatListener) context;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using null as the parent view group when inflating the alert dialog.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the screenshot and theme preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Get a handle for the activity and the context.
        Activity activity = getActivity();
        Context context = getContext();

        // Remove the incorrect lint warnings.
        assert activity != null;
        assert context != null;

        // Set the style according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.save_logcat);

        // Set the icon according to the theme.
        if (darkTheme) {
            dialogBuilder.setIcon(R.drawable.save_dialog_dark);
        } else {
            dialogBuilder.setIcon(R.drawable.save_dialog_light);
        }

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(activity.getLayoutInflater().inflate(R.layout.save_logcat_dialog, null));

        // Set the cancel button listener.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The alert dialog will close automatically.
        });

        // Set the save button listener.
        dialogBuilder.setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
            // Return the dialog fragment to the parent activity.
            saveLogcatListener.onSaveLogcat(this);
        });

        // Create an alert dialog from the builder.
        AlertDialog alertDialog = dialogBuilder.create();

        // Remove the incorrect lint warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the layout items.
        EditText fileNameEditText = alertDialog.findViewById(R.id.file_name_edittext);
        Button browseButton = alertDialog.findViewById(R.id.browse_button);
        TextView fileExistsWarningTextView = alertDialog.findViewById(R.id.file_exists_warning_textview);
        TextView storagePermissionTextView = alertDialog.findViewById(R.id.storage_permission_textview);
        Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

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
                saveButton.setEnabled(!fileNameString.isEmpty());
            }
        });

        // Instantiate the download location helper.
        DownloadLocationHelper downloadLocationHelper = new DownloadLocationHelper();

        // Get the default file path.
        String defaultFilePath = downloadLocationHelper.getDownloadLocation(context) + "/" + getString(R.string.privacy_browser_logcat_txt);

        // Display the default file path.
        fileNameEditText.setText(defaultFilePath);

        // Hide the storage permission text view if the permission has already been granted.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            storagePermissionTextView.setVisibility(View.GONE);
        }

        // Handle clicks on the browse button.
        browseButton.setOnClickListener((View view) -> {
            // Create the file picker intent.
            Intent browseIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // Set the intent MIME type to include all files so that everything is visible.
            browseIntent.setType("*/*");

            // Set the initial file name.
            browseIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.privacy_browser_logcat_txt));

            // Set the initial directory if the minimum API >= 26.
            if (Build.VERSION.SDK_INT >= 26) {
                browseIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
            }

            // Request a file that can be opened.
            browseIntent.addCategory(Intent.CATEGORY_OPENABLE);

            // Launch the file picker.  There is only one `startActivityForResult()`, so the request code is simply set to 0, but it must be run under `activity` so the request code is correct.
            activity.startActivityForResult(browseIntent, 0);
        });

        // Return the alert dialog.
        return alertDialog;
    }
}