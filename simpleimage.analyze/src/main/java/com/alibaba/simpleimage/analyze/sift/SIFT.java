/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.simpleimage.analyze.sift.scale.FeaturePoint;
import com.alibaba.simpleimage.analyze.sift.scale.GaussianArray;
import com.alibaba.simpleimage.analyze.sift.scale.KDFeaturePoint;
import com.alibaba.simpleimage.analyze.sift.scale.OctaveSpace;
import com.alibaba.simpleimage.analyze.sift.scale.Pyramid;
import com.alibaba.simpleimage.analyze.sift.scale.ScalePeak;

/**
 * 类SIFT.java的实现描述：面向用户的主操作接口
 * 
 * @author axman 2013-6-28 上午10:15:45
 */
public class SIFT {

    // 以下常数均是论文中推荐的参数值
    float preprocSigma             = 1.5f;   // 用于处理被double后的图像预处理的模糊因子
    float octaveSigma              = 1.6f;   // 用于处理每个8度空间图像的模糊因子
    int    minimumRequiredPixelsize = 32;    // 高斯金字塔中缩小时最小的尺寸
    int    scaleSpaceLevels         = 3;     // 每个8度空间需要获取极值点的差分图层，加上用于比较的上下层至少要有5个差分图像，所以至少要有6个高斯模糊图象。
    float dogThresh                = 0.0075f; // 在差分图象上的极致点值最小值，防止大片的模糊后的点被选中，这个值越小选中的点越多。
    float dValueLowThresh          = 0.008f; // 和周围点比较的差值，这个差值是经过导数运算的差值，不是直接比较的。论文中建议为0.03（page
                                              // 11），但获取的点数太少，这里修改为0.008
    float maximumEdgeRatio         = 20.0f;  // 非角点的过虑比
    float scaleAdjustThresh        = 0.50f;  // 尺度空间的精确点和真实图象上的离散点在投谢时需要调整，这个是最大调整范围，超这个值就可能是下一个点。
    float peakRelThresh            = 0.8f;   //
    int    relocationMaximum        = 4;

    public int detectFeatures(ImagePixelArray img) {
        return (detectFeaturesDownscaled(img, -1, 1.0f));
    }

    /**
     * @param img
     * @param preProcessMark 图象预处理的标记，小于0，img需要double,大于0时，说明图象的长和宽要half到这个尺寸以下，等于0则不预处理
     * @param startScale
     * @return
     */
    public int detectFeaturesDownscaled(ImagePixelArray img, int preProcessMark, float startScale) {

        if (preProcessMark < 0) {
            img = img.doubled();
            startScale *= 0.5;
        } else if (preProcessMark > 0) {
            while (img.width > preProcessMark || img.height > preProcessMark) {
                img = img.halved();
                startScale *= 2.0;
            }
        }
        if (preprocSigma > 0.0) {
            GaussianArray gaussianPre = new GaussianArray(preprocSigma);
            img = gaussianPre.convolve(img);
        }

        Pyramid pyr = new Pyramid();
        pyr.buildOctaves(img, startScale, scaleSpaceLevels, octaveSigma, minimumRequiredPixelsize);
        
        globalFeaturePoints = new ArrayList<FeaturePoint>();
        // Generate featurePoints from each scalespace.
        for (int on = 0; on < pyr.octaves.size(); ++on) {
            OctaveSpace osp = pyr.octaves.get(on);

            ArrayList<ScalePeak> peaks = osp.findPeaks(dogThresh);// 寻找图片中的极值点
            ArrayList<ScalePeak> peaksFilted = osp.filterAndLocalizePeaks(peaks, maximumEdgeRatio, dValueLowThresh,
                                                                          scaleAdjustThresh, relocationMaximum);

            // 先将要处理的图层上所有象素的梯度大小和方向计算出来
            osp.pretreatMagnitudeAndDirectionImgs();
            ArrayList<FeaturePoint> faturePoints = osp.makeFeaturePoints(peaksFilted, peakRelThresh, scaleSpaceLevels,
                                                                         octaveSigma);
            osp.clear();
            globalFeaturePoints.addAll(faturePoints);
        }
        return (globalFeaturePoints.size());

    }
    
    private List<FeaturePoint> globalFeaturePoints;
    private List<KDFeaturePoint> globalKDFeaturePoints;
    
    public List<KDFeaturePoint> getGlobalKDFeaturePoints() {

        if (globalKDFeaturePoints != null) return (globalKDFeaturePoints);
        if (globalFeaturePoints == null) throw (new IllegalArgumentException("No featurePoints generated yet."));
        globalKDFeaturePoints = new ArrayList<KDFeaturePoint>();
        for (FeaturePoint fp : globalFeaturePoints) {
            globalKDFeaturePoints.add(new KDFeaturePoint(fp));
        }
        return globalKDFeaturePoints;
    }
}

