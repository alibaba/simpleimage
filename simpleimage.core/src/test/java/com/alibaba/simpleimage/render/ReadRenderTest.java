/**
 * Project: headquarters-biz-image File Created at 2010-6-17 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All
 * rights reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.ImageWrapper;

/**
 * TODO Comment of ReadRenderText
 * 
 * @author wendell
 */
public class ReadRenderTest extends TestCase {

    static File path  = new File("./src/test/resources/conf.test/simpleimage/scale");
    static File rpath = new File("./src/test/resources/conf.test/simpleimage/result");

    /**
     * Test method for {@link com.alibaba.simpleimage.render.ReadRender#ReadRender(java.io.InputStream, boolean)}.
     */
    public void testReadRenderInputStreamBoolean() throws Exception {
        File file = new File(path, "334.jpg");
        InputStream input = null;
        ImageWrapper img = null;
        ImageRender rr = null;

        try {
            input = new FileInputStream(file);
            rr = new ReadRender(input, true);
            img = rr.render();
            assertNotNull(img);

        } finally {
            IOUtils.closeQuietly(input);
            if (rr != null) {
                rr.dispose();
            }
        }
    }
    
    public void testReadImageFormat() throws Exception {
        File dir1 = new File("./src/test/resources/conf.test/simpleimage/rgb");
        ImageWrapper img = read(dir1, "adobe_RGB_1998.jpg");
        assertEquals(ImageFormat.JPEG, img.getImageFormat());
        
        File dir2 = new File("./src/test/resources/conf.test/simpleimage/gif");
        img = read(dir2, "1212.gif");
        assertEquals(ImageFormat.GIF, img.getImageFormat());
        
        File dir3 = new File("./src/test/resources/conf.test/simpleimage/png");
        img = read(dir3, "dst.png");
        assertEquals(ImageFormat.PNG, img.getImageFormat());
        
        File dir4 = new File("./src/test/resources/conf.test/simpleimage/bmp");
        img = read(dir4, "normal.bmp");
        assertEquals(ImageFormat.BMP, img.getImageFormat());
        
        File dir5 = new File("./src/test/resources/conf.test/simpleimage/tiff");
        img = read(dir5, "input_16.tiff");
        assertEquals(ImageFormat.TIFF, img.getImageFormat());
    }
    
    private ImageWrapper read(File dir, String filename) throws Exception {
        File file = new File(dir, filename);
        InputStream input = null;
        ImageWrapper img = null;
        ImageRender rr = null;

        try {
            input = new FileInputStream(file);
            rr = new ReadRender(input);
            img = rr.render();
            
            return img;
        } finally {
            IOUtils.closeQuietly(input);
            if (rr != null) {
                rr.dispose();
            }
        }
    }

    /**
     * Test method for {@link com.alibaba.simpleimage.render.ReadRender#ReadRender(java.io.InputStream)}.
     */
    public void testReadRenderInputStream() throws Exception {
        File file = new File(path, "334.jpg");
        InputStream input = null;
        ImageWrapper img = null;
        ImageRender rr = null;

        try {
            input = new FileInputStream(file);
            rr = new ReadRender(input);
            img = rr.render();
            assertNotNull(img);

        } finally {
            IOUtils.closeQuietly(input);
            if (rr != null) {
                rr.dispose();
            }
        }
    }
}
