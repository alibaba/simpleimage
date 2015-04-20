/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.simpleimage.analyze.sift.scale;

import java.util.ArrayList;

import com.alibaba.simpleimage.analyze.RefFloat;
import com.alibaba.simpleimage.analyze.sift.FloatArray;
import com.alibaba.simpleimage.analyze.sift.ImagePixelArray;
import com.alibaba.simpleimage.analyze.sift.scale.ScalePeak.LocalInfo;

/**
 * 类Octave.java的实现描述：表示8度金字塔中的一个8度空间，即以尺寸为坐标的某一尺寸上的那个8度空间
 * 
 * @author axman 2013-6-27 上午11:30:08
 */
public class OctaveSpace {

	OctaveSpace down; // down指的是下一个8度空间
	OctaveSpace up;
	ImagePixelArray baseImg; // 当前8度空间的原始图片，由上一个8度空间的某层（默认为倒数第三层）获取
	public float baseScale; // 原始图片在塔中的原始尺度
	public ImagePixelArray[] smoothedImgs; // 同一尺寸用不同模糊因子模糊后的高斯图像集
	public ImagePixelArray[] diffImags; // 由smoothedImgs得到的差分图集

	private ImagePixelArray[] magnitudes;
	private ImagePixelArray[] directions;

	/**
	 * @return 返回下一8度空间的原始基准图象
	 * @see page5 of
	 *      "Distinctive Image Features from Scale-Invariant featurePoints"
	 *      (David G.Lowe @January 5, 2004)
	 */
	// 高斯函数G对图像I的模糊函数 L(x,y,σ) = G(x,y,σ) * I(x,y)
	// 高斯差分函数:D(x,y,σ) = (G(x,y,kσ)−G(x,y,σ)) * I(x,y) = L(x,y,kσ)
	// L(x,y,σ) 对于scales幅图象产生连续尺度，推导 k = 2 ^ (1/s)，论文中默认
	// scales为3所以一共6幅图像，它们的尺度应该为
	// 1σ,1.26σ,1.59σ,2.0σ,2.52σ,3.17σ
	// 倒数第三幅正好发生一个二倍的阶跃，把它作为下一个8度空间的第一幅图片，保证差分金字塔的尺度空间的连续性，其实对于任义scales,length-2为固定的位置，
	// 因为smoothedImgs长度为s+3,前面去掉1个原始图片，只有length-2的时 k = 2 ^ (s/s)才正好是一个2倍的阶跃
	public ImagePixelArray getLastGaussianImg() {
		if (this.smoothedImgs.length < 2) {
			throw new java.lang.IllegalArgumentException(
					"err: too few gaussian maps.");
		}
		return (this.smoothedImgs[this.smoothedImgs.length - 2]);
	}

	/**
	 * 在一个8空间用不同的模糊因子构造更多层的高期模糊图像集,这里是不同模糊因子的模糊但是尺寸是相同的
	 * 
	 * @param first
	 * @param firstScale
	 * @param scales
	 * @param sigma
	 */
	public void makeGaussianImgs(ImagePixelArray base, float baseScale,
			int scales, float sigma) {

		// 对于DOG(差分图像集)我们需要一张以上的图片才能生成差分图，但是查找极值点更多要差分图。见buildDiffMaps
		smoothedImgs = new ImagePixelArray[scales + 3];
		// 每一个极值点是在三维空间中比较获得，即要和它周围8个点和上一幅对应的9个点以及下一幅对应的9个点，因此为了获得scales层点，
		// 那么在差分高斯金字塔中需要有scales+2幅图像,而如果差分图幅数是scales+2，那么8度空间中至少需要scales+2＋1幅高斯模糊图像。
		this.baseScale = baseScale;
		ImagePixelArray prev = base;
		smoothedImgs[0] = base;

		float w = sigma;
		float kTerm = (float) Math.sqrt(Math.pow(Math.pow(2.0, 1.0 / scales),
				2.0) - 1.0);
		for (int i = 1; i < smoothedImgs.length; i++) {
			GaussianArray gauss = new GaussianArray(w * kTerm);
			prev = smoothedImgs[i] = gauss.convolve(prev);
			w *= Math.pow(2.0, 1.0 / scales);
		}
	}

	public void makeGaussianDiffImgs() {
		// Generate DoG maps. The maps are organized like this:
		// 0: D(sigma)
		// 1: D(k * sigma)
		// 2: D(k^2 * sigma)
		// ...
		// s: D(k^s * sigma) = D(2 * sigma)
		// s+1: D(k * 2 * sigma)
		//

		diffImags = new ImagePixelArray[smoothedImgs.length - 1];
		for (int sn = 0; sn < diffImags.length; sn++) {
			diffImags[sn] = ImagePixelArray.minus(smoothedImgs[sn + 1],
					smoothedImgs[sn]);
		}
	}

