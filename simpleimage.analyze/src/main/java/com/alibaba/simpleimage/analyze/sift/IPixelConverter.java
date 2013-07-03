/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift;

/**
 * 类IPixelConverter.java的实现描述：将三通道的RGB转换成一通道的灰度,并进行归一化处理
 * 
 * @author axman 2013-6-27 上午10:30:59
 */
public interface IPixelConverter {
    float convert(int r, int g, int b);
}

