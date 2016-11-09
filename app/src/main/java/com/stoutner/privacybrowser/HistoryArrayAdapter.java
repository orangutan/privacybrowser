/**
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class HistoryArrayAdapter extends ArrayAdapter<History> {

    // `currentPage` is used in `HistoryArrayAdapter` and `getView()`.
    private int currentPage;

    HistoryArrayAdapter(Context context, ArrayList<History> historyArrayList, int currentPageId) {
        // We need to call `super` from the base `ArrayAdapter`.  `0` is the `textViewResourceId`.
        super(context, 0, historyArrayList);

        // Store `currentPageId` in the class variable.
        currentPage = currentPageId;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Inflate the view if it is `null`.
        if (convertView == null) {
            // `false` does not attach `url_history_item_linearlayout` to `parent`.
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.url_history_item_linearlayout, parent, false);
        }

        // Get handles for `favoriteIconImageView` and `urlTextView`.
        ImageView favoriteIconImageView = (ImageView) convertView.findViewById(R.id.history_favorite_icon_imageview);
        TextView urlTextView = (TextView) convertView.findViewById(R.id.history_url_textview);

        // Get the URL history for this position.
        History history = getItem(position);

        // Remove the lint warning below that `history` might be `null`.
        assert history != null;

        // Set `favoriteIconImageView` and `urlTextView`.
        favoriteIconImageView.setImageBitmap(history.entryFavoriteIcon);
        urlTextView.setText(history.entryUrl);

        // Set the URL text for `currentPage` to be grey.
        if (position == currentPage) {
            urlTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.grey_500));
        } else {
            urlTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }

        // Return the modified `convertView`.
        return convertView;
    }
}