	public ArrayList<ScalePeak> findPeaks(float dogThresh) {

		ArrayList<ScalePeak> peaks = new ArrayList<ScalePeak>();

		ImagePixelArray current, above, below;

		// Search the D(k * sigma) to D(2 * sigma) spaces
		for (int level = 1; level < (this.diffImags.length - 1); level++) {
			current = this.diffImags[level];
			below = this.diffImags[level - 1];
			above = this.diffImags[level + 1];
			peaks.addAll(findPeaks4ThreeLayer(below, current, above, level,
					dogThresh));
			below = current;
		}

		return (peaks);
	}

	/**
	 * 精确化特征点位置并生成本地化信息以及过虑躁点
	 * 
	 * @param peaks
	 * @param maximumEdgeRatio
	 * @param dValueLowThresh
	 * @param scaleAdjustThresh
	 * @param relocationMaximum
	 * @return
	 */

	public ArrayList<ScalePeak> filterAndLocalizePeaks(
			ArrayList<ScalePeak> peaks, float maximumEdgeRatio,
			float dValueLowThresh, float scaleAdjustThresh,
			int relocationMaximum) {
		ArrayList<ScalePeak> filtered = new ArrayList<ScalePeak>();
		int[][] processedMap = new int[this.diffImags[0].width][this.diffImags[0].height];
		for (ScalePeak peak : peaks) {

			// 去除边缘点 @see isTooEdgelike
			if (isTooEdgelike(diffImags[peak.level], peak.x, peak.y,
					maximumEdgeRatio))
				continue;
			// 精确化特征点位置 @see localizeIsWeak
			if (localizeIsWeak(peak, relocationMaximum, processedMap))
				continue;

			if (Math.abs(peak.local.scaleAdjust) > scaleAdjustThresh)
				continue;

			if (Math.abs(peak.local.dValue) <= dValueLowThresh)
				continue;

			filtered.add(peak);
		}
		return filtered;
	}

	/**
	 * 先将差分图上每个点的梯度方向和梯度幅值计算出来，预计算的总体性能比统计在范围内的点再计算的总体性能要高，因为特征点分布较大，
	 * 它周围的点可能被其它中心点多次使用到， 如果统计在范围内再计算的的话每个点可能被多次计算。
	 */
	public void pretreatMagnitudeAndDirectionImgs() {

		magnitudes = new ImagePixelArray[this.smoothedImgs.length - 1];// 梯度的数组
		directions = new ImagePixelArray[this.smoothedImgs.length - 1];// 方向的数组
		for (int s = 1; s < (this.smoothedImgs.length - 1); s++) {
			magnitudes[s] = new ImagePixelArray(this.smoothedImgs[s].width,
					this.smoothedImgs[s].height);
			directions[s] = new ImagePixelArray(this.smoothedImgs[s].width,
					this.smoothedImgs[s].height);
			int w = smoothedImgs[s].width;
			int h = smoothedImgs[s].height;
			for (int y = 1; y < (h - 1); ++y) {
				for (int x = 1; x < (w - 1); ++x) {
					magnitudes[s].data[y * w + x] = (float) Math
							.sqrt(Math
									.pow(smoothedImgs[s].data[y * w + x + 1]
											- smoothedImgs[s].data[y * w + x
													- 1], 2.0f)
									+ Math.pow(smoothedImgs[s].data[(y + 1) * w
											+ x]
											- smoothedImgs[s].data[(y - 1) * w
													+ x], 2.0f));

					directions[s].data[y * w + x] = (float) Math.atan2(
							smoothedImgs[s].data[(y + 1) * w + x]
									- smoothedImgs[s].data[(y - 1) * w + x],
							smoothedImgs[s].data[y * w + x + 1]
									- smoothedImgs[s].data[y * w + x - 1]);
				}
			}
		}
	}

	public ArrayList<FeaturePoint> makeFeaturePoints(
			ArrayList<ScalePeak> localizedPeaks, float peakRelThresh,
			int scaleCount, float octaveSigma) {
		ArrayList<FeaturePoint> featurePoints = new ArrayList<FeaturePoint>();
		for (ScalePeak sp : localizedPeaks) {
			ArrayList<FeaturePoint> thisPointKeys = makeFeaturePoint(
					this.baseScale, sp, peakRelThresh, scaleCount, octaveSigma);
			thisPointKeys = createDescriptors(thisPointKeys,
					magnitudes[sp.level], directions[sp.level], 2.0f, 4, 8,
					0.2f);
			for (FeaturePoint fp : thisPointKeys) {
				if (!fp.hasFeatures) {
					throw new java.lang.IllegalStateException(
							"should not happen");
				}

				fp.x *= fp.imgScale;
				fp.y *= fp.imgScale;
				fp.scale *= fp.imgScale;
				featurePoints.add(fp);
			}
		}
		return featurePoints;
	}

