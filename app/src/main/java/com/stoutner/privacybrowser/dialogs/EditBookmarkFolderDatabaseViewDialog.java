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

package com.stoutner.privacybrowser.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.ResourceCursorAdapter;
// `AppCompatDialogFragment` is required instead of `DialogFragment` or an error is produced on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;

public class EditBookmarkFolderDatabaseViewDialog extends AppCompatDialogFragment {
    // Instantiate the constants.
    public static final int HOME_FOLDER_DATABASE_ID = -1;

    // Instantiate the class variables.
    private EditBookmarkFolderDatabaseViewListener editBookmarkFolderDatabaseViewListener;
    private BookmarksDatabaseHelper bookmarksDatabaseHelper;
    private int folderDatabaseId;
    private StringBuilder exceptFolders;
    private String currentFolderName;
    private int currentParentFolderDatabaseId;
    private String currentDisplayOrder;
    private RadioButton currentIconRadioButton;
    private EditText nameEditText;
    private Spinner folderSpinner;
    private EditText displayOrderEditText;
    private Button editButton;

    // The public interface is used to send information back to the parent activity.
    public interface EditBookmarkFolderDatabaseViewListener {
        void onSaveBookmarkFolder(AppCompatDialogFragment dialogFragment, int selectedFolderDatabaseId);
    }

    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for `EditBookmarkDatabaseViewListener` from `context`.
        try {
            editBookmarkFolderDatabaseViewListener = (EditBookmarkFolderDatabaseViewListener) context;
        } catch(ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement EditBookmarkFolderDatabaseViewListener.");
        }
    }

    // Store the database ID in the arguments bundle.
    public static EditBookmarkFolderDatabaseViewDialog folderDatabaseId(int databaseId) {
        // Create a bundle.
        Bundle bundle = new Bundle();

        // Store the bookmark database ID in the bundle.
        bundle.putInt("Database ID", databaseId);

        // Add the bundle to the dialog.
        EditBookmarkFolderDatabaseViewDialog editBookmarkFolderDatabaseViewDialog = new EditBookmarkFolderDatabaseViewDialog();
        editBookmarkFolderDatabaseViewDialog.setArguments(bundle);

        // Return the new dialog.
        return editBookmarkFolderDatabaseViewDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Remove the incorrect lint warning that `getInt()` might be null.
        assert getArguments() != null;

        // Store the bookmark database ID in the class variable.
        folderDatabaseId = getArguments().getInt("Database ID");
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Initialize the database helper.  The two `nulls` do not specify the database name or a `CursorFactory`.  The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        bookmarksDatabaseHelper = new BookmarksDatabaseHelper(getContext(), null, null, 0);

        // Get a `Cursor` with the selected bookmark and move it to the first position.
        Cursor folderCursor = bookmarksDatabaseHelper.getBookmarkCursor(folderDatabaseId);
        folderCursor.moveToFirst();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.edit_folder);

        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.edit_bookmark_folder_databaseview_dialog, null));

        // Set the listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The `AlertDialog` will close automatically.
        });

        // Set the listener fo the positive button.
        dialogBuilder.setPositiveButton(R.string.save, (DialogInterface dialog, int which) -> {
            // Return the `DialogFragment` to the parent activity on save.
            editBookmarkFolderDatabaseViewListener.onSaveBookmarkFolder(EditBookmarkFolderDatabaseViewDialog.this, folderDatabaseId);
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the keyboard to be hidden when the `AlertDialog` is first shown.  If this is not set, the `AlertDialog` will not shrink when the keyboard is displayed.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show();

        // Get handles for the layout items.
        TextView databaseIdTextView = alertDialog.findViewById(R.id.edit_folder_database_id_textview);
        RadioGroup iconRadioGroup = alertDialog.findViewById(R.id.edit_folder_icon_radiogroup);
        ImageView currentIconImageView = alertDialog.findViewById(R.id.edit_folder_current_icon_imageview);
        ImageView newFavoriteIconImageView = alertDialog.findViewById(R.id.edit_folder_webpage_favorite_icon_imageview);
        currentIconRadioButton = alertDialog.findViewById(R.id.edit_folder_current_icon_radiobutton);
        nameEditText = alertDialog.findViewById(R.id.edit_folder_name_edittext);
        folderSpinner = alertDialog.findViewById(R.id.edit_folder_parent_folder_spinner);
        displayOrderEditText = alertDialog.findViewById(R.id.edit_folder_display_order_edittext);
        editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        // Store the current folder values.
        currentFolderName = folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
        currentDisplayOrder = folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER));
        String parentFolder = folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER));

        // Set the database ID.
        databaseIdTextView.setText(String.valueOf(folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper._ID))));

        // Get the current favorite icon byte array from the `Cursor`.
        byte[] currentIconByteArray = folderCursor.getBlob(folderCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

        // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
        Bitmap currentIconBitmap = BitmapFactory.decodeByteArray(currentIconByteArray, 0, currentIconByteArray.length);

        // Display `currentIconBitmap` in `edit_bookmark_current_icon`.
        currentIconImageView.setImageBitmap(currentIconBitmap);

        // Get a `Bitmap` of the favorite icon from `MainWebViewActivity` and display it in `edit_bookmark_web_page_favorite_icon`.
        newFavoriteIconImageView.setImageBitmap(MainWebViewActivity.favoriteIconBitmap);

        // Populate the folder name `EditText`.
        nameEditText.setText(currentFolderName);

        // Setup a `MatrixCursor` "Home Folder".
        String[] matrixCursorColumnNames = {BookmarksDatabaseHelper._ID, BookmarksDatabaseHelper.BOOKMARK_NAME};
        MatrixCursor matrixCursor = new MatrixCursor(matrixCursorColumnNames);
        matrixCursor.addRow(new Object[]{HOME_FOLDER_DATABASE_ID, getString(R.string.home_folder)});

        // Initialize a `StringBuilder` to track the folders not to display in the `Spinner` and populate it with the current folder.
        exceptFolders = new StringBuilder(DatabaseUtils.sqlEscapeString(currentFolderName));

        // Add all subfolders of the current folder to the list of folders not to display.
        addSubfoldersToExceptFolders(currentFolderName);

        // Get a `Cursor` with the list of all the folders.
        Cursor foldersCursor = bookmarksDatabaseHelper.getFoldersCursorExcept(exceptFolders.toString());

        // Combine `matrixCursor` and `foldersCursor`.
        MergeCursor foldersMergeCursor = new MergeCursor(new Cursor[]{matrixCursor, foldersCursor});

        // Remove the incorrect lint warning that `getContext()` might be null.
        assert getContext() != null;

        // Create a `ResourceCursorAdapter` for the `Spinner`.  `0` specifies no flags.;
        ResourceCursorAdapter foldersCursorAdapter = new ResourceCursorAdapter(getContext(), R.layout.edit_bookmark_databaseview_spinner_item, foldersMergeCursor, 0) {
            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get a handle for the `Spinner` item `TextView`.
                TextView spinnerItemTextView = view.findViewById(R.id.spinner_item_textview);

                // Set the `TextView` to display the folder name.
                spinnerItemTextView.setText(cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME)));
            }
        };

        // Set the `ResourceCursorAdapter` drop drown view resource.
        foldersCursorAdapter.setDropDownViewResource(R.layout.edit_bookmark_databaseview_spinner_dropdown_item);

        // Set the adapter for the folder `Spinner`.
        folderSpinner.setAdapter(foldersCursorAdapter);

        // Select the current folder in the `Spinner` if the bookmark isn't in the "Home Folder".
        if (!parentFolder.equals("")) {
            // Get the database ID of the parent folder.
            int folderDatabaseId = bookmarksDatabaseHelper.getFolderDatabaseId(folderCursor.getString(folderCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER)));

            // Initialize `parentFolderPosition` and the iteration variable.
            int parentFolderPosition = 0;
            int i = 0;

            // Find the parent folder position in folders `ResourceCursorAdapter`.
            do {
                if (foldersCursorAdapter.getItemId(i) == folderDatabaseId) {
                    // Store the current position for the parent folder.
                    parentFolderPosition = i;
                } else {
                    // Try the next entry.
                    i++;
                }
                // Stop when the parent folder position is found or all the items in the `ResourceCursorAdapter` have been checked.
            } while ((parentFolderPosition == 0) && (i < foldersCursorAdapter.getCount()));

            // Select the parent folder in the `Spinner`.
            folderSpinner.setSelection(parentFolderPosition);
        }

        // Store the current folder database ID.
        currentParentFolderDatabaseId = (int) folderSpinner.getSelectedItemId();

        // Populate the display order `EditText`.
        displayOrderEditText.setText(String.valueOf(folderCursor.getInt(folderCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER))));

        // Initially disable the edit button.
        editButton.setEnabled(false);

        // Update the edit button if the icon selection changes.
        iconRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Update the edit button.
            updateEditButton();
        });

        // Update the edit button if the bookmark name changes.
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the edit button.
                updateEditButton();
            }
        });

        // Update the edit button if the folder changes.
        folderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Update the edit button.
                updateEditButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Update the edit button if the display order changes.
        displayOrderEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update the edit button.
                updateEditButton();
            }
        });

        // Allow the `enter` key on the keyboard to save the bookmark from the bookmark name `EditText`.
        nameEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the `DialogFragment` to the parent activity.
                editBookmarkFolderDatabaseViewListener.onSaveBookmarkFolder(EditBookmarkFolderDatabaseViewDialog.this, folderDatabaseId);

                // Manually dismiss `alertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // Allow the "enter" key on the keyboard to save the bookmark from the display order `EditText`.
        displayOrderEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // Save the bookmark if the event is a key-down on the "enter" button.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && editButton.isEnabled()) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the `Listener` and return the `DialogFragment` to the parent activity.
                editBookmarkFolderDatabaseViewListener.onSaveBookmarkFolder(EditBookmarkFolderDatabaseViewDialog.this, folderDatabaseId);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else { // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog` requires the return of an `AlertDialog`.
        return alertDialog;
    }

    private void updateEditButton() {
        // Get the values from the dialog.
        String newFolderName = nameEditText.getText().toString();
        int newParentFolderDatabaseId = (int) folderSpinner.getSelectedItemId();
        String newDisplayOrder = displayOrderEditText.getText().toString();

        // Get a cursor for the new folder name if it exists.
        Cursor folderExistsCursor = bookmarksDatabaseHelper.getFolderCursor(newFolderName);

        // Is the new folder name empty?
        boolean folderNameNotEmpty = !newFolderName.isEmpty();

        // Does the folder name already exist?
        boolean folderNameAlreadyExists = (!newFolderName.equals(currentFolderName) && (folderExistsCursor.getCount() > 0));

        // Has the favorite icon changed?
        boolean iconChanged = !currentIconRadioButton.isChecked();

        // Has the name been renamed?
        boolean folderRenamed = (!newFolderName.equals(currentFolderName) && !folderNameAlreadyExists);

        // Has the folder changed?
        boolean parentFolderChanged = newParentFolderDatabaseId != currentParentFolderDatabaseId;

        // Has the display order changed?
        boolean displayOrderChanged = !newDisplayOrder.equals(currentDisplayOrder);

        // Is the display order empty?
        boolean displayOrderNotEmpty = !newDisplayOrder.isEmpty();

        // Update the enabled status of the edit button.
        editButton.setEnabled((iconChanged || folderRenamed || parentFolderChanged || displayOrderChanged) && folderNameNotEmpty && displayOrderNotEmpty);
    }

    private void addSubfoldersToExceptFolders(String folderName) {
        // Get a `Cursor` will all the immediate subfolders.
        Cursor subfoldersCursor = bookmarksDatabaseHelper.getSubfoldersCursor(folderName);

        for (int i = 0; i < subfoldersCursor.getCount(); i++) {
            // Move `subfolderCursor` to the current item.
            subfoldersCursor.moveToPosition(i);

            // Get the name of the subfolder.
            String subfolderName = subfoldersCursor.getString(subfoldersCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

            // Add the subfolder to `exceptFolders`.
            exceptFolders.append(",");
            exceptFolders.append(DatabaseUtils.sqlEscapeString(subfolderName));

            // Run the same tasks for any subfolders of the subfolder.
            addSubfoldersToExceptFolders(subfolderName);
        }
    }
}