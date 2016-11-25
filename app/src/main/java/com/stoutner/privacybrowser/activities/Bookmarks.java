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

package com.stoutner.privacybrowser.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.stoutner.privacybrowser.dialogs.EditBookmark;
import com.stoutner.privacybrowser.dialogs.EditBookmarkFolder;
import com.stoutner.privacybrowser.dialogs.MoveToFolder;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;
import com.stoutner.privacybrowser.dialogs.CreateBookmark;
import com.stoutner.privacybrowser.dialogs.CreateBookmarkFolder;

import java.io.ByteArrayOutputStream;

public class Bookmarks extends AppCompatActivity implements CreateBookmark.CreateBookmarkListener,
        CreateBookmarkFolder.CreateBookmarkFolderListener, EditBookmark.EditBookmarkListener,
        EditBookmarkFolder.EditBookmarkFolderListener, MoveToFolder.MoveToFolderListener {

    // `bookmarksDatabaseHelper` is public static so it can be accessed from `EditBookmark` and `MoveToFolder`.  It is also used in `onCreate()`,
    // `onCreateBookmarkCreate()`, `updateBookmarksListView()`, and `updateBookmarksListViewExcept()`.
    public static BookmarksDatabaseHelper bookmarksDatabaseHelper;

    // `currentFolder` is public static so it can be accessed from `MoveToFolder`.
    // It is used in `onCreate`, `onOptionsItemSelected()`, `onCreateBookmarkCreate`, `onCreateBookmarkFolderCreate`, and `onEditBookmarkSave`.
    public static String currentFolder;

    // `checkedItemIds` is public static so it can be accessed from `EditBookmark`, `EditBookmarkFolder`, and `MoveToFolder`.
    // It is also used in `onActionItemClicked`.
    public static long[] checkedItemIds;


    // `bookmarksListView` is used in `onCreate()`, `updateBookmarksListView()`, and `updateBookmarksListViewExcept()`.
    private ListView bookmarksListView;

    // `contextualActionMode` is used in `onCreate()` and `onEditBookmarkSave()`.
    private ActionMode contextualActionMode;

    // `selectedBookmarkPosition` is used in `onCreate()` and `onEditBookmarkSave()`.
    private int selectedBookmarkPosition;

    // `appBar` is used in `onCreate()` and `updateBookmarksListView()`.
    private ActionBar appBar;

    // `bookmarksCursor` is used in `onCreate()`, `updateBookmarksListView()`, and `updateBookmarksListViewExcept()`.
    private Cursor bookmarksCursor;

    // `oldFolderName` is used in `onCreate()` and `onEditBookmarkFolderSave()`.
    private String oldFolderNameString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarks_coordinatorlayout);

        // We need to use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        final Toolbar bookmarksAppBar = (Toolbar) findViewById(R.id.bookmarks_toolbar);
        setSupportActionBar(bookmarksAppBar);

        // Display the home arrow on `SupportActionBar`.
        appBar = getSupportActionBar();
        assert appBar != null;// This assert removes the incorrect warning in Android Studio on the following line that `appBar` might be null.
        appBar.setDisplayHomeAsUpEnabled(true);


        // Initialize the database handler and the ListView.  `this` specifies the context.  The two `null`s do not specify the database name or a `CursorFactory`.
        // The `0` is to specify a database version, but that is set instead using a constant in `BookmarksDatabaseHelper`.
        bookmarksDatabaseHelper = new BookmarksDatabaseHelper(this, null, null, 0);
        bookmarksListView = (ListView) findViewById(R.id.bookmarks_listview);

        // Set currentFolder to the home folder, which is null in the database.
        currentFolder = "";

        // Display the bookmarks in the ListView.
        updateBookmarksListView(currentFolder);

        // Set a listener so that tapping a list item loads the URL.  We need to store the activity in a variable so that we can return to the parent activity after loading the URL.
        final Activity bookmarksActivity = this;
        bookmarksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Convert the id from long to int to match the format of the bookmarks database.
                int databaseID = (int) id;

                // Get the bookmark `Cursor` and move it to the first row.
                Cursor bookmarkCursor = bookmarksDatabaseHelper.getBookmarkCursor(databaseID);
                bookmarkCursor.moveToFirst();

                // If the bookmark is a folder load its contents into the ListView.
                if (bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1) {
                    // Update `currentFolder`.
                    currentFolder = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                    // Reload the ListView with `currentFolder`.
                    updateBookmarksListView(currentFolder);
                } else {  // Load the URL into `mainWebView`.
                    // Get the bookmark URL and assign it to formattedUrlString.  `mainWebView` will automatically reload when `Bookmarks` closes.
                    MainWebView.formattedUrlString = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL));

                    NavUtils.navigateUpFromSameTask(bookmarksActivity);
                }

                // Close the `Cursor`.
                bookmarkCursor.close();
            }
        });

        // `MultiChoiceModeListener` handles long clicks.
        bookmarksListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            // `moveBookmarkUpMenuItem` is used in `onCreateActionMode()` and `onItemCheckedStateChanged`.
            MenuItem moveBookmarkUpMenuItem;

            // `moveBookmarkDownMenuItem` is used in `onCreateActionMode()` and `onItemCheckedStateChanged`.
            MenuItem moveBookmarkDownMenuItem;

            // `editBookmarkMenuItem` is used in `onCreateActionMode()` and `onItemCheckedStateChanged`.
            MenuItem editBookmarkMenuItem;

            // `selectAllBookmarks` is used in `onCreateActionMode()` and `onItemCheckedStateChanges`.
            MenuItem selectAllBookmarksMenuItem;

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the contextual app bar and set the title.
                getMenuInflater().inflate(R.menu.bookmarks_context_menu, menu);

                // Set the title.
                if (currentFolder.isEmpty()) {
                    // Use `R.string.bookmarks` if we are in the home folder.
                    mode.setTitle(R.string.bookmarks);
                } else {  // Use the current folder name as the title.
                    mode.setTitle(currentFolder);
                }

                // Get a handle for MenuItems we need to selectively disable.
                moveBookmarkUpMenuItem = menu.findItem(R.id.move_bookmark_up);
                moveBookmarkDownMenuItem = menu.findItem(R.id.move_bookmark_down);
                editBookmarkMenuItem = menu.findItem(R.id.edit_bookmark);
                selectAllBookmarksMenuItem = menu.findItem(R.id.context_menu_select_all_bookmarks);

                // Get a handle for `contextualActionMode` so we can close it programatically.
                contextualActionMode = mode;

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Get an array of the selected bookmarks.
                long[] selectedBookmarksLongArray = bookmarksListView.getCheckedItemIds();

                // Calculate the number of selected bookmarks.
                int numberOfSelectedBookmarks = selectedBookmarksLongArray.length;

                // Sometimes Android forgets to close the contextual app bar when all the items are deselected.
                if (numberOfSelectedBookmarks == 0) {
                    mode.finish();
                }

                // List the number of selected bookmarks in the subtitle.
                mode.setSubtitle(numberOfSelectedBookmarks + " " + getString(R.string.selected));

                if (numberOfSelectedBookmarks == 1) {
                    // Show the `Move Up`, `Move Down`, and  `Edit` option only if 1 bookmark is selected.
                    moveBookmarkUpMenuItem.setVisible(true);
                    moveBookmarkDownMenuItem.setVisible(true);
                    editBookmarkMenuItem.setVisible(true);

                    // Get the database IDs for the bookmarks.
                    int selectedBookmarkDatabaseId = (int) selectedBookmarksLongArray[0];
                    int firstBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(0);
                    // bookmarksListView is 0 indexed.
                    int lastBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(bookmarksListView.getCount() - 1);

                    // Disable `moveBookmarkUpMenuItem` if the selected bookmark is at the top of the ListView.
                    if (selectedBookmarkDatabaseId == firstBookmarkDatabaseId) {
                        moveBookmarkUpMenuItem.setEnabled(false);
                        moveBookmarkUpMenuItem.setIcon(R.drawable.move_bookmark_up_disabled);
                    } else {  // Otherwise enable `moveBookmarkUpMenuItem`.
                        moveBookmarkUpMenuItem.setEnabled(true);
                        moveBookmarkUpMenuItem.setIcon(R.drawable.move_bookmark_up_enabled);
                    }

                    // Disable `moveBookmarkDownMenuItem` if the selected bookmark is at the bottom of the ListView.
                    if (selectedBookmarkDatabaseId == lastBookmarkDatabaseId) {
                        moveBookmarkDownMenuItem.setEnabled(false);
                        moveBookmarkDownMenuItem.setIcon(R.drawable.move_bookmark_down_disabled);
                    } else {  // Otherwise enable `moveBookmarkDownMenuItem`.
                        moveBookmarkDownMenuItem.setEnabled(true);
                        moveBookmarkDownMenuItem.setIcon(R.drawable.move_bookmark_down_enabled);
                    }
                } else {  // Hide the MenuItems because more than one bookmark is selected.
                    moveBookmarkUpMenuItem.setVisible(false);
                    moveBookmarkDownMenuItem.setVisible(false);
                    editBookmarkMenuItem.setVisible(false);
                }

                // Do not show `Select All` if all the bookmarks are already checked.
                if (bookmarksListView.getCheckedItemIds().length == bookmarksListView.getCount()) {
                    selectAllBookmarksMenuItem.setVisible(false);
                } else {
                    selectAllBookmarksMenuItem.setVisible(true);
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int menuItemId = item.getItemId();

                // `numberOfBookmarks` is used in `R.id.move_bookmark_up_enabled`, `R.id.move_bookmark_down_enabled`, and `R.id.context_menu_select_all_bookmarks`.
                int numberOfBookmarks;

                // `selectedBookmarkLongArray` is used in `R.id.move_bookmark_up`, `R.id.move_bookmark_down`, and `R.id.edit_bookmark`.
                long[]selectedBookmarkLongArray;
                // `selectedBookmarkDatabaseId` is used in `R.id.move_bookmark_up`, `R.id.move_bookmark_down`, and `R.id.edit_bookmark`.
                int selectedBookmarkDatabaseId;
                // `selectedBookmarkNewPosition` is used in `R.id.move_bookmark_up` and `R.id.move_bookmark_down`.
                int selectedBookmarkNewPosition;
                // `bookmarkPositionSparseBooleanArray` is used in `R.id.edit_bookmark` and `R.id.delete_bookmark`.
                SparseBooleanArray bookmarkPositionSparseBooleanArray;

                switch (menuItemId) {
                    case R.id.move_bookmark_up:
                        // Get the selected bookmark database ID.
                        selectedBookmarkLongArray = bookmarksListView.getCheckedItemIds();
                        selectedBookmarkDatabaseId = (int) selectedBookmarkLongArray[0];

                        // Initialize `selectedBookmarkNewPosition`.
                        selectedBookmarkNewPosition = 0;

                        for (int i = 0; i < bookmarksListView.getCount(); i++) {
                            int databaseId = (int) bookmarksListView.getItemIdAtPosition(i);
                            int nextBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(i + 1);

                            if (databaseId == selectedBookmarkDatabaseId || nextBookmarkDatabaseId == selectedBookmarkDatabaseId) {
                                if (databaseId == selectedBookmarkDatabaseId) {
                                    // Move the selected bookmark up one and store the new bookmark position.
                                    bookmarksDatabaseHelper.updateBookmarkDisplayOrder(databaseId, i - 1);
                                    selectedBookmarkNewPosition = i - 1;
                                } else {  // Move the bookmark above the selected bookmark down one.
                                    bookmarksDatabaseHelper.updateBookmarkDisplayOrder(databaseId, i + 1);
                                }
                            } else {
                                // Reset the rest of the bookmarks' DISPLAY_ORDER to match the position in the ListView.
                                // This isn't necessary, but it clears out any stray values that might have crept into the database.
                                bookmarksDatabaseHelper.updateBookmarkDisplayOrder(databaseId, i);
                            }
                        }

                        // Refresh the ListView.
                        updateBookmarksListView(currentFolder);

                        // Select the previously selected bookmark in the new location.
                        bookmarksListView.setItemChecked(selectedBookmarkNewPosition, true);

                        bookmarksListView.setSelection(selectedBookmarkNewPosition - 5);

                        break;

                    case R.id.move_bookmark_down:
                        // Get the selected bookmark database ID.
                        selectedBookmarkLongArray = bookmarksListView.getCheckedItemIds();
                        selectedBookmarkDatabaseId = (int) selectedBookmarkLongArray[0];

                        // Initialize `selectedBookmarkNewPosition`.
                        selectedBookmarkNewPosition = 0;

                        for (int i = 0; i <bookmarksListView.getCount(); i++) {
                            int databaseId = (int) bookmarksListView.getItemIdAtPosition(i);
                            int previousBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(i - 1);

                            if (databaseId == selectedBookmarkDatabaseId || previousBookmarkDatabaseId == selectedBookmarkDatabaseId) {
                                if (databaseId == selectedBookmarkDatabaseId) {
                                    // Move the selected bookmark down one and store the new bookmark position.
                                    bookmarksDatabaseHelper.updateBookmarkDisplayOrder(databaseId, i + 1);
                                    selectedBookmarkNewPosition = i + 1;
                                } else {  // Move the bookmark below the selected bookmark up one.
                                    bookmarksDatabaseHelper.updateBookmarkDisplayOrder(databaseId, i - 1);
                                }
                            } else {
                                // Reset the rest of the bookmark' DISPLAY_ORDER to match the position in the ListView.
                                // This isn't necessary, but it clears out any stray values that might have crept into the database.
                                bookmarksDatabaseHelper.updateBookmarkDisplayOrder(databaseId, i);
                            }
                        }

                        // Refresh the ListView.
                        updateBookmarksListView(currentFolder);

                        // Select the previously selected bookmark in the new location.
                        bookmarksListView.setItemChecked(selectedBookmarkNewPosition, true);

                        bookmarksListView.setSelection(selectedBookmarkNewPosition - 5);
                        break;

                    case R.id.move_to_folder:
                        // Store `checkedItemIds` for use by the `AlertDialog`.
                        checkedItemIds = bookmarksListView.getCheckedItemIds();

                        // Show the `MoveToFolder` `AlertDialog` and name the instance `@string/move_to_folder
                        AppCompatDialogFragment moveToFolderDialog = new MoveToFolder();
                        moveToFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.move_to_folder));
                        break;

                    case R.id.edit_bookmark:
                        // Get a handle for `selectedBookmarkPosition` so we can scroll to it after refreshing the ListView.
                        bookmarkPositionSparseBooleanArray = bookmarksListView.getCheckedItemPositions();
                        for (int i = 0; i < bookmarkPositionSparseBooleanArray.size(); i++) {
                            // Find the bookmark that is selected and save the position to `selectedBookmarkPosition`.
                            if (bookmarkPositionSparseBooleanArray.valueAt(i))
                                selectedBookmarkPosition = bookmarkPositionSparseBooleanArray.keyAt(i);
                        }

                        // Move to the selected database ID and find out if it is a folder.
                        bookmarksCursor.moveToPosition(selectedBookmarkPosition);
                        boolean isFolder = (bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1);

                        // Store `checkedItemIds` for use by the `AlertDialog`.
                        checkedItemIds = bookmarksListView.getCheckedItemIds();

                        if (isFolder) {
                            // Save the current folder name.
                            oldFolderNameString = bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                            // Show the `EditBookmarkFolder` `AlertDialog` and name the instance `@string/edit_folder`.
                            AppCompatDialogFragment editFolderDialog = new EditBookmarkFolder();
                            editFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_folder));
                        } else {
                            // Show the `EditBookmark` `AlertDialog` and name the instance `@string/edit_bookmark`.
                            AppCompatDialogFragment editBookmarkDialog = new EditBookmark();
                            editBookmarkDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_bookmark));
                        }
                        break;

                    case R.id.delete_bookmark:
                        // Get an array of the selected rows.
                        final long[] selectedBookmarksLongArray = bookmarksListView.getCheckedItemIds();

                        // Get a handle for `selectedBookmarkPosition` so we can scroll to it after refreshing the ListView.
                        bookmarkPositionSparseBooleanArray = bookmarksListView.getCheckedItemPositions();
                        for (int i = 0; i < bookmarkPositionSparseBooleanArray.size(); i++) {
                            // Find the bookmark that is selected and save the position to `selectedBookmarkPosition`.
                            if (bookmarkPositionSparseBooleanArray.valueAt(i))
                                selectedBookmarkPosition = bookmarkPositionSparseBooleanArray.keyAt(i);
                        }

                        updateBookmarksListViewExcept(selectedBookmarksLongArray, currentFolder);

                        // Scroll to where the deleted bookmark was located.
                        bookmarksListView.setSelection(selectedBookmarkPosition - 5);

                        String snackbarMessage;

                        // Determine how many items are in the array and prepare an appropriate Snackbar message.
                        if (selectedBookmarksLongArray.length == 1) {
                            snackbarMessage = getString(R.string.one_bookmark_deleted);
                        } else {
                            snackbarMessage = selectedBookmarksLongArray.length + " " + getString(R.string.bookmarks_deleted);
                        }

                        // Show a SnackBar.
                        Snackbar.make(findViewById(R.id.bookmarks_coordinatorlayout), snackbarMessage, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Do nothing because everything will be handled by `onDismissed()` below.
                                    }
                                })
                                .setCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        // Android Studio wants to see entries for every possible `Snackbar.Callback` even if they aren't used.
                                        switch (event) {
                                            // The user pushed the "Undo" button.
                                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                                // Refresh the ListView to show the rows again.
                                                updateBookmarksListView(currentFolder);

                                                // Scroll to where the deleted bookmark was located.
                                                bookmarksListView.setSelection(selectedBookmarkPosition - 5);

                                                break;

                                            case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                                                // Do nothing and let the default behavior run.

                                            case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                                                // Do nothing and let the default behavior run.

                                            case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                                                // Do nothing and let the default behavior run.

                                            case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                                // Do nothing and let the default behavior run.

                                            // The Snackbar was dismissed without the "Undo" button being pushed.
                                            default:
                                                // Delete each selected row.
                                                for (long databaseIdLong : selectedBookmarksLongArray) {
                                                    // Convert `databaseIdLong` to an int.
                                                    int databaseIdInt = (int) databaseIdLong;

                                                    if (bookmarksDatabaseHelper.isFolder(databaseIdInt)) {
                                                        deleteBookmarkFolderContents(databaseIdInt);
                                                    }

                                                    // Delete `databaseIdInt`.
                                                    bookmarksDatabaseHelper.deleteBookmark(databaseIdInt);
                                                }
                                                break;
                                        }
                                    }
                                })
                                .show();

                        // Close the contextual app bar.
                        mode.finish();
                        break;

                    case R.id.context_menu_select_all_bookmarks:
                        numberOfBookmarks = bookmarksListView.getCount();

                        for (int i = 0; i < numberOfBookmarks; i++) {
                            bookmarksListView.setItemChecked(i, true);
                        }
                        break;
                }
                // Consume the click.
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        // Set a FloatingActionButton for creating new bookmarks.
        FloatingActionButton createBookmarkFAB = (FloatingActionButton) findViewById(R.id.create_bookmark_fab);
        createBookmarkFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the `CreateBookmark` `AlertDialog` and name the instance `@string/create_bookmark`.
                AppCompatDialogFragment createBookmarkDialog = new CreateBookmark();
                createBookmarkDialog.show(getSupportFragmentManager(), getResources().getString(R.string.create_bookmark));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu.
        getMenuInflater().inflate(R.menu.bookmarks_options_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case android.R.id.home:
                if (currentFolder.isEmpty()) {  // Exit Bookmarks if currently in the home folder.
                    NavUtils.navigateUpFromSameTask(this);
                } else {  // Navigate up one folder.
                    // Place the former parent folder in `currentFolder`.
                    currentFolder = bookmarksDatabaseHelper.getParentFolder(currentFolder);

                    // Exit Bookmarks if currently in the home folder.
                    updateBookmarksListView(currentFolder);
                }
                break;

            case R.id.create_folder:
                // Show the `CreateBookmarkFolder` `AlertDialog` and name the instance `@string/create_folder`.
                AppCompatDialogFragment createBookmarkFolderDialog = new CreateBookmarkFolder();
                createBookmarkFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.create_folder));
                break;

            case R.id.options_menu_select_all_bookmarks:
                int numberOfBookmarks = bookmarksListView.getCount();

                for (int i = 0; i < numberOfBookmarks; i++) {
                    bookmarksListView.setItemChecked(i, true);
                }
                break;

            case R.id.bookmarks_database_view:
                // Launch `BookmarksDatabaseView`.
                Intent bookmarksDatabaseViewIntent = new Intent(this, BookmarksDatabaseView.class);
                startActivity(bookmarksDatabaseViewIntent);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (currentFolder.isEmpty()) {  // Exit Bookmarks if currently in the home folder.
            super.onBackPressed();
        } else {  // Navigate up one folder.
            // Place the former parent folder in `currentFolder`.
            currentFolder = bookmarksDatabaseHelper.getParentFolder(currentFolder);

            // Reload the `ListView`.
            updateBookmarksListView(currentFolder);
        }
    }

    @Override
    public void onCreateBookmark(AppCompatDialogFragment dialogFragment) {
        // Get the `EditText`s from the `createBookmarkDialogFragment` and extract the strings.
        EditText createBookmarkNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.create_bookmark_name_edittext);
        String bookmarkNameString = createBookmarkNameEditText.getText().toString();
        EditText createBookmarkUrlEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.create_bookmark_url_edittext);
        String bookmarkUrlString = createBookmarkUrlEditText.getText().toString();

        // Convert the favoriteIcon Bitmap to a byte array.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
        // `0` is for lossless compression (the only option for a PNG).
        MainWebView.favoriteIcon.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Display the new bookmark below the current items in the (0 indexed) list.
        int newBookmarkDisplayOrder = bookmarksListView.getCount();

        // Create the bookmark.
        bookmarksDatabaseHelper.createBookmark(bookmarkNameString, bookmarkUrlString, newBookmarkDisplayOrder, currentFolder, favoriteIconByteArray);

        // Refresh the ListView.  `setSelection` scrolls to the bottom of the list.
        updateBookmarksListView(currentFolder);
        bookmarksListView.setSelection(newBookmarkDisplayOrder);
    }

    @Override
    public void onCreateBookmarkFolder(AppCompatDialogFragment dialogFragment) {
        // Get `create_folder_name_edit_text` and extract the string.
        EditText createFolderNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.create_folder_name_edittext);
        String folderNameString = createFolderNameEditText.getText().toString();

        // Check to see if the folder already exists.
        Cursor bookmarkFolderCursor = bookmarksDatabaseHelper.getFolderCursor(folderNameString);
        int existingFoldersWithNewName = bookmarkFolderCursor.getCount();
        bookmarkFolderCursor.close();
        if (folderNameString.isEmpty() || (existingFoldersWithNewName > 0)) {
            String cannotCreateFolder = getResources().getString(R.string.cannot_create_folder) + " \"" + folderNameString + "\"";
            Snackbar.make(findViewById(R.id.bookmarks_coordinatorlayout), cannotCreateFolder, Snackbar.LENGTH_INDEFINITE).show();
        } else {  // Create the folder.
            // Get the new folder icon `Bitmap`.
            RadioButton defaultFolderIconRadioButton = (RadioButton) dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon_radiobutton);
            Bitmap folderIconBitmap;
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon `ImageView` from the `Dialog` and convert it to a `Bitmap`.
                ImageView folderIconImageView = (ImageView) dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon);
                Drawable folderIconDrawable = folderIconImageView.getDrawable();
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Assign `favoriteIcon` from the `WebView`.
                folderIconBitmap = MainWebView.favoriteIcon;
            }

            // Convert `folderIconBitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
            byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

            // Move all the bookmarks down one in the display order.
            for (int i = 0; i < bookmarksListView.getCount(); i++) {
                int databaseId = (int) bookmarksListView.getItemIdAtPosition(i);
                bookmarksDatabaseHelper.updateBookmarkDisplayOrder(databaseId, i + 1);
            }

            // Create the folder, placing it at the top of the ListView
            bookmarksDatabaseHelper.createFolder(folderNameString, 0, currentFolder, folderIconByteArray);

            // Refresh the ListView.
            updateBookmarksListView(currentFolder);
        }
    }

    @Override
    public void onSaveEditBookmark(AppCompatDialogFragment dialogFragment) {
        // Get a long array with the the databaseId of the selected bookmark and convert it to an `int`.
        long[] selectedBookmarksLongArray = bookmarksListView.getCheckedItemIds();
        int selectedBookmarkDatabaseId = (int) selectedBookmarksLongArray[0];

        // Get the `EditText`s from the `editBookmarkDialogFragment` and extract the strings.
        EditText editBookmarkNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.edit_bookmark_name_edittext);
        String bookmarkNameString = editBookmarkNameEditText.getText().toString();
        EditText editBookmarkUrlEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.edit_bookmark_url_edittext);
        String bookmarkUrlString = editBookmarkUrlEditText.getText().toString();

        // Get `edit_bookmark_current_icon_radiobutton`.
        RadioButton currentBookmarkIconRadioButton = (RadioButton) dialogFragment.getDialog().findViewById(R.id.edit_bookmark_current_icon_radiobutton);

        if (currentBookmarkIconRadioButton.isChecked()) {  // Update the bookmark without changing the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString);
        } else {  // Update the bookmark using the `WebView` favorite icon.
            ByteArrayOutputStream newFavoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
            MainWebView.favoriteIcon.compress(Bitmap.CompressFormat.PNG, 0, newFavoriteIconByteArrayOutputStream);
            byte[] newFavoriteIconByteArray = newFavoriteIconByteArrayOutputStream.toByteArray();

            //  Update the bookmark and the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString, newFavoriteIconByteArray);
        }

        // Close the contextual action mode.
        contextualActionMode.finish();

        // Refresh the `ListView`.  `setSelection` scrolls to the position of the bookmark that was edited.
        updateBookmarksListView(currentFolder);
        bookmarksListView.setSelection(selectedBookmarkPosition);
    }

    @Override
    public void onSaveEditBookmarkFolder(AppCompatDialogFragment dialogFragment) {
        // Get the new folder name.
        EditText editFolderNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.edit_folder_name_edittext);
        String newFolderNameString = editFolderNameEditText.getText().toString();

        // Check to see if the new folder name is unique.
        Cursor bookmarkFolderCursor = bookmarksDatabaseHelper.getFolderCursor(newFolderNameString);
        int existingFoldersWithNewName = bookmarkFolderCursor.getCount();
        bookmarkFolderCursor.close();
        if ( ((existingFoldersWithNewName == 0) || newFolderNameString.equals(oldFolderNameString)) && !newFolderNameString.isEmpty()) {
            // Get a long array with the the database ID of the selected folder and convert it to an `int`.
            long[] selectedFolderLongArray = bookmarksListView.getCheckedItemIds();
            int selectedFolderDatabaseId = (int) selectedFolderLongArray[0];

            // Get the `RadioButtons` from the `Dialog`.
            RadioButton currentFolderIconRadioButton = (RadioButton) dialogFragment.getDialog().findViewById(R.id.edit_folder_current_icon_radiobutton);
            RadioButton defaultFolderIconRadioButton = (RadioButton) dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_radiobutton);

            // Check if the favorite icon has changed.
            if (currentFolderIconRadioButton.isChecked()) {
                // Update the folder name if it has changed without modifying the favorite icon.
                if (!newFolderNameString.equals(oldFolderNameString)) {
                    bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString);

                    // Refresh the `ListView`.  `setSelection` scrolls to the position of the folder that was edited.
                    updateBookmarksListView(currentFolder);
                    bookmarksListView.setSelection(selectedBookmarkPosition);
                }
            } else {  // Update the folder icon.
                // Get the new folder icon `Bitmap`.
                Bitmap folderIconBitmap;
                if (defaultFolderIconRadioButton.isChecked()) {
                    // Get the default folder icon `ImageView` from the `Drawable` and convert it to a `Bitmap`.
                    ImageView folderIconImageView = (ImageView) dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon);
                    Drawable folderIconDrawable = folderIconImageView.getDrawable();
                    BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
                    folderIconBitmap = folderIconBitmapDrawable.getBitmap();
                } else {  // Get the web page icon `ImageView` from the `Dialog`.
                    folderIconBitmap = MainWebView.favoriteIcon;
                }

                // Convert the folder `Bitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
                ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
                folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
                byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

                bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString, folderIconByteArray);

                // Refresh the `ListView`.  `setSelection` scrolls to the position of the folder that was edited.
                updateBookmarksListView(currentFolder);
                bookmarksListView.setSelection(selectedBookmarkPosition);
            }
        } else {  // Don't edit the folder because the new name is not unique.
            String cannot_rename_folder = getResources().getString(R.string.cannot_rename_folder) + " \"" + newFolderNameString + "\"";
            Snackbar.make(findViewById(R.id.bookmarks_coordinatorlayout), cannot_rename_folder, Snackbar.LENGTH_INDEFINITE).show();
        }

        // Close the contextual action mode.
        contextualActionMode.finish();
    }

    @Override
    public void onMoveToFolder(AppCompatDialogFragment dialogFragment) {
        // Get the new folder database id.
        ListView folderListView = (ListView) dialogFragment.getDialog().findViewById(R.id.move_to_folder_listview);
        long[] newFolderLongArray = folderListView.getCheckedItemIds();

        if (newFolderLongArray.length == 0) {  // No new folder was selected.
            Snackbar.make(findViewById(R.id.bookmarks_coordinatorlayout), getString(R.string.cannot_move_bookmarks), Snackbar.LENGTH_LONG).show();
        } else {  // Move the selected bookmarks.
            // Get the new folder database ID.
            int newFolderDatabaseId = (int) newFolderLongArray[0];

            // Instantiate `newFolderName`.
            String newFolderName;

            if (newFolderDatabaseId == 0) {
                // The new folder is the home folder, represented as `""` in the database.
                newFolderName = "";
            } else {
                // Get the new folder name from the database.
                newFolderName = bookmarksDatabaseHelper.getFolderName(newFolderDatabaseId);
            }

            // Get a long array with the the database ID of the selected bookmarks.
            long[] selectedBookmarksLongArray = bookmarksListView.getCheckedItemIds();
            for (long databaseIdLong : selectedBookmarksLongArray) {
                // Get `databaseIdInt` for each selected bookmark.
                int databaseIdInt = (int) databaseIdLong;

                // Move the selected bookmark to the new folder.
                bookmarksDatabaseHelper.moveToFolder(databaseIdInt, newFolderName);
            }

            // Refresh the `ListView`.
            updateBookmarksListView(currentFolder);

            // Close the contextual app bar.
            contextualActionMode.finish();
        }
    }

    private void updateBookmarksListView(String folderName) {
        // Get a `Cursor` with the current contents of the bookmarks database.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(folderName);

        // Setup `bookmarksCursorAdapter` with `this` context.  `false` disables autoRequery.
        CursorAdapter bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_item_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get the favorite icon byte array from the `Cursor`.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

                // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

                // Display the bitmap in `bookmarkFavoriteIcon`.
                ImageView bookmarkFavoriteIcon = (ImageView) view.findViewById(R.id.bookmark_favorite_icon);
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);


                // Get the bookmark name from the cursor and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
                TextView bookmarkNameTextView = (TextView) view.findViewById(R.id.bookmark_name);
                bookmarkNameTextView.setText(bookmarkNameString);

                // Make the font bold for folders.
                if (cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1) {
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {  // Reset the font to default for normal bookmarks.
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT);
                }
            }
        };

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);

        // Set the AppBar title.
        if (currentFolder.isEmpty()) {
            appBar.setTitle(R.string.bookmarks);
        } else {
            appBar.setTitle(currentFolder);
        }
    }

    private void updateBookmarksListViewExcept(long[] exceptIdLongArray, String folderName) {
        // Get a `Cursor` with the current contents of the bookmarks database except for the specified database IDs.
        bookmarksCursor = bookmarksDatabaseHelper.getBookmarksCursorExcept(exceptIdLongArray, folderName);

        // Setup `bookmarksCursorAdapter` with `this` context.  `false` disables autoRequery.
        CursorAdapter bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_item_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get the favorite icon byte array from the cursor.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

                // Convert the byte array to a Bitmap beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

                // Display the bitmap in `bookmarkFavoriteIcon`.
                ImageView bookmarkFavoriteIcon = (ImageView) view.findViewById(R.id.bookmark_favorite_icon);
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);


                // Get the bookmark name from the cursor and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
                TextView bookmarkNameTextView = (TextView) view.findViewById(R.id.bookmark_name);
                bookmarkNameTextView.setText(bookmarkNameString);

                // Make the font bold for folders.
                if (cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1) {
                    // The first argument is `null` because we don't want to change the font.
                    bookmarkNameTextView.setTypeface(null, Typeface.BOLD);
                } else {  // Reset the font to default.
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT);
                }
            }
        };

        // Update the ListView.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);
    }

    private void deleteBookmarkFolderContents(int databaseId) {
        // Get the name of the folder.
        String folderName = bookmarksDatabaseHelper.getFolderName(databaseId);

        // Get the contents of the folder.
        Cursor folderCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(folderName);

        for (int i = 0; i < folderCursor.getCount(); i++) {
            // Move `folderCursor` to the current row.
            folderCursor.moveToPosition(i);

            // Get the database ID of the item.
            int itemDatabaseId = folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper._ID));

            // If this is a folder, delete the contents first.
            if (bookmarksDatabaseHelper.isFolder(itemDatabaseId)) {
                deleteBookmarkFolderContents(itemDatabaseId);
            }

            bookmarksDatabaseHelper.deleteBookmark(itemDatabaseId);
        }
    }
}