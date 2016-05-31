/*
 * Copyright (C) 2016 Cyanide Android (rogersb11)
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

package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.util.SparseArray;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.keyguard.R;

import java.io.InputStream;

public class KeyguardRomLogo extends FrameLayout {

    private static Context mContext;
    private static ContentResolver mResolver;
    private static SparseArray<Drawable> mCache = new SparseArray<Drawable>();

    private ImageView mLogo;
    private static final int DEFAULT_IMAGE = R.drawable.keyguard_broken_logo;

    private boolean mShowLogo = false;

    public KeyguardRomLogo(Context context) {
        this(context, null);
    }

    public KeyguardRomLogo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardRomLogo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mResolver = mContext.getContentResolver();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLogo = (ImageView) findViewById(R.id.keyguard_rom_logo);
    }

    public void updateLogoImage() {
        final String customLogo = Settings.System.getString(mResolver,
                Settings.System.KEYGUARD_LOGO_CUSTOM);
        if (customLogo != null && !(new String("").equals(customLogo))) {
            try {
                InputStream input = mResolver.openInputStream(Uri.parse(customLogo));
                mLogo.setImageDrawable(Drawable.createFromStream(input, customLogo));
            } catch (Exception ugh) {
                mLogo.setImageDrawable(getDefaultImage());
            }
        } else {
            mLogo.setImageDrawable(getDefaultImage());
        }
    }

    private Drawable getDefaultImage() {
        return loadOrFetch(DEFAULT_IMAGE);
    }

    private static Drawable loadOrFetch(int resId) {
        Drawable res = mCache.get(resId);

        if (res == null) {
            // We don't have this drawable cached, do it!
            final Resources r = mContext.getResources();
            res = r.getDrawable(resId);
            mCache.put(resId, res);
        }
        return res;
    }

    public void showLogo(boolean showLogo) {
        mShowLogo = showLogo;
        if (mShowLogo) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    public void setIconColor(int color) {
        if (mLogo != null) {
            mLogo.setColorFilter(color, Mode.MULTIPLY);
        }
    }
}
