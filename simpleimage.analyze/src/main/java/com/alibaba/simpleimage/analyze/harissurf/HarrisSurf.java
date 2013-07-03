package com.alibaba.simpleimage.analyze.harissurf;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.simpleimage.analyze.ModifiableConst;
import com.alibaba.simpleimage.analyze.harris.Corner;
import com.alibaba.simpleimage.analyze.harris.HarrisFast;

/**
 * 结合了Harris Corner Detector 以及 Surf Haarwave Descriptor 削弱了对尺度缩放的抵抗性，降低特征维度从128到64，增加特征提取的效率
 * 
 * @author hui.xueh
 */
public class HarrisSurf {

    private IntegralImage           mIntegralImage;
    private double                  sigma;
    private double                  k;
    int                             spacing;
    private int[][]                 input;
    private int                     width;
    private int                     height;
    private List<SURFInterestPoint> interestPoints;
    private static float[][]        guassian81_25;

    static {
        int radius = 13;
        guassian81_25 = new float[radius][radius];
        for (int j = 0; j < radius; j++)
            for (int i = 0; i < radius; i++)
                guassian81_25[i][j] = (float) gaussian(i, j, 2.5F);
    }

    public List<SURFInterestPoint> getInterestPoints() {
        return interestPoints;
    }

    public HarrisSurf(BufferedImage image){
        this(image, 1.2, 0.06, 4);
    }

    /**
     * @param image ，输入图像
     * @param sigma ，高斯平滑的参数
     * @param k ，Harris Corner计算的参数
     * @param spacing ，邻近点的最小距离，该范围内只取一个强度最大的特征点
     */
    public HarrisSurf(BufferedImage image, double sigma, double k, int spacing){
        this.sigma = sigma;
        this.k = k;
        this.spacing = spacing;

        width = image.getWidth();
        height = image.getHeight();
        input = new int[width][height];
        for (int i = 0; i < width - 1; i++) {
            for (int j = 0; j < height - 1; j++) {
                input[i][j] = rgb2gray(image.getRGB(i, j));
            }
        }
        mIntegralImage = new IntegralImage(image);
        interestPoints = new ArrayList<SURFInterestPoint>();
    }

