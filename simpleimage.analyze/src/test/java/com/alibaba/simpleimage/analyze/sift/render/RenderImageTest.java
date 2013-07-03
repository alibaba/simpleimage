/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import com.alibaba.simpleimage.analyze.sift.ImagePixelArray;
import com.alibaba.simpleimage.analyze.sift.render.RenderImage;
import com.alibaba.simpleimage.analyze.sift.scale.Pyramid;


public class RenderImageTest extends TestCase {

    public void testRenderImage() throws Exception {

        BufferedImage bi = ImageIO.read(this.getClass().getResource("/test.png"));
        RenderImage ri = new RenderImage(bi);
        ImagePixelArray arr = ri.toPixelFloatArray(null);
        DumpImage.dump(arr, "/Users/axman/Downloads/arr.jpg");
        //arr = arr.doubled();
        Pyramid pyr = new Pyramid();
        pyr.buildOctaves(arr, 0.5f, 3, 1.6f, 32);
        
        
        BufferedImage dest = new BufferedImage(arr.width, arr.height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < arr.height; y++) {
            for (int x = 0; x < arr.width; x++) {
                int c = (int) (arr.data[x + y * arr.width] * 255);
                Color cl = new Color(c, c, c);
                dest.setRGB(x, y, cl.getRGB());
            }
        }
        ImageIO.write(dest, "JPEG", new File("/Users/axman/Downloads/aaa.jpg"));
    }
}
