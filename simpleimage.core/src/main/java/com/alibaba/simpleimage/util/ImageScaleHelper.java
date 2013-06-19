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
package com.alibaba.simpleimage.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;

import org.w3c.dom.Node;

import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.jai.scale.LanczosScaleOp;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.sun.media.jai.opimage.SubsampleAverageCRIF;

public class ImageScaleHelper {

    private static ImageLog log = ImageLog.getLog(ImageScaleHelper.class);

    public static ImageWrapper scaleGIF(ImageWrapper imgWrapper, ScaleParameter zoom) throws SimpleImageException {
        Node streamMetadata = imgWrapper.getStreamMetadata();

        int width = 0, height = 0;
        Node screenDescNode = NodeUtils.getChild(streamMetadata, "LogicalScreenDescriptor");
        if (screenDescNode != null) {
            width = NodeUtils.getIntAttr(screenDescNode, "logicalScreenWidth");
            height = NodeUtils.getIntAttr(screenDescNode, "logicalScreenHeight");
        }
        if (width <= 0 || height <= 0) {
            width = imgWrapper.getAsBufferedImage().getWidth();
            height = imgWrapper.getAsBufferedImage().getHeight();
        }

        // do not need scale the image
        if (zoom.getMaxWidth() >= width && zoom.getMaxHeight() >= height) {
            return imgWrapper;
        }

        double scale = computeDoubleScale(width, height, zoom);
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);
        NodeUtils.setAttrValue(screenDescNode, "logicalScreenWidth", newWidth);
        NodeUtils.setAttrValue(screenDescNode, "logicalScreenHeight", newHeight);
        NodeUtils.removeChild(streamMetadata, "GlobalColorTable");

