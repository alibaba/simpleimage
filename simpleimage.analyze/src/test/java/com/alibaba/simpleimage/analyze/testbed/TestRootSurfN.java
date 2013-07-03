/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.testbed;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.ModifiableConst;
import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPointN;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointNInfoReader;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointNListInfo;
import com.alibaba.simpleimage.analyze.harris.match.SurfMatch;
import com.alibaba.simpleimage.analyze.harris.match.SurfMatchPoints;

/**
 * 类TestSurf.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-23 上午11:37:12
 */
public class TestRootSurfN extends Thread {

    static {
        System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
        System.setProperty(ModifiableConst._SLOPEARCSTEP, "2");
        System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

    }

    private String[] args;
    private int      idx;

    public TestRootSurfN(String[] args, int idx){
        this.args = args;
        this.idx = idx;
    }

    public void run() {
        try {
            testSurf(args, idx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("argrements must be more than 3.");
            return;
        }
        System.out.println("model path:" + args[0]);
        System.out.println("logo path:" + args[1]);
        System.out.println("diff file path:" + args[2]);
        // args[0] = "/Users/axman/Downloads/model/";
        // args[1] = "/Users/axman/Downloads/logo/";
        // args[2] = "/Users/axman/Downloads/output/";
        for (int i = 0; i < 10; i++)
            new TestRootSurfN(args, i).start();
    }

    public static void testSurf(String[] args, int offset) throws IOException {

        String model_path = args[0];
        String logo_path = args[1];
        String diff_filepath = args[2];
        String model_surf_path = model_path + "rsurfn/";
        String logo_surf_path = logo_path + "rsurfn/";
        String model_img_path = model_path + "img/";
        String logo_img_path = logo_path + "img/";

        File[] tfs = new File(model_surf_path).listFiles();
        File[] fs = new File(logo_surf_path).listFiles();

        Map<String, InterestPointNListInfo> logoPonits = new HashMap<String, InterestPointNListInfo>();
        Map<String, BufferedImage> logoImgs = new HashMap<String, BufferedImage>();

        for (File f : fs) {
            String logo_surf_name = f.getName();
            if (!logo_surf_name.endsWith("rsurfn")) continue;
            InterestPointNListInfo ipl = InterestPointNInfoReader.readComplete(f.getAbsolutePath());
            if (f.getName().startsWith("taobao_annual_des_1.png")) // read from a configuration file
            ipl.setMaxSize(30);
            logoPonits.put(logo_surf_name, ipl);

        }
        int q = tfs.length / 10;
        int end = (offset == 9) ? tfs.length : (offset + 1) * q;
        System.out.println("work task from " + (offset * q) + ",end of " + end);
        Color[] cs = { Color.RED, Color.GREEN, Color.BLUE };
        for (int i = offset * q; i < end; i++) {
            File tf = tfs[i];
            if (!tf.getName().endsWith("rsurfn")) continue;
            InterestPointNListInfo ipl = InterestPointNInfoReader.readComplete(tf.getAbsolutePath());

            List<SURFInterestPointN> targetPoint = ipl.getList();
            if (targetPoint.size() < ModifiableConst.getMinPointCount()) continue;
            for (Entry<String, InterestPointNListInfo> e : logoPonits.entrySet()) {
                InterestPointNListInfo logoipl = e.getValue();
                String logo_surf_name = e.getKey();
                List<SurfMatch> ms = null;
                try {
                    ms = SurfMatchPoints.findMatchesBBF(logoipl.getList(), targetPoint);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (ms == null) continue;

                ms = SurfMatchPoints.filterFarMatchL(ms, logoipl.getWidth(), logoipl.getHeight());
                ms = SurfMatchPoints.filterJoins(ms);
                if (ms.size() < logoipl.getMaxSize()) continue;
                FileOutputStream fos;
                BufferedImage logo = logoImgs.get(logo_surf_name);
                if (logo == null) {
                    logo = ImageIO.read(new File(logo_img_path + logo_surf_name.replaceFirst(".rsurfn", "")));
                    logoImgs.put(logo_surf_name, logo);
                }
                BufferedImage model = ImageIO.read(new File(model_img_path + tf.getName().replaceFirst(".rsurfn", "")));
                int logo_width = logo.getWidth();
                int logo_height = logo.getHeight();

                int model_width = model.getWidth();
                int model_height = model.getHeight();
                BufferedImage outputImage = new BufferedImage(logo_width + model_width, logo_height + model_height,
                                                              BufferedImage.TYPE_INT_RGB);

                Graphics2D g = outputImage.createGraphics();
                g.drawImage(logo, 0, 0, logo_width, logo_height, null);
                g.drawImage(model, logo_width, logo_height, model_width, model_height, null);

                int x = 0;
                for (SurfMatch m : ms) {

                    SURFInterestPointN fromPoint = m.getSp1();
                    SURFInterestPointN toPoint = m.getSp2();
                    g.setColor(cs[x++ % 3]);
                    g.drawLine((int) fromPoint.getX(), (int) fromPoint.getY(), (int) toPoint.getX() + logo_width,
                               (int) toPoint.getY() + logo_height);
                }
                g.dispose();
                fos = new FileOutputStream(diff_filepath + tf.getName() + "_" + logo_surf_name + "2." + ms.size()
                                           + ".jpg");
                ImageIO.write(outputImage, "JPEG", fos);
                fos.close();
                break;
            }
            System.out.println("i=" + i);
        }
    }
}
