/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.scala;

/**
 * 类PointLocalInformation.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-25 下午12:58:23
 */
public class PointLocalInformation {

    // Sub-pixel offset relative from this point. In the range of [-0.5 ; 0.5]
    double fineX, fineY;

    public double getFineX() {
        return (fineX);
    }

    public double getFineY() {
        return (fineY);
    }

    // Relative scale adjustment to the base image scale
    double scaleAdjust;

    public double getScaleAdjust() {
        {
            return (scaleAdjust);
        }
    }

    public void setScaleAdjust(double scaleAdjust) {
        this.scaleAdjust = scaleAdjust;

    }

    double dValue;
    public double getDValue() {

        return (dValue);
    }

    public void setDValue(double dValue) {
        this.dValue = dValue;
    }


    public PointLocalInformation(double fineS, double fineX, double fineY){
        this.fineX = fineX;
        this.fineY = fineY;
        this.scaleAdjust = fineS;
    }
}
