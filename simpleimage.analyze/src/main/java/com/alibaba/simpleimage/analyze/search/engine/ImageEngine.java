/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.search.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.simpleimage.analyze.search.cluster.Clusterable;
import com.alibaba.simpleimage.analyze.search.tree.KMeansTree;
import com.alibaba.simpleimage.analyze.search.util.SerializationUtils;

/**
 * 类ImageEngine.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-7-24 下午1:38:57
 */
public class ImageEngine {

    private Map<Integer, LinkedList<Integer>> simpleInvertedFile;
    private Map<Integer, List<Integer>>       simpleHistogramFile;
    private Map<Integer, Float>               simpleWeightFile;
    private int                               total_images;
    private int                               total_features;

    private int                               total_words;
    private KMeansTree                        tree;

    /**
     * @param args
     */
    public static void main(String[] args) {

    }

    public void init(String treePath) {
        tree = (KMeansTree) SerializationUtils.loadObject(treePath);
        simpleHistogramFile = new HashMap<Integer, List<Integer>>();
        total_words = tree.getLeafsList().size();
        total_images = 0;
        total_features = 0;
        simpleInvertedFile = new HashMap<Integer, LinkedList<Integer>>(total_words);
    }

    /**
     * @return the total_features
     */
    public int getTotal_features() {
        return total_features;
    }

    public List<Score> getRankedList(List<Integer> queryVWList, List<Score> candidate, int topNum) {

        float queryNorm = 0f;
        float dictNorm = 0f;
        Float weight = 0.0f;
        List<Score> scoreList = new ArrayList<Score>();
        Integer count;
        Map<Integer, Integer> queryMap = new HashMap<Integer, Integer>();
        for (Integer visualWord : queryVWList) {
            count = queryMap.get(visualWord);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            queryMap.put(visualWord, count);
        }

        Iterator<Entry<Integer, Integer>> queryIter = queryMap.entrySet().iterator();
        while (queryIter.hasNext()) {
            Entry<Integer, Integer> entry = queryIter.next();
            weight = simpleWeightFile.get(entry.getKey());
            if (weight == null) {
                weight = 0.0f;
            }
            // queryNorm += entry.getValue() * weight * weight;
            queryNorm += (1 + Math.log(entry.getValue())) * weight * weight;
        }
        queryNorm = (float)Math.sqrt(queryNorm);

        /*
         * for(Integer visualWord : visualWords) { Double d = normQueryMap.get(visualWord); if(d == null) { d = 0.0; } d
         * += simpleWeightFile.get(visualWord) / norm; normQueryMap.put(visualWord, d); }
         */
        /*
         * Iterator<Entry<Integer, Double>> iter = normQueryMap.entrySet().iterator(); while(iter.hasNext()) {
         * Entry<Integer, Double> entry = iter.next(); System.out.println(entry.getKey() + ", " + entry.getValue()); }
         */

        // ranking using Normalized L2
        // Collections.sort(queryVWList);

        // filter
        if (candidate.size() > topNum) {
            candidate = candidate.subList(0, topNum);
        }

        Iterator<Score> iter = candidate.iterator();
        while (iter.hasNext()) {
            float sum = 0;
            Map<Integer, Integer> dictMap = new HashMap<Integer, Integer>();
            Integer docId = iter.next().getIdentity();
            List<Integer> dictVWList = simpleHistogramFile.get(docId);
            for (Integer visualWord : dictVWList) {
                count = dictMap.get(visualWord);
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                dictMap.put(visualWord, count);
            }

            Iterator<Entry<Integer, Integer>> dictIter = dictMap.entrySet().iterator();
            while (dictIter.hasNext()) {
                Entry<Integer, Integer> entry = dictIter.next();
                weight = simpleWeightFile.get(entry.getKey());
                if (weight == null) {
                    weight = 0.0f;
                }
                // dictNorm += entry.getValue() * weight * weight;
                dictNorm += (1 + Math.log(entry.getValue())) * weight * weight;
                // dictNorm += (1 + Math.log(entry.getValue())) * weight *
                // weight;
            }
            dictNorm = (float)Math.sqrt(dictNorm);

            dictIter = dictMap.entrySet().iterator();
            while (dictIter.hasNext()) {
                Entry<Integer, Integer> entry = dictIter.next();
                Integer dictWord = entry.getKey();
                Integer dictCount = entry.getValue();
                weight = simpleWeightFile.get(entry.getKey());
                if (weight == null) {
                    weight = 0.0f;
                }

                Integer queryCount = queryMap.get(dictWord);
                if (queryCount != null) {
                    // sum += Math.min(queryCount, dictCount) * weight * weight;
                    sum += (1 + Math.log(Math.min(queryCount, dictCount))) * weight * weight;
                }
            }
            sum /= (queryNorm * dictNorm);
            Score score = new Score(docId, sum);
            scoreList.add(score);
        }
        Collections.sort(scoreList);
        return scoreList;
    }

