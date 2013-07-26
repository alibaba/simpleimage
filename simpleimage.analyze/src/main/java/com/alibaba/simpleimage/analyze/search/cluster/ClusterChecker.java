/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.search.cluster;


/**
 * 类ClusterChecker.java的实现描述：TODO 类实现描述 
 * @author axman 2013-7-24 上午10:41:42
 */
public interface ClusterChecker {
    public boolean recalculateClusters(Clusterable[] clusters);
}
