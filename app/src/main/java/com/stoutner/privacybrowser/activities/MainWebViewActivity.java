/*
 * Copyright 2015-2017 Soren Stoutner <soren@stoutner.com>.
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

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stoutner.privacybrowser.BannerAd;
import com.stoutner.privacybrowser.BuildConfig;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.CreateHomeScreenShortcutDialog;
import com.stoutner.privacybrowser.dialogs.DownloadImageDialog;
import com.stoutner.privacybrowser.dialogs.UrlHistoryDialog;
import com.stoutner.privacybrowser.dialogs.ViewSslCertificateDialog;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;
import com.stoutner.privacybrowser.helpers.OrbotProxyHelper;
import com.stoutner.privacybrowser.dialogs.DownloadFileDialog;
import com.stoutner.privacybrowser.dialogs.SslCertificateErrorDialog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// We need to use AppCompatActivity from android.support.v7.app.AppCompatActivity to have access to the SupportActionBar until the minimum API is >= 21.
public class MainWebViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CreateHomeScreenShortcutDialog.CreateHomeScreenSchortcutListener,
        SslCertificateErrorDialog.SslCertificateErrorListener, DownloadFileDialog.DownloadFileListener, DownloadImageDialog.DownloadImageListener, UrlHistoryDialog.UrlHistoryListener {

    // `appBar` is public static so it can be accessed from `OrbotProxyHelper`.  It is also used in `onCreate()`, `onOptionsItemSelected()`, `closeFindOnPage()`, and `applyAppSettings()`.
    public static ActionBar appBar;

    // `favoriteIconBitmap` is public static so it can be accessed from `CreateHomeScreenShortcutDialog`, `BookmarksActivity`, `CreateBookmarkDialog`, `CreateBookmarkFolderDialog`, `EditBookmarkDialog`, `EditBookmarkFolderDialog`, `ViewSslCertificateDialog`.
    // It is also used in `onCreate()`, `onCreateHomeScreenShortcutCreate()`, and `applyDomainSettings`.
    public static Bitmap favoriteIconBitmap;

    // `formattedUrlString` is public static so it can be accessed from `BookmarksActivity`, `CreateBookmarkDialog`, and `AddDomainDialog`.
    // It is also used in `onCreate()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onCreateHomeScreenShortcutCreate()`, and `loadUrlFromTextBox()`.
    public static String formattedUrlString;

    // `sslCertificate` is public static so it can be accessed from `ViewSslCertificateDialog`.  It is also used in `onCreate()`.
    public static SslCertificate sslCertificate;

    // `orbotStatus` is public static so it can be accessed from `OrbotProxyHelper`.  It is also used in `onCreate()`.
    public static String orbotStatus;

    // `webViewTitle` is public static so it can be accessed from `CreateBookmarkDialog` and `CreateHomeScreenShortcutDialog`.  It is also used in `onCreate()`.
    public static String webViewTitle;


    // `navigatingHistory` is used in `onCreate()`, `onNavigationItemSelected()`, and `applyDomainSettings()`.
    private boolean navigatingHistory;

    // `favoriteIconDefaultBitmap` is used in `onCreate()` and `applyDomainSettings`.
    private Bitmap favoriteIconDefaultBitmap;

    // `drawerLayout` is used in `onCreate()`, `onNewIntent()`, and `onBackPressed()`.
    private DrawerLayout drawerLayout;

    // `rootCoordinatorLayout` is used in `onCreate()` and `applyAppSettings()`.
    private CoordinatorLayout rootCoordinatorLayout;

    // 'mainWebView' is used in `onCreate()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onRestart()`, `onCreateContextMenu()`, `findPreviousOnPage()`, `findNextOnPage()`, `closeFindOnPage()`, and `loadUrlFromTextBox()`.
    private WebView mainWebView;

    // `fullScreenVideoFrameLayout` is used in `onCreate()` and `onConfigurationChanged()`.
    private FrameLayout fullScreenVideoFrameLayout;

    // `swipeRefreshLayout` is used in `onCreate()`, `onPrepareOptionsMenu`, and `onRestart()`.
    private SwipeRefreshLayout swipeRefreshLayout;

    // `urlAppBarRelativeLayout` is used in `onCreate()` and `applyDomainSettings()`.
    private RelativeLayout urlAppBarRelativeLayout;

    // `favoriteIconImageView` is used in `onCreate()` and `applyDomainSettings()`
    private ImageView favoriteIconImageView;

    // `cookieManager` is used in `onCreate()`, `onOptionsItemSelected()`, and `onNavigationItemSelected()`, `loadUrlFromTextBox()`, `onDownloadImage()`, `onDownloadFile()`, and `onRestart()`.
    private CookieManager cookieManager;

    // `customHeader` is used in `onCreate()`, `onOptionsItemSelected()`, `onCreateContextMenu()`, and `loadUrl()`.
    private final Map<String, String> customHeaders = new HashMap<>();

    // `javaScriptEnabled` is also used in `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, `loadUrlFromTextBox()`, and `applyAppSettings()`.
    // It is `Boolean` instead of `boolean` because `applyAppSettings()` needs to know if it is `null`.
    private Boolean javaScriptEnabled;

    // `firstPartyCookiesEnabled` is used in `onCreate()`, `onCreateOptionsMenu()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, `onDownloadImage()`, `onDownloadFile()`, and `applyAppSettings()`.
    private boolean firstPartyCookiesEnabled;

    // `thirdPartyCookiesEnabled` used in `onCreate()`, `onCreateOptionsMenu()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and `applyAppSettings()`.
    private boolean thirdPartyCookiesEnabled;

    // `domStorageEnabled` is used in `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, and `applyAppSettings()`.
    private boolean domStorageEnabled;

    // `saveFormDataEnabled` is used in `onCreate()`, `onCreateOptionsMenu()`, `onOptionsItemSelected()`, and `applyAppSettings()`.
    private boolean saveFormDataEnabled;

    // `swipeToRefreshEnabled` is used in `onPrepareOptionsMenu()` and `applyAppSettings()`.
    private boolean swipeToRefreshEnabled;

    // 'homepage' is used in `onCreate()`, `onNavigationItemSelected()`, and `applyAppSettings()`.
    private String homepage;

    // `searchURL` is used in `loadURLFromTextBox()` and `applyAppSettings()`.
    private String searchURL;

    // `adBlockerEnabled` is used in `onCreate()` and `applyAppSettings()`.
    private boolean adBlockerEnabled;

    // `privacyBrowserRuntime` is used in `onCreate()` and `applyAppSettings()`.
    Runtime privacyBrowserRuntime;

    // `incognitoModeEnabled` is used in `onCreate()` and `applyAppSettings()`.
    private boolean incognitoModeEnabled;

    // `fullScreenBrowsingModeEnabled` is used in `onCreate()` and `applyAppSettings()`.
    private boolean fullScreenBrowsingModeEnabled;

    // `inFullScreenBrowsingMode` is used in `onCreate()`, `onConfigurationChanged()`, and `applyAppSettings()`.
    private boolean inFullScreenBrowsingMode;

    // `hideSystemBarsOnFullscreen` is used in `onCreate()` and `applyAppSettings()`.
    private boolean hideSystemBarsOnFullscreen;

    // `translucentNavigationBarOnFullscreen` is used in `onCreate()` and `applyAppSettings()`.
    private boolean translucentNavigationBarOnFullscreen;

    // `currentDomainName` is used in `onCreate(), `onNavigationItemSelected()`, and `applyDomainSettings()`.
    private String currentDomainName;

    // `waitingForOrbot` is used in `onCreate()` and `applyAppSettings()`.
    private boolean waitingForOrbot;

    // `waitingForOrbotData` is used in `onCreate()` and `applyAppSettings()`.
    private String waitingForOrbotHTMLString;

    // `privateDataDirectoryString` is used in `onCreate()` and `onNavigationItemSelected()`.
    private String privateDataDirectoryString;

    // `findOnPageLinearLayout` is used in `onCreate()`, `onOptionsItemSelected()`, and `closeFindOnPage()`.
    private LinearLayout findOnPageLinearLayout;

    // `findOnPageEditText` is used in `onCreate()`, `onOptionsItemSelected()`, and `closeFindOnPage()`.
    private EditText findOnPageEditText;

    // `mainMenu` is used in `onCreateOptionsMenu()` and `updatePrivacyIcons()`.
    private Menu mainMenu;

    // `drawerToggle` is used in `onCreate()`, `onPostCreate()`, `onConfigurationChanged()`, `onNewIntent()`, and `onNavigationItemSelected()`.
    private ActionBarDrawerToggle drawerToggle;

    // `supportAppBar` is used in `onCreate()`, `onOptionsItemSelected()`, and `closeFindOnPage()`.
    private Toolbar supportAppBar;

    // `urlTextBox` is used in `onCreate()`, `onOptionsItemSelected()`, `loadUrlFromTextBox()`, and `loadUrl()`.
    private EditText urlTextBox;

    // `adView` is used in `onCreate()` and `onConfigurationChanged()`.
    private View adView;

    // `sslErrorHandler` is used in `onCreate()`, `onSslErrorCancel()`, and `onSslErrorProceed`.
    private SslErrorHandler sslErrorHandler;

    // `inputMethodManager` is used in `onOptionsItemSelected()`, `loadUrlFromTextBox()`, and `closeFindOnPage()`.
    private InputMethodManager inputMethodManager;

    // `mainWebViewRelativeLayout` is used in `onCreate()` and `onNavigationItemSelected()`.
    private RelativeLayout mainWebViewRelativeLayout;

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  The whole premise of Privacy Browser is built around an understanding of these dangers.
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_drawerlayout);

        // Get a handle for `inputMethodManager`.
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // We need to use the `SupportActionBar` from `android.support.v7.app.ActionBar` until the minimum API is >= 21.
        supportAppBar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(supportAppBar);
        appBar = getSupportActionBar();

        // This is needed to get rid of the Android Studio warning that `appBar` might be null.
        assert appBar != null;

        // Add the custom `url_app_bar` layout, which shows the favorite icon and the URL text bar.
        appBar.setCustomView(R.layout.url_app_bar);
        appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Set the "go" button on the keyboard to load the URL in urlTextBox.
        urlTextBox = (EditText) appBar.getCustomView().findViewById(R.id.url_edittext);
        urlTextBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the `enter` button, load the URL.
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Load the URL into the mainWebView and consume the event.
                    try {
                        loadUrlFromTextBox();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    // If the enter key was pressed, consume the event.
                    return true;
                } else {
                    // If any other key was pressed, do not consume the event.
                    return false;
                }
            }
        });

        // Set `waitingForOrbotHTMLString`.
        waitingForOrbotHTMLString = "<html><body><br/><center><h1>" + getString(R.string.waiting_for_orbot) + "</h1></center></body></html>";

        // Initialize `currentDomainName`, `orbotStatus`, and `waitingForOrbot`.
        currentDomainName = "";
        orbotStatus = "unknown";
        waitingForOrbot = false;

        // Create an Orbot status `BroadcastReceiver`.
        BroadcastReceiver orbotStatusBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Store the content of the status message in `orbotStatus`.
                orbotStatus = intent.getStringExtra("org.torproject.android.intent.extra.STATUS");

                // If we are waiting on Orbot, load the website now that Orbot is connected.
                if (orbotStatus.equals("ON") && waitingForOrbot) {
                    // Reset `waitingForOrbot`.
                    waitingForOrbot = false;

                    // Load `formattedUrlString
                    loadUrl(formattedUrlString);
                }
            }
        };

        // Register `orbotStatusBroadcastReceiver` on `this` context.
        this.registerReceiver(orbotStatusBroadcastReceiver, new IntentFilter("org.torproject.android.intent.action.STATUS"));

        // Get handles for views that need to be accessed.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        rootCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.root_coordinatorlayout);
        mainWebViewRelativeLayout = (RelativeLayout) findViewById(R.id.main_webview_relativelayout);
        mainWebView = (WebView) findViewById(R.id.main_webview);
        findOnPageLinearLayout = (LinearLayout) findViewById(R.id.find_on_page_linearlayout);
        findOnPageEditText = (EditText) findViewById(R.id.find_on_page_edittext);
        fullScreenVideoFrameLayout = (FrameLayout) findViewById(R.id.full_screen_video_framelayout);
        urlAppBarRelativeLayout = (RelativeLayout) findViewById(R.id.url_app_bar_relativelayout);
        favoriteIconImageView = (ImageView) findViewById(R.id.favorite_icon);

        // Create a double-tap listener to toggle full-screen mode.
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            // Override `onDoubleTap()`.  All other events are handled using the default settings.
            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (fullScreenBrowsingModeEnabled) {  // Only process the double-tap if full screen browsing mode is enabled.
                    // Toggle `inFullScreenBrowsingMode`.
                    inFullScreenBrowsingMode = !inFullScreenBrowsingMode;

                    if (inFullScreenBrowsingMode) {  // Switch to full screen mode.
                        // Hide the `appBar`.
                        appBar.hide();

                        // Hide the `BannerAd` in the free flavor.
                        if (BuildConfig.FLAVOR.contentEquals("free")) {
                            BannerAd.hideAd(adView);
                        }

                        // Modify the system bars.
                        if (hideSystemBarsOnFullscreen) {  // Hide everything.
                            // Remove the translucent overlays.
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                            // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                            drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                            /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                             * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                             * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically rehides them after they are shown.
                             */
                            rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                            // Set `rootCoordinatorLayout` to fill the whole screen.
                            rootCoordinatorLayout.setFitsSystemWindows(false);
                        } else {  // Hide everything except the status and navigation bars.
                            // Set `rootCoordinatorLayout` to fit under the status and navigation bars.
                            rootCoordinatorLayout.setFitsSystemWindows(false);

                            if (translucentNavigationBarOnFullscreen) {  // There is an Android Support Library bug that causes a scrim to print on the right side of the `Drawer Layout` when the navigation bar is displayed on the right of the screen.
                                // Set the navigation bar to be translucent.
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                            }
                        }
                    } else {  // Switch to normal viewing mode.
                        // Show the `appBar`.
                        appBar.show();

                        // Show the `BannerAd` in the free flavor.
                        if (BuildConfig.FLAVOR.contentEquals("free")) {
                            // Reload the ad.  Because the screen may have rotated, we need to use `reloadAfterRotate`.
                            BannerAd.reloadAfterRotate(adView, getApplicationContext(), getString(R.string.ad_id));

                            // Reinitialize the `adView` variable, as the `View` will have been removed and re-added by `BannerAd.reloadAfterRotate()`.
                            adView = findViewById(R.id.adview);
                        }

                        // Remove the translucent navigation bar flag if it is set.
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                        // Add the translucent status flag if it is unset.  This also resets `drawerLayout's` `View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN`.
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        // Remove any `SYSTEM_UI` flags from `rootCoordinatorLayout`.
                        rootCoordinatorLayout.setSystemUiVisibility(0);

                        // Constrain `rootCoordinatorLayout` inside the status and navigation bars.
                        rootCoordinatorLayout.setFitsSystemWindows(true);
                    }

                    // Consume the double-tap.
                    return true;
                } else { // Do not consume the double-tap because full screen browsing mode is disabled.
                    return false;
                }
            }
        });

        // Pass all touch events on `mainWebView` through `gestureDetector` to check for double-taps.
        mainWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Send the `event` to `gestureDetector`.
                return gestureDetector.onTouchEvent(event);
            }
        });

        // Update `findOnPageCountTextView`.
        mainWebView.setFindListener(new WebView.FindListener() {
            // Get a handle for `findOnPageCountTextView`.
            final TextView findOnPageCountTextView = (TextView) findViewById(R.id.find_on_page_count_textview);

            @Override
            public void onFindResultReceived(int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
                if ((isDoneCounting) && (numberOfMatches == 0)) {  // There are no matches.
                    // Set `findOnPageCountTextView` to `0/0`.
                    findOnPageCountTextView.setText(R.string.zero_of_zero);
                } else if (isDoneCounting) {  // There are matches.
                    // `activeMatchOrdinal` is zero-based.
                    int activeMatch = activeMatchOrdinal + 1;

                    // Set `findOnPageCountTextView`.
                    findOnPageCountTextView.setText(activeMatch + "/" + numberOfMatches);
                }
            }
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
                // Search for the text in `mainWebView`.
                mainWebView.findAllAsync(findOnPageEditText.getText().toString());
            }
        });

        // Set the `check mark` button for the `findOnPageEditText` keyboard to close the soft keyboard.
        findOnPageEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {  // The `enter` key was pressed.
                    // Hide the soft keyboard.  `0` indicates no additional flags.
                    inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);

                    // Consume the event.
                    return true;
                } else {  // A different key was pressed.
                    // Do not consume the event.
                    return false;
                }
            }
        });

        // Implement swipe to refresh
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refreshlayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue_700);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainWebView.reload();
            }
        });

        // `DrawerTitle` identifies the `DrawerLayout` in accessibility mode.
        drawerLayout.setDrawerTitle(GravityCompat.START, getString(R.string.navigation_drawer));

        // Listen for touches on the navigation menu.
        final NavigationView navigationView = (NavigationView) findViewById(R.id.navigationview);
        navigationView.setNavigationItemSelectedListener(this);

        // Get handles for `navigationMenu` and the back and forward menu items.  The menu is zero-based, so items 1, 2, and 3 are the second, third, and fourth entries in the menu.
        final Menu navigationMenu = navigationView.getMenu();
        final MenuItem navigationBackMenuItem = navigationMenu.getItem(1);
        final MenuItem navigationForwardMenuItem = navigationMenu.getItem(2);
        final MenuItem navigationHistoryMenuItem = navigationMenu.getItem(3);

        // The `DrawerListener` allows us to update the Navigation Menu.
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if ((newState == DrawerLayout.STATE_SETTLING) || (newState == DrawerLayout.STATE_DRAGGING)) {  // The drawer is opening or closing.
                    // Update the `Back`, `Forward`, and `History` menu items.
                    navigationBackMenuItem.setEnabled(mainWebView.canGoBack());
                    navigationForwardMenuItem.setEnabled(mainWebView.canGoForward());
                    navigationHistoryMenuItem.setEnabled((mainWebView.canGoBack() || mainWebView.canGoForward()));

                    // Hide the keyboard so we can see the navigation menu.  `0` indicates no additional flags.
                    inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
                }
            }
        });

        // drawerToggle creates the hamburger icon at the start of the AppBar.
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, supportAppBar, R.string.open_navigation_drawer, R.string.close_navigation_drawer);

        // Initialize `adServerSet`.
        final Set<String> adServersSet = new HashSet<>();

        // Load the list of ad servers into memory.
        try {
            // Load `pgl.yoyo.org_adservers.txt` into a `BufferedReader`.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("pgl.yoyo.org_adservers.txt")));

            // Create a string for storing each ad server.
            String adServer;

            // Populate `adServersSet`.
            while ((adServer = bufferedReader.readLine()) != null) {
                adServersSet.add(adServer);
            }

            // Close `bufferedReader`.
            bufferedReader.close();
        } catch (IOException ioException) {
            // We're pretty sure the asset exists, so we don't need to worry about the `IOException` ever being thrown.
        }

        mainWebView.setWebViewClient(new WebViewClient() {
            // `shouldOverrideUrlLoading` makes this `WebView` the default handler for URLs inside the app, so that links are not kicked out to other apps.
            // We have to use the deprecated `shouldOverrideUrlLoading` until API >= 24.
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("mailto:")) {  // Load the URL in an external email program because it begins with `mailto:`.
                    // We use `ACTION_SENDTO` instead of `ACTION_SEND` so that only email programs are launched.
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                    // Parse the url and set it as the data for the `Intent`.
                    emailIntent.setData(Uri.parse(url));

                    // `FLAG_ACTIVITY_NEW_TASK` opens the email program in a new task instead as part of Privacy Browser.
                    emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Make it so.
                    startActivity(emailIntent);

                    // Returning `true` indicates the application is handling the URL.
                    return true;
                } else {  // Load the URL in Privacy Browser.
                    // Apply the domain settings for the new URL.
                    applyDomainSettings(url);

                    // Returning `false` causes the current `WebView` to handle the URL and prevents it from adding redirects to the history list.
                    return false;
                }
            }

            // Block ads.  We have to use the deprecated `shouldInterceptRequest` until minimum API >= 21.
            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url){
                if (adBlockerEnabled) {  // Block ads.
                    // Extract the host from `url`.
                    Uri requestUri = Uri.parse(url);
                    String requestHost = requestUri.getHost();

                    // Initialize a variable to track if this is an ad server.
                    boolean requestHostIsAdServer = false;

                    // Check all the subdomains of `requestHost` if it is not `null` against the ad server database.
                    if (requestHost != null) {
                        while (requestHost.contains(".") && !requestHostIsAdServer) {  // Stop checking if we run out of `.` or if we already know that `requestHostIsAdServer` is `true`.
                            if (adServersSet.contains(requestHost)) {
                                requestHostIsAdServer = true;
                            }

                            // Strip out the lowest subdomain of `requestHost`.
                            requestHost = requestHost.substring(requestHost.indexOf(".") + 1);
                        }
                    }

                    if (requestHostIsAdServer) {  // It is an ad server.
                        // Return an empty `WebResourceResponse`.
                        return new WebResourceResponse("text/plain", "utf8", new ByteArrayInputStream("".getBytes()));
                    } else {  // It is not an ad server.
                        // `return null` loads the requested resource.
                        return null;
                    }
                } else {  // Ad blocking is disabled.
                    // `return null` loads the requested resource.
                    return null;
                }
            }

            // Update the URL in urlTextBox when the page starts to load.
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Reset `webViewTitle`
                webViewTitle = getString(R.string.no_title);

                // Check to see if we are waiting on Orbot.
                if (!waitingForOrbot) {  // We are not waiting on Orbot, so we need to process the URL.
                    // We need to update `formattedUrlString` at the beginning of the load, so that if the user toggles JavaScript during the load the new website is reloaded.
                    formattedUrlString = url;

                    // Display the loading URL is the URL text box.
                    urlTextBox.setText(url);

                    // Apply any custom domain settings if the URL was loaded by navigating history.
                    if (navigatingHistory) {
                        applyDomainSettings(url);
                    }
                }
            }

            // Update formattedUrlString and urlTextBox.  It is necessary to do this after the page finishes loading because the final URL can change during load.
            @Override
            public void onPageFinished(WebView view, String url) {
                // Clear the cache and history if Incognito Mode is enabled.
                if (incognitoModeEnabled) {
                    // Clear the cache.  `true` includes disk files.
                    mainWebView.clearCache(true);

                    // Clear the back/forward history.
                    mainWebView.clearHistory();

                    // Manually delete cache folders.
                    try {
                        // Delete the main `cache` folder.
                        privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/cache");

                        // Delete the `app_webview` folder, which contains an additional `WebView` cache.  See `https://code.google.com/p/android/issues/detail?id=233826&thanks=233826&ts=1486670530`.
                        privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/app_webview");
                    } catch (IOException e) {
                        // Do nothing if an error is thrown.
                    }
                }

                // Update `urlTextBox` and apply domain settings if not waiting on Orbot.
                if (!waitingForOrbot) {
                    // Check to see if `WebView` has set `url` to be `about:blank`.
                    if (url.equals("about:blank")) {  // `WebView` is blank, so `formattedUrlString` should be `""` and `urlTextBox` should display a hint.
                        // Set `formattedUrlString` to `""`.
                        formattedUrlString = "";

                        // Update `urlTextBox`.
                        urlTextBox.setText(formattedUrlString);

                        // Request focus for `urlTextBox`.
                        urlTextBox.requestFocus();

                        // Display the keyboard.
                        inputMethodManager.showSoftInput(urlTextBox, 0);

                        // Apply the domain settings.
                        applyDomainSettings(formattedUrlString);
                    } else {  // `WebView` has loaded a webpage.
                        // Set `formattedUrlString`.
                        formattedUrlString = url;

                        // Only update `urlTextBox` if the user is not typing in it.
                        if (!urlTextBox.hasFocus()) {
                            urlTextBox.setText(formattedUrlString);
                        }
                    }

                    // Store the SSL certificate so it can be accessed from `ViewSslCertificateDialog`.
                    sslCertificate = mainWebView.getCertificate();
                }
            }

            // Handle SSL Certificate errors.
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // Store `handler` so it can be accesses from `onSslErrorCancel()` and `onSslErrorProceed()`.
                sslErrorHandler = handler;

                // Display the SSL error `AlertDialog`.
                AppCompatDialogFragment sslCertificateErrorDialogFragment = SslCertificateErrorDialog.displayDialog(error);
                sslCertificateErrorDialogFragment.show(getSupportFragmentManager(), getResources().getString(R.string.ssl_certificate_error));
            }
        });

        // Get a handle for the progress bar.
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mainWebView.setWebChromeClient(new WebChromeClient() {
            // Update the progress bar when a page is loading.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);

                    //Stop the `SwipeToRefresh` indicator if it is running
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            // Set the favorite icon when it changes.
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                // Only update the favorite icon if the website has finished loading.
                if (progressBar.getVisibility() == View.GONE) {
                    // Save a copy of the favorite icon.
                    favoriteIconBitmap = icon;

                    // Place the favorite icon in the appBar.
                    favoriteIconImageView.setImageBitmap(Bitmap.createScaledBitmap(icon, 64, 64, true));
                }
            }

            // Save a copy of the title when it changes.
            @Override
            public void onReceivedTitle(WebView view, String title) {
                // Save a copy of the title.
                webViewTitle = title;
            }

            // Enter full screen video
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // Pause the ad if this is the free flavor.
                if (BuildConfig.FLAVOR.contentEquals("free")) {
                    BannerAd.pauseAd(adView);
                }

                // Remove the translucent overlays.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                 * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                 * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically rehides them after they are shown.
                 */
                rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                // Set `rootCoordinatorLayout` to fill the entire screen.
                rootCoordinatorLayout.setFitsSystemWindows(false);

                // Add `view` to `fullScreenVideoFrameLayout` and display it on the screen.
                fullScreenVideoFrameLayout.addView(view);
                fullScreenVideoFrameLayout.setVisibility(View.VISIBLE);
            }

            // Exit full screen video
            public void onHideCustomView() {
                // Hide `fullScreenVideoFrameLayout`.
                fullScreenVideoFrameLayout.removeAllViews();
                fullScreenVideoFrameLayout.setVisibility(View.GONE);

                // Add the translucent status flag.  This also resets `drawerLayout's` `View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN`.
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                // Set `rootCoordinatorLayout` to fit inside the status and navigation bars.  This also clears the `SYSTEM_UI` flags.
                rootCoordinatorLayout.setFitsSystemWindows(true);

                // Show the ad if this is the free flavor.
                if (BuildConfig.FLAVOR.contentEquals("free")) {
                    // Reload the ad.  Because the screen may have rotated, we need to use `reloadAfterRotate`.
                    BannerAd.reloadAfterRotate(adView, getApplicationContext(), getString(R.string.ad_id));

                    // Reinitialize the `adView` variable, as the `View` will have been removed and re-added by `BannerAd.reloadAfterRotate()`.
                    adView = findViewById(R.id.adview);
                }
            }
        });

        // Register `mainWebView` for a context menu.  This is used to see link targets and download images.
        registerForContextMenu(mainWebView);

        // Allow the downloading of files.
        mainWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // Show the `DownloadFileDialog` `AlertDialog` and name this instance `@string/download`.
                AppCompatDialogFragment downloadFileDialogFragment = DownloadFileDialog.fromUrl(url, contentDisposition, contentLength);
                downloadFileDialogFragment.show(getSupportFragmentManager(), getResources().getString(R.string.download));
            }
        });

        // Allow pinch to zoom.
        mainWebView.getSettings().setBuiltInZoomControls(true);

        // Hide zoom controls.
        mainWebView.getSettings().setDisplayZoomControls(false);

        // Set `mainWebView` to use a wide viewport.  Otherwise, some web pages will be scrunched and some content will render outside the screen.
        mainWebView.getSettings().setUseWideViewPort(true);

        // Set `mainWebView` to load in overview mode (zoomed out to the maximum width).
        mainWebView.getSettings().setLoadWithOverviewMode(true);

        // Initialize cookieManager.
        cookieManager = CookieManager.getInstance();

        // Replace the header that `WebView` creates for `X-Requested-With` with a null value.  The default value is the application ID (com.stoutner.privacybrowser.standard).
        customHeaders.put("X-Requested-With", "");

        // Initialize the default preference values the first time the program is run.  `this` is the context.  `false` keeps this command from resetting any current preferences back to default.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Get the intent that started the app.
        final Intent launchingIntent = getIntent();

        // Extract the launching intent data as `launchingIntentUriData`.
        final Uri launchingIntentUriData = launchingIntent.getData();

        // Convert the launching intent URI data (if it exists) to a string and store it in `formattedUrlString`.
        if (launchingIntentUriData != null) {
            formattedUrlString = launchingIntentUriData.toString();
        }

        // Get a handle for the `Runtime`.
        privacyBrowserRuntime = Runtime.getRuntime();

        // Store the application's private data directory.
        privateDataDirectoryString = getApplicationInfo().dataDir;  // `dataDir` will vary, but will be something like `/data/user/0/com.stoutner.privacybrowser.standard`, which links to `/data/data/com.stoutner.privacybrowser.standard`.

        // Initialize `inFullScreenBrowsingMode`, which is always false at this point because Privacy Browser never starts in full screen browsing mode.
        inFullScreenBrowsingMode = false;

        // Initialize AdView for the free flavor.
        adView = findViewById(R.id.adview);

        // Initialize the privacy settings variables.
        javaScriptEnabled = false;
        firstPartyCookiesEnabled = false;
        thirdPartyCookiesEnabled = false;
        domStorageEnabled = false;
        saveFormDataEnabled = false;

        // Initialize `webViewTitle`.
        webViewTitle = getString(R.string.no_title);

        // Initialize `favoriteIconBitmap`.  We have to use `ContextCompat` until API >= 21.
        Drawable favoriteIconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.world);
        BitmapDrawable favoriteIconBitmapDrawable = (BitmapDrawable) favoriteIconDrawable;
        favoriteIconDefaultBitmap = favoriteIconBitmapDrawable.getBitmap();

        // If the favorite icon is null, load the default.
        if (favoriteIconBitmap == null) {
            favoriteIconBitmap = favoriteIconDefaultBitmap;
        }

        // Apply the app settings from the shared preferences.
        applyAppSettings();

        // Load `formattedUrlString` if we are not waiting for Orbot to connect.
        if (!waitingForOrbot) {
            loadUrl(formattedUrlString);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        // Sets the new intent as the activity intent, so that any future `getIntent()`s pick up this one instead of creating a new activity.
        setIntent(intent);

        if (intent.getData() != null) {
            // Get the intent data and convert it to a string.
            final Uri intentUriData = intent.getData();
            formattedUrlString = intentUriData.toString();
        }

        // Close the navigation drawer if it is open.
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        // Load the website.
        loadUrl(formattedUrlString);

        // Clear the keyboard if displayed and remove the focus on the urlTextBar if it has it.
        mainWebView.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.webview_options_menu, menu);

        // Set mainMenu so it can be used by `onOptionsItemSelected()` and `updatePrivacyIcons`.
        mainMenu = menu;

        // Set the initial status of the privacy icons.  `false` does not call `invalidateOptionsMenu` as the last step.
        updatePrivacyIcons(false);

        // Get handles for the menu items.
        MenuItem toggleFirstPartyCookies = menu.findItem(R.id.toggleFirstPartyCookies);
        MenuItem toggleThirdPartyCookies = menu.findItem(R.id.toggleThirdPartyCookies);
        MenuItem toggleDomStorage = menu.findItem(R.id.toggleDomStorage);
        MenuItem toggleSaveFormData = menu.findItem(R.id.toggleSaveFormData);

        // Only display third-party cookies if SDK >= 21
        toggleThirdPartyCookies.setVisible(Build.VERSION.SDK_INT >= 21);

        // Get the shared preference values.  `this` references the current context.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Set the status of the additional app bar icons.  The default is `false`.
        if (sharedPreferences.getBoolean("display_additional_app_bar_icons", false)) {
            toggleFirstPartyCookies.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            toggleDomStorage.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            toggleSaveFormData.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else { //Do not display the additional icons.
            toggleFirstPartyCookies.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            toggleDomStorage.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            toggleSaveFormData.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get handles for the menu items.
        MenuItem toggleFirstPartyCookies = menu.findItem(R.id.toggleFirstPartyCookies);
        MenuItem toggleThirdPartyCookies = menu.findItem(R.id.toggleThirdPartyCookies);
        MenuItem toggleDomStorage = menu.findItem(R.id.toggleDomStorage);
        MenuItem toggleSaveFormData = menu.findItem(R.id.toggleSaveFormData);
        MenuItem clearCookies = menu.findItem(R.id.clearCookies);
        MenuItem clearFormData = menu.findItem(R.id.clearFormData);
        MenuItem refreshMenuItem = menu.findItem(R.id.refresh);

        // Set the status of the menu item checkboxes.
        toggleFirstPartyCookies.setChecked(firstPartyCookiesEnabled);
        toggleThirdPartyCookies.setChecked(thirdPartyCookiesEnabled);
        toggleDomStorage.setChecked(domStorageEnabled);
        toggleSaveFormData.setChecked(saveFormDataEnabled);

        // Enable third-party cookies if first-party cookies are enabled.
        toggleThirdPartyCookies.setEnabled(firstPartyCookiesEnabled);

        // Enable DOM Storage if JavaScript is enabled.
        toggleDomStorage.setEnabled(javaScriptEnabled);

        // Enable Clear Cookies if there are any.
        clearCookies.setEnabled(cookieManager.hasCookies());

        // Enable Clear Form Data is there is any.
        WebViewDatabase mainWebViewDatabase = WebViewDatabase.getInstance(this);
        clearFormData.setEnabled(mainWebViewDatabase.hasFormData());

        // Only show `Refresh` if `swipeToRefresh` is disabled.
        refreshMenuItem.setVisible(!swipeToRefreshEnabled);

        // Initialize font size variables.
        int fontSize = mainWebView.getSettings().getTextZoom();
        String fontSizeTitle;
        MenuItem selectedFontSizeMenuItem;

        // Prepare the font size title and current size menu item.
        switch (fontSize) {
            case 25:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.twenty_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeTwentyFivePercent);
                break;

            case 50:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.fifty_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeFiftyPercent);
                break;

            case 75:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.seventy_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeSeventyFivePercent);
                break;

            case 100:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.one_hundred_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeOneHundredPercent);
                break;

            case 125:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.one_hundred_twenty_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeOneHundredTwentyFivePercent);
                break;

            case 150:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.one_hundred_fifty_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeOneHundredFiftyPercent);
                break;

            case 175:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.one_hundred_seventy_five_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeOneHundredSeventyFivePercent);
                break;

            case 200:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.two_hundred_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeTwoHundredPercent);
                break;

            default:
                fontSizeTitle = getResources().getString(R.string.font_size) + " - " + getResources().getString(R.string.one_hundred_percent);
                selectedFontSizeMenuItem = menu.findItem(R.id.fontSizeOneHundredPercent);
                break;
        }

        // Set the font size title and select the current size menu item.
        MenuItem fontSizeMenuItem = menu.findItem(R.id.fontSize);
        fontSizeMenuItem.setTitle(fontSizeTitle);
        selectedFontSizeMenuItem.setChecked(true);

        // Run all the other default commands.
        super.onPrepareOptionsMenu(menu);

        // `return true` displays the menu.
        return true;
    }

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.
    @SuppressLint("SetJavaScriptEnabled")
    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        // Set the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.toggleJavaScript:
                // Switch the status of javaScriptEnabled.
                javaScriptEnabled = !javaScriptEnabled;

                // Apply the new JavaScript status.
                mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);

                // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
                updatePrivacyIcons(true);

                // Display a `Snackbar`.
                if (javaScriptEnabled) {  // JavaScrip is enabled.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.javascript_enabled, Snackbar.LENGTH_SHORT).show();
                } else if (firstPartyCookiesEnabled) {  // JavaScript is disabled, but first-party cookies are enabled.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.javascript_disabled, Snackbar.LENGTH_SHORT).show();
                } else {  // Privacy mode.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                }

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.toggleFirstPartyCookies:
                // Switch the status of firstPartyCookiesEnabled.
                firstPartyCookiesEnabled = !firstPartyCookiesEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(firstPartyCookiesEnabled);

                // Apply the new cookie status.
                cookieManager.setAcceptCookie(firstPartyCookiesEnabled);

                // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
                updatePrivacyIcons(true);

                // Display a `Snackbar`.
                if (firstPartyCookiesEnabled) {  // First-party cookies are enabled.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.first_party_cookies_enabled, Snackbar.LENGTH_SHORT).show();
                } else if (javaScriptEnabled){  // JavaScript is still enabled.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.first_party_cookies_disabled, Snackbar.LENGTH_SHORT).show();
                } else {  // Privacy mode.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                }

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.toggleThirdPartyCookies:
                if (Build.VERSION.SDK_INT >= 21) {
                    // Switch the status of thirdPartyCookiesEnabled.
                    thirdPartyCookiesEnabled = !thirdPartyCookiesEnabled;

                    // Update the menu checkbox.
                    menuItem.setChecked(thirdPartyCookiesEnabled);

                    // Apply the new cookie status.
                    cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);

                    // Display a `Snackbar`.
                    if (thirdPartyCookiesEnabled) {
                        Snackbar.make(findViewById(R.id.main_webview), R.string.third_party_cookies_enabled, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(findViewById(R.id.main_webview), R.string.third_party_cookies_disabled, Snackbar.LENGTH_SHORT).show();
                    }

                    // Reload the WebView.
                    mainWebView.reload();
                } // Else do nothing because SDK < 21.
                return true;

            case R.id.toggleDomStorage:
                // Switch the status of domStorageEnabled.
                domStorageEnabled = !domStorageEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(domStorageEnabled);

                // Apply the new DOM Storage status.
                mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);

                // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
                updatePrivacyIcons(true);

                // Display a `Snackbar`.
                if (domStorageEnabled) {
                    Snackbar.make(findViewById(R.id.main_webview), R.string.dom_storage_enabled, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.main_webview), R.string.dom_storage_disabled, Snackbar.LENGTH_SHORT).show();
                }

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.toggleSaveFormData:
                // Switch the status of saveFormDataEnabled.
                saveFormDataEnabled = !saveFormDataEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(saveFormDataEnabled);

                // Apply the new form data status.
                mainWebView.getSettings().setSaveFormData(saveFormDataEnabled);

                // Display a `Snackbar`.
                if (saveFormDataEnabled) {
                    Snackbar.make(findViewById(R.id.main_webview), R.string.form_data_enabled, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(findViewById(R.id.main_webview), R.string.form_data_disabled, Snackbar.LENGTH_SHORT).show();
                }

                // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
                updatePrivacyIcons(true);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.clearCookies:
                if (Build.VERSION.SDK_INT < 21) {
                    cookieManager.removeAllCookie();
                } else {
                    cookieManager.removeAllCookies(null);
                }
                Snackbar.make(findViewById(R.id.main_webview), R.string.cookies_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.clearDomStorage:
                WebStorage webStorage = WebStorage.getInstance();
                webStorage.deleteAllData();
                Snackbar.make(findViewById(R.id.main_webview), R.string.dom_storage_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.clearFormData:
                WebViewDatabase mainWebViewDatabase = WebViewDatabase.getInstance(this);
                mainWebViewDatabase.clearFormData();
                Snackbar.make(findViewById(R.id.main_webview), R.string.form_data_deleted, Snackbar.LENGTH_SHORT).show();
                return true;

            case R.id.fontSizeTwentyFivePercent:
                mainWebView.getSettings().setTextZoom(25);
                return true;

            case R.id.fontSizeFiftyPercent:
                mainWebView.getSettings().setTextZoom(50);
                return true;

            case R.id.fontSizeSeventyFivePercent:
                mainWebView.getSettings().setTextZoom(75);
                return true;

            case R.id.fontSizeOneHundredPercent:
                mainWebView.getSettings().setTextZoom(100);
                return true;

            case R.id.fontSizeOneHundredTwentyFivePercent:
                mainWebView.getSettings().setTextZoom(125);
                return true;

            case R.id.fontSizeOneHundredFiftyPercent:
                mainWebView.getSettings().setTextZoom(150);
                return true;

            case R.id.fontSizeOneHundredSeventyFivePercent:
                mainWebView.getSettings().setTextZoom(175);
                return true;

            case R.id.fontSizeTwoHundredPercent:
                mainWebView.getSettings().setTextZoom(200);
                return true;

            case R.id.share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, urlTextBox.getText().toString());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share URL"));
                return true;

            case R.id.find_on_page:
                // Hide the URL app bar.
                supportAppBar.setVisibility(View.GONE);

                // Show the Find on Page `RelativeLayout`.
                findOnPageLinearLayout.setVisibility(View.VISIBLE);

                // Display the keyboard.  We have to wait 200 ms before running the command to work around a bug in Android.
                // http://stackoverflow.com/questions/5520085/android-show-softkeyboard-with-showsoftinput-is-not-working
                findOnPageEditText.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // Set the focus on `findOnPageEditText`.
                        findOnPageEditText.requestFocus();

                        // Display the keyboard.
                        inputMethodManager.showSoftInput(findOnPageEditText, 0);
                    }
                }, 200);
                return true;

            case R.id.refresh:
                mainWebView.reload();
                return true;

            case R.id.print:
                // Get a `PrintManager` instance.
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

                // Convert `mainWebView` to `printDocumentAdapter`.
                PrintDocumentAdapter printDocumentAdapter = mainWebView.createPrintDocumentAdapter();

                // Print the document.  The print attributes are `null`.
                printManager.print(getResources().getString(R.string.privacy_browser_web_page), printDocumentAdapter, null);
                return true;

            case R.id.addToHomescreen:
                // Show the `CreateHomeScreenShortcutDialog` `AlertDialog` and name this instance `R.string.create_shortcut`.
                AppCompatDialogFragment createHomeScreenShortcutDialogFragment = new CreateHomeScreenShortcutDialog();
                createHomeScreenShortcutDialogFragment.show(getSupportFragmentManager(), getResources().getString(R.string.create_shortcut));

                //Everything else will be handled by `CreateHomeScreenShortcutDialog` and the associated listener below.
                return true;

            default:
                // Don't consume the event.
                return super.onOptionsItemSelected(menuItem);
        }
    }

    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int menuItemId = menuItem.getItemId();

        switch (menuItemId) {
            case R.id.home:
                loadUrl(homepage);
                break;

            case R.id.back:
                if (mainWebView.canGoBack()) {
                    // Set `navigatingHistory` so that the domain settings are applied when the new URL is loaded.
                    navigatingHistory = true;

                    // Load the previous website in the history.
                    mainWebView.goBack();
                }
                break;

            case R.id.forward:
                if (mainWebView.canGoForward()) {
                    // Set `navigatingHistory` so that the domain settings are applied when the new URL is loaded.
                    navigatingHistory = true;

                    // Load the next website in the history.
                    mainWebView.goForward();
                }
                break;

            case R.id.history:
                // Get the `WebBackForwardList`.
                WebBackForwardList webBackForwardList = mainWebView.copyBackForwardList();

                // Show the `UrlHistoryDialog` `AlertDialog` and name this instance `R.string.history`.  `this` is the `Context`.
                AppCompatDialogFragment urlHistoryDialogFragment = UrlHistoryDialog.loadBackForwardList(this, webBackForwardList);
                urlHistoryDialogFragment.show(getSupportFragmentManager(), getResources().getString(R.string.history));
                break;

            case R.id.bookmarks:
                // Launch BookmarksActivity.
                Intent bookmarksIntent = new Intent(this, BookmarksActivity.class);
                startActivity(bookmarksIntent);
                break;

            case R.id.downloads:
                // Launch the system Download Manager.
                Intent downloadManagerIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);

                // Launch as a new task so that Download Manager and Privacy Browser show as separate windows in the recent tasks list.
                downloadManagerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(downloadManagerIntent);
                break;

            case R.id.settings:
                // Reset `currentDomainName` so that domain settings are reapplied after returning to `MainWebViewActivity`.
                currentDomainName = "";

                // Launch `SettingsActivity`.
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.domains:
                // Reset `currentDomainName` so that domain settings are reapplied after returning to `MainWebViewActivity`.
                currentDomainName = "";

                // Launch `DomainsActivity`.
                Intent domainsIntent = new Intent(this, DomainsActivity.class);
                startActivity(domainsIntent);
                break;

            case R.id.guide:
                // Launch `GuideActivity`.
                Intent guideIntent = new Intent(this, GuideActivity.class);
                startActivity(guideIntent);
                break;

            case R.id.about:
                // Launch `AboutActivity`.
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;

            case R.id.clearAndExit:
                // Clear cookies.  The commands changed slightly in API 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.removeAllCookies(null);
                } else {
                    cookieManager.removeAllCookie();
                }

                // Clear DOM storage.
                WebStorage domStorage = WebStorage.getInstance();
                domStorage.deleteAllData();

                // Clear form data.
                WebViewDatabase webViewDatabase = WebViewDatabase.getInstance(this);
                webViewDatabase.clearFormData();

                // Clear the cache.  `true` includes disk files.
                mainWebView.clearCache(true);

                // Clear the back/forward history.
                mainWebView.clearHistory();

                // Clear any SSL certificate preferences.
                mainWebView.clearSslPreferences();

                // Clear `formattedUrlString`.
                formattedUrlString = null;

                // Clear `customHeaders`.
                customHeaders.clear();

                // Detach all views from `mainWebViewRelativeLayout`.
                mainWebViewRelativeLayout.removeAllViews();

                // Destroy the internal state of `mainWebView`.
                mainWebView.destroy();

                // Manually delete cache folders.
                try {
                    // Delete the main `cache` folder.
                    privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/cache");

                    // Delete the `app_webview` folder, which contains an additional `WebView` cache.  See `https://code.google.com/p/android/issues/detail?id=233826&thanks=233826&ts=1486670530`.
                    privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/app_webview");
                } catch (IOException e) {
                    // Do nothing if an error is thrown.
                }

                // Close Privacy Browser.  `finishAndRemoveTask` also removes Privacy Browser from the recent app list.
                if (Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }

                // Remove the terminated program from RAM.  The status code is `0`.
                System.exit(0);
                break;

            default:
                break;
        }

        // Close the navigation drawer.
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the state of the DrawerToggle after onRestoreInstanceState has finished.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Reload the ad for the free flavor if we are not in full screen mode.
        if (BuildConfig.FLAVOR.contentEquals("free") && !inFullScreenBrowsingMode) {
            // Reload the ad.
            BannerAd.reloadAfterRotate(adView, getApplicationContext(), getString(R.string.ad_id));

            // Reinitialize the `adView` variable, as the `View` will have been removed and re-added by `BannerAd.reloadAfterRotate()`.
            adView = findViewById(R.id.adview);
        }

        // `invalidateOptionsMenu` should recalculate the number of action buttons from the menu to display on the app bar, but it doesn't because of the this bug:  https://code.google.com/p/android/issues/detail?id=20493#c8
        // ActivityCompat.invalidateOptionsMenu(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        // Store the `HitTestResult`.
        final WebView.HitTestResult hitTestResult = mainWebView.getHitTestResult();

        // Create strings.
        final String imageUrl;
        final String linkUrl;

        // Get a handle for the `ClipboardManager`.
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        switch (hitTestResult.getType()) {
            // `SRC_ANCHOR_TYPE` is a link.
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                // Get the target URL.
                linkUrl = hitTestResult.getExtra();

                // Set the target URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(linkUrl);

                // Add a `Load URL` entry.
                menu.add(R.string.load_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        loadUrl(linkUrl);
                        return false;
                    }
                });

                // Add a `Copy URL` entry.
                menu.add(R.string.copy_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Save the link URL in a `ClipData`.
                        ClipData srcAnchorTypeClipData = ClipData.newPlainText(getResources().getString(R.string.url), linkUrl);

                        // Set the `ClipData` as the clipboard's primary clip.
                        clipboardManager.setPrimaryClip(srcAnchorTypeClipData);
                        return false;
                    }
                });

                // Add a `Cancel` entry, which by default closes the `ContextMenu`.
                menu.add(R.string.cancel);
                break;

            case WebView.HitTestResult.EMAIL_TYPE:
                // Get the target URL.
                linkUrl = hitTestResult.getExtra();

                // Set the target URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(linkUrl);

                // Add a `Write Email` entry.
                menu.add(R.string.write_email).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // We use `ACTION_SENDTO` instead of `ACTION_SEND` so that only email programs are launched.
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

                        // Parse the url and set it as the data for the `Intent`.
                        emailIntent.setData(Uri.parse("mailto:" + linkUrl));

                        // `FLAG_ACTIVITY_NEW_TASK` opens the email program in a new task instead as part of Privacy Browser.
                        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        // Make it so.
                        startActivity(emailIntent);
                        return false;
                    }
                });

                // Add a `Copy Email Address` entry.
                menu.add(R.string.copy_email_address).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Save the email address in a `ClipData`.
                        ClipData srcEmailTypeClipData = ClipData.newPlainText(getResources().getString(R.string.email_address), linkUrl);

                        // Set the `ClipData` as the clipboard's primary clip.
                        clipboardManager.setPrimaryClip(srcEmailTypeClipData);
                        return false;
                    }
                });

                // Add a `Cancel` entry, which by default closes the `ContextMenu`.
                menu.add(R.string.cancel);
                break;

            // `IMAGE_TYPE` is an image.
            case WebView.HitTestResult.IMAGE_TYPE:
                // Get the image URL.
                imageUrl = hitTestResult.getExtra();

                // Set the image URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(imageUrl);

                // Add a `View Image` entry.
                menu.add(R.string.view_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        loadUrl(imageUrl);
                        return false;
                    }
                });

                // Add a `Download Image` entry.
                menu.add(R.string.download_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Show the `DownloadImageDialog` `AlertDialog` and name this instance `@string/download`.
                        AppCompatDialogFragment downloadImageDialogFragment = DownloadImageDialog.imageUrl(imageUrl);
                        downloadImageDialogFragment.show(getSupportFragmentManager(), getResources().getString(R.string.download));
                        return false;
                    }
                });

                // Add a `Copy URL` entry.
                menu.add(R.string.copy_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Save the image URL in a `ClipData`.
                        ClipData srcImageTypeClipData = ClipData.newPlainText(getResources().getString(R.string.url), imageUrl);

                        // Set the `ClipData` as the clipboard's primary clip.
                        clipboardManager.setPrimaryClip(srcImageTypeClipData);
                        return false;
                    }
                });

                // Add a `Cancel` entry, which by default closes the `ContextMenu`.
                menu.add(R.string.cancel);
                break;


            // `SRC_IMAGE_ANCHOR_TYPE` is an image that is also a link.
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                // Get the image URL.
                imageUrl = hitTestResult.getExtra();

                // Set the image URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(imageUrl);

                // Add a `View Image` entry.
                menu.add(R.string.view_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        loadUrl(imageUrl);
                        return false;
                    }
                });

                // Add a `Download Image` entry.
                menu.add(R.string.download_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Show the `DownloadImageDialog` `AlertDialog` and name this instance `@string/download`.
                        AppCompatDialogFragment downloadImageDialogFragment = DownloadImageDialog.imageUrl(imageUrl);
                        downloadImageDialogFragment.show(getSupportFragmentManager(), getResources().getString(R.string.download));
                        return false;
                    }
                });

                // Add a `Copy URL` entry.
                menu.add(R.string.copy_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Save the image URL in a `ClipData`.
                        ClipData srcImageAnchorTypeClipData = ClipData.newPlainText(getResources().getString(R.string.url), imageUrl);

                        // Set the `ClipData` as the clipboard's primary clip.
                        clipboardManager.setPrimaryClip(srcImageAnchorTypeClipData);
                        return false;
                    }
                });

                // Add a `Cancel` entry, which by default closes the `ContextMenu`.
                menu.add(R.string.cancel);
                break;
        }
    }

    @Override
    public void onCreateHomeScreenShortcut(AppCompatDialogFragment dialogFragment) {
        // Get shortcutNameEditText from the alert dialog.
        EditText shortcutNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.shortcut_name_edittext);

        // Create the bookmark shortcut based on formattedUrlString.
        Intent bookmarkShortcut = new Intent();
        bookmarkShortcut.setAction(Intent.ACTION_VIEW);
        bookmarkShortcut.setData(Uri.parse(formattedUrlString));

        // Place the bookmark shortcut on the home screen.
        Intent placeBookmarkShortcut = new Intent();
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.INTENT", bookmarkShortcut);
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.NAME", shortcutNameEditText.getText().toString());
        placeBookmarkShortcut.putExtra("android.intent.extra.shortcut.ICON", favoriteIconBitmap);
        placeBookmarkShortcut.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(placeBookmarkShortcut);
    }

    @Override
    public void onDownloadImage(AppCompatDialogFragment dialogFragment, String imageUrl) {
        // Download the image if it has an HTTP or HTTPS URI.
        if (imageUrl.startsWith("http")) {
            // Get a handle for the system `DOWNLOAD_SERVICE`.
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            // Parse `imageUrl`.
            DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(imageUrl));

            // Pass cookies to download manager if cookies are enabled.  This is required to download images from websites that require a login.
            if (firstPartyCookiesEnabled) {
                // Get the cookies for `imageUrl`.
                String cookies = cookieManager.getCookie(imageUrl);

                // Add the cookies to `downloadRequest`.  In the HTTP request header, cookies are named `Cookie`.
                downloadRequest.addRequestHeader("Cookie", cookies);
            }

            // Get the file name from `dialogFragment`.
            EditText downloadImageNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.download_image_name);
            String imageName = downloadImageNameEditText.getText().toString();

            // Once we have `WRITE_EXTERNAL_STORAGE` permissions we can use `setDestinationInExternalPublicDir`.
            if (Build.VERSION.SDK_INT >= 23) { // If API >= 23, set the download save in the the `DIRECTORY_DOWNLOADS` using `imageName`.
                downloadRequest.setDestinationInExternalFilesDir(this, "/", imageName);
            } else { // Only set the title using `imageName`.
                downloadRequest.setTitle(imageName);
            }

            // Allow `MediaScanner` to index the download if it is a media file.
            downloadRequest.allowScanningByMediaScanner();

            // Add the URL as the description for the download.
            downloadRequest.setDescription(imageUrl);

            // Show the download notification after the download is completed.
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Initiate the download.
            downloadManager.enqueue(downloadRequest);
        } else {  // The image is not an HTTP or HTTPS URI.
            Snackbar.make(mainWebView, R.string.cannot_download_image, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    public void onDownloadFile(AppCompatDialogFragment dialogFragment, String downloadUrl) {
        // Download the file if it has an HTTP or HTTPS URI.
        if (downloadUrl.startsWith("http")) {

            // Get a handle for the system `DOWNLOAD_SERVICE`.
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

            // Parse `downloadUrl`.
            DownloadManager.Request downloadRequest = new DownloadManager.Request(Uri.parse(downloadUrl));

            // Pass cookies to download manager if cookies are enabled.  This is required to download files from websites that require a login.
            if (firstPartyCookiesEnabled) {
                // Get the cookies for `downloadUrl`.
                String cookies = cookieManager.getCookie(downloadUrl);

                // Add the cookies to `downloadRequest`.  In the HTTP request header, cookies are named `Cookie`.
                downloadRequest.addRequestHeader("Cookie", cookies);
            }

            // Get the file name from `dialogFragment`.
            EditText downloadFileNameEditText = (EditText) dialogFragment.getDialog().findViewById(R.id.download_file_name);
            String fileName = downloadFileNameEditText.getText().toString();

            // Once we have `WRITE_EXTERNAL_STORAGE` permissions we can use `setDestinationInExternalPublicDir`.
            if (Build.VERSION.SDK_INT >= 23) { // If API >= 23, set the download location to `/sdcard/Android/data/com.stoutner.privacybrowser.standard/files` named `fileName`.
                downloadRequest.setDestinationInExternalFilesDir(this, "/", fileName);
            } else { // Only set the title using `fileName`.
                downloadRequest.setTitle(fileName);
            }

            // Allow `MediaScanner` to index the download if it is a media file.
            downloadRequest.allowScanningByMediaScanner();

            // Add the URL as the description for the download.
            downloadRequest.setDescription(downloadUrl);

            // Show the download notification after the download is completed.
            downloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            // Initiate the download.
            downloadManager.enqueue(downloadRequest);
        } else {  // The download is not an HTTP or HTTPS URI.
            Snackbar.make(mainWebView, R.string.cannot_download_file, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    public void viewSslCertificate(View view) {
        // Show the `ViewSslCertificateDialog` `AlertDialog` and name this instance `@string/view_ssl_certificate`.
        DialogFragment viewSslCertificateDialogFragment = new ViewSslCertificateDialog();
        viewSslCertificateDialogFragment.show(getFragmentManager(), getResources().getString(R.string.view_ssl_certificate));
    }

    @Override
    public void onSslErrorCancel() {
        sslErrorHandler.cancel();
    }

    @Override
    public void onSslErrorProceed() {
        sslErrorHandler.proceed();
    }

    @Override
    public void onUrlHistoryEntrySelected(int moveBackOrForwardSteps) {
        // Set `navigatingHistory` so that the domain settings are applied when the new URL is loaded.
        navigatingHistory = true;

        // Load the history entry.
        mainWebView.goBackOrForward(moveBackOrForwardSteps);
    }

    @Override
    public void onClearHistory() {
        // Clear the history.
        mainWebView.clearHistory();
    }

    // Override `onBackPressed` to handle the navigation drawer and `mainWebView`.
    @Override
    public void onBackPressed() {
        // Close the navigation drawer if it is available.  GravityCompat.START is the drawer on the left on Left-to-Right layout text.
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Load the previous URL if available.
            if (mainWebView.canGoBack()) {
                // Set `navigatingHistory` so that the domain settings are applied when the new URL is loaded.
                navigatingHistory = true;

                // Go back.
                mainWebView.goBack();
            } else {
                // Pass `onBackPressed()` to the system.
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onPause() {
        // Pause `mainWebView`.
        mainWebView.onPause();

        // Stop all JavaScript.
        mainWebView.pauseTimers();

        // Pause the adView or it will continue to consume resources in the background on the free flavor.
        if (BuildConfig.FLAVOR.contentEquals("free")) {
            BannerAd.pauseAd(adView);
        }

        super.onPause();
    }

    @Override
    public void onResume() {  // `onResume()` also runs every time the app starts after `onCreate()` and `onStart()`.
        super.onResume();

        // Resume JavaScript (if enabled).
        mainWebView.resumeTimers();

        // Resume `mainWebView`.
        mainWebView.onResume();

        // Resume the adView for the free flavor.
        if (BuildConfig.FLAVOR.contentEquals("free")) {
            BannerAd.resumeAd(adView);
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();

        // Apply the settings from shared preferences, which might have been changed in `SettingsActivity`.
        applyAppSettings();

        // Update the privacy icon.  `true` runs `invalidateOptionsMenu` as the last step.
        updatePrivacyIcons(true);

    }

    private void loadUrlFromTextBox() throws UnsupportedEncodingException {
        // Get the text from urlTextBox and convert it to a string.  trim() removes white spaces from the beginning and end of the string.
        String unformattedUrlString = urlTextBox.getText().toString().trim();

        // Check to see if `unformattedUrlString` is a valid URL.  Otherwise, convert it into a search.
        if ((Patterns.WEB_URL.matcher(unformattedUrlString).matches()) || (unformattedUrlString.startsWith("http://")) || (unformattedUrlString.startsWith("https://"))) {
            // Add `http://` at the beginning if it is missing.  Otherwise the app will segfault.
            if (!unformattedUrlString.startsWith("http")) {
                unformattedUrlString = "http://" + unformattedUrlString;
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
            final String scheme = unformattedUrl != null ? unformattedUrl.getProtocol() : null;
            final String authority = unformattedUrl != null ? unformattedUrl.getAuthority() : null;
            final String path = unformattedUrl != null ? unformattedUrl.getPath() : null;
            final String query = unformattedUrl != null ? unformattedUrl.getQuery() : null;
            final String fragment = unformattedUrl != null ? unformattedUrl.getRef() : null;

            // Build the URI.
            Uri.Builder formattedUri = new Uri.Builder();
            formattedUri.scheme(scheme).authority(authority).path(path).query(query).fragment(fragment);

            // Decode `formattedUri` as a `String` in `UTF-8`.
            formattedUrlString = URLDecoder.decode(formattedUri.build().toString(), "UTF-8");
        } else {
            // Sanitize the search input and convert it to a search.
            final String encodedUrlString = URLEncoder.encode(unformattedUrlString, "UTF-8");

            // Add the base search URL.
            formattedUrlString = searchURL + encodedUrlString;
        }

        loadUrl(formattedUrlString);

        // Hide the keyboard so we can see the webpage.  `0` indicates no additional flags.
        inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
    }


    private void loadUrl(String url) {
        // Apply any custom domain settings.
        applyDomainSettings(url);

        // Load the URL.
        mainWebView.loadUrl(url, customHeaders);
    }

    // We have to use the deprecated `.getDrawable()` until the minimum API >= 21.
    @SuppressWarnings("deprecation")
    private void applyDomainSettings(String url) {
        // Reset `navigatingHistory`.
        navigatingHistory = false;

        // Parse the URL into a URI.
        Uri uri = Uri.parse(url);

        // Extract the domain from `uri`.
        String hostName = uri.getHost();

        // Initialize `loadingNewDomainName`.
        boolean loadingNewDomainName;

        // If either `hostName` or `currentDomainName` are `null`, run the options for loading a new domain name.
        // The lint suggestion to simplify the `if` statement is incorrect, because `hostName.equals(currentDomainName)` can produce a `null object reference.`
        //noinspection SimplifiableIfStatement
        if ((hostName == null) || (currentDomainName == null)) {
            loadingNewDomainName = true;
        } else {  // Determine if `hostName` equals `currentDomainName`.
            loadingNewDomainName = !hostName.equals(currentDomainName);
        }

        // Only apply the domain settings if we are loading a new domain.  This allows the user to set temporary settings for JavaScript, cookies, DOM storage, etc.
        if (loadingNewDomainName) {
            // Set the new `hostname` as the `currentDomainName`.
            currentDomainName = hostName;

            // Reset `favoriteIconBitmap` and display it in the `appbar`.
            favoriteIconBitmap = favoriteIconDefaultBitmap;
            favoriteIconImageView.setImageBitmap(Bitmap.createScaledBitmap(favoriteIconBitmap, 64, 64, true));

            // Initialize the database handler.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
            // The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
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

            // Initialize variables to track if this domain has stored domain settings, and if so, under which name.
            boolean hostHasDomainSettings = false;
            String domainNameInDatabase = null;

            // Check the hostname.
            if (domainSettingsSet.contains(hostName)) {
                hostHasDomainSettings = true;
                domainNameInDatabase = hostName;
            }

            // If `hostName` is not `null`, check all the subdomains of `hostName` against wildcard domains in `domainCursor`.
            if (hostName != null) {
                while (hostName.contains(".") && !hostHasDomainSettings) {  // Stop checking if we run out of  `.` or if we already know that `hostHasDomainSettings` is `true`.
                    if (domainSettingsSet.contains("*." + hostName)) {  // Check the host name prepended by `*.`.
                        hostHasDomainSettings = true;
                        domainNameInDatabase = "*." + hostName;
                    }

                    // Strip out the lowest subdomain of `host`.
                    hostName = hostName.substring(hostName.indexOf(".") + 1);
                }
            }

            if (hostHasDomainSettings) {  // The url we are loading has custom domain settings.
                // Get a cursor for the current host and move it to the first position.
                Cursor currentHostDomainSettingsCursor = domainsDatabaseHelper.getCursorForDomainName(domainNameInDatabase);
                currentHostDomainSettingsCursor.moveToFirst();

                // Get the settings from the cursor.
                javaScriptEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_JAVASCRIPT)) == 1);
                firstPartyCookiesEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES)) == 1);
                thirdPartyCookiesEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES)) == 1);
                domStorageEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_DOM_STORAGE)) == 1);
                saveFormDataEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FORM_DATA)) == 1);
                String userAgentString = (currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.USER_AGENT)));
                int fontSize = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE)));

                // Close `currentHostDomainSettingsCursor`.
                currentHostDomainSettingsCursor.close();

                // Apply the domain settings.
                mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);
                cookieManager.setAcceptCookie(firstPartyCookiesEnabled);
                mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);
                mainWebView.getSettings().setSaveFormData(saveFormDataEnabled);
                mainWebView.getSettings().setTextZoom(fontSize);

                // Set third-party cookies status if API >= 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);
                }

                // Set the user agent.
                if (userAgentString.equals("WebView default user agent")) {
                    // Set the user agent to `""`, which uses the default value.
                    mainWebView.getSettings().setUserAgentString("");
                } else {
                    // Use the selected user agent.
                    mainWebView.getSettings().setUserAgentString(userAgentString);
                }

                // Set a green background on `urlTextBox` to indicate that custom domain settings are being used.  We have to use the deprecated `.getDrawable()` until the minimum API >= 21.
                urlAppBarRelativeLayout.setBackground(getResources().getDrawable(R.drawable.url_bar_background_green));
            } else {  // The URL we are loading does not have custom domain settings.  Load the defaults.
                // Get the shared preference values.  `this` references the current context.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                // Store the values from `sharedPreferences` in variables.
                javaScriptEnabled = sharedPreferences.getBoolean("javascript_enabled", false);
                firstPartyCookiesEnabled = sharedPreferences.getBoolean("first_party_cookies_enabled", false);
                thirdPartyCookiesEnabled = sharedPreferences.getBoolean("third_party_cookies_enabled", false);
                domStorageEnabled = sharedPreferences.getBoolean("dom_storage_enabled", false);
                saveFormDataEnabled = sharedPreferences.getBoolean("save_form_data_enabled", false);
                String userAgentString = sharedPreferences.getString("user_agent", "PrivacyBrowser/1.0");
                String customUserAgentString = sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0");
                String defaultFontSizeString = sharedPreferences.getString("default_font_size", "100");

                // Apply the default settings.
                mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);
                cookieManager.setAcceptCookie(firstPartyCookiesEnabled);
                mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);
                mainWebView.getSettings().setSaveFormData(saveFormDataEnabled);
                mainWebView.getSettings().setTextZoom(Integer.valueOf(defaultFontSizeString));

                // Set third-party cookies status if API >= 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);
                }

                // Set the default user agent.
                switch (userAgentString) {
                    case "WebView default user agent":
                        // Set the user agent to `""`, which uses the default value.
                        mainWebView.getSettings().setUserAgentString("");
                        break;

                    case "Custom user agent":
                        // Set the custom user agent.
                        mainWebView.getSettings().setUserAgentString(customUserAgentString);
                        break;

                    default:
                        // Use the selected user agent.
                        mainWebView.getSettings().setUserAgentString(userAgentString);
                        break;
                }

                // Set a transparent background on `urlTextBox`.  We have to use the deprecated `.getDrawable()` until the minimum API >= 21.
                urlAppBarRelativeLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.url_bar_background_transparent));
            }

            // Close `domainsDatabaseHelper`.
            domainsDatabaseHelper.close();

            // Update the privacy icons, but only if `mainMenu` has already been populated.
            if (mainMenu != null) {
                updatePrivacyIcons(true);
            }
        }
    }

    public void findPreviousOnPage(View view) {
        // Go to the previous highlighted phrase on the page.  `false` goes backwards instead of forwards.
        mainWebView.findNext(false);
    }

    public void findNextOnPage(View view) {
        // Go to the next highlighted phrase on the page. `true` goes forwards instead of backwards.
        mainWebView.findNext(true);
    }

    public void closeFindOnPage(View view) {
        // Delete the contents of `find_on_page_edittext`.
        findOnPageEditText.setText(null);

        // Clear the highlighted phrases.
        mainWebView.clearMatches();

        // Hide the Find on Page `RelativeLayout`.
        findOnPageLinearLayout.setVisibility(View.GONE);

        // Show the URL app bar.
        supportAppBar.setVisibility(View.VISIBLE);

        // Hide the keyboard so we can see the webpage.  `0` indicates no additional flags.
        inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);
    }

    private void applyAppSettings() {
        // Get the shared preference values.  `this` references the current context.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Store the values from `sharedPreferences` in variables.
        String homepageString = sharedPreferences.getString("homepage", "https://duckduckgo.com");
        String torHomepageString = sharedPreferences.getString("tor_homepage", "https://3g2upl4pq6kufc4m.onion");
        String torSearchString = sharedPreferences.getString("tor_search", "https://3g2upl4pq6kufc4m.onion/html/?q=");
        String torSearchCustomURLString = sharedPreferences.getString("tor_search_custom_url", "");
        String searchString = sharedPreferences.getString("search", "https://duckduckgo.com/html/?q=");
        String searchCustomURLString = sharedPreferences.getString("search_custom_url", "");
        adBlockerEnabled = sharedPreferences.getBoolean("block_ads", true);
        incognitoModeEnabled = sharedPreferences.getBoolean("incognito_mode", false);
        boolean doNotTrackEnabled = sharedPreferences.getBoolean("do_not_track", false);
        boolean proxyThroughOrbot = sharedPreferences.getBoolean("proxy_through_orbot", false);
        fullScreenBrowsingModeEnabled = sharedPreferences.getBoolean("enable_full_screen_browsing_mode", false);
        hideSystemBarsOnFullscreen = sharedPreferences.getBoolean("hide_system_bars", false);
        translucentNavigationBarOnFullscreen = sharedPreferences.getBoolean("translucent_navigation_bar", true);
        swipeToRefreshEnabled = sharedPreferences.getBoolean("swipe_to_refresh", false);

        // Set the homepage, search, and proxy options.
        if (proxyThroughOrbot) {  // Set the Tor options.
            // Set `torHomepageString` as `homepage`.
            homepage = torHomepageString;

            // If formattedUrlString is null assign the homepage to it.
            if (formattedUrlString == null) {
                formattedUrlString = homepage;
            }

            // Set the search URL.
            if (torSearchString.equals("Custom URL")) {  // Get the custom URL string.
                searchURL = torSearchCustomURLString;
            } else {  // Use the string from the pre-built list.
                searchURL = torSearchString;
            }

            // Set the proxy.  `this` refers to the current activity where an `AlertDialog` might be displayed.
            OrbotProxyHelper.setProxy(getApplicationContext(), this, "localhost", "8118");

            // Display a message to the user if we are waiting on Orbot.
            if (!orbotStatus.equals("ON")) {
                // Set `waitingForOrbot`.
                waitingForOrbot = true;

                // Load a waiting page.  `null` specifies no encoding, which defaults to ASCII.
                mainWebView.loadData(waitingForOrbotHTMLString, "text/html", null);
            }
        } else {  // Set the non-Tor options.
            // Set `homepageString` as `homepage`.
            homepage = homepageString;

            // If formattedUrlString is null assign the homepage to it.
            if (formattedUrlString == null) {
                formattedUrlString = homepage;
            }

            // Set the search URL.
            if (searchString.equals("Custom URL")) {  // Get the custom URL string.
                searchURL = searchCustomURLString;
            } else {  // Use the string from the pre-built list.
                searchURL = searchString;
            }

            // Reset the proxy to default.  The host is `""` and the port is `"0"`.
            OrbotProxyHelper.setProxy(getApplicationContext(), this, "", "0");

            // Reset `waitingForOrbot.
            waitingForOrbot = false;
        }

        // Set swipe to refresh.
        swipeRefreshLayout.setEnabled(swipeToRefreshEnabled);

        // Set Do Not Track status.
        if (doNotTrackEnabled) {
            customHeaders.put("DNT", "1");
        } else {
            customHeaders.remove("DNT");
        }

        // Apply the appropriate full screen mode the `SYSTEM_UI` flags.
        if (fullScreenBrowsingModeEnabled && inFullScreenBrowsingMode) {
            if (hideSystemBarsOnFullscreen) {  // Hide everything.
                // Remove the translucent navigation setting if it is currently flagged.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                // Remove the translucent status bar overlay.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                 * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                 * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically rehides them after they are shown.
                 */
                rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {  // Hide everything except the status and navigation bars.
                // Add the translucent status flag if it is unset.
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                if (translucentNavigationBarOnFullscreen) {
                    // Set the navigation bar to be translucent.
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                } else {
                    // Set the navigation bar to be black.
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                }
            }
        } else {  // Switch to normal viewing mode.
            // Reset `inFullScreenBrowsingMode` to `false`.
            inFullScreenBrowsingMode = false;

            // Show the `appBar` if `findOnPageLinearLayout` is not visible.
            if (findOnPageLinearLayout.getVisibility() == View.GONE) {
                appBar.show();
            }

            // Show the `BannerAd` in the free flavor.
            if (BuildConfig.FLAVOR.contentEquals("free")) {
                // Reload the ad.  Because the screen may have rotated, we need to use `reloadAfterRotate`.
                BannerAd.reloadAfterRotate(adView, getApplicationContext(), getString(R.string.ad_id));

                // Reinitialize the `adView` variable, as the `View` will have been removed and re-added by `BannerAd.reloadAfterRotate()`.
                adView = findViewById(R.id.adview);
            }

            // Remove the translucent navigation bar flag if it is set.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            // Add the translucent status flag if it is unset.  This also resets `drawerLayout's` `View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN`.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Remove any `SYSTEM_UI` flags from `rootCoordinatorLayout`.
            rootCoordinatorLayout.setSystemUiVisibility(0);

            // Constrain `rootCoordinatorLayout` inside the status and navigation bars.
            rootCoordinatorLayout.setFitsSystemWindows(true);
        }
    }

    private void updatePrivacyIcons(boolean runInvalidateOptionsMenu) {
        // Get handles for the icons.
        MenuItem privacyIcon = mainMenu.findItem(R.id.toggleJavaScript);
        MenuItem firstPartyCookiesIcon = mainMenu.findItem(R.id.toggleFirstPartyCookies);
        MenuItem domStorageIcon = mainMenu.findItem(R.id.toggleDomStorage);
        MenuItem formDataIcon = mainMenu.findItem(R.id.toggleSaveFormData);

        // Update `privacyIcon`.
        if (javaScriptEnabled) {  // JavaScript is enabled.
            privacyIcon.setIcon(R.drawable.javascript_enabled);
        } else if (firstPartyCookiesEnabled) {  // JavaScript is disabled but cookies are enabled.
            privacyIcon.setIcon(R.drawable.warning);
        } else {  // All the dangerous features are disabled.
            privacyIcon.setIcon(R.drawable.privacy_mode);
        }

        // Update `firstPartyCookiesIcon`.
        if (firstPartyCookiesEnabled) {  // First-party cookies are enabled.
            firstPartyCookiesIcon.setIcon(R.drawable.cookies_enabled);
        } else {  // First-party cookies are disabled.
            firstPartyCookiesIcon.setIcon(R.drawable.cookies_disabled);
        }

        // Update `domStorageIcon`.
        if (javaScriptEnabled && domStorageEnabled) {  // Both JavaScript and DOM storage are enabled.
            domStorageIcon.setIcon(R.drawable.dom_storage_enabled);
        } else if (javaScriptEnabled) {  // JavaScript is enabled but DOM storage is disabled.
            domStorageIcon.setIcon(R.drawable.dom_storage_disabled);
        } else {  // JavaScript is disabled, so DOM storage is ghosted.
            domStorageIcon.setIcon(R.drawable.dom_storage_ghosted);
        }

        // Update `formDataIcon`.
        if (saveFormDataEnabled) {  // Form data is enabled.
            formDataIcon.setIcon(R.drawable.form_data_enabled);
        } else {  // Form data is disabled.
            formDataIcon.setIcon(R.drawable.form_data_disabled);
        }

        // `invalidateOptionsMenu` calls `onPrepareOptionsMenu()` and redraws the icons in the `AppBar`.  `this` references the current activity.
        if (runInvalidateOptionsMenu) {
            ActivityCompat.invalidateOptionsMenu(this);
        }
    }
}
