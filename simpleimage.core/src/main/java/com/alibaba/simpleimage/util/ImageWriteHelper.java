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

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.PlanarImage;

import org.w3c.dom.Node;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.render.WriteParameter;
import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageWriteHelper {

    public static final int DEFAULT_MIN_QUALITY = 50;

    public static void write(ImageWrapper imgWrapper, OutputStream os, ImageFormat format, WriteParameter param)
                                                                                                                throws SimpleImageException {
        if (format == ImageFormat.JPEG) {
            writeJPEG(imgWrapper, os, param);
        } else if (format == ImageFormat.GIF) {
            writeGIF(imgWrapper, os, param.getQuantAlgorithm());
        } else if (format == ImageFormat.TIFF || format == ImageFormat.PNG || format == ImageFormat.BMP) {
            try {
                ImageIO.write(imgWrapper.getAsBufferedImage(), format.getDesc(), os);
            } catch (IOException e) {
                throw new SimpleImageException(e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported output format, only JPEG, BMP, GIF, PNG and TIFF are ok");
        }
    }

    public static void writeJPEG(ImageWrapper imgWrapper, OutputStream os, WriteParameter inParam)
                                                                                                  throws SimpleImageException {
        BufferedImage img = imgWrapper.getAsBufferedImage();

        int channels = img.getColorModel().getNumComponents();
        // Convert RGBA colorspace to RGB
        if (channels == 4 && img.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_CMYK) {
            img = ImageColorConvertHelper.convertRGBA2RGB(PlanarImage.wrapRenderedImage(img)).getAsBufferedImage();
            channels = img.getColorModel().getNumComponents();
        }

        JPEGEncodeParam encodeParam = JPEGCodec.getDefaultJPEGEncodeParam(img);
        if (inParam.isSamplingSet()) {
            encodeParam.setHorizontalSubsampling(0, inParam.getHorizontalSubsampling(0));
            encodeParam.setVerticalSubsampling(0, inParam.getVerticalSubsampling(0));

            if (channels >= 3) {
                encodeParam.setHorizontalSubsampling(1, inParam.getHorizontalSubsampling(1));
                encodeParam.setVerticalSubsampling(1, inParam.getVerticalSubsampling(1));

                encodeParam.setHorizontalSubsampling(2, inParam.getHorizontalSubsampling(2));
                encodeParam.setVerticalSubsampling(2, inParam.getVerticalSubsampling(2));
            }

            if (channels >= 4) {
                encodeParam.setHorizontalSubsampling(3, inParam.getHorizontalSubsampling(3));
                encodeParam.setVerticalSubsampling(3, inParam.getVerticalSubsampling(3));
            }
        } else {
            encodeParam.setHorizontalSubsampling(0, imgWrapper.getHorizontalSubsampling(0));
            encodeParam.setVerticalSubsampling(0, imgWrapper.getVerticalSubsampling(0));

            if (channels >= 3) {
                encodeParam.setHorizontalSubsampling(1, imgWrapper.getHorizontalSubsampling(1));
                encodeParam.setVerticalSubsampling(1, imgWrapper.getVerticalSubsampling(1));

                encodeParam.setHorizontalSubsampling(2, imgWrapper.getHorizontalSubsampling(2));
                encodeParam.setVerticalSubsampling(2, imgWrapper.getVerticalSubsampling(2));
            }
            if (channels >= 4) {
                encodeParam.setHorizontalSubsampling(3, imgWrapper.getHorizontalSubsampling(3));
                encodeParam.setVerticalSubsampling(3, imgWrapper.getVerticalSubsampling(3));
            }
        }

        if (inParam.isQualitySet()) {
            encodeParam.setQuality(inParam.getDefaultQuality(), false);
        } else {
            int q = imgWrapper.getQuality();
            if (q < DEFAULT_MIN_QUALITY) {
                q = DEFAULT_MIN_QUALITY;
            }
            encodeParam.setQuality(q / 100.0f, false);
        }

        try {
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os, encodeParam);
            encoder.encode(img);
        } catch (IOException e) {
            throw new SimpleImageException(e);
        } catch (ImageFormatException e) {
            throw new SimpleImageException(e);
        }
    }

    public static void writeGIF(ImageWrapper imgWrapper, OutputStream os, WriteParameter.QuantAlgorithm quantAlg)
                                                                                                                 throws SimpleImageException {
        ImageOutputStream imageOut = null;
        ImageWriter writer = null;

        BufferedImage[] images = imgWrapper.getAsBufferedImages();
        Node[] metadatas = imgWrapper.getMetadatas();

        if (metadatas == null || imgWrapper.getStreamMetadata() == null) {
            try {
                imageOut = ImageIO.createImageOutputStream(os);
                RenderedImage img = images[0];
                if (IndexImageBuilder.needConvertToIndex(images[0])) {
                    img = IndexImageBuilder.createIndexedImage(images[0], quantAlg);
                }
                ImageIO.write(img, "GIF", imageOut);
            } catch (IOException e) {
                throw new SimpleImageException(e);
            } finally {
                ImageUtils.closeQuietly(imageOut);
            }

            return;
        }

        try {
            imageOut = ImageIO.createImageOutputStream(os);
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("GIF");
            while (writers.hasNext()) {
                writer = writers.next();
                if (writer.canWriteSequence()) {
                    break;
                }
            }

            if (writer == null || !writer.canWriteSequence()) {
                throw new IllegalStateException("No GIF writer matched");
            }

            writer.setOutput(imageOut);

            ImageWriteParam param = writer.getDefaultWriteParam();
            IIOMetadata streamMeta = writer.getDefaultStreamMetadata(param);
            //merge stream metadata
            streamMeta.mergeTree(ImageWrapper.GIF_STREAM_METADATA_NAME, imgWrapper.getStreamMetadata());
            writer.prepareWriteSequence(streamMeta);
            for (int i = 0; i < images.length; i++) {
                ImageTypeSpecifier imageType = new ImageTypeSpecifier(images[i].getColorModel(),
                                                                      images[i].getSampleModel());
                RenderedImage renderedImg = images[i];
                if (IndexImageBuilder.needConvertToIndex(renderedImg)) {
                    NodeUtils.removeChild(metadatas[i], "LocalColorTable");
                    renderedImg = IndexImageBuilder.createIndexedImage(renderedImg, quantAlg);
                }
                IIOMetadata meta = writer.getDefaultImageMetadata(imageType, param);
                meta.mergeTree(ImageWrapper.GIF_IMAGE_METADATA_NAME, metadatas[i]);

                IIOImage img = new IIOImage(renderedImg, null, meta);
                writer.writeToSequence(img, param);
            }
            writer.endWriteSequence();

            imageOut.flush();
        } catch (IOException e) {
            throw new SimpleImageException(e);
        } finally {
            ImageUtils.closeQuietly(imageOut);

            if (writer != null) {
                writer.dispose();
            }
        }
    }
}
