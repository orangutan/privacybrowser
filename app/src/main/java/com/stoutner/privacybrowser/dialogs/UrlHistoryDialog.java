/*
 * Copyright © 2016-2019 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebBackForwardList;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.adapters.HistoryArrayAdapter;
import com.stoutner.privacybrowser.definitions.History;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class UrlHistoryDialog extends DialogFragment{
    // Declare the class variables.
    private final ArrayList<History> historyArrayList = new ArrayList<>();
    private int currentPageId;

    // Create a URL history listener.
    private UrlHistoryListener urlHistoryListener;


    // The public interface is used to send information back to the parent activity.
    public interface UrlHistoryListener {
        // Send back the number of steps to move forward or back.
        void onUrlHistoryEntrySelected(int moveBackOrForwardSteps);

        // Clear the history.
        void onClearHistory();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Check to make sure tha the parent activity implements the listener.
        try {
            urlHistoryListener = (UrlHistoryListener) context;
        } catch (ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement UrlHistoryListener.");
        }
    }


    public static UrlHistoryDialog loadBackForwardList(Context context, WebBackForwardList webBackForwardList) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the current page index.
        int currentPageIndex = webBackForwardList.getCurrentIndex();

        // Setup the URL array list and the icon array list.
        ArrayList<String> urlArrayList = new ArrayList<>();
        ArrayList<String> iconBase64StringArrayList = new ArrayList<>();

        // Get the default favorite icon drawable.  `ContextCompat` must be used until the minimum API >= 21.
        Drawable defaultFavoriteIconDrawable = ContextCompat.getDrawable(context, R.drawable.world);

        // Convert the default favorite icon drawable to a `BitmapDrawable`.
        BitmapDrawable defaultFavoriteIconBitmapDrawable = (BitmapDrawable) defaultFavoriteIconDrawable;

        // Remove the incorrect lint error that `getBitmap()` might be null.
        assert defaultFavoriteIconBitmapDrawable != null;

        // Extract a `Bitmap` from the default favorite icon `BitmapDrawable`.
        Bitmap defaultFavoriteIcon = defaultFavoriteIconBitmapDrawable.getBitmap();

        // Populate the URL array list and the icon array list from `webBackForwardList`.
        for (int i=0; i < webBackForwardList.getSize(); i++) {
            // Store the URL.
            urlArrayList.add(webBackForwardList.getItemAtIndex(i).getUrl());

            // Create a variable to store the icon bitmap.
            Bitmap iconBitmap;

            // Store the icon bitmap.
            if (webBackForwardList.getItemAtIndex(i).getFavicon() == null) {
                // If `webBackForwardList` does not have a favorite icon, use Privacy Browser's default world icon.
                iconBitmap = defaultFavoriteIcon;
            } else {  // Get the icon from `webBackForwardList`.
                iconBitmap = webBackForwardList.getItemAtIndex(i).getFavicon();
            }

            // Create a `ByteArrayOutputStream`.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // Remove the incorrect lint error that `compress()` might be null;
            assert iconBitmap != null;

            // Convert the favorite icon `Bitmap` to a `ByteArrayOutputStream`.  `100` is the compression quality, which is ignored by `PNG`.
            iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

            // Convert the favorite icon `ByteArrayOutputStream` to a `byte[]`.
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Encode the favorite icon `byte[]` as a Base64 `String`.
            String iconBase64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Store the favorite icon Base64 `String` in `iconBase64StringArrayList`.
            iconBase64StringArrayList.add(iconBase64String);
        }

        // Store the variables in the `Bundle`.
        argumentsBundle.putInt("Current_Page", currentPageIndex);
        argumentsBundle.putStringArrayList("URL_History", urlArrayList);
        argumentsBundle.putStringArrayList("Favorite_Icons", iconBase64StringArrayList);

        // Add the arguments bundle to this instance of `UrlHistoryDialog`.
        UrlHistoryDialog thisUrlHistoryDialog = new UrlHistoryDialog();
        thisUrlHistoryDialog.setArguments(argumentsBundle);
        return thisUrlHistoryDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the incorrect lint error that `getArguments()` might be null.
        assert getArguments() != null;

        // Get the `ArrayLists` from the `Arguments`.
        ArrayList<String> urlStringArrayList = getArguments().getStringArrayList("URL_History");
        ArrayList<String> favoriteIconBase64StringArrayList = getArguments().getStringArrayList("Favorite_Icons");

        // Remove the lint warning below that the `ArrayLists` might be `null`.
        assert urlStringArrayList != null;
        assert favoriteIconBase64StringArrayList != null;

        // Populate `historyArrayList`.  We go down from `urlStringArrayList.size()` so that the newest entries are at the top.  `-1` is needed because `historyArrayList` is zero-based.
        for (int i=urlStringArrayList.size() -1; i >= 0; i--) {
            // Decode the favorite icon Base64 `String` to a `byte[]`.
            byte[] favoriteIconByteArray = Base64.decode(favoriteIconBase64StringArrayList.get(i), Base64.DEFAULT);

            // Convert the favorite icon `byte[]` to a `Bitmap`.  `0` is the starting offset.
            Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

            // Store the favorite icon and the URL in `historyEntry`.
            History historyEntry = new History(favoriteIconBitmap, urlStringArrayList.get(i));

            // Add this history entry to `historyArrayList`.
            historyArrayList.add(historyEntry);
        }

        // Get the original current page ID.
        int originalCurrentPageId = getArguments().getInt("Current_Page");

        // Subtract `originalCurrentPageId` from the array size because we reversed the order of the array so that the newest entries are at the top.  `-1` is needed because the array is zero-based.
        currentPageId = urlStringArrayList.size() - 1 - originalCurrentPageId;
    }

    @Override
    @NonNull
    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.history);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.url_history_dialog, null));

        // Set an `onClick()` listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.clear_history, (DialogInterface dialog, int which) -> {
            // Clear the history.
            urlHistoryListener.onClearHistory();
        });

        // Set an `onClick()` listener on the positive button.
        dialogBuilder.setPositiveButton(R.string.close, (DialogInterface dialog, int which) -> {
            // Do nothing if `Close` is clicked.  The `Dialog` will automatically close.
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        //The alert dialog must be shown before the contents can be modified.
        alertDialog.show();

        // Instantiate a `HistoryArrayAdapter`.
        HistoryArrayAdapter historyArrayAdapter = new HistoryArrayAdapter(getContext(), historyArrayList, currentPageId);

        // Get a handle for the list view.
        ListView listView = alertDialog.findViewById(R.id.history_listview);

        // Set the list view adapter.
        listView.setAdapter(historyArrayAdapter);

        // Listen for clicks on entries in the list view.
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Convert the long ID to an int.
            int itemId = (int) id;

            // Only consume the click if it is not on the `currentPageId`.
            if (itemId != currentPageId) {
                // Go forward or back to `itemId`.
                urlHistoryListener.onUrlHistoryEntrySelected(currentPageId - itemId);

                // Dismiss the `Dialog`.
                alertDialog.dismiss();
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}