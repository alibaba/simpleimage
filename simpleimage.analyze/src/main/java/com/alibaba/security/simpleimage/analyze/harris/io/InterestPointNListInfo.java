/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.harris.io;

import java.util.List;

import com.alibaba.security.simpleimage.analyze.harissurf.SURFInterestPointN;

/**
 * 类InterestPointFileList.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-20 上午9:55:51
 */
public class InterestPointNListInfo {

    private String                  imageFile;
    private List<SURFInterestPointN> list;
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

    public List<SURFInterestPointN> getList() {
        return list;
    }

    public void setList(List<SURFInterestPointN> list) {
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
