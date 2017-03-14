/*
 * Copyright 2017 Soren Stoutner <soren@stoutner.com>.
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
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.AddDomainDialog;
import com.stoutner.privacybrowser.fragments.DomainSettingsFragment;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class DomainsActivity extends AppCompatActivity implements AddDomainDialog.AddDomainListener {
    // `context` is used in `onCreate()` and `onOptionsItemSelected()`.
    Context context;

    // `domainsDatabaseHelper` is used in `onCreate()`, `onOptionsItemSelected()`, `onAddDomain()`, and `updateDomainsRecyclerView()`.
    private static DomainsDatabaseHelper domainsDatabaseHelper;

    // `domainsRecyclerView` is used in `onCreate()` and `updateDomainsListView()`.
    private ListView domainsListView;

    // `databaseId` is used in `onCreate()` and `onOptionsItemSelected()`.
    private int databaseId;

    // `saveMenuItem` is used in `onCreate()`, `onOptionsItemSelected()`, and `onCreateOptionsMenu()`.
    private MenuItem saveMenuItem;

    // `deleteMenuItem` is used in `onCreate()`, `onOptionsItemSelected()`, and `onCreateOptionsMenu()`.
    private MenuItem deleteMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.domains_coordinatorlayout);

        // Get a handle for the context.
        context = this;

        // We need to use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        final Toolbar bookmarksAppBar = (Toolbar) findViewById(R.id.domains_toolbar);
        setSupportActionBar(bookmarksAppBar);

        // Display the home arrow on `SupportActionBar`.
        ActionBar appBar = getSupportActionBar();
        assert appBar != null;// This assert removes the incorrect warning in Android Studio on the following line that `appBar` might be null.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Initialize the database handler.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
        // The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        domainsDatabaseHelper = new DomainsDatabaseHelper(this, null, null, 0);

        // Determine if we are in two pane mode.  `domains_settings_linearlayout` is only populated if two panes are present.
        final boolean twoPaneMode = ((findViewById(R.id.domain_settings_scrollview)) != null);

        // Initialize `domainsListView`.
        domainsListView = (ListView) findViewById(R.id.domains_listview);

        domainsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Convert the id from `long` to `int` to match the format of the domains database.
                databaseId = (int) id;

                // Display the Domain Settings.
                if (twoPaneMode) {  // Display a fragment in two paned mode.
                    // Display the options `MenuItems`.
                    saveMenuItem.setVisible(true);
                    deleteMenuItem.setVisible(true);

                    // Store `databaseId` in `argumentsBundle`.
                    Bundle argumentsBundle = new Bundle();
                    argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, databaseId);

                    // Add `argumentsBundle` to `domainSettingsFragment`.
                    DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
                    domainSettingsFragment.setArguments(argumentsBundle);

                    // Display `domainSettingsFragment`.
                    getSupportFragmentManager().beginTransaction().replace(R.id.domain_settings_scrollview, domainSettingsFragment).commit();
                } else { // Load the second activity on smaller screens.
                    // Create `domainSettingsActivityIntent` with the `databaseId`.
                    Intent domainSettingsActivityIntent = new Intent(context, DomainSettingsActivity.class);
                    domainSettingsActivityIntent.putExtra(DomainSettingsFragment.DATABASE_ID, databaseId);

                    // Start `DomainSettingsActivity`.
                    context.startActivity(domainSettingsActivityIntent);
                }
            }
        });

        FloatingActionButton addDomainFAB = (FloatingActionButton) findViewById(R.id.add_domain_fab);
        addDomainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the `AddDomainDialog` `AlertDialog` and name the instance `@string/add_domain`.
                AppCompatDialogFragment addDomainDialog = new AddDomainDialog();
                addDomainDialog.show(getSupportFragmentManager(), getResources().getString(R.string.add_domain));
            }
        });

        // Load the `ListView`.
        updateDomainsListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.domains_options_menu, menu);

        // Store the `MenuItems` for future use.
        saveMenuItem = menu.findItem(R.id.save_domain);
        deleteMenuItem = menu.findItem(R.id.delete_domain);

        // Success!
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the ID of the `MenuItem` that was selected.
        int menuItemID = menuItem.getItemId();

        switch (menuItemID) {
            case android.R.id.home:  // The home arrow is identified as `android.R.id.home`, not just `R.id.home`.
                // Go home.
                NavUtils.navigateUpFromSameTask(this);
                break;

            case R.id.save_domain:
                // Get handles for the domain settings.
                EditText domainNameEditText = (EditText) findViewById(R.id.domain_settings_name_edittext);
                Switch javaScriptEnabledSwitch = (Switch) findViewById(R.id.domain_settings_javascript_switch);
                Switch firstPartyCookiesEnabledSwitch = (Switch) findViewById(R.id.domain_settings_first_party_cookies_switch);
                Switch thirdPartyCookiesEnabledSwitch = (Switch) findViewById(R.id.domain_settings_third_party_cookies_switch);
                Switch domStorageEnabledSwitch = (Switch) findViewById(R.id.domain_settings_dom_storage_switch);
                Switch formDataEnabledSwitch = (Switch) findViewById(R.id.domain_settings_form_data_switch);
                Spinner userAgentSpinner = (Spinner) findViewById(R.id.domain_settings_user_agent_spinner);
                EditText customUserAgentEditText = (EditText) findViewById(R.id.domain_settings_custom_user_agent_edittext);
                Spinner fontSizeSpinner = (Spinner) findViewById(R.id.domain_settings_font_size_spinner);

                // Extract the data for the domain settings.
                String domainNameString = domainNameEditText.getText().toString();
                boolean javaScriptEnabled = javaScriptEnabledSwitch.isChecked();
                boolean firstPartyCookiesEnabled = firstPartyCookiesEnabledSwitch.isChecked();
                boolean thirdPartyCookiesEnabled = thirdPartyCookiesEnabledSwitch.isChecked();
                boolean domStorageEnabledEnabled = domStorageEnabledSwitch.isChecked();
                boolean formDataEnabled = formDataEnabledSwitch.isChecked();
                int userAgentPosition = userAgentSpinner.getSelectedItemPosition();
                int fontSizePosition = fontSizeSpinner.getSelectedItemPosition();

                // Get the data for the `Spinners` from the entry values string arrays.
                String userAgentString = getResources().getStringArray(R.array.user_agent_entry_values)[userAgentPosition];
                int fontSizeInt = Integer.parseInt(getResources().getStringArray(R.array.default_font_size_entry_values)[fontSizePosition]);

                // Check to see if we are using a custom user agent.
                if (userAgentString.equals("Custom user agent")) {
                    // Set `userAgentString` to the custom user agent string.
                    userAgentString = customUserAgentEditText.getText().toString();
                }

                // Save the domain settings.
                domainsDatabaseHelper.saveDomain(databaseId, domainNameString, javaScriptEnabled, firstPartyCookiesEnabled, thirdPartyCookiesEnabled, domStorageEnabledEnabled, formDataEnabled, userAgentString, fontSizeInt);
                break;

            case R.id.delete_domain:
                // Delete the selected domain.
                domainsDatabaseHelper.deleteDomain(databaseId);

                // Detach the domain settings fragment.
                getSupportFragmentManager().beginTransaction().detach(getSupportFragmentManager().findFragmentById(R.id.domain_settings_scrollview)).commit();

                // Hide the options `MenuItems`.
                saveMenuItem.setVisible(false);
                deleteMenuItem.setVisible(false);

                // Update the `ListView`.
                updateDomainsListView();
                break;
        }
        return true;
    }

    @Override
    public void onAddDomain(AppCompatDialogFragment dialogFragment) {
        // Get the `domainNameEditText` from `dialogFragment` and extract the string.
        EditText domainNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.domain_name_edittext);
        String domainNameString = domainNameEditText.getText().toString();

        // Create the domain.
        domainsDatabaseHelper.addDomain(domainNameString);

        // Refresh the `ListView`.
        updateDomainsListView();
    }

    private void updateDomainsListView() {
        // Get a `Cursor` with the current contents of the domains database.
        Cursor domainsCursor = domainsDatabaseHelper.getCursorOrderedByDomain();

        // Setup `domainsCursorAdapter` with `this` context.  `false` disables `autoRequery`.
        CursorAdapter domainsCursorAdapter = new CursorAdapter(this, domainsCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.domain_name_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Set the domain name.
                String domainNameString = cursor.getString(cursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
                TextView domainNameTextView = (TextView) view.findViewById(R.id.domain_name_textview);
                domainNameTextView.setText(domainNameString);
            }
        };

        // Update the `RecyclerView`.
        domainsListView.setAdapter(domainsCursorAdapter);
    }
}