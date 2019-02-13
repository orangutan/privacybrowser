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

package com.stoutner.privacybrowser.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.StoragePermissionDialog;
import com.stoutner.privacybrowser.dialogs.SaveLogcatDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

public class LogcatActivity extends AppCompatActivity implements SaveLogcatDialog.SaveLogcatListener, StoragePermissionDialog.StoragePermissionDialogListener {
    private String filePathString;

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
        setContentView(R.layout.logcat_coordinatorlayout);

        // Use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        Toolbar logcatAppBar = findViewById(R.id.logcat_toolbar);
        setSupportActionBar(logcatAppBar);

        // Get a handle for the app bar.
        ActionBar appBar = getSupportActionBar();

        // Remove the incorrect lint warning that `appBar` might be null.
        assert appBar != null;

        // Display the the back arrow in the app bar.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Implement swipe to refresh.
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.logcat_swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Get the current logcat.
            new GetLogcat(this).execute();
        });

        // Set the swipe to refresh color according to the theme.
        if (MainWebViewActivity.darkTheme) {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_600);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.gray_800);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_700);
        }

        // Get the logcat.
        new GetLogcat(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.  This adds items to the action bar.
        getMenuInflater().inflate(R.menu.logcat_options_menu, menu);

        // Display the menu.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the selected menu item ID.
        int menuItemId = menuItem.getItemId();

        // Run the commands that correlate to the selected menu item.
        switch (menuItemId) {
            case R.id.copy:
                // Get a handle for the clipboard manager.
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                // Get a handle for the logcat text view.
                TextView logcatTextView = findViewById(R.id.logcat_textview);

                // Save the logcat in a ClipData.
                ClipData logcatClipData = ClipData.newPlainText(getString(R.string.logcat), logcatTextView.getText());

                // Remove the incorrect lint error that `clipboardManager.setPrimaryClip()` might produce a null pointer exception.
                assert clipboardManager != null;

                // Place the ClipData on the clipboard.
                clipboardManager.setPrimaryClip(logcatClipData);

                // Display a snackbar.
                Snackbar.make(logcatTextView, R.string.logcat_copied, Snackbar.LENGTH_SHORT).show();

                // Consume the event.
                return true;

            case R.id.save:
                // Get a handle for the save alert dialog.
                DialogFragment saveDialogFragment = new SaveLogcatDialog();

                // Show the save alert dialog.
                saveDialogFragment.show(getSupportFragmentManager(), getString(R.string.save_logcat));

                // Consume the event.
                return true;

            case R.id.clear:
                try {
                    // Clear the logcat.  `-c` clears the logcat.  `-b all` clears all the buffers (instead of just crash, main, and system).
                    Process process = Runtime.getRuntime().exec("logcat -b all -c");

                    // Wait for the process to finish.
                    process.waitFor();

                    // Reload the logcat.
                    new GetLogcat(this).execute();
                } catch (IOException|InterruptedException exception) {
                    // Do nothing.
                }

                // Consume the event.
                return true;

            default:
                // Don't consume the event.
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onSaveLogcat(AppCompatDialogFragment dialogFragment) {
        // Get a handle for the file name edit text.
        EditText fileNameEditText = dialogFragment.getDialog().findViewById(R.id.file_name_edittext);

        // Get the file path string.
        filePathString = fileNameEditText.getText().toString();

        // Check to see if the storage permission is needed.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // The storage permission has been granted.
            // Save the logcat.
            saveLogcat(filePathString);
        } else {  // The storage permission has not been granted.
            // Get the external private directory `File`.
            File externalPrivateDirectoryFile = getExternalFilesDir(null);

            // Remove the incorrect lint error below that the file might be null.
            assert externalPrivateDirectoryFile != null;

            // Get the external private directory string.
            String externalPrivateDirectory = externalPrivateDirectoryFile.toString();

            // Check to see if the file path is in the external private directory.
            if (filePathString.startsWith(externalPrivateDirectory)) {  // The file path is in the external private directory.
                // Save the logcat.
                saveLogcat(filePathString);
            } else {  // The file path in in a public directory.
                // Check if the user has previously denied the storage permission.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                    // Instantiate the storage permission alert dialog.
                    DialogFragment storagePermissionDialogFragment = new StoragePermissionDialog();

                    // Show the storage permission alert dialog.  The permission will be requested when the dialog is closed.
                    storagePermissionDialogFragment.show(getSupportFragmentManager(), getString(R.string.storage_permission));
                } else {  // Show the permission request directly.
                    // Request the storage permission.  The logcat will be saved when it finishes.
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

                }
            }
        }
    }

    @Override
    public void onCloseStoragePermissionDialog() {
        // Request the write external storage permission.  The logcat will be saved when it finishes.
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Check to see if the storage permission was granted.  If the dialog was canceled the grant result will be empty.
        if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {  // The storage permission was granted.
            // Save the logcat.
            saveLogcat(filePathString);
        } else {  // The storage permission was not granted.
            // Get a handle for the logcat text view.
            TextView logcatTextView = findViewById(R.id.logcat_textview);

            // Display an error snackbar.
            Snackbar.make(logcatTextView, getString(R.string.cannot_use_location), Snackbar.LENGTH_LONG).show();
        }
    }

    private void saveLogcat(String fileNameString) {
        // Get a handle for the logcat text view.
        TextView logcatTextView = findViewById(R.id.logcat_textview);

        try {
            // Get the logcat as a string.
            String logcatString = logcatTextView.getText().toString();

            // Create an input stream with the contents of the logcat.
            InputStream logcatInputStream = new ByteArrayInputStream(logcatString.getBytes(StandardCharsets.UTF_8));

            // Create a logcat buffered reader.
            BufferedReader logcatBufferedReader = new BufferedReader(new InputStreamReader(logcatInputStream));

            // Create a file from the file name string.
            File saveFile = new File(fileNameString);

            // Create a file buffered writer.
            BufferedWriter fileBufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveFile)));

            // Create a transfer string.
            String transferString;

            // Use the transfer string to copy the logcat from the buffered reader to the buffered writer.
            while ((transferString = logcatBufferedReader.readLine()) != null) {
                // Append the line to the buffered writer.
                fileBufferedWriter.append(transferString);

                // Append a line break.
                fileBufferedWriter.append("\n");
            }

            // Close the buffered reader and writer.
            logcatBufferedReader.close();
            fileBufferedWriter.close();

            // Add the file to the list of recent files.  This doesn't currently work, but maybe it will someday.
            MediaScannerConnection.scanFile(this, new String[] {fileNameString}, new String[] {"text/plain"}, null);

            // Display a snackbar.
            Snackbar.make(logcatTextView, getString(R.string.file_saved_successfully), Snackbar.LENGTH_SHORT).show();
        } catch (Exception exception) {
            // Display a snackbar with the error message.
            Snackbar.make(logcatTextView, getString(R.string.save_failed) + "  " + exception.toString(), Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    // The activity result is called after browsing for a file in the save alert dialog.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Don't do anything if the user pressed back from the file picker.
        if (resultCode == Activity.RESULT_OK) {
            // Get a handle for the save dialog fragment.
            DialogFragment saveDialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.save_logcat));

            // Remove the incorrect lint error that the save dialog fragment might be null.
            assert saveDialogFragment != null;

            // Get a handle for the save dialog.
            Dialog saveDialog = saveDialogFragment.getDialog();

            // Get a handle for the file name edit text.
            EditText fileNameEditText = saveDialog.findViewById(R.id.file_name_edittext);

            // Get the file name URI.
            Uri fileNameUri = data.getData();

            // Remove the incorrect lint warning that the file name URI might be null.
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

                // Set the file name path as the text of the file name edit text.
                fileNameEditText.setText(fileNamePath);
            } else {  // The path is invalid.
                // Close the alert dialog.
                saveDialog.dismiss();

                // Get a handle for the logcat text view.
                TextView logcatTextView = findViewById(R.id.logcat_textview);

                // Display a snackbar with the error message.
                Snackbar.make(logcatTextView, rawFileNamePath + " " + getString(R.string.invalid_location), Snackbar.LENGTH_INDEFINITE).show();
            }
        }
    }

    // `Void` does not declare any parameters.  `Void` does not declare progress units.  `String` contains the results.
    private static class GetLogcat extends AsyncTask<Void, Void, String> {
        // Create a weak reference to the calling activity.
        private final WeakReference<Activity> activityWeakReference;

        // Populate the weak reference to the calling activity.
        GetLogcat(Activity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(Void... parameters) {
            // Get a handle for the activity.
            Activity activity = activityWeakReference.get();

            // Abort if the activity is gone.
            if ((activity == null) || activity.isFinishing()) {
                return "";
            }

            // Create a log string builder.
            StringBuilder logStringBuilder = new StringBuilder();

            try {
                // Get the logcat.  `-b all` gets all the buffers (instead of just crash, main, and system).  `-v long` produces more complete information.  `-d` dumps the logcat and exits.
                Process process = Runtime.getRuntime().exec("logcat -b all -v long -d");

                // Wrap the logcat in a buffered reader.
                BufferedReader logBufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                // Create a log transfer string.
                String logTransferString;

                // Use the log transfer string to copy the logcat from the buffered reader to the string builder.
                while ((logTransferString = logBufferedReader.readLine()) != null) {
                    // Append a line.
                    logStringBuilder.append(logTransferString);

                    // Append a line break.
                    logStringBuilder.append("\n");
                }

                // Close the buffered reader.
                logBufferedReader.close();
            } catch (IOException exception) {
                // Do nothing.
            }

            // Return the logcat.
            return logStringBuilder.toString();
        }

        // `onPostExecute()` operates on the UI thread.
        @Override
        protected void onPostExecute(String logcatString) {
            // Get a handle for the activity.
            Activity activity = activityWeakReference.get();

            // Abort if the activity is gone.
            if ((activity == null) || activity.isFinishing()) {
                return;
            }

            // Get handles for the views.
            TextView logcatTextView = activity.findViewById(R.id.logcat_textview);
            SwipeRefreshLayout swipeRefreshLayout = activity.findViewById(R.id.logcat_swiperefreshlayout);

            // Display the logcat.
            logcatTextView.setText(logcatString);

            // Stop the swipe to refresh animation if it is displayed.
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}