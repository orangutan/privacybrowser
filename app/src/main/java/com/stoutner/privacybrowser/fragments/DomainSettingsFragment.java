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

package com.stoutner.privacybrowser.fragments;

import android.database.Cursor;
import android.os.Bundle;
// We have to use `android.support.v4.app.Fragment` until minimum API >= 23.  Otherwise we cannot call `getContext()`.
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class DomainSettingsFragment extends Fragment {
    // `DATABASE_ID` is used by activities calling this fragment.
    public static final String DATABASE_ID = "database_id";

    // `databaseId` is used in `onCreate()` and `onCreateView()`.
    private int databaseId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the database id in `databaseId`.
        databaseId = getArguments().getInt(DATABASE_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate `domain_settings`.  `false` does not attach it to the root `container`.
        View domainSettingsView = inflater.inflate(R.layout.domain_settings, container, false);

        // Initialize the database handler.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
        // The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getContext(), null, null, 0);

        // Get the database `Cursor` for this ID and move it to the first row.
        Cursor domainCursor = domainsDatabaseHelper.getCursorForId(databaseId);
        domainCursor.moveToFirst();

        // Get handles for the `EditTexts`.
        EditText domainNameEditText = (EditText) domainSettingsView.findViewById(R.id.domain_settings_name_edittext);

        // Set the text from the database cursor.
        domainNameEditText.setText(domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN)));

        return domainSettingsView;
    }
}
