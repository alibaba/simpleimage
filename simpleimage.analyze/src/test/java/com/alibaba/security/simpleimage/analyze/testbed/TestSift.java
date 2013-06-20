/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.testbed;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.alibaba.security.simpleimage.analyze.kdtree.KDTree;
import com.alibaba.security.simpleimage.analyze.sift.ModifiableConst;
import com.alibaba.security.simpleimage.analyze.sift.io.KeyPointInfoReader;
import com.alibaba.security.simpleimage.analyze.sift.io.KeyPointListInfo;
import com.alibaba.security.simpleimage.analyze.sift.match.Match;
import com.alibaba.security.simpleimage.analyze.sift.match.MatchKeys;
import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类TestSift.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-23 上午11:42:32
 */
class TestSift extends Thread {

    static {
        System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
        System.setProperty(ModifiableConst._SLOPEARCSTEP, "5");
        System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

    }

    private String[] args;
    private int      offset;

    public TestSift(String[] args, int offset){
        this.args = args;
        this.offset = offset;
    }

    public void run() {
        try {
            testSift(args, offset);
        } catch (Exception e) {
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
        for (int i = 0; i < 10; i++)
            try {
                testSift(args, i);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public static void testSift(String[] args, int offset) throws Exception {
        String targetPath = args[0];
        String srcPath = args[1];
        String diffPath = args[2];

        File[] modlefs = new File(targetPath + "sift/").listFiles();
        File[] logofs = new File(srcPath + "sift/").listFiles();
        List<KeyPointListInfo> srcPoints = new ArrayList<KeyPointListInfo>();

        for (File logof : logofs) {
            if (logof.getName().endsWith("sift")) {
                KeyPointListInfo kpl = KeyPointInfoReader.readComplete(logof.getAbsolutePath());
                srcPoints.add(kpl);
            }
        }

        int q = modlefs.length / 10;
        int end = (offset == 9) ? modlefs.length : (offset + 1) * q;
        System.out.println("work task from " + (offset * q) + ",end of " + end);

        for (int i = offset * q; i < end; i++) {
            File modelf = modlefs[i];
            if (!modelf.getName().endsWith("sift")) continue;

            KeyPointListInfo modelkpl = KeyPointInfoReader.readComplete(modelf.getAbsolutePath());
            KDTree kd = KDTree.createKDTree(modelkpl.getList());
            for (KeyPointListInfo logokpl : srcPoints) {
                List<Match> ms = MatchKeys.findMatchesBBF(logokpl.getList(), kd);
                ms = MatchKeys.filterJoins(ms);
                System.out.println("found max points:" + ms.size());
                ms = MatchKeys.filterFarMatchL(ms, logokpl.getWidth(), logokpl.getHeight());
                ms = MatchKeys.filterFarMatchR(ms, modelkpl.getWidth(), logokpl.getHeight());
                System.out.println("found points:" + ms.size());
                if (ms.size() < 5) continue;
                String srcImg = srcPath + "img/" + logokpl.getImageFile().replaceFirst(".sift", "");
                String destImg = targetPath + "img/" + logokpl.getImageFile().replaceFirst(".sift", "");
                drawDiff(srcImg, destImg, ms, diffPath + modelkpl.getImageFile() + "_" + logokpl.getImageFile());
            }
        }
        // System.out.println("match keys times:" + (System.currentTimeMillis() - start));
    }

    private static void drawDiff(String srcFile, String destFile, List<Match> ms, String diffPath) throws IOException {
        BufferedImage src = ImageIO.read(new File(srcFile));
        BufferedImage dest = ImageIO.read(new File(destFile));
        Color[] cs = { Color.RED, Color.GREEN, Color.BLUE };
        int w = Math.max(src.getWidth(), dest.getWidth());
        int h = src.getHeight() + dest.getHeight();
        int minH = src.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.drawImage(src, 0, 0, null);
        g.drawImage(dest, 0, src.getHeight(), null);
        int x = 0;
        for (Match m : ms) {
            x++;
            g.setColor(cs[x % 3]);
            KeyPointN kp1 = m.getKp1();
            KeyPointN kp2 = m.getKp2();
            g.drawLine((int) kp1.getX(), (int) kp1.getY(), (int) kp2.getX(), (int) kp2.getY() + minH);
        }
        g.dispose();
        FileOutputStream out = new FileOutputStream(diffPath + ".jpg");
        ImageIO.write(bi, "JPEG", out);
        out.flush();
        out.close();
    }
}
