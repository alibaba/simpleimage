/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.simpleimage.jai.scale;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

import com.alibaba.simpleimage.util.ImageUtils;

/**
 * Lanczos缩路算法实现，参考自ImageMagick http://www.imagemagick.org
 * 
 * @author wendell 2011-3-29 上午11:13:35
 */
public class LanczosScaleOp {

    static final double           WORK_LOAD_FACTOR = 0.265;

    protected double              scaleX;
    protected double              scaleY;
    protected LanczosResizeFilter filter;
    protected double              scaleFactor;
    protected Rectangle           filterRect;
    protected Rectangle           destRect;
    protected int                 destWidth;
    protected int                 destHeight;

    public LanczosScaleOp(double scaleX, double scaleY){
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.filter = new LanczosResizeFilter();
        this.scaleFactor = this.scaleX * this.scaleY;
    }

    public BufferedImage compute(BufferedImage src) {
        this.destWidth = (int) (src.getWidth() * scaleX);
        this.destHeight = (int) (src.getHeight() * scaleY);
        if (destWidth <= 0) {
            destWidth = 1;
        }
        if (destHeight <= 0) {
            destHeight = 1;
        }
        destRect = new Rectangle(destWidth, destHeight);

        if (scaleFactor > WORK_LOAD_FACTOR) {
            filterRect = new Rectangle(destWidth, src.getHeight());
        } else {
            filterRect = new Rectangle(src.getWidth(), destHeight);
        }

        WritableRaster srcRst = src.getRaster();

        ColorModel srcCM = src.getColorModel();
        SampleModel destSM = src.getSampleModel().createCompatibleSampleModel(destRect.width, destRect.height);
        WritableRaster destRst = Raster.createWritableRaster(destSM, destRect.getLocation());

        SampleModel filterSM = src.getSampleModel().createCompatibleSampleModel(filterRect.width, filterRect.height);
        WritableRaster filterRst = Raster.createWritableRaster(filterSM, filterRect.getLocation());

        BufferedImage dest = new BufferedImage(srcCM, destRst, src.isAlphaPremultiplied(), null);

        RasterFormatTag[] formatTags = RasterAccessor.findCompatibleTags(new RenderedImage[] { src }, dest);
        RasterAccessor srcRA = new RasterAccessor(srcRst, srcRst.getBounds(), formatTags[0], src.getColorModel());
        RasterAccessor filterRA = new RasterAccessor(filterRst, filterRect, formatTags[1], src.getColorModel());
        RasterAccessor dstRA = new RasterAccessor(destRst, destRect, formatTags[1], dest.getColorModel());

        switch (dstRA.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                computeRectByte(srcRA, filterRA, dstRA);
                break;
            case DataBuffer.TYPE_USHORT:
                computeRectUShort(srcRA, filterRA, dstRA);
                break;
            case DataBuffer.TYPE_SHORT:
                computeRectShort(srcRA, filterRA, dstRA);
                break;
            case DataBuffer.TYPE_INT:
                computeRectInt(srcRA, filterRA, dstRA);
                break;
            case DataBuffer.TYPE_FLOAT:
                computeRectFloat(srcRA, filterRA, dstRA);
                break;
            case DataBuffer.TYPE_DOUBLE:
                computeRectDouble(srcRA, filterRA, dstRA);
                break;
            default:
                throw new IllegalArgumentException("");
        }

        return dest;
    }

    private void computeRectByte(RasterAccessor src, RasterAccessor mid, RasterAccessor dst) {
        if (scaleFactor > WORK_LOAD_FACTOR) {
            lanczosHorizontalFilterByte(src, mid);
            lanczosVerticalFilterByte(mid, dst);
        } else {
            lanczosVerticalFilterByte(src, mid);
            lanczosHorizontalFilterByte(mid, dst);
        }
    }

    private void computeRectUShort(RasterAccessor src, RasterAccessor mid, RasterAccessor dst) {
        if (scaleFactor > WORK_LOAD_FACTOR) {
             lanczosHorizontalFilterUShort(src, mid);
             lanczosVerticalFilterUShort(mid, dst);
        } else {
             lanczosVerticalFilterUShort(src, mid);
             lanczosHorizontalFilterUShort(mid, dst);
        }
    }

