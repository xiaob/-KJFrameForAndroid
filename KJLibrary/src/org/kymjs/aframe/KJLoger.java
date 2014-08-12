/*
 * Copyright (c), KJFrameForAndroid 张涛 (kymjs123@gmail.com).
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
package org.kymjs.aframe;

import android.util.Log;

/**
 * 应用程序的Log管理
 * 
 * @author kymjs(kymjs123@gmail.com)
 * @version 1.1
 * @created 2014-2-28
 */
public class KJLoger {
    public static final boolean IS_DEBUG = true;
    public static final boolean DEBUG_LOG = true;
    public static final boolean SHOW_ACTIVITY_STATE = true;

    public static final void debug(String msg) {
        if (IS_DEBUG) {
            Log.i("debug", msg);
        }
    }

    public static final void debug(String msg, Throwable tr) {
        if (IS_DEBUG) {
            Log.i("debug", msg, tr);
        }
    }

    public static final void state(String packName, String state) {
        if (SHOW_ACTIVITY_STATE) {
            Log.d("activity_state", packName + state);
        }
    }

    public static final void debugLog(String packName, String state) {
        if (DEBUG_LOG) {
            Log.d("debug", packName + state);
        }
    }
}
