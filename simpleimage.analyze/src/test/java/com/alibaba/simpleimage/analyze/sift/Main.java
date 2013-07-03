/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.ModifiableConst;
import com.alibaba.simpleimage.analyze.sift.io.KDFeaturePointInfoReader;
import com.alibaba.simpleimage.analyze.sift.io.KDFeaturePointListInfo;
import com.alibaba.simpleimage.analyze.sift.match.Match;
import com.alibaba.simpleimage.analyze.sift.match.MatchKeys;
import com.alibaba.simpleimage.analyze.sift.scale.KDFeaturePoint;


public class Main {

    static {
        System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
        System.setProperty(ModifiableConst._SLOPEARCSTEP, "5");
        System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

    }

    public static void drawImage(BufferedImage logo, BufferedImage model, String file, List<Match> ms) throws Exception {
        int lw = logo.getWidth();
        int lh = logo.getHeight();

        int mw = model.getWidth();
        int mh = model.getHeight();

        BufferedImage outputImage = new BufferedImage(lw + mw, lh + mh, BufferedImage.TYPE_INT_RGB);

        Graphics2D g = outputImage.createGraphics();
        g.drawImage(logo, 0, 0, lw, lh, null);
        g.drawImage(model, lw, lh, lw + mw, lh + mh, null);
        g.setColor(Color.GREEN);
        for (Match m : ms) {
            KDFeaturePoint fromPoint = m.fp1;
            KDFeaturePoint toPoint = m.fp2;
            g.drawLine((int) fromPoint.x, (int) fromPoint.y, (int) toPoint.x + lw, (int) toPoint.y + lh);
        }
        g.dispose();
        FileOutputStream fos = new FileOutputStream(file);
        ImageIO.write(outputImage, "JPEG", fos);
        fos.close();
    }

    public static void main(String[] args) throws Exception {
        Map<String, KDFeaturePointListInfo> logoMap = new HashMap<String, KDFeaturePointListInfo>();
        File logoDir = new File("/Users/axman/Downloads/logo/sift");
        File[] logoFiles = logoDir.listFiles(new FileFilter() {

            public boolean accept(File arg0) {
                return arg0.getName().endsWith(".sift");
            }
        });
        for (File logoFile : logoFiles) {
            KDFeaturePointListInfo info = KDFeaturePointInfoReader.readComplete(logoFile.getAbsolutePath());
            logoMap.put(logoFile.getName(), info);
        }

        File modelDir = new File("/Users/axman/Downloads/model/sift");
        File[] modleFiles = modelDir.listFiles(new FileFilter() {

            public boolean accept(File arg0) {
                return arg0.getName().endsWith(".sift");
            }
        });

        for (File modelFile : modleFiles) {
            KDFeaturePointListInfo modelInfo = KDFeaturePointInfoReader.readComplete(modelFile.getAbsolutePath());

            List<KDFeaturePoint> alm = modelInfo.getList();
            if (alm.size() < 10) continue;
            for (KDFeaturePointListInfo logoInfo : logoMap.values()) {

                List<KDFeaturePoint> all = logoInfo.getList();
                if (all.size() < 10) continue;
                List<Match> ms = MatchKeys.findMatchesBBF(all, alm);
                ms = MatchKeys.filterMore(ms);
                ms = MatchKeys.filterFarMatchL(ms, logoInfo.getWidth(), logoInfo.getHeight());
                ms = MatchKeys.filterFarMatchR(ms, modelInfo.getWidth(), logoInfo.getHeight());
                if(ms.size() < 5) continue;
                String fileName = logoInfo.getImageFile() + "," + modelInfo.getImageFile() + "," + ms.size();
                System.out.println(fileName);
                String lf = "/Users/axman/Downloads/logo/img/" + logoInfo.getImageFile().replaceFirst(".sift", "");

                BufferedImage logo = ImageIO.read(new File(lf));
                String mf = "/Users/axman/Downloads/model/img/" + modelInfo.getImageFile().replaceFirst(".sift", "");
                BufferedImage model = ImageIO.read(new File(mf));
                drawImage(logo, model, "/Users/axman/Downloads/tmp/" + fileName+".jpg", ms);
            }

        }
    }
}
