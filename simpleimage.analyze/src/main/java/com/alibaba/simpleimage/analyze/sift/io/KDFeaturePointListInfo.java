/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.io;

import java.util.List;

import com.alibaba.simpleimage.analyze.sift.scale.KDFeaturePoint;

/**

 * @author axman 2013-5-20 9:45:17
 */
public class KDFeaturePointListInfo {
    private String imageFile;
    private List<KDFeaturePoint> list;
    private int width;
    private int height;
    
    public String getImageFile() {
        return imageFile;
    }
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
    
    public List<KDFeaturePoint> getList() {
        return list;
    }
    public void setList(List<KDFeaturePoint> list) {
        this.list = list;
    }
    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }    
}

