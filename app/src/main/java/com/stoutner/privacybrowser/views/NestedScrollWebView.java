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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import java.util.ArrayList;
import java.util.Date;

// NestedScrollWebView extends WebView to handle nested scrolls (scrolling the app bar off the screen).
public class NestedScrollWebView extends WebView implements NestedScrollingChild2 {
    // These constants identify the blocklists.
    public final static int BLOCKED_REQUESTS = 0;
    public final static int EASY_LIST_BLOCKED_REQUESTS = 1;
    public final static int EASY_PRIVACY_BLOCKED_REQUESTS = 2;
    public final static int FANBOYS_ANNOYANCE_LIST_BLOCKED_REQUESTS = 3;
    public final static int FANBOYS_SOCIAL_BLOCKING_LIST_BLOCKED_REQUESTS = 4;
    public final static int ULTRA_PRIVACY_BLOCKED_REQUESTS = 5;
    public final static int THIRD_PARTY_BLOCKED_REQUESTS = 6;

    // Keep a copy of the WebView fragment ID.
    private long webViewFragmentId;

    // Track if domain settings are applied to this nested scroll WebView and, if so, the database ID.
    private boolean domainSettingsApplied;
    private int domainSettingsDatabaseId;

    // Track the resource requests.
    private ArrayList<String[]> resourceRequests = new ArrayList<>();
    private int blockedRequests;
    private int easyListBlockedRequests;
    private int easyPrivacyBlockedRequests;
    private int fanboysAnnoyanceListBlockedRequests;
    private int fanboysSocialBlockingListBlockedRequests;
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


    // Resource requests.
    public void addResourceRequest(String[] resourceRequest) {
        // Add the resource request to the list.
        resourceRequests.add(resourceRequest);
    }

    public ArrayList<String[]> getResourceRequests() {
        // Return the list of resource requests.
        return resourceRequests;
    }

    public void clearResourceRequests() {
        // Clear the resource requests.
        resourceRequests.clear();
    }


    // Resource request counters.
    public void resetRequestsCount(int list) {
        // Run the command on the indicated list.
        switch (list) {
            case BLOCKED_REQUESTS:
                // Reset the blocked requests count.
                blockedRequests = 0;
                break;

            case EASY_LIST_BLOCKED_REQUESTS:
                // Reset the EasyList blocked requests count.
                easyListBlockedRequests = 0;
                break;

            case EASY_PRIVACY_BLOCKED_REQUESTS:
                // Reset the EasyPrivacy blocked requests count.
                easyPrivacyBlockedRequests = 0;
                break;

            case FANBOYS_ANNOYANCE_LIST_BLOCKED_REQUESTS:
                // Reset the Fanboy's Annoyance List blocked requests count.
                fanboysAnnoyanceListBlockedRequests = 0;
                break;

            case FANBOYS_SOCIAL_BLOCKING_LIST_BLOCKED_REQUESTS:
                // Reset the Fanboy's Social Blocking List blocked requests count.
                fanboysSocialBlockingListBlockedRequests = 0;
                break;

            case ULTRA_PRIVACY_BLOCKED_REQUESTS:
                // Reset the UltraPrivacy blocked requests count.
                ultraPrivacyBlockedRequests = 0;
                break;

            case THIRD_PARTY_BLOCKED_REQUESTS:
                // Reset the Third Party blocked requests count.
                thirdPartyBlockedRequests = 0;
                break;
        }
    }

    public void incrementRequestsCount(int list) {
        // Run the command on the indicated list.
        switch (list) {
            case BLOCKED_REQUESTS:
                // Increment the blocked requests count.
                blockedRequests++;
                break;

            case EASY_LIST_BLOCKED_REQUESTS:
                // Increment the EasyList blocked requests count.
                easyListBlockedRequests++;
                break;

            case EASY_PRIVACY_BLOCKED_REQUESTS:
                // Increment the EasyPrivacy blocked requests count.
                easyPrivacyBlockedRequests++;
                break;

            case FANBOYS_ANNOYANCE_LIST_BLOCKED_REQUESTS:
                // Increment the Fanboy's Annoyance List blocked requests count.
                fanboysAnnoyanceListBlockedRequests++;
                break;

            case FANBOYS_SOCIAL_BLOCKING_LIST_BLOCKED_REQUESTS:
                // Increment the Fanboy's Social Blocking List blocked requests count.
                fanboysSocialBlockingListBlockedRequests++;
                break;

            case ULTRA_PRIVACY_BLOCKED_REQUESTS:
                // Increment the UltraPrivacy blocked requests count.
                ultraPrivacyBlockedRequests++;
                break;

            case THIRD_PARTY_BLOCKED_REQUESTS:
                // Increment the Third Party blocked requests count.
                thirdPartyBlockedRequests++;
                break;
        }
    }

    public int getRequestsCount(int list) {
        // Run the command on the indicated list.
        switch (list) {
            case BLOCKED_REQUESTS:
                // Return the blocked requests count.
                return blockedRequests;

            case EASY_LIST_BLOCKED_REQUESTS:
                // Return the EasyList blocked requests count.
                return easyListBlockedRequests;

            case EASY_PRIVACY_BLOCKED_REQUESTS:
                // Return the EasyPrivacy blocked requests count.
                return easyPrivacyBlockedRequests;

            case FANBOYS_ANNOYANCE_LIST_BLOCKED_REQUESTS:
                // Return the Fanboy's Annoyance List blocked requests count.
                return fanboysAnnoyanceListBlockedRequests;

            case FANBOYS_SOCIAL_BLOCKING_LIST_BLOCKED_REQUESTS:
                // Return the Fanboy's Social Blocking List blocked requests count.
                return fanboysSocialBlockingListBlockedRequests;

            case ULTRA_PRIVACY_BLOCKED_REQUESTS:
                // Return the UltraPrivacy blocked requests count.
                return ultraPrivacyBlockedRequests;

            case THIRD_PARTY_BLOCKED_REQUESTS:
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


    // Ignore pinned information.  The syntax looks better as written, even if it is always inverted.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean ignorePinnedDomainInformation() {
        // Return the status of the ignore pinned domain information tracker.
        return ignorePinnedDomainInformation;
    }

    public void setIgnorePinnedDomainInformation(boolean status) {
        // Set the status of the ignore pinned domain information tracker.
        ignorePinnedDomainInformation = status;
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
                    scrollDeltaY = preScrollDeltaY - consumedScroll[1];
                }

                // Check to see if the WebView is at the top and and the scroll action is downward.
                if ((webViewYPosition == 0) && (scrollDeltaY < 0)) {  // Swipe to refresh is being engaged.
                    // Stop the nested scroll so that swipe to refresh has complete control.
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