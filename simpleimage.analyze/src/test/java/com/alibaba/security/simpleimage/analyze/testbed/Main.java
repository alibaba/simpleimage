/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.testbed;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import com.alibaba.security.simpleimage.analyze.harissurf.HarrisSurf;
import com.alibaba.security.simpleimage.analyze.harissurf.SURFInterestPoint;
import com.alibaba.security.simpleimage.analyze.harissurf.SURFInterestPointN;
import com.alibaba.security.simpleimage.analyze.harris.io.InterestPointNListInfo;
import com.alibaba.security.simpleimage.analyze.harris.match.SurfMatch;
import com.alibaba.security.simpleimage.analyze.harris.match.SurfMatchPoints;
import com.alibaba.security.simpleimage.analyze.sift.ModifiableConst;

public class Main {

    static {
        System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
        System.setProperty(ModifiableConst._SLOPEARCSTEP, "5");
        System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

    }

    public static void drawImage(BufferedImage logo, BufferedImage model, String file, List<SurfMatch> ms)
                                                                                                          throws Exception {
        int lw = logo.getWidth();
        int lh = logo.getHeight();

        int mw = model.getWidth();
        int mh = model.getHeight();

        BufferedImage outputImage = new BufferedImage(lw + mw, lh + mh, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = outputImage.createGraphics();
        g.drawImage(logo, 0, 0, lw, lh, null);
        g.drawImage(model, lw, lh, lw + mw, lh + mh, null);
        g.setColor(Color.GREEN);
        for (SurfMatch m : ms) {
            SURFInterestPointN fromPoint = m.getSp1();
            SURFInterestPointN toPoint = m.getSp2();
            g.drawLine((int) fromPoint.getX(), (int) fromPoint.getY(), (int) toPoint.getX() + lw, (int) toPoint.getY()
                                                                                                  + lh);
        }
        g.dispose();
        FileOutputStream fos = new FileOutputStream(file);
        ImageIO.write(outputImage, "JPEG", fos);
        fos.close();
    }

    public static void drawImage(BufferedImage logo, BufferedImage model, String file,
                                 Map<SURFInterestPoint, SURFInterestPoint> ms) throws Exception {
        int lw = logo.getWidth();
        int lh = logo.getHeight();
        int mw = model.getWidth();
        int mh = model.getHeight();

        BufferedImage outputImage = new BufferedImage(lw + mw, lh + mh, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = outputImage.createGraphics();
        g.drawImage(logo, 0, 0, lw, lh, null);
        g.drawImage(model, lw, lh, lw + mw, lh + mh, null);
        g.setColor(Color.GREEN);
        for (Entry<SURFInterestPoint, SURFInterestPoint> e : ms.entrySet()) {
            SURFInterestPoint fromPoint = e.getKey();
            SURFInterestPoint toPoint = e.getValue();
            g.drawLine((int) fromPoint.getX(), (int) fromPoint.getY(), (int) toPoint.getX() + lw, (int) toPoint.getY()
                                                                                                  + lh);
        }
        g.dispose();
        FileOutputStream fos = new FileOutputStream(file);
        ImageIO.write(outputImage, "JPEG", fos);
        fos.close();
    }

    public static void main(String[] args) throws Exception {

        BufferedImage bil = ImageIO.read(new File("/Users/axman/Downloads/logo/img/taobao_login_des_1.png"));

        HarrisSurf tempalte_hs = new HarrisSurf(bil);
        tempalte_hs.getDescriptions(tempalte_hs.detectInterestPoints(), true);
        List<SURFInterestPoint> logo = tempalte_hs.getInterestPoints();

        List<SURFInterestPointN> logoN = tempalte_hs.getGlobalNaturalInterestPoints();

        BufferedImage bim = ImageIO.read(new File("/Users/axman/Downloads/model/image/51242944.png"));
        HarrisSurf model_hs = new HarrisSurf(bim);
        model_hs.getDescriptions(model_hs.detectInterestPoints(), true);
        List<SURFInterestPoint> model = model_hs.getInterestPoints();

        List<SURFInterestPointN> modelN = model_hs.getGlobalNaturalInterestPoints();

        Map<SURFInterestPoint, SURFInterestPoint> matchMap = HarrisSurf.match(logo, model);
        System.out.println(matchMap.size());
        drawImage(bil, bim, "/Users/axman/Downloads/mapall.jpg", matchMap);

        HarrisSurf.geometricFilter(matchMap, bil.getWidth(), bil.getHeight());
        System.out.println(matchMap.size());
        drawImage(bil, bim, "/Users/axman/Downloads/mapf1.jpg", matchMap);

        HarrisSurf.joinsFilter(matchMap);
        System.out.println(matchMap.size());
        drawImage(bil, bim, "/Users/axman/Downloads/mapf2.jpg", matchMap);

        List<SurfMatch> ms = SurfMatchPoints.findMatchesBBF(logoN, modelN);

        System.out.println(ms.size());
        drawImage(bil, bim, "/Users/axman/Downloads/listall.jpg", ms);

        ms = SurfMatchPoints.filterFarMatchL(ms, bil.getWidth(), bil.getHeight());
        System.out.println(ms.size());
        drawImage(bil, bim, "/Users/axman/Downloads/listf1.jpg", ms);

        ms = SurfMatchPoints.filterJoins(ms);
        System.out.println(ms.size());
        drawImage(bil, bim, "/Users/axman/Downloads/listf2.jpg", ms);

    }

    static class TestImgSurf extends Thread {

        private String[] args;
        private int      idx;

        public TestImgSurf(String[] args, int idx){
            this.args = args;
            this.idx = idx;
        }

        public void run() {
            try {
                testImgSurf(args, idx);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static InterestPointNListInfo readComplete(File file) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<SURFInterestPointN> al = null;
        if (bi != null) {
            HarrisSurf tempalte_hs = new HarrisSurf(bi);
            tempalte_hs.getDescriptions(tempalte_hs.detectInterestPoints(), false);
            al = tempalte_hs.getGlobalNaturalInterestPoints();
        }
        if (al == null) al = new ArrayList<SURFInterestPointN>();
        InterestPointNListInfo ipln = new InterestPointNListInfo();
        ipln.setList(al);
        ipln.setWidth(bi == null ? 0 : bi.getWidth());
        ipln.setHeight(bi == null ? 0 : bi.getHeight());
        return ipln;
    }

    public static void testImgSurf(String[] args, int offset) throws IOException {

        // String model_path = args[0];
        String model_path = "/Users/axman/Downloads/";
        // String logo_path = args[1];
        String logo_path = "/Users/axman/Downloads/img/";
        // String diff_filepath = args[2];
        String diff_filepath = "/Users/axman/Downloads/";

        File[] tfs = new File(model_path).listFiles();
        File[] fs = new File(logo_path).listFiles();

        Map<String, InterestPointNListInfo> logoPonits = new HashMap<String, InterestPointNListInfo>();
        Map<String, BufferedImage> logoImgs = new HashMap<String, BufferedImage>();

        for (File f : fs) {
            String logo_name = f.getName();
            if (!logo_name.endsWith("png")) continue;
            logoPonits.put(logo_name, readComplete(f));
        }

        int q = tfs.length / 10;
        int end = (offset == 9) ? tfs.length : (offset + 1) * q;
        System.out.println("work task from " + (offset * q) + ",end of " + end);
        for (File tf : tfs) {
            // File tf = tfs[i];
            if (!tf.getName().endsWith("png")) continue;
            InterestPointNListInfo ipl = readComplete(tf);
            List<SURFInterestPointN> targetPoint = ipl.getList();

            for (Entry<String, InterestPointNListInfo> e : logoPonits.entrySet()) {

                InterestPointNListInfo logoipl = e.getValue();
                String logo_name = e.getKey();
                List<SurfMatch> ms = SurfMatchPoints.findMatchesBBF(logoipl.getList(), targetPoint);
                ms = SurfMatchPoints.filterFarMatchL(ms, logoipl.getWidth(), logoipl.getHeight());
                ms = SurfMatchPoints.filterJoins(ms);

                // if (matchMap.size() < 5) continue;
                System.out.println("match points:" + ms.size());

                FileOutputStream fos;
                BufferedImage logo = logoImgs.get(logo_name);
                if (logo == null) {
                    logo = ImageIO.read(new File(logo_path + logo_name));
                    logoImgs.put(logo_name, logo);
                }
                BufferedImage model = ImageIO.read(new File(model_path + tf.getName()));
                int logo_width = logo.getWidth();
                int logo_height = logo.getHeight();

                int model_width = model.getWidth();
                int model_height = model.getHeight();
                BufferedImage outputImage = new BufferedImage(Math.max(logo_width, model_width), logo_height
                                                                                                 + model_height,
                                                              BufferedImage.TYPE_INT_RGB);

                Graphics2D g = outputImage.createGraphics();
                g.drawImage(logo, 0, 0, logo_width, logo_height, null);
                g.drawImage(model, 0, logo_height, model_width, model_height, null);

                for (SurfMatch m : ms) {

                    SURFInterestPointN fromPoint = m.getSp1();
                    SURFInterestPointN toPoint = m.getSp2();
                    g.setColor(Color.BLUE);
                    g.drawOval((int) fromPoint.getX() - 2, (int) fromPoint.getY() - 2, 4, 4);
                    g.drawOval((int) toPoint.getX() - 2, (int) toPoint.getY() + logo_height - 2, 4, 4);
                    g.setColor(Color.GREEN);
                    g.drawLine((int) fromPoint.getX(), (int) fromPoint.getY(), (int) toPoint.getX(),
                               (int) toPoint.getY() + logo_height);
                }
                g.dispose();
                fos = new FileOutputStream(diff_filepath + tf.getName() + "_" + logo_name + ".jpg");
                ImageIO.write(outputImage, "JPEG", fos);
                fos.close();
                // break;
            }
            // System.out.println("i=" + i);
        }
    }

    public static Set<String> readTxt(String file) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line;
            Set<String> set = new TreeSet<String>();
            while ((line = in.readLine()) != null) {
                set.add(line.trim());
            }
            return set;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void count() {
        Set<String> all = readTxt("/Users/axman/Downloads/all1.txt");
        Set<String> out = readTxt("/Users/axman/Downloads/1.txt");
        System.out.println(all.size() + "," + out.size());
        int x = 0, y = 0;

        for (final String o : out) {
            if (all.contains(o)) {
                x++;

                // File[] fs = new File("/Users/axman/Downloads/diff/").listFiles(new FileFilter() {
                //
                // public boolean accept(File arg0) {
                // return arg0.getName().startsWith(o);
                // }
                // });
                //
                // if (fs != null) for (File f : fs)
                // f.renameTo(new File("/Users/axman/Downloads/output/" + f.getName()));
            } else {
                y++;
            }
        }
        System.out.println(x + "," + y);
    }

    public static void print(Map<SURFInterestPoint, SURFInterestPoint> map) {
        List<SURFInterestPoint> l = new ArrayList<SURFInterestPoint>();
        l.addAll(map.keySet());

    }

}
