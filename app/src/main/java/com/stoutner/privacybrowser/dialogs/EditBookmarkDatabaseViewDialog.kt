/*
 * Copyright © 2016-2020 Soren Stoutner <soren@stoutner.com>.
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
package com.stoutner.privacybrowser.dialogs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener

import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

import com.stoutner.privacybrowser.R
import com.stoutner.privacybrowser.activities.BookmarksDatabaseViewActivity
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper

import java.io.ByteArrayOutputStream

class EditBookmarkDatabaseViewDialog: DialogFragment() {
    // The public interface is used to send information back to the parent activity.
    interface EditBookmarkDatabaseViewListener {
        fun onSaveBookmark(dialogFragment: DialogFragment, selectedBookmarkDatabaseId: Int, favoriteIconBitmap: Bitmap)
    }

    // Define the edit bookmark database view listener.
    private lateinit var editBookmarkDatabaseViewListener: EditBookmarkDatabaseViewListener

    // Define the handles for the views that need to be accessed from `updateEditButton()`.
    private lateinit var newIconRadioButton: RadioButton
    private lateinit var nameEditText: EditText
    private lateinit var urlEditText: EditText
    private lateinit var folderSpinner: Spinner
    private lateinit var displayOrderEditText: EditText
    private lateinit var editButton: Button

    override fun onAttach(context: Context) {
        // Run the default commands.
        super.onAttach(context)

        // Get a handle for edit bookmark database view listener from the launching context.
        editBookmarkDatabaseViewListener = context as EditBookmarkDatabaseViewListener
    }

    companion object {
        // `@JvmStatic` will no longer be required once all the code has transitioned to Kotlin.  Also, the function can then be moved out of a companion object and just become a package-level function.
        @JvmStatic
        fun bookmarkDatabaseId(databaseId: Int, favoriteIconBitmap: Bitmap): EditBookmarkDatabaseViewDialog {
            // Create a favorite icon byte array output stream.
            val favoriteIconByteArrayOutputStream = ByteArrayOutputStream()

            // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
            favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream)

            // Convert the byte array output stream to a byte array.
            val favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray()

            // Create an arguments bundle.
            val argumentsBundle = Bundle()

            // Store the variables in the bundle.
            argumentsBundle.putInt("database_id", databaseId)
            argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray)

            // Create a new instance of the dialog.
            val editBookmarkDatabaseViewDialog = EditBookmarkDatabaseViewDialog()

            // Add the arguments bundle to the dialog.
            editBookmarkDatabaseViewDialog.arguments = argumentsBundle

            // Return the new dialog.
            return editBookmarkDatabaseViewDialog
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the alert dialog.
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the arguments.
        val arguments = requireArguments()

        // Get the bookmark database ID from the bundle.
        val bookmarkDatabaseId = arguments.getInt("database_id")

        // Get the favorite icon byte array.
        val favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array")!!

        // Convert the favorite icon byte array to a bitmap.
        val favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.size)

        // Initialize the database helper.  The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        val bookmarksDatabaseHelper = BookmarksDatabaseHelper(context, null, null, 0)

        // Get a cursor with the selected bookmark.
        val bookmarkCursor = bookmarksDatabaseHelper.getBookmark(bookmarkDatabaseId)

        // Move the cursor to the first position.
        bookmarkCursor.moveToFirst()

        // Get a handle for the shared preferences.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Get the screenshot and theme preferences.
        val allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false)
        val darkTheme = sharedPreferences.getBoolean("dark_theme", false)

        // Use an alert dialog builder to create the dialog and set the style according to the theme.
        val dialogBuilder = if (darkTheme) {
            AlertDialog.Builder(context, R.style.PrivacyBrowserAlertDialogDark)
        } else {
            AlertDialog.Builder(context, R.style.PrivacyBrowserAlertDialogLight)
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.edit_bookmark)

        // Set the view.  The parent view is `null` because it will be assigned by the alert dialog.
        dialogBuilder.setView(requireActivity().layoutInflater.inflate(R.layout.edit_bookmark_databaseview_dialog, null))

        // Set the listener for the cancel button.  Using `null` as the listener closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.cancel, null)

        // Set the listener for the save button.
        dialogBuilder.setPositiveButton(R.string.save) { _: DialogInterface, _: Int ->
            // Return the dialog fragment to the parent activity on save.
            editBookmarkDatabaseViewListener.onSaveBookmark(this, bookmarkDatabaseId, favoriteIconBitmap)
        }

        // Create an alert dialog from the alert dialog builder.
        val alertDialog = dialogBuilder.create()

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // The alert dialog must be shown before items in the layout can be modified.
        alertDialog.show()

        // Get handles for the layout items.
        val databaseIdTextView = alertDialog.findViewById<TextView>(R.id.edit_bookmark_database_id_textview)
        val iconRadioGroup = alertDialog.findViewById<RadioGroup>(R.id.edit_bookmark_icon_radiogroup)
        val currentIconImageView = alertDialog.findViewById<ImageView>(R.id.edit_bookmark_current_icon)
        val newFavoriteIconImageView = alertDialog.findViewById<ImageView>(R.id.edit_bookmark_webpage_favorite_icon)
        newIconRadioButton = alertDialog.findViewById(R.id.edit_bookmark_webpage_favorite_icon_radiobutton)
        nameEditText = alertDialog.findViewById(R.id.edit_bookmark_name_edittext)
        urlEditText = alertDialog.findViewById(R.id.edit_bookmark_url_edittext)
        folderSpinner = alertDialog.findViewById(R.id.edit_bookmark_folder_spinner)
        displayOrderEditText = alertDialog.findViewById(R.id.edit_bookmark_display_order_edittext)
        editButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

        // Store the current bookmark values.
        val currentBookmarkName = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME))
        val currentUrl = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL))
        val currentDisplayOrder = bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER))

        // Set the database ID.
        databaseIdTextView.text = bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper._ID)).toString()

        // Get the current favorite icon byte array from the cursor.
        val currentIconByteArray = bookmarkCursor.getBlob(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON))

        // Convert the byte array to a bitmap beginning at the first byte and ending at the last.
        val currentIconBitmap = BitmapFactory.decodeByteArray(currentIconByteArray, 0, currentIconByteArray.size)

        // Display the current icon bitmap.
        currentIconImageView.setImageBitmap(currentIconBitmap)

        // Set the new favorite icon bitmap.
        newFavoriteIconImageView.setImageBitmap(favoriteIconBitmap)

        // Populate the bookmark name and URL edit texts.
        nameEditText.setText(currentBookmarkName)
        urlEditText.setText(currentUrl)

        // Create an an array of column names for the matrix cursor comprised of the ID and the name.
        val matrixCursorColumnNamesArray = arrayOf(BookmarksDatabaseHelper._ID, BookmarksDatabaseHelper.BOOKMARK_NAME)

        // Create a matrix cursor based on the column names array.
        val matrixCursor = MatrixCursor(matrixCursorColumnNamesArray)

        // Add `Home Folder` as the first entry in the matrix folder.
        matrixCursor.addRow(arrayOf<Any>(BookmarksDatabaseViewActivity.HOME_FOLDER_DATABASE_ID, getString(R.string.home_folder)))

        // Get a cursor with the list of all the folders.
        val foldersCursor = bookmarksDatabaseHelper.allFolders

        // Combine the matrix cursor and the folders cursor.
        val foldersMergeCursor = MergeCursor(arrayOf(matrixCursor, foldersCursor))

        // Create a resource cursor adapter for the spinner.
        val foldersCursorAdapter: ResourceCursorAdapter = object: ResourceCursorAdapter(context, R.layout.databaseview_spinner_item, foldersMergeCursor, 0) {
            override fun bindView(view: View, context: Context, cursor: Cursor) {
                // Get handles for the spinner views.
                val spinnerItemImageView = view.findViewById<ImageView>(R.id.spinner_item_imageview)
                val spinnerItemTextView = view.findViewById<TextView>(R.id.spinner_item_textview)

                // Set the folder icon according to the type.
                if (foldersMergeCursor.position == 0) {  // The home folder.
                    // Set the gray folder image.  `ContextCompat` must be used until the minimum API >= 21.
                    spinnerItemImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.folder_gray))
                } else {  // A user folder
                    // Get the folder icon byte array.
                    val folderIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON))

                    // Convert the byte array to a bitmap beginning at the first byte and ending at the last.
                    val folderIconBitmap = BitmapFactory.decodeByteArray(folderIconByteArray, 0, folderIconByteArray.size)

                    // Set the folder icon.
                    spinnerItemImageView.setImageBitmap(folderIconBitmap)
                }

                // Set the text view to display the folder name.
                spinnerItemTextView.text = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME))
            }
        }

        // Set the folder cursor adapter drop drown view resource.
        foldersCursorAdapter.setDropDownViewResource(R.layout.databaseview_spinner_dropdown_items)

        // Set the adapter for the folder spinner.
        folderSpinner.adapter = foldersCursorAdapter

        // Get the parent folder name.
        val parentFolder = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER))

        // Select the current folder in the spinner if the bookmark isn't in the home folder.
        if (parentFolder != "") {
            // Get the database ID of the parent folder.
            val folderDatabaseId = bookmarksDatabaseHelper.getFolderDatabaseId(bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER)))

            // Initialize the parent folder position and the iteration variable.
            var parentFolderPosition = 0
            var i = 0

            // Find the parent folder position in folders cursor adapter.
            do {
                if (foldersCursorAdapter.getItemId(i) == folderDatabaseId.toLong()) {
                    // Store the current position for the parent folder.
                    parentFolderPosition = i
                } else {
                    // Try the next entry.
                    i++
                }
                // Stop when the parent folder position is found or all the items in the folders cursor adapter have been checked.
            } while (parentFolderPosition == 0 && i < foldersCursorAdapter.count)

            // Select the parent folder in the spinner.
            folderSpinner.setSelection(parentFolderPosition)
        }

        // Store the current folder database ID.
        val currentFolderDatabaseId = folderSpinner.selectedItemId.toInt()

        // Populate the display order edit text.
        displayOrderEditText.setText(bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER)).toString())

        // Initially disable the edit button.
        editButton.isEnabled = false

        // Update the edit button if the icon selection changes.
        iconRadioGroup.setOnCheckedChangeListener { _: RadioGroup, _: Int ->
            // Update the edit button.
            updateEditButton(currentBookmarkName, currentUrl, currentFolderDatabaseId, currentDisplayOrder)
        }

        // Update the edit button if the bookmark name changes.
        nameEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing.
            }

            override fun afterTextChanged(s: Editable) {
                // Update the edit button.
                updateEditButton(currentBookmarkName, currentUrl, currentFolderDatabaseId, currentDisplayOrder)
            }
        })

        // Update the edit button if the URL changes.
        urlEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing.
            }

            override fun afterTextChanged(s: Editable) {
                // Update the edit button.
                updateEditButton(currentBookmarkName, currentUrl, currentFolderDatabaseId, currentDisplayOrder)
            }
        })

        // Update the edit button if the folder changes.
        folderSpinner.onItemSelectedListener = object: OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Update the edit button.
                updateEditButton(currentBookmarkName, currentUrl, currentFolderDatabaseId, currentDisplayOrder)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing.
            }
        }

        // Update the edit button if the display order changes.
        displayOrderEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing.
            }

            override fun afterTextChanged(s: Editable) {
                // Update the edit button.
                updateEditButton(currentBookmarkName, currentUrl, currentFolderDatabaseId, currentDisplayOrder)
            }
        })

        // Allow the enter key on the keyboard to save the bookmark from the bookmark name edit text.
        nameEditText.setOnKeyListener { _: View, keyCode: Int, keyEvent: KeyEvent ->
            // Check the key code, event, and button status.
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && editButton.isEnabled) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the listener and return the dialog fragment to the parent activity.
                editBookmarkDatabaseViewListener.onSaveBookmark(this@EditBookmarkDatabaseViewDialog, bookmarkDatabaseId, favoriteIconBitmap)

                // Manually dismiss the alert dialog.
                alertDialog.dismiss()

                // Consume the event.
                return@setOnKeyListener true
            } else {  // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return@setOnKeyListener false
            }
        }

        // Allow the enter key on the keyboard to save the bookmark from the URL edit text.
        urlEditText.setOnKeyListener { _: View, keyCode: Int, keyEvent: KeyEvent ->
            // Check the key code, event, and button status.
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && editButton.isEnabled) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the listener and return the dialog fragment to the parent activity.
                editBookmarkDatabaseViewListener.onSaveBookmark(this@EditBookmarkDatabaseViewDialog, bookmarkDatabaseId, favoriteIconBitmap)

                // Manually dismiss the alert dialog.
                alertDialog.dismiss()

                // Consume the event.
                return@setOnKeyListener true
            } else { // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return@setOnKeyListener false
            }
        }

        // Allow the enter key on the keyboard to save the bookmark from the display order edit text.
        displayOrderEditText.setOnKeyListener { _: View, keyCode: Int, keyEvent: KeyEvent ->
            // Check the key code, event, and button status.
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && editButton.isEnabled) {  // The enter key was pressed and the edit button is enabled.
                // Trigger the listener and return the dialog fragment to the parent activity.
                editBookmarkDatabaseViewListener.onSaveBookmark(this@EditBookmarkDatabaseViewDialog, bookmarkDatabaseId, favoriteIconBitmap)

                // Manually dismiss the alert dialog.
                alertDialog.dismiss()

                // Consume the event.
                return@setOnKeyListener true
            } else { // If any other key was pressed, or if the edit button is currently disabled, do not consume the event.
                return@setOnKeyListener false
            }
        }

        // Return the alert dialog.
        return alertDialog
    }

    private fun updateEditButton(currentBookmarkName: String, currentUrl: String, currentFolderDatabaseId: Int, currentDisplayOrder: Int) {
        // Get the values from the dialog.
        val newName = nameEditText.text.toString()
        val newUrl = urlEditText.text.toString()
        val newFolderDatabaseId = folderSpinner.selectedItemId.toInt()
        val newDisplayOrder = displayOrderEditText.text.toString()

        // Has the favorite icon changed?
        val iconChanged = newIconRadioButton.isChecked

        // Has the name changed?
        val nameChanged = (newName != currentBookmarkName)

        // Has the URL changed?
        val urlChanged = (newUrl != currentUrl)

        // Has the folder changed?
        val folderChanged = (newFolderDatabaseId != currentFolderDatabaseId)

        // Has the display order changed?
        val displayOrderChanged = (newDisplayOrder != currentDisplayOrder.toString())

        // Is the display order empty?
        val displayOrderNotEmpty = newDisplayOrder.isNotEmpty()

        // Update the enabled status of the edit button.
        editButton.isEnabled = (iconChanged || nameChanged || urlChanged || folderChanged || displayOrderChanged) && displayOrderNotEmpty
    }
}