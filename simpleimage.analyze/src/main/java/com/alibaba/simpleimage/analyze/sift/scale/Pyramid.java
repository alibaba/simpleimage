/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.scale;

import java.util.ArrayList;

import com.alibaba.simpleimage.analyze.sift.ImagePixelArray;

/**
 * 类Pyramid.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-6-27 上午11:29:45
 */
public class Pyramid {

    public ArrayList<OctaveSpace> octaves; // 该塔中一共有几个8度空间

    public int buildOctaves(ImagePixelArray source, float scale, int levelsPerOctave, float octaveSigm, int minSize) {
        this.octaves = new ArrayList<OctaveSpace>();
        OctaveSpace downSpace = null;
        ImagePixelArray prev = source;

        while (prev != null && prev.width >= minSize && prev.height >= minSize) {
            OctaveSpace osp = new OctaveSpace();

            // Create both the gaussian filtered images and the DOG maps
            osp.makeGaussianImgs(prev, scale, levelsPerOctave, octaveSigm);
            osp.makeGaussianDiffImgs();
            octaves.add(osp);
            prev = osp.getLastGaussianImg().halved();
            if (downSpace != null) downSpace.up = osp;
            osp.down = downSpace;
            downSpace = osp;
            scale *= 2.0;
        }
        return (octaves.size());
    }
}

