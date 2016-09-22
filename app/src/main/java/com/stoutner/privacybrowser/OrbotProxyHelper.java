/**
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Proxy;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class OrbotProxyHelper {
    static void setProxy(Context privacyBrowserContext, Activity parentActivity, String proxyHost, String proxyPort) {
        // Set the proxy values
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", proxyPort);

        // Use reflection to apply the new proxy values.
        try {
            Class applicationClass = Class.forName("android.app.Application");
            Field mLoadedApkField = applicationClass.getDeclaredField("mLoadedApk");
            // `setAccessible(true)` allows us to change the value of `mLoadedApkField`.
            mLoadedApkField.setAccessible(true);
            Object mLoadedApkObject = mLoadedApkField.get(privacyBrowserContext);

            Class loadedApkClass = Class.forName("android.app.LoadedApk");
            Field mReceiversField = loadedApkClass.getDeclaredField("mReceivers");
            // `setAccessible(true)` allows us to change the value of `mReceiversField`.
            mReceiversField.setAccessible(true);

            ArrayMap receivers = (ArrayMap) mReceiversField.get(mLoadedApkObject);

            for (Object receiverMap : receivers.values()) {
                for (Object receiver : ((ArrayMap) receiverMap).keySet()) {
                    // We have to use `Class<?>`, which is an `unbounded wildcard parameterized type`, instead of `Class`, which is a `raw type`, or `receiveClass.getDeclaredMethod` below will produce an error.
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
            // Set the `appBar` background to be light blue if Orbot proxy support is enabled.
            MainWebViewActivity.appBar.setBackgroundDrawable(ContextCompat.getDrawable(privacyBrowserContext, R.color.blue_50));

            try {  // Check to see if Orbot is installed.
                PackageManager packageManager = privacyBrowserContext.getPackageManager();
                packageManager.getPackageInfo("org.torproject.android", PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException exception){  // If an exception is thrown, Orbot is not installed.
                // Build an `AlertDialog`.  `R.style.LightAlertDialog` formats the color of the button text.
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(parentActivity, R.style.LightAlertDialog);
                dialogBuilder.setMessage(R.string.orbot_proxy_not_installed);
                dialogBuilder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing.  The `AlertDialog` will close automatically.
                    }
                });

                // Convert `dialogBuilder` to `alertDialog` and display it on the screen.
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        } else {  // Otherwise set the default grey `appBar` background.
            MainWebViewActivity.appBar.setBackgroundDrawable(ContextCompat.getDrawable(privacyBrowserContext, R.color.grey_100));
        }
    }
}
