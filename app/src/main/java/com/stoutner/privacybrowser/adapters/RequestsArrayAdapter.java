/*
 * Copyright © 2018 Soren Stoutner <soren@stoutner.com>.
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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import java.util.List;

public class RequestsArrayAdapter extends ArrayAdapter<String[]> {
    public RequestsArrayAdapter(Context context, List<String[]> resourceRequestsList) {
        // `super` must be called form the base ArrayAdapter.  `0` is the `textViewResourceId`, which is unused.
        super(context, 0, resourceRequestsList);
    }

    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        // Get a handle for the context.
        Context context = getContext();

        // Inflate the view if it is null.
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.requests_item_linearlayout, parent, false);
        }

        // Get handles for the views.
        LinearLayout linearLayout = view.findViewById(R.id.request_item_linearlayout);
        TextView actionTextView = view.findViewById(R.id.request_item_action);
        TextView urlTextView = view.findViewById(R.id.request_item_url);

        // Get the string array for this entry.
        String[] entryStringArray = getItem(position);

        // Remove the lint warning below that `entryStringArray` might be null.
        assert entryStringArray != null;

        // The ID is one greater than the position because it is 0 based.
        int id = position + 1;

        // Set the action text and the background color.
        switch (Integer.valueOf(entryStringArray[0])) {
            case MainWebViewActivity.REQUEST_DEFAULT:
                // Create the disposition string.
                String requestDefault = id + ". " + context.getResources().getString(R.string.allowed);

                // Set the disposition text.
                actionTextView.setText(requestDefault);

                // Set the background color.
                linearLayout.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                break;

            case MainWebViewActivity.REQUEST_ALLOWED:
                // Create the disposition string.
                String requestAllowed = id + ". " + context.getResources().getString(R.string.allowed);

                // Set the disposition text.
                actionTextView.setText(requestAllowed);

                // Set the background color.
                if (MainWebViewActivity.darkTheme) {
                    linearLayout.setBackgroundColor(context.getResources().getColor(R.color.blue_700_50));
                } else {
                    linearLayout.setBackgroundColor(context.getResources().getColor(R.color.blue_100));
                }
                break;


            case MainWebViewActivity.REQUEST_BLOCKED:
                // Create the disposition string.
                String requestBlocked = id + ". " + context.getResources().getString(R.string.blocked);

                // Set the disposition text.
                actionTextView.setText(requestBlocked);

                // Set the background color.
                if (MainWebViewActivity.darkTheme) {
                    linearLayout.setBackgroundColor(context.getResources().getColor(R.color.red_700_50));
                } else {
                    linearLayout.setBackgroundColor(context.getResources().getColor(R.color.red_100));
                }
                break;
        }

        // Set the URL text.
        urlTextView.setText(entryStringArray[1]);

        // Set the text color.  For some unexplained reason, `android:textColor="?android:textColorPrimary"` doesn't work in the layout file.  Probably some bug relating to array adapters.
        if (MainWebViewActivity.darkTheme) {
            actionTextView.setTextColor(context.getResources().getColor(R.color.gray_200));
            urlTextView.setTextColor(context.getResources().getColor(R.color.gray_200));
        } else {
            actionTextView.setTextColor(context.getResources().getColor(R.color.black));
            urlTextView.setTextColor(context.getResources().getColor(R.color.black));
        }

        // Return the modified view.
        return view;
    }
}
