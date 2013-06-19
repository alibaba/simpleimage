/**
 * Project: headquarters-biz-image File Created at 2010-6-17 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All
 * rights reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageRender;

/**
 * TODO Comment of ScaleRenderTest
 * 
 * @author wendell
 */
public class ScaleRenderTest extends TestCase {

    static File path = new File("./src/test/resources/conf.test/simpleimage/scale");

    public ScaleParameter getParam() {
        ScaleParameter param = new ScaleParameter(300, 300);

        return param;
    }

    public void testDefaultValue() {
        ScaleParameter param = new ScaleParameter(300, 300);
        assertEquals(param.getAlgorithm(), ScaleParameter.Algorithm.AUTO);
    }
    
    public void write(ImageRender sr) throws Exception {
        OutputStream output = null;
        ImageRender wr = null;
        try {
            output = new FileOutputStream(new File("./src/test/resources/conf.test/simpleimage/result/snow-result.jpg"));
            wr = new WriteRender(sr, output, ImageFormat.JPEG);
            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.ScaleRender#ScaleRender(java.io.InputStream, com.alibaba.simpleimage.render.ScaleParameter)}
     * .
     */

    public void testScaleRenderInputStreamScaleParameter() throws Exception {
        InputStream input = null;
        ImageRender sr = null;
        try {
            input = new FileInputStream(path.getCanonicalFile() + File.separator + "snow.jpg");
            sr = new ScaleRender(input, getParam());
            write(sr);
        } finally {
            if (sr != null) {
                sr.dispose();
            }
            IOUtils.closeQuietly(input);
        }
    }


    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.ScaleRender#ScaleRender(java.io.InputStream, boolean, com.alibaba.simpleimage.render.ScaleParameter)}
     * .
     */
    public void testScaleRenderInputStreamBooleanScaleParameter() throws Exception {
        InputStream input = null;
        ImageRender sr = null;
        try {
            input = new FileInputStream(path.getCanonicalFile() + File.separator + "snow.jpg");
            sr = new ScaleRender(input, true, getParam());
            write(sr);
        } finally {
            if (sr != null) {
                sr.dispose();
            }
            IOUtils.closeQuietly(input);
        }
    }
}
