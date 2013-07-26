/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.search.cluster.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.util.ClusterUtils;

/**
 * 类Cluster.java的实现描述：TODO 类实现描述 
 * @author axman 2013-7-24 上午10:44:29
 */
public class Cluster implements Clusterable{
    private float[] mOriginalMeanLocation;
    private float[] mCurrentMeanLocation;
    private List<Clusterable> mClusterItems;
    
    private int id;
    
    public Cluster(float[] location, int id){
        mOriginalMeanLocation = location;
        mClusterItems = new ArrayList<Clusterable>();
        this.id = id;
    }
    
    /**
     * Get the current mean value of the cluster's items
     * @return
     */
    public float[] getClusterMean(){
        float[] normedCurrentLocation = new float[mCurrentMeanLocation.length];
        for ( int i = 0; i < mCurrentMeanLocation.length; i++ ){
            normedCurrentLocation[i] = mCurrentMeanLocation[i]/((float)mClusterItems.size());
        }
        return normedCurrentLocation;
    }
    
    public float getClusterMeanDist()
    {
        float[] clusterMean = getClusterMean();
        float sum = 0f;
        for (Clusterable clusterItem : mClusterItems)
        {
            float dist = ClusterUtils.getEuclideanDistance(clusterItem.getLocation(), clusterMean);
            sum += dist * dist;
        }
        return (float)Math.sqrt(sum);
    }
    
    public void removeItem(Clusterable item){
        mClusterItems.remove(item);
    }
    
    public void addItem(Clusterable item){
        if ( mCurrentMeanLocation == null ){
            mCurrentMeanLocation = item.getLocation().clone();
        } else {
            mCurrentMeanLocation = sumArrays(mCurrentMeanLocation, item.getLocation());
        }
        mClusterItems.add(item);
    }
    
    public List<Clusterable> getItems(){
        return mClusterItems;
    }
    
    /**
     * Get the original location of the cluster
     */
    public float[] getLocation() {
        return mOriginalMeanLocation;
    }
    
    public void setLocation(float[] location){
        mOriginalMeanLocation = location;
    }
    
    public static float[] getMeanValue(List<Clusterable> items){
        assert(items != null);
        assert(items.size() > 0);
        float[] newLocation = new float[items.get(0).getLocation().length];
        for ( Clusterable item : items ){
            newLocation = sumArrays(newLocation, item.getLocation());
        }
        for ( int i = 0; i < newLocation.length; i++ ){
            newLocation[i] = newLocation[i]/items.size();
        }
        return newLocation;
    }
    
    private static float[] sumArrays(float[] valsA, float[] valsB){
        for ( int i = 0; i < valsA.length; i++ ){
            valsA[i] += valsB[i];
        }
        return valsA;
    }
    

    
    public String toString(){
        return String.valueOf(id);
    }
    
    public int getId(){
        return id;
    }
    
    public static void main(String args[]){
        float center[] = {0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F};
        Cluster c = new Cluster(center,0);
        float center2[] = {1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F,1F};
        c.addItem(new Cluster(center2,0));
        System.out.print("avg = [");
        for ( float val : c.getClusterMean() ){
            System.out.print(val + ",");
        }
        System.out.println("]");
        float center3[] = {0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F,0F};
        c.addItem(new Cluster(center3,0));
        System.out.print("avg = [");
        for ( float val : c.getClusterMean() ){
            System.out.print(val + ",");
        }
        System.out.println("]");
        float center4[] = {-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F,-4F};
        c.addItem(new Cluster(center4,0));
        System.out.print("avg = [");
        for ( float val : c.getClusterMean() ){
            System.out.print(val + ",");
        }
        System.out.println("]");
    }
}
