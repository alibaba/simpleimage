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
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.WriteRender;


/**
 * 类BaseTest.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-8-17 下午04:18:21
 */
public class BaseTest extends TestCase {
    public static File resultDir = new File("./src/test/resources/conf.test/simpleimage/result");
    
    public void doReadWrite(File in, File out, ImageFormat format) throws Exception {
        doReadWrite(in, true, out, format);
    }
    
    public void doReadWrite(File in, boolean toRGB, File out, ImageFormat format) throws Exception {
        WriteRender wr = null;
        InputStream inStream = new FileInputStream(in);
        try {
            ReadRender rr = new ReadRender(inStream, toRGB);
            wr = new WriteRender(rr, out, format);
            
            wr.render();
        } finally {
            IOUtils.closeQuietly(inStream);
            
            if(wr != null) {
                wr.dispose();
            }
        }
    }
    
    public ImageWrapper read(File file) throws Exception {
        ImageRender rr = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            rr = new ReadRender(in);
            
            return rr.render();
        } finally {
            IOUtils.closeQuietly(in);
            if(rr != null) {
                rr.dispose();
            }
        }
    }
    
    public void testNull() {
        
    }
}
