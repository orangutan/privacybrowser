/*
 * Copyright © 2018-2020 Soren Stoutner <soren@stoutner.com>.
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

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager

import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

import com.stoutner.privacybrowser.R

class AboutViewSourceDialog: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get a handle for the shared preferences.
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        // Get the screenshot and theme preferences.
        val allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false)
        val darkTheme = sharedPreferences.getBoolean("dark_theme", false)

        // Use a builder to create the alert dialog.
        val dialogBuilder: AlertDialog.Builder

        // Set the style and the icon according to the theme.
        if (darkTheme) {
            // Use a dark style.
            dialogBuilder = AlertDialog.Builder(context, R.style.PrivacyBrowserAlertDialogDark)

            // Set a dark icon.
            dialogBuilder.setIcon(R.drawable.about_dark)
        } else {
            // Use a light style.
            dialogBuilder = AlertDialog.Builder(context, R.style.PrivacyBrowserAlertDialogLight)

            // Set a light icon.
            dialogBuilder.setIcon(R.drawable.about_light)
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.about_view_source)

        // Set the text.
        dialogBuilder.setMessage(R.string.about_view_source_message)

        // Set a listener on the close button.  Using `null` as the listener closes the dialog without doing anything else.
        dialogBuilder.setNegativeButton(R.string.close, null)

        // Create an alert dialog from the alert dialog builder.
        val alertDialog = dialogBuilder.create()

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            alertDialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Return the alert dialog.
        return alertDialog
    }
}