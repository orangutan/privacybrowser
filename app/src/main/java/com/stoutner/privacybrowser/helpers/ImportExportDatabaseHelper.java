/*
 * Copyright © 2018 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import java.io.File;

public class ImportExportDatabaseHelper {
    public static final String EXPORT_SUCCESSFUL = "Export Successful";
    public static final String IMPORT_SUCCESSFUL = "Import Successful";

    private static final int SCHEMA_VERSION = 1;
    private static final String PREFERENCES_TABLE = "preferences";

    // The preferences constants.
    private static final String _ID = "_id";
    private static final String JAVASCRIPT = "javascript";
    private static final String FIRST_PARTY_COOKIES = "first_party_cookies";
    private static final String THIRD_PARTY_COOKIES = "third_party_cookies";
    private static final String DOM_STORAGE = "dom_storage";
    private static final String SAVE_FORM_DATA = "save_form_data";
    private static final String USER_AGENT = "user_agent";
    private static final String CUSTOM_USER_AGENT = "custom_user_agent";
    private static final String INCOGNITO_MODE = "incognito_mode";
    private static final String DO_NOT_TRACK = "do_not_track";
    private static final String ALLOW_SCREENSHOTS = "allow_screenshots";
    private static final String EASYLIST = "easylist";
    private static final String EASYPRIVACY = "easyprivacy";
    private static final String FANBOYS_ANNOYANCE_LIST = "fanboys_annoyance_list";
    private static final String FANBOYS_SOCIAL_BLOCKING_LIST = "fanboys_social_blocking_list";
    private static final String ULTRAPRIVACY = "ultraprivacy";
    private static final String BLOCK_ALL_THIRD_PARTY_REQUESTS = "block_all_third_party_requests";
    private static final String PROXY_THROUGH_ORBOT = "proxy_through_orbot";
    private static final String TOR_HOMEPAGE = "tor_homepage";
    private static final String TOR_SEARCH = "tor_search";
    private static final String TOR_SEARCH_CUSTOM_URL = "tor_search_custom_url";
    private static final String SEARCH = "search";
    private static final String SEARCH_CUSTOM_URL = "search_custom_url";
    private static final String FULL_SCREEN_BROWSING_MODE = "full_screen_browsing_mode";
    private static final String HIDE_SYSTEM_BARS = "hide_system_bars";
    private static final String TRANSLUCENT_NAVIGATION_BAR = "translucent_navigation_bar";
    private static final String CLEAR_EVERYTHING = "clear_everything";
    private static final String CLEAR_COOKIES = "clear_cookies";
    private static final String CLEAR_DOM_STORAGE = "clear_dom_storage";
    private static final String CLEAR_FORM_DATA = "clear_form_data";
    private static final String CLEAR_CACHE = "clear_cache";
    private static final String HOMEPAGE = "homepage";
    private static final String DEFAULT_FONT_SIZE = "default_font_size";
    private static final String SWIPE_TO_REFRESH = "swipe_to_refresh";
    private static final String DISPLAY_ADDITIONAL_APP_BAR_ICONS = "display_additional_app_bar_icons";
    private static final String DARK_THEME = "dark_theme";
    private static final String NIGHT_MODE = "night_mode";
    private static final String DISPLAY_WEBPAGE_IMAGES = "display_webpage_images";

    public String exportUnencrypted(File databaseFile, Context context) {
        try {
            // Delete the current file if it exists.
            if (databaseFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                databaseFile.delete();
            }

            // Create the export database.
            SQLiteDatabase exportDatabase = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);

            // Set the export database version number.
            exportDatabase.setVersion(SCHEMA_VERSION);

            // Create the export database domains table.
            exportDatabase.execSQL(DomainsDatabaseHelper.CREATE_DOMAINS_TABLE);

            // Open the domains database.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
            DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(context, null, null, 0);

            // Get a full domains database cursor.
            Cursor domainsCursor = domainsDatabaseHelper.getCompleteCursorOrderedByDomain();

            // Move to the first domain.
            domainsCursor.moveToFirst();

            // Copy the data from the domains cursor into the export database.
            for (int i = 0; i < domainsCursor.getCount(); i++) {
                // Extract the record from the cursor and store the data in a ContentValues.
                ContentValues domainsContentValues = new ContentValues();
                domainsContentValues.put(DomainsDatabaseHelper.DOMAIN_NAME, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_JAVASCRIPT, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_JAVASCRIPT)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_DOM_STORAGE, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_DOM_STORAGE)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FORM_DATA, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FORM_DATA)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_EASYLIST, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYLIST)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_EASYPRIVACY, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYPRIVACY)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FANBOYS_ANNOYANCE_LIST, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_ANNOYANCE_LIST)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FANBOYS_SOCIAL_BLOCKING_LIST, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_SOCIAL_BLOCKING_LIST)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_ULTRAPRIVACY, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_ULTRAPRIVACY)));
                domainsContentValues.put(DomainsDatabaseHelper.BLOCK_ALL_THIRD_PARTY_REQUESTS, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.BLOCK_ALL_THIRD_PARTY_REQUESTS)));
                domainsContentValues.put(DomainsDatabaseHelper.USER_AGENT, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.USER_AGENT)));
                domainsContentValues.put(DomainsDatabaseHelper.FONT_SIZE, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE)));
                domainsContentValues.put(DomainsDatabaseHelper.SWIPE_TO_REFRESH, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SWIPE_TO_REFRESH)));
                domainsContentValues.put(DomainsDatabaseHelper.NIGHT_MODE, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.NIGHT_MODE)));
                domainsContentValues.put(DomainsDatabaseHelper.DISPLAY_IMAGES, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.DISPLAY_IMAGES)));
                domainsContentValues.put(DomainsDatabaseHelper.PINNED_SSL_CERTIFICATE, domainsCursor.getInt(domainsCursor.getColumnIndex(DomainsDatabaseHelper.PINNED_SSL_CERTIFICATE)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_TO_COMMON_NAME, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_COMMON_NAME)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATION, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATION)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATIONAL_UNIT, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATIONAL_UNIT)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_BY_COMMON_NAME, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_COMMON_NAME)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATION, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATION)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATIONAL_UNIT, domainsCursor.getString(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATIONAL_UNIT)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_START_DATE, domainsCursor.getLong(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_END_DATE, domainsCursor.getLong(domainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)));

                // Insert the record into the export database.
                exportDatabase.insert(DomainsDatabaseHelper.DOMAINS_TABLE, null, domainsContentValues);

                // Advance to the next record.
                domainsCursor.moveToNext();
            }

            // Close the domains database.
            domainsCursor.close();
            domainsDatabaseHelper.close();


            // Create the export database bookmarks table.
            exportDatabase.execSQL(BookmarksDatabaseHelper.CREATE_BOOKMARKS_TABLE);

            // Open the bookmarks database.  The `0` specifies the database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
            BookmarksDatabaseHelper bookmarksDatabaseHelper = new BookmarksDatabaseHelper(context, null, null, 0);

            // Get a full bookmarks cursor.
            Cursor bookmarksCursor = bookmarksDatabaseHelper.getAllBookmarksCursor();

            // Move to the first bookmark.
            bookmarksCursor.moveToFirst();

            // Copy the data from the bookmarks cursor into the export database.
            for (int i = 0; i < bookmarksCursor.getCount(); i++) {
                // Extract the record from the cursor and store the data in a ContentValues.
                ContentValues bookmarksContentValues = new ContentValues();
                bookmarksContentValues.put(BookmarksDatabaseHelper.BOOKMARK_NAME, bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.BOOKMARK_URL, bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.PARENT_FOLDER, bookmarksCursor.getString(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.DISPLAY_ORDER, bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.IS_FOLDER, bookmarksCursor.getInt(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.FAVORITE_ICON, bookmarksCursor.getBlob(bookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON)));

                // Insert the record into the export database.
                exportDatabase.insert(BookmarksDatabaseHelper.BOOKMARKS_TABLE, null, bookmarksContentValues);

                // Advance to the next record.
                bookmarksCursor.moveToNext();
            }

            // Close the bookmarks database.
            bookmarksCursor.close();
            bookmarksDatabaseHelper.close();


            // Prepare the preferences table SQL creation string.
            String CREATE_PREFERENCES_TABLE = "CREATE TABLE " + PREFERENCES_TABLE + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    JAVASCRIPT + " BOOLEAN, " +
                    FIRST_PARTY_COOKIES + " BOOLEAN, " +
                    THIRD_PARTY_COOKIES + " BOOLEAN, " +
                    DOM_STORAGE + " BOOLEAN, " +
                    SAVE_FORM_DATA + " BOOLEAN, " +
                    USER_AGENT + " TEXT, " +
                    CUSTOM_USER_AGENT + " TEXT, " +
                    INCOGNITO_MODE + " BOOLEAN, " +
                    DO_NOT_TRACK + " BOOLEAN, " +
                    ALLOW_SCREENSHOTS + " BOOLEAN, " +
                    EASYLIST + " BOOLEAN, " +
                    EASYPRIVACY + " BOOLEAN, " +
                    FANBOYS_ANNOYANCE_LIST + " BOOLEAN, " +
                    FANBOYS_SOCIAL_BLOCKING_LIST + " BOOLEAN, " +
                    ULTRAPRIVACY + " BOOLEAN, " +
                    BLOCK_ALL_THIRD_PARTY_REQUESTS + " BOOLEAN, " +
                    PROXY_THROUGH_ORBOT + " BOOLEAN, " +
                    TOR_HOMEPAGE + " TEXT, " +
                    TOR_SEARCH + " TEXT, " +
                    TOR_SEARCH_CUSTOM_URL + " TEXT, " +
                    SEARCH + " TEXT, " +
                    SEARCH_CUSTOM_URL + " TEXT, " +
                    FULL_SCREEN_BROWSING_MODE + " BOOLEAN, " +
                    HIDE_SYSTEM_BARS + " BOOLEAN, " +
                    TRANSLUCENT_NAVIGATION_BAR + " BOOLEAN, " +
                    CLEAR_EVERYTHING + " BOOLEAN, " +
                    CLEAR_COOKIES + " BOOLEAN, " +
                    CLEAR_DOM_STORAGE + " BOOLEAN, " +
                    CLEAR_FORM_DATA + " BOOLEAN, " +
                    CLEAR_CACHE + " BOOLEAN, " +
                    HOMEPAGE + " TEXT, " +
                    DEFAULT_FONT_SIZE + " TEXT, " +
                    SWIPE_TO_REFRESH + " BOOLEAN, " +
                    DISPLAY_ADDITIONAL_APP_BAR_ICONS + " BOOLEAN, " +
                    DARK_THEME + " BOOLEAN, " +
                    NIGHT_MODE + " BOOLEAN, " +
                    DISPLAY_WEBPAGE_IMAGES + " BOOLEAN)";

            // Create the export database preferences table.
            exportDatabase.execSQL(CREATE_PREFERENCES_TABLE);

            // Get a handle for the shared preference.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            // Create a ContentValues with the preferences information.
            ContentValues preferencesContentValues = new ContentValues();
            preferencesContentValues.put(JAVASCRIPT, sharedPreferences.getBoolean("javascript_enabled", false));
            preferencesContentValues.put(FIRST_PARTY_COOKIES, sharedPreferences.getBoolean("first_party_cookies_enabled", false));
            preferencesContentValues.put(THIRD_PARTY_COOKIES, sharedPreferences.getBoolean("third_party_cookies_enabled", false));
            preferencesContentValues.put(DOM_STORAGE, sharedPreferences.getBoolean("dom_storage_enabled", false));
            preferencesContentValues.put(SAVE_FORM_DATA, sharedPreferences.getBoolean("save_form_data_enabled", false));  // Save form data can be removed once the minimum API >= 26.
            preferencesContentValues.put(USER_AGENT, sharedPreferences.getString("user_agent", "Privacy Browser"));
            preferencesContentValues.put(CUSTOM_USER_AGENT, sharedPreferences.getString("custom_user_agent", "PrivacyBrowser/1.0"));
            preferencesContentValues.put(INCOGNITO_MODE, sharedPreferences.getBoolean("incognito_mode", false));
            preferencesContentValues.put(DO_NOT_TRACK, sharedPreferences.getBoolean("do_not_track", false));
            preferencesContentValues.put(ALLOW_SCREENSHOTS, sharedPreferences.getBoolean("allow_screenshots", false));
            preferencesContentValues.put(EASYLIST, sharedPreferences.getBoolean("easylist", true));
            preferencesContentValues.put(EASYPRIVACY, sharedPreferences.getBoolean("easyprivacy", true));
            preferencesContentValues.put(FANBOYS_ANNOYANCE_LIST, sharedPreferences.getBoolean("fanboy_annoyance_list", true));
            preferencesContentValues.put(FANBOYS_SOCIAL_BLOCKING_LIST, sharedPreferences.getBoolean("fanboy_social_blocking_list", true));
            preferencesContentValues.put(ULTRAPRIVACY, sharedPreferences.getBoolean("ultraprivacy", true));
            preferencesContentValues.put(BLOCK_ALL_THIRD_PARTY_REQUESTS, sharedPreferences.getBoolean("block_all_third_party_requests", false));
            preferencesContentValues.put(PROXY_THROUGH_ORBOT, sharedPreferences.getBoolean("proxy_through_orbot", false));
            preferencesContentValues.put(TOR_HOMEPAGE, sharedPreferences.getString("tor_homepage", "http://ulrn6sryqaifefld.onion/"));
            preferencesContentValues.put(TOR_SEARCH, sharedPreferences.getString("tor_search", "http://ulrn6sryqaifefld.onion/?q="));
            preferencesContentValues.put(TOR_SEARCH_CUSTOM_URL, sharedPreferences.getString("tor_search_custom_url", ""));
            preferencesContentValues.put(SEARCH, sharedPreferences.getString("search", "https://searx.me/?q="));
            preferencesContentValues.put(SEARCH_CUSTOM_URL, sharedPreferences.getString("search_custom_url", ""));
            preferencesContentValues.put(FULL_SCREEN_BROWSING_MODE, sharedPreferences.getBoolean("full_screen_browsing_mode", false));
            preferencesContentValues.put(HIDE_SYSTEM_BARS, sharedPreferences.getBoolean("hide_system_bars", false));
            preferencesContentValues.put(TRANSLUCENT_NAVIGATION_BAR, sharedPreferences.getBoolean("translucent_navigation_bar", true));
            preferencesContentValues.put(CLEAR_EVERYTHING, sharedPreferences.getBoolean("clear_everything", true));
            preferencesContentValues.put(CLEAR_COOKIES, sharedPreferences.getBoolean("clear_cookies", true));
            preferencesContentValues.put(CLEAR_DOM_STORAGE, sharedPreferences.getBoolean("clear_dom_storage", true));
            preferencesContentValues.put(CLEAR_FORM_DATA, sharedPreferences.getBoolean("clear_form_data", true));  // Clear form data can be removed once the minimum API >= 26.
            preferencesContentValues.put(CLEAR_CACHE, sharedPreferences.getBoolean("clear_cache", true));
            preferencesContentValues.put(HOMEPAGE, sharedPreferences.getString("homepage", "https://searx.me"));
            preferencesContentValues.put(DEFAULT_FONT_SIZE, sharedPreferences.getString("default_font_size", "100"));
            preferencesContentValues.put(SWIPE_TO_REFRESH, sharedPreferences.getBoolean("swipe_to_refresh", true));
            preferencesContentValues.put(DISPLAY_ADDITIONAL_APP_BAR_ICONS, sharedPreferences.getBoolean("display_additional_app_bar_icons", false));
            preferencesContentValues.put(DARK_THEME, sharedPreferences.getBoolean("dark_theme", false));
            preferencesContentValues.put(NIGHT_MODE, sharedPreferences.getBoolean("night_mode", false));
            preferencesContentValues.put(DISPLAY_WEBPAGE_IMAGES, sharedPreferences.getBoolean("display_webpage_images", true));

            // Insert the preferences into the export database.
            exportDatabase.insert(PREFERENCES_TABLE, null, preferencesContentValues);

            // Close the export database.
            exportDatabase.close();

            // Convert the database file to a string.
            String databaseString = databaseFile.toString();

            // Create strings for the temporary database files.
            String journalFileString = databaseString + "-journal";

            // Get `Files` for the temporary database files.
            File journalFile = new File(journalFileString);

            // Delete the Journal file if it exists.
            if (journalFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                journalFile.delete();
            }

            // Export successful.
            return EXPORT_SUCCESSFUL;
        } catch (Exception exception) {
            // Return the export error.
            return exception.toString();
        }
    }

    public String importUnencrypted(File databaseFile, Context context){
        try {
            // Convert the database file to a string.  Once API >= 27 the file can be opened directly.
            String databaseString = databaseFile.toString();

            // Open the import database.
            SQLiteDatabase importDatabase = SQLiteDatabase.openDatabase(databaseString, null, SQLiteDatabase.OPEN_READONLY);

            // Get a cursor for the domains table.
            Cursor importDomainsCursor = importDatabase.rawQuery("SELECT * FROM " + DomainsDatabaseHelper.DOMAINS_TABLE + " ORDER BY " + DomainsDatabaseHelper.DOMAIN_NAME + " ASC", null);

            // Delete the current domains database.
            context.deleteDatabase(DomainsDatabaseHelper.DOMAINS_DATABASE);

            // Create a new domains database.  The `0` specifies the database version, but that is ignored and set instead using a constant in `DomainsDatabaseHelper`.
            DomainsDatabaseHelper domainsDatabaseHelper = new DomainsDatabaseHelper(context, null, null, 0);

            // Move to the first domain.
            importDomainsCursor.moveToFirst();

            // Copy the data from the import domains cursor into the domains database.
            for (int i = 0; i < importDomainsCursor.getCount(); i++) {
                // Extract the record from the cursor and store the data in a ContentValues.
                ContentValues domainsContentValues = new ContentValues();
                domainsContentValues.put(DomainsDatabaseHelper.DOMAIN_NAME, importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.DOMAIN_NAME)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_JAVASCRIPT, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_JAVASCRIPT)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FIRST_PARTY_COOKIES)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_THIRD_PARTY_COOKIES)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_DOM_STORAGE, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_DOM_STORAGE)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FORM_DATA, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FORM_DATA)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_EASYLIST, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYLIST)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_EASYPRIVACY, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_EASYPRIVACY)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FANBOYS_ANNOYANCE_LIST,
                        importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_ANNOYANCE_LIST)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_FANBOYS_SOCIAL_BLOCKING_LIST,
                        importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_FANBOYS_SOCIAL_BLOCKING_LIST)));
                domainsContentValues.put(DomainsDatabaseHelper.ENABLE_ULTRAPRIVACY, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.ENABLE_ULTRAPRIVACY)));
                domainsContentValues.put(DomainsDatabaseHelper.BLOCK_ALL_THIRD_PARTY_REQUESTS,
                        importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.BLOCK_ALL_THIRD_PARTY_REQUESTS)));
                domainsContentValues.put(DomainsDatabaseHelper.USER_AGENT, importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.USER_AGENT)));
                domainsContentValues.put(DomainsDatabaseHelper.FONT_SIZE, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.FONT_SIZE)));
                domainsContentValues.put(DomainsDatabaseHelper.SWIPE_TO_REFRESH, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SWIPE_TO_REFRESH)));
                domainsContentValues.put(DomainsDatabaseHelper.NIGHT_MODE, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.NIGHT_MODE)));
                domainsContentValues.put(DomainsDatabaseHelper.DISPLAY_IMAGES, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.DISPLAY_IMAGES)));
                domainsContentValues.put(DomainsDatabaseHelper.PINNED_SSL_CERTIFICATE, importDomainsCursor.getInt(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.PINNED_SSL_CERTIFICATE)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_TO_COMMON_NAME, importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_COMMON_NAME)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATION, importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATION)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATIONAL_UNIT,
                        importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_TO_ORGANIZATIONAL_UNIT)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_BY_COMMON_NAME, importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_COMMON_NAME)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATION, importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATION)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATIONAL_UNIT,
                        importDomainsCursor.getString(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_ISSUED_BY_ORGANIZATIONAL_UNIT)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_START_DATE, importDomainsCursor.getLong(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_START_DATE)));
                domainsContentValues.put(DomainsDatabaseHelper.SSL_END_DATE, importDomainsCursor.getLong(importDomainsCursor.getColumnIndex(DomainsDatabaseHelper.SSL_END_DATE)));

                // Insert the record into the export database.
                domainsDatabaseHelper.addDomain(domainsContentValues);

                // Advance to the next record.
                importDomainsCursor.moveToNext();
            }

            // Close the domains cursor.
            importDomainsCursor.close();

            // Close the domains database.
            domainsDatabaseHelper.close();


            // Get a cursor for the bookmarks table.
            Cursor importBookmarksCursor = importDatabase.rawQuery("SELECT * FROM " + BookmarksDatabaseHelper.BOOKMARKS_TABLE, null);

            // Delete the current bookmarks database.
            context.deleteDatabase(BookmarksDatabaseHelper.BOOKMARKS_DATABASE);

            // Create a new bookmarks database.  The `0` specifies the database version, but that is ignored and set instead using a constant in `BookmarksDatabaseHelper`.
            BookmarksDatabaseHelper bookmarksDatabaseHelper = new BookmarksDatabaseHelper(context, null, null, 0);

            // Move to the first bookmark.
            importBookmarksCursor.moveToFirst();

            // Copy the data from the import bookmarks cursor into the bookmarks database.
            for (int i = 0; i < importBookmarksCursor.getCount(); i++) {
                // Extract the record from the cursor and store the data in a ContentValues.
                ContentValues bookmarksContentValues = new ContentValues();
                bookmarksContentValues.put(BookmarksDatabaseHelper.BOOKMARK_NAME, importBookmarksCursor.getString(importBookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_NAME)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.BOOKMARK_URL, importBookmarksCursor.getString(importBookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.BOOKMARK_URL)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.PARENT_FOLDER, importBookmarksCursor.getString(importBookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.PARENT_FOLDER)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.DISPLAY_ORDER, importBookmarksCursor.getInt(importBookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.DISPLAY_ORDER)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.IS_FOLDER, importBookmarksCursor.getInt(importBookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.IS_FOLDER)));
                bookmarksContentValues.put(BookmarksDatabaseHelper.FAVORITE_ICON, importBookmarksCursor.getBlob(importBookmarksCursor.getColumnIndex(BookmarksDatabaseHelper.FAVORITE_ICON)));

                // Insert the record into the export database.
                bookmarksDatabaseHelper.createBookmark(bookmarksContentValues);

                // Advance to the next record.
                importBookmarksCursor.moveToNext();
            }

            // Close the bookmarks cursor.
            importBookmarksCursor.close();

            // Close the bookmarks database.
            bookmarksDatabaseHelper.close();


            // Get a handle for the shared preference.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            // Get a cursor for the bookmarks table.
            Cursor importPreferencesCursor = importDatabase.rawQuery("SELECT * FROM " + PREFERENCES_TABLE, null);

            // Move to the first preference.
            importPreferencesCursor.moveToFirst();

            // Import the preference data.
            sharedPreferences.edit()
                    .putBoolean("javascript_enabled", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(JAVASCRIPT)) == 1)
                    .putBoolean("first_party_cookies_enabled", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(FIRST_PARTY_COOKIES)) == 1)
                    .putBoolean("third_party_cookies_enabled", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(THIRD_PARTY_COOKIES)) == 1)
                    .putBoolean("dom_storage_enabled", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DOM_STORAGE)) == 1)
                    // Save form data can be removed once the minimum API >= 26.
                    .putBoolean("save_form_data_enabled", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(SAVE_FORM_DATA)) == 1)
                    .putString("user_agent", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(USER_AGENT)))
                    .putString("custom_user_agent", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(CUSTOM_USER_AGENT)))
                    .putBoolean("incognito_mode", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(INCOGNITO_MODE)) == 1)
                    .putBoolean("do_not_track", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DO_NOT_TRACK)) == 1)
                    .putBoolean("allow_screenshots", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(ALLOW_SCREENSHOTS)) == 1)
                    .putBoolean("easylist", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(EASYLIST)) == 1)
                    .putBoolean("easyprivacy", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(EASYPRIVACY)) == 1)
                    .putBoolean("fanboy_annoyance_list", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(FANBOYS_ANNOYANCE_LIST)) == 1)
                    .putBoolean("fanboy_social_blocking_list", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(FANBOYS_SOCIAL_BLOCKING_LIST)) == 1)
                    .putBoolean("ultraprivacy", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(ULTRAPRIVACY)) == 1)
                    .putBoolean("block_all_third_party_requests", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(BLOCK_ALL_THIRD_PARTY_REQUESTS)) == 1)
                    .putBoolean("proxy_through_orbot", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(PROXY_THROUGH_ORBOT)) == 1)
                    .putString("tor_homepage", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(TOR_HOMEPAGE)))
                    .putString("tor_search", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(TOR_SEARCH)))
                    .putString("tor_search_custom_url", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(TOR_SEARCH_CUSTOM_URL)))
                    .putString("search", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(SEARCH)))
                    .putString("search_custom_url", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(SEARCH_CUSTOM_URL)))
                    .putBoolean("full_screen_browsing_mode", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(FULL_SCREEN_BROWSING_MODE)) == 1)
                    .putBoolean("hide_system_bars", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(HIDE_SYSTEM_BARS)) == 1)
                    .putBoolean("translucent_navigation_bar", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(TRANSLUCENT_NAVIGATION_BAR)) == 1)
                    .putBoolean("clear_everything", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_EVERYTHING)) == 1)
                    .putBoolean("clear_cookies", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_COOKIES)) == 1)
                    .putBoolean("clear_dom_storage", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_DOM_STORAGE)) == 1)
                    // Clear form data can be removed once the minimum API >= 26.
                    .putBoolean("clear_form_data", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_FORM_DATA)) == 1)
                    .putBoolean("clear_cache", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_CACHE)) == 1)
                    .putString("homepage", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(HOMEPAGE)))
                    .putString("default_font_size", importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(DEFAULT_FONT_SIZE)))
                    .putBoolean("swipe_to_refresh", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(SWIPE_TO_REFRESH)) == 1)
                    .putBoolean("display_additional_app_bar_icons", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DISPLAY_ADDITIONAL_APP_BAR_ICONS)) == 1)
                    .putBoolean("dark_theme", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DARK_THEME)) == 1)
                    .putBoolean("night_mode", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(NIGHT_MODE)) == 1)
                    .putBoolean("display_webpage_images", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DISPLAY_WEBPAGE_IMAGES)) == 1).apply();

            // Close the preferences cursor.
            importPreferencesCursor.close();


            // Close the import database.
            importDatabase.close();

            // Create strings for the temporary database files.
            String shmFileString = databaseString + "-shm";
            String walFileString = databaseString + "-wal";
            String journalFileString = databaseString + "-journal";

            // Get `Files` for the temporary database files.
            File shmFile = new File(shmFileString);
            File walFile = new File(walFileString);
            File journalFile = new File(journalFileString);

            // Delete the Shared Memory file if it exists.
            if (shmFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                shmFile.delete();
            }

            // Delete the Write Ahead Log file if it exists.
            if (walFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                walFile.delete();
            }

            // Delete the Journal file if it exists.
            if (journalFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                journalFile.delete();
            }

            // Import successful.
            return IMPORT_SUCCESSFUL;
        } catch (Exception exception) {
            // Return the import error.
            return exception.toString();
        }
    }
}
