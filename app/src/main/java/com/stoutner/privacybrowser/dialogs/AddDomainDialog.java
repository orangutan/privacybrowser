/*
 * Copyright © 2017-2019 Soren Stoutner <soren@stoutner.com>.
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
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class AddDomainDialog extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface AddDomainListener {
        void onAddDomain(DialogFragment dialogFragment);
    }

    // The add domain listener is used in `onAttach()` and `onCreateDialog()`.
    private AddDomainListener addDomainListener;

    @Override
    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the listener from the launching context.
        addDomainListener = (AddDomainListener) context;
    }

    public static AddDomainDialog addDomain(String url) {
        // Create an arguments bundle.
        Bundle argumentsBundle = new Bundle();

        // Store the URL in the bundle.
        argumentsBundle.putString("url", url);

        // Create a new instance of the dialog.
        AddDomainDialog addDomainDialog = new AddDomainDialog();

        // Add the bundle to the dialog.
        addDomainDialog.setArguments(argumentsBundle);

        // Return the new dialog.
        return addDomainDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the arguments.
        Bundle arguments = getArguments();

        // Remove the incorrect lint warning below that the arguments might be null.
        assert arguments != null;

        // Get the URL from the bundle.
        String url = arguments.getString("url");

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.add_domain);

        // Remove the incorrect lint warning below that `getActivity()` might be null.
        assert getActivity() != null;

        // Set the view.  The parent view is `null` because it will be assigned by the alert dialog.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.add_domain_dialog, null));

        // Set a listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
            // Do nothing.  The alert dialog will close automatically.
        });

        // Set a listener for the positive button.
        dialogBuilder.setPositiveButton(R.string.add, (DialogInterface dialog, int which) -> {
            // Return the dialog fragment to the parent activity on add.
            addDomainListener.onAddDomain(this);
        });

        // Create an alert dialog from the builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when the alert dialog is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The alert dialog must be shown before the contents can be edited.
        alertDialog.show();

        // Initialize `domainsDatabaseHelper`.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        final DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getContext(), null, null, 0);

        // Get handles for the views in the alert dialog.
        final EditText addDomainEditText = alertDialog.findViewById(R.id.domain_name_edittext);
        final TextView domainNameAlreadyExistsTextView = alertDialog.findViewById(R.id.domain_name_already_exists_textview);
        final Button addButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        //  Update the status of the warning text and the add button.
        addDomainEditText.addTextChangedListener(new TextWatcher() {
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
                if (domainsDatabaseHelper.getCursorForDomainName(addDomainEditText.getText().toString()).getCount() >0) {  // The domain already exists.
                    // Show the warning text.
                    domainNameAlreadyExistsTextView.setVisibility(View.VISIBLE);

                    // Disable the add button.
                    addButton.setEnabled(false);
                } else {  // The domain do not yet exist.
                    // Hide the warning text.
                    domainNameAlreadyExistsTextView.setVisibility(View.GONE);

                    // Enable the add button.
                    addButton.setEnabled(true);
                }
            }
        });

        // Convert the URL to a URI.
        Uri currentUri = Uri.parse(url);

        // Display the host in the add domain edit text.
        addDomainEditText.setText(currentUri.getHost());

        // Allow the enter key on the keyboard to create the domain from the add domain edit text.
        addDomainEditText.setOnKeyListener((View view, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the enter key, select the `PositiveButton` `Add`.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger `addDomainListener` and return the dialog fragment to the parent activity.
                addDomainListener.onAddDomain(this);

                // Manually dismiss the alert dialog.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else { // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // Return the alert dialog.
        return alertDialog;
    }
}