/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.match;

import java.util.Comparator;

import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;

/**
 * 类Match.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-4-3 上午11:28:04
 */
public class Match {

    int slopeArc;
    KeyPointN kp1;
    KeyPointN kp2;

    public KeyPointN getKp1() {
        return (kp1);
    }

    public KeyPointN getKp2() {
        return (kp2);
    }

    double dist1;

    public double getDist1() {
        return (dist1);
    }

    double dist2;

    public double getDist2() {
        return (dist2);
    }

    // dist1: distance between kp1/kp2,
    // dist2: distance between kp1 and kp3, where kp3 is the next closest
    // match
    public Match(KeyPointN kp1, KeyPointN kp2, double dist1, double dist2){
        this.kp1 = kp1;
        this.kp2 = kp2;
        this.dist1 = dist1;
        this.dist2 = dist2;
    }

    public static class MatchFarPnt implements Comparator<Match> {

        private double avgX, avgY;

        public MatchFarPnt(double avgX, double avgY){
            this.avgX = avgX;
            this.avgY = avgY;
        }

        public int compare(Match o1, Match o2) {
            double x1 = o1.kp2.getX() - avgX;
            double y1 = o1.kp2.getY() - avgY;
            double dist1 = Math.sqrt(x1 * x1 + y1 * y1);
            double x2 = o2.kp2.getX() - avgX;
            double y2 = o2.kp2.getY() - avgY;
            double dist2 = Math.sqrt(x2 * x2 + y2 * y2);
            if (dist1 < dist2) return -1;
            if (dist1 > dist2) return 1;
            return 0;
        }
    }

    public static class MatchWeighter implements Comparator<Match> {

        private double distExp;
        private double quotExp;

        public MatchWeighter(){
            this(1.0d, 1.0d);
        }

        // The formula goes like this, with lowest weight being best matches:
        // w(kp) = kp.dist1^{distExp} *
        // {\frac{1}{kp.dist2 - kp.dist1}}^{quotExp}
        //
        // This means, as both dist1 and dist2 are in [0.0 ; 1.0], that a high
        // distExp exponent (and distExp > quotExp) will make the absolute
        // distance for the best match more important. A high value for
        // quotExp will make the difference between the best and second best
        // match more important (as in "how many other candidates are likely
        // matches?").
        public MatchWeighter(double distExp, double quotExp){
            this.distExp = distExp;
            this.quotExp = quotExp;
        }

        public double OverallFitness(Match m) {
            double fitness = Math.pow(m.getDist1(), distExp) * Math.pow(1.0 / (m.getDist2() - m.getDist1()), quotExp);
            return (fitness);
        }

        public int compare(Match o1, Match o2) {

            double fit1 = OverallFitness(o1);
            double fit2 = OverallFitness(o2);
            if (fit1 < fit2) return (-1);
            else if (fit1 > fit2) return (1);
            return (0);
        }

    }

}
