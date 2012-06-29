/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

import com.alibaba.simpleimage.render.ReadRender;

/**
 * 类JPEGBrokenTest.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2011-6-16 下午05:21:07
 */
public class JPEGBrokenTest extends TestCase {

    static String datadamage      = "./src/test/resources/conf.test/simpleimage/malformed/datadamge.jpg";
    static String huffcodeError   = "./src/test/resources/conf.test/simpleimage/malformed/huffcodeerror.jpg";
    static String prematureEnd    = "./src/test/resources/conf.test/simpleimage/malformed/prematureend.jpg";
    static String unknownMarker   = "./src/test/resources/conf.test/simpleimage/malformed/unknownmarker.jpg";

    static String correctImageDir = "./src/test/resources/conf.test/simpleimage/scale/";

    private ImageWrapper readImage(String path) throws Exception {
        File file = new File(path);
        InputStream inStream = new FileInputStream(file);
        ReadRender rr = new ReadRender(inStream, false);
        try {
            return rr.render();
        } finally {
            if (inStream != null) {
                inStream.close();
            }

            if (rr != null) {
                rr.dispose();
            }
        }
    }

    public void testDatadamage() throws Exception {
        ImageWrapper img = readImage(datadamage);
        assertTrue(img.isBroken());
    }

    public void testHuffcodeError() throws Exception {
        ImageWrapper img = readImage(huffcodeError);
        assertTrue(img.isBroken());
    }

    public void testPrematureEnd() throws Exception {
        ImageWrapper img = readImage(prematureEnd);
        assertTrue(img.isBroken());
    }

    public void testUnkownMarker() throws Exception {
        ImageWrapper img = readImage(unknownMarker);
        assertTrue(img.isBroken());
    }

    public void testCorrectImage() throws Exception {
        File dir = new File(correctImageDir);
        for (String img : dir.list()) {
            if (img.indexOf(".jpg") > 0) {
                ImageWrapper imgWrap = readImage(correctImageDir + img);
                assertFalse(imgWrap.isBroken());
            }
        }
    }
}
