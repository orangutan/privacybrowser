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

package com.stoutner.privacybrowser.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ResourceCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;  // The AndroidX toolbar must be used until the minimum API is >= 21.
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.google.android.material.snackbar.Snackbar;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.EditBookmarkDatabaseViewDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkFolderDatabaseViewDialog;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class BookmarksDatabaseViewActivity extends AppCompatActivity implements EditBookmarkDatabaseViewDialog.EditBookmarkDatabaseViewListener,
        EditBookmarkFolderDatabaseViewDialog.EditBookmarkFolderDatabaseViewListener {
    // Instantiate the constants.
    private static final int ALL_FOLDERS_DATABASE_ID = -2;
    private static final int HOME_FOLDER_DATABASE_ID = -1;

    // `bookmarksDatabaseHelper` is used in `onCreate()`, `updateBookmarksListView()`, `selectAllBookmarksInFolder()`, and `onDestroy()`.
    private BookmarksDatabaseHelper bookmarksDatabaseHelper;

    // `bookmarksCursor` is used in `onCreate()`, `updateBookmarksListView()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`, and `onDestroy()`.
    private Cursor bookmarksCursor;

    // `bookmarksCursorAdapter` is used in `onCreate()`, `selectAllBookmarksInFolder()`, and `updateBookmarksListView()`.
    private CursorAdapter bookmarksCursorAdapter;

    // `oldFolderNameString` is used in `onCreate()` and `onSaveBookmarkFolder()`.
    private String oldFolderNameString;

    // `currentFolderDatabaseId` is used in `onCreate()`, `updateBookmarksListView()`, `onSaveBookmark()`, and `onSaveBookmarkFolder()`.
    private int currentFolderDatabaseId;

    // `currentFolder` is used in `onCreate()`, `onSaveBookmark()`, and `onSaveBookmarkFolder()`.
    private String currentFolderName;

    // `sortByDisplayOrder` is used in `onCreate()`, `onOptionsItemSelected()`, and `updateBookmarksListView()`.
    private boolean sortByDisplayOrder;

    // `bookmarksDeletedSnackbar` is used in `onCreate()`, `onOptionsItemSelected()`, and `onBackPressed()`.
    private Snackbar bookmarksDeletedSnackbar;

    // `closeActivityAfterDismissingSnackbar` is used in `onCreate()`, `onOptionsItemSelected()`, and `onBackPressed()`.
    private boolean closeActivityAfterDismissingSnackbar;

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

        // Get the intent that launched the activity.
        Intent launchingIntent = getIntent();

        // Get the favorite icon byte array.
        byte[] favoriteIconByteArray = launchingIntent.getByteArrayExtra("favorite_icon_byte_array");

        // Remove the incorrect lint warning below that the favorite icon byte array might be null.
        assert favoriteIconByteArray != null;

        // Convert the favorite icon byte array to a bitmap and store it in a class variable.
        Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

        // Set the content view.
        setContentView(R.layout.bookmarks_databaseview_coordinatorlayout);

        // The AndroidX toolbar must be used until the minimum API is >= 21.
        Toolbar toolbar = findViewById(R.id.bookmarks_databaseview_toolbar);
        setSupportActionBar(toolbar);

        // Get a handle for the action bar.
        ActionBar actionBar = getSupportActionBar();

        // Remove the incorrect lint warning that the action bar might be null.
        assert actionBar != null;

        // Display the spinner and the back arrow in the action bar.
        actionBar.setCustomView(R.layout.spinner);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);

        // Initialize the database handler.  The `0` is to specify a database version, but that is set instead using a constant in `BookmarksDatabaseHelper`.
        bookmarksDatabaseHelper = new BookmarksDatabaseHelper(this, null, null, 0);

        // Setup a matrix cursor for "All Folders" and "Home Folder".
        String[] matrixCursorColumnNames = {BookmarksDatabaseHelper._ID, BookmarksDatabaseHelper.BOOKMARK_NAME};
        MatrixCursor matrixCursor = new MatrixCursor(matrixCursorColumnNames);
        matrixCursor.addRow(new Object[]{ALL_FOLDERS_DATABASE_ID, getString(R.string.all_folders)});
        matrixCursor.addRow(new Object[]{HOME_FOLDER_DATABASE_ID, getString(R.string.home_folder)});

        // Get a cursor with the list of all the folders.
        Cursor foldersCursor = bookmarksDatabaseHelper.getAllFolders();

        // Combine `matrixCursor` and `foldersCursor`.
        MergeCursor foldersMergeCursor = new MergeCursor(new Cursor[]{matrixCursor, foldersCursor});


        // Get the default folder bitmap.  `ContextCompat` must be used until the minimum API >= 21.
        Drawable defaultFolderDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.folder_blue_bitmap);

        // Cast the default folder drawable to a `BitmapDrawable`.
        BitmapDrawable defaultFolderBitmapDrawable = (BitmapDrawable) defaultFolderDrawable;

        // Remove the incorrect lint warning that `.getBitmap()` might be null.
        assert defaultFolderBitmapDrawable != null;

        // Convert the default folder `BitmapDrawable` to a bitmap.
        Bitmap defaultFolderBitmap = defaultFolderBitmapDrawable.getBitmap();


        // Create a resource cursor adapter for the spinner.
        ResourceCursorAdapter foldersCursorAdapter = new ResourceCursorAdapter(this, R.layout.appbar_spinner_item, foldersMergeCursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get handles for the spinner views.
                ImageView spinnerItemImageView = view.findViewById(R.id.spinner_item_imageview);
                TextView spinnerItemTextView = view.findViewById(R.id.spinner_item_textview);

                // Set the folder icon according to the type.
                if (foldersMergeCursor.getPosition() > 1) {  // Set a user folder icon.
                    // Initialize a default folder icon byte array output stream.
                    ByteArrayOutputStream defaultFolderIconByteArrayOutputStream = new ByteArrayOutputStream();

                    // Covert the default folder bitmap to a PNG and store it in the output stream.  `0` is for lossless compression (the only option for a PNG).
                    defaultFolderBitmap.compress(Bitmap.CompressFormat.PNG, 0, defaultFolderIconByteArrayOutputStream);

                    // Convert the default folder icon output stream to a byte array.
                    byte[] defaultFolderIconByteArray = defaultFolderIconByteArrayOutputStream.toByteArray();


                    // Get the folder icon byte array from the cursor.
                    byte[] folderIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

                    // Convert the byte array to a bitmap beginning at the first byte and ending at the last.
                    Bitmap folderIconBitmap = BitmapFactory.decodeByteArray(folderIconByteArray, 0, folderIconByteArray.length);


                    // Set the icon according to the type.
                    if (Arrays.equals(folderIconByteArray, defaultFolderIconByteArray)) {  // The default folder icon is used.
                        // Set a smaller and darker folder icon, which works well with the spinner.
                        spinnerItemImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.folder_dark_blue));
                    } else {  // A custom folder icon is uses.
                        // Set the folder image stored in the cursor.
                        spinnerItemImageView.setImageBitmap(folderIconBitmap);
                    }
                } else {  // Set the `All Folders` or `Home Folder` icon.
                    // Set the gray folder image.  `ContextCompat` must be used until the minimum API >= 21.
                    spinnerItemImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.folder_gray));
                }

                // Set the text view to display the folder name.
                spinnerItemTextView.setText(cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME)));
            }
        };

        // Set the resource cursor adapter drop drown view resource.
        foldersCursorAdapter.setDropDownViewResource(R.layout.appbar_spinner_dropdown_item);

        // Get a handle for the folder spinner and set the adapter.
        Spinner folderSpinner = findViewById(R.id.spinner);
        folderSpinner.setAdapter(foldersCursorAdapter);

        // Handle taps on the spinner dropdown.
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the current folder database ID.
                currentFolderDatabaseId = (int) id;

                // Get a handle for the selected view.
                TextView selectedFolderTextView = findViewById(R.id.spinner_item_textview);

                // Store the current folder name.
                currentFolderName = selectedFolderTextView.getText().toString();

                // Update the list view.
                updateBookmarksListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });

        // Get a handle for the bookmarks `ListView`.
        ListView bookmarksListView = findViewById(R.id.bookmarks_databaseview_listview);

        // Get a `Cursor` with the current contents of the bookmarks database.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarks();

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
            // Convert the database ID to an int.
            int databaseId = (int) id;

            // Show the edit bookmark or edit bookmark folder dialog.
            if (bookmarksDatabaseHelper.isFolder(databaseId)) {
                // Save the current folder name, which is used in `onSaveBookmarkFolder()`.
                oldFolderNameString = bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                // Show the edit bookmark folder dialog.
                DialogFragment editBookmarkFolderDatabaseViewDialog = EditBookmarkFolderDatabaseViewDialog.folderDatabaseId(databaseId, favoriteIconBitmap);
                editBookmarkFolderDatabaseViewDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_folder));
            } else {
                // Show the edit bookmark dialog.
                DialogFragment editBookmarkDatabaseViewDialog = EditBookmarkDatabaseViewDialog.bookmarkDatabaseId(databaseId, favoriteIconBitmap);
                editBookmarkDatabaseViewDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_bookmark));
            }
        });

        // Handle long presses on the list view.
        bookmarksListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            // Instantiate the common variables.
            MenuItem selectAllMenuItem;
            MenuItem deleteMenuItem;
            boolean deletingBookmarks;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the contextual app bar.
                getMenuInflater().inflate(R.menu.bookmarks_databaseview_context_menu, menu);

                // Set the title.
                mode.setTitle(R.string.bookmarks);

                // Get handles for the menu items.
                selectAllMenuItem = menu.findItem(R.id.select_all);
                deleteMenuItem = menu.findItem(R.id.delete);

                // Disable the delete menu item if a delete is pending.
                deleteMenuItem.setEnabled(!deletingBookmarks);

                // Make it so.
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Do nothing.
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Calculate the number of selected bookmarks.
                int numberOfSelectedBookmarks = bookmarksListView.getCheckedItemCount();

                // Adjust the ActionMode according to the number of selected bookmarks.
                mode.setSubtitle(getString(R.string.selected) + "  " + numberOfSelectedBookmarks);

                // Do not show the select all menu item if all the bookmarks are already checked.
                if (bookmarksListView.getCheckedItemCount() == bookmarksListView.getCount()) {
                    selectAllMenuItem.setVisible(false);
                } else {
                    selectAllMenuItem.setVisible(true);
                }

                // Convert the database ID to an int.
                int databaseId = (int) id;

                // If a folder was selected, also select all the contents.
                if (checked && bookmarksDatabaseHelper.isFolder(databaseId)) {
                    selectAllBookmarksInFolder(databaseId);
                }

                // Do not allow a bookmark to be deselected if the folder is selected.
                if (!checked) {
                    // Get the folder name.
                    String folderName = bookmarksDatabaseHelper.getParentFolderName((int) id);

                    // If the bookmark is not in the root folder, check to see if the folder is selected.
                    if (!folderName.isEmpty()) {
                        // Get the database ID of the folder.
                        int folderDatabaseId = bookmarksDatabaseHelper.getFolderDatabaseId(folderName);

                        // Move the bookmarks cursor to the first position.
                        bookmarksCursor.moveToFirst();

                        // Initialize the folder position variable.
                        int folderPosition = -1;

                        // Get the position of the folder in the bookmarks cursor.
                        while ((folderPosition < 0) && (bookmarksCursor.getPosition() < bookmarksCursor.getCount())) {
                            // Check if the folder database ID matches the bookmark database ID.
                            if (folderDatabaseId == bookmarksCursor.getInt((bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper._ID)))) {
                                // Get the folder position.
                                folderPosition = bookmarksCursor.getPosition();

                                // Check if the folder is selected.
                                if (bookmarksListView.isItemChecked(folderPosition)) {
                                    // Reselect the bookmark.
                                    bookmarksListView.setItemChecked(position, true);

                                    // Display a snackbar explaining why the bookmark cannot be deselected.
                                    Snackbar.make(bookmarksListView, R.string.cannot_deselect_bookmark, Snackbar.LENGTH_LONG).show();
                                }
                            }

                            // Increment the bookmarks cursor.
                            bookmarksCursor.moveToNext();
                        }
                    }
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.select_all:
                        // Get the total number of bookmarks.
                        int numberOfBookmarks = bookmarksListView.getCount();

                        // Select them all.
                        for (int i = 0; i < numberOfBookmarks; i++) {
                            bookmarksListView.setItemChecked(i, true);
                        }
                        break;

                    case R.id.delete:
                        // Set the deleting bookmarks flag, which prevents the delete menu item from being enabled until the current process finishes.
                        deletingBookmarks = true;

                        // Get an array of the selected row IDs.
                        long[] selectedBookmarksIdsLongArray = bookmarksListView.getCheckedItemIds();

                        // Get an array of checked bookmarks.  `.clone()` makes a copy that won't change if the list view is reloaded, which is needed for re-selecting the bookmarks on undelete.
                        SparseBooleanArray selectedBookmarksPositionsSparseBooleanArray = bookmarksListView.getCheckedItemPositions().clone();

                        // Update the bookmarks cursor with the current contents of the bookmarks database except for the specified database IDs.
                        switch (currentFolderDatabaseId) {
                            // Get a cursor with all the folders.
                            case ALL_FOLDERS_DATABASE_ID:
                                if (sortByDisplayOrder) {
                                    bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksByDisplayOrderExcept(selectedBookmarksIdsLongArray);
                                } else {
                                    bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksExcept(selectedBookmarksIdsLongArray);
                                }
                                break;

                            // Get a cursor for the home folder.
                            case HOME_FOLDER_DATABASE_ID:
                                if (sortByDisplayOrder) {
                                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrderExcept(selectedBookmarksIdsLongArray, "");
                                } else {
                                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarksExcept(selectedBookmarksIdsLongArray, "");
                                }
                                break;

                            // Display the selected folder.
                            default:
                                // Get a cursor for the selected folder.
                                if (sortByDisplayOrder) {
                                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrderExcept(selectedBookmarksIdsLongArray, currentFolderName);
                                } else {
                                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarksExcept(selectedBookmarksIdsLongArray, currentFolderName);
                                }
                        }

                        // Update the list view.
                        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

                        // Show a Snackbar with the number of deleted bookmarks.
                        bookmarksDeletedSnackbar = Snackbar.make(findViewById(R.id.bookmarks_databaseview_coordinatorlayout),
                                getString(R.string.bookmarks_deleted) + "  " + selectedBookmarksIdsLongArray.length, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, view -> {
                                    // Do nothing because everything will be handled by `onDismissed()` below.
                                })
                                .addCallback(new Snackbar.Callback() {
                                    @SuppressLint("SwitchIntDef")  // Ignore the lint warning about not handling the other possible events as they are covered by `default:`.
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        switch (event) {
                                            // The user pushed the `Undo` button.
                                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                                // Update the bookmarks list view with the current contents of the bookmarks database, including the "deleted bookmarks.
                                                updateBookmarksListView();

                                                // Re-select the previously selected bookmarks.
                                                for (int i = 0; i < selectedBookmarksPositionsSparseBooleanArray.size(); i++) {
                                                    bookmarksListView.setItemChecked(selectedBookmarksPositionsSparseBooleanArray.keyAt(i), true);
                                                }
                                                break;

                                                // The Snackbar was dismissed without the `Undo` button being pushed.
                                            default:
                                                // Delete each selected bookmark.
                                                for (long databaseIdLong : selectedBookmarksIdsLongArray) {
                                                    // Convert `databaseIdLong` to an int.
                                                    int databaseIdInt = (int) databaseIdLong;

                                                    // Delete the selected bookmark.
                                                    bookmarksDatabaseHelper.deleteBookmark(databaseIdInt);
                                                }
                                        }

                                        // Reset the deleting bookmarks flag.
                                        deletingBookmarks = false;

                                        // Enable the delete menu item.
                                        deleteMenuItem.setEnabled(true);

                                        // Close the activity if back has been pressed.
                                        if (closeActivityAfterDismissingSnackbar) {
                                            onBackPressed();
                                        }
                                    }
                                });

                        // Show the Snackbar.
                        bookmarksDeletedSnackbar.show();
                        break;
                }

                // Consume the click.
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Do nothing.
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.bookmarks_databaseview_options_menu, menu);

        // Success.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the ID of the menu item that was selected.
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case android.R.id.home:  // The home arrow is identified as `android.R.id.home`, not just `R.id.home`.
                // Exit the activity.
                onBackPressed();
                break;

            case R.id.options_menu_sort:
                // Update the sort by display order tracker.
                sortByDisplayOrder = !sortByDisplayOrder;

                // Get a handle for the bookmarks `ListView`.
                ListView bookmarksListView = findViewById(R.id.bookmarks_databaseview_listview);

                // Update the icon and display a snackbar.
                if (sortByDisplayOrder) {  // Sort by display order.
                    // Update the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        menuItem.setIcon(R.drawable.sort_selected_dark);
                    } else {
                        menuItem.setIcon(R.drawable.sort_selected_light);
                    }

                    // Display a Snackbar indicating the current sort type.
                    Snackbar.make(bookmarksListView, R.string.sorted_by_display_order, Snackbar.LENGTH_SHORT).show();
                } else {  // Sort by database id.
                    // Update the icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        menuItem.setIcon(R.drawable.sort_dark);
                    } else {
                        menuItem.setIcon(R.drawable.sort_light);
                    }

                    // Display a Snackbar indicating the current sort type.
                    Snackbar.make(bookmarksListView, R.string.sorted_by_database_id, Snackbar.LENGTH_SHORT).show();
                }

                // Update the list view.
                updateBookmarksListView();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // Check to see if a snackbar is currently displayed.  If so, it must be closed before existing so that a pending delete is completed before reloading the list view in the bookmarks activity.
        if ((bookmarksDeletedSnackbar != null) && bookmarksDeletedSnackbar.isShown()) { // Close the bookmarks deleted snackbar before going home.
            // Set the close flag.
            closeActivityAfterDismissingSnackbar = true;

            // Dismiss the snackbar.
            bookmarksDeletedSnackbar.dismiss();
        } else {  // Go home immediately.
            // Update the current folder in the bookmarks activity.
            switch (currentFolderDatabaseId) {
                case ALL_FOLDERS_DATABASE_ID:
                    // Load the home folder.
                    BookmarksActivity.currentFolder = "";
                    break;

                case HOME_FOLDER_DATABASE_ID:
                    // Load the home folder.
                    BookmarksActivity.currentFolder = "";
                    break;

                default:
                    // Load the current folder.
                    BookmarksActivity.currentFolder = currentFolderName;
            }

            // Reload the bookmarks list view when returning to the bookmarks activity.
            BookmarksActivity.restartFromBookmarksDatabaseViewActivity = true;

            // Exit the bookmarks database view activity.
            super.onBackPressed();
        }
    }

    private void updateBookmarksListView() {
        // Populate the bookmarks list view based on the spinner selection.
        switch (currentFolderDatabaseId) {
            // Get a cursor with all the folders.
            case ALL_FOLDERS_DATABASE_ID:
                if (sortByDisplayOrder) {
                    bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksByDisplayOrder();
                } else {
                    bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarks();
                }
                break;

            // Get a cursor for the home folder.
            case HOME_FOLDER_DATABASE_ID:
                if (sortByDisplayOrder) {
                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrder("");
                } else {
                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarks("");
                }
                break;

            // Display the selected folder.
            default:
                // Get a cursor for the selected folder.
                if (sortByDisplayOrder) {
                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrder(currentFolderName);
                } else {
                    bookmarksCursor = bookmarksDatabaseHelper.getBookmarks(currentFolderName);
                }
        }

        // Update the list view.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }

    private void selectAllBookmarksInFolder(int folderId) {
        // Get a handle for the bookmarks list view.
        ListView bookmarksListView = findViewById(R.id.bookmarks_databaseview_listview);

        // Get the folder name.
        String folderName = bookmarksDatabaseHelper.getFolderName(folderId);

        // Get a cursor with the contents of the folder.
        Cursor folderCursor = bookmarksDatabaseHelper.getBookmarks(folderName);

        // Move to the beginning of the cursor.
        folderCursor.moveToFirst();

        while (folderCursor.getPosition() < folderCursor.getCount()) {
            // Get the bookmark database ID.
            int bookmarkId = folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper._ID));

            // Move the bookmarks cursor to the first position.
            bookmarksCursor.moveToFirst();

            // Initialize the bookmark position variable.
            int bookmarkPosition = -1;

            // Get the position of this bookmark in the bookmarks cursor.
            while ((bookmarkPosition < 0) && (bookmarksCursor.getPosition() < bookmarksCursor.getCount())) {
                // Check if the bookmark IDs match.
                if (bookmarkId == bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper._ID))) {
                    // Get the bookmark position.
                    bookmarkPosition = bookmarksCursor.getPosition();

                    // If this bookmark is a folder, select all the bookmarks inside it.
                    if (bookmarksDatabaseHelper.isFolder(bookmarkId)) {
                        selectAllBookmarksInFolder(bookmarkId);
                    }

                    // Select the bookmark.
                    bookmarksListView.setItemChecked(bookmarkPosition, true);
                }

                // Increment the bookmarks cursor position.
                bookmarksCursor.moveToNext();
            }

            // Move to the next position.
            folderCursor.moveToNext();
        }
    }

    @Override
    public void onSaveBookmark(DialogFragment dialogFragment, int selectedBookmarkDatabaseId, Bitmap favoriteIconBitmap) {
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
            // Create a favorite icon byte array output stream.
            ByteArrayOutputStream newFavoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

            // Convert the favorite icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
            favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFavoriteIconByteArrayOutputStream);

            // Convert the favorite icon byte array stream to a byte array.
            byte[] newFavoriteIconByteArray = newFavoriteIconByteArrayOutputStream.toByteArray();

            //  Update the bookmark and the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString, parentFolderNameString, displayOrderInt, newFavoriteIconByteArray);
        }

        // Update the list view.
        updateBookmarksListView();
    }

    @Override
    public void onSaveBookmarkFolder(DialogFragment dialogFragment, int selectedBookmarkDatabaseId, Bitmap favoriteIconBitmap) {
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
            // Create the new folder icon Bitmap.
            Bitmap folderIconBitmap;

            // Populate the new folder icon bitmap.
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon drawable.
                Drawable folderIconDrawable = defaultFolderIconImageView.getDrawable();

                // Convert the folder icon drawable to a bitmap drawable.
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;

                // Convert the folder icon bitmap drawable to a bitmap.
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Use the `WebView` favorite icon.
                // Get a copy of the favorite icon bitmap.
                folderIconBitmap = favoriteIconBitmap;
            }

            // Create a folder icon byte array output stream.
            ByteArrayOutputStream newFolderIconByteArrayOutputStream = new ByteArrayOutputStream();

            // Convert the folder icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFolderIconByteArrayOutputStream);

            // Convert the folder icon byte array stream to a byte array.
            byte[] newFolderIconByteArray = newFolderIconByteArrayOutputStream.toByteArray();

            //  Update the folder and the icon.
            bookmarksDatabaseHelper.updateFolder(selectedBookmarkDatabaseId, oldFolderNameString, newFolderNameString, parentFolderNameString, displayOrderInt, newFolderIconByteArray);
        }

        // Update the list view.
        updateBookmarksListView();
    }

    @Override
    public void onDestroy() {
        // Close the bookmarks cursor and database.
        bookmarksCursor.close();
        bookmarksDatabaseHelper.close();

        // Run the default commands.
        super.onDestroy();
    }
}