/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.detect;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.security.simpleimage.analyze.sift.ImageMap;
import com.alibaba.security.simpleimage.analyze.sift.conv.GaussianConvolution;
import com.alibaba.security.simpleimage.analyze.sift.scala.DScaleSpace;
import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPoint;
import com.alibaba.security.simpleimage.analyze.sift.scala.KeyPointN;
import com.alibaba.security.simpleimage.analyze.sift.scala.OctavePyramid;
import com.alibaba.security.simpleimage.analyze.sift.scala.ScalePoint;

/**
 * 类LoweFeatureDetector.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-26 上午10:13:09
 */
public class LoweFeatureDetector {

    OctavePyramid pyr;

    public OctavePyramid getPyr() {
        return pyr;
    }

    // Detection parameters, suggested by Lowe's research paper.
    // Initial parameters
    double  octaveSigma              = 1.6;

    // Sigma for gaussian filter applied to double-scaled input image.
    // sigma 英文发音西格码，用于高斯滤波器
    double  preprocSigma             = 1.5;

    // Once one of the downscaled image's dimension falls below this,
    // downscaling is stopped.
    int     minimumRequiredPixelsize = 32;

    // How many DoG levels for each octave.
    int     scaleSpaceLevels         = 3;

    boolean printWarning             = true;

    public void setPrintWarning(boolean printWarning) {
        this.printWarning = printWarning;
    }

    boolean verbose = System.getProperty("_verbose") == null?false:true;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    // Minimum absolute DoG value of a pixel to be allowed as minimum/maximum
    // peak. This control how much general non-differing areas, such as the
    // sky is filtered. Higher value = less peaks, lower value = more peaks.
    // Good values from 0.005 to 0.01. Note this is related to
    // 'dValueLowThresh', which should be a bit larger, factor 1.0 to 1.5.
    double              dogThresh         = 0.0075;

    // D-value filter highcap value, higher = less keypoints, lower = more.
    // Lower: only keep keypoints with good localization properties, i.e.
    // those that are precisely and easily to localize (high contrast, see
    // Lowe, page 11. He recommends 0.03, but this seems way too high to
    // me.)
    double              dValueLowThresh   = 0.008;

    // Required cornerness ratio level, higher = more keypoints, lower = less.
    double              maximumEdgeRatio  = 20.0;

    // The exact sub-pixel localization is done on just one DoG plane. Even
    // when the scale adjustment exceeds +/- 0.5, the plane is not changed.
    // With this value you can discard peaks that are localized to be too far
    // from the plane. A high value will allow for peaks to be used that are
    // more far away from the plane used for localization, while a low value
    // will sort out more peaks, that drifted too far away.
    //
    // Be very careful with this value, as a too large value will lead to a
    // high number of keypoints in hard to localize areas such as in photos of
    // the sky.
    //
    // Good values seem to lie between 0.30 and 0.6.
    double              scaleAdjustThresh = 0.50;

    // Number of maximum steps a single keypoint can make in its space.
    int                 relocationMaximum = 4;

    // Results
    ArrayList<KeyPoint> globalKeypoints;

    public ArrayList<KeyPoint> getGlobalKeypoints() {
        return globalKeypoints;
    }

    // The Integer-normalized version of the globalKeypoints.
    ArrayList<KeyPointN> globalNaturalKeypoints = null;
    public List<KeyPointN> getGlobalNaturalKeypoints() {
 
            if (globalNaturalKeypoints != null)
                return (globalNaturalKeypoints);

            if (globalKeypoints == null)
                throw (new IllegalArgumentException ("No keypoints generated yet."));
            globalNaturalKeypoints = new ArrayList<KeyPointN>();
            for (KeyPoint kp : globalKeypoints){
                globalNaturalKeypoints.add (new KeyPointN(kp));
            }
            return (globalNaturalKeypoints);
    }


    public int detectFeatures(ImageMap img) {
        return (detectFeaturesDownscaled(img, -1, 1.0));
    }

    // Scale down the images down so that both dimensions are smaller than
    // 'bothDimHi'. If 'bothDimHi' is < 0, the image is doubled before
    // processing, if it is zero, nothing is done to the image.
    public int detectFeaturesDownscaled(ImageMap img, int bothDimHi, double startScale) {
        // globalkeypoints 是一个arraylist：数组列表
        globalKeypoints = null;
        globalNaturalKeypoints = null;
        if (bothDimHi < 0) {
            img = img.scaleDouble();// pass
            startScale *= 0.5;
        } else if (bothDimHi > 0) {
            while (img.xDim > bothDimHi || img.yDim > bothDimHi) {
                img = img.scaleHalf();// pass
                startScale *= 2.0;
            }
        }

        //  Maybe the blurring has to be before double-sizing?
        // better not, if we would lose more information then?

        // (Lowe03, p10, "We assume that the original image has a blur of at
        // least \sigma = 0.5 ...")
        // So, do one initial image smoothing pass.
        if (preprocSigma > 0.0) {
            GaussianConvolution gaussianPre = new GaussianConvolution(preprocSigma);
            img = gaussianPre.convolve(img);
        }

        pyr = new OctavePyramid();
        pyr.setVerbose(verbose);
        pyr.buildOctaves(img, startScale, scaleSpaceLevels, octaveSigma, minimumRequiredPixelsize);

        globalKeypoints = new ArrayList<KeyPoint>();

        // Generate keypoints from each scalespace.
        for (int on = 0; on < pyr.count(); ++on) {
            DScaleSpace dsp = pyr.octaves.get(on);

            ArrayList<ScalePoint> peaks = dsp.findPeaks(dogThresh);// 寻找图片中的极值点
            int oldCount = peaks.size();
            ArrayList<ScalePoint> peaksFilt = dsp.filterAndLocalizePeaks(peaks, maximumEdgeRatio, dValueLowThresh,
                                                             scaleAdjustThresh, relocationMaximum);
            
            if (verbose) {
                System.out.printf ("  filtered: %d remaining from %d, thats %5.2f %%\r\n",
                    peaksFilt.size(), oldCount, (100.0 * peaksFilt.size()) / oldCount);
                System.out.println ("generating keypoints from peaks");
            }

            // Generate the actual keypoint descriptors, using pre-computed
            // values for the gradient magnitude and direction.
            dsp.generateMagnitudeAndDirectionMaps();
            ArrayList<KeyPoint> keypoints = dsp.generateKeyPoints(peaksFilt, scaleSpaceLevels, octaveSigma);

            dsp.clearMagnitudeAndDirectionMaps();

            globalKeypoints.addAll(keypoints);
        }

        return (globalKeypoints.size());
    }
}
