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

import com.stoutner.privacybrowser.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImportExportDatabaseHelper {
    public static final String EXPORT_SUCCESSFUL = "Export Successful";
    public static final String IMPORT_SUCCESSFUL = "Import Successful";

    private static final int SCHEMA_VERSION = 3;
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
    private static final String FONT_SIZE = "font_size";
    private static final String SWIPE_TO_REFRESH = "swipe_to_refresh";
    private static final String DOWNLOAD_WITH_EXTERNAL_APP = "download_with_external_app";
    private static final String DISPLAY_ADDITIONAL_APP_BAR_ICONS = "display_additional_app_bar_icons";
    private static final String DARK_THEME = "dark_theme";
    private static final String NIGHT_MODE = "night_mode";
    private static final String DISPLAY_WEBPAGE_IMAGES = "display_webpage_images";

    public String exportUnencrypted(File exportFile, Context context) {
        try {
            // Delete the current file if it exists.
            if (exportFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                exportFile.delete();
            }

            // Create the export database.
            SQLiteDatabase exportDatabase = SQLiteDatabase.openOrCreateDatabase(exportFile, null);

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
                    FONT_SIZE + " TEXT, " +
                    SWIPE_TO_REFRESH + " BOOLEAN, " +
                    DOWNLOAD_WITH_EXTERNAL_APP + " BOOLEAN, " +
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
            preferencesContentValues.put(USER_AGENT, sharedPreferences.getString(USER_AGENT, context.getString(R.string.user_agent_default_value)));
            preferencesContentValues.put(CUSTOM_USER_AGENT, sharedPreferences.getString(CUSTOM_USER_AGENT, context.getString(R.string.custom_user_agent_default_value)));
            preferencesContentValues.put(INCOGNITO_MODE, sharedPreferences.getBoolean(INCOGNITO_MODE, false));
            preferencesContentValues.put(DO_NOT_TRACK, sharedPreferences.getBoolean(DO_NOT_TRACK, false));
            preferencesContentValues.put(ALLOW_SCREENSHOTS, sharedPreferences.getBoolean(ALLOW_SCREENSHOTS, false));
            preferencesContentValues.put(EASYLIST, sharedPreferences.getBoolean(EASYLIST, true));
            preferencesContentValues.put(EASYPRIVACY, sharedPreferences.getBoolean(EASYPRIVACY, true));
            preferencesContentValues.put(FANBOYS_ANNOYANCE_LIST, sharedPreferences.getBoolean("fanboy_annoyance_list", true));
            preferencesContentValues.put(FANBOYS_SOCIAL_BLOCKING_LIST, sharedPreferences.getBoolean("fanboy_social_blocking_list", true));
            preferencesContentValues.put(ULTRAPRIVACY, sharedPreferences.getBoolean(ULTRAPRIVACY, true));
            preferencesContentValues.put(BLOCK_ALL_THIRD_PARTY_REQUESTS, sharedPreferences.getBoolean(BLOCK_ALL_THIRD_PARTY_REQUESTS, false));
            preferencesContentValues.put(PROXY_THROUGH_ORBOT, sharedPreferences.getBoolean(PROXY_THROUGH_ORBOT, false));
            preferencesContentValues.put(TOR_HOMEPAGE, sharedPreferences.getString(TOR_HOMEPAGE, context.getString(R.string.tor_homepage_default_value)));
            preferencesContentValues.put(TOR_SEARCH, sharedPreferences.getString(TOR_SEARCH, context.getString(R.string.tor_search_default_value)));
            preferencesContentValues.put(TOR_SEARCH_CUSTOM_URL, sharedPreferences.getString(TOR_SEARCH_CUSTOM_URL, context.getString(R.string.tor_search_custom_url_default_value)));
            preferencesContentValues.put(SEARCH, sharedPreferences.getString(SEARCH, context.getString(R.string.search_default_value)));
            preferencesContentValues.put(SEARCH_CUSTOM_URL, sharedPreferences.getString(SEARCH_CUSTOM_URL, context.getString(R.string.search_custom_url_default_value)));
            preferencesContentValues.put(FULL_SCREEN_BROWSING_MODE, sharedPreferences.getBoolean(FULL_SCREEN_BROWSING_MODE, false));
            preferencesContentValues.put(HIDE_SYSTEM_BARS, sharedPreferences.getBoolean(HIDE_SYSTEM_BARS, false));
            preferencesContentValues.put(TRANSLUCENT_NAVIGATION_BAR, sharedPreferences.getBoolean(TRANSLUCENT_NAVIGATION_BAR, true));
            preferencesContentValues.put(CLEAR_EVERYTHING, sharedPreferences.getBoolean(CLEAR_EVERYTHING, true));
            preferencesContentValues.put(CLEAR_COOKIES, sharedPreferences.getBoolean(CLEAR_COOKIES, true));
            preferencesContentValues.put(CLEAR_DOM_STORAGE, sharedPreferences.getBoolean(CLEAR_DOM_STORAGE, true));
            preferencesContentValues.put(CLEAR_FORM_DATA, sharedPreferences.getBoolean(CLEAR_FORM_DATA, true));  // Clear form data can be removed once the minimum API >= 26.
            preferencesContentValues.put(CLEAR_CACHE, sharedPreferences.getBoolean(CLEAR_CACHE, true));
            preferencesContentValues.put(HOMEPAGE, sharedPreferences.getString(HOMEPAGE, context.getString(R.string.homepage_default_value)));
            preferencesContentValues.put(FONT_SIZE, sharedPreferences.getString(FONT_SIZE, context.getString(R.string.font_size_default_value)));
            preferencesContentValues.put(SWIPE_TO_REFRESH, sharedPreferences.getBoolean(SWIPE_TO_REFRESH, true));
            preferencesContentValues.put(DOWNLOAD_WITH_EXTERNAL_APP, sharedPreferences.getBoolean(DOWNLOAD_WITH_EXTERNAL_APP, false));
            preferencesContentValues.put(DISPLAY_ADDITIONAL_APP_BAR_ICONS, sharedPreferences.getBoolean(DISPLAY_ADDITIONAL_APP_BAR_ICONS, false));
            preferencesContentValues.put(DARK_THEME, sharedPreferences.getBoolean(DARK_THEME, false));
            preferencesContentValues.put(NIGHT_MODE, sharedPreferences.getBoolean(NIGHT_MODE, false));
            preferencesContentValues.put(DISPLAY_WEBPAGE_IMAGES, sharedPreferences.getBoolean(DISPLAY_WEBPAGE_IMAGES, true));

            // Insert the preferences into the export database.
            exportDatabase.insert(PREFERENCES_TABLE, null, preferencesContentValues);

            // Close the export database.
            exportDatabase.close();

            // Convert the database file to a string.
            String exportFileString = exportFile.toString();

            // Create strings for the temporary database files.
            String journalFileString = exportFileString + "-journal";

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

    public String importUnencrypted(File importFile, Context context){
        try {
            // Create a temporary import file string.
            String temporaryImportFileString = context.getCacheDir() + "/" + "temporary_import_file";

            // Get a handle for a temporary import file.
            File temporaryImportFile = new File(temporaryImportFileString);

            // Delete the temporary import file if it already exists.
            if (temporaryImportFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                temporaryImportFile.delete();
            }

            // Create input and output streams.
            InputStream importFileInputStream = new FileInputStream(importFile);
            OutputStream temporaryImportFileOutputStream = new FileOutputStream(temporaryImportFile);

            // Create a byte array.
            byte[] transferByteArray = new byte[1024];

            // Create an integer to track the number of bytes read.
            int bytesRead;

            // Copy the import file to the temporary import file.  Once API >= 26 `Files.copy` can be used instead.
            while ((bytesRead = importFileInputStream.read(transferByteArray)) > 0) {
                temporaryImportFileOutputStream.write(transferByteArray, 0, bytesRead);
            }

            // Close the file streams.
            importFileInputStream.close();
            temporaryImportFileOutputStream.close();


            // Get a handle for the shared preference.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            // Open the import database.  Once API >= 27 the file can be opened directly without using the string.
            SQLiteDatabase importDatabase = SQLiteDatabase.openDatabase(temporaryImportFileString, null, SQLiteDatabase.OPEN_READWRITE);

            // Get the database version.
            int importDatabaseVersion = importDatabase.getVersion();

            // Upgrade the database if needed.
            if (importDatabaseVersion < SCHEMA_VERSION) {
                switch (importDatabaseVersion){
                    // Upgrade from schema version 1.
                    case 1:
                        // Add the download with external app preference.
                        importDatabase.execSQL("ALTER TABLE " + PREFERENCES_TABLE + " ADD COLUMN " + DOWNLOAD_WITH_EXTERNAL_APP + " BOOLEAN");

                        // Get the current setting for downloading with an external app.
                        boolean downloadWithExternalApp = sharedPreferences.getBoolean("download_with_external_app", false);

                        // Set the download with external app preference to the current default.
                        if (downloadWithExternalApp) {
                            importDatabase.execSQL("UPDATE " + PREFERENCES_TABLE + " SET " + DOWNLOAD_WITH_EXTERNAL_APP + " = " + 1);
                        } else {
                            importDatabase.execSQL("UPDATE " + PREFERENCES_TABLE + " SET " + DOWNLOAD_WITH_EXTERNAL_APP + " = " + 0);
                        }

                    // Upgrade from schema version 2.
                    case 2:
                        // Once the SQLite version is >= 3.25.0 `ALTER TABLE RENAME` can be used.  https://www.sqlite.org/lang_altertable.html  https://www.sqlite.org/changes.html
                        // https://developer.android.com/reference/android/database/sqlite/package-summary
                        // In the meantime, we can create a new column with the new name.  There is no need to delete the old column on the temporary import database.

                        // Get a cursor with the current `default_font_size` value.
                        Cursor importDatabasePreferenceCursor = importDatabase.rawQuery("SELECT default_font_size FROM " + PREFERENCES_TABLE, null);

                        // Move to the beginning fo the cursor.
                        importDatabasePreferenceCursor.moveToFirst();

                        // Get the current value in `default_font_size`.
                        String fontSize = importDatabasePreferenceCursor.getString(importDatabasePreferenceCursor.getColumnIndex("default_font_size"));

                        // Close the cursor.
                        importDatabasePreferenceCursor.close();

                        // Create a new column named `font_size`.
                        importDatabase.execSQL("ALTER TABLE " + PREFERENCES_TABLE + " ADD COLUMN " + FONT_SIZE + " TEXT");

                        // Place the font size string in the new column.
                        importDatabase.execSQL("UPDATE " + PREFERENCES_TABLE + " SET " + FONT_SIZE + " = " + fontSize);
                }
            }

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
                    .putString(USER_AGENT, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(USER_AGENT)))
                    .putString(CUSTOM_USER_AGENT, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(CUSTOM_USER_AGENT)))
                    .putBoolean(INCOGNITO_MODE, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(INCOGNITO_MODE)) == 1)
                    .putBoolean(DO_NOT_TRACK, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DO_NOT_TRACK)) == 1)
                    .putBoolean(ALLOW_SCREENSHOTS, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(ALLOW_SCREENSHOTS)) == 1)
                    .putBoolean(EASYLIST, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(EASYLIST)) == 1)
                    .putBoolean(EASYPRIVACY, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(EASYPRIVACY)) == 1)
                    .putBoolean("fanboy_annoyance_list", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(FANBOYS_ANNOYANCE_LIST)) == 1)
                    .putBoolean("fanboy_social_blocking_list", importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(FANBOYS_SOCIAL_BLOCKING_LIST)) == 1)
                    .putBoolean(ULTRAPRIVACY, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(ULTRAPRIVACY)) == 1)
                    .putBoolean(BLOCK_ALL_THIRD_PARTY_REQUESTS, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(BLOCK_ALL_THIRD_PARTY_REQUESTS)) == 1)
                    .putBoolean(PROXY_THROUGH_ORBOT, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(PROXY_THROUGH_ORBOT)) == 1)
                    .putString(TOR_HOMEPAGE, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(TOR_HOMEPAGE)))
                    .putString(TOR_SEARCH, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(TOR_SEARCH)))
                    .putString(TOR_SEARCH_CUSTOM_URL, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(TOR_SEARCH_CUSTOM_URL)))
                    .putString(SEARCH, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(SEARCH)))
                    .putString(SEARCH_CUSTOM_URL, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(SEARCH_CUSTOM_URL)))
                    .putBoolean(FULL_SCREEN_BROWSING_MODE, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(FULL_SCREEN_BROWSING_MODE)) == 1)
                    .putBoolean(HIDE_SYSTEM_BARS, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(HIDE_SYSTEM_BARS)) == 1)
                    .putBoolean(TRANSLUCENT_NAVIGATION_BAR, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(TRANSLUCENT_NAVIGATION_BAR)) == 1)
                    .putBoolean(CLEAR_EVERYTHING, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_EVERYTHING)) == 1)
                    .putBoolean(CLEAR_COOKIES, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_COOKIES)) == 1)
                    .putBoolean(CLEAR_DOM_STORAGE, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_DOM_STORAGE)) == 1)
                    // Clear form data can be removed once the minimum API >= 26.
                    .putBoolean(CLEAR_FORM_DATA, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_FORM_DATA)) == 1)
                    .putBoolean(CLEAR_CACHE, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(CLEAR_CACHE)) == 1)
                    .putString(HOMEPAGE, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(HOMEPAGE)))
                    .putString(FONT_SIZE, importPreferencesCursor.getString(importPreferencesCursor.getColumnIndex(FONT_SIZE)))
                    .putBoolean(SWIPE_TO_REFRESH, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(SWIPE_TO_REFRESH)) == 1)
                    .putBoolean(DOWNLOAD_WITH_EXTERNAL_APP, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DOWNLOAD_WITH_EXTERNAL_APP)) == 1)
                    .putBoolean(DISPLAY_ADDITIONAL_APP_BAR_ICONS, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DISPLAY_ADDITIONAL_APP_BAR_ICONS)) == 1)
                    .putBoolean(DARK_THEME, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DARK_THEME)) == 1)
                    .putBoolean(NIGHT_MODE, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(NIGHT_MODE)) == 1)
                    .putBoolean(DISPLAY_WEBPAGE_IMAGES, importPreferencesCursor.getInt(importPreferencesCursor.getColumnIndex(DISPLAY_WEBPAGE_IMAGES)) == 1)
                    .apply();

            // Close the preferences cursor.
            importPreferencesCursor.close();


            // Close the import database.
            importDatabase.close();

            // Create strings for the temporary database files.
            String shmFileString = temporaryImportFileString + "-shm";
            String walFileString = temporaryImportFileString + "-wal";
            String journalFileString = temporaryImportFileString + "-journal";

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

            // Delete the temporary import file.
            //noinspection ResultOfMethodCallIgnored
            temporaryImportFile.delete();

            // Import successful.
            return IMPORT_SUCCESSFUL;
        } catch (Exception exception) {
            // Return the import error.
            return exception.toString();
        }
    }
}
