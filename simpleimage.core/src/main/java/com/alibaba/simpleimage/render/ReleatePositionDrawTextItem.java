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
 * 类ReleatePositionDrawTextItem.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-3-28 下午01:43:45
 */
public class ReleatePositionDrawTextItem extends DrawTextItem {
     // 主要的文本占图片宽度的百分比,比如0.85,0.95
     protected float textWidthPercent;
     
     protected float xFactor;
     protected float yFactor;
    /**
     * @param text
     * @param fontColor
     * @param fontShadowColor
     * @param font
     * @param minFontSize
     * @param textWidthPercent
     */
    public ReleatePositionDrawTextItem(String text, Color fontColor, Color fontShadowColor, Font font, int minFontSize,
                                       float textWidthPercent, float xFactor, float yFactor){
        super(text, fontColor, fontShadowColor, font, minFontSize);
        this.textWidthPercent = textWidthPercent;
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    /* (non-Javadoc)
     * @see com.alibaba.simpleimage.render.DrawTextItem#drawText(java.awt.Graphics2D, int, int)
     */
    @Override
    public void drawText(Graphics2D graphics, int width, int height) {
        int textLength = (int) (width * textWidthPercent);
        // 计算水印字体大小
        int fontsize = textLength / text.length();
        if(fontsize < minFontSize) {
            return ;
        }

        graphics.setFont(new Font(defaultFont.getFontName(), Font.PLAIN, fontsize));
        graphics.setColor(fontColor);
        graphics.drawString(text, (int)(width * xFactor), (int)(height * yFactor));
    }

}
