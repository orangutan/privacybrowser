/*
 * Copyright 2017 Soren Stoutner <soren@stoutner.com>.
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DomainsDatabaseHelper extends SQLiteOpenHelper {
    private static final int SCHEMA_VERSION = 1;
    private static final String DOMAINS_DATABASE = "domains.db";
    private static final String DOMAINS_TABLE = "domains";

    public static final String _ID = "_id";
    public static final String DOMAIN_NAME = "domainname";
    public static final String ENABLE_JAVASCRIPT = "enablejavascript";
    public static final String ENABLE_FIRST_PARTY_COOKIES = "enablefirstpartycookies";
    public static final String ENABLE_THIRD_PARTY_COOKIES = "enablethirdpartycookies";
    public static final String ENABLE_DOM_STORAGE = "enabledomstorage";
    public static final String ENABLE_FORM_DATA = "enableformdata";
    public static final String USER_AGENT = "useragent";
    public static final String FONT_SIZE = "fontsize";

    // Initialize the database.  The lint warnings for the unused parameters are suppressed.
    public DomainsDatabaseHelper(Context context, @SuppressWarnings("UnusedParameters") String name, SQLiteDatabase.CursorFactory cursorFactory, @SuppressWarnings("UnusedParameters") int version) {
        super(context, DOMAINS_DATABASE, cursorFactory, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase domainsDatabase) {
        // Setup the SQL string to create the `domains` table.
        final String CREATE_DOMAINS_TABLE = "CREATE TABLE " + DOMAINS_TABLE + " (" +
                _ID + " integer primary key, " +
                DOMAIN_NAME + " text, " +
                ENABLE_JAVASCRIPT + " boolean, " +
                ENABLE_FIRST_PARTY_COOKIES + " boolean, " +
                ENABLE_THIRD_PARTY_COOKIES + " boolean, " +
                ENABLE_DOM_STORAGE + " boolean, " +
                ENABLE_FORM_DATA + " boolean, " +
                USER_AGENT + " text, " +
                FONT_SIZE + " integer);";

        // Create the `domains` table if it doesn't exist.
        domainsDatabase.execSQL(CREATE_DOMAINS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase domainsDatabase, int oldVersion, int newVersion) {
        // Code for upgrading the database will be added here when the schema version > 1.
    }

    public Cursor getDomainNameCursorOrderedByDomain() {
        // Get a readable database handle.
        SQLiteDatabase domainsDatabase = this.getReadableDatabase();

        // Get everything in `DOMAINS_TABLE` ordered by `DOMAIN_NAME`.
        final String GET_CURSOR_ORDERED_BY_DOMAIN = "SELECT " + _ID + ", " + DOMAIN_NAME +
                " FROM " + DOMAINS_TABLE +
                " ORDER BY " + DOMAIN_NAME + " ASC";

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.  We can't close the `Cursor` because we need to use it in the parent activity.
        return domainsDatabase.rawQuery(GET_CURSOR_ORDERED_BY_DOMAIN, null);
    }

    public Cursor getDomainNameCursorOrderedByDomainExcept(int databaseId) {
        // Get a readable database handle.
        SQLiteDatabase domainsDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to select all rows except that with `databaseId`.
        final String GET_CURSOR_ORDERED_BY_DOMAIN_EXCEPT = "SELECT " + _ID + ", " + DOMAIN_NAME +
                " FROM " + DOMAINS_TABLE +
                " WHERE " + _ID + " IS NOT " + databaseId +
                " ORDER BY " + DOMAIN_NAME + " ASC";

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.  We can't close the `Cursor` because we need to use it in the calling activity.
        return domainsDatabase.rawQuery(GET_CURSOR_ORDERED_BY_DOMAIN_EXCEPT, null);
    }

    public Cursor getCursorForId(int databaseId) {
        // Get a readable database handle.
        SQLiteDatabase domainsDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to get the `Cursor` for `databaseId`.
        final String GET_CURSOR_FOR_ID = "SELECT * FROM " + DOMAINS_TABLE +
                " WHERE " + _ID + " = " + databaseId;

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.  We can't close the `Cursor` because we need to use it in the calling activity.
        return domainsDatabase.rawQuery(GET_CURSOR_FOR_ID, null);
    }

    public Cursor getCursorForDomainName(String domainName) {
        // Get a readable database handle.
        SQLiteDatabase domainsDatabase = this.getReadableDatabase();

        // Prepare the SQL statement to get the `Cursor` for `domainName`.
        final String GET_CURSOR_FOR_DOMAIN_NAME = "SELECT * FROM " + DOMAINS_TABLE +
                " WHERE " + DOMAIN_NAME + " = " + "\"" + domainName + "\"";

        // Return the results as a `Cursor`.  The second argument is `null` because there are no `selectionArgs`.  We can't close the `Cursor` because we need to us it in the calling activity.
        return domainsDatabase.rawQuery(GET_CURSOR_FOR_DOMAIN_NAME, null);
    }

    public void addDomain(String domainName) {
        // Store the domain data in a `ContentValues`.
        ContentValues domainContentValues = new ContentValues();

        // Create entries for each field in the database.  The ID is created automatically.
        domainContentValues.put(DOMAIN_NAME, domainName);
        domainContentValues.put(ENABLE_JAVASCRIPT, false);
        domainContentValues.put(ENABLE_FIRST_PARTY_COOKIES, false);
        domainContentValues.put(ENABLE_THIRD_PARTY_COOKIES, false);
        domainContentValues.put(ENABLE_DOM_STORAGE, false);
        domainContentValues.put(ENABLE_FORM_DATA, false);
        domainContentValues.put(USER_AGENT, "PrivacyBrowser/1.0");
        domainContentValues.put(FONT_SIZE, "100");

        // Get a writable database handle.
        SQLiteDatabase domainsDatabase = this.getWritableDatabase();

        // Insert a new row.  The second argument is `null`, which makes it so that a completely null row cannot be created.
        domainsDatabase.insert(DOMAINS_TABLE, null, domainContentValues);

        // Close the database handle.
        domainsDatabase.close();
    }

    public void saveDomain(int databaseId, String domainName, boolean javaScriptEnabled, boolean firstPartyCookiesEnabled, boolean thirdPartyCookiesEnabled, boolean domStorageEnabled, boolean formDataEnabled, String userAgent, int fontSize) {
        // Store the domain data in a `ContentValues`.
        ContentValues domainContentValues = new ContentValues();

        // Add entries for each field in the database.
        domainContentValues.put(DOMAIN_NAME, domainName);
        domainContentValues.put(ENABLE_JAVASCRIPT, javaScriptEnabled);
        domainContentValues.put(ENABLE_FIRST_PARTY_COOKIES, firstPartyCookiesEnabled);
        domainContentValues.put(ENABLE_THIRD_PARTY_COOKIES, thirdPartyCookiesEnabled);
        domainContentValues.put(ENABLE_DOM_STORAGE, domStorageEnabled);
        domainContentValues.put(ENABLE_FORM_DATA, formDataEnabled);
        domainContentValues.put(USER_AGENT, userAgent);
        domainContentValues.put(FONT_SIZE, fontSize);

        // Get a writable database handle.
        SQLiteDatabase domainsDatabase = this.getWritableDatabase();

        // Update the row for `databaseId`.  The last argument is `null` because there are no `whereArgs`.
        domainsDatabase.update(DOMAINS_TABLE, domainContentValues, _ID + " = " + databaseId, null);

        // Close the database handle.
        domainsDatabase.close();
    }

    public void deleteDomain(int databaseId) {
        // Get a writable database handle.
        SQLiteDatabase domainsDatabase = this.getWritableDatabase();

        // Delete the row for `databaseId`.  The last argument is `null` because we don't need additional parameters.
        domainsDatabase.delete(DOMAINS_TABLE, _ID + " = " + databaseId, null);

        // Close the database handle.
        domainsDatabase.close();
    }
}