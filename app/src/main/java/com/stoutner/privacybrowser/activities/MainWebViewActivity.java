/*
 * Copyright © 2015-2019 Soren Stoutner <soren@stoutner.com>.
 *
 * Download cookie code contributed 2017 Hendrik Knackstedt.  Copyright assigned to Soren Stoutner <soren@stoutner.com>.
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import com.stoutner.privacybrowser.BuildConfig;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.adapters.WebViewPagerAdapter;
import com.stoutner.privacybrowser.asynctasks.GetHostIpAddresses;
import com.stoutner.privacybrowser.dialogs.AdConsentDialog;
import com.stoutner.privacybrowser.dialogs.CreateBookmarkDialog;
import com.stoutner.privacybrowser.dialogs.CreateBookmarkFolderDialog;
import com.stoutner.privacybrowser.dialogs.CreateHomeScreenShortcutDialog;
import com.stoutner.privacybrowser.dialogs.DownloadFileDialog;
import com.stoutner.privacybrowser.dialogs.DownloadImageDialog;
import com.stoutner.privacybrowser.dialogs.DownloadLocationPermissionDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkFolderDialog;
import com.stoutner.privacybrowser.dialogs.HttpAuthenticationDialog;
import com.stoutner.privacybrowser.dialogs.SslCertificateErrorDialog;
import com.stoutner.privacybrowser.dialogs.UrlHistoryDialog;
import com.stoutner.privacybrowser.dialogs.ViewSslCertificateDialog;
import com.stoutner.privacybrowser.fragments.WebViewTabFragment;
import com.stoutner.privacybrowser.helpers.AdHelper;
import com.stoutner.privacybrowser.helpers.BlockListHelper;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;
import com.stoutner.privacybrowser.helpers.CheckPinnedMismatchHelper;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;
import com.stoutner.privacybrowser.helpers.OrbotProxyHelper;
import com.stoutner.privacybrowser.views.NestedScrollWebView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// AppCompatActivity from android.support.v7.app.AppCompatActivity must be used to have access to the SupportActionBar until the minimum API is >= 21.
public class MainWebViewActivity extends AppCompatActivity implements CreateBookmarkDialog.CreateBookmarkListener, CreateBookmarkFolderDialog.CreateBookmarkFolderListener,
        DownloadFileDialog.DownloadFileListener, DownloadImageDialog.DownloadImageListener, DownloadLocationPermissionDialog.DownloadLocationPermissionDialogListener, EditBookmarkDialog.EditBookmarkListener,
        EditBookmarkFolderDialog.EditBookmarkFolderListener, NavigationView.OnNavigationItemSelectedListener, WebViewTabFragment.NewTabListener {

    // `orbotStatus` is public static so it can be accessed from `OrbotProxyHelper`.  It is also used in `onCreate()`, `onResume()`, and `applyProxyThroughOrbot()`.
    public static String orbotStatus;

    // The WebView pager adapter is accessed from `HttpAuthenticationDialog`, `PinnedMismatchDialog`, and `SslCertificateErrorDialog`.  It is also used in `onCreate()`, `onResume()`, and `addTab()`.
    public static WebViewPagerAdapter webViewPagerAdapter;

    // The load URL on restart variables are public static so they can be accessed from `BookmarksActivity`.  They are used in `onRestart()`.
    public static boolean loadUrlOnRestart;
    public static String urlToLoadOnRestart;

    // `restartFromBookmarksActivity` is public static so it can be accessed from `BookmarksActivity`.  It is also used in `onRestart()`.
    public static boolean restartFromBookmarksActivity;

    // `currentBookmarksFolder` is public static so it can be accessed from `BookmarksActivity`.  It is also used in `onCreate()`, `onBackPressed()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`,
    // `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`, and `loadBookmarksFolder()`.
    public static String currentBookmarksFolder;

    // The user agent constants are public static so they can be accessed from `SettingsFragment`, `DomainsActivity`, and `DomainSettingsFragment`.
    public final static int UNRECOGNIZED_USER_AGENT = -1;
    public final static int SETTINGS_WEBVIEW_DEFAULT_USER_AGENT = 1;
    public final static int SETTINGS_CUSTOM_USER_AGENT = 12;
    public final static int DOMAINS_SYSTEM_DEFAULT_USER_AGENT = 0;
    public final static int DOMAINS_WEBVIEW_DEFAULT_USER_AGENT = 2;
    public final static int DOMAINS_CUSTOM_USER_AGENT = 13;



    // The current WebView is used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onRestart()`, `onCreateContextMenu()`, `findPreviousOnPage()`,
    // `findNextOnPage()`, `closeFindOnPage()`, `loadUrlFromTextBox()`, `onSslMismatchBack()`, `applyProxyThroughOrbot()`, and `applyDomainSettings()`.
    private NestedScrollWebView currentWebView;

    // `customHeader` is used in `onCreate()`, `onOptionsItemSelected()`, `onCreateContextMenu()`, and `loadUrl()`.
    private final Map<String, String> customHeaders = new HashMap<>();

    // The search URL is set in `applyProxyThroughOrbot()` and used in `onCreate()`, `onNewIntent()`, `loadURLFromTextBox()`, and `initializeWebView()`.
    private String searchURL;

    // The options menu is set in `onCreateOptionsMenu()` and used in `onOptionsItemSelected()`, `updatePrivacyIcons()`, and `initializeWebView()`.
    private Menu optionsMenu;

    // The blocklists are populated in `onCreate()` and accessed from `initializeWebView()`.
    private ArrayList<List<String[]>> easyList;
    private ArrayList<List<String[]>> easyPrivacy;
    private ArrayList<List<String[]>> fanboysAnnoyanceList;
    private ArrayList<List<String[]>> fanboysSocialList;
    private ArrayList<List<String[]>> ultraPrivacy;

    // `webViewDefaultUserAgent` is used in `onCreate()` and `onPrepareOptionsMenu()`.
    private String webViewDefaultUserAgent;

    // `proxyThroughOrbot` is used in `onRestart()`, `onOptionsItemSelected()`, `applyAppSettings()`, and `applyProxyThroughOrbot()`.
    private boolean proxyThroughOrbot;

    // The incognito mode is set in `applyAppSettings()` and used in `initializeWebView()`.
    private boolean incognitoModeEnabled;

    // The full screen browsing mode tracker is set it `applyAppSettings()` and used in `initializeWebView()`.
    private boolean fullScreenBrowsingModeEnabled;

    // `inFullScreenBrowsingMode` is used in `onCreate()`, `onConfigurationChanged()`, and `applyAppSettings()`.
    private boolean inFullScreenBrowsingMode;

    // The app bar trackers are set in `applyAppSettings()` and used in `initializeWebView()`.
    private boolean hideAppBar;
    private boolean scrollAppBar;

    // The loading new intent tracker is set in `onNewIntent()` and used in `setCurrentWebView()`.
    private boolean loadingNewIntent;

    // `reapplyDomainSettingsOnRestart` is used in `onCreate()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onRestart()`, and `onAddDomain()`, .
    private boolean reapplyDomainSettingsOnRestart;

    // `reapplyAppSettingsOnRestart` is used in `onNavigationItemSelected()` and `onRestart()`.
    private boolean reapplyAppSettingsOnRestart;

    // `displayingFullScreenVideo` is used in `onCreate()` and `onResume()`.
    private boolean displayingFullScreenVideo;

    // `orbotStatusBroadcastReceiver` is used in `onCreate()` and `onDestroy()`.
    private BroadcastReceiver orbotStatusBroadcastReceiver;

    // `waitingForOrbot` is used in `onCreate()`, `onResume()`, and `applyProxyThroughOrbot()`.
    private boolean waitingForOrbot;

    // The action bar drawer toggle is initialized in `onCreate()` and used in `onResume()`.
    private ActionBarDrawerToggle actionBarDrawerToggle;

    // The color spans are used in `onCreate()` and `highlightUrlText()`.
    private ForegroundColorSpan redColorSpan;
    private ForegroundColorSpan initialGrayColorSpan;
    private ForegroundColorSpan finalGrayColorSpan;

    // The drawer header padding variables are used in `onCreate()` and `onConfigurationChanged()`.
    private int drawerHeaderPaddingLeftAndRight;
    private int drawerHeaderPaddingTop;
    private int drawerHeaderPaddingBottom;

    // `bookmarksDatabaseHelper` is used in `onCreate()`, `onDestroy`, `onOptionsItemSelected()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`,
    // and `loadBookmarksFolder()`.
    private BookmarksDatabaseHelper bookmarksDatabaseHelper;

    // `bookmarksCursor` is used in `onDestroy()`, `onOptionsItemSelected()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`, and `loadBookmarksFolder()`.
    private Cursor bookmarksCursor;

    // `bookmarksCursorAdapter` is used in `onCreateBookmark()`, `onCreateBookmarkFolder()` `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`, and `loadBookmarksFolder()`.
    private CursorAdapter bookmarksCursorAdapter;

    // `oldFolderNameString` is used in `onCreate()` and `onSaveEditBookmarkFolder()`.
    private String oldFolderNameString;

    // `fileChooserCallback` is used in `onCreate()` and `onActivityResult()`.
    private ValueCallback<Uri[]> fileChooserCallback;

    // The default progress view offsets are set in `onCreate()` and used in `initializeWebView()`.
    private int defaultProgressViewStartOffset;
    private int defaultProgressViewEndOffset;

    // The swipe refresh layout top padding is used when exiting full screen browsing mode.  It is used in an inner class in `initializeWebView()`.
    private int swipeRefreshLayoutPaddingTop;

    // The URL sanitizers are set in `applyAppSettings()` and used in `sanitizeUrl()`.
    private boolean sanitizeGoogleAnalytics;
    private boolean sanitizeFacebookClickIds;

    // The download strings are used in `onCreate()`, `onRequestPermissionResult()` and `initializeWebView()`.
    private String downloadUrl;
    private String downloadContentDisposition;
    private long downloadContentLength;

    // `downloadImageUrl` is used in `onCreateContextMenu()` and `onRequestPermissionResult()`.
    private String downloadImageUrl;

    // The request codes are used in `onCreate()`, `onCreateContextMenu()`, `onCloseDownloadLocationPermissionDialog()`, `onRequestPermissionResult()`, and `initializeWebView()`.
    private final int DOWNLOAD_FILE_REQUEST_CODE = 1;
    private final int DOWNLOAD_IMAGE_REQUEST_CODE = 2;

    @Override
    // Remove the warning about needing to override `performClick()` when using an `OnTouchListener` with `WebView`.
    @SuppressLint("ClickableViewAccessibility")
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
            setTheme(R.style.PrivacyBrowserDark);
        } else {
            setTheme(R.style.PrivacyBrowserLight);
        }

        // Run the default commands.
        super.onCreate(savedInstanceState);

        // Set the content view.
        setContentView(R.layout.main_framelayout);

        // Get a handle for the input method.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Remove the lint warning below that the input method manager might be null.
        assert inputMethodManager != null;

        // Get a handle for the toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set the action bar.  `SupportActionBar` must be used until the minimum API is >= 21.
        setSupportActionBar(toolbar);

        // Get a handle for the action bar.
        ActionBar actionBar = getSupportActionBar();

        // This is needed to get rid of the Android Studio warning that the action bar might be null.
        assert actionBar != null;

        // Add the custom layout, which shows the URL text bar.
        actionBar.setCustomView(R.layout.url_app_bar);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Initialize the foreground color spans for highlighting the URLs.  We have to use the deprecated `getColor()` until API >= 23.
        redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));
        initialGrayColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.gray_500));
        finalGrayColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.gray_500));

        // Get handles for the URL views.
        EditText urlEditText = findViewById(R.id.url_edittext);

        // Remove the formatting from `urlTextBar` when the user is editing the text.
        urlEditText.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (hasFocus) {  // The user is editing the URL text box.
                // Remove the highlighting.
                urlEditText.getText().removeSpan(redColorSpan);
                urlEditText.getText().removeSpan(initialGrayColorSpan);
                urlEditText.getText().removeSpan(finalGrayColorSpan);
            } else {  // The user has stopped editing the URL text box.
                // Move to the beginning of the string.
                urlEditText.setSelection(0);

                // Reapply the highlighting.
                highlightUrlText();
            }
        });

        // Set the go button on the keyboard to load the URL in `urlTextBox`.
        urlEditText.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
            // If the event is a key-down event on the `enter` button, load the URL.
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Load the URL into the mainWebView and consume the event.
                loadUrlFromTextBox();

                // If the enter key was pressed, consume the event.
                return true;
            } else {
                // If any other key was pressed, do not consume the event.
                return false;
            }
        });

        // Initialize the Orbot status and the waiting for Orbot trackers.
        orbotStatus = "unknown";
        waitingForOrbot = false;

        // Create an Orbot status `BroadcastReceiver`.
        orbotStatusBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Store the content of the status message in `orbotStatus`.
                orbotStatus = intent.getStringExtra("org.torproject.android.intent.extra.STATUS");

                // If Privacy Browser is waiting on Orbot, load the website now that Orbot is connected.
                if (orbotStatus.equals("ON") && waitingForOrbot) {
                    // Reset the waiting for Orbot status.
                    waitingForOrbot = false;

                    // Get the intent that started the app.
                    Intent launchingIntent = getIntent();

                    // Get the information from the intent.
                    String launchingIntentAction = launchingIntent.getAction();
                    Uri launchingIntentUriData = launchingIntent.getData();

                    // If the intent action is a web search, perform the search.
                    if ((launchingIntentAction != null) && launchingIntentAction.equals(Intent.ACTION_WEB_SEARCH)) {
                        // Create an encoded URL string.
                        String encodedUrlString;

                        // Sanitize the search input and convert it to a search.
                        try {
                            encodedUrlString = URLEncoder.encode(launchingIntent.getStringExtra(SearchManager.QUERY), "UTF-8");
                        } catch (UnsupportedEncodingException exception) {
                            encodedUrlString = "";
                        }

                        // Load the completed search URL.
                        loadUrl(searchURL + encodedUrlString);
                    } else if (launchingIntentUriData != null){  // Check to see if the intent contains a new URL.
                        // Load the URL from the intent.
                        loadUrl(launchingIntentUriData.toString());
                    } else {  // The is no URL in the intent.
                        // Select the homepage based on the proxy through Orbot status.
                        if (proxyThroughOrbot) {
                            // Load the Tor homepage.
                            loadUrl(sharedPreferences.getString("tor_homepage", getString(R.string.tor_homepage_default_value)));
                        } else {
                            // Load the normal homepage.
                            loadUrl(sharedPreferences.getString("homepage", getString(R.string.homepage_default_value)));
                        }
                    }
                }
            }
        };

        // Register `orbotStatusBroadcastReceiver` on `this` context.
        this.registerReceiver(orbotStatusBroadcastReceiver, new IntentFilter("org.torproject.android.intent.action.STATUS"));

        // Instantiate the blocklist helper.
        BlockListHelper blockListHelper = new BlockListHelper();

        // Parse the block lists.
        easyList = blockListHelper.parseBlockList(getAssets(), "blocklists/easylist.txt");
        easyPrivacy = blockListHelper.parseBlockList(getAssets(), "blocklists/easyprivacy.txt");
        fanboysAnnoyanceList = blockListHelper.parseBlockList(getAssets(), "blocklists/fanboy-annoyance.txt");
        fanboysSocialList = blockListHelper.parseBlockList(getAssets(), "blocklists/fanboy-social.txt");
        ultraPrivacy = blockListHelper.parseBlockList(getAssets(), "blocklists/ultraprivacy.txt");

        // Get handles for views that need to be modified.
        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);
        NavigationView navigationView = findViewById(R.id.navigationview);
        TabLayout tabLayout = findViewById(R.id.tablayout);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        ViewPager webViewPager = findViewById(R.id.webviewpager);
        ListView bookmarksListView = findViewById(R.id.bookmarks_drawer_listview);
        FloatingActionButton launchBookmarksActivityFab = findViewById(R.id.launch_bookmarks_activity_fab);
        FloatingActionButton createBookmarkFolderFab = findViewById(R.id.create_bookmark_folder_fab);
        FloatingActionButton createBookmarkFab = findViewById(R.id.create_bookmark_fab);
        EditText findOnPageEditText = findViewById(R.id.find_on_page_edittext);

        // Listen for touches on the navigation menu.
        navigationView.setNavigationItemSelectedListener(this);

        // Get handles for the navigation menu and the back and forward menu items.  The menu is zero-based.
        Menu navigationMenu = navigationView.getMenu();
        MenuItem navigationBackMenuItem = navigationMenu.getItem(2);
        MenuItem navigationForwardMenuItem = navigationMenu.getItem(3);
        MenuItem navigationHistoryMenuItem = navigationMenu.getItem(4);
        MenuItem navigationRequestsMenuItem = navigationMenu.getItem(5);

        // Initialize the web view pager adapter.
        webViewPagerAdapter = new WebViewPagerAdapter(getSupportFragmentManager());

        // Set the pager adapter on the web view pager.
        webViewPager.setAdapter(webViewPagerAdapter);

        // Store up to 100 tabs in memory.
        webViewPager.setOffscreenPageLimit(100);

        // Update the web view pager every time a tab is modified.
        webViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing.
            }

            @Override
            public void onPageSelected(int position) {
                // Close the find on page bar if it is open.
                closeFindOnPage(null);

                // Set the current WebView.
                setCurrentWebView(position);

                // Select the corresponding tab if it does not match the currently selected page.  This will happen if the page was scrolled via swiping in the view pager or by creating a new tab.
                if (tabLayout.getSelectedTabPosition() != position) {
                    // Create a handler to select the tab.
                    Handler selectTabHandler = new Handler();

                    // Create a runnable select the new tab.
                    Runnable selectTabRunnable = () -> {
                        // Get a handle for the tab.
                        TabLayout.Tab tab = tabLayout.getTabAt(position);

                        // Assert that the tab is not null.
                        assert tab != null;

                        // Select the tab.
                        tab.select();
                    };

                    // Select the tab layout after 100 milliseconds, which leaves enough time for a new tab to be created.
                    selectTabHandler.postDelayed(selectTabRunnable, 100);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing.
            }
        });

        // Display the View SSL Certificate dialog when the currently selected tab is reselected.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Select the same page in the view pager.
                webViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing.
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Instantiate the View SSL Certificate dialog.
                DialogFragment viewSslCertificateDialogFragment = ViewSslCertificateDialog.displayDialog(currentWebView.getWebViewFragmentId());

                // Display the View SSL Certificate dialog.
                viewSslCertificateDialogFragment.show(getSupportFragmentManager(), getString(R.string.view_ssl_certificate));
            }
        });

        // Add the first tab.
        addTab(null);

        // Set the bookmarks drawer resources according to the theme.  This can't be done in the layout due to compatibility issues with the `DrawerLayout` support widget.
        // The deprecated `getResources().getDrawable()` must be used until the minimum API >= 21 and and `getResources().getColor()` must be used until the minimum API >= 23.
        if (darkTheme) {
            launchBookmarksActivityFab.setImageDrawable(getResources().getDrawable(R.drawable.bookmarks_dark));
            createBookmarkFolderFab.setImageDrawable(getResources().getDrawable(R.drawable.create_folder_dark));
            createBookmarkFab.setImageDrawable(getResources().getDrawable(R.drawable.create_bookmark_dark));
            bookmarksListView.setBackgroundColor(getResources().getColor(R.color.gray_850));
        } else {
            launchBookmarksActivityFab.setImageDrawable(getResources().getDrawable(R.drawable.bookmarks_light));
            createBookmarkFolderFab.setImageDrawable(getResources().getDrawable(R.drawable.create_folder_light));
            createBookmarkFab.setImageDrawable(getResources().getDrawable(R.drawable.create_bookmark_light));
            bookmarksListView.setBackgroundColor(getResources().getColor(R.color.white));
        }

        // Set the launch bookmarks activity FAB to launch the bookmarks activity.
        launchBookmarksActivityFab.setOnClickListener(v -> {
            // Get a copy of the favorite icon bitmap.
            Bitmap favoriteIconBitmap = currentWebView.getFavoriteOrDefaultIcon();

            // Create a favorite icon byte array output stream.
            ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

            // Convert the favorite icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
            favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

            // Convert the favorite icon byte array stream to a byte array.
            byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

            // Create an intent to launch the bookmarks activity.
            Intent bookmarksIntent = new Intent(getApplicationContext(), BookmarksActivity.class);

            // Add the extra information to the intent.
            bookmarksIntent.putExtra("current_url", currentWebView.getUrl());
            bookmarksIntent.putExtra("current_title", currentWebView.getTitle());
            bookmarksIntent.putExtra("current_folder", currentBookmarksFolder);
            bookmarksIntent.putExtra("favorite_icon_byte_array", favoriteIconByteArray);

            // Make it so.
            startActivity(bookmarksIntent);
        });

        // Set the create new bookmark folder FAB to display an alert dialog.
        createBookmarkFolderFab.setOnClickListener(v -> {
            // Create a create bookmark folder dialog.
            DialogFragment createBookmarkFolderDialog = CreateBookmarkFolderDialog.createBookmarkFolder(currentWebView.getFavoriteOrDefaultIcon());

            // Show the create bookmark folder dialog.
            createBookmarkFolderDialog.show(getSupportFragmentManager(), getString(R.string.create_folder));
        });

        // Set the create new bookmark FAB to display an alert dialog.
        createBookmarkFab.setOnClickListener(view -> {
            // Instantiate the create bookmark dialog.
            DialogFragment createBookmarkDialog = CreateBookmarkDialog.createBookmark(currentWebView.getUrl(), currentWebView.getTitle(), currentWebView.getFavoriteOrDefaultIcon());

            // Display the create bookmark dialog.
            createBookmarkDialog.show(getSupportFragmentManager(), getString(R.string.create_bookmark));
        });

        // Search for the string on the page whenever a character changes in the `findOnPageEditText`.
        findOnPageEditText.addTextChangedListener(new TextWatcher() {
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
                // Search for the text in the WebView if it is not null.  Sometimes on resume after a period of non-use the WebView will be null.
                if (currentWebView != null) {
                    currentWebView.findAllAsync(findOnPageEditText.getText().toString());
                }
            }
        });

        // Set the `check mark` button for the `findOnPageEditText` keyboard to close the soft keyboard.
        findOnPageEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {  // The `enter` key was pressed.
                // Hide the soft keyboard.
                inputMethodManager.hideSoftInputFromWindow(currentWebView.getWindowToken(), 0);

                // Consume the event.
                return true;
            } else {  // A different key was pressed.
                // Do not consume the event.
                return false;
            }
        });

        // Implement swipe to refresh.
        swipeRefreshLayout.setOnRefreshListener(() -> currentWebView.reload());

        // Store the default progress view offsets for use later in `initializeWebView()`.
        defaultProgressViewStartOffset = swipeRefreshLayout.getProgressViewStartOffset();
        defaultProgressViewEndOffset = swipeRefreshLayout.getProgressViewEndOffset();

        // Set the swipe to refresh color according to the theme.
        if (darkTheme) {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_800);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.gray_850);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_500);
        }

        // `DrawerTitle` identifies the `DrawerLayouts` in accessibility mode.
        drawerLayout.setDrawerTitle(GravityCompat.START, getString(R.string.navigation_drawer));
        drawerLayout.setDrawerTitle(GravityCompat.END, getString(R.string.bookmarks));

        // Initialize the bookmarks database helper.  The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        bookmarksDatabaseHelper = new BookmarksDatabaseHelper(this, null, null, 0);

        // Initialize `currentBookmarksFolder`.  `""` is the home folder in the database.
        currentBookmarksFolder = "";

        // Load the home folder, which is `""` in the database.
        loadBookmarksFolder();

        bookmarksListView.setOnItemClickListener((parent, view, position, id) -> {
            // Convert the id from long to int to match the format of the bookmarks database.
            int databaseID = (int) id;

            // Get the bookmark cursor for this ID and move it to the first row.
            Cursor bookmarkCursor = bookmarksDatabaseHelper.getBookmark(databaseID);
            bookmarkCursor.moveToFirst();

            // Act upon the bookmark according to the type.
            if (bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1) {  // The selected bookmark is a folder.
                // Store the new folder name in `currentBookmarksFolder`.
                currentBookmarksFolder = bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                // Load the new folder.
                loadBookmarksFolder();
            } else {  // The selected bookmark is not a folder.
                // Load the bookmark URL.
                loadUrl(bookmarkCursor.getString(bookmarkCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL)));

                // Close the bookmarks drawer.
                drawerLayout.closeDrawer(GravityCompat.END);
            }

            // Close the `Cursor`.
            bookmarkCursor.close();
        });

        bookmarksListView.setOnItemLongClickListener((parent, view, position, id) -> {
            // Convert the database ID from `long` to `int`.
            int databaseId = (int) id;

            // Find out if the selected bookmark is a folder.
            boolean isFolder = bookmarksDatabaseHelper.isFolder(databaseId);

            if (isFolder) {
                // Save the current folder name, which is used in `onSaveEditBookmarkFolder()`.
                oldFolderNameString = bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));

                // Show the edit bookmark folder `AlertDialog` and name the instance `@string/edit_folder`.
                DialogFragment editBookmarkFolderDialog = EditBookmarkFolderDialog.folderDatabaseId(databaseId, currentWebView.getFavoriteOrDefaultIcon());
                editBookmarkFolderDialog.show(getSupportFragmentManager(), getString(R.string.edit_folder));
            } else {
                // Show the edit bookmark `AlertDialog` and name the instance `@string/edit_bookmark`.
                DialogFragment editBookmarkDialog = EditBookmarkDialog.bookmarkDatabaseId(databaseId, currentWebView.getFavoriteOrDefaultIcon());
                editBookmarkDialog.show(getSupportFragmentManager(), getString(R.string.edit_bookmark));
            }

            // Consume the event.
            return true;
        });

        // Get the status bar pixel size.
        int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarPixelSize = getResources().getDimensionPixelSize(statusBarResourceId);

        // Get the resource density.
        float screenDensity = getResources().getDisplayMetrics().density;

        // Calculate the drawer header padding.  This is used to move the text in the drawer headers below any cutouts.
        drawerHeaderPaddingLeftAndRight = (int) (15 * screenDensity);
        drawerHeaderPaddingTop = statusBarPixelSize + (int) (4 * screenDensity);
        drawerHeaderPaddingBottom = (int) (8 * screenDensity);

        // The drawer listener is used to update the navigation menu.`
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if ((newState == DrawerLayout.STATE_SETTLING) || (newState == DrawerLayout.STATE_DRAGGING)) {  // A drawer is opening or closing.
                    // Get handles for the drawer headers.
                    TextView navigationHeaderTextView = findViewById(R.id.navigationText);
                    TextView bookmarksHeaderTextView = findViewById(R.id.bookmarks_title_textview);

                    // Apply the navigation header paddings if the view is not null (sometimes it is null if another activity has already started).  This moves the text in the header below any cutouts.
                    if (navigationHeaderTextView != null) {
                        navigationHeaderTextView.setPadding(drawerHeaderPaddingLeftAndRight, drawerHeaderPaddingTop, drawerHeaderPaddingLeftAndRight, drawerHeaderPaddingBottom);
                    }

                    // Apply the bookmarks header paddings if the view is not null (sometimes it is null if another activity has already started).  This moves the text in the header below any cutouts.
                    if (bookmarksHeaderTextView != null) {
                        bookmarksHeaderTextView.setPadding(drawerHeaderPaddingLeftAndRight, drawerHeaderPaddingTop, drawerHeaderPaddingLeftAndRight, drawerHeaderPaddingBottom);
                    }

                    // Update the navigation menu items.
                    navigationBackMenuItem.setEnabled(currentWebView.canGoBack());
                    navigationForwardMenuItem.setEnabled(currentWebView.canGoForward());
                    navigationHistoryMenuItem.setEnabled((currentWebView.canGoBack() || currentWebView.canGoForward()));
                    navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + currentWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));

                    // Hide the keyboard (if displayed).
                    inputMethodManager.hideSoftInputFromWindow(currentWebView.getWindowToken(), 0);

                    // Clear the focus from from the URL text box and the WebView.  This removes any text selection markers and context menus, which otherwise draw above the open drawers.
                    urlEditText.clearFocus();
                    currentWebView.clearFocus();
                }
            }
        });

        // Create the hamburger icon at the start of the AppBar.
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer);

        // Replace the header that `WebView` creates for `X-Requested-With` with a null value.  The default value is the application ID (com.stoutner.privacybrowser.standard).
        customHeaders.put("X-Requested-With", "");

        // Initialize the default preference values the first time the program is run.  `false` keeps this command from resetting any current preferences back to default.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Inflate a bare WebView to get the default user agent.  It is not used to render content on the screen.
        @SuppressLint("InflateParams") View webViewLayout = getLayoutInflater().inflate(R.layout.bare_webview, null, false);

        // Get a handle for the WebView.
        WebView bareWebView = webViewLayout.findViewById(R.id.bare_webview);

        // Store the default user agent.
        webViewDefaultUserAgent = bareWebView.getSettings().getUserAgentString();

        // Destroy the bare WebView.
        bareWebView.destroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Get the information from the intent.
        String intentAction = intent.getAction();
        Uri intentUriData = intent.getData();

        // Determine if this is a web search.
        boolean isWebSearch = ((intentAction != null) && intentAction.equals(Intent.ACTION_WEB_SEARCH));

        // Only process the URI if it contains data or it is a web search.  If the user pressed the desktop icon after the app was already running the URI will be null.
        if (intentUriData != null || isWebSearch) {
            // Get the shared preferences.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            // Create a URL string.
            String url;

            // If the intent action is a web search, perform the search.
            if (isWebSearch) {
                // Create an encoded URL string.
                String encodedUrlString;

                // Sanitize the search input and convert it to a search.
                try {
                    encodedUrlString = URLEncoder.encode(intent.getStringExtra(SearchManager.QUERY), "UTF-8");
                } catch (UnsupportedEncodingException exception) {
                    encodedUrlString = "";
                }

                // Add the base search URL.
                url = searchURL + encodedUrlString;
            } else {  // The intent should contain a URL.
                // Set the intent data as the URL.
                url = intentUriData.toString();
            }

            // Add a new tab if specified in the preferences.
            if (sharedPreferences.getBoolean("open_intents_in_new_tab", true)) {  // Load the URL in a new tab.
                // Set the loading new intent flag.
                loadingNewIntent = true;

                // Add a new tab.
                addNewTab(url);
            } else {  // Load the URL in the current tab.
                // Make it so.
                loadUrl(url);
            }

            // Get a handle for the drawer layout.
            DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);

            // Close the navigation drawer if it is open.
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            // Close the bookmarks drawer if it is open.
            if (drawerLayout.isDrawerVisible(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            }
        }
    }

    @Override
    public void onRestart() {
        // Run the default commands.
        super.onRestart();

        // Make sure Orbot is running if Privacy Browser is proxying through Orbot.
        if (proxyThroughOrbot) {
            // Request Orbot to start.  If Orbot is already running no hard will be caused by this request.
            Intent orbotIntent = new Intent("org.torproject.android.intent.action.START");

            // Send the intent to the Orbot package.
            orbotIntent.setPackage("org.torproject.android");

            // Make it so.
            sendBroadcast(orbotIntent);
        }

        // Apply the app settings if returning from the Settings activity.
        if (reapplyAppSettingsOnRestart) {
            // Reset the reapply app settings on restart tracker.
            reapplyAppSettingsOnRestart = false;

            // Apply the app settings.
            applyAppSettings();
        }

        // Apply the domain settings if returning from the settings or domains activity.
        if (reapplyDomainSettingsOnRestart) {
            // Reset the reapply domain settings on restart tracker.
            reapplyDomainSettingsOnRestart = false;

            // Reapply the domain settings for each tab.
            for (int i = 0; i < webViewPagerAdapter.getCount(); i++) {
                // Get the WebView tab fragment.
                WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(i);

                // Get the fragment view.
                View fragmentView = webViewTabFragment.getView();

                // Only reload the WebViews if they exist.
                if (fragmentView != null) {
                    // Get the nested scroll WebView from the tab fragment.
                    NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

                    // Reset the current domain name so the domain settings will be reapplied.
                    nestedScrollWebView.resetCurrentDomainName();

                    // Reapply the domain settings if the URL is not null, which can happen if an empty tab is active when returning from settings.
                    if (nestedScrollWebView.getUrl() != null) {
                        applyDomainSettings(nestedScrollWebView, nestedScrollWebView.getUrl(), false, true);
                    }
                }
            }
        }

        // Load the URL on restart (used when loading a bookmark).
        if (loadUrlOnRestart) {
            // Load the specified URL.
            loadUrl(urlToLoadOnRestart);

            // Reset the load on restart tracker.
            loadUrlOnRestart = false;
        }

        // Update the bookmarks drawer if returning from the Bookmarks activity.
        if (restartFromBookmarksActivity) {
            // Get a handle for the drawer layout.
            DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);

            // Close the bookmarks drawer.
            drawerLayout.closeDrawer(GravityCompat.END);

            // Reload the bookmarks drawer.
            loadBookmarksFolder();

            // Reset `restartFromBookmarksActivity`.
            restartFromBookmarksActivity = false;
        }

        // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.  This can be important if the screen was rotated.
        updatePrivacyIcons(true);
    }

    // `onResume()` runs after `onStart()`, which runs after `onCreate()` and `onRestart()`.
    @Override
    public void onResume() {
        // Run the default commands.
        super.onResume();

        for (int i = 0; i < webViewPagerAdapter.getCount(); i++) {
            // Get the WebView tab fragment.
            WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(i);

            // Get the fragment view.
            View fragmentView = webViewTabFragment.getView();

            // Only resume the WebViews if they exist (they won't when the app is first created).
            if (fragmentView != null) {
                // Get the nested scroll WebView from the tab fragment.
                NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

                // Resume the nested scroll WebView JavaScript timers.
                nestedScrollWebView.resumeTimers();

                // Resume the nested scroll WebView.
                nestedScrollWebView.onResume();
            }
        }

        // Display a message to the user if waiting for Orbot.
        if (waitingForOrbot && !orbotStatus.equals("ON")) {
            // Disable the wide view port so that the waiting for Orbot text is displayed correctly.
            currentWebView.getSettings().setUseWideViewPort(false);

            // Load a waiting page.  `null` specifies no encoding, which defaults to ASCII.
            currentWebView.loadData("<html><body><br/><center><h1>" + getString(R.string.waiting_for_orbot) + "</h1></center></body></html>", "text/html", null);
        }

        if (displayingFullScreenVideo || inFullScreenBrowsingMode) {
            // Get a handle for the root frame layouts.
            FrameLayout rootFrameLayout = findViewById(R.id.root_framelayout);

            // Remove the translucent status flag.  This is necessary so the root frame layout can fill the entire screen.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            /* Hide the system bars.
             * SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
             * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN makes the root frame layout fill the area that is normally reserved for the status bar.
             * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
             * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
             */
            rootFrameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else if (BuildConfig.FLAVOR.contentEquals("free")) {  // Resume the adView for the free flavor.
            // Resume the ad.
            AdHelper.resumeAd(findViewById(R.id.adview));
        }
    }

    @Override
    public void onPause() {
        // Run the default commands.
        super.onPause();

        for (int i = 0; i < webViewPagerAdapter.getCount(); i++) {
            // Get the WebView tab fragment.
            WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(i);

            // Get the fragment view.
            View fragmentView = webViewTabFragment.getView();

            // Only pause the WebViews if they exist (they won't when the app is first created).
            if (fragmentView != null) {
                // Get the nested scroll WebView from the tab fragment.
                NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

                // Pause the nested scroll WebView.
                nestedScrollWebView.onPause();

                // Pause the nested scroll WebView JavaScript timers.
                nestedScrollWebView.pauseTimers();
            }
        }

        // Pause the ad or it will continue to consume resources in the background on the free flavor.
        if (BuildConfig.FLAVOR.contentEquals("free")) {
            // Pause the ad.
            AdHelper.pauseAd(findViewById(R.id.adview));
        }
    }

    @Override
    public void onDestroy() {
        // Unregister the Orbot status broadcast receiver.
        this.unregisterReceiver(orbotStatusBroadcastReceiver);

        // Close the bookmarks cursor and database.
        bookmarksCursor.close();
        bookmarksDatabaseHelper.close();

        // Run the default commands.
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu.  This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.webview_options_menu, menu);

        // Store a handle for the options menu so it can be used by `onOptionsItemSelected()` and `updatePrivacyIcons()`.
        optionsMenu = menu;

        // Set the initial status of the privacy icons.  `false` does not call `invalidateOptionsMenu` as the last step.
        updatePrivacyIcons(false);

        // Get handles for the menu items.
        MenuItem toggleFirstPartyCookiesMenuItem = menu.findItem(R.id.toggle_first_party_cookies);
        MenuItem toggleThirdPartyCookiesMenuItem = menu.findItem(R.id.toggle_third_party_cookies);
        MenuItem toggleDomStorageMenuItem = menu.findItem(R.id.toggle_dom_storage);
        MenuItem toggleSaveFormDataMenuItem = menu.findItem(R.id.toggle_save_form_data);  // Form data can be removed once the minimum API >= 26.
        MenuItem clearFormDataMenuItem = menu.findItem(R.id.clear_form_data);  // Form data can be removed once the minimum API >= 26.
        MenuItem refreshMenuItem = menu.findItem(R.id.refresh);
        MenuItem adConsentMenuItem = menu.findItem(R.id.ad_consent);

        // Only display third-party cookies if API >= 21
        toggleThirdPartyCookiesMenuItem.setVisible(Build.VERSION.SDK_INT >= 21);

        // Only display the form data menu items if the API < 26.
        toggleSaveFormDataMenuItem.setVisible(Build.VERSION.SDK_INT < 26);
        clearFormDataMenuItem.setVisible(Build.VERSION.SDK_INT < 26);

        // Disable the clear form data menu item if the API >= 26 so that the status of the main Clear Data is calculated correctly.
        clearFormDataMenuItem.setEnabled(Build.VERSION.SDK_INT < 26);

        // Only show Ad Consent if this is the free flavor.
        adConsentMenuItem.setVisible(BuildConfig.FLAVOR.contentEquals("free"));

        // Get the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the dark theme and app bar preferences..
        boolean displayAdditionalAppBarIcons = sharedPreferences.getBoolean("display_additional_app_bar_icons", false);
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

        // Set the status of the additional app bar icons.  Setting the refresh menu item to `SHOW_AS_ACTION_ALWAYS` makes it appear even on small devices like phones.
        if (displayAdditionalAppBarIcons) {
            toggleFirstPartyCookiesMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            toggleDomStorageMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            refreshMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else { //Do not display the additional icons.
            toggleFirstPartyCookiesMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            toggleDomStorageMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            refreshMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        // Replace Refresh with Stop if a URL is already loading.
        if (currentWebView != null && currentWebView.getProgress() != 100) {
            // Set the title.
            refreshMenuItem.setTitle(R.string.stop);

            // If the icon is displayed in the AppBar, set it according to the theme.
            if (displayAdditionalAppBarIcons) {
                if (darkTheme) {
                    refreshMenuItem.setIcon(R.drawable.close_dark);
                } else {
                    refreshMenuItem.setIcon(R.drawable.close_light);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get handles for the menu items.
        MenuItem addOrEditDomain = menu.findItem(R.id.add_or_edit_domain);
        MenuItem firstPartyCookiesMenuItem = menu.findItem(R.id.toggle_first_party_cookies);
        MenuItem thirdPartyCookiesMenuItem = menu.findItem(R.id.toggle_third_party_cookies);
        MenuItem domStorageMenuItem = menu.findItem(R.id.toggle_dom_storage);
        MenuItem saveFormDataMenuItem = menu.findItem(R.id.toggle_save_form_data);  // Form data can be removed once the minimum API >= 26.
        MenuItem clearDataMenuItem = menu.findItem(R.id.clear_data);
        MenuItem clearCookiesMenuItem = menu.findItem(R.id.clear_cookies);
        MenuItem clearDOMStorageMenuItem = menu.findItem(R.id.clear_dom_storage);
        MenuItem clearFormDataMenuItem = menu.findItem(R.id.clear_form_data);  // Form data can be removed once the minimum API >= 26.
        MenuItem blocklistsMenuItem = menu.findItem(R.id.blocklists);
        MenuItem easyListMenuItem = menu.findItem(R.id.easylist);
        MenuItem easyPrivacyMenuItem = menu.findItem(R.id.easyprivacy);
        MenuItem fanboysAnnoyanceListMenuItem = menu.findItem(R.id.fanboys_annoyance_list);
        MenuItem fanboysSocialBlockingListMenuItem = menu.findItem(R.id.fanboys_social_blocking_list);
        MenuItem ultraPrivacyMenuItem = menu.findItem(R.id.ultraprivacy);
        MenuItem blockAllThirdPartyRequestsMenuItem = menu.findItem(R.id.block_all_third_party_requests);
        MenuItem fontSizeMenuItem = menu.findItem(R.id.font_size);
        MenuItem swipeToRefreshMenuItem = menu.findItem(R.id.swipe_to_refresh);
        MenuItem displayImagesMenuItem = menu.findItem(R.id.display_images);
        MenuItem nightModeMenuItem = menu.findItem(R.id.night_mode);
        MenuItem proxyThroughOrbotMenuItem = menu.findItem(R.id.proxy_through_orbot);

        // Get a handle for the cookie manager.
        CookieManager cookieManager = CookieManager.getInstance();

        // Initialize the current user agent string and the font size.
        String currentUserAgent = getString(R.string.user_agent_privacy_browser);
        int fontSize = 100;

        // Set items that require the current web view to be populated.  It will be null when the program is first opened, as `onPrepareOptionsMenu()` is called before the first WebView is initialized.
        if (currentWebView != null) {
            // Set the add or edit domain text.
            if (currentWebView.getDomainSettingsApplied()) {
                addOrEditDomain.setTitle(R.string.edit_domain_settings);
            } else {
                addOrEditDomain.setTitle(R.string.add_domain_settings);
            }

            // Get the current user agent from the WebView.
            currentUserAgent = currentWebView.getSettings().getUserAgentString();

            // Get the current font size from the
            fontSize = currentWebView.getSettings().getTextZoom();

            // Set the status of the menu item checkboxes.
            domStorageMenuItem.setChecked(currentWebView.getSettings().getDomStorageEnabled());
            saveFormDataMenuItem.setChecked(currentWebView.getSettings().getSaveFormData());  // Form data can be removed once the minimum API >= 26.
            easyListMenuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.EASY_LIST));
            easyPrivacyMenuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.EASY_PRIVACY));
            fanboysAnnoyanceListMenuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST));
            fanboysSocialBlockingListMenuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST));
            ultraPrivacyMenuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.ULTRA_PRIVACY));
            blockAllThirdPartyRequestsMenuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.THIRD_PARTY_REQUESTS));
            swipeToRefreshMenuItem.setChecked(currentWebView.getSwipeToRefresh());
            displayImagesMenuItem.setChecked(currentWebView.getSettings().getLoadsImagesAutomatically());
            nightModeMenuItem.setChecked(currentWebView.getNightMode());

            // Initialize the display names for the blocklists with the number of blocked requests.
            blocklistsMenuItem.setTitle(getString(R.string.blocklists) + " - " + currentWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));
            easyListMenuItem.setTitle(currentWebView.getRequestsCount(NestedScrollWebView.EASY_LIST) + " - " + getString(R.string.easylist));
            easyPrivacyMenuItem.setTitle(currentWebView.getRequestsCount(NestedScrollWebView.EASY_PRIVACY) + " - " + getString(R.string.easyprivacy));
            fanboysAnnoyanceListMenuItem.setTitle(currentWebView.getRequestsCount(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST) + " - " + getString(R.string.fanboys_annoyance_list));
            fanboysSocialBlockingListMenuItem.setTitle(currentWebView.getRequestsCount(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST) + " - " + getString(R.string.fanboys_social_blocking_list));
            ultraPrivacyMenuItem.setTitle(currentWebView.getRequestsCount(NestedScrollWebView.ULTRA_PRIVACY) + " - " + getString(R.string.ultraprivacy));
            blockAllThirdPartyRequestsMenuItem.setTitle(currentWebView.getRequestsCount(NestedScrollWebView.THIRD_PARTY_REQUESTS) + " - " + getString(R.string.block_all_third_party_requests));

            // Only modify third-party cookies if the API >= 21.
            if (Build.VERSION.SDK_INT >= 21) {
                // Set the status of the third-party cookies checkbox.
                thirdPartyCookiesMenuItem.setChecked(cookieManager.acceptThirdPartyCookies(currentWebView));

                // Enable third-party cookies if first-party cookies are enabled.
                thirdPartyCookiesMenuItem.setEnabled(cookieManager.acceptCookie());
            }

            // Enable DOM Storage if JavaScript is enabled.
            domStorageMenuItem.setEnabled(currentWebView.getSettings().getJavaScriptEnabled());
        }

        // Set the status of the menu item checkboxes.
        firstPartyCookiesMenuItem.setChecked(cookieManager.acceptCookie());
        proxyThroughOrbotMenuItem.setChecked(proxyThroughOrbot);

        // Enable Clear Cookies if there are any.
        clearCookiesMenuItem.setEnabled(cookieManager.hasCookies());

        // Get the application's private data directory, which will be something like `/data/user/0/com.stoutner.privacybrowser.standard`, which links to `/data/data/com.stoutner.privacybrowser.standard`.
        String privateDataDirectoryString = getApplicationInfo().dataDir;

        // Get a count of the number of files in the Local Storage directory.
        File localStorageDirectory = new File (privateDataDirectoryString + "/app_webview/Local Storage/");
        int localStorageDirectoryNumberOfFiles = 0;
        if (localStorageDirectory.exists()) {
            localStorageDirectoryNumberOfFiles = localStorageDirectory.list().length;
        }

        // Get a count of the number of files in the IndexedDB directory.
        File indexedDBDirectory = new File (privateDataDirectoryString + "/app_webview/IndexedDB");
        int indexedDBDirectoryNumberOfFiles = 0;
        if (indexedDBDirectory.exists()) {
            indexedDBDirectoryNumberOfFiles = indexedDBDirectory.list().length;
        }

        // Enable Clear DOM Storage if there is any.
        clearDOMStorageMenuItem.setEnabled(localStorageDirectoryNumberOfFiles > 0 || indexedDBDirectoryNumberOfFiles > 0);

        // Enable Clear Form Data is there is any.  This can be removed once the minimum API >= 26.
        if (Build.VERSION.SDK_INT < 26) {
            // Get the WebView database.
            WebViewDatabase webViewDatabase = WebViewDatabase.getInstance(this);

            // Enable the clear form data menu item if there is anything to clear.
            clearFormDataMenuItem.setEnabled(webViewDatabase.hasFormData());
        }

        // Enable Clear Data if any of the submenu items are enabled.
        clearDataMenuItem.setEnabled(clearCookiesMenuItem.isEnabled() || clearDOMStorageMenuItem.isEnabled() || clearFormDataMenuItem.isEnabled());

        // Disable Fanboy's Social Blocking List menu item if Fanboy's Annoyance List is checked.
        fanboysSocialBlockingListMenuItem.setEnabled(!fanboysAnnoyanceListMenuItem.isChecked());

        // Select the current user agent menu item.  A switch statement cannot be used because the user agents are not compile time constants.
        if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[0])) {  // Privacy Browser.
            menu.findItem(R.id.user_agent_privacy_browser).setChecked(true);
        } else if (currentUserAgent.equals(webViewDefaultUserAgent)) {  // WebView Default.
            menu.findItem(R.id.user_agent_webview_default).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[2])) {  // Firefox on Android.
            menu.findItem(R.id.user_agent_firefox_on_android).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[3])) {  // Chrome on Android.
            menu.findItem(R.id.user_agent_chrome_on_android).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[4])) {  // Safari on iOS.
            menu.findItem(R.id.user_agent_safari_on_ios).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[5])) {  // Firefox on Linux.
            menu.findItem(R.id.user_agent_firefox_on_linux).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[6])) {  // Chromium on Linux.
            menu.findItem(R.id.user_agent_chromium_on_linux).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[7])) {  // Firefox on Windows.
            menu.findItem(R.id.user_agent_firefox_on_windows).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[8])) {  // Chrome on Windows.
            menu.findItem(R.id.user_agent_chrome_on_windows).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[9])) {  // Edge on Windows.
            menu.findItem(R.id.user_agent_edge_on_windows).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[10])) {  // Internet Explorer on Windows.
            menu.findItem(R.id.user_agent_internet_explorer_on_windows).setChecked(true);
        } else if (currentUserAgent.equals(getResources().getStringArray(R.array.user_agent_data)[11])) {  // Safari on macOS.
            menu.findItem(R.id.user_agent_safari_on_macos).setChecked(true);
        } else {  // Custom user agent.
            menu.findItem(R.id.user_agent_custom).setChecked(true);
        }

        // Instantiate the font size title and the selected font size menu item.
        String fontSizeTitle;
        MenuItem selectedFontSizeMenuItem;

        // Prepare the font size title and current size menu item.
        switch (fontSize) {
            case 25:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.twenty_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_twenty_five_percent);
                break;

            case 50:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.fifty_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_fifty_percent);
                break;

            case 75:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.seventy_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_seventy_five_percent);
                break;

            case 100:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.one_hundred_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_one_hundred_percent);
                break;

            case 125:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.one_hundred_twenty_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_one_hundred_twenty_five_percent);
                break;

            case 150:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.one_hundred_fifty_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_one_hundred_fifty_percent);
                break;

            case 175:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.one_hundred_seventy_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_one_hundred_seventy_five_percent);
                break;

            case 200:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.two_hundred_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_two_hundred_percent);
                break;

            default:
                fontSizeTitle = getString(R.string.font_size) + " - " + getString(R.string.one_hundred_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.font_size_one_hundred_percent);
                break;
        }

        // Set the font size title and select the current size menu item.
        fontSizeMenuItem.setTitle(fontSizeTitle);
        selectedFontSizeMenuItem.setChecked(true);

        // Run all the other default commands.
        super.onPrepareOptionsMenu(menu);

        // Display the menu.
        return true;
    }

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Reenter full screen browsing mode if it was interrupted by the options menu.  <https://redmine.stoutner.com/issues/389>
        if (inFullScreenBrowsingMode) {
            // Remove the translucent status flag.  This is necessary so the root frame layout can fill the entire screen.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            FrameLayout rootFrameLayout = findViewById(R.id.root_framelayout);

            /* Hide the system bars.
             * SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
             * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN makes the root frame layout fill the area that is normally reserved for the status bar.
             * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
             * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
             */
            rootFrameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        // Get the selected menu item ID.
        int menuItemId = menuItem.getItemId();

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get a handle for the cookie manager.
        CookieManager cookieManager = CookieManager.getInstance();

        // Run the commands that correlate to the selected menu item.
        switch (menuItemId) {
            case R.id.toggle_javascript:
                // Toggle the JavaScript status.
                currentWebView.getSettings().setJavaScriptEnabled(!currentWebView.getSettings().getJavaScriptEnabled());

                // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
                updatePrivacyIcons(true);

                // Display a `Snackbar`.
                if (currentWebView.getSettings().getJavaScriptEnabled()) {  // JavaScrip is enabled.
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.javascript_enabled, Snackbar.LENGTH_SHORT).show();
                } else if (cookieManager.acceptCookie()) {  // JavaScript is disabled, but first-party cookies are enabled.
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.javascript_disabled, Snackbar.LENGTH_SHORT).show();
                } else {  // Privacy mode.
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                }

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.add_or_edit_domain:
                if (currentWebView.getDomainSettingsApplied()) {  // Edit the current domain settings.
                    // Reapply the domain settings on returning to `MainWebViewActivity`.
                    reapplyDomainSettingsOnRestart = true;

                    // Create an intent to launch the domains activity.
                    Intent domainsIntent = new Intent(this, DomainsActivity.class);

                    // Add the extra information to the intent.
                    domainsIntent.putExtra("load_domain", currentWebView.getDomainSettingsDatabaseId());
                    domainsIntent.putExtra("close_on_back", true);
                    domainsIntent.putExtra("current_url", currentWebView.getUrl());

                    // Get the current certificate.
                    SslCertificate sslCertificate = currentWebView.getCertificate();

                    // Check to see if the SSL certificate is populated.
                    if (sslCertificate != null) {
                        // Extract the certificate to strings.
                        String issuedToCName = sslCertificate.getIssuedTo().getCName();
                        String issuedToOName = sslCertificate.getIssuedTo().getOName();
                        String issuedToUName = sslCertificate.getIssuedTo().getUName();
                        String issuedByCName = sslCertificate.getIssuedBy().getCName();
                        String issuedByOName = sslCertificate.getIssuedBy().getOName();
                        String issuedByUName = sslCertificate.getIssuedBy().getUName();
                        long startDateLong = sslCertificate.getValidNotBeforeDate().getTime();
                        long endDateLong = sslCertificate.getValidNotAfterDate().getTime();

                        // Add the certificate to the intent.
                        domainsIntent.putExtra("ssl_issued_to_cname", issuedToCName);
                        domainsIntent.putExtra("ssl_issued_to_oname", issuedToOName);
                        domainsIntent.putExtra("ssl_issued_to_uname", issuedToUName);
                        domainsIntent.putExtra("ssl_issued_by_cname", issuedByCName);
                        domainsIntent.putExtra("ssl_issued_by_oname", issuedByOName);
                        domainsIntent.putExtra("ssl_issued_by_uname", issuedByUName);
                        domainsIntent.putExtra("ssl_start_date", startDateLong);
                        domainsIntent.putExtra("ssl_end_date", endDateLong);
                    }

                    // Check to see if the current IP addresses have been received.
                    if (currentWebView.hasCurrentIpAddresses()) {
                        // Add the current IP addresses to the intent.
                        domainsIntent.putExtra("current_ip_addresses", currentWebView.getCurrentIpAddresses());
                    }

                    // Make it so.
                    startActivity(domainsIntent);
                } else {  // Add a new domain.
                    // Apply the new domain settings on returning to `MainWebViewActivity`.
                    reapplyDomainSettingsOnRestart = true;

                    // Get the current domain
                    Uri currentUri = Uri.parse(currentWebView.getUrl());
                    String currentDomain = currentUri.getHost();

                    // Initialize the database handler.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
                    DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(this, null, null, 0);

                    // Create the domain and store the database ID.
                    int newDomainDatabaseId = domainsDatabaseHelper.addDomain(currentDomain);

                    // Create an intent to launch the domains activity.
                    Intent domainsIntent = new Intent(this, DomainsActivity.class);

                    // Add the extra information to the intent.
                    domainsIntent.putExtra("load_domain", newDomainDatabaseId);
                    domainsIntent.putExtra("close_on_back", true);
                    domainsIntent.putExtra("current_url", currentWebView.getUrl());

                    // Get the current certificate.
                    SslCertificate sslCertificate = currentWebView.getCertificate();

                    // Check to see if the SSL certificate is populated.
                    if (sslCertificate != null) {
                        // Extract the certificate to strings.
                        String issuedToCName = sslCertificate.getIssuedTo().getCName();
                        String issuedToOName = sslCertificate.getIssuedTo().getOName();
                        String issuedToUName = sslCertificate.getIssuedTo().getUName();
                        String issuedByCName = sslCertificate.getIssuedBy().getCName();
                        String issuedByOName = sslCertificate.getIssuedBy().getOName();
                        String issuedByUName = sslCertificate.getIssuedBy().getUName();
                        long startDateLong = sslCertificate.getValidNotBeforeDate().getTime();
                        long endDateLong = sslCertificate.getValidNotAfterDate().getTime();

                        // Add the certificate to the intent.
                        domainsIntent.putExtra("ssl_issued_to_cname", issuedToCName);
                        domainsIntent.putExtra("ssl_issued_to_oname", issuedToOName);
                        domainsIntent.putExtra("ssl_issued_to_uname", issuedToUName);
                        domainsIntent.putExtra("ssl_issued_by_cname", issuedByCName);
                        domainsIntent.putExtra("ssl_issued_by_oname", issuedByOName);
                        domainsIntent.putExtra("ssl_issued_by_uname", issuedByUName);
                        domainsIntent.putExtra("ssl_start_date", startDateLong);
                        domainsIntent.putExtra("ssl_end_date", endDateLong);
                    }

                    // Check to see if the current IP addresses have been received.
                    if (currentWebView.hasCurrentIpAddresses()) {
                        // Add the current IP addresses to the intent.
                        domainsIntent.putExtra("current_ip_addresses", currentWebView.getCurrentIpAddresses());
                    }

                    // Make it so.
                    startActivity(domainsIntent);
                }
                return true;

            case R.id.toggle_first_party_cookies:
                // Switch the first-party cookie status.
                cookieManager.setAcceptCookie(!cookieManager.acceptCookie());

                // Store the first-party cookie status.
                currentWebView.setAcceptFirstPartyCookies(cookieManager.acceptCookie());

                // Update the menu checkbox.
                menuItem.setChecked(cookieManager.acceptCookie());

                // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
                updatePrivacyIcons(true);

                // Display a snackbar.
                if (cookieManager.acceptCookie()) {  // First-party cookies are enabled.
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.first_party_cookies_enabled, Snackbar.LENGTH_SHORT).show();
                } else if (currentWebView.getSettings().getJavaScriptEnabled()) {  // JavaScript is still enabled.
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.first_party_cookies_disabled, Snackbar.LENGTH_SHORT).show();
                } else {  // Privacy mode.
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                }

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.toggle_third_party_cookies:
                if (Build.VERSION.SDK_INT >= 21) {
                    // Switch the status of thirdPartyCookiesEnabled.
                    cookieManager.setAcceptThirdPartyCookies(currentWebView, !cookieManager.acceptThirdPartyCookies(currentWebView));

                    // Update the menu checkbox.
                    menuItem.setChecked(cookieManager.acceptThirdPartyCookies(currentWebView));

                    // Display a snackbar.
                    if (cookieManager.acceptThirdPartyCookies(currentWebView)) {
                        Snackbar.make(findViewById(R.id.webviewpager), R.string.third_party_cookies_enabled, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(R.id.webviewpager), R.string.third_party_cookies_disabled, Snackbar.LENGTH_SHORT).show();
                    }

                    // Reload the current WebView.
                    currentWebView.reload();
                } // Else do nothing because SDK < 21.
                return true;

            case R.id.toggle_dom_storage:
                // Toggle the status of domStorageEnabled.
                currentWebView.getSettings().setDomStorageEnabled(!currentWebView.getSettings().getDomStorageEnabled());

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.getSettings().getDomStorageEnabled());

                // Update the privacy icon.  `true` refreshes the app bar icons.
                updatePrivacyIcons(true);

                // Display a snackbar.
                if (currentWebView.getSettings().getDomStorageEnabled()) {
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.dom_storage_enabled, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.dom_storage_disabled, Snackbar.LENGTH_SHORT).show();
                }

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            // Form data can be removed once the minimum API >= 26.
            case R.id.toggle_save_form_data:
                // Switch the status of saveFormDataEnabled.
                currentWebView.getSettings().setSaveFormData(!currentWebView.getSettings().getSaveFormData());

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.getSettings().getSaveFormData());

                // Display a snackbar.
                if (currentWebView.getSettings().getSaveFormData()) {
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.form_data_enabled, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.webviewpager), R.string.form_data_disabled, Snackbar.LENGTH_SHORT).show();
                }

                // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
                updatePrivacyIcons(true);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.clear_cookies:
                Snackbar.make(findViewById(R.id.webviewpager), R.string.cookies_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Do nothing because everything will be handled by `onDismissed()` below.
                        })
                        .addCallback(new Snackbar.Callback() {
                            @SuppressLint("SwitchIntDef")  // Ignore the lint warning about not handling the other possible events as they are covered by `default:`.
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {  // The snackbar was dismissed without the undo button being pushed.
                                    // Delete the cookies, which command varies by SDK.
                                    if (Build.VERSION.SDK_INT < 21) {
                                        cookieManager.removeAllCookie();
                                    } else {
                                        cookieManager.removeAllCookies(null);
                                    }
                                }
                            }
                        })
                        .show();
                return true;

            case R.id.clear_dom_storage:
                Snackbar.make(findViewById(R.id.webviewpager), R.string.dom_storage_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Do nothing because everything will be handled by `onDismissed()` below.
                        })
                        .addCallback(new Snackbar.Callback() {
                            @SuppressLint("SwitchIntDef")  // Ignore the lint warning about not handling the other possible events as they are covered by `default:`.
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {  // The snackbar was dismissed without the undo button being pushed.
                                    // Delete the DOM Storage.
                                    WebStorage webStorage = WebStorage.getInstance();
                                    webStorage.deleteAllData();

                                    // Initialize a handler to manually delete the DOM storage files and directories.
                                    Handler deleteDomStorageHandler = new Handler();

                                    // Setup a runnable to manually delete the DOM storage files and directories.
                                    Runnable deleteDomStorageRunnable = () -> {
                                        try {
                                            // Get a handle for the runtime.
                                            Runtime runtime = Runtime.getRuntime();

                                            // Get the application's private data directory, which will be something like `/data/user/0/com.stoutner.privacybrowser.standard`,
                                            // which links to `/data/data/com.stoutner.privacybrowser.standard`.
                                            String privateDataDirectoryString = getApplicationInfo().dataDir;

                                            // A string array must be used because the directory contains a space and `Runtime.exec` will otherwise not escape the string correctly.
                                            Process deleteLocalStorageProcess = runtime.exec(new String[]{"rm", "-rf", privateDataDirectoryString + "/app_webview/Local Storage/"});

                                            // Multiple commands must be used because `Runtime.exec()` does not like `*`.
                                            Process deleteIndexProcess = runtime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/IndexedDB");
                                            Process deleteQuotaManagerProcess = runtime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager");
                                            Process deleteQuotaManagerJournalProcess = runtime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager-journal");
                                            Process deleteDatabasesProcess = runtime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/databases");

                                            // Wait for the processes to finish.
                                            deleteLocalStorageProcess.waitFor();
                                            deleteIndexProcess.waitFor();
                                            deleteQuotaManagerProcess.waitFor();
                                            deleteQuotaManagerJournalProcess.waitFor();
                                            deleteDatabasesProcess.waitFor();
                                        } catch (Exception exception) {
                                            // Do nothing if an error is thrown.
                                        }
                                    };

                                    // Manually delete the DOM storage files after 200 milliseconds.
                                    deleteDomStorageHandler.postDelayed(deleteDomStorageRunnable, 200);
                                }
                            }
                        })
                        .show();
                return true;

            // Form data can be remove once the minimum API >= 26.
            case R.id.clear_form_data:
                Snackbar.make(findViewById(R.id.webviewpager), R.string.form_data_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Do nothing because everything will be handled by `onDismissed()` below.
                        })
                        .addCallback(new Snackbar.Callback() {
                            @SuppressLint("SwitchIntDef")  // Ignore the lint warning about not handling the other possible events as they are covered by `default:`.
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {  // The snackbar was dismissed without the undo button being pushed.
                                    // Delete the form data.
                                    WebViewDatabase mainWebViewDatabase = WebViewDatabase.getInstance(getApplicationContext());
                                    mainWebViewDatabase.clearFormData();
                                }
                            }
                        })
                        .show();
                return true;

            case R.id.easylist:
                // Toggle the EasyList status.
                currentWebView.enableBlocklist(NestedScrollWebView.EASY_LIST, !currentWebView.isBlocklistEnabled(NestedScrollWebView.EASY_LIST));

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.EASY_LIST));

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.easyprivacy:
                // Toggle the EasyPrivacy status.
                currentWebView.enableBlocklist(NestedScrollWebView.EASY_PRIVACY, !currentWebView.isBlocklistEnabled(NestedScrollWebView.EASY_PRIVACY));

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.EASY_PRIVACY));

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.fanboys_annoyance_list:
                // Toggle Fanboy's Annoyance List status.
                currentWebView.enableBlocklist(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST, !currentWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST));

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST));

                // Update the staus of Fanboy's Social Blocking List.
                MenuItem fanboysSocialBlockingListMenuItem = optionsMenu.findItem(R.id.fanboys_social_blocking_list);
                fanboysSocialBlockingListMenuItem.setEnabled(!currentWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST));

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.fanboys_social_blocking_list:
                // Toggle Fanboy's Social Blocking List status.
                currentWebView.enableBlocklist(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST, !currentWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST));

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST));

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.ultraprivacy:
                // Toggle the UltraPrivacy status.
                currentWebView.enableBlocklist(NestedScrollWebView.ULTRA_PRIVACY, !currentWebView.isBlocklistEnabled(NestedScrollWebView.ULTRA_PRIVACY));

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.ULTRA_PRIVACY));

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.block_all_third_party_requests:
                //Toggle the third-party requests blocker status.
                currentWebView.enableBlocklist(NestedScrollWebView.THIRD_PARTY_REQUESTS, !currentWebView.isBlocklistEnabled(NestedScrollWebView.THIRD_PARTY_REQUESTS));

                // Update the menu checkbox.
                menuItem.setChecked(currentWebView.isBlocklistEnabled(NestedScrollWebView.THIRD_PARTY_REQUESTS));

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_privacy_browser:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[0]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_webview_default:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString("");

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_firefox_on_android:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[2]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_chrome_on_android:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[3]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_safari_on_ios:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[4]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_firefox_on_linux:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[5]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_chromium_on_linux:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[6]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_firefox_on_windows:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[7]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_chrome_on_windows:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[8]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_edge_on_windows:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[9]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_internet_explorer_on_windows:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[10]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_safari_on_macos:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[11]);

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.user_agent_custom:
                // Update the user agent.
                currentWebView.getSettings().setUserAgentString(sharedPreferences.getString("custom_user_agent", getString(R.string.custom_user_agent_default_value)));

                // Reload the current WebView.
                currentWebView.reload();
                return true;

            case R.id.font_size_twenty_five_percent:
                currentWebView.getSettings().setTextZoom(25);
                return true;

            case R.id.font_size_fifty_percent:
                currentWebView.getSettings().setTextZoom(50);
                return true;

            case R.id.font_size_seventy_five_percent:
                currentWebView.getSettings().setTextZoom(75);
                return true;

            case R.id.font_size_one_hundred_percent:
                currentWebView.getSettings().setTextZoom(100);
                return true;

            case R.id.font_size_one_hundred_twenty_five_percent:
                currentWebView.getSettings().setTextZoom(125);
                return true;

            case R.id.font_size_one_hundred_fifty_percent:
                currentWebView.getSettings().setTextZoom(150);
                return true;

            case R.id.font_size_one_hundred_seventy_five_percent:
                currentWebView.getSettings().setTextZoom(175);
                return true;

            case R.id.font_size_two_hundred_percent:
                currentWebView.getSettings().setTextZoom(200);
                return true;

            case R.id.swipe_to_refresh:
                // Toggle the stored status of swipe to refresh.
                currentWebView.setSwipeToRefresh(!currentWebView.getSwipeToRefresh());

                // Get a handle for the swipe refresh layout.
                SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

                // Update the swipe refresh layout.
                if (currentWebView.getSwipeToRefresh()) {  // Swipe to refresh is enabled.
                    if (Build.VERSION.SDK_INT >= 23) {  // For API >= 23, the status of the scroll refresh listener is continuously updated by the on scroll change listener.
                        // Only enable the swipe refresh layout if the WebView is scrolled to the top.
                        swipeRefreshLayout.setEnabled(currentWebView.getY() == 0);
                    } else {  // For API < 23, the swipe refresh layout is always enabled.
                        // Enable the swipe refresh layout.
                        swipeRefreshLayout.setEnabled(true);
                    }
                } else {  // Swipe to refresh is disabled.
                    // Disable the swipe refresh layout.
                    swipeRefreshLayout.setEnabled(false);
                }
                return true;

            case R.id.display_images:
                if (currentWebView.getSettings().getLoadsImagesAutomatically()) {  // Images are currently loaded automatically.
                    // Disable loading of images.
                    currentWebView.getSettings().setLoadsImagesAutomatically(false);

                    // Reload the website to remove existing images.
                    currentWebView.reload();
                } else {  // Images are not currently loaded automatically.
                    // Enable loading of images.  Missing images will be loaded without the need for a reload.
                    currentWebView.getSettings().setLoadsImagesAutomatically(true);
                }
                return true;

            case R.id.night_mode:
                // Toggle night mode.
                currentWebView.setNightMode(!currentWebView.getNightMode());

                // Enable or disable JavaScript according to night mode, the global preference, and any domain settings.
                if (currentWebView.getNightMode()) {  // Night mode is enabled, which requires JavaScript.
                    // Enable JavaScript.
                    currentWebView.getSettings().setJavaScriptEnabled(true);
                } else if (currentWebView.getDomainSettingsApplied()) {  // Night mode is disabled and domain settings are applied.  Set JavaScript according to the domain settings.
                    // Apply the JavaScript preference that was stored the last time domain settings were loaded.
                    currentWebView.getSettings().setJavaScriptEnabled(currentWebView.getDomainSettingsJavaScriptEnabled());
                } else {  // Night mode is disabled and domain settings are not applied.  Set JavaScript according to the global preference.
                    // Apply the JavaScript preference.
                    currentWebView.getSettings().setJavaScriptEnabled(sharedPreferences.getBoolean("javascript", false));
                }

                // Update the privacy icons.
                updatePrivacyIcons(false);

                // Reload the website.
                currentWebView.reload();
                return true;

            case R.id.find_on_page:
                // Get a handle for the views.
                Toolbar toolbar = findViewById(R.id.toolbar);
                LinearLayout findOnPageLinearLayout = findViewById(R.id.find_on_page_linearlayout);
                EditText findOnPageEditText = findViewById(R.id.find_on_page_edittext);

                // Set the minimum height of the find on page linear layout to match the toolbar.
                findOnPageLinearLayout.setMinimumHeight(toolbar.getHeight());

                // Hide the toolbar.
                toolbar.setVisibility(View.GONE);

                // Show the find on page linear layout.
                findOnPageLinearLayout.setVisibility(View.VISIBLE);

                // Display the keyboard.  The app must wait 200 ms before running the command to work around a bug in Android.
                // http://stackoverflow.com/questions/5520085/android-show-softkeyboard-with-showsoftinput-is-not-working
                findOnPageEditText.postDelayed(() -> {
                    // Set the focus on `findOnPageEditText`.
                    findOnPageEditText.requestFocus();

                    // Get a handle for the input method manager.
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    // Remove the lint warning below that the input method manager might be null.
                    assert inputMethodManager != null;

                    // Display the keyboard.  `0` sets no input flags.
                    inputMethodManager.showSoftInput(findOnPageEditText, 0);
                }, 200);
                return true;

            case R.id.view_source:
                // Create an intent to launch the view source activity.
                Intent viewSourceIntent = new Intent(this, ViewSourceActivity.class);

                // Add the variables to the intent.
                viewSourceIntent.putExtra("user_agent", currentWebView.getSettings().getUserAgentString());
                viewSourceIntent.putExtra("current_url", currentWebView.getUrl());

                // Make it so.
                startActivity(viewSourceIntent);
                return true;

            case R.id.share_url:
                // Setup the share string.
                String shareString = currentWebView.getTitle() + " – " + currentWebView.getUrl();

                // Create the share intent.
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
                shareIntent.setType("text/plain");

                // Make it so.
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_url)));
                return true;

            case R.id.print:
                // Get a print manager instance.
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

                // Remove the lint error below that print manager might be null.
                assert printManager != null;

                // Create a print document adapter from the current WebView.
                PrintDocumentAdapter printDocumentAdapter = currentWebView.createPrintDocumentAdapter();

                // Print the document.
                printManager.print(getString(R.string.privacy_browser_web_page), printDocumentAdapter, null);
                return true;

            case R.id.open_with_app:
                openWithApp(currentWebView.getUrl());
                return true;

            case R.id.open_with_browser:
                openWithBrowser(currentWebView.getUrl());
                return true;

            case R.id.add_to_homescreen:
                // Instantiate the create home screen shortcut dialog.
                DialogFragment createHomeScreenShortcutDialogFragment = CreateHomeScreenShortcutDialog.createDialog(currentWebView.getTitle(), currentWebView.getUrl(),
                        currentWebView.getFavoriteOrDefaultIcon());

                // Show the create home screen shortcut dialog.
                createHomeScreenShortcutDialogFragment.show(getSupportFragmentManager(), getString(R.string.create_shortcut));
                return true;

            case R.id.proxy_through_orbot:
                // Toggle the proxy through Orbot variable.
                proxyThroughOrbot = !proxyThroughOrbot;

                // Apply the proxy through Orbot settings.
                applyProxyThroughOrbot(true);
                return true;

            case R.id.refresh:
                if (menuItem.getTitle().equals(getString(R.string.refresh))) {  // The refresh button was pushed.
                    // Reload the current WebView.
                    currentWebView.reload();
                } else {  // The stop button was pushed.
                    // Stop the loading of the WebView.
                    currentWebView.stopLoading();
                }
                return true;

            case R.id.ad_consent:
                // Display the ad consent dialog.
                DialogFragment adConsentDialogFragment = new AdConsentDialog();
                adConsentDialogFragment.show(getSupportFragmentManager(), getString(R.string.ad_consent));
                return true;

            default:
                // Don't consume the event.
                return super.onOptionsItemSelected(menuItem);
        }
    }

    // removeAllCookies is deprecated, but it is required for API < 21.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Get the menu item ID.
        int menuItemId = menuItem.getItemId();

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Run the commands that correspond to the selected menu item.
        switch (menuItemId) {
            case R.id.clear_and_exit:
                // Clear and exit Privacy Browser.
                clearAndExit();
                break;

            case R.id.home:
                // Select the homepage based on the proxy through Orbot status.
                if (proxyThroughOrbot) {
                    // Load the Tor homepage.
                    loadUrl(sharedPreferences.getString("tor_homepage", getString(R.string.tor_homepage_default_value)));
                } else {
                    // Load the normal homepage.
                    loadUrl(sharedPreferences.getString("homepage", getString(R.string.homepage_default_value)));
                }
                break;

            case R.id.back:
                if (currentWebView.canGoBack()) {
                    // Reset the current domain name so that navigation works if third-party requests are blocked.
                    currentWebView.resetCurrentDomainName();

                    // Set navigating history so that the domain settings are applied when the new URL is loaded.
                    currentWebView.setNavigatingHistory(true);

                    // Load the previous website in the history.
                    currentWebView.goBack();
                }
                break;

            case R.id.forward:
                if (currentWebView.canGoForward()) {
                    // Reset the current domain name so that navigation works if third-party requests are blocked.
                    currentWebView.resetCurrentDomainName();

                    // Set navigating history so that the domain settings are applied when the new URL is loaded.
                    currentWebView.setNavigatingHistory(true);

                    // Load the next website in the history.
                    currentWebView.goForward();
                }
                break;

            case R.id.history:
                // Instantiate the URL history dialog.
                DialogFragment urlHistoryDialogFragment = UrlHistoryDialog.loadBackForwardList(currentWebView.getWebViewFragmentId());

                // Show the URL history dialog.
                urlHistoryDialogFragment.show(getSupportFragmentManager(), getString(R.string.history));
                break;

            case R.id.requests:
                // Populate the resource requests.
                RequestsActivity.resourceRequests = currentWebView.getResourceRequests();

                // Create an intent to launch the Requests activity.
                Intent requestsIntent = new Intent(this, RequestsActivity.class);

                // Add the block third-party requests status to the intent.
                requestsIntent.putExtra("block_all_third_party_requests", currentWebView.isBlocklistEnabled(NestedScrollWebView.THIRD_PARTY_REQUESTS));

                // Make it so.
                startActivity(requestsIntent);
                break;

            case R.id.downloads:
                // Launch the system Download Manager.
                Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);

                // Launch as a new task so that Download Manager and Privacy Browser show as separate windows in the recent tasks list.
                downloadManagerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(downloadManagerIntent);
                break;

            case R.id.domains:
                // Set the flag to reapply the domain settings on restart when returning from Domain Settings.
                reapplyDomainSettingsOnRestart = true;

                // Launch the domains activity.
                Intent domainsIntent = new Intent(this, DomainsActivity.class);

                // Add the extra information to the intent.
                domainsIntent.putExtra("current_url", currentWebView.getUrl());

                // Get the current certificate.
                SslCertificate sslCertificate = currentWebView.getCertificate();

                // Check to see if the SSL certificate is populated.
                if (sslCertificate != null) {
                    // Extract the certificate to strings.
                    String issuedToCName = sslCertificate.getIssuedTo().getCName();
                    String issuedToOName = sslCertificate.getIssuedTo().getOName();
                    String issuedToUName = sslCertificate.getIssuedTo().getUName();
                    String issuedByCName = sslCertificate.getIssuedBy().getCName();
                    String issuedByOName = sslCertificate.getIssuedBy().getOName();
                    String issuedByUName = sslCertificate.getIssuedBy().getUName();
                    long startDateLong = sslCertificate.getValidNotBeforeDate().getTime();
                    long endDateLong = sslCertificate.getValidNotAfterDate().getTime();

                    // Add the certificate to the intent.
                    domainsIntent.putExtra("ssl_issued_to_cname", issuedToCName);
                    domainsIntent.putExtra("ssl_issued_to_oname", issuedToOName);
                    domainsIntent.putExtra("ssl_issued_to_uname", issuedToUName);
                    domainsIntent.putExtra("ssl_issued_by_cname", issuedByCName);
                    domainsIntent.putExtra("ssl_issued_by_oname", issuedByOName);
                    domainsIntent.putExtra("ssl_issued_by_uname", issuedByUName);
                    domainsIntent.putExtra("ssl_start_date", startDateLong);
                    domainsIntent.putExtra("ssl_end_date", endDateLong);
                }

                // Check to see if the current IP addresses have been received.
                if (currentWebView.hasCurrentIpAddresses()) {
                    // Add the current IP addresses to the intent.
                    domainsIntent.putExtra("current_ip_addresses", currentWebView.getCurrentIpAddresses());
                }

                // Make it so.
                startActivity(domainsIntent);
                break;

            case R.id.settings:
                // Set the flag to reapply app settings on restart when returning from Settings.
                reapplyAppSettingsOnRestart = true;

                // Set the flag to reapply the domain settings on restart when returning from Settings.
                reapplyDomainSettingsOnRestart = true;

                // Launch the settings activity.
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.import_export:
                // Launch the import/export activity.
                Intent importExportIntent = new Intent (this, ImportExportActivity.class);
                startActivity(importExportIntent);
                break;

            case R.id.logcat:
                // Launch the logcat activity.
                Intent logcatIntent = new Intent(this, LogcatActivity.class);
                startActivity(logcatIntent);
                break;

            case R.id.guide:
                // Launch `GuideActivity`.
                Intent guideIntent = new Intent(this, GuideActivity.class);
                startActivity(guideIntent);
                break;

            case R.id.about:
                // Create an intent to launch the about activity.
                Intent aboutIntent = new Intent(this, AboutActivity.class);

                // Create a string array for the blocklist versions.
                String[] blocklistVersions = new String[] {easyList.get(0).get(0)[0], easyPrivacy.get(0).get(0)[0], fanboysAnnoyanceList.get(0).get(0)[0], fanboysSocialList.get(0).get(0)[0],
                        ultraPrivacy.get(0).get(0)[0]};

                // Add the blocklist versions to the intent.
                aboutIntent.putExtra("blocklist_versions", blocklistVersions);

                // Make it so.
                startActivity(aboutIntent);
                break;
        }

        // Get a handle for the drawer layout.
        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);

        // Close the navigation drawer.
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        // Run the default commands.
        super.onPostCreate(savedInstanceState);

        // Sync the state of the DrawerToggle after the default `onRestoreInstanceState()` has finished.  This creates the navigation drawer icon.
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Run the default commands.
        super.onConfigurationChanged(newConfig);

        // Get the status bar pixel size.
        int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarPixelSize = getResources().getDimensionPixelSize(statusBarResourceId);

        // Get the resource density.
        float screenDensity = getResources().getDisplayMetrics().density;

        // Recalculate the drawer header padding.
        drawerHeaderPaddingLeftAndRight = (int) (15 * screenDensity);
        drawerHeaderPaddingTop = statusBarPixelSize + (int) (4 * screenDensity);
        drawerHeaderPaddingBottom = (int) (8 * screenDensity);

        // Reload the ad for the free flavor if not in full screen mode.
        if (BuildConfig.FLAVOR.contentEquals("free") && !inFullScreenBrowsingMode) {
            // Reload the ad.  The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
            AdHelper.loadAd(findViewById(R.id.adview), getApplicationContext(), getString(R.string.ad_unit_id));
        }

        // `invalidateOptionsMenu` should recalculate the number of action buttons from the menu to display on the app bar, but it doesn't because of the this bug:
        // https://code.google.com/p/android/issues/detail?id=20493#c8
        // ActivityCompat.invalidateOptionsMenu(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        // Store the hit test result.
        final WebView.HitTestResult hitTestResult = currentWebView.getHitTestResult();

        // Create the URL strings.
        final String imageUrl;
        final String linkUrl;

        // Get handles for the system managers.
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Remove the lint errors below that the clipboard manager might be null.
        assert clipboardManager != null;

        // Process the link according to the type.
        switch (hitTestResult.getType()) {
            // `SRC_ANCHOR_TYPE` is a link.
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                // Get the target URL.
                linkUrl = hitTestResult.getExtra();

                // Set the target URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(linkUrl);

                // Add an Open in New Tab entry.
                menu.add(R.string.open_in_new_tab).setOnMenuItemClickListener((MenuItem item) -> {
                    // Load the link URL in a new tab.
                    addNewTab(linkUrl);
                    return false;
                });

                // Add an Open with App entry.
                menu.add(R.string.open_with_app).setOnMenuItemClickListener((MenuItem item) -> {
                    openWithApp(linkUrl);
                    return false;
                });

                // Add an Open with Browser entry.
                menu.add(R.string.open_with_browser).setOnMenuItemClickListener((MenuItem item) -> {
                    openWithBrowser(linkUrl);
                    return false;
                });

                // Add a Copy URL entry.
                menu.add(R.string.copy_url).setOnMenuItemClickListener((MenuItem item) -> {
                    // Save the link URL in a `ClipData`.
                    ClipData srcAnchorTypeClipData = ClipData.newPlainText(getString(R.string.url), linkUrl);

                    // Set the `ClipData` as the clipboard's primary clip.
                    clipboardManager.setPrimaryClip(srcAnchorTypeClipData);
                    return false;
                });

                // Add a Download URL entry.
                menu.add(R.string.download_url).setOnMenuItemClickListener((MenuItem item) -> {
                    // Check if the download should be processed by an external app.
                    if (sharedPreferences.getBoolean("download_with_external_app", false)) {  // Download with an external app.
                        openUrlWithExternalApp(linkUrl);
                    } else {  // Download with Android's download manager.
                        // Check to see if the storage permission has already been granted.
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {  // The storage permission needs to be requested.
                            // Store the variables for future use by `onRequestPermissionsResult()`.
                            downloadUrl = linkUrl;
                            downloadContentDisposition = "none";
                            downloadContentLength = -1;

                            // Show a dialog if the user has previously denied the permission.
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                                // Instantiate the download location permission alert dialog and set the download type to DOWNLOAD_FILE.
                                DialogFragment downloadLocationPermissionDialogFragment = DownloadLocationPermissionDialog.downloadType(DownloadLocationPermissionDialog.DOWNLOAD_FILE);

                                // Show the download location permission alert dialog.  The permission will be requested when the the dialog is closed.
                                downloadLocationPermissionDialogFragment.show(fragmentManager, getString(R.string.download_location));
                            } else {  // Show the permission request directly.
                                // Request the permission.  The download dialog will be launched by `onRequestPermissionResult()`.
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_FILE_REQUEST_CODE);
                            }
                        } else {  // The storage permission has already been granted.
                            // Get a handle for the download file alert dialog.
                            DialogFragment downloadFileDialogFragment = DownloadFileDialog.fromUrl(linkUrl, "none", -1);

                            // Show the download file alert dialog.
                            downloadFileDialogFragment.show(fragmentManager, getString(R.string.download));
                        }
                    }
                    return false;
                });

                // Add a Cancel entry, which by default closes the context menu.
                menu.add(R.string.cancel);
                break;

            case WebView.HitTestResult.EMAIL_TYPE:
                // Get the target URL.
                linkUrl = hitTestResult.getExtra();

                // Set the target URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(linkUrl);

                // Add a Write Email entry.
                menu.add(R.string.write_email).setOnMenuItemClickListener(item -> {
                    // Use `ACTION_SENDTO` instead of `ACTION_SEND` so that only email programs are launched.
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                    // Parse the url and set it as the data for the `Intent`.
                    emailIntent.setData(Uri.parse("mailto:" + linkUrl));

                    // `FLAG_ACTIVITY_NEW_TASK` opens the email program in a new task instead as part of Privacy Browser.
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Make it so.
                    startActivity(emailIntent);
                    return false;
                });

                // Add a Copy Email Address entry.
                menu.add(R.string.copy_email_address).setOnMenuItemClickListener(item -> {
                    // Save the email address in a `ClipData`.
                    ClipData srcEmailTypeClipData = ClipData.newPlainText(getString(R.string.email_address), linkUrl);

                    // Set the `ClipData` as the clipboard's primary clip.
                    clipboardManager.setPrimaryClip(srcEmailTypeClipData);
                    return false;
                });

                // Add a `Cancel` entry, which by default closes the `ContextMenu`.
                menu.add(R.string.cancel);
                break;

            // `IMAGE_TYPE` is an image. `SRC_IMAGE_ANCHOR_TYPE` is an image that is also a link.  Privacy Browser processes them the same.
            case WebView.HitTestResult.IMAGE_TYPE:
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                // Get the image URL.
                imageUrl = hitTestResult.getExtra();

                // Set the image URL as the title of the context menu.
                menu.setHeaderTitle(imageUrl);

                // Add an Open in New Tab entry.
                menu.add(R.string.open_in_new_tab).setOnMenuItemClickListener((MenuItem item) -> {
                    // Load the image URL in a new tab.
                    addNewTab(imageUrl);
                    return false;
                });

                // Add a View Image entry.
                menu.add(R.string.view_image).setOnMenuItemClickListener(item -> {
                    loadUrl(imageUrl);
                    return false;
                });

                // Add a `Download Image` entry.
                menu.add(R.string.download_image).setOnMenuItemClickListener((MenuItem item) -> {
                    // Check if the download should be processed by an external app.
                    if (sharedPreferences.getBoolean("download_with_external_app", false)) {  // Download with an external app.
                        openUrlWithExternalApp(imageUrl);
                    } else {  // Download with Android's download manager.
                        // Check to see if the storage permission has already been granted.
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {  // The storage permission needs to be requested.
                            // Store the image URL for use by `onRequestPermissionResult()`.
                            downloadImageUrl = imageUrl;

                            // Show a dialog if the user has previously denied the permission.
                            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                                // Instantiate the download location permission alert dialog and set the download type to DOWNLOAD_IMAGE.
                                DialogFragment downloadLocationPermissionDialogFragment = DownloadLocationPermissionDialog.downloadType(DownloadLocationPermissionDialog.DOWNLOAD_IMAGE);

                                // Show the download location permission alert dialog.  The permission will be requested when the dialog is closed.
                                downloadLocationPermissionDialogFragment.show(fragmentManager, getString(R.string.download_location));
                            } else {  // Show the permission request directly.
                                // Request the permission.  The download dialog will be launched by `onRequestPermissionResult().
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_IMAGE_REQUEST_CODE);
                            }
                        } else {  // The storage permission has already been granted.
                            // Get a handle for the download image alert dialog.
                            DialogFragment downloadImageDialogFragment = DownloadImageDialog.imageUrl(imageUrl);

                            // Show the download image alert dialog.
                            downloadImageDialogFragment.show(fragmentManager, getString(R.string.download));
                        }
                    }
                    return false;
                });

                // Add a `Copy URL` entry.
                menu.add(R.string.copy_url).setOnMenuItemClickListener(item -> {
                    // Save the image URL in a `ClipData`.
                    ClipData srcImageAnchorTypeClipData = ClipData.newPlainText(getString(R.string.url), imageUrl);

                    // Set the `ClipData` as the clipboard's primary clip.
                    clipboardManager.setPrimaryClip(srcImageAnchorTypeClipData);
                    return false;
                });

                // Add an Open with App entry.
                menu.add(R.string.open_with_app).setOnMenuItemClickListener((MenuItem item) -> {
                    openWithApp(imageUrl);
                    return false;
                });

                // Add an Open with Browser entry.
                menu.add(R.string.open_with_browser).setOnMenuItemClickListener((MenuItem item) -> {
                    openWithBrowser(imageUrl);
                    return false;
                });

                // Add a `Cancel` entry, which by default closes the `ContextMenu`.
                menu.add(R.string.cancel);
                break;
        }
    }

    @Override
    public void onCreateBookmark(DialogFragment dialogFragment, Bitmap favoriteIconBitmap) {
        // Get a handle for the bookmarks list view.
        ListView bookmarksListView = findViewById(R.id.bookmarks_drawer_listview);

        // Get the views from the dialog fragment.
        EditText createBookmarkNameEditText = dialogFragment.getDialog().findViewById(R.id.create_bookmark_name_edittext);
        EditText createBookmarkUrlEditText = dialogFragment.getDialog().findViewById(R.id.create_bookmark_url_edittext);

        // Extract the strings from the edit texts.
        String bookmarkNameString = createBookmarkNameEditText.getText().toString();
        String bookmarkUrlString = createBookmarkUrlEditText.getText().toString();

        // Create a favorite icon byte array output stream.
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the favorite icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);

        // Convert the favorite icon byte array stream to a byte array.
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Display the new bookmark below the current items in the (0 indexed) list.
        int newBookmarkDisplayOrder = bookmarksListView.getCount();

        // Create the bookmark.
        bookmarksDatabaseHelper.createBookmark(bookmarkNameString, bookmarkUrlString, currentBookmarksFolder, newBookmarkDisplayOrder, favoriteIconByteArray);

        // Update the bookmarks cursor with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrder(currentBookmarksFolder);

        // Update the list view.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Scroll to the new bookmark.
        bookmarksListView.setSelection(newBookmarkDisplayOrder);
    }

    @Override
    public void onCreateBookmarkFolder(DialogFragment dialogFragment, Bitmap favoriteIconBitmap) {
        // Get a handle for the bookmarks list view.
        ListView bookmarksListView = findViewById(R.id.bookmarks_drawer_listview);

        // Get handles for the views in the dialog fragment.
        EditText createFolderNameEditText = dialogFragment.getDialog().findViewById(R.id.create_folder_name_edittext);
        RadioButton defaultFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon_radiobutton);
        ImageView folderIconImageView = dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon);

        // Get new folder name string.
        String folderNameString = createFolderNameEditText.getText().toString();

        // Create a folder icon bitmap.
        Bitmap folderIconBitmap;

        // Set the folder icon bitmap according to the dialog.
        if (defaultFolderIconRadioButton.isChecked()) {  // Use the default folder icon.
            // Get the default folder icon drawable.
            Drawable folderIconDrawable = folderIconImageView.getDrawable();

            // Convert the folder icon drawable to a bitmap drawable.
            BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;

            // Convert the folder icon bitmap drawable to a bitmap.
            folderIconBitmap = folderIconBitmapDrawable.getBitmap();
        } else {  // Use the WebView favorite icon.
            // Copy the favorite icon bitmap to the folder icon bitmap.
            folderIconBitmap = favoriteIconBitmap;
        }

        // Create a folder icon byte array output stream.
        ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();

        // Convert the folder icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
        folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);

        // Convert the folder icon byte array stream to a byte array.
        byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

        // Move all the bookmarks down one in the display order.
        for (int i = 0; i < bookmarksListView.getCount(); i++) {
            int databaseId = (int) bookmarksListView.getItemIdAtPosition(i);
            bookmarksDatabaseHelper.updateDisplayOrder(databaseId, i + 1);
        }

        // Create the folder, which will be placed at the top of the `ListView`.
        bookmarksDatabaseHelper.createFolder(folderNameString, currentBookmarksFolder, folderIconByteArray);

        // Update the bookmarks cursor with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrder(currentBookmarksFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Scroll to the new folder.
        bookmarksListView.setSelection(0);
    }

    @Override
    public void onSaveBookmark(DialogFragment dialogFragment, int selectedBookmarkDatabaseId, Bitmap favoriteIconBitmap) {
        // Get handles for the views from `dialogFragment`.
        EditText editBookmarkNameEditText = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_name_edittext);
        EditText editBookmarkUrlEditText = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_url_edittext);
        RadioButton currentBookmarkIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_bookmark_current_icon_radiobutton);

        // Store the bookmark strings.
        String bookmarkNameString = editBookmarkNameEditText.getText().toString();
        String bookmarkUrlString = editBookmarkUrlEditText.getText().toString();

        // Update the bookmark.
        if (currentBookmarkIconRadioButton.isChecked()) {  // Update the bookmark without changing the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString);
        } else {  // Update the bookmark using the `WebView` favorite icon.
            // Create a favorite icon byte array output stream.
            ByteArrayOutputStream newFavoriteIconByteArrayOutputStream = new ByteArrayOutputStream();

            // Convert the favorite icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
            favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFavoriteIconByteArrayOutputStream);

            // Convert the favorite icon byte array stream to a byte array.
            byte[] newFavoriteIconByteArray = newFavoriteIconByteArrayOutputStream.toByteArray();

            //  Update the bookmark and the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString, newFavoriteIconByteArray);
        }

        // Update the bookmarks cursor with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrder(currentBookmarksFolder);

        // Update the list view.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }

    @Override
    public void onSaveBookmarkFolder(DialogFragment dialogFragment, int selectedFolderDatabaseId, Bitmap favoriteIconBitmap) {
        // Get handles for the views from `dialogFragment`.
        EditText editFolderNameEditText = dialogFragment.getDialog().findViewById(R.id.edit_folder_name_edittext);
        RadioButton currentFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_current_icon_radiobutton);
        RadioButton defaultFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_radiobutton);
        ImageView defaultFolderIconImageView = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_imageview);

        // Get the new folder name.
        String newFolderNameString = editFolderNameEditText.getText().toString();

        // Check if the favorite icon has changed.
        if (currentFolderIconRadioButton.isChecked()) {  // Only the name has changed.
            // Update the name in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString);
        } else if (!currentFolderIconRadioButton.isChecked() && newFolderNameString.equals(oldFolderNameString)) {  // Only the icon has changed.
            // Create the new folder icon Bitmap.
            Bitmap folderIconBitmap;

            // Populate the new folder icon bitmap.
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon drawable.
                Drawable folderIconDrawable = defaultFolderIconImageView.getDrawable();

                // Convert the folder icon drawable to a bitmap drawable.
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;

                // Convert the folder icon bitmap drawable to a bitmap.
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Use the `WebView` favorite icon.
                // Copy the favorite icon bitmap to the folder icon bitmap.
                folderIconBitmap = favoriteIconBitmap;
            }

            // Create a folder icon byte array output stream.
            ByteArrayOutputStream newFolderIconByteArrayOutputStream = new ByteArrayOutputStream();

            // Convert the folder icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFolderIconByteArrayOutputStream);

            // Convert the folder icon byte array stream to a byte array.
            byte[] newFolderIconByteArray = newFolderIconByteArrayOutputStream.toByteArray();

            // Update the folder icon in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, newFolderIconByteArray);
        } else {  // The folder icon and the name have changed.
            // Get the new folder icon `Bitmap`.
            Bitmap folderIconBitmap;
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon drawable.
                Drawable folderIconDrawable = defaultFolderIconImageView.getDrawable();

                // Convert the folder icon drawable to a bitmap drawable.
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;

                // Convert the folder icon bitmap drawable to a bitmap.
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Use the `WebView` favorite icon.
                // Copy the favorite icon bitmap to the folder icon bitmap.
                folderIconBitmap = favoriteIconBitmap;
            }

            // Create a folder icon byte array output stream.
            ByteArrayOutputStream newFolderIconByteArrayOutputStream = new ByteArrayOutputStream();

            // Convert the folder icon bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFolderIconByteArrayOutputStream);

            // Convert the folder icon byte array stream to a byte array.
            byte[] newFolderIconByteArray = newFolderIconByteArrayOutputStream.toByteArray();

            // Update the folder name and icon in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString, newFolderIconByteArray);
        }

        // Update the bookmarks cursor with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrder(currentBookmarksFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }

    @Override
    public void onCloseDownloadLocationPermissionDialog(int downloadType) {
        switch (downloadType) {
            case DownloadLocationPermissionDialog.DOWNLOAD_FILE:
                // Request the WRITE_EXTERNAL_STORAGE permission with a file request code.
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_FILE_REQUEST_CODE);
                break;

            case DownloadLocationPermissionDialog.DOWNLOAD_IMAGE:
                // Request the WRITE_EXTERNAL_STORAGE permission with an image request code.
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_IMAGE_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Get a handle for the fragment manager.
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (requestCode) {
            case DOWNLOAD_FILE_REQUEST_CODE:
                // Show the download file alert dialog.  When the dialog closes, the correct command will be used based on the permission status.
                DialogFragment downloadFileDialogFragment = DownloadFileDialog.fromUrl(downloadUrl, downloadContentDisposition, downloadContentLength);

                // On API 23, displaying the fragment must be delayed or the app will crash.
                if (Build.VERSION.SDK_INT == 23) {
                    new Handler().postDelayed(() -> downloadFileDialogFragment.show(fragmentManager, getString(R.string.download)), 500);
                } else {
                    downloadFileDialogFragment.show(fragmentManager, getString(R.string.download));
                }

                // Reset the download variables.
                downloadUrl = "";
                downloadContentDisposition = "";
                downloadContentLength = 0;
                break;

            case DOWNLOAD_IMAGE_REQUEST_CODE:
                // Show the download image alert dialog.  When the dialog closes, the correct command will be used based on the permission status.
                DialogFragment downloadImageDialogFragment = DownloadImageDialog.imageUrl(downloadImageUrl);

                // On API 23, displaying the fragment must be delayed or the app will crash.
                if (Build.VERSION.SDK_INT == 23) {
                    new Handler().postDelayed(() -> downloadImageDialogFragment.show(fragmentManager, getString(R.string.download)), 500);
                } else {
                    downloadImageDialogFragment.show(fragmentManager, getString(R.string.download));
                }

                // Reset the image URL variable.
                downloadImageUrl = "";
                break;
        }
    }

    @Override
    public void onDownloadImage(DialogFragment dialogFragment, String imageUrl) {
        // Download the image if it has an HTTP or HTTPS URI.
        if (imageUrl.startsWith("http")) {
            // Get a handle for the system `DOWNLOAD_SERVICE`.
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            // Parse `imageUrl`.
            DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(imageUrl));

            // Get a handle for the cookie manager.
            CookieManager cookieManager = CookieManager.getInstance();

            // Pass cookies to download manager if cookies are enabled.  This is required to download images from websites that require a login.
            // Code contributed 2017 Hendrik Knackstedt.  Copyright assigned to Soren Stoutner <soren@stoutner.com>.
            if (cookieManager.acceptCookie()) {
                // Get the cookies for `imageUrl`.
                String cookies = cookieManager.getCookie(imageUrl);

                // Add the cookies to `downloadRequest`.  In the HTTP request header, cookies are named `Cookie`.
                downloadRequest.addRequestHeader("Cookie", cookies);
            }

            // Get the file name from the dialog fragment.
            EditText downloadImageNameEditText = dialogFragment.getDialog().findViewById(R.id.download_image_name);
            String imageName = downloadImageNameEditText.getText().toString();

            // Specify the download location.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // External write permission granted.
                // Download to the public download directory.
                downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, imageName);
            } else {  // External write permission denied.
                // Download to the app's external download directory.
                downloadRequest.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, imageName);
            }

            // Allow `MediaScanner` to index the download if it is a media file.
            downloadRequest.allowScanningByMediaScanner();

            // Add the URL as the description for the download.
            downloadRequest.setDescription(imageUrl);

            // Show the download notification after the download is completed.
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Remove the lint warning below that `downloadManager` might be `null`.
            assert downloadManager != null;

            // Initiate the download.
            downloadManager.enqueue(downloadRequest);
        } else {  // The image is not an HTTP or HTTPS URI.
            Snackbar.make(currentWebView, R.string.cannot_download_image, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    public void onDownloadFile(DialogFragment dialogFragment, String downloadUrl) {
        // Download the file if it has an HTTP or HTTPS URI.
        if (downloadUrl.startsWith("http")) {
            // Get a handle for the system `DOWNLOAD_SERVICE`.
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            // Parse `downloadUrl`.
            DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(downloadUrl));

            // Get a handle for the cookie manager.
            CookieManager cookieManager = CookieManager.getInstance();

            // Pass cookies to download manager if cookies are enabled.  This is required to download files from websites that require a login.
            // Code contributed 2017 Hendrik Knackstedt.  Copyright assigned to Soren Stoutner <soren@stoutner.com>.
            if (cookieManager.acceptCookie()) {
                // Get the cookies for `downloadUrl`.
                String cookies = cookieManager.getCookie(downloadUrl);

                // Add the cookies to `downloadRequest`.  In the HTTP request header, cookies are named `Cookie`.
                downloadRequest.addRequestHeader("Cookie", cookies);
            }

            // Get the file name from the dialog fragment.
            EditText downloadFileNameEditText = dialogFragment.getDialog().findViewById(R.id.download_file_name);
            String fileName = downloadFileNameEditText.getText().toString();

            // Specify the download location.
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {  // External write permission granted.
                // Download to the public download directory.
                downloadRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            } else {  // External write permission denied.
                // Download to the app's external download directory.
                downloadRequest.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, fileName);
            }

            // Allow `MediaScanner` to index the download if it is a media file.
            downloadRequest.allowScanningByMediaScanner();

            // Add the URL as the description for the download.
            downloadRequest.setDescription(downloadUrl);

            // Show the download notification after the download is completed.
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Remove the lint warning below that `downloadManager` might be `null`.
            assert downloadManager != null;

            // Initiate the download.
            downloadManager.enqueue(downloadRequest);
        } else {  // The download is not an HTTP or HTTPS URI.
            Snackbar.make(currentWebView, R.string.cannot_download_file, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    // Override `onBackPressed` to handle the navigation drawer and and the WebView.
    @Override
    public void onBackPressed() {
        // Get a handle for the drawer layout and the tab layout.
        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);
        TabLayout tabLayout = findViewById(R.id.tablayout);

        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {  // The navigation drawer is open.
            // Close the navigation drawer.
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerVisible(GravityCompat.END)){  // The bookmarks drawer is open.
            if (currentBookmarksFolder.isEmpty()) {  // The home folder is displayed.
                // close the bookmarks drawer.
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {  // A subfolder is displayed.
                // Place the former parent folder in `currentFolder`.
                currentBookmarksFolder = bookmarksDatabaseHelper.getParentFolderName(currentBookmarksFolder);

                // Load the new folder.
                loadBookmarksFolder();
            }
        } else if (currentWebView.canGoBack()) {  // There is at least one item in the current WebView history.
            // Reset the current domain name so that navigation works if third-party requests are blocked.
            currentWebView.resetCurrentDomainName();

            // Set navigating history so that the domain settings are applied when the new URL is loaded.
            currentWebView.setNavigatingHistory(true);

            // Go back.
            currentWebView.goBack();
        } else if (tabLayout.getTabCount() > 1) {  // There are at least two tabs.
            // Close the current tab.
            closeCurrentTab();
        } else {  // There isn't anything to do in Privacy Browser.
            // Run the default commands.
            super.onBackPressed();

            // Manually kill Privacy Browser.  Otherwise, it is glitchy when restarted.
            System.exit(0);
        }
    }

    // Process the results of an upload file chooser.  Currently there is only one `startActivityForResult` in this activity, so the request code, used to differentiate them, is ignored.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // File uploads only work on API >= 21.
        if (Build.VERSION.SDK_INT >= 21) {
            // Pass the file to the WebView.
            fileChooserCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
        }
    }

    private void loadUrlFromTextBox() {
        // Get a handle for the URL edit text.
        EditText urlEditText = findViewById(R.id.url_edittext);

        // Get the text from urlTextBox and convert it to a string.  trim() removes white spaces from the beginning and end of the string.
        String unformattedUrlString = urlEditText.getText().toString().trim();

        // Initialize the formatted URL string.
        String url = "";

        // Check to see if `unformattedUrlString` is a valid URL.  Otherwise, convert it into a search.
        if (unformattedUrlString.startsWith("content://")) {  // This is a Content URL.
            // Load the entire content URL.
            url = unformattedUrlString;
        } else if (Patterns.WEB_URL.matcher(unformattedUrlString).matches() || unformattedUrlString.startsWith("http://") || unformattedUrlString.startsWith("https://") ||
                unformattedUrlString.startsWith("file://")) {  // This is a standard URL.
            // Add `https://` at the beginning if there is no protocol.  Otherwise the app will segfault.
            if (!unformattedUrlString.startsWith("http") && !unformattedUrlString.startsWith("file://") && !unformattedUrlString.startsWith("content://")) {
                unformattedUrlString = "https://" + unformattedUrlString;
            }

            // Initialize `unformattedUrl`.
            URL unformattedUrl = null;

            // Convert `unformattedUrlString` to a `URL`, then to a `URI`, and then back to a `String`, which sanitizes the input and adds in any missing components.
            try {
                unformattedUrl = new URL(unformattedUrlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            // The ternary operator (? :) makes sure that a null pointer exception is not thrown, which would happen if `.get` was called on a `null` value.
            String scheme = unformattedUrl != null ? unformattedUrl.getProtocol() : null;
            String authority = unformattedUrl != null ? unformattedUrl.getAuthority() : null;
            String path = unformattedUrl != null ? unformattedUrl.getPath() : null;
            String query = unformattedUrl != null ? unformattedUrl.getQuery() : null;
            String fragment = unformattedUrl != null ? unformattedUrl.getRef() : null;

            // Build the URI.
            Uri.Builder uri = new Uri.Builder();
            uri.scheme(scheme).authority(authority).path(path).query(query).fragment(fragment);

            // Decode the URI as a UTF-8 string in.
            try {
                url = URLDecoder.decode(uri.build().toString(), "UTF-8");
            } catch (UnsupportedEncodingException exception) {
                // Do nothing.  The formatted URL string will remain blank.
            }
        } else if (!unformattedUrlString.isEmpty()){  // This is not a URL, but rather a search string.
            // Create an encoded URL String.
            String encodedUrlString;

            // Sanitize the search input.
            try {
                encodedUrlString = URLEncoder.encode(unformattedUrlString, "UTF-8");
            } catch (UnsupportedEncodingException exception) {
                encodedUrlString = "";
            }

            // Add the base search URL.
            url = searchURL + encodedUrlString;
        }

        // Clear the focus from the URL edit text.  Otherwise, proximate typing in the box will retain the colorized formatting instead of being reset during refocus.
        urlEditText.clearFocus();

        // Make it so.
        loadUrl(url);
    }

    private void loadUrl(String url) {
        // Sanitize the URL.
        url = sanitizeUrl(url);

        // Apply the domain settings.
        applyDomainSettings(currentWebView, url, true, false);

        // Load the URL.
        currentWebView.loadUrl(url, customHeaders);
    }

    public void findPreviousOnPage(View view) {
        // Go to the previous highlighted phrase on the page.  `false` goes backwards instead of forwards.
        currentWebView.findNext(false);
    }

    public void findNextOnPage(View view) {
        // Go to the next highlighted phrase on the page. `true` goes forwards instead of backwards.
        currentWebView.findNext(true);
    }

    public void closeFindOnPage(View view) {
        // Get a handle for the views.
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout findOnPageLinearLayout = findViewById(R.id.find_on_page_linearlayout);
        EditText findOnPageEditText = findViewById(R.id.find_on_page_edittext);

        // Delete the contents of `find_on_page_edittext`.
        findOnPageEditText.setText(null);

        // Clear the highlighted phrases if the WebView is not null.
        if (currentWebView != null) {
            currentWebView.clearMatches();
        }

        // Hide the find on page linear layout.
        findOnPageLinearLayout.setVisibility(View.GONE);

        // Show the toolbar.
        toolbar.setVisibility(View.VISIBLE);

        // Get a handle for the input method manager.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Remove the lint warning below that the input method manager might be null.
        assert inputMethodManager != null;

        // Hide the keyboard.
        inputMethodManager.hideSoftInputFromWindow(currentWebView.getWindowToken(), 0);
    }

    private void applyAppSettings() {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Store the values from the shared preferences in variables.
        incognitoModeEnabled = sharedPreferences.getBoolean("incognito_mode", false);
        boolean doNotTrackEnabled = sharedPreferences.getBoolean("do_not_track", false);
        sanitizeGoogleAnalytics = sharedPreferences.getBoolean("google_analytics", true);
        sanitizeFacebookClickIds = sharedPreferences.getBoolean("facebook_click_ids", true);
        proxyThroughOrbot = sharedPreferences.getBoolean("proxy_through_orbot", false);
        fullScreenBrowsingModeEnabled = sharedPreferences.getBoolean("full_screen_browsing_mode", false);
        hideAppBar = sharedPreferences.getBoolean("hide_app_bar", true);
        scrollAppBar = sharedPreferences.getBoolean("scroll_app_bar", true);

        // Get handles for the views that need to be modified.
        FrameLayout rootFrameLayout = findViewById(R.id.root_framelayout);
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        ActionBar actionBar = getSupportActionBar();
        Toolbar toolbar = findViewById(R.id.toolbar);
        LinearLayout findOnPageLinearLayout = findViewById(R.id.find_on_page_linearlayout);
        LinearLayout tabsLinearLayout = findViewById(R.id.tabs_linearlayout);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

        // Remove the incorrect lint warning below that the action bar might be null.
        assert actionBar != null;

        // Apply the proxy through Orbot settings.
        applyProxyThroughOrbot(false);

        // Set Do Not Track status.
        if (doNotTrackEnabled) {
            customHeaders.put("DNT", "1");
        } else {
            customHeaders.remove("DNT");
        }

        // Get the current layout parameters.  Using coordinator layout parameters allows the `setBehavior()` command and using app bar layout parameters allows the `setScrollFlags()` command.
        CoordinatorLayout.LayoutParams swipeRefreshLayoutParams = (CoordinatorLayout.LayoutParams) swipeRefreshLayout.getLayoutParams();
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        AppBarLayout.LayoutParams findOnPageLayoutParams = (AppBarLayout.LayoutParams) findOnPageLinearLayout.getLayoutParams();
        AppBarLayout.LayoutParams tabsLayoutParams = (AppBarLayout.LayoutParams) tabsLinearLayout.getLayoutParams();

        // Add the scrolling behavior to the layout parameters.
        if (scrollAppBar) {
            // Enable scrolling of the app bar.
            swipeRefreshLayoutParams.setBehavior(new AppBarLayout.ScrollingViewBehavior());
            toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            findOnPageLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            tabsLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        } else {
            // Disable scrolling of the app bar.
            swipeRefreshLayoutParams.setBehavior(null);
            toolbarLayoutParams.setScrollFlags(0);
            findOnPageLayoutParams.setScrollFlags(0);
            tabsLayoutParams.setScrollFlags(0);

            // Expand the app bar if it is currently collapsed.
            appBarLayout.setExpanded(true);
        }

        // Apply the modified layout parameters.
        swipeRefreshLayout.setLayoutParams(swipeRefreshLayoutParams);
        toolbar.setLayoutParams(toolbarLayoutParams);
        findOnPageLinearLayout.setLayoutParams(findOnPageLayoutParams);
        tabsLinearLayout.setLayoutParams(tabsLayoutParams);

        // Set the app bar scrolling for each WebView.
        for (int i = 0; i < webViewPagerAdapter.getCount(); i++) {
            // Get the WebView tab fragment.
            WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(i);

            // Get the fragment view.
            View fragmentView = webViewTabFragment.getView();

            // Only modify the WebViews if they exist.
            if (fragmentView != null) {
                // Get the nested scroll WebView from the tab fragment.
                NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

                // Set the app bar scrolling.
                nestedScrollWebView.setNestedScrollingEnabled(scrollAppBar);
            }
        }

        // Update the full screen browsing mode settings.
        if (fullScreenBrowsingModeEnabled && inFullScreenBrowsingMode) {  // Privacy Browser is currently in full screen browsing mode.
            // Update the visibility of the app bar, which might have changed in the settings.
            if (hideAppBar) {
                // Hide the tab linear layout.
                tabsLinearLayout.setVisibility(View.GONE);

                // Hide the action bar.
                actionBar.hide();
            } else {
                // Show the tab linear layout.
                tabsLinearLayout.setVisibility(View.VISIBLE);

                // Show the action bar.
                actionBar.show();
            }

            // Hide the banner ad in the free flavor.
            if (BuildConfig.FLAVOR.contentEquals("free")) {
                AdHelper.hideAd(findViewById(R.id.adview));
            }

            // Remove the translucent status flag.  This is necessary so the root frame layout can fill the entire screen.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            /* Hide the system bars.
             * SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
             * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN makes the root frame layout fill the area that is normally reserved for the status bar.
             * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
             * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
             */
            rootFrameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {  // Privacy Browser is not in full screen browsing mode.
            // Reset the full screen tracker, which could be true if Privacy Browser was in full screen mode before entering settings and full screen browsing was disabled.
            inFullScreenBrowsingMode = false;

            // Show the tab linear layout.
            tabsLinearLayout.setVisibility(View.VISIBLE);

            // Show the action bar.
            actionBar.show();

            // Show the banner ad in the free flavor.
            if (BuildConfig.FLAVOR.contentEquals("free")) {
                // Initialize the ads.  If this isn't the first run, `loadAd()` will be automatically called instead.
                AdHelper.initializeAds(findViewById(R.id.adview), getApplicationContext(), getSupportFragmentManager(), getString(R.string.google_app_id), getString(R.string.ad_unit_id));
            }

            // Remove the `SYSTEM_UI` flags from the root frame layout.
            rootFrameLayout.setSystemUiVisibility(0);

            // Add the translucent status flag.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }


    // `reloadWebsite` is used if returning from the Domains activity.  Otherwise JavaScript might not function correctly if it is newly enabled.
    @SuppressLint("SetJavaScriptEnabled")
    private boolean applyDomainSettings(NestedScrollWebView nestedScrollWebView, String url, boolean resetTab, boolean reloadWebsite) {
        // Store a copy of the current user agent to track changes for the return boolean.
        String initialUserAgent = nestedScrollWebView.getSettings().getUserAgentString();

        // Parse the URL into a URI.
        Uri uri = Uri.parse(url);

        // Extract the domain from `uri`.
        String newHostName = uri.getHost();

        // Strings don't like to be null.
        if (newHostName == null) {
            newHostName = "";
        }

        // Only apply the domain settings if a new domain is being loaded.  This allows the user to set temporary settings for JavaScript, cookies, DOM storage, etc.
        if (!nestedScrollWebView.getCurrentDomainName().equals(newHostName)) {
            // Set the new host name as the current domain name.
            nestedScrollWebView.setCurrentDomainName(newHostName);

            // Reset the ignoring of pinned domain information.
            nestedScrollWebView.setIgnorePinnedDomainInformation(false);

            // Clear any pinned SSL certificate or IP addresses.
            nestedScrollWebView.clearPinnedSslCertificate();
            nestedScrollWebView.clearPinnedIpAddresses();

            // Reset the favorite icon if specified.
            if (resetTab) {
                // Initialize the favorite icon.
                nestedScrollWebView.initializeFavoriteIcon();

                // Get the current page position.
                int currentPagePosition = webViewPagerAdapter.getPositionForId(nestedScrollWebView.getWebViewFragmentId());

                // Get a handle for the tab layout.
                TabLayout tabLayout = findViewById(R.id.tablayout);

                // Get the corresponding tab.
                TabLayout.Tab tab = tabLayout.getTabAt(currentPagePosition);

                // Update the tab if it isn't null, which sometimes happens when restarting from the background.
                if (tab != null) {
                    // Get the tab custom view.
                    View tabCustomView = tab.getCustomView();

                    // Remove the warning below that the tab custom view might be null.
                    assert tabCustomView != null;

                    // Get the tab views.
                    ImageView tabFavoriteIconImageView = tabCustomView.findViewById(R.id.favorite_icon_imageview);
                    TextView tabTitleTextView = tabCustomView.findViewById(R.id.title_textview);

                    // Set the default favorite icon as the favorite icon for this tab.
                    tabFavoriteIconImageView.setImageBitmap(Bitmap.createScaledBitmap(nestedScrollWebView.getFavoriteOrDefaultIcon(), 64, 64, true));

                    // Set the loading title text.
                    tabTitleTextView.setText(R.string.loading);
                }
            }

            // Initialize the database handler.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
            DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(this, null, null, 0);

            // Get a full cursor from `domainsDatabaseHelper`.
            Cursor domainNameCursor = domainsDatabaseHelper.getDomainNameCursorOrderedByDomain();

            // Initialize `domainSettingsSet`.
            Set<String> domainSettingsSet = new HashSet<>();

            // Get the domain name column index.
            int domainNameColumnIndex = domainNameCursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME);

            // Populate `domainSettingsSet`.
            for (int i = 0; i < domainNameCursor.getCount(); i++) {
                // Move `domainsCursor` to the current row.
                domainNameCursor.moveToPosition(i);

                // Store the domain name in `domainSettingsSet`.
                domainSettingsSet.add(domainNameCursor.getString(domainNameColumnIndex));
            }

            // Close `domainNameCursor.
            domainNameCursor.close();

            // Initialize the domain name in database variable.
            String domainNameInDatabase = null;

            // Check the hostname against the domain settings set.
            if (domainSettingsSet.contains(newHostName)) {  // The hostname is contained in the domain settings set.
                // Record the domain name in the database.
                domainNameInDatabase = newHostName;

                // Set the domain settings applied tracker to true.
                nestedScrollWebView.setDomainSettingsApplied(true);
            } else {  // The hostname is not contained in the domain settings set.
                // Set the domain settings applied tracker to false.
                nestedScrollWebView.setDomainSettingsApplied(false);
            }

            // Check all the subdomains of the host name against wildcard domains in the domain cursor.
            while (!nestedScrollWebView.getDomainSettingsApplied() && newHostName.contains(".")) {  // Stop checking if domain settings are already applied or there are no more `.` in the host name.
                if (domainSettingsSet.contains("*." + newHostName)) {  // Check the host name prepended by `*.`.
                    // Set the domain settings applied tracker to true.
                    nestedScrollWebView.setDomainSettingsApplied(true);

                    // Store the applied domain names as it appears in the database.
                    domainNameInDatabase = "*." + newHostName;
                }

                // Strip out the lowest subdomain of of the host name.
                newHostName = newHostName.substring(newHostName.indexOf(".") + 1);
            }


            // Get a handle for the shared preferences.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            // Store the general preference information.
            String defaultFontSizeString = sharedPreferences.getString("font_size", getString(R.string.font_size_default_value));
            String defaultUserAgentName = sharedPreferences.getString("user_agent", getString(R.string.user_agent_default_value));
            boolean defaultSwipeToRefresh = sharedPreferences.getBoolean("swipe_to_refresh", true);
            boolean displayWebpageImages = sharedPreferences.getBoolean("display_webpage_images", true);
            boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

            // Get a handle for the cookie manager.
            CookieManager cookieManager = CookieManager.getInstance();

            // Get handles for the views.
            RelativeLayout urlRelativeLayout = findViewById(R.id.url_relativelayout);
            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

            // Initialize the user agent array adapter and string array.
            ArrayAdapter<CharSequence> userAgentNamesArray = ArrayAdapter.createFromResource(this, R.array.user_agent_names, R.layout.spinner_item);
            String[] userAgentDataArray = getResources().getStringArray(R.array.user_agent_data);

            if (nestedScrollWebView.getDomainSettingsApplied()) {  // The url has custom domain settings.
                // Get a cursor for the current host and move it to the first position.
                Cursor currentDomainSettingsCursor = domainsDatabaseHelper.getCursorForDomainName(domainNameInDatabase);
                currentDomainSettingsCursor.moveToFirst();

                // Get the settings from the cursor.
                nestedScrollWebView.setDomainSettingsDatabaseId(currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper._ID)));
                nestedScrollWebView.setDomainSettingsJavaScriptEnabled(currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_JAVASCRIPT)) == 1);
                nestedScrollWebView.setAcceptFirstPartyCookies(currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES)) == 1);
                boolean domainThirdPartyCookiesEnabled = (currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES)) == 1);
                nestedScrollWebView.getSettings().setDomStorageEnabled(currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_DOM_STORAGE)) == 1);
                // Form data can be removed once the minimum API >= 26.
                boolean saveFormData = (currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FORM_DATA)) == 1);
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.EASY_LIST,
                        currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYLIST)) == 1);
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.EASY_PRIVACY,
                        currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYPRIVACY)) == 1);
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST,
                        currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_ANNOYANCE_LIST)) == 1);
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST,
                        currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_SOCIAL_BLOCKING_LIST)) == 1);
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.ULTRA_PRIVACY,
                        currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_ULTRAPRIVACY)) == 1);
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.THIRD_PARTY_REQUESTS,
                        currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.BLOCK_ALL_THIRD_PARTY_REQUESTS)) == 1);
                String userAgentName = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.USER_AGENT));
                int fontSize = currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE));
                int swipeToRefreshInt = currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SWIPE_TO_REFRESH));
                int nightModeInt = currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.NIGHT_MODE));
                int displayWebpageImagesInt = currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.DISPLAY_IMAGES));
                boolean pinnedSslCertificate = (currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.PINNED_SSL_CERTIFICATE)) == 1);
                String pinnedSslIssuedToCName = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_COMMON_NAME));
                String pinnedSslIssuedToOName = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATION));
                String pinnedSslIssuedToUName = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATIONAL_UNIT));
                String pinnedSslIssuedByCName = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_COMMON_NAME));
                String pinnedSslIssuedByOName = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATION));
                String pinnedSslIssuedByUName = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATIONAL_UNIT));
                boolean pinnedIpAddresses = (currentDomainSettingsCursor.getInt(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.PINNED_IP_ADDRESSES)) == 1);
                String pinnedHostIpAddresses = currentDomainSettingsCursor.getString(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.IP_ADDRESSES));

                // Create the pinned SSL date variables.
                Date pinnedSslStartDate;
                Date pinnedSslEndDate;

                // Set the pinned SSL certificate start date to `null` if the saved date `long` is 0 because creating a new Date results in an error if the input is 0.
                if (currentDomainSettingsCursor.getLong(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)) == 0) {
                    pinnedSslStartDate = null;
                } else {
                    pinnedSslStartDate = new Date(currentDomainSettingsCursor.getLong(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)));
                }

                // Set the pinned SSL certificate end date to `null` if the saved date `long` is 0 because creating a new Date results in an error if the input is 0.
                if (currentDomainSettingsCursor.getLong(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)) == 0) {
                    pinnedSslEndDate = null;
                } else {
                    pinnedSslEndDate = new Date(currentDomainSettingsCursor.getLong(currentDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)));
                }

                // If there is a pinned SSL certificate, store it in the WebView.
                if (pinnedSslCertificate) {
                    nestedScrollWebView.setPinnedSslCertificate(pinnedSslIssuedToCName, pinnedSslIssuedToOName, pinnedSslIssuedToUName, pinnedSslIssuedByCName, pinnedSslIssuedByOName, pinnedSslIssuedByUName,
                            pinnedSslStartDate, pinnedSslEndDate);
                }

                // If there is a pinned IP address, store it in the WebView.
                if (pinnedIpAddresses) {
                    nestedScrollWebView.setPinnedIpAddresses(pinnedHostIpAddresses);
                }

                // Set night mode according to the night mode int.
                switch (nightModeInt) {
                    case DomainsDatabaseHelper.NIGHT_MODE_SYSTEM_DEFAULT:
                        // Set night mode according to the current default.
                        nestedScrollWebView.setNightMode(sharedPreferences.getBoolean("night_mode", false));
                        break;

                    case DomainsDatabaseHelper.NIGHT_MODE_ENABLED:
                        // Enable night mode.
                        nestedScrollWebView.setNightMode(true);
                        break;

                    case DomainsDatabaseHelper.NIGHT_MODE_DISABLED:
                        // Disable night mode.
                        nestedScrollWebView.setNightMode(false);
                        break;
                }

                // Enable JavaScript if night mode is enabled.
                if (nestedScrollWebView.getNightMode()) {
                    // Enable JavaScript.
                    nestedScrollWebView.getSettings().setJavaScriptEnabled(true);
                } else {
                    // Set JavaScript according to the domain settings.
                    nestedScrollWebView.getSettings().setJavaScriptEnabled(nestedScrollWebView.getDomainSettingsJavaScriptEnabled());
                }

                // Close the current host domain settings cursor.
                currentDomainSettingsCursor.close();

                // Apply the domain settings.
                cookieManager.setAcceptCookie(nestedScrollWebView.getAcceptFirstPartyCookies());

                // Set third-party cookies status if API >= 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.setAcceptThirdPartyCookies(nestedScrollWebView, domainThirdPartyCookiesEnabled);
                }

                // Apply the form data setting if the API < 26.
                if (Build.VERSION.SDK_INT < 26) {
                    nestedScrollWebView.getSettings().setSaveFormData(saveFormData);
                }

                // Apply the font size.
                if (fontSize == 0) {  // Apply the default font size.
                    nestedScrollWebView.getSettings().setTextZoom(Integer.valueOf(defaultFontSizeString));
                } else {  // Apply the specified font size.
                    nestedScrollWebView.getSettings().setTextZoom(fontSize);
                }

                // Only set the user agent if the webpage is not currently loading.  Otherwise, changing the user agent on redirects can cause the original website to reload.
                // <https://redmine.stoutner.com/issues/160>
                if (nestedScrollWebView.getProgress() == 100) {  // A URL is not loading.
                    // Set the user agent.
                    if (userAgentName.equals(getString(R.string.system_default_user_agent))) {  // Use the system default user agent.
                        // Get the array position of the default user agent name.
                        int defaultUserAgentArrayPosition = userAgentNamesArray.getPosition(defaultUserAgentName);

                        // Set the user agent according to the system default.
                        switch (defaultUserAgentArrayPosition) {
                            case UNRECOGNIZED_USER_AGENT:  // The default user agent name is not on the canonical list.
                                // This is probably because it was set in an older version of Privacy Browser before the switch to persistent user agent names.
                                nestedScrollWebView.getSettings().setUserAgentString(defaultUserAgentName);
                                break;

                            case SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                                // Set the user agent to `""`, which uses the default value.
                                nestedScrollWebView.getSettings().setUserAgentString("");
                                break;

                            case SETTINGS_CUSTOM_USER_AGENT:
                                // Set the default custom user agent.
                                nestedScrollWebView.getSettings().setUserAgentString(sharedPreferences.getString("custom_user_agent", getString(R.string.custom_user_agent_default_value)));
                                break;

                            default:
                                // Get the user agent string from the user agent data array
                                nestedScrollWebView.getSettings().setUserAgentString(userAgentDataArray[defaultUserAgentArrayPosition]);
                        }
                    } else {  // Set the user agent according to the stored name.
                        // Get the array position of the user agent name.
                        int userAgentArrayPosition = userAgentNamesArray.getPosition(userAgentName);

                        switch (userAgentArrayPosition) {
                            case UNRECOGNIZED_USER_AGENT:  // The user agent name contains a custom user agent.
                                nestedScrollWebView.getSettings().setUserAgentString(userAgentName);
                                break;

                            case SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                                // Set the user agent to `""`, which uses the default value.
                                nestedScrollWebView.getSettings().setUserAgentString("");
                                break;

                            default:
                                // Get the user agent string from the user agent data array.
                                nestedScrollWebView.getSettings().setUserAgentString(userAgentDataArray[userAgentArrayPosition]);
                        }
                    }
                }

                // Set swipe to refresh.
                switch (swipeToRefreshInt) {
                    case DomainsDatabaseHelper.SWIPE_TO_REFRESH_SYSTEM_DEFAULT:
                        // Store the swipe to refresh status in the nested scroll WebView.
                        nestedScrollWebView.setSwipeToRefresh(defaultSwipeToRefresh);

                        // Apply swipe to refresh according to the default.  This can be removed once the minimum API >= 23 because it is continuously set by an on scroll change listener.
                        swipeRefreshLayout.setEnabled(defaultSwipeToRefresh);
                        break;

                    case DomainsDatabaseHelper.SWIPE_TO_REFRESH_ENABLED:
                        // Store the swipe to refresh status in the nested scroll WebView.
                        nestedScrollWebView.setSwipeToRefresh(true);

                        // Enable swipe to refresh.  This can be removed once the minimum API >= 23 because it is continuously set by an on scroll change listener.
                        swipeRefreshLayout.setEnabled(true);
                        break;

                    case DomainsDatabaseHelper.SWIPE_TO_REFRESH_DISABLED:
                        // Store the swipe to refresh status in the nested scroll WebView.
                        nestedScrollWebView.setSwipeToRefresh(false);

                        // Disable swipe to refresh.  This can be removed once the minimum API >= 23 because it is continuously set by an on scroll change listener.
                        swipeRefreshLayout.setEnabled(false);
                }

                // Set the loading of webpage images.
                switch (displayWebpageImagesInt) {
                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_SYSTEM_DEFAULT:
                        nestedScrollWebView.getSettings().setLoadsImagesAutomatically(displayWebpageImages);
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_ENABLED:
                        nestedScrollWebView.getSettings().setLoadsImagesAutomatically(true);
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_DISABLED:
                        nestedScrollWebView.getSettings().setLoadsImagesAutomatically(false);
                        break;
                }

                // Set a green background on the URL relative layout to indicate that custom domain settings are being used. The deprecated `.getDrawable()` must be used until the minimum API >= 21.
                if (darkTheme) {
                    urlRelativeLayout.setBackground(getResources().getDrawable(R.drawable.url_bar_background_dark_blue));
                } else {
                    urlRelativeLayout.setBackground(getResources().getDrawable(R.drawable.url_bar_background_light_green));
                }
            } else {  // The new URL does not have custom domain settings.  Load the defaults.
                // Store the values from the shared preferences.
                boolean defaultJavaScriptEnabled = sharedPreferences.getBoolean("javascript", false);
                nestedScrollWebView.setAcceptFirstPartyCookies(sharedPreferences.getBoolean("first_party_cookies", false));
                boolean defaultThirdPartyCookiesEnabled = sharedPreferences.getBoolean("third_party_cookies", false);
                nestedScrollWebView.getSettings().setDomStorageEnabled(sharedPreferences.getBoolean("dom_storage", false));
                boolean saveFormData = sharedPreferences.getBoolean("save_form_data", false);  // Form data can be removed once the minimum API >= 26.
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.EASY_LIST, sharedPreferences.getBoolean("easylist", true));
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.EASY_PRIVACY, sharedPreferences.getBoolean("easyprivacy", true));
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST, sharedPreferences.getBoolean("fanboys_annoyance_list", true));
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST, sharedPreferences.getBoolean("fanboys_social_blocking_list", true));
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.ULTRA_PRIVACY, sharedPreferences.getBoolean("ultraprivacy", true));
                nestedScrollWebView.enableBlocklist(NestedScrollWebView.THIRD_PARTY_REQUESTS, sharedPreferences.getBoolean("block_all_third_party_requests", false));
                nestedScrollWebView.setNightMode(sharedPreferences.getBoolean("night_mode", false));

                // Enable JavaScript if night mode is enabled.
                if (nestedScrollWebView.getNightMode()) {
                    // Enable JavaScript.
                    nestedScrollWebView.getSettings().setJavaScriptEnabled(true);
                } else {
                    // Set JavaScript according to the domain settings.
                    nestedScrollWebView.getSettings().setJavaScriptEnabled(defaultJavaScriptEnabled);
                }

                // Apply the default settings.
                cookieManager.setAcceptCookie(nestedScrollWebView.getAcceptFirstPartyCookies());
                nestedScrollWebView.getSettings().setTextZoom(Integer.valueOf(defaultFontSizeString));

                // Apply the form data setting if the API < 26.
                if (Build.VERSION.SDK_INT < 26) {
                    nestedScrollWebView.getSettings().setSaveFormData(saveFormData);
                }

                // Store the swipe to refresh status in the nested scroll WebView.
                nestedScrollWebView.setSwipeToRefresh(defaultSwipeToRefresh);

                // Apply swipe to refresh according to the default.
                swipeRefreshLayout.setEnabled(defaultSwipeToRefresh);

                // Reset the pinned variables.
                nestedScrollWebView.setDomainSettingsDatabaseId(-1);

                // Set third-party cookies status if API >= 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.setAcceptThirdPartyCookies(nestedScrollWebView, defaultThirdPartyCookiesEnabled);
                }

                // Only set the user agent if the webpage is not currently loading.  Otherwise, changing the user agent on redirects can cause the original website to reload.
                // <https://redmine.stoutner.com/issues/160>
                if (nestedScrollWebView.getProgress() == 100) {  // A URL is not loading.
                    // Get the array position of the user agent name.
                    int userAgentArrayPosition = userAgentNamesArray.getPosition(defaultUserAgentName);

                    // Set the user agent.
                    switch (userAgentArrayPosition) {
                        case UNRECOGNIZED_USER_AGENT:  // The default user agent name is not on the canonical list.
                            // This is probably because it was set in an older version of Privacy Browser before the switch to persistent user agent names.
                            nestedScrollWebView.getSettings().setUserAgentString(defaultUserAgentName);
                            break;

                        case SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                            // Set the user agent to `""`, which uses the default value.
                            nestedScrollWebView.getSettings().setUserAgentString("");
                            break;

                        case SETTINGS_CUSTOM_USER_AGENT:
                            // Set the default custom user agent.
                            nestedScrollWebView.getSettings().setUserAgentString(sharedPreferences.getString("custom_user_agent", getString(R.string.custom_user_agent_default_value)));
                            break;

                        default:
                            // Get the user agent string from the user agent data array
                            nestedScrollWebView.getSettings().setUserAgentString(userAgentDataArray[userAgentArrayPosition]);
                    }
                }

                // Set the loading of webpage images.
                nestedScrollWebView.getSettings().setLoadsImagesAutomatically(displayWebpageImages);

                // Set a transparent background on URL edit text.  The deprecated `getResources().getDrawable()` must be used until the minimum API >= 21.
                urlRelativeLayout.setBackground(getResources().getDrawable(R.color.transparent));
            }

            // Close the domains database helper.
            domainsDatabaseHelper.close();

            // Update the privacy icons.
            updatePrivacyIcons(true);
        }

        // Reload the website if returning from the Domains activity.
        if (reloadWebsite) {
            nestedScrollWebView.reload();
        }

        // Return the user agent changed status.
        return !nestedScrollWebView.getSettings().getUserAgentString().equals(initialUserAgent);
    }

    private void applyProxyThroughOrbot(boolean reloadWebsite) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the search and theme preferences.
        String torSearchString = sharedPreferences.getString("tor_search", getString(R.string.tor_search_default_value));
        String torSearchCustomUrlString = sharedPreferences.getString("tor_search_custom_url", getString(R.string.tor_search_custom_url_default_value));
        String searchString = sharedPreferences.getString("search", getString(R.string.search_default_value));
        String searchCustomUrlString = sharedPreferences.getString("search_custom_url", getString(R.string.search_custom_url_default_value));
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

        // Get a handle for the app bar layout.
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);

        // Set the homepage, search, and proxy options.
        if (proxyThroughOrbot) {  // Set the Tor options.
            // Set the search URL.
            if (torSearchString.equals("Custom URL")) {  // Get the custom URL string.
                searchURL = torSearchCustomUrlString;
            } else {  // Use the string from the pre-built list.
                searchURL = torSearchString;
            }

            // Set the proxy.  `this` refers to the current activity where an `AlertDialog` might be displayed.
            OrbotProxyHelper.setProxy(getApplicationContext(), this, "localhost", "8118");

            // Set the app bar background to indicate proxying through Orbot is enabled.
            if (darkTheme) {
                appBarLayout.setBackgroundResource(R.color.dark_blue_30);
            } else {
                appBarLayout.setBackgroundResource(R.color.blue_50);
            }

            // Check to see if Orbot is ready.
            if (!orbotStatus.equals("ON")) {  // Orbot is not ready.
                // Set `waitingForOrbot`.
                waitingForOrbot = true;

                // Disable the wide view port so that the waiting for Orbot text is displayed correctly.
                currentWebView.getSettings().setUseWideViewPort(false);

                // Load a waiting page.  `null` specifies no encoding, which defaults to ASCII.
                currentWebView.loadData("<html><body><br/><center><h1>" + getString(R.string.waiting_for_orbot) + "</h1></center></body></html>", "text/html", null);
            } else if (reloadWebsite) {  // Orbot is ready and the website should be reloaded.
                // Reload the website.
                currentWebView.reload();
            }
        } else {  // Set the non-Tor options.
            // Set the search URL.
            if (searchString.equals("Custom URL")) {  // Get the custom URL string.
                searchURL = searchCustomUrlString;
            } else {  // Use the string from the pre-built list.
                searchURL = searchString;
            }

            // Reset the proxy to default.  The host is `""` and the port is `"0"`.
            OrbotProxyHelper.setProxy(getApplicationContext(), this, "", "0");

            // Set the default app bar layout background.
            if (darkTheme) {
                appBarLayout.setBackgroundResource(R.color.gray_900);
            } else {
                appBarLayout.setBackgroundResource(R.color.gray_100);
            }

            // Reset `waitingForOrbot.
            waitingForOrbot = false;

            // Reload the WebViews if requested.
            if (reloadWebsite) {
                // Reload the WebViews.
                for (int i = 0; i < webViewPagerAdapter.getCount(); i++) {
                    // Get the WebView tab fragment.
                    WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(i);

                    // Get the fragment view.
                    View fragmentView = webViewTabFragment.getView();

                    // Only reload the WebViews if they exist.
                    if (fragmentView != null) {
                        // Get the nested scroll WebView from the tab fragment.
                        NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

                        // Reload the WebView.
                        nestedScrollWebView.reload();
                    }
                }
            }
        }
    }

    private void updatePrivacyIcons(boolean runInvalidateOptionsMenu) {
        // Only update the privacy icons if the options menu and the current WebView have already been populated.
        if ((optionsMenu != null) && (currentWebView != null)) {
            // Get a handle for the shared preferences.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            // Get the theme and screenshot preferences.
            boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

            // Get handles for the menu items.
            MenuItem privacyMenuItem = optionsMenu.findItem(R.id.toggle_javascript);
            MenuItem firstPartyCookiesMenuItem = optionsMenu.findItem(R.id.toggle_first_party_cookies);
            MenuItem domStorageMenuItem = optionsMenu.findItem(R.id.toggle_dom_storage);
            MenuItem refreshMenuItem = optionsMenu.findItem(R.id.refresh);

            // Update the privacy icon.
            if (currentWebView.getSettings().getJavaScriptEnabled()) {  // JavaScript is enabled.
                privacyMenuItem.setIcon(R.drawable.javascript_enabled);
            } else if (currentWebView.getAcceptFirstPartyCookies()) {  // JavaScript is disabled but cookies are enabled.
                privacyMenuItem.setIcon(R.drawable.warning);
            } else {  // All the dangerous features are disabled.
                privacyMenuItem.setIcon(R.drawable.privacy_mode);
            }

            // Update the first-party cookies icon.
            if (currentWebView.getAcceptFirstPartyCookies()) {  // First-party cookies are enabled.
                firstPartyCookiesMenuItem.setIcon(R.drawable.cookies_enabled);
            } else {  // First-party cookies are disabled.
                if (darkTheme) {
                    firstPartyCookiesMenuItem.setIcon(R.drawable.cookies_disabled_dark);
                } else {
                    firstPartyCookiesMenuItem.setIcon(R.drawable.cookies_disabled_light);
                }
            }

            // Update the DOM storage icon.
            if (currentWebView.getSettings().getJavaScriptEnabled() && currentWebView.getSettings().getDomStorageEnabled()) {  // Both JavaScript and DOM storage are enabled.
                domStorageMenuItem.setIcon(R.drawable.dom_storage_enabled);
            } else if (currentWebView.getSettings().getJavaScriptEnabled()) {  // JavaScript is enabled but DOM storage is disabled.
                if (darkTheme) {
                    domStorageMenuItem.setIcon(R.drawable.dom_storage_disabled_dark);
                } else {
                    domStorageMenuItem.setIcon(R.drawable.dom_storage_disabled_light);
                }
            } else {  // JavaScript is disabled, so DOM storage is ghosted.
                if (darkTheme) {
                    domStorageMenuItem.setIcon(R.drawable.dom_storage_ghosted_dark);
                } else {
                    domStorageMenuItem.setIcon(R.drawable.dom_storage_ghosted_light);
                }
            }

            // Update the refresh icon.
            if (darkTheme) {
                refreshMenuItem.setIcon(R.drawable.refresh_enabled_dark);
            } else {
                refreshMenuItem.setIcon(R.drawable.refresh_enabled_light);
            }

            // `invalidateOptionsMenu` calls `onPrepareOptionsMenu()` and redraws the icons in the `AppBar`.
            if (runInvalidateOptionsMenu) {
                invalidateOptionsMenu();
            }
        }
    }

    private void openUrlWithExternalApp(String url) {
        // Create a download intent.  Not specifying the action type will display the maximum number of options.
        Intent downloadIntent = new Intent();

        // Set the URI and the MIME type.  Specifying `text/html` displays a good number of options.
        downloadIntent.setDataAndType(Uri.parse(url), "text/html");

        // Flag the intent to open in a new task.
        downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Show the chooser.
        startActivity(Intent.createChooser(downloadIntent, getString(R.string.open_with)));
    }

    private void highlightUrlText() {
        // Get a handle for the URL edit text.
        EditText urlEditText = findViewById(R.id.url_edittext);

        // Only highlight the URL text if the box is not currently selected.
        if (!urlEditText.hasFocus()) {
            // Get the URL string.
            String urlString = urlEditText.getText().toString();

            // Highlight the URL according to the protocol.
            if (urlString.startsWith("file://")) {  // This is a file URL.
                // De-emphasize only the protocol.
                urlEditText.getText().setSpan(initialGrayColorSpan, 0, 7, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else if (urlString.startsWith("content://")) {
                // De-emphasize only the protocol.
                urlEditText.getText().setSpan(initialGrayColorSpan, 0, 10, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {  // This is a web URL.
                // Get the index of the `/` immediately after the domain name.
                int endOfDomainName = urlString.indexOf("/", (urlString.indexOf("//") + 2));

                // Create a base URL string.
                String baseUrl;

                // Get the base URL.
                if (endOfDomainName > 0) {  // There is at least one character after the base URL.
                    // Get the base URL.
                    baseUrl = urlString.substring(0, endOfDomainName);
                } else {  // There are no characters after the base URL.
                    // Set the base URL to be the entire URL string.
                    baseUrl = urlString;
                }

                // Get the index of the last `.` in the domain.
                int lastDotIndex = baseUrl.lastIndexOf(".");

                // Get the index of the penultimate `.` in the domain.
                int penultimateDotIndex = baseUrl.lastIndexOf(".", lastDotIndex - 1);

                // Markup the beginning of the URL.
                if (urlString.startsWith("http://")) {  // Highlight the protocol of connections that are not encrypted.
                    urlEditText.getText().setSpan(redColorSpan, 0, 7, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                    // De-emphasize subdomains.
                    if (penultimateDotIndex > 0) {  // There is more than one subdomain in the domain name.
                        urlEditText.getText().setSpan(initialGrayColorSpan, 7, penultimateDotIndex + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                } else if (urlString.startsWith("https://")) {  // De-emphasize the protocol of connections that are encrypted.
                    if (penultimateDotIndex > 0) {  // There is more than one subdomain in the domain name.
                        // De-emphasize the protocol and the additional subdomains.
                        urlEditText.getText().setSpan(initialGrayColorSpan, 0, penultimateDotIndex + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    } else {  // There is only one subdomain in the domain name.
                        // De-emphasize only the protocol.
                        urlEditText.getText().setSpan(initialGrayColorSpan, 0, 8, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                }

                // De-emphasize the text after the domain name.
                if (endOfDomainName > 0) {
                    urlEditText.getText().setSpan(finalGrayColorSpan, endOfDomainName, urlString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                }
            }
        }
    }

    private void loadBookmarksFolder() {
        // Update the bookmarks cursor with the contents of the bookmarks database for the current folder.
        bookmarksCursor = bookmarksDatabaseHelper.getBookmarksByDisplayOrder(currentBookmarksFolder);

        // Populate the bookmarks cursor adapter.  `this` specifies the `Context`.  `false` disables `autoRequery`.
        bookmarksCursorAdapter = new CursorAdapter(this, bookmarksCursor, false) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the individual item layout.  `false` does not attach it to the root.
                return getLayoutInflater().inflate(R.layout.bookmarks_drawer_item_linearlayout, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Get handles for the views.
                ImageView bookmarkFavoriteIcon = view.findViewById(R.id.bookmark_favorite_icon);
                TextView bookmarkNameTextView = view.findViewById(R.id.bookmark_name);

                // Get the favorite icon byte array from the cursor.
                byte[] favoriteIconByteArray = cursor.getBlob(cursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON));

                // Convert the byte array to a `Bitmap` beginning at the first byte and ending at the last.
                Bitmap favoriteIconBitmap = BitmapFactory.decodeByteArray(favoriteIconByteArray, 0, favoriteIconByteArray.length);

                // Display the bitmap in `bookmarkFavoriteIcon`.
                bookmarkFavoriteIcon.setImageBitmap(favoriteIconBitmap);

                // Get the bookmark name from the cursor and display it in `bookmarkNameTextView`.
                String bookmarkNameString = cursor.getString(cursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME));
                bookmarkNameTextView.setText(bookmarkNameString);

                // Make the font bold for folders.
                if (cursor.getInt(cursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)) == 1) {
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {  // Reset the font to default for normal bookmarks.
                    bookmarkNameTextView.setTypeface(Typeface.DEFAULT);
                }
            }
        };

        // Get a handle for the bookmarks list view.
        ListView bookmarksListView = findViewById(R.id.bookmarks_drawer_listview);

        // Populate the list view with the adapter.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);

        // Get a handle for the bookmarks title text view.
        TextView bookmarksTitleTextView = findViewById(R.id.bookmarks_title_textview);

        // Set the bookmarks drawer title.
        if (currentBookmarksFolder.isEmpty()) {
            bookmarksTitleTextView.setText(R.string.bookmarks);
        } else {
            bookmarksTitleTextView.setText(currentBookmarksFolder);
        }
    }

    private void openWithApp(String url) {
        // Create the open with intent with `ACTION_VIEW`.
        Intent openWithAppIntent = new Intent(Intent.ACTION_VIEW);

        // Set the URI but not the MIME type.  This should open all available apps.
        openWithAppIntent.setData(Uri.parse(url));

        // Flag the intent to open in a new task.
        openWithAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Show the chooser.
        startActivity(openWithAppIntent);
    }

    private void openWithBrowser(String url) {
        // Create the open with intent with `ACTION_VIEW`.
        Intent openWithBrowserIntent = new Intent(Intent.ACTION_VIEW);

        // Set the URI and the MIME type.  `"text/html"` should load browser options.
        openWithBrowserIntent.setDataAndType(Uri.parse(url), "text/html");

        // Flag the intent to open in a new task.
        openWithBrowserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Show the chooser.
        startActivity(openWithBrowserIntent);
    }

    private String sanitizeUrl(String url) {
        // Sanitize Google Analytics.
        if (sanitizeGoogleAnalytics) {
            // Remove `?utm_`.
            if (url.contains("?utm_")) {
                url = url.substring(0, url.indexOf("?utm_"));
            }

            // Remove `&utm_`.
            if (url.contains("&utm_")) {
                url = url.substring(0, url.indexOf("&utm_"));
            }
        }

        // Sanitize Facebook Click IDs.
        if (sanitizeFacebookClickIds) {
            // Remove `?fbclid=`.
            if (url.contains("?fbclid=")) {
                url = url.substring(0, url.indexOf("?fbclid="));
            }

            // Remove &fbclid=`.
            if (url.contains("&fbclid=")) {
                url = url.substring(0, url.indexOf("&fbclid="));
            }
        }

        // Return the sanitized URL.
        return url;
    }

    public void addTab(View view) {
        // Add a new tab with a blank URL.
        addNewTab("");
    }

    private void addNewTab(String url) {
        // Sanitize the URL.
        url = sanitizeUrl(url);

        // Get a handle for the tab layout and the view pager.
        TabLayout tabLayout = findViewById(R.id.tablayout);
        ViewPager webViewPager = findViewById(R.id.webviewpager);

        // Get the new page number.  The page numbers are 0 indexed, so the new page number will match the current count.
        int newTabNumber = tabLayout.getTabCount();

        // Add a new tab.
        tabLayout.addTab(tabLayout.newTab());

        // Get the new tab.
        TabLayout.Tab newTab = tabLayout.getTabAt(newTabNumber);

        // Remove the lint warning below that the current tab might be null.
        assert newTab != null;

        // Set a custom view on the new tab.
        newTab.setCustomView(R.layout.tab_custom_view);

        // Add the new WebView page.
        webViewPagerAdapter.addPage(newTabNumber, webViewPager, url);
    }

    public void closeTab(View view) {
        // Get a handle for the tab layout.
        TabLayout tabLayout = findViewById(R.id.tablayout);

        // Run the command according to the number of tabs.
        if (tabLayout.getTabCount() > 1) {  // There is more than one tab open.
            // Close the current tab.
            closeCurrentTab();
        } else {  // There is only one tab open.
            clearAndExit();
        }
    }

    private void closeCurrentTab() {
        // Get handles for the views.
        AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);
        TabLayout tabLayout = findViewById(R.id.tablayout);
        ViewPager webViewPager = findViewById(R.id.webviewpager);

        // Get the current tab number.
        int currentTabNumber = tabLayout.getSelectedTabPosition();

        // Delete the current tab.
        tabLayout.removeTabAt(currentTabNumber);

        // Delete the current page.  If the selected page number did not change during the delete, it will return true, meaning that the current WebView must be reset.
        if (webViewPagerAdapter.deletePage(currentTabNumber, webViewPager)) {
            setCurrentWebView(currentTabNumber);
        }

        // Expand the app bar if it is currently collapsed.
        appBarLayout.setExpanded(true);
    }

    private void clearAndExit() {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Close the bookmarks cursor and database.
        bookmarksCursor.close();
        bookmarksDatabaseHelper.close();

        // Get the status of the clear everything preference.
        boolean clearEverything = sharedPreferences.getBoolean("clear_everything", true);

        // Get a handle for the runtime.
        Runtime runtime = Runtime.getRuntime();

        // Get the application's private data directory, which will be something like `/data/user/0/com.stoutner.privacybrowser.standard`,
        // which links to `/data/data/com.stoutner.privacybrowser.standard`.
        String privateDataDirectoryString = getApplicationInfo().dataDir;

        // Clear cookies.
        if (clearEverything || sharedPreferences.getBoolean("clear_cookies", true)) {
            // The command to remove cookies changed slightly in API 21.
            if (Build.VERSION.SDK_INT >= 21) {
                CookieManager.getInstance().removeAllCookies(null);
            } else {
                CookieManager.getInstance().removeAllCookie();
            }

            // Manually delete the cookies database, as `CookieManager` sometimes will not flush its changes to disk before `System.exit(0)` is run.
            try {
                // Two commands must be used because `Runtime.exec()` does not like `*`.
                Process deleteCookiesProcess = runtime.exec("rm -f " + privateDataDirectoryString + "/app_webview/Cookies");
                Process deleteCookiesJournalProcess = runtime.exec("rm -f " + privateDataDirectoryString + "/app_webview/Cookies-journal");

                // Wait until the processes have finished.
                deleteCookiesProcess.waitFor();
                deleteCookiesJournalProcess.waitFor();
            } catch (Exception exception) {
                // Do nothing if an error is thrown.
            }
        }

        // Clear DOM storage.
        if (clearEverything || sharedPreferences.getBoolean("clear_dom_storage", true)) {
            // Ask `WebStorage` to clear the DOM storage.
            WebStorage webStorage = WebStorage.getInstance();
            webStorage.deleteAllData();

            // Manually delete the DOM storage files and directories, as `WebStorage` sometimes will not flush its changes to disk before `System.exit(0)` is run.
            try {
                // A `String[]` must be used because the directory contains a space and `Runtime.exec` will otherwise not escape the string correctly.
                Process deleteLocalStorageProcess = runtime.exec(new String[] {"rm", "-rf", privateDataDirectoryString + "/app_webview/Local Storage/"});

                // Multiple commands must be used because `Runtime.exec()` does not like `*`.
                Process deleteIndexProcess = runtime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/IndexedDB");
                Process deleteQuotaManagerProcess = runtime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager");
                Process deleteQuotaManagerJournalProcess = runtime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager-journal");
                Process deleteDatabaseProcess = runtime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/databases");

                // Wait until the processes have finished.
                deleteLocalStorageProcess.waitFor();
                deleteIndexProcess.waitFor();
                deleteQuotaManagerProcess.waitFor();
                deleteQuotaManagerJournalProcess.waitFor();
                deleteDatabaseProcess.waitFor();
            } catch (Exception exception) {
                // Do nothing if an error is thrown.
            }
        }

        // Clear form data if the API < 26.
        if ((Build.VERSION.SDK_INT < 26) && (clearEverything || sharedPreferences.getBoolean("clear_form_data", true))) {
            WebViewDatabase webViewDatabase = WebViewDatabase.getInstance(this);
            webViewDatabase.clearFormData();

            // Manually delete the form data database, as `WebViewDatabase` sometimes will not flush its changes to disk before `System.exit(0)` is run.
            try {
                // A string array must be used because the database contains a space and `Runtime.exec` will not otherwise escape the string correctly.
                Process deleteWebDataProcess = runtime.exec(new String[] {"rm", "-f", privateDataDirectoryString + "/app_webview/Web Data"});
                Process deleteWebDataJournalProcess = runtime.exec(new String[] {"rm", "-f", privateDataDirectoryString + "/app_webview/Web Data-journal"});

                // Wait until the processes have finished.
                deleteWebDataProcess.waitFor();
                deleteWebDataJournalProcess.waitFor();
            } catch (Exception exception) {
                // Do nothing if an error is thrown.
            }
        }

        // Clear the cache.
        if (clearEverything || sharedPreferences.getBoolean("clear_cache", true)) {
            // Clear the cache from each WebView.
            for (int i = 0; i < webViewPagerAdapter.getCount(); i++) {
                // Get the WebView tab fragment.
                WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(i);

                // Get the fragment view.
                View fragmentView = webViewTabFragment.getView();

                // Only clear the cache if the WebView exists.
                if (fragmentView != null) {
                    // Get the nested scroll WebView from the tab fragment.
                    NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

                    // Clear the cache for this WebView.
                    nestedScrollWebView.clearCache(true);
                }
            }

            // Manually delete the cache directories.
            try {
                // Delete the main cache directory.
                Process deleteCacheProcess = runtime.exec("rm -rf " + privateDataDirectoryString + "/cache");

                // Delete the secondary `Service Worker` cache directory.
                // A string array must be used because the directory contains a space and `Runtime.exec` will otherwise not escape the string correctly.
                Process deleteServiceWorkerProcess = runtime.exec(new String[] {"rm", "-rf", privateDataDirectoryString + "/app_webview/Service Worker/"});

                // Wait until the processes have finished.
                deleteCacheProcess.waitFor();
                deleteServiceWorkerProcess.waitFor();
            } catch (Exception exception) {
                // Do nothing if an error is thrown.
            }
        }

        // Wipe out each WebView.
        for (int i = 0; i < webViewPagerAdapter.getCount(); i++) {
            // Get the WebView tab fragment.
            WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(i);

            // Get the fragment view.
            View fragmentView = webViewTabFragment.getView();

            // Only wipe out the WebView if it exists.
            if (fragmentView != null) {
                // Get the nested scroll WebView from the tab fragment.
                NestedScrollWebView nestedScrollWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

                // Clear SSL certificate preferences for this WebView.
                nestedScrollWebView.clearSslPreferences();

                // Clear the back/forward history for this WebView.
                nestedScrollWebView.clearHistory();

                // Destroy the internal state of `mainWebView`.
                nestedScrollWebView.destroy();
            }
        }

        // Clear the custom headers.
        customHeaders.clear();

        // Manually delete the `app_webview` folder, which contains the cookies, DOM storage, form data, and `Service Worker` cache.
        // See `https://code.google.com/p/android/issues/detail?id=233826&thanks=233826&ts=1486670530`.
        if (clearEverything) {
            try {
                // Delete the folder.
                Process deleteAppWebviewProcess = runtime.exec("rm -rf " + privateDataDirectoryString + "/app_webview");

                // Wait until the process has finished.
                deleteAppWebviewProcess.waitFor();
            } catch (Exception exception) {
                // Do nothing if an error is thrown.
            }
        }

        // Close Privacy Browser.  `finishAndRemoveTask` also removes Privacy Browser from the recent app list.
        if (Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }

        // Remove the terminated program from RAM.  The status code is `0`.
        System.exit(0);
    }

    private void setCurrentWebView(int pageNumber) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the theme preference.
        boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

        // Get handles for the URL views.
        RelativeLayout urlRelativeLayout = findViewById(R.id.url_relativelayout);
        EditText urlEditText = findViewById(R.id.url_edittext);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

        //Stop the swipe to refresh indicator if it is running
        swipeRefreshLayout.setRefreshing(false);

        // Get the WebView tab fragment.
        WebViewTabFragment webViewTabFragment = webViewPagerAdapter.getPageFragment(pageNumber);

        // Get the fragment view.
        View fragmentView = webViewTabFragment.getView();

        // Set the current WebView if the fragment view is not null.
        if (fragmentView != null) {
            // Store the current WebView.
            currentWebView = fragmentView.findViewById(R.id.nestedscroll_webview);

            // Update the status of swipe to refresh.
            if (currentWebView.getSwipeToRefresh()) {  // Swipe to refresh is enabled.
                if (Build.VERSION.SDK_INT >= 23) {  // For API >= 23, swipe refresh layout is continuously updated with an on scroll change listener and only enabled if the WebView is scrolled to the top.
                    // Enable the swipe refresh layout if the WebView is scrolled all the way to the top.
                    swipeRefreshLayout.setEnabled(currentWebView.getY() == 0);
                } else {
                    // Enable the swipe refresh layout.
                    swipeRefreshLayout.setEnabled(true);
                }
            } else {  // Swipe to refresh is disabled.
                // Disable the swipe refresh layout.
                swipeRefreshLayout.setEnabled(false);
            }

            // Get a handle for the cookie manager.
            CookieManager cookieManager = CookieManager.getInstance();

            // Set the first-party cookie status.
            cookieManager.setAcceptCookie(currentWebView.getAcceptFirstPartyCookies());

            // Update the privacy icons.  `true` redraws the icons in the app bar.
            updatePrivacyIcons(true);

            // Get a handle for the input method manager.
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            // Remove the lint warning below that the input method manager might be null.
            assert inputMethodManager != null;

            // Get the current URL.
            String url = currentWebView.getUrl();

            // Update the URL edit text if not loading a new intent.  Otherwise, this will be handled by `onPageStarted()` (if called) and `onPageFinished()`.
            if (!loadingNewIntent) {  // A new intent is not being loaded.
                if ((url == null) || url.equals("about:blank")) {  // The WebView is blank.
                    // Display the hint in the URL edit text.
                    urlEditText.setText("");

                    // Request focus for the URL text box.
                    urlEditText.requestFocus();

                    // Display the keyboard.
                    inputMethodManager.showSoftInput(urlEditText, 0);
                } else {  // The WebView has a loaded URL.
                    // Clear the focus from the URL text box.
                    urlEditText.clearFocus();

                    // Hide the soft keyboard.
                    inputMethodManager.hideSoftInputFromWindow(currentWebView.getWindowToken(), 0);

                    // Display the current URL in the URL text box.
                    urlEditText.setText(url);

                    // Highlight the URL text.
                    highlightUrlText();
                }
            } else {  // A new intent is being loaded.
                // Reset the loading new intent tracker.
                loadingNewIntent = false;
            }

            // Set the background to indicate the domain settings status.
            if (currentWebView.getDomainSettingsApplied()) {
                // Set a green background on the URL relative layout to indicate that custom domain settings are being used. The deprecated `.getDrawable()` must be used until the minimum API >= 21.
                if (darkTheme) {
                    urlRelativeLayout.setBackground(getResources().getDrawable(R.drawable.url_bar_background_dark_blue));
                } else {
                    urlRelativeLayout.setBackground(getResources().getDrawable(R.drawable.url_bar_background_light_green));
                }
            } else {
                urlRelativeLayout.setBackground(getResources().getDrawable(R.color.transparent));
            }
        }
    }

    @Override
    public void initializeWebView(NestedScrollWebView nestedScrollWebView, int pageNumber, ProgressBar progressBar, String url) {
        // Get handles for the activity views.
        FrameLayout rootFrameLayout = findViewById(R.id.root_framelayout);
        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout);
        RelativeLayout mainContentRelativeLayout = findViewById(R.id.main_content_relativelayout);
        ActionBar actionBar = getSupportActionBar();
        LinearLayout tabsLinearLayout = findViewById(R.id.tabs_linearlayout);
        EditText urlEditText = findViewById(R.id.url_edittext);
        TabLayout tabLayout = findViewById(R.id.tablayout);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);

        // Remove the incorrect lint warning below that the action bar might be null.
        assert actionBar != null;

        // Get a handle for the activity
        Activity activity = this;

        // Get a handle for the input method manager.
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Instantiate the blocklist helper.
        BlockListHelper blockListHelper = new BlockListHelper();

        // Remove the lint warning below that the input method manager might be null.
        assert inputMethodManager != null;

        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the relevant preferences.
        boolean downloadWithExternalApp = sharedPreferences.getBoolean("download_with_external_app", false);

        // Initialize the favorite icon.
        nestedScrollWebView.initializeFavoriteIcon();

        // Set the app bar scrolling.
        nestedScrollWebView.setNestedScrollingEnabled(sharedPreferences.getBoolean("scroll_app_bar", true));

        // Allow pinch to zoom.
        nestedScrollWebView.getSettings().setBuiltInZoomControls(true);

        // Hide zoom controls.
        nestedScrollWebView.getSettings().setDisplayZoomControls(false);

        // Don't allow mixed content (HTTP and HTTPS) on the same website.
        if (Build.VERSION.SDK_INT >= 21) {
            nestedScrollWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        // Set the WebView to use a wide viewport.  Otherwise, some web pages will be scrunched and some content will render outside the screen.
        nestedScrollWebView.getSettings().setUseWideViewPort(true);

        // Set the WebView to load in overview mode (zoomed out to the maximum width).
        nestedScrollWebView.getSettings().setLoadWithOverviewMode(true);

        // Explicitly disable geolocation.
        nestedScrollWebView.getSettings().setGeolocationEnabled(false);

        // Create a double-tap gesture detector to toggle full-screen mode.
        GestureDetector doubleTapGestureDetector = new GestureDetector(getApplicationContext(), new GestureDetector.SimpleOnGestureListener() {
            // Override `onDoubleTap()`.  All other events are handled using the default settings.
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (fullScreenBrowsingModeEnabled) {  // Only process the double-tap if full screen browsing mode is enabled.
                    // Toggle the full screen browsing mode tracker.
                    inFullScreenBrowsingMode = !inFullScreenBrowsingMode;

                    // Toggle the full screen browsing mode.
                    if (inFullScreenBrowsingMode) {  // Switch to full screen mode.
                        // Store the swipe refresh layout top padding.
                        swipeRefreshLayoutPaddingTop = swipeRefreshLayout.getPaddingTop();

                        // Hide the app bar if specified.
                        if (hideAppBar) {
                            // Close the find on page bar if it is visible.
                            closeFindOnPage(null);

                            // Hide the tab linear layout.
                            tabsLinearLayout.setVisibility(View.GONE);

                            // Hide the action bar.
                            actionBar.hide();

                            // Check to see if app bar scrolling is disabled.
                            if (!scrollAppBar) {
                                // Remove the padding from the top of the swipe refresh layout.
                                swipeRefreshLayout.setPadding(0, 0, 0, 0);
                            }
                        }

                        // Hide the banner ad in the free flavor.
                        if (BuildConfig.FLAVOR.contentEquals("free")) {
                            AdHelper.hideAd(findViewById(R.id.adview));
                        }

                        // Remove the translucent status flag.  This is necessary so the root frame layout can fill the entire screen.
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        /* Hide the system bars.
                         * SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                         * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN makes the root frame layout fill the area that is normally reserved for the status bar.
                         * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                         * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
                         */
                        rootFrameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    } else {  // Switch to normal viewing mode.
                        // Show the tab linear layout.
                        tabsLinearLayout.setVisibility(View.VISIBLE);

                        // Show the action bar.
                        actionBar.show();

                        // Check to see if app bar scrolling is disabled.
                        if (!scrollAppBar) {
                            // Add the padding from the top of the swipe refresh layout.
                            swipeRefreshLayout.setPadding(0, swipeRefreshLayoutPaddingTop, 0, 0);
                        }

                        // Show the banner ad in the free flavor.
                        if (BuildConfig.FLAVOR.contentEquals("free")) {
                            // Reload the ad.
                            AdHelper.loadAd(findViewById(R.id.adview), getApplicationContext(), getString(R.string.ad_unit_id));
                        }

                        // Remove the `SYSTEM_UI` flags from the root frame layout.
                        rootFrameLayout.setSystemUiVisibility(0);

                        // Add the translucent status flag.
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }

                    // Consume the double-tap.
                    return true;
                } else { // Do not consume the double-tap because full screen browsing mode is disabled.
                    return false;
                }
            }
        });

        // Pass all touch events on the WebView through the double-tap gesture detector.
        nestedScrollWebView.setOnTouchListener((View view, MotionEvent event) -> {
            // Call `performClick()` on the view, which is required for accessibility.
            view.performClick();

            // Send the event to the gesture detector.
            return doubleTapGestureDetector.onTouchEvent(event);
        });

        // Register the WebView for a context menu.  This is used to see link targets and download images.
        registerForContextMenu(nestedScrollWebView);

        // Allow the downloading of files.
        nestedScrollWebView.setDownloadListener((String downloadUrl, String userAgent, String contentDisposition, String mimetype, long contentLength) -> {
            // Check if the download should be processed by an external app.
            if (downloadWithExternalApp) {  // Download with an external app.
                // Create a download intent.  Not specifying the action type will display the maximum number of options.
                Intent downloadIntent = new Intent();

                // Set the URI and the MIME type.  Specifying `text/html` displays a good number of options.
                downloadIntent.setDataAndType(Uri.parse(downloadUrl), "text/html");

                // Flag the intent to open in a new task.
                downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Show the chooser.
                startActivity(Intent.createChooser(downloadIntent, getString(R.string.open_with)));
            } else {  // Download with Android's download manager.
                // Check to see if the WRITE_EXTERNAL_STORAGE permission has already been granted.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {  // The storage permission has not been granted.
                    // The WRITE_EXTERNAL_STORAGE permission needs to be requested.

                    // Store the variables for future use by `onRequestPermissionsResult()`.
                    this.downloadUrl = downloadUrl;
                    downloadContentDisposition = contentDisposition;
                    downloadContentLength = contentLength;

                    // Show a dialog if the user has previously denied the permission.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                        // Instantiate the download location permission alert dialog and set the download type to DOWNLOAD_FILE.
                        DialogFragment downloadLocationPermissionDialogFragment = DownloadLocationPermissionDialog.downloadType(DownloadLocationPermissionDialog.DOWNLOAD_FILE);

                        // Show the download location permission alert dialog.  The permission will be requested when the the dialog is closed.
                        downloadLocationPermissionDialogFragment.show(getSupportFragmentManager(), getString(R.string.download_location));
                    } else {  // Show the permission request directly.
                        // Request the permission.  The download dialog will be launched by `onRequestPermissionResult()`.
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_FILE_REQUEST_CODE);
                    }
                } else {  // The storage permission has already been granted.
                    // Get a handle for the download file alert dialog.
                    DialogFragment downloadFileDialogFragment = DownloadFileDialog.fromUrl(downloadUrl, contentDisposition, contentLength);

                    // Show the download file alert dialog.
                    downloadFileDialogFragment.show(getSupportFragmentManager(), getString(R.string.download));
                }
            }
        });

        // Update the find on page count.
        nestedScrollWebView.setFindListener(new WebView.FindListener() {
            // Get a handle for `findOnPageCountTextView`.
            final TextView findOnPageCountTextView = findViewById(R.id.find_on_page_count_textview);

            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                if ((isDoneCounting) && (numberOfMatches == 0)) {  // There are no matches.
                    // Set `findOnPageCountTextView` to `0/0`.
                    findOnPageCountTextView.setText(R.string.zero_of_zero);
                } else if (isDoneCounting) {  // There are matches.
                    // `activeMatchOrdinal` is zero-based.
                    int activeMatch = activeMatchOrdinal + 1;

                    // Build the match string.
                    String matchString = activeMatch + "/" + numberOfMatches;

                    // Set `findOnPageCountTextView`.
                    findOnPageCountTextView.setText(matchString);
                }
            }
        });

        // Update the status of swipe to refresh based on the scroll position of the nested scroll WebView.
        // Once the minimum API >= 23 this can be replaced with `nestedScrollWebView.setOnScrollChangeListener()`.
        nestedScrollWebView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (nestedScrollWebView.getSwipeToRefresh()) {
                // Only enable swipe to refresh if the WebView is scrolled to the top.
                swipeRefreshLayout.setEnabled(nestedScrollWebView.getScrollY() == 0);
            }
        });

        // Set the web chrome client.
        nestedScrollWebView.setWebChromeClient(new WebChromeClient() {
            // Update the progress bar when a page is loading.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Inject the night mode CSS if night mode is enabled.
                if (nestedScrollWebView.getNightMode()) {  // Night mode is enabled.
                    // `background-color: #212121` sets the background to be dark gray.  `color: #BDBDBD` sets the text color to be light gray.  `box-shadow: none` removes a lower underline on links
                    // used by WordPress.  `text-decoration: none` removes all text underlines.  `text-shadow: none` removes text shadows, which usually have a hard coded color.
                    // `border: none` removes all borders, which can also be used to underline text.  `a {color: #1565C0}` sets links to be a dark blue.
                    // `::selection {background: #0D47A1}' sets the text selection highlight color to be a dark blue. `!important` takes precedent over any existing sub-settings.
                    nestedScrollWebView.evaluateJavascript("(function() {var parent = document.getElementsByTagName('head').item(0); var style = document.createElement('style'); style.type = 'text/css'; " +
                            "style.innerHTML = '* {background-color: #212121 !important; color: #BDBDBD !important; box-shadow: none !important; text-decoration: none !important;" +
                            "text-shadow: none !important; border: none !important;} a {color: #1565C0 !important;} ::selection {background: #0D47A1 !important;}'; parent.appendChild(style)})()", value -> {
                        // Initialize a handler to display `mainWebView`.
                        Handler displayWebViewHandler = new Handler();

                        // Setup a runnable to display `mainWebView` after a delay to allow the CSS to be applied.
                        Runnable displayWebViewRunnable = () -> {
                            // Only display `mainWebView` if the progress bar is gone.  This prevents the display of the `WebView` while it is still loading.
                            if (progressBar.getVisibility() == View.GONE) {
                                nestedScrollWebView.setVisibility(View.VISIBLE);
                            }
                        };

                        // Display the WebView after 500 milliseconds.
                        displayWebViewHandler.postDelayed(displayWebViewRunnable, 500);
                    });
                } else {  // Night mode is disabled.
                    // Display the nested scroll WebView if night mode is disabled.
                    // Because of a race condition between `applyDomainSettings` and `onPageStarted`,
                    // when night mode is set by domain settings the WebView may be hidden even if night mode is not currently enabled.
                    nestedScrollWebView.setVisibility(View.VISIBLE);
                }

                // Update the progress bar.
                progressBar.setProgress(progress);

                // Set the visibility of the progress bar.
                if (progress < 100) {
                    // Show the progress bar.
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    // Hide the progress bar.
                    progressBar.setVisibility(View.GONE);

                    //Stop the swipe to refresh indicator if it is running
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            // Set the favorite icon when it changes.
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // Only update the favorite icon if the website has finished loading.
                if (progressBar.getVisibility() == View.GONE) {
                    // Store the new favorite icon.
                    nestedScrollWebView.setFavoriteOrDefaultIcon(icon);

                    // Get the current page position.
                    int currentPosition = webViewPagerAdapter.getPositionForId(nestedScrollWebView.getWebViewFragmentId());

                    // Get the current tab.
                    TabLayout.Tab tab = tabLayout.getTabAt(currentPosition);

                    // Check to see if the tab has been populated.
                    if (tab != null) {
                        // Get the custom view from the tab.
                        View tabView = tab.getCustomView();

                        // Check to see if the custom tab view has been populated.
                        if (tabView != null) {
                            // Get the favorite icon image view from the tab.
                            ImageView tabFavoriteIconImageView = tabView.findViewById(R.id.favorite_icon_imageview);

                            // Display the favorite icon in the tab.
                            tabFavoriteIconImageView.setImageBitmap(Bitmap.createScaledBitmap(icon, 64, 64, true));
                        }
                    }
                }
            }

            // Save a copy of the title when it changes.
            @Override
            public void onReceivedTitle(WebView view, String title) {
                // Get the current page position.
                int currentPosition = webViewPagerAdapter.getPositionForId(nestedScrollWebView.getWebViewFragmentId());

                // Get the current tab.
                TabLayout.Tab tab = tabLayout.getTabAt(currentPosition);

                // Only populate the title text view if the tab has been fully created.
                if (tab != null) {
                    // Get the custom view from the tab.
                    View tabView = tab.getCustomView();

                    // Remove the incorrect warning below that the current tab view might be null.
                    assert tabView != null;

                    // Get the title text view from the tab.
                    TextView tabTitleTextView = tabView.findViewById(R.id.title_textview);

                    // Set the title as the tab text.
                    tabTitleTextView.setText(title);
                }
            }

            // Enter full screen video.
            @Override
            public void onShowCustomView(View video, CustomViewCallback callback) {
                // Get a handle for the full screen video frame layout.
                FrameLayout fullScreenVideoFrameLayout = findViewById(R.id.full_screen_video_framelayout);

                // Set the full screen video flag.
                displayingFullScreenVideo = true;

                // Pause the ad if this is the free flavor.
                if (BuildConfig.FLAVOR.contentEquals("free")) {
                    // The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
                    AdHelper.pauseAd(findViewById(R.id.adview));
                }

                // Hide the keyboard.
                inputMethodManager.hideSoftInputFromWindow(nestedScrollWebView.getWindowToken(), 0);

                // Hide the main content relative layout.
                mainContentRelativeLayout.setVisibility(View.GONE);

                // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                // Remove the translucent status flag.  This is necessary so the root frame layout can fill the entire screen.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                /* Hide the system bars.
                 * SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                 * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN makes the root frame layout fill the area that is normally reserved for the status bar.
                 * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                 * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
                 */
                rootFrameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                // Disable the sliding drawers.
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                // Add the video view to the full screen video frame layout.
                fullScreenVideoFrameLayout.addView(video);

                // Show the full screen video frame layout.
                fullScreenVideoFrameLayout.setVisibility(View.VISIBLE);
            }

            // Exit full screen video.
            @Override
            public void onHideCustomView() {
                // Get a handle for the full screen video frame layout.
                FrameLayout fullScreenVideoFrameLayout = findViewById(R.id.full_screen_video_framelayout);

                // Unset the full screen video flag.
                displayingFullScreenVideo = false;

                // Remove all the views from the full screen video frame layout.
                fullScreenVideoFrameLayout.removeAllViews();

                // Hide the full screen video frame layout.
                fullScreenVideoFrameLayout.setVisibility(View.GONE);

                // Enable the sliding drawers.
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                // Show the main content relative layout.
                mainContentRelativeLayout.setVisibility(View.VISIBLE);

                // Apply the appropriate full screen mode flags.
                if (fullScreenBrowsingModeEnabled && inFullScreenBrowsingMode) {  // Privacy Browser is currently in full screen browsing mode.
                    // Hide the app bar if specified.
                    if (hideAppBar) {
                        // Hide the tab linear layout.
                        tabsLinearLayout.setVisibility(View.GONE);

                        // Hide the action bar.
                        actionBar.hide();
                    }

                    // Hide the banner ad in the free flavor.
                    if (BuildConfig.FLAVOR.contentEquals("free")) {
                        AdHelper.hideAd(findViewById(R.id.adview));
                    }

                    // Remove the translucent status flag.  This is necessary so the root frame layout can fill the entire screen.
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                    /* Hide the system bars.
                     * SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                     * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN makes the root frame layout fill the area that is normally reserved for the status bar.
                     * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                     * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
                     */
                    rootFrameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                } else {  // Switch to normal viewing mode.
                    // Remove the `SYSTEM_UI` flags from the root frame layout.
                    rootFrameLayout.setSystemUiVisibility(0);

                    // Add the translucent status flag.
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }

                // Reload the ad for the free flavor if not in full screen mode.
                if (BuildConfig.FLAVOR.contentEquals("free") && !inFullScreenBrowsingMode) {
                    // Reload the ad.
                    AdHelper.loadAd(findViewById(R.id.adview), getApplicationContext(), getString(R.string.ad_unit_id));
                }
            }

            // Upload files.
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                // Show the file chooser if the device is running API >= 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    // Store the file path callback.
                    fileChooserCallback = filePathCallback;

                    // Create an intent to open a chooser based ont the file chooser parameters.
                    Intent fileChooserIntent = fileChooserParams.createIntent();

                    // Open the file chooser.  Currently only one `startActivityForResult` exists in this activity, so the request code, used to differentiate them, is simply `0`.
                    startActivityForResult(fileChooserIntent, 0);
                }
                return true;
            }
        });

        nestedScrollWebView.setWebViewClient(new WebViewClient() {
            // `shouldOverrideUrlLoading` makes this `WebView` the default handler for URLs inside the app, so that links are not kicked out to other apps.
            // The deprecated `shouldOverrideUrlLoading` must be used until API >= 24.
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Sanitize the url.
                url = sanitizeUrl(url);

                if (url.startsWith("http")) {  // Load the URL in Privacy Browser.
                    // Apply the domain settings for the new URL.  This doesn't do anything if the domain has not changed.
                    boolean userAgentChanged = applyDomainSettings(nestedScrollWebView, url, true, false);

                    // Check if the user agent has changed.
                    if (userAgentChanged) {
                        // Manually load the URL.  The changing of the user agent will cause WebView to reload the previous URL.
                        nestedScrollWebView.loadUrl(url, customHeaders);

                        // Returning true indicates that Privacy Browser is manually handling the loading of the URL.
                        return true;
                    } else {
                        // Returning false causes the current WebView to handle the URL and prevents it from adding redirects to the history list.
                        return false;
                    }
                } else if (url.startsWith("mailto:")) {  // Load the email address in an external email program.
                    // Use `ACTION_SENDTO` instead of `ACTION_SEND` so that only email programs are launched.
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                    // Parse the url and set it as the data for the intent.
                    emailIntent.setData(Uri.parse(url));

                    // Open the email program in a new task instead of as part of Privacy Browser.
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Make it so.
                    startActivity(emailIntent);

                    // Returning true indicates Privacy Browser is handling the URL by creating an intent.
                    return true;
                } else if (url.startsWith("tel:")) {  // Load the phone number in the dialer.
                    // Open the dialer and load the phone number, but wait for the user to place the call.
                    Intent dialIntent = new Intent(Intent.ACTION_DIAL);

                    // Add the phone number to the intent.
                    dialIntent.setData(Uri.parse(url));

                    // Open the dialer in a new task instead of as part of Privacy Browser.
                    dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Make it so.
                    startActivity(dialIntent);

                    // Returning true indicates Privacy Browser is handling the URL by creating an intent.
                    return true;
                } else {  // Load a system chooser to select an app that can handle the URL.
                    // Open an app that can handle the URL.
                    Intent genericIntent = new Intent(Intent.ACTION_VIEW);

                    // Add the URL to the intent.
                    genericIntent.setData(Uri.parse(url));

                    // List all apps that can handle the URL instead of just opening the first one.
                    genericIntent.addCategory(Intent.CATEGORY_BROWSABLE);

                    // Open the app in a new task instead of as part of Privacy Browser.
                    genericIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Start the app or display a snackbar if no app is available to handle the URL.
                    try {
                        startActivity(genericIntent);
                    } catch (ActivityNotFoundException exception) {
                        Snackbar.make(nestedScrollWebView, getString(R.string.unrecognized_url) + "  " + url, Snackbar.LENGTH_SHORT).show();
                    }

                    // Returning true indicates Privacy Browser is handling the URL by creating an intent.
                    return true;
                }
            }

            // Check requests against the block lists.  The deprecated `shouldInterceptRequest()` must be used until minimum API >= 21.
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                // Get a handle for the navigation view.
                NavigationView navigationView = findViewById(R.id.navigationview);

                // Get a handle for the navigation menu.
                Menu navigationMenu = navigationView.getMenu();

                // Get a handle for the navigation requests menu item.  The menu is 0 based.
                MenuItem navigationRequestsMenuItem = navigationMenu.getItem(5);

                // Create an empty web resource response to be used if the resource request is blocked.
                WebResourceResponse emptyWebResourceResponse = new WebResourceResponse("text/plain", "utf8", new ByteArrayInputStream("".getBytes()));

                // Reset the whitelist results tracker.
                String[] whitelistResultStringArray = null;

                // Initialize the third party request tracker.
                boolean isThirdPartyRequest = false;

                // Get the current URL.  `.getUrl()` throws an error because operations on the WebView cannot be made from this thread.
                String currentBaseDomain = nestedScrollWebView.getCurrentDomainName();

                // Store a copy of the current domain for use in later requests.
                String currentDomain = currentBaseDomain;

                // Nobody is happy when comparing null strings.
                if ((currentBaseDomain != null) && (url != null)) {
                    // Convert the request URL to a URI.
                    Uri requestUri = Uri.parse(url);

                    // Get the request host name.
                    String requestBaseDomain = requestUri.getHost();

                    // Only check for third-party requests if the current base domain is not empty and the request domain is not null.
                    if (!currentBaseDomain.isEmpty() && (requestBaseDomain != null)) {
                        // Determine the current base domain.
                        while (currentBaseDomain.indexOf(".", currentBaseDomain.indexOf(".") + 1) > 0) {  // There is at least one subdomain.
                            // Remove the first subdomain.
                            currentBaseDomain = currentBaseDomain.substring(currentBaseDomain.indexOf(".") + 1);
                        }

                        // Determine the request base domain.
                        while (requestBaseDomain.indexOf(".", requestBaseDomain.indexOf(".") + 1) > 0) {  // There is at least one subdomain.
                            // Remove the first subdomain.
                            requestBaseDomain = requestBaseDomain.substring(requestBaseDomain.indexOf(".") + 1);
                        }

                        // Update the third party request tracker.
                        isThirdPartyRequest = !currentBaseDomain.equals(requestBaseDomain);
                    }
                }

                // Get the current WebView page position.
                int webViewPagePosition = webViewPagerAdapter.getPositionForId(nestedScrollWebView.getWebViewFragmentId());

                // Determine if the WebView is currently displayed.
                boolean webViewDisplayed = (webViewPagePosition == tabLayout.getSelectedTabPosition());

                // Block third-party requests if enabled.
                if (isThirdPartyRequest && nestedScrollWebView.isBlocklistEnabled(NestedScrollWebView.THIRD_PARTY_REQUESTS)) {
                    // Add the result to the resource requests.
                    nestedScrollWebView.addResourceRequest(new String[]{BlockListHelper.REQUEST_THIRD_PARTY, url});

                    // Increment the blocked requests counters.
                    nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS);
                    nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.THIRD_PARTY_REQUESTS);

                    // Update the titles of the blocklist menu items if the WebView is currently displayed.
                    if (webViewDisplayed) {
                        // Updating the UI must be run from the UI thread.
                        activity.runOnUiThread(() -> {
                            // Update the menu item titles.
                            navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));

                            // Update the options menu if it has been populated.
                            if (optionsMenu != null) {
                                optionsMenu.findItem(R.id.blocklists).setTitle(getString(R.string.blocklists) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));
                                optionsMenu.findItem(R.id.block_all_third_party_requests).setTitle(nestedScrollWebView.getRequestsCount(NestedScrollWebView.THIRD_PARTY_REQUESTS) + " - " +
                                        getString(R.string.block_all_third_party_requests));
                            }
                        });
                    }

                    // Return an empty web resource response.
                    return emptyWebResourceResponse;
                }

                // Check UltraPrivacy if it is enabled.
                if (nestedScrollWebView.isBlocklistEnabled(NestedScrollWebView.ULTRA_PRIVACY)) {
                    // Check the URL against UltraPrivacy.
                    String[] ultraPrivacyResults = blockListHelper.checkBlocklist(currentDomain, url, isThirdPartyRequest, ultraPrivacy);

                    // Process the UltraPrivacy results.
                    if (ultraPrivacyResults[0].equals(BlockListHelper.REQUEST_BLOCKED)) {  // The resource request matched UltraPrivacy's blacklist.
                        // Add the result to the resource requests.
                        nestedScrollWebView.addResourceRequest(new String[] {ultraPrivacyResults[0], ultraPrivacyResults[1], ultraPrivacyResults[2], ultraPrivacyResults[3], ultraPrivacyResults[4],
                                ultraPrivacyResults[5]});

                        // Increment the blocked requests counters.
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS);
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.ULTRA_PRIVACY);

                        // Update the titles of the blocklist menu items if the WebView is currently displayed.
                        if (webViewDisplayed) {
                            // Updating the UI must be run from the UI thread.
                            activity.runOnUiThread(() -> {
                                // Update the menu item titles.
                                navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));

                                // Update the options menu if it has been populated.
                                if (optionsMenu != null) {
                                    optionsMenu.findItem(R.id.blocklists).setTitle(getString(R.string.blocklists) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));
                                    optionsMenu.findItem(R.id.ultraprivacy).setTitle(nestedScrollWebView.getRequestsCount(NestedScrollWebView.ULTRA_PRIVACY) + " - " + getString(R.string.ultraprivacy));
                                }
                            });
                        }

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    } else if (ultraPrivacyResults[0].equals(BlockListHelper.REQUEST_ALLOWED)) {  // The resource request matched UltraPrivacy's whitelist.
                        // Add a whitelist entry to the resource requests array.
                        nestedScrollWebView.addResourceRequest(new String[] {ultraPrivacyResults[0], ultraPrivacyResults[1], ultraPrivacyResults[2], ultraPrivacyResults[3], ultraPrivacyResults[4],
                                ultraPrivacyResults[5]});

                        // The resource request has been allowed by UltraPrivacy.  `return null` loads the requested resource.
                        return null;
                    }
                }

                // Check EasyList if it is enabled.
                if (nestedScrollWebView.isBlocklistEnabled(NestedScrollWebView.EASY_LIST)) {
                    // Check the URL against EasyList.
                    String[] easyListResults = blockListHelper.checkBlocklist(currentDomain, url, isThirdPartyRequest, easyList);

                    // Process the EasyList results.
                    if (easyListResults[0].equals(BlockListHelper.REQUEST_BLOCKED)) {  // The resource request matched EasyList's blacklist.
                        // Add the result to the resource requests.
                        nestedScrollWebView.addResourceRequest(new String[] {easyListResults[0], easyListResults[1], easyListResults[2], easyListResults[3], easyListResults[4], easyListResults[5]});

                        // Increment the blocked requests counters.
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS);
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.EASY_LIST);

                        // Update the titles of the blocklist menu items if the WebView is currently displayed.
                        if (webViewDisplayed) {
                            // Updating the UI must be run from the UI thread.
                            activity.runOnUiThread(() -> {
                                // Update the menu item titles.
                                navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));

                                // Update the options menu if it has been populated.
                                if (optionsMenu != null) {
                                    optionsMenu.findItem(R.id.blocklists).setTitle(getString(R.string.blocklists) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));
                                    optionsMenu.findItem(R.id.easylist).setTitle(nestedScrollWebView.getRequestsCount(NestedScrollWebView.EASY_LIST) + " - " + getString(R.string.easylist));
                                }
                            });
                        }

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    } else if (easyListResults[0].equals(BlockListHelper.REQUEST_ALLOWED)) {  // The resource request matched EasyList's whitelist.
                        // Update the whitelist result string array tracker.
                        whitelistResultStringArray = new String[] {easyListResults[0], easyListResults[1], easyListResults[2], easyListResults[3], easyListResults[4], easyListResults[5]};
                    }
                }

                // Check EasyPrivacy if it is enabled.
                if (nestedScrollWebView.isBlocklistEnabled(NestedScrollWebView.EASY_PRIVACY)) {
                    // Check the URL against EasyPrivacy.
                    String[] easyPrivacyResults = blockListHelper.checkBlocklist(currentDomain, url, isThirdPartyRequest, easyPrivacy);

                    // Process the EasyPrivacy results.
                    if (easyPrivacyResults[0].equals(BlockListHelper.REQUEST_BLOCKED)) {  // The resource request matched EasyPrivacy's blacklist.
                        // Add the result to the resource requests.
                        nestedScrollWebView.addResourceRequest(new String[] {easyPrivacyResults[0], easyPrivacyResults[1], easyPrivacyResults[2], easyPrivacyResults[3], easyPrivacyResults[4],
                                easyPrivacyResults[5]});

                        // Increment the blocked requests counters.
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS);
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.EASY_PRIVACY);

                        // Update the titles of the blocklist menu items if the WebView is currently displayed.
                        if (webViewDisplayed) {
                            // Updating the UI must be run from the UI thread.
                            activity.runOnUiThread(() -> {
                                // Update the menu item titles.
                                navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));

                                // Update the options menu if it has been populated.
                                if (optionsMenu != null) {
                                    optionsMenu.findItem(R.id.blocklists).setTitle(getString(R.string.blocklists) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));
                                    optionsMenu.findItem(R.id.easyprivacy).setTitle(nestedScrollWebView.getRequestsCount(NestedScrollWebView.EASY_PRIVACY) + " - " + getString(R.string.easyprivacy));
                                }
                            });
                        }

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    } else if (easyPrivacyResults[0].equals(BlockListHelper.REQUEST_ALLOWED)) {  // The resource request matched EasyPrivacy's whitelist.
                        // Update the whitelist result string array tracker.
                        whitelistResultStringArray = new String[] {easyPrivacyResults[0], easyPrivacyResults[1], easyPrivacyResults[2], easyPrivacyResults[3], easyPrivacyResults[4], easyPrivacyResults[5]};
                    }
                }

                // Check Fanboy’s Annoyance List if it is enabled.
                if (nestedScrollWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST)) {
                    // Check the URL against Fanboy's Annoyance List.
                    String[] fanboysAnnoyanceListResults = blockListHelper.checkBlocklist(currentDomain, url, isThirdPartyRequest, fanboysAnnoyanceList);

                    // Process the Fanboy's Annoyance List results.
                    if (fanboysAnnoyanceListResults[0].equals(BlockListHelper.REQUEST_BLOCKED)) {  // The resource request matched Fanboy's Annoyance List's blacklist.
                        // Add the result to the resource requests.
                        nestedScrollWebView.addResourceRequest(new String[] {fanboysAnnoyanceListResults[0], fanboysAnnoyanceListResults[1], fanboysAnnoyanceListResults[2], fanboysAnnoyanceListResults[3],
                                fanboysAnnoyanceListResults[4], fanboysAnnoyanceListResults[5]});

                        // Increment the blocked requests counters.
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS);
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST);

                        // Update the titles of the blocklist menu items if the WebView is currently displayed.
                        if (webViewDisplayed) {
                            // Updating the UI must be run from the UI thread.
                            activity.runOnUiThread(() -> {
                                // Update the menu item titles.
                                navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));

                                // Update the options menu if it has been populated.
                                if (optionsMenu != null) {
                                    optionsMenu.findItem(R.id.blocklists).setTitle(getString(R.string.blocklists) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));
                                    optionsMenu.findItem(R.id.fanboys_annoyance_list).setTitle(nestedScrollWebView.getRequestsCount(NestedScrollWebView.FANBOYS_ANNOYANCE_LIST) + " - " +
                                            getString(R.string.fanboys_annoyance_list));
                                }
                            });
                        }

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    } else if (fanboysAnnoyanceListResults[0].equals(BlockListHelper.REQUEST_ALLOWED)){  // The resource request matched Fanboy's Annoyance List's whitelist.
                        // Update the whitelist result string array tracker.
                        whitelistResultStringArray = new String[] {fanboysAnnoyanceListResults[0], fanboysAnnoyanceListResults[1], fanboysAnnoyanceListResults[2], fanboysAnnoyanceListResults[3],
                                fanboysAnnoyanceListResults[4], fanboysAnnoyanceListResults[5]};
                    }
                } else if (nestedScrollWebView.isBlocklistEnabled(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST)) {  // Only check Fanboy’s Social Blocking List if Fanboy’s Annoyance List is disabled.
                    // Check the URL against Fanboy's Annoyance List.
                    String[] fanboysSocialListResults = blockListHelper.checkBlocklist(currentDomain, url, isThirdPartyRequest, fanboysSocialList);

                    // Process the Fanboy's Social Blocking List results.
                    if (fanboysSocialListResults[0].equals(BlockListHelper.REQUEST_BLOCKED)) {  // The resource request matched Fanboy's Social Blocking List's blacklist.
                        // Add the result to the resource requests.
                        nestedScrollWebView.addResourceRequest(new String[] {fanboysSocialListResults[0], fanboysSocialListResults[1], fanboysSocialListResults[2], fanboysSocialListResults[3],
                                fanboysSocialListResults[4], fanboysSocialListResults[5]});

                        // Increment the blocked requests counters.
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS);
                        nestedScrollWebView.incrementRequestsCount(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST);

                        // Update the titles of the blocklist menu items if the WebView is currently displayed.
                        if (webViewDisplayed) {
                            // Updating the UI must be run from the UI thread.
                            activity.runOnUiThread(() -> {
                                // Update the menu item titles.
                                navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));

                                // Update the options menu if it has been populated.
                                if (optionsMenu != null) {
                                    optionsMenu.findItem(R.id.blocklists).setTitle(getString(R.string.blocklists) + " - " + nestedScrollWebView.getRequestsCount(NestedScrollWebView.BLOCKED_REQUESTS));
                                    optionsMenu.findItem(R.id.fanboys_social_blocking_list).setTitle(nestedScrollWebView.getRequestsCount(NestedScrollWebView.FANBOYS_SOCIAL_BLOCKING_LIST) + " - " +
                                            getString(R.string.fanboys_social_blocking_list));
                                }
                            });
                        }

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    } else if (fanboysSocialListResults[0].equals(BlockListHelper.REQUEST_ALLOWED)) {  // The resource request matched Fanboy's Social Blocking List's whitelist.
                        // Update the whitelist result string array tracker.
                        whitelistResultStringArray = new String[] {fanboysSocialListResults[0], fanboysSocialListResults[1], fanboysSocialListResults[2], fanboysSocialListResults[3],
                                fanboysSocialListResults[4], fanboysSocialListResults[5]};
                    }
                }

                // Add the request to the log because it hasn't been processed by any of the previous checks.
                if (whitelistResultStringArray != null) {  // The request was processed by a whitelist.
                    nestedScrollWebView.addResourceRequest(whitelistResultStringArray);
                } else {  // The request didn't match any blocklist entry.  Log it as a default request.
                    nestedScrollWebView.addResourceRequest(new String[]{BlockListHelper.REQUEST_DEFAULT, url});
                }

                // The resource request has not been blocked.  `return null` loads the requested resource.
                return null;
            }

            // Handle HTTP authentication requests.
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                // Store the handler.
                nestedScrollWebView.setHttpAuthHandler(handler);

                // Instantiate an HTTP authentication dialog.
                DialogFragment httpAuthenticationDialogFragment = HttpAuthenticationDialog.displayDialog(host, realm, nestedScrollWebView.getWebViewFragmentId());

                // Show the HTTP authentication dialog.
                httpAuthenticationDialogFragment.show(getSupportFragmentManager(), getString(R.string.http_authentication));
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Get the preferences.
                boolean scrollAppBar = sharedPreferences.getBoolean("scroll_app_bar", true);
                boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

                // Get a handler for the app bar layout.
                AppBarLayout appBarLayout = findViewById(R.id.appbar_layout);

                // Set the top padding of the swipe refresh layout according to the app bar scrolling preference.
                if (scrollAppBar) {
                    // No padding is needed because it will automatically be placed below the app bar layout due to the scrolling layout behavior.
                    swipeRefreshLayout.setPadding(0, 0, 0, 0);

                    // The swipe to refresh circle doesn't always hide itself completely unless it is moved up 10 pixels.
                    swipeRefreshLayout.setProgressViewOffset(false, defaultProgressViewStartOffset - 10, defaultProgressViewEndOffset);
                } else {
                    // Get the app bar layout height.  This can't be done in `applyAppSettings()` because the app bar is not yet populated.
                    int appBarHeight = appBarLayout.getHeight();

                    // The swipe refresh layout must be manually moved below the app bar layout.
                    swipeRefreshLayout.setPadding(0, appBarHeight, 0, 0);

                    // The swipe to refresh circle doesn't always hide itself completely unless it is moved up 10 pixels.
                    swipeRefreshLayout.setProgressViewOffset(false, defaultProgressViewStartOffset - 10 + appBarHeight, defaultProgressViewEndOffset + appBarHeight);
                }

                // Reset the list of resource requests.
                nestedScrollWebView.clearResourceRequests();

                // Reset the requests counters.
                nestedScrollWebView.resetRequestsCounters();

                // If night mode is enabled, hide `mainWebView` until after the night mode CSS is applied.
                if (nestedScrollWebView.getNightMode()) {
                    nestedScrollWebView.setVisibility(View.INVISIBLE);
                } else {
                    nestedScrollWebView.setVisibility(View.VISIBLE);
                }

                // Hide the keyboard.
                inputMethodManager.hideSoftInputFromWindow(nestedScrollWebView.getWindowToken(), 0);

                // Check to see if Privacy Browser is waiting on Orbot.
                if (!waitingForOrbot) {  // Process the URL.
                    // Get the current page position.
                    int currentPagePosition = webViewPagerAdapter.getPositionForId(nestedScrollWebView.getWebViewFragmentId());

                    // Update the URL text bar if the page is currently selected.
                    if (tabLayout.getSelectedTabPosition() == currentPagePosition) {
                        // Clear the focus from the URL edit text.
                        urlEditText.clearFocus();

                        // Display the formatted URL text.
                        urlEditText.setText(url);

                        // Apply text highlighting to `urlTextBox`.
                        highlightUrlText();
                    }

                    // Reset the list of host IP addresses.
                    nestedScrollWebView.clearCurrentIpAddresses();

                    // Get a URI for the current URL.
                    Uri currentUri = Uri.parse(url);

                    // Get the IP addresses for the host.
                    new GetHostIpAddresses(activity, getSupportFragmentManager(), nestedScrollWebView).execute(currentUri.getHost());

                    // Apply any custom domain settings if the URL was loaded by navigating history.
                    if (nestedScrollWebView.getNavigatingHistory()) {
                        // Reset navigating history.
                        nestedScrollWebView.setNavigatingHistory(false);

                        // Apply the domain settings.
                        boolean userAgentChanged = applyDomainSettings(nestedScrollWebView, url, true, false);

                        // Manually load the URL if the user agent has changed, which will have caused the previous URL to be reloaded.
                        if (userAgentChanged) {
                            loadUrl(url);
                        }
                    }

                    // Replace Refresh with Stop if the options menu has been created.  (The WebView typically begins loading before the menu items are instantiated.)
                    if (optionsMenu != null) {
                        // Get a handle for the refresh menu item.
                        MenuItem refreshMenuItem = optionsMenu.findItem(R.id.refresh);

                        // Set the title.
                        refreshMenuItem.setTitle(R.string.stop);

                        // Get the app bar and theme preferences.
                        boolean displayAdditionalAppBarIcons = sharedPreferences.getBoolean("display_additional_app_bar_icons", false);

                        // If the icon is displayed in the AppBar, set it according to the theme.
                        if (displayAdditionalAppBarIcons) {
                            if (darkTheme) {
                                refreshMenuItem.setIcon(R.drawable.close_dark);
                            } else {
                                refreshMenuItem.setIcon(R.drawable.close_light);
                            }
                        }
                    }
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Reset the wide view port if it has been turned off by the waiting for Orbot message.
                if (!waitingForOrbot) {
                    // Only use a wide view port if the URL starts with `http`, not for `file://` and `content://`.
                    nestedScrollWebView.getSettings().setUseWideViewPort(url.startsWith("http"));
                }

                // Flush any cookies to persistent storage.  The cookie manager has become very lazy about flushing cookies in recent versions.
                if (nestedScrollWebView.getAcceptFirstPartyCookies() && Build.VERSION.SDK_INT >= 21) {
                    CookieManager.getInstance().flush();
                }

                // Update the Refresh menu item if the options menu has been created.
                if (optionsMenu != null) {
                    // Get a handle for the refresh menu item.
                    MenuItem refreshMenuItem = optionsMenu.findItem(R.id.refresh);

                    // Reset the Refresh title.
                    refreshMenuItem.setTitle(R.string.refresh);

                    // Get the app bar and theme preferences.
                    boolean displayAdditionalAppBarIcons = sharedPreferences.getBoolean("display_additional_app_bar_icons", false);
                    boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

                    // If the icon is displayed in the AppBar, reset it according to the theme.
                    if (displayAdditionalAppBarIcons) {
                        if (darkTheme) {
                            refreshMenuItem.setIcon(R.drawable.refresh_enabled_dark);
                        } else {
                            refreshMenuItem.setIcon(R.drawable.refresh_enabled_light);
                        }
                    }
                }

                // Clear the cache and history if Incognito Mode is enabled.
                if (incognitoModeEnabled) {
                    // Clear the cache.  `true` includes disk files.
                    nestedScrollWebView.clearCache(true);

                    // Clear the back/forward history.
                    nestedScrollWebView.clearHistory();

                    // Manually delete cache folders.
                    try {
                        // Get the application's private data directory, which will be something like `/data/user/0/com.stoutner.privacybrowser.standard`,
                        // which links to `/data/data/com.stoutner.privacybrowser.standard`.
                        String privateDataDirectoryString = getApplicationInfo().dataDir;

                        // Delete the main cache directory.
                        Runtime.getRuntime().exec("rm -rf " + privateDataDirectoryString + "/cache");

                        // Delete the secondary `Service Worker` cache directory.
                        // A `String[]` must be used because the directory contains a space and `Runtime.exec` will not escape the string correctly otherwise.
                        Runtime.getRuntime().exec(new String[]{"rm", "-rf", privateDataDirectoryString + "/app_webview/Service Worker/"});
                    } catch (IOException e) {
                        // Do nothing if an error is thrown.
                    }
                }

                // Update the URL text box and apply domain settings if not waiting on Orbot.
                if (!waitingForOrbot) {
                    // Get the current page position.
                    int currentPagePosition = webViewPagerAdapter.getPositionForId(nestedScrollWebView.getWebViewFragmentId());

                    // Check the current website information against any pinned domain information if the current IP addresses have been loaded.
                    if ((nestedScrollWebView.hasPinnedSslCertificate() || nestedScrollWebView.hasPinnedIpAddresses()) && nestedScrollWebView.hasCurrentIpAddresses() &&
                            !nestedScrollWebView.ignorePinnedDomainInformation()) {
                        CheckPinnedMismatchHelper.checkPinnedMismatch(getSupportFragmentManager(), nestedScrollWebView);
                    }

                    // Get the current URL from the nested scroll WebView.  This is more accurate than using the URL passed into the method, which is sometimes not the final one.
                    String currentUrl = nestedScrollWebView.getUrl();

                    // Update the URL text bar if the page is currently selected and the user is not currently typing in the URL edit text.
                    // Crash records show that, in some crazy way, it is possible for the current URL to be blank at this point.
                    // Probably some sort of race condition when Privacy Browser is being resumed.
                    if ((tabLayout.getSelectedTabPosition() == currentPagePosition) && !urlEditText.hasFocus() && (currentUrl != null)) {
                        // Check to see if the URL is `about:blank`.
                        if (currentUrl.equals("about:blank")) {  // The WebView is blank.
                            // Display the hint in the URL edit text.
                            urlEditText.setText("");

                            // Request focus for the URL text box.
                            urlEditText.requestFocus();

                            // Display the keyboard.
                            inputMethodManager.showSoftInput(urlEditText, 0);

                            // Hide the WebView, which causes the default background color to be displayed according to the theme.
                            nestedScrollWebView.setVisibility(View.INVISIBLE);

                            // Apply the domain settings.  This clears any settings from the previous domain.
                            applyDomainSettings(nestedScrollWebView, "", true, false);
                        } else {  // The WebView has loaded a webpage.
                            // Display the final URL.  Getting the URL from the WebView instead of using the one provided by `onPageFinished()` makes websites like YouTube function correctly.
                            urlEditText.setText(currentUrl);

                            // Apply text highlighting to the URL.
                            highlightUrlText();
                        }
                    }

                    // Get the current tab.
                    TabLayout.Tab tab = tabLayout.getTabAt(currentPagePosition);

                    // Only populate the title text view if the tab has been fully created.
                    if (tab != null) {
                        // Get the custom view from the tab.
                        View tabView = tab.getCustomView();

                        // Remove the incorrect warning below that the current tab view might be null.
                        assert tabView != null;

                        // Get the title text view from the tab.
                        TextView tabTitleTextView = tabView.findViewById(R.id.title_textview);

                        // Set the title as the tab text.  Sometimes `onReceivedTitle()` is not called, especially when navigating history.
                        tabTitleTextView.setText(nestedScrollWebView.getTitle());
                    }
                }
            }

            // Handle SSL Certificate errors.
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Get the current website SSL certificate.
                SslCertificate currentWebsiteSslCertificate = error.getCertificate();

                // Extract the individual pieces of information from the current website SSL certificate.
                String currentWebsiteIssuedToCName = currentWebsiteSslCertificate.getIssuedTo().getCName();
                String currentWebsiteIssuedToOName = currentWebsiteSslCertificate.getIssuedTo().getOName();
                String currentWebsiteIssuedToUName = currentWebsiteSslCertificate.getIssuedTo().getUName();
                String currentWebsiteIssuedByCName = currentWebsiteSslCertificate.getIssuedBy().getCName();
                String currentWebsiteIssuedByOName = currentWebsiteSslCertificate.getIssuedBy().getOName();
                String currentWebsiteIssuedByUName = currentWebsiteSslCertificate.getIssuedBy().getUName();
                Date currentWebsiteSslStartDate = currentWebsiteSslCertificate.getValidNotBeforeDate();
                Date currentWebsiteSslEndDate = currentWebsiteSslCertificate.getValidNotAfterDate();

                // Proceed to the website if the current SSL website certificate matches the pinned domain certificate.
                if (nestedScrollWebView.hasPinnedSslCertificate()) {
                    // Get the pinned SSL certificate.
                    ArrayList<Object> pinnedSslCertificateArrayList = nestedScrollWebView.getPinnedSslCertificate();

                    // Extract the arrays from the array list.
                    String[] pinnedSslCertificateStringArray = (String[]) pinnedSslCertificateArrayList.get(0);
                    Date[] pinnedSslCertificateDateArray = (Date[]) pinnedSslCertificateArrayList.get(1);

                    // Check if the current SSL certificate matches the pinned certificate.
                    if (currentWebsiteIssuedToCName.equals(pinnedSslCertificateStringArray[0]) && currentWebsiteIssuedToOName.equals(pinnedSslCertificateStringArray[1]) &&
                        currentWebsiteIssuedToUName.equals(pinnedSslCertificateStringArray[2]) && currentWebsiteIssuedByCName.equals(pinnedSslCertificateStringArray[3]) &&
                        currentWebsiteIssuedByOName.equals(pinnedSslCertificateStringArray[4]) && currentWebsiteIssuedByUName.equals(pinnedSslCertificateStringArray[5]) &&
                        currentWebsiteSslStartDate.equals(pinnedSslCertificateDateArray[0]) && currentWebsiteSslEndDate.equals(pinnedSslCertificateDateArray[1])) {

                        // An SSL certificate is pinned and matches the current domain certificate.  Proceed to the website without displaying an error.
                        handler.proceed();
                    }
                } else {  // Either there isn't a pinned SSL certificate or it doesn't match the current website certificate.
                    // Store the SSL error handler.
                    nestedScrollWebView.setSslErrorHandler(handler);

                    // Instantiate an SSL certificate error alert dialog.
                    DialogFragment sslCertificateErrorDialogFragment = SslCertificateErrorDialog.displayDialog(error, nestedScrollWebView.getWebViewFragmentId());

                    // Show the SSL certificate error dialog.
                    sslCertificateErrorDialogFragment.show(getSupportFragmentManager(), getString(R.string.ssl_certificate_error));
                }
            }
        });

        // Check to see if this is the first page.
        if (pageNumber == 0) {
            // Set this nested scroll WebView as the current WebView.
            currentWebView = nestedScrollWebView;

            // Apply the app settings from the shared preferences.
            applyAppSettings();

            // Load the website if not waiting for Orbot to connect.
            if (!waitingForOrbot) {
                // Get the intent that started the app.
                Intent launchingIntent = getIntent();

                // Get the information from the intent.
                String launchingIntentAction = launchingIntent.getAction();
                Uri launchingIntentUriData = launchingIntent.getData();

                // If the intent action is a web search, perform the search.
                if ((launchingIntentAction != null) && launchingIntentAction.equals(Intent.ACTION_WEB_SEARCH)) {
                    // Create an encoded URL string.
                    String encodedUrlString;

                    // Sanitize the search input and convert it to a search.
                    try {
                        encodedUrlString = URLEncoder.encode(launchingIntent.getStringExtra(SearchManager.QUERY), "UTF-8");
                    } catch (UnsupportedEncodingException exception) {
                        encodedUrlString = "";
                    }

                    // Load the completed search URL.
                    loadUrl(searchURL + encodedUrlString);
                } else if (launchingIntentUriData != null){  // Check to see if the intent contains a new URL.
                    // Load the URL from the intent.
                    loadUrl(launchingIntentUriData.toString());
                } else {  // The is no URL in the intent.
                    // Select the homepage based on the proxy through Orbot status.
                    if (proxyThroughOrbot) {
                        // Load the Tor homepage.
                        loadUrl(sharedPreferences.getString("tor_homepage", getString(R.string.tor_homepage_default_value)));
                    } else {
                        // Load the normal homepage.
                        loadUrl(sharedPreferences.getString("homepage", getString(R.string.homepage_default_value)));
                    }
                }
            }
        } else {  // This is not the first tab.
            // Apply the domain settings.
            applyDomainSettings(nestedScrollWebView, url, false, false);

            // Load the URL.
            nestedScrollWebView.loadUrl(url, customHeaders);

            // Display the keyboard if the URL is blank.
            if (url.equals("")) {
                inputMethodManager.showSoftInput(urlEditText, 0);
            }
        }
    }
}