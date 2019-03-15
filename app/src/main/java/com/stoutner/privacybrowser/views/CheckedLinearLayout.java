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
 *
 *
 *
 * This file is a modified version of <https://android.googlesource.com/platform/packages/apps/Camera/+/master/src/com/android/camera/ui/CheckedLinearLayout.java>.
 *
 * The original licensing information is below.
 *
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stoutner.privacybrowser.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class CheckedLinearLayout extends LinearLayout implements Checkable {
    private boolean isCurrentlyChecked;
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    public CheckedLinearLayout(Context context) {
        // Run the default commands.
        super(context);
    }

    public CheckedLinearLayout(Context context, @Nullable AttributeSet attributeSet) {
        // Run the default commands.
        super(context, attributeSet);
    }

    public CheckedLinearLayout(Context context, @Nullable AttributeSet attributeSet, int defaultStyleAttribute) {
        // Run the default commands.
        super(context, attributeSet, defaultStyleAttribute);
    }

    /*  This constructor can only be added once the minimum API >= 21.
    public CheckedLinearLayout(Context context, @Nullable AttributeSet attributeSet, int defaultStyleAttribute, int defaultStyleResource) {
        // Run the default commands.
        super(context, attributeSet, defaultStyleAttribute, defaultStyleResource);
    } */

    @Override
    public boolean isChecked() {
        // Return the checked status.
        return isCurrentlyChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        // Only process the command if a change is requested.
        if (isCurrentlyChecked != checked) {
            // Update the is currently checked tracker.
            isCurrentlyChecked = checked;

            // Refresh the drawable state.
            refreshDrawableState();

            // Propagate the checked status to the child views.
            for (int i = 0; i < getChildCount(); i++) {
                // Get a handle for the child view.
                View childView = getChildAt(i);

                // Propagate the checked status if the child view is checkable.
                if (childView instanceof Checkable) {
                    // Cast the child view to `Checkable`.
                    Checkable checkableChildView = (Checkable) childView;

                    // Set the checked status.
                    checkableChildView.setChecked(checked);
                }
            }
        }
    }

    @Override
    public void toggle() {
        // Toggle the state.
        setChecked(!isCurrentlyChecked);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);

        if (isCurrentlyChecked) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }

        return drawableState;
    }
}
