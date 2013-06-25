/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.scala;

import java.util.ArrayList;

import com.alibaba.security.simpleimage.analyze.sift.ImageMap;

/**
 * 类OctavePyramid.java的实现描述：构建8度金字塔，提取不同尺度空间上稳定的极值点
 * 
 * @author axman 2013-3-25 上午11:08:38
 */
public class OctavePyramid {

	boolean verbose = System.getProperty("_verbose") == null ? false : true;
	public ArrayList<DScaleSpace> octaves;

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public int count() {
		if (octaves == null)
			return 0;
		return this.octaves.size();
	}

	public int buildOctaves(ImageMap source, double scale, int levelsPerOctave,
			double octaveSigm, int minSize) {
		this.octaves = new ArrayList<DScaleSpace>();
		DScaleSpace downSpace = null;
		ImageMap prev = source;

		while (prev != null && prev.xDim >= minSize && prev.yDim >= minSize) {
			DScaleSpace dsp = new DScaleSpace();
			dsp.verbose = verbose;

			if (verbose)
				System.out.printf("Building octave, (%d, %d)\r\n", prev.xDim,
						prev.yDim);

			// Create both the gaussian filtered images and the DoG maps
			dsp.buildGaussianMaps(prev, scale, levelsPerOctave, octaveSigm);
			dsp.buildDiffMaps();
			octaves.add(dsp);
			prev = dsp.getLastGaussianMap().scaleHalf();
			if (downSpace != null)
				downSpace.up = dsp;
			dsp.down = downSpace;
			downSpace = dsp;
			scale *= 2.0;
		}
		return (octaves.size());
	}

}
