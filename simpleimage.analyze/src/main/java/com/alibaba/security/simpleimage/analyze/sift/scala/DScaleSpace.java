/*
 * Copyright 2013 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package com.alibaba.security.simpleimage.analyze.sift.scala;

import java.util.ArrayList;

import com.alibaba.security.simpleimage.analyze.reftype.RefDouble;
import com.alibaba.security.simpleimage.analyze.sift.ImageMap;
import com.alibaba.security.simpleimage.analyze.sift.conv.GaussianConvolution;
import com.alibaba.security.simpleimage.analyze.sift.matrix.SimpleMatrix;

/**
 * 类DScaleSpace.java的实现描述：TODO 类实现描述
 * 
 * @author axman 2013-3-25 上午11:12:20
 */
public class DScaleSpace {

    boolean     verbose = System.getProperty("_verbose") == null ? false : true;
    DScaleSpace down    = null;
    DScaleSpace up      = null;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public DScaleSpace getDown() {
        return this.down;
    }

    public void setDown(DScaleSpace down) {
        this.down = down;
    }

    public DScaleSpace getUp() {
        return this.up;
    }

    public void setUp(DScaleSpace up) {
        this.up = up;
    }

    ImageMap   baseImg;
    double     basePixScale;

    ImageMap[] imgScaled;

    public ImageMap getGaussianMap(int idx) {
        return this.imgScaled[idx];
    }

    private ImageMap[] magnitudes;
    private ImageMap[] directions;

    public ImageMap getLastGaussianMap() {
        if (this.imgScaled.length < 2) {
            throw new java.lang.IllegalArgumentException("err: too few gaussian maps.");
        }
        return (this.imgScaled[this.imgScaled.length - 2]);
    }

    public ImageMap[] spaces;

    public int getCount() {
        return this.spaces.length;
    }

    public ArrayList<ScalePoint> findPeaks(double dogThresh) {
        if (verbose) System.out.printf("FindPeaks: scale %9.2f, testing %d levels\r\n", basePixScale,
                                       this.spaces.length - 2);

        ArrayList<ScalePoint> peaks = new ArrayList<ScalePoint>();

        ImageMap current, above, below;

        // Search the D(k * sigma) to D(2 * sigma) spaces
        for (int level = 1; level < (this.spaces.length - 1); ++level) {
            current = this.spaces[level];
            below = this.spaces[level - 1];
            above = this.spaces[level + 1];

            //System.out.printf("peak-search at level %d\r\n", level);
            /*
             * Console.WriteLine ("below/current/above: {0} {1} {2}", below == null ? "-" : "X", current == null ? "-" :
             * "X", above == null ? "-" : "X"); Console.WriteLine ("peak-search at level {0}", level);
             */

            peaks.addAll(findPeaksThreeLevel(below, current, above, level, dogThresh));
            below = current;
        }

        return (peaks);
    }

    public ArrayList<KeyPoint> generateKeyPoints(ArrayList<ScalePoint> localizedPeaks, int scaleCount,
                                                 double octaveSigma) {
        ArrayList<KeyPoint> keypoints = new ArrayList<KeyPoint>();
        for (ScalePoint sp : localizedPeaks) {
            ArrayList<KeyPoint> thisPointKeys = generateKeyPointSingle(this.basePixScale, sp, 36, 0.8, scaleCount,
                                                                       octaveSigma);
            thisPointKeys = createDescriptors(thisPointKeys, magnitudes[sp.getLevel()], directions[sp.getLevel()], 2.0,
                                              4, 8, 0.2);
            for (KeyPoint kp : thisPointKeys) {
                if (kp.hasFV == false) {
                    throw new java.lang.IllegalStateException("should not happen");
                }

                kp.x *= kp.imgScale;
                kp.y *= kp.imgScale;
                kp.scale *= kp.imgScale;
                keypoints.add(kp);
            }
        }
        return keypoints;
    }

