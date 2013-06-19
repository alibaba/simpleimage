/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift;

/**
 * 类ModifiableConst.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-4-10 上午9:48:23
 */
public class ModifiableConst {

    public static String _TOWPNTDISTRATIO        = "_TOWPNTDISTRATIO";
    public static String _TOWPNTORIENTATIONMINUS = "_TOWPNTORIENTATIONMINUS";
    public static String _TOWPNTSCALAMINUS       = "_TOWPNTSCALAMINUS";
    public static String _SLOPEARCSTEP           = "_SLOPEARCSTEP";
    public static String _MINPOINTCOUNT          = "_MINPOINTCOUNT";

    public static int getMinPointCount() {
        return PropertiesHolder.minPointCount;
    }

    public static double getTowPntDistRatio() {
        return PropertiesHolder.towPntDistRatio;
    }

    public static double getTowPntOrientationMinus() {
        return PropertiesHolder.towPntOrientationMinus;
    }

    public static double getTowPntScaleMinus() {
        return PropertiesHolder.towPntScaleMinus;
    }

    public static int getSolpeArcStep() {
        return PropertiesHolder.solpeArcStep;
    }

    /******************* setteies,可以动态修改这些参数配置 **********************************/
    public static void setTowPntDistRatio(double val) {
        PropertiesHolder.towPntDistRatio = val;
    }

    public static void setTowPntOrientationMinus(double val) {
        PropertiesHolder.towPntOrientationMinus = val;
    }

    public static void setTowPntScaleMinus(double val) {
        PropertiesHolder.towPntScaleMinus = val;
    }

    public static void setSolpeArcStep(int val) {
        PropertiesHolder.solpeArcStep = val;
    }

    private static class PropertiesHolder {

        // lazyload, 让应用程序运行之前有机会setProperties

        private static double towPntDistRatio        = 0.8d;
        private static double towPntOrientationMinus = 0.05d;
        private static double towPntScaleMinus       = 4.0d;
        private static int    solpeArcStep           = 5;
        private static int    minPointCount          = 10;
        static {
            String ratio = System.getProperty(_TOWPNTDISTRATIO);
            String orientation = System.getProperty(_TOWPNTORIENTATIONMINUS);
            String scala = System.getProperty(_TOWPNTSCALAMINUS);
            String arcStep = System.getProperty(_SLOPEARCSTEP);
            String minCount = System.getProperty(_MINPOINTCOUNT);
            towPntDistRatio = getDoubleValue(ratio, towPntDistRatio);
            towPntOrientationMinus = getDoubleValue(orientation, towPntOrientationMinus);
            towPntScaleMinus = getDoubleValue(scala, towPntScaleMinus);
            solpeArcStep = getIntValue(arcStep, solpeArcStep);
            minPointCount = getIntValue(minCount,minPointCount);
        }

        private static int getIntValue(String str, int defVal) {
            try {
                return Integer.parseInt(str);
            } catch (Exception e) {
                return defVal;
            }
        }

        private static double getDoubleValue(String str, double defVal) {
            try {
                return Double.parseDouble(str);
            } catch (Exception e) {
                return defVal;
            }
        }

    }

}