	public void clear() {
		for (int i = 0; i < this.magnitudes.length; i++)
			this.magnitudes[i] = null;
		for (int i = 0; i < this.directions.length; i++)
			this.directions[i] = null;
		magnitudes = directions = null;
	}

	private ArrayList<FeaturePoint> makeFeaturePoint(float imgScale,
			ScalePeak point, float peakRelThresh, int scaleCount,
			float octaveSigma) {

		// 计算特征点的相对scale,这里是预估值
		float fpScale = (float) (octaveSigma * Math.pow(2.0,
				(point.level + point.local.scaleAdjust) / scaleCount));

		// Lowe03, "A gaussian-weighted circular window with a \sigma three
		// times that of the scale of the featurePoints".

		float sigma = 3.0f * fpScale;
		int radius = (int) (3.0 * sigma / 2.0 + 0.5);
		int radiusSq = radius * radius;

		ImagePixelArray magnitude = magnitudes[point.level];
		ImagePixelArray direction = directions[point.level];
		// 确定邻点范围
		int xMin = Math.max(point.x - radius, 1);
		int xMax = Math.min(point.x + radius, magnitude.width - 1);
		int yMin = Math.max(point.y - radius, 1);
		int yMax = Math.min(point.y + radius, magnitude.height - 1);

		// G(r) = e^{-\frac{r^2}{2 \sigma^2}}
		float gaussianSigmaFactor = 2.0f * sigma * sigma;

		float[] boxes = new float[36]; // 构造该点邻域梯度方向直方图，将一个圆周360°划分10个槽，从0°开始每槽递增10°，所以一共有36个槽

		for (int y = yMin; y < yMax; ++y) {
			for (int x = xMin; x < xMax; ++x) {
				int relX = x - point.x;// 求半径
				int relY = y - point.y;// 求半径
				if (relX * relX + relY * relY > radiusSq)
					continue; // 勾股定理

				float gaussianWeight = (float) Math.exp(-((relX * relX + relY
						* relY) / gaussianSigmaFactor));

				// find the closest bin and add the direction
				int boxIdx = findClosestRotationBox(direction.data[y
						* direction.width + x]);

				boxes[boxIdx] += magnitude.data[y * magnitude.width + x]
						* gaussianWeight;
			}
		}

		averageBoxes(boxes);

		float maxGrad = 0.0f;
		int maxBox = 0;
		for (int b = 0; b < 36; ++b) {
			if (boxes[b] > maxGrad) {
				maxGrad = boxes[b];
				maxBox = b;
			}
		}

		RefPeakValueAndDegreeCorrection ref1 = new RefPeakValueAndDegreeCorrection();
		interpolateOrientation(boxes[maxBox == 0 ? (36 - 1) : (maxBox - 1)],
				boxes[maxBox], boxes[(maxBox + 1) % 36], ref1);

		// 这样找到的不止是两个最大的方向 @see page 13
		boolean[] boxIsFeaturePoint = new boolean[36];
		for (int b = 0; b < 36; ++b) {
			boxIsFeaturePoint[b] = false;
			if (b == maxBox) {
				boxIsFeaturePoint[b] = true;
				continue;
			}
			if (boxes[b] < (peakRelThresh * ref1.peakValue))
				continue;
			int leftI = (b == 0) ? (36 - 1) : (b - 1);
			int rightI = (b + 1) % 36;
			if (boxes[b] <= boxes[leftI] || boxes[b] <= boxes[rightI])
				continue; // no local peak
			boxIsFeaturePoint[b] = true;
		}

		ArrayList<FeaturePoint> featurePoints = new ArrayList<FeaturePoint>();

		float oneBoxRad = (float) (2.0f * Math.PI) / 36;

		for (int b = 0; b < 36; ++b) {
			if (boxIsFeaturePoint[b] == false)
				continue;

			int bLeft = (b == 0) ? (36 - 1) : (b - 1);
			int bRight = (b + 1) % 36;

			RefPeakValueAndDegreeCorrection ref2 = new RefPeakValueAndDegreeCorrection();

			if (interpolateOrientation(boxes[bLeft], boxes[b], boxes[bRight],
					ref2) == false) {
				throw (new java.lang.IllegalStateException(
						"BUG: Parabola fitting broken"));
			}
			float degree = (float) ((b + ref2.degreeCorrection) * oneBoxRad - Math.PI);
			// 完全化在 -180 到 +180 之间

			if (degree < -Math.PI)
				degree += 2.0 * Math.PI;
			else if (degree > Math.PI)
				degree -= 2.0 * Math.PI;

			FeaturePoint fp = new FeaturePoint(this.smoothedImgs[point.level],
					point.x + point.local.fineX, point.y + point.local.fineY,
					imgScale, fpScale, degree);
			featurePoints.add(fp);
		}
		return (featurePoints);
	}

