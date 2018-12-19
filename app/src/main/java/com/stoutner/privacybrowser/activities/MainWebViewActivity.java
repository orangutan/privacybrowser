/*
 * Copyright © 2015-2018 Soren Stoutner <soren@stoutner.com>.
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
import android.app.DialogFragment;
import android.app.DownloadManager;
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
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
// `ShortcutInfoCompat`, `ShortcutManagerCompat`, and `IconCompat` can be switched to the non-compat version once API >= 26.
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
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
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
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

import com.stoutner.privacybrowser.BuildConfig;
import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.dialogs.AdConsentDialog;
import com.stoutner.privacybrowser.dialogs.CreateBookmarkDialog;
import com.stoutner.privacybrowser.dialogs.CreateBookmarkFolderDialog;
import com.stoutner.privacybrowser.dialogs.CreateHomeScreenShortcutDialog;
import com.stoutner.privacybrowser.dialogs.DownloadImageDialog;
import com.stoutner.privacybrowser.dialogs.DownloadLocationPermissionDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkDialog;
import com.stoutner.privacybrowser.dialogs.EditBookmarkFolderDialog;
import com.stoutner.privacybrowser.dialogs.HttpAuthenticationDialog;
import com.stoutner.privacybrowser.dialogs.PinnedSslCertificateMismatchDialog;
import com.stoutner.privacybrowser.dialogs.UrlHistoryDialog;
import com.stoutner.privacybrowser.dialogs.ViewSslCertificateDialog;
import com.stoutner.privacybrowser.helpers.AdHelper;
import com.stoutner.privacybrowser.helpers.BlockListHelper;
import com.stoutner.privacybrowser.helpers.BookmarksDatabaseHelper;
import com.stoutner.privacybrowser.helpers.DomainsDatabaseHelper;
import com.stoutner.privacybrowser.helpers.OrbotProxyHelper;
import com.stoutner.privacybrowser.dialogs.DownloadFileDialog;
import com.stoutner.privacybrowser.dialogs.SslCertificateErrorDialog;

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
        CreateHomeScreenShortcutDialog.CreateHomeScreenShortcutListener, DownloadFileDialog.DownloadFileListener, DownloadImageDialog.DownloadImageListener,
        DownloadLocationPermissionDialog.DownloadLocationPermissionDialogListener, EditBookmarkDialog.EditBookmarkListener, EditBookmarkFolderDialog.EditBookmarkFolderListener,
        HttpAuthenticationDialog.HttpAuthenticationListener, NavigationView.OnNavigationItemSelectedListener, PinnedSslCertificateMismatchDialog.PinnedSslCertificateMismatchListener,
        SslCertificateErrorDialog.SslCertificateErrorListener, UrlHistoryDialog.UrlHistoryListener {

    // `darkTheme` is public static so it can be accessed from everywhere.
    public static boolean darkTheme;

    // `allowScreenshots` is public static so it can be accessed from everywhere.  It is also used in `onCreate()`.
    public static boolean allowScreenshots;

    // `favoriteIconBitmap` is public static so it can be accessed from `CreateHomeScreenShortcutDialog`, `BookmarksActivity`, `BookmarksDatabaseViewActivity`, `CreateBookmarkDialog`,
    // `CreateBookmarkFolderDialog`, `EditBookmarkDialog`, `EditBookmarkFolderDialog`, `EditBookmarkDatabaseViewDialog`, and `ViewSslCertificateDialog`.  It is also used in `onCreate()`,
    // `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onCreateHomeScreenShortcutCreate()`, `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`, and `applyDomainSettings()`.
    public static Bitmap favoriteIconBitmap;

    // `formattedUrlString` is public static so it can be accessed from `BookmarksActivity`, `CreateBookmarkDialog`, and `AddDomainDialog`.
    // It is also used in `onCreate()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onCreateHomeScreenShortcutCreate()`, `loadUrlFromTextBox()`, and `applyProxyThroughOrbot()`.
    public static String formattedUrlString;

    // `sslCertificate` is public static so it can be accessed from `DomainsActivity`, `DomainsListFragment`, `DomainSettingsFragment`, `PinnedSslCertificateMismatchDialog`,
    // and `ViewSslCertificateDialog`.  It is also used in `onCreate()`.
    public static SslCertificate sslCertificate;

    // `orbotStatus` is public static so it can be accessed from `OrbotProxyHelper`.  It is also used in `onCreate()`, `onResume()`, and `applyProxyThroughOrbot()`.
    public static String orbotStatus;

    // `webViewTitle` is public static so it can be accessed from `CreateBookmarkDialog` and `CreateHomeScreenShortcutDialog`.  It is also used in `onCreate()`.
    public static String webViewTitle;

    // `appliedUserAgentString` is public static so it can be accessed from `ViewSourceActivity`.  It is also used in `applyDomainSettings()`.
    public static String appliedUserAgentString;

    // `reloadOnRestart` is public static so it can be accessed from `SettingsFragment`.  It is also used in `onRestart()`
    public static boolean reloadOnRestart;

    // `reloadUrlOnRestart` is public static so it can be accessed from `SettingsFragment` and `BookmarksActivity`.  It is also used in `onRestart()`.
    public static boolean loadUrlOnRestart;

    // `restartFromBookmarksActivity` is public static so it can be accessed from `BookmarksActivity`.  It is also used in `onRestart()`.
    public static boolean restartFromBookmarksActivity;

    // The block list versions are public static so they can be accessed from `AboutTabFragment`.  They are also used in `onCreate()`.
    public static String easyListVersion;
    public static String easyPrivacyVersion;
    public static String fanboysAnnoyanceVersion;
    public static String fanboysSocialVersion;
    public static String ultraPrivacyVersion;

    // The request items are public static so they can be accessed by `BlockListHelper`, `RequestsArrayAdapter`, and `ViewRequestsDialog`.  They are also used in `onCreate()` and `onPrepareOptionsMenu()`.
    public static List<String[]> resourceRequests;
    public static String[] whiteListResultStringArray;
    private int blockedRequests;
    private int easyListBlockedRequests;
    private int easyPrivacyBlockedRequests;
    private int fanboysAnnoyanceListBlockedRequests;
    private int fanboysSocialBlockingListBlockedRequests;
    private int ultraPrivacyBlockedRequests;
    private int thirdPartyBlockedRequests;

    public final static int REQUEST_DISPOSITION = 0;
    public final static int REQUEST_URL = 1;
    public final static int REQUEST_BLOCKLIST = 2;
    public final static int REQUEST_SUBLIST = 3;
    public final static int REQUEST_BLOCKLIST_ENTRIES = 4;
    public final static int REQUEST_BLOCKLIST_ORIGINAL_ENTRY = 5;

    public final static int REQUEST_DEFAULT = 0;
    public final static int REQUEST_ALLOWED = 1;
    public final static int REQUEST_THIRD_PARTY = 2;
    public final static int REQUEST_BLOCKED = 3;

    public final static int MAIN_WHITELIST = 1;
    public final static int FINAL_WHITELIST = 2;
    public final static int DOMAIN_WHITELIST = 3;
    public final static int DOMAIN_INITIAL_WHITELIST = 4;
    public final static int DOMAIN_FINAL_WHITELIST = 5;
    public final static int THIRD_PARTY_WHITELIST = 6;
    public final static int THIRD_PARTY_DOMAIN_WHITELIST = 7;
    public final static int THIRD_PARTY_DOMAIN_INITIAL_WHITELIST = 8;

    public final static int MAIN_BLACKLIST = 9;
    public final static int INITIAL_BLACKLIST = 10;
    public final static int FINAL_BLACKLIST = 11;
    public final static int DOMAIN_BLACKLIST = 12;
    public final static int DOMAIN_INITIAL_BLACKLIST = 13;
    public final static int DOMAIN_FINAL_BLACKLIST = 14;
    public final static int DOMAIN_REGULAR_EXPRESSION_BLACKLIST = 15;
    public final static int THIRD_PARTY_BLACKLIST = 16;
    public final static int THIRD_PARTY_INITIAL_BLACKLIST = 17;
    public final static int THIRD_PARTY_DOMAIN_BLACKLIST = 18;
    public final static int THIRD_PARTY_DOMAIN_INITIAL_BLACKLIST = 19;
    public final static int THIRD_PARTY_REGULAR_EXPRESSION_BLACKLIST = 20;
    public final static int THIRD_PARTY_DOMAIN_REGULAR_EXPRESSION_BLACKLIST = 21;
    public final static int REGULAR_EXPRESSION_BLACKLIST = 22;

    // `blockAllThirdPartyRequests` is public static so it can be accessed from `RequestsActivity`.
    // It is also used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and `applyAppSettings()`
    public static boolean blockAllThirdPartyRequests;

    // `currentBookmarksFolder` is public static so it can be accessed from `BookmarksActivity`.  It is also used in `onCreate()`, `onBackPressed()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`,
    // `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`, and `loadBookmarksFolder()`.
    public static String currentBookmarksFolder;

    // `domainSettingsDatabaseId` is public static so it can be accessed from `PinnedSslCertificateMismatchDialog`.  It is also used in `onCreate()`, `onOptionsItemSelected()`, and `applyDomainSettings()`.
    public static int domainSettingsDatabaseId;

    // The pinned domain SSL Certificate variables are public static so they can be accessed from `PinnedSslCertificateMismatchDialog`.  They are also used in `onCreate()` and `applyDomainSettings()`.
    public static String pinnedDomainSslIssuedToCNameString;
    public static String pinnedDomainSslIssuedToONameString;
    public static String pinnedDomainSslIssuedToUNameString;
    public static String pinnedDomainSslIssuedByCNameString;
    public static String pinnedDomainSslIssuedByONameString;
    public static String pinnedDomainSslIssuedByUNameString;
    public static Date pinnedDomainSslStartDate;
    public static Date pinnedDomainSslEndDate;

    // The user agent constants are public static so they can be accessed from `SettingsFragment`, `DomainsActivity`, and `DomainSettingsFragment`.
    public final static int UNRECOGNIZED_USER_AGENT = -1;
    public final static int SETTINGS_WEBVIEW_DEFAULT_USER_AGENT = 1;
    public final static int SETTINGS_CUSTOM_USER_AGENT = 12;
    public final static int DOMAINS_SYSTEM_DEFAULT_USER_AGENT = 0;
    public final static int DOMAINS_WEBVIEW_DEFAULT_USER_AGENT = 2;
    public final static int DOMAINS_CUSTOM_USER_AGENT = 13;


    // `appBar` is used in `onCreate()`, `onOptionsItemSelected()`, `closeFindOnPage()`, `applyAppSettings()`, and `applyProxyThroughOrbot()`.
    private ActionBar appBar;

    // `navigatingHistory` is used in `onCreate()`, `onNavigationItemSelected()`, `onSslMismatchBack()`, and `applyDomainSettings()`.
    private boolean navigatingHistory;

    // `favoriteIconDefaultBitmap` is used in `onCreate()` and `applyDomainSettings`.
    private Bitmap favoriteIconDefaultBitmap;

    // `drawerLayout` is used in `onCreate()`, `onNewIntent()`, `onBackPressed()`, and `onRestart()`.
    private DrawerLayout drawerLayout;

    // `rootCoordinatorLayout` is used in `onCreate()` and `applyAppSettings()`.
    private CoordinatorLayout rootCoordinatorLayout;

    // `mainWebView` is used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onRestart()`, `onCreateContextMenu()`, `findPreviousOnPage()`,
    // `findNextOnPage()`, `closeFindOnPage()`, `loadUrlFromTextBox()`, `onSslMismatchBack()`, and `applyProxyThroughOrbot()`.
    private WebView mainWebView;

    // `fullScreenVideoFrameLayout` is used in `onCreate()` and `onConfigurationChanged()`.
    private FrameLayout fullScreenVideoFrameLayout;

    // `swipeRefreshLayout` is used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and `onRestart()`.
    private SwipeRefreshLayout swipeRefreshLayout;

    // `urlAppBarRelativeLayout` is used in `onCreate()` and `applyDomainSettings()`.
    private RelativeLayout urlAppBarRelativeLayout;

    // `favoriteIconImageView` is used in `onCreate()` and `applyDomainSettings()`
    private ImageView favoriteIconImageView;

    // `cookieManager` is used in `onCreate()`, `onOptionsItemSelected()`, and `onNavigationItemSelected()`, `loadUrlFromTextBox()`, `onDownloadImage()`, `onDownloadFile()`, and `onRestart()`.
    private CookieManager cookieManager;

    // `customHeader` is used in `onCreate()`, `onOptionsItemSelected()`, `onCreateContextMenu()`, and `loadUrl()`.
    private final Map<String, String> customHeaders = new HashMap<>();

    // `javaScriptEnabled` is also used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, `applyDomainSettings()`, and `updatePrivacyIcons()`.
    private boolean javaScriptEnabled;

    // `firstPartyCookiesEnabled` is used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, `onDownloadImage()`, `onDownloadFile()`, and `applyDomainSettings()`.
    private boolean firstPartyCookiesEnabled;

    // `thirdPartyCookiesEnabled` used in `onCreate()`, `onPrepareOptionsMenu()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and `applyDomainSettings()`.
    private boolean thirdPartyCookiesEnabled;

    // `domStorageEnabled` is used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and `applyDomainSettings()`.
    private boolean domStorageEnabled;

    // `saveFormDataEnabled` is used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and `applyDomainSettings()`.  It can be removed once the minimum API >= 26.
    private boolean saveFormDataEnabled;

    // `nightMode` is used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and  `applyDomainSettings()`.
    private boolean nightMode;

    // 'homepage' is used in `onCreate()`, `onNavigationItemSelected()`, and `applyProxyThroughOrbot()`.
    private String homepage;

    // `searchURL` is used in `loadURLFromTextBox()` and `applyProxyThroughOrbot()`.
    private String searchURL;

    // `mainMenu` is used in `onCreateOptionsMenu()` and `updatePrivacyIcons()`.
    private Menu mainMenu;

    // `refreshMenuItem` is used in `onCreate()` and `onCreateOptionsMenu()`.
    private MenuItem refreshMenuItem;

    // The blocklist menu items are used in `onCreate()`, `onCreateOptionsMenu()`, and `onPrepareOptionsMenu()`.
    private MenuItem blocklistsMenuItem;
    private MenuItem easyListMenuItem;
    private MenuItem easyPrivacyMenuItem;
    private MenuItem fanboysAnnoyanceListMenuItem;
    private MenuItem fanboysSocialBlockingListMenuItem;
    private MenuItem ultraPrivacyMenuItem;
    private MenuItem blockAllThirdPartyRequestsMenuItem;

    // The blocklist variables are used in `onCreate()`, `onPrepareOptionsMenu()`, `onOptionsItemSelected()`, and `applyAppSettings()`.
    private boolean easyListEnabled;
    private boolean easyPrivacyEnabled;
    private boolean fanboysAnnoyanceListEnabled;
    private boolean fanboysSocialBlockingListEnabled;
    private boolean ultraPrivacyEnabled;

    // `webViewDefaultUserAgent` is used in `onCreate()` and `onPrepareOptionsMenu()`.
    private String webViewDefaultUserAgent;

    // `defaultCustomUserAgentString` is used in `onPrepareOptionsMenu()` and `applyDomainSettings()`.
    private String defaultCustomUserAgentString;

    // `privacyBrowserRuntime` is used in `onCreate()`, `onOptionsItemSelected()`, and `applyAppSettings()`.
    private Runtime privacyBrowserRuntime;

    // `proxyThroughOrbot` is used in `onRestart()`, `onOptionsItemSelected()`, `applyAppSettings()`, and `applyProxyThroughOrbot()`.
    private boolean proxyThroughOrbot;

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

    // `reapplyDomainSettingsOnRestart` is used in `onCreate()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onRestart()`, and `onAddDomain()`, .
    private boolean reapplyDomainSettingsOnRestart;

    // `reapplyAppSettingsOnRestart` is used in `onNavigationItemSelected()` and `onRestart()`.
    private boolean reapplyAppSettingsOnRestart;

    // `displayingFullScreenVideo` is used in `onCreate()` and `onResume()`.
    private boolean displayingFullScreenVideo;

    // `downloadWithExternalApp` is used in `onCreate()`, `onCreateContextMenu()`, and `applyDomainSettings()`.
    private boolean downloadWithExternalApp;

    // `currentDomainName` is used in `onCreate()`, `onOptionsItemSelected()`, `onNavigationItemSelected()`, `onAddDomain()`, and `applyDomainSettings()`.
    private String currentDomainName;

    // `ignorePinnedSslCertificateForDomain` is used in `onCreate()`, `onSslMismatchProceed()`, and `applyDomainSettings()`.
    private boolean ignorePinnedSslCertificate;

    // `orbotStatusBroadcastReceiver` is used in `onCreate()` and `onDestroy()`.
    private BroadcastReceiver orbotStatusBroadcastReceiver;

    // `waitingForOrbot` is used in `onCreate()`, `onResume()`, and `applyProxyThroughOrbot()`.
    private boolean waitingForOrbot;

    // `domainSettingsApplied` is used in `prepareOptionsMenu()` and `applyDomainSettings()`.
    private boolean domainSettingsApplied;

    // `domainSettingsJavaScriptEnabled` is used in `onOptionsItemSelected()` and `applyDomainSettings()`.
    private Boolean domainSettingsJavaScriptEnabled;

    // `waitingForOrbotHtmlString` is used in `onCreate()` and `applyProxyThroughOrbot()`.
    private String waitingForOrbotHtmlString;

    // `privateDataDirectoryString` is used in `onCreate()`, `onOptionsItemSelected()`, and `onNavigationItemSelected()`.
    private String privateDataDirectoryString;

    // `findOnPageLinearLayout` is used in `onCreate()`, `onOptionsItemSelected()`, and `closeFindOnPage()`.
    private LinearLayout findOnPageLinearLayout;

    // `findOnPageEditText` is used in `onCreate()`, `onOptionsItemSelected()`, and `closeFindOnPage()`.
    private EditText findOnPageEditText;

    // `displayAdditionalAppBarIcons` is used in `onCreate()` and `onCreateOptionsMenu()`.
    private boolean displayAdditionalAppBarIcons;

    // `drawerToggle` is used in `onCreate()`, `onPostCreate()`, `onConfigurationChanged()`, `onNewIntent()`, and `onNavigationItemSelected()`.
    private ActionBarDrawerToggle drawerToggle;

    // `supportAppBar` is used in `onCreate()`, `onOptionsItemSelected()`, and `closeFindOnPage()`.
    private Toolbar supportAppBar;

    // `urlTextBox` is used in `onCreate()`, `onOptionsItemSelected()`, `loadUrlFromTextBox()`, `loadUrl()`, and `highlightUrlText()`.
    private EditText urlTextBox;

    // The color spans are used in `onCreate()` and `highlightUrlText()`.
    private ForegroundColorSpan redColorSpan;
    private ForegroundColorSpan initialGrayColorSpan;
    private ForegroundColorSpan finalGrayColorSpan;

    // `sslErrorHandler` is used in `onCreate()`, `onSslErrorCancel()`, and `onSslErrorProceed`.
    private SslErrorHandler sslErrorHandler;

    // `httpAuthHandler` is used in `onCreate()`, `onHttpAuthenticationCancel()`, and `onHttpAuthenticationProceed()`.
    private static HttpAuthHandler httpAuthHandler;

    // `inputMethodManager` is used in `onOptionsItemSelected()`, `loadUrlFromTextBox()`, and `closeFindOnPage()`.
    private InputMethodManager inputMethodManager;

    // `mainWebViewRelativeLayout` is used in `onCreate()` and `onNavigationItemSelected()`.
    private RelativeLayout mainWebViewRelativeLayout;

    // `urlIsLoading` is used in `onCreate()`, `onCreateOptionsMenu()`, `loadUrl()`, and `applyDomainSettings()`.
    private boolean urlIsLoading;

    // `pinnedDomainSslCertificate` is used in `onCreate()` and `applyDomainSettings()`.
    private boolean pinnedDomainSslCertificate;

    // `bookmarksDatabaseHelper` is used in `onCreate()`, `onDestroy`, `onOptionsItemSelected()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`,
    // and `loadBookmarksFolder()`.
    private BookmarksDatabaseHelper bookmarksDatabaseHelper;

    // `bookmarksListView` is used in `onCreate()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, and `loadBookmarksFolder()`.
    private ListView bookmarksListView;

    // `bookmarksTitleTextView` is used in `onCreate()` and `loadBookmarksFolder()`.
    private TextView bookmarksTitleTextView;

    // `bookmarksCursor` is used in `onDestroy()`, `onOptionsItemSelected()`, `onCreateBookmark()`, `onCreateBookmarkFolder()`, `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`, and `loadBookmarksFolder()`.
    private Cursor bookmarksCursor;

    // `bookmarksCursorAdapter` is used in `onCreateBookmark()`, `onCreateBookmarkFolder()` `onSaveEditBookmark()`, `onSaveEditBookmarkFolder()`, and `loadBookmarksFolder()`.
    private CursorAdapter bookmarksCursorAdapter;

    // `oldFolderNameString` is used in `onCreate()` and `onSaveEditBookmarkFolder()`.
    private String oldFolderNameString;

    // `fileChooserCallback` is used in `onCreate()` and `onActivityResult()`.
    private ValueCallback<Uri[]> fileChooserCallback;

    // The download strings are used in `onCreate()` and `onRequestPermissionResult()`.
    private String downloadUrl;
    private String downloadContentDisposition;
    private long downloadContentLength;

    // `downloadImageUrl` is used in `onCreateContextMenu()` and `onRequestPermissionResult()`.
    private String downloadImageUrl;

    // The user agent variables are used in `onCreate()` and `applyDomainSettings()`.
    private ArrayAdapter<CharSequence> userAgentNamesArray;
    private String[] userAgentDataArray;

    // The request codes are used in `onCreate()`, `onCreateContextMenu()`, `onCloseDownloadLocationPermissionDialog()`, and `onRequestPermissionResult()`.
    private final int DOWNLOAD_FILE_REQUEST_CODE = 1;
    private final int DOWNLOAD_IMAGE_REQUEST_CODE = 2;

    @Override
    // Remove Android Studio's warning about the dangers of using SetJavaScriptEnabled.  The whole premise of Privacy Browser is built around an understanding of these dangers.
    // Also, remove the warning about needing to override `performClick()` when using an `OnTouchListener` with `WebView`.
    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    // Remove Android Studio's warning about deprecations.  We have to use the deprecated `getColor()` until API >= 23.
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the theme and screenshot preferences.
        darkTheme = sharedPreferences.getBoolean("dark_theme", false);
        allowScreenshots = sharedPreferences.getBoolean("allow_screenshots", false);

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
        setContentView(R.layout.main_drawerlayout);

        // Get a handle for `inputMethodManager`.
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // `SupportActionBar` from `android.support.v7.app.ActionBar` must be used until the minimum API is >= 21.
        supportAppBar = findViewById(R.id.app_bar);
        setSupportActionBar(supportAppBar);
        appBar = getSupportActionBar();

        // This is needed to get rid of the Android Studio warning that `appBar` might be null.
        assert appBar != null;

        // Add the custom `url_app_bar` layout, which shows the favorite icon and the URL text bar.
        appBar.setCustomView(R.layout.url_app_bar);
        appBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        // Initialize the foreground color spans for highlighting the URLs.  We have to use the deprecated `getColor()` until API >= 23.
        redColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red_a700));
        initialGrayColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.gray_500));
        finalGrayColorSpan = new ForegroundColorSpan(getResources().getColor(R.color.gray_500));

        // Get a handle for `urlTextBox`.
        urlTextBox = findViewById(R.id.url_edittext);

        // Remove the formatting from `urlTextBar` when the user is editing the text.
        urlTextBox.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            if (hasFocus) {  // The user is editing the URL text box.
                // Remove the highlighting.
                urlTextBox.getText().removeSpan(redColorSpan);
                urlTextBox.getText().removeSpan(initialGrayColorSpan);
                urlTextBox.getText().removeSpan(finalGrayColorSpan);
            } else {  // The user has stopped editing the URL text box.
                // Move to the beginning of the string.
                urlTextBox.setSelection(0);

                // Reapply the highlighting.
                highlightUrlText();
            }
        });

        // Set the go button on the keyboard to load the URL in `urlTextBox`.
        urlTextBox.setOnKeyListener((View v, int keyCode, KeyEvent event) -> {
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
        });

        // Set `waitingForOrbotHTMLString`.
        waitingForOrbotHtmlString = "<html><body><br/><center><h1>" + getString(R.string.waiting_for_orbot) + "</h1></center></body></html>";

        // Initialize `currentDomainName`, `orbotStatus`, and `waitingForOrbot`.
        currentDomainName = "";
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
        drawerLayout = findViewById(R.id.drawerlayout);
        rootCoordinatorLayout = findViewById(R.id.root_coordinatorlayout);
        bookmarksListView = findViewById(R.id.bookmarks_drawer_listview);
        bookmarksTitleTextView = findViewById(R.id.bookmarks_title_textview);
        FloatingActionButton launchBookmarksActivityFab = findViewById(R.id.launch_bookmarks_activity_fab);
        FloatingActionButton createBookmarkFolderFab = findViewById(R.id.create_bookmark_folder_fab);
        FloatingActionButton createBookmarkFab = findViewById(R.id.create_bookmark_fab);
        mainWebViewRelativeLayout = findViewById(R.id.main_webview_relativelayout);
        mainWebView = findViewById(R.id.main_webview);
        findOnPageLinearLayout = findViewById(R.id.find_on_page_linearlayout);
        findOnPageEditText = findViewById(R.id.find_on_page_edittext);
        fullScreenVideoFrameLayout = findViewById(R.id.full_screen_video_framelayout);
        urlAppBarRelativeLayout = findViewById(R.id.url_app_bar_relativelayout);
        favoriteIconImageView = findViewById(R.id.favorite_icon);

        // Set the bookmarks drawer resources according to the theme.  This can't be done in the layout due to compatibility issues with the `DrawerLayout` support widget.
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
            // Create an intent to launch the bookmarks activity.
            Intent bookmarksIntent = new Intent(getApplicationContext(), BookmarksActivity.class);

            // Include the current folder with the `Intent`.
            bookmarksIntent.putExtra("Current Folder", currentBookmarksFolder);

            // Make it so.
            startActivity(bookmarksIntent);
        });

        // Set the create new bookmark folder FAB to display an alert dialog.
        createBookmarkFolderFab.setOnClickListener(v -> {
            // Show the `CreateBookmarkFolderDialog` `AlertDialog` and name the instance `@string/create_folder`.
            AppCompatDialogFragment createBookmarkFolderDialog = new CreateBookmarkFolderDialog();
            createBookmarkFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.create_folder));
        });

        // Set the create new bookmark FAB to display an alert dialog.
        createBookmarkFab.setOnClickListener(view -> {
            // Show the `CreateBookmarkDialog` `AlertDialog` and name the instance `@string/create_bookmark`.
            AppCompatDialogFragment createBookmarkDialog = new CreateBookmarkDialog();
            createBookmarkDialog.show(getSupportFragmentManager(), getResources().getString(R.string.create_bookmark));
        });

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

                        // Hide the banner ad in the free flavor.
                        if (BuildConfig.FLAVOR.contentEquals("free")) {
                            // The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
                            AdHelper.hideAd(findViewById(R.id.adview));
                        }

                        // Modify the system bars.
                        if (hideSystemBarsOnFullscreen) {  // Hide everything.
                            // Remove the translucent overlays.
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                            // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                            drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                            /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                             * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                             * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
                             */
                            rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                            // Set `rootCoordinatorLayout` to fill the whole screen.
                            rootCoordinatorLayout.setFitsSystemWindows(false);
                        } else {  // Hide everything except the status and navigation bars.
                            // Set `rootCoordinatorLayout` to fit under the status and navigation bars.
                            rootCoordinatorLayout.setFitsSystemWindows(false);

                            // There is an Android Support Library bug that causes a scrim to print on the right side of the `Drawer Layout` when the navigation bar is displayed on the right of the screen.
                            if (translucentNavigationBarOnFullscreen) {
                                // Set the navigation bar to be translucent.
                                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                            }
                        }
                    } else {  // Switch to normal viewing mode.
                        // Show the `appBar`.
                        appBar.show();

                        // Show the `BannerAd` in the free flavor.
                        if (BuildConfig.FLAVOR.contentEquals("free")) {
                            // Reload the ad.  The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
                            AdHelper.loadAd(findViewById(R.id.adview), getApplicationContext(), getString(R.string.ad_unit_id));
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
        mainWebView.setOnTouchListener((View v, MotionEvent event) -> {
            // Call `performClick()` on the view, which is required for accessibility.
            v.performClick();

            // Send the `event` to `gestureDetector`.
            return gestureDetector.onTouchEvent(event);
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

                    // Build the match string.
                    String matchString = activeMatch + "/" + numberOfMatches;

                    // Set `findOnPageCountTextView`.
                    findOnPageCountTextView.setText(matchString);
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
        findOnPageEditText.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {  // The `enter` key was pressed.
                // Hide the soft keyboard.  `0` indicates no additional flags.
                inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);

                // Consume the event.
                return true;
            } else {  // A different key was pressed.
                // Do not consume the event.
                return false;
            }
        });

        // Implement swipe to refresh.
        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(() -> mainWebView.reload());

        // Set the swipe to refresh color according to the theme.
        if (darkTheme) {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_600);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.gray_850);
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue_700);
        }

        // `DrawerTitle` identifies the `DrawerLayouts` in accessibility mode.
        drawerLayout.setDrawerTitle(GravityCompat.START, getString(R.string.navigation_drawer));
        drawerLayout.setDrawerTitle(GravityCompat.END, getString(R.string.bookmarks));

        // Listen for touches on the navigation menu.
        final NavigationView navigationView = findViewById(R.id.navigationview);
        navigationView.setNavigationItemSelectedListener(this);

        // Get handles for `navigationMenu` and the back and forward menu items.  The menu is zero-based, so items 1, 2, and 3 are the second, third, and fourth entries in the menu.
        final Menu navigationMenu = navigationView.getMenu();
        final MenuItem navigationBackMenuItem = navigationMenu.getItem(1);
        final MenuItem navigationForwardMenuItem = navigationMenu.getItem(2);
        final MenuItem navigationHistoryMenuItem = navigationMenu.getItem(3);
        final MenuItem navigationRequestsMenuItem = navigationMenu.getItem(4);

        // Initialize the bookmarks database helper.  `this` specifies the context.  The two `nulls` do not specify the database name or a `CursorFactory`.
        // The `0` specifies a database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
        bookmarksDatabaseHelper = new BookmarksDatabaseHelper(this, null, null, 0);

        // Initialize `currentBookmarksFolder`.  `""` is the home folder in the database.
        currentBookmarksFolder = "";

        // Load the home folder, which is `""` in the database.
        loadBookmarksFolder();

        bookmarksListView.setOnItemClickListener((parent, view, position, id) -> {
            // Convert the id from long to int to match the format of the bookmarks database.
            int databaseID = (int) id;

            // Get the bookmark cursor for this ID and move it to the first row.
            Cursor bookmarkCursor = bookmarksDatabaseHelper.getBookmarkCursor(databaseID);
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
                AppCompatDialogFragment editFolderDialog = EditBookmarkFolderDialog.folderDatabaseId(databaseId);
                editFolderDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_folder));
            } else {
                // Show the edit bookmark `AlertDialog` and name the instance `@string/edit_bookmark`.
                AppCompatDialogFragment editBookmarkDialog = EditBookmarkDialog.bookmarkDatabaseId(databaseId);
                editBookmarkDialog.show(getSupportFragmentManager(), getResources().getString(R.string.edit_bookmark));
            }

            // Consume the event.
            return true;
        });

        // The drawer listener is used to update the navigation menu.
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
                    // Update the back, forward, history, and requests menu items.
                    navigationBackMenuItem.setEnabled(mainWebView.canGoBack());
                    navigationForwardMenuItem.setEnabled(mainWebView.canGoForward());
                    navigationHistoryMenuItem.setEnabled((mainWebView.canGoBack() || mainWebView.canGoForward()));
                    navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);

                    // Hide the keyboard (if displayed).
                    inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);

                    // Clear the focus from from the URL text box and the WebView.  This removes any text selection markers and context menus, which otherwise draw above the open drawers.
                    urlTextBox.clearFocus();
                    mainWebView.clearFocus();
                }
            }
        });

        // drawerToggle creates the hamburger icon at the start of the AppBar.
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, supportAppBar, R.string.open_navigation_drawer, R.string.close_navigation_drawer);

        // Get a handle for the progress bar.
        final ProgressBar progressBar = findViewById(R.id.progress_bar);

        mainWebView.setWebChromeClient(new WebChromeClient() {
            // Update the progress bar when a page is loading.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // Inject the night mode CSS if night mode is enabled.
                if (nightMode) {
                    // `background-color: #212121` sets the background to be dark gray.  `color: #BDBDBD` sets the text color to be light gray.  `box-shadow: none` removes a lower underline on links
                    // used by WordPress.  `text-decoration: none` removes all text underlines.  `text-shadow: none` removes text shadows, which usually have a hard coded color.
                    // `border: none` removes all borders, which can also be used to underline text.
                    // `a {color: #1565C0}` sets links to be a dark blue.  `!important` takes precedent over any existing sub-settings.
                    mainWebView.evaluateJavascript("(function() {var parent = document.getElementsByTagName('head').item(0); var style = document.createElement('style'); style.type = 'text/css'; " +
                            "style.innerHTML = '* {background-color: #212121 !important; color: #BDBDBD !important; box-shadow: none !important; text-decoration: none !important;" +
                            "text-shadow: none !important; border: none !important;} a {color: #1565C0 !important;}'; parent.appendChild(style)})()", value -> {
                                // Initialize a handler to display `mainWebView`.
                                Handler displayWebViewHandler = new Handler();

                                // Setup a runnable to display `mainWebView` after a delay to allow the CSS to be applied.
                                Runnable displayWebViewRunnable = () -> {
                                    // Only display `mainWebView` if the progress bar is one.  This prevents the display of the `WebView` while it is still loading.
                                    if (progressBar.getVisibility() == View.GONE) {
                                        mainWebView.setVisibility(View.VISIBLE);
                                    }
                                };

                                // Displaying of `mainWebView` after 500 milliseconds.
                                displayWebViewHandler.postDelayed(displayWebViewRunnable, 500);
                            });
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

                    // Display `mainWebView` if night mode is disabled.
                    // Because of a race condition between `applyDomainSettings` and `onPageStarted`, when night mode is set by domain settings the `WebView` may be hidden even if night mode is not
                    // currently enabled.
                    if (!nightMode) {
                        mainWebView.setVisibility(View.VISIBLE);
                    }

                    //Stop the swipe to refresh indicator if it is running
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

            // Enter full screen video.
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                // Set the full screen video flag.
                displayingFullScreenVideo = true;

                // Pause the ad if this is the free flavor.
                if (BuildConfig.FLAVOR.contentEquals("free")) {
                    // The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
                    AdHelper.pauseAd(findViewById(R.id.adview));
                }

                // Remove the translucent overlays.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                 * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                 * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
                 */
                rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                // Set `rootCoordinatorLayout` to fill the entire screen.
                rootCoordinatorLayout.setFitsSystemWindows(false);

                // Disable the sliding drawers.
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                // Add `view` to `fullScreenVideoFrameLayout` and display it on the screen.
                fullScreenVideoFrameLayout.addView(view);
                fullScreenVideoFrameLayout.setVisibility(View.VISIBLE);
            }

            // Exit full screen video.
            @Override
            public void onHideCustomView() {
                // Unset the full screen video flag.
                displayingFullScreenVideo = false;

                // Hide `fullScreenVideoFrameLayout`.
                fullScreenVideoFrameLayout.removeAllViews();
                fullScreenVideoFrameLayout.setVisibility(View.GONE);

                // Enable the sliding drawers.
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                // Apply the appropriate full screen mode the `SYSTEM_UI` flags.
                if (fullScreenBrowsingModeEnabled && inFullScreenBrowsingMode) {  // Privacy Browser is currently in full screen browsing mode.
                    if (hideSystemBarsOnFullscreen) {  // Hide everything.
                        // Remove the translucent navigation setting if it is currently flagged.
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                        // Remove the translucent status bar overlay.
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                        drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                        /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                         * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                         * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
                         */
                        rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    } else {  // Hide everything except the status and navigation bars.
                        // Remove any `SYSTEM_UI` flags from `rootCoordinatorLayout`.
                        rootCoordinatorLayout.setSystemUiVisibility(0);

                        // Add the translucent status flag if it is unset.
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        if (translucentNavigationBarOnFullscreen) {
                            // Set the navigation bar to be translucent.  This also resets `drawerLayout's` `View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN`.
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        } else {
                            // Set the navigation bar to be black.
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        }
                    }
                } else {  // Switch to normal viewing mode.
                    // Show the `appBar` if `findOnPageLinearLayout` is not visible.
                    if (findOnPageLinearLayout.getVisibility() == View.GONE) {
                        appBar.show();
                    }

                    // Show the `BannerAd` in the free flavor.
                    if (BuildConfig.FLAVOR.contentEquals("free")) {
                        // Initialize the ad.  The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
                        AdHelper.initializeAds(findViewById(R.id.adview), getApplicationContext(), getFragmentManager(), getString(R.string.google_app_id), getString(R.string.ad_unit_id));
                    }

                    // Remove any `SYSTEM_UI` flags from `rootCoordinatorLayout`.
                    rootCoordinatorLayout.setSystemUiVisibility(0);

                    // Remove the translucent navigation bar flag if it is set.
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                    // Add the translucent status flag if it is unset.  This also resets `drawerLayout's` `View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN`.
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                    // Constrain `rootCoordinatorLayout` inside the status and navigation bars.
                    rootCoordinatorLayout.setFitsSystemWindows(true);
                }

                // Show the ad if this is the free flavor.
                if (BuildConfig.FLAVOR.contentEquals("free")) {
                    // Reload the ad.  The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
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

        // Register `mainWebView` for a context menu.  This is used to see link targets and download images.
        registerForContextMenu(mainWebView);

        // Allow the downloading of files.
        mainWebView.setDownloadListener((String url, String userAgent, String contentDisposition, String mimetype, long contentLength) -> {
            // Check if the download should be processed by an external app.
            if (downloadWithExternalApp) {  // Download with an external app.
                openUrlWithExternalApp(url);
            } else {  // Download with Android's download manager.
                // Check to see if the WRITE_EXTERNAL_STORAGE permission has already been granted.
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {  // The storage permission has not been granted.
                    // The WRITE_EXTERNAL_STORAGE permission needs to be requested.

                    // Store the variables for future use by `onRequestPermissionsResult()`.
                    downloadUrl = url;
                    downloadContentDisposition = contentDisposition;
                    downloadContentLength = contentLength;

                    // Show a dialog if the user has previously denied the permission.
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {  // Show a dialog explaining the request first.
                        // Instantiate the download location permission alert dialog and set the download type to DOWNLOAD_FILE.
                        DialogFragment downloadLocationPermissionDialogFragment = DownloadLocationPermissionDialog.downloadType(DownloadLocationPermissionDialog.DOWNLOAD_FILE);

                        // Show the download location permission alert dialog.  The permission will be requested when the the dialog is closed.
                        downloadLocationPermissionDialogFragment.show(getFragmentManager(), getString(R.string.download_location));
                    } else {  // Show the permission request directly.
                        // Request the permission.  The download dialog will be launched by `onRequestPermissionResult()`.
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_FILE_REQUEST_CODE);
                    }
                } else {  // The storage permission has already been granted.
                    // Get a handle for the download file alert dialog.
                    AppCompatDialogFragment downloadFileDialogFragment = DownloadFileDialog.fromUrl(url, contentDisposition, contentLength);

                    // Show the download file alert dialog.
                    downloadFileDialogFragment.show(getSupportFragmentManager(), getString(R.string.download));
                }
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

        // Explicitly disable geolocation.
        mainWebView.getSettings().setGeolocationEnabled(false);

        // Initialize cookieManager.
        cookieManager = CookieManager.getInstance();

        // Replace the header that `WebView` creates for `X-Requested-With` with a null value.  The default value is the application ID (com.stoutner.privacybrowser.standard).
        customHeaders.put("X-Requested-With", "");

        // Initialize the default preference values the first time the program is run.  `false` keeps this command from resetting any current preferences back to default.
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
        privateDataDirectoryString = getApplicationInfo().dataDir;
        // `dataDir` will vary, but will be something like `/data/user/0/com.stoutner.privacybrowser.standard`, which links to `/data/data/com.stoutner.privacybrowser.standard`.

        // Initialize `inFullScreenBrowsingMode`, which is always false at this point because Privacy Browser never starts in full screen browsing mode.
        inFullScreenBrowsingMode = false;

        // Initialize the privacy settings variables.
        javaScriptEnabled = false;
        firstPartyCookiesEnabled = false;
        thirdPartyCookiesEnabled = false;
        domStorageEnabled = false;
        saveFormDataEnabled = false;  // Form data can be removed once the minimum API >= 26.
        nightMode = false;

        // Store the default user agent.
        webViewDefaultUserAgent = mainWebView.getSettings().getUserAgentString();

        // Initialize the WebView title.
        webViewTitle = getString(R.string.no_title);

        // Initialize the favorite icon bitmap.  `ContextCompat` must be used until API >= 21.
        Drawable favoriteIconDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.world);
        BitmapDrawable favoriteIconBitmapDrawable = (BitmapDrawable) favoriteIconDrawable;
        assert favoriteIconBitmapDrawable != null;
        favoriteIconDefaultBitmap = favoriteIconBitmapDrawable.getBitmap();

        // If the favorite icon is null, load the default.
        if (favoriteIconBitmap == null) {
            favoriteIconBitmap = favoriteIconDefaultBitmap;
        }

        // Initialize the user agent array adapter and string array.
        userAgentNamesArray = ArrayAdapter.createFromResource(this, R.array.user_agent_names, R.layout.spinner_item);
        userAgentDataArray = getResources().getStringArray(R.array.user_agent_data);

        // Apply the app settings from the shared preferences.
        applyAppSettings();

        // Instantiate the block list helper.
        BlockListHelper blockListHelper = new BlockListHelper();

        // Initialize the list of resource requests.
        resourceRequests = new ArrayList<>();

        // Parse the block lists.
        final ArrayList<List<String[]>> easyList = blockListHelper.parseBlockList(getAssets(), "blocklists/easylist.txt");
        final ArrayList<List<String[]>> easyPrivacy = blockListHelper.parseBlockList(getAssets(), "blocklists/easyprivacy.txt");
        final ArrayList<List<String[]>> fanboysAnnoyanceList = blockListHelper.parseBlockList(getAssets(), "blocklists/fanboy-annoyance.txt");
        final ArrayList<List<String[]>> fanboysSocialList = blockListHelper.parseBlockList(getAssets(), "blocklists/fanboy-social.txt");
        final ArrayList<List<String[]>> ultraPrivacy = blockListHelper.parseBlockList(getAssets(), "blocklists/ultraprivacy.txt");

        // Store the list versions.
        easyListVersion = easyList.get(0).get(0)[0];
        easyPrivacyVersion = easyPrivacy.get(0).get(0)[0];
        fanboysAnnoyanceVersion = fanboysAnnoyanceList.get(0).get(0)[0];
        fanboysSocialVersion = fanboysSocialList.get(0).get(0)[0];
        ultraPrivacyVersion = ultraPrivacy.get(0).get(0)[0];

        // Get a handle for the activity.  This is used to update the requests counter while the navigation menu is open.
        Activity activity = this;

        mainWebView.setWebViewClient(new WebViewClient() {
            // `shouldOverrideUrlLoading` makes this `WebView` the default handler for URLs inside the app, so that links are not kicked out to other apps.
            // The deprecated `shouldOverrideUrlLoading` must be used until API >= 24.
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http")) {  // Load the URL in Privacy Browser.
                    // Reset the formatted URL string so the page will load correctly if blocking of third-party requests is enabled.
                    formattedUrlString = "";

                    // Apply the domain settings for the new URL.  `applyDomainSettings` doesn't do anything if the domain has not changed.
                    boolean userAgentChanged = applyDomainSettings(url, true, false);

                    // Check if the user agent has changed.
                    if (userAgentChanged) {
                        // Manually load the URL.  The changing of the user agent will cause WebView to reload the previous URL.
                        mainWebView.loadUrl(url, customHeaders);

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
                        Snackbar.make(mainWebView, getString(R.string.unrecognized_url) + "  " + url, Snackbar.LENGTH_SHORT).show();
                    }

                    // Returning true indicates Privacy Browser is handling the URL by creating an intent.
                    return true;
                }
            }

            // Check requests against the block lists.  The deprecated `shouldInterceptRequest()` must be used until minimum API >= 21.
            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url){
                // Create an empty web resource response to be used if the resource request is blocked.
                WebResourceResponse emptyWebResourceResponse = new WebResourceResponse("text/plain", "utf8", new ByteArrayInputStream("".getBytes()));

                // Reset the whitelist results tracker.
                whiteListResultStringArray = null;

                // Initialize the third party request tracker.
                boolean isThirdPartyRequest = false;

                // Initialize the current domain string.
                String currentDomain = "";

                // Nobody is happy when comparing null strings.
                if (!(formattedUrlString == null) && !(url == null)) {
                    // Get the domain strings to URIs.
                    Uri currentDomainUri = Uri.parse(formattedUrlString);
                    Uri requestDomainUri = Uri.parse(url);

                    // Get the domain host names.
                    String currentBaseDomain = currentDomainUri.getHost();
                    String requestBaseDomain = requestDomainUri.getHost();

                    // Update the current domain variable.
                    currentDomain = currentBaseDomain;

                    // Only compare the current base domain and the request base domain if neither is null.
                    if (!(currentBaseDomain == null) && !(requestBaseDomain == null)) {
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

                // Block third-party requests if enabled.
                if (isThirdPartyRequest && blockAllThirdPartyRequests) {
                    // Increment the blocked requests counters.
                    blockedRequests++;
                    thirdPartyBlockedRequests++;

                    // Update the titles of the blocklist menu items.  This must be run from the UI thread.
                    activity.runOnUiThread(() -> {
                        navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                        blocklistsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                        blockAllThirdPartyRequestsMenuItem.setTitle(thirdPartyBlockedRequests + " - " + getString(R.string.block_all_third_party_requests));
                    });

                    // Add the request to the log.
                    resourceRequests.add(new String[]{String.valueOf(REQUEST_THIRD_PARTY), url});

                    // Return an empty web resource response.
                    return emptyWebResourceResponse;
                }

                // Check UltraPrivacy if it is enabled.
                if (ultraPrivacyEnabled) {
                    if (blockListHelper.isBlocked(currentDomain, url, isThirdPartyRequest, ultraPrivacy)) {
                        // Increment the blocked requests counters.
                        blockedRequests++;
                        ultraPrivacyBlockedRequests++;

                        // Update the titles of the blocklist menu items.  This must be run from the UI thread.
                        activity.runOnUiThread(() -> {
                            navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            blocklistsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            ultraPrivacyMenuItem.setTitle(ultraPrivacyBlockedRequests + " - " + getString(R.string.ultraprivacy));
                        });

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    }

                    // If the whitelist result is not null, the request has been allowed by UltraPrivacy.
                    if (whiteListResultStringArray != null) {
                        // Add a whitelist entry to the resource requests array.
                        resourceRequests.add(whiteListResultStringArray);

                        // The resource request has been allowed by UltraPrivacy.  `return null` loads the requested resource.
                        return null;
                    }
                }

                // Check EasyList if it is enabled.
                if (easyListEnabled) {
                    if (blockListHelper.isBlocked(currentDomain, url, isThirdPartyRequest, easyList)) {
                        // Increment the blocked requests counters.
                        blockedRequests++;
                        easyListBlockedRequests++;

                        // Update the titles of the blocklist menu items.  This must be run from the UI thread.
                        activity.runOnUiThread(() -> {
                            navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            blocklistsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            easyListMenuItem.setTitle(easyListBlockedRequests + " - " + getString(R.string.easylist));
                        });

                        // Reset the whitelist results tracker (because otherwise it will sometimes add results to the list due to a race condition).
                        whiteListResultStringArray = null;

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    }
                }

                // Check EasyPrivacy if it is enabled.
                if (easyPrivacyEnabled) {
                    if (blockListHelper.isBlocked(currentDomain, url, isThirdPartyRequest, easyPrivacy)) {
                        // Increment the blocked requests counters.
                        blockedRequests++;
                        easyPrivacyBlockedRequests++;

                        // Update the titles of the blocklist menu items.  This must be run from the UI thread.
                        activity.runOnUiThread(() -> {
                            navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            blocklistsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            easyPrivacyMenuItem.setTitle(easyPrivacyBlockedRequests + " - " + getString(R.string.easyprivacy));
                        });

                        // Reset the whitelist results tracker (because otherwise it will sometimes add results to the list due to a race condition).
                        whiteListResultStringArray = null;

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    }
                }

                // Check Fanboy’s Annoyance List if it is enabled.
                if (fanboysAnnoyanceListEnabled) {
                    if (blockListHelper.isBlocked(currentDomain, url, isThirdPartyRequest, fanboysAnnoyanceList)) {
                        // Increment the blocked requests counters.
                        blockedRequests++;
                        fanboysAnnoyanceListBlockedRequests++;

                        // Update the titles of the blocklist menu items.  This must be run from the UI thread.
                        activity.runOnUiThread(() -> {
                            navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            blocklistsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            fanboysAnnoyanceListMenuItem.setTitle(fanboysAnnoyanceListBlockedRequests + " - " + getString(R.string.fanboys_annoyance_list));
                        });

                        // Reset the whitelist results tracker (because otherwise it will sometimes add results to the list due to a race condition).
                        whiteListResultStringArray = null;

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    }
                } else if (fanboysSocialBlockingListEnabled){  // Only check Fanboy’s Social Blocking List if Fanboy’s Annoyance List is disabled.
                    if (blockListHelper.isBlocked(currentDomain, url, isThirdPartyRequest, fanboysSocialList)) {
                        // Increment the blocked requests counters.
                        blockedRequests++;
                        fanboysSocialBlockingListBlockedRequests++;

                        // Update the titles of the blocklist menu items.  This must be run from the UI thread.
                        activity.runOnUiThread(() -> {
                            navigationRequestsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            blocklistsMenuItem.setTitle(getString(R.string.requests) + " - " + blockedRequests);
                            fanboysSocialBlockingListMenuItem.setTitle(fanboysSocialBlockingListBlockedRequests + " - " + getString(R.string.fanboys_social_blocking_list));
                        });

                        // Reset the whitelist results tracker (because otherwise it will sometimes add results to the list due to a race condition).
                        whiteListResultStringArray = null;

                        // The resource request was blocked.  Return an empty web resource response.
                        return emptyWebResourceResponse;
                    }
                }

                // Add the request to the log because it hasn't been processed by any of the previous checks.
                if (whiteListResultStringArray != null ) {  // The request was processed by a whitelist.
                    resourceRequests.add(whiteListResultStringArray);
                } else {  // The request didn't match any blocklist entry.  Log it as a default request.
                    resourceRequests.add(new String[]{String.valueOf(REQUEST_DEFAULT), url});
                }

                // The resource request has not been blocked.  `return null` loads the requested resource.
                return null;
            }

            // Handle HTTP authentication requests.
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                // Store `handler` so it can be accessed from `onHttpAuthenticationCancel()` and `onHttpAuthenticationProceed()`.
                httpAuthHandler = handler;

                // Display the HTTP authentication dialog.
                AppCompatDialogFragment httpAuthenticationDialogFragment = HttpAuthenticationDialog.displayDialog(host, realm);
                httpAuthenticationDialogFragment.show(getSupportFragmentManager(), getString(R.string.http_authentication));
            }

            // Update the URL in urlTextBox when the page starts to load.
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Reset the list of resource requests.
                resourceRequests.clear();

                // Initialize the counters for requests blocked by each blocklist.
                blockedRequests = 0;
                easyListBlockedRequests = 0;
                easyPrivacyBlockedRequests = 0;
                fanboysAnnoyanceListBlockedRequests = 0;
                fanboysSocialBlockingListBlockedRequests = 0;
                ultraPrivacyBlockedRequests = 0;
                thirdPartyBlockedRequests = 0;

                // If night mode is enabled, hide `mainWebView` until after the night mode CSS is applied.
                if (nightMode) {
                    mainWebView.setVisibility(View.INVISIBLE);
                }

                // Hide the keyboard.  `0` indicates no additional flags.
                inputMethodManager.hideSoftInputFromWindow(mainWebView.getWindowToken(), 0);

                // Check to see if Privacy Browser is waiting on Orbot.
                if (!waitingForOrbot) {  // We are not waiting on Orbot, so we need to process the URL.
                    // We need to update `formattedUrlString` at the beginning of the load, so that if the user toggles JavaScript during the load the new website is reloaded.
                    formattedUrlString = url;

                    // Display the formatted URL text.
                    urlTextBox.setText(formattedUrlString);

                    // Apply text highlighting to `urlTextBox`.
                    highlightUrlText();

                    // Apply any custom domain settings if the URL was loaded by navigating history.
                    if (navigatingHistory) {
                        // Apply the domain settings.
                        boolean userAgentChanged = applyDomainSettings(url, true, false);

                        // Reset `navigatingHistory`.
                        navigatingHistory = false;

                        // Manually load the URL if the user agent has changed, which will have caused the previous URL to be reloaded.
                        if (userAgentChanged) {
                            loadUrl(formattedUrlString);
                        }
                    }

                    // Set `urlIsLoading` to `true`, so that redirects while loading do not trigger changes in the user agent, which forces another reload of the existing page.
                    urlIsLoading = true;

                    // Replace Refresh with Stop if the menu item has been created.  (The WebView typically begins loading before the menu items are instantiated.)
                    if (refreshMenuItem != null) {
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
                }
            }

            // It is necessary to update `formattedUrlString` and `urlTextBox` after the page finishes loading because the final URL can change during load.
            @Override
            public void onPageFinished(WebView view, String url) {
                // Reset the wide view port if it has been turned off by the waiting for Orbot message.
                if (!waitingForOrbot) {
                    mainWebView.getSettings().setUseWideViewPort(true);
                }

                // Flush any cookies to persistent storage.  `CookieManager` has become very lazy about flushing cookies in recent versions.
                if (firstPartyCookiesEnabled && Build.VERSION.SDK_INT >= 21) {
                    cookieManager.flush();
                }

                // Update the Refresh menu item if it has been created.
                if (refreshMenuItem != null) {
                    // Reset the Refresh title.
                    refreshMenuItem.setTitle(R.string.refresh);

                    // If the icon is displayed in the AppBar, reset it according to the theme.
                    if (displayAdditionalAppBarIcons) {
                        if (darkTheme) {
                            refreshMenuItem.setIcon(R.drawable.refresh_enabled_dark);
                        } else {
                            refreshMenuItem.setIcon(R.drawable.refresh_enabled_light);
                        }
                    }
                }

                // Reset `urlIsLoading`, which is used to prevent reloads on redirect if the user agent changes.
                urlIsLoading = false;

                // Clear the cache and history if Incognito Mode is enabled.
                if (incognitoModeEnabled) {
                    // Clear the cache.  `true` includes disk files.
                    mainWebView.clearCache(true);

                    // Clear the back/forward history.
                    mainWebView.clearHistory();

                    // Manually delete cache folders.
                    try {
                        // Delete the main cache directory.
                        privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/cache");

                        // Delete the secondary `Service Worker` cache directory.
                        // A `String[]` must be used because the directory contains a space and `Runtime.exec` will not escape the string correctly otherwise.
                        privacyBrowserRuntime.exec(new String[] {"rm", "-rf", privateDataDirectoryString + "/app_webview/Service Worker/"});
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

                        urlTextBox.setText(formattedUrlString);

                        // Request focus for `urlTextBox`.
                        urlTextBox.requestFocus();

                        // Display the keyboard.
                        inputMethodManager.showSoftInput(urlTextBox, 0);

                        // Apply the domain settings.  This clears any settings from the previous domain.
                        applyDomainSettings(formattedUrlString, true, false);
                    } else {  // `WebView` has loaded a webpage.
                        // Set `formattedUrlString`.
                        formattedUrlString = url;

                        // Only update `urlTextBox` if the user is not typing in it.
                        if (!urlTextBox.hasFocus()) {
                            // Display the formatted URL text.
                            urlTextBox.setText(formattedUrlString);

                            // Apply text highlighting to `urlTextBox`.
                            highlightUrlText();
                        }
                    }

                    // Store the SSL certificate so it can be accessed from `ViewSslCertificateDialog` and `PinnedSslCertificateMismatchDialog`.
                    sslCertificate = mainWebView.getCertificate();

                    // Check the current website SSL certificate against the pinned SSL certificate if there is a pinned SSL certificate the user has not chosen to ignore it for this session.
                    // Also ignore if changes in the user agent causes an error while navigating history.
                    if (pinnedDomainSslCertificate && !ignorePinnedSslCertificate && navigatingHistory) {
                        // Initialize the current SSL certificate variables.
                        String currentWebsiteIssuedToCName = "";
                        String currentWebsiteIssuedToOName = "";
                        String currentWebsiteIssuedToUName = "";
                        String currentWebsiteIssuedByCName = "";
                        String currentWebsiteIssuedByOName = "";
                        String currentWebsiteIssuedByUName = "";
                        Date currentWebsiteSslStartDate = null;
                        Date currentWebsiteSslEndDate = null;


                        // Extract the individual pieces of information from the current website SSL certificate if it is not null.
                        if (sslCertificate != null) {
                            currentWebsiteIssuedToCName = sslCertificate.getIssuedTo().getCName();
                            currentWebsiteIssuedToOName = sslCertificate.getIssuedTo().getOName();
                            currentWebsiteIssuedToUName = sslCertificate.getIssuedTo().getUName();
                            currentWebsiteIssuedByCName = sslCertificate.getIssuedBy().getCName();
                            currentWebsiteIssuedByOName = sslCertificate.getIssuedBy().getOName();
                            currentWebsiteIssuedByUName = sslCertificate.getIssuedBy().getUName();
                            currentWebsiteSslStartDate = sslCertificate.getValidNotBeforeDate();
                            currentWebsiteSslEndDate = sslCertificate.getValidNotAfterDate();
                        }

                        // Initialize `String` variables to store the SSL certificate dates.  `Strings` are needed to compare the values below, which doesn't work with `Dates` if they are `null`.
                        String currentWebsiteSslStartDateString = "";
                        String currentWebsiteSslEndDateString = "";
                        String pinnedDomainSslStartDateString = "";
                        String pinnedDomainSslEndDateString = "";

                        // Convert the `Dates` to `Strings` if they are not `null`.
                        if (currentWebsiteSslStartDate != null) {
                            currentWebsiteSslStartDateString = currentWebsiteSslStartDate.toString();
                        }

                        if (currentWebsiteSslEndDate != null) {
                            currentWebsiteSslEndDateString = currentWebsiteSslEndDate.toString();
                        }

                        if (pinnedDomainSslStartDate != null) {
                            pinnedDomainSslStartDateString = pinnedDomainSslStartDate.toString();
                        }

                        if (pinnedDomainSslEndDate != null) {
                            pinnedDomainSslEndDateString = pinnedDomainSslEndDate.toString();
                        }

                        // Check to see if the pinned SSL certificate matches the current website certificate.
                        if (!currentWebsiteIssuedToCName.equals(pinnedDomainSslIssuedToCNameString) || !currentWebsiteIssuedToOName.equals(pinnedDomainSslIssuedToONameString) ||
                                !currentWebsiteIssuedToUName.equals(pinnedDomainSslIssuedToUNameString) || !currentWebsiteIssuedByCName.equals(pinnedDomainSslIssuedByCNameString) ||
                                !currentWebsiteIssuedByOName.equals(pinnedDomainSslIssuedByONameString) || !currentWebsiteIssuedByUName.equals(pinnedDomainSslIssuedByUNameString) ||
                                !currentWebsiteSslStartDateString.equals(pinnedDomainSslStartDateString) || !currentWebsiteSslEndDateString.equals(pinnedDomainSslEndDateString)) {
                            // The pinned SSL certificate doesn't match the current domain certificate.
                            //Display the pinned SSL certificate mismatch `AlertDialog`.
                            AppCompatDialogFragment pinnedSslCertificateMismatchDialogFragment = new PinnedSslCertificateMismatchDialog();
                            pinnedSslCertificateMismatchDialogFragment.show(getSupportFragmentManager(), getString(R.string.ssl_certificate_mismatch));
                        }
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
                if (pinnedDomainSslCertificate &&
                        currentWebsiteIssuedToCName.equals(pinnedDomainSslIssuedToCNameString) && currentWebsiteIssuedToOName.equals(pinnedDomainSslIssuedToONameString) &&
                        currentWebsiteIssuedToUName.equals(pinnedDomainSslIssuedToUNameString) && currentWebsiteIssuedByCName.equals(pinnedDomainSslIssuedByCNameString) &&
                        currentWebsiteIssuedByOName.equals(pinnedDomainSslIssuedByONameString) && currentWebsiteIssuedByUName.equals(pinnedDomainSslIssuedByUNameString) &&
                        currentWebsiteSslStartDate.equals(pinnedDomainSslStartDate) && currentWebsiteSslEndDate.equals(pinnedDomainSslEndDate)) {
                    // An SSL certificate is pinned and matches the current domain certificate.
                    // Proceed to the website without displaying an error.
                    handler.proceed();
                } else {  // Either there isn't a pinned SSL certificate or it doesn't match the current website certificate.
                    // Store `handler` so it can be accesses from `onSslErrorCancel()` and `onSslErrorProceed()`.
                    sslErrorHandler = handler;

                    // Display the SSL error `AlertDialog`.
                    AppCompatDialogFragment sslCertificateErrorDialogFragment = SslCertificateErrorDialog.displayDialog(error);
                    sslCertificateErrorDialogFragment.show(getSupportFragmentManager(), getString(R.string.ssl_certificate_error));
                }
            }
        });

        // Load the website if not waiting for Orbot to connect.
        if (!waitingForOrbot) {
            loadUrl(formattedUrlString);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Sets the new intent as the activity intent, so that any future `getIntent()`s pick up this one instead of creating a new activity.
        setIntent(intent);

        // Check to see if the intent contains a new URL.
        if (intent.getData() != null) {
            // Get the intent data.
            final Uri intentUriData = intent.getData();

            // Load the website.
            loadUrl(intentUriData.toString());

            // Close the navigation drawer if it is open.
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }

            // Close the bookmarks drawer if it is open.
            if (drawerLayout.isDrawerVisible(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            }

            // Clear the keyboard if displayed and remove the focus on the urlTextBar if it has it.
            mainWebView.requestFocus();
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

        // Apply the app settings if returning from the Settings activity..
        if (reapplyAppSettingsOnRestart) {
            // Apply the app settings.
            applyAppSettings();

            // Reload the webpage if displaying of images has been disabled in the Settings activity.
            if (reloadOnRestart) {
                // Reload `mainWebView`.
                mainWebView.reload();

                // Reset `reloadOnRestartBoolean`.
                reloadOnRestart = false;
            }

            // Reset the return from settings flag.
            reapplyAppSettingsOnRestart = false;
        }

        // Apply the domain settings if returning from the Domains activity.
        if (reapplyDomainSettingsOnRestart) {
            // Reapply the domain settings.
            applyDomainSettings(formattedUrlString, false, true);

            // Reset `reapplyDomainSettingsOnRestart`.
            reapplyDomainSettingsOnRestart = false;
        }

        // Load the URL on restart to apply changes to night mode.
        if (loadUrlOnRestart) {
            // Load the current `formattedUrlString`.
            loadUrl(formattedUrlString);

            // Reset `loadUrlOnRestart.
            loadUrlOnRestart = false;
        }

        // Update the bookmarks drawer if returning from the Bookmarks activity.
        if (restartFromBookmarksActivity) {
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

        // Resume JavaScript (if enabled).
        mainWebView.resumeTimers();

        // Resume `mainWebView`.
        mainWebView.onResume();

        // Resume the adView for the free flavor.
        if (BuildConfig.FLAVOR.contentEquals("free")) {
            // Resume the ad.
            AdHelper.resumeAd(findViewById(R.id.adview));
        }

        // Display a message to the user if waiting for Orbot.
        if (waitingForOrbot && !orbotStatus.equals("ON")) {
            // Disable the wide view port so that the waiting for Orbot text is displayed correctly.
            mainWebView.getSettings().setUseWideViewPort(false);

            // Load a waiting page.  `null` specifies no encoding, which defaults to ASCII.
            mainWebView.loadData(waitingForOrbotHtmlString, "text/html", null);
        }

        if (displayingFullScreenVideo) {
            // Remove the translucent overlays.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
            drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
             * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
             * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
             */
            rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onPause() {
        // Run the default commands.
        super.onPause();

        // Pause `mainWebView`.
        mainWebView.onPause();

        // Stop all JavaScript.
        mainWebView.pauseTimers();

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.webview_options_menu, menu);

        // Set mainMenu so it can be used by `onOptionsItemSelected()` and `updatePrivacyIcons`.
        mainMenu = menu;

        // Set the initial status of the privacy icons.  `false` does not call `invalidateOptionsMenu` as the last step.
        updatePrivacyIcons(false);

        // Get handles for the menu items.
        MenuItem toggleFirstPartyCookiesMenuItem = menu.findItem(R.id.toggle_first_party_cookies);
        MenuItem toggleThirdPartyCookiesMenuItem = menu.findItem(R.id.toggle_third_party_cookies);
        MenuItem toggleDomStorageMenuItem = menu.findItem(R.id.toggle_dom_storage);
        MenuItem toggleSaveFormDataMenuItem = menu.findItem(R.id.toggle_save_form_data);  // Form data can be removed once the minimum API >= 26.
        MenuItem clearFormDataMenuItem = menu.findItem(R.id.clear_form_data);  // Form data can be removed once the minimum API >= 26.
        refreshMenuItem = menu.findItem(R.id.refresh);
        blocklistsMenuItem = menu.findItem(R.id.blocklists);
        easyListMenuItem = menu.findItem(R.id.easylist);
        easyPrivacyMenuItem = menu.findItem(R.id.easyprivacy);
        fanboysAnnoyanceListMenuItem = menu.findItem(R.id.fanboys_annoyance_list);
        fanboysSocialBlockingListMenuItem = menu.findItem(R.id.fanboys_social_blocking_list);
        ultraPrivacyMenuItem = menu.findItem(R.id.ultraprivacy);
        blockAllThirdPartyRequestsMenuItem = menu.findItem(R.id.block_all_third_party_requests);
        MenuItem adConsentMenuItem = menu.findItem(R.id.ad_consent);

        // Only display third-party cookies if API >= 21
        toggleThirdPartyCookiesMenuItem.setVisible(Build.VERSION.SDK_INT >= 21);

        // Only display the form data menu items if the API < 26.
        toggleSaveFormDataMenuItem.setVisible(Build.VERSION.SDK_INT < 26);
        clearFormDataMenuItem.setVisible(Build.VERSION.SDK_INT < 26);

        // Only show Ad Consent if this is the free flavor.
        adConsentMenuItem.setVisible(BuildConfig.FLAVOR.contentEquals("free"));

        // Get the shared preference values.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the status of the additional AppBar icons.
        displayAdditionalAppBarIcons = sharedPreferences.getBoolean("display_additional_app_bar_icons", false);

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
        if (urlIsLoading) {
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
        MenuItem toggleFirstPartyCookiesMenuItem = menu.findItem(R.id.toggle_first_party_cookies);
        MenuItem toggleThirdPartyCookiesMenuItem = menu.findItem(R.id.toggle_third_party_cookies);
        MenuItem toggleDomStorageMenuItem = menu.findItem(R.id.toggle_dom_storage);
        MenuItem toggleSaveFormDataMenuItem = menu.findItem(R.id.toggle_save_form_data);  // Form data can be removed once the minimum API >= 26.
        MenuItem clearDataMenuItem = menu.findItem(R.id.clear_data);
        MenuItem clearCookiesMenuItem = menu.findItem(R.id.clear_cookies);
        MenuItem clearDOMStorageMenuItem = menu.findItem(R.id.clear_dom_storage);
        MenuItem clearFormDataMenuItem = menu.findItem(R.id.clear_form_data);  // Form data can be removed once the minimum API >= 26.
        MenuItem fontSizeMenuItem = menu.findItem(R.id.font_size);
        MenuItem swipeToRefreshMenuItem = menu.findItem(R.id.swipe_to_refresh);
        MenuItem displayImagesMenuItem = menu.findItem(R.id.display_images);
        MenuItem nightModeMenuItem = menu.findItem(R.id.night_mode);
        MenuItem proxyThroughOrbotMenuItem = menu.findItem(R.id.proxy_through_orbot);

        // Set the text for the domain menu item.
        if (domainSettingsApplied) {
            addOrEditDomain.setTitle(R.string.edit_domain_settings);
        } else {
            addOrEditDomain.setTitle(R.string.add_domain_settings);
        }

        // Set the status of the menu item checkboxes.
        toggleFirstPartyCookiesMenuItem.setChecked(firstPartyCookiesEnabled);
        toggleThirdPartyCookiesMenuItem.setChecked(thirdPartyCookiesEnabled);
        toggleDomStorageMenuItem.setChecked(domStorageEnabled);
        toggleSaveFormDataMenuItem.setChecked(saveFormDataEnabled);  // Form data can be removed once the minimum API >= 26.
        easyListMenuItem.setChecked(easyListEnabled);
        easyPrivacyMenuItem.setChecked(easyPrivacyEnabled);
        fanboysAnnoyanceListMenuItem.setChecked(fanboysAnnoyanceListEnabled);
        fanboysSocialBlockingListMenuItem.setChecked(fanboysSocialBlockingListEnabled);
        ultraPrivacyMenuItem.setChecked(ultraPrivacyEnabled);
        blockAllThirdPartyRequestsMenuItem.setChecked(blockAllThirdPartyRequests);
        swipeToRefreshMenuItem.setChecked(swipeRefreshLayout.isEnabled());
        displayImagesMenuItem.setChecked(mainWebView.getSettings().getLoadsImagesAutomatically());
        nightModeMenuItem.setChecked(nightMode);
        proxyThroughOrbotMenuItem.setChecked(proxyThroughOrbot);

        // Enable third-party cookies if first-party cookies are enabled.
        toggleThirdPartyCookiesMenuItem.setEnabled(firstPartyCookiesEnabled);

        // Enable DOM Storage if JavaScript is enabled.
        toggleDomStorageMenuItem.setEnabled(javaScriptEnabled);

        // Enable Clear Cookies if there are any.
        clearCookiesMenuItem.setEnabled(cookieManager.hasCookies());

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
            WebViewDatabase mainWebViewDatabase = WebViewDatabase.getInstance(this);
            clearFormDataMenuItem.setEnabled(mainWebViewDatabase.hasFormData());
        } else {
            // Disable clear form data because it is not supported on current version of Android.
            clearFormDataMenuItem.setEnabled(false);
        }

        // Enable Clear Data if any of the submenu items are enabled.
        clearDataMenuItem.setEnabled(clearCookiesMenuItem.isEnabled() || clearDOMStorageMenuItem.isEnabled() || clearFormDataMenuItem.isEnabled());

        // Disable Fanboy's Social Blocking List if Fanboy's Annoyance List is checked.
        fanboysSocialBlockingListMenuItem.setEnabled(!fanboysAnnoyanceListEnabled);

        // Initialize the display names for the blocklists with the number of blocked requests.
        blocklistsMenuItem.setTitle(getString(R.string.blocklists) + " - " + blockedRequests);
        easyListMenuItem.setTitle(easyListBlockedRequests + " - " + getString(R.string.easylist));
        easyPrivacyMenuItem.setTitle(easyPrivacyBlockedRequests + " - " + getString(R.string.easyprivacy));
        fanboysAnnoyanceListMenuItem.setTitle(fanboysAnnoyanceListBlockedRequests + " - " + getString(R.string.fanboys_annoyance_list));
        fanboysSocialBlockingListMenuItem.setTitle(fanboysSocialBlockingListBlockedRequests + " - " + getString(R.string.fanboys_social_blocking_list));
        ultraPrivacyMenuItem.setTitle(ultraPrivacyBlockedRequests + " - " + getString(R.string.ultraprivacy));
        blockAllThirdPartyRequestsMenuItem.setTitle(thirdPartyBlockedRequests + " - " + getString(R.string.block_all_third_party_requests));

        // Get the current user agent.
        String currentUserAgent = mainWebView.getSettings().getUserAgentString();

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

        // Initialize font size variables.
        int fontSize = mainWebView.getSettings().getTextZoom();
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
    // removeAllCookies is deprecated, but it is required for API < 21.
    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        // Get the selected menu item ID.
        int menuItemId = menuItem.getItemId();

        // Set the commands that relate to the menu entries.
        switch (menuItemId) {
            case R.id.toggle_javascript:
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

            case R.id.add_or_edit_domain:
                if (domainSettingsApplied) {  // Edit the current domain settings.
                    // Reapply the domain settings on returning to `MainWebViewActivity`.
                    reapplyDomainSettingsOnRestart = true;
                    currentDomainName = "";

                    // Create an intent to launch the domains activity.
                    Intent domainsIntent = new Intent(this, DomainsActivity.class);

                    // Put extra information instructing the domains activity to directly load the current domain and close on back instead of returning to the domains list.
                    domainsIntent.putExtra("loadDomain", domainSettingsDatabaseId);
                    domainsIntent.putExtra("closeOnBack", true);

                    // Make it so.
                    startActivity(domainsIntent);
                } else {  // Add a new domain.
                    // Apply the new domain settings on returning to `MainWebViewActivity`.
                    reapplyDomainSettingsOnRestart = true;
                    currentDomainName = "";

                    // Get the current domain
                    Uri currentUri = Uri.parse(formattedUrlString);
                    String currentDomain = currentUri.getHost();

                    // Initialize the database handler.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
                    DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(this, null, null, 0);

                    // Create the domain and store the database ID.
                    int newDomainDatabaseId = domainsDatabaseHelper.addDomain(currentDomain);

                    // Create an intent to launch the domains activity.
                    Intent domainsIntent = new Intent(this, DomainsActivity.class);

                    // Put extra information instructing the domains activity to directly load the new domain and close on back instead of returning to the domains list.
                    domainsIntent.putExtra("loadDomain", newDomainDatabaseId);
                    domainsIntent.putExtra("closeOnBack", true);

                    // Make it so.
                    startActivity(domainsIntent);
                }
                return true;

            case R.id.toggle_first_party_cookies:
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
                } else if (javaScriptEnabled) {  // JavaScript is still enabled.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.first_party_cookies_disabled, Snackbar.LENGTH_SHORT).show();
                } else {  // Privacy mode.
                    Snackbar.make(findViewById(R.id.main_webview), R.string.privacy_mode, Snackbar.LENGTH_SHORT).show();
                }

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.toggle_third_party_cookies:
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

            case R.id.toggle_dom_storage:
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

            // Form data can be removed once the minimum API >= 26.
            case R.id.toggle_save_form_data:
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

            case R.id.clear_cookies:
                Snackbar.make(findViewById(R.id.main_webview), R.string.cookies_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Do nothing because everything will be handled by `onDismissed()` below.
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                switch (event) {
                                    // The user pushed the `Undo` button.
                                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                        // Do nothing.
                                        break;

                                    // The `Snackbar` was dismissed without the `Undo` button being pushed.
                                    default:
                                        // `cookieManager.removeAllCookie()` varies by SDK.
                                        if (Build.VERSION.SDK_INT < 21) {
                                            cookieManager.removeAllCookie();
                                        } else {
                                            // `null` indicates no callback.
                                            cookieManager.removeAllCookies(null);
                                        }
                                }
                            }
                        })
                        .show();
                return true;

            case R.id.clear_dom_storage:
                Snackbar.make(findViewById(R.id.main_webview), R.string.dom_storage_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Do nothing because everything will be handled by `onDismissed()` below.
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                switch (event) {
                                    // The user pushed the `Undo` button.
                                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                        // Do nothing.
                                        break;

                                    // The `Snackbar` was dismissed without the `Undo` button being pushed.
                                    default:
                                        // Delete the DOM Storage.
                                        WebStorage webStorage = WebStorage.getInstance();
                                        webStorage.deleteAllData();

                                        // Initialize a handler to manually delete the DOM storage files and directories.
                                        Handler deleteDomStorageHandler = new Handler();

                                        // Setup a runnable to manually delete the DOM storage files and directories.
                                        Runnable deleteDomStorageRunnable = () -> {
                                            try {
                                                // A `String[]` must be used because the directory contains a space and `Runtime.exec` will otherwise not escape the string correctly.
                                                privacyBrowserRuntime.exec(new String[]{"rm", "-rf", privateDataDirectoryString + "/app_webview/Local Storage/"});

                                                // Multiple commands must be used because `Runtime.exec()` does not like `*`.
                                                privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/IndexedDB");
                                                privacyBrowserRuntime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager");
                                                privacyBrowserRuntime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager-journal");
                                                privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/databases");
                                            } catch (IOException e) {
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
                Snackbar.make(findViewById(R.id.main_webview), R.string.form_data_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            // Do nothing because everything will be handled by `onDismissed()` below.
                        })
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                switch (event) {
                                    // The user pushed the `Undo` button.
                                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                        // Do nothing.
                                        break;

                                    // The `Snackbar` was dismissed without the `Undo` button being pushed.
                                    default:
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
                easyListEnabled = !easyListEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(easyListEnabled);

                // Reload the main WebView.
                mainWebView.reload();
                return true;

            case R.id.easyprivacy:
                // Toggle the EasyPrivacy status.
                easyPrivacyEnabled = !easyPrivacyEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(easyPrivacyEnabled);

                // Reload the main WebView.
                mainWebView.reload();
                return true;

            case R.id.fanboys_annoyance_list:
                // Toggle Fanboy's Annoyance List status.
                fanboysAnnoyanceListEnabled = !fanboysAnnoyanceListEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(fanboysAnnoyanceListEnabled);

                // Update the staus of Fanboy's Social Blocking List.
                MenuItem fanboysSocialBlockingListMenuItem = mainMenu.findItem(R.id.fanboys_social_blocking_list);
                fanboysSocialBlockingListMenuItem.setEnabled(!fanboysAnnoyanceListEnabled);

                // Reload the main WebView.
                mainWebView.reload();
                return true;

            case R.id.fanboys_social_blocking_list:
                // Toggle Fanboy's Social Blocking List status.
                fanboysSocialBlockingListEnabled = !fanboysSocialBlockingListEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(fanboysSocialBlockingListEnabled);

                // Reload the main WebView.
                mainWebView.reload();
                return true;

            case R.id.ultraprivacy:
                // Toggle the UltraPrivacy status.
                ultraPrivacyEnabled = !ultraPrivacyEnabled;

                // Update the menu checkbox.
                menuItem.setChecked(ultraPrivacyEnabled);

                // Reload the main WebView.
                mainWebView.reload();
                return true;

            case R.id.block_all_third_party_requests:
                //Toggle the third-party requests blocker status.
                blockAllThirdPartyRequests = !blockAllThirdPartyRequests;

                // Update the menu checkbox.
                menuItem.setChecked(blockAllThirdPartyRequests);

                // Reload the main WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_privacy_browser:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[0]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_webview_default:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString("");

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_firefox_on_android:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[2]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_chrome_on_android:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[3]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_safari_on_ios:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[4]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_firefox_on_linux:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[5]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_chromium_on_linux:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[6]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_firefox_on_windows:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[7]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_chrome_on_windows:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[8]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_edge_on_windows:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[9]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_internet_explorer_on_windows:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[10]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_safari_on_macos:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(getResources().getStringArray(R.array.user_agent_data)[11]);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.user_agent_custom:
                // Update the user agent.
                mainWebView.getSettings().setUserAgentString(defaultCustomUserAgentString);

                // Reload the WebView.
                mainWebView.reload();
                return true;

            case R.id.font_size_twenty_five_percent:
                mainWebView.getSettings().setTextZoom(25);
                return true;

            case R.id.font_size_fifty_percent:
                mainWebView.getSettings().setTextZoom(50);
                return true;

            case R.id.font_size_seventy_five_percent:
                mainWebView.getSettings().setTextZoom(75);
                return true;

            case R.id.font_size_one_hundred_percent:
                mainWebView.getSettings().setTextZoom(100);
                return true;

            case R.id.font_size_one_hundred_twenty_five_percent:
                mainWebView.getSettings().setTextZoom(125);
                return true;

            case R.id.font_size_one_hundred_fifty_percent:
                mainWebView.getSettings().setTextZoom(150);
                return true;

            case R.id.font_size_one_hundred_seventy_five_percent:
                mainWebView.getSettings().setTextZoom(175);
                return true;

            case R.id.font_size_two_hundred_percent:
                mainWebView.getSettings().setTextZoom(200);
                return true;

            case R.id.swipe_to_refresh:
                // Toggle swipe to refresh.
                swipeRefreshLayout.setEnabled(!swipeRefreshLayout.isEnabled());
                return true;

            case R.id.display_images:
                if (mainWebView.getSettings().getLoadsImagesAutomatically()) {  // Images are currently loaded automatically.
                    mainWebView.getSettings().setLoadsImagesAutomatically(false);
                    mainWebView.reload();
                } else {  // Images are not currently loaded automatically.
                    mainWebView.getSettings().setLoadsImagesAutomatically(true);
                }
                return true;

            case R.id.night_mode:
                // Toggle night mode.
                nightMode = !nightMode;

                // Enable or disable JavaScript according to night mode, the global preference, and any domain settings.
                if (nightMode) {  // Night mode is enabled.  Enable JavaScript.
                    // Update the global variable.
                    javaScriptEnabled = true;
                } else if (domainSettingsApplied) {  // Night mode is disabled and domain settings are applied.  Set JavaScript according to the domain settings.
                    // Get the JavaScript preference that was stored the last time domain settings were loaded.
                    javaScriptEnabled = domainSettingsJavaScriptEnabled;
                } else {  // Night mode is disabled and domain settings are not applied.  Set JavaScript according to the global preference.
                    // Get a handle for the shared preference.
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                    // Get the JavaScript preference.
                    javaScriptEnabled = sharedPreferences.getBoolean("javascript", false);
                }

                // Apply the JavaScript setting to the WebView.
                mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);

                // Update the privacy icons.
                updatePrivacyIcons(false);

                // Reload the website.
                mainWebView.reload();
                return true;

            case R.id.print:
                // Get a `PrintManager` instance.
                PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);

                // Convert `mainWebView` to `printDocumentAdapter`.
                PrintDocumentAdapter printDocumentAdapter = mainWebView.createPrintDocumentAdapter();

                // Remove the lint error below that `printManager` might be `null`.
                assert printManager != null;

                // Print the document.  The print attributes are `null`.
                printManager.print(getString(R.string.privacy_browser_web_page), printDocumentAdapter, null);
                return true;

            case R.id.view_source:
                // Launch the View Source activity.
                Intent viewSourceIntent = new Intent(this, ViewSourceActivity.class);
                startActivity(viewSourceIntent);
                return true;

            case R.id.proxy_through_orbot:
                // Toggle the proxy through Orbot variable.
                proxyThroughOrbot = !proxyThroughOrbot;

                // Apply the proxy through Orbot settings.
                applyProxyThroughOrbot(true);
                return true;

            case R.id.share:
                // Setup the share string.
                String shareString = webViewTitle + " – " + urlTextBox.getText().toString();

                // Create the share intent.
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareString);
                shareIntent.setType("text/plain");

                // Make it so.
                startActivity(Intent.createChooser(shareIntent, "Share URL"));
                return true;

            case R.id.find_on_page:
                // Hide the URL app bar.
                supportAppBar.setVisibility(View.GONE);

                // Show the Find on Page `RelativeLayout`.
                findOnPageLinearLayout.setVisibility(View.VISIBLE);

                // Display the keyboard.  We have to wait 200 ms before running the command to work around a bug in Android.
                // http://stackoverflow.com/questions/5520085/android-show-softkeyboard-with-showsoftinput-is-not-working
                findOnPageEditText.postDelayed(() -> {
                    // Set the focus on `findOnPageEditText`.
                    findOnPageEditText.requestFocus();

                    // Display the keyboard.  `0` sets no input flags.
                    inputMethodManager.showSoftInput(findOnPageEditText, 0);
                }, 200);
                return true;

            case R.id.add_to_homescreen:
                // Show the `CreateHomeScreenShortcutDialog` `AlertDialog` and name this instance `R.string.create_shortcut`.
                AppCompatDialogFragment createHomeScreenShortcutDialogFragment = new CreateHomeScreenShortcutDialog();
                createHomeScreenShortcutDialogFragment.show(getSupportFragmentManager(), getString(R.string.create_shortcut));

                //Everything else will be handled by `CreateHomeScreenShortcutDialog` and the associated listener below.
                return true;

            case R.id.refresh:
                if (menuItem.getTitle().equals(getString(R.string.refresh))) {  // The refresh button was pushed.
                    // Reload the WebView.
                    mainWebView.reload();
                } else {  // The stop button was pushed.
                    // Stop the loading of the WebView.
                    mainWebView.stopLoading();
                }
                return true;

            case R.id.ad_consent:
                // Display the ad consent dialog.
                DialogFragment adConsentDialogFragment = new AdConsentDialog();
                adConsentDialogFragment.show(getFragmentManager(), getString(R.string.ad_consent));
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
                    // Reset the formatted URL string so the page will load correctly if blocking of third-party requests is enabled.
                    formattedUrlString = "";

                    // Set `navigatingHistory` so that the domain settings are applied when the new URL is loaded.
                    navigatingHistory = true;

                    // Load the previous website in the history.
                    mainWebView.goBack();
                }
                break;

            case R.id.forward:
                if (mainWebView.canGoForward()) {
                    // Reset the formatted URL string so the page will load correctly if blocking of third-party requests is enabled.
                    formattedUrlString = "";

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
                urlHistoryDialogFragment.show(getSupportFragmentManager(), getString(R.string.history));
                break;

            case R.id.requests:
                // Launch the requests activity.
                Intent requestsIntent = new Intent(this, RequestsActivity.class);
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
                currentDomainName = "";

                // Launch the domains activity.
                Intent domainsIntent = new Intent(this, DomainsActivity.class);
                startActivity(domainsIntent);
                break;

            case R.id.settings:
                // Set the flag to reapply app settings on restart when returning from Settings.
                reapplyAppSettingsOnRestart = true;

                // Set the flag to reapply the domain settings on restart when returning from Settings.
                reapplyDomainSettingsOnRestart = true;
                currentDomainName = "";

                // Launch the settings activity.
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.import_export:
                // Launch the import/export activity.
                Intent importExportIntent = new Intent (this, ImportExportActivity.class);
                startActivity(importExportIntent);
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
                // Close the bookmarks cursor and database.
                bookmarksCursor.close();
                bookmarksDatabaseHelper.close();

                // Get a handle for `sharedPreferences`.  `this` references the current context.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                boolean clearEverything = sharedPreferences.getBoolean("clear_everything", true);

                // Clear cookies.
                if (clearEverything || sharedPreferences.getBoolean("clear_cookies", true)) {
                    // The command to remove cookies changed slightly in API 21.
                    if (Build.VERSION.SDK_INT >= 21) {
                        cookieManager.removeAllCookies(null);
                    } else {
                        cookieManager.removeAllCookie();
                    }

                    // Manually delete the cookies database, as `CookieManager` sometimes will not flush its changes to disk before `System.exit(0)` is run.
                    try {
                        // We have to use two commands because `Runtime.exec()` does not like `*`.
                        privacyBrowserRuntime.exec("rm -f " + privateDataDirectoryString + "/app_webview/Cookies");
                        privacyBrowserRuntime.exec("rm -f " + privateDataDirectoryString + "/app_webview/Cookies-journal");
                    } catch (IOException e) {
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
                        privacyBrowserRuntime.exec(new String[] {"rm", "-rf", privateDataDirectoryString + "/app_webview/Local Storage/"});

                        // Multiple commands must be used because `Runtime.exec()` does not like `*`.
                        privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/IndexedDB");
                        privacyBrowserRuntime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager");
                        privacyBrowserRuntime.exec("rm -f " + privateDataDirectoryString + "/app_webview/QuotaManager-journal");
                        privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/app_webview/databases");
                    } catch (IOException e) {
                        // Do nothing if an error is thrown.
                    }
                }

                // Clear form data if the API < 26.
                if ((Build.VERSION.SDK_INT < 26) && (clearEverything || sharedPreferences.getBoolean("clear_form_data", true))) {
                    WebViewDatabase webViewDatabase = WebViewDatabase.getInstance(this);
                    webViewDatabase.clearFormData();

                    // Manually delete the form data database, as `WebViewDatabase` sometimes will not flush its changes to disk before `System.exit(0)` is run.
                    try {
                        // We have to use a `String[]` because the database contains a space and `Runtime.exec` will not escape the string correctly otherwise.
                        privacyBrowserRuntime.exec(new String[] {"rm", "-f", privateDataDirectoryString + "/app_webview/Web Data"});
                        privacyBrowserRuntime.exec(new String[] {"rm", "-f", privateDataDirectoryString + "/app_webview/Web Data-journal"});
                    } catch (IOException e) {
                        // Do nothing if an error is thrown.
                    }
                }

                // Clear the cache.
                if (clearEverything || sharedPreferences.getBoolean("clear_cache", true)) {
                    // `true` includes disk files.
                    mainWebView.clearCache(true);

                    // Manually delete the cache directories.
                    try {
                        // Delete the main cache directory.
                        privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/cache");

                        // Delete the secondary `Service Worker` cache directory.
                        // A `String[]` must be used because the directory contains a space and `Runtime.exec` will not escape the string correctly otherwise.
                        privacyBrowserRuntime.exec(new String[] {"rm", "-rf", privateDataDirectoryString + "/app_webview/Service Worker/"});
                    } catch (IOException e) {
                        // Do nothing if an error is thrown.
                    }
                }

                // Clear SSL certificate preferences.
                mainWebView.clearSslPreferences();

                // Clear the back/forward history.
                mainWebView.clearHistory();

                // Clear `formattedUrlString`.
                formattedUrlString = null;

                // Clear `customHeaders`.
                customHeaders.clear();

                // Detach all views from `mainWebViewRelativeLayout`.
                mainWebViewRelativeLayout.removeAllViews();

                // Destroy the internal state of `mainWebView`.
                mainWebView.destroy();

                // Manually delete the `app_webview` folder, which contains the cookies, DOM storage, form data, and `Service Worker` cache.
                // See `https://code.google.com/p/android/issues/detail?id=233826&thanks=233826&ts=1486670530`.
                if (clearEverything) {
                    try {
                        privacyBrowserRuntime.exec("rm -rf " + privateDataDirectoryString + "/app_webview");
                    } catch (IOException e) {
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

        // Reload the ad for the free flavor if we not in full screen mode.
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
        // Store the `HitTestResult`.
        final WebView.HitTestResult hitTestResult = mainWebView.getHitTestResult();

        // Create strings.
        final String imageUrl;
        final String linkUrl;

        // Get a handle for the `ClipboardManager`.
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // Remove the lint errors below that `clipboardManager` might be `null`.
        assert clipboardManager != null;

        switch (hitTestResult.getType()) {
            // `SRC_ANCHOR_TYPE` is a link.
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                // Get the target URL.
                linkUrl = hitTestResult.getExtra();

                // Set the target URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(linkUrl);

                // Add a Load URL entry.
                menu.add(R.string.load_url).setOnMenuItemClickListener((MenuItem item) -> {
                    loadUrl(linkUrl);
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
                    if (downloadWithExternalApp) {  // Download with an external app.
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
                                downloadLocationPermissionDialogFragment.show(getFragmentManager(), getString(R.string.download_location));
                            } else {  // Show the permission request directly.
                                // Request the permission.  The download dialog will be launched by `onRequestPermissionResult()`.
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_FILE_REQUEST_CODE);
                            }
                        } else {  // The storage permission has already been granted.
                            // Get a handle for the download file alert dialog.
                            AppCompatDialogFragment downloadFileDialogFragment = DownloadFileDialog.fromUrl(linkUrl, "none", -1);

                            // Show the download file alert dialog.
                            downloadFileDialogFragment.show(getSupportFragmentManager(), getString(R.string.download));
                        }
                    }
                    return false;
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

                // Add a `Copy Email Address` entry.
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

            // `IMAGE_TYPE` is an image.
            case WebView.HitTestResult.IMAGE_TYPE:
                // Get the image URL.
                imageUrl = hitTestResult.getExtra();

                // Set the image URL as the title of the `ContextMenu`.
                menu.setHeaderTitle(imageUrl);

                // Add a `View Image` entry.
                menu.add(R.string.view_image).setOnMenuItemClickListener(item -> {
                    loadUrl(imageUrl);
                    return false;
                });

                // Add a `Download Image` entry.
                menu.add(R.string.download_image).setOnMenuItemClickListener((MenuItem item) -> {
                    // Check if the download should be processed by an external app.
                    if (downloadWithExternalApp) {  // Download with an external app.
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
                                downloadLocationPermissionDialogFragment.show(getFragmentManager(), getString(R.string.download_location));
                            } else {  // Show the permission request directly.
                                // Request the permission.  The download dialog will be launched by `onRequestPermissionResult().
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_IMAGE_REQUEST_CODE);
                            }
                        } else {  // The storage permission has already been granted.
                            // Get a handle for the download image alert dialog.
                            AppCompatDialogFragment downloadImageDialogFragment = DownloadImageDialog.imageUrl(imageUrl);

                            // Show the download image alert dialog.
                            downloadImageDialogFragment.show(getSupportFragmentManager(), getString(R.string.download));
                        }
                    }
                    return false;
                });

                // Add a `Copy URL` entry.
                menu.add(R.string.copy_url).setOnMenuItemClickListener(item -> {
                    // Save the image URL in a `ClipData`.
                    ClipData srcImageTypeClipData = ClipData.newPlainText(getString(R.string.url), imageUrl);

                    // Set the `ClipData` as the clipboard's primary clip.
                    clipboardManager.setPrimaryClip(srcImageTypeClipData);
                    return false;
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
                menu.add(R.string.view_image).setOnMenuItemClickListener(item -> {
                    loadUrl(imageUrl);
                    return false;
                });

                // Add a `Download Image` entry.
                menu.add(R.string.download_image).setOnMenuItemClickListener((MenuItem item) -> {
                    // Check if the download should be processed by an external app.
                    if (downloadWithExternalApp) {  // Download with an external app.
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
                                downloadLocationPermissionDialogFragment.show(getFragmentManager(), getString(R.string.download_location));
                            } else {  // Show the permission request directly.
                                // Request the permission.  The download dialog will be launched by `onRequestPermissionResult().
                                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, DOWNLOAD_IMAGE_REQUEST_CODE);
                            }
                        } else {  // The storage permission has already been granted.
                            // Get a handle for the download image alert dialog.
                            AppCompatDialogFragment downloadImageDialogFragment = DownloadImageDialog.imageUrl(imageUrl);

                            // Show the download image alert dialog.
                            downloadImageDialogFragment.show(getSupportFragmentManager(), getString(R.string.download));
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

                // Add a `Cancel` entry, which by default closes the `ContextMenu`.
                menu.add(R.string.cancel);
                break;
        }
    }

    @Override
    public void onCreateBookmark(AppCompatDialogFragment dialogFragment) {
        // Get the `EditTexts` from the `dialogFragment`.
        EditText createBookmarkNameEditText = dialogFragment.getDialog().findViewById(R.id.create_bookmark_name_edittext);
        EditText createBookmarkUrlEditText = dialogFragment.getDialog().findViewById(R.id.create_bookmark_url_edittext);

        // Extract the strings from the `EditTexts`.
        String bookmarkNameString = createBookmarkNameEditText.getText().toString();
        String bookmarkUrlString = createBookmarkUrlEditText.getText().toString();

        // Convert the favoriteIcon Bitmap to a byte array.  `0` is for lossless compression (the only option for a PNG).
        ByteArrayOutputStream favoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
        favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, favoriteIconByteArrayOutputStream);
        byte[] favoriteIconByteArray = favoriteIconByteArrayOutputStream.toByteArray();

        // Display the new bookmark below the current items in the (0 indexed) list.
        int newBookmarkDisplayOrder = bookmarksListView.getCount();

        // Create the bookmark.
        bookmarksDatabaseHelper.createBookmark(bookmarkNameString, bookmarkUrlString, currentBookmarksFolder, newBookmarkDisplayOrder, favoriteIconByteArray);

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentBookmarksFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Scroll to the new bookmark.
        bookmarksListView.setSelection(newBookmarkDisplayOrder);
    }

    @Override
    public void onCreateBookmarkFolder(AppCompatDialogFragment dialogFragment) {
        // Get handles for the views in `dialogFragment`.
        EditText createFolderNameEditText = dialogFragment.getDialog().findViewById(R.id.create_folder_name_edittext);
        RadioButton defaultFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon_radiobutton);
        ImageView folderIconImageView = dialogFragment.getDialog().findViewById(R.id.create_folder_default_icon);

        // Get new folder name string.
        String folderNameString = createFolderNameEditText.getText().toString();

        // Get the new folder icon `Bitmap`.
        Bitmap folderIconBitmap;
        if (defaultFolderIconRadioButton.isChecked()) {  // Use the default folder icon.
            // Get the default folder icon and convert it to a `Bitmap`.
            Drawable folderIconDrawable = folderIconImageView.getDrawable();
            BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
            folderIconBitmap = folderIconBitmapDrawable.getBitmap();
        } else {  // Use the `WebView` favorite icon.
            folderIconBitmap = favoriteIconBitmap;
        }

        // Convert `folderIconBitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
        ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
        folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
        byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

        // Move all the bookmarks down one in the display order.
        for (int i = 0; i < bookmarksListView.getCount(); i++) {
            int databaseId = (int) bookmarksListView.getItemIdAtPosition(i);
            bookmarksDatabaseHelper.updateDisplayOrder(databaseId, i + 1);
        }

        // Create the folder, which will be placed at the top of the `ListView`.
        bookmarksDatabaseHelper.createFolder(folderNameString, currentBookmarksFolder, folderIconByteArray);

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentBookmarksFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);

        // Scroll to the new folder.
        bookmarksListView.setSelection(0);
    }

    @Override
    public void onCreateHomeScreenShortcut(AppCompatDialogFragment dialogFragment) {
        // Get the shortcut name.
        EditText shortcutNameEditText = dialogFragment.getDialog().findViewById(R.id.shortcut_name_edittext);
        String shortcutNameString = shortcutNameEditText.getText().toString();

        // Convert the favorite icon bitmap to an `Icon`.  `IconCompat` is required until API >= 26.
        IconCompat favoriteIcon = IconCompat.createWithBitmap(favoriteIconBitmap);

        // Setup the shortcut intent.
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setData(Uri.parse(formattedUrlString));

        // Create a shortcut info builder.  The shortcut name becomes the shortcut ID.
        ShortcutInfoCompat.Builder shortcutInfoBuilder = new ShortcutInfoCompat.Builder(this, shortcutNameString);

        // Add the required fields to the shortcut info builder.
        shortcutInfoBuilder.setIcon(favoriteIcon);
        shortcutInfoBuilder.setIntent(shortcutIntent);
        shortcutInfoBuilder.setShortLabel(shortcutNameString);

        // Request the pin.  `ShortcutManagerCompat` can be switched to `ShortcutManager` once API >= 26.
        ShortcutManagerCompat.requestPinShortcut(this, shortcutInfoBuilder.build(), null);
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
        switch (requestCode) {
            case DOWNLOAD_FILE_REQUEST_CODE:
                // Show the download file alert dialog.  When the dialog closes, the correct command will be used based on the permission status.
                AppCompatDialogFragment downloadFileDialogFragment = DownloadFileDialog.fromUrl(downloadUrl, downloadContentDisposition, downloadContentLength);

                // On API 23, displaying the fragment must be delayed or the app will crash.
                if (Build.VERSION.SDK_INT == 23) {
                    new Handler().postDelayed(() -> downloadFileDialogFragment.show(getSupportFragmentManager(), getString(R.string.download)), 500);
                } else {
                    downloadFileDialogFragment.show(getSupportFragmentManager(), getString(R.string.download));
                }

                // Reset the download variables.
                downloadUrl = "";
                downloadContentDisposition = "";
                downloadContentLength = 0;
                break;

            case DOWNLOAD_IMAGE_REQUEST_CODE:
                // Show the download image alert dialog.  When the dialog closes, the correct command will be used based on the permission status.
                AppCompatDialogFragment downloadImageDialogFragment = DownloadImageDialog.imageUrl(downloadImageUrl);

                // On API 23, displaying the fragment must be delayed or the app will crash.
                if (Build.VERSION.SDK_INT == 23) {
                    new Handler().postDelayed(() -> downloadImageDialogFragment.show(getSupportFragmentManager(), getString(R.string.download)), 500);
                } else {
                    downloadImageDialogFragment.show(getSupportFragmentManager(), getString(R.string.download));
                }

                // Reset the image URL variable.
                downloadImageUrl = "";
                break;
        }
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
            // Code contributed 2017 Hendrik Knackstedt.  Copyright assigned to Soren Stoutner <soren@stoutner.com>.
            if (firstPartyCookiesEnabled) {
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
            // Code contributed 2017 Hendrik Knackstedt.  Copyright assigned to Soren Stoutner <soren@stoutner.com>.
            if (firstPartyCookiesEnabled) {
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
            Snackbar.make(mainWebView, R.string.cannot_download_file, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    public void onSaveBookmark(AppCompatDialogFragment dialogFragment, int selectedBookmarkDatabaseId) {
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
            // Convert the favorite icon to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream newFavoriteIconByteArrayOutputStream = new ByteArrayOutputStream();
            favoriteIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, newFavoriteIconByteArrayOutputStream);
            byte[] newFavoriteIconByteArray = newFavoriteIconByteArrayOutputStream.toByteArray();

            //  Update the bookmark and the favorite icon.
            bookmarksDatabaseHelper.updateBookmark(selectedBookmarkDatabaseId, bookmarkNameString, bookmarkUrlString, newFavoriteIconByteArray);
        }

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentBookmarksFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }

    @Override
    public void onSaveBookmarkFolder(AppCompatDialogFragment dialogFragment, int selectedFolderDatabaseId) {
        // Get handles for the views from `dialogFragment`.
        EditText editFolderNameEditText = dialogFragment.getDialog().findViewById(R.id.edit_folder_name_edittext);
        RadioButton currentFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_current_icon_radiobutton);
        RadioButton defaultFolderIconRadioButton = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_radiobutton);
        ImageView folderIconImageView = dialogFragment.getDialog().findViewById(R.id.edit_folder_default_icon_imageview);

        // Get the new folder name.
        String newFolderNameString = editFolderNameEditText.getText().toString();

        // Check if the favorite icon has changed.
        if (currentFolderIconRadioButton.isChecked()) {  // Only the name has changed.
            // Update the name in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString);
        } else if (!currentFolderIconRadioButton.isChecked() && newFolderNameString.equals(oldFolderNameString)) {  // Only the icon has changed.
            // Get the new folder icon `Bitmap`.
            Bitmap folderIconBitmap;
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon and convert it to a `Bitmap`.
                Drawable folderIconDrawable = folderIconImageView.getDrawable();
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Use the `WebView` favorite icon.
                folderIconBitmap = favoriteIconBitmap;
            }

            // Convert the folder `Bitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
            byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

            // Update the folder icon in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, folderIconByteArray);
        } else {  // The folder icon and the name have changed.
            // Get the new folder icon `Bitmap`.
            Bitmap folderIconBitmap;
            if (defaultFolderIconRadioButton.isChecked()) {
                // Get the default folder icon and convert it to a `Bitmap`.
                Drawable folderIconDrawable = folderIconImageView.getDrawable();
                BitmapDrawable folderIconBitmapDrawable = (BitmapDrawable) folderIconDrawable;
                folderIconBitmap = folderIconBitmapDrawable.getBitmap();
            } else {  // Use the `WebView` favorite icon.
                folderIconBitmap = MainWebViewActivity.favoriteIconBitmap;
            }

            // Convert the folder `Bitmap` to a byte array.  `0` is for lossless compression (the only option for a PNG).
            ByteArrayOutputStream folderIconByteArrayOutputStream = new ByteArrayOutputStream();
            folderIconBitmap.compress(Bitmap.CompressFormat.PNG, 0, folderIconByteArrayOutputStream);
            byte[] folderIconByteArray = folderIconByteArrayOutputStream.toByteArray();

            // Update the folder name and icon in the database.
            bookmarksDatabaseHelper.updateFolder(selectedFolderDatabaseId, oldFolderNameString, newFolderNameString, folderIconByteArray);
        }

        // Update `bookmarksCursor` with the current contents of this folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentBookmarksFolder);

        // Update the `ListView`.
        bookmarksCursorAdapter.changeCursor(bookmarksCursor);
    }

    @Override
    public void onHttpAuthenticationCancel() {
        // Cancel the `HttpAuthHandler`.
        httpAuthHandler.cancel();
    }

    @Override
    public void onHttpAuthenticationProceed(AppCompatDialogFragment dialogFragment) {
        // Get handles for the `EditTexts`.
        EditText usernameEditText = dialogFragment.getDialog().findViewById(R.id.http_authentication_username);
        EditText passwordEditText = dialogFragment.getDialog().findViewById(R.id.http_authentication_password);

        // Proceed with the HTTP authentication.
        httpAuthHandler.proceed(usernameEditText.getText().toString(), passwordEditText.getText().toString());
    }

    public void viewSslCertificate(View view) {
        // Show the `ViewSslCertificateDialog` `AlertDialog` and name this instance `@string/view_ssl_certificate`.
        DialogFragment viewSslCertificateDialogFragment = new ViewSslCertificateDialog();
        viewSslCertificateDialogFragment.show(getFragmentManager(), getString(R.string.view_ssl_certificate));
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
    public void onSslMismatchBack() {
        if (mainWebView.canGoBack()) {  // There is a back page in the history.
            // Reset the formatted URL string so the page will load correctly if blocking of third-party requests is enabled.
            formattedUrlString = "";

            // Set `navigatingHistory` so that the domain settings are applied when the new URL is loaded.
            navigatingHistory = true;

            // Go back.
            mainWebView.goBack();
        } else {  // There are no pages to go back to.
            // Load a blank page
            loadUrl("");
        }
    }

    @Override
    public void onSslMismatchProceed() {
        // Do not check the pinned SSL certificate for this domain again until the domain changes.
        ignorePinnedSslCertificate = true;
    }

    @Override
    public void onUrlHistoryEntrySelected(int moveBackOrForwardSteps) {
        // Reset the formatted URL string so the page will load correctly if blocking of third-party requests is enabled.
        formattedUrlString = "";

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
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {  // The navigation drawer is open.
            // Close the navigation drawer.
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (drawerLayout.isDrawerVisible(GravityCompat.END)){  // The bookmarks drawer is open.
            if (currentBookmarksFolder.isEmpty()) {  // The home folder is displayed.
                // close the bookmarks drawer.
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {  // A subfolder is displayed.
                // Place the former parent folder in `currentFolder`.
                currentBookmarksFolder = bookmarksDatabaseHelper.getParentFolder(currentBookmarksFolder);

                // Load the new folder.
                loadBookmarksFolder();
            }

        } else if (mainWebView.canGoBack()) {  // There is at least one item in the `WebView` history.
            // Reset the formatted URL string so the page will load correctly if blocking of third-party requests is enabled.
            formattedUrlString = "";

            // Set `navigatingHistory` so that the domain settings are applied when the new URL is loaded.
            navigatingHistory = true;

            // Go back.
            mainWebView.goBack();
        } else {  // There isn't anything to do in Privacy Browser.
            // Pass `onBackPressed()` to the system.
            super.onBackPressed();
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

    private void loadUrlFromTextBox() throws UnsupportedEncodingException {
        // Get the text from urlTextBox and convert it to a string.  trim() removes white spaces from the beginning and end of the string.
        String unformattedUrlString = urlTextBox.getText().toString().trim();

        // Check to see if `unformattedUrlString` is a valid URL.  Otherwise, convert it into a search.
        if ((Patterns.WEB_URL.matcher(unformattedUrlString).matches()) || (unformattedUrlString.startsWith("http://")) || (unformattedUrlString.startsWith("https://"))) {
            // Add `https://` at the beginning if it is missing.  Otherwise the app will segfault.
            if (!unformattedUrlString.startsWith("http")) {
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
        } else if (unformattedUrlString.isEmpty()){  // Load a blank web site.
            // Load a blank string.
            formattedUrlString = "";
        } else {  // Search for the contents of the URL box.
            // Sanitize the search input and convert it to a search.
            final String encodedUrlString = URLEncoder.encode(unformattedUrlString, "UTF-8");

            // Add the base search URL.
            formattedUrlString = searchURL + encodedUrlString;
        }

        // Clear the focus from the URL text box.  Otherwise, proximate typing in the box will retain the colorized formatting instead of being reset during refocus.
        urlTextBox.clearFocus();

        // Make it so.
        loadUrl(formattedUrlString);
    }

    private void loadUrl(String url) {// Apply any custom domain settings.
        // Set the URL as the formatted URL string so that checking third-party requests works correctly.
        formattedUrlString = url;

        // Apply the domain settings.
        applyDomainSettings(url, true, false);

        // If loading a website, set `urlIsLoading` to prevent changes in the user agent on websites with redirects from reloading the current website.
        urlIsLoading = !url.equals("");

        // Load the URL.
        mainWebView.loadUrl(url, customHeaders);
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
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Store the values from the shared preferences in variables.
        incognitoModeEnabled = sharedPreferences.getBoolean("incognito_mode", false);
        boolean doNotTrackEnabled = sharedPreferences.getBoolean("do_not_track", false);
        proxyThroughOrbot = sharedPreferences.getBoolean("proxy_through_orbot", false);
        fullScreenBrowsingModeEnabled = sharedPreferences.getBoolean("full_screen_browsing_mode", false);
        hideSystemBarsOnFullscreen = sharedPreferences.getBoolean("hide_system_bars", false);
        translucentNavigationBarOnFullscreen = sharedPreferences.getBoolean("translucent_navigation_bar", true);
        downloadWithExternalApp = sharedPreferences.getBoolean("download_with_external_app", false);

        // Apply the proxy through Orbot settings.
        applyProxyThroughOrbot(false);

        // Set Do Not Track status.
        if (doNotTrackEnabled) {
            customHeaders.put("DNT", "1");
        } else {
            customHeaders.remove("DNT");
        }

        // Apply the appropriate full screen mode the `SYSTEM_UI` flags.
        if (fullScreenBrowsingModeEnabled && inFullScreenBrowsingMode) {  // Privacy Browser is currently in full screen browsing mode.
            if (hideSystemBarsOnFullscreen) {  // Hide everything.
                // Remove the translucent navigation setting if it is currently flagged.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                // Remove the translucent status bar overlay.
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                // Remove the translucent status bar overlay on the `Drawer Layout`, which is special and needs its own command.
                drawerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                /* SYSTEM_UI_FLAG_FULLSCREEN hides the status bar at the top of the screen.
                 * SYSTEM_UI_FLAG_HIDE_NAVIGATION hides the navigation bar on the bottom or right of the screen.
                 * SYSTEM_UI_FLAG_IMMERSIVE_STICKY makes the status and navigation bars translucent and automatically re-hides them after they are shown.
                 */
                rootCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {  // Hide everything except the status and navigation bars.
                // Remove any `SYSTEM_UI` flags from `rootCoordinatorLayout`.
                rootCoordinatorLayout.setSystemUiVisibility(0);

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
        } else {  // Privacy Browser is not in full screen browsing mode.
            // Reset the full screen tracker, which could be true if Privacy Browser was in full screen mode before entering settings and full screen browsing was disabled.
            inFullScreenBrowsingMode = false;

            // Show the `appBar` if `findOnPageLinearLayout` is not visible.
            if (findOnPageLinearLayout.getVisibility() == View.GONE) {
                appBar.show();
            }

            // Show the `BannerAd` in the free flavor.
            if (BuildConfig.FLAVOR.contentEquals("free")) {
                // Initialize the ad.  The AdView is destroyed and recreated, which changes the ID, every time it is reloaded to handle possible rotations.
                AdHelper.initializeAds(findViewById(R.id.adview), getApplicationContext(), getFragmentManager(), getString(R.string.google_app_id), getString(R.string.ad_unit_id));
            }

            // Remove any `SYSTEM_UI` flags from `rootCoordinatorLayout`.
            rootCoordinatorLayout.setSystemUiVisibility(0);

            // Remove the translucent navigation bar flag if it is set.
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            // Add the translucent status flag if it is unset.  This also resets `drawerLayout's` `View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN`.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Constrain `rootCoordinatorLayout` inside the status and navigation bars.
            rootCoordinatorLayout.setFitsSystemWindows(true);
        }
    }

    // `reloadWebsite` is used if returning from the Domains activity.  Otherwise JavaScript might not function correctly if it is newly enabled.
    // The deprecated `.getDrawable()` must be used until the minimum API >= 21.
    @SuppressWarnings("deprecation")
    private boolean applyDomainSettings(String url, boolean resetFavoriteIcon, boolean reloadWebsite) {
        // Get the current user agent.
        String initialUserAgent = mainWebView.getSettings().getUserAgentString();

        // Initialize a variable to track if the user agent changes.
        boolean userAgentChanged = false;

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

        // Strings don't like to be null.
        if (hostName == null) {
            hostName = "";
        }

        // Only apply the domain settings if a new domain is being loaded.  This allows the user to set temporary settings for JavaScript, cookies, DOM storage, etc.
        if (loadingNewDomainName) {
            // Set the new `hostname` as the `currentDomainName`.
            currentDomainName = hostName;

            // Reset `ignorePinnedSslCertificate`.
            ignorePinnedSslCertificate = false;

            // Reset the favorite icon if specified.
            if (resetFavoriteIcon) {
                favoriteIconBitmap = favoriteIconDefaultBitmap;
                favoriteIconImageView.setImageBitmap(Bitmap.createScaledBitmap(favoriteIconBitmap, 64, 64, true));
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

            // Initialize variables to track if domain settings will be applied and, if so, under which name.
            domainSettingsApplied = false;
            String domainNameInDatabase = null;

            // Check the hostname.
            if (domainSettingsSet.contains(hostName)) {
                domainSettingsApplied = true;
                domainNameInDatabase = hostName;
            }

            // Check all the subdomains of the host name against wildcard domains in the domain cursor.
            while (!domainSettingsApplied && hostName.contains(".")) {  // Stop checking if domain settings are already applied or there are no more `.` in the host name.
                if (domainSettingsSet.contains("*." + hostName)) {  // Check the host name prepended by `*.`.
                    // Apply the domain settings.
                    domainSettingsApplied = true;

                    // Store the applied domain names as it appears in the database.
                    domainNameInDatabase = "*." + hostName;
                }

                // Strip out the lowest subdomain of of the host name.
                hostName = hostName.substring(hostName.indexOf(".") + 1);
            }


            // Get a handle for the shared preference.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            // Store the general preference information.
            String defaultFontSizeString = sharedPreferences.getString("font_size", getString(R.string.font_size_default_value));
            String defaultUserAgentName = sharedPreferences.getString("user_agent", getString(R.string.user_agent_default_value));
            defaultCustomUserAgentString = sharedPreferences.getString("custom_user_agent", getString(R.string.custom_user_agent_default_value));
            boolean defaultSwipeToRefresh = sharedPreferences.getBoolean("swipe_to_refresh", true);
            nightMode = sharedPreferences.getBoolean("night_mode", false);
            boolean displayWebpageImages = sharedPreferences.getBoolean("display_webpage_images", true);

            if (domainSettingsApplied) {  // The url has custom domain settings.
                // Get a cursor for the current host and move it to the first position.
                Cursor currentHostDomainSettingsCursor = domainsDatabaseHelper.getCursorForDomainName(domainNameInDatabase);
                currentHostDomainSettingsCursor.moveToFirst();

                // Get the settings from the cursor.
                domainSettingsDatabaseId = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper._ID)));
                javaScriptEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_JAVASCRIPT)) == 1);
                firstPartyCookiesEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES)) == 1);
                thirdPartyCookiesEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES)) == 1);
                domStorageEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_DOM_STORAGE)) == 1);
                // Form data can be removed once the minimum API >= 26.
                saveFormDataEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FORM_DATA)) == 1);
                easyListEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYLIST)) == 1);
                easyPrivacyEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYPRIVACY)) == 1);
                fanboysAnnoyanceListEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_ANNOYANCE_LIST)) == 1);
                fanboysSocialBlockingListEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_SOCIAL_BLOCKING_LIST)) == 1);
                ultraPrivacyEnabled = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_ULTRAPRIVACY)) == 1);
                blockAllThirdPartyRequests = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.BLOCK_ALL_THIRD_PARTY_REQUESTS)) == 1);
                String userAgentName = currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.USER_AGENT));
                int fontSize = currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE));
                int swipeToRefreshInt = currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SWIPE_TO_REFRESH));
                int nightModeInt = currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.NIGHT_MODE));
                int displayWebpageImagesInt = currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.DISPLAY_IMAGES));
                pinnedDomainSslCertificate = (currentHostDomainSettingsCursor.getInt(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.PINNED_SSL_CERTIFICATE)) == 1);
                pinnedDomainSslIssuedToCNameString = currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_COMMON_NAME));
                pinnedDomainSslIssuedToONameString = currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATION));
                pinnedDomainSslIssuedToUNameString = currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATIONAL_UNIT));
                pinnedDomainSslIssuedByCNameString = currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_COMMON_NAME));
                pinnedDomainSslIssuedByONameString = currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATION));
                pinnedDomainSslIssuedByUNameString = currentHostDomainSettingsCursor.getString(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATIONAL_UNIT));

                // Set `nightMode` according to `nightModeInt`.  If `nightModeInt` is `DomainsDatabaseHelper.NIGHT_MODE_SYSTEM_DEFAULT` the current setting from `sharedPreferences` will be used.
                switch (nightModeInt) {
                    case DomainsDatabaseHelper.NIGHT_MODE_ENABLED:
                        nightMode = true;
                        break;

                    case DomainsDatabaseHelper.NIGHT_MODE_DISABLED:
                        nightMode = false;
                        break;
                }

                // Store the domain JavaScript status.  This is used by the options menu night mode toggle.
                domainSettingsJavaScriptEnabled = javaScriptEnabled;

                // Enable JavaScript if night mode is enabled.
                if (nightMode) {
                    javaScriptEnabled = true;
                }

                // Set the pinned SSL certificate start date to `null` if the saved date `long` is 0.
                if (currentHostDomainSettingsCursor.getLong(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)) == 0) {
                    pinnedDomainSslStartDate = null;
                } else {
                    pinnedDomainSslStartDate = new Date(currentHostDomainSettingsCursor.getLong(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)));
                }

                // Set the pinned SSL certificate end date to `null` if the saved date `long` is 0.
                if (currentHostDomainSettingsCursor.getLong(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)) == 0) {
                    pinnedDomainSslEndDate = null;
                } else {
                    pinnedDomainSslEndDate = new Date(currentHostDomainSettingsCursor.getLong(currentHostDomainSettingsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)));
                }

                // Close `currentHostDomainSettingsCursor`.
                currentHostDomainSettingsCursor.close();

                // Apply the domain settings.
                mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);
                cookieManager.setAcceptCookie(firstPartyCookiesEnabled);
                mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);

                // Apply the form data setting if the API < 26.
                if (Build.VERSION.SDK_INT < 26) {
                    mainWebView.getSettings().setSaveFormData(saveFormDataEnabled);
                }

                // Apply the font size.
                if (fontSize == 0) {  // Apply the default font size.
                    mainWebView.getSettings().setTextZoom(Integer.valueOf(defaultFontSizeString));
                } else {  // Apply the specified font size.
                    mainWebView.getSettings().setTextZoom(fontSize);
                }

                // Set third-party cookies status if API >= 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);
                }

                // Only set the user agent if the webpage is not currently loading.  Otherwise, changing the user agent on redirects can cause the original website to reload.
                // <https://redmine.stoutner.com/issues/160>
                if (!urlIsLoading) {
                    // Set the user agent.
                    if (userAgentName.equals(getString(R.string.system_default_user_agent))) {  // Use the system default user agent.
                        // Get the array position of the default user agent name.
                        int defaultUserAgentArrayPosition = userAgentNamesArray.getPosition(defaultUserAgentName);

                        // Set the user agent according to the system default.
                        switch (defaultUserAgentArrayPosition) {
                            case UNRECOGNIZED_USER_AGENT:  // The default user agent name is not on the canonical list.
                                // This is probably because it was set in an older version of Privacy Browser before the switch to persistent user agent names.
                                mainWebView.getSettings().setUserAgentString(defaultUserAgentName);
                                break;

                            case SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                                // Set the user agent to `""`, which uses the default value.
                                mainWebView.getSettings().setUserAgentString("");
                                break;

                            case SETTINGS_CUSTOM_USER_AGENT:
                                // Set the custom user agent.
                                mainWebView.getSettings().setUserAgentString(defaultCustomUserAgentString);
                                break;

                            default:
                                // Get the user agent string from the user agent data array
                                mainWebView.getSettings().setUserAgentString(userAgentDataArray[defaultUserAgentArrayPosition]);
                        }
                    } else {  // Set the user agent according to the stored name.
                        // Get the array position of the user agent name.
                        int userAgentArrayPosition = userAgentNamesArray.getPosition(userAgentName);

                        switch (userAgentArrayPosition) {
                            case UNRECOGNIZED_USER_AGENT:  // The user agent name contains a custom user agent.
                                mainWebView.getSettings().setUserAgentString(userAgentName);
                                break;

                            case SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                                // Set the user agent to `""`, which uses the default value.
                                mainWebView.getSettings().setUserAgentString("");
                                break;

                            default:
                                // Get the user agent string from the user agent data array.
                                mainWebView.getSettings().setUserAgentString(userAgentDataArray[userAgentArrayPosition]);
                        }
                    }

                    // Store the applied user agent string, which is used in the View Source activity.
                    appliedUserAgentString = mainWebView.getSettings().getUserAgentString();

                    // Update the user agent change tracker.
                    userAgentChanged = !appliedUserAgentString.equals(initialUserAgent);
                }

                // Set swipe to refresh.
                switch (swipeToRefreshInt) {
                    case DomainsDatabaseHelper.SWIPE_TO_REFRESH_SYSTEM_DEFAULT:
                        // Set swipe to refresh according to the default.
                        swipeRefreshLayout.setEnabled(defaultSwipeToRefresh);
                        break;

                    case DomainsDatabaseHelper.SWIPE_TO_REFRESH_ENABLED:
                        // Enable swipe to refresh.
                        swipeRefreshLayout.setEnabled(true);
                        break;

                    case DomainsDatabaseHelper.SWIPE_TO_REFRESH_DISABLED:
                        // Disable swipe to refresh.
                        swipeRefreshLayout.setEnabled(false);
                }

                // Set the loading of webpage images.
                switch (displayWebpageImagesInt) {
                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_SYSTEM_DEFAULT:
                        mainWebView.getSettings().setLoadsImagesAutomatically(displayWebpageImages);
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_ENABLED:
                        mainWebView.getSettings().setLoadsImagesAutomatically(true);
                        break;

                    case DomainsDatabaseHelper.DISPLAY_WEBPAGE_IMAGES_DISABLED:
                        mainWebView.getSettings().setLoadsImagesAutomatically(false);
                        break;
                }

                // Set a green background on `urlTextBox` to indicate that custom domain settings are being used.  We have to use the deprecated `.getDrawable()` until the minimum API >= 21.
                if (darkTheme) {
                    urlAppBarRelativeLayout.setBackground(getResources().getDrawable(R.drawable.url_bar_background_dark_blue));
                } else {
                    urlAppBarRelativeLayout.setBackground(getResources().getDrawable(R.drawable.url_bar_background_light_green));
                }
            } else {  // The new URL does not have custom domain settings.  Load the defaults.
                // Store the values from `sharedPreferences` in variables.
                javaScriptEnabled = sharedPreferences.getBoolean("javascript", false);
                firstPartyCookiesEnabled = sharedPreferences.getBoolean("first_party_cookies", false);
                thirdPartyCookiesEnabled = sharedPreferences.getBoolean("third_party_cookies", false);
                domStorageEnabled = sharedPreferences.getBoolean("dom_storage", false);
                saveFormDataEnabled = sharedPreferences.getBoolean("save_form_data", false);  // Form data can be removed once the minimum API >= 26.
                easyListEnabled = sharedPreferences.getBoolean("easylist", true);
                easyPrivacyEnabled = sharedPreferences.getBoolean("easyprivacy", true);
                fanboysAnnoyanceListEnabled = sharedPreferences.getBoolean("fanboys_annoyance_list", true);
                fanboysSocialBlockingListEnabled = sharedPreferences.getBoolean("fanboys_social_blocking_list", true);
                ultraPrivacyEnabled = sharedPreferences.getBoolean("ultraprivacy", true);
                blockAllThirdPartyRequests = sharedPreferences.getBoolean("block_all_third_party_requests", false);

                // Set `javaScriptEnabled` to be `true` if `night_mode` is `true`.
                if (nightMode) {
                    javaScriptEnabled = true;
                }

                // Apply the default settings.
                mainWebView.getSettings().setJavaScriptEnabled(javaScriptEnabled);
                cookieManager.setAcceptCookie(firstPartyCookiesEnabled);
                mainWebView.getSettings().setDomStorageEnabled(domStorageEnabled);
                mainWebView.getSettings().setTextZoom(Integer.valueOf(defaultFontSizeString));
                swipeRefreshLayout.setEnabled(defaultSwipeToRefresh);

                // Apply the form data setting if the API < 26.
                if (Build.VERSION.SDK_INT < 26) {
                    mainWebView.getSettings().setSaveFormData(saveFormDataEnabled);
                }

                // Reset the pinned SSL certificate information.
                domainSettingsDatabaseId = -1;
                pinnedDomainSslCertificate = false;
                pinnedDomainSslIssuedToCNameString = "";
                pinnedDomainSslIssuedToONameString = "";
                pinnedDomainSslIssuedToUNameString = "";
                pinnedDomainSslIssuedByCNameString = "";
                pinnedDomainSslIssuedByONameString = "";
                pinnedDomainSslIssuedByUNameString = "";
                pinnedDomainSslStartDate = null;
                pinnedDomainSslEndDate = null;

                // Set third-party cookies status if API >= 21.
                if (Build.VERSION.SDK_INT >= 21) {
                    cookieManager.setAcceptThirdPartyCookies(mainWebView, thirdPartyCookiesEnabled);
                }

                // Only set the user agent if the webpage is not currently loading.  Otherwise, changing the user agent on redirects can cause the original website to reload.
                // <https://redmine.stoutner.com/issues/160>
                if (!urlIsLoading) {
                    // Get the array position of the user agent name.
                    int userAgentArrayPosition = userAgentNamesArray.getPosition(defaultUserAgentName);

                    // Set the user agent.
                    switch (userAgentArrayPosition) {
                        case UNRECOGNIZED_USER_AGENT:  // The default user agent name is not on the canonical list.
                            // This is probably because it was set in an older version of Privacy Browser before the switch to persistent user agent names.
                            mainWebView.getSettings().setUserAgentString(defaultUserAgentName);
                            break;

                        case SETTINGS_WEBVIEW_DEFAULT_USER_AGENT:
                            // Set the user agent to `""`, which uses the default value.
                            mainWebView.getSettings().setUserAgentString("");
                            break;

                        case SETTINGS_CUSTOM_USER_AGENT:
                            // Set the custom user agent.
                            mainWebView.getSettings().setUserAgentString(defaultCustomUserAgentString);
                            break;

                        default:
                            // Get the user agent string from the user agent data array
                            mainWebView.getSettings().setUserAgentString(userAgentDataArray[userAgentArrayPosition]);
                    }

                    // Store the applied user agent string, which is used in the View Source activity.
                    appliedUserAgentString = mainWebView.getSettings().getUserAgentString();

                    // Update the user agent change tracker.
                    userAgentChanged = !appliedUserAgentString.equals(initialUserAgent);
                }

                // Set the loading of webpage images.
                mainWebView.getSettings().setLoadsImagesAutomatically(displayWebpageImages);

                // Set a transparent background on `urlTextBox`.  We have to use the deprecated `.getDrawable()` until the minimum API >= 21.
                urlAppBarRelativeLayout.setBackgroundDrawable(getResources().getDrawable(R.color.transparent));
            }

            // Close the domains database helper.
            domainsDatabaseHelper.close();

            // Update the privacy icons, but only if `mainMenu` has already been populated.
            if (mainMenu != null) {
                updatePrivacyIcons(true);
            }
        }

        // Reload the website if returning from the Domains activity.
        if (reloadWebsite) {
            mainWebView.reload();
        }

        // Return the user agent changed status.
        return userAgentChanged;
    }

    private void applyProxyThroughOrbot(boolean reloadWebsite) {
        // Get a handle for the shared preferences.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the search preferences.
        String homepageString = sharedPreferences.getString("homepage", getString(R.string.homepage_default_value));
        String torHomepageString = sharedPreferences.getString("tor_homepage", getString(R.string.tor_homepage_default_value));
        String torSearchString = sharedPreferences.getString("tor_search", getString(R.string.tor_search_default_value));
        String torSearchCustomUrlString = sharedPreferences.getString("tor_search_custom_url", getString(R.string.tor_search_custom_url_default_value));
        String searchString = sharedPreferences.getString("search", getString(R.string.search_default_value));
        String searchCustomUrlString = sharedPreferences.getString("search_custom_url", getString(R.string.search_custom_url_default_value));

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
                searchURL = torSearchCustomUrlString;
            } else {  // Use the string from the pre-built list.
                searchURL = torSearchString;
            }

            // Set the proxy.  `this` refers to the current activity where an `AlertDialog` might be displayed.
            OrbotProxyHelper.setProxy(getApplicationContext(), this, "localhost", "8118");

            // Set the `appBar` background to indicate proxying through Orbot is enabled.  `this` refers to the context.
            if (darkTheme) {
                appBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.dark_blue_30));
            } else {
                appBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.blue_50));
            }

            // Check to see if Orbot is ready.
            if (!orbotStatus.equals("ON")) {  // Orbot is not ready.
                // Set `waitingForOrbot`.
                waitingForOrbot = true;

                // Disable the wide view port so that the waiting for Orbot text is displayed correctly.
                mainWebView.getSettings().setUseWideViewPort(false);

                // Load a waiting page.  `null` specifies no encoding, which defaults to ASCII.
                mainWebView.loadData(waitingForOrbotHtmlString, "text/html", null);
            } else if (reloadWebsite) {  // Orbot is ready and the website should be reloaded.
                // Reload the website.
                mainWebView.reload();
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
                searchURL = searchCustomUrlString;
            } else {  // Use the string from the pre-built list.
                searchURL = searchString;
            }

            // Reset the proxy to default.  The host is `""` and the port is `"0"`.
            OrbotProxyHelper.setProxy(getApplicationContext(), this, "", "0");

            // Set the default `appBar` background.  `this` refers to the context.
            if (darkTheme) {
                appBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.gray_900));
            } else {
                appBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.color.gray_100));
            }

            // Reset `waitingForOrbot.
            waitingForOrbot = false;

            // Reload the website if requested.
            if (reloadWebsite) {
                mainWebView.reload();
            }
        }
    }

    private void updatePrivacyIcons(boolean runInvalidateOptionsMenu) {
        // Get handles for the menu items.
        MenuItem privacyMenuItem = mainMenu.findItem(R.id.toggle_javascript);
        MenuItem firstPartyCookiesMenuItem = mainMenu.findItem(R.id.toggle_first_party_cookies);
        MenuItem domStorageMenuItem = mainMenu.findItem(R.id.toggle_dom_storage);
        MenuItem refreshMenuItem = mainMenu.findItem(R.id.refresh);

        // Update the privacy icon.
        if (javaScriptEnabled) {  // JavaScript is enabled.
            privacyMenuItem.setIcon(R.drawable.javascript_enabled);
        } else if (firstPartyCookiesEnabled) {  // JavaScript is disabled but cookies are enabled.
            privacyMenuItem.setIcon(R.drawable.warning);
        } else {  // All the dangerous features are disabled.
            privacyMenuItem.setIcon(R.drawable.privacy_mode);
        }

        // Update the first-party cookies icon.
        if (firstPartyCookiesEnabled) {  // First-party cookies are enabled.
            firstPartyCookiesMenuItem.setIcon(R.drawable.cookies_enabled);
        } else {  // First-party cookies are disabled.
            if (darkTheme) {
                firstPartyCookiesMenuItem.setIcon(R.drawable.cookies_disabled_dark);
            } else {
                firstPartyCookiesMenuItem.setIcon(R.drawable.cookies_disabled_light);
            }
        }

        // Update the DOM storage icon.
        if (javaScriptEnabled && domStorageEnabled) {  // Both JavaScript and DOM storage are enabled.
            domStorageMenuItem.setIcon(R.drawable.dom_storage_enabled);
        } else if (javaScriptEnabled) {  // JavaScript is enabled but DOM storage is disabled.
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

    private void openUrlWithExternalApp(String url) {
        // Create a download intent.  Not specifying the action type will display the maximum number of options.
        Intent downloadIntent = new Intent();

        // Set the URI and the mime type.  `"*/*"` will display the maximum number of options.
        downloadIntent.setDataAndType(Uri.parse(url), "text/html");

        // Flag the intent to open in a new task.
        downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Show the chooser.
        startActivity(Intent.createChooser(downloadIntent, getString(R.string.open_with)));
    }

    private void highlightUrlText() {
        // Get the URL string.
        String urlString = urlTextBox.getText().toString();

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
            urlTextBox.getText().setSpan(redColorSpan, 0, 7, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            // De-emphasize subdomains.
            if (penultimateDotIndex > 0)  {  // There is more than one subdomain in the domain name.
                urlTextBox.getText().setSpan(initialGrayColorSpan, 7, penultimateDotIndex + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        } else if (urlString.startsWith("https://")) {  // De-emphasize the protocol of connections that are encrypted.
            if (penultimateDotIndex > 0) {  // There is more than one subdomain in the domain name.
                // De-emphasize the protocol and the additional subdomains.
                urlTextBox.getText().setSpan(initialGrayColorSpan, 0, penultimateDotIndex + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            } else {  // There is only one subdomain in the domain name.
                // De-emphasize only the protocol.
                urlTextBox.getText().setSpan(initialGrayColorSpan, 0, 8, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }

        // De-emphasize the text after the domain name.
        if (endOfDomainName > 0) {
            urlTextBox.getText().setSpan(finalGrayColorSpan, endOfDomainName, urlString.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
    }

    private void loadBookmarksFolder() {
        // Update the bookmarks cursor with the contents of the bookmarks database for the current folder.
        bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursorByDisplayOrder(currentBookmarksFolder);

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

        // Populate the `ListView` with the adapter.
        bookmarksListView.setAdapter(bookmarksCursorAdapter);

        // Set the bookmarks drawer title.
        if (currentBookmarksFolder.isEmpty()) {
            bookmarksTitleTextView.setText(R.string.bookmarks);
        } else {
            bookmarksTitleTextView.setText(currentBookmarksFolder);
        }
    }
}