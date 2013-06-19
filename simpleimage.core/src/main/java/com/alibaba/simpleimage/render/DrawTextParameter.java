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
package com.alibaba.simpleimage.render;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.simpleimage.font.FontManager;

/**
 * @author leon
 */
public class DrawTextParameter {
    public static final Color  DEFAULT_COLOR         = new Color(170, 170, 170, 100);
    public static final Color  DEFAULT_SHADOW_COLOR  = new Color(255, 255, 255, 100);
    public static final float  DEFAULT_WIDTH_PERCENT = 0.85f;
    public static final Font   DEFAULT_FONT          = FontManager.getFont("·½ÕýºÚÌå");
    public static final int    DEFAULT_FONT_SIZE_MIN = 10;
    List<DrawTextItem>          textInfo              = null;

    public DrawTextParameter(){

    }

    public DrawTextParameter(List<DrawTextItem> info){
        this.textInfo = info;

    }

    public void addTextInfo(DrawTextItem info) {
        if (textInfo == null) {
            textInfo = new ArrayList<DrawTextItem>();
        }
        textInfo.add(info);
    }

    public List<DrawTextItem> getTextInfo() {
        return textInfo;
    }
}
