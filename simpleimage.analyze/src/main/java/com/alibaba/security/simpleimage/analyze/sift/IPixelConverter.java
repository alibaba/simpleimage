/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift;

/**
 * 类IPixelBinaryzation.java的实现描述：TODO 类实现描述 
 * @author axman 2013-3-21 上午9:58:18
 */
public interface IPixelConverter {
    double convert (int r, int g, int b);
}