    private ArrayList<KeyPoint> generateKeyPointSingle(double imgScale, ScalePoint point, int binCount,
                                                       double peakRelThresh, int scaleCount, double octaveSigma) {

        // The relative estimated keypoint scale. The actual absolute keypoint
        // scale to the original image is yielded by the product of imgScale.
        // But as we operate in the current octave, the size relative to the
        // anchoring images is missing the imgScale factor.
        double kpScale = octaveSigma * Math.pow(2.0, (point.level + point.local.scaleAdjust) / scaleCount);

        // Lowe03, "A gaussian-weighted circular window with a \sigma three
        // times that of the scale of the keypoint".
        //
        // With sigma = 3.0 * kpScale, the square dimension we have to
        // consider is (3 * sigma) (until the weight becomes very small).
        double sigma = 3.0 * kpScale;
        int radius = (int) (3.0 * sigma / 2.0 + 0.5);
        int radiusSq = radius * radius;

        ImageMap magnitude = magnitudes[point.level];
        ImageMap direction = directions[point.level];

        // As the point may lie near the border, build the rectangle
        // coordinates we can still reach, minus the border pixels, for which
        // we do not have gradient information available.
        int xMin = Math.max(point.x - radius, 1);
        int xMax = Math.min(point.x + radius, magnitude.xDim - 1);
        int yMin = Math.max(point.y - radius, 1);
        int yMax = Math.min(point.y + radius, magnitude.yDim - 1);

        // Precompute 1D gaussian divisor (2 \sigma^2) in:
        // G(r) = e^{-\frac{r^2}{2 \sigma^2}}
        double gaussianSigmaFactor = 2.0 * sigma * sigma;

        double[] bins = new double[binCount];

        // Build the direction histogram
        for (int y = yMin; y < yMax; ++y) {
            for (int x = xMin; x < xMax; ++x) {
                // Only consider pixels in the circle, else we might skew the
                // orientation histogram by considering more pixels into the
                // corner directions
                int relX = x - point.x;// 求半径
                int relY = y - point.y;// 求半径
                if (isInCircle(relX, relY, radiusSq) == false) continue;

                // The gaussian weight factor.
                double gaussianWeight = Math.exp(-((relX * relX + relY * relY) / gaussianSigmaFactor));

                // find the closest bin and add the direction
                int binIdx = findClosestRotationBin(binCount, direction.valArr[y][x]);
                bins[binIdx] += magnitude.valArr[y][x] * gaussianWeight;
            }
        }

        // As there may be succeeding histogram entries like this:
        // ( ..., 0.4, 0.3, 0.4, ... ) where the real peak is located at the
        // middle of this three entries, we can improve the distinctiveness of
        // the bins by applying an averaging pass.
        //
        // is this really the best method? (we also loose a bit of
        // information. Maybe there is a one-step method that conserves more)
        averageWeakBins(bins, binCount);

        // find the maximum peak in gradient orientation
        double maxGrad = 0.0;
        int maxBin = 0;
        for (int b = 0; b < binCount; ++b) {
            if (bins[b] > maxGrad) {
                maxGrad = bins[b];
                maxBin = b;
            }
        }

        // First determine the real interpolated peak high at the maximum bin
        // position, which is guaranteed to be an absolute peak.
        //
        // XXX: should we use the estimated peak value as reference for the
        // 0.8 check or the original bin-value?

        RefPVAndDC ref1 = new RefPVAndDC();
        InterpolateOrientation(bins[maxBin == 0 ? (binCount - 1) : (maxBin - 1)], bins[maxBin], bins[(maxBin + 1)
                                                                                                     % binCount], ref1);

        // Now that we know the maximum peak value, we can find other keypoint
        // orientations, which have to fulfill two criterias:
        // 这样找到的不止是两个最大的方向 看
        // paper pager 13
        // Therefore,for locations with multiple peaks of similar magnitude
        // ,there will be multiple keypoints created at the same location and
        // scale but different orientations 2006.03.8
        //
        // 1. They must be a local peak themselves. Else we might add a very
        // similar keypoint orientation twice (imagine for example the
        // values: 0.4 1.0 0.8, if 1.0 is maximum peak, 0.8 is still added
        // with the default threshhold, but the maximum peak orientation
        // was already added).
        // 2. They must have at least peakRelThresh times the maximum peak
        // value.
        boolean[] binIsKeypoint = new boolean[binCount];
        for (int b = 0; b < binCount; ++b) {
            binIsKeypoint[b] = false;

            // The maximum peak of course is
            if (b == maxBin) {
                binIsKeypoint[b] = true;
                continue;
            }

            // Local peaks are, too, in case they fulfill the threshhold
            if (bins[b] < (peakRelThresh * ref1.peakValue)) continue;

            int leftI = (b == 0) ? (binCount - 1) : (b - 1);
            int rightI = (b + 1) % binCount;
            if (bins[b] <= bins[leftI] || bins[b] <= bins[rightI]) continue; // no local peak

            binIsKeypoint[b] = true;
        }

        // All the valid keypoint bins are now marked in binIsKeypoint, now
        // build them.
        ArrayList<KeyPoint> keypoints = new ArrayList<KeyPoint>();

        // find other possible locations
        double oneBinRad = (2.0 * Math.PI) / binCount;

        for (int b = 0; b < binCount; ++b) {
            if (binIsKeypoint[b] == false) continue;

            int bLeft = (b == 0) ? (binCount - 1) : (b - 1);
            int bRight = (b + 1) % binCount;

            // Get an interpolated peak direction and value guess.
            // double peakValue;
            // double degreeCorrection;
            RefPVAndDC ref2 = new RefPVAndDC();

            if (InterpolateOrientation(bins[bLeft], bins[b], bins[bRight], ref2) == false) {
                throw (new java.lang.IllegalStateException("BUG: Parabola fitting broken"));
            }

            // [-1.0 ; 1.0] -> [0 ; binrange], and add the fixed absolute bin
            // position.
            // We subtract PI because bin 0 refers to 0, binCount-1 bin refers
            // to a bin just below 2PI, so -> [-PI ; PI]. Note that at this
            // point we determine the canonical descriptor anchor angle. It
            // does not matter where we set it relative to the peak degree,
            // but it has to be constant. Also, if the output of this
            // implementation is to be matched with other implementations it
            // must be the same constant angle (here: -PI).
            double degree = (b + ref2.degreeCorrection) * oneBinRad - Math.PI;
            // 完全化在 -180 到 +180 之间
            if (degree < -Math.PI) degree += 2.0 * Math.PI;
            else if (degree > Math.PI) degree -= 2.0 * Math.PI;

            KeyPoint kp = new KeyPoint(imgScaled[point.level], point.x + point.local.fineX,
                                       point.y + point.local.fineY, imgScale, kpScale, degree);
            keypoints.add(kp);
        }

        return (keypoints);
    }

