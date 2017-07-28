/*
 * Copyright © 2017 Soren Stoutner <soren@stoutner.com>.
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
// We have to use `AppCompatDialogFragment` instead of `DialogFragment` or an error is produced on API <= 22.
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class AddDomainDialog extends AppCompatDialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface AddDomainListener {
        void onAddDomain(AppCompatDialogFragment dialogFragment);
    }

    // `addDomainListener` is used in `onAttach()` and `onCreateDialog()`.
    private AddDomainListener addDomainListener;


    public void onAttach(Context context) {
        super.onAttach(context);

        // Get a handle for `AddDomainListener` from `context`.
        try {
            addDomainListener = (AddDomainListener) context;
        } catch(ClassCastException exception) {
            throw new ClassCastException(context.toString() + " must implement `AddDomainListener`.");
        }
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use `AlertDialog.Builder` to create the `AlertDialog`.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.add_domain);

        // Set the view.  The parent view is `null` because it will be assigned by the `AlertDialog`.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.add_domain_dialog, null));

        // Set an `onClick()` listener for the negative button.
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.  The `AlertDialog` will close automatically.
            }
        });

        // Set an `onClick()` listener for the positive button.
        dialogBuilder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Return the `DialogFragment` to the parent activity on add.
                addDomainListener.onAddDomain(AddDomainDialog.this);
            }
        });

        // Create an `AlertDialog` from the `AlertDialog.Builder`.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below the `setSoftInputMode` might produce `java.lang.NullPointerException`.
        assert alertDialog.getWindow() != null;

        // Show the keyboard when the `AlertDialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The `AlertDialog` must be shown before the contents can be edited.
        alertDialog.show();

        // Initialize `domainsDatabaseHelper`.  The two `nulls` do not specify the database name or a `CursorFactory`.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        final DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getContext(), null, null, 0);

        // Get handles for the views in `alertDialog`.
        final EditText addDomainEditText = (EditText) alertDialog.findViewById(R.id.domain_name_edittext);
        final TextView domainNameAlreadyExistsTextView = (TextView) alertDialog.findViewById(R.id.domain_name_already_exists_textview);
        final Button addButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        //  Update the status of the warning text and the `add` button.
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

                    // Disable the `add` button.
                    addButton.setEnabled(false);
                } else {  // The domain do not yet exist.
                    // Hide the warning text.
                    domainNameAlreadyExistsTextView.setVisibility(View.GONE);

                    // Enable the `add` button.
                    addButton.setEnabled(true);
                }
            }
        });

        // Get the current domain from `formattedUrlString`.
        Uri currentUri = Uri.parse(MainWebViewActivity.formattedUrlString);
        addDomainEditText.setText(currentUri.getHost());

        // Allow the `enter` key on the keyboard to create the domain from `add_domain_edittext`.
        addDomainEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                // If the event is a key-down on the `enter` key, select the `PositiveButton` `Add`.
                if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                    // Trigger `addDomainListener` and return the `DialogFragment` to the parent activity.
                    addDomainListener.onAddDomain(AddDomainDialog.this);
                    // Manually dismiss the `AlertDialog`.
                    alertDialog.dismiss();
                    // Consume the event.
                    return true;
                } else { // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}
