/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.testbed;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.alibaba.security.simpleimage.analyze.sift.IImaging;
import com.alibaba.security.simpleimage.analyze.sift.ImageMap;
import com.alibaba.security.simpleimage.analyze.sift.detect.LoweFeatureDetector;
import com.alibaba.security.simpleimage.analyze.sift.draw.DisplayImage;
import com.alibaba.security.simpleimage.analyze.sift.io.KeyPointInfoReader;
import com.alibaba.security.simpleimage.analyze.sift.io.KeyPointInfoWriter;
import com.alibaba.security.simpleimage.analyze.sift.io.KeyPointListInfo;
import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类MakeKeyPoint.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-23 上午11:33:39
 */
public class MakeKeyPoint {

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("argrements must be more than 2.");
            return;
        }
        System.out.println("image source path:" + args[0]);
        System.out.println("surf file path:" + args[1]);
        makeKpFile(args[0], args[1]);
    }

    public static void makeKpFile(String imgDir, String pntDir) {
        File[] imgFs = new File(imgDir).listFiles();
        int i = 0;
        for (File f : imgFs) {
            i++;
            if (f.getName().endsWith("png") || f.getName().endsWith("jpg")) {
                BufferedImage bi = null;
                try {
                    bi = ImageIO.read(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String target = pntDir + f.getName() + ".sift";
                List<KeyPointN> al = null;
                if (bi != null) {
                    IImaging dimg = new DisplayImage(bi);
                    ImageMap picMap = dimg.toImageMap(null);
                    LoweFeatureDetector lf = new LoweFeatureDetector();
                    lf.detectFeatures(picMap);
                    al = lf.getGlobalNaturalKeypoints();
                }
                if (al == null) al = new ArrayList<KeyPointN>();
                KeyPointListInfo kpl = new KeyPointListInfo();
                kpl.setList(al);
                kpl.setWidth(bi == null ? 0 : bi.getWidth());
                kpl.setHeight(bi == null ? 0 : bi.getHeight());
                KeyPointInfoWriter.writeComplete(target, kpl);

                // check
                if (i % 100 == 0) {
                    KeyPointListInfo kpl1 = KeyPointInfoReader.readComplete(target);
                    if (kpl.getList().size() != kpl1.getList().size()) {
                        System.out.println("Write failed:" + f.getName());
                    }
                }
            }
        }

    }
}
