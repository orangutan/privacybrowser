/*
 * Copyright © 2016-2017 Soren Stoutner <soren@stoutner.com>.
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

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.stoutner.privacybrowser.fragments.AboutTabFragment;
import com.stoutner.privacybrowser.R;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the theme.
        if (MainWebViewActivity.darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.about_coordinatorlayout);

        // `SupportActionBar` from `android.support.v7.app.ActionBar` must be used until the minimum API is >= 21.
        Toolbar aboutAppBar = findViewById(R.id.about_toolbar);
        setSupportActionBar(aboutAppBar);

        // Display the home arrow on `supportAppBar`.
        final ActionBar appBar = getSupportActionBar();
        assert appBar != null;  // This assert removes the incorrect warning in Android Studio on the following line that appBar might be null.
        appBar.setDisplayHomeAsUpEnabled(true);

        //  Setup the ViewPager.
        ViewPager aboutViewPager = findViewById(R.id.about_viewpager);
        aboutViewPager.setAdapter(new aboutPagerAdapter(getSupportFragmentManager()));

        // Setup the TabLayout and connect it to the ViewPager.
        TabLayout aboutTabLayout = findViewById(R.id.about_tablayout);
        aboutTabLayout.setupWithViewPager(aboutViewPager);
    }

    private class aboutPagerAdapter extends FragmentPagerAdapter {
        private aboutPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        // Get the count of the number of tabs.
        public int getCount() {
            return 7;
        }

        @Override
        // Get the name of each tab.  Tab numbers start at 0.
        public CharSequence getPageTitle(int tab) {
            switch (tab) {
                case 0:
                    return getString(R.string.version);

                case 1:
                    return getString(R.string.permissions);

                case 2:
                    return getString(R.string.privacy_policy);

                case 3:
                    return getString(R.string.changelog);

                case 4:
                    return getString(R.string.licenses);

                case 5:
                    return getString(R.string.contributors);

                case 6:
                    return getString(R.string.links);

                default:
                    return "";
            }
        }

        @Override
        // Setup each tab.
        public Fragment getItem(int tab) {
            return AboutTabFragment.createTab(tab);
        }
    }
}