    private ArrayList<KeyPoint> createDescriptors(ArrayList<KeyPoint> keypoints, ImageMap magnitude,
                                                  ImageMap direction, double considerScaleFactor, int descDim,
                                                  int directionCount, double fvGradHicap) {
        if (keypoints.size() <= 0) return (keypoints);
        // 通过尺度因子找到周围所包含的像素
        considerScaleFactor *= keypoints.get(0).scale;
        double dDim05 = ((double) descDim) / 2.0;

        // Now calculate the radius: We consider pixels in a square with
        // dimension 'descDim' plus 0.5 in each direction. As the feature
        // vector elements at the diagonal borders are most distant from the
        // center pixel we have scale up with sqrt(2).
        int radius = (int) (((descDim + 1.0) / 2) * Math.sqrt(2.0) * considerScaleFactor + 0.5);

        // Instead of modifying the original list, we just copy the keypoints
        // that received a descriptor.
        ArrayList<KeyPoint> survivors = new ArrayList<KeyPoint>();

        // Precompute the sigma for the "center-most, border-less" gaussian
        // weighting.
        // (We are operating to dDim05, CV(computer vision) book tells us G(x), x > 3 * sigma
        // negligible, but this range seems much shorter!?)
        //
        // In Lowe03, page 15 it says "A Gaussian weighting function with
        // sigma equal to one half the width of the descriptor window is
        // used", so we just use his advice.它指的是描述器窗口的一半
        double sigma2Sq = 2.0 * dDim05 * dDim05;// 2 * sigma ^2是高斯函数e指数上的分母上的数

        for (KeyPoint kp : keypoints) {
            // The angle to rotate with: negate the orientation.
            double angle = -kp.orientation;// 旋转-angle拉到水平方向上来

            kp.createVector(descDim, descDim, directionCount);
            // Console.WriteLine ("  FV allocated");
            // 旋转angle度的坐标
            for (int y = -radius; y < radius; ++y) {
                for (int x = -radius; x < radius; ++x) {
                    // Rotate and scale
                    double yR = Math.sin(angle) * x + Math.cos(angle) * y;
                    double xR = Math.cos(angle) * x - Math.sin(angle) * y;

                    // 使他定义在描述器的纬度之内
                    yR /= considerScaleFactor; // yR = yR / considerScaleFactor
                    xR /= considerScaleFactor; // yR = yR / considerScaleFactor

                    // Now consider all (xR, yR) that are anchored within
                    // (- descDim/2 - 0.5 ; -descDim/2 - 0.5) to
                    // (descDim/2 + 0.5 ; descDim/2 + 0.5),
                    // as only those can influence the FV.
                    // 使该点不超出描述器的范围
                    if (yR >= (dDim05 + 0.5) || xR >= (dDim05 + 0.5) || xR <= -(dDim05 + 0.5) || yR <= -(dDim05 + 0.5)) continue;
                    // 计算关键点和加权的点的具体x位置
                    int currentX = (int) (x + kp.x + 0.5);
                    // 计算关键点和加权的点的具体y位置
                    int currentY = (int) (y + kp.y + 0.5);
                    // 这保证它在范围之内部出去
                    if (currentX < 1 || currentX >= (magnitude.xDim - 1) || currentY < 1
                        || currentY >= (magnitude.yDim - 1)) continue;

                    /*
                     * Console.WriteLine ("    ({0},{1}) by angle {2} -> ({3},{4})", x, y, angle, xR, yR);
                     */

                    // Weight the magnitude relative to the center of the
                    // whole FV. We do not need a normalizing factor now, as
                    // we normalize the whole FV later anyway (see below).
                    // xR, yR are each in -(dDim05 + 0.5) to (dDim05 + 0.5)
                    // range
                    // 高斯权重的计算
                    double magW = Math.exp(-(xR * xR + yR * yR) / sigma2Sq) * magnitude.valArr[currentY][currentX];

                    // Anchor to (-1.0, -1.0)-(dDim + 1.0, dDim + 1.0), where
                    // the FV points are located at ( x , y )
                    yR += dDim05 - 0.5;
                    xR += dDim05 - 0.5;

                    // 记住在两个点之间有阶跃的时候都可以用插值

                    // Build linear interpolation weights:
                    // A B
                    // C D
                    //
                    // The keypoint is located between A, B, C and D.
                    int[] xIdx = new int[2];
                    int[] yIdx = new int[2];
                    int[] dirIdx = new int[2]; // 每个点的坐标的orientation索引 [0] 方向的值 [1]是那个方向
                    double[] xWeight = new double[2];
                    double[] yWeight = new double[2];
                    double[] dirWeight = new double[2];// 方向上
                    // 可能在做插值
                    if (xR >= 0) {
                        xIdx[0] = (int) xR;
                        xWeight[0] = (1.0 - (xR - xIdx[0]));
                    }
                    if (yR >= 0) {
                        yIdx[0] = (int) yR;
                        yWeight[0] = (1.0 - (yR - yIdx[0]));
                    }

                    if (xR < (descDim - 1)) {
                        xIdx[1] = (int) (xR + 1.0);
                        xWeight[1] = xR - xIdx[1] + 1.0;
                    }
                    if (yR < (descDim - 1)) {
                        yIdx[1] = (int) (yR + 1.0);
                        yWeight[1] = yR - yIdx[1] + 1.0;
                    }
                    // end 可能在做插值

                    // Rotate the gradient direction by the keypoint
                    // orientation, then normalize to [-pi ; pi] range.
                    // 旋转角度到keypoint的坐标下来，并且用[ -pi : pi ] 来表示
                    double dir = direction.valArr[currentY][currentX] - kp.orientation;
                    if (dir <= -Math.PI) dir += Math.PI;
                    if (dir > Math.PI) dir -= Math.PI;
                    // 统一在分着个八个方向上来
                    double idxDir = (dir * directionCount) / (2.0 * Math.PI);// directionCount/8为每一个度数有几个方向，然后 *
                                                                             // dir就统一到一至的方向上来了
                    if (idxDir < 0.0) idxDir += directionCount;

                    dirIdx[0] = (int) idxDir;
                    dirIdx[1] = (dirIdx[0] + 1) % directionCount; // 下一个方向
                    dirWeight[0] = 1.0 - (idxDir - dirIdx[0]); // 和下一个方向所差的值
                    dirWeight[1] = idxDir - dirIdx[0]; // 和所在方向所差的值

                    /*
                     * Console.WriteLine ("    ({0},{1}) yields:", xR, yR); Console.WriteLine
                     * ("      x<{0},{1}>*({2},{3})", xIdx[0], xIdx[1], xWeight[0], xWeight[1]); Console.WriteLine
                     * ("      y<{0},{1}>*({2},{3})", yIdx[0], yIdx[1], yWeight[0], yWeight[1]); Console.WriteLine
                     * ("      dir<{0},{1}>*({2},{3})", dirIdx[0], dirIdx[1], dirWeight[0], dirWeight[1]);
                     * Console.WriteLine ("    weighting m * w: {0} * {1}", magnitude[currentX, currentY], Math.Exp
                     * (-(xR * xR + yR * yR) / sigma2Sq));
                     */
                    for (int iy = 0; iy < 2; ++iy) {
                        for (int ix = 0; ix < 2; ++ix) {
                            for (int id = 0; id < 2; ++id) {
                                kp.featureVectorSet(xIdx[ix], yIdx[iy], dirIdx[id],
                                                    kp.featureVectorGet(xIdx[ix], yIdx[iy], dirIdx[id]) + xWeight[ix]
                                                            * yWeight[iy] * dirWeight[id] * magW);
                            }
                        }
                    }
                }
            }

            // Normalize and hicap the feature vector, as recommended on page
            // 16 in Lowe03.
            capAndNormalizeFV(kp, fvGradHicap);

            survivors.add(kp);
        }

        return (survivors);
    }

