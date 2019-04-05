/*
 * Copyright © 2019 Soren Stoutner <soren@stoutner.com>.
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.stoutner.privacybrowser.fragments.WebViewTabFragment;

import java.util.LinkedList;

public class WebViewPagerAdapter extends FragmentPagerAdapter {
    // The WebView fragments list contains all the WebViews.
    private LinkedList<WebViewTabFragment> webViewFragmentsList = new LinkedList<>();

    // Define the constructor.
    public WebViewPagerAdapter(FragmentManager fragmentManager){
        // Run the default commands.
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        // Return the number of pages.
        return webViewFragmentsList.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        //noinspection SuspiciousMethodCalls
        if (webViewFragmentsList.contains(object)) {
            // Return the current page position.
            //noinspection SuspiciousMethodCalls
            return webViewFragmentsList.indexOf(object);
        } else {
            // The tab has been deleted.
            return POSITION_NONE;
        }
    }

    @Override
    public Fragment getItem(int pageNumber) {
        // Get the fragment for a particular page.  Page numbers are 0 indexed.
        return webViewFragmentsList.get(pageNumber);
    }

    @Override
    public long getItemId(int position) {
        // Return the unique ID for this page.
        return webViewFragmentsList.get(position).fragmentId;
    }

    public int getPositionForId(long fragmentId) {
        // Initialize the position variable.
        int position = -1;

        // Initialize the while counter.
        int i = 0;

        // Find the current position of the WebView fragment with the given ID.
        while (position < 0 && i < webViewFragmentsList.size()) {
            // Check to see if the tab ID of this WebView matches the page ID.
            if (webViewFragmentsList.get(i).fragmentId == fragmentId) {
                // Store the position if they are a match.
                position = i;
            }

            // Increment the counter.
            i++;
        }

        // Return the position.
        return position;
    }

    public void addPage(int pageNumber, ViewPager webViewPager) {
        // Add a new page.
        webViewFragmentsList.add(WebViewTabFragment.createPage(pageNumber));

        // Update the view pager.
        notifyDataSetChanged();

        // Move to the new page if it isn't the first one.
        if (pageNumber > 0) {
            webViewPager.setCurrentItem(pageNumber);
        }
    }

    public void deletePage(int pageNumber) {
        // Delete the page.
        webViewFragmentsList.remove(pageNumber);

        // Update the view pager.
        notifyDataSetChanged();
    }

    public WebViewTabFragment getPageFragment(int pageNumber) {
        return webViewFragmentsList.get(pageNumber);
    }
}