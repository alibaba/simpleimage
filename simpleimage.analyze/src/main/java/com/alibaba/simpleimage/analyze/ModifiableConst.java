/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze;

/**
 * 
 * 
 * @author axman 2013-4-10 9:48:23
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

    public static float getTowPntDistRatio() {
        return PropertiesHolder.towPntDistRatio;
    }

    public static float getTowPntOrientationMinus() {
        return PropertiesHolder.towPntOrientationMinus;
    }

    public static float getTowPntScaleMinus() {
        return PropertiesHolder.towPntScaleMinus;
    }

    public static int getSolpeArcStep() {
        return PropertiesHolder.solpeArcStep;
    }

    /******************* setteies **********************************/
    public static void setTowPntDistRatio(float val) {
        PropertiesHolder.towPntDistRatio = val;
    }

    public static void setTowPntOrientationMinus(float val) {
        PropertiesHolder.towPntOrientationMinus = val;
    }

    public static void setTowPntScaleMinus(float val) {
        PropertiesHolder.towPntScaleMinus = val;
    }

    public static void setSolpeArcStep(int val) {
        PropertiesHolder.solpeArcStep = val;
    }

    private static class PropertiesHolder {

        // lazyload, 
        private static float towPntDistRatio        = 0.8f;
        private static float towPntOrientationMinus = 0.05f;
        private static float towPntScaleMinus       = 4.0f;
        private static int    solpeArcStep           = 5;
        private static int    minPointCount          = 10;
        static {
            String ratio = System.getProperty(_TOWPNTDISTRATIO);
            String orientation = System.getProperty(_TOWPNTORIENTATIONMINUS);
            String scala = System.getProperty(_TOWPNTSCALAMINUS);
            String arcStep = System.getProperty(_SLOPEARCSTEP);
            String minCount = System.getProperty(_MINPOINTCOUNT);
            towPntDistRatio = getFloatValue(ratio, towPntDistRatio);
            towPntOrientationMinus = getFloatValue(orientation, towPntOrientationMinus);
            towPntScaleMinus = getFloatValue(scala, towPntScaleMinus);
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

        private static float getFloatValue(String str, float defVal) {
            try {
                return Float.parseFloat(str);
            } catch (Exception e) {
                return defVal;
            }
        }

    }

}

