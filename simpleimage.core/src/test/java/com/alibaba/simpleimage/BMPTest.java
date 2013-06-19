/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.io.File;


/**
 * 类BMPTest.java的实现描述：TODO 类实现描述 
 * @author wendell 2011-8-16 下午06:37:24
 */
public class BMPTest extends BaseTest {

    static File bmpDir = new File("./src/test/resources/conf.test/simpleimage/bmp");
    
    public void testBMP2JPG() throws Exception {
        for(File bmpFile : bmpDir.listFiles()) {
            String fileName = bmpFile.getName().toLowerCase();
            if(fileName.endsWith("bmp")) {
                File out = new File(resultDir, "BMP2JPG_" + fileName.substring(0, fileName.indexOf(".")) + ".jpg");
                doReadWrite(bmpFile, out, ImageFormat.JPEG);
            }
        }
    }
    
    public void testBMP2BMP() throws Exception {
        for(File bmpFile : bmpDir.listFiles()) {
            String fileName = bmpFile.getName().toLowerCase();
            if(fileName.endsWith("bmp")) {
                File out = new File(resultDir, "BMP2BMP_" + fileName);
                doReadWrite(bmpFile, out, ImageFormat.BMP);
            }
        }
    }
}
