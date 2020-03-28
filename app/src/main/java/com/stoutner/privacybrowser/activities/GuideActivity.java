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

package com.stoutner.privacybrowser.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import com.stoutner.privacybrowser.adapters.GuidePagerAdapter;
import com.stoutner.privacybrowser.R;

public class GuideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the theme and screenshot preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the theme.
        if (darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.guide_coordinatorlayout);

        // The AndroidX toolbar must be used until the minimum API is >= 21.
        Toolbar toolbar = findViewById(R.id.guide_toolbar);
        setSupportActionBar(toolbar);

        // Get a handle for the action bar.
        final ActionBar actionBar = getSupportActionBar();

        // Remove the incorrect lint warning that the action bar might be null.
        assert actionBar != null;

        // Display the home arrow on the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        //  Get a handle for the view pager and the tab layout.
        ViewPager aboutViewPager = findViewById(R.id.guide_viewpager);
        TabLayout aboutTabLayout = findViewById(R.id.guide_tablayout);

        // Remove the incorrect lint warnings that the views might be null
        assert aboutViewPager != null;
        assert aboutTabLayout != null;

        // Set the view pager adapter.
        aboutViewPager.setAdapter(new GuidePagerAdapter(getSupportFragmentManager(), getApplicationContext()));

        // Link the tab layout to the view pager.
        aboutTabLayout.setupWithViewPager(aboutViewPager);
    }
}