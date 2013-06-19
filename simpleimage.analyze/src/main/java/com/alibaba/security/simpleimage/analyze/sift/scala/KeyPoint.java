/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.scala;

import com.alibaba.security.simpleimage.analyze.sift.ImageMap;

/**
 * 类Keypoint.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-25 上午11:21:03
 */
public class KeyPoint {

    public KeyPoint(){
    }

    ImageMap image;

    public ImageMap getImage() {

        return this.image;
    }

    double x, y;
    double imgScale;   // The scale of the image the keypoint was found in
    double scale;
    double orientation;

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

    public double getImgScale() {
        return imgScale;
    }

    public void setImgScale(double imgScale) {
        this.imgScale = imgScale;
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

    // The actual keypoint descriptor.
    boolean hasFV = false;

    public boolean isHasFV() {
        return hasFV;
    }

    public void setHasFV(boolean hasFV) {
        this.hasFV = hasFV;
    }

    double[] featureVector;

    public double[] getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(double[] featureVector) {
        this.featureVector = featureVector;
    }

    public double featureVectorGet(int xI, int yI, int oI) {
        return (featureVector[(xI * yDim * oDim) + (yI * oDim) + oI]);
    }

    public void featureVectorSet(int xI, int yI, int oI, double value) {
        featureVector[(xI * yDim * oDim) + (yI * oDim) + oI] = value;
    }

    public int getFVLinearDim() {
        return (featureVector.length);
    }

    public double featureVectorLinearGet(int idx) {
        return (featureVector[idx]);
    }

    public void featureVectorLinearSet(int idx, double value) {
        featureVector[idx] = value;
    }

    public void createLinearVector(int dim) {
        featureVector = new double[dim];
    }

    //private int xDim; 
    private int yDim;
    private int oDim;

    public void createVector(int xDim, int yDim, int oDim) {
        // FV feature Vector 特征描述器向量
        hasFV = true;
//        this.xDim = xDim;// 4 dim
//        this.yDim = yDim;// 4 dim
//        this.oDim = oDim;// 8 dim
        featureVector = new double[yDim * xDim * oDim];
    }

    // Keypoint constructor.
    //
    // image: The smoothed gaussian image the keypoint was located in.
    // x, y: The subpixel level coordinates of the keypoint.
    // imgScale: The scale of the gaussian image, with 1.0 being the original
    // detail scale (source image), and doubling at each octave.
    // kpScale: The scale of the keypoint.
    // orientation: Orientation degree in the range of [-PI ; PI] of the
    // keypoint.
    //
    // First add a keypoint, then use 'MakeDescriptor' to generate the local
    // image descriptor for this keypoint.
    public KeyPoint(ImageMap image, double x, double y, double imgScale, double kpScale, double orientation){
        this.image = image;
        this.x = x;
        this.y = y;
        this.imgScale = imgScale;
        this.scale = kpScale;
        this.orientation = orientation;
    }

    public int getDimensionCount() {
        return featureVector.length;
    }

    public double getDimensionElement(int dim) {
        return (featureVectorLinearGet(dim));
    }

}
