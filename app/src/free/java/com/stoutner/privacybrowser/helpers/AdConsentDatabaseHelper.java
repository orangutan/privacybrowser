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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdConsentDatabaseHelper extends SQLiteOpenHelper {
    private static final int SCHEMA_VERSION = 1;
    private static final String AD_CONSENT_DATABASE = "ad_consent.db";
    private static final String AD_CONSENT_TABLE = "ad_consent";

    private static final String _ID = "_id";
    private static final String AD_CONSENT = "ad_consent";

    private static final String CREATE_AD_CONSENT_TABLE = "CREATE TABLE " + AD_CONSENT_TABLE + " (" +
            _ID + " INTEGER PRIMARY KEY, " +
            AD_CONSENT + " BOOLEAN)";

    // Initialize the database.  The lint warnings for the unused parameters are suppressed.
    public AdConsentDatabaseHelper(Context context, @SuppressWarnings("UnusedParameters") String name, SQLiteDatabase.CursorFactory cursorFactory, @SuppressWarnings("UnusedParameters") int version) {
        super(context, AD_CONSENT_DATABASE, cursorFactory, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase adConsentDatabase) {
        // Create the ad consent database.
        adConsentDatabase.execSQL(CREATE_AD_CONSENT_TABLE);

        // Create an ad consent ContentValues.
        ContentValues adConsentContentValues = new ContentValues();

        // Populate the ad consent content values.
        adConsentContentValues.put(AD_CONSENT, false);

        // Insert a new row.  The second argument is `null`, which makes it so that a completely null row cannot be created.
        adConsentDatabase.insert(AD_CONSENT_TABLE, null, adConsentContentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase adConsentDatabase, int oldVersion, int newVersion) {
        // Code for upgrading the database will be added here if the schema version ever increases above 1.
    }

    // Check to see if ad consent has been granted.
    public boolean isGranted() {
        // Get a readable database handle.
        SQLiteDatabase adConsentDatabase = this.getReadableDatabase();

        // Get the ad consent cursor.
        Cursor adConsentCursor = adConsentDatabase.rawQuery("SELECT * FROM " + AD_CONSENT_TABLE, null);

        // Move to the first entry.
        adConsentCursor.moveToFirst();

        // Get the ad consent boolean.
        boolean adConsent = (adConsentCursor.getInt(adConsentCursor.getColumnIndex(AD_CONSENT)) == 1);

        // Close the cursor.
        adConsentCursor.close();

        // Close the database.
        adConsentDatabase.close();

        // Return the ad consent boolean.
        return adConsent;
    }

    // Update the ad consent.
    public void updateAdConsent(boolean adConsent) {
        // Get a writable database handle.
        SQLiteDatabase adConsentDatabase = this.getWritableDatabase();

        // Create an ad consent integer.
        int adConsentInt;

        // Set the ad consent integer according to the boolean.
        if (adConsent) {
            adConsentInt = 1;
        } else {
            adConsentInt = 0;
        }

        // Update the ad consent in the database.
        adConsentDatabase.execSQL("UPDATE " + AD_CONSENT_TABLE + " SET " + AD_CONSENT + " = " + adConsentInt);

        // Close the database.
        adConsentDatabase.close();
    }
}