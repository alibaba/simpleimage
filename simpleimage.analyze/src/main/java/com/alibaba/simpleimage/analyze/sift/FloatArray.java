/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift;

/**
 * 类floatArray.java的实现描述：一个用于存储float的一维数组，可以快速地直接访问数组元素。 
 * @author axman 2013-6-27 上午9:21:12
 */
public abstract class FloatArray {
    public float[] data;                     //公开为public可以直接访问而不是在访问大量的数据元素时需要大量的getter方法调用
    public abstract FloatArray clone();
}

