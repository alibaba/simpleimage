/**
 * Project: simpleimage-1.1
 * 
 * File Created at 2010-9-1
 * $Id$
 * 
 * Copyright 2008 Alibaba.com Croporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Alibaba Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Alibaba.com.
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
import com.alibaba.simpleimage.render.WriteRender;

/**
 * TODO Comment of JPEGQualityTest
 * @author wendell
 *
 */
public class JPEGQualityTest extends TestCase {
    static File quaDir = new File("./src/test/resources/conf.test/simpleimage/quality");
    static File resultDir = new File("./src/test/resources/conf.test/simpleimage/result");
    
    public void testQualityConsistent() throws Exception{
        for (File imgFile : quaDir.listFiles()) {
            if(imgFile.getName().indexOf("jpg") < 0){
                continue;
            }
            
            InputStream in = new FileInputStream(imgFile);
            OutputStream out = new FileOutputStream(new File(resultDir, "QUALITY_" + imgFile.getName()));
            WriteRender wr = null;
            try {
                ReadRender rr = new ReadRender(in, true);
                wr = new WriteRender(rr, out);

                wr.render();
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        
        ImageWrapper wi = null;
        
        wi = readImage(resultDir, "QUALITY_quality_80.jpg");
        assertTrue(wi.getQuality() == 80);
        
        wi = readImage(resultDir, "QUALITY_quality_90.jpg");
        assertTrue(wi.getQuality() == 90);
        
        wi = readImage(resultDir, "QUALITY_quality_95.jpg");
        assertTrue(wi.getQuality() == 95);
        
        wi = readImage(resultDir, "QUALITY_seq_1x1_1x1_1x1.jpg");
        for(int i = 0; i < 3; i++){
            assertTrue( wi.getHorizontalSamplingFactor(0) == 1);
            assertTrue( wi.getVerticalSamplingFactor(0) == 1);
        }

        wi = readImage(resultDir, "QUALITY_seq_1x2_1x1_1x1.jpg");    
        assertTrue(wi.getHorizontalSamplingFactor(0) == 1);
        assertTrue(wi.getVerticalSamplingFactor(0) == 2);
        assertTrue(wi.getHorizontalSamplingFactor(1) == 1);
        assertTrue(wi.getVerticalSamplingFactor(1) == 1);
        assertTrue(wi.getHorizontalSamplingFactor(2) == 1);
        assertTrue(wi.getVerticalSamplingFactor(2) == 1);
        
        wi = readImage(resultDir, "QUALITY_seq_2x1_1x1_1x1.jpg");
        assertTrue(wi.getHorizontalSamplingFactor(0) == 2);
        assertTrue(wi.getVerticalSamplingFactor(0) == 1);
        assertTrue(wi.getHorizontalSamplingFactor(1) == 1);
        assertTrue(wi.getVerticalSamplingFactor(1) == 1);
        assertTrue(wi.getHorizontalSamplingFactor(2) == 1);
        assertTrue(wi.getVerticalSamplingFactor(2) == 1);
        
        wi = readImage(resultDir, "QUALITY_seq_2x2_1x1_1x1.jpg");
        assertTrue(wi.getHorizontalSamplingFactor(0) == 2);
        assertTrue(wi.getVerticalSamplingFactor(0) == 2);
        assertTrue(wi.getHorizontalSamplingFactor(1) == 1);
        assertTrue(wi.getVerticalSamplingFactor(1) == 1);
        assertTrue(wi.getHorizontalSamplingFactor(2) == 1);
        assertTrue(wi.getVerticalSamplingFactor(2) == 1);
    }
    
    public ImageWrapper readImage(File dir, String filename) throws Exception{
        InputStream in = new FileInputStream(new File(dir, filename));
        try {
            ReadRender rr = new ReadRender(in, true);
            
            return rr.render();
        }finally{
            IOUtils.closeQuietly(in);
        }
    }
}
