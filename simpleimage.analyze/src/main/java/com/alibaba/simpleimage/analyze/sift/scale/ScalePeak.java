/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.scale;

/**
 * 类ScalePeak.java的实现描述：备选的极值点
 * 
 * @author axman 2013-6-28 上午9:34:56
 */
public class ScalePeak {

    public int       x;
    public int       y;
    public int       level;
    public LocalInfo local;

    public static class LocalInfo {

        public float fineX;
        public float fineY;
        public float scaleAdjust;
        public float dValue;

        public LocalInfo(float scaleAdjust,float fineX, float fineY){
            this.fineX = fineX;
            this.fineY = fineY;
            this.scaleAdjust = scaleAdjust;
        }
    }

    public ScalePeak(int x, int y, int level){
        this.x = x;
        this.y = y;
        this.level = level;
    }
}

