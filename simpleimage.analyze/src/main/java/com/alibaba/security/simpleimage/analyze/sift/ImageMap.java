/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift;

/**
 * 类ImageMap.java的描述 * 
 * @author axman 2013-3-21 10:02:03
 */
public class ImageMap implements Cloneable {

    /**
     * 为了性能的原因不提 供getter和setter方法，直接访问成员变量
     */
    public int        xDim;
    public int        yDim;
    public double[][] valArr;

    public ImageMap(int xDim, int yDim){
        this.xDim = xDim;
        this.yDim = yDim;
        valArr = new double[yDim][xDim];
    }

    public Object clone() {
        ImageMap cl = new ImageMap(xDim, yDim);
        for (int y = 0; y < yDim; ++y) {
            for (int x = 0; x < xDim; ++x) {
                cl.valArr[y][x] = this.valArr[y][x];
            }
        }
        return cl;
    }

    public ImageMap scaleHalf() {
        if (xDim / 2 == 0 || yDim / 2 == 0) return null;

        ImageMap half = new ImageMap(xDim / 2, yDim / 2);
        for (int y = 0; y < half.yDim; y++) {
            for (int x = 0; x < half.xDim; x++) {
                half.valArr[y][x] = this.valArr[2 * y][2 * x]; // 图象缩小时每隔一定比例取原图的一个点
            }
        }
        return half;
    }

    public ImageMap scaleDouble() {
        // 图象放大时采用插值计算
        if (xDim <= 2 || yDim <= 2) return null;

        ImageMap db = new ImageMap(xDim * 2 - 2, yDim * 2 - 2);
        for (int y = 0; y < yDim - 1; y++) {
            for (int x = 0; x < xDim - 1; x++) {
                db.valArr[2 * y][2 * x] = this.valArr[y][x];// 原始点                
                db.valArr[2 * y + 1][2 * x] = (this.valArr[y][x] + this.valArr[y + 1][x]) / 2.0;//右上插值                
                db.valArr[2 * y][2 * x + 1] = (this.valArr[y][x] + this.valArr[y][x + 1]) / 2.0;//左下插值                
                db.valArr[2 * y + 1][2 * x + 1] = (this.valArr[y][x] + this.valArr[y + 1][x] + this.valArr[y][x + 1] + this.valArr[y + 1][x + 1]) / 4.0;//右下插值 
            }
        }
        return db;
    }

    public void Normalize() {
        double min = 0.0d;
        double max = 0.0d;
        for (int y = 0; y < this.yDim; y++) {
            for (int x = 0; x < this.xDim; x++) {
                if (min > this.valArr[y][x]) min = this.valArr[y][x];
                if (max < this.valArr[y][x]) min = this.valArr[y][x];
            }
        }
        if (min == max) return;
        double diff = max - min;
        for (int y = 0; y < this.yDim; y++) {
            for (int x = 0; x < this.xDim; x++) {
                this.valArr[y][x] = (this.valArr[y][x] - min) / diff;
            }
        }
    }

    public static ImageMap multiply(ImageMap im1, ImageMap im2) {
        if (im1.xDim != im2.xDim || im1.yDim != im2.yDim) {
            throw new IllegalArgumentException("Mismatching dimensions.");
        }
        ImageMap rsm = new ImageMap(im1.xDim, im2.yDim);
        for (int y = 0; y < im1.yDim; y++) {
            for (int x = 0; x < im1.xDim; x++) {
                rsm.valArr[y][x] = im1.valArr[y][x] * im2.valArr[y][x];
            }
        }
        return rsm;
    }

    public static ImageMap plus(ImageMap im1, ImageMap im2) {
        if (im1.xDim != im2.xDim || im1.yDim != im2.yDim) {
            throw new IllegalArgumentException("Mismatching dimensions.");
        }
        ImageMap rsm = new ImageMap(im1.xDim, im2.yDim);
        for (int y = 0; y < im1.yDim; y++) {
            for (int x = 0; x < im1.xDim; x++) {
                rsm.valArr[y][x] = im1.valArr[y][x] + im2.valArr[y][x];
            }
        }
        return rsm;
    }

    public static ImageMap minus(ImageMap im1, ImageMap im2) {
        if (im1.xDim != im2.xDim || im1.yDim != im2.yDim) {
            throw new IllegalArgumentException("Mismatching dimensions.");
        }
        ImageMap rsm = new ImageMap(im1.xDim, im2.yDim);
        for (int y = 0; y < im1.yDim; y++) {
            for (int x = 0; x < im1.xDim; x++) {
                rsm.valArr[y][x] = im1.valArr[y][x] - im2.valArr[y][x];
            }
        }
        return rsm;
    }

}
