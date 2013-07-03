/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.sift.ImagePixelArray;
import com.alibaba.simpleimage.analyze.sift.scale.OctaveSpace;


public class DumpImage {

    public static void dump(ImagePixelArray img, String outFile) {
        int w = img.width;
        int h = img.height;

        BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int c = (int) (img.data[j + i * w] * 255);

                Color cl = new Color(c, c, c);
                target.setRGB(j, i, cl.getRGB());

            }
        }

        try {
            ImageIO.write(target, "JPEG", new File(outFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dumpDiff(ImagePixelArray img, String outFile) {
        int w = img.width;
        int h = img.height;

        BufferedImage target = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int c = (int) (img.data[j + i * w] * 255);
                if (c < 0) c = -c;
                c = 255 - c;
                c *= 10;
                c %= 255;
                Color cl = new Color(c, c, c);
                target.setRGB(j, i, cl.getRGB());

            }
        }

        try {
            ImageIO.write(target, "JPEG", new File(outFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dump(ImagePixelArray[] mapArr, String outFile) {
        for (int i = 0; i < mapArr.length; i++)
            dump(mapArr[i], outFile + i + ".jpg");
    }

    public static void dumpDiff(ImagePixelArray[] mapArr, String outFile) {
        for (int i = 0; i < mapArr.length; i++)
            dumpDiff(mapArr[i], outFile + i + ".jpg");
    }

    public static void dumpValue(ImagePixelArray img, String outfile) {
        int w = img.width;
        int h = img.height;
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outfile));
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    if(img.data[j + i * w] != 1.0d)
                    out.println(img.data[j + i * w]);

                }
            }
            out.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
    public static void dumpValue(double[] v, String outfile) {

        try {
            PrintWriter out = new PrintWriter(new FileWriter(outfile));
            for (int i = 0; i < v.length; i++) {
                    out.println(v[i]);
            }
            out.close();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
    public static void dumpOctaves(List<OctaveSpace> list, String outfilePrefix) {
        for(OctaveSpace os:list){
            int x = 0;
            for(ImagePixelArray img:os.diffImags){
                dumpValue(img,outfilePrefix+os.baseScale+"."+ (x++)+".txt");
            }
        }
    }
    
}
