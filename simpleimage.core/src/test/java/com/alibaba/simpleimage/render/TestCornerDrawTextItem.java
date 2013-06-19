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
 * 类TestCornerDrawTextItem.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-3-28 下午03:48:35
 */
public class TestCornerDrawTextItem extends TestCase {
    public void testDefaultValue() {
        CornerDrawTextItem item = new CornerDrawTextItem("阿里巴巴");
        assertEquals(item.getText(), "阿里巴巴");
        assertEquals(item.getTextWidthPercent(), 0.5f);
        String fontName = item.getFont().getFontName();
        boolean ret = "方正黑体_GBK".equalsIgnoreCase(fontName) || "FZHei-B01".equalsIgnoreCase(fontName);
        assertTrue(ret);
        assertEquals(item.getFontColor(), new Color(255, 255, 255, 115));
        assertEquals(item.getFontShadowColor(), new Color(170, 170, 170, 77));
    }
}
