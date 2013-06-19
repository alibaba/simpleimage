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
import java.awt.Graphics2D;

/**
 * 类DrawTextItem.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2011-3-28 下午01:33:53
 */
public abstract class DrawTextItem {

    static final int MIN_FONT_SIZE = 10;

    // 文本
    protected String text;
    // 字的颜色,主要加的文本
    protected Color  fontColor;
    // 阴影颜色
    protected Color  fontShadowColor;
    // 文本字体
    protected Font   defaultFont;
    // 显示文字的最小大小，低于这个大小的文字不显示
    protected int    minFontSize   = MIN_FONT_SIZE;

    /**
     * @param text
     * @param fontColor
     * @param fontShadowColor
     * @param font
     * @param minFontSize
     * @param textWidthPercent
     */
    public DrawTextItem(String text, Color fontColor, Color fontShadowColor, Font font, int minFontSize){
        super();
        this.text = text;
        this.fontColor = fontColor;
        this.fontShadowColor = fontShadowColor;
        this.defaultFont = font;
        this.minFontSize = minFontSize;
    }

    public abstract void drawText(Graphics2D graphics, int width, int height);

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the fontColor
     */
    public Color getFontColor() {
        return fontColor;
    }

    /**
     * @return the fontShadowColor
     */
    public Color getFontShadowColor() {
        return fontShadowColor;
    }

    /**
     * @return the font
     */
    public Font getFont() {
        return defaultFont;
    }

    /**
     * @return the minFontSize
     */
    public int getMinFontSize() {
        return minFontSize;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @param fontColor the fontColor to set
     */
    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    /**
     * @param fontShadowColor the fontShadowColor to set
     */
    public void setFontShadowColor(Color fontShadowColor) {
        this.fontShadowColor = fontShadowColor;
    }

    /**
     * @param font the font to set
     */
    public void setFont(Font font) {
        this.defaultFont = font;
    }

    /**
     * @param minFontSize the minFontSize to set
     */
    public void setMinFontSize(int minFontSize) {
        this.minFontSize = minFontSize;
    }
    
    public int getShadowTranslation(int fontsize) {
        if(fontsize < 34) {
            return 1;
        } 
        if(fontsize < 140) {
            return 2;
        }
        
        return 3;
    }
}
