/*
 * Copyright © 2018 Soren Stoutner <soren@stoutner.com>.
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

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.ImportExportStoragePermissionDialog;
import com.stoutner.privacybrowser.helpers.ImportExportDatabaseHelper;

import java.io.File;

public class ImportExportActivity extends AppCompatActivity implements ImportExportStoragePermissionDialog.ImportExportStoragePermissionDialogListener {
    private final static int EXPORT_FILE_PICKER_REQUEST_CODE = 1;
    private final static int IMPORT_FILE_PICKER_REQUEST_CODE = 2;
    private final static int EXPORT_REQUEST_CODE = 3;
    private final static int IMPORT_REQUEST_CODE = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the activity theme.
        if (MainWebViewActivity.darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.import_export_coordinatorlayout);

        // Use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        Toolbar importExportAppBar = findViewById(R.id.import_export_toolbar);
        setSupportActionBar(importExportAppBar);

        // Display the home arrow on the support action bar.
        ActionBar appBar = getSupportActionBar();
        assert appBar != null;// This assert removes the incorrect warning in Android Studio on the following line that `appBar` might be null.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Get handles for the views that need to be modified.
        EditText exportFileEditText = findViewById(R.id.export_file_edittext);
        Button exportButton = findViewById(R.id.export_button);
        EditText importFileEditText = findViewById(R.id.import_file_edittext);
        Button importButton = findViewById(R.id.import_button);
        TextView storagePermissionTextView = findViewById(R.id.import_export_storage_permission_textview);

        // Initially disable the buttons.
        exportButton.setEnabled(false);
        importButton.setEnabled(false);

        // Enable the export button when the export file EditText isn't empty.
        exportFileEditText.addTextChangedListener(new TextWatcher() {
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
                exportButton.setEnabled(!exportFileEditText.getText().toString().isEmpty());
            }
        });

        // Enable the import button when the export file EditText isn't empty.
        importFileEditText.addTextChangedListener(new TextWatcher() {
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
                importButton.setEnabled(!importFileEditText.getText().toString().isEmpty());
            }
        });

        // Set the initial file paths.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // The storage permission has been granted.
            // Create a string for the external public path.
            String EXTERNAL_PUBLIC_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.privacy_browser_settings);

            // Set the default path.
            exportFileEditText.setText(EXTERNAL_PUBLIC_PATH);
            importFileEditText.setText(EXTERNAL_PUBLIC_PATH);
        } else {  // The storage permission has not been granted.
            // Create a string for the external private path.
            String EXTERNAL_PRIVATE_PATH = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + getString(R.string.privacy_browser_settings);

            // Set the default path.
            exportFileEditText.setText(EXTERNAL_PRIVATE_PATH);
            importFileEditText.setText(EXTERNAL_PRIVATE_PATH);
        }

        // Hide the storage permissions TextView on API < 23 as permissions on older devices are automatically granted.
        if (Build.VERSION.SDK_INT < 23) {
            storagePermissionTextView.setVisibility(View.GONE);
        }
    }

    public void exportBrowse(View view) {
        // Create the file picker intent.
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        // Set the intent MIME type to include all files.
        intent.setType("*/*");

        // Set the initial export file name.
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.privacy_browser_settings));

        // Set the initial directory if API >= 26.
        if (Build.VERSION.SDK_INT >= 26) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
        }

        // Specify that a file that can be opened is requested.
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Launch the file picker.
        startActivityForResult(intent, EXPORT_FILE_PICKER_REQUEST_CODE);
    }

    public void onClickExport(View view) {
        // Check to see if the storage permission has been granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // Storage permission granted.
            // Export the settings.
            exportSettings();
        } else {  // Storage permission not granted.
            // Get a handle for the export file EditText.
            EditText exportFileEditText = findViewById(R.id.export_file_edittext);

            // Get the export file string.
            String exportFileString = exportFileEditText.getText().toString();

            // Get the external private directory `File`.
            File externalPrivateDirectoryFile = getApplicationContext().getExternalFilesDir(null);

            // Remove the lint error below that the `File` might be null.
            assert externalPrivateDirectoryFile != null;

            // Get the external private directory string.
            String externalPrivateDirectory = externalPrivateDirectoryFile.toString();

            // Check to see if the export file path is in the external private directory.
            if (exportFileString.startsWith(externalPrivateDirectory)) {  // The export path is in the external private directory.
                // Export the settings.
                exportSettings();
            } else {  // The export path is in a public directory.
                // Check if the user has previously denied the storage permission.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                    // Instantiate the storage permission alert dialog and set the type to EXPORT_SETTINGS.
                    DialogFragment importExportStoragePermissionDialogFragment = ImportExportStoragePermissionDialog.type(ImportExportStoragePermissionDialog.EXPORT_SETTINGS);

                    // Show the storage permission alert dialog.  The permission will be requested when the dialog is closed.
                    importExportStoragePermissionDialogFragment.show(getFragmentManager(), getString(R.string.storage_permission));
                } else {  // Show the permission request directly.
                    // Request the storage permission.  The export will be run when it finishes.
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXPORT_REQUEST_CODE);
                }
            }
        }
    }

    public void importBrowse(View view) {
        // Create the file picker intent.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Set the intent MIME type to include all files.
        intent.setType("*/*");

        // Set the initial directory if API >= 26.
        if (Build.VERSION.SDK_INT >= 26) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
        }

        // Specify that a file that can be opened is requested.
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Launch the file picker.
        startActivityForResult(intent, IMPORT_FILE_PICKER_REQUEST_CODE);
    }

    public void onClickImport(View view) {
        // Check to see if the storage permission has been granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // Storage permission granted.
            // Import the settings.
            importSettings();
        } else {  // Storage permission not granted.
            // Get a handle for the import file EditText.
            EditText importFileEditText = findViewById(R.id.import_file_edittext);

            // Get the import file string.
            String importFileString = importFileEditText.getText().toString();

            // Get the external private directory `File`.
            File externalPrivateDirectoryFile = getApplicationContext().getExternalFilesDir(null);

            // Remove the lint error below that `File` might be null.
            assert externalPrivateDirectoryFile != null;

            // Get the external private directory string.
            String externalPrivateDirectory = externalPrivateDirectoryFile.toString();

            // Check to see if the import file path is in the external private directory.
            if (importFileString.startsWith(externalPrivateDirectory)) {  // The import path is in the external private directory.
                // Import the settings.
                importSettings();
            } else {  // The import path is in a public directory.
                // Check if the user has previously denied the storage permission.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                    // Instantiate the storage permission alert dialog and set the type to IMPORT_SETTINGS.
                    DialogFragment importExportStoragePermissionDialogFragment = ImportExportStoragePermissionDialog.type(ImportExportStoragePermissionDialog.IMPORT_SETTINGS);

                    // Show the storage permission alert dialog.  The permission will be requested when the dialog is closed.
                    importExportStoragePermissionDialogFragment.show(getFragmentManager(), getString(R.string.storage_permission));
                } else {  // Show the permission request directly.
                    // Request the storage permission.  The export will be run when it finishes.
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, IMPORT_REQUEST_CODE);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Don't do anything if the user pressed back from the file picker.
        if (resultCode == Activity.RESULT_OK) {
            // Run the commands for the specific request code.
            switch (requestCode) {
                case EXPORT_FILE_PICKER_REQUEST_CODE:
                    // Get a handle for the export file EditText.
                    EditText exportFileEditText = findViewById(R.id.export_file_edittext);

                    // Get the selected export file.
                    Uri exportUri = data.getData();

                    // Remove the lint warning that the export URI might be null.
                    assert exportUri != null;

                    // Get the raw export path.
                    String rawExportPath = exportUri.getPath();

                    // Remove the warning that the raw export path might be null.
                    assert rawExportPath != null;

                    // Check to see if the rawExportPath includes a valid storage location.
                    if (rawExportPath.contains(":")) {  // The path is valid.
                        // Split the path into the initial content uri and the path information.
                        String exportContentPath = rawExportPath.substring(0, rawExportPath.indexOf(":"));
                        String exportFilePath = rawExportPath.substring(rawExportPath.indexOf(":") + 1);

                        // Create the export path string.
                        String exportPath;

                        // Construct the export path.
                        switch (exportContentPath) {
                            // The documents home has a special content path.
                            case "/document/home":
                                exportPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + exportFilePath;
                                break;

                            // Everything else for the primary user should be in `/document/primary`.
                            case "/document/primary":
                                exportPath = Environment.getExternalStorageDirectory() + "/" + exportFilePath;
                                break;

                            // Just in case, catch everything else and place it in the external storage directory.
                            default:
                                exportPath = Environment.getExternalStorageDirectory() + "/" + exportFilePath;
                                break;
                        }

                        // Set the export file URI as the text for the export file EditText.
                        exportFileEditText.setText(exportPath);
                    } else {  // The path is invalid.
                        Snackbar.make(exportFileEditText, rawExportPath + " + " + getString(R.string.invalid_location), Snackbar.LENGTH_INDEFINITE).show();
                    }
                    break;

                case IMPORT_FILE_PICKER_REQUEST_CODE:
                    // Get a handle for the import file EditText.
                    EditText importFileEditText = findViewById(R.id.import_file_edittext);

                    // Get the selected import file.
                    Uri importUri = data.getData();

                    // Remove the lint warning that the import URI might be null.
                    assert importUri != null;

                    // Get the raw import path.
                    String rawImportPath = importUri.getPath();

                    // Remove the warning that the raw import path might be null.
                    assert rawImportPath != null;

                    // Check to see if the rawExportPath includes a valid storage location.
                    if (rawImportPath.contains(":")) {  // The path is valid.
                        // Split the path into the initial content uri and the path information.
                        String importContentPath = rawImportPath.substring(0, rawImportPath.indexOf(":"));
                        String importFilePath = rawImportPath.substring(rawImportPath.indexOf(":") + 1);

                        // Create the export path string.
                        String importPath;

                        // Construct the export path.
                        switch (importContentPath) {
                            // The documents folder has a special content path.
                            case "/document/home":
                                importPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + importFilePath;
                                break;

                            // Everything else for the primary user should be in `/document/primary`.
                            case "/document/primary":
                                importPath = Environment.getExternalStorageDirectory() + "/" + importFilePath;
                                break;

                            // Just in case, catch everything else and place it in the external storage directory.
                            default:
                                importPath = Environment.getExternalStorageDirectory() + "/" + importFilePath;
                                break;
                        }

                        // Set the export file URI as the text for the export file EditText.
                        importFileEditText.setText(importPath);
                    } else {  // The path is invalid.
                        Snackbar.make(importFileEditText, rawImportPath + " + " + getString(R.string.invalid_location), Snackbar.LENGTH_INDEFINITE).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void onCloseImportExportStoragePermissionDialog(int type) {
        // Request the storage permission based on the button that was pressed.
        switch (type) {
            case ImportExportStoragePermissionDialog.EXPORT_SETTINGS:
                // Request the storage permission.  The export will be run when it finishes.
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXPORT_REQUEST_CODE);
                break;

            case ImportExportStoragePermissionDialog.IMPORT_SETTINGS:
                // Request the storage permission.  The import will be run when it finishes.
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, IMPORT_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXPORT_REQUEST_CODE:
                // Check to see if the storage permission was granted.  If the dialog was canceled the grant results will be empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {  // The storage permission was granted.
                    // Export the settings.
                    exportSettings();
                } else {  // The storage permission was not granted.
                    // Get a handle for the export file EditText.
                    EditText exportFileEditText = findViewById(R.id.export_file_edittext);

                    // Display an error snackbar.
                    Snackbar.make(exportFileEditText, getString(R.string.cannot_export), Snackbar.LENGTH_LONG).show();
                }
                break;

            case IMPORT_REQUEST_CODE:
                // Check to see if the storage permission was granted.  If the dialog was canceled the grant results will be empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {  // The storage permission was granted.
                    // Import the settings.
                    importSettings();
                } else {  // The storage permission was not granted.
                    // Get a handle for the import file EditText.
                    EditText importFileEditText = findViewById(R.id.import_file_edittext);

                    // Display an error snackbar.
                    Snackbar.make(importFileEditText, getString(R.string.cannot_import), Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void exportSettings() {
        // Get a handle for the export file EditText.
        EditText exportFileEditText = findViewById(R.id.export_file_edittext);

        // Get the export file string.
        String exportFileString = exportFileEditText.getText().toString();

        // Set the export file.
        File exportFile = new File(exportFileString);

        // Instantiate the import export database helper.
        ImportExportDatabaseHelper importExportDatabaseHelper = new ImportExportDatabaseHelper();

        // Export the unencrypted file.
        String exportStatus = importExportDatabaseHelper.exportUnencrypted(exportFile, getApplicationContext());

        // Show a disposition snackbar.
        if (exportStatus.equals(ImportExportDatabaseHelper.EXPORT_SUCCESSFUL)) {
            Snackbar.make(exportFileEditText, getString(R.string.export_successful), Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(exportFileEditText, getString(R.string.export_failed) + "  " + exportStatus, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private void importSettings() {
        // Get a handle for the import file EditText.
        EditText importFileEditText = findViewById(R.id.import_file_edittext);

        // Get the import file string.
        String importFileString = importFileEditText.getText().toString();

        // Set the import file.
        File importFile = new File(importFileString);

        // Instantiate the import export database helper.
        ImportExportDatabaseHelper importExportDatabaseHelper = new ImportExportDatabaseHelper();

        // Import the unencrypted file.
        String importStatus = importExportDatabaseHelper.importUnencrypted(importFile, getApplicationContext());

        // Respond to the import disposition.
        if (importStatus.equals(ImportExportDatabaseHelper.IMPORT_SUCCESSFUL)) {  // The import was successful.
            // Create an intent to restart Privacy Browser.
            Intent restartIntent = getParentActivityIntent();

            // Assert that the intent is not null to remove the lint error below.
            assert restartIntent != null;

            // `Intent.FLAG_ACTIVITY_CLEAR_TASK` removes all activities from the stack.  It requires `Intent.FLAG_ACTIVITY_NEW_TASK`.
            restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Make it so.
            startActivity(restartIntent);
        } else {  // The import was not successful.
            // Display a snack bar with the import error.
            Snackbar.make(importFileEditText, getString(R.string.import_failed) + "  " + importStatus, Snackbar.LENGTH_INDEFINITE).show();
        }
    }
}