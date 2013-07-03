/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.alibaba.simpleimage.analyze.sift.IPixelConverter;
import com.alibaba.simpleimage.analyze.sift.ImagePixelArray;

/**
 * 类RenderImage.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-6-27 上午10:09:16
 */
public class RenderImage {

    private BufferedImage srcImage;

    public RenderImage(BufferedImage srcImage){
        this.srcImage = srcImage;
    }

    public int getWidth() {
        return this.srcImage.getWidth();
    }

    public int getHeight() {
        return this.srcImage.getHeight();
    }

    public float scaleWithin(int dim) {
        if (this.srcImage.getWidth() <= dim && this.srcImage.getHeight() <= dim) return 1.0f;
        float xScala = (float) dim / this.srcImage.getWidth();
        float yScala = (float) dim / this.srcImage.getHeight();

        float smallestScala = xScala <= yScala ? xScala : yScala; // 取最小的比例

        // 创建一个缩小后的位图
        BufferedImage bmScalaed = new BufferedImage((int) (this.srcImage.getWidth() * smallestScala + 0.5),
                                                    (int) (this.srcImage.getHeight() * smallestScala + 0.5),
                                                    BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bmScalaed.createGraphics();
        g.drawImage(this.srcImage, 0, 0, (int) (this.srcImage.getWidth() * smallestScala),
                    (int) (this.srcImage.getHeight() * smallestScala), null);
        // TODO,这里可以优化
        this.srcImage = bmScalaed;
        return smallestScala;
    }

    public ImagePixelArray toPixelFloatArray(IPixelConverter converter) {
        int h = this.srcImage.getHeight();
        int w = this.srcImage.getWidth();
        ImagePixelArray res = new ImagePixelArray(w, h);
        int[] pix = srcImage.getRGB(0, 0, w, h, null, 0, w);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int c = pix[x + y * w];
                int R = (c >> 16) & 0xFF;
                int G = (c >> 8) & 0xFF;
                int B = (c >> 0) & 0xFF;
                if (converter == null) res.data[x + y * w] = (R + G + B) / (255.0f * 3.0f); // 默认实现直接计算，减少h*w次方法调用
                else res.data[x + y * w] = converter.convert(R, G, B);
            }
        }
        return res;
    }

}

