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

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
// We have to use `android.support.v4.app.Fragment` until minimum API >= 23.  Otherwise we cannot call `getContext()`.
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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

        // Get a handle for the `Context`.
        Context context = getContext();

        // Get handles for the views in the fragment.
        EditText domainNameEditText = (EditText) domainSettingsView.findViewById(R.id.domain_settings_name_edittext);
        Spinner userAgentSpinner = (Spinner) domainSettingsView.findViewById(R.id.domain_settings_user_agent_spinner);
        Spinner fontSizeSpinner = (Spinner) domainSettingsView.findViewById(R.id.domain_settings_font_size_spinner);

        // Initialize the database handler.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
        // The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getContext(), null, null, 0);

        // Get the database `Cursor` for this ID and move it to the first row.
        Cursor domainCursor = domainsDatabaseHelper.getCursorForId(databaseId);
        domainCursor.moveToFirst();

        // Save the `Cursor` entries as variables.
        String domainNameString = domainCursor.getString(domainCursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN));
        int fontSizeInt = domainCursor.getInt(domainCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE));

        // Create `ArrayAdapters` for the `Spinners`and their `entry values`.
        ArrayAdapter<CharSequence> userAgentArrayAdapter = ArrayAdapter.createFromResource(context, R.array.user_agent_entries, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> userAgentEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.user_agent_entry_values, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> fontSizeArrayAdapter = ArrayAdapter.createFromResource(context, R.array.default_font_size_entries, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> fontSizeEntryValuesArrayAdapter = ArrayAdapter.createFromResource(context, R.array.default_font_size_entry_values, android.R.layout.simple_spinner_item);

        // Set the drop down style for the `ArrayAdapters`.
        userAgentArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSizeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the `ArrayAdapters` for the `Spinners`.
        userAgentSpinner.setAdapter(userAgentArrayAdapter);
        fontSizeSpinner.setAdapter(fontSizeArrayAdapter);

        //
        // int userAgentArrayPosition =

        // Set the selected font size.
        int fontSizeArrayPosition = fontSizeEntryValuesArrayAdapter.getPosition(String.valueOf(fontSizeInt));
        fontSizeSpinner.setSelection(fontSizeArrayPosition);


        // Set the text from the database cursor.
        domainNameEditText.setText(domainNameString);

        return domainSettingsView;
    }
}