    private boolean isInCircle(int rX, int rY, int radiusSq) {
        rX *= rX;
        rY *= rY;
        if ((rX + rY) <= radiusSq) return (true);

        return (false);
    }

    private ArrayList<ScalePoint> findPeaksThreeLevel(ImageMap below, ImageMap current, ImageMap above, int curLev,
                                                      double dogThresh) {
        ArrayList<ScalePoint> peaks = new ArrayList<ScalePoint>();

        for (int y = 1; y < (current.yDim - 1); ++y) {
            for (int x = 1; x < (current.xDim - 1); ++x) {
                RefCheckMark ref = new RefCheckMark();
                ref.isMin = true;
                ref.isMax = true;

                double c = current.valArr[y][x]; // Center value

                /*
                 * If the magnitude is below the threshhold, skip it early.
                 */
                if (Math.abs(c) <= dogThresh) continue;

                checkMinMax(current, c, x, y, ref, true);
                checkMinMax(below, c, x, y, ref, false);
                checkMinMax(above, c, x, y, ref, false);
                if (ref.isMin == false && ref.isMax == false) continue;

                // Console.WriteLine ("{0} {1} {2} # DOG", y, x, c);

                /*
                 * Add the peak that survived the first checks, to the peak list.
                 */
                peaks.add(new ScalePoint(x, y, curLev));
            }
        }

        return (peaks);
    }

