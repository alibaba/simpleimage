/*
 * Copyright 1999-2101 Alibaba Group.
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
package com.alibaba.simpleimage.font;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

/**
 * 类FontManager.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2011-4-19 下午01:41:58
 */
public class FontManager {

    private static final Map<String, FontLoader> caches = new HashMap<String, FontLoader>();

    static {
        //TODO 重新实现
    }

    public static Font getFont(String name) {
        FontLoader loader = caches.get(name);
        if (loader == null) {
            return null;
        }

        return loader.getFont();
    }
}
