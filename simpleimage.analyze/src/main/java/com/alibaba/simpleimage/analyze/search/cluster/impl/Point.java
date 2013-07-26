/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.search.cluster.impl;

import java.util.List;

import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;

/**
 * 类Point.java的实现描述：TODO 类实现描述 
 * @author axman 2013-7-24 上午11:28:26
 */
public class Point implements Clusterable {
    private float x;
    private float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float[] getLocation() {
        return new float[] { x, y };
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }


    public List<Clusterable> getItems() {

        return null;
    }
    public float[] getClusterMean() {
        return null;
    }
}

