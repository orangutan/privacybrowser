/*
 * Copyright © 2017-2018 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
// `android.support.v4.app.Fragment` must be used until minimum API >= 23.  Otherwise `getContext()` cannot be called.
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.DomainsActivity;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

public class DomainsListFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate `domains_list_fragment`.  `false` does not attach it to the root `container`.
        View domainsListFragmentView = inflater.inflate(R.layout.domains_list_fragment, container, false);

        // Initialize `domainsListView`.
        ListView domainsListView = domainsListFragmentView.findViewById(R.id.domains_listview);

        // Remove the incorrect lint error below that `.getSupportFragmentManager()` might be null.
        assert getActivity() != null;

        // Get a handle for `supportFragmentManager`.
        final FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();

        domainsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Dismiss `undoDeleteSnackbar` if it is currently displayed (because a domain has just been deleted).
            if ((DomainsActivity.undoDeleteSnackbar != null) && (DomainsActivity.undoDeleteSnackbar.isShown())) {
                DomainsActivity.dismissingSnackbar = true;

                DomainsActivity.undoDeleteSnackbar.dismiss();
            }

            // Save the current domain settings if operating in two-paned mode and a domain is currently selected.
            if (DomainsActivity.twoPanedMode && DomainsActivity.deleteMenuItem.isEnabled()) {
                // Get a handle for the domain settings fragment view.
                View domainSettingsFragmentView = supportFragmentManager.findFragmentById(R.id.domain_settings_fragment_container).getView();

                // Get a handle for the domains activity.
                DomainsActivity domainsActivity = new DomainsActivity();

                // Save the domain settings.
                domainsActivity.saveDomainSettings(domainSettingsFragmentView, getResources());
            }

            // Store the new `currentDomainDatabaseId`, converting it from `long` to `int` to match the format of the domains database.
            DomainsActivity.currentDomainDatabaseId = (int) id;

            // Add `currentDomainDatabaseId` to `argumentsBundle`.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, DomainsActivity.currentDomainDatabaseId);

            // Add `argumentsBundle` to `domainSettingsFragment`.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Display the domain settings fragment.
            if (DomainsActivity.twoPanedMode) {  // The device in in two-paned mode.
                // enable `deleteMenuItem` if the system is not waiting for a `Snackbar` to be dismissed.
                if (!DomainsActivity.dismissingSnackbar) {
                    // Enable `deleteMenuItem`.
                    DomainsActivity.deleteMenuItem.setEnabled(true);

                    // Set the delete icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        DomainsActivity.deleteMenuItem.setIcon(R.drawable.delete_dark);
                    } else {
                        DomainsActivity.deleteMenuItem.setIcon(R.drawable.delete_light);
                    }
                }

                // Display `domainSettingsFragment`.
                supportFragmentManager.beginTransaction().replace(R.id.domain_settings_fragment_container, domainSettingsFragment).commit();
            } else { // The device in in single-paned mode
                // Show `deleteMenuItem` if the system is not waiting for a `Snackbar` to be dismissed.
                if (!DomainsActivity.dismissingSnackbar) {
                    DomainsActivity.deleteMenuItem.setVisible(true);
                }

                // Hide `add_domain_fab`.
                FloatingActionButton addDomainFAB = getActivity().findViewById(R.id.add_domain_fab);
                addDomainFAB.setVisibility(View.GONE);

                // Display `domainSettingsFragment`.
                supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
            }
        });

        return domainsListFragmentView;
    }
}
