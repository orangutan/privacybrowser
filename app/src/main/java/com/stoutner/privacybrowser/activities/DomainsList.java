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
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.AddDomain;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class DomainsList extends AppCompatActivity implements AddDomain.AddDomainListener {
    // `domainsDatabaseHelper` is used in `onCreate()`, `onAddDomain()`, and `updateDomainsRecyclerView()`.
    private static DomainsDatabaseHelper domainsDatabaseHelper;

    // `domainsRecyclerView` is used in `onCreate()` and `updateDomainsListView()`.
    private ListView domainsListView;

    private boolean twoPaneMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.domains_list_coordinatorlayout);

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

        // Determine if we are in two pane mode.  `domains_list_framelayout` is only populated if two panes are present.
        twoPaneMode = ((findViewById(R.id.domains_list_framelayout)) != null);

        // Initialize `domainsListView`.
        domainsListView = (ListView) findViewById(R.id.domains_listview);

        domainsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Convert the id from long to int to match the format of the domains database.
                int databaseId = (int) id;

                // Get the database `Cursor` for this ID and move it to the first row.
                Cursor domainCursor = domainsDatabaseHelper.getCursorForId(databaseId);
                domainCursor.moveToFirst();

                // If the
            }
        });

        FloatingActionButton addDomainFAB = (FloatingActionButton) findViewById(R.id.add_domain_fab);
        addDomainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the `AddDomain` `AlertDialog` and name the instance `@string/add_domain`.
                AppCompatDialogFragment addDomainDialog = new AddDomain();
                addDomainDialog.show(getSupportFragmentManager(), getResources().getString(R.string.add_domain));
            }
        });

        // Load the `ListView`.
        updateDomainsListView();
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
                String domainNameString = cursor.getString(cursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN));
                TextView domainNameTextView = (TextView) view.findViewById(R.id.domain_name_textview);
                domainNameTextView.setText(domainNameString);
            }
        };

        // Update the `RecyclerView`.
        domainsListView.setAdapter(domainsCursorAdapter);
    }
}