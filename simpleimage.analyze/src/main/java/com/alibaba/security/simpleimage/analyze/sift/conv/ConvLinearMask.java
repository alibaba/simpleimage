/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.conv;

/**
 * 类ConvLineaeMark.java的实现描述：卷积等系数的一维数组
 * 
 * @author axman 2013-3-21 下午1:46:36
 */
public class ConvLinearMask {

    int      dim;    // 卷积核元素个数
    int      middle; // 卷积矩阵的中心象素
    double[] mask;   // 卷积矩阵
    double   maskSum; // 所有元素求和

    public int getDim() {
        return this.dim;
    }

    public int getMiddle() {
        return this.middle;
    }

    public void setMask(int idx, double val) {
        mask[idx] = val;
    }

    public double getMask(int idx) {
        return this.mask[idx];
    }

    public double getMaskSum() {
        return maskSum;
    }

    public void setMaskSum(double maskSum) {
        this.maskSum = maskSum;
    }

    public ConvLinearMask(int dim){
        mask = new double[dim];
        this.dim = dim;
        this.middle = dim / 2;
    }

}
