/*
 * Copyright © 2018-2019 Soren Stoutner <soren@stoutner.com>.
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.helpers.BlockListHelper;

public class ViewRequestDialog extends DialogFragment {
    // The public interface is used to send information back to the parent activity.
    public interface ViewRequestListener {
        void onPrevious(int id);

        void onNext(int id);
    }

    // `viewRequestListener` is used in `onAttach()` and `onCreateDialog()`.
    private ViewRequestListener viewRequestListener;

    public void onAttach(Context context) {
        // Run the default commands.
        super.onAttach(context);

        // Get a handle for the listener from the launching context.
        viewRequestListener = (ViewRequestListener) context;
    }

    public static ViewRequestDialog request(int id, boolean isLastRequest, String[] requestDetails) {
        // Create a bundle.
        Bundle bundle = new Bundle();

        // Store the request details.
        bundle.putInt("ID", id);
        bundle.putBoolean("Is Last Request", isLastRequest);
        bundle.putStringArray("Request Details", requestDetails);

        // Add the bundle to the dialog.
        ViewRequestDialog viewRequestDialog = new ViewRequestDialog();
        viewRequestDialog.setArguments(bundle);

        // Return the new dialog.
        return viewRequestDialog;
    }

    @Override
    @NonNull
    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Get the theme and screenshot preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Remove the incorrect lint warning that `getInt()` might be null.
        assert getArguments() != null;

        // Get the info from the bundle.
        int id = getArguments().getInt("ID");
        boolean isLastRequest = getArguments().getBoolean("Is Last Request");
        String[] requestDetails = getArguments().getStringArray("Request Details");

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style and icon according to the theme.
        if (darkTheme) {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);
            dialogBuilder.setIcon(R.drawable.block_ads_enabled_dark);
        } else {
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);
            dialogBuilder.setIcon(R.drawable.block_ads_enabled_light);
        }

        // Create the dialog title.
        String title = getResources().getString(R.string.request_details) + " - " + id;

        // Set the title.
        dialogBuilder.setTitle(title);

        // Remove the incorrect lint warnings about items being null.
        assert requestDetails != null;
        assert getActivity() != null;

        // Set the view.  The parent view is null because it will be assigned by the alert dialog.
        dialogBuilder.setView(getActivity().getLayoutInflater().inflate(R.layout.view_request_dialog, null));

        // Set the close button.
        dialogBuilder.setNeutralButton(R.string.close, (DialogInterface dialog, int which) -> {
            // Do nothing.  The dialog will close automatically.
        });

        // Set the previous button.
        dialogBuilder.setNegativeButton(R.string.previous, (DialogInterface dialog, int which) -> {
            // Load the previous request.
            viewRequestListener.onPrevious(id);
        });

        // Set the next button.
        dialogBuilder.setPositiveButton(R.string.next, (DialogInterface dialog, int which) -> {
            // Load the next request.
            viewRequestListener.onNext(id);
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            // Remove the warning below that `getWindow()` might be null.
            assert alertDialog.getWindow() != null;

            // Disable screenshots.
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        //The alert dialog must be shown before the contents can be modified.
        alertDialog.show();

        // Get handles for the dialog views.
        TextView requestDisposition = alertDialog.findViewById(R.id.request_disposition);
        TextView requestUrl = alertDialog.findViewById(R.id.request_url);
        TextView requestBlockListLabel = alertDialog.findViewById(R.id.request_blocklist_label);
        TextView requestBlockList = alertDialog.findViewById(R.id.request_blocklist);
        TextView requestSubListLabel = alertDialog.findViewById(R.id.request_sublist_label);
        TextView requestSubList = alertDialog.findViewById(R.id.request_sublist);
        TextView requestBlockListEntriesLabel = alertDialog.findViewById(R.id.request_blocklist_entries_label);
        TextView requestBlockListEntries = alertDialog.findViewById(R.id.request_blocklist_entries);
        TextView requestBlockListOriginalEntryLabel = alertDialog.findViewById(R.id.request_blocklist_original_entry_label);
        TextView requestBlockListOriginalEntry = alertDialog.findViewById(R.id.request_blocklist_original_entry);
        Button previousButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        Button nextButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

        // Disable the previous button if the first resource request is displayed.
        previousButton.setEnabled(!(id == 1));

        // Disable the next button if the last resource request is displayed.
        nextButton.setEnabled(!isLastRequest);

        // Set the request action text.
        switch (requestDetails[BlockListHelper.REQUEST_DISPOSITION]) {
            case BlockListHelper.REQUEST_DEFAULT:
                // Set the text.
                requestDisposition.setText(R.string.default_allowed);

                // Set the background color.
                requestDisposition.setBackgroundColor(getResources().getColor(R.color.transparent));
                break;

            case BlockListHelper.REQUEST_ALLOWED:
                // Set the text.
                requestDisposition.setText(R.string.allowed);

                // Set the background color.
                if (darkTheme) {
                    requestDisposition.setBackgroundColor(getResources().getColor(R.color.blue_700_50));
                } else {
                    requestDisposition.setBackgroundColor(getResources().getColor(R.color.blue_100));
                }
                break;

            case BlockListHelper.REQUEST_THIRD_PARTY:
                // Set the text.
                requestDisposition.setText(R.string.third_party_blocked);

                // Set the background color.
                if (darkTheme) {
                    requestDisposition.setBackgroundColor(getResources().getColor(R.color.yellow_700_50));
                } else {
                    requestDisposition.setBackgroundColor(getResources().getColor(R.color.yellow_100));
                }
                break;

            case BlockListHelper.REQUEST_BLOCKED:
                // Set the text.
                requestDisposition.setText(R.string.blocked);

                // Set the background color.
                if (darkTheme) {
                    requestDisposition.setBackgroundColor(getResources().getColor(R.color.red_700_40));
                } else {
                    requestDisposition.setBackgroundColor(getResources().getColor(R.color.red_100));
                }
                break;
        }

        // Display the request URL.
        requestUrl.setText(requestDetails[BlockListHelper.REQUEST_URL]);

        // Modify the dialog based on the request action.
        if (requestDetails.length == 2) {  // A default request.
            // Hide the unused views.
            requestBlockListLabel.setVisibility(View.GONE);
            requestBlockList.setVisibility(View.GONE);
            requestSubListLabel.setVisibility(View.GONE);
            requestSubList.setVisibility(View.GONE);
            requestBlockListEntriesLabel.setVisibility(View.GONE);
            requestBlockListEntries.setVisibility(View.GONE);
            requestBlockListOriginalEntryLabel.setVisibility(View.GONE);
            requestBlockListOriginalEntry.setVisibility(View.GONE);
        } else {  // A blocked or allowed request.
            // Set the text on the text views.
            requestBlockList.setText(requestDetails[BlockListHelper.REQUEST_BLOCKLIST]);
            requestBlockListEntries.setText(requestDetails[BlockListHelper.REQUEST_BLOCKLIST_ENTRIES]);
            requestBlockListOriginalEntry.setText(requestDetails[BlockListHelper.REQUEST_BLOCKLIST_ORIGINAL_ENTRY]);

            // Set the sublist text.
            switch (requestDetails[BlockListHelper.REQUEST_SUBLIST]) {
                case BlockListHelper.MAIN_WHITELIST:
                    requestSubList.setText(R.string.main_whitelist);
                    break;

                case BlockListHelper.FINAL_WHITELIST:
                    requestSubList.setText(R.string.final_whitelist);
                    break;

                case BlockListHelper.DOMAIN_WHITELIST:
                    requestSubList.setText(R.string.domain_whitelist);
                    break;

                case BlockListHelper.DOMAIN_INITIAL_WHITELIST:
                    requestSubList.setText(R.string.domain_initial_whitelist);
                    break;

                case BlockListHelper.DOMAIN_FINAL_WHITELIST:
                    requestSubList.setText(R.string.domain_final_whitelist);
                    break;

                case BlockListHelper.THIRD_PARTY_WHITELIST:
                    requestSubList.setText(R.string.third_party_whitelist);
                    break;

                case BlockListHelper.THIRD_PARTY_DOMAIN_WHITELIST:
                    requestSubList.setText(R.string.third_party_domain_whitelist);
                    break;

                case BlockListHelper.THIRD_PARTY_DOMAIN_INITIAL_WHITELIST:
                    requestSubList.setText(R.string.third_party_domain_initial_whitelist);
                    break;

                case BlockListHelper.MAIN_BLACKLIST:
                    requestSubList.setText(R.string.main_blacklist);
                    break;

                case BlockListHelper.INITIAL_BLACKLIST:
                    requestSubList.setText(R.string.initial_blacklist);
                    break;

                case BlockListHelper.FINAL_BLACKLIST:
                    requestSubList.setText(R.string.final_blacklist);
                    break;

                case BlockListHelper.DOMAIN_BLACKLIST:
                    requestSubList.setText(R.string.domain_blacklist);
                    break;

                case BlockListHelper.DOMAIN_INITIAL_BLACKLIST:
                    requestSubList.setText(R.string.domain_initial_blacklist);
                    break;

                case BlockListHelper.DOMAIN_FINAL_BLACKLIST:
                    requestSubList.setText(R.string.domain_final_blacklist);
                    break;

                case BlockListHelper.DOMAIN_REGULAR_EXPRESSION_BLACKLIST:
                    requestSubList.setText(R.string.domain_regular_expression_blacklist);
                    break;

                case BlockListHelper.THIRD_PARTY_BLACKLIST:
                    requestSubList.setText(R.string.third_party_blacklist);
                    break;

                case BlockListHelper.THIRD_PARTY_INITIAL_BLACKLIST:
                    requestSubList.setText(R.string.third_party_initial_blacklist);
                    break;

                case BlockListHelper.THIRD_PARTY_DOMAIN_BLACKLIST:
                    requestSubList.setText(R.string.third_party_domain_blacklist);
                    break;

                case BlockListHelper.THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST:
                    requestSubList.setText(R.string.third_party_domain_initial_blacklist);
                    break;

                case BlockListHelper.THIRD_PARTY_REGULAR_EXPRESSION_BLACKLIST:
                    requestSubList.setText(R.string.third_party_regular_expression_blacklist);
                    break;

                case BlockListHelper.THIRD_PARTY_DOMAIN_REGULAR_EXPRESSION_BLACKLIST:
                    requestSubList.setText(R.string.third_party_domain_regular_expression_blacklist);
                    break;

                case BlockListHelper.REGULAR_EXPRESSION_BLACKLIST:
                    requestSubList.setText(R.string.regular_expression_blacklist);
                    break;
            }
        }

        // `onCreateDialog` requires the return of an alert dialog.
        return alertDialog;
    }
}