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

package com.stoutner.privacybrowser.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.fragments.GuideTabFragment;

public class GuidePagerAdapter extends FragmentPagerAdapter {
    // Define the class context variable.
    private Context context;

    public GuidePagerAdapter(FragmentManager fragmentManager, Context context) {
        // Run the default commands.
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        // Store the context in a class variable.
        this.context = context;
    }

    @Override
    // Get the count of the number of tabs.
    public int getCount() {
        return 9;
    }

    @Override
    // Get the name of each tab.  Tab numbers start at 0.
    public CharSequence getPageTitle(int tab) {
        switch (tab) {
            case 0:
                return context.getString(R.string.overview);

            case 1:
                return context.getString(R.string.javascript);

            case 2:
                return context.getString(R.string.local_storage);

            case 3:
                return context.getString(R.string.user_agent);

            case 4:
                return context.getString(R.string.requests);

            case 5:
                return context.getString(R.string.domain_settings);

            case 6:
                return context.getString(R.string.ssl_certificates);

            case 7:
                return context.getString(R.string.proxies);

            case 8:
                return context.getString(R.string.tracking_ids);

            default:
                return "";
        }
    }

    @Override
    @NonNull
    // Setup each tab.
    public Fragment getItem(int tabNumber) {
        return GuideTabFragment.createTab(tabNumber);
    }
}