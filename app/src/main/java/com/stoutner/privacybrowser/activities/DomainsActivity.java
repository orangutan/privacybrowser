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

package com.stoutner.privacybrowser.activities;

import android.content.Context;
import android.database.Cursor;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.AddDomainDialog;
import com.stoutner.privacybrowser.fragments.DomainSettingsFragment;
import com.stoutner.privacybrowser.fragments.DomainsListFragment;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

public class DomainsActivity extends AppCompatActivity implements AddDomainDialog.AddDomainListener {
    // `twoPanedMode` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onCreate()`, `onCreateOptionsMenu()`, and `populateDomainsListView()`.
    public static boolean twoPanedMode;

    // `databaseId` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onCreateOptionsMenu()`, `saveDomainSettings()` and `populateDomainsListView()`.
    public static int currentDomainDatabaseId;

    // `deleteMenuItem` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onCreateOptionsMenu()`, `onOptionsItemSelected()`, and `onBackPressed()`.
    public static MenuItem deleteMenuItem;

    // `undoDeleteSnackbar` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onOptionsItemSelected()`.
    public static Snackbar undoDeleteSnackbar;

    // `dismissingSnackbar` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onOptionsItemSelected()`.
    public static boolean dismissingSnackbar;

    // `context` is used in `onCreate()`, `onOptionsItemSelected()`, and `onAddDomain()`.
    private Context context;

    // `supportFragmentManager` is used in `onCreate()` and `onCreateOptionsMenu()`.
    private FragmentManager supportFragmentManager;

    // `domainsDatabaseHelper` is used in `onCreate()` and `saveDomainSettings()`.
    private static DomainsDatabaseHelper domainsDatabaseHelper;

    // `domainsListView` is used in `onCreate()` and `populateDomainsList()`.
    private ListView domainsListView;

    // `addDomainFAB` is used in `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, and `onBackPressed()`.
    private FloatingActionButton addDomainFAB;

    // `deletedDomainPosition` is used in an inner and outer class in `onOptionsItemSelected()`.
    private int deletedDomainPosition;

    // `restartAfterRotate` is used in `onCreate()` and `onCreateOptionsMenu()`.
    private boolean restartAfterRotate;

    // `domainSettingsDisplayedBeforeRotate` is used in `onCreate()` and `onCreateOptionsMenu()`.
    private boolean domainSettingsDisplayedBeforeRotate;

