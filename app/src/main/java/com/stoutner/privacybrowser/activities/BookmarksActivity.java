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
// `AppCompatDialogFragment` is required instead of `DialogFragment` or an error is produced on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.stoutner.privacybrowser.dialogs.CreateBookmarkDialog;
import com.stoutner.privacybrowser.dialogs.CreateBookmarkFolderDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkFolderDialog;
import com.stoutner.privacybrowser.dialogs.MoveToFolderDialog;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

import java.io.ByteArrayOutputStream;

public class BookmarksActivity extends AppCompatActivity implements CreateBookmarkDialog.CreateBookmarkListener, CreateBookmarkFolderDialog.CreateBookmarkFolderListener, EditBookmarkDialog.EditBookmarkListener,
        EditBookmarkFolderDialog.EditBookmarkFolderListener, MoveToFolderDialog.MoveToFolderListener {

    // `currentFolder` is public static so it can be accessed from `MoveToFolderDialog`.
    // It is used in `onCreate`, `onOptionsItemSelected()`, `onBackPressed()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`, `onMoveToFolder()`,
    // and `loadFolder()`.
    public static String currentFolder;

    // `checkedItemIds` is public static so it can be accessed from `MoveToFolderDialog`.  It is also used in `onCreate()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`, `onMoveToFolder()`,
    // and `updateMoveIcons()`.
    public static long[] checkedItemIds;


    // `bookmarksDatabaseHelper` is used in `onCreate()`, `onOptionsItemSelected()`, `onBackPressed()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`,
    // `onMoveToFolder()`, `deleteBookmarkFolderContents()`, `loadFolder()`, and `onDestroy()`.
    private BookmarksDatabaseHelper bookmarksDatabaseHelper;

    // `bookmarksListView` is used in `onCreate()`, `onOptionsItemSelected()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`, `onMoveToFolder()`,
    // `updateMoveIcons()`, `scrollBookmarks()`, and `loadFolder()`.
    private ListView bookmarksListView;

    // `bookmarksCursor` is used in `onCreate()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`, `onMoveToFolder()`, `deleteBookmarkFolderContents()`,
    // `loadFolder()`, and `onDestroy()`.
    private Cursor bookmarksCursor;

    // `bookmarksCursorAdapter` is used in `onCreate(), `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveBookmark()`, `onSaveBookmarkFolder()`, `onMoveToFolder()`, and `onLoadFolder()`.
    private CursorAdapter bookmarksCursorAdapter;

    // `contextualActionMode` is used in `onCreate()`, `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()` and `onMoveToFolder()`.
    private ActionMode contextualActionMode;

    // `appBar` is used in `onCreate()` and `loadFolder()`.
    private ActionBar appBar;

    // `oldFolderName` is used in `onCreate()` and `onSaveBookmarkFolder()`.
    private String oldFolderNameString;

    // `moveBookmarkUpMenuItem` is used in `onCreate()` and `updateMoveIcons()`.
    private MenuItem moveBookmarkUpMenuItem;

    // `moveBookmarkDownMenuItem` is used in `onCreate()` and `updateMoveIcons()`.
    private MenuItem moveBookmarkDownMenuItem;

    // `bookmarksDeletedSnackbar` is used in `onCreate()`, `onOptionsItemSelected()`, and `onBackPressed()`.
    private Snackbar bookmarksDeletedSnackbar;

    // `closeActivityAfterDismissingSnackbar` is used in `onCreate()`, `onOptionsItemSelected()`, and `onBackPressed()`.
    private boolean closeActivityAfterDismissingSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Set the current folder variable.
        if (launchingIntent.getStringExtra("Current Folder") != null) {  // Set the current folder from the intent.
            currentFolder = launchingIntent.getStringExtra("Current Folder");
        } else {  // Set the current folder to be `""`, which is the home folder.
            currentFolder = "";
        }

        // Set the content view.
        setContentView(R.layout.bookmarks_coordinatorlayout);

        // Use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        final Toolbar bookmarksAppBar = findViewById(R.id.bookmarks_toolbar);
        setSupportActionBar(bookmarksAppBar);

        // Get a handle for the activity, the app bar, and the ListView.
        final Activity bookmarksActivity = this;
        appBar = getSupportActionBar();
        bookmarksListView = findViewById(R.id.bookmarks_listview);

        // Remove the incorrect lint warning that `appBar` might be null.
        assert appBar != null;

        // Display the home arrow on the app bar.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Initialize the database helper.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
        // The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        bookmarksDatabaseHelper = new BookmarksDatabaseHelper(this, null, null, 0);

        // Load the home folder.
        loadFolder();

        // Set a listener so that tapping a list item loads the URL or folder.
        bookmarksListView.setOnItemClickListener((parent, view, position, id) -> {
            // Convert the id from long to int to match the format of the bookmarks database.
            int databaseID = (int) id;

            // Get the bookmark cursor for this ID and move it to the first row.
            Cursor bookmarkCursor = bookmarksDatabaseHelper.getBookmarkCursor(databaseID);
            bookmarkCursor.moveToFirst();

            // Act upon the bookmark according to the type.
            if (bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1) {  // The selected bookmark is a folder.
                // Update the current folder.
                currentFolder = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                // Load the new folder.
                loadFolder();
            } else {  // The selected bookmark is not a folder.
                // Get the bookmark URL and assign it to `formattedUrlString`.
                MainWebViewActivity.formattedUrlString = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL));

                // Set `MainWebViewActivity` to load the new URL on restart.
                MainWebViewActivity.loadUrlOnRestart = true;

                // Update the bookmarks folder for the bookmarks drawer in `MainWebViewActivity`.
                MainWebViewActivity.currentBookmarksFolder = currentFolder;

                // Close the bookmarks drawer and reload the bookmarks `ListView` when returning to `MainWebViewActivity`.
                MainWebViewActivity.restartFromBookmarksActivity = true;

                // Return to `MainWebViewActivity`.
                NavUtils.navigateUpFromSameTask(bookmarksActivity);
            }

            // Close the `Cursor`.
            bookmarkCursor.close();
        });

        // Get a handle for the activity.
        Activity activity = this;

        // `MultiChoiceModeListener` handles long clicks.
        bookmarksListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            // Instantiate the common variables.
            MenuItem editBookmarkMenuItem;
            MenuItem deleteBookmarksMenuItem;
            MenuItem selectAllBookmarksMenuItem;
            boolean deletingBookmarks;

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

                // Get a handle for `MenuItems` that need to be selectively disabled.
                moveBookmarkUpMenuItem = menu.findItem(R.id.move_bookmark_up);
                moveBookmarkDownMenuItem = menu.findItem(R.id.move_bookmark_down);
                editBookmarkMenuItem = menu.findItem(R.id.edit_bookmark);
                deleteBookmarksMenuItem = menu.findItem(R.id.delete_bookmark);
                selectAllBookmarksMenuItem = menu.findItem(R.id.context_menu_select_all_bookmarks);

                // Disable the delete bookmarks menu item if a delete is pending.
                if (deletingBookmarks) {
                    deleteBookmarksMenuItem.setEnabled(false);
                }

                // Store `contextualActionMode` so it can be closed programatically.
                contextualActionMode = mode;

                // Make it so.
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Get a handle for the move to folder menu item.
                MenuItem moveToFolderMenuItem = menu.findItem(R.id.move_to_folder);

                // Get a `Cursor` with all of the folders.
                Cursor folderCursor = bookmarksDatabaseHelper.getAllFoldersCursor();

                // Enable the move to folder menu item if at least one folder exists.
                moveToFolderMenuItem.setVisible(folderCursor.getCount() > 0);

                // Make it so.
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Get a long array of the selected bookmarks.
                long[] selectedBookmarksLongArray = bookmarksListView.getCheckedItemIds();

                // Calculate the number of selected bookmarks.
                int numberOfSelectedBookmarks = selectedBookmarksLongArray.length;

                // Adjust the `ActionMode` and the `Menu` according to the number of selected bookmarks.
                if (numberOfSelectedBookmarks == 0) {  // No bookmarks are selected.
                    // Close the `ActionMode`.
                    mode.finish();
                } else if (numberOfSelectedBookmarks == 1) {  // One bookmark is selected.
                    // List the number of selected bookmarks in the subtitle.
                    mode.setSubtitle(getString(R.string.one_selected));

                    // Show the `Move Up`, `Move Down`, and  `Edit` options.
                    moveBookmarkUpMenuItem.setVisible(true);
                    moveBookmarkDownMenuItem.setVisible(true);
                    editBookmarkMenuItem.setVisible(true);

                    // Update the enabled status of the move icons.
                    updateMoveIcons();
                } else {  // More than one bookmark is selected.
                    // List the number of selected bookmarks according to the language.
                    if (getString(R.string.android_asset_path).equals("ru")) {  // The Russian translation is used.
                        // Convert the number of selected bookmarks to a string.
                        String numberOfSelectedBookmarksString = String.valueOf(numberOfSelectedBookmarks);

                        // Russian follows rule #7 at <https://developer.mozilla.org/en-US/docs/Mozilla/Localization/Localization_and_Plurals>.
                        if (numberOfSelectedBookmarksString.endsWith("1") && !numberOfSelectedBookmarksString.equals("11")) {  //  Ends in 1.
                            mode.setSubtitle(numberOfSelectedBookmarks + " " + getString(R.string.selected_russian_ends_in_1));
                        } else if ((numberOfSelectedBookmarksString.endsWith("2") || numberOfSelectedBookmarksString.endsWith("3") || numberOfSelectedBookmarksString.endsWith("4")) &&
                                !numberOfSelectedBookmarksString.equals("12") && !numberOfSelectedBookmarksString.equals("13") && !numberOfSelectedBookmarksString.equals("14")) {  // Ends in 2-4.
                            mode.setSubtitle(numberOfSelectedBookmarks + " " + getString(R.string.selected_russian_ends_in_2));
                        } else {  // Everything else.
                            mode.setSubtitle(numberOfSelectedBookmarks + " " + getString(R.string.selected_russian_everything_else));
                        }
                    } else {  // Another language is used.
                        // List the number of selected bookmarks in the subtitle.
                        mode.setSubtitle(numberOfSelectedBookmarks + " " + getString(R.string.selected));
                    }

                    // Hide non-applicable `MenuItems`.
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
                // Get the menu item ID.
                int menuItemId = item.getItemId();

                // Instantiate the common variables.
                int selectedBookmarkPosition;
                int selectedBookmarkNewPosition;
                final SparseBooleanArray selectedBookmarksPositionsSparseBooleanArray;

                switch (menuItemId) {
                    case R.id.move_bookmark_up:
                        // Get the array of checked bookmark positions.
                        selectedBookmarksPositionsSparseBooleanArray = bookmarksListView.getCheckedItemPositions();

                        // Store the position of the selected bookmark.  Only one bookmark is selected when `move_bookmark_up` is enabled.
                        selectedBookmarkPosition = selectedBookmarksPositionsSparseBooleanArray.keyAt(0);

                        // Calculate the new position of the selected bookmark.
                        selectedBookmarkNewPosition = selectedBookmarkPosition - 1;

                        // Iterate through the bookmarks.
                        for (int i = 0; i < bookmarksListView.getCount(); i++) {
                            // Get the database ID for the current bookmark.
                            int currentBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(i);

                            // Update the display order for the current bookmark.
                            if (i == selectedBookmarkPosition) {  // The current bookmark is the selected bookmark.
                                // Move the current bookmark up one.
                                bookmarksDatabaseHelper.updateDisplayOrder(currentBookmarkDatabaseId, i - 1);
                            } else if ((i + 1) == selectedBookmarkPosition){  // The current bookmark is immediately above the selected bookmark.
                                // Move the current bookmark down one.
                                bookmarksDatabaseHelper.updateDisplayOrder(currentBookmarkDatabaseId, i + 1);
                            } else {  // The current bookmark is not changing positions.
                                // Move `bookmarksCursor` to the current bookmark position.
                                bookmarksCursor.moveToPosition(i);

                                // Update the display order only if it is not correct in the database.
                                if (bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER)) != i) {
                                    bookmarksDatabaseHelper.updateDisplayOrder(currentBookmarkDatabaseId, i);
                                }
                            }
                        }

                        // Update `bookmarksCursor` with the current contents of the bookmarks database.
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

                        // Update the `ListView`.
                        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

                        // Scroll with the bookmark.
                        scrollBookmarks(selectedBookmarkNewPosition);

                        // Update the enabled status of the move icons.
                        updateMoveIcons();
                        break;

                    case R.id.move_bookmark_down:
                        // Get the array of checked bookmark positions.
                        selectedBookmarksPositionsSparseBooleanArray = bookmarksListView.getCheckedItemPositions();

                        // Store the position of the selected bookmark.  Only one bookmark is selected when `move_bookmark_down` is enabled.
                        selectedBookmarkPosition = selectedBookmarksPositionsSparseBooleanArray.keyAt(0);

                        // Calculate the new position of the selected bookmark.
                        selectedBookmarkNewPosition = selectedBookmarkPosition + 1;

                        // Iterate through the bookmarks.
                        for (int i = 0; i <bookmarksListView.getCount(); i++) {
                            // Get the database ID for the current bookmark.
                            int currentBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(i);

                            // Update the display order for the current bookmark.
                            if (i == selectedBookmarkPosition) {  // The current bookmark is the selected bookmark.
                                // Move the current bookmark down one.
                                bookmarksDatabaseHelper.updateDisplayOrder(currentBookmarkDatabaseId, i + 1);
                            } else if ((i - 1) == selectedBookmarkPosition) {  // The current bookmark is immediately below the selected bookmark.
                                // Move the bookmark below the selected bookmark up one.
                                bookmarksDatabaseHelper.updateDisplayOrder(currentBookmarkDatabaseId, i - 1);
                            } else {  // The current bookmark is not changing positions.
                                // Move `bookmarksCursor` to the current bookmark position.
                                bookmarksCursor.moveToPosition(i);

                                // Update the display order only if it is not correct in the database.
                                if (bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER)) != i) {
                                    bookmarksDatabaseHelper.updateDisplayOrder(currentBookmarkDatabaseId, i);
                                }
                            }
                        }

                        // Update `bookmarksCursor` with the current contents of the bookmarks database.
                        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

                        // Update the `ListView`.
                        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

                        // Scroll with the bookmark.
                        scrollBookmarks(selectedBookmarkNewPosition);

                        // Update the enabled status of the move icons.
                        updateMoveIcons();
                        break;

                    case R.id.move_to_folder:
                        // Store `checkedItemIds` for use by the `AlertDialog`.
                        checkedItemIds = bookmarksListView.getCheckedItemIds();

                        // Show the `MoveToFolderDialog` `AlertDialog` and name the instance `@string/move_to_folder
                        AppCompatDialogFragment moveToFolderDialog = new MoveToFolderDialog();
                        moveToFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.move_to_folder));
                        break;

                    case R.id.edit_bookmark:
                        // Get the array of checked bookmark positions.
                        selectedBookmarksPositionsSparseBooleanArray = bookmarksListView.getCheckedItemPositions();

                        // Get the position of the selected bookmark.  Only one bookmark is selected when `edit_bookmark_down` is enabled.
                        selectedBookmarkPosition = selectedBookmarksPositionsSparseBooleanArray.keyAt(0);

                        // Move the `Cursor` to the selected position and find out if it is a folder.
                        bookmarksCursor.moveToPosition(selectedBookmarkPosition);
                        boolean isFolder = (bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1);

                        // Get the selected bookmark database ID.
                        int databaseId = bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper._ID));

                        // Show the edit bookmark or edit bookmark folder dialog.
                        if (isFolder) {
                            // Save the current folder name, which is used in `onSaveBookmarkFolder()`.
                            oldFolderNameString = bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                            // Show the edit bookmark folder dialog.
                            AppCompatDialogFragment editFolderDialog = EditBookmarkFolderDialog.folderDatabaseId(databaseId);
                            editFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_folder));
                        } else {
                            // Show the edit bookmark dialog.
                            AppCompatDialogFragment editBookmarkDialog = EditBookmarkDialog.bookmarkDatabaseId(databaseId);
                            editBookmarkDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_bookmark));
                        }
                        break;

                    case R.id.delete_bookmark:
                        // Set the deleting bookmarks flag, which prevents the delete menu item from being enabled until the current process finishes.
                        deletingBookmarks = true;

                        // Get an array of the selected row IDs.
                        final long[] selectedBookmarksIdsLongArray = bookmarksListView.getCheckedItemIds();

                        // Get the array of checked bookmarks.  `.clone()` makes a copy that won't change if `bookmarksListView` is reloaded, which is needed for re-selecting the bookmarks on undelete.
                        selectedBookmarksPositionsSparseBooleanArray = bookmarksListView.getCheckedItemPositions().clone();

                        // Update `bookmarksCursor` with the current contents of the bookmarks database except for the specified database IDs.
                        bookmarksCursor = bookmarksDatabaseHelper.getBookmarksCursorExcept(selectedBookmarksIdsLongArray, currentFolder);

                        // Update the `ListView`.
                        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

                        // Instantiate `snackbarMessage`.
                        String snackbarMessage;

                        // Determine how many items are in the array and prepare an appropriate snackbar message.
                        if (selectedBookmarksIdsLongArray.length == 1) {
                            snackbarMessage = getString(R.string.one_bookmark_deleted);
                        } else {
                            // Prepare a snackbar according to the language.
                            if (getString(R.string.android_asset_path).equals("ru")) {  // The Russian translation is used.
                                // Convert the number of selected bookmarks to a string.
                                String numberOfBookmarksString = String.valueOf(selectedBookmarksIdsLongArray.length);

                                // Russian follows rule #7 at <https://developer.mozilla.org/en-US/docs/Mozilla/Localization/Localization_and_Plurals>.
                                if (numberOfBookmarksString.endsWith("1") && !numberOfBookmarksString.equals("11")) {  //  Ends in 1.
                                    snackbarMessage = numberOfBookmarksString + " " + getString(R.string.bookmarks_deleted_russian_ends_in_1);
                                } else if ((numberOfBookmarksString.endsWith("2") || numberOfBookmarksString.endsWith("3") || numberOfBookmarksString.endsWith("4")) &&
                                        !numberOfBookmarksString.equals("12") && !numberOfBookmarksString.equals("13") && !numberOfBookmarksString.equals("14")) {  // Ends in 2-4.
                                    snackbarMessage = numberOfBookmarksString + " " + getString(R.string.bookmarks_deleted_russian_ends_in_2);
                                } else {  // Everything else.
                                    snackbarMessage = numberOfBookmarksString + " " + getString(R.string.bookmarks_deleted_russian_everything_else);
                                }
                            } else {  // Another language is used.
                                snackbarMessage = selectedBookmarksIdsLongArray.length + " " + getString(R.string.bookmarks_deleted);
                            }
                        }

                        // Show a SnackBar.
                        bookmarksDeletedSnackbar = Snackbar.make(findViewById(R.id.bookmarks_coordinatorlayout), snackbarMessage, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, view -> {
                                    // Do nothing because everything will be handled by `onDismissed()` below.
                                })
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        switch (event) {
                                            // The user pushed the `Undo` button.
                                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                                // Update `bookmarksCursor` with the current contents of the bookmarks database, including the "deleted" bookmarks.
                                                bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

                                                // Update the `ListView`.
                                                bookmarksCursorAdapter.changeCursor(bookmarksCursor);

                                                // Re-select the previously selected bookmarks.
                                                for (int i = 0; i < selectedBookmarksPositionsSparseBooleanArray.size(); i++) {
                                                    bookmarksListView.setItemChecked(selectedBookmarksPositionsSparseBooleanArray.keyAt(i), true);
                                                }

                                                break;

                                            // The `Snackbar` was dismissed without the `Undo` button being pushed.
                                            default:
                                                // Delete each selected bookmark.
                                                for (long databaseIdLong : selectedBookmarksIdsLongArray) {
                                                    // Convert `databaseIdLong` to an int.
                                                    int databaseIdInt = (int) databaseIdLong;

                                                    // Delete the contents of the folder if the selected bookmark is a folder.
                                                    if (bookmarksDatabaseHelper.isFolder(databaseIdInt)) {
                                                        deleteBookmarkFolderContents(databaseIdInt);
                                                    }

                                                    // Delete the selected bookmark.
                                                    bookmarksDatabaseHelper.deleteBookmark(databaseIdInt);
                                                }

                                                // Update the display order.
                                                for (int i = 0; i < bookmarksListView.getCount(); i++) {
                                                    // Get the database ID for the current bookmark.
                                                    int currentBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(i);

                                                    // Move `bookmarksCursor` to the current bookmark position.
                                                    bookmarksCursor.moveToPosition(i);

                                                    // Update the display order only if it is not correct in the database.
                                                    if (bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER)) != i) {
                                                        bookmarksDatabaseHelper.updateDisplayOrder(currentBookmarkDatabaseId, i);
                                                    }
                                                }
                                        }

                                        // Enable the delete bookmarks menu item.
                                        deleteBookmarksMenuItem.setEnabled(true);

                                        // Reset the deleting bookmarks flag.
                                        deletingBookmarks = false;

                                        // Close the activity if back has been pressed.
                                        if (closeActivityAfterDismissingSnackbar) {
                                            // Update the bookmarks folder for the bookmarks drawer in the main WebView activity.
                                            MainWebViewActivity.currentBookmarksFolder = currentFolder;

                                            // Close the bookmarks drawer and reload the bookmarks ListView when returning to the main WebView activity.
                                            MainWebViewActivity.restartFromBookmarksActivity = true;

                                            // Return to `MainWebViewActivity`.
                                            NavUtils.navigateUpFromSameTask(activity);
                                        }
                                    }
                                });

                        //Show the `SnackBar`.
                        bookmarksDeletedSnackbar.show();

                        // Close the `ActionBar`.
                        mode.finish();
                        break;

                    case R.id.context_menu_select_all_bookmarks:
                        // Get the total number of bookmarks.
                        int numberOfBookmarks = bookmarksListView.getCount();

                        // Select them all.
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
                // Do nothing.
            }
        });

        // Get handles for the `FloatingActionButtons`.
        FloatingActionButton createBookmarkFolderFab = findViewById(R.id.create_bookmark_folder_fab);
        FloatingActionButton createBookmarkFab = findViewById(R.id.create_bookmark_fab);

        // Set the create new bookmark folder FAB to display the `AlertDialog`.
        createBookmarkFolderFab.setOnClickListener(v -> {
            // Show the `CreateBookmarkFolderDialog` `AlertDialog` and name the instance `@string/create_folder`.
            AppCompatDialogFragment createBookmarkFolderDialog = new CreateBookmarkFolderDialog();
            createBookmarkFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.create_folder));
        });

        // Set the create new bookmark FAB to display the `AlertDialog`.
        createBookmarkFab.setOnClickListener(view -> {
            // Show the `CreateBookmarkDialog` `AlertDialog` and name the instance `@string/create_bookmark`.
            AppCompatDialogFragment createBookmarkDialog = new CreateBookmarkDialog();
            createBookmarkDialog.show(getSupportFragmentManager(), getResources().getString(R.string.create_bookmark));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.bookmarks_options_menu, menu);

        // Success.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the ID of the `MenuItem` that was selected.
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case android.R.id.home:  // The home arrow is identified as `android.R.id.home`, not just `R.id.home`.
                if (currentFolder.isEmpty()) {  // Currently in the home folder.
                    // Go home.
                    if ((bookmarksDeletedSnackbar != null) && bookmarksDeletedSnackbar.isShown()) {  // Close the bookmarks delete snackbar before going home.
                        // Set the close flag.
                        closeActivityAfterDismissingSnackbar = true;

                        // Dismiss the snackbar.
                        bookmarksDeletedSnackbar.dismiss();
                    } else {  // Go home immediately.
                        // Update the bookmarks folder for the bookmarks drawer in `MainWebViewActivity`.
                        MainWebViewActivity.currentBookmarksFolder = "";

                        // Close the bookmarks drawer and reload the bookmarks `ListView` when returning to `MainWebViewActivity`.
                        MainWebViewActivity.restartFromBookmarksActivity = true;

                        // Return to `MainWebViewActivity`.
                        NavUtils.navigateUpFromSameTask(this);
                    }
                } else {  // Currently in a subfolder.
                    // Place the former parent folder in `currentFolder`.
                    currentFolder = bookmarksDatabaseHelper.getParentFolder(currentFolder);

                    // Load the new folder.
                    loadFolder();
                }
                break;

            case R.id.options_menu_select_all_bookmarks:
                // Get the total number of bookmarks.
                int numberOfBookmarks = bookmarksListView.getCount();

                // Select them all.
                for (int i = 0; i < numberOfBookmarks; i++) {
                    bookmarksListView.setItemChecked(i, true);
                }
                break;

            case R.id.bookmarks_database_view:
                // Launch `BookmarksDatabaseViewActivity`.
                Intent bookmarksDatabaseViewIntent = new Intent(this, BookmarksDatabaseViewActivity.class);
                startActivity(bookmarksDatabaseViewIntent);
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // Check to see if a snackbar is currently displayed.  If so, it must be closed before exiting so that a pending delete is completed before reloading the ListView in the bookmarks drawer.
        if ((bookmarksDeletedSnackbar != null) && bookmarksDeletedSnackbar.isShown()) {  // Close the bookmarks delete snackbar before going home.
            // Set the close flag.
            closeActivityAfterDismissingSnackbar = true;

            // Dismiss the snackbar.
            bookmarksDeletedSnackbar.dismiss();
        } else {  // Go home immediately.
            // Update the bookmarks folder for the bookmarks drawer in the main WebView activity.
            MainWebViewActivity.currentBookmarksFolder = currentFolder;

            // Close the bookmarks drawer and reload the bookmarks ListView when returning to the main WebView activity.
            MainWebViewActivity.restartFromBookmarksActivity = true;

            // Exit `BookmarksActivity`.
            super.onBackPressed();
        }
    }

    @Override
    public void onCreateBookmark(AppCompatDialogFragment dialogFragment) {
        // Get the `EditTexts` from the `dialogFragment`.
        EditText createBookmarkNameEditText = dialogFragment.getDialog().findViewById(R.id.create_bookmark_name_edittext);
        EditText createBookmarkUrlEditText = dialogFragment.getDialog().findViewById(R.id.create_bookmark_url_edittext);

        // Extract the strings from the `EditTexts`.
        String bookmarkNameString = createBookmarkNameEditText.getText().toString();
        String bookmarkUrlString = createBookmarkUrlEditText.getText().toString();

        // Convert the favoriteIcon Bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
        MainWebViewActivity.favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Display the new bookmark below the current items in the (0 indexed) list.
        int newBookmarkDisplayOrder = bookmarksListView.getCount();

        // Create the bookmark.
        bookmarksDatabaseHelper.createBookmark(bookmarkNameString, bookmarkUrlString, currentFolder, newBookmarkDisplayOrder, favoriteIconByteArray);

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Scroll to the new bookmark.
        bookmarksListView.setSelection(newBookmarkDisplayOrder);
    }

    @Override
    public void onCreateBookmarkFolder(AppCompatDialogFragment dialogFragment) {
        // Get handles for the views in `dialogFragment`.
        EditText createFolderNameEditText = dialogFragment.getDialog().findViewById(R.id.create_folder_name_edittext);
        RadioButton defaultFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon_radiobutton);
        ImageView folderIconImageView = dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon);

        // Get new folder name string.
        String folderNameString = createFolderNameEditText.getText().toString();

        // Get the new folder icon `Bitmap`.
        Bitmap folderIconBitmap;
        if (defaultFolderIconRadioButton.isChecked()) {  // Use the default folder icon.
            // Get the default folder icon and convert it to a `Bitmap`.
            Drawable folderIconDrawable = folderIconImageView.getDrawable();
            BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
            folderIconBitmap = folderIconBitmapDrawable.getBitmap();
        } else {  // Use the `WebView` favorite icon.
            folderIconBitmap = MainWebViewActivity.favoriteIconBitmap;
        }

        // Convert `folderIconBitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
        ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
        folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
        byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

        // Move all the bookmarks down one in the display order.
        for (int i = 0; i < bookmarksListView.getCount(); i++) {
            int databaseId = (int) bookmarksListView.getItemIdAtPosition(i);
            bookmarksDatabaseHelper.updateDisplayOrder(databaseId, i + 1);
        }

        // Create the folder, which will be placed at the top of the `ListView`.
        bookmarksDatabaseHelper.createFolder(folderNameString, currentFolder, folderIconByteArray);

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Scroll to the new folder.
        bookmarksListView.setSelection(0);
    }

    @Override
    public void onSaveBookmark(AppCompatDialogFragment dialogFragment, int selectedBookmarkDatabaseId) {
        // Get handles for the views from `dialogFragment`.
        EditText editBookmarkNameEditText = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_name_edittext);
        EditText editBookmarkUrlEditText = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_url_edittext);
        RadioButton currentBookmarkIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_current_icon_radiobutton);

        // Store the bookmark strings.
        String bookmarkNameString = editBookmarkNameEditText.getText().toString();
        String bookmarkUrlString = editBookmarkUrlEditText.getText().toString();

        // Update the bookmark.
        if (currentBookmarkIconRadioButton.isChecked()) {  // Update the bookmark without changing the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString);
        } else {  // Update the bookmark using the `WebView` favorite icon.
            // Convert the favorite icon to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream newFavoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
            MainWebViewActivity.favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFavoriteIconByteArrayOutputStream);
            byte[] newFavoriteIconByteArray = newFavoriteIconByteArrayOutputStream.toByteArray();

            //  Update the bookmark and the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString, newFavoriteIconByteArray);
        }

        // Close the contextual action mode.
        contextualActionMode.finish();

        // Update `bookmarksCursor` with the contents of the current folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }

    @Override
    public void onSaveBookmarkFolder(AppCompatDialogFragment dialogFragment, int selectedFolderDatabaseId) {
        // Get handles for the views from `dialogFragment`.
        RadioButton currentFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_current_icon_radiobutton);
        RadioButton defaultFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_radiobutton);
        ImageView defaultFolderIconImageView = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_imageview);
        EditText editFolderNameEditText = dialogFragment.getDialog().findViewById(R.id.edit_folder_name_edittext);

        // Get the new folder name.
        String newFolderNameString = editFolderNameEditText.getText().toString();

        // Check if the favorite icon has changed.
        if (currentFolderIconRadioButton.isChecked()) {  // Only the name has changed.
            // Update the name in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString);
        } else if (!currentFolderIconRadioButton.isChecked() && newFolderNameString.equals(oldFolderNameString)) {  // Only the icon has changed.
            // Get the new folder icon `Bitmap`.
            Bitmap folderIconBitmap;
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon and convert it to a `Bitmap`.
                Drawable folderIconDrawable = defaultFolderIconImageView.getDrawable();
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Use the `WebView` favorite icon.
                folderIconBitmap = MainWebViewActivity.favoriteIconBitmap;
            }

            // Convert the folder `Bitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
            byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

            // Update the folder icon in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, folderIconByteArray);
        } else {  // The folder icon and the name have changed.
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

            // Convert the folder `Bitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
            byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

            // Update the folder name and icon in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString, folderIconByteArray);
        }

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Close the contextual action mode.
        contextualActionMode.finish();
    }

    @Override
    public void onMoveToFolder(AppCompatDialogFragment dialogFragment) {
        // Get a handle for the `ListView` from `dialogFragment`.
        ListView folderListView = dialogFragment.getDialog().findViewById(R.id.move_to_folder_listview);

        // Store a long array of the selected folders.
        long[] newFolderLongArray = folderListView.getCheckedItemIds();

        // Get the new folder database ID.  Only one folder will be selected.
        int newFolderDatabaseId = (int) newFolderLongArray[0];

        // Instantiate `newFolderName`.
        String newFolderName;

        // Set the new folder name.
        if (newFolderDatabaseId == 0) {
            // The new folder is the home folder, represented as `""` in the database.
            newFolderName = "";
        } else {
            // Get the new folder name from the database.
            newFolderName = bookmarksDatabaseHelper.getFolderName(newFolderDatabaseId);
        }

        // Get a long array with the the database ID of the selected bookmarks.
        long[] selectedBookmarksLongArray = bookmarksListView.getCheckedItemIds();

        // Move each of the selected bookmarks to the new folder.
        for (long databaseIdLong : selectedBookmarksLongArray) {
            // Get `databaseIdInt` for each selected bookmark.
            int databaseIdInt = (int) databaseIdLong;

            // Move the selected bookmark to the new folder.
            bookmarksDatabaseHelper.moveToFolder(databaseIdInt, newFolderName);
        }

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Close the contextual app bar.
        contextualActionMode.finish();
    }

    private void deleteBookmarkFolderContents(int databaseId) {
        // Get the name of the folder.
        String folderName = bookmarksDatabaseHelper.getFolderName(databaseId);

        // Get the contents of the folder.
        Cursor folderCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(folderName);

        // Delete each of the bookmarks in the folder.
        for (int i = 0; i < folderCursor.getCount(); i++) {
            // Move `folderCursor` to the current row.
            folderCursor.moveToPosition(i);

            // Get the database ID of the item.
            int itemDatabaseId = folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper._ID));

            // If this is a folder, recursively delete the contents first.
            if (bookmarksDatabaseHelper.isFolder(itemDatabaseId)) {
                deleteBookmarkFolderContents(itemDatabaseId);
            }

            // Delete the bookmark.
            bookmarksDatabaseHelper.deleteBookmark(itemDatabaseId);
        }
    }

    private void updateMoveIcons() {
        // Get a long array of the selected bookmarks.
        long[] selectedBookmarksLongArray = bookmarksListView.getCheckedItemIds();

        // Get the database IDs for the first, last, and selected bookmarks.
        int selectedBookmarkDatabaseId = (int) selectedBookmarksLongArray[0];
        int firstBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(0);
        // bookmarksListView is 0 indexed.
        int lastBookmarkDatabaseId = (int) bookmarksListView.getItemIdAtPosition(bookmarksListView.getCount() - 1);

        // Update the move bookmark up `MenuItem`.
        if (selectedBookmarkDatabaseId == firstBookmarkDatabaseId) {  // The selected bookmark is in the first position.
            // Disable the move bookmark up `MenuItem`.
            moveBookmarkUpMenuItem.setEnabled(false);

            //  Set the move bookmark up icon to be ghosted.
            moveBookmarkUpMenuItem.setIcon(R.drawable.move_up_disabled);
        } else {  // The selected bookmark is not in the first position.
            // Enable the move bookmark up `MenuItem`.
            moveBookmarkUpMenuItem.setEnabled(true);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                moveBookmarkUpMenuItem.setIcon(R.drawable.move_up_enabled_dark);
            } else {
                moveBookmarkUpMenuItem.setIcon(R.drawable.move_up_enabled_light);
            }
        }

        // Update the move bookmark down `MenuItem`.
        if (selectedBookmarkDatabaseId == lastBookmarkDatabaseId) {  // The selected bookmark is in the last position.
            // Disable the move bookmark down `MenuItem`.
            moveBookmarkDownMenuItem.setEnabled(false);

            // Set the move bookmark down icon to be ghosted.
            moveBookmarkDownMenuItem.setIcon(R.drawable.move_down_disabled);
        } else {  // The selected bookmark is not in the last position.
            // Enable the move bookmark down `MenuItem`.
            moveBookmarkDownMenuItem.setEnabled(true);

            // Set the icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                moveBookmarkDownMenuItem.setIcon(R.drawable.move_down_enabled_dark);
            } else {
                moveBookmarkDownMenuItem.setIcon(R.drawable.move_down_enabled_light);
            }
        }
    }

    private void scrollBookmarks(int selectedBookmarkPosition) {
        // Get the first and last visible bookmark positions.
        int firstVisibleBookmarkPosition = bookmarksListView.getFirstVisiblePosition();
        int lastVisibleBookmarkPosition = bookmarksListView.getLastVisiblePosition();

        // Calculate the number of bookmarks per screen.
        int numberOfBookmarksPerScreen = lastVisibleBookmarkPosition - firstVisibleBookmarkPosition;

        // Scroll with the moved bookmark if necessary.
        if (selectedBookmarkPosition <= firstVisibleBookmarkPosition) {  // The selected bookmark position is at or above the top of the screen.
            // Scroll to the selected bookmark position.
            bookmarksListView.setSelection(selectedBookmarkPosition);
        } else if (selectedBookmarkPosition >= (lastVisibleBookmarkPosition - 1)) {  // The selected bookmark is at or below the bottom of the screen.  The `-1` handles partial bookmarks displayed at the bottom of the `ListView`.
            // Scroll to display the selected bookmark at the bottom of the screen.  `+1` assured that the entire bookmark will be displayed in situations where only a partial bookmark fits at the bottom of the `ListView`.
            bookmarksListView.setSelection(selectedBookmarkPosition - numberOfBookmarksPerScreen + 1);
        }
    }

    private void loadFolder() {
        // Update `bookmarksCursor` with the contents of the bookmarks database for the current folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentFolder);

        // Setup a `CursorAdapter`.  `this` specifies the `Context`.  `false` disables `autoRequery`.
        bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_activity_item_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get handles for the views.
                ImageView bookmarkFavoriteIcon = view.findViewById(R.id.bookmark_favorite_icon);
                TextView bookmarkNameTextView = view.findViewById(R.id.bookmark_name);

                // Get the favorite icon byte array from the `Cursor`.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

                // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

                // Display the bitmap in `bookmarkFavoriteIcon`.
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);

                // Get the bookmark name from the cursor and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
                bookmarkNameTextView.setText(bookmarkNameString);

                // Make the font bold for folders.
                if (cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1) {
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {  // Reset the font to default for normal bookmarks.
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT);
                }
            }
        };

        // Populate the `ListView` with the adapter.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);

        // Set the `AppBar` title.
        if (currentFolder.isEmpty()) {
            appBar.setTitle(R.string.bookmarks);
        } else {
            appBar.setTitle(currentFolder);
        }
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