/**
 * Project: simple-image-1.0 File Created at 2010-7-13 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.jai.cmm;

import java.awt.color.ColorSpace;

import junit.framework.TestCase;

/**
 * TODO Comment of CMMTest
 * 
 * @author wendell
 */
public class CMMTest extends TestCase {

    public void testCMYK() throws Exception {
        ColorSpace cs = CMMColorSpace.getInstance(ColorSpace.TYPE_CMYK);
        assertTrue(cs.getType() == ColorSpace.TYPE_CMYK);
    }
}
