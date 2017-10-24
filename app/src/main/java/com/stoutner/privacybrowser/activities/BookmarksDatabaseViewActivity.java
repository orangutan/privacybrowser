/*
 * Copyright © 2016-2017 Soren Stoutner <soren@stoutner.com>.
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
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

public class BookmarksDatabaseViewActivity extends AppCompatActivity {
    // `bookmarksDatabaseHelper` is used in `onCreate()` and `updateBookmarksListView()`.
    private BookmarksDatabaseHelper bookmarksDatabaseHelper;

    // `bookmarksListView` is used in `onCreate()` and `updateBookmarksListView()`.
    private ListView bookmarksListView;

    // `bookmarksCursorAdapter` is used in `onCreate()` and `updateBookmarksListView()`.
    private CursorAdapter bookmarksCursorAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Set the activity theme.
        if (MainWebViewActivity.darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarks_databaseview_coordinatorlayout);

        // We need to use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        final Toolbar bookmarksDatabaseViewAppBar = (Toolbar) findViewById(R.id.bookmarks_database_view_toolbar);
        setSupportActionBar(bookmarksDatabaseViewAppBar);

        // Get a handle for the `AppBar`.
        final ActionBar appBar = getSupportActionBar();

        // Remove the incorrect warning in Android Studio that `appBar` might be null.
        assert appBar != null;

        // Display the `Spinner` and the back arrow in the `AppBar`.
        appBar.setCustomView(R.layout.bookmarks_databaseview_spinner);
        appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);

        // Initialize the database handler.  `this` specifies the context.  The two `null`s do not specify the database name or a `CursorFactory`.  The `0` is to specify a database version, but that is set instead using a constant in `BookmarksDatabaseHelper`.
        bookmarksDatabaseHelper = new BookmarksDatabaseHelper(this, null, null, 0);

        // Setup a `MatrixCursor` for "All Folders" and "Home Folder".
        String[] matrixCursorColumnNames = {BookmarksDatabaseHelper._ID, BookmarksDatabaseHelper.BOOKMARK_NAME};
        MatrixCursor matrixCursor = new MatrixCursor(matrixCursorColumnNames);
        matrixCursor.addRow(new Object[]{-2, getString(R.string.all_folders)});
        matrixCursor.addRow(new Object[]{-1, getString(R.string.home_folder)});

        // Get a `Cursor` with the list of all the folders.
        Cursor foldersCursor = bookmarksDatabaseHelper.getAllFoldersCursor();

        // Combine `matrixCursor` and `foldersCursor`.
        MergeCursor foldersMergeCursor = new MergeCursor(new Cursor[]{matrixCursor, foldersCursor});

        // Create a `ResourceCursorAdapter` for the spinner with `this` context.  `0` specifies no flags.;
        ResourceCursorAdapter foldersCursorAdapter = new ResourceCursorAdapter(this, R.layout.bookmarks_databaseview_spinner_item, foldersMergeCursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get a handle for the spinner item `TextView`.
                TextView spinnerItemTextView = (TextView) view.findViewById(R.id.spinner_item_textview);

                // Set the `TextView` to display the folder name.
                spinnerItemTextView.setText(cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME)));
            }
        };

        // Set the `ResourceCursorAdapter` drop drown view resource.
        foldersCursorAdapter.setDropDownViewResource(R.layout.bookmarks_databaseview_spinner_dropdown_item);

        // Get a handle for the folder `Spinner`.
        Spinner folderSpinner = (Spinner) findViewById(R.id.bookmarks_database_view_spinner);

        // Set the adapter for the folder `Spinner`.
        folderSpinner.setAdapter(foldersCursorAdapter);

        // Handle clicks on the `Spinner` dropdown.
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Convert the database ID to an `int`.
                int databaseId = (int) id;

                // Instantiate the bookmarks `Cursor`.
                Cursor bookmarksCursor;

                // Populate the bookmarks `ListView` based on the `Spinner` selection.
                switch (databaseId) {
                    // Display all the folders.
                    case -2:
                        // Get a cursor with all the folders.
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor();
                        break;

                    // Display the home folder.
                    case -1:
                        // Get a cursor for the home folder.
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor("");
                        break;

                    // Display the selected folder.
                    default:
                        // Get a handle for the selected view.
                        TextView selectedFolderTextView = (TextView) view.findViewById(R.id.spinner_item_textview);

                        // Extract the name of the selected folder.
                        String folderName = selectedFolderTextView.getText().toString();

                        // Get a cursor for the selected folder.
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor(folderName);
                }

                // Update the `ListView`.
                bookmarksCursorAdapter.changeCursor(bookmarksCursor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // Get a handle for the bookmarks `ListView`.
        bookmarksListView = (ListView) findViewById(R.id.bookmarks_database_view_listview);

        // Display the bookmarks in the `ListView`.
        updateBookmarksListView();
    }

    private void updateBookmarksListView() {
        // Get a `Cursor` with the current contents of the bookmarks database.
        final Cursor bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor();

        // Setup `bookmarksCursorAdapter` with `this` context.  The `false` disables autoRequery.
        bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_databaseview_item_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                boolean isFolder = (cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1);

                // Get the database ID from the `Cursor` and display it in `bookmarkDatabaseIdTextView`.
                int bookmarkDatabaseId = cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper._ID));
                TextView bookmarkDatabaseIdTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_database_id);
                bookmarkDatabaseIdTextView.setText(String.valueOf(bookmarkDatabaseId));

                // Get the favorite icon byte array from the `Cursor`.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));
                // Convert the byte array to a `Bitmap` beginning at the beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);
                // Display the bitmap in `bookmarkFavoriteIcon`.
                ImageView bookmarkFavoriteIcon = (ImageView) view.findViewById(R.id.bookmarks_database_view_favorite_icon);
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);

                // Get the bookmark name from the `Cursor` and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
                TextView bookmarkNameTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_bookmark_name);
                bookmarkNameTextView.setText(bookmarkNameString);
                // Make the font bold for folders.
                if (isFolder) {
                    // The first argument is `null` because we don't want to change the font.
                    bookmarkNameTextView.setTypeface(null, Typeface.BOLD);
                } else {  // Reset the font to default.
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT);
                }

                // Get the display order from the `Cursor` and display it in `bookmarkDisplayOrderTextView`.
                int bookmarkDisplayOrder = cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER));
                TextView bookmarkDisplayOrderTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_display_order);
                bookmarkDisplayOrderTextView.setText(String.valueOf(bookmarkDisplayOrder));

                // Get the parent folder from the `Cursor` and display it in `bookmarkParentFolder`.
                String bookmarkParentFolder = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER));
                ImageView parentFolderImageView = (ImageView) view.findViewById(R.id.bookmarks_database_view_parent_folder_icon);
                TextView bookmarkParentFolderTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_parent_folder);
                // Make the folder name gray if it is the home folder.
                if (bookmarkParentFolder.isEmpty()) {
                    parentFolderImageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.folder_gray));
                    bookmarkParentFolderTextView.setText(R.string.home_folder);
                    bookmarkParentFolderTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_500));
                } else {
                    parentFolderImageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.folder_dark_blue));
                    bookmarkParentFolderTextView.setText(bookmarkParentFolder);

                    // Set the text color according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        bookmarkParentFolderTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_300));
                    } else {
                        bookmarkParentFolderTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
                    }
                }

                // Get the bookmark URL form the `Cursor` and display it in `bookmarkUrlTextView`.
                String bookmarkUrlString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL));
                TextView bookmarkUrlTextView = (TextView) view.findViewById(R.id.bookmarks_database_view_bookmark_url);
                bookmarkUrlTextView.setText(bookmarkUrlString);
                if (isFolder) {
                    bookmarkUrlTextView.setVisibility(View.GONE);
                } else {
                    bookmarkUrlTextView.setVisibility(View.VISIBLE);
                }
            }
        };

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);
    }
}