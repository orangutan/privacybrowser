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
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Build;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyHelper {
    public static final String NONE = "None";
    public static final String TOR = "Tor";
    public static final String I2P = "I2P";
    public static final String CUSTOM = "Custom";

    public static void setProxy(Context context, String mode) {
        // Initialize the proxy host and port strings.
        String proxyHost = "0";
        String proxyPort = "0";

        // Run the commands that correlate to the proxy mode.
        switch (mode) {
            case NONE:
                // Clear the proxy values.
                System.clearProperty("proxyHost");
                System.clearProperty("proxyHost");
                break;

            case TOR:
                // Update the proxy host and port strings.
                proxyHost = "localhost";
                proxyPort = "8118";

                // Set the proxy values
                System.setProperty("proxyHost", proxyHost);
                System.setProperty("proxyPort", proxyPort);

                // Ask Orbot to connect if its current status is not `"ON"`.
                if (!MainWebViewActivity.orbotStatus.equals("ON")) {
                    // Request Orbot to start.
                    Intent orbotIntent = new Intent("org.torproject.android.intent.action.START");

                    // Send the intent to the Orbot package.
                    orbotIntent.setPackage("org.torproject.android");

                    // Request a status response be sent back to this package.
                    orbotIntent.putExtra("org.torproject.android.intent.extra.PACKAGE_NAME", context.getPackageName());

                    // Make it so.
                    context.sendBroadcast(orbotIntent);
                }
                break;

            case I2P:
                // Update the proxy host and port strings.
                proxyHost = "localhost";
                proxyPort = "4444";

                // Set the proxy values
                System.setProperty("proxyHost", proxyHost);
                System.setProperty("proxyPort", proxyPort);
                break;

            case CUSTOM:
                // Update the proxy host and port strings.
                proxyHost = "0";
                proxyPort = "0";

                // Set the proxy values
                System.setProperty("proxyHost", proxyHost);
                System.setProperty("proxyPort", proxyPort);
                break;
        }

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
            Object methodLoadedApkObject = methodLoadedApkField.get(context);

            // Get an array map of the receivers.
            ArrayMap receivers = (ArrayMap) methodReceiversField.get(methodLoadedApkObject);

            // Set the proxy if the receivers has at least one entry.
            if (receivers != null) {
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
                            onReceiveMethod.invoke(receiver, context, proxyChangeIntent);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
            Log.d("enableProxyThroughOrbot", "Exception: " + exception);
        }
    }
}