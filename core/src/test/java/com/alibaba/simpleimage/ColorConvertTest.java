/**
 * Project: simple-image-1.0 File Created at 2010-7-9 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.io.File;

/**
 * ColorConvertTest主要是测试CMYK转RGB的颜色是否失真， 带有内置profile的非标准RGB转成RGB是否失真
 * 
 * @author wendell
 */
public class ColorConvertTest extends BaseTest {

    static File cmykDir = new File("./src/test/resources/conf.test/simpleimage/cmyk");
    static File rgbDir  = new File("./src/test/resources/conf.test/simpleimage/rgb");
    static File gifDir  = new File("./src/test/resources/conf.test/simpleimage/gif");

    public void testCMYK2RGB() throws Exception {
        for (File cmykFile : cmykDir.listFiles()) {
            String fileName = cmykFile.getName().toLowerCase();
            if (fileName.endsWith("jpg")) {
                File out = new File(resultDir, "CMYK2RGB_" + fileName);
                doReadWrite(cmykFile, out, ImageFormat.JPEG);
            }
        }
    }

    public void testRGB2sRGB() throws Exception {
        for (File rgbFile : rgbDir.listFiles()) {
            String fileName = rgbFile.getName().toLowerCase();
            if (fileName.endsWith("jpg")) {
                File out = new File(resultDir, "RGB2RGB_" + fileName);
                doReadWrite(rgbFile, out, ImageFormat.JPEG);
            }
        }
    }

    public void testIndexColor2RGB() throws Exception {
        for (File gifFile : gifDir.listFiles()) {
            String fileName = gifFile.getName().toLowerCase();
            if (fileName.endsWith("gif")) {
                File out = new File(resultDir, "GIF2RGB_" + fileName);
                doReadWrite(gifFile, out, ImageFormat.GIF);
            }
        }
    }

    public void testGray2RGB() throws Exception {
        File in = new File("./src/test/resources/conf.test/simpleimage/gray/gray.jpg");
        File out = new File(resultDir, "GRAY2RGB_" + in.getName());
        doReadWrite(in, out, ImageFormat.JPEG);
    }
}