    // `domainSettingsDatabaseIdBeforeRotate` is used in `onCreate()` and `onCreateOptionsMenu()`.
    private int domainSettingsDatabaseIdBeforeRotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the activity theme.
        if (MainWebViewActivity.darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Extract the values from `savedInstanceState` if it is not `null`.
        if (savedInstanceState != null) {
            restartAfterRotate = true;
            domainSettingsDisplayedBeforeRotate = savedInstanceState.getBoolean("domainSettingsDisplayed");
            domainSettingsDatabaseIdBeforeRotate = savedInstanceState.getInt("domainSettingsDatabaseId");
        }

        // Set the content view.
        setContentView(R.layout.domains_coordinatorlayout);

        // Get a handle for the context.
        context = this;

        // Get a handle for the fragment manager.
        supportFragmentManager = getSupportFragmentManager();

        // We need to use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        final Toolbar domainsAppBar = (Toolbar) findViewById(R.id.domains_toolbar);
        setSupportActionBar(domainsAppBar);

        // Display the home arrow on `SupportActionBar`.
        ActionBar appBar = getSupportActionBar();
        assert appBar != null;// This assert removes the incorrect warning in Android Studio on the following line that `appBar` might be null.
        appBar.setDisplayHomeAsUpEnabled(true);

        // Initialize the database handler.  The two `nulls` do not specify the database name or a `CursorFactory`.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        domainsDatabaseHelper = new DomainsDatabaseHelper(context, null, null, 0);

        // Determine if we are in two pane mode.  `domain_settings_fragment_container` does not exist on devices with a width less than 900dp.
        twoPanedMode = (findViewById(R.id.domain_settings_fragment_container) != null);

        // Configure `addDomainFAB`.
        addDomainFAB = (FloatingActionButton) findViewById(R.id.add_domain_fab);
        addDomainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the `AddDomainDialog` `AlertDialog` and name the instance `@string/add_domain`.
                AppCompatDialogFragment addDomainDialog = new AddDomainDialog();
                addDomainDialog.show(supportFragmentManager, getResources().getString(R.string.add_domain));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.
        getMenuInflater().inflate(R.menu.domains_options_menu, menu);

        // Store `deleteMenuItem` for future use.
        deleteMenuItem = menu.findItem(R.id.delete_domain);

        // Only display `deleteMenuItem` (initially) in two-paned mode.
        deleteMenuItem.setVisible(twoPanedMode);

        // Display the fragments.  This must be done from `onCreateOptionsMenu()` instead of `onCreate()` because `populateDomainsListView()` needs `deleteMenuItem` to be inflated.
        if (restartAfterRotate && !twoPanedMode && domainSettingsDisplayedBeforeRotate) {  // The device was rotated, the new configuration is in single-paned mode, and domain settings were displayed previously.
            // Reset `restartAfterRotate`.
            restartAfterRotate = false;

            // Store `currentDomainDatabaseId`.
            currentDomainDatabaseId = domainSettingsDatabaseIdBeforeRotate;

            // Add `currentDomainDatabaseId` to `argumentsBundle`.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, currentDomainDatabaseId);

            // Add `argumentsBundle` to `domainSettingsFragment`.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Show `deleteMenuItem`.
            deleteMenuItem.setVisible(true);

            // Hide `add_domain_fab`.
            addDomainFAB.setVisibility(View.GONE);

            // Display `domainSettingsFragment`.
            supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
        } else if (restartAfterRotate && twoPanedMode && domainSettingsDisplayedBeforeRotate) {  // The device was rotated, the new configuration is in two-paned mode, and domain settings were displayed previously.
            // Reset `restartAfterRotate`.
            restartAfterRotate = false;

            // Display `DomainsListFragment`.
            DomainsListFragment domainsListFragment = new DomainsListFragment();
            supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
            supportFragmentManager.executePendingTransactions();

            // Populate the list of domains.  `domainSettingsDatabaseId` highlights the domain that was highlighted before the rotation.
            populateDomainsListView(domainSettingsDatabaseIdBeforeRotate);
        } else {  // The device was not rotated or, if it was, domain settings were not displayed previously.
            // Display `DomainsListFragment`.
            DomainsListFragment domainsListFragment = new DomainsListFragment();
            supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
            supportFragmentManager.executePendingTransactions();

            // Populate the list of domains.  `-1` highlights the first domain.
            populateDomainsListView(-1);
        }

        // Success!
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the ID of the `MenuItem` that was selected.
        int menuItemID = menuItem.getItemId();

        switch (menuItemID) {
            case android.R.id.home:  // The home arrow is identified as `android.R.id.home`, not just `R.id.home`.
                if (twoPanedMode) {  // The device is in two-paned mode.
                    // Save the current domain settings if the domain settings fragment is displayed.
                    if (findViewById(R.id.domain_settings_scrollview) != null) {
                        saveDomainSettings();
                    }

                    // Go home.
                    NavUtils.navigateUpFromSameTask(this);
                } else if (findViewById(R.id.domain_settings_scrollview) != null) {  // The device is in single-paned mode and `DomainSettingsFragment` is displayed.
                    // Save the current domain settings.
                    saveDomainSettings();

                    // Display `DomainsListFragment`.
                    DomainsListFragment domainsListFragment = new DomainsListFragment();
                    supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
                    supportFragmentManager.executePendingTransactions();

                    // Populate the list of domains.  `-1` highlights the first domain if in two-paned mode.  It has no effect in single-paned mode.
                    populateDomainsListView(-1);

                    // Display `addDomainFAB`.
                    addDomainFAB.setVisibility(View.VISIBLE);

                    // Hide `deleteMenuItem`.
                    deleteMenuItem.setVisible(false);
                } else {  // The device is in single-paned mode and `DomainsListFragment` is displayed.
                    // Go home.
                    NavUtils.navigateUpFromSameTask(this);
                }
                break;

            case R.id.delete_domain:
                // Store a copy of `currentDomainDatabaseId` because it could change while the `Snackbar` is displayed.
                final int databaseIdToDelete = currentDomainDatabaseId;

                // Update the fragments and menu items.
                if (twoPanedMode) {  // Two-paned mode.
                    // Store the deleted domain position, which is needed if `Undo` is selected in the `Snackbar`.
                    deletedDomainPosition = domainsListView.getCheckedItemPosition();

                    // Disable the options `MenuItems`.
                    deleteMenuItem.setEnabled(false);
                    deleteMenuItem.setIcon(R.drawable.delete_blue);

                    // Remove the domain settings fragment.
                    supportFragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentById(R.id.domain_settings_fragment_container)).commit();
                } else {  // Single-paned mode.
                    // Display `DomainsListFragment`.
                    DomainsListFragment domainsListFragment = new DomainsListFragment();
                    supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
                    supportFragmentManager.executePendingTransactions();

                    // Display `addDomainFAB`.
                    addDomainFAB.setVisibility(View.VISIBLE);

                    // Hide `deleteMenuItem`.
                    deleteMenuItem.setVisible(false);
                }

                // Get a `Cursor` that does not show the domain to be deleted.
                Cursor domainsPendingDeleteCursor = domainsDatabaseHelper.getDomainNameCursorOrderedByDomainExcept(databaseIdToDelete);

                // Setup `domainsPendingDeleteCursorAdapter` with `this` context.  `false` disables `autoRequery`.
                CursorAdapter domainsPendingDeleteCursorAdapter = new CursorAdapter(this, domainsPendingDeleteCursor, false) {
                    @Override
                    public View newView(Context context, Cursor cursor, ViewGroup parent) {
                        // Inflate the individual item layout.  `false` does not attach it to the root.
                        return getLayoutInflater().inflate(R.layout.domain_name_linearlayout, parent, false);
                    }

                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        // Set the domain name.
                        String domainNameString = cursor.getString(cursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
                        TextView domainNameTextView = (TextView) view.findViewById(R.id.domain_name_textview);
                        domainNameTextView.setText(domainNameString);
                    }
                };

                // Update the handle for the current `domains_listview`.
                domainsListView = (ListView) findViewById(R.id.domains_listview);

                // Update the `ListView`.
                domainsListView.setAdapter(domainsPendingDeleteCursorAdapter);

                // Display a `Snackbar`.
                undoDeleteSnackbar = Snackbar.make(domainsListView, R.string.domain_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Do nothing because everything will be handled by `onDismissed()` below.
                            }
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                switch (event) {
                                    // The user pushed the `Undo` button.
                                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                        // Store `databaseId` in `argumentsBundle`.
                                        Bundle argumentsBundle = new Bundle();
                                        argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, databaseIdToDelete);

                                        // Add `argumentsBundle` to `domainSettingsFragment`.
                                        DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
                                        domainSettingsFragment.setArguments(argumentsBundle);

                                        // Display the correct fragments.
                                        if (twoPanedMode) {  // The device in in two-paned mode.
                                            // Get a `Cursor` with the current contents of the domains database.
                                            Cursor undoDeleteDomainsCursor = domainsDatabaseHelper.getDomainNameCursorOrderedByDomain();

                                            // Setup `domainsCursorAdapter` with `this` context.  `false` disables `autoRequery`.
                                            CursorAdapter undoDeleteDomainsCursorAdapter = new CursorAdapter(context, undoDeleteDomainsCursor, false) {
                                                @Override
                                                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                                    // Inflate the individual item layout.  `false` does not attach it to the root.
                                                    return getLayoutInflater().inflate(R.layout.domain_name_linearlayout, parent, false);
                                                }

                                                @Override
                                                public void bindView(View view, Context context, Cursor cursor) {
                                                    // Set the domain name.
                                                    String domainNameString = cursor.getString(cursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
                                                    TextView domainNameTextView = (TextView) view.findViewById(R.id.domain_name_textview);
                                                    domainNameTextView.setText(domainNameString);
                                                }
                                            };

                                            // Update the `ListView`.
                                            domainsListView.setAdapter(undoDeleteDomainsCursorAdapter);
                                            // Select the previously deleted domain in `domainsListView`.
                                            domainsListView.setItemChecked(deletedDomainPosition, true);

                                            // Display `domainSettingsFragment`.
                                            supportFragmentManager.beginTransaction().replace(R.id.domain_settings_fragment_container, domainSettingsFragment).commit();

                                            // Enable the options `MenuItems`.
                                            deleteMenuItem.setEnabled(true);
                                            deleteMenuItem.setIcon(R.drawable.delete_light);
                                        } else {  // The device in in one-paned mode.
                                            // Display `domainSettingsFragment`.
                                            supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();

                                            // Hide `add_domain_fab`.
                                            FloatingActionButton addDomainFAB = (FloatingActionButton) findViewById(R.id.add_domain_fab);
                                            addDomainFAB.setVisibility(View.GONE);

                                            // Show and enable `deleteMenuItem`.
                                            deleteMenuItem.setVisible(true);

                                            // Display `domainSettingsFragment`.
                                            supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
                                        }
                                        break;

                                    // The `Snackbar` was dismissed without the `Undo` button being pushed.
                                    default:
                                        // Delete the selected domain.
                                        domainsDatabaseHelper.deleteDomain(databaseIdToDelete);

                                        // enable `deleteMenuItem` if the system was waiting for a `Snackbar` to be dismissed.
                                        if (DomainsActivity.dismissingSnackbar) {
                                            // Create a `Runnable` to enable the delete menu item.
                                            Runnable enableDeleteMenuItemRunnable = new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Enable `deleteMenuItem` according to the display mode.
                                                    if (twoPanedMode) {  // Two-paned mode.
                                                        // Enable `deleteMenuItem`.
                                                        deleteMenuItem.setEnabled(true);

                                                        // Set the delete icon according to the theme.
                                                        if (MainWebViewActivity.darkTheme) {
                                                            deleteMenuItem.setIcon(R.drawable.delete_dark);
                                                        } else {
                                                            deleteMenuItem.setIcon(R.drawable.delete_light);
                                                        }
                                                    } else {  // Single-paned mode.
                                                        // Show `deleteMenuItem`.
                                                        deleteMenuItem.setVisible(true);
                                                    }

                                                    // Reset `dismissingSnackbar`.
                                                    dismissingSnackbar = false;
                                                }
                                            };