	private boolean interpolateOrientation(float left, float middle,
			float right, RefPeakValueAndDegreeCorrection ref) {
		float a = ((left + right) - 2.0f * middle) / 2.0f;
		ref.degreeCorrection = ref.peakValue = Float.NaN;
		if (a == 0.0)
			return false;
		float c = (((left - middle) / a) - 1.0f) / 2.0f;
		float b = middle - c * c * a;

		if (c < -0.5 || c > 0.5)
			throw (new IllegalStateException(
					"InterpolateOrientation: off peak ]-0.5 ; 0.5["));
		ref.degreeCorrection = c;
		ref.peakValue = b;
		return true;
	}

	private void averageBoxes(float[] boxes) {
		// ( 0.4, 0.4, 0.3, 0.4, 0.4 ))
		// 每三个做1个平均直至完成
		for (int sn = 0; sn < 4; ++sn) {
			float first = boxes[0];
			float last = boxes[boxes.length - 1];

			for (int sw = 0; sw < boxes.length; ++sw) {
				float cur = boxes[sw];
				float next = (sw == (boxes.length - 1)) ? first
						: boxes[(sw + 1) % boxes.length];

				boxes[sw] = (last + cur + next) / 3.0f;
				last = cur;
			}
		}
	}

	private int findClosestRotationBox(float angle) {
		angle += Math.PI;
		angle /= 2.0f * Math.PI;
		angle *= 36;
		int idx = (int) angle;
		if (idx == 36)
			idx = 0;
		return idx;
	}