    public static void joinsFilter(Map<SURFInterestPoint, SURFInterestPoint> matchMap) {
        Iterator<Entry<SURFInterestPoint, SURFInterestPoint>> iter = matchMap.entrySet().iterator();
        Map<SURFInterestPoint, Integer> map = new HashMap<SURFInterestPoint, Integer>();
        while (iter.hasNext()) {
            Entry<SURFInterestPoint, SURFInterestPoint> e = iter.next();
            Integer kp1V = map.get(e.getKey());
            int lI = (kp1V == null) ? 0 : (int) kp1V;
            map.put(e.getKey(), lI + 1);
            Integer kp2V = map.get(e.getValue());
            int rI = (kp2V == null) ? 0 : (int) kp2V;
            map.put(e.getValue(), rI + 1);
        }
        iter = matchMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<SURFInterestPoint, SURFInterestPoint> e = iter.next();
            Integer kp1V = map.get(e.getKey());
            Integer kp2V = map.get(e.getValue());
            if (kp1V > 1 || kp2V > 1) iter.remove();
        }
    }

    /**
     * 给点一组匹配的特征点对，使用几何位置过滤其中不符合的点对，目前的策略包括： 1、特征主方向差别 2、斜率一致性
     * 
     * @param matchMap
     */
    public static void geometricFilter(Map<SURFInterestPoint, SURFInterestPoint> matchMap, int w, int h) {
        if (matchMap.size() <= 1) return;
        int arcStep = ModifiableConst.getSolpeArcStep();
        int[] ms = new int[90 / arcStep + 1]; // 用数据的索引拂过每个度数的key，不使用map来保存，性能优化

        Iterator<Entry<SURFInterestPoint, SURFInterestPoint>> iter = matchMap.entrySet().iterator();
        // Map<Long, Integer> voteMap = new HashMap<Long, Integer>();
        int max_vote_count = 0;
        long max_vote = 0;

        while (iter.hasNext()) {
            Entry<SURFInterestPoint, SURFInterestPoint> entry = iter.next();
            SURFInterestPoint fromPoint = entry.getKey();
            SURFInterestPoint toPoint = entry.getValue();
            if (Math.abs(toPoint.getOrientation() - fromPoint.getOrientation()) > 0.1) {
                iter.remove();
            } else {

                double r = Math.atan((toPoint.getY() + h - fromPoint.getY()) / (toPoint.getX() + w - fromPoint.getX()))
                           * 360 / (2 * Math.PI);
                if (r < 0) r += 90;
                int idx = (int) r / arcStep; // 取整

                ms[idx] = ms[idx] + 1;
                if (ms[idx] > max_vote_count) {
                    max_vote_count = ms[idx];
                    max_vote = idx;
                }
            }
        }

        iter = matchMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<SURFInterestPoint, SURFInterestPoint> entry = iter.next();
            SURFInterestPoint fromPoint = entry.getKey();
            SURFInterestPoint toPoint = entry.getValue();
            double r = Math.atan((toPoint.getY() + h - fromPoint.getY()) / (toPoint.getX() + w - fromPoint.getX()))
                       * 360 / (2 * Math.PI);
            if (r < 0) r += 90;
            int idx = (int) r / arcStep; // 取整
            if (idx != max_vote) iter.remove();
        }

    }

    /**
     * 特征检测，使用harris corner detector
     * 
     * @return
     */
    public List<Corner> detectInterestPoints() {
        HarrisFast hf = new HarrisFast(input, width, height, mIntegralImage);
        hf.filter(sigma, k, spacing);
        return hf.corners;
    }

    /**
     * 特征描述，在已输入的角点上提取surf descriptor
     * 
     * @param corners
     */
    public void getDescriptions(List<Corner> corners, boolean brootsift) {
        for (Corner c : corners) {
            SURFInterestPoint sp = new SURFInterestPoint(c.getX(), c.getY(), 1, (int) c.getH());
            getOrientation(sp);
            // System.out.println(sp.getOrientation());
            getMDescriptor(sp, true, brootsift);
            // System.out.println(sp.getDescriptorString());
            interestPoints.add(sp);
        }
    }

    /**
     * 灰度化
     * 
     * @param srgb
     * @return
     */
    private static int rgb2gray(int srgb) {
        int r = (srgb >> 16) & 0xFF;
        int g = (srgb >> 8) & 0xFF;
        int b = srgb & 0xFF;
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    /**
     * 计算主方向
     * 
     * @param input
     */
    private void getOrientation(SURFInterestPoint input) {
        double gauss;
        float scale = input.getScale();

        int s = (int) Math.round(scale);
        int r = (int) Math.round(input.getY());
        int c = (int) Math.round(input.getX());

        List<Double> xHaarResponses = new ArrayList<Double>();
        List<Double> yHaarResponses = new ArrayList<Double>();
        List<Double> angles = new ArrayList<Double>();

        // calculate haar responses for points within radius of 6*scale
        for (int i = -6; i <= 6; ++i) {
            for (int j = -6; j <= 6; ++j) {
                if (i * i + j * j < 36) {
                    gauss = GaussianConstants.Gauss25[Math.abs(i)][Math.abs(j)];
                    double xHaarResponse = gauss * haarX(r + j * s, c + i * s, 4 * s);
                    double yHaarResponse = gauss * haarY(r + j * s, c + i * s, 4 * s);
                    xHaarResponses.add(xHaarResponse);
                    yHaarResponses.add(yHaarResponse);
                    angles.add(getAngle(xHaarResponse, yHaarResponse));
                }
            }
        }

        // calculate the dominant direction
        float sumX = 0, sumY = 0;
        float ang1, ang2, ang;
        float max = 0;
        float orientation = 0;

        // loop slides pi/3 window around feature point
        for (ang1 = 0; ang1 < 2 * Math.PI; ang1 += 0.15f) {
            ang2 = (float) (ang1 + Math.PI / 3.0f > 2 * Math.PI ? ang1 - 5.0f * Math.PI / 3.0f : ang1 + Math.PI / 3.0f);
            sumX = sumY = 0;
            for (int k = 0; k < angles.size(); k++) {
                ang = angles.get(k).floatValue();

                if (ang1 < ang2 && ang1 < ang && ang < ang2) {
                    sumX += xHaarResponses.get(k).floatValue();
                    sumY += yHaarResponses.get(k).floatValue();
                } else if (ang2 < ang1 && ((ang > 0 && ang < ang2) || (ang > ang1 && ang < 2 * Math.PI))) {
                    sumX += xHaarResponses.get(k).floatValue();
                    sumY += yHaarResponses.get(k).floatValue();
                }
            }

            if (sumX * sumX + sumY * sumY > max) {
                max = sumX * sumX + sumY * sumY;
                orientation = (float) getAngle(sumX, sumY);
            }
        }
        input.setOrientation(orientation);
    }

    private float haarX(int row, int column, int s) {
        return ImageTransformUtils.BoxIntegral(mIntegralImage, row - s / 2, column, s, s / 2) - 1
               * ImageTransformUtils.BoxIntegral(mIntegralImage, row - s / 2, column - s / 2, s, s / 2);
    }

    private float haarY(int row, int column, int s) {
        return ImageTransformUtils.BoxIntegral(mIntegralImage, row, column - s / 2, s / 2, s) - 1
               * ImageTransformUtils.BoxIntegral(mIntegralImage, row - s / 2, column - s / 2, s / 2, s);
    }

    private static double getAngle(double xHaarResponse, double yHaarResponse) {
        if (xHaarResponse >= 0 && yHaarResponse >= 0) return Math.atan(yHaarResponse / xHaarResponse);

        if (xHaarResponse < 0 && yHaarResponse >= 0) return Math.PI - Math.atan(-yHaarResponse / xHaarResponse);

        if (xHaarResponse < 0 && yHaarResponse < 0) return Math.PI + Math.atan(yHaarResponse / xHaarResponse);

        if (xHaarResponse >= 0 && yHaarResponse < 0) return 2 * Math.PI - Math.atan(-yHaarResponse / xHaarResponse);

        return 0;
    }

    /**
     * 获取描述子
     * 
     * @param point
     * @param upright ，是否采用方向归一化，影响到对旋转的抵抗性
     */
    private void getMDescriptor(SURFInterestPoint point, boolean upright, boolean brootsift) {
        int y, x, count = 0;
        int sample_x, sample_y;
        double scale, dx, dy, mdx, mdy;//, co = 1F, si = 0F;
        float desc[] = new float[64];
        double gauss_s1 = 0.0D, gauss_s2 = 0.0D;//, xs = 0.0D, ys = 0.0D;
        double rx = 0.0D, ry = 0.0D, rrx = 0.0D, rry = 0.0D, len = 0.0D;
        int i = 0,  j = 0; //ix = 0,jx = 0;

        float cx = -0.5f, cy = 0.0f; // Subregion centers for the 4x4 gaussian
                                     // weighting

        scale = point.getScale();
        x = Math.round(point.getX());
        y = Math.round(point.getY());
        // System.out.println("x = " + point.getX() + ", y = " + point.getY());
        // System.out.println("x = " + x + ", y = " + y);
        // if (!upright) {
        // co = Math.cos(point.getOrientation());
        // si = Math.sin(point.getOrientation());
        // }
        // System.out.println("co = " + co + ", sin = " + si);
        i = -8;
        // Calculate descriptor for this interest point
        // Area of size 24 s x 24 s
        // ***********************************************
        while (i < 12) {
            j = -8;
            i = i - 4;

            cx += 1.0F;
            cy = -0.5F;

            while (j < 12) {
                dx = dy = mdx = mdy = 0.0F;
                cy += 1.0F;

                j = j - 4;

//                ix = i + 5;
//                jx = j + 5;

                // if (!upright) {
                // xs = Math.round(x + (-jx * scale * si + ix * scale * co));
                // ys = Math.round(y + (jx * scale * co + ix * scale * si));
                // } else {
                // xs = x;
                // ys = y;
                // }

                for (int k = i; k < i + 9; ++k) {
                    for (int l = j; l < j + 9; ++l) {
                        // Get coords of sample point on the rotated axis

                        // sample_x = (int)Math.round(x + (-1D * l * scale * si
                        // + k * scale * co));
                        // sample_y = (int)Math.round(y + ( l * scale * co + k *
                        // scale * si));
                        sample_x = x + k;
                        sample_y = y + l;
                        // System.out.println(k + ", " + l);
                        // Get the gaussian weighted x and y responses
                        // gauss_s1 = gaussian(xs - sample_x, ys - sample_y,
                        // 2.5F * scale);
                        gauss_s1 = guassian81_25[Math.abs(k)][Math.abs(l)];
                        rx = haarX(sample_y, sample_x, (int) (2 * Math.round(scale)));
                        ry = haarY(sample_y, sample_x, (int) (2 * Math.round(scale)));

                        // Get the gaussian weighted x and y responses on
                        // rotated axis
                        // rrx = gauss_s1 * (-rx*si + ry*co);
                        // rry = gauss_s1 * (rx*co + ry*si);
                        rrx = gauss_s1 * (ry);
                        rry = gauss_s1 * (rx);

                        dx += rrx;
                        dy += rry;

                        mdx += Math.abs(rrx);
                        mdy += Math.abs(rry);
                    }
                }

                // Add the values to the descriptor vector
                gauss_s2 = gaussian(cx - 2.0f, cy - 2.0f, 1.5f);

                // Casting from a double to a float, might be a terrible idea
                // but doubles are expensive
                desc[count++] = (float) (dx * gauss_s2);
                desc[count++] = (float) (dy * gauss_s2);

                desc[count++] = (float) (mdx * gauss_s2);
                desc[count++] = (float) (mdy * gauss_s2);

                // Accumulate length for vector normalisation
                len += (dx * dx + dy * dy + mdx * mdx + mdy * mdy) * (gauss_s2 * gauss_s2);

                j += 9;
            }
            i += 9;
        }

        len = Math.sqrt(len);

        for (i = 0; i < desc.length; i++) {
            desc[i] /= len;
        }
        // RootSift from [1]
        // [1] R. Arandjelović, A. Zisserman.
        // Three things everyone should know to improve object retrieval. CVPR2012.
        // -> rootsift= sqrt( sift / sum(sift) );
        if (brootsift) {
            float sum = 0.0f;
            for (float f : desc)
                sum += Math.abs(f);

            for (i = 0; i < desc.length; i++) {
                if (desc[i] < 0) desc[i] = (float) -Math.sqrt(-desc[i] / sum);
                else desc[i] = (float) Math.sqrt(desc[i] / sum);
            }

        }
        point.setDescriptor(desc);
        // for ( double v : desc ){
        // System.out.printf("%.7f",v);
        // System.out.print(",");
        // }
        // System.out.println();
    }

    /**
     * 采用高斯分布作为距离的权重因子
     * 
     * @param x
     * @param y
     * @param sig
     * @return
     */
    private static double gaussian(double x, double y, double sig) {
        return (1.0f / (2.0f * Math.PI * sig * sig)) * Math.exp(-(x * x + y * y) / (2.0f * sig * sig));
    }

    public static Map<SURFInterestPoint, SURFInterestPoint> match(List<SURFInterestPoint> src,
                                                                  List<SURFInterestPoint> dest) {

        Map<SURFInterestPoint, SURFInterestPoint> matchMap = new HashMap<SURFInterestPoint, SURFInterestPoint>();
        for (SURFInterestPoint sp : src) {
            double min_dist = Double.MAX_VALUE;
            SURFInterestPoint min_sp = null;
            for (SURFInterestPoint sp2 : dest) {
                double distance = sp.getDistance(sp2);
                if (distance < min_dist) {
                    min_dist = distance;
                    min_sp = sp2;
                }
            }

            matchMap.put(sp, min_sp);
        }
        return matchMap;
    }

    // The Integer-normalized version of the globalKeypoints.
    List<SURFInterestPointN> globalNaturalKeypoints = null;

    public List<SURFInterestPointN> getGlobalNaturalInterestPoints() {
        if (globalNaturalKeypoints != null) return (globalNaturalKeypoints);

        if (this.interestPoints == null) throw (new IllegalArgumentException("No interestPoints generated yet."));
        globalNaturalKeypoints = new ArrayList<SURFInterestPointN>();
        for (SURFInterestPoint sp : interestPoints) {
            globalNaturalKeypoints.add(new SURFInterestPointN(sp));
        }
        return (globalNaturalKeypoints);
    }
}
