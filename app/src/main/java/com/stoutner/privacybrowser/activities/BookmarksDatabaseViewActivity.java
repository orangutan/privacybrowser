/*
 * Copyright © 2016-2018 Soren Stoutner <soren@stoutner.com>.
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
// `AppCompatDialogFragment` is required instead of `DialogFragment` or an error is produced on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.EditBookmarkDatabaseViewDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkFolderDatabaseViewDialog;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

import java.io.ByteArrayOutputStream;

public class BookmarksDatabaseViewActivity extends AppCompatActivity implements EditBookmarkDatabaseViewDialog.EditBookmarkDatabaseViewListener, EditBookmarkFolderDatabaseViewDialog.EditBookmarkFolderDatabaseViewListener {
    // Instantiate the constants.
    private static final int ALL_FOLDERS_DATABASE_ID = -2;
    private static final int HOME_FOLDER_DATABASE_ID = -1;

    // `bookmarksDatabaseHelper` is used in `onCreate()` and `updateBookmarksListView()`.
    private BookmarksDatabaseHelper bookmarksDatabaseHelper;

    // `bookmarksCursor` is used in `onCreate()`, `updateBookmarksListView()`, `onSaveBookmark()`, and `onSaveBookmarkFolder()`.
    private Cursor bookmarksCursor;

    // `bookmarksCursorAdapter` is used in `onCreate()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`.
    private CursorAdapter bookmarksCursorAdapter;

    // `oldFolderNameString` is used in `onCreate()` and `onSaveBookmarkFolder()`.
    private String oldFolderNameString;

    // `currentFolderDatabaseId` is used in `onCreate()`, `onSaveBookmark()`, and `onSaveBookmarkFolder()`.
    private int currentFolderDatabaseId;

    // `currentFolder` is used in `onCreate()`, `onSaveBookmark()`, and `onSaveBookmarkFolder()`.
    private String currentFolderName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the activity theme.
        if (MainWebViewActivity.darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.bookmarks_databaseview_coordinatorlayout);

        // The `SupportActionBar` from `android.support.v7.app.ActionBar` must be used until the minimum API is >= 21.
        final Toolbar bookmarksDatabaseViewAppBar = findViewById(R.id.bookmarks_databaseview_toolbar);
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
        matrixCursor.addRow(new Object[]{ALL_FOLDERS_DATABASE_ID, getString(R.string.all_folders)});
        matrixCursor.addRow(new Object[]{HOME_FOLDER_DATABASE_ID, getString(R.string.home_folder)});

        // Get a `Cursor` with the list of all the folders.
        Cursor foldersCursor = bookmarksDatabaseHelper.getAllFoldersCursor();

        // Combine `matrixCursor` and `foldersCursor`.
        MergeCursor foldersMergeCursor = new MergeCursor(new Cursor[]{matrixCursor, foldersCursor});

        // Create a `ResourceCursorAdapter` for the `Spinner` with `this` context.  `0` specifies no flags.;
        ResourceCursorAdapter foldersCursorAdapter = new ResourceCursorAdapter(this, R.layout.bookmarks_databaseview_spinner_item, foldersMergeCursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get a handle for the `Spinner` item `TextView`.
                TextView spinnerItemTextView = view.findViewById(R.id.spinner_item_textview);

                // Set the `TextView` to display the folder name.
                spinnerItemTextView.setText(cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME)));
            }
        };

        // Set the `ResourceCursorAdapter` drop drown view resource.
        foldersCursorAdapter.setDropDownViewResource(R.layout.bookmarks_databaseview_spinner_dropdown_item);

        // Get a handle for the folder `Spinner`.
        Spinner folderSpinner = findViewById(R.id.bookmarks_databaseview_spinner);

        // Set the adapter for the folder `Spinner`.
        folderSpinner.setAdapter(foldersCursorAdapter);

        // Handle clicks on the `Spinner` dropdown.
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Convert the database ID to an `int`.
                int databaseId = (int) id;

                // Store the current folder database ID.
                currentFolderDatabaseId = databaseId;

                // Populate the bookmarks `ListView` based on the `Spinner` selection.
                switch (databaseId) {
                    // Get a cursor with all the folders.
                    case ALL_FOLDERS_DATABASE_ID:
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor();
                        break;

                    // Get a cursor for the home folder.
                    case HOME_FOLDER_DATABASE_ID:
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor("");
                        break;

                    // Display the selected folder.
                    default:
                        // Get a handle for the selected view.
                        TextView selectedFolderTextView = view.findViewById(R.id.spinner_item_textview);

                        // Extract the name of the selected folder.
                        String folderName = selectedFolderTextView.getText().toString();

                        // Get a cursor for the selected folder.
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor(folderName);

                        // Store the current folder name.
                        currentFolderName = folderName;
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
        ListView bookmarksListView = findViewById(R.id.bookmarks_databaseview_listview);

        // Get a `Cursor` with the current contents of the bookmarks database.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor();

        // Setup a `CursorAdapter` with `this` context.  `false` disables autoRequery.
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
                TextView bookmarkDatabaseIdTextView = view.findViewById(R.id.bookmarks_databaseview_database_id);
                bookmarkDatabaseIdTextView.setText(String.valueOf(bookmarkDatabaseId));

                // Get the favorite icon byte array from the `Cursor`.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));
                // Convert the byte array to a `Bitmap` beginning at the beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);
                // Display the bitmap in `bookmarkFavoriteIcon`.
                ImageView bookmarkFavoriteIcon = view.findViewById(R.id.bookmarks_databaseview_favorite_icon);
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);

                // Get the bookmark name from the `Cursor` and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
                TextView bookmarkNameTextView = view.findViewById(R.id.bookmarks_databaseview_bookmark_name);
                bookmarkNameTextView.setText(bookmarkNameString);

                // Make the font bold for folders.
                if (isFolder) {
                    // The first argument is `null` prevent changing of the font.
                    bookmarkNameTextView.setTypeface(null, Typeface.BOLD);
                } else {  // Reset the font to default.
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT);
                }

                // Get the bookmark URL form the `Cursor` and display it in `bookmarkUrlTextView`.
                String bookmarkUrlString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL));
                TextView bookmarkUrlTextView = view.findViewById(R.id.bookmarks_databaseview_bookmark_url);
                bookmarkUrlTextView.setText(bookmarkUrlString);

                // Hide the URL if the bookmark is a folder.
                if (isFolder) {
                    bookmarkUrlTextView.setVisibility(View.GONE);
                } else {
                    bookmarkUrlTextView.setVisibility(View.VISIBLE);
                }

                // Get the display order from the `Cursor` and display it in `bookmarkDisplayOrderTextView`.
                int bookmarkDisplayOrder = cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER));
                TextView bookmarkDisplayOrderTextView = view.findViewById(R.id.bookmarks_databaseview_display_order);
                bookmarkDisplayOrderTextView.setText(String.valueOf(bookmarkDisplayOrder));

                // Get the parent folder from the `Cursor` and display it in `bookmarkParentFolder`.
                String bookmarkParentFolder = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER));
                ImageView parentFolderImageView = view.findViewById(R.id.bookmarks_databaseview_parent_folder_icon);
                TextView bookmarkParentFolderTextView = view.findViewById(R.id.bookmarks_databaseview_parent_folder);

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
            }
        };

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);

        // Set the current folder database ID.
        currentFolderDatabaseId = ALL_FOLDERS_DATABASE_ID;

        // Set a listener to edit a bookmark when it is tapped.
        bookmarksListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Convert the database ID to an `int`.
            int databaseId = (int) id;

            // Show the edit bookmark or edit bookmark folder dialog.
            if (bookmarksDatabaseHelper.isFolder(databaseId)) {
                // Save the current folder name, which is used in `onSaveBookmarkFolder()`.
                oldFolderNameString = bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                // Show the edit bookmark folder dialog.
                AppCompatDialogFragment editBookmarkFolderDatabaseViewDialog = EditBookmarkFolderDatabaseViewDialog.folderDatabaseId(databaseId);
                editBookmarkFolderDatabaseViewDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_folder));
            } else {
                // Show the edit bookmark dialog.
                AppCompatDialogFragment editBookmarkDatabaseViewDialog = EditBookmarkDatabaseViewDialog.bookmarkDatabaseId(databaseId);
                editBookmarkDatabaseViewDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_bookmark));
            }
        });
    }

    @Override
    public void onSaveBookmark(AppCompatDialogFragment dialogFragment, int selectedBookmarkDatabaseId) {
        // Get handles for the views from dialog fragment.
        RadioButton currentBookmarkIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_current_icon_radiobutton);
        EditText editBookmarkNameEditText = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_name_edittext);
        EditText editBookmarkUrlEditText = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_url_edittext);
        Spinner folderSpinner = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_folder_spinner);
        EditText displayOrderEditText = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_display_order_edittext);

        // Extract the bookmark information.
        String bookmarkNameString = editBookmarkNameEditText.getText().toString();
        String bookmarkUrlString = editBookmarkUrlEditText.getText().toString();
        int folderDatabaseId = (int) folderSpinner.getSelectedItemId();
        int displayOrderInt = Integer.valueOf(displayOrderEditText.getText().toString());

        // Instantiate the parent folder name `String`.
        String parentFolderNameString;

        // Set the parent folder name.
        if (folderDatabaseId == EditBookmarkDatabaseViewDialog.HOME_FOLDER_DATABASE_ID) {  // The home folder is selected.  Use `""`.
            parentFolderNameString = "";
        } else {  // Get the parent folder name from the database.
            parentFolderNameString = bookmarksDatabaseHelper.getFolderName(folderDatabaseId);
        }

        // Update the bookmark.
        if (currentBookmarkIconRadioButton.isChecked()) {  // Update the bookmark without changing the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString, parentFolderNameString, displayOrderInt);
        } else {  // Update the bookmark using the `WebView` favorite icon.
            // Convert the favorite icon to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream newFavoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
            MainWebViewActivity.favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFavoriteIconByteArrayOutputStream);
            byte[] newFavoriteIconByteArray = newFavoriteIconByteArrayOutputStream.toByteArray();

            //  Update the bookmark and the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString, parentFolderNameString, displayOrderInt, newFavoriteIconByteArray);
        }

        // Update `bookmarksCursor` with the contents of the current folder.
        switch (currentFolderDatabaseId) {
            case ALL_FOLDERS_DATABASE_ID:
                // Get a cursor with all the bookmarks.
                bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor();
                break;

            case HOME_FOLDER_DATABASE_ID:
                // Get a cursor with all the bookmarks in the home folder.
                bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor("");
                break;

            default:
                // Get a cursor with all the bookmarks in the current folder.
                bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor(currentFolderName);
        }

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }

    @Override
    public void onSaveBookmarkFolder(AppCompatDialogFragment dialogFragment, int selectedBookmarkDatabaseId) {
        // Get handles for the views from dialog fragment.
        RadioButton currentBookmarkIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_current_icon_radiobutton);
        RadioButton defaultFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_radiobutton);
        ImageView defaultFolderIconImageView = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_imageview);
        EditText editBookmarkNameEditText = dialogFragment.getDialog().findViewById(R.id.edit_folder_name_edittext);
        Spinner parentFolderSpinner = dialogFragment.getDialog().findViewById(R.id.edit_folder_parent_folder_spinner);
        EditText displayOrderEditText = dialogFragment.getDialog().findViewById(R.id.edit_folder_display_order_edittext);

        // Extract the folder information.
        String newFolderNameString = editBookmarkNameEditText.getText().toString();
        int parentFolderDatabaseId = (int) parentFolderSpinner.getSelectedItemId();
        int displayOrderInt = Integer.valueOf(displayOrderEditText.getText().toString());

        // Instantiate the parent folder name `String`.
        String parentFolderNameString;

        // Set the parent folder name.
        if (parentFolderDatabaseId == EditBookmarkFolderDatabaseViewDialog.HOME_FOLDER_DATABASE_ID) {  // The home folder is selected.  Use `""`.
            parentFolderNameString = "";
        } else {  // Get the parent folder name from the database.
            parentFolderNameString = bookmarksDatabaseHelper.getFolderName(parentFolderDatabaseId);
        }

        // Update the folder.
        if (currentBookmarkIconRadioButton.isChecked()) {  // Update the folder without changing the favorite icon.
            bookmarksDatabaseHelper.updateFolder(selectedBookmarkDatabaseId, oldFolderNameString, newFolderNameString, parentFolderNameString, displayOrderInt);
        } else {  // Update the folder and the icon.
            // Instantiate the new folder icon `Bitmap`.
            Bitmap folderIconBitmap;

            // Populate the new folder icon bitmap.
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon and convert it to a `Bitmap`.
                Drawable folderIconDrawable = defaultFolderIconImageView.getDrawable();
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Use the `WebView` favorite icon.
                folderIconBitmap = MainWebViewActivity.favoriteIconBitmap;
            }

            // Convert the folder icon to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream newFolderIconByteArrayOutputStream = new ByteArrayOutputStream();
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFolderIconByteArrayOutputStream);
            byte[] newFolderIconByteArray = newFolderIconByteArrayOutputStream.toByteArray();

            //  Update the folder and the icon.
            bookmarksDatabaseHelper.updateFolder(selectedBookmarkDatabaseId, oldFolderNameString, newFolderNameString, parentFolderNameString, displayOrderInt, newFolderIconByteArray);
        }

        // Update `bookmarksCursor` with the contents of the current folder.
        switch (currentFolderDatabaseId) {
            case ALL_FOLDERS_DATABASE_ID:
                // Get a cursor with all the bookmarks.
                bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor();
                break;

            case HOME_FOLDER_DATABASE_ID:
                // Get a cursor with all the bookmarks in the home folder.
                bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor("");
                break;

            default:
                // Get a cursor with all the bookmarks in the current folder.
                bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor(currentFolderName);
        }

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }
}