	private ArrayList<FeaturePoint> createDescriptors(
			ArrayList<FeaturePoint> featurePoints, ImagePixelArray magnitude,
			ImagePixelArray direction, float considerScaleFactor, int descDim,
			int directionCount, float fvGradHicap) {

		if (featurePoints.size() <= 0)
			return (featurePoints);
		// 通过尺度因子找到周围所包含的像素
		considerScaleFactor *= featurePoints.get(0).scale;
		float dDim05 = ((float) descDim) / 2.0f;

		int radius = (int) (((descDim + 1.0f) / 2f) * Math.sqrt(2.0f)
				* considerScaleFactor + 0.5f);

		ArrayList<FeaturePoint> survivors = new ArrayList<FeaturePoint>();

		float sigma2Sq = 2.0f * dDim05 * dDim05;// 2 * sigma ^2是高斯函数e指数上的分母
		for (FeaturePoint fp : featurePoints) {
			float angle = -fp.orientation;// 旋转-angle拉到水平方向上来

			fp.createVector(descDim, descDim, directionCount);

			// 旋转angle度的坐标
			for (int y = -radius; y < radius; ++y) {
				for (int x = -radius; x < radius; ++x) {

					float yR = (float) (Math.sin(angle) * x + Math.cos(angle)
							* y);
					float xR = (float) (Math.cos(angle) * x - Math.sin(angle)
							* y);

					// 使他定义在描述器的纬度之内
					yR /= considerScaleFactor;
					xR /= considerScaleFactor;

					// 使该点不超出描述器的范围
					if (yR >= (dDim05 + 0.5) || xR >= (dDim05 + 0.5)
							|| xR <= -(dDim05 + 0.5) || yR <= -(dDim05 + 0.5))
						continue;
					// 计算关键点和加权的点的具体x位置
					int currentX = (int) (x + fp.x + 0.5);
					// 计算关键点和加权的点的具体y位置

					int currentY = (int) (y + fp.y + 0.5);
					// 这保证它在范围之内部出去
					if (currentX < 1 || currentX >= (magnitude.width - 1)
							|| currentY < 1
							|| currentY >= (magnitude.height - 1))
						continue;
					// 高斯权重的计算
					float magW = (float) Math.exp(-(xR * xR + yR * yR)
							/ sigma2Sq)
							* magnitude.data[currentY * magnitude.width
									+ currentX];
					yR += dDim05 - 0.5;
					xR += dDim05 - 0.5;

					// 在两个点之间有阶跃的时候都可以用插值
					int[] xIdx = new int[2];
					int[] yIdx = new int[2];
					int[] dirIdx = new int[2]; // 每个点的坐标的orientation索引 [0] 方向的值
												// [1]是那个方向
					float[] xWeight = new float[2];
					float[] yWeight = new float[2];
					float[] dirWeight = new float[2];// 方向上
					// 可能在做插值
					if (xR >= 0) {
						xIdx[0] = (int) xR;
						xWeight[0] = (1.0f - (xR - xIdx[0]));
					}
					if (yR >= 0) {
						yIdx[0] = (int) yR;
						yWeight[0] = (1.0f - (yR - yIdx[0]));
					}

					if (xR < (descDim - 1)) {
						xIdx[1] = (int) (xR + 1.0f);
						xWeight[1] = xR - xIdx[1] + 1.0f;
					}
					if (yR < (descDim - 1)) {
						yIdx[1] = (int) (yR + 1.0f);
						yWeight[1] = yR - yIdx[1] + 1.0f;
					}
					// end 可能在做插值

					// 旋转角度到featurePoint的坐标下来，并且用[ -pi : pi ] 来表示
					float dir = direction.data[currentY * direction.width
							+ currentX]
							- fp.orientation;
					if (dir <= -Math.PI)
						dir += Math.PI;

					if (dir > Math.PI)
						dir -= Math.PI;

					// 统一到8个方向上
					float idxDir = (float) ((dir * directionCount) / (2.0 * Math.PI)); // directionCount/8为每一个度数有几个方向，然后
																						// *
																						// dir就统一到一至的方向上来了
					if (idxDir < 0.0)
						idxDir += directionCount;
					dirIdx[0] = (int) idxDir;
					dirIdx[1] = (dirIdx[0] + 1) % directionCount; // 下一个方向
					dirWeight[0] = 1.0f - (idxDir - dirIdx[0]); // 和下一个方向所差的值
					dirWeight[1] = idxDir - dirIdx[0]; // 和所在方向所差的值
					for (int iy = 0; iy < 2; ++iy) {
						for (int ix = 0; ix < 2; ++ix) {
							for (int d = 0; d < 2; ++d) {
								int idx = xIdx[ix] * fp.yDim * fp.oDim
										+ yIdx[iy] * fp.oDim + dirIdx[d];

								idx %= 128;
								fp.features[idx] += xWeight[ix] * yWeight[iy]
										* dirWeight[d] * magW;
							}
						}
					}
				}
			}

			capAndNormalizeFV(fp, fvGradHicap);
			survivors.add(fp);
		}

		return (survivors);
	}

	// 这里使用root sift（）进行二次归一化，可以有降燥
	private void capAndNormalizeFV(FeaturePoint kp, float fvGradHicap) {

		float norm = 0.0f;
		for (int n = 0; n < kp.features.length; ++n)
			norm += Math.pow(kp.features[n], 2.0);// 所有的值平方

		norm = (float) Math.sqrt(norm);// // feature vector的模
		if (norm == 0.0)
			throw (new IllegalStateException(
					"CapAndNormalizeFV cannot normalize with norm = 0.0"));

		for (int n = 0; n < kp.features.length; ++n) {
			kp.features[n] /= norm;
			if (kp.features[n] > fvGradHicap)
				kp.features[n] = fvGradHicap;
		}

		norm = 0.0f;
		for (int n = 0; n < kp.features.length; ++n)
			norm += Math.pow(kp.features[n], 2.0);
		norm = (float) Math.sqrt(norm);

		for (int n = 0; n < kp.features.length; ++n)
			kp.features[n] /= norm;
	}

	/**
	 * 从一个8度空间的高斯差分图集合中第二幅起到到数第二幅，每一幅上的点和它周围的8个点以及上一幅对应位置的9个点和下一幅对应位置的9个点进行比较，
	 * 看是否是最大值或最小值。 所以称为ThreeLeve
	 * 
	 * @param below
	 * @param current
	 * @param above
	 * @param curLev
	 * @param dogThresh
	 * @return
	 */
	private ArrayList<ScalePeak> findPeaks4ThreeLayer(ImagePixelArray below,
			ImagePixelArray current, ImagePixelArray above, int curLev,
			float dogThresh) {
		ArrayList<ScalePeak> peaks = new ArrayList<ScalePeak>();

		for (int y = 1; y < (current.height - 1); ++y) {
			for (int x = 1; x < (current.width - 1); ++x) {
				RefCheckMark ref = new RefCheckMark();
				ref.isMin = true;
				ref.isMax = true;
				float c = current.data[x + y * current.width]; // 作为中值

				if (Math.abs(c) <= dogThresh)
					continue; // 绝对值小于dogThresh直接过虑，防止大片被高期模糊后的低值点被选中

				checkMinMax(current, c, x, y, ref, true);
				checkMinMax(below, c, x, y, ref, false);
				checkMinMax(above, c, x, y, ref, false);
				if (ref.isMin == false && ref.isMax == false)
					continue;
				peaks.add(new ScalePeak(x, y, curLev));
			}
		}
		return peaks;
	}