    static class RefCheckMark {

        /**
         * 用于传递引用的数据结构
         */
        boolean isMin;
        boolean isMax;
    }

    static class RefPVAndDC {

        /**
         * 用于传递引用的数据结构
         */
        double peakValue;
        double degreeCorrection;
    }

    private void checkMinMax(ImageMap layer, double c, int x, int y, RefCheckMark ref, boolean cLayer) {
        if (layer == null) return;

        if (ref.isMin) {
            if (layer.valArr[y - 1][x - 1] <= c || layer.valArr[y][x - 1] <= c || layer.valArr[y + 1][x - 1] <= c
                || layer.valArr[y - 1][x] <= c
                ||
                // note here its just < instead of <=
                (cLayer ? false : (layer.valArr[y][x] < c)) || layer.valArr[y + 1][x] <= c
                || layer.valArr[y - 1][x + 1] <= c || layer.valArr[y][x + 1] <= c || layer.valArr[y + 1][x + 1] <= c) ref.isMin = false;
        }
        if (ref.isMax) {
            if (layer.valArr[y - 1][x - 1] >= c || layer.valArr[y][x - 1] >= c || layer.valArr[y + 1][x - 1] >= c
                || layer.valArr[y - 1][x] >= c
                ||
                // note here its just > instead of >=
                (cLayer ? false : (layer.valArr[y][x] > c)) || layer.valArr[y + 1][x] >= c
                || layer.valArr[y - 1][x + 1] >= c || layer.valArr[y][x + 1] >= c || layer.valArr[y + 1][x + 1] >= c) ref.isMax = false;
        }
    }

    private int findClosestRotationBin(int binCount, double angle) {
        angle += Math.PI;
        angle /= 2.0 * Math.PI;
        // calculate the aligned bin
        angle *= binCount;

        int idx = (int) angle;
        if (idx == binCount) idx = 0;
        return (idx);
    }

    private void averageWeakBins(double[] bins, int binCount) {
        // ( 0.4, 0.4, 0.3, 0.4, 0.4 ))
        // 每三个做一个平均直至完成
        for (int sn = 0; sn < 4; ++sn) {
            double firstE = bins[0];
            double last = bins[binCount - 1];

            for (int sw = 0; sw < binCount; ++sw) {
                double cur = bins[sw];
                double next = (sw == (binCount - 1)) ? firstE : bins[(sw + 1) % binCount];

                bins[sw] = (last + cur + next) / 3.0;
                last = cur;
            }
        }
    }

    private boolean InterpolateOrientation(double left, double middle, double right, RefPVAndDC ref) {
        double a = ((left + right) - 2.0 * middle) / 2.0;
        ref.degreeCorrection = ref.peakValue = Double.NaN;

        // Not a parabol
        if (a == 0.0) return (false);

        double c = (((left - middle) / a) - 1.0) / 2.0;
        double b = middle - c * c * a;

        if (c < -0.5 || c > 0.5) throw (new IllegalStateException("InterpolateOrientation: off peak ]-0.5 ; 0.5["));

        ref.degreeCorrection = c;
        ref.peakValue = b;

        return true;
    }

