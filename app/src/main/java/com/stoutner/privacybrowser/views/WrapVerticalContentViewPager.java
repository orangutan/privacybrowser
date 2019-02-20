/*
 * Copyright © 2017,2019 Soren Stoutner <soren@stoutner.com>.
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
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class WrapVerticalContentViewPager extends ViewPager {
    // Setup the default constructors.
    public WrapVerticalContentViewPager(Context context) {
        super(context);
    }

    public WrapVerticalContentViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Perform an initial `super.onMeasure`, which populates `getChildCount`.
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Initialize `maximumHeight`.
        int maximumHeight = 0;

        // Find the maximum height of each of the child views.
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);

            // Measure the child view height with no constraints.
            childView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            // Store the child's height if it is larger than `maximumHeight`.
            if (childView.getMeasuredHeight() > maximumHeight) {
                maximumHeight = childView.getMeasuredHeight();
            }
        }

        // Perform a final `super.onMeasure` to set the `maximumHeight`.
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(maximumHeight, MeasureSpec.EXACTLY));
    }
}