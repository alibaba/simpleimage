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
 * 绫籓ctave.java鐨勫疄鐜版弿杩帮細琛ㄧず8搴﹂噾瀛楀涓殑涓�涓�︼紝鍗充互灏哄涓哄潗鏍囩殑鏌愪竴灏哄涓婄殑閭ｄ釜8搴�
 * 
 * @author axman 2013-6-27 涓婂崍11:30:08
 */
public class OctaveSpace {

    OctaveSpace               down;        // down鎸囩殑鏄互灏哄涓哄潗鏍囩殑涓婁竴灞傦紝涓嶆槸褰撳墠8搴︾┖闂寸殑涓嶅悓楂樻柉妯＄硦鍥剧墖鐨勬煇涓�灞�
    OctaveSpace               up;
    ImagePixelArray           baseImg;     // 褰撳墠8搴︾┖闂寸殑鍘熷鍥剧墖锛岀敱涓�︾┖闂寸殑鏌愬眰锛堥粯璁ゅ�掓暟绗笁灞傦級鑾峰彇
    public float              baseScale;   // 褰撳墠8搴﹀闂村湪濉斾腑鐨勫師濮嬪昂搴�
    public ImagePixelArray[]  smoothedImgs; // 鐢ㄤ笉鍚屾ā绯婂洜瀛愭ā绯婂悗鐨勫浘璞￠泦鍚�
    public ImagePixelArray[]  diffImags;   // 宸垎鍥鹃泦

    private ImagePixelArray[] magnitudes;
    private ImagePixelArray[] directions;

    /**
     * @return 杩斿洖涓嬩竴涓�︾┖闂寸殑鍘熷鍩哄噯鍥捐薄 @see page5 of "Distinctive Image Features from Scale-Invariant featurePoints" (David G.
     * Lowe @January 5, 2004) 楂樻柉鍑芥暟G瀵瑰浘鍍廔鐨勬ā绯婂嚱鏁�L(x,y,蟽) = G(x,y,蟽) 鈭� I(x,y) 楂樻柉宸垎鍑芥暟:D(x,y,蟽) = (G(x,y,k蟽)鈭扜(x,y,蟽))鈭桰(x,y)
     * = L(x,y,k蟽) 鈭� L(x,y,蟽) 瀵逛簬scales骞呭浘璞′骇鐢熻繛缁昂搴︼紝鎺ㄥ鍑� k = 2 ^ (1/s)锛岃鏂囦腑榛樿 scales涓�屾墍浠ユ�诲叡鏈�呭浘鐗囷紝瀹冧滑鐨勫昂搴﹀簲璇ヤ负
     * 1蟽锛�26蟽锛�59蟽锛�0蟽锛�52蟽锛�17蟽
     * 鍊掓暟绗笁骞呮濂藉彂鐢熶竴涓�嶇殑閫掕繘锛屾妸瀹冧綔涓轰笅涓�涓�︾┖闂寸殑绗竴骞呭浘鐗囷紝姝ｅソ淇濊瘉宸垎閲戝瓧濉旂殑灏哄害绌洪棿鐨勮繛缁�э紝鍏跺疄瀵逛簬浠讳箟scales,length-2涓哄浐瀹氱殑浣嶇疆锛屽洜涓烘�婚暱搴︿负s+3,鍓嶉潰鍘绘帀涓�涓師濮嬪浘鐗囷紝
     * 鍙湁length-2鐨勬椂鍊� k = 2 ^ (s/s)鎵嶆濂借儗鏃�嶃��
     */
    public ImagePixelArray getLastGaussianImg() {
        if (this.smoothedImgs.length < 2) {
            throw new java.lang.IllegalArgumentException("err: too few gaussian maps.");
        }
        return (this.smoothedImgs[this.smoothedImgs.length - 2]);
    }

    /**
     * 鍦ㄤ竴涓�︾┖闂寸敤涓嶅悓鐨勬ā绯婂洜瀛愭瀯閫犳洿澶氬眰鐨勯珮鏈熸ā绯婂浘灞�,杩欓噷鏄笉鍚屾ā绯婂洜瀛愮殑妯＄硦浣嗘槸灏哄鏄浉鍚岀殑銆�
     * 
     * @param first
     * @param firstScale
     * @param scales
     * @param sigma
     */
    public void makeGaussianImgs(ImagePixelArray base, float baseScale, int scales, float sigma) {

        // 瀵逛簬DOG(宸垎鍥�)鎴戜滑闇�瑕佷竴寮犱互涓婄殑鍥剧墖鎵嶈兘鐢熸垚宸垎鍥撅紝浣嗘槸鏌ユ壘鏋佸�肩偣闇�瑕佺悊澶氬樊鍒嗗浘銆傝buildDiffMaps
        smoothedImgs = new ImagePixelArray[scales + 3];
        // 姣忎竴涓瀬鍊肩偣鏄湪涓夌淮绌洪棿涓瘮杈冭幏寰�,銆�鍗宠鍜屽畠鍛ㄥ洿8涓偣鍜屼笂涓�骞呭搴旂殑9涓偣浠ュ強涓嬩竴骞呭搴旂殑9涓偣锛屽洜姝や负浜嗚幏寰梥cales灞傜偣锛岄偅涔堝湪宸垎楂樻柉閲戝瓧濉斾腑闇�瑕佹湁scales+2骞呭浘鍍�,
        // 鑰屽鏋滃樊鍒嗗浘骞呮暟鏄痵cales+2锛岄偅涔堜竴涓�︾┖闂翠腑鑷冲皯闇�瑕乻cales+2骞�
        this.baseScale = baseScale;
        ImagePixelArray prev = base;
        smoothedImgs[0] = base;

        float w = sigma;
        float kTerm = (float) Math.sqrt(Math.pow(Math.pow(2.0, 1.0 / scales), 2.0) - 1.0);
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
            diffImags[sn] = ImagePixelArray.minus(smoothedImgs[sn + 1], smoothedImgs[sn]);
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
            peaks.addAll(findPeaks4ThreeLayer(below, current, above, level, dogThresh));
            below = current;
        }

