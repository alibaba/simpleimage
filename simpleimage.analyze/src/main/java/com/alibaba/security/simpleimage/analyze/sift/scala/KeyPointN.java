/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.scala;

import java.io.Serializable;

import com.alibaba.security.simpleimage.analyze.sift.kdtree.IKDTreeDomain;

/**
 * 类KeyPointN.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-26 上午10:41:44
 */
public class KeyPointN implements IKDTreeDomain, Cloneable,Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6364618552158107021L;
    double x, y;
    double scale;
    double orientation;

    int    dim;
    int[]  descriptor;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public int[] getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(int[] descriptor) {
        this.descriptor = descriptor;
    }

    public Object clone() {
        KeyPointN kc = new KeyPointN();

        kc.x = x;
        kc.y = y;
        kc.scale = scale;
        kc.orientation = orientation;
        kc.dim = dim;
        kc.descriptor = (int[]) descriptor.clone();
        return (kc);
    }

    public KeyPointN(){
    }

    public KeyPointN(KeyPoint kp){
        if (kp.hasFV != true) throw (new IllegalArgumentException("While trying to generate integer "
                                                                  + "vector: source keypoint has no feature vector yet"));
        x = kp.x;
        y = kp.y;
        scale = kp.scale;
        orientation = kp.orientation;

        dim = kp.getFVLinearDim();
        descriptor = new int[dim];

        for (int d = 0; d < dim; ++d) {
            descriptor[d] = (int) (255.0 * kp.featureVectorLinearGet(d));
            if (descriptor[d] < 0 || descriptor[d] > 255) {
                throw (new IllegalArgumentException("Resulting integer descriptor k is not 0 <= k <= 255"));
            }
        }
    }

    // IKDTreeDomain interface implementation
    public int getDimensionCount() {
        return (dim);
    }

    public int getDimensionElement(int n) {
        return (descriptor[n]);
    }
}
