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

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.adapters.RequestsArrayAdapter;
import com.stoutner.privacybrowser.dialogs.ViewRequestDialog;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity implements ViewRequestDialog.ViewRequestListener {
    // The list view is used in `onCreate()` and `launchViewRequestDialog()`.
    private ListView resourceRequestsListView;

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
        setContentView(R.layout.requests_coordinatorlayout);

        // Use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        Toolbar blockListsAppBar = findViewById(R.id.blocklists_toolbar);
        setSupportActionBar(blockListsAppBar);

        // Get a handle for the app bar and the list view.
        ActionBar appBar = getSupportActionBar();
        resourceRequestsListView = findViewById(R.id.resource_requests_listview);

        // Remove the incorrect lint warning that `appBar` might be null.
        assert appBar != null;

        // Display the spinner and the back arrow in the app bar.
        appBar.setCustomView(R.layout.requests_spinner);
        appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);

        // Initialize the resource array lists.
        List<String[]> defaultResourceRequests = new ArrayList<>();
        List<String[]> allowedResourceRequests = new ArrayList<>();
        List<String[]> blockedResourceRequests = new ArrayList<>();

        // Populate the resource array lists.
        for (String[] request : MainWebViewActivity.resourceRequests) {
            switch (Integer.valueOf(request[MainWebViewActivity.REQUEST_DISPOSITION])) {
                case MainWebViewActivity.REQUEST_DEFAULT:
                    defaultResourceRequests.add(request);
                    break;

                case MainWebViewActivity.REQUEST_ALLOWED:
                    allowedResourceRequests.add(request);
                    break;

                case MainWebViewActivity.REQUEST_BLOCKED:
                    blockedResourceRequests.add(request);
                    break;
            }
        }

        // Setup a matrix cursor for the resource lists.
        MatrixCursor spinnerCursor = new MatrixCursor(new String[]{"_id", "Requests"});
        spinnerCursor.addRow(new Object[]{0, getString(R.string.all) + " - " + MainWebViewActivity.resourceRequests.size()});
        spinnerCursor.addRow(new Object[]{1, getString(R.string.default_label) + " - " + defaultResourceRequests.size()});
        spinnerCursor.addRow(new Object[]{2, getString(R.string.allowed_plural) + " - " + allowedResourceRequests.size()});
        spinnerCursor.addRow(new Object[]{3, getString(R.string.blocked_plural) + " - " + blockedResourceRequests.size()});

        // Create a resource cursor adapter for the spinner.
        ResourceCursorAdapter spinnerCursorAdapter = new ResourceCursorAdapter(this, R.layout.requests_spinner_item, spinnerCursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get a handle for the spinner item text view.
                TextView spinnerItemTextView = view.findViewById(R.id.spinner_item_textview);

                // Set the text view to display the resource list.
                spinnerItemTextView.setText(cursor.getString(1));
            }
        };

        // Set the resource cursor adapter drop down view resource.
        spinnerCursorAdapter.setDropDownViewResource(R.layout.requests_spinner_dropdown_item);

        // Get a handle for the app bar spinner and set the adapter.
        Spinner appBarSpinner = findViewById(R.id.requests_spinner);
        appBarSpinner.setAdapter(spinnerCursorAdapter);

        // Handle clicks on the spinner dropdown.
        appBarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:  // All requests.
                        // Get an adapter for all the request.
                        ArrayAdapter<String[]> allResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), MainWebViewActivity.resourceRequests);

                        // Display the adapter in the list view.
                        resourceRequestsListView.setAdapter(allResourceRequestsArrayAdapter);
                        break;

                    case 1:  // Default requests.
                        // Get an adapter for the default requests.
                        ArrayAdapter<String[]> defaultResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), defaultResourceRequests);

                        // Display the adapter in the list view.
                        resourceRequestsListView.setAdapter(defaultResourceRequestsArrayAdapter);
                        break;

                    case 2:  // Allowed requests.
                        // Get an adapter for the allowed requests.
                        ArrayAdapter<String[]> allowedResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), allowedResourceRequests);

                        // Display the adapter in the list view.
                        resourceRequestsListView.setAdapter(allowedResourceRequestsArrayAdapter);
                        break;

                    case 3:  // Blocked requests.
                        // Get an adapter fo the blocked requests.
                        ArrayAdapter<String[]> blockedResourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), blockedResourceRequests);

                        // Display the adapter in the list view.
                        resourceRequestsListView.setAdapter(blockedResourceRequestsArrayAdapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // Create an array adapter with the list of the resource requests.
        ArrayAdapter<String[]> resourceRequestsArrayAdapter = new RequestsArrayAdapter(getApplicationContext(), MainWebViewActivity.resourceRequests);

        // Populate the list view with the resource requests adapter.
        resourceRequestsListView.setAdapter(resourceRequestsArrayAdapter);

        // Listen for taps on entries in the list view.
        resourceRequestsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
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
        boolean isLastRequest = (id == resourceRequestsListView.getCount());

        // Get the string array for the selected resource request.  The resource requests list view is zero based.
        String[] selectedRequestStringArray = (String[]) resourceRequestsListView.getItemAtPosition(id - 1);

        // Remove the warning that `selectedRequest` might be null.
        assert selectedRequestStringArray != null;

        // Show the request detail dialog.
        AppCompatDialogFragment viewRequestDialogFragment = ViewRequestDialog.request(id, isLastRequest, selectedRequestStringArray);
        viewRequestDialogFragment.show(getSupportFragmentManager(), getString(R.string.request_details));
    }
}