    private void capAndNormalizeFV(KeyPoint kp, double fvGradHicap) {
        // Straight normalization
        double norm = 0.0;
        for (int n = 0; n < kp.getFVLinearDim(); ++n)
            norm += Math.pow(kp.featureVectorLinearGet(n), 2.0);// 所有的值平方

        norm = Math.sqrt(norm);// feature vector的模
        if (norm == 0.0) throw (new java.lang.IllegalStateException(
                                                                    "CapAndNormalizeFV cannot normalize with norm = 0.0"));

        for (int n = 0; n < kp.getFVLinearDim(); ++n)
            kp.featureVectorLinearSet(n, kp.featureVectorLinearGet(n) / norm);

        // Hicap after normalization
        for (int n = 0; n < kp.getFVLinearDim(); ++n) {
            if (kp.featureVectorLinearGet(n) > fvGradHicap) {
                kp.featureVectorLinearSet(n, fvGradHicap);
            }
        }

        // Renormalize again
        norm = 0.0;
        for (int n = 0; n < kp.getFVLinearDim(); ++n)
            norm += Math.pow(kp.featureVectorLinearGet(n), 2.0);

        norm = Math.sqrt(norm);

        for (int n = 0; n < kp.getFVLinearDim(); ++n)
            kp.featureVectorLinearSet(n, kp.featureVectorLinearGet(n) / norm);
    }

    /**
     * @param peaks
     * @param maximumEdgeRatio
     * @param dValueLowThresh
     * @param scaleAdjustThresh
     * @param relocationMaximum
     * @return
     */
    public ArrayList<ScalePoint> filterAndLocalizePeaks(ArrayList<ScalePoint> peaks, double edgeRatio,
                                                        double dValueLoThresh, double scaleAdjustThresh,
                                                        int relocationMaximum) {
        ArrayList<ScalePoint> filtered = new ArrayList<ScalePoint>();

        int[][] processedMap = new int[spaces[0].xDim][spaces[0].yDim];

        for (ScalePoint peak : peaks) {
            if (isTooEdgelike(spaces[peak.level], peak.x, peak.y, edgeRatio)) continue;

            // When the localization hits some problem, i.e. while moving the
            // point a border is reached, then skip this point.

            if (localizeIsWeak(peak, relocationMaximum, processedMap)) continue;

            /*
             * Console.WriteLine ("peak.Local.ScaleAdjust = {0}", peak.Local.ScaleAdjust);
             */
            if (Math.abs(peak.local.scaleAdjust) > scaleAdjustThresh) continue;

            // Additional local pixel information is now available, threshhold
            // the D(^x)
            // Console.WriteLine ("{0} {1} {2} # == DVALUE", peak.Y, peak.X, peak.Local.DValue);

            if (Math.abs(peak.local.dValue) <= dValueLoThresh) continue;

            /*
             * Console.WriteLine ("{0} {1} {2} {3} # FILTERLOCALIZE", peak.Y, peak.X, peak.Local.ScaleAdjust,
             * peak.Local.DValue);
             */

            // its edgy enough, add it
            filtered.add(peak);

        }
        return (filtered);
    }

    public void generateMagnitudeAndDirectionMaps() {
        // We leave the first entry to null, and ommit the last. This way, the
        // magnitudes and directions maps have the same index as the
        // imgScaled maps they below to.
        magnitudes = new ImageMap[this.getCount() - 1];// 图像的数组
        directions = new ImageMap[this.getCount() - 1];// 图像的数组

        // Build the maps, omitting the border pixels, as we cannot build
        // gradient information there.
        // top left border corner -> * * *
        // * [*] *
        // * * *
        //
        for (int s = 1; s < (this.getCount() - 1); ++s) {
            magnitudes[s] = new ImageMap(imgScaled[s].xDim, imgScaled[s].yDim);
            directions[s] = new ImageMap(imgScaled[s].xDim, imgScaled[s].yDim);

            for (int y = 1; y < (imgScaled[s].yDim - 1); ++y) {
                for (int x = 1; x < (imgScaled[s].xDim - 1); ++x) {
                    // gradient magnitude m
                    magnitudes[s].valArr[y][x] = Math.sqrt(Math.pow(imgScaled[s].valArr[y][x + 1]
                                                                    - imgScaled[s].valArr[y][x - 1], 2.0)
                                                           + Math.pow(imgScaled[s].valArr[y + 1][x]
                                                                      - imgScaled[s].valArr[y - 1][x], 2.0));

                    // gradient direction theta
                    directions[s].valArr[y][x] = Math.atan2(imgScaled[s].valArr[y + 1][x]
                                                                    - imgScaled[s].valArr[y - 1][x],
                                                            imgScaled[s].valArr[y][x + 1]
                                                                    - imgScaled[s].valArr[y][x - 1]);
                }
            }
        }
    }

    /**
     * 
     */
    public void clearMagnitudeAndDirectionMaps() {
        for (int i = 0; i < this.magnitudes.length; i++)
            this.magnitudes[i] = null;
        for (int i = 0; i < this.directions.length; i++)
            this.directions[i] = null;
        magnitudes = directions = null;
    }

