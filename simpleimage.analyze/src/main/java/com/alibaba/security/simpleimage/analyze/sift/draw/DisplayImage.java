/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.draw;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.alibaba.security.simpleimage.analyze.sift.IImaging;
import com.alibaba.security.simpleimage.analyze.sift.IPixelConverter;
import com.alibaba.security.simpleimage.analyze.sift.ImageMap;

/**
 * 类DisplayImage.java的实现描述：
 * 
 * @author axman 2013-3-21 下午3:29:34
 */
public class DisplayImage implements IImaging {
    private final static Logger logger = Logger.getLogger(DisplayImage.class);
    private BufferedImage bimg;

    public int getWidth() {
        return this.bimg.getWidth();
    }

    public int getHeight() {
        return this.bimg.getHeight();
    }

    public DisplayImage(String filePath) throws IOException{
        this.bimg = ImageIO.read(new File(filePath));
        if (this.bimg == null) throw new RuntimeException("can't read the map file");

    }

    public DisplayImage(BufferedImage bimg){
        this.bimg = bimg;
    }

    public double scaleWithin(int dim) {
        if (this.bimg.getWidth() <= dim && this.bimg.getHeight() <= dim) return 1.0;
        float xScala = (float) dim / this.bimg.getWidth();
        float yScala = (float) dim / this.bimg.getHeight();

        float smallestScala = xScala <= yScala ? xScala : yScala; // 取最小的比例

        // 创建一个缩小后的位图
        BufferedImage bmScalaed = new BufferedImage((int) (this.bimg.getWidth() * smallestScala + 0.5f),
                                                    (int) (this.bimg.getHeight() * smallestScala + 0.5f),
                                                    BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bmScalaed.createGraphics();
        g.drawImage(this.bimg, 0, 0, (int) (this.bimg.getWidth() * smallestScala),
                    (int) (this.bimg.getHeight() * smallestScala), null);
        // TODO,这里可以优化
        this.bimg = bmScalaed;
        return smallestScala;
    }

    // public DisplayImage carve(int x, int y, int width, int height) {
    // BufferedImage carved = null; 
    // return new DisplayImage(carved);
    // }

    public void save(String fileName) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(fileName);
            ImageIO.write(bimg, "JPEG", out);
        } catch (IOException e) {
            logger.equals(e.getMessage());
        } finally {
            if (out != null) try {
                out.close();
            } catch (IOException e) {
                logger.equals(e.getMessage());
            }
        }

    }

    public ImageMap toImageMap(IPixelConverter converter) {
        // if (converter == null) {
        // converter = new CanonicalPixelConverter();
        // }
        ImageMap res = new ImageMap(this.bimg.getWidth(), this.bimg.getHeight());
        int h = this.bimg.getHeight();
        int w = this.bimg.getWidth();
        //int[] pix = bimg.getRGB(0,0, w, h,null, 0, w*3); 无优化必要
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int color = bimg.getRGB(x, y);
                int R = (color >> 16) & 0xFF;
                int G = (color >> 8) & 0xFF;
                int B = (color >> 0) & 0xFF;
                if (converter == null) res.valArr[y][x] = (R + G + B) / (255.0 * 3.0); // 默认实现直接计算，减少h*w次方法调用
                else res.valArr[y][x] = converter.convert(R, G, B);
            }
        }
        return res;
    }
}
