/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.gif;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.BaseTest;
import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleRender;
import com.alibaba.simpleimage.render.WriteParameter;
import com.alibaba.simpleimage.render.WriteRender;


/**
 * 类GIFTest.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-8-16 下午02:25:26
 */
public class GIFTest extends BaseTest {

    static File gifDir = new File("./src/test/resources/conf.test/simpleimage/gif");
    
    
    public void testGIF2GIF() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "GIF2GIF_" + fileName);
                doReadWrite(file, outFile, ImageFormat.GIF);
            }
        }
    }
    
    public void testCMYK2GIF() throws Exception {
        File cmykDir = new File("./src/test/resources/conf.test/simpleimage/cmyk");
        for(File file : cmykDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("jpg")) {
                File outFile = new File(resultDir, "CMYK2GIF_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doReadWrite(file, outFile, ImageFormat.GIF);
            }
        }
    }
    
    public void testRGB2GIF() throws Exception {
        File rgbDir = new File("./src/test/resources/conf.test/simpleimage/rgb");
        for(File file : rgbDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("jpg")) {
                File outFile = new File(resultDir, "RGB2GIF_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doReadWrite(file, outFile, ImageFormat.GIF);
            }
        }
    }
    
    public void testNormalJPEG2GIF() throws Exception {
        File jpegDir = new File("./src/test/resources/conf.test/simpleimage/scale");
        for(File file : jpegDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("jpg")) {
                File outFile = new File(resultDir, "JPEG2GIF_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doReadWrite(file, outFile, ImageFormat.GIF);
            }
        }
    }
    
    public void testBMP2GIF() throws Exception {
        File bmpDir = new File("./src/test/resources/conf.test/simpleimage/bmp");
        for(File file : bmpDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("bmp")) {
                File outFile = new File(resultDir, "BMP2GIF_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doReadWrite(file, outFile, ImageFormat.GIF);
            }
        }
    }
    
    public void testPNG2GIF() throws Exception {
        File pngDir = new File("./src/test/resources/conf.test/simpleimage/png");
        for(File file : pngDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("png")) {
                File outFile = new File(resultDir, "PNG2GIF_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                try {
                    if(fileName.equals("dst.png") || "16rgba.png".equals(fileName)) {
                        continue;
                    }
                    doReadWrite(file, outFile, ImageFormat.GIF);
                } catch(Exception e) {
                    throw new RuntimeException(fileName, e);
                }
            }
        }
    }
    
    public void testGIF2JPEG() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "GIF2JPEG_" + fileName.substring(0, fileName.indexOf(".")) + ".jpg");
                doReadWrite(file, outFile, ImageFormat.JPEG);
            }
        }
    }
    
    public void testGIFScale() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "SCALEGIF_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doScaleWork(file, outFile, ImageFormat.GIF);
            }
        }
    }
    
    public void testColorQuantOctTree() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "GIF2GIF_OCTTREE_" + fileName);
                doColorQuant(file, outFile, WriteParameter.QuantAlgorithm.OctTree);
            }
        }
        
        File jpegDir = new File("./src/test/resources/conf.test/simpleimage/scale");
        for(File file : jpegDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("jpg")) {
                File outFile = new File(resultDir, "JPEG2GIF_OCTTREE_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doColorQuant(file, outFile, WriteParameter.QuantAlgorithm.OctTree);
            }
        }
    }
    
    void N_testColorQuantNeuQuant() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "GIF2GIF_NEUQUANT_" + fileName);
                doColorQuant(file, outFile, WriteParameter.QuantAlgorithm.NeuQuant);
            }
        }
        
        File jpegDir = new File("./src/test/resources/conf.test/simpleimage/scale");
        for(File file : jpegDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("jpg")) {
                File outFile = new File(resultDir, "JPEG2GIF_NEUQUANT_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doColorQuant(file, outFile, WriteParameter.QuantAlgorithm.NeuQuant);
            }
        }
    }
    
    public void testColorQuantMedianCut() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "GIF2GIF_MEDIANCUT_" + fileName);
                doColorQuant(file, outFile, WriteParameter.QuantAlgorithm.MedianCut);
            }
        }
        
        File jpegDir = new File("./src/test/resources/conf.test/simpleimage/scale");
        for(File file : jpegDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("jpg")) {
                File outFile = new File(resultDir, "JPEG2GIF_MEDIANCUT_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                doColorQuant(file, outFile, WriteParameter.QuantAlgorithm.MedianCut);
            }
        }
    }
    
    void doColorQuant(File in, File out, WriteParameter.QuantAlgorithm quantAlg) throws Exception {
        WriteRender wr = null;
        InputStream inStream = new FileInputStream(in);
        try {
            ReadRender rr = new ReadRender(inStream);
            wr = new WriteRender(rr, out, ImageFormat.GIF, new WriteParameter(quantAlg));
            
            wr.render();
        } finally {
            IOUtils.closeQuietly(inStream);
            
            if(wr != null) {
                wr.dispose();
            }
        }
    }
    
    void doScaleWork(File in, boolean toRGB, File out, ImageFormat format) throws Exception {
        WriteRender wr = null;
        InputStream inStream = new FileInputStream(in);
        try {
            ReadRender rr = new ReadRender(inStream, toRGB);
            ScaleRender sr = new ScaleRender(rr, new ScaleParameter(100, 100));
            wr = new WriteRender(sr, out, format);
            
            wr.render();
        } finally {
            IOUtils.closeQuietly(inStream);
            
            if(wr != null) {
                wr.dispose();
            }
        }
    }
    
    void doScaleWork(File in, File out, ImageFormat format) throws Exception {
        this.doScaleWork(in, true, out, format);
    }
    
    public void testGIFSize() throws Exception {
        ImageWrapper img = read(new File(gifDir, "size.gif"));
        assertEquals(720, img.getWidth());
        assertEquals(531, img.getHeight());
        
        img = read(new File(gifDir, "1212.gif"));
        assertEquals(266, img.getWidth());
        assertEquals(200, img.getHeight());
        
        img = read(new File(gifDir, "GIF89a_1.gif"));
        assertEquals(640, img.getWidth());
        assertEquals(480, img.getHeight());
        
        img = read(new File(gifDir, "input87.gif"));
        assertEquals(70, img.getWidth());
        assertEquals(46, img.getHeight());
    }
    
    public void testIndexColorModelGIFScale() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "SCALEGIF_" + fileName.substring(0, fileName.indexOf(".")) + ".gif");
                if(fileName.equals("animate_transparent.gif") || fileName.equals("input87.gif")) {
                    continue;
                }
                try {
                    doScaleWork(file, false, outFile, ImageFormat.GIF);
                } catch(SimpleImageException e) {
                    assertTrue(e.getMessage(), true);
                    continue;
                }
                assertTrue("IndexColorModel GIF scale success: " + fileName, false);
            }
        }
    }
    
    public void testIndexColorModelGIFOutput() throws Exception {
        for(File file : gifDir.listFiles()) {
            String fileName = file.getName().toLowerCase();
            if(fileName.endsWith("gif")) {
                File outFile = new File(resultDir, "GIF2GIF_" + fileName);
                doReadWrite(file, false, outFile, ImageFormat.GIF);
            }
        }
    }
}
