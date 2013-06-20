/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.harissurf;

import java.io.Serializable;

import com.alibaba.security.simpleimage.analyze.kdtree.IKDTreeDomain;

/**
 * 类SURFInterestPointN.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-23 下午2:12:23
 */
public class SURFInterestPointN implements IKDTreeDomain, Cloneable, Serializable {

    private static final long serialVersionUID = -7331283230792139068L;

    private float             x, y;
    private float             dx, dy;
    private int               clusterIndex;
    private float             scale;
    private float             orientation;
    private int               laplacian;

    private int               dim;
    private int[]             descriptor;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public float getDy() {
        return dy;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }

    public int getClusterIndex() {
        return clusterIndex;
    }

    public void setClusterIndex(int clusterIndex) {
        this.clusterIndex = clusterIndex;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public int getLaplacian() {
        return laplacian;
    }

    public void setLaplacian(int laplacian) {
        this.laplacian = laplacian;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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

    public SURFInterestPointN(){
    }

    public SURFInterestPointN(SURFInterestPoint sp){
        this.x = sp.getX();
        this.y = sp.getY();
        this.dx = sp.getDx();
        this.dy = sp.getDy();
        this.clusterIndex = sp.getClusterIndex();
        this.scale = sp.getScale();
        this.orientation = sp.getOrientation();
        this.laplacian = sp.getLaplacian();
        float[] desc = sp.getDescriptor();
        this.dim = desc.length;
        this.descriptor = new int[this.dim];
        for (int i = 0; i < this.dim; i++) {
            this.descriptor[i] = (int) (desc[i] * 255.0);
            if(this.descriptor[i] > 255 || this.descriptor[i] < -255){
                throw (new IllegalArgumentException("Resulting integer descriptor k is not -255 <= k <= 255"));
            }
        }
        this.dim = this.descriptor.length;
    }

    public int getDimensionCount() {
        return dim;
    }

    public int getDimensionElement(int dim) {
        return descriptor[dim];
    }
}
