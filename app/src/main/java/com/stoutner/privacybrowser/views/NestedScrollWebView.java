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

// NestedScrollWebView extends WebView to handle nested scrolls (scrolling the app bar off the screen).
public class NestedScrollWebView extends WebView implements NestedScrollingChild2 {
    // The nested scrolling child helper is used throughout the class.
    private NestedScrollingChildHelper nestedScrollingChildHelper;

    // The previous Y position needs to be tracked between motion events.
    private int previousYPosition;


    // Basic constructor.
    public NestedScrollWebView(Context context) {
        // Roll up to the next constructor.
        this(context, null);
    }

    // Intermediate constructor.
    public NestedScrollWebView(Context context, AttributeSet attributeSet) {
        // Roll up to the next constructor.
        this(context, attributeSet, android.R.attr.webViewStyle);
    }

    // Full constructor.
    public NestedScrollWebView(Context context, AttributeSet attributeSet, int defaultStyle) {
        // Run the default commands.
        super(context, attributeSet, defaultStyle);

        // Initialize the nested scrolling child helper.
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);

        // Enable nested scrolling by default.
        nestedScrollingChildHelper.setNestedScrollingEnabled(true);
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
                int currentYPosition = (int) motionEvent.getY();

                // Calculate the delta Y.
                int deltaY = previousYPosition - currentYPosition;

                // Store the current Y position for use in the next action move.
                previousYPosition = currentYPosition;

                // Dispatch the nested pre-school.
                dispatchNestedPreScroll(0, deltaY, null, null);

                // Dispatch the nested scroll.
                dispatchNestedScroll(0, deltaY, 0, 0, null);

                // Run the default commands.
                motionEventHandled = super.onTouchEvent(motionEvent);
                break;


            default:
                // Stop nested scrolling.
                stopNestedScroll();

                // Run the default commands.
                motionEventHandled = super.onTouchEvent(motionEvent);
        }

        // Return the status of the motion event.
        return motionEventHandled;
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