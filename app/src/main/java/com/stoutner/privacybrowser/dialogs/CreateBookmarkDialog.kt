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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText

import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

import com.stoutner.privacybrowser.R

import java.io.ByteArrayOutputStream

class CreateBookmarkDialog: DialogFragment() {
    // The public interface is used to send information back to the parent activity.
    interface CreateBookmarkListener {
        fun onCreateBookmark(dialogFragment: DialogFragment, favoriteIconBitmap: Bitmap)
    }

    // The create bookmark listener is initialized in `onAttach()` and used in `onCreateDialog()`.
    private lateinit var createBookmarkListener: CreateBookmarkListener

    override fun onAttach(context: Context) {
        // Run the default commands.
        super.onAttach(context)

        // Get a handle for the create bookmark listener from the launching context.
        createBookmarkListener = context as CreateBookmarkListener
    }

    companion object {
        // `@JvmStatic` will no longer be required once all the code has transitioned to Kotlin.  Also, the function can then be moved out of a companion object and just become a package-level function.
        @JvmStatic
        fun createBookmark(urlString: String, titleString: String, favoriteIconBitmap: Bitmap): CreateBookmarkDialog {
            // Create a favorite icon byte array output stream.
            val favoriteIconByteArrayOutputStream = ByteArrayOutputStream()

            // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
            favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream)

            // Convert the byte array output stream to a byte array.
            val favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray()

            // Create an arguments bundle.
            val argumentsBundle = Bundle()

            // Store the variables in the bundle.
            argumentsBundle.putString("url_string", urlString)
            argumentsBundle.putString("title_string", titleString)
            argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray)

            // Create a new instance of the dialog.
            val createBookmarkDialog = CreateBookmarkDialog()

            // Add the bundle to the dialog.
            createBookmarkDialog.arguments = argumentsBundle

            // Return the new dialog.
            return createBookmarkDialog
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the alert dialog.
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the arguments.
        val arguments = requireArguments()

        // Get the contents of the arguments.
        val urlString = arguments.getString("url_string")
        val titleString = arguments.getString("title_string")
        val favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array")!!

        // Convert the favorite icon byte array to a bitmap.
        val favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.size)

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
        dialogBuilder.setTitle(R.string.create_bookmark)

        // Create a drawable version of the favorite icon.
        val favoriteIconDrawable: Drawable = BitmapDrawable(resources, favoriteIconBitmap)

        // Set the icon.
        dialogBuilder.setIcon(favoriteIconDrawable)

        // Set the view.  The parent view is `null` because it will be assigned by the alert dialog.
        dialogBuilder.setView(requireActivity().layoutInflater.inflate(R.layout.create_bookmark_dialog, null))

        // Set a listener on the cancel button.  Using `null` as the listener closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.cancel, null)

        // Set a listener on the create button.
        dialogBuilder.setPositiveButton(R.string.create) { _: DialogInterface, _: Int ->
            // Return the dialog fragment and the favorite icon bitmap to the parent activity.
            createBookmarkListener.onCreateBookmark(this, favoriteIconBitmap)
        }

        // Create an alert dialog from the builder.
        val alertDialog = dialogBuilder.create()

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // The alert dialog needs to be shown before the contents can be modified.
        alertDialog.show()

        // Get a handle for the edit texts.
        val createBookmarkNameEditText = alertDialog.findViewById<EditText>(R.id.create_bookmark_name_edittext)
        val createBookmarkUrlEditText = alertDialog.findViewById<EditText>(R.id.create_bookmark_url_edittext)

        // Set the initial texts for the edit texts.
        createBookmarkNameEditText.setText(titleString)
        createBookmarkUrlEditText.setText(urlString)

        // Allow the enter key on the keyboard to create the bookmark from the create bookmark name edit text.
        createBookmarkNameEditText.setOnKeyListener { _: View, keyCode: Int, keyEvent: KeyEvent ->
            // Check the key code and event.
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {  // The event is a key-down on the enter key.
                // Trigger the create bookmark listener and return the dialog fragment and the favorite icon bitmap to the parent activity.
                createBookmarkListener.onCreateBookmark(this, favoriteIconBitmap)

                // Manually dismiss the alert dialog.
                alertDialog.dismiss()

                // Consume the event.
                return@setOnKeyListener true
            } else {  // Some other key was pressed.
                // Do not consume the event.
                return@setOnKeyListener false
            }
        }

        // Allow the enter key on the keyboard to create the bookmark from create bookmark URL edit text.
        createBookmarkUrlEditText.setOnKeyListener { _: View, keyCode: Int, keyEvent: KeyEvent ->
            // Check the key code and event.
            if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == KeyEvent.ACTION_DOWN) {  // The event is a key-down on the enter key.
                // Trigger the create bookmark listener and return the dialog fragment and the favorite icon bitmap to the parent activity.
                createBookmarkListener.onCreateBookmark(this, favoriteIconBitmap)

                // Manually dismiss the alert dialog.
                alertDialog.dismiss()

                // Consume the event.
                return@setOnKeyListener true
            } else { // Some other key was pressed.
                // Do not consume the event.
                return@setOnKeyListener false
            }
        }

        // Return the alert dialog.
        return alertDialog
    }
}