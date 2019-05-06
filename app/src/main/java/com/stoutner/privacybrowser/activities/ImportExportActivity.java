/*
 * Copyright © 2018-2019 Soren Stoutner <soren@stoutner.com>.
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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.StoragePermissionDialog;
import com.stoutner.privacybrowser.helpers.ImportExportDatabaseHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ImportExportActivity extends AppCompatActivity implements StoragePermissionDialog.StoragePermissionDialogListener {
    // Create the encryption constants.
    private final int NO_ENCRYPTION = 0;
    private final int PASSWORD_ENCRYPTION = 1;
    private final int OPENPGP_ENCRYPTION = 2;

    // Create the activity result constants.
    private final int BROWSE_RESULT_CODE = 0;
    private final int OPENPGP_EXPORT_RESULT_CODE = 1;

    // `openKeychainInstalled` is accessed from an inner class.
    private boolean openKeychainInstalled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the theme and screenshot preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the activity theme.
        if (darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.import_export_coordinatorlayout);

        // Use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        Toolbar toolbar = findViewById(R.id.import_export_toolbar);
        setSupportActionBar(toolbar);

        // Get a handle for the action bar.
        ActionBar actionBar = getSupportActionBar();

        // Remove the incorrect lint warning that the action bar might be null.
        assert actionBar != null;

        // Display the home arrow on the support action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Find out if we are running KitKat
        boolean runningKitKat = (Build.VERSION.SDK_INT == 19);

        // Find out if OpenKeychain is installed.
        try {
            openKeychainInstalled = !getPackageManager().getPackageInfo("org.sufficientlysecure.keychain", 0).versionName.isEmpty();
        } catch (PackageManager.NameNotFoundException exception) {
            openKeychainInstalled = false;
        }

        // Get handles for the views that need to be modified.
        Spinner encryptionSpinner = findViewById(R.id.encryption_spinner);
        TextInputLayout passwordEncryptionTextInputLayout = findViewById(R.id.password_encryption_textinputlayout);
        EditText encryptionPasswordEditText = findViewById(R.id.password_encryption_edittext);
        TextView kitKatPasswordEncryptionTextView = findViewById(R.id.kitkat_password_encryption_textview);
        TextView openKeychainRequiredTextView = findViewById(R.id.openkeychain_required_textview);
        CardView fileLocationCardView = findViewById(R.id.file_location_cardview);
        RadioButton importRadioButton = findViewById(R.id.import_radiobutton);
        RadioButton exportRadioButton = findViewById(R.id.export_radiobutton);
        LinearLayout fileNameLinearLayout = findViewById(R.id.file_name_linearlayout);
        EditText fileNameEditText = findViewById(R.id.file_name_edittext);
        TextView openKeychainImportInstructionsTextView = findViewById(R.id.openkeychain_import_instructions_textview);
        Button importExportButton = findViewById(R.id.import_export_button);
        TextView storagePermissionTextView = findViewById(R.id.import_export_storage_permission_textview);

        // Create an array adapter for the spinner.
        ArrayAdapter<CharSequence> encryptionArrayAdapter = ArrayAdapter.createFromResource(this, R.array.encryption_type, R.layout.spinner_item);

        // Set the drop down view resource on the spinner.
        encryptionArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_items);

        // Set the array adapter for the spinner.
        encryptionSpinner.setAdapter(encryptionArrayAdapter);

        // Initially hide the unneeded views.
        passwordEncryptionTextInputLayout.setVisibility(View.GONE);
        kitKatPasswordEncryptionTextView.setVisibility(View.GONE);
        openKeychainRequiredTextView.setVisibility(View.GONE);
        fileNameLinearLayout.setVisibility(View.GONE);
        openKeychainImportInstructionsTextView.setVisibility(View.GONE);
        importExportButton.setVisibility(View.GONE);

        // Create strings for the default file paths.
        String defaultFilePath;
        String defaultPasswordEncryptionFilePath;

        // Set the default file paths according to the storage permission status.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // The storage permission has been granted.
            // Set the default file paths to use the external public directory.
            defaultFilePath = Environment.getExternalStorageDirectory() + "/" + getString(R.string.settings_pbs);
            defaultPasswordEncryptionFilePath = defaultFilePath + ".aes";
        } else {  // The storage permission has not been granted.
            // Set the default file paths to use the external private directory.
            defaultFilePath = getApplicationContext().getExternalFilesDir(null) + "/" + getString(R.string.settings_pbs);
            defaultPasswordEncryptionFilePath = defaultFilePath + ".aes";
        }

        // Set the default file path.
        fileNameEditText.setText(defaultFilePath);

        // Display the encryption information when the spinner changes.
        encryptionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case NO_ENCRYPTION:
                        // Hide the unneeded layout items.
                        passwordEncryptionTextInputLayout.setVisibility(View.GONE);
                        kitKatPasswordEncryptionTextView.setVisibility(View.GONE);
                        openKeychainRequiredTextView.setVisibility(View.GONE);
                        openKeychainImportInstructionsTextView.setVisibility(View.GONE);

                        // Show the file location card.
                        fileLocationCardView.setVisibility(View.VISIBLE);

                        // Show the file name linear layout if either import or export is checked.
                        if (importRadioButton.isChecked() || exportRadioButton.isChecked()) {
                            fileNameLinearLayout.setVisibility(View.VISIBLE);
                        }

                        // Reset the text of the import button, which may have been changed to `Decrypt`.
                        if (importRadioButton.isChecked()) {
                            importExportButton.setText(R.string.import_button);
                        }

                        // Reset the default file path.
                        fileNameEditText.setText(defaultFilePath);

                        // Enable the import/export button if a file name exists.
                        importExportButton.setEnabled(!fileNameEditText.getText().toString().isEmpty());
                        break;

                    case PASSWORD_ENCRYPTION:
                        if (runningKitKat) {
                            // Show the KitKat password encryption message.
                            kitKatPasswordEncryptionTextView.setVisibility(View.VISIBLE);

                            // Hide the OpenPGP required text view and the file location card.
                            openKeychainRequiredTextView.setVisibility(View.GONE);
                            fileLocationCardView.setVisibility(View.GONE);
                        } else {
                            // Hide the OpenPGP layout items.
                            openKeychainRequiredTextView.setVisibility(View.GONE);
                            openKeychainImportInstructionsTextView.setVisibility(View.GONE);

                            // Show the password encryption layout items.
                            passwordEncryptionTextInputLayout.setVisibility(View.VISIBLE);

                            // Show the file location card.
                            fileLocationCardView.setVisibility(View.VISIBLE);

                            // Show the file name linear layout if either import or export is checked.
                            if (importRadioButton.isChecked() || exportRadioButton.isChecked()) {
                                fileNameLinearLayout.setVisibility(View.VISIBLE);
                            }

                            // Reset the text of the import button, which may have been changed to `Decrypt`.
                            if (importRadioButton.isChecked()) {
                                importExportButton.setText(R.string.import_button);
                            }

                            // Update the default file path.
                            fileNameEditText.setText(defaultPasswordEncryptionFilePath);

                            // Enable the import/export button if a password exists.
                            importExportButton.setEnabled(!encryptionPasswordEditText.getText().toString().isEmpty());
                        }
                        break;

                    case OPENPGP_ENCRYPTION:
                        // Hide the password encryption layout items.
                        passwordEncryptionTextInputLayout.setVisibility(View.GONE);
                        kitKatPasswordEncryptionTextView.setVisibility(View.GONE);

                        // Updated items based on the installation status of OpenKeychain.
                        if (openKeychainInstalled) {  // OpenKeychain is installed.
                            // Remove the default file path.
                            fileNameEditText.setText("");

                            // Show the file location card.
                            fileLocationCardView.setVisibility(View.VISIBLE);

                            if (importRadioButton.isChecked()) {
                                // Show the file name linear layout and the OpenKeychain import instructions.
                                fileNameLinearLayout.setVisibility(View.VISIBLE);
                                openKeychainImportInstructionsTextView.setVisibility(View.VISIBLE);

                                // Set the text of the import button to be `Decrypt`.
                                importExportButton.setText(R.string.decrypt);

                                // Disable the import/export button.  The user needs to select a file to import first.
                                importExportButton.setEnabled(false);
                            } else if (exportRadioButton.isChecked()) {
                                // Hide the file name linear layout and the OpenKeychain import instructions.
                                fileNameLinearLayout.setVisibility(View.GONE);
                                openKeychainImportInstructionsTextView.setVisibility(View.GONE);

                                // Enable the import/export button.
                                importExportButton.setEnabled(true);
                            }
                        } else {  // OpenKeychain is not installed.
                            // Show the OpenPGP required layout item.
                            openKeychainRequiredTextView.setVisibility(View.VISIBLE);

                            // Hide the file location card.
                            fileLocationCardView.setVisibility(View.GONE);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Update the status of the import/export button when the password changes.
        encryptionPasswordEditText.addTextChangedListener(new TextWatcher() {
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
                // Enable the import/export button if a file name and password exists.
                importExportButton.setEnabled(!fileNameEditText.getText().toString().isEmpty() && !encryptionPasswordEditText.getText().toString().isEmpty());
            }
        });

        // Update the status of the import/export button when the file name EditText changes.
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
                // Adjust the export button according to the encryption spinner position.
                switch (encryptionSpinner.getSelectedItemPosition()) {
                    case NO_ENCRYPTION:
                        // Enable the import/export button if a file name exists.
                        importExportButton.setEnabled(!fileNameEditText.getText().toString().isEmpty());
                        break;

                    case PASSWORD_ENCRYPTION:
                        // Enable the import/export button if a file name and password exists.
                        importExportButton.setEnabled(!fileNameEditText.getText().toString().isEmpty() && !encryptionPasswordEditText.getText().toString().isEmpty());
                        break;

                    case OPENPGP_ENCRYPTION:
                        // Enable the import/export button if OpenKeychain is installed and a file name exists.
                        importExportButton.setEnabled(openKeychainInstalled && !fileNameEditText.getText().toString().isEmpty());
                        break;
                }
            }
        });

        // Hide the storage permissions text view on API < 23 as permissions on older devices are automatically granted.
        if (Build.VERSION.SDK_INT < 23) {
            storagePermissionTextView.setVisibility(View.GONE);
        }
    }

    public void onClickRadioButton(View view) {
        // Get handles for the views.
        Spinner encryptionSpinner = findViewById(R.id.encryption_spinner);
        LinearLayout fileNameLinearLayout = findViewById(R.id.file_name_linearlayout);
        EditText fileNameEditText = findViewById(R.id.file_name_edittext);
        TextView openKeychainImportInstructionTextView = findViewById(R.id.openkeychain_import_instructions_textview);
        Button importExportButton = findViewById(R.id.import_export_button);

        // Check to see if import or export was selected.
        switch (view.getId()) {
            case R.id.import_radiobutton:
                // Check to see if OpenPGP encryption is selected.
                if (encryptionSpinner.getSelectedItemPosition() == OPENPGP_ENCRYPTION) {  // OpenPGP encryption selected.
                    // Show the OpenKeychain import instructions.
                    openKeychainImportInstructionTextView.setVisibility(View.VISIBLE);

                    // Set the text on the import/export button to be `Decrypt`.
                    importExportButton.setText(R.string.decrypt);

                    // Enable the decrypt button if there is a file name.
                    importExportButton.setEnabled(!fileNameEditText.getText().toString().isEmpty());
                } else {  // OpenPGP encryption not selected.
                    // Hide the OpenKeychain import instructions.
                    openKeychainImportInstructionTextView.setVisibility(View.GONE);

                    // Set the text on the import/export button to be `Import`.
                    importExportButton.setText(R.string.import_button);
                }

                // Display the file name views.
                fileNameLinearLayout.setVisibility(View.VISIBLE);
                importExportButton.setVisibility(View.VISIBLE);
                break;

            case R.id.export_radiobutton:
                // Hide the OpenKeychain import instructions.
                openKeychainImportInstructionTextView.setVisibility(View.GONE);

                // Set the text on the import/export button to be `Export`.
                importExportButton.setText(R.string.export);

                // Show the import/export button.
                importExportButton.setVisibility(View.VISIBLE);

                // Check to see if OpenPGP encryption is selected.
                if (encryptionSpinner.getSelectedItemPosition() == OPENPGP_ENCRYPTION) {  // OpenPGP encryption is selected.
                    // Hide the file name views.
                    fileNameLinearLayout.setVisibility(View.GONE);

                    // Enable the export button.
                    importExportButton.setEnabled(true);
                } else {  // OpenPGP encryption is not selected.
                    // Show the file name views.
                    fileNameLinearLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    public void browse(View view) {
        // Get a handle for the import radiobutton.
        RadioButton importRadioButton = findViewById(R.id.import_radiobutton);

        // Check to see if import or export is selected.
        if (importRadioButton.isChecked()) {  // Import is selected.
            // Create the file picker intent.
            Intent importBrowseIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

            // Set the intent MIME type to include all files so that everything is visible.
            importBrowseIntent.setType("*/*");

            // Set the initial directory if the minimum API >= 26.
            if (Build.VERSION.SDK_INT >= 26) {
                importBrowseIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
            }

            // Request a file that can be opened.
            importBrowseIntent.addCategory(Intent.CATEGORY_OPENABLE);

            // Launch the file picker.
            startActivityForResult(importBrowseIntent, BROWSE_RESULT_CODE);
        } else {  // Export is selected
            // Create the file picker intent.
            Intent exportBrowseIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

            // Set the intent MIME type to include all files so that everything is visible.
            exportBrowseIntent.setType("*/*");

            // Set the initial export file name.
            exportBrowseIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.settings_pbs));

            // Set the initial directory if the minimum API >= 26.
            if (Build.VERSION.SDK_INT >= 26) {
                exportBrowseIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.getExternalStorageDirectory());
            }

            // Request a file that can be opened.
            exportBrowseIntent.addCategory(Intent.CATEGORY_OPENABLE);

            // Launch the file picker.
            startActivityForResult(exportBrowseIntent, BROWSE_RESULT_CODE);
        }
    }

    public void importExport(View view) {
        // Get a handle for the views.
        Spinner encryptionSpinner = findViewById(R.id.encryption_spinner);
        RadioButton importRadioButton = findViewById(R.id.import_radiobutton);
        RadioButton exportRadioButton = findViewById(R.id.export_radiobutton);

        // Check to see if the storage permission is needed.
        if ((encryptionSpinner.getSelectedItemPosition() == OPENPGP_ENCRYPTION) && exportRadioButton.isChecked()) {  // Permission not needed to export via OpenKeychain.
            // Export the settings.
            exportSettings();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // The storage permission has been granted.
            // Check to see if import or export is selected.
            if (importRadioButton.isChecked()) {  // Import is selected.
                // Import the settings.
                importSettings();
            } else {  // Export is selected.
                // Export the settings.
                exportSettings();
            }
        } else {  // The storage permission has not been granted.
            // Get a handle for the file name EditText.
            EditText fileNameEditText = findViewById(R.id.file_name_edittext);

            // Get the file name string.
            String fileNameString = fileNameEditText.getText().toString();

            // Get the external private directory `File`.
            File externalPrivateDirectoryFile = getExternalFilesDir(null);

            // Remove the incorrect lint error below that the file might be null.
            assert externalPrivateDirectoryFile != null;

            // Get the external private directory string.
            String externalPrivateDirectory = externalPrivateDirectoryFile.toString();

            // Check to see if the file path is in the external private directory.
            if (fileNameString.startsWith(externalPrivateDirectory)) {  // The file path is in the external private directory.
                // Check to see if import or export is selected.
                if (importRadioButton.isChecked()) {  // Import is selected.
                    // Import the settings.
                    importSettings();
                } else {  // Export is selected.
                    // Export the settings.
                    exportSettings();
                }
            } else {  // The file path is in a public directory.
                // Check if the user has previously denied the storage permission.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                    // Instantiate the storage permission alert dialog.
                    DialogFragment storagePermissionDialogFragment = new StoragePermissionDialog();

                    // Show the storage permission alert dialog.  The permission will be requested when the dialog is closed.
                    storagePermissionDialogFragment.show(getSupportFragmentManager(), getString(R.string.storage_permission));
                } else {  // Show the permission request directly.
                    // Request the storage permission.  The export will be run when it finishes.
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        }
    }

    @Override
    public void onCloseStoragePermissionDialog() {
        // Request the write external storage permission.  The import/export will be run when it finishes.
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Get a handle for the import radiobutton.
        RadioButton importRadioButton = findViewById(R.id.import_radiobutton);

        // Check to see if the storage permission was granted.  If the dialog was canceled the grant results will be empty.
        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {  // The storage permission was granted.
            // Run the import or export methods according to which radio button is selected.
            if (importRadioButton.isChecked()) {  // Import is selected.
                // Import the settings.
                importSettings();
            } else {  // Export is selected.
                // Export the settings.
                exportSettings();
            }
        } else {  // The storage permission was not granted.
            // Display an error snackbar.
            Snackbar.make(importRadioButton, getString(R.string.cannot_use_location), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (BROWSE_RESULT_CODE):
                // Don't do anything if the user pressed back from the file picker.
                if (resultCode == Activity.RESULT_OK) {
                    // Get a handle for the file name edit text.
                    EditText fileNameEditText = findViewById(R.id.file_name_edittext);

                    // Get the file name URI.
                    Uri fileNameUri = data.getData();

                    // Remove the lint warning that the file name URI might be null.
                    assert fileNameUri != null;

                    // Get the raw file name path.
                    String rawFileNamePath = fileNameUri.getPath();

                    // Remove the incorrect lint warning that the file name path might be null.
                    assert rawFileNamePath != null;

                    // Check to see if the file name Path includes a valid storage location.
                    if (rawFileNamePath.contains(":")) {  // The path is valid.
                        // Split the path into the initial content uri and the final path information.
                        String fileNameContentPath = rawFileNamePath.substring(0, rawFileNamePath.indexOf(":"));
                        String fileNameFinalPath = rawFileNamePath.substring(rawFileNamePath.indexOf(":") + 1);

                        // Create the file name path string.
                        String fileNamePath;

                        // Check to see if the current file name final patch is a complete, valid path
                        if (fileNameFinalPath.startsWith("/storage/emulated/")) {  // The existing file name final path is a complete, valid path.
                            // Use the provided file name path as is.
                            fileNamePath = fileNameFinalPath;
                        } else {  // The existing file name final path is not a complete, valid path.
                            // Construct the file name path.
                            switch (fileNameContentPath) {
                                // The documents home has a special content path.
                                case "/document/home":
                                    fileNamePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + fileNameFinalPath;
                                    break;

                                // Everything else for the primary user should be in `/document/primary`.
                                case "/document/primary":
                                    fileNamePath = Environment.getExternalStorageDirectory() + "/" + fileNameFinalPath;
                                    break;

                                // Just in case, catch everything else and place it in the external storage directory.
                                default:
                                    fileNamePath = Environment.getExternalStorageDirectory() + "/" + fileNameFinalPath;
                                    break;
                            }
                        }

                        // Set the file name path as the text of the file name edit text.
                        fileNameEditText.setText(fileNamePath);
                    } else {  // The path is invalid.
                        Snackbar.make(fileNameEditText, rawFileNamePath + " " + getString(R.string.invalid_location), Snackbar.LENGTH_INDEFINITE).show();
                    }
                }
                break;

            case OPENPGP_EXPORT_RESULT_CODE:
                // Get the temporary unencrypted export file.
                File temporaryUnencryptedExportFile = new File(getApplicationContext().getCacheDir() + "/" + getString(R.string.settings_pbs));

                // Delete the temporary unencrypted export file if it exists.
                if (temporaryUnencryptedExportFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    temporaryUnencryptedExportFile.delete();
                }
                break;
        }
    }

    private void exportSettings() {
        // Get a handle for the views.
        Spinner encryptionSpinner = findViewById(R.id.encryption_spinner);
        EditText fileNameEditText = findViewById(R.id.file_name_edittext);

        // Instantiate the import export database helper.
        ImportExportDatabaseHelper importExportDatabaseHelper = new ImportExportDatabaseHelper();

        // Get the export file string.
        String exportFileString = fileNameEditText.getText().toString();

        // Get the export and temporary unencrypted export files.
        File exportFile = new File(exportFileString);
        File temporaryUnencryptedExportFile = new File(getApplicationContext().getCacheDir() + "/" + getString(R.string.settings_pbs));

        // Create an export status string.
        String exportStatus;

        // Export according to the encryption type.
        switch (encryptionSpinner.getSelectedItemPosition()) {
            case NO_ENCRYPTION:
                // Export the unencrypted file.
                exportStatus = importExportDatabaseHelper.exportUnencrypted(exportFile, this);

                // Show a disposition snackbar.
                if (exportStatus.equals(ImportExportDatabaseHelper.EXPORT_SUCCESSFUL)) {
                    Snackbar.make(fileNameEditText, getString(R.string.export_successful), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(fileNameEditText, getString(R.string.export_failed) + "  " + exportStatus, Snackbar.LENGTH_INDEFINITE).show();
                }
                break;

            case PASSWORD_ENCRYPTION:
                // Create an unencrypted export in a private directory.
                exportStatus = importExportDatabaseHelper.exportUnencrypted(temporaryUnencryptedExportFile, this);

                try {
                    // Create an unencrypted export file input stream.
                    FileInputStream unencryptedExportFileInputStream = new FileInputStream(temporaryUnencryptedExportFile);

                    // Delete the encrypted export file if it exists.
                    if (exportFile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        exportFile.delete();
                    }

                    // Create an encrypted export file output stream.
                    FileOutputStream encryptedExportFileOutputStream = new FileOutputStream(exportFile);

                    // Get a handle for the encryption password EditText.
                    EditText encryptionPasswordEditText = findViewById(R.id.password_encryption_edittext);

                    // Get the encryption password.
                    String encryptionPasswordString = encryptionPasswordEditText.getText().toString();

                    // Initialize a secure random number generator.
                    SecureRandom secureRandom = new SecureRandom();

                    // Get a 256 bit (32 byte) random salt.
                    byte[] saltByteArray = new byte[32];
                    secureRandom.nextBytes(saltByteArray);

                    // Convert the encryption password to a byte array.
                    byte[] encryptionPasswordByteArray = encryptionPasswordString.getBytes(StandardCharsets.UTF_8);

                    // Append the salt to the encryption password byte array.  This protects against rainbow table attacks.
                    byte[] encryptionPasswordWithSaltByteArray = new byte[encryptionPasswordByteArray.length + saltByteArray.length];
                    System.arraycopy(encryptionPasswordByteArray, 0, encryptionPasswordWithSaltByteArray, 0, encryptionPasswordByteArray.length);
                    System.arraycopy(saltByteArray, 0, encryptionPasswordWithSaltByteArray, encryptionPasswordByteArray.length, saltByteArray.length);

                    // Get a SHA-512 message digest.
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");

                    // Hash the salted encryption password.  Otherwise, any characters after the 32nd character in the password are ignored.
                    byte[] hashedEncryptionPasswordWithSaltByteArray = messageDigest.digest(encryptionPasswordWithSaltByteArray);

                    // Truncate the encryption password byte array to 256 bits (32 bytes).
                    byte[] truncatedHashedEncryptionPasswordWithSaltByteArray = Arrays.copyOf(hashedEncryptionPasswordWithSaltByteArray, 32);

                    // Create an AES secret key from the encryption password byte array.
                    SecretKeySpec secretKey = new SecretKeySpec(truncatedHashedEncryptionPasswordWithSaltByteArray, "AES");

                    // Generate a random 12 byte initialization vector.  According to NIST, a 12 byte initialization vector is more secure than a 16 byte one.
                    byte[] initializationVector = new byte[12];
                    secureRandom.nextBytes(initializationVector);

                    // Get a Advanced Encryption Standard, Galois/Counter Mode, No Padding cipher instance. Galois/Counter mode protects against modification of the ciphertext.  It doesn't use padding.
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

                    // Set the GCM tag length to be 128 bits (the maximum) and apply the initialization vector.
                    GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, initializationVector);

                    // Initialize the cipher.
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

                    // Add the salt and the initialization vector to the export file.
                    encryptedExportFileOutputStream.write(saltByteArray);
                    encryptedExportFileOutputStream.write(initializationVector);

                    // Create a cipher output stream.
                    CipherOutputStream cipherOutputStream = new CipherOutputStream(encryptedExportFileOutputStream, cipher);

                    // Initialize variables to store data as it is moved from the unencrypted export file input stream to the cipher output stream.  Move 128 bits (16 bytes) at a time.
                    int numberOfBytesRead;
                    byte[] encryptedBytes = new byte[16];

                    // Read up to 128 bits (16 bytes) of data from the unencrypted export file stream.  `-1` will be returned when the end of the file is reached.
                    while ((numberOfBytesRead = unencryptedExportFileInputStream.read(encryptedBytes)) != -1) {
                        // Write the data to the cipher output stream.
                        cipherOutputStream.write(encryptedBytes, 0, numberOfBytesRead);
                    }

                    // Close the streams.
                    cipherOutputStream.flush();
                    cipherOutputStream.close();
                    encryptedExportFileOutputStream.close();
                    unencryptedExportFileInputStream.close();

                    // Wipe the encryption data from memory.
                    //noinspection UnusedAssignment
                    encryptionPasswordString = "";
                    Arrays.fill(saltByteArray, (byte) 0);
                    Arrays.fill(encryptionPasswordByteArray, (byte) 0);
                    Arrays.fill(encryptionPasswordWithSaltByteArray, (byte) 0);
                    Arrays.fill(hashedEncryptionPasswordWithSaltByteArray, (byte) 0);
                    Arrays.fill(truncatedHashedEncryptionPasswordWithSaltByteArray, (byte) 0);
                    Arrays.fill(initializationVector, (byte) 0);
                    Arrays.fill(encryptedBytes, (byte) 0);

                    // Delete the temporary unencrypted export file.
                    //noinspection ResultOfMethodCallIgnored
                    temporaryUnencryptedExportFile.delete();
                } catch (Exception exception) {
                    exportStatus = exception.toString();
                }

                // Show a disposition snackbar.
                if (exportStatus.equals(ImportExportDatabaseHelper.EXPORT_SUCCESSFUL)) {
                    Snackbar.make(fileNameEditText, getString(R.string.export_successful), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(fileNameEditText, getString(R.string.export_failed) + "  " + exportStatus, Snackbar.LENGTH_INDEFINITE).show();
                }
                break;

            case OPENPGP_ENCRYPTION:
                // Create an unencrypted export in the private location.
                importExportDatabaseHelper.exportUnencrypted(temporaryUnencryptedExportFile, this);

                // Create an encryption intent for OpenKeychain.
                Intent openKeychainEncryptIntent = new Intent("org.sufficientlysecure.keychain.action.ENCRYPT_DATA");

                // Include the temporary unencrypted export file URI.
                openKeychainEncryptIntent.setData(FileProvider.getUriForFile(this, getString(R.string.file_provider), temporaryUnencryptedExportFile));

                // Allow OpenKeychain to read the file URI.
                openKeychainEncryptIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Send the intent to the OpenKeychain package.
                openKeychainEncryptIntent.setPackage("org.sufficientlysecure.keychain");

                // Make it so.
                startActivityForResult(openKeychainEncryptIntent, OPENPGP_EXPORT_RESULT_CODE);
                break;
        }

        // Add the file to the list of recent files.  This doesn't currently work, but maybe it will someday.
        MediaScannerConnection.scanFile(this, new String[] {exportFileString}, new String[] {"application/x-sqlite3"}, null);
    }

    private void importSettings() {
        // Get a handle for the views.
        Spinner encryptionSpinner = findViewById(R.id.encryption_spinner);
        EditText fileNameEditText = findViewById(R.id.file_name_edittext);

        // Instantiate the import export database helper.
        ImportExportDatabaseHelper importExportDatabaseHelper = new ImportExportDatabaseHelper();

        // Get the import file.
        File importFile = new File(fileNameEditText.getText().toString());

        // Initialize the import status string
        String importStatus = "";

        // Import according to the encryption type.
        switch (encryptionSpinner.getSelectedItemPosition()) {
            case NO_ENCRYPTION:
                // Import the unencrypted file.
                importStatus = importExportDatabaseHelper.importUnencrypted(importFile, this);
                break;

            case PASSWORD_ENCRYPTION:
                // Use a private temporary import location.
                File temporaryUnencryptedImportFile = new File(getApplicationContext().getCacheDir() + "/" + getString(R.string.settings_pbs));

                try {
                    // Create an encrypted import file input stream.
                    FileInputStream encryptedImportFileInputStream = new FileInputStream(importFile);

                    // Delete the temporary import file if it exists.
                    if (temporaryUnencryptedImportFile.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        temporaryUnencryptedImportFile.delete();
                    }

                    // Create an unencrypted import file output stream.
                    FileOutputStream unencryptedImportFileOutputStream = new FileOutputStream(temporaryUnencryptedImportFile);

                    // Get a handle for the encryption password EditText.
                    EditText encryptionPasswordEditText = findViewById(R.id.password_encryption_edittext);

                    // Get the encryption password.
                    String encryptionPasswordString = encryptionPasswordEditText.getText().toString();

                    // Get the salt from the beginning of the import file.
                    byte[] saltByteArray = new byte[32];
                    //noinspection ResultOfMethodCallIgnored
                    encryptedImportFileInputStream.read(saltByteArray);

                    // Get the initialization vector from the import file.
                    byte[] initializationVector = new byte[12];
                    //noinspection ResultOfMethodCallIgnored
                    encryptedImportFileInputStream.read(initializationVector);

                    // Convert the encryption password to a byte array.
                    byte[] encryptionPasswordByteArray = encryptionPasswordString.getBytes(StandardCharsets.UTF_8);

                    // Append the salt to the encryption password byte array.  This protects against rainbow table attacks.
                    byte[] encryptionPasswordWithSaltByteArray = new byte[encryptionPasswordByteArray.length + saltByteArray.length];
                    System.arraycopy(encryptionPasswordByteArray, 0, encryptionPasswordWithSaltByteArray, 0, encryptionPasswordByteArray.length);
                    System.arraycopy(saltByteArray, 0, encryptionPasswordWithSaltByteArray, encryptionPasswordByteArray.length, saltByteArray.length);

                    // Get a SHA-512 message digest.
                    MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");

                    // Hash the salted encryption password.  Otherwise, any characters after the 32nd character in the password are ignored.
                    byte[] hashedEncryptionPasswordWithSaltByteArray = messageDigest.digest(encryptionPasswordWithSaltByteArray);

                    // Truncate the encryption password byte array to 256 bits (32 bytes).
                    byte[] truncatedHashedEncryptionPasswordWithSaltByteArray = Arrays.copyOf(hashedEncryptionPasswordWithSaltByteArray, 32);

                    // Create an AES secret key from the encryption password byte array.
                    SecretKeySpec secretKey = new SecretKeySpec(truncatedHashedEncryptionPasswordWithSaltByteArray, "AES");

                    // Get a Advanced Encryption Standard, Galois/Counter Mode, No Padding cipher instance. Galois/Counter mode protects against modification of the ciphertext.  It doesn't use padding.
                    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

                    // Set the GCM tag length to be 128 bits (the maximum) and apply the initialization vector.
                    GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, initializationVector);

                    // Initialize the cipher.
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

                    // Create a cipher input stream.
                    CipherInputStream cipherInputStream = new CipherInputStream(encryptedImportFileInputStream, cipher);

                    // Initialize variables to store data as it is moved from the cipher input stream to the unencrypted import file output stream.  Move 128 bits (16 bytes) at a time.
                    int numberOfBytesRead;
                    byte[] decryptedBytes = new byte[16];

                    // Read up to 128 bits (16 bytes) of data from the cipher input stream.  `-1` will be returned when the end fo the file is reached.
                    while ((numberOfBytesRead = cipherInputStream.read(decryptedBytes)) != -1) {
                        // Write the data to the unencrypted import file output stream.
                        unencryptedImportFileOutputStream.write(decryptedBytes, 0, numberOfBytesRead);
                    }

                    // Close the streams.
                    unencryptedImportFileOutputStream.flush();
                    unencryptedImportFileOutputStream.close();
                    cipherInputStream.close();
                    encryptedImportFileInputStream.close();

                    // Wipe the encryption data from memory.
                    //noinspection UnusedAssignment
                    encryptionPasswordString = "";
                    Arrays.fill(saltByteArray, (byte) 0);
                    Arrays.fill(initializationVector, (byte) 0);
                    Arrays.fill(encryptionPasswordByteArray, (byte) 0);
                    Arrays.fill(encryptionPasswordWithSaltByteArray, (byte) 0);
                    Arrays.fill(hashedEncryptionPasswordWithSaltByteArray, (byte) 0);
                    Arrays.fill(truncatedHashedEncryptionPasswordWithSaltByteArray, (byte) 0);
                    Arrays.fill(decryptedBytes, (byte) 0);

                    // Import the unencrypted database from the private location.
                    importStatus = importExportDatabaseHelper.importUnencrypted(temporaryUnencryptedImportFile, this);

                    // Delete the temporary unencrypted import file.
                    //noinspection ResultOfMethodCallIgnored
                    temporaryUnencryptedImportFile.delete();
                } catch (Exception exception) {
                    importStatus = exception.toString();
                }
                break;

            case OPENPGP_ENCRYPTION:
                try {
                    // Create an decryption intent for OpenKeychain.
                    Intent openKeychainDecryptIntent = new Intent("org.sufficientlysecure.keychain.action.DECRYPT_DATA");

                    // Include the URI to be decrypted.
                    openKeychainDecryptIntent.setData(FileProvider.getUriForFile(this, getString(R.string.file_provider), importFile));

                    // Allow OpenKeychain to read the file URI.
                    openKeychainDecryptIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Send the intent to the OpenKeychain package.
                    openKeychainDecryptIntent.setPackage("org.sufficientlysecure.keychain");

                    // Make it so.
                    startActivity(openKeychainDecryptIntent);
                } catch (IllegalArgumentException exception) {  // The file import location is not valid.
                    // Display a snack bar with the import error.
                    Snackbar.make(fileNameEditText, getString(R.string.import_failed) + "  " + exception.toString(), Snackbar.LENGTH_INDEFINITE).show();
                }
                break;
        }

        // Respond to the import disposition.
        if (importStatus.equals(ImportExportDatabaseHelper.IMPORT_SUCCESSFUL)) {  // The import was successful.
            // Create an intent to restart Privacy Browser.
            Intent restartIntent = getParentActivityIntent();

            // Assert that the intent is not null to remove the lint error below.
            assert restartIntent != null;

            // `Intent.FLAG_ACTIVITY_CLEAR_TASK` removes all activities from the stack.  It requires `Intent.FLAG_ACTIVITY_NEW_TASK`.
            restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Create a restart handler.
            Handler restartHandler = new Handler();

            // Create a restart runnable.
            Runnable restartRunnable =  () -> {
                // Restart Privacy Browser.
                startActivity(restartIntent);

                // Kill this instance of Privacy Browser.  Otherwise, the app exhibits sporadic behavior after the restart.
                System.exit(0);
            };

            // Restart Privacy Browser after 100 milliseconds to allow enough time for the preferences to be saved.
            restartHandler.postDelayed(restartRunnable, 100);

        } else if (!(encryptionSpinner.getSelectedItemPosition() == OPENPGP_ENCRYPTION)){  // The import was not successful.
            // Display a snack bar with the import error.
            Snackbar.make(fileNameEditText, getString(R.string.import_failed) + "  " + importStatus, Snackbar.LENGTH_INDEFINITE).show();
        }
    }
}