/*
 * Copyright © 2019 Soren Stoutner <soren@stoutner.com>.
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

package com.stoutner.privacybrowser.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import com.stoutner.privacybrowser.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// NestedScrollWebView extends WebView to handle nested scrolls (scrolling the app bar off the screen).
public class NestedScrollWebView extends WebView implements NestedScrollingChild2 {
    // These constants identify the blocklists.
    public final static int BLOCKED_REQUESTS = 0;
    public final static int EASYLIST = 1;
    public final static int EASYPRIVACY = 2;
    public final static int FANBOYS_ANNOYANCE_LIST = 3;
    public final static int FANBOYS_SOCIAL_BLOCKING_LIST = 4;
    public final static int ULTRALIST = 5;
    public final static int ULTRAPRIVACY = 6;
    public final static int THIRD_PARTY_REQUESTS = 7;

    // Keep a copy of the WebView fragment ID.
    private long webViewFragmentId;

    // Store the handlers.
    private SslErrorHandler sslErrorHandler;
    private HttpAuthHandler httpAuthHandler;

    // Track if domain settings are applied to this nested scroll WebView and, if so, the database ID.
    private boolean domainSettingsApplied;
    private int domainSettingsDatabaseId;

    // Keep track of when the domain name changes so that domain settings can be reapplied.  This should never be null.
    private String currentDomainName = "";

    // Track the status of first-party cookies.
    private boolean acceptFirstPartyCookies;

    // Track the domain settings JavaScript status.  This can be removed once night mode does not require JavaScript.
    private boolean domainSettingsJavaScriptEnabled;

    // Track the resource requests.
    private List<String[]> resourceRequests = Collections.synchronizedList(new ArrayList<>());  // Using a synchronized list makes adding resource requests thread safe.
    private boolean easyListEnabled;
    private boolean easyPrivacyEnabled;
    private boolean fanboysAnnoyanceListEnabled;
    private boolean fanboysSocialBlockingListEnabled;
    private boolean ultraListEnabled;
    private boolean ultraPrivacyEnabled;
    private boolean blockAllThirdPartyRequests;
    private int blockedRequests;
    private int easyListBlockedRequests;
    private int easyPrivacyBlockedRequests;
    private int fanboysAnnoyanceListBlockedRequests;
    private int fanboysSocialBlockingListBlockedRequests;
    private int ultraListBlockedRequests;
    private int ultraPrivacyBlockedRequests;
    private int thirdPartyBlockedRequests;

    // The pinned SSL certificate variables.
    private boolean hasPinnedSslCertificate;
    private String pinnedSslIssuedToCName;
    private String pinnedSslIssuedToOName;
    private String pinnedSslIssuedToUName;
    private String pinnedSslIssuedByCName;
    private String pinnedSslIssuedByOName;
    private String pinnedSslIssuedByUName;
    private Date pinnedSslStartDate;
    private Date pinnedSslEndDate;

    // The current IP addresses variables.
    private boolean hasCurrentIpAddresses;
    private String currentIpAddresses;

    // The pinned IP addresses variables.
    private boolean hasPinnedIpAddresses;
    private String pinnedIpAddresses;

    // The ignore pinned domain information tracker.  This is set when a user proceeds past a pinned mismatch dialog to prevent the dialog from showing again until after the domain changes.
    private boolean ignorePinnedDomainInformation;

    // Track navigation of history.
    private boolean navigatingHistory;

    // The default or favorite icon.
    private Bitmap favoriteOrDefaultIcon;

    // Track night mode.
    private boolean nightMode;

    // Track swipe to refresh.
    private boolean swipeToRefresh;

    // The nested scrolling child helper is used throughout the class.
    private NestedScrollingChildHelper nestedScrollingChildHelper;

    // The previous Y position needs to be tracked between motion events.
    private int previousYPosition;



    // The basic constructor.
    public NestedScrollWebView(Context context) {
        // Roll up to the next constructor.
        this(context, null);
    }

    // The intermediate constructor.
    public NestedScrollWebView(Context context, AttributeSet attributeSet) {
        // Roll up to the next constructor.
        this(context, attributeSet, android.R.attr.webViewStyle);
    }

    // The full constructor.
    public NestedScrollWebView(Context context, AttributeSet attributeSet, int defaultStyle) {
        // Run the default commands.
        super(context, attributeSet, defaultStyle);

        // Initialize the nested scrolling child helper.
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);

        // Enable nested scrolling by default.
        nestedScrollingChildHelper.setNestedScrollingEnabled(true);
    }



    // WebView Fragment ID.
    public void setWebViewFragmentId(long webViewFragmentId) {
        // Store the WebView fragment ID.
        this.webViewFragmentId = webViewFragmentId;
    }

    public long getWebViewFragmentId() {
        // Return the WebView fragment ID.
        return webViewFragmentId;
    }


    // SSL error handler.
    public void setSslErrorHandler(SslErrorHandler sslErrorHandler) {
        // Store the current SSL error handler.
        this.sslErrorHandler = sslErrorHandler;
    }

    public SslErrorHandler getSslErrorHandler() {
        // Return the current SSL error handler.
        return sslErrorHandler;
    }

    public void resetSslErrorHandler() {
        // Reset the current SSL error handler.
        sslErrorHandler = null;
    }


    // HTTP authentication handler.
    public void setHttpAuthHandler(HttpAuthHandler httpAuthHandler) {
        // Store the current HTTP authentication handler.
        this.httpAuthHandler = httpAuthHandler;
    }

    public HttpAuthHandler getHttpAuthHandler() {
        // Return the current HTTP authentication handler.
        return httpAuthHandler;
    }

    public void resetHttpAuthHandler() {
        // Reset the current HTTP authentication handler.
        httpAuthHandler = null;
    }


    // Domain settings.
    public void setDomainSettingsApplied(boolean applied) {
        // Store the domain settings applied status.
        domainSettingsApplied = applied;
    }

    public boolean getDomainSettingsApplied() {
        // Return the domain settings applied status.
        return domainSettingsApplied;
    }


    // Domain settings database ID.
    public void setDomainSettingsDatabaseId(int databaseId) {
        // Store the domain settings database ID.
        domainSettingsDatabaseId = databaseId;
    }

    public int getDomainSettingsDatabaseId() {
        // Return the domain settings database ID.
        return domainSettingsDatabaseId;
    }


    // Current domain name.  To function well when called, the domain name should never be allowed to be null.
    public void setCurrentDomainName(@NonNull String domainName) {
        // Store the current domain name.
        currentDomainName = domainName;
    }

    public void resetCurrentDomainName() {
        // Reset the current domain name.
        currentDomainName = "";
    }

    public String getCurrentDomainName() {
        // Return the current domain name.
        return currentDomainName;
    }


    // First-party cookies.
    public void setAcceptFirstPartyCookies(boolean status) {
        // Store the accept first-party cookies status.
        acceptFirstPartyCookies = status;
    }

    public boolean getAcceptFirstPartyCookies() {
        // Return the accept first-party cookies status.
        return acceptFirstPartyCookies;
    }


    // Domain settings JavaScript enabled.  This can be removed once night mode does not require JavaScript.
    public void setDomainSettingsJavaScriptEnabled(boolean status) {
        // Store the domain settings JavaScript status.
        domainSettingsJavaScriptEnabled = status;
    }

    public boolean getDomainSettingsJavaScriptEnabled() {
        // Return the domain settings JavaScript status.
        return domainSettingsJavaScriptEnabled;
    }


    // Resource requests.
    public void addResourceRequest(String[] resourceRequest) {
        // Add the resource request to the list.
        resourceRequests.add(resourceRequest);
    }

    public List<String[]> getResourceRequests() {
        // Return the list of resource requests as an array list.
        return resourceRequests;
    }

    public void clearResourceRequests() {
        // Clear the resource requests.
        resourceRequests.clear();
    }


    // Blocklists.
    public void enableBlocklist(int blocklist, boolean status) {
        // Update the status of the indicated blocklist.
        switch (blocklist) {
            case EASYLIST:
                // Update the status of the blocklist.
                easyListEnabled = status;
                break;

            case EASYPRIVACY:
                // Update the status of the blocklist.
                easyPrivacyEnabled = status;
                break;

            case FANBOYS_ANNOYANCE_LIST:
                // Update the status of the blocklist.
                fanboysAnnoyanceListEnabled = status;
                break;

            case FANBOYS_SOCIAL_BLOCKING_LIST:
                // Update the status of the blocklist.
                fanboysSocialBlockingListEnabled = status;
                break;

            case ULTRALIST:
                // Update the status of the blocklist.
                ultraListEnabled = status;
                break;

            case ULTRAPRIVACY:
                // Update the status of the blocklist.
                ultraPrivacyEnabled = status;
                break;

            case THIRD_PARTY_REQUESTS:
                // Update the status of the blocklist.
                blockAllThirdPartyRequests = status;
                break;
        }
    }

    public boolean isBlocklistEnabled(int blocklist) {
        // Get the status of the indicated blocklist.
        switch (blocklist) {
            case EASYLIST:
                // Return the status of the blocklist.
                return easyListEnabled;

            case EASYPRIVACY:
                // Return the status of the blocklist.
                return easyPrivacyEnabled;

            case FANBOYS_ANNOYANCE_LIST:
                // Return the status of the blocklist.
                return fanboysAnnoyanceListEnabled;

            case FANBOYS_SOCIAL_BLOCKING_LIST:
                // Return the status of the blocklist.
                return fanboysSocialBlockingListEnabled;

            case ULTRALIST:
                // Return the status of the blocklist.
                return ultraListEnabled;

            case ULTRAPRIVACY:
                // Return the status of the blocklist.
                return ultraPrivacyEnabled;

            case THIRD_PARTY_REQUESTS:
                // Return the status of the blocklist.
                return blockAllThirdPartyRequests;

            default:
                // The default value is required but should never be used.
                return false;
        }
    }


    // Resource request counters.
    public void resetRequestsCounters() {
        // Reset all the resource request counters.
        blockedRequests = 0;
        easyListBlockedRequests = 0;
        easyPrivacyBlockedRequests = 0;
        fanboysAnnoyanceListBlockedRequests = 0;
        fanboysSocialBlockingListBlockedRequests = 0;
        ultraListBlockedRequests = 0;
        ultraPrivacyBlockedRequests = 0;
        thirdPartyBlockedRequests = 0;
    }

    public void incrementRequestsCount(int blocklist) {
        // Increment the count of the indicated blocklist.
        switch (blocklist) {
            case BLOCKED_REQUESTS:
                // Increment the blocked requests count.
                blockedRequests++;
                break;

            case EASYLIST:
                // Increment the EasyList blocked requests count.
                easyListBlockedRequests++;
                break;

            case EASYPRIVACY:
                // Increment the EasyPrivacy blocked requests count.
                easyPrivacyBlockedRequests++;
                break;

            case FANBOYS_ANNOYANCE_LIST:
                // Increment the Fanboy's Annoyance List blocked requests count.
                fanboysAnnoyanceListBlockedRequests++;
                break;

            case FANBOYS_SOCIAL_BLOCKING_LIST:
                // Increment the Fanboy's Social Blocking List blocked requests count.
                fanboysSocialBlockingListBlockedRequests++;
                break;

            case ULTRALIST:
                // Increment the UltraList blocked requests count.
                ultraListBlockedRequests++;
                break;

            case ULTRAPRIVACY:
                // Increment the UltraPrivacy blocked requests count.
                ultraPrivacyBlockedRequests++;
                break;

            case THIRD_PARTY_REQUESTS:
                // Increment the Third Party blocked requests count.
                thirdPartyBlockedRequests++;
                break;
        }
    }

    public int getRequestsCount(int blocklist) {
        // Get the count of the indicated blocklist.
        switch (blocklist) {
            case BLOCKED_REQUESTS:
                // Return the blocked requests count.
                return blockedRequests;

            case EASYLIST:
                // Return the EasyList blocked requests count.
                return easyListBlockedRequests;

            case EASYPRIVACY:
                // Return the EasyPrivacy blocked requests count.
                return easyPrivacyBlockedRequests;

            case FANBOYS_ANNOYANCE_LIST:
                // Return the Fanboy's Annoyance List blocked requests count.
                return fanboysAnnoyanceListBlockedRequests;

            case FANBOYS_SOCIAL_BLOCKING_LIST:
                // Return the Fanboy's Social Blocking List blocked requests count.
                return fanboysSocialBlockingListBlockedRequests;

            case ULTRALIST:
                // Return the UltraList blocked requests count.
                return ultraListBlockedRequests;

            case ULTRAPRIVACY:
                // Return the UltraPrivacy blocked requests count.
                return ultraPrivacyBlockedRequests;

            case THIRD_PARTY_REQUESTS:
                // Return the Third Party blocked requests count.
                return thirdPartyBlockedRequests;

            default:
                // Return 0.  This should never end up being called.
                return 0;
        }
    }


    // Pinned SSL certificates.
    public boolean hasPinnedSslCertificate() {
        // Return the status of the pinned SSL certificate.
        return hasPinnedSslCertificate;
    }

    public void setPinnedSslCertificate(String issuedToCName, String issuedToOName, String issuedToUName, String issuedByCName, String issuedByOName, String issuedByUName, Date startDate, Date endDate) {
        // Store the pinned SSL certificate information.
        pinnedSslIssuedToCName = issuedToCName;
        pinnedSslIssuedToOName = issuedToOName;
        pinnedSslIssuedToUName = issuedToUName;
        pinnedSslIssuedByCName = issuedByCName;
        pinnedSslIssuedByOName = issuedByOName;
        pinnedSslIssuedByUName = issuedByUName;
        pinnedSslStartDate = startDate;
        pinnedSslEndDate = endDate;

        // Set the pinned SSL certificate tracker.
        hasPinnedSslCertificate = true;
    }

    public ArrayList<Object> getPinnedSslCertificate() {
        // Initialize an array list.
        ArrayList<Object> arrayList = new ArrayList<>();

        // Create the SSL certificate string array.
        String[] sslCertificateStringArray = new String[] {pinnedSslIssuedToCName, pinnedSslIssuedToOName, pinnedSslIssuedToUName, pinnedSslIssuedByCName, pinnedSslIssuedByOName, pinnedSslIssuedByUName};

        // Create the SSL certificate date array.
        Date[] sslCertificateDateArray = new Date[] {pinnedSslStartDate, pinnedSslEndDate};

        // Add the arrays to the array list.
        arrayList.add(sslCertificateStringArray);
        arrayList.add(sslCertificateDateArray);

        // Return the pinned SSL certificate array list.
        return arrayList;
    }

    public void clearPinnedSslCertificate() {
        // Clear the pinned SSL certificate.
        pinnedSslIssuedToCName = null;
        pinnedSslIssuedToOName = null;
        pinnedSslIssuedToUName = null;
        pinnedSslIssuedByCName = null;
        pinnedSslIssuedByOName = null;
        pinnedSslIssuedByUName = null;
        pinnedSslStartDate = null;
        pinnedSslEndDate = null;

        // Clear the pinned SSL certificate tracker.
        hasPinnedSslCertificate = false;
    }


    // Current IP addresses.
    public boolean hasCurrentIpAddresses() {
        // Return the status of the current IP addresses.
        return hasCurrentIpAddresses;
    }

    public void setCurrentIpAddresses(String ipAddresses) {
        // Store the current IP addresses.
        currentIpAddresses = ipAddresses;

        // Set the current IP addresses tracker.
        hasCurrentIpAddresses = true;
    }

    public String getCurrentIpAddresses() {
        // Return the current IP addresses.
        return currentIpAddresses;
    }

    public void clearCurrentIpAddresses() {
        // Clear the current IP addresses.
        currentIpAddresses = null;

        // Clear the current IP addresses tracker.
        hasCurrentIpAddresses = false;
    }


    // Pinned IP addresses.
    public boolean hasPinnedIpAddresses() {
        // Return the status of the pinned IP addresses.
        return hasPinnedIpAddresses;
    }

    public void setPinnedIpAddresses(String ipAddresses) {
        // Store the pinned IP addresses.
        pinnedIpAddresses = ipAddresses;

        // Set the pinned IP addresses tracker.
        hasPinnedIpAddresses = true;
    }

    public String getPinnedIpAddresses() {
        // Return the pinned IP addresses.
        return pinnedIpAddresses;
    }

    public void clearPinnedIpAddresses() {
        // Clear the pinned IP addresses.
        pinnedIpAddresses = null;

        // Clear the pinned IP addresses tracker.
        hasPinnedIpAddresses = false;
    }


    // Ignore pinned information.
    public void setIgnorePinnedDomainInformation(boolean status) {
        // Set the status of the ignore pinned domain information tracker.
        ignorePinnedDomainInformation = status;
    }

    // The syntax looks better as written, even if it is always inverted.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean ignorePinnedDomainInformation() {
        // Return the status of the ignore pinned domain information tracker.
        return ignorePinnedDomainInformation;
    }


    // Navigating history.
    public void setNavigatingHistory(boolean status) {
        // Set the status of navigating history.
        navigatingHistory = status;
    }

    public boolean getNavigatingHistory() {
        // Return the status of navigating history.
        return navigatingHistory;
    }


    // Favorite or default icon.
    public void initializeFavoriteIcon() {
        // Get the default favorite icon drawable.  `ContextCompat` must be used until API >= 21.
        Drawable favoriteIconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.world);

        // Cast the favorite icon drawable to a bitmap drawable.
        BitmapDrawable favoriteIconBitmapDrawable = (BitmapDrawable) favoriteIconDrawable;

        // Remove the incorrect warning below that the favorite icon bitmap drawable might be null.
        assert favoriteIconBitmapDrawable != null;

        // Store the default icon bitmap.
        favoriteOrDefaultIcon = favoriteIconBitmapDrawable.getBitmap();
    }

    public void setFavoriteOrDefaultIcon(Bitmap icon) {
        // Scale the favorite icon bitmap down if it is larger than 256 x 256.  Filtering uses bilinear interpolation.
        if ((icon.getHeight() > 256) || (icon.getWidth() > 256)) {
            favoriteOrDefaultIcon = Bitmap.createScaledBitmap(icon, 256, 256, true);
        } else {
            // Store the icon as presented.
            favoriteOrDefaultIcon = icon;
        }
    }

    public Bitmap getFavoriteOrDefaultIcon() {
        // Return the favorite or default icon.
        return favoriteOrDefaultIcon;
    }


    // Night mode.
    public void setNightMode(boolean status) {
        // Store the night mode status.
        nightMode = status;
    }

    public boolean getNightMode() {
        // Return the night mode status.
        return nightMode;
    }


    // Swipe to refresh.
    public void setSwipeToRefresh(boolean status) {
        // Store the swipe to refresh status.
        swipeToRefresh = status;
    }

    public boolean getSwipeToRefresh() {
        // Return the swipe to refresh status.
        return swipeToRefresh;
    }


    // Scroll range.
    public int getHorizontalScrollRange() {
        // Return the horizontal scroll range.
        return computeHorizontalScrollRange();
    }

    public int getVerticalScrollRange() {
        // Return the vertical scroll range.
        return computeVerticalScrollRange();
    }



    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // Initialize a tracker to return if this motion event is handled.
        boolean motionEventHandled;

        // Run the commands for the given motion event action.
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Start nested scrolling along the vertical axis.  `ViewCompat` must be used until the minimum API >= 21.
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);

                // Save the current Y position.  Action down will not be called again until a new motion starts.
                previousYPosition = (int) motionEvent.getY();

                // Run the default commands.
                motionEventHandled = super.onTouchEvent(motionEvent);
                break;

            case MotionEvent.ACTION_MOVE:
                // Get the current Y position.
                int currentYMotionPosition = (int) motionEvent.getY();

                // Calculate the pre-scroll delta Y.
                int preScrollDeltaY = previousYPosition - currentYMotionPosition;

                // Initialize a variable to track how much of the scroll is consumed.
                int[] consumedScroll = new int[2];

                // Initialize a variable to track the offset in the window.
                int[] offsetInWindow = new int[2];

                // Get the WebView Y position.
                int webViewYPosition = getScrollY();

                // Set the scroll delta Y to initially be the same as the pre-scroll delta Y.
                int scrollDeltaY = preScrollDeltaY;

                // Dispatch the nested pre-school.  This scrolls the app bar if it needs it.  `offsetInWindow` will be returned with an updated value.
                if (dispatchNestedPreScroll(0, preScrollDeltaY, consumedScroll, offsetInWindow)) {
                    // Update the scroll delta Y if some of it was consumed.
                    // There is currently a bug in Android where if scrolling up at a certain slow speed the input can lock the pre scroll and continue to consume it after the app bar is fully displayed.
                    scrollDeltaY = preScrollDeltaY - consumedScroll[1];
                }

                // Check to see if the WebView is at the top and and the scroll action is downward.
                if ((webViewYPosition == 0) && (scrollDeltaY < 0)) {  // Swipe to refresh is being engaged.
                    // Stop the nested scroll so that swipe to refresh has complete control.  This way releasing the scroll to refresh circle doesn't scroll the WebView at the same time.
                    stopNestedScroll();
                } else {  // Swipe to refresh is not being engaged.
                    // Start the nested scroll so that the app bar can scroll off the screen.
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);

                    // Dispatch the nested scroll.  This scrolls the WebView.  The delta Y unconsumed normally controls the swipe refresh layout, but that is handled with the `if` statement above.
                    dispatchNestedScroll(0, scrollDeltaY, 0, 0, offsetInWindow);

                    // Store the current Y position for use in the next action move.
                    previousYPosition = previousYPosition - scrollDeltaY;
                }

                // Run the default commands.
                motionEventHandled = super.onTouchEvent(motionEvent);
                break;


            default:
                // Stop nested scrolling.
                stopNestedScroll();

                // Run the default commands.
                motionEventHandled = super.onTouchEvent(motionEvent);
        }

        // Perform a click.  This is required by the Android accessibility guidelines.
        performClick();

        // Return the status of the motion event.
        return motionEventHandled;
    }

    // The Android accessibility guidelines require overriding `performClick()` and calling it from `onTouchEvent()`.
    @Override
    public boolean performClick() {
        return super.performClick();
    }


    // Method from NestedScrollingChild.
    @Override
    public void setNestedScrollingEnabled(boolean status) {
        // Set the status of the nested scrolling.
        nestedScrollingChildHelper.setNestedScrollingEnabled(status);
    }

    // Method from NestedScrollingChild.
    @Override
    public boolean isNestedScrollingEnabled() {
        // Return the status of nested scrolling.
        return nestedScrollingChildHelper.isNestedScrollingEnabled();
    }


    // Method from NestedScrollingChild.
    @Override
    public boolean startNestedScroll(int axes) {
        // Start a nested scroll along the indicated axes.
        return nestedScrollingChildHelper.startNestedScroll(axes);
    }

    // Method from NestedScrollingChild2.
    @Override
    public boolean startNestedScroll(int axes, int type) {
        // Start a nested scroll along the indicated axes for the given type of input which caused the scroll event.
        return nestedScrollingChildHelper.startNestedScroll(axes, type);
    }


    // Method from NestedScrollingChild.
    @Override
    public void stopNestedScroll() {
        // Stop the nested scroll.
        nestedScrollingChildHelper.stopNestedScroll();
    }

    // Method from NestedScrollingChild2.
    @Override
    public void stopNestedScroll(int type) {
        // Stop the nested scroll of the given type of input which caused the scroll event.
        nestedScrollingChildHelper.stopNestedScroll(type);
    }


    // Method from NestedScrollingChild.
    @Override
    public boolean hasNestedScrollingParent() {
        // Return the status of the nested scrolling parent.
        return nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    // Method from NestedScrollingChild2.
    @Override
    public boolean hasNestedScrollingParent(int type) {
        // return the status of the nested scrolling parent for the given type of input which caused the scroll event.
        return nestedScrollingChildHelper.hasNestedScrollingParent(type);
    }


    // Method from NestedScrollingChild.
    @Override
    public boolean dispatchNestedPreScroll(int deltaX, int deltaY, int[] consumed, int[] offsetInWindow) {
        // Dispatch a nested pre-scroll with the specified deltas, which lets a parent to consume some of the scroll if desired.
        return nestedScrollingChildHelper.dispatchNestedPreScroll(deltaX, deltaY, consumed, offsetInWindow);
    }

    // Method from NestedScrollingChild2.
    @Override
    public boolean dispatchNestedPreScroll(int deltaX, int deltaY, int[] consumed, int[] offsetInWindow, int type) {
        // Dispatch a nested pre-scroll with the specified deltas for the given type of input which caused the scroll event, which lets a parent to consume some of the scroll if desired.
        return nestedScrollingChildHelper.dispatchNestedPreScroll(deltaX, deltaY, consumed, offsetInWindow, type);
    }


    // Method from NestedScrollingChild.
    @Override
    public boolean dispatchNestedScroll(int deltaXConsumed, int deltaYConsumed, int deltaXUnconsumed, int deltaYUnconsumed, int[] offsetInWindow) {
        // Dispatch a nested scroll with the specified deltas.
        return nestedScrollingChildHelper.dispatchNestedScroll(deltaXConsumed, deltaYConsumed, deltaXUnconsumed, deltaYUnconsumed, offsetInWindow);
    }

    // Method from NestedScrollingChild2.
    @Override
    public boolean dispatchNestedScroll(int deltaXConsumed, int deltaYConsumed, int deltaXUnconsumed, int deltaYUnconsumed, int[] offsetInWindow, int type) {
        // Dispatch a nested scroll with the specified deltas for the given type of input which caused the scroll event.
        return nestedScrollingChildHelper.dispatchNestedScroll(deltaXConsumed, deltaYConsumed, deltaXUnconsumed, deltaYUnconsumed, offsetInWindow, type);
    }


    // Method from NestedScrollingChild.
    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        // Dispatch a nested pre-fling with the specified velocity, which lets a parent consume the fling if desired.
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    // Method from NestedScrollingChild.
    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        // Dispatch a nested fling with the specified velocity.
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }
}