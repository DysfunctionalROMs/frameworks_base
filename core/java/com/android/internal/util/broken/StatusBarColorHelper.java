/*
* Copyright (C) 2015 DarkKat
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.internal.util.broken;

import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;

public class StatusBarColorHelper {

    private static final int WHITE             = 0xffffffff;
    private static final int TRANSLUCENT_BLACK = 0x7a000000;

    public static int getGreetingColor(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.STATUS_BAR_GREETING_COLOR, WHITE);
    }

    public static int getGreetingColorDark(Context context) {
        final int color = Settings.System.getInt(context.getContentResolver(),
                Settings.System.STATUS_BAR_GREETING_COLOR_DARK_MODE,
                TRANSLUCENT_BLACK);
        return (153 << 24) | (color & 0x00ffffff);
    }
}
