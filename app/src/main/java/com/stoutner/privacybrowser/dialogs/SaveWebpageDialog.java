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

public class SaveWebpageDialog extends DialogFragment {
    // Define the save type constants.
    public static final int ARCHIVE = 0;
    public static final int IMAGE = 1;

    // Define the save webpage listener.
    private SaveWebpageListener saveWebpageListener;

    // The public interface is used to send information back to the parent activity.
    public interface SaveWebpageListener {
        void onSaveWebpage(int saveType, DialogFragment dialogFragment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the save webpage listener from the launching context.
        saveWebpageListener = (SaveWebpageListener) context;
    }

    public static SaveWebpageDialog saveWebpage(int saveType) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the save type in the bundle.
        argumentsBundle.putInt("save_type", saveType);

        // Create a new instance of the save webpage dialog.
        SaveWebpageDialog saveWebpageDialog = new SaveWebpageDialog();

        // Add the arguments bundle to the new dialog.
        saveWebpageDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return saveWebpageDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using null as the parent view group when inflating the alert dialog.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning that the arguments might be null.
        assert arguments != null;

        // Get the save type.
        int saveType = arguments.getInt("save_type");

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
                case ARCHIVE:
                    dialogBuilder.setIcon(R.drawable.dom_storage_cleared_dark);
                    break;

                case IMAGE:
                    dialogBuilder.setIcon(R.drawable.images_enabled_dark);
                    break;
            }
        } else {
            // Set the style.
            dialogBuilder = new AlertDialog.Builder(activity, R.style.PrivacyBrowserAlertDialogLight);

            // Set the icon according to the save type.
            switch (saveType) {
                case ARCHIVE:
                    dialogBuilder.setIcon(R.drawable.dom_storage_cleared_light);
                    break;

                case IMAGE:
                    dialogBuilder.setIcon(R.drawable.images_enabled_light);
                    break;
            }
        }

        // Set the title according to the type.
        switch (saveType) {
            case ARCHIVE:
                dialogBuilder.setTitle(R.string.save_archive);
                break;

            case IMAGE:
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
        TextView storagePermissionTextView = alertDialog.findViewById(R.id.storage_permission_textview);
        Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Create a default file name string.
        String defaultFileName = "";

        // Set the default file name according to the type.
        switch (saveType) {
            case ARCHIVE:
                defaultFileName = getString(R.string.webpage_mht);
                break;

            case IMAGE:
                defaultFileName = getString(R.string.webpage_png);
                break;
        }

        // Create a string for the default file path.
        String defaultFilePath;

        // Set the default file path according to the storage permission state.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // The storage permission has been granted.
            // Set the default file path to use the external public directory.
            defaultFilePath = Environment.getExternalStorageDirectory() + "/" + defaultFileName;
        } else {  // The storage permission has not been granted.
            // Set the default file path to use the external private directory.
            defaultFilePath = context.getExternalFilesDir(null) + "/" + defaultFileName;
        }

        // Display the default file path.
        fileNameEditText.setText(defaultFilePath);

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
                // // Enable the save button if a file name exists.
                saveButton.setEnabled(!fileNameEditText.getText().toString().isEmpty());
            }
        });

        // Handle clicks on the browse button.
        browseButton.setOnClickListener((View view) -> {
            // Create the file picker intent.
            Intent browseIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // Set the intent MIME type to include all files so that everything is visible.
            browseIntent.setType("*/*");

            // Set the initial file name according to the type.
            switch (saveType) {
                case ARCHIVE:
                    browseIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.webpage_mht));
                    break;

                case IMAGE:
                    browseIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.webpage_png));
                    break;
            }

            // Set the initial directory if the minimum API >= 26.
            if (Build.VERSION.SDK_INT >= 26) {
                browseIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
            }

            // Request a file that can be opened.
            browseIntent.addCategory(Intent.CATEGORY_OPENABLE);

            // Start the file picker.  This must be started under `activity` so that the request code is returned correctly.
            activity.startActivityForResult(browseIntent, MainWebViewActivity.BROWSE_SAVE_WEBPAGE_REQUEST_CODE);
        });

        // Hide the storage permission text view on API < 23 as permissions on older devices are automatically granted.
        if (Build.VERSION.SDK_INT < 23) {
            storagePermissionTextView.setVisibility(View.GONE);
        }

        // Return the alert dialog.
        return alertDialog;
    }
}