    public List<Integer> quntinize(List<? extends Clusterable> points) {
        return tree.addImage(points);
    }

    public List<Score> getCandidate(List<Integer> visualWords) {
        Map<Integer, Integer> voteMap = new HashMap<Integer, Integer>();
        List<Integer> docList;
        Iterator<Integer> iter = visualWords.iterator();
        Integer vwId;
        Integer docId;
        Integer vote;
        Iterator<Integer> iterDoc;

        while (iter.hasNext()) {
            vwId = iter.next();
            docList = simpleInvertedFile.get(vwId);
            if (docList == null) break;

            iterDoc = docList.iterator();
            while (iterDoc.hasNext()) {
                docId = iterDoc.next();
                vote = voteMap.get(docId);
                if (vote == null) {
                    vote = 1;
                } else {
                    vote++;
                }
                voteMap.put(docId, vote);
            }
        }

        // 按照value排序

        List<Score> scoreList = new ArrayList<Score>();
        Iterator<Entry<Integer, Integer>> iterVote = voteMap.entrySet().iterator();
        while (iterVote.hasNext()) {
            Entry<Integer, Integer> entry = iterVote.next();
            scoreList.add(new Score(entry.getKey(), entry.getValue()));
        }
        Collections.sort(scoreList);
        /*
         * ArrayList<Entry<Integer, Integer>> scoreList = new ArrayList<Entry<Integer, Integer>>( voteMap.entrySet());
         * Collections.sort(scoreList, new Comparator<Map.Entry<Integer, Integer>>() { public int
         * compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) { return (o2.getValue() -
         * o1.getValue()); } });
         */
        /*
         * for(Entry<Integer,Integer> e : scoreList) { System.out.println(e.getKey() + "::::" + e.getValue()); }
         */

        return scoreList;
    }

    public void buildIndex(List<? extends Clusterable> points, int Id) {

        List<Integer> visualWords;
        LinkedList<Integer> tmpInvertFile;

        visualWords = tree.addImage(points);
        Collections.sort(visualWords);
        simpleHistogramFile.put(Id, visualWords);

        Iterator<Integer> vwIter = visualWords.iterator();
        while (vwIter.hasNext()) {
            Integer vw = vwIter.next();
            tmpInvertFile = simpleInvertedFile.get(vw);
            if (tmpInvertFile == null) {
                tmpInvertFile = new LinkedList<Integer>();
            }
            if (!tmpInvertFile.contains(Id)) {
                tmpInvertFile.add(Id);
            }
            simpleInvertedFile.put(vw, tmpInvertFile);
        }
        total_images++;
        total_features += points.size();
        points.clear();
    }

    public void buildWeight() {
        if (simpleInvertedFile == null) return;

        simpleWeightFile = new HashMap<Integer, Float>();

        Iterator<Entry<Integer, LinkedList<Integer>>> iter = simpleInvertedFile.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, LinkedList<Integer>> entry = iter.next();
            Integer vwId = entry.getKey();
            Integer docNum = entry.getValue().size();
            Float weight = 0.0f;
            if (docNum > 0) {
                weight = (float)Math.log(total_images / docNum);
            }
            // System.out.println(weight);
            simpleWeightFile.put(vwId, weight);
        }
    }

    public void saveIndex(String indexPath) {
        SerializationUtils.saveObject(simpleInvertedFile, new File(indexPath));
    }

    @SuppressWarnings("unchecked")
    public boolean loadIndex(String indexPath) {
        simpleInvertedFile = (Map<Integer, LinkedList<Integer>>) SerializationUtils.loadObject(indexPath);
        return (simpleInvertedFile != null);
    }

    public void saveWeight(String weightPath) {
        SerializationUtils.saveObject(simpleWeightFile, new File(weightPath));
    }

    @SuppressWarnings("unchecked")
    public boolean loadWeight(String weightPath) {
        simpleWeightFile = (Map<Integer, Float>) SerializationUtils.loadObject(weightPath);
        return (simpleWeightFile != null);
    }

    public boolean loadTree(String treePath) {
        tree = (KMeansTree) SerializationUtils.loadObject(treePath);
        return (tree != null);
    }

    @SuppressWarnings("unchecked")
    public void loadHistogram(String histogramPath) {
        simpleHistogramFile = (Map<Integer, List<Integer>>) SerializationUtils.loadObject(histogramPath);
    }

    public void saveHistogram(String histogramPath) {
        SerializationUtils.saveObject(simpleHistogramFile, new File(histogramPath));
    }
}