	private void checkMinMax(ImagePixelArray layer, float c, int x, int y,
			RefCheckMark ref, boolean isCurrentLayer) {

		if (layer == null)
			return;

		if (ref.isMin) {
			if (layer.data[(y - 1) * layer.width + x - 1] <= c // // 左上
					|| layer.data[y * layer.width + x - 1] <= c // 左边
					|| layer.data[(y + 1) * layer.width + x - 1] <= c // 左下
					|| layer.data[(y - 1) * layer.width + x] <= c // 上边
					|| (isCurrentLayer ? false : (layer.data[y * layer.width
							+ x] < c))// 中间点，如果是当前层直接为false(自己),不是当前层应该小于,没有等于的条件
					|| layer.data[(y + 1) * layer.width + x] <= c // 下边
					|| layer.data[(y - 1) * layer.width + x + 1] <= c // 右上
					|| layer.data[y * layer.width + x + 1] <= c // 右边
					|| layer.data[(y + 1) * layer.width + x + 1] <= c) // 右下
				ref.isMin = false;
		}
		if (ref.isMax) {
			if (layer.data[(y - 1) * layer.width + x - 1] >= c // 左上
					|| layer.data[y * layer.width + x - 1] >= c // 左边
					|| layer.data[(y + 1) * layer.width + x - 1] >= c // 左下
					|| layer.data[(y - 1) * layer.width + x] >= c // 上边
					|| (isCurrentLayer ? false : (layer.data[y * layer.width
							+ x] > c)) // 中间点，如果是当前层直接为false(自己),不是当前层应该大于,没有等于的条件
					|| layer.data[(y + 1) * layer.width + x] >= c // 下边
					|| layer.data[(y - 1) * layer.width + x + 1] >= c // 右上
					|| layer.data[y * layer.width + x + 1] >= c // 右边
					|| layer.data[(y + 1) * layer.width + x + 1] >= c) // 右下
				ref.isMax = false;
		}
	}

	/**
	 * 边缘点的特点是沿边缘两侧的点的主曲率很大（曲率半径小），而与边缘相切的主曲率小（曲率半径大），说白了就是虽然它和边缘线旁边的点比较差值大，
	 * 但沿边缘线上的点之间的 差值很小，这样在边缘上的一点和另一点的描述子基本是差不多了，很难精确定位是哪一个点，所以要去掉。@page 12
	 * 
	 * @param space
	 * @param x
	 * @param y
	 * @param r
	 * @return
	 */
	private boolean isTooEdgelike(ImagePixelArray space, int x, int y, float r) {
		float d_xx, d_yy, d_xy;

		// d_xx = d_f(x+1) - d_f( x );0
		// d_f(x+1) = f(x+1) - f( x ); 1
		// d_f(x) = f(x) - f( x-1 );2
		// 将 1， 2式代入0式得
		// d_xx = f(x+1) + f(x-1) - 2 * f(x);
		// 对于d_xy = ( d_f( x , y+1 ) - d_f( x, y-1 ) ) * 0.5; 0
		// d_f(x,y+1) = (f(x+1,y+1) - f(x-1,y+1)) * 0.5; 1
		// d_f(x,y-1) = (f(x+1,y-1) - f(x-1,y-1)) * 0.5; 2
		// 将1，2代入 0 式
		// (f(x+1,y+1)+f(x+1,y-1)-f(x-1,y+1)-f(x-1,y-1)) * 0.25

		d_xx = space.data[(y + 1) * space.width + x]
				+ space.data[(y - 1) * space.width + x] - 2.0f
				* space.data[y * space.width + x];
		d_yy = space.data[y * space.width + x + 1]
				+ space.data[y * space.width + x - 1] - 2.0f
				* space.data[y * space.width + x];
		d_xy = 0.25f * ((space.data[(y + 1) * space.width + x + 1] - space.data[(y + 1)
				* space.width + x - 1]) //
		- (space.data[(y - 1) * space.width + x + 1] - space.data[(y - 1)
				* space.width + x - 1]));

		// @see page 13 in Lowe's paper
		float trHsq = d_xx + d_yy;
		trHsq *= trHsq;
		float detH = d_xx * d_yy - (d_xy * d_xy);
		float r1sq = (r + 1.0f);
		r1sq *= r1sq;
		if ((trHsq / detH) < (r1sq / r)) {
			return false;
		}
		return true;
	}

