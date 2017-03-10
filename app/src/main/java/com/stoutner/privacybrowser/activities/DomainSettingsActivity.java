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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.fragments.DomainSettingsFragment;

public class DomainSettingsActivity extends AppCompatActivity {
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

        // Extract the `databaseID`.  The default value is `0`.
        int databaseId = launchingIntent.getIntExtra(DomainSettingsFragment.DATABASE_ID, 0);

        // Store `databaseId` in `argumentsBundle`.
        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, databaseId);

        // Add `argumentsBundle` to `domainSettingsFragment`.
        DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
        domainSettingsFragment.setArguments(argumentsBundle);

        // Display `domainSettingsFragment`.
        getSupportFragmentManager().beginTransaction().replace(R.id.domain_settings_linearlayout, domainSettingsFragment).commit();
    }
}
