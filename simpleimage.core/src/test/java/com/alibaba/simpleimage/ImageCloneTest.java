/*
 * Copyright 2012 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.io.File;

import org.w3c.dom.Node;

import com.alibaba.simpleimage.render.DrawTextParameter;
import com.alibaba.simpleimage.render.DrawTextRender;
import com.alibaba.simpleimage.render.FixDrawTextItem;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleRender;
import com.alibaba.simpleimage.render.WriteRender;
import com.alibaba.simpleimage.util.NodeUtils;

/**
 * 类ImageCloneTest.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2012-3-12 下午4:17:53
 */
public class ImageCloneTest extends BaseTest {

    static File jpgDir  = new File("./src/test/resources/conf.test/simpleimage/scale");
    static File cmykDir = new File("./src/test/resources/conf.test/simpleimage/cmyk");
    static File grayDir = new File("./src/test/resources/conf.test/simpleimage/gray");
    static File gifDir  = new File("./src/test/resources/conf.test/simpleimage/gif");
    static File pngDir  = new File("./src/test/resources/conf.test/simpleimage/png");

    public void testJPEGClone() throws Exception {
        File input = new File(jpgDir, "334.jpg");
        ImageWrapper imgWrapper = read(input);
        ImageWrapper newImg = (ImageWrapper) imgWrapper.clone();
        assertEquals(imgWrapper.getHeight(), newImg.getHeight());
        assertEquals(imgWrapper.getWidth(), newImg.getWidth());
        assertEquals(imgWrapper.getHorizontalSamplingFactor(0), newImg.getHorizontalSamplingFactor(0));
        assertEquals(imgWrapper.getHorizontalSamplingFactor(1), newImg.getHorizontalSamplingFactor(1));
        assertEquals(imgWrapper.getHorizontalSamplingFactor(2), newImg.getHorizontalSamplingFactor(2));
        assertEquals(imgWrapper.getNumOfImages(), newImg.getNumOfImages());
        assertEquals(imgWrapper.getQuality(), newImg.getQuality());
        assertEquals(imgWrapper.isBroken(), newImg.isBroken());
        assertEquals(imgWrapper.getVerticalSamplingFactor(0), newImg.getVerticalSamplingFactor(0));
        assertEquals(imgWrapper.getVerticalSamplingFactor(1), newImg.getVerticalSamplingFactor(1));
        assertEquals(imgWrapper.getVerticalSamplingFactor(2), newImg.getVerticalSamplingFactor(2));
        assertEquals(imgWrapper.getImageFormat(), newImg.getImageFormat());
    }

    public void testPNGClone() throws Exception {
        File input = new File(pngDir, "normal.png");
        ImageWrapper imgWrapper = read(input);
        ImageWrapper newImg = (ImageWrapper) imgWrapper.clone();
        assertEquals(imgWrapper.getHeight(), newImg.getHeight());
        assertEquals(imgWrapper.getWidth(), newImg.getWidth());
        assertEquals(imgWrapper.getHorizontalSamplingFactor(0), newImg.getHorizontalSamplingFactor(0));
        assertEquals(imgWrapper.getHorizontalSamplingFactor(1), newImg.getHorizontalSamplingFactor(1));
        assertEquals(imgWrapper.getHorizontalSamplingFactor(2), newImg.getHorizontalSamplingFactor(2));
        assertEquals(imgWrapper.getNumOfImages(), newImg.getNumOfImages());
        assertEquals(imgWrapper.getQuality(), newImg.getQuality());
        assertEquals(imgWrapper.getVerticalSamplingFactor(0), newImg.getVerticalSamplingFactor(0));
        assertEquals(imgWrapper.getVerticalSamplingFactor(1), newImg.getVerticalSamplingFactor(1));
        assertEquals(imgWrapper.getVerticalSamplingFactor(2), newImg.getVerticalSamplingFactor(2));
        assertEquals(imgWrapper.getImageFormat(), newImg.getImageFormat());
    }

    public void testGIFClone() throws Exception {
        File input = new File(gifDir, "1212.gif");
        ImageWrapper imgWrapper = read(input);
        ImageWrapper newImg = (ImageWrapper) imgWrapper.clone();

        assertTrue(imgWrapper.getStreamMetadata() != newImg.getStreamMetadata());
        assertEquals(imgWrapper.getHeight(), newImg.getHeight());
        assertEquals(imgWrapper.getWidth(), newImg.getWidth());
        for (int i = 0; i < imgWrapper.getNumOfImages(); i++) {
            assertEquals(imgWrapper.getHeight(i), newImg.getHeight(i));
            assertEquals(imgWrapper.getWidth(i), newImg.getWidth(i));
        }
        assertEquals(imgWrapper.getHorizontalSamplingFactor(0), newImg.getHorizontalSamplingFactor(0));
        assertEquals(imgWrapper.getHorizontalSamplingFactor(1), newImg.getHorizontalSamplingFactor(1));
        assertEquals(imgWrapper.getHorizontalSamplingFactor(2), newImg.getHorizontalSamplingFactor(2));
        assertEquals(imgWrapper.getNumOfImages(), newImg.getNumOfImages());
        assertEquals(imgWrapper.getQuality(), newImg.getQuality());
        assertEquals(imgWrapper.getVerticalSamplingFactor(0), newImg.getVerticalSamplingFactor(0));
        assertEquals(imgWrapper.getVerticalSamplingFactor(1), newImg.getVerticalSamplingFactor(1));
        assertEquals(imgWrapper.getVerticalSamplingFactor(2), newImg.getVerticalSamplingFactor(2));
        assertEquals(imgWrapper.getImageFormat(), newImg.getImageFormat());
    }