        for (int i = 0; i < imgWrapper.getNumOfImages(); i++) {
            PlanarImage img = imgWrapper.getAsPlanarImage(i);
            Node imgMetadata = imgWrapper.getMetadata(i);

            if (img.getColorModel() instanceof IndexColorModel) {
                throw new SimpleImageException(
                                               "Unsupported scale image with IndexColorModel, please convert to RGB color model first");
            }
            // No more need, index color model will triger throws exception first
            // NodeUtils.removeChild(imgMetadata, "LocalColorTable");
            Node imgDescNode = NodeUtils.getChild(imgMetadata, "ImageDescriptor");

            int x = NodeUtils.getIntAttr(imgDescNode, "imageLeftPosition");
            int y = NodeUtils.getIntAttr(imgDescNode, "imageTopPosition");
            int newX = (int) (x * scale), newY = (int) (y * scale);
            newX = newX > 0 ? newX : 0;
            newY = newY > 0 ? newY : 0;
            NodeUtils.setAttrValue(imgDescNode, "imageLeftPosition", newX);
            NodeUtils.setAttrValue(imgDescNode, "imageTopPosition", newY);

            int imgWidth = NodeUtils.getIntAttr(imgDescNode, "imageWidth");
            int imgHeight = NodeUtils.getIntAttr(imgDescNode, "imageHeight");
            int newImgWidth = (int) (imgWidth * scale), newImgHeight = (int) (imgHeight * scale);
            newImgWidth = newImgWidth > 1 ? newImgWidth : 1;
            newImgHeight = newImgHeight > 1 ? newImgHeight : 1;
            NodeUtils.setAttrValue(imgDescNode, "imageWidth", newImgWidth);
            NodeUtils.setAttrValue(imgDescNode, "imageHeight", newImgHeight);

            if (zoom.getAlgorithm() == ScaleParameter.Algorithm.INTERP_BICUBIC) {
                img = bicubicScaleImage(img, (float) scale, Interpolation.INTERP_BICUBIC);
            } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.INTERP_BICUBIC_2) {
                img = bicubicScaleImage(img, (float) scale, Interpolation.INTERP_BICUBIC_2);
            } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.SUBSAMPLE_AVG) {
                img = subsampleavgScaleImage(img, scale);
            } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.LANCZOS) {
                img = lanczosScaleImage(img, scale);
            } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.AUTO) {
                img = autoScaleImage(img, scale);
            } else {
                throw new IllegalArgumentException("Unknow algorithm");
            }

            imgWrapper.setImage(i, img);
        }

        return imgWrapper;
    }

    public static PlanarImage scale(PlanarImage input, ScaleParameter zoom) {
        int w = input.getWidth();
        int h = input.getHeight();

        // 如果不超过最大限制则不做任何处理
        if (zoom.getMaxWidth() >= w && zoom.getMaxHeight() >= h) {
            return input;
        }

        if (zoom.getAlgorithm() == ScaleParameter.Algorithm.INTERP_BICUBIC) {
            float scale = computeFloatScale(w, h, zoom);

            return bicubicScaleImage(input, scale, Interpolation.INTERP_BICUBIC);
        } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.INTERP_BICUBIC_2) {
            float scale = computeFloatScale(w, h, zoom);

            return bicubicScaleImage(input, scale, Interpolation.INTERP_BICUBIC_2);
        } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.SUBSAMPLE_AVG) {
            double scale = computeDoubleScale(w, h, zoom);

            return subsampleavgScaleImage(input, scale);
        } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.LANCZOS) {
            double scale = computeDoubleScale(w, h, zoom);

            return lanczosScaleImage(input, scale);
        } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.AUTO) {
            double scale = computeDoubleScale(w, h, zoom);

            return autoScaleImage(input, scale);
        } else if (zoom.getAlgorithm() == ScaleParameter.Algorithm.PROGRESSIVE) {
            throw new UnsupportedOperationException("Deprecated method");
        } else {
            throw new IllegalArgumentException("Unknow algorithm");
        }
    }

    public static float computeFloatScale(int w, int h, ScaleParameter zoom) {
        int maxWidth = zoom.getMaxWidth();
        int maxHeight = zoom.getMaxHeight();
        float scale = 0.0f;

        scale = Math.min(((float) maxWidth) / w, ((float) maxHeight) / h);

        return scale;
    }

    public static double computeDoubleScale(int w, int h, ScaleParameter zoom) {
        int maxWidth = zoom.getMaxWidth();
        int maxHeight = zoom.getMaxHeight();
        double scale = 0.0;

        scale = Math.min(((double) maxWidth) / w, ((double) maxHeight) / h);

        return scale;
    }

    public static PlanarImage bicubicScaleImage(PlanarImage input, float scale, int alg) {
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                         RenderingHints.VALUE_RENDER_QUALITY);
        // 必须使用该hint，否则会出现边框变黑到情况
        qualityHints.put(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(BorderExtender.BORDER_COPY));

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(input);
        pb.add(scale);
        pb.add(scale);
        pb.add(0.0F);
        pb.add(0.0F);
        pb.add(Interpolation.getInstance(alg));

        return JAI.create("scale", pb, qualityHints);
    }

    public static PlanarImage subsampleavgScaleImage(PlanarImage input, double scale) {
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                         RenderingHints.VALUE_RENDER_QUALITY);

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(input);
        pb.add(scale);
        pb.add(scale);

        // Because the mlib subsampleaverage has bug, use pure java version
        SubsampleAverageCRIF factory = new SubsampleAverageCRIF();
        PlanarImage zoomOp = (PlanarImage) factory.create(pb, qualityHints);

        return zoomOp;
    }

    public static PlanarImage lanczosScaleImage(PlanarImage input, double scale) {
        LanczosScaleOp lanczosOp = new LanczosScaleOp(scale, scale);
        BufferedImage dest = lanczosOp.compute(input.getAsBufferedImage());

        return PlanarImage.wrapRenderedImage(dest);
    }

    public static PlanarImage autoScaleImage(PlanarImage input, double scale) {
        if (input.getWidth() > 3000 || input.getHeight() > 3000) {
            return subsampleavgScaleImage(input, scale);
        }

        try {
            return lanczosScaleImage(input, scale);
        } catch (Exception e) {
            log.warn("LanczosScale fail : " + e.getMessage(), input);

            return subsampleavgScaleImage(input, scale);
        }
    }

    /**
     * 折半渐进压缩图片方法 测试后觉得效果不太理想，不鼓励使用
     * 
     * @param img
     * @param targetWidth
     * @param targetHeight
     * @param hint
     * @param progressiveBilinear
     * @return
     */
    @Deprecated
    public static PlanarImage progressiveScaleImage(PlanarImage input, int targetWidth, int targetHeight, Object hint,
                                                    boolean progressive) {
        BufferedImage img = input.getAsBufferedImage();
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        BufferedImage scratchImage = null;
        Graphics2D g2 = null;
        int w, h;
        int prevW = ret.getWidth();
        int prevH = ret.getHeight();
        boolean isTranslucent = img.getTransparency() != Transparency.OPAQUE;

        if (progressive) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (progressive && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (progressive && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            if (scratchImage == null || isTranslucent) {
                // Use a single scratch buffer for all iterations
                // and then copy to the final, correctly-sized image
                // before returning
                scratchImage = new BufferedImage(w, h, type);
                g2 = scratchImage.createGraphics();
            }
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
            prevW = w;
            prevH = h;

            ret = scratchImage;
        } while (w != targetWidth || h != targetHeight);

        if (g2 != null) {
            g2.dispose();
        }

        // If we used a scratch buffer that is larger than our target size,
        // create an image of the right size and copy the results into it
        if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
            scratchImage = new BufferedImage(targetWidth, targetHeight, type);
            g2 = scratchImage.createGraphics();
            g2.drawImage(ret, 0, 0, null);
            g2.dispose();
            ret = scratchImage;
        }

        return new RenderedImageAdapter(ret);
    }
}
