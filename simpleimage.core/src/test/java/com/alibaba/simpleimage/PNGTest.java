/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.io.File;

/**
 * 类PNGTest.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2011-8-16 下午06:42:32
 */
public class PNGTest extends BaseTest {

    static File pngDir = new File("./src/test/resources/conf.test/simpleimage/png");

    public void testPNG2JPG() throws Exception {
        for (File pngFile : pngDir.listFiles()) {
            String filename = pngFile.getName().toLowerCase();
            //dst.png is 16-bit RGBA image, exception is JDK'bug
            if ("dst.png".equals(filename)) {
                continue;
            }
            if (filename.endsWith("png")) {
                File out = new File(resultDir, "PNG2JPG_" + filename.substring(0, filename.indexOf(".")) + ".jpg");
                doReadWrite(pngFile, out, ImageFormat.JPEG);
            }
        }
    }

    public void testPNG2PNG() throws Exception {
        for (File pngFile : pngDir.listFiles()) {
            String filename = pngFile.getName().toLowerCase();
            if ("dst.png".equals(filename)) {
                continue;
            }
            if (filename.endsWith("png")) {
                File out = new File(resultDir, "PNG2PNG_" + filename);
                doReadWrite(pngFile, out, ImageFormat.PNG);
            }
        }
    }
}