    public void buildDiffMaps() {
        // Generate DoG maps. The maps are organized like this:
        // 0: D(sigma)
        // 1: D(k * sigma)
        // 2: D(k^2 * sigma)
        // ...
        // s: D(k^s * sigma) = D(2 * sigma)
        // s+1: D(k * 2 * sigma)
        //
        // So, we can start peak searching at 1 to s, and have a DoG map into
        // each direction.
        spaces = new ImageMap[imgScaled.length - 1];

        // After the loop completes, we have used (s + 1) maps, yielding s
        // D-gaussian maps in the range of sigma to 2*sigma, as k^s = 2, which
        // is defined as one octave.
        for (int sn = 0; sn < spaces.length; ++sn) {
            // XXX: order correct? It should not really matter as we search
            // for both minimums and maximums, but still, what is recommended?
            // (otherwise maybe the gradient directions are inverted?)
            spaces[sn] = ImageMap.minus(imgScaled[sn + 1], imgScaled[sn]);
        }
    }

    public void buildGaussianMaps(ImageMap first, double firstScale, int scales, double sigma) {
        // We need one more gaussian blurred image than the number of DoG
        // maps. But for the minima/maxima pixel search, we need two more. See
        // BuildDiffMaps.
        imgScaled = new ImageMap[scales + 1 + 1 + 1];
        this.basePixScale = firstScale;
        // Ln1(x, y, k^{p+1}) = G(x, y, \sqrt{k^2-1}) * Ln0(x, y, k^p).
        ImageMap prev = first;
        imgScaled[0] = first;

        double w = sigma;
        double kTerm = Math.sqrt(Math.pow(SToK(scales), 2.0) - 1.0);
        for (int scI = 1; scI < imgScaled.length; ++scI) {
            GaussianConvolution gauss = new GaussianConvolution(w * kTerm);
            prev = imgScaled[scI] = gauss.convolve(prev);
            w *= SToK(scales);
        }
    }

    static public double SToK(int s) {
        return (Math.pow(2.0, 1.0 / s));
    }

    private SimpleMatrix getAdjustment(ScalePoint point, int level, int x, int y, RefDouble ref) {
        /*
         * Console.WriteLine ("GetAdjustment (point, {0}, {1}, {2}, out double dp)", level, x, y);
         */
        ref.val = 0.0;
        if (point.level <= 0 || point.level >= (spaces.length - 1)) throw (new IllegalArgumentException(
                                                                                                        "point.Level is not within [bottom-1;top-1] range"));

        ImageMap below = spaces[level - 1];
        ImageMap current = spaces[level];
        ImageMap above = spaces[level + 1];

        SimpleMatrix H = new SimpleMatrix(3, 3);
        /*
         * 下面是该幅图像尺度空间的三元偏导数，记住是尺度空间上 的二阶自变量为3的偏导数2006.3.1
         */
        H.values[0][0] = below.valArr[y][x] - 2 * current.valArr[y][x] + above.valArr[y][x];
        H.values[0][1] = H.values[1][0] = 0.25 * (above.valArr[y + 1][x] - above.valArr[y - 1][x] - (below.valArr[y + 1][x] - below.valArr[y - 1][x]));
        H.values[0][2] = H.values[2][0] = 0.25 * (above.valArr[y][x + 1] - above.valArr[y][x - 1] - (below.valArr[y][x + 1] - below.valArr[y][x - 1]));
        H.values[1][1] = current.valArr[y - 1][x] - 2 * current.valArr[y][x] + current.valArr[y + 1][x];
        H.values[1][2] = H.values[2][1] = 0.25 * (current.valArr[y + 1][x + 1] - current.valArr[y + 1][x - 1] - (current.valArr[y - 1][x + 1] - current.valArr[y - 1][x - 1]));
        H.values[2][2] = current.valArr[y][x - 1] - 2 * current.valArr[y][x] + current.valArr[y][x + 1];

        SimpleMatrix d = new SimpleMatrix(3, 1);
        /*
         * 下面这个是自变量为3的一阶偏导数2006.3.1
         */
        d.values[0][0] = 0.5 * (above.valArr[y][x] - below.valArr[y][x]);
        d.values[1][0] = 0.5 * (current.valArr[y + 1][x] - current.valArr[y - 1][x]);
        d.values[2][0] = 0.5 * (current.valArr[y][x + 1] - current.valArr[y][x - 1]);

        SimpleMatrix b = (SimpleMatrix) d.clone();
        b.negate();
        // Solve: A x = b
        H.solveLinear(b);
        ref.val = b.dot(d);
        return (b);
    }

