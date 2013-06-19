/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.io;

import java.util.List;

import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类KeyPointFileList.java的实现描述：TODO 类实现描述 
 * @author axman 2013-5-20 上午9:45:17
 */
public class KeyPointListInfo {
    private String imageFile;
    private List<KeyPointN> list;
    private int width;
    private int height;
    
    public String getImageFile() {
        return imageFile;
    }
    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }
    
    public List<KeyPointN> getList() {
        return list;
    }
    public void setList(List<KeyPointN> list) {
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
