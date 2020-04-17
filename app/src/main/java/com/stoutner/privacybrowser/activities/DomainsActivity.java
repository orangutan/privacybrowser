/*
 * Copyright © 2017-2020 Soren Stoutner <soren@stoutner.com>.
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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;  // The AndroidX toolbar must be used until the minimum API is >= 21.
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;  // The AndroidX dialog fragment must be used or an error is produced on API <=22.

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.AddDomainDialog;
import com.stoutner.privacybrowser.fragments.DomainSettingsFragment;
import com.stoutner.privacybrowser.fragments.DomainsListFragment;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;

import java.util.Objects;

public class DomainsActivity extends AppCompatActivity implements AddDomainDialog.AddDomainListener, DomainsListFragment.DismissSnackbarInterface {
    // `twoPanedMode` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onCreate()`, `onCreateOptionsMenu()`, and `populateDomainsListView()`.
    public static boolean twoPanedMode;

    // `databaseId` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onCreateOptionsMenu()`, `saveDomainSettings()` and `populateDomainsListView()`.
    public static int currentDomainDatabaseId;

    // `deleteMenuItem` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onCreateOptionsMenu()`, `onOptionsItemSelected()`, and `onBackPressed()`.
    public static MenuItem deleteMenuItem;

    // `dismissingSnackbar` is public static so it can be accessed from `DomainsListFragment`.  It is also used in `onOptionsItemSelected()`.
    public static boolean dismissingSnackbar;

    // The SSL certificate and IP address information are accessed from `DomainSettingsFragment` and `saveDomainSettings()`.
    public static String sslIssuedToCName;
    public static String sslIssuedToOName;
    public static String sslIssuedToUName;
    public static String sslIssuedByCName;
    public static String sslIssuedByOName;
    public static String sslIssuedByUName;
    public static long sslStartDateLong;
    public static long sslEndDateLong;
    public static String currentIpAddresses;


    // `closeActivityAfterDismissingSnackbar` is used in `onOptionsItemSelected()`, and `onBackPressed()`.
    private boolean closeActivityAfterDismissingSnackbar;

    // The undelete snackbar is used in `onOptionsItemSelected()` and `onBackPressed()`.
    private Snackbar undoDeleteSnackbar;

    // `domainsDatabaseHelper` is used in `onCreate()`, `saveDomainSettings()`, and `onDestroy()`.
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

    // `goDirectlyToDatabaseId` is used in `onCreate()` and `onCreateOptionsMenu()`.
    private int goDirectlyToDatabaseId;

    // `closeOnBack` is used in `onCreate()`, `onOptionsItemSelected()` and `onBackPressed()`.
    private boolean closeOnBack;

    // `coordinatorLayout` is use in `onCreate()`, `onOptionsItemSelected()`, and `onSaveInstanceState()`.
    private View coordinatorLayout;

    // `resources` is used in `onCreate()`, `onOptionsItemSelected()`, and `onSaveInstanceState()`.
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the theme and screenshot preferences.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        boolean allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

        // Disable screenshots if not allowed.
        if (!allowScreenshots) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        // Set the activity theme.
        if (darkTheme) {
            setTheme(R.style.PrivacyBrowserDark_SecondaryActivity);
        } else {
            setTheme(R.style.PrivacyBrowserLight_SecondaryActivity);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Extract the values from `savedInstanceState` if it is not `null`.
        if (savedInstanceState != null) {
            restartAfterRotate = true;
            domainSettingsDisplayedBeforeRotate = savedInstanceState.getBoolean("domain_settings_displayed");
            domainSettingsDatabaseIdBeforeRotate = savedInstanceState.getInt("domain_settings_database_id");
        }

        // Get the launching intent
        Intent intent = getIntent();

        // Extract the domain to load if there is one.  `-1` is the default value.
        goDirectlyToDatabaseId = intent.getIntExtra("load_domain", -1);

        // Get the status of close-on-back, which is true when the domains activity is called from the options menu.
        closeOnBack = intent.getBooleanExtra("close_on_back", false);

        // Get the current URL.
        String currentUrl = intent.getStringExtra("current_url");

        // Store the current SSL certificate information in class variables.
        sslIssuedToCName = intent.getStringExtra("ssl_issued_to_cname");
        sslIssuedToOName = intent.getStringExtra("ssl_issued_to_oname");
        sslIssuedToUName = intent.getStringExtra("ssl_issued_to_uname");
        sslIssuedByCName = intent.getStringExtra("ssl_issued_by_cname");
        sslIssuedByOName = intent.getStringExtra("ssl_issued_by_oname");
        sslIssuedByUName = intent.getStringExtra("ssl_issued_by_uname");
        sslStartDateLong = intent.getLongExtra("ssl_start_date", 0);
        sslEndDateLong = intent.getLongExtra("ssl_end_date", 0);
        currentIpAddresses = intent.getStringExtra("current_ip_addresses");

        // Set the content view.
        setContentView(R.layout.domains_coordinatorlayout);

        // Populate the class variables.
        coordinatorLayout = findViewById(R.id.domains_coordinatorlayout);
        resources = getResources();

        // `SupportActionBar` from `android.support.v7.app.ActionBar` must be used until the minimum API is >= 21.
        final Toolbar toolbar = findViewById(R.id.domains_toolbar);
        setSupportActionBar(toolbar);

        // Get a handle for the action bar.
        ActionBar actionBar = getSupportActionBar();

        // Remove the incorrect lint warning that the action bar might be null.
        assert actionBar != null;

        // Set the back arrow on the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize the database handler.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
        domainsDatabaseHelper = new DomainsDatabaseHelper(this, null, null, 0);

        // Determine if we are in two pane mode.  `domain_settings_fragment_container` does not exist on devices with a width less than 900dp.
        twoPanedMode = (findViewById(R.id.domain_settings_fragment_container) != null);

        // Get a handle for the add domain floating action button.
        addDomainFAB = findViewById(R.id.add_domain_fab);

        // Configure the add domain floating action button.
        addDomainFAB.setOnClickListener((View view) -> {
            // Remove the incorrect warning below that the current URL might be null.
            assert currentUrl != null;

            // Create an add domain dialog.
            DialogFragment addDomainDialog = AddDomainDialog.addDomain(currentUrl);

            // Show the add domain dialog.
            addDomainDialog.show(getSupportFragmentManager(), resources.getString(R.string.add_domain));
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

        // Get a handle for the fragment manager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Display the fragments.  This must be done from `onCreateOptionsMenu()` instead of `onCreate()` because `populateDomainsListView()` needs `deleteMenuItem` to be inflated.
        if (restartAfterRotate && domainSettingsDisplayedBeforeRotate) {  // The device was rotated and domain settings were displayed previously.
            if (twoPanedMode) {  // The device is in two-paned mode.
                // Reset `restartAfterRotate`.
                restartAfterRotate = false;

                // Display `DomainsListFragment`.
                DomainsListFragment domainsListFragment = new DomainsListFragment();
                fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
                fragmentManager.executePendingTransactions();

                // Populate the list of domains.  `domainSettingsDatabaseId` highlights the domain that was highlighted before the rotation.
                populateDomainsListView(domainSettingsDatabaseIdBeforeRotate);
            } else {  // The device is in single-paned mode.
                // Reset `restartAfterRotate`.
                restartAfterRotate = false;

                // Store the current domain database ID.
                currentDomainDatabaseId = domainSettingsDatabaseIdBeforeRotate;

                // Add `currentDomainDatabaseId` to `argumentsBundle`.
                Bundle argumentsBundle = new Bundle();
                argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, currentDomainDatabaseId);

                // Add `argumentsBundle` to `domainSettingsFragment`.
                DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
                domainSettingsFragment.setArguments(argumentsBundle);

                // Show `deleteMenuItem`.
                deleteMenuItem.setVisible(true);

                // Hide the add domain floating action button.
                addDomainFAB.hide();

                // Display `domainSettingsFragment`.
                fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
            }
        } else {  // The device was not rotated or, if it was, domain settings were not displayed previously.
            if (goDirectlyToDatabaseId >=0) {  // Load the indicated domain settings.
                // Store the current domain database ID.
                currentDomainDatabaseId = goDirectlyToDatabaseId;

                if (twoPanedMode) {  // The device is in two-paned mode.
                    // Display `DomainsListFragment`.
                    DomainsListFragment domainsListFragment = new DomainsListFragment();
                    fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
                    fragmentManager.executePendingTransactions();

                    // Populate the list of domains.  `domainSettingsDatabaseId` highlights the domain that was highlighted before the rotation.
                    populateDomainsListView(goDirectlyToDatabaseId);
                } else {  // The device is in single-paned mode.
                    // Add the domain ID to be loaded to `argumentsBundle`.
                    Bundle argumentsBundle = new Bundle();
                    argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, goDirectlyToDatabaseId);

                    // Add `argumentsBundle` to `domainSettingsFragment`.
                    DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
                    domainSettingsFragment.setArguments(argumentsBundle);

                    // Show `deleteMenuItem`.
                    deleteMenuItem.setVisible(true);

                    // Hide the add domain floating action button.
                    addDomainFAB.hide();

                    // Display `domainSettingsFragment`.
                    fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
                }
            } else {  // Highlight the first domain.
                // Display `DomainsListFragment`.
                DomainsListFragment domainsListFragment = new DomainsListFragment();
                fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
                fragmentManager.executePendingTransactions();

                // Populate the list of domains.  `-1` highlights the first domain.
                populateDomainsListView(-1);
            }
        }

        // Success!
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the ID of the menu item that was selected.
        int menuItemID = menuItem.getItemId();

        // Get a handle for the fragment manager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (menuItemID) {
            case android.R.id.home:  // The home arrow is identified as `android.R.id.home`, not just `R.id.home`.
                if (twoPanedMode) {  // The device is in two-paned mode.
                    // Save the current domain settings if the domain settings fragment is displayed.
                    if (findViewById(R.id.domain_settings_scrollview) != null) {
                        saveDomainSettings(coordinatorLayout, resources);
                    }

                    // Dismiss the undo delete `SnackBar` if it is shown.
                    if (undoDeleteSnackbar != null && undoDeleteSnackbar.isShown()) {
                        // Set the close flag.
                        closeActivityAfterDismissingSnackbar = true;

                        // Dismiss the snackbar.
                        undoDeleteSnackbar.dismiss();
                    } else {
                        // Go home.
                        NavUtils.navigateUpFromSameTask(this);
                    }
                } else if (closeOnBack) {  // Go directly back to the main WebView activity because the domains activity was launched from the options menu.
                    // Save the current domain settings.
                    saveDomainSettings(coordinatorLayout, resources);

                    // Go home.
                    NavUtils.navigateUpFromSameTask(this);
                } else if (findViewById(R.id.domain_settings_scrollview) != null) {  // The device is in single-paned mode and the domain settings fragment is displayed.
                    // Save the current domain settings.
                    saveDomainSettings(coordinatorLayout, resources);

                    // Display the domains list fragment.
                    DomainsListFragment domainsListFragment = new DomainsListFragment();
                    fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
                    fragmentManager.executePendingTransactions();

                    // Populate the list of domains.  `-1` highlights the first domain if in two-paned mode.  It has no effect in single-paned mode.
                    populateDomainsListView(-1);

                    // Show the add domain floating action button.
                    addDomainFAB.show();

                    // Hide the delete menu item.
                    deleteMenuItem.setVisible(false);
                } else {  // The device is in single-paned mode and `DomainsListFragment` is displayed.
                    // Dismiss the undo delete `SnackBar` if it is shown.
                    if (undoDeleteSnackbar != null && undoDeleteSnackbar.isShown()) {
                        // Set the close flag.
                        closeActivityAfterDismissingSnackbar = true;

                        // Dismiss the snackbar.
                        undoDeleteSnackbar.dismiss();
                    } else {
                        // Go home.
                        NavUtils.navigateUpFromSameTask(this);
                    }
                }
                break;

            case R.id.delete_domain:
                // Reset close-on-back, which otherwise can cause errors if the system attempts to save settings for a domain that no longer exists.
                closeOnBack = false;

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
                    fragmentManager.beginTransaction().remove(Objects.requireNonNull(fragmentManager.findFragmentById(R.id.domain_settings_fragment_container))).commit();
                } else {  // Single-paned mode.
                    // Display `DomainsListFragment`.
                    DomainsListFragment domainsListFragment = new DomainsListFragment();
                    fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
                    fragmentManager.executePendingTransactions();

                    // Show the add domain floating action button.
                    addDomainFAB.show();

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
                        TextView domainNameTextView = view.findViewById(R.id.domain_name_textview);
                        domainNameTextView.setText(domainNameString);
                    }
                };

                // Update the handle for the current `domains_listview`.
                domainsListView = findViewById(R.id.domains_listview);

                // Update the `ListView`.
                domainsListView.setAdapter(domainsPendingDeleteCursorAdapter);

                // Get a handle for the activity.
                Activity activity = this;

                // Display a `Snackbar`.
                undoDeleteSnackbar = Snackbar.make(domainsListView, R.string.domain_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, (View v) -> {
                            // Do nothing because everything will be handled by `onDismissed()` below.
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                // Run commands based on the event.
                                if (event == Snackbar.Callback.DISMISS_EVENT_ACTION) {  // The user pushed the `Undo` button.
                                    // Store the database ID in arguments bundle.
                                    Bundle argumentsBundle = new Bundle();
                                    argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, databaseIdToDelete);

                                    // Add the arguments bundle to the domain settings fragment.
                                    DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
                                    domainSettingsFragment.setArguments(argumentsBundle);

                                    // Display the correct fragments.
                                    if (twoPanedMode) {  // The device in in two-paned mode.
                                        // Get a `Cursor` with the current contents of the domains database.
                                        Cursor undoDeleteDomainsCursor = domainsDatabaseHelper.getDomainNameCursorOrderedByDomain();

                                        // Setup `domainsCursorAdapter` with `this` context.  `false` disables `autoRequery`.
                                        CursorAdapter undoDeleteDomainsCursorAdapter = new CursorAdapter(getApplicationContext(), undoDeleteDomainsCursor, false) {
                                            @Override
                                            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                                                // Inflate the individual item layout.  `false` does not attach it to the root.
                                                return getLayoutInflater().inflate(R.layout.domain_name_linearlayout, parent, false);
                                            }

                                            @Override
                                            public void bindView(View view, Context context, Cursor cursor) {
                                                // Set the domain name.
                                                String domainNameString = cursor.getString(cursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
                                                TextView domainNameTextView = view.findViewById(R.id.domain_name_textview);
                                                domainNameTextView.setText(domainNameString);
                                            }
                                        };

                                        // Update the `ListView`.
                                        domainsListView.setAdapter(undoDeleteDomainsCursorAdapter);
                                        // Select the previously deleted domain in `domainsListView`.
                                        domainsListView.setItemChecked(deletedDomainPosition, true);

                                        // Display `domainSettingsFragment`.
                                        fragmentManager.beginTransaction().replace(R.id.domain_settings_fragment_container, domainSettingsFragment).commit();

                                        // Enable the options `MenuItems`.
                                        deleteMenuItem.setEnabled(true);
                                        deleteMenuItem.setIcon(R.drawable.delete_light);
                                    } else {  // The device in in one-paned mode.
                                        // Display `domainSettingsFragment`.
                                        fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();

                                        // Hide the add domain floating action button.
                                        addDomainFAB.hide();

                                        // Show and enable `deleteMenuItem`.
                                        deleteMenuItem.setVisible(true);

                                        // Display `domainSettingsFragment`.
                                        fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
                                    }
                                } else {  // The snackbar was dismissed without the undo button being pushed.
                                    // Delete the selected domain.
                                    domainsDatabaseHelper.deleteDomain(databaseIdToDelete);

                                    // Enable the delete menu item if the system was waiting for a snackbar to be dismissed.
                                    if (dismissingSnackbar) {
                                        // Create a `Runnable` to enable the delete menu item.
                                        Runnable enableDeleteMenuItemRunnable = () -> {
                                            // Enable `deleteMenuItem` according to the display mode.
                                            if (twoPanedMode) {  // Two-paned mode.
                                                // Enable the delete menu item.
                                                deleteMenuItem.setEnabled(true);

                                                // Get a handle for the shared preferences.
                                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                                                // Get the theme preferences.
                                                boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

                                                // Set the delete icon according to the theme.
                                                if (darkTheme) {
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
                                        };

                                        // Enable the delete menu icon after 100 milliseconds to make sure that the previous domain has been deleted from the database.
                                        Handler handler = new Handler();
                                        handler.postDelayed(enableDeleteMenuItemRunnable, 100);
                                    }

                                    // Close the activity if back was pressed.
                                    if (closeActivityAfterDismissingSnackbar) {
                                        // Go home.
                                        NavUtils.navigateUpFromSameTask(activity);
                                    }
                                }
                            }
                        });

                // Show the Snackbar.
                undoDeleteSnackbar.show();
                break;
        }

        // Consume the event.
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // Store the current `DomainSettingsFragment` state in `outState`.
        if (findViewById(R.id.domain_settings_scrollview) != null) {  // `DomainSettingsFragment` is displayed.
            // Save any changes that have been made to the domain settings.
            saveDomainSettings(coordinatorLayout, resources);

            // Store `DomainSettingsDisplayed`.
            outState.putBoolean("domain_settings_displayed", true);
            outState.putInt("domain_settings_database_id", DomainSettingsFragment.databaseId);
        } else {  // `DomainSettingsFragment` is not displayed.
            outState.putBoolean("domain_settings_displayed", false);
            outState.putInt("domain_settings_database_id", -1);
        }

        super.onSaveInstanceState(outState);
    }

    // Control what the navigation bar back button does.
    @Override
    public void onBackPressed() {
        // Get a handle for the fragment manager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (twoPanedMode) {  // The device is in two-paned mode.
            // Save the current domain settings if the domain settings fragment is displayed.
            if (findViewById(R.id.domain_settings_scrollview) != null) {
                saveDomainSettings(coordinatorLayout, resources);
            }

            // Dismiss the undo delete SnackBar if it is shown.
            if ((undoDeleteSnackbar != null) && undoDeleteSnackbar.isShown()) {
                // Set the close flag.
                closeActivityAfterDismissingSnackbar = true;

                // Dismiss the snackbar.
                undoDeleteSnackbar.dismiss();
            } else {
                // Pass `onBackPressed()` to the system.
                super.onBackPressed();
            }
        } else if (closeOnBack) {  // Go directly back to the main WebView activity because the domains activity was launched from the options menu.
            // Save the current domain settings.
            saveDomainSettings(coordinatorLayout, resources);

            // Pass `onBackPressed()` to the system.
            super.onBackPressed();
        } else if (findViewById(R.id.domain_settings_scrollview) != null) {  // The device is in single-paned mode and domain settings fragment is displayed.
            // Save the current domain settings.
            saveDomainSettings(coordinatorLayout, resources);

            // Display the domains list fragment.
            DomainsListFragment domainsListFragment = new DomainsListFragment();
            fragmentManager.beginTransaction().replace(R.id.domains_listview_fragment_container, domainsListFragment).commit();
            fragmentManager.executePendingTransactions();

            // Populate the list of domains.  `-1` highlights the first domain if in two-paned mode.  It has no effect in single-paned mode.
            populateDomainsListView(-1);

            // Show the add domain floating action button.
            addDomainFAB.show();

            // Hide the delete menu item.
            deleteMenuItem.setVisible(false);
        } else {  // The device is in single-paned mode and the domain list fragment is displayed.
            // Dismiss the undo delete SnackBar if it is shown.
            if ((undoDeleteSnackbar != null) && undoDeleteSnackbar.isShown()) {
                // Set the close flag.
                closeActivityAfterDismissingSnackbar = true;

                // Dismiss the snackbar.
                undoDeleteSnackbar.dismiss();
            } else {
                // Pass `onBackPressed()` to the system.
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onAddDomain(@NonNull DialogFragment dialogFragment) {
        // Dismiss the undo delete snackbar if it is currently displayed.
        if ((undoDeleteSnackbar != null) && undoDeleteSnackbar.isShown()) {
            undoDeleteSnackbar.dismiss();
        }

        // Remove the incorrect lint warning below that the dialog might be null.
        assert dialogFragment.getDialog() != null;

        // Get a handle for the domain name edit text.
        EditText domainNameEditText = dialogFragment.getDialog().findViewById(R.id.domain_name_edittext);

        // Get the domain name string.
        String domainNameString = domainNameEditText.getText().toString();

        // Create the domain and store the database ID in `currentDomainDatabaseId`.
        currentDomainDatabaseId = domainsDatabaseHelper.addDomain(domainNameString);

        // Display the newly created domain.
        if (twoPanedMode) {  // The device in in two-paned mode.
            populateDomainsListView(currentDomainDatabaseId);
        } else {  // The device is in single-paned mode.
            // Hide the add domain floating action button.
            addDomainFAB.hide();

            // Show and enable `deleteMenuItem`.
            DomainsActivity.deleteMenuItem.setVisible(true);

            // Add the current domain database ID to the arguments bundle.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, currentDomainDatabaseId);

            // Add and arguments bundle to the domain setting fragment.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Display the domain settings fragment.
            getSupportFragmentManager().beginTransaction().replace(R.id.domains_listview_fragment_container, domainSettingsFragment).commit();
        }
    }

    public void saveDomainSettings(View view, Resources resources) {
        // Get handles for the domain settings.
        EditText domainNameEditText = view.findViewById(R.id.domain_settings_name_edittext);
        Switch javaScriptSwitch = view.findViewById(R.id.javascript_switch);
        Switch firstPartyCookiesSwitch = view.findViewById(R.id.first_party_cookies_switch);
        Switch thirdPartyCookiesSwitch = view.findViewById(R.id.third_party_cookies_switch);
        Switch domStorageSwitch = view.findViewById(R.id.dom_storage_switch);
        Switch formDataSwitch = view.findViewById(R.id.form_data_switch);  // Form data can be removed once the minimum API >= 26.
        Switch easyListSwitch = view.findViewById(R.id.easylist_switch);
        Switch easyPrivacySwitch = view.findViewById(R.id.easyprivacy_switch);
        Switch fanboysAnnoyanceSwitch = view.findViewById(R.id.fanboys_annoyance_list_switch);
        Switch fanboysSocialBlockingSwitch = view.findViewById(R.id.fanboys_social_blocking_list_switch);
        Switch ultraListSwitch = view.findViewById(R.id.ultralist_switch);
        Switch ultraPrivacySwitch = view.findViewById(R.id.ultraprivacy_switch);
        Switch blockAllThirdPartyRequestsSwitch = view.findViewById(R.id.block_all_third_party_requests_switch);
        Spinner userAgentSpinner = view.findViewById(R.id.user_agent_spinner);
        EditText customUserAgentEditText = view.findViewById(R.id.custom_user_agent_edittext);
        Spinner fontSizeSpinner = view.findViewById(R.id.font_size_spinner);
        EditText customFontSizeEditText = view.findViewById(R.id.custom_font_size_edittext);
        Spinner swipeToRefreshSpinner = view.findViewById(R.id.swipe_to_refresh_spinner);
        Spinner nightModeSpinner = view.findViewById(R.id.night_mode_spinner);
        Spinner wideViewportSpinner = view.findViewById(R.id.wide_viewport_spinner);
        Spinner displayWebpageImagesSpinner = view.findViewById(R.id.display_webpage_images_spinner);
        Switch pinnedSslCertificateSwitch = view.findViewById(R.id.pinned_ssl_certificate_switch);
        RadioButton currentWebsiteCertificateRadioButton = view.findViewById(R.id.current_website_certificate_radiobutton);
        Switch pinnedIpAddressesSwitch = view.findViewById(R.id.pinned_ip_addresses_switch);
        RadioButton currentIpAddressesRadioButton = view.findViewById(R.id.current_ip_addresses_radiobutton);

        // Extract the data for the domain settings.
        String domainNameString = domainNameEditText.getText().toString();
        boolean javaScript = javaScriptSwitch.isChecked();
        boolean firstPartyCookies = firstPartyCookiesSwitch.isChecked();
        boolean thirdPartyCookies = thirdPartyCookiesSwitch.isChecked();
        boolean domStorage  = domStorageSwitch.isChecked();
        boolean formData = formDataSwitch.isChecked();  // Form data can be removed once the minimum API >= 26.
        boolean easyList = easyListSwitch.isChecked();
        boolean easyPrivacy = easyPrivacySwitch.isChecked();
        boolean fanboysAnnoyance = fanboysAnnoyanceSwitch.isChecked();
        boolean fanboysSocialBlocking = fanboysSocialBlockingSwitch.isChecked();
        boolean ultraList = ultraListSwitch.isChecked();
        boolean ultraPrivacy = ultraPrivacySwitch.isChecked();
        boolean blockAllThirdPartyRequests = blockAllThirdPartyRequestsSwitch.isChecked();
        int userAgentSwitchPosition = userAgentSpinner.getSelectedItemPosition();
        int fontSizeSwitchPosition = fontSizeSpinner.getSelectedItemPosition();
        int swipeToRefreshInt = swipeToRefreshSpinner.getSelectedItemPosition();
        int nightModeInt = nightModeSpinner.getSelectedItemPosition();
        int wideViewportInt = wideViewportSpinner.getSelectedItemPosition();
        int displayWebpageImagesInt = displayWebpageImagesSpinner.getSelectedItemPosition();
        boolean pinnedSslCertificate = pinnedSslCertificateSwitch.isChecked();
        boolean pinnedIpAddress = pinnedIpAddressesSwitch.isChecked();

        // Initialize the user agent name string.
        String userAgentName;

        // Set the user agent name.
        switch (userAgentSwitchPosition) {
            case MainWebViewActivity.DOMAINS_SYSTEM_DEFAULT_USER_AGENT:
                // Set the user agent name to be `System default user agent`.
                userAgentName = resources.getString(R.string.system_default_user_agent);
                break;

            case MainWebViewActivity.DOMAINS_CUSTOM_USER_AGENT:
                // Set the user agent name to be the custom user agent.
                userAgentName = customUserAgentEditText.getText().toString();
                break;

            default:
                // Get the array of user agent names.
                String[] userAgentNameArray = resources.getStringArray(R.array.user_agent_names);

                // Set the user agent name from the array.  The domain spinner has one more entry than the name array, so the position must be decremented.
                userAgentName = userAgentNameArray[userAgentSwitchPosition - 1];
        }

        // Initialize the font size integer.  `0` indicates the system default font size.
        int fontSizeInt = 0;

        // Use a custom font size if it is selected.
        if (fontSizeSwitchPosition == 1) {  // A custom font size is specified.
            // Get the custom font size from the edit text.
            fontSizeInt = Integer.parseInt(customFontSizeEditText.getText().toString());
        }

        // Save the domain settings.
        domainsDatabaseHelper.updateDomain(DomainsActivity.currentDomainDatabaseId, domainNameString, javaScript, firstPartyCookies, thirdPartyCookies, domStorage, formData, easyList, easyPrivacy,
                fanboysAnnoyance, fanboysSocialBlocking, ultraList, ultraPrivacy, blockAllThirdPartyRequests, userAgentName, fontSizeInt, swipeToRefreshInt, nightModeInt, wideViewportInt,
                displayWebpageImagesInt, pinnedSslCertificate, pinnedIpAddress);

        // Update the pinned SSL certificate if a new one is checked.
        if (currentWebsiteCertificateRadioButton.isChecked()) {
            // Update the database.
            domainsDatabaseHelper.updatePinnedSslCertificate(currentDomainDatabaseId, sslIssuedToCName, sslIssuedToOName, sslIssuedToUName, sslIssuedByCName, sslIssuedByOName, sslIssuedByUName,
                    sslStartDateLong, sslEndDateLong);
        }

        // Update the pinned IP addresses if new ones are checked.
        if (currentIpAddressesRadioButton.isChecked()) {
            // Update the database.
            domainsDatabaseHelper.updatePinnedIpAddresses(currentDomainDatabaseId, currentIpAddresses);
        }
    }

    private void populateDomainsListView(final int highlightedDomainDatabaseId) {
        // get a handle for the current `domains_listview`.
        domainsListView = findViewById(R.id.domains_listview);

        // Get a `Cursor` with the current contents of the domains database.
        Cursor domainsCursor = domainsDatabaseHelper.getDomainNameCursorOrderedByDomain();

        // Setup `domainsCursorAdapter` with `this` context.  `false` disables `autoRequery`.
        CursorAdapter domainsCursorAdapter = new CursorAdapter(getApplicationContext(), domainsCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.domain_name_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Set the domain name.
                String domainNameString = cursor.getString(cursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME));
                TextView domainNameTextView = view.findViewById(R.id.domain_name_textview);
                domainNameTextView.setText(domainNameString);
            }
        };

        // Update the list view.
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

            // Get the database ID for the highlighted domain.
            domainsCursor.moveToPosition(highlightedDomainPosition);
            currentDomainDatabaseId = domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper._ID));

            // Store the database ID in the arguments bundle.
            Bundle argumentsBundle = new Bundle();
            argumentsBundle.putInt(DomainSettingsFragment.DATABASE_ID, currentDomainDatabaseId);

            // Add and arguments bundle to the domain settings fragment.
            DomainSettingsFragment domainSettingsFragment = new DomainSettingsFragment();
            domainSettingsFragment.setArguments(argumentsBundle);

            // Display the domain settings fragment.
            getSupportFragmentManager().beginTransaction().replace(R.id.domain_settings_fragment_container, domainSettingsFragment).commit();

            // Enable the delete options menu items.
            deleteMenuItem.setEnabled(true);

            // Get a handle for the shared preferences.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            // Get the theme and screenshot preferences.
            boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

            // Set the delete icon according to the theme.
            if (darkTheme) {
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

    @Override
    public void dismissSnackbar() {
        // Dismiss the undo delete snackbar if it is shown.
        if (undoDeleteSnackbar != null && undoDeleteSnackbar.isShown()) {
            // Dismiss the snackbar.
            undoDeleteSnackbar.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        // Close the domains database helper.
        domainsDatabaseHelper.close();

        // Run the default commands.
        super.onDestroy();
    }
}