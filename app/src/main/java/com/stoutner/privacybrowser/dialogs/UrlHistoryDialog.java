/*
 * Copyright 2016-2017 Soren Stoutner <soren@stoutner.com>.
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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
// We have to use `AppCompatDialogFragment` instead of `DialogFragment` or an error is produced on API <= 22.  `android.support.v7.app.AlertDialog` also uses more of the horizontal screen real estate versus `android.app.AlertDialog's` smaller width.
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.widget.AdapterView;
import android.widget.ListView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.adapters.HistoryArrayAdapter;
import com.stoutner.privacybrowser.definitions.History;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class UrlHistoryDialog extends AppCompatDialogFragment{

    // `historyArrayList`  and `currentPageId` pass information from `onCreate()` to `onCreateDialog()`.
    private ArrayList<History> historyArrayList = new ArrayList<>();
    private int currentPageId;

    public static UrlHistoryDialog loadBackForwardList(Context context, WebBackForwardList webBackForwardList) {
        // Create `argumentsBundle`.
        Bundle argumentsBundle = new Bundle();

        // Store `currentPageIndex`.
        int currentPageIndex = webBackForwardList.getCurrentIndex();

        // Setup `urlArrayList` and `iconArrayList`.
        ArrayList<String> urlArrayList = new ArrayList<>();
        ArrayList<String> iconBase64StringArrayList = new ArrayList<>();

        // Get the default favorite icon `Drawable`.
        Drawable defaultFavoriteIconDrawable = ContextCompat.getDrawable(context, R.drawable.world);

        // Convert `defaultFavoriteIconDrawable` to a `BitmapDrawable`.
        BitmapDrawable defaultFavoriteIconBitmapDrawable = (BitmapDrawable) defaultFavoriteIconDrawable;

        // Extract a `Bitmap` from `defaultFavoriteIconBitmapDrawable`.
        Bitmap defaultFavoriteIcon = defaultFavoriteIconBitmapDrawable.getBitmap();

        // Populate `urlArrayList` and `iconArrayList` from `webBackForwardList`.
        for (int i=0; i < webBackForwardList.getSize(); i++) {
            // Store the URL.
            urlArrayList.add(webBackForwardList.getItemAtIndex(i).getUrl());

            // Create a variable to store the icon `Bitmap`.
            Bitmap iconBitmap;

            // Store the icon `Bitmap`.
            if (webBackForwardList.getItemAtIndex(i).getFavicon() == null) {
                // If `webBackForwardList` does not have a favorite icon, use Privacy Browser's default world icon.
                iconBitmap = defaultFavoriteIcon;
            } else {  // Get the icon from `webBackForwardList`.
                iconBitmap = webBackForwardList.getItemAtIndex(i).getFavicon();
            }

            // Create a `ByteArrayOutputStream`.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

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

        // Add `argumentsBundle` to this instance of `UrlHistoryDialog`.
        UrlHistoryDialog thisUrlHistoryDialog = new UrlHistoryDialog();
        thisUrlHistoryDialog.setArguments(argumentsBundle);
        return thisUrlHistoryDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    // The public interface is used to send information back to the parent activity.
    public interface UrlHistoryListener {
        // Send back the number of steps to move forward or back.
        void onUrlHistoryEntrySelected(int moveBackOrForwardSteps);

        // Clear the history.
        void onClearHistory();
    }

    // `urlHistoryListener` is used in `onAttach()` and `onCreateDialog()`.
    private UrlHistoryListener urlHistoryListener;

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

    @Override
    @NonNull
    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use `AlertDialog.Builder` to create the `AlertDialog`.  `R.style.lightAlertDialog` formats the color of the button text.
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.LightAlertDialog);

        // Set the title.
        dialogBuilder.setTitle(R.string.history);

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.url_history_dialog, null));

        // Set an `onClick()` listener on the negative button.
        dialogBuilder.setNegativeButton(R.string.clear_history, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Clear the history.
                urlHistoryListener.onClearHistory();
            }
        });

        // Set an `onClick()` listener on the positive button.
        dialogBuilder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing if `Close` is clicked.  The `Dialog` will automatically close.
            }
        });

        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // We need to show `alertDialog` before we can modify the contents.
        alertDialog.show();

        // Instantiate a `HistoryArrayAdapter`.
        final HistoryArrayAdapter historyArrayAdapter = new HistoryArrayAdapter(getContext(), historyArrayList, currentPageId);

        // Get a handle for `listView`.
        ListView listView = (ListView) alertDialog.findViewById(R.id.history_listview);

        // Remove the warning below that `listView` might be `null`.
        assert listView != null;

        // Set the adapter on `listView`.
        listView.setAdapter(historyArrayAdapter);

        // Listen for clicks on entries in `listView`.
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Convert the `long` `id` to an `int`.
                int itemId = (int) id;

                // Only enable the click if it is not on the `currentPageId`.
                if (itemId != currentPageId) {
                    // Get the history entry for this `itemId`.
                    History historyEntry = historyArrayAdapter.getItem(itemId);

                    // Remove the lint warning below that `historyEntry` might be `null`.
                    assert historyEntry != null;

                    // Send the history entry URL to be loaded in `mainWebView`.
                    urlHistoryListener.onUrlHistoryEntrySelected(currentPageId - itemId);

                    // Dismiss the `Dialog`.
                    alertDialog.dismiss();
                }
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}