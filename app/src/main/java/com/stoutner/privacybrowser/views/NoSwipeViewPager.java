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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NoSwipeViewPager extends ViewPager {
    // The basic constructor
    public NoSwipeViewPager(@NonNull Context context) {
        // Roll up to the full constructor.
        this(context, null);
    }

    // The full constructor.
    public NoSwipeViewPager(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        // Run the default commands.
        super(context, attributeSet);
    }

    // It is necessary to override `performClick()` when overriding `onTouchEvent()`
    @Override
    public boolean performClick() {
        // Run the default commands.
        super.performClick();

        // Do not consume the events.
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // `onTouchEvent()` requires calling `performClick()`.
        performClick();

        // Do not allow swiping.
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Do not allow swiping.
        return false;
    }
}