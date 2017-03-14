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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.fragments.DomainSettingsFragment;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class DomainSettingsActivity extends AppCompatActivity {
    // `databaseId` is used in `onCreate()` and `onOptionsItemSelected()`.
    int databaseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.domain_settings_coordinatorlayout);

        // We ned to use `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        Toolbar domainSettingsAppBar = (Toolbar) findViewById(R.id.domain_settings_toolbar);
        setSupportActionBar(domainSettingsAppBar);

        // Display the home arrow on `appBar`.
        final ActionBar appBar = getSupportActionBar();
        assert appBar != null;  // This assert removes the incorrect lint warning in Android Studio on the following line that `appBar` might be `null`.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Get the intent that started the activity.
        final Intent launchingIntent = getIntent();

        // Extract the `databaseID`.  The default value is `1`.
        databaseId = launchingIntent.getIntExtra(DomainSettingsFragment.DATABASE_ID, 1);

        // Store `databaseId` in `argumentsBundle`.
        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, databaseId);

        // Add `argumentsBundle` to `domainSettingsFragment`.
        DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
        domainSettingsFragment.setArguments(argumentsBundle);

        // Display `domainSettingsFragment`.
        getSupportFragmentManager().beginTransaction().replace(R.id.domain_settings_scrollview, domainSettingsFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.domains_options_menu, menu);

        // Success!
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the ID of the `MenuItem` that was selected.
        int menuItemID = menuItem.getItemId();

        // Initialize the database handler.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
        // The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getApplicationContext(), null, null, 0);

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

                // Navigate to `DomainsActivity`.
                NavUtils.navigateUpFromSameTask(this);
                break;

            case R.id.delete_domain:
                // Delete the selected domain.
                domainsDatabaseHelper.deleteDomain(databaseId);

                // Navigate to `DomainsActivity`.
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return true;
    }
}
