/*
 * Copyright © 2016-2018 Soren Stoutner <soren@stoutner.com>.
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
import android.content.pm.PackageManager;
import android.net.Proxy;
import android.support.v7.app.AlertDialog;
import android.util.ArrayMap;
import android.util.Log;

import com.stoutner.privacybrowser.activities.MainWebViewActivity;
import com.stoutner.privacybrowser.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class OrbotProxyHelper {
    public static void setProxy(Context privacyBrowserContext, Activity parentActivity, String proxyHost, String proxyPort) {

        // Set the proxy values
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);

        // Use reflection to apply the new proxy values.
        try {
            Class applicationClass = Class.forName("android.app.Application");

            // Suppress the lint warning that `mLoadedApk` cannot be resolved.
            @SuppressWarnings("JavaReflectionMemberAccess") Field mLoadedApkField = applicationClass.getDeclaredField("mLoadedApk");

            // `setAccessible(true)` allows the value of `mLoadedApkField` to be changed..
            mLoadedApkField.setAccessible(true);
            Object mLoadedApkObject = mLoadedApkField.get(privacyBrowserContext);

            // Suppress the lint warning that reflection may not alwasy work in the future and on all devices.
            @SuppressLint("PrivateApi") Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");

            // `setAccessible(true)` allows the value of `mReceiversField` to be changed.
            mReceiversField.setAccessible(true);

            ArrayMap receivers = (ArrayMap) mReceiversField.get(mLoadedApkObject);

            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    // `Class<?>`, which is an `unbounded wildcard parameterized type`, must be used instead of `Class`, which is a `raw type`.
                    // Otherwise, `receiveClass.getDeclaredMethod` below will produce an error.
                    Class<?> receiverClass = receiver.getClass();
                    if (receiverClass.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = receiverClass.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        onReceiveMethod.invoke(receiver, privacyBrowserContext, intent);
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
            Log.d("enableProxyThroughOrbot", "Exception: " + exception);
        }

        if (proxyPort.equals("8118")) {  // Orbot proxy was turned on.
            try {  // Check to see if Orbot is installed.
                PackageManager packageManager = privacyBrowserContext.getPackageManager();
                packageManager.getPackageInfo("org.torproject.android", PackageManager.GET_ACTIVITIES);

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

                // Set the style according to the theme.
                if (MainWebViewActivity.darkTheme) {
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
