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
import java.awt.font.FontRenderContext;

import org.apache.commons.lang.StringUtils;

import com.alibaba.simpleimage.font.FontManager;

/**
 * 类FootnoteDrawTextItem.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2011-3-28 下午02:56:23
 */
public class FootnoteDrawTextItem extends DrawTextItem {

    static final Color FONT_COLOR                = new Color(255, 255, 255, 153);
    static final Color FONT_SHADOW_COLOR         = new Color(170, 170, 170, 115);
    static final Font  DEFAULT_COMPANY_NAME_FONT = FontManager.getFont("方正黑体");
    static final Font  DEFAULT_DOMAIN_NAME_FONT  = new Font("Arial", Font.PLAIN, 10);
    static final float TEXT_WIDTH_PERCENT        = 0.8F;
    static final int   MIN_FONT_SIZE             = 10;

    protected float    textWidthPercent;
    protected Font     domainFont;
    protected String   domainName;

    public FootnoteDrawTextItem(String text, String domainName){
        super(text, FONT_COLOR, FONT_SHADOW_COLOR, DEFAULT_COMPANY_NAME_FONT, MIN_FONT_SIZE);
        this.domainName = domainName;
        this.textWidthPercent = TEXT_WIDTH_PERCENT;
        this.domainFont = DEFAULT_DOMAIN_NAME_FONT;
    }
    
    public FootnoteDrawTextItem(String text, String domainName, float textWidthPercent, Font domainNameFont) {
        super(text, FONT_COLOR, FONT_SHADOW_COLOR, DEFAULT_COMPANY_NAME_FONT, MIN_FONT_SIZE);
        this.domainName = domainName;
        this.textWidthPercent = textWidthPercent;
        this.domainFont = domainNameFont;
    }
    /**
     * @param text
     * @param fontColor
     * @param fontShadowColor
     * @param font
     * @param minFontSize
     */
    public FootnoteDrawTextItem(String text, String domainName, Color fontColor, Color fontShadowColor, Font font, Font domainFont, int minFontSize,
                                float textWidthPercent) {
        super(text, fontColor, fontShadowColor, font, minFontSize);
        this.domainName = domainName;
        this.domainFont = domainFont;
        this.textWidthPercent = textWidthPercent;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.render.DrawTextItem#drawText(java.awt.Graphics2D, int, int)
     */
    @Override
    public void drawText(Graphics2D graphics, int width, int height) {
        if(StringUtils.isBlank(text) || StringUtils.isBlank(domainName)) {
            return ;
        }
        
        int x = 0, y = 0; 
        int fontsize = ((int)(width * textWidthPercent)) / domainName.length();
        if (fontsize < minFontSize) {
            return;
        }
        
        float fsize = (float)fontsize;
        Font font = domainFont.deriveFont(fsize);
        graphics.setFont(font);
        FontRenderContext context = graphics.getFontRenderContext();
        int sw = (int) font.getStringBounds(domainName, context).getWidth();

        x = width - sw - fontsize;
        y = height - fontsize;
        if(x <= 0 || y <= 0) {
            return ;
        }

        if (fontShadowColor != null) {
            graphics.setColor(fontShadowColor);
            graphics.drawString(domainName, x + getShadowTranslation(fontsize), y + getShadowTranslation(fontsize));
        }
        graphics.setColor(fontColor);
        graphics.drawString(domainName, x, y);
        
        //draw company name
        fsize = (float)fontsize;
        font = defaultFont.deriveFont(fsize);
        graphics.setFont(font);
        context = graphics.getFontRenderContext();
        sw = (int) font.getStringBounds(text, context).getWidth();
        x = width - sw - fontsize;
        y = height - (int)(fontsize * 2.5);
        if(x <= 0 || y <= 0) {
            return ;
        }
        
        if (fontShadowColor != null) {
            graphics.setColor(fontShadowColor);
            graphics.drawString(text, x + getShadowTranslation(fontsize), y + getShadowTranslation(fontsize));
        }
        graphics.setColor(fontColor);
        graphics.drawString(text, x, y);
    }

    
    /**
     * @return the textWidthPercent
     */
    public float getTextWidthPercent() {
        return textWidthPercent;
    }

    
    /**
     * @return the domainFont
     */
    public Font getDomainFont() {
        return domainFont;
    }

    
    /**
     * @param textWidthPercent the textWidthPercent to set
     */
    public void setTextWidthPercent(float textWidthPercent) {
        this.textWidthPercent = textWidthPercent;
    }

    
    /**
     * @param domainFont the domainFont to set
     */
    public void setDomainFont(Font domainFont) {
        this.domainFont = domainFont;
    }
    
    /**
     * @return the domainName
     */
    public String getDomainName() {
        return domainName;
    }

    
    /**
     * @param domainName the domainName to set
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}
