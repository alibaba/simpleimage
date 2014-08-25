/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.ModifiableConst;
import com.alibaba.simpleimage.analyze.sift.io.KDFeaturePointInfoReader;
import com.alibaba.simpleimage.analyze.sift.io.KDFeaturePointListInfo;
import com.alibaba.simpleimage.analyze.sift.match.Match;
import com.alibaba.simpleimage.analyze.sift.match.MatchKeys;
import com.alibaba.simpleimage.analyze.sift.render.RenderImage;
import com.alibaba.simpleimage.analyze.sift.scale.KDFeaturePoint;

public class Main {

	static {
		System.setProperty(ModifiableConst._TOWPNTSCALAMINUS, "8.0");
		System.setProperty(ModifiableConst._SLOPEARCSTEP, "5");
		System.setProperty(ModifiableConst._TOWPNTORIENTATIONMINUS, "0.05");

	}

	public static void drawImage(BufferedImage logo, BufferedImage model,
			String file, List<Match> ms) throws Exception {
		int lw = logo.getWidth();
		int lh = logo.getHeight();

		int mw = model.getWidth();
		int mh = model.getHeight();

		BufferedImage outputImage = new BufferedImage(lw + mw, lh + mh,
				BufferedImage.TYPE_INT_RGB);

		Graphics2D g = outputImage.createGraphics();
		g.drawImage(logo, 0, 0, lw, lh, null);
		g.drawImage(model, lw, lh, mw, mh, null);
		g.setColor(Color.GREEN);
		for (Match m : ms) {
			KDFeaturePoint fromPoint = m.fp1;
			KDFeaturePoint toPoint = m.fp2;
			g.drawLine((int) fromPoint.x, (int) fromPoint.y, (int) toPoint.x
					+ lw, (int) toPoint.y + lh);
		}
		g.dispose();
		FileOutputStream fos = new FileOutputStream(file);
		ImageIO.write(outputImage, "JPEG", fos);
		fos.close();
	}

	public static void main(String[] args) throws Exception {

		BufferedImage img = ImageIO.read(new File(
				"/Users/axman/Downloads/min.png"));
		RenderImage ri = new RenderImage(img);
		SIFT sift = new SIFT();
		sift.detectFeatures(ri.toPixelFloatArray(null));
		List<KDFeaturePoint> al = sift.getGlobalKDFeaturePoints();

		BufferedImage img1 = ImageIO.read(new File(
				"/Users/axman/Downloads/big.png"));
		RenderImage ri1 = new RenderImage(img1);
		SIFT sift1 = new SIFT();
		sift1.detectFeatures(ri1.toPixelFloatArray(null));
		List<KDFeaturePoint> al1 = sift1.getGlobalKDFeaturePoints();

		List<Match> ms = MatchKeys.findMatchesBBF(al1, al);
		ms = MatchKeys.filterMore(ms);
		//ms = MatchKeys.filterFarMatchL(ms, img.getWidth(), img.getHeight());
		//ms = MatchKeys.filterFarMatchR(ms, img1.getWidth(), img.getHeight());
		drawImage(img1, img, "/Users/axman/Downloads/test1.jpg", ms);

	}
}
