/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.search.cluster.impl;

import java.util.List;

import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.util.ClusterUtils;

/**
 * 类MeansClusterBuilder.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-7-24 上午11:25:16
 */
public class MeansClusterBuilder extends AbstractClusterBuilder {

    public MeansClusterBuilder(){
        super();
    }

    protected Clusterable[] assignClusters(Clusterable[] clusters, final List<? extends Clusterable> values) {
        assignClustersByDistance(clusters, values);
        return clusters;
    }

    protected void assignClustersByDistance(Clusterable[] clusters, List<? extends Clusterable> values) {
        // long start = 0;
        // long end = 0;
        // long cost = 0;
        // start = System.currentTimeMillis();
        for (int j = 0; j < values.size(); j++) {
            // start = System.currentTimeMillis();
            Clusterable val = values.get(j);
            // end = System.currentTimeMillis();
            // start = System.currentTimeMillis();
            Clusterable nearestCluster = null;
            float minDistance = Float.MAX_VALUE;
            for (int i = 0; i < clusters.length; i++) {
                Clusterable cluster = clusters[i];
                float distance = ClusterUtils.getEuclideanDistance(val, cluster);

                if (distance < minDistance) {
                    nearestCluster = cluster;
                    minDistance = distance;
                }
            }
            // end = System.currentTimeMillis();
            ((Cluster) nearestCluster).addItem(val);
            // end = System.currentTimeMillis();
            // cost += (end - start);
            /*
             * if(j % 1000 == 0) { System.out.println(j + " --- " + cost); cost = 0; }
             */
        }
    }

    protected Clusterable[] getNewClusters(Clusterable[] clusters) {
        for (int i = 0; i < clusters.length; i++) {
            if (((Cluster) clusters[i]).getItems().size() > 0) {
                clusters[i] = new Cluster(((Cluster) clusters[i]).getClusterMean(), i);
            }
        }
        return clusters;
    }

    public static void main(String args[]) {
        // Random random = new Random(System.currentTimeMillis());
        // int numPoints = 400000;
        // List<Clusterable> points = new ArrayList<Clusterable>(numPoints);
        // for ( int i = 0; i < numPoints; i++ ){
        // int x = random.nextInt(1000) - 500;
        // int y = random.nextInt(1000) - 500;
        // points.add(new Point((float)x,(float)y));
        //
        // }
        // KClusterer clusterer = new KMeansClusterer();
        // Cluster[] clusters = clusterer.cluster(points,10);
        // System.out.println("--------- cluster info --------------");
        // for ( Cluster c : clusters ){
        // System.out.println(c.getItems().size());
        // }
    }

    public static boolean hasBadValue(float[] values) {
        for (float value : values) {
            if (!(value < 1 && value > -1)) {
                return true;
            }
        }
        return false;
    }

}
