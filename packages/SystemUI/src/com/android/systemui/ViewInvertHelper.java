/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * Helper to invert the colors of views and fade between the states.
 */
public class ViewInvertHelper {

    private final Paint mDarkPaint = new Paint();
    private final Interpolator mLinearOutSlowInInterpolator;
    private final View mTarget;
    private final ColorMatrix mMatrix = new ColorMatrix();
    private final ColorMatrix mGrayscaleMatrix = new ColorMatrix();
    private final long mFadeDuration;

    public ViewInvertHelper(View target, long fadeDuration) {
        mTarget = target;
        mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(mTarget.getContext(),
                android.R.interpolator.linear_out_slow_in);
        mFadeDuration = fadeDuration;
    }

    public void fade(final boolean invert, long delay) {
        float startIntensity = invert ? 0f : 1f;
        float endIntensity = invert ? 1f : 0f;
        ValueAnimator animator = ValueAnimator.ofFloat(startIntensity, endIntensity);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateInvertPaint((Float) animation.getAnimatedValue());
                mTarget.setLayerType(View.LAYER_TYPE_HARDWARE, mDarkPaint);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!invert) {
                    mTarget.setLayerType(View.LAYER_TYPE_NONE, null);
                }
            }
        });
        animator.setDuration(mFadeDuration);
        animator.setInterpolator(mLinearOutSlowInInterpolator);
        animator.setStartDelay(delay);
        animator.start();
    }

    public void update(boolean invert) {
        if (invert) {
            updateInvertPaint(1f);
            mTarget.setLayerType(View.LAYER_TYPE_HARDWARE, mDarkPaint);
        } else {
            mTarget.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    public View getTarget() {
        return mTarget;
    }

    private void updateInvertPaint(float intensity) {
        float components = 1 - 2 * intensity;
        int inversionMultiple = 1;

        // if config boolean to invert is false (do NOT invert),
        // multiply components by -1 (double negative) to undo inversion.
        // ALSO, we want to multiply the E channel by 0 to make it 0
        if (!Resources.getSystem().getBoolean(
                com.android.internal.R.bool.config_invert_colors_on_doze)) {
            components = -1 * components;
            inversionMultiple = 0;
        }

        final float[] invert = {
                components, 0f,         0f,         0f, 255f * intensity * inversionMultiple,
                0f,         components, 0f,         0f, 255f * intensity * inversionMultiple,
                0f,         0f,         components, 0f, 255f * intensity * inversionMultiple,
                0f,         0f,         0f,         1f, 0f
        };
        mMatrix.set(invert);

        // we only apply grayscale if configured to do so
        if (Resources.getSystem().getBoolean(
                com.android.internal.R.bool.config_apply_grayscale_on_doze)) {
            mGrayscaleMatrix.setSaturation(1 - intensity);
            mMatrix.preConcat(mGrayscaleMatrix);
        }

        // custom tint array gets applied here (if no overlay present, default array
        // is equal to identity - it will do nothing)
        final TypedArray rawTint = Resources.getSystem().obtainTypedArray(
                com.android.internal.R.array.doze_tint);
        int len = rawTint.length();
        float[] resolvedTint = new float[len];
        for (int i = 0; i < len; i++)
            resolvedTint[i] = rawTint.getFloat(i, 0);
        rawTint.recycle();
        final ColorMatrix tint = new ColorMatrix(resolvedTint);
        mMatrix.preConcat(tint);

        mDarkPaint.setColorFilter(new ColorMatrixColorFilter(mMatrix));
    }
}
