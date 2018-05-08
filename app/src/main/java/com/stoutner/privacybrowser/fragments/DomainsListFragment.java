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

package com.stoutner.privacybrowser.fragments;

import android.net.http.SslCertificate;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
// We have to use `android.support.v4.app.Fragment` until minimum API >= 23.  Otherwise we cannot call `getContext()`.
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.DomainsActivity;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class DomainsListFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate `domains_list_fragment`.  `false` does not attach it to the root `container`.
        View domainsListFragmentView = inflater.inflate(R.layout.domains_list_fragment, container, false);

        // Initialize `domainsListView`.
        ListView domainsListView = domainsListFragmentView.findViewById(R.id.domains_listview);

        // Initialize the database handler.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        final DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(getContext(), null, null, 0);

        // Remove the incorrect lint error below that `.getSupportFragmentManager()` might be null.
        assert getActivity() != null;

        // Get a handle for `supportFragmentManager`.
        final FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();

        domainsListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            // Dismiss `undoDeleteSnackbar` if it is currently displayed (because a domain has just been deleted).
            if ((DomainsActivity.undoDeleteSnackbar != null) && (DomainsActivity.undoDeleteSnackbar.isShown())) {
                DomainsActivity.dismissingSnackbar = true;

                DomainsActivity.undoDeleteSnackbar.dismiss();
            }

            // Save the current domain settings if operating in two-paned mode and a domain is currently selected.
            if (DomainsActivity.twoPanedMode && DomainsActivity.deleteMenuItem.isEnabled()) {
                View domainSettingsFragmentView = supportFragmentManager.findFragmentById(R.id.domain_settings_fragment_container).getView();
                assert domainSettingsFragmentView != null;

                // Get handles for the domain settings.
                EditText domainNameEditText = domainSettingsFragmentView.findViewById(R.id.domain_settings_name_edittext);
                Switch javaScriptSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_javascript_switch);
                Switch firstPartyCookiesSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_first_party_cookies_switch);
                Switch thirdPartyCookiesSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_third_party_cookies_switch);
                Switch domStorageSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_dom_storage_switch);
                Switch formDataSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_form_data_switch);
                Switch easyListSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_easylist_switch);
                Switch easyPrivacySwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_easyprivacy_switch);
                Switch fanboysAnnoyanceSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_fanboys_annoyance_list_switch);
                Switch fanboysSocialBlockingSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_fanboys_social_blocking_list_switch);
                Spinner userAgentSpinner = domainSettingsFragmentView.findViewById(R.id.domain_settings_user_agent_spinner);
                EditText customUserAgentEditText = domainSettingsFragmentView.findViewById(R.id.domain_settings_custom_user_agent_edittext);
                Spinner fontSizeSpinner = domainSettingsFragmentView.findViewById(R.id.domain_settings_font_size_spinner);
                Spinner displayWebpageImagesSpinner = domainSettingsFragmentView.findViewById(R.id.domain_settings_display_webpage_images_spinner);
                Spinner nightModeSpinner = domainSettingsFragmentView.findViewById(R.id.domain_settings_night_mode_spinner);
                Switch pinnedSslCertificateSwitch = domainSettingsFragmentView.findViewById(R.id.domain_settings_pinned_ssl_certificate_switch);
                RadioButton savedSslCertificateRadioButton = domainSettingsFragmentView.findViewById(R.id.saved_ssl_certificate_radiobutton);
                RadioButton currentWebsiteCertificateRadioButton = domainSettingsFragmentView.findViewById(R.id.current_website_certificate_radiobutton);

                // Extract the data for the domain settings.
                String domainNameString = domainNameEditText.getText().toString();
                boolean javaScriptEnabled = javaScriptSwitch.isChecked();
                boolean firstPartyCookiesEnabled = firstPartyCookiesSwitch.isChecked();
                boolean thirdPartyCookiesEnabled = thirdPartyCookiesSwitch.isChecked();
                boolean domStorageEnabled  = domStorageSwitch.isChecked();
                boolean formDataEnabled = formDataSwitch.isChecked();
                boolean easyListEnabled = easyListSwitch.isChecked();
                boolean easyPrivacyEnabled = easyPrivacySwitch.isChecked();
                boolean fanboysAnnoyanceEnabled = fanboysAnnoyanceSwitch.isChecked();
                boolean fanboysSocialBlockingEnabled = fanboysSocialBlockingSwitch.isChecked();
                int userAgentPositionInt = userAgentSpinner.getSelectedItemPosition();
                int fontSizePositionInt = fontSizeSpinner.getSelectedItemPosition();
                int displayWebpageImagesInt = displayWebpageImagesSpinner.getSelectedItemPosition();
                int nightModeInt = nightModeSpinner.getSelectedItemPosition();
                boolean pinnedSslCertificate = pinnedSslCertificateSwitch.isChecked();

                // Get the data for the `Spinners` from the entry values string arrays.
                String userAgentString = getResources().getStringArray(R.array.domain_settings_user_agent_entry_values)[userAgentPositionInt];
                int fontSizeInt = Integer.parseInt(getResources().getStringArray(R.array.domain_settings_font_size_entry_values)[fontSizePositionInt]);

                // Check to see if we are using a custom user agent.
                if (userAgentString.equals("Custom user agent")) {
                    // Set `userAgentString` to the custom user agent string.
                    userAgentString = customUserAgentEditText.getText().toString();
                }

                // Save the domain settings.
                if (savedSslCertificateRadioButton.isChecked()) {  // The current certificate is being used.
                    // Update the database except for the certificate.
                    domainsDatabaseHelper.updateDomainExceptCertificate(DomainsActivity.currentDomainDatabaseId, domainNameString, javaScriptEnabled, firstPartyCookiesEnabled, thirdPartyCookiesEnabled,
                            domStorageEnabled, formDataEnabled, easyListEnabled, easyPrivacyEnabled, fanboysAnnoyanceEnabled, fanboysSocialBlockingEnabled, userAgentString, fontSizeInt, displayWebpageImagesInt,
                            nightModeInt, pinnedSslCertificate);
                } else if (currentWebsiteCertificateRadioButton.isChecked()) {  // The certificate is being updated with the current website certificate.
                    // Get the current website SSL certificate.
                    SslCertificate currentWebsiteSslCertificate = MainWebViewActivity.sslCertificate;

                    // Store the values from the SSL certificate.
                    String issuedToCommonName = currentWebsiteSslCertificate.getIssuedTo().getCName();
                    String issuedToOrganization = currentWebsiteSslCertificate.getIssuedTo().getOName();
                    String issuedToOrganizationalUnit = currentWebsiteSslCertificate.getIssuedTo().getUName();
                    String issuedByCommonName = currentWebsiteSslCertificate.getIssuedBy().getCName();
                    String issuedByOrganization = currentWebsiteSslCertificate.getIssuedBy().getOName();
                    String issuedByOrganizationalUnit = currentWebsiteSslCertificate.getIssuedBy().getUName();
                    long startDateLong = currentWebsiteSslCertificate.getValidNotBeforeDate().getTime();
                    long endDateLong = currentWebsiteSslCertificate.getValidNotAfterDate().getTime();

                    // Update the database.
                    domainsDatabaseHelper.updateDomainWithCertificate(DomainsActivity.currentDomainDatabaseId, domainNameString, javaScriptEnabled, firstPartyCookiesEnabled, thirdPartyCookiesEnabled,
                            domStorageEnabled, formDataEnabled, easyListEnabled, easyPrivacyEnabled, fanboysAnnoyanceEnabled, fanboysSocialBlockingEnabled, userAgentString, fontSizeInt, displayWebpageImagesInt,
                            nightModeInt, pinnedSslCertificate, issuedToCommonName, issuedToOrganization, issuedToOrganizationalUnit, issuedByCommonName, issuedByOrganization,
                            issuedByOrganizationalUnit, startDateLong, endDateLong);
                } else {  // No certificate is selected.
                    // Update the database, with PINNED_SSL_CERTIFICATE set to false.
                    domainsDatabaseHelper.updateDomainExceptCertificate(DomainsActivity.currentDomainDatabaseId, domainNameString, javaScriptEnabled, firstPartyCookiesEnabled, thirdPartyCookiesEnabled,
                            domStorageEnabled, formDataEnabled, easyListEnabled, easyPrivacyEnabled, fanboysAnnoyanceEnabled, fanboysSocialBlockingEnabled, userAgentString, fontSizeInt, displayWebpageImagesInt,
                            nightModeInt, false);
                }
            }

            // Store the new `currentDomainDatabaseId`, converting it from `long` to `int` to match the format of the domains database.
            DomainsActivity.currentDomainDatabaseId = (int) id;

            // Add `currentDomainDatabaseId` to `argumentsBundle`.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, DomainsActivity.currentDomainDatabaseId);

            // Add `argumentsBundle` to `domainSettingsFragment`.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Display the domain settings fragment.
            if (DomainsActivity.twoPanedMode) {  // The device in in two-paned mode.
                // enable `deleteMenuItem` if the system is not waiting for a `Snackbar` to be dismissed.
                if (!DomainsActivity.dismissingSnackbar) {
                    // Enable `deleteMenuItem`.
                    DomainsActivity.deleteMenuItem.setEnabled(true);

                    // Set the delete icon according to the theme.
                    if (MainWebViewActivity.darkTheme) {
                        DomainsActivity.deleteMenuItem.setIcon(R.drawable.delete_dark);
                    } else {
                        DomainsActivity.deleteMenuItem.setIcon(R.drawable.delete_light);
                    }
                }

                // Display `domainSettingsFragment`.
                supportFragmentManager.beginTransaction().replace(R.id.domain_settings_fragment_container, domainSettingsFragment).commit();
            } else { // The device in in single-paned mode
                // Show `deleteMenuItem` if the system is not waiting for a `Snackbar` to be dismissed.
                if (!DomainsActivity.dismissingSnackbar) {
                    DomainsActivity.deleteMenuItem.setVisible(true);
                }

                // Hide `add_domain_fab`.
                FloatingActionButton addDomainFAB = getActivity().findViewById(R.id.add_domain_fab);
                addDomainFAB.setVisibility(View.GONE);

                // Display `domainSettingsFragment`.
                supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
            }
        });

        return domainsListFragmentView;
    }
}
