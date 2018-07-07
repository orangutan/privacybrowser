/*
 * Copyright © 2017-2018 Soren Stoutner <soren@stoutner.com>.
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
import android.os.Bundle;
import android.support.annotation.NonNull;
// `AppCompatDialogFragment` is used instead of `DialogFragment` to avoid an error on API <=22.
import android.support.v7.app.AppCompatDialogFragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

public class HttpAuthenticationDialog extends AppCompatDialogFragment{
    // `httpAuthenticationListener` is used in `onAttach()` and `onCreateDialog()`
    private HttpAuthenticationListener httpAuthenticationListener;

    // The public interface is used to send information back to the parent activity.
    public interface HttpAuthenticationListener {
        void onHttpAuthenticationCancel();

        void onHttpAuthenticationProceed(AppCompatDialogFragment dialogFragment);
    }

    public void onAttach(Context context) {
        super.onAttach(context);

        // Get a handle for `httpAuthenticationListener` from `context`.
        httpAuthenticationListener = (HttpAuthenticationListener) context;
    }

    public static HttpAuthenticationDialog displayDialog(String host, String realm) {
        // Store the strings in a `Bundle`.
        Bundle argumentsBundle = new Bundle();
        argumentsBundle.putString("Host", host);
        argumentsBundle.putString("Realm", realm);

        // Add `argumentsBundle` to this instance of `HttpAuthenticationDialog`.
        HttpAuthenticationDialog thisHttpAuthenticationDialog = new HttpAuthenticationDialog();
        thisHttpAuthenticationDialog.setArguments(argumentsBundle);
        return thisHttpAuthenticationDialog;
    }

    // `@SuppressLing("InflateParams")` removes the warning about using `null` as the parent view group when inflating the `AlertDialog`.
    @SuppressLint("InflateParams")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Remove the incorrect lint warnings that `getString()` might be null.
        assert getArguments() != null;

        // Get the host and realm variables from the bundle.
        String httpAuthHost = getArguments().getString("Host");
        String httpAuthRealm = getArguments().getString("Realm");

        // Remove the incorrect lint warning that `getActivity()` might be null.
        assert getActivity() != null;

        // Get the activity's layout inflater.
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        // Use an alert dialog builder to create the alert dialog.
        AlertDialog.Builder dialogBuilder;

        // Set the style according to the theme.
        if (MainWebViewActivity.darkTheme) {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogDark);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.lock_dark);
        } else {
            // Set the dialog theme.
            dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.PrivacyBrowserAlertDialogLight);

            // Set the icon.
            dialogBuilder.setIcon(R.drawable.lock_light);
        }

        // Set the title.
        dialogBuilder.setTitle(R.string.http_authentication);

        // Set the layout.  The parent view is `null` because it will be assigned by `AlertDialog`.
        dialogBuilder.setView(layoutInflater.inflate(R.layout.http_authentication_dialog, null));

        // Setup the negative button.
        dialogBuilder.setNegativeButton(R.string.close, (DialogInterface dialog, int which) -> {
            // Call `onHttpAuthenticationCancel()` and return the `DialogFragment` to the parent activity.
            httpAuthenticationListener.onHttpAuthenticationCancel();
        });

        // Setup the positive button.
        dialogBuilder.setPositiveButton(R.string.proceed, (DialogInterface dialog, int which) -> {
            // Call `onHttpAuthenticationProceed()` and return the `DialogFragment` to the parent activity.
            httpAuthenticationListener.onHttpAuthenticationProceed(HttpAuthenticationDialog.this);
        });

        // Create an alert dialog from the alert dialog builder.
        final AlertDialog alertDialog = dialogBuilder.create();

        // Remove the warning below that `getWindow()` might be null.
        assert alertDialog.getWindow() != null;

        // Disable screenshots if not allowed.
        if (!MainWebViewActivity.allowScreenshots) {
            alertDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Show the keyboard when the `AlertDialog` is displayed on the screen.
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // The alert dialog needs to be shown before the contents can be modified.
        alertDialog.show();

        // Get handles for the views in `alertDialog`.
        TextView realmTextView = alertDialog.findViewById(R.id.http_authentication_realm);
        TextView hostTextView = alertDialog.findViewById(R.id.http_authentication_host);
        EditText usernameEditText = alertDialog.findViewById(R.id.http_authentication_username);
        EditText passwordEditText = alertDialog.findViewById(R.id.http_authentication_password);

        // Set the realm text.
        realmTextView.setText(httpAuthRealm);

        // Set the realm text color according to the theme.  The deprecated `.getColor()` must be used until API >= 23.
        if (MainWebViewActivity.darkTheme) {
            //noinspection deprecation
            realmTextView.setTextColor(getResources().getColor(R.color.gray_300));
        } else {
            //noinspection deprecation
            realmTextView.setTextColor(getResources().getColor(R.color.black));
        }

        // Initialize the host label and the `SpannableStringBuilder`.
        String hostLabel = getString(R.string.host) + "  ";
        SpannableStringBuilder hostStringBuilder = new SpannableStringBuilder(hostLabel + httpAuthHost);

        // Create a blue `ForegroundColorSpan`.
        ForegroundColorSpan blueColorSpan;

        // Set `blueColorSpan` according to the theme.  The deprecated `getColor()` must be used until API >= 23.
        if (MainWebViewActivity.darkTheme) {
            //noinspection deprecation
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_400));
        } else {
            //noinspection deprecation
            blueColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.blue_700));
        }

        // Setup the span to display the host name in blue.  `SPAN_INCLUSIVE_INCLUSIVE` allows the span to grow in either direction.
        hostStringBuilder.setSpan(blueColorSpan, hostLabel.length(), hostStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        // Set the host text.
        hostTextView.setText(hostStringBuilder);

        // Allow the `enter` key on the keyboard to trigger `onHttpAuthenticationProceed` from `usernameEditText`.
        usernameEditText.setOnKeyListener((View view, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the `enter` key, call `onHttpAuthenticationProceed()`.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger `onHttpAuthenticationProceed` and return the `DialogFragment` to the parent activity.
                httpAuthenticationListener.onHttpAuthenticationProceed(HttpAuthenticationDialog.this);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // Allow the `enter` key on the keyboard to trigger `onHttpAuthenticationProceed()` from `passwordEditText`.
        passwordEditText.setOnKeyListener((View view, int keyCode, KeyEvent event) -> {
            // If the event is a key-down on the `enter` key, call `onHttpAuthenticationProceed()`.
            if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                // Trigger `onHttpAuthenticationProceed` and return the `DialogFragment` to the parent activity.
                httpAuthenticationListener.onHttpAuthenticationProceed(HttpAuthenticationDialog.this);

                // Manually dismiss the `AlertDialog`.
                alertDialog.dismiss();

                // Consume the event.
                return true;
            } else {  // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // `onCreateDialog()` requires the return of an `AlertDialog`.
        return alertDialog;
    }
}