    private boolean isTooEdgelike(ImageMap space, int x, int y, double r) {
        double D_xx, D_yy, D_xy;

        // Calculate the Hessian H elements [ D_xx, D_xy ; D_xy , D_yy ]
        /*
         * 感谢徐小明的推导 2006.02.26.于重大数字图像实验室 d_xx = d_f(x+1) - d_f( x );0 d_f(x+1) = f(x+1) - f( x ); 1 d_f(x) = f(x) - f(
         * x-1 );2 将 1， 2式代入0式得 d_xx = f(x+1) + f(x-1) - 2 * f(x); 对于d_xy = ( d_f( x , y+1 ) - d_f( x, y-1 ) ) * 0.5; 0
         * d_f(x,y+1) = (f(x+1,y+1) - f(x-1,y+1)) * 0.5; 1 d_f(x,y-1) = (f(x+1,y-1) - f(x-1,y-1)) * 0.5; 2 将1，2代入 0 式
         * (f(x+1,y+1)+f(x+1,y-1)-f(x-1,y+1)-f(x-1,y-1)) * 0.25
         */
        D_xx = space.valArr[y + 1][x] + space.valArr[y - 1][x] - 2.0 * space.valArr[y][x];
        D_yy = space.valArr[y][x + 1] + space.valArr[y][x - 1] - 2.0 * space.valArr[y][x];
        D_xy = 0.25 * ((space.valArr[y + 1][x + 1] - space.valArr[y + 1][x - 1]) - (space.valArr[y - 1][x + 1] - space.valArr[y - 1][x - 1]));

        // page 13 in Lowe's paper
        double TrHsq = D_xx + D_yy;
        TrHsq *= TrHsq;
        double DetH = D_xx * D_yy - (D_xy * D_xy);

        double r1sq = (r + 1.0);
        r1sq *= r1sq;

        // BUG: this can invert < to >, uhh: if ((TrHsq * r) < (DetH * r1sq))
        if ((TrHsq / DetH) < (r1sq / r)) {
            /*
             * Console.WriteLine ("{0} {1} {2} {3} {4} # EDGETEST", y, x, (TrHsq * r), (DetH * r1sq), (TrHsq / DetH) /
             * (r1sq / r));
             */
            return (false);
        }

        return (true);
    }

    // 1,302,57,4,547,187

    boolean localizeIsWeak(ScalePoint point, int steps, int[][] processed) {
        boolean needToAdjust = true;
        int adjusted = steps;
        while (needToAdjust) {
            int x = point.x;
            int y = point.y;

            // Points we cannot say anything about, as they lie on the border
            // of the scale space
            if (point.level <= 0 || point.level >= (spaces.length - 1)) return (true);

            ImageMap space = spaces[point.level];
            if (x <= 0 || x >= (space.xDim - 1)) return (true);
            if (y <= 0 || y >= (space.yDim - 1)) return (true);

            RefDouble dp = new RefDouble();
            SimpleMatrix adj = getAdjustment(point, point.level, x, y, dp);

            // Get adjustments and check if we require further adjustments due
            // to pixel level moves. If so, turn the adjustments into real
            // changes and continue the loop. Do not adjust the plane, as we
            // are usually quite low on planes in thie space and could not do
            // further adjustments from the top/bottom planes.
            double adjS = adj.values[0][0];
            double adjY = adj.values[1][0];
            double adjX = adj.values[2][0];
            if (Math.abs(adjX) > 0.5 || Math.abs(adjY) > 0.5) {
                // Already adjusted the last time, give up
                if (adjusted == 0) {
                    // Console.WriteLine ("too many adjustments, returning");
                    return (true);
                }

                adjusted -= 1;

                // Check that just one pixel step is needed, otherwise discard
                // the point
                // 用平方做它的偏离程度
                // 亚像素的应用意义2006年3月3日
                double distSq = adjX * adjX + adjY * adjY;
                if (distSq > 2.0) return (true);

                // 如果不满足边缘中心准则：若（adjX,adjY）不在[-0.5,0.5]之间
                // 则以（ x + 1 ）或 （x - 1) 为新的展开点

                point.x = (int) (point.x + adjX + 0.5);
                point.y = (int) (point.y + adjY + 0.5);
                // point.Level = (int) (point.Level + adjS + 0.5);

                /*
                 * Console.WriteLine ("moved point by ({0},{1}: {2}) to ({3},{4}: {5})", adjX, adjY, adjS, point.X,
                 * point.Y, point.Level);
                 */
                continue;
            }

            if (processed[point.x][point.y] != 0) return (true);

            processed[point.x][point.y] = 1;

            // Save final sub-pixel adjustments.
            PointLocalInformation local = new PointLocalInformation(adjS, adjX, adjY);
            // local.DValue = dp;
            local.dValue = space.valArr[point.y][point.x] + 0.5 * dp.val;
            point.local = local;

            needToAdjust = false;
        }
        return (false);
    }

}
