/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.scale;

import com.alibaba.simpleimage.analyze.kdtree.IKDTreeDomain;

/**
 * 类KDFeaturePoint.java的实现描述：该类主要将FeaturePoint的feature修改为整数以便在KDTree上查找
 * 
 * @author axman 2013-7-1 下午2:44:35
 */
public class KDFeaturePoint extends IKDTreeDomain implements Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = 814942706491557514L;
    /**
     * 
     */
    public float              x, y;
    public float              scale;
    public float              orientation;

    public KDFeaturePoint(){
    }

    public KDFeaturePoint(FeaturePoint fp){
        if (!fp.hasFeatures) throw (new IllegalArgumentException(
                                                                 "While trying to generate integer "
                                                                         + "vector: source FeaturePoint has no feature vector yet"));
        x = fp.x;
        y = fp.y;
        scale = fp.scale;
        orientation = fp.orientation;
        dim = fp.features.length;
        descriptor = new int[dim];
        for (int d = 0; d < dim; ++d) {
            descriptor[d] = (int) (255.0 * fp.features[d]);
            if (descriptor[d] < 0 || descriptor[d] > 255) {
                throw (new IllegalArgumentException("Resulting integer descriptor k is not 0 <= k <= 255"));
            }
        }
    }

    public KDFeaturePoint clone() {
        KDFeaturePoint other = new KDFeaturePoint();
        other.dim = dim;
        other.x = x;
        other.y = y;
        other.scale = scale;
        other.orientation = orientation;
        other.descriptor = new int[dim];
        System.arraycopy(descriptor, 0, other.descriptor, 0, dim);
        return other;
    }
}

