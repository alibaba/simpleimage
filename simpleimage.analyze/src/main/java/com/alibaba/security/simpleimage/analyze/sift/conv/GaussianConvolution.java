/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.conv;

import com.alibaba.security.simpleimage.analyze.sift.ImageMap;



/**
 * 类GaussianConvolution.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-21 下午1:59:52
 */
public class GaussianConvolution {

    private ConvLinearMask mask;

    public GaussianConvolution(double sigma){
        this (sigma, 1 + 2 * ((int) (3.0 * sigma)));
    }

    public GaussianConvolution(double sigma, int dim){
        // 卷积核必须是奇数，才能有一个明显的中心核：
        // * * *
        // *[*]*
        // * * *
        dim |= 1; // 保证奇数矩阵
        this.mask = new ConvLinearMask(dim); // 创建卷积核

        double sigma2sq = 2 * sigma * sigma;
        double normalizeFactor = 1.0 / (Math.sqrt(2.0 * Math.PI) * sigma);

        for (int i = 0; i < dim; i++) {
            int relPos = i - mask.middle;
            double G = (relPos * relPos) / sigma2sq;
            G = Math.exp(-G);
            G *= normalizeFactor;
            mask.mask[i] = G;
            mask.maskSum += G;
        }
    }

    /**
     * @param img
     * @return
     */
    public ImageMap convolve(ImageMap img) {
        return ConvolutionFilter.convolve (img, mask);
    }
}
