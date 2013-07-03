/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift;

/**
 * 类PixelfloatArray.java的实现描述：用于存储图象象素的一维数组，按行依次存储
 * 
 * @author axman 2013-6-27 上午9:26:11
 */
public class ImagePixelArray extends FloatArray {

    /*
     * 基础数据结构在图形计算中会被大量访问，直接公开成员可以避免大量的方法调用。 对于基础数据结构可以不考虑其封装性，即如C实现的地址访问。
     */
    public int width;
    public int height;

    public ImagePixelArray(int w, int h){
        this(null, w, h);
    }

    public ImagePixelArray(float[] d, int w, int h){
        this.data = (d == null) ? new float[w * h] : d;
        this.width = w;
        this.height = h;
    }

    @Override
    public ImagePixelArray clone() {
        ImagePixelArray other = new ImagePixelArray(this.width, this.height);
        System.arraycopy(this.data, 0, other.data, 0, this.data.length);
        return other;
    }

    public ImagePixelArray doubled() {
        if (this.width <= 2 || this.height <= 2) return null;
        int nw = this.width * 2 - 2;
        int nh = this.height * 2 - 2;
        ImagePixelArray db = new ImagePixelArray(nw, nh);
        for (int y = 0; y < this.height - 1; y++) {
            for (int x = 0; x < this.width - 1; x++) {
                db.data[2 * (x + y * nw)] = this.data[y * this.width + x];// 原始点

                db.data[2 * (x + y * nw) + nw] = (this.data[x + y * this.width] + this.data[x + (y + 1) * this.width]) / 2.0f; // 下边插值

                db.data[2 * (x + y * nw) + 1] = (this.data[x + y * this.width] + this.data[x + y * this.width + 1]) / 2.0f; // 右边插值

                db.data[2 * (x + y * nw) + nw + 1] = (this.data[x + y * this.width]
                                                      + this.data[x + (y + 1) * this.width] //
                                                      + this.data[x + y * this.width + 1] //
                + this.data[x + (y + 1) * this.width + 1]) / 4.0f;// 右下插值
            }
        }
        return db;
    }

    public ImagePixelArray halved() {
        if (this.width / 2 == 0 || this.height / 2 == 0) return null;
        int nw = this.width / 2;
        int nh = this.height / 2;
        ImagePixelArray half = new ImagePixelArray(nw, nh);
        for (int y = 0; y < nh; y++) {
            for (int x = 0; x < nw; x++) {
                half.data[x + nw * y] = this.data[2 * (x + y * this.width)];
                // 图象缩小时每隔一定比例取原图的一个点
            }
        }
        return half;
    }

    /**
     * @param img2
     * @param img1
     * @return
     */
    public static ImagePixelArray minus(ImagePixelArray img1, ImagePixelArray img2) {
        if (img2.width != img1.width || img2.height != img1.height) {
            throw new IllegalArgumentException("Mismatching dimensions.");
        }

        ImagePixelArray min = new ImagePixelArray(img1.width, img1.height);
        // 可以不生成一个ImagePixelArray,使用var来保存差分后的数据，节省空间。
        for (int y = 0; y < min.height; y++) {
            for (int x = 0; x < min.width; x++) {
                min.data[x + y * min.width] = img1.data[x + y * img1.width] - img2.data[x + y * img1.width];
            }
        }
        return min;
    }
}

