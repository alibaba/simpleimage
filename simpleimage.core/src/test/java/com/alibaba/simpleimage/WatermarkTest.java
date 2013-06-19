/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleRender;
import com.alibaba.simpleimage.render.WatermarkParameter;
import com.alibaba.simpleimage.render.WatermarkRender;
import com.alibaba.simpleimage.render.WriteRender;


/**
 * 类WatermarkTest.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-8-17 下午05:18:52
 */
public class WatermarkTest extends TestCase {

    static File path = new File("./src/test/resources/conf.test/simpleimage/gif");
    static File jpegDir = new File("./src/test/resources/conf.test/simpleimage/scale");
    static File resultDir = new File("./src/test/resources/conf.test/simpleimage/result");
    
    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.WatermarkRender#WatermarkRender(com.alibaba.simpleimage.ImageRender, com.alibaba.simpleimage.render.WatermarkParameter)}
     * .
     */
    public void testJPGWatermarkRender() throws Exception {
        doTest(jpegDir, jpegDir,"334.jpg", "snow.jpg", 0.7f, 50, 50);
    }

    public void testAlphaRangeWatermarkRender() throws Exception {
        float alpha = 0.0f;
        for(int i = 0; i < 10; i++) {
            doTest(jpegDir, path, "334.jpg", "alibaba1.gif", alpha, 0, 0);
            alpha += 0.1f;
        }
    }

    public void testApp() throws Exception {
        InputStream in = null;
        ImageWrapper srcImageWrapper = null;
        ImageRender rr = null;
        try {
            in = new FileInputStream(new File(path, "cb.GIF"));
            rr = new ReadRender(in, true);
            srcImageWrapper = rr.render();
        } finally {
            if (rr != null) {
                rr.dispose();
            }
            IOUtils.closeQuietly(in);
        }
        
        int width = srcImageWrapper.getWidth();
        int height = srcImageWrapper.getHeight();
        
        ImageWrapper wmImage = null;
        ImageRender sr = null;
        try {
            in = new FileInputStream(new File(path, "alibaba1.gif"));
            rr = new ReadRender(in, false);
            sr = new ScaleRender(rr, new ScaleParameter(width, height, ScaleParameter.Algorithm.SUBSAMPLE_AVG));
            wmImage = sr.render();
        } finally {
            if (sr != null) {
                sr.dispose();
            }
            IOUtils.closeQuietly(in);
        }
        
        ImageRender wi = null;
        WatermarkParameter param = new WatermarkParameter(wmImage, 0.3f, 0, 0);
        try {
            wi = new WatermarkRender(srcImageWrapper, param);
            srcImageWrapper = wi.render();
        } finally {
            if(wi != null) {
                wi.dispose();
            }
        }
        
        write(srcImageWrapper, "app-result.jpg");
    }
    
    void doTest(File dir, File wmDir, String bg, String wm, float alpha, int x, int y) throws Exception {
        InputStream in = null;
        ImageRender sr = null;
        try {
            in = new FileInputStream(new File(dir, bg));
            ImageRender rr = new ReadRender(in, true);
            sr = new WatermarkRender(rr, createParam(wmDir, wm, alpha, x, y));
            write(sr, alpha + "-result-" + bg);
        } finally {
            if (sr != null) {
                sr.dispose();
            }
            IOUtils.closeQuietly(in);
        }
    }

    WatermarkParameter createParam(File dir, String imgPath, float alpha, int x, int y) throws Exception {
        InputStream in = null;
        ImageRender rr = null;
        ImageWrapper imageWrapper = null;
        try {
            in = new FileInputStream(new File(dir, imgPath));
            rr = new ReadRender(in, false);
            imageWrapper = rr.render();
        } finally {
            if (rr != null) {
                rr.dispose();
            }
            IOUtils.closeQuietly(in);
        }

        WatermarkParameter param = new WatermarkParameter(imageWrapper, alpha, x, y);

        return param;
    }

    public void write(ImageRender sr, String filename) throws Exception {
        OutputStream output = null;
        ImageRender wr = null;

        try {
            output = new FileOutputStream(new File(resultDir, filename));
            wr = new WriteRender(sr, output, ImageFormat.JPEG);
            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(output);
        }
    }
    
    public void write(ImageWrapper sr, String filename) throws Exception {
        OutputStream output = null;
        ImageRender wr = null;

        try {
            output = new FileOutputStream(new File(resultDir, filename));
            wr = new WriteRender(sr, output, ImageFormat.JPEG);
            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(output);
        }
    }
}