    private void computeRectShort(RasterAccessor src, RasterAccessor mid, RasterAccessor dst) {
        if (scaleFactor > WORK_LOAD_FACTOR) {
             lanczosHorizontalFilterShort(src, mid);
             lanczosVerticalFilterShort(mid, dst);
        } else {
             lanczosVerticalFilterShort(src, mid);
             lanczosHorizontalFilterShort(mid, dst);
        }
    }

    private void computeRectInt(RasterAccessor src, RasterAccessor mid, RasterAccessor dst) {
        if (scaleFactor > WORK_LOAD_FACTOR) {
             lanczosHorizontalFilterInt(src, mid);
             lanczosVerticalFilterInt(mid, dst);
        } else {
             lanczosVerticalFilterInt(src, mid);
             lanczosHorizontalFilterInt(mid, dst);
        }
    }

    private void computeRectFloat(RasterAccessor src, RasterAccessor mid, RasterAccessor dst) {
        if (scaleFactor > WORK_LOAD_FACTOR) {
             lanczosHorizontalFilterFloat(src, mid);
             lanczosVerticalFilterFloat(mid, dst);
        } else {
             lanczosVerticalFilterFloat(src, mid);
             lanczosHorizontalFilterFloat(mid, dst);
        }
    }

    private void computeRectDouble(RasterAccessor src, RasterAccessor mid, RasterAccessor dst) {
        if (scaleFactor > WORK_LOAD_FACTOR) {
             lanczosHorizontalFilterDouble(src, mid);
             lanczosVerticalFilterDouble(mid, dst);
        } else {
             lanczosVerticalFilterDouble(src, mid);
             lanczosHorizontalFilterDouble(mid, dst);
        }
    }

