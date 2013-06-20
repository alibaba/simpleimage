/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.harris.io;

import java.util.List;

import com.alibaba.security.simpleimage.analyze.harissurf.SURFInterestPoint;

/**
 * 类InterestPointFileList.java的实现描述：每张图片的SURFInterestPoint信息，用于缓存logo信息。其中width和height在比较的时候需要用到，maxSize为
 * 该图片需要的最少匹配点数
 * @author axman 2013-5-20 上午9:55:51
 */
public class InterestPointListInfo {

    private String                  imageFile;
    private List<SURFInterestPoint> list;
    private int                     width;
    private int                     height;
    private int                     maxSize = 10;

    

    public int getMaxSize() {
        return maxSize;
    }


    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public List<SURFInterestPoint> getList() {
        return list;
    }

    public void setList(List<SURFInterestPoint> list) {
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
