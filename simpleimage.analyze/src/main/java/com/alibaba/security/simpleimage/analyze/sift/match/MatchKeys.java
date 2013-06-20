/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.match;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.security.simpleimage.analyze.kdtree.KDTree;
import com.alibaba.security.simpleimage.analyze.sift.ModifiableConst;
import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类MatchKeys.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-4-3 上午11:20:03
 */
public class MatchKeys {

    private static class _mylist extends ArrayList<Match> {

        private static final long serialVersionUID = -1672787720681683109L;

        public void removeRange(int formIndex, int toIndex) {
            super.removeRange(formIndex, toIndex);
        }
    }

    public static List<Match> findMatchesBBF(List<KeyPointN> keys1, List<KeyPointN> keys2) {
        return findMatchesBBF(keys1, KDTree.createKDTree(keys2));
    }

    public static List<Match> findMatchesBBF(List<KeyPointN> keys1, KDTree kd) {
        List<Match> matches = new _mylist();
        for (KeyPointN kp : keys1) {
            ArrayList<KDTree.BestEntry> kpNNList = kd.nearestNeighbourListBBF(kp, 2, 40);
            if (kpNNList.size() < 2) throw (new IllegalArgumentException("BUG: less than two neighbours!"));
            KDTree.BestEntry be1 = (KDTree.BestEntry) kpNNList.get(0);
            KDTree.BestEntry be2 = (KDTree.BestEntry) kpNNList.get(1);
            if ((be1.getDist() / be2.getDist()) > ModifiableConst.getTowPntDistRatio()) continue;
            // XXX:最近邻点和次近邻点的距离比值
            KeyPointN kpN = (KeyPointN) be1.getNeighbour();
            if (Math.abs(kpN.getOrientation() - kp.getOrientation()) > ModifiableConst.getTowPntOrientationMinus()) continue;
            // if (Math.abs(kpN.getScale() - kp.getScale()) > ModifiableConst.getTowPntScaleMinus()) continue;
            matches.add(new Match(kp, kpN, be1.getDist(), be2.getDist()));
        }
        return (matches);
    }

    /*
     * public ArrayList FindMatches (ArrayList keys1, ArrayList keys2) { ArrayList matches = new ArrayList (); //KDTree
     * kd = KDTree.CreateKDTree (keys2); foreach (Keypoint kp in keys1) { double distNearest = Double.PositiveInfinity;
     * int nearest = -1; double dist2Nearest = Double.PositiveInfinity; int nearest2 = -1; for (int kn = 0 ; kn <
     * keys2.Count ; ++kn) { Keypoint kp2 = (Keypoint) keys2[kn]; double dist = Math.Sqrt (KDTree.DistanceSq (kp, kp2));
     * if (dist < distNearest) { nearest2 = nearest; dist2Nearest = distNearest; nearest = kn; distNearest = dist; } }
     * if (nearest == -1 || nearest2 == -1) continue; if ((distNearest / dist2Nearest) > 0.6) continue; matches.Add (new
     * Match (kp, (Keypoint) keys2[nearest])); Console.WriteLine ("({0},{1}) ({2},{3}) {4}", (int)(kp.X + 0.5),
     * (int)(kp.Y + 0.5), (int)(((Keypoint) keys2[nearest]).X + 0.5), (int)(((Keypoint) keys2[nearest]).Y + 0.5),
     * distNearest); } return (matches); }
     */

    public static ArrayList<Match> filterJoins(List<Match> matches) {
        Map<KeyPointN, Integer> map = new HashMap<KeyPointN, Integer>();

        // Count the references to each keypoint
        for (Match m : matches) {
            Integer kp1V = map.get(m.getKp1());
            int lI = (kp1V == null) ? 0 : (int) kp1V;
            map.put(m.getKp1(), lI + 1);
            Integer kp2V = map.get(m.getKp2());
            int rI = (kp2V == null) ? 0 : (int) kp2V;
            map.put(m.getKp2(), rI + 1);
        }
        ArrayList<Match> survivors = new ArrayList<Match>();
        for (Match m : matches) {
            Integer kp1V = map.get(m.getKp1());
            Integer kp2V = map.get(m.getKp2());
            if (kp1V <= 1 && kp2V <= 1) survivors.add(m);
        }
        return (survivors);
    }

    public static void filterNBest(ArrayList<Match> matches, int bestQ) {
        Collections.sort(matches, new Match.MatchWeighter());
        if (matches.size() > bestQ) {
            ((_mylist) matches).removeRange(bestQ, matches.size() - bestQ);
        }
    }

    public static List<Match> filterFarMatchL(List<Match> matches, double minX, double minY) {
        int arcStep = ModifiableConst.getSolpeArcStep();
        if (matches.size() <= 1) return matches;
        int[] ms = new int[90 / arcStep]; // 用数据的索引拂过每个度数的key，不使用map来保存，性能优化
        for (Match m : matches) {
            double r = Math.atan((m.kp2.getY() + minY - m.kp1.getY()) / (m.kp2.getX() + minX - m.kp1.getX())) * 360
                       / (2 * Math.PI);
            m.slopeArc = (int) r / arcStep * arcStep; // 第一次计算就把 match的斜率保存起来。
            if (m.slopeArc < 0) m.slopeArc += 90;
            ms[m.slopeArc / arcStep] = ms[m.slopeArc / arcStep] + 1;
        }
        int count = 0;
        int idx = 0;
        for (int i = 0; i < ms.length; i++) {// 找到斜率相同的最多的一个度数
            if (ms[i] > count) {
                count = ms[i];
                idx = i;
            }
        }
        idx = idx * arcStep;
        ArrayList<Match> survivors = new ArrayList<Match>();
        for (Match m : matches) {
            if (m.slopeArc == idx) survivors.add(m);
        }
        return survivors;
    }

    public static List<Match> filterFarMatchR(List<Match> matches, double minX, double minY) {
        int arcStep = ModifiableConst.getSolpeArcStep();
        if (matches.size() <= 1) return matches;
        int[] ms = new int[90 / arcStep]; // 用数据的索引拂过每个度数的key，不使用map来保存，性能优化
        for (Match m : matches) {
            double r = Math.atan((m.kp1.getY() - (m.kp2.getY() + minY)) / (m.kp1.getX() + minX - m.kp2.getX())) * 360
                       / (2 * Math.PI);
            m.slopeArc = ((int) r / arcStep * arcStep); // 第一次计算就把 match的斜率保存起来。
            if (m.slopeArc < 0) m.slopeArc += 90;
            ms[m.slopeArc / arcStep] = ms[m.slopeArc / arcStep] + 1;
        }
        int count = 0;
        int idx = 0;
        for (int i = 0; i < ms.length; i++) {// 找到斜率相同的最多的一个度数
            if (ms[i] > count) {
                count = ms[i];
                idx = i;
            }
        }
        idx = idx * arcStep;
        ArrayList<Match> survivors = new ArrayList<Match>();
        for (Match m : matches) {
            if (m.slopeArc == idx) survivors.add(m);
        }
        return survivors;

    }
}