                                            // Run `enableDeleteMenuItemRunnable` after 100 milliseconds to make sure that the previous domain has been deleted from the database.
                                            Handler handler = new Handler();
                                            handler.postDelayed(enableDeleteMenuItemRunnable, 100);
                                        }
                                        break;
                                }
                            }
                        });
                undoDeleteSnackbar.show();
                break;
        }

        // Consume the event.
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Store the current `DomainSettingsFragment` state in `outState`.
        if (findViewById(R.id.domain_settings_scrollview) != null) {  // `DomainSettingsFragment` is displayed.
            // Save any changes that have been made to the domain settings.
            saveDomainSettings();

            // Store `DomainSettingsDisplayed`.
            outState.putBoolean("domainSettingsDisplayed", true);
            outState.putInt("domainSettingsDatabaseId", DomainSettingsFragment.databaseId);
        } else {  // `DomainSettingsFragment` is not displayed.
            outState.putBoolean("domainSettingsDisplayed", false);
            outState.putInt("domainSettingsDatabaseId", -1);
        }

        super.onSaveInstanceState(outState);
    }

    // Control what the navigation bar back button does.
    @Override
    public void onBackPressed() {
        if (twoPanedMode) {  // The device is in two-paned mode.
            // Save the current domain settings if the domain settings fragment is displayed.
            if (findViewById(R.id.domain_settings_scrollview) != null) {
                saveDomainSettings();
            }

            // Go home.
            NavUtils.navigateUpFromSameTask(this);
        } else if (findViewById(R.id.domain_settings_scrollview) != null) {  // The device is in single-paned mode and `DomainSettingsFragment` is displayed.
            // Save the current domain settings.
            saveDomainSettings();

            // Display `DomainsListFragment`.
            DomainsListFragment domainsListFragment = new DomainsListFragment();
            supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
            supportFragmentManager.executePendingTransactions();

            // Populate the list of domains.  `-1` highlights the first domain if in two-paned mode.  It has no effect in single-paned mode.
            populateDomainsListView(-1);

            // Display `addDomainFAB`.
            addDomainFAB.setVisibility(View.VISIBLE);

            // Hide `deleteMenuItem`.
            deleteMenuItem.setVisible(false);
        } else {  // The device is in single-paned mode and `DomainsListFragment` is displayed.
            // Pass `onBackPressed()` to the system.
            super.onBackPressed();
        }
    }

    @Override
    public void onAddDomain(AppCompatDialogFragment dialogFragment) {
        // Dismiss `undoDeleteSnackbar` if it is currently displayed.
        if ((undoDeleteSnackbar != null) && (undoDeleteSnackbar.isShown())) {
            undoDeleteSnackbar.dismiss();
        }

        // Get the `domainNameEditText` from `dialogFragment` and extract the string.
        EditText domainNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.domain_name_edittext);
        String domainNameString = domainNameEditText.getText().toString();

        // Create the domain and store the database ID in `currentDomainDatabaseId`.
        currentDomainDatabaseId = domainsDatabaseHelper.addDomain(domainNameString);

        // Display the newly created domain.
        if (twoPanedMode) {  // The device in in two-paned mode.
            populateDomainsListView(currentDomainDatabaseId);
        } else {  // The device is in single-paned mode.
            // Hide `add_domain_fab`.
            addDomainFAB.setVisibility(View.GONE);

            // Show and enable `deleteMenuItem`.
            DomainsActivity.deleteMenuItem.setVisible(true);

            // Add `currentDomainDatabaseId` to `argumentsBundle`.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, currentDomainDatabaseId);

            // Add `argumentsBundle` to `domainSettingsFragment`.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Display `domainSettingsFragment`.
            supportFragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
        }
    }

    private void saveDomainSettings() {
        // Get handles for the domain settings.
        EditText domainNameEditText = (EditText) findViewById(R.id.domain_settings_name_edittext);
        Switch javaScriptEnabledSwitch = (Switch) findViewById(R.id.domain_settings_javascript_switch);
        Switch firstPartyCookiesEnabledSwitch = (Switch) findViewById(R.id.domain_settings_first_party_cookies_switch);
        Switch thirdPartyCookiesEnabledSwitch = (Switch) findViewById(R.id.domain_settings_third_party_cookies_switch);
        Switch domStorageEnabledSwitch = (Switch) findViewById(R.id.domain_settings_dom_storage_switch);
        Switch formDataEnabledSwitch = (Switch) findViewById(R.id.domain_settings_form_data_switch);
        Spinner userAgentSpinner = (Spinner) findViewById(R.id.domain_settings_user_agent_spinner);
        EditText customUserAgentEditText = (EditText) findViewById(R.id.domain_settings_custom_user_agent_edittext);
        Spinner fontSizeSpinner = (Spinner) findViewById(R.id.domain_settings_font_size_spinner);
        Spinner displayWebpageImagesSpinner = (Spinner) findViewById(R.id.domain_settings_display_webpage_images_spinner);
        Switch pinnedSslCertificateSwitch = (Switch) findViewById(R.id.domain_settings_pinned_ssl_certificate_switch);
        RadioButton savedSslCertificateRadioButton = (RadioButton) findViewById(R.id.saved_ssl_certificate_radiobutton);
        RadioButton currentWebsiteCertificateRadioButton = (RadioButton) findViewById(R.id.current_website_certificate_radiobutton);

        // Extract the data for the domain settings.
        String domainNameString = domainNameEditText.getText().toString();
        boolean javaScriptEnabledBoolean = javaScriptEnabledSwitch.isChecked();
        boolean firstPartyCookiesEnabledBoolean = firstPartyCookiesEnabledSwitch.isChecked();
        boolean thirdPartyCookiesEnabledBoolean = thirdPartyCookiesEnabledSwitch.isChecked();
        boolean domStorageEnabledEnabledBoolean  = domStorageEnabledSwitch.isChecked();
        boolean formDataEnabledBoolean = formDataEnabledSwitch.isChecked();
        int userAgentPositionInt = userAgentSpinner.getSelectedItemPosition();
        int fontSizePositionInt = fontSizeSpinner.getSelectedItemPosition();
        int displayWebpageImagesInt = displayWebpageImagesSpinner.getSelectedItemPosition();
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
            domainsDatabaseHelper.updateDomainExceptCertificate(DomainsActivity.currentDomainDatabaseId, domainNameString, javaScriptEnabledBoolean, firstPartyCookiesEnabledBoolean, thirdPartyCookiesEnabledBoolean, domStorageEnabledEnabledBoolean,
                    formDataEnabledBoolean, userAgentString, fontSizeInt, displayWebpageImagesInt, pinnedSslCertificate);
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
            domainsDatabaseHelper.updateDomainWithCertificate(currentDomainDatabaseId, domainNameString, javaScriptEnabledBoolean, firstPartyCookiesEnabledBoolean, thirdPartyCookiesEnabledBoolean, domStorageEnabledEnabledBoolean, formDataEnabledBoolean,
                    userAgentString, fontSizeInt, displayWebpageImagesInt, pinnedSslCertificate, issuedToCommonName, issuedToOrganization, issuedToOrganizationalUnit, issuedByCommonName, issuedByOrganization, issuedByOrganizationalUnit, startDateLong,
                    endDateLong);
        } else {  // No certificate is selected.
            // Update the database, with PINNED_SSL_CERTIFICATE set to false.
            domainsDatabaseHelper.updateDomainExceptCertificate(currentDomainDatabaseId, domainNameString, javaScriptEnabledBoolean, firstPartyCookiesEnabledBoolean, thirdPartyCookiesEnabledBoolean, domStorageEnabledEnabledBoolean, formDataEnabledBoolean,
                    userAgentString, fontSizeInt, displayWebpageImagesInt, false);
        }
    }

    private void populateDomainsListView(final int highlightedDomainDatabaseId) {
        // get a handle for the current `domains_listview`.
        domainsListView = (ListView) findViewById(R.id.domains_listview);

        // Get a `Cursor` with the current contents of the domains database.
        Cursor domainsCursor = domainsDatabaseHelper.getDomainNameCursorOrderedByDomain();

        // Setup `domainsCursorAdapter` with `this` context.  `false` disables `autoRequery`.
        CursorAdapter domainsCursorAdapter = new CursorAdapter(context, domainsCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.domain_name_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Set the domain name.
                String domainNameString = cursor.getString(cursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
                TextView domainNameTextView = (TextView) view.findViewById(R.id.domain_name_textview);
                domainNameTextView.setText(domainNameString);
            }
        };

        // Update the `ListView`.
        domainsListView.setAdapter(domainsCursorAdapter);

        // Display the domain settings in the second pane if operating in two pane mode and the database contains at least one domain.
        if (DomainsActivity.twoPanedMode && (domainsCursor.getCount() > 0)) {  // Two-paned mode is enabled and there is at least one domain.
            // Initialize `highlightedDomainPosition`.
            int highlightedDomainPosition = 0;

            // Get the cursor position for the highlighted domain.
            for (int i = 0; i < domainsCursor.getCount(); i++) {
                // Move to position `i` in the cursor.
                domainsCursor.moveToPosition(i);

                // Get the database ID for this position.
                int currentDatabaseId = domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper._ID));

                // Set `highlightedDomainPosition` if the database ID for this matches `highlightedDomainDatabaseId`.
                if (highlightedDomainDatabaseId == currentDatabaseId) {
                    highlightedDomainPosition = i;
                }
            }

            // Select the highlighted domain.
            domainsListView.setItemChecked(highlightedDomainPosition, true);

            // Get the `databaseId` for the highlighted domain.
            domainsCursor.moveToPosition(highlightedDomainPosition);
            currentDomainDatabaseId = domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper._ID));

            // Store `databaseId` in `argumentsBundle`.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, currentDomainDatabaseId);

            // Add `argumentsBundle` to `domainSettingsFragment`.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Display `domainSettingsFragment`.
            supportFragmentManager.beginTransaction().replace(R.id.domain_settings_fragment_container, domainSettingsFragment).commit();

            // Enable the options `MenuItems`.
            deleteMenuItem.setEnabled(true);

            // Set the delete icon according to the theme.
            if (MainWebViewActivity.darkTheme) {
                deleteMenuItem.setIcon(R.drawable.delete_dark);
            } else {
                deleteMenuItem.setIcon(R.drawable.delete_light);
            }
        } else if (twoPanedMode) {  // Two-paned mode is enabled but there are no domains.
            // Disable the options `MenuItems`.
            deleteMenuItem.setEnabled(false);
            deleteMenuItem.setIcon(R.drawable.delete_blue);
        }
    }
}