	/**
	 * 由于图像是一个离散的空间，最后的特征点的位置的坐标都是整数，但是备选的极值点的坐标是在连续尺度空间获取的，并不一定是整数，
	 * 所以要把当前备选的极值点投射到图像的坐标上， 需要进行一定的调整
	 * 
	 * @see page 10
	 * @param peak
	 * @param steps
	 * @param processed
	 * @return
	 */
	private boolean localizeIsWeak(ScalePeak peak, int steps, int[][] processed) {
		boolean needToAdjust = true;
		int adjusted = steps;
		while (needToAdjust) {
			int x = peak.x;
			int y = peak.y;
			if (peak.level <= 0 || peak.level >= (this.diffImags.length - 1))
				return (true);

			ImagePixelArray space = diffImags[peak.level];
			if (x <= 0 || x >= (space.width - 1))
				return (true);
			if (y <= 0 || y >= (space.height - 1))
				return (true);

			RefFloat dp = new RefFloat();
			AdjustedArray adj = getAdjustment(peak, peak.level, x, y, dp);

			float adjS = adj.data[0];
			float adjY = adj.data[1];
			float adjX = adj.data[2];

			if (Math.abs(adjX) > 0.5 || Math.abs(adjY) > 0.5) {
				// 调整的范围超过0.5，可能是下一个象素，直接过虑掉
				if (adjusted == 0) {
					return (true);
				}
				adjusted -= 1;

				// 用平方做它的偏离程度
				// 亚像素的应用意义
				float distSq = adjX * adjX + adjY * adjY;
				if (distSq > 2.0)
					return (true);

				// 如果不满足边缘中心准则：若（adjX,adjY）不在[-0.5,0.5]之间 则以（ x + 1 ）或 （x - 1)
				// 为新的展开点
				peak.x = (int) (peak.x + adjX + 0.5);
				peak.y = (int) (peak.y + adjY + 0.5);
				// point.Level = (int) (point.Level + adjS + 0.5);
				continue;
			}

			if (processed[peak.x][peak.y] != 0)
				return (true);

			processed[peak.x][peak.y] = 1;

			// 保存调整后的参数以便后面的过虑
			LocalInfo local = new LocalInfo(adjS, adjX, adjY);
			local.dValue = space.data[peak.y * space.width + peak.x] + 0.5f
					* dp.val;
			peak.local = local;
			needToAdjust = false;
		}
		return (false);
	}

	private AdjustedArray getAdjustment(ScalePeak peak, int level, int x,
			int y, RefFloat ref) {

		ref.val = 0.0f;
		if (peak.level <= 0 || peak.level >= (this.diffImags.length - 1)) {
			throw (new IllegalArgumentException(
					"point.Level is not within [bottom-1;top-1] range"));
		}
		ImagePixelArray b = this.diffImags[level - 1]; // below
		ImagePixelArray c = this.diffImags[level]; // current
		ImagePixelArray a = this.diffImags[level + 1]; // above

		AdjustedArray h = new AdjustedArray(3, 3);
		/*
		 * 下面是该幅图像尺度空间的三元偏导数，尺度空间上的二阶自变量为3的偏导数2006.3.1
		 */
		h.data[0] = b.data[y * b.width + x] - 2 * c.data[y * c.width + x]
				+ a.data[y * a.width + x]; // h.data[0][0]

		h.data[h.width] = h.data[1] = 0.25f * (a.data[(y + 1) * a.width + x] //
				- a.data[(y - 1) * a.width + x] //
		- (b.data[(y + 1) * b.width + x] - b.data[(y - 1) * b.width + x])); // h.data[0][1]

		h.data[h.width * 2] = h.data[2] = 0.25f * (a.data[y * a.width + x + 1]
				- a.data[y * a.width + x - 1] //
		- (b.data[y * b.width + x + 1] - b.data[y * b.width + x - 1]));

		h.data[1 * h.width + 1] = c.data[(y - 1) * c.width + x] - 2f
				* c.data[y * c.width + x] + c.data[(y + 1) * c.width + x];

		h.data[1 + h.width * 2] = h.data[2 + h.width] = 0.25f * (c.data[(y + 1)
				* c.width + x + 1] //
				- c.data[(y + 1) * c.width + x - 1] //
		- (c.data[(y - 1) * c.width + x + 1] //
		- c.data[(y - 1) * c.width + x - 1]));

		h.data[2 * h.width + 2] = c.data[y * c.width + x - 1] - 2
				* c.data[y * c.width + x] + c.data[y * c.width + x + 1];
		AdjustedArray d = new AdjustedArray(1, 3);
		/*
		 * 下面这个是自变量为3的一阶偏导数2006.3.1
		 */

		d.data[0] = 0.5f * (a.data[y * a.width + x] - b.data[y * b.width + x]); // d.data[1][0]
																				// =>
																				// d.data[0*width+1]
																				// =
																				// d.data[1]
		d.data[1] = 0.5f * (c.data[(y + 1) * c.width + x] - c.data[(y - 1)
				* c.width + x]);
		d.data[2] = 0.5f * (c.data[y * c.width + x + 1] - c.data[y * c.width
				+ x - 1]);

		AdjustedArray back = d.clone();
		back.negate();
		// 求解Solve: A x = b
		h.solveLinear(back);
		ref.val = back.dot(d);
		return (back);
	}

