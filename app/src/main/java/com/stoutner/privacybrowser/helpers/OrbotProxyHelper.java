/*
 * Copyright © 2016-2019 Soren Stoutner <soren@stoutner.com>.
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Proxy;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OrbotProxyHelper {
    public static void setProxy(Context privacyBrowserContext, Activity parentActivity, String proxyHost, String proxyPort) {
        if (proxyPort.equals("0")) {
            // Clear the proxy values.
            System.clearProperty("proxyHost");
            System.clearProperty("proxyPort");
        } else {
            // Set the proxy values
            System.setProperty("proxyHost", proxyHost);
            System.setProperty("proxyPort", proxyPort);
        }

        // These entries shouldn't be needed if the above general settings are applied.  They are here for troubleshooting just in case.
        // System.setProperty("http.proxyHost", proxyHost);
        // System.setProperty("http.proxyPort", proxyPort);
        // System.setProperty("https.proxyHost", proxyHost);
        // System.setProperty("https.proxyPort", proxyPort);

        // The SOCKS entries do not appear to do anything.  But maybe someday they will.
        // System.setProperty("socksProxyHost", proxyHost);
        // System.setProperty("socksProxyPort", "9050");
        // System.setProperty("socks.ProxyHost", proxyHost);
        // System.setProperty("socks.ProxyPort", "9050");

        // Use reflection to apply the new proxy values.
        try {
            // Get the application and APK classes.  Suppress the lint warning that reflection may not always work in the future and on all devices.
            Class applicationClass = Class.forName("android.app.Application");
            @SuppressLint("PrivateApi") Class loadedApkClass = Class.forName("android.app.LoadedApk");

            // Get the declared fields.  Suppress the lint warning that `mLoadedApk` cannot be resolved.
            @SuppressWarnings("JavaReflectionMemberAccess") Field methodLoadedApkField = applicationClass.getDeclaredField("mLoadedApk");
            Field methodReceiversField = loadedApkClass.getDeclaredField("mReceivers");

            // Allow the values to be changed.
            methodLoadedApkField.setAccessible(true);
            methodReceiversField.setAccessible(true);

            // Get the APK object.
            Object methodLoadedApkObject = methodLoadedApkField.get(privacyBrowserContext);

            // Get an array map of the receivers.
            ArrayMap receivers = (ArrayMap) methodReceiversField.get(methodLoadedApkObject);

            // Set the proxy.
            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    // Get the receiver class.
                    // `Class<?>`, which is an `unbounded wildcard parameterized type`, must be used instead of `Class`, which is a `raw type`.  Otherwise, `receiveClass.getDeclaredMethod()` is unhappy.
                    Class<?> receiverClass = receiver.getClass();

                    // Apply the new proxy settings to any classes whose names contain `ProxyChangeListener`.
                    if (receiverClass.getName().contains("ProxyChangeListener")) {
                        // Get the `onReceive` method from the class.
                        Method onReceiveMethod = receiverClass.getDeclaredMethod("onReceive", Context.class, Intent.class);

                        // Create a proxy change intent.
                        Intent proxyChangeIntent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        if (Build.VERSION.SDK_INT >= 21) {
                            // Get a proxy info class.
                            // `Class<?>`, which is an `unbounded wildcard parameterized type`, must be used instead of `Class`, which is a `raw type`.  Otherwise, `proxyInfoClass.getMethod()` is unhappy.
                            Class<?> proxyInfoClass = Class.forName("android.net.ProxyInfo");

                            // Get the build direct proxy method from the proxy info class.
                            Method buildDirectProxyMethod = proxyInfoClass.getMethod("buildDirectProxy", String.class, Integer.TYPE);

                            // Populate a proxy info object with the new proxy information.
                            Object proxyInfoObject = buildDirectProxyMethod.invoke(proxyInfoClass, proxyHost, Integer.valueOf(proxyPort));

                            // Add the proxy info object into the proxy change intent.
                            proxyChangeIntent.putExtra("proxy", (Parcelable) proxyInfoObject);
                        }

                        // Pass the proxy change intent to the `onReceive` method of the receiver class.
                        onReceiveMethod.invoke(receiver, privacyBrowserContext, proxyChangeIntent);
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
            Log.d("enableProxyThroughOrbot", "Exception: " + exception);
        }

        if (proxyPort.equals("8118")) {  // Orbot proxy was turned on.
            try {  // Check to see if Orbot is installed.
                PackageManager packageManager = privacyBrowserContext.getPackageManager();
                packageManager.getPackageInfo("org.torproject.android", 0);

                // Ask Orbot to connect if its current status is not "ON".
                if (!MainWebViewActivity.orbotStatus.equals("ON")) {
                    // Request Orbot to start.
                    Intent orbotIntent = new Intent("org.torproject.android.intent.action.START");

                    // Send the intent to the Orbot package.
                    orbotIntent.setPackage("org.torproject.android");

                    // Request a status response be sent back to this package.
                    orbotIntent.putExtra("org.torproject.android.intent.extra.PACKAGE_NAME", privacyBrowserContext.getPackageName());

                    // Make it so.
                    privacyBrowserContext.sendBroadcast(orbotIntent);
                }
            } catch (PackageManager.NameNotFoundException exception){  // If an exception is thrown, Orbot is not installed.
                // Use `AlertDialog.Builder` to create the `AlertDialog`.
                AlertDialog.Builder dialogBuilder;

                // Get a handle for the shared preferences.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(privacyBrowserContext);

                // Get the theme preference.
                boolean darkTheme = sharedPreferences.getBoolean("dark_theme", false);

                // Set the style according to the theme.
                if (darkTheme) {
                    dialogBuilder = new AlertDialog.Builder(parentActivity, R.style.PrivacyBrowserAlertDialogDark);
                } else {
                    dialogBuilder = new AlertDialog.Builder(parentActivity, R.style.PrivacyBrowserAlertDialogLight);
                }

                // Set the message.
                dialogBuilder.setMessage(R.string.orbot_proxy_not_installed);

                // Set the positive button.
                dialogBuilder.setPositiveButton(R.string.close, (DialogInterface dialog, int which) -> {
                    // Do nothing.  The `AlertDialog` will close automatically.
                });

                // Convert `dialogBuilder` to `alertDialog`.
                AlertDialog alertDialog = dialogBuilder.create();

                // Make it so.
                alertDialog.show();
            }
        }
    }
}