        return (peaks);
    }

    /**
     * 绮剧‘鍖栫壒寰佺偣浣嶇疆骞剁敓鎴愭湰鍦板寲淇℃伅浠ュ強杩囪檻韬佺偣
     * 
     * @param peaks
     * @param maximumEdgeRatio
     * @param dValueLowThresh
     * @param scaleAdjustThresh
     * @param relocationMaximum
     * @return
     */

    public ArrayList<ScalePeak> filterAndLocalizePeaks(ArrayList<ScalePeak> peaks, float maximumEdgeRatio,
                                                       float dValueLowThresh, float scaleAdjustThresh,
                                                       int relocationMaximum) {
        ArrayList<ScalePeak> filtered = new ArrayList<ScalePeak>();
        int[][] processedMap = new int[this.diffImags[0].width][this.diffImags[0].height];
        for (ScalePeak peak : peaks) {

            // 鍘婚櫎杈圭紭鐐� @see isTooEdgelike
            if (isTooEdgelike(diffImags[peak.level], peak.x, peak.y, maximumEdgeRatio)) continue;
            // 绮剧‘鍖栫壒寰佺偣浣嶇疆 @see localizeIsWeak
            if (localizeIsWeak(peak, relocationMaximum, processedMap)) continue;

            if (Math.abs(peak.local.scaleAdjust) > scaleAdjustThresh) continue;

            if (Math.abs(peak.local.dValue) <= dValueLowThresh) continue;

            filtered.add(peak);
        }
        return filtered;
    }

    /**
     * 鍏堝皢宸垎鍥句笂姣忎釜鐐圭殑鏂瑰悜鍜岄�掑害璁＄畻鍑烘潵锛� 棰勮绠楃殑鎬讳綋鎬ц兘姣旂粺璁″湪鑼冨洿鍐呯殑鐐瑰啀璁＄畻鐨勯儴浣撴�ц兘瑕侀珮锛屽洜涓虹壒寰佺偣鍒嗗竷杈冨ぇ锛屽畠鍛ㄥ洿鐨勭偣鍙兘琚叾瀹冧腑蹇冪偣澶氭浣跨敤鍒帮紝 濡傛灉缁熻鍦ㄨ寖鍥村唴鍐嶈绠楃殑鐨勮瘽姣忎釜鐐瑰彲鑳借澶氭璁＄畻
     */
    public void pretreatMagnitudeAndDirectionImgs() {

        magnitudes = new ImagePixelArray[this.smoothedImgs.length - 1];// 姊害鐨勬暟缁�
        directions = new ImagePixelArray[this.smoothedImgs.length - 1];// 鏂瑰悜鐨勬暟缁�
        for (int s = 1; s < (this.smoothedImgs.length - 1); s++) {
            magnitudes[s] = new ImagePixelArray(this.smoothedImgs[s].width, this.smoothedImgs[s].height);
            directions[s] = new ImagePixelArray(this.smoothedImgs[s].width, this.smoothedImgs[s].height);
            int w = smoothedImgs[s].width;
            int h = smoothedImgs[s].height;
            for (int y = 1; y < (h - 1); ++y) {
                for (int x = 1; x < (w - 1); ++x) {
                    magnitudes[s].data[y * w + x] = (float) Math.sqrt(Math.pow(smoothedImgs[s].data[y * w + x + 1]
                                                                                       - smoothedImgs[s].data[y * w + x
                                                                                                              - 1],
                                                                               2.0f)
                                                                      + Math.pow(smoothedImgs[s].data[(y + 1) * w + x]
                                                                                         - smoothedImgs[s].data[(y - 1)
                                                                                                                * w + x],
                                                                                 2.0f));

                    directions[s].data[y * w + x] = (float) Math.atan2(smoothedImgs[s].data[(y + 1) * w + x]
                                                                               - smoothedImgs[s].data[(y - 1) * w + x],
                                                                       smoothedImgs[s].data[y * w + x + 1]
                                                                               - smoothedImgs[s].data[y * w + x - 1]);
                }
            }
        }
    }

    public ArrayList<FeaturePoint> makeFeaturePoints(ArrayList<ScalePeak> localizedPeaks, float peakRelThresh,
                                                     int scaleCount, float octaveSigma) {
        ArrayList<FeaturePoint> featurePoints = new ArrayList<FeaturePoint>();
        for (ScalePeak sp : localizedPeaks) {
            ArrayList<FeaturePoint> thisPointKeys = makeFeaturePoint(this.baseScale, sp, peakRelThresh, scaleCount,
                                                                     octaveSigma);
            thisPointKeys = createDescriptors(thisPointKeys, magnitudes[sp.level], directions[sp.level], 2.0f, 4, 8,
                                              0.2f);
            for (FeaturePoint fp : thisPointKeys) {
                if (!fp.hasFeatures) {
                    throw new java.lang.IllegalStateException("should not happen");
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

    private ArrayList<FeaturePoint> makeFeaturePoint(float imgScale, ScalePeak point, float peakRelThresh,
                                                     int scaleCount, float octaveSigma) {

        // 璁＄畻鐗瑰緛鐐圭殑鐩稿scale,杩欓噷鏄浼板��
        float fpScale = (float) (octaveSigma * Math.pow(2.0, (point.level + point.local.scaleAdjust) / scaleCount));

        // Lowe03, "A gaussian-weighted circular window with a \sigma three
        // times that of the scale of the featurePoints".

        float sigma = 3.0f * fpScale;
        int radius = (int) (3.0 * sigma / 2.0 + 0.5);
        int radiusSq = radius * radius;

        ImagePixelArray magnitude = magnitudes[point.level];
        ImagePixelArray direction = directions[point.level];
        // 纭畾閭荤偣鑼冨洿
        int xMin = Math.max(point.x - radius, 1);
        int xMax = Math.min(point.x + radius, magnitude.width - 1);
        int yMin = Math.max(point.y - radius, 1);
        int yMax = Math.min(point.y + radius, magnitude.height - 1);

        // G(r) = e^{-\frac{r^2}{2 \sigma^2}}
        float gaussianSigmaFactor = 2.0f * sigma * sigma;

        float[] boxes = new float[36]; // 鏋勯�犺鐐归偦鍩熸搴︽柟鍚戠洿鏂瑰浘锛屽皢涓�鍦嗗懆360掳鍒掑垎鎴�涓Ы锛屼粠0掳寮�濮嬫瘡妲介�掑10掳锛屾墍浠ヤ竴鍏辨湁36涓Ы

        for (int y = yMin; y < yMax; ++y) {
            for (int x = xMin; x < xMax; ++x) {
                int relX = x - point.x;// 姹傚崐寰�
                int relY = y - point.y;// 姹傚崐寰�
                if (relX * relX + relY * relY > radiusSq) continue; // 鍕捐偂瀹氱悊

                float gaussianWeight = (float) Math.exp(-((relX * relX + relY * relY) / gaussianSigmaFactor));

                // find the closest bin and add the direction
                int boxIdx = findClosestRotationBox(direction.data[y * direction.width + x]);

                boxes[boxIdx] += magnitude.data[y * magnitude.width + x] * gaussianWeight;
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
        interpolateOrientation(boxes[maxBox == 0 ? (36 - 1) : (maxBox - 1)], boxes[maxBox], boxes[(maxBox + 1) % 36],
                               ref1);

        // 杩欐牱鎵惧埌鐨勪笉姝㈡槸涓や釜鏈�澶х殑鏂瑰悜 @see page 13
        boolean[] boxIsFeaturePoint = new boolean[36];
        for (int b = 0; b < 36; ++b) {
            boxIsFeaturePoint[b] = false;
            if (b == maxBox) {
                boxIsFeaturePoint[b] = true;
                continue;
            }
            if (boxes[b] < (peakRelThresh * ref1.peakValue)) continue;
            int leftI = (b == 0) ? (36 - 1) : (b - 1);
            int rightI = (b + 1) % 36;
            if (boxes[b] <= boxes[leftI] || boxes[b] <= boxes[rightI]) continue; // no local peak
            boxIsFeaturePoint[b] = true;
        }

        ArrayList<FeaturePoint> featurePoints = new ArrayList<FeaturePoint>();

        float oneBoxRad = (float) (2.0f * Math.PI) / 36;

        for (int b = 0; b < 36; ++b) {
            if (boxIsFeaturePoint[b] == false) continue;

            int bLeft = (b == 0) ? (36 - 1) : (b - 1);
            int bRight = (b + 1) % 36;

            RefPeakValueAndDegreeCorrection ref2 = new RefPeakValueAndDegreeCorrection();

            if (interpolateOrientation(boxes[bLeft], boxes[b], boxes[bRight], ref2) == false) {
                throw (new java.lang.IllegalStateException("BUG: Parabola fitting broken"));
            }
            float degree = (float) ((b + ref2.degreeCorrection) * oneBoxRad - Math.PI);
            // 瀹屽叏鍖栧湪 -180 鍒� +180 涔嬮棿

            if (degree < -Math.PI) degree += 2.0 * Math.PI;
            else if (degree > Math.PI) degree -= 2.0 * Math.PI;

            FeaturePoint fp = new FeaturePoint(this.smoothedImgs[point.level], point.x + point.local.fineX,
                                               point.y + point.local.fineY, imgScale, fpScale, degree);
            featurePoints.add(fp);
        }
        return (featurePoints);
    }

    private boolean interpolateOrientation(float left, float middle, float right, RefPeakValueAndDegreeCorrection ref) {
        float a = ((left + right) - 2.0f * middle) / 2.0f;
        ref.degreeCorrection = ref.peakValue = Float.NaN;
        if (a == 0.0) return false;
        float c = (((left - middle) / a) - 1.0f) / 2.0f;
        float b = middle - c * c * a;

        if (c < -0.5 || c > 0.5) throw (new IllegalStateException("InterpolateOrientation: off peak ]-0.5 ; 0.5["));
        ref.degreeCorrection = c;
        ref.peakValue = b;
        return true;
    }

    private void averageBoxes(float[] boxes) {
        // ( 0.4, 0.4, 0.3, 0.4, 0.4 ))
        // 姣忎笁涓仛涓�涓钩鍧囩洿鑷冲畬鎴�
        for (int sn = 0; sn < 4; ++sn) {
            float first = boxes[0];
            float last = boxes[boxes.length - 1];

            for (int sw = 0; sw < boxes.length; ++sw) {
                float cur = boxes[sw];
                float next = (sw == (boxes.length - 1)) ? first : boxes[(sw + 1) % boxes.length];

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
        if (idx == 36) idx = 0;
        return idx;
    }

    private ArrayList<FeaturePoint> createDescriptors(ArrayList<FeaturePoint> featurePoints, ImagePixelArray magnitude,
                                                      ImagePixelArray direction, float considerScaleFactor,
                                                      int descDim, int directionCount, float fvGradHicap) {

        if (featurePoints.size() <= 0) return (featurePoints);
        // 閫氳繃灏哄害鍥犲瓙鎵惧埌鍛ㄥ洿鎵�鍖呭惈鐨勫儚绱�
        considerScaleFactor *= featurePoints.get(0).scale;
        float dDim05 = ((float) descDim) / 2.0f;

        int radius = (int) (((descDim + 1.0) / 2) * Math.sqrt(2.0f) * considerScaleFactor + 0.5f);

        ArrayList<FeaturePoint> survivors = new ArrayList<FeaturePoint>();

        float sigma2Sq = 2.0f * dDim05 * dDim05;// 2 * sigma ^2鏄珮鏂嚱鏁癳鎸囨暟涓婄殑鍒嗘瘝涓婄殑鏁�
        for (FeaturePoint fp : featurePoints) {
            float angle = -fp.orientation;// 鏃嬭浆-angle鎷夊埌姘村钩鏂瑰悜涓婃潵

            fp.createVector(descDim, descDim, directionCount);

            // 鏃嬭浆angle搴︾殑鍧愭爣
            for (int y = -radius; y < radius; ++y) {
                for (int x = -radius; x < radius; ++x) {

                    float yR = (float) (Math.sin(angle) * x + Math.cos(angle) * y);
                    float xR = (float) (Math.cos(angle) * x - Math.sin(angle) * y);

                    // 浣夸粬瀹氫箟鍦ㄦ弿杩板櫒鐨勭含搴︿箣鍐�
                    yR /= considerScaleFactor; // yR = yR / considerScaleFactor
                    xR /= considerScaleFactor; // yR = yR / considerScaleFactor

                    // 浣胯鐐逛笉瓒呭嚭鎻忚堪鍣ㄧ殑鑼冨洿
                    if (yR >= (dDim05 + 0.5) || xR >= (dDim05 + 0.5) || xR <= -(dDim05 + 0.5) || yR <= -(dDim05 + 0.5)) continue;
                    // 璁＄畻鍏抽敭鐐瑰拰鍔犳潈鐨勭偣鐨勫叿浣搙浣嶇疆
                    int currentX = (int) (x + fp.x + 0.5);
                    // 璁＄畻鍏抽敭鐐瑰拰鍔犳潈鐨勭偣鐨勫叿浣搚浣嶇疆

                    int currentY = (int) (y + fp.y + 0.5);
                    // 杩欎繚璇佸畠鍦ㄨ寖鍥翠箣鍐呴儴鍑哄幓
                    if (currentX < 1 || currentX >= (magnitude.width - 1) || currentY < 1
                        || currentY >= (magnitude.height - 1)) continue;
                    // 楂樻柉鏉冮噸鐨勮绠�
                    float magW = (float) Math.exp(-(xR * xR + yR * yR) / sigma2Sq)
                                 * magnitude.data[currentY * magnitude.width + currentX];
                    yR += dDim05 - 0.5;
                    xR += dDim05 - 0.5;

                    // 鍦ㄤ袱涓偣涔嬮棿鏈夐樁璺冪殑鏃跺�欓兘鍙互鐢ㄦ彃鍊�
                    int[] xIdx = new int[2];
                    int[] yIdx = new int[2];
                    int[] dirIdx = new int[2]; // 姣忎釜鐐圭殑鍧愭爣鐨刼rientation绱㈠紩 [0] 鏂瑰悜鐨勫�� [1]鏄偅涓柟鍚�
                    float[] xWeight = new float[2];
                    float[] yWeight = new float[2];
                    float[] dirWeight = new float[2];// 鏂瑰悜涓�
                    // 鍙兘鍦ㄥ仛鎻掑��
                    if (xR >= 0) {
                        xIdx[0] = (int) xR;
                        xWeight[0] = (1.0f - (xR - xIdx[0]));
                    }
                    if (yR >= 0) {
                        yIdx[0] = (int) yR;
                        yWeight[0] = (1.0f - (yR - yIdx[0]));
                    }

                    if (xR < (descDim - 1)) {
                        xIdx[1] = (int) (xR + 1.0);
                        xWeight[1] = xR - xIdx[1] + 1.0f;
                    }
                    if (yR < (descDim - 1)) {
                        yIdx[1] = (int) (yR + 1.0);
                        yWeight[1] = yR - yIdx[1] + 1.0f;
                    }
                    // end 鍙兘鍦ㄥ仛鎻掑��

                    // 鏃嬭浆瑙掑害鍒癴eaturePoint鐨勫潗鏍囦笅鏉ワ紝骞朵笖鐢╗ -pi : pi ] 鏉ヨ〃绀�
                    float dir = direction.data[currentY * direction.width + currentX] - fp.orientation;
                    if (dir <= -Math.PI) dir += Math.PI;
                    if (dir > Math.PI) dir -= Math.PI;
                    // 缁熶竴鍦ㄥ垎鐫�涓叓涓柟鍚戜笂鏉�
                    float idxDir = (float) ((dir * directionCount) / (2.0 * Math.PI));// directionCount/8涓烘瘡涓�涓害鏁版湁鍑犱釜鏂瑰悜锛岀劧鍚�
                                                                                      // dir灏辩粺涓�鍒颁竴鑷崇殑鏂瑰悜涓婃潵浜�
                    if (idxDir < 0.0) idxDir += directionCount;
                    dirIdx[0] = (int) idxDir;
                    dirIdx[1] = (dirIdx[0] + 1) % directionCount; // 涓嬩竴涓柟鍚�
                    dirWeight[0] = 1.0f - (idxDir - dirIdx[0]); // 鍜屼笅涓�涓柟鍚戞墍宸殑鍊�
                    dirWeight[1] = idxDir - dirIdx[0]; // 鍜屾墍鍦ㄦ柟鍚戞墍宸殑鍊�
                    for (int iy = 0; iy < 2; ++iy) {
                        for (int ix = 0; ix < 2; ++ix) {
                            for (int d = 0; d < 2; ++d) {
                                int idx = xIdx[ix] * fp.yDim * fp.oDim + yIdx[iy] * fp.oDim + dirIdx[d];
                                fp.features[idx] += xWeight[ix] * yWeight[iy] * dirWeight[d] * magW;
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

    // use root sift?
    private void capAndNormalizeFV(FeaturePoint kp, float fvGradHicap) {

        float norm = 0.0f;
        for (int n = 0; n < kp.features.length; ++n)
            norm += Math.pow(kp.features[n], 2.0);// 鎵�鏈夌殑鍊煎钩鏂�

        norm = (float) Math.sqrt(norm);// // feature vector鐨勬ā
        if (norm == 0.0) throw (new IllegalStateException("CapAndNormalizeFV cannot normalize with norm = 0.0"));

        for (int n = 0; n < kp.features.length; ++n) {
            kp.features[n] /= norm;
            if (kp.features[n] > fvGradHicap) kp.features[n] = fvGradHicap;
        }
        // Hicap after normalization
        // for (int n = 0; n < kp.featureVector.length; ++n) {
        // if(kp.featureVector[n] > fvGradHicap)
        // kp.featureVector[n] = fvGradHicap;
        // }

        // Renormalize again
        norm = 0.0f;
        for (int n = 0; n < kp.features.length; ++n)
            norm += Math.pow(kp.features[n], 2.0);
        norm = (float) Math.sqrt(norm);

        for (int n = 0; n < kp.features.length; ++n)
            kp.features[n] /= norm;
    }

    /**
     * 浠庝竴涓�︾┖闂寸殑楂樻柉宸垎鍥鹃泦鍚堜腑绗簩骞呰捣鍒板埌鏁扮浜屽箙锛岀涓�骞呬笂鐨勭偣鍜屽畠鍛ㄥ洿鐨�偣浠ュ強涓婁竴骞呭搴斾綅缃殑9涓偣鍜屼笅涓�骞呭搴斾綅缃殑鐐硅繘琛屾瘮杈冿紝鐪嬫槸鍚︽槸鏈�澶ф垨鏈�灏忓�笺�� 鎵�浠ョО涓篢hreeLeve
     * 
     * @param below
     * @param current
     * @param above
     * @param curLev
     * @param dogThresh
     * @return
     */
    private ArrayList<ScalePeak> findPeaks4ThreeLayer(ImagePixelArray below, ImagePixelArray current,
                                                      ImagePixelArray above, int curLev, float dogThresh) {
        ArrayList<ScalePeak> peaks = new ArrayList<ScalePeak>();

        for (int y = 1; y < (current.height - 1); ++y) {
            for (int x = 1; x < (current.width - 1); ++x) {
                RefCheckMark ref = new RefCheckMark();
                ref.isMin = true;
                ref.isMax = true;
                float c = current.data[x + y * current.width]; // 浣滀负涓��

                if (Math.abs(c) <= dogThresh) continue; // 鏈�灏忓�煎皬浜巇ogThresh鐩存帴杩囪檻锛岄槻姝㈠ぇ鐗囪楂樻湡妯＄硦鍚庣殑浣庡�肩偣琚�変腑

                checkMinMax(current, c, x, y, ref, true);
                checkMinMax(below, c, x, y, ref, false);
                checkMinMax(above, c, x, y, ref, false);
                if (ref.isMin == false && ref.isMax == false) continue;
                peaks.add(new ScalePeak(x, y, curLev));
            }
        }
        return peaks;
    }

    private void checkMinMax(ImagePixelArray layer, float c, int x, int y, RefCheckMark ref, boolean isCurrentLayer) {

        if (layer == null) return;

        if (ref.isMin) {
            if (layer.data[(y - 1) * layer.width + x - 1] <= c // // 宸︿笂瑙�
                || layer.data[y * layer.width + x - 1] <= c // 宸﹁竟
                || layer.data[(y + 1) * layer.width + x - 1] <= c // 宸︿笅
                || layer.data[(y - 1) * layer.width + x] <= c // 涓婅竟
                || (isCurrentLayer ? false : (layer.data[y * layer.width + x] < c))// 涓棿鐐癸紝濡傛灉鏄綋鍓嶅眰鐩存帴涓篺alse(鑷繁),涓嶆槸褰撳墠灞傚簲璇ュ皬浜�,娌℃湁绛変簬鐨勬潯浠�
                || layer.data[(y + 1) * layer.width + x] <= c // 涓嬭竟
                || layer.data[(y - 1) * layer.width + x + 1] <= c // 鍙充笂
                || layer.data[y * layer.width + x + 1] <= c // 鍙宠竟
                || layer.data[(y + 1) * layer.width + x + 1] <= c) // 鍙充笅
            ref.isMin = false;
        }
        if (ref.isMax) {
            if (layer.data[(y - 1) * layer.width + x - 1] >= c // 宸︿笂
                || layer.data[y * layer.width + x - 1] >= c // 宸﹁竟
                || layer.data[(y + 1) * layer.width + x - 1] >= c // 宸︿笅
                || layer.data[(y - 1) * layer.width + x] >= c // 涓婅竟
                || (isCurrentLayer ? false : (layer.data[y * layer.width + x] > c)) // 涓棿鐐癸紝濡傛灉鏄綋鍓嶅眰鐩存帴涓篺alse(鑷繁),涓嶆槸褰撳墠灞傚簲璇ュぇ浜�,娌℃湁绛変簬鐨勬潯浠�
                || layer.data[(y + 1) * layer.width + x] >= c // 涓嬭竟
                || layer.data[(y - 1) * layer.width + x + 1] >= c // 鍙充笂
                || layer.data[y * layer.width + x + 1] >= c // 鍙宠竟
                || layer.data[(y + 1) * layer.width + x + 1] >= c) // 鍙充笅
            ref.isMax = false;
        }
    }

    /**
     * 杈圭紭鐐圭殑鐗圭偣鏄部杈圭紭涓や晶鐨勭偣鐨勪富鏇茬巼寰堝ぇ锛堟洸鐜囧崐寰勫皬锛夛紝鑰屼笌杈圭紭鐩稿垏鐨勪富鏇茬巼灏忥紙鏇茬巼鍗婂緞澶э級锛岃鐧戒簡灏辨槸铏界劧瀹冨拰杈圭紭绾挎梺杈圭殑鐐规瘮杈冨樊鍊煎ぇ锛屼絾娌胯竟缂樼嚎涓婄殑鐐逛箣闂�
     * 宸�煎緢灏忥紝杩欐牱鍦ㄨ竟缂樹笂鐨勪竴鐐瑰拰鍙︿竴鐐圭殑鎻忚堪瀛愬熀鏈槸宸笉澶氫簡锛屽緢闅剧簿纭畾浣嶆槸鍝竴涓偣锛屾墍浠ヨ鍘绘帀銆侤page 12
     * 
     * @param space
     * @param x
     * @param y
     * @param r
     * @return
     */
    private boolean isTooEdgelike(ImagePixelArray space, int x, int y, float r) {
        float d_xx, d_yy, d_xy;

        /*
         * d_xx = d_f(x+1) - d_f( x );0 d_f(x+1) = f(x+1) - f( x ); 1 d_f(x) = f(x) - f( x-1 );2 灏� 1锛� 2寮忎唬鍏�忓緱 d_xx =
         * f(x+1) + f(x-1) - 2 * f(x); 瀵癸拷?d_xy = ( d_f( x , y+1 ) - d_f( x, y-1 ) ) * 0.5; 0 d_f(x,y+1) = (f(x+1,y+1) -
         * f(x-1,y+1)) * 0.5; 1 d_f(x,y-1) = (f(x+1,y-1) - f(x-1,y-1)) * 0.5; 2 灏��ｅ叆 0 寮� *
         * (f(x+1,y+1)+f(x+1,y-1)-f(x-1,y+1)-f(x-1,y-1)) * 0.25
         */

        d_xx = space.data[(y + 1) * space.width + x] + space.data[(y - 1) * space.width + x] - 2.0f
               * space.data[y * space.width + x];
        d_yy = space.data[y * space.width + x + 1] + space.data[y * space.width + x - 1] - 2.0f
               * space.data[y * space.width + x];
        d_xy = 0.25f * ((space.data[(y + 1) * space.width + x + 1] - space.data[(y + 1) * space.width + x - 1]) //
        - (space.data[(y - 1) * space.width + x + 1] - space.data[(y - 1) * space.width + x - 1]));

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
     * 鐢变簬鍥惧儚鏄竴涓鏁ｇ殑绌洪棿锛屾渶鍚庣殑鐗瑰緛鐐圭殑浣嶇疆鐨勫潗鏍囬兘鏄暣鏁帮紝浣嗘槸澶囬�夌殑鏋佸�肩偣鐨勫潗鏍囧苟涓嶄竴瀹氭槸鏁存暟锛屾墍浠ヨ鎶婂綋鍓嶅閫夌殑鏋佸�肩偣鎶曞皠鍒板浘鍍忕殑鍧愭爣涓婏紝闇�瑕佷竴瀹氱殑璋冩暣
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
            if (peak.level <= 0 || peak.level >= (this.diffImags.length - 1)) return (true);

            ImagePixelArray space = diffImags[peak.level];
            if (x <= 0 || x >= (space.width - 1)) return (true);
            if (y <= 0 || y >= (space.height - 1)) return (true);

            RefFloat dp = new RefFloat();
            AdjustedArray adj = getAdjustment(peak, peak.level, x, y, dp);

            float adjS = adj.data[0];
            float adjY = adj.data[1];
            float adjX = adj.data[2];

            if (Math.abs(adjX) > 0.5 || Math.abs(adjY) > 0.5) {
                // 璋冩暣鐨勮寖鍥磋秴杩�5锛屽彲鑳芥槸涓嬩竴涓薄绱狅紝鐩存帴杩囪檻鎺夈��
                if (adjusted == 0) {
                    return (true);
                }
                adjusted -= 1;

                // 鐢ㄥ钩鏂瑰仛瀹冪殑鍋忕绋嬪害
                // 浜氬儚绱犵殑搴旂敤鎰忎箟2006骞���
                float distSq = adjX * adjX + adjY * adjY;
                if (distSq > 2.0) return (true);

                // 濡傛灉涓嶆弧瓒宠竟缂樹腑蹇冨噯鍒欙細鑻ワ紙adjX,adjY锛変笉鍦╗-0.5,0.5]涔嬮棿
                // 鍒欎互锛� x + 1 锛夋垨 锛坸 - 1) 涓烘柊鐨勫睍寮�鐐�
                peak.x = (int) (peak.x + adjX + 0.5);
                peak.y = (int) (peak.y + adjY + 0.5);
                // point.Level = (int) (point.Level + adjS + 0.5);
                continue;
            }

            if (processed[peak.x][peak.y] != 0) return (true);

            processed[peak.x][peak.y] = 1;

            // 淇濆瓨璋冩暣鍚庣殑鍙傛暟浠ヤ究鍚庨潰鐨勮繃铏�
            LocalInfo local = new LocalInfo(adjS, adjX, adjY);
            local.dValue = space.data[peak.y * space.width + peak.x] + 0.5f * dp.val;
            peak.local = local;
            needToAdjust = false;
        }
        return (false);
    }

    private AdjustedArray getAdjustment(ScalePeak peak, int level, int x, int y, RefFloat ref) {

        ref.val = 0.0f;
        if (peak.level <= 0 || peak.level >= (this.diffImags.length - 1)) {
            throw (new IllegalArgumentException("point.Level is not within [bottom-1;top-1] range"));
        }
        ImagePixelArray b = this.diffImags[level - 1]; // below
        ImagePixelArray c = this.diffImags[level]; // current
        ImagePixelArray a = this.diffImags[level + 1]; // above

        AdjustedArray h = new AdjustedArray(3, 3);
        /*
         * 涓嬮潰鏄骞呭浘鍍忓昂搴︾┖闂寸殑涓夊厓鍋忓鏁帮紝璁颁綇鏄昂搴︾┖闂翠笂 鐨勪簩闃惰嚜鍙橀噺涓�勫亸瀵兼暟2006.3.1
         */
        h.data[0] = b.data[y * b.width + x] - 2 * c.data[y * c.width + x] + a.data[y * a.width + x]; // h.data[0][0]

        h.data[h.width] = h.data[1] = 0.25f * (a.data[(y + 1) * a.width + x] //
                                               - a.data[(y - 1) * a.width + x] //
        - (b.data[(y + 1) * b.width + x] - b.data[(y - 1) * b.width + x])); // h.data[0][1]

        h.data[h.width * 2] = h.data[2] = 0.25f * (a.data[y * a.width + x + 1] - a.data[y * a.width + x - 1] //
        - (b.data[y * b.width + x + 1] - b.data[y * b.width + x - 1]));

        h.data[1 * h.width + 1] = c.data[(y - 1) * c.width + x] - 2f * c.data[y * c.width + x]
                                  + c.data[(y + 1) * c.width + x];

        h.data[1 + h.width * 2] = h.data[2 + h.width] = 0.25f * (c.data[(y + 1) * c.width + x + 1] //
                                                                 - c.data[(y + 1) * c.width + x - 1] //
        - (c.data[(y - 1) * c.width + x + 1] //
        - c.data[(y - 1) * c.width + x - 1]));

        h.data[2 * h.width + 2] = c.data[y * c.width + x - 1] - 2 * c.data[y * c.width + x]
                                  + c.data[y * c.width + x + 1];
        AdjustedArray d = new AdjustedArray(1, 3);
        /*
         * 涓嬮潰杩欎釜鏄嚜鍙橀噺涓�勪竴闃跺亸瀵兼暟2006.3.1
         */

        d.data[0] = 0.5f * (a.data[y * a.width + x] - b.data[y * b.width + x]); // d.data[1][0] => d.data[0*width+1] =
                                                                                // d.data[1]
        d.data[1] = 0.5f * (c.data[(y + 1) * c.width + x] - c.data[(y - 1) * c.width + x]);
        d.data[2] = 0.5f * (c.data[y * c.width + x + 1] - c.data[y * c.width + x - 1]);

        AdjustedArray back = d.clone();
        back.negate();
        // 姹傝ВSolve: A x = b
        h.solveLinear(back);
        ref.val = back.dot(d);
        return (back);
    }

    private static class AdjustedArray extends FloatArray implements Cloneable {

        public int width;
        public int height;

        public AdjustedArray(int width, int height){
            this.width = width;
            this.height = height;
            this.data = new float[width * height];
        }

        public AdjustedArray clone() {
            AdjustedArray cp = new AdjustedArray(this.width, this.height);
            System.arraycopy(this.data, 0, cp.data, 0, this.data.length);
            return cp;
        }

        // 鐭╅樀鐨勭偣涔�
        public float dot(AdjustedArray aa) {
            if (this.width != aa.width || this.width != 1 || aa.width != 1) {
                throw (new IllegalArgumentException("Dotproduct only possible for two equal n x 1 matrices"));
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

        // 楂樻柉涓诲厓绱犳秷鍘绘硶
        public void solveLinear(AdjustedArray vec) {
            if (this.width != this.height || this.height != vec.height) {
                throw (new IllegalArgumentException("Matrix not quadratic or vector dimension mismatch"));
            }

            // Gaussian Elimination Algorithm, as described by
            // "Numerical Methods - A Software Approach", R.L. Johnston

            // Forward elimination with partial pivoting
            for (int y = 0; y < (this.height - 1); ++y) {

                int yMaxIndex = y;
                float yMaxValue = Math.abs(data[y * this.width + y]);
                // 鎵惧垪涓渶澶х殑閭ｄ釜鍏冪礌
                for (int py = y; py < this.height; ++py) {
                    if (Math.abs(data[py * this.width + y]) > yMaxValue) {
                        yMaxValue = Math.abs(data[py * this.width + y]);
                        yMaxIndex = py;
                    }
                }

                swapRow(y, yMaxIndex);
                vec.swapRow(y, yMaxIndex);
                // 鍖栨垚涓婁笁瑙掗樀
                for (int py = y + 1; py < this.height; ++py) {
                    float elimMul = data[py * this.width + y] / data[y * this.width + y];
                    for (int x = 0; x < this.width; ++x)
                        data[py * this.width + x] -= elimMul * data[y * this.width + x];
                    vec.data[py * vec.width + 0] -= elimMul * vec.data[y * vec.width + 0];
                }
            }

            // 姹傝В鏀惧叆vec涓�
            // 浠庤繖閲屾垜浠繕鍙互鐪嬪嚭锛屽弬鏁版槸绫荤殑瀵硅薄锛岀瓑浜庢槸浼犲叆绫荤殑鎸囬拡
            for (int y = this.height - 1; y >= 0; --y) {
                float solY = vec.data[y * vec.width + 0];
                for (int x = this.width - 1; x > y; --x)
                    solY -= data[y * this.width + x] * vec.data[x * vec.width + 0];
                vec.data[y * vec.width + 0] = solY / data[y * this.width + y];
            }
        }

        // Swap two rows r1, r2
        private void swapRow(int r1, int r2) {
            if (r1 == r2) return;
            for (int x = 0; x < this.width; ++x) {
                float temp = data[r1 * this.width + x];
                data[r1 * this.width + x] = data[r2 * this.width + x];
                data[r2 * this.width + x] = temp;
            }
        }
    }

    /**
     * 鐢ㄤ簬浼犻�掑紩鐢ㄧ殑鏁版嵁缁撴瀯
     */
    static class RefCheckMark {

        boolean isMin;
        boolean isMax;
    }

    /**
     * 鐢ㄤ簬浼犻�掑紩鐢ㄧ殑鏁版嵁缁撴瀯
     */
    static class RefPeakValueAndDegreeCorrection {

        float peakValue;
        float degreeCorrection;
    }

}

