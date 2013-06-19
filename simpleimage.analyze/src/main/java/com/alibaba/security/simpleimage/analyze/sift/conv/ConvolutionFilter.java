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
 * 类ConvoLutionFilter.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-21 下午2:18:53
 */
public class ConvolutionFilter {

    public enum Direction {
        VERTICAL, HORIZONTAL
    }

    public static ImageMap convolve(ImageMap img, ConvLinearMask mask) {
        ImageMap im1 = new ImageMap(img.xDim, img.yDim);
        ImageMap im2 = new ImageMap(img.xDim, img.yDim);;
        convolve1D(im1, mask, img, Direction.VERTICAL);
        convolve1D(im2, mask, im1, Direction.HORIZONTAL);
        return im2;
    }

    public static void convolve1D(ImageMap dest, ConvLinearMask mask, ImageMap src, Direction dir) {
        int maxN; // outer loop max index
        int maxP; // inner loop mac index

        if (dir == Direction.VERTICAL) {
            maxN = src.xDim;
            maxP = src.yDim;
        } else if (dir == Direction.HORIZONTAL) {
            maxN = src.yDim;
            maxP = src.xDim;
        } else {
            throw new java.lang.IllegalArgumentException("invalid direction");
        }

        for (int n = 0; n < maxN; n++) {
            for (int p = 0; p < maxP; p++) {
                double val = calculateConvolutionValue1D(src, mask, n, p, maxN, maxP, dir);
                if (dir == Direction.VERTICAL) dest.valArr[p][n] = val;
                else dest.valArr[n][p] = val;
            }
        }

    }

    private static double calculateConvolutionValue1D(ImageMap src, ConvLinearMask mask, int n, int p, int maxN,
                                                      int maxP, Direction dir) {
        double sum = 0.0;
        boolean isOut = false;
        double outBound = 0.0;
        for (int i = 0; i < mask.dim; i++) {
            int curAbsP = i - mask.middle + p;
            if (curAbsP < 0 || curAbsP >= maxP) {
                isOut = true;
                outBound += mask.mask[i];
                continue;
            }
            if (dir == Direction.VERTICAL) sum += (mask.mask[i] * src.valArr[curAbsP][n]);
            else sum += mask.mask[i] * src.valArr[n][curAbsP];
        }
        if (isOut) sum *= 1.0 / (1.0 - outBound);
        return sum;
    }
}
