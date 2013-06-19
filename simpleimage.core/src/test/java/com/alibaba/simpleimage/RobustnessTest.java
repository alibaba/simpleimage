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

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.WriteRender;

/**
 * 主要测试读取格式有问题的图片
 * 
 * @author wendell
 */
public class RobustnessTest extends BaseTest {
    static File imgDir = new File("./src/test/resources/conf.test/simpleimage/malformed");

    public void testReadWrite() throws Exception {
        for (File imgFile : imgDir.listFiles()) {
            if (imgFile.getName().indexOf("jpg") < 0) {
                continue;
            }
            if (imgFile.getName().indexOf("result") > 0) {
                continue;
            }

            String filename = imgFile.getName().substring(0, imgFile.getName().lastIndexOf("."));
            InputStream in = new FileInputStream(imgFile);
            OutputStream out = new FileOutputStream(new File(resultDir, "MALFORMED_" + filename + ".jpg"));
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
    }
}
