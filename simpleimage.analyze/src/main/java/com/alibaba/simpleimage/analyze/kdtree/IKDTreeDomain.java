/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.kdtree;

import java.io.Serializable;

/**
 * 类IKDTreeDomain.java的实现描述：在kdtree 上查找的元素必须存在这两个字段
 * 
 * @author axman 2013-7-1 下午2:45:10
 */
public abstract class IKDTreeDomain implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -6956627943184526276L;
    public int   dim;
    public int[] descriptor;
}

