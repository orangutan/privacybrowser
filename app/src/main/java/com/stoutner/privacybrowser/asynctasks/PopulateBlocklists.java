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

package com.stoutner.privacybrowser.asynctasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BlocklistHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PopulateBlocklists extends AsyncTask<Void, String, ArrayList<ArrayList<List<String[]>>>> {
    // The public interface is used to send information back to the parent activity.
    public interface PopulateBlocklistsListener {
        void finishedPopulatingBlocklists(ArrayList<ArrayList<List<String[]>>> combinedBlocklists);
    }

    // Declare a populate blocklists listener.
    private PopulateBlocklistsListener populateBlocklistsListener;

    // Declare weak references for the activity and context.
    private WeakReference<Context> contextWeakReference;
    private WeakReference<Activity> activityWeakReference;

    public PopulateBlocklists(Context context, Activity activity) {
        // Populate the weak reference to the context.
        contextWeakReference = new WeakReference<>(context);

        // Populate the weak reference to the activity.
        activityWeakReference = new WeakReference<>(activity);

        // Get a handle for the populate blocklists listener from the launching activity.
        populateBlocklistsListener = (PopulateBlocklistsListener) context;
    }

    // `onPreExecute()` operates on the UI thread.
    @Override
    protected void onPreExecute() {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            return;
        }

        // Get handles for the views.
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        LinearLayout tabsLinearLayout = activity.findViewById(R.id.tabs_linearlayout);
        RelativeLayout loadingBlocklistsRelativeLayout = activity.findViewById(R.id.loading_blocklists_relativelayout);

        // Hide the toolbar and tabs linear layout, which will be visible if this is being run after the app process has been killed in the background.
        toolbar.setVisibility(View.GONE);
        tabsLinearLayout.setVisibility(View.GONE);

        // Show the loading blocklists screen.
        loadingBlocklistsRelativeLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected ArrayList<ArrayList<List<String[]>>> doInBackground(Void... none) {
        // Get a handle for the context.
        Context context = contextWeakReference.get();

        // Instantiate the blocklist helper.
        BlocklistHelper blocklistHelper = new BlocklistHelper();

        // Create a combined array list.
        ArrayList<ArrayList<List<String[]>>> combinedBlocklists = new ArrayList<>();

        // Load the blocklists if the context still exists.
        if (context != null) {
            // Update the progress.
            publishProgress(context.getString(R.string.loading_easylist));

            // Populate EasyList.
            ArrayList<List<String[]>> easyList = blocklistHelper.parseBlocklist(context.getAssets(), "blocklists/easylist.txt");


            // Update the progress.
            publishProgress(context.getString(R.string.loading_easyprivacy));

            // Populate EasyPrivacy.
            ArrayList<List<String[]>> easyPrivacy = blocklistHelper.parseBlocklist(context.getAssets(), "blocklists/easyprivacy.txt");


            // Update the progress.
            publishProgress(context.getString(R.string.loading_fanboys_annoyance_list));

            // Populate Fanboy's Annoyance List.
            ArrayList<List<String[]>> fanboysAnnoyanceList = blocklistHelper.parseBlocklist(context.getAssets(), "blocklists/fanboy-annoyance.txt");


            // Update the progress.
            publishProgress(context.getString(R.string.loading_fanboys_social_blocking_list));

            // Populate Fanboy's Social Blocking List.
            ArrayList<List<String[]>> fanboysSocialList = blocklistHelper.parseBlocklist(context.getAssets(), "blocklists/fanboy-social.txt");


            // Update the progress.
            publishProgress(context.getString(R.string.loading_ultraprivacy));

            // Populate UltraPrivacy.
            ArrayList<List<String[]>> ultraPrivacy = blocklistHelper.parseBlocklist(context.getAssets(), "blocklists/ultraprivacy.txt");


            // Populate the combined array list.
            combinedBlocklists.add(easyList);
            combinedBlocklists.add(easyPrivacy);
            combinedBlocklists.add(fanboysAnnoyanceList);
            combinedBlocklists.add(fanboysSocialList);
            combinedBlocklists.add(ultraPrivacy);
        }

        // Return the combined array list.
        return combinedBlocklists;
    }

    @Override
    protected void onProgressUpdate(String... loadingBlocklist) {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            return;
        }

        // Get a handle for the loading blocklist text view.
        TextView loadingBlocklistTextView = activity.findViewById(R.id.loading_blocklist_textview);

        // Update the status.
        loadingBlocklistTextView.setText(loadingBlocklist[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<ArrayList<List<String[]>>> combinedBlocklists) {
        // Get a handle for the activity.
        Activity activity = activityWeakReference.get();

        // Abort if the activity is gone.
        if ((activity == null) || activity.isFinishing()) {
            return;
        }

        // Get handles for the views.
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        DrawerLayout drawerLayout = activity.findViewById(R.id.drawerlayout);
        LinearLayout tabsLinearLayout = activity.findViewById(R.id.tabs_linearlayout);
        RelativeLayout loadingBlocklistsRelativeLayout = activity.findViewById(R.id.loading_blocklists_relativelayout);

        // Show the toolbar and tabs linear layout.
        toolbar.setVisibility(View.VISIBLE);
        tabsLinearLayout.setVisibility(View.VISIBLE);

        // Hide the loading blocklists screen.
        loadingBlocklistsRelativeLayout.setVisibility(View.GONE);

        // Enable the sliding drawers.
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        // Add the first tab.
        populateBlocklistsListener.finishedPopulatingBlocklists(combinedBlocklists);
    }
}