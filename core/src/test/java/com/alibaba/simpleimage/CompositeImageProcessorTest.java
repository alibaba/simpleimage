/**
 * Project: simple-image File Created at 2010-6-28 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.io.ByteArrayOutputStream;
import com.alibaba.simpleimage.render.DrawTextItem;
import com.alibaba.simpleimage.render.DrawTextParameter;
import com.alibaba.simpleimage.render.FixDrawTextItem;
import com.alibaba.simpleimage.render.FootnoteDrawTextItem;
import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.util.ImageUtils;

/**
 * TODO Comment of CompositeImageProcessorTest
 * 
 * @author wendell
 */
public class CompositeImageProcessorTest extends TestCase {

    File           sourceDir               = new File("./src/test/resources/conf.test/simpleimage");
    File           rpath                   = new File("./src/test/resources/conf.test/simpleimage/result");
    Font           DEFAULT_MAIN_TEXT_FONT  = new Font("Monospace", 0, 1);
    Color          DEFAULT_MAIN_TEXT_COLOR = new Color(0.7F, 0.7F, 0.7F, 0.50F);
    Font           DEFAULT_FOOT_TEXT_FONT  = new Font("arial", 0, 1);
    ScaleParameter DEFAULT_SCALE_PARAM     = new ScaleParameter(1024, 1024);

    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        if (!rpath.exists()) {
            rpath.mkdirs();
        }
        super.setUp();
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.CompositeImageProcessor#process(java.io.InputStream, com.alibaba.simpleimage.render.DrawTextParameter, com.alibaba.simpleimage.render.ScaleParameter, com.alibaba.simpleimage.render.WriteParameter)}
     * .
     */

    public void testProcess() throws Exception {
        CompositeImageProcessor processor = new CompositeImageProcessor();
        List<File> images = new ArrayList<File>();
        File imgDir = new File(sourceDir, "bmp");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "cmyk");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "gif");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "gray");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "rgb");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "malformed");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "quality");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "scale");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "png");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        imgDir = new File(sourceDir, "tiff");
        for (File img : imgDir.listFiles()) {
            images.add(img);
        }
        assertTrue(DEFAULT_SCALE_PARAM.getAlgorithm() == ScaleParameter.Algorithm.AUTO);
        assertTrue(DEFAULT_SCALE_PARAM.getMaxHeight() == 1024);
        assertTrue(DEFAULT_SCALE_PARAM.getMaxWidth() == 1024);
        for (File img : images) {
            if (img.isDirectory()) {
                continue;
            }
            if (img.getName().indexOf("result") > 0) {
                continue;
            } 
            // ignore this image 
            if("input_256_matte.tiff".equalsIgnoreCase(img.getName())){ 
                continue; 
            }
            
            FileInputStream inputStream = null;
            FileOutputStream outputStream = null;
            InputStream inputToStore = null;
            File outputFile = null;
            boolean check = true;
            InputStream memoryStream = null;
            String suffix = ".jpg";
            try {
                check = true;
                inputStream = new FileInputStream(img);
                memoryStream = ImageUtils.createMemoryStream(inputStream);
                if (ImageUtils.isGIF(memoryStream)) {
                    suffix = ".gif";
                }
                DrawTextParameter dtp = createDrawTextParameter("∞¢¿Ô∞Õ∞Õ≤‚ ‘", true, true);
                inputToStore = ((ByteArrayOutputStream) processor.process(memoryStream, dtp, DEFAULT_SCALE_PARAM.getMaxWidth(), DEFAULT_SCALE_PARAM.getMaxHeight())).toInputStream();
                String outputName = img.getName().substring(0, img.getName().indexOf("."));
                outputFile = new File(rpath, "COMPOSITETEST_" + outputName + suffix);
                outputStream = new FileOutputStream(outputFile);
                IOUtils.copy(inputToStore, outputStream);
                outputStream.flush();
            } catch (Exception e) {
                check = false;
            } finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(inputToStore);
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(memoryStream);
            }
            if (check) {
                check(img, outputFile);
            }
        }
    }

    protected void check(File src, File dest) throws Exception {
        FileInputStream in = null;
        FileInputStream in2 = null;
        try {
            in = new FileInputStream(dest);
            in2 = new FileInputStream(src);
            ImageRender rr = new ReadRender(in, false);
            ImageRender rr2 = new ReadRender(in2, false);
            ImageWrapper srcImg = rr2.render();
            ImageWrapper dstImg = rr.render();
            BufferedImage dstBi = dstImg.getAsBufferedImage();

            assertTrue(dstBi.getColorModel().getColorSpace().isCS_sRGB());
            assertTrue(dstBi.getWidth() <= 1024);
            assertTrue(dstBi.getHeight() <= 1024);
            if (srcImg.getImageFormat() != ImageFormat.GIF) {
                if (srcImg.getQuality() >= 50) {
                    assertTrue(dstImg.getQuality() == srcImg.getQuality());
                } else {
                    assertTrue(dstImg.getQuality() == 50);
                }

                assertTrue(srcImg.getHorizontalSamplingFactor(0) == dstImg.getHorizontalSamplingFactor(0));
                assertTrue(srcImg.getVerticalSamplingFactor(0) == dstImg.getVerticalSamplingFactor(0));
                assertTrue(srcImg.getHorizontalSamplingFactor(1) == dstImg.getHorizontalSamplingFactor(1));
                assertTrue(srcImg.getVerticalSamplingFactor(1) == dstImg.getVerticalSamplingFactor(1));
                assertTrue(srcImg.getHorizontalSamplingFactor(2) == dstImg.getHorizontalSamplingFactor(2));
                assertTrue(srcImg.getVerticalSamplingFactor(2) == dstImg.getVerticalSamplingFactor(2));
            }
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(in2);
        }
    }

    public DrawTextParameter createDrawTextParameter(String mainTxt, boolean drawMainTxt, boolean drawFootTxt) {
        List<DrawTextItem> textItems = new ArrayList<DrawTextItem>(4);
        if (drawMainTxt) {
            DrawTextItem mainTextItem = new FixDrawTextItem(mainTxt);
            textItems.add(mainTextItem);
        }

        if (drawFootTxt) {
            DrawTextItem footTextItem = new FootnoteDrawTextItem(mainTxt, "www.alibaba.com.cn");
            textItems.add(footTextItem);
        }

        DrawTextParameter dtp = new DrawTextParameter(textItems);

        return dtp;
    }
}
