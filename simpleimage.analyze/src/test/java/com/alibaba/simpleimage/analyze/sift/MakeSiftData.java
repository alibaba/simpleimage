/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.ModifiableConst;
import com.alibaba.simpleimage.analyze.sift.SIFT;
import com.alibaba.simpleimage.analyze.sift.io.KDFeaturePointListInfo;
import com.alibaba.simpleimage.analyze.sift.io.KDFeaturePointWriter;
import com.alibaba.simpleimage.analyze.sift.render.RenderImage;
import com.alibaba.simpleimage.analyze.sift.scale.KDFeaturePoint;

public class MakeSiftData {

    static {
        System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
        System.setProperty(ModifiableConst._SLOPEARCSTEP, "5");
        System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

    }

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        File logoDir = new File("/Users/axman/Downloads/model/img");
        File[] logoFiles = logoDir.listFiles(new FileFilter() {

            public boolean accept(File arg0) {
                return arg0.getName().endsWith(".png");
            }
        });
        int i = 0;
        for (File logoFile : logoFiles) {
            BufferedImage img = ImageIO.read(logoFile);
            RenderImage ri = new RenderImage(img);
            SIFT sift = new SIFT();
            sift.detectFeatures(ri.toPixelFloatArray(null));
            List<KDFeaturePoint> al = sift.getGlobalKDFeaturePoints();
            KDFeaturePointListInfo info = new KDFeaturePointListInfo();
            info.setHeight(img.getHeight());
            info.setWidth(img.getWidth());
            info.setImageFile(logoFile.getName());
            info.setList(al);
            KDFeaturePointWriter.writeComplete("/Users/axman/Downloads/model/sift/" + logoFile.getName() + ".sift",
                                               info);
            i++;
            System.out.println(i);
            if (i == 100) break;
        }
        System.out.println("total times:" + (System.currentTimeMillis() - start));
    }
}
