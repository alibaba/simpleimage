/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.testbed;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.harissurf.HarrisSurf;
import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPointN;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointNInfoReader;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointNListInfo;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointNinfoWriter;

/**
 * 类MakeSurfPoint.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-23 上午11:22:00
 */
public class MakeRootSurfPointN extends Thread {

    private String[] args;
    private int      idx;

    public MakeRootSurfPointN(String[] args, int idx){
        this.args = args;
        this.idx = idx;
    }

    public void run() {
        makeRootSpFile(args[0], args[1], idx);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("argrements must be more than 2.");
            return;
        }
//        args[0] = "/Users/axman/Downloads/model/img/";
//        args[1] = "/Users/axman/Downloads/model/rsurfn/";
        System.out.println("image source path:" + args[0]);
        System.out.println("surf file path:" + args[1]);

        for (int i = 0; i < 10; i++)
            new MakeRootSurfPointN(args, i).start();
    }

    private static void makeRootSpFile(String imgDir, String pntDir, int offset) {
        File[] imgFs = new File(imgDir).listFiles();

        int q = imgFs.length / 10;
        int end = (offset == 9) ? imgFs.length : (offset + 1) * q;
        System.out.println("work task from " + (offset * q) + ",end of " + end);
        for (int i = offset * q; i < end; i++) {
            File f = imgFs[i];
            if (f.getName().endsWith("png") || f.getName().endsWith("jpg")) {
                BufferedImage bi = null;
                try {
                    bi = ImageIO.read(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String target = pntDir + f.getName() + ".rsurfn";

                List<SURFInterestPointN> al = null;
                if (bi != null) {
                    HarrisSurf tempalte_hs = new HarrisSurf(bi);
                    tempalte_hs.getDescriptions(tempalte_hs.detectInterestPoints(), true);
                    al = tempalte_hs.getGlobalNaturalInterestPoints();
                }
                if (al == null) al = new ArrayList<SURFInterestPointN>();
                InterestPointNListInfo ipl = new InterestPointNListInfo();
                ipl.setList(al);
                ipl.setWidth(bi == null ? 0 : bi.getWidth());
                ipl.setHeight(bi == null ? 0 : bi.getHeight());
                InterestPointNinfoWriter.writeComplete(target, ipl);

                // check
                InterestPointNListInfo ipl1 = InterestPointNInfoReader.readComplete(target);
                if (ipl.getList().size() != ipl1.getList().size()) {
                    System.out.println("Write failed:" + f.getName());
                }
            }
        }
    }
}
