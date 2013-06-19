/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.conv;

import com.alibaba.security.simpleimage.analyze.sift.IPixelConverter;

/**
 * 类CanonicalPixelConverter.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-21 下午1:02:57
 */
public class CanonicalPixelConverter implements IPixelConverter {

    /*
     * 将值设置在0-1之间
     */

    public double convert(int r, int g, int b) {
        return (r + g + b) / (255.0 * 3.0);
    }

}
