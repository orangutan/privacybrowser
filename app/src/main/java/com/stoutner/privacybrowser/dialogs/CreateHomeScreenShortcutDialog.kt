/*
 * Copyright © 2015-2020 Soren Stoutner <soren@stoutner.com>.
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
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton

import androidx.appcompat.app.AlertDialog
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

import com.stoutner.privacybrowser.BuildConfig
import com.stoutner.privacybrowser.R

import java.io.ByteArrayOutputStream

class CreateHomeScreenShortcutDialog: DialogFragment() {
    // Define the class variables.
    private lateinit var shortcutNameEditText: EditText
    private lateinit var urlEditText: EditText
    private lateinit var openWithPrivacyBrowserRadioButton: RadioButton

    companion object {
        // `@JvmStatic` will no longer be required once all the code has transitioned to Kotlin.  Also, the function can then be moved out of a companion object and just become a package-level function.
        @JvmStatic
        fun createDialog(shortcutName: String, urlString: String, favoriteIconBitmap: Bitmap): CreateHomeScreenShortcutDialog {
            // Create a favorite icon byte array output stream.
            val favoriteIconByteArrayOutputStream = ByteArrayOutputStream()

            // Convert the favorite icon to a PNG and place it in the byte array output stream.  `0` is for lossless compression (the only option for a PNG).
            favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream)

            // Convert the byte array output stream to a byte array.
            val favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray()

            // Create an arguments bundle.
            val argumentsBundle = Bundle()

            // Store the variables in the bundle.
            argumentsBundle.putString("shortcut_name", shortcutName)
            argumentsBundle.putString("url_string", urlString)
            argumentsBundle.putByteArray("favorite_icon_byte_array", favoriteIconByteArray)

            // Create a new instance of the dialog.
            val createHomeScreenShortcutDialog = CreateHomeScreenShortcutDialog()

            // Add the bundle to the dialog.
            createHomeScreenShortcutDialog.arguments = argumentsBundle

            // Return the new dialog.
            return createHomeScreenShortcutDialog
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the arguments.
        val arguments = requireArguments()

        // Get the strings from the arguments.
        val initialShortcutName = arguments.getString("shortcut_name")
        val initialUrlString = arguments.getString("url_string")

        // Get the favorite icon byte array.
        val favoriteIconByteArray = arguments.getByteArray("favorite_icon_byte_array")!!

        // Convert the favorite icon byte array to a bitmap.
        val favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.size)

        // Get a handle for the shared preferences.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Get the theme and screenshot preferences.
        val allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false)
        val darkTheme = sharedPreferences.getBoolean("dark_theme", false)

        // Use an alert dialog builder to create the dialog and set the style according to the theme.
        val dialogBuilder = if (darkTheme) {
            AlertDialog.Builder(requireContext(), R.style.PrivacyBrowserAlertDialogDark)
        } else {
            AlertDialog.Builder(requireContext(), R.style.PrivacyBrowserAlertDialogLight)
        }

        // Create a drawable version of the favorite icon.
        val favoriteIconDrawable: Drawable = BitmapDrawable(resources, favoriteIconBitmap)

        // Set the title and icon.
        dialogBuilder.setTitle(R.string.create_shortcut)
        dialogBuilder.setIcon(favoriteIconDrawable)

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(requireActivity().layoutInflater.inflate(R.layout.create_home_screen_shortcut_dialog, null))

        // Set a listener on the close button.  Using null closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.cancel, null)

        // Set a listener on the create button.
        dialogBuilder.setPositiveButton(R.string.create) { _: DialogInterface, _: Int ->
            // Create the home screen shortcut.
            createHomeScreenShortcut(favoriteIconBitmap)
        }

        // Create an alert dialog from the alert dialog builder.
        val alertDialog = dialogBuilder.create()

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // The alert dialog must be shown before the contents can be modified.
        alertDialog.show()

        // Get handles for the views.
        shortcutNameEditText = alertDialog.findViewById(R.id.shortcut_name_edittext)!!
        urlEditText = alertDialog.findViewById(R.id.url_edittext)!!
        openWithPrivacyBrowserRadioButton = alertDialog.findViewById(R.id.open_with_privacy_browser_radiobutton)!!
        val createButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

        // Populate the edit texts.
        shortcutNameEditText.setText(initialShortcutName)
        urlEditText.setText(initialUrlString)

        // Add a text change listener to the shortcut name edit text.
        shortcutNameEditText.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing.
            }

            override fun afterTextChanged(s: Editable) {
                // Update the create button.
                updateCreateButton(createButton)
            }
        })

        // Add a text change listener to the URL edit text.
        urlEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing.
            }

            override fun afterTextChanged(s: Editable) {
                // Update the create button.
                updateCreateButton(createButton)
            }
        })

        // Allow the enter key on the keyboard to create the shortcut when editing the name.
        shortcutNameEditText.setOnKeyListener { _: View?, keyCode: Int, keyEvent: KeyEvent ->
            // Check the key code, event, and button status.
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && createButton.isEnabled) {  // The event is a key-down on the enter key and the create button is enabled.
                // Create the home screen shortcut.
                createHomeScreenShortcut(favoriteIconBitmap)

                // Manually dismiss the alert dialog.
                alertDialog.dismiss()

                // Consume the event.
                return@setOnKeyListener true
            } else {  // Some other key was pressed or the create button is disabled.
                // Do not consume the event.
                return@setOnKeyListener false
            }
        }

        // Set the enter key on the keyboard to create the shortcut when editing the URL.
        urlEditText.setOnKeyListener { _: View?, keyCode: Int, keyEvent: KeyEvent ->
            // Check the key code, event, and button status.
            if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && createButton.isEnabled) {  // The event is a key-down on the enter key and the create button is enabled.
                // Create the home screen shortcut.
                createHomeScreenShortcut(favoriteIconBitmap)

                // Manually dismiss the alert dialog.
                alertDialog.dismiss()

                // Consume the event.
                return@setOnKeyListener true
            } else {  // Some other key was pressed or the create button is disabled.
                // Do not consume the event.
                return@setOnKeyListener false
            }
        }

        // Return the alert dialog.
        return alertDialog
    }

    private fun updateCreateButton(createButton: Button) {
        // Get the contents of the edit texts.
        val shortcutName = shortcutNameEditText.text.toString()
        val urlString = urlEditText.text.toString()

        // Enable the create button if both the shortcut name and the URL are not empty.
        createButton.isEnabled = shortcutName.isNotEmpty() && urlString.isNotEmpty()
    }

    private fun createHomeScreenShortcut(favoriteIconBitmap: Bitmap) {
        // Get the strings from the edit texts.
        val shortcutName = shortcutNameEditText.text.toString()
        val urlString = urlEditText.text.toString()

        // Convert the favorite icon bitmap to an icon.  `IconCompat` must be used until the minimum API >= 26.
        val favoriteIcon = IconCompat.createWithBitmap(favoriteIconBitmap)

        // Create a shortcut intent.
        val shortcutIntent = Intent(Intent.ACTION_VIEW)

        // Check to see if the shortcut should open up Privacy Browser explicitly.
        if (openWithPrivacyBrowserRadioButton.isChecked) {
            // Set the current application ID as the target package.
            shortcutIntent.setPackage(BuildConfig.APPLICATION_ID)
        }

        // Add the URL to the intent.
        shortcutIntent.data = Uri.parse(urlString)

        // Create a shortcut info builder.  The shortcut name becomes the shortcut ID.
        val shortcutInfoBuilder = ShortcutInfoCompat.Builder(requireContext(), shortcutName)

        // Add the required fields to the shortcut info builder.
        shortcutInfoBuilder.setIcon(favoriteIcon)
        shortcutInfoBuilder.setIntent(shortcutIntent)
        shortcutInfoBuilder.setShortLabel(shortcutName)

        // Add the shortcut to the home screen.  `ShortcutManagerCompat` can be switched to `ShortcutManager` once the minimum API >= 26.
        ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcutInfoBuilder.build(), null)
    }
}