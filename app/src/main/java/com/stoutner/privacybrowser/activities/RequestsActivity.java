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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;  // The AndroidX toolbar must be used until the minimum API >= 21.
import androidx.fragment.app.DialogFragment;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.adapters.RequestsArrayAdapter;
import com.stoutner.privacybrowser.dialogs.ViewRequestDialog;
import com.stoutner.privacybrowser.helpers.BlocklistHelper;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity implements ViewRequestDialog.ViewRequestListener {
    // The resource requests are populated by `MainWebViewActivity` before `RequestsActivity` is launched.
    public static List<String[]> resourceRequests;

    // The list view is used in `onCreate()` and `launchViewRequestDialog()`.
    private ListView requestsListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Get the screenshot and theme preferences.
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

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

        // Get the launching intent
        Intent intent = getIntent();

        // Get the status of the third-party blocklist.
        boolean blockAllThirdPartyRequests = intent.getBooleanExtra("block_all_third_party_requests", false);

        // Set the content view.
        setContentView(R.layout.requests_coordinatorlayout);

        // Use the AndroidX toolbar until the minimum API is >= 21.
        Toolbar toolbar = findViewById(R.id.requests_toolbar);
        setSupportActionBar(toolbar);

        // Get a handle for the app bar and the list view.
        ActionBar appBar = getSupportActionBar();
        requestsListView = findViewById(R.id.requests_listview);

        // Remove the incorrect lint warning that `appBar` might be null.
        assert appBar != null;

        // Display the spinner and the back arrow in the app bar.
        appBar.setCustomView(R.layout.spinner);
        appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);

        // Initialize the resource array lists.  A list is needed for all the resource requests, or the activity can crash if `MainWebViewActivity.resourceRequests` is modified after the activity loads.
        List<String[]> allResourceRequests = new ArrayList<>();
        List<String[]> defaultResourceRequests = new ArrayList<>();
        List<String[]> allowedResourceRequests = new ArrayList<>();
        List<String[]> thirdPartyResourceRequests = new ArrayList<>();
        List<String[]> blockedResourceRequests = new ArrayList<>();

        // Populate the resource array lists.
        for (String[] request : resourceRequests) {
            switch (request[BlocklistHelper.REQUEST_DISPOSITION]) {
                case BlocklistHelper.REQUEST_DEFAULT:
                    // Add the request to the list of all requests.
                    allResourceRequests.add(request);

                    // Add the request to the list of default requests.
                    defaultResourceRequests.add(request);
                    break;

                case BlocklistHelper.REQUEST_ALLOWED:
                    // Add the request to the list of all requests.
                    allResourceRequests.add(request);

                    // Add the request to the list of allowed requests.
                    allowedResourceRequests.add(request);
                    break;

                case BlocklistHelper.REQUEST_THIRD_PARTY:
                    // Add the request to the list of all requests.
                    allResourceRequests.add(request);

                    // Add the request to the list of third-party requests.
                    thirdPartyResourceRequests.add(request);
                    break;

                case BlocklistHelper.REQUEST_BLOCKED:
                    // Add the request to the list of all requests.
                    allResourceRequests.add(request);

                    // Add the request to the list of blocked requests.
                    blockedResourceRequests.add(request);
                    break;
            }
        }

        // Setup a matrix cursor for the resource lists.
        MatrixCursor spinnerCursor = new MatrixCursor(new String[]{"_id", "Requests"});
        spinnerCursor.addRow(new Object[]{0, getString(R.string.all) + " - " + allResourceRequests.size()});
        spinnerCursor.addRow(new Object[]{1, getString(R.string.default_label) + " - " + defaultResourceRequests.size()});
        spinnerCursor.addRow(new Object[]{2, getString(R.string.allowed_plural) + " - " + allowedResourceRequests.size()});
        if (blockAllThirdPartyRequests) {
            spinnerCursor.addRow(new Object[]{3, getString(R.string.third_party_plural) + " - " + thirdPartyResourceRequests.size()});
        }
        spinnerCursor.addRow(new Object[]{4, getString(R.string.blocked_plural) + " - " + blockedResourceRequests.size()});

        // Create a resource cursor adapter for the spinner.
        ResourceCursorAdapter spinnerCursorAdapter = new ResourceCursorAdapter(this, R.layout.requests_appbar_spinner_item, spinnerCursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get a handle for the spinner item text view.
                TextView spinnerItemTextView = view.findViewById(R.id.spinner_item_textview);

                // Set the text view to display the resource list.
                spinnerItemTextView.setText(cursor.getString(1));
            }
        };

        // Set the resource cursor adapter drop down view resource.
        spinnerCursorAdapter.setDropDownViewResource(R.layout.requests_appbar_spinner_dropdown_item);

        // Get a handle for the app bar spinner and set the adapter.
        Spinner appBarSpinner = findViewById(R.id.spinner);
        appBarSpinner.setAdapter(spinnerCursorAdapter);

        // Handle clicks on the spinner dropdown.
        appBarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch ((int) id) {
                    case 0:  // All requests.
                        // Get an adapter for all the request.
                        ArrayAdapter<String[]> allResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), allResourceRequests);

                        // Display the adapter in the list view.
                        requestsListView.setAdapter(allResourceRequestsArrayAdapter);
                        break;

                    case 1:  // Default requests.
                        // Get an adapter for the default requests.
                        ArrayAdapter<String[]> defaultResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), defaultResourceRequests);

                        // Display the adapter in the list view.
                        requestsListView.setAdapter(defaultResourceRequestsArrayAdapter);
                        break;

                    case 2:  // Allowed requests.
                        // Get an adapter for the allowed requests.
                        ArrayAdapter<String[]> allowedResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), allowedResourceRequests);

                        // Display the adapter in the list view.
                        requestsListView.setAdapter(allowedResourceRequestsArrayAdapter);
                        break;

                    case 3:  // Third-party requests.
                        // Get an adapter for the third-party requests.
                        ArrayAdapter<String[]> thirdPartyResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), thirdPartyResourceRequests);

                        //Display the adapter in the list view.
                        requestsListView.setAdapter(thirdPartyResourceRequestsArrayAdapter);
                        break;

                    case 4:  // Blocked requests.
                        // Get an adapter fo the blocked requests.
                        ArrayAdapter<String[]> blockedResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), blockedResourceRequests);

                        // Display the adapter in the list view.
                        requestsListView.setAdapter(blockedResourceRequestsArrayAdapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // Create an array adapter with the list of the resource requests.
        ArrayAdapter<String[]> resourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), allResourceRequests);

        // Populate the list view with the resource requests adapter.
        requestsListView.setAdapter(resourceRequestsArrayAdapter);

        // Listen for taps on entries in the list view.
        requestsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Display the view request dialog.  The list view is 0 based, so the position must be incremented by 1.
            launchViewRequestDialog(position + 1);
        });
    }

    @Override
    public void onPrevious(int id) {
        // Show the previous dialog.
        launchViewRequestDialog(id -1);
    }

    @Override
    public void onNext(int id) {
        // Show the next dialog.
        launchViewRequestDialog(id + 1);
    }

    private void launchViewRequestDialog(int id) {
        // Determine if this is the last request in the list.
        boolean isLastRequest = (id == requestsListView.getCount());

        // Get the string array for the selected resource request.  The resource requests list view is zero based.
        String[] selectedRequestStringArray = (String[]) requestsListView.getItemAtPosition(id - 1);

        // Remove the warning that `selectedRequest` might be null.
        assert selectedRequestStringArray != null;

        // Show the request detail dialog.
        DialogFragment viewRequestDialogFragment = ViewRequestDialog.request(id, isLastRequest, selectedRequestStringArray);
        viewRequestDialogFragment.show(getSupportFragmentManager(), getString(R.string.request_details));
    }
}