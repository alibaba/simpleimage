/**
 * Project: headquarters-biz-image File Created at 2010-6-17 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All
 * rights reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.render;

import java.awt.image.BufferedImage;

import junit.framework.TestCase;

import com.alibaba.simpleimage.ImageWrapper;

/**
 * TODO Comment of WatermarkRenderTest
 * 
 * @author wendell
 */
public class WatermarkRenderTest extends TestCase {

    public void testWatermarkParameter() throws Exception {
        WatermarkParameter param = new WatermarkParameter(
                                                          new ImageWrapper(
                                                                           new BufferedImage(1, 1,
                                                                                             BufferedImage.TYPE_INT_RGB)),
                                                          0.5f, 0, 0);
        try {
            param = new WatermarkParameter(null, 0.5f, 0, 0);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            param = new WatermarkParameter(null);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            param = new WatermarkParameter(new ImageWrapper(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)),
                                           -0.9f, 0, 0);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            param = new WatermarkParameter(new ImageWrapper(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)), 1.2f,
                                           0, 0);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            param = new WatermarkParameter(new ImageWrapper(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)), 0.9f,
                                           -1, 0);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }

        try {
            param = new WatermarkParameter(new ImageWrapper(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)), 0.9f,
                                           0, -2);
            assertTrue(param.getX() == 0);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
}