	private static class AdjustedArray extends FloatArray implements Cloneable {

		public int width;
		public int height;

		public AdjustedArray(int width, int height) {
			this.width = width;
			this.height = height;
			this.data = new float[width * height];
		}

		public AdjustedArray clone() {
			AdjustedArray cp = new AdjustedArray(this.width, this.height);
			System.arraycopy(this.data, 0, cp.data, 0, this.data.length);
			return cp;
		}

		// 矩阵的点乘
		public float dot(AdjustedArray aa) {
			if (this.width != aa.width || this.width != 1 || aa.width != 1) {
				throw (new IllegalArgumentException(
						"Dotproduct only possible for two equal n x 1 matrices"));
			}
			float sum = 0.0f;

			for (int y = 0; y < this.height; ++y)
				sum += data[y * this.width + 0] * aa.data[y * aa.width + 0];
			return (sum);
		}

		public void negate() {
			for (int y = 0; y < this.data.length; ++y) {
				data[y] = -data[y];
			}
		}

		// 高斯主元素消去法
		public void solveLinear(AdjustedArray vec) {
			if (this.width != this.height || this.height != vec.height) {
				throw (new IllegalArgumentException(
						"Matrix not quadratic or vector dimension mismatch"));
			}

			// Gaussian Elimination Algorithm, as described by
			// "Numerical Methods - A Software Approach", R.L. Johnston

			// Forward elimination with partial pivoting
			for (int y = 0; y < (this.height - 1); ++y) {

				int yMaxIndex = y;
				float yMaxValue = Math.abs(data[y * this.width + y]);
				// 找列中最大的那个元素
				for (int py = y; py < this.height; ++py) {
					if (Math.abs(data[py * this.width + y]) > yMaxValue) {
						yMaxValue = Math.abs(data[py * this.width + y]);
						yMaxIndex = py;
					}
				}

				swapRow(y, yMaxIndex);
				vec.swapRow(y, yMaxIndex);
				// 化成上三角阵
				for (int py = y + 1; py < this.height; ++py) {
					float elimMul = data[py * this.width + y]
							/ data[y * this.width + y];
					for (int x = 0; x < this.width; ++x)
						data[py * this.width + x] -= elimMul
								* data[y * this.width + x];
					vec.data[py * vec.width + 0] -= elimMul
							* vec.data[y * vec.width + 0];
				}
			}

			// 求解放入vec中
			// 从这里我们还可以看出，参数是类的对象，等于是传入类的指针
			for (int y = this.height - 1; y >= 0; --y) {
				float solY = vec.data[y * vec.width + 0];
				for (int x = this.width - 1; x > y; --x)
					solY -= data[y * this.width + x]
							* vec.data[x * vec.width + 0];
				vec.data[y * vec.width + 0] = solY / data[y * this.width + y];
			}
		}

		// Swap two rows r1, r2
		private void swapRow(int r1, int r2) {
			if (r1 == r2)
				return;
			for (int x = 0; x < this.width; ++x) {
				float temp = data[r1 * this.width + x];
				data[r1 * this.width + x] = data[r2 * this.width + x];
				data[r2 * this.width + x] = temp;
			}
		}
	}

	/**
	 * 用于传递引用的数据结构
	 */
	static class RefCheckMark {

		boolean isMin;
		boolean isMax;
	}

	/**
	 * 用于传递引用的数据结构
	 */
	static class RefPeakValueAndDegreeCorrection {

		float peakValue;
		float degreeCorrection;
	}

}