    private void lanczosHorizontalFilterByte(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleX, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        byte[][] srcDataArrays = src.getByteDataArrays();
        byte[][] dstDataArrays = dst.getByteDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = src.getNumBands();
        scale = 1.0 / scale;

        int contributionsNum = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionsNum];
        for (int t = 0; t < contributionsNum; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstWidth = dst.getWidth(), dstHeight = dst.getHeight();
        final int srcWidth = src.getWidth();
        final int dstScanlineStride = dst.getScanlineStride();
        final int srcScanlineStride = src.getScanlineStride();

        for (int x = 0; x < dstWidth; x++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (x + 0.5) / scaleX;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcWidth) ? t2 : srcWidth);
            density = 0.0;

            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = x * channel;
            int tmp5 = ((int) (start - center) + (int) center) * channel;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++] & 0xFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++] & 0xFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++] & 0xFF) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundByte(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundByte(pixel[1]);
                    dstDataArrays[2][tmp4] = ImageUtils.clampRoundByte(pixel[2]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 1) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++] & 0xFF) * contribution[i].weight;
                    }

                    dstDataArrays[0][tmp3] = ImageUtils.clampRoundByte(pixel[0]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 4) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++] & 0xFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++] & 0xFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++] & 0xFF) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp6++] & 0xFF) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundByte(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundByte(pixel[1]);
                    dstDataArrays[2][tmp4++] = ImageUtils.clampRoundByte(pixel[2]);
                    dstDataArrays[3][tmp4] = ImageUtils.clampRoundByte(pixel[3]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }

    private void lanczosVerticalFilterByte(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleY, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        /* Get memory of filter array */
        scale = 1.0 / scale;
        byte[][] srcDataArrays = src.getByteDataArrays();
        byte[][] dstDataArrays = dst.getByteDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = dst.getNumBands();

        int contributionNums = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionNums];
        for (int t = 0; t < contributionNums; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstHeight = dst.getHeight(), dstWidth = dst.getWidth();
        final int srcHeight = src.getHeight();
        final int srcScanlineStride = src.getScanlineStride();
        final int dstScanlineStride = dst.getScanlineStride();

        /* First, we compute rows pixel, then compute columns pixel */
        for (int y = 0; y < dstHeight; y++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (y + 0.5) / scaleY;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcHeight) ? t2 : srcHeight);
            density = 0.0;
            /* Get the value of Sinc */
            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = y * dstScanlineStride;
            int tmp5 = ((int) (center)) * srcScanlineStride + ((int) (start - center)) * srcScanlineStride;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++] & 0xFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++] & 0xFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7] & 0xFF) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundByte(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundByte(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundByte(pixel[2]);

                    tmp5 += channel;
                }
            } else if (channel == 1) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6] & 0xFF) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundByte(pixel[0]);

                    tmp5 += channel;
                }
            } else if (channel == 4) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++] & 0xFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++] & 0xFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7++] & 0xFF) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp7] & 0xFF) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundByte(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundByte(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundByte(pixel[2]);
                    dstDataArrays[3][tmp3++] = ImageUtils.clampRoundByte(pixel[3]);

                    tmp5 += channel;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }

    private void lanczosHorizontalFilterUShort(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleX, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        short[][] srcDataArrays = src.getShortDataArrays();
        short[][] dstDataArrays = dst.getShortDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = src.getNumBands();
        scale = 1.0 / scale;

        int contributionsNum = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionsNum];
        for (int t = 0; t < contributionsNum; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstWidth = dst.getWidth(), dstHeight = dst.getHeight();
        final int srcWidth = src.getWidth();
        final int dstScanlineStride = dst.getScanlineStride();
        final int srcScanlineStride = src.getScanlineStride();

        for (int x = 0; x < dstWidth; x++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (x + 0.5) / scaleX;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcWidth) ? t2 : srcWidth);
            density = 0.0;

            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = x * channel;
            int tmp5 = ((int) (start - center) + (int) center) * channel;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++] & 0xFFFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++] & 0xFFFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++] & 0xFFFF) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundUShort(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundUShort(pixel[1]);
                    dstDataArrays[2][tmp4] = ImageUtils.clampRoundUShort(pixel[2]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 1) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++] & 0xFFFF) * contribution[i].weight;
                    }

                    dstDataArrays[0][tmp3] = ImageUtils.clampRoundUShort(pixel[0]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 4) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++] & 0xFFFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++] & 0xFFFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++] & 0xFFFF) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp6++] & 0xFFFF) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundUShort(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundUShort(pixel[1]);
                    dstDataArrays[2][tmp4++] = ImageUtils.clampRoundUShort(pixel[2]);
                    dstDataArrays[3][tmp4] = ImageUtils.clampRoundUShort(pixel[3]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosVerticalFilterUShort(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleY, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        /* Get memory of filter array */
        scale = 1.0 / scale;
        short[][] srcDataArrays = src.getShortDataArrays();
        short[][] dstDataArrays = dst.getShortDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = dst.getNumBands();

        int contributionNums = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionNums];
        for (int t = 0; t < contributionNums; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstHeight = dst.getHeight(), dstWidth = dst.getWidth();
        final int srcHeight = src.getHeight();
        final int srcScanlineStride = src.getScanlineStride();
        final int dstScanlineStride = dst.getScanlineStride();

        /* First, we compute rows pixel, then compute columns pixel */
        for (int y = 0; y < dstHeight; y++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (y + 0.5) / scaleY;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcHeight) ? t2 : srcHeight);
            density = 0.0;
            /* Get the value of Sinc */
            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = y * dstScanlineStride;
            int tmp5 = ((int) (center)) * srcScanlineStride + ((int) (start - center)) * srcScanlineStride;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++] & 0xFFFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++] & 0xFFFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7] & 0xFFFF) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundUShort(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundUShort(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundUShort(pixel[2]);

                    tmp5 += channel;
                }
            } else if (channel == 1) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6] & 0xFFFF) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundUShort(pixel[0]);

                    tmp5 += channel;
                }
            } else if (channel == 4) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++] & 0xFFFF) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++] & 0xFFFF) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7++] & 0xFFFF) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp7] & 0xFFFF) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundUShort(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundUShort(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundUShort(pixel[2]);
                    dstDataArrays[3][tmp3++] = ImageUtils.clampRoundUShort(pixel[3]);

                    tmp5 += channel;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosHorizontalFilterShort(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleX, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        short[][] srcDataArrays = src.getShortDataArrays();
        short[][] dstDataArrays = dst.getShortDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = src.getNumBands();
        scale = 1.0 / scale;

        int contributionsNum = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionsNum];
        for (int t = 0; t < contributionsNum; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstWidth = dst.getWidth(), dstHeight = dst.getHeight();
        final int srcWidth = src.getWidth();
        final int dstScanlineStride = dst.getScanlineStride();
        final int srcScanlineStride = src.getScanlineStride();

        for (int x = 0; x < dstWidth; x++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (x + 0.5) / scaleX;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcWidth) ? t2 : srcWidth);
            density = 0.0;

            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = x * channel;
            int tmp5 = ((int) (start - center) + (int) center) * channel;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundShort(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundShort(pixel[1]);
                    dstDataArrays[2][tmp4] = ImageUtils.clampRoundShort(pixel[2]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 1) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                    }

                    dstDataArrays[0][tmp3] = ImageUtils.clampRoundShort(pixel[0]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 4) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundShort(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundShort(pixel[1]);
                    dstDataArrays[2][tmp4++] = ImageUtils.clampRoundShort(pixel[2]);
                    dstDataArrays[3][tmp4] = ImageUtils.clampRoundShort(pixel[3]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosVerticalFilterShort(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleY, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        /* Get memory of filter array */
        scale = 1.0 / scale;
        short[][] srcDataArrays = src.getShortDataArrays();
        short[][] dstDataArrays = dst.getShortDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = dst.getNumBands();

        int contributionNums = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionNums];
        for (int t = 0; t < contributionNums; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstHeight = dst.getHeight(), dstWidth = dst.getWidth();
        final int srcHeight = src.getHeight();
        final int srcScanlineStride = src.getScanlineStride();
        final int dstScanlineStride = dst.getScanlineStride();

        /* First, we compute rows pixel, then compute columns pixel */
        for (int y = 0; y < dstHeight; y++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (y + 0.5) / scaleY;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcHeight) ? t2 : srcHeight);
            density = 0.0;
            /* Get the value of Sinc */
            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = y * dstScanlineStride;
            int tmp5 = ((int) (center)) * srcScanlineStride + ((int) (start - center)) * srcScanlineStride;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundShort(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundShort(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundShort(pixel[2]);

                    tmp5 += channel;
                }
            } else if (channel == 1) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundShort(pixel[0]);

                    tmp5 += channel;
                }
            } else if (channel == 4) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundShort(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundShort(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundShort(pixel[2]);
                    dstDataArrays[3][tmp3++] = ImageUtils.clampRoundShort(pixel[3]);

                    tmp5 += channel;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosHorizontalFilterInt(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleX, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        int[][] srcDataArrays = src.getIntDataArrays();
        int[][] dstDataArrays = dst.getIntDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = src.getNumBands();
        scale = 1.0 / scale;

        int contributionsNum = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionsNum];
        for (int t = 0; t < contributionsNum; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstWidth = dst.getWidth(), dstHeight = dst.getHeight();
        final int srcWidth = src.getWidth();
        final int dstScanlineStride = dst.getScanlineStride();
        final int srcScanlineStride = src.getScanlineStride();

        for (int x = 0; x < dstWidth; x++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (x + 0.5) / scaleX;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcWidth) ? t2 : srcWidth);
            density = 0.0;

            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = x * channel;
            int tmp5 = ((int) (start - center) + (int) center) * channel;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundInt(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundInt(pixel[1]);
                    dstDataArrays[2][tmp4] = ImageUtils.clampRoundInt(pixel[2]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 1) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                    }

                    dstDataArrays[0][tmp3] = ImageUtils.clampRoundInt(pixel[0]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 4) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampRoundInt(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampRoundInt(pixel[1]);
                    dstDataArrays[2][tmp4++] = ImageUtils.clampRoundInt(pixel[2]);
                    dstDataArrays[3][tmp4] = ImageUtils.clampRoundInt(pixel[3]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosVerticalFilterInt(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleY, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        /* Get memory of filter array */
        scale = 1.0 / scale;
        int[][] srcDataArrays = src.getIntDataArrays();
        int[][] dstDataArrays = dst.getIntDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = dst.getNumBands();

        int contributionNums = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionNums];
        for (int t = 0; t < contributionNums; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstHeight = dst.getHeight(), dstWidth = dst.getWidth();
        final int srcHeight = src.getHeight();
        final int srcScanlineStride = src.getScanlineStride();
        final int dstScanlineStride = dst.getScanlineStride();

        /* First, we compute rows pixel, then compute columns pixel */
        for (int y = 0; y < dstHeight; y++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (y + 0.5) / scaleY;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcHeight) ? t2 : srcHeight);
            density = 0.0;
            /* Get the value of Sinc */
            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = y * dstScanlineStride;
            int tmp5 = ((int) (center)) * srcScanlineStride + ((int) (start - center)) * srcScanlineStride;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundInt(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundInt(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundInt(pixel[2]);

                    tmp5 += channel;
                }
            } else if (channel == 1) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundInt(pixel[0]);

                    tmp5 += channel;
                }
            } else if (channel == 4) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampRoundInt(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampRoundInt(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampRoundInt(pixel[2]);
                    dstDataArrays[3][tmp3++] = ImageUtils.clampRoundInt(pixel[3]);

                    tmp5 += channel;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosHorizontalFilterFloat(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleX, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        float[][] srcDataArrays = src.getFloatDataArrays();
        float[][] dstDataArrays = dst.getFloatDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = src.getNumBands();
        scale = 1.0 / scale;

        int contributionsNum = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionsNum];
        for (int t = 0; t < contributionsNum; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstWidth = dst.getWidth(), dstHeight = dst.getHeight();
        final int srcWidth = src.getWidth();
        final int dstScanlineStride = dst.getScanlineStride();
        final int srcScanlineStride = src.getScanlineStride();

        for (int x = 0; x < dstWidth; x++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (x + 0.5) / scaleX;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcWidth) ? t2 : srcWidth);
            density = 0.0;

            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = x * channel;
            int tmp5 = ((int) (start - center) + (int) center) * channel;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampFloat(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampFloat(pixel[1]);
                    dstDataArrays[2][tmp4] = ImageUtils.clampFloat(pixel[2]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 1) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                    }

                    dstDataArrays[0][tmp3] = ImageUtils.clampFloat(pixel[0]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 4) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = ImageUtils.clampFloat(pixel[0]);
                    dstDataArrays[1][tmp4++] = ImageUtils.clampFloat(pixel[1]);
                    dstDataArrays[2][tmp4++] = ImageUtils.clampFloat(pixel[2]);
                    dstDataArrays[3][tmp4] = ImageUtils.clampFloat(pixel[3]);

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosVerticalFilterFloat(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleY, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        /* Get memory of filter array */
        scale = 1.0 / scale;
        float[][] srcDataArrays = src.getFloatDataArrays();
        float[][] dstDataArrays = dst.getFloatDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = dst.getNumBands();

        int contributionNums = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionNums];
        for (int t = 0; t < contributionNums; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstHeight = dst.getHeight(), dstWidth = dst.getWidth();
        final int srcHeight = src.getHeight();
        final int srcScanlineStride = src.getScanlineStride();
        final int dstScanlineStride = dst.getScanlineStride();

        /* First, we compute rows pixel, then compute columns pixel */
        for (int y = 0; y < dstHeight; y++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (y + 0.5) / scaleY;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcHeight) ? t2 : srcHeight);
            density = 0.0;
            /* Get the value of Sinc */
            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = y * dstScanlineStride;
            int tmp5 = ((int) (center)) * srcScanlineStride + ((int) (start - center)) * srcScanlineStride;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampFloat(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampFloat(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampFloat(pixel[2]);

                    tmp5 += channel;
                }
            } else if (channel == 1) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampFloat(pixel[0]);

                    tmp5 += channel;
                }
            } else if (channel == 4) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = ImageUtils.clampFloat(pixel[0]);
                    dstDataArrays[1][tmp3++] = ImageUtils.clampFloat(pixel[1]);
                    dstDataArrays[2][tmp3++] = ImageUtils.clampFloat(pixel[2]);
                    dstDataArrays[3][tmp3++] = ImageUtils.clampFloat(pixel[3]);

                    tmp5 += channel;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosHorizontalFilterDouble(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleX, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        double[][] srcDataArrays = src.getDoubleDataArrays();
        double[][] dstDataArrays = dst.getDoubleDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = src.getNumBands();
        scale = 1.0 / scale;

        int contributionsNum = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionsNum];
        for (int t = 0; t < contributionsNum; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstWidth = dst.getWidth(), dstHeight = dst.getHeight();
        final int srcWidth = src.getWidth();
        final int dstScanlineStride = dst.getScanlineStride();
        final int srcScanlineStride = src.getScanlineStride();

        for (int x = 0; x < dstWidth; x++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (x + 0.5) / scaleX;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcWidth) ? t2 : srcWidth);
            density = 0.0;

            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = x * channel;
            int tmp5 = ((int) (start - center) + (int) center) * channel;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = pixel[0];
                    dstDataArrays[1][tmp4++] = pixel[1];
                    dstDataArrays[2][tmp4] = pixel[2];

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 1) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                    }

                    dstDataArrays[0][tmp3] = pixel[0];

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else if (channel == 4) {
                for (int y = 0; y < dstHeight; y++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp6++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp6++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp6++]) * contribution[i].weight;
                    }

                    int tmp4 = tmp3;
                    dstDataArrays[0][tmp4++] = pixel[0];
                    dstDataArrays[1][tmp4++] = pixel[1];
                    dstDataArrays[2][tmp4++] = pixel[2];
                    dstDataArrays[3][tmp4] = pixel[3];

                    tmp3 += dstScanlineStride;
                    tmp5 += srcScanlineStride;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private void lanczosVerticalFilterDouble(RasterAccessor src, RasterAccessor dst) {
        double scale = Math.max(1.0 / scaleY, 1.0);
        double support = scale * filter.getSupport() * filter.getBlur();

        if (support < 0.5) {
            support = 0.5;
            scale = 1.0;
        }

        /* Get memory of filter array */
        scale = 1.0 / scale;
        double[][] srcDataArrays = src.getDoubleDataArrays();
        double[][] dstDataArrays = dst.getDoubleDataArrays();
        double pixel[] = { 0, 0, 0, 0 };
        final int channel = dst.getNumBands();

        int contributionNums = (int) (2.0 * support + 3.0);
        LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionNums];
        for (int t = 0; t < contributionNums; t++) {
            contribution[t] = new LanczosContributionInfo();
        }

        double center, t1, t2, density;
        int start, stop, n;
        final int dstHeight = dst.getHeight(), dstWidth = dst.getWidth();
        final int srcHeight = src.getHeight();
        final int srcScanlineStride = src.getScanlineStride();
        final int dstScanlineStride = dst.getScanlineStride();

        /* First, we compute rows pixel, then compute columns pixel */
        for (int y = 0; y < dstHeight; y++) {
            /*
             * Get the location of the piexls that will be used to compute new pixel
             */
            center = (y + 0.5) / scaleY;
            t1 = center - support + 0.5;
            t2 = center + support + 0.5;
            start = (int) (t1 > 0 ? t1 : 0);
            stop = (int) (t2 < (srcHeight) ? t2 : srcHeight);
            density = 0.0;
            /* Get the value of Sinc */
            for (n = 0; n < (stop - start); n++) {
                contribution[n].pixel = start + n;
                contribution[n].weight = getLanczosResizeFilterWeight(scale * ((double) (start + n) - center + 0.5));
                density += contribution[n].weight;
            }
            if ((density != 0.0) && (density != 1.0)) {
                density = 1.0 / density;
                for (int i = 0; i < n; i++) {
                    contribution[i].weight *= density;
                }
            }

            int tmp3 = y * dstScanlineStride;
            int tmp5 = ((int) (center)) * srcScanlineStride + ((int) (start - center)) * srcScanlineStride;

            /* Start compute new piexl */
            if (channel == 3) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = pixel[0];
                    dstDataArrays[1][tmp3++] = pixel[1];
                    dstDataArrays[2][tmp3++] = pixel[2];

                    tmp5 += channel;
                }
            } else if (channel == 1) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        pixel[0] += (srcDataArrays[0][tmp6]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = pixel[0];

                    tmp5 += channel;
                }
            } else if (channel == 4) {
                for (int x = 0; x < dstWidth; x++) {
                    pixel[0] = 0.0;
                    pixel[1] = 0.0;
                    pixel[2] = 0.0;
                    pixel[3] = 0.0;

                    int tmp6 = tmp5;
                    for (int i = 0; i < n; i++) {
                        int tmp7 = tmp6;
                        pixel[0] += (srcDataArrays[0][tmp7++]) * contribution[i].weight;
                        pixel[1] += (srcDataArrays[1][tmp7++]) * contribution[i].weight;
                        pixel[2] += (srcDataArrays[2][tmp7++]) * contribution[i].weight;
                        pixel[3] += (srcDataArrays[3][tmp7]) * contribution[i].weight;
                        tmp6 += srcScanlineStride;
                    }

                    dstDataArrays[0][tmp3++] = pixel[0];
                    dstDataArrays[1][tmp3++] = pixel[1];
                    dstDataArrays[2][tmp3++] = pixel[2];
                    dstDataArrays[3][tmp3++] = pixel[3];

                    tmp5 += channel;
                }
            } else {
                throw new IllegalArgumentException("Unsupported channels num : " + channel);
            }
        }
    }
    
    private double getLanczosResizeFilterWeight(final double x) {
        double blur;
        double scale;

        blur = Math.abs(x) / filter.getBlur();
        scale = filter.getScale() / filter.getWindowSupport();
        scale = filter.window(blur * scale);

        return scale * filter.filter(blur);
    }
}
