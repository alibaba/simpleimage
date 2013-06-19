/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.scala;

/**
 * 类ScalePoint.java的实现描述：TODO 类实现描述 
 * @author axman 2013-3-25 下午1:03:06
 */
public class ScalePoint {
    int x, y;
    int level;

    // Sub-pixel level information from the Localization step are put here
    PointLocalInformation local;
   

    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public PointLocalInformation getLocal() {
        return local;
    }
    public void setLocal(PointLocalInformation local) {
        this.local = local;
    }

  
    public ScalePoint (int x, int y, int level)
    {
        this.x = x;
        this.y = y;
        this.level = level;
    }
}
