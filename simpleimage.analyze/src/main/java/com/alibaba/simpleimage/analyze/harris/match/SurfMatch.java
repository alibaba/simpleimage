/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.harris.match;

import java.util.Comparator;

import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPointN;

/**
 * 类SurfMatch.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-5-23 下午4:34:17
 */
public class SurfMatch {
    int slopeArc;
    SURFInterestPointN sp1;
    SURFInterestPointN sp2;

    public SURFInterestPointN getSp1() {
        return (sp1);
    }

    public SURFInterestPointN getSp2() {
        return (sp2);
    }

    double dist1;

    public double getDist1() {
        return (dist1);
    }

    double dist2;

    public double getDist2() {
        return (dist2);
    }

    public SurfMatch(SURFInterestPointN sp1, SURFInterestPointN sp2, double dist1, double dist2){
        this.sp1 = sp1;
        this.sp2 = sp2;
        this.dist1 = dist1;
        this.dist2 = dist2;
    }

    public static class MatchWeighter implements Comparator<SurfMatch> {

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

        public double OverallFitness(SurfMatch m) {
            double fitness = Math.pow(m.getDist1(), distExp) * Math.pow(1.0 / (m.getDist2() - m.getDist1()), quotExp);
            return (fitness);
        }

        public int compare(SurfMatch o1, SurfMatch o2) {

            double fit1 = OverallFitness(o1);
            double fit2 = OverallFitness(o2);
            if (fit1 < fit2) return (-1);
            else if (fit1 > fit2) return (1);
            return (0);
        }
    }
    
}