    public void testCloneInstanceIndependence() throws Exception {
        File input = new File(jpgDir, "334.jpg");
        ImageWrapper imgWrapper = read(input);
        ImageWrapper newImg = (ImageWrapper) imgWrapper.clone();
        imgWrapper.setHorizontalSamplingFactor(1, 2);
        assertEquals(2, imgWrapper.getHorizontalSamplingFactor(1));
        assertEquals(1, newImg.getHorizontalSamplingFactor(1));

        imgWrapper.setVerticalSamplingFactor(2, 2);
        assertEquals(2, imgWrapper.getVerticalSamplingFactor(2));
        assertEquals(1, newImg.getVerticalSamplingFactor(2));

        imgWrapper.setBroken(true);
        assertEquals(false, newImg.isBroken());

        input = new File(gifDir, "animate_4.gif");
        imgWrapper = read(input);
        newImg = (ImageWrapper) imgWrapper.clone();
        Node streamMetadata = imgWrapper.getStreamMetadata();
        NodeUtils.removeChild(streamMetadata, "GlobalColorTable");
        assertNull(NodeUtils.getChild(streamMetadata, "GlobalColorTable"));
        assertNotNull(NodeUtils.getChild(newImg.getStreamMetadata(), "GlobalColorTable"));

        Node imageMetadata = imgWrapper.getMetadata(0);
        assertNotNull(NodeUtils.getChild(imageMetadata, "LocalColorTable"));
        NodeUtils.removeChild(imageMetadata, "LocalColorTable");
        assertNull(NodeUtils.getChild(imageMetadata, "LocalColorTable"));
        assertNotNull(NodeUtils.getChild(newImg.getMetadata(0), "LocalColorTable"));
    }

    public void testCloneJPEGOutput() throws Exception {
        DrawTextParameter dparam = new DrawTextParameter();
        dparam.addTextInfo(new FixDrawTextItem("alibaba"));

        File[] files = jpgDir.listFiles();
  

        for (File file : files) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith("jpg")) {
                File outFile1 = new File(resultDir, "CLONEJPEG_" + fileName + "_1.jpg");
                File outFile2 = new File(resultDir, "CLONEJPEG_" + fileName + "_2.jpg");
                ImageWrapper imgWrapper = read(file);
                ImageWrapper cloneWrapper = (ImageWrapper) imgWrapper.clone();

                scale(imgWrapper, new ScaleParameter(100, 100), outFile1, ImageFormat.JPEG);
                draw(cloneWrapper, dparam, outFile2, ImageFormat.JPEG);
            }
        }
    }

    public void testCloneGIFOutput() throws Exception {
        DrawTextParameter dparam = new DrawTextParameter();
        dparam.addTextInfo(new FixDrawTextItem("alibaba"));
        
        File[] files2 = gifDir.listFiles();
        for (File file : files2) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith("gif")) {
                File outFile1 = new File(resultDir, "CLONEGIF_" + fileName + "_1.gif");
                File outFile2 = new File(resultDir, "CLONEGIF_" + fileName + "_2.gif");
                ImageWrapper imgWrapper = read(file);
                ImageWrapper cloneWrapper = (ImageWrapper) imgWrapper.clone();

                draw(imgWrapper, dparam, outFile2, ImageFormat.GIF);
                scale(cloneWrapper, new ScaleParameter(100, 100), outFile1, ImageFormat.GIF);
            }
        }
    }

    void scale(ImageWrapper imgWrapper, ScaleParameter param, File output, ImageFormat format) throws Exception {
        WriteRender wr = null;
        try {
            ImageRender sr = new ScaleRender(imgWrapper, param);
            wr = new WriteRender(sr, output, format);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
        }
    }

    void draw(ImageWrapper imgWrapper, DrawTextParameter param, File output, ImageFormat format) throws Exception {
        WriteRender wr = null;
        try {
            ImageRender sr = new DrawTextRender(imgWrapper, param);
            wr = new WriteRender(sr, output, format);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
        }
    }
}
