/*
 * Copyright © 2016-2020 Soren Stoutner <soren@stoutner.com>.
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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.view.View;

import androidx.preference.PreferenceManager;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;

import com.google.android.material.snackbar.Snackbar;

import com.stoutner.privacybrowser.R;
import com.stoutner.privacybrowser.activities.MainWebViewActivity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.concurrent.Executor;

public class ProxyHelper {
    public static final String NONE = "None";
    public static final String TOR = "Tor";
    public static final String I2P = "I2P";
    public static final String CUSTOM = "Custom";

    public static void setProxy(Context context, View activityView, String proxyMode) {
        // Initialize the proxy host and port strings.
        String proxyHost = "0";
        String proxyPort = "0";

        // Create a proxy config builder.
        ProxyConfig.Builder proxyConfigBuilder = new ProxyConfig.Builder();

        // Run the commands that correlate to the proxy mode.
        switch (proxyMode) {
            case NONE:
                // Clear the proxy values.
                System.clearProperty("proxyHost");
                System.clearProperty("proxyHost");
                break;

            case TOR:
                // Update the proxy host and port strings.  These can be removed once the minimum API >= 21.
                proxyHost = "localhost";
                proxyPort = "8118";

                // Set the proxy values.  These can be removed once the minimum API >= 21.
                System.setProperty("proxyHost", proxyHost);
                System.setProperty("proxyPort", proxyPort);

                // Add the proxy to the builder.  The proxy config builder can use a SOCKS proxy.
                proxyConfigBuilder.addProxyRule("socks://localhost:9050");

                // Ask Orbot to connect if its current status is not `"ON"`.
                if (!MainWebViewActivity.orbotStatus.equals("ON")) {
                    // Create an intent to request Orbot to start.
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
                // Update the proxy host and port strings.  These can be removed once the minimum API >= 21.
                proxyHost = "localhost";
                proxyPort = "4444";

                // Set the proxy values.  These can be removed once the minimum API >= 21.
                System.setProperty("proxyHost", proxyHost);
                System.setProperty("proxyPort", proxyPort);

                // Add the proxy to the builder.
                proxyConfigBuilder.addProxyRule("http://localhost:4444");
                break;

            case CUSTOM:
                // Get a handle for the shared preferences.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                // Get the custom proxy URL string.
                String customProxyUrlString = sharedPreferences.getString("proxy_custom_url", context.getString(R.string.proxy_custom_url_default_value));

                // Parse the custom proxy URL.
                try {
                    // Convert the custom proxy URL string to a URI.
                    Uri customProxyUri = Uri.parse(customProxyUrlString);

                    // Get the proxy host and port strings from the shared preferences.  These can be removed once the minimum API >= 21.
                    proxyHost = customProxyUri.getHost();
                    proxyPort = String.valueOf(customProxyUri.getPort());

                    // Set the proxy values.  These can be removed once the minimum API >= 21.
                    System.setProperty("proxyHost", proxyHost);
                    System.setProperty("proxyPort", proxyPort);

                    // Add the proxy to the builder.
                    proxyConfigBuilder.addProxyRule(customProxyUrlString);
                } catch (Exception exception){  // The custom proxy URL is invalid.
                    // Display a Snackbar.
                    Snackbar.make(activityView, R.string.custom_proxy_invalid, Snackbar.LENGTH_LONG).show();
                }
                break;
        }

        // Apply the proxy settings
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {  // The fancy new proxy config can be used because the API >= 21.
            // Convert the proxy config builder into a proxy config.
            ProxyConfig proxyConfig = proxyConfigBuilder.build();

            // Get the proxy controller.
            ProxyController proxyController = ProxyController.getInstance();

            // Applying a proxy requires an executor.
            Executor executor = runnable -> {
                // Do nothing.
            };

            // Applying a proxy requires a runnable.
            Runnable runnable = () -> {
                // Do nothing.
            };

            // Apply the proxy settings.
            if (proxyMode.equals(NONE)) {  // Remove the proxy.
                proxyController.clearProxyOverride(executor, runnable);
            } else {  // Apply the proxy.
                try {
                    // Apply the proxy.
                    proxyController.setProxyOverride(proxyConfig, executor, runnable);
                } catch (IllegalArgumentException exception) {  // The proxy config is invalid.
                    // Display a Snackbar.
                    Snackbar.make(activityView, R.string.custom_proxy_invalid, Snackbar.LENGTH_LONG).show();
                }
            }
        } else {  // The old proxy method must be used, either because an old WebView is installed or because the API == 19;
            // Get a handle for the shared preferences.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            // Check to make sure a SOCKS proxy is not selected.
            if (proxyMode.equals(CUSTOM) && sharedPreferences.getString("proxy_custom_url", context.getString(R.string.proxy_custom_url_default_value)).startsWith("socks://")) {
                // Display a Snackbar.
                Snackbar.make(activityView, R.string.socks_proxies_do_not_work_on_kitkat, Snackbar.LENGTH_LONG).show();
            } else {  // Use reflection to apply the new proxy values.
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
                                    Intent proxyChangeIntent = new Intent(android.net.Proxy.PROXY_CHANGE_ACTION);

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
                    // Do nothing.
                }
            }
        }
    }

    public Proxy getCurrentProxy(Context context) {
        // Define a proxy variable.
        Proxy proxy;

        // Get the proxy according to the current proxy mode.
        switch (MainWebViewActivity.proxyMode) {
            case (ProxyHelper.TOR):
                if (Build.VERSION.SDK_INT >= 21) {
                    // Use localhost port 9050 as the socket address.
                    SocketAddress torSocketAddress = new InetSocketAddress("localhost", 9050);

                    // Create a SOCKS proxy.
                    proxy = new Proxy(Proxy.Type.SOCKS, torSocketAddress);
                } else {
                    // Use localhost port 8118 as the socket address.
                    SocketAddress oldTorSocketAddress = new InetSocketAddress("localhost", 8118);

                    // Create an HTTP proxy.
                    proxy = new Proxy(Proxy.Type.HTTP, oldTorSocketAddress);
                }
                break;

            case (ProxyHelper.I2P):
                // Use localhost port 4444 as the socket address.
                SocketAddress i2pSocketAddress = new InetSocketAddress("localhost", 4444);

                // Create an HTTP proxy.
                proxy = new Proxy(Proxy.Type.HTTP, i2pSocketAddress);
                break;

            case (ProxyHelper.CUSTOM):
                // Get the shared preferences.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                // Get the custom proxy URL string.
                String customProxyUrlString = sharedPreferences.getString("proxy_custom_url", context.getString(R.string.proxy_custom_url_default_value));

                // Parse the custom proxy URL.
                try {
                    // Convert the custom proxy URL string to a URI.
                    Uri customProxyUri = Uri.parse(customProxyUrlString);

                    // Get the custom socket address.
                    SocketAddress customSocketAddress = new InetSocketAddress(customProxyUri.getHost(), customProxyUri.getPort());

                    // Get the custom proxy scheme.
                    String customProxyScheme = customProxyUri.getScheme();

                    // Create a proxy according to the scheme.
                    if ((customProxyScheme != null) && customProxyScheme.startsWith("socks")) {  // A SOCKS proxy is specified.
                        // Create a SOCKS proxy.
                        proxy = new Proxy(Proxy.Type.SOCKS, customSocketAddress);
                    } else {  // A SOCKS proxy is not specified.
                        // Create an HTTP proxy.
                        proxy = new Proxy(Proxy.Type.HTTP, customSocketAddress);
                    }
                } catch (Exception exception) {  // The custom proxy cannot be parsed.
                    // Disable the proxy.
                    proxy = Proxy.NO_PROXY;
                }
                break;

            default:  // No proxy is in use.
                // Create a direct proxy.
                proxy = Proxy.NO_PROXY;
                break;
        }

        // Return the proxy.
        return proxy;
    }
}