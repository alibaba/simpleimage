/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.testbed;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.harissurf.HarrisSurf;
import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPoint;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointInfoReader;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointInfoWriter;
import com.alibaba.simpleimage.analyze.harris.io.InterestPointListInfo;
import com.alibaba.simpleimage.analyze.search.cluster.ClusterBuilder;
import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.cluster.impl.MeansClusterBuilder;
import com.alibaba.simpleimage.analyze.search.engine.ImageEngine;
import com.alibaba.simpleimage.analyze.search.engine.Score;
import com.alibaba.simpleimage.analyze.search.tree.KMeansTree;
import com.alibaba.simpleimage.analyze.search.tree.VocabTreeManager;
import com.alibaba.simpleimage.analyze.search.util.SerializationUtils;

/**
 * 类TreeSearchTest.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-7-24 下午2:33:27
 */
public class TreeSearchTest {

    public static void train(String location, String path, int imageCount, int useEvery) {
        List<Clusterable> points = new ArrayList<Clusterable>();
        int count = 0;
        BufferedImage image;
        for (int i = 0; i < imageCount; i++) {
            String tmp = String.format("ukbench%05d", i);
            String featfile = path + tmp + ".surf";
            InterestPointListInfo info = InterestPointInfoReader.readComplete(featfile);
            List<SURFInterestPoint> surfPoints = null;
            if (info != null) surfPoints = info.getList();
            if (surfPoints == null) {
                try {
                    String imgfile = path + tmp + ".jpg";
                    image = ImageIO.read(new File(imgfile));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                HarrisSurf hs = new HarrisSurf(image);
                hs.getDescriptions(hs.detectInterestPoints(), false);
                surfPoints = hs.getInterestPoints();
                info = new InterestPointListInfo();
                info.setHeight(image.getHeight());
                info.setWidth(image.getWidth());
                info.setList(surfPoints);
                info.setImageFile(tmp + ".jpg");
                InterestPointInfoWriter.writeComplete(featfile, info);
            }

            Iterator<SURFInterestPoint> itr = surfPoints.iterator();
            while (itr.hasNext()) {
                Clusterable next = itr.next();
                if (next != null) {
                    if (count % useEvery == 0) {
                        points.add(next);
                    }
                    count++;
                }
                if (count % 5000 == 0) System.out.println(count + " points loaded");
            }
            surfPoints.clear();
        }

        System.out.println("Begin to Build Tree");
        ClusterBuilder clusterer = new MeansClusterBuilder();
        KMeansTree tree = VocabTreeManager.makeTree(points, clusterer, 1);
        points.clear();
        // System.out.println("Total Points: " + count);
        // System.out.println("Total Words: " + tree.getLeafsList().size());
        SerializationUtils.saveObject(tree, new File(location));
    }

    public static void main(String args[]) {
        int imageCount = 1000;
        String path = "/Users/axman/Downloads/ukbench/simple/";
        String treePath = "/Users/axman/Downloads/ukbench/tree";
        String indexPath = "/Users/axman/Downloads/ukbench/index";
        String weightPath = "/Users/axman/Downloads/ukbench/weight";
        String histogramPath = "/Users/axman/Downloads/ukbench/histogram";

        if (!new File(treePath).exists()) {
            System.out.println("Begin to Build Tree");
            train(treePath, path, imageCount, 1);
        }

        ImageEngine ie = new ImageEngine();
        if (new File(indexPath).exists()) {
            System.out.println("Begin to Load Index,Tree,Weight,Histogram");
            ie.loadIndex(indexPath);
            ie.loadWeight(weightPath);
            ie.loadTree(treePath);
            ie.loadHistogram(histogramPath);
        } else {
            System.out.println("Begin to Build Index");
            ie.init(treePath);
            for (int i = 0; i < imageCount; i++) {
                String tmp = String.format("ukbench%05d", i);
                String featfile = path + tmp + ".surf";
                InterestPointListInfo info = InterestPointInfoReader.readComplete(featfile);
                List<SURFInterestPoint> surfPoints = null;
                if (info != null) surfPoints = info.getList();
                ie.buildIndex(surfPoints, i);
            }
            ie.buildWeight();
            ie.saveIndex(indexPath);
            ie.saveHistogram(histogramPath);
            ie.saveWeight(weightPath);
        }

        System.out.println("Begin to Search");
        long start = System.currentTimeMillis();
        String featfile = path + "ukbench00001" + ".surf";
        List<SURFInterestPoint> surfPoints = InterestPointInfoReader.readComplete(featfile).getList();

        List<Integer> visualWords = ie.quntinize(surfPoints);
        ie.getCandidate(visualWords);
        // visualWords = visualWords.subList(0, 20);
        List<Score> scoreList = ie.getRankedList(visualWords, ie.getCandidate(visualWords), 20);
        long end = System.currentTimeMillis();
        System.out.println("Time Cost: " + (end - start) + "ms");
        for (Score score : scoreList) {
            System.out.println(score.getIdentity() + "," + score.getScore());
        }

    }
}
