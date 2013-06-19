/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.render;

import java.awt.Color;

import junit.framework.TestCase;


/**
 * 类TestFixDrawTextItem.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-3-28 下午03:34:50
 */
public class TestFixDrawTextItem extends TestCase {

    /**
     * Test method for {@link com.alibaba.simpleimage.render.FixDrawTextItem#getPosition()}.
     */
    public void testDefaultValue() {
        FixDrawTextItem item = new FixDrawTextItem("阿里巴巴");
        assertEquals(item.getPosition(), FixDrawTextItem.Position.CENTER);
        assertEquals(item.getTextWidthPercent(), 0.85f);
        assertEquals(item.getText(), "阿里巴巴");
        assertEquals(item.getFontColor(), new Color(255, 255, 255, 115));
        assertEquals(item.getFontShadowColor(), new Color(170, 170, 170, 77));
        String fontName = item.getFont().getFontName();
        boolean ret = "方正黑体_GBK".equalsIgnoreCase(fontName) || "FZHei-B01".equalsIgnoreCase(fontName);
        assertTrue(ret);
    }
}
