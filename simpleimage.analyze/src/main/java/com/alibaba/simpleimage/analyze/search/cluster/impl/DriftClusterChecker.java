/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.search.cluster.impl;

import com.alibaba.simpleimage.analyze.search.cluster.ClusterChecker;
import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.util.ClusterUtils;

/**
 * 类DriftClusterChecker.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-7-24 上午10:54:54
 */
public class DriftClusterChecker implements ClusterChecker {

    private float mDriftTolerance;

    public DriftClusterChecker(float driftTolerance){
        mDriftTolerance = driftTolerance;
    }

    public boolean recalculateClusters(Clusterable[] clusters) {
        for (Clusterable cluster : clusters) {
            if (cluster instanceof Cluster) {
                if (((Cluster) cluster).getItems().size() > 0) {
                    float distanceChange = ClusterUtils.getEuclideanDistance(((Cluster) cluster).getClusterMean(),
                                                                             cluster.getLocation());
                    if (distanceChange > mDriftTolerance) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
