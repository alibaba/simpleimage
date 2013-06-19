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

import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.w3c.dom.Node;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.codec.jpeg.JPEGDecoder;
import com.alibaba.simpleimage.io.ByteArraySeekableStreamWrap;
import com.alibaba.simpleimage.io.ImageBitsInputStream;
import com.alibaba.simpleimage.io.ImageInputStream;

/**
 * TODO Comment of ImageReadHelper
 * 
 * @author wendell
 */
public class ImageReadHelper {
    /**
     * Used by PNG, GIF default quality
     */
    public static final int   DEFAULT_HIGHT_QUALITY = 93;

    static {
        JAIRegisterHelper.register();
    }

    protected static ImageLog log                   = ImageLog.getLog(ImageReadHelper.class);

    public static ImageWrapper read(InputStream input)
            throws SimpleImageException {
        try {
            input = ImageUtils.createMemoryStream(input);

            if (ImageUtils.isJPEG(input)) {
                return readJPEG(input);
            }

            if (ImageUtils.isGIF(input)) {
                return readGIF(input);
            }

            return readGeneral(input);
        } catch (Exception e) {
            throw new SimpleImageException(e);
        }
    }

    public static ImageWrapper readJPEG(InputStream input)
            throws SimpleImageException {
        ImageWrapper img = null;
        ImageInputStream imageStream = null;

        try {
            imageStream = new ImageBitsInputStream(input);
            JPEGDecoder decoder = new JPEGDecoder(imageStream);

            img = decoder.decode();
        } catch (Exception e) {
            throw new SimpleImageException(e);
        }

        return img;
    }

    public static ImageWrapper readGIF(InputStream input)
            throws SimpleImageException {
        javax.imageio.stream.ImageInputStream imageIn = null;
        ImageReader reader = null;
        
        try {
            imageIn = ImageIO.createImageInputStream(input);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageIn);

            if (readers.hasNext()) {
                reader = readers.next();
            } else {
                throw new IllegalStateException("No GIF reader matched");
            }

            reader.setInput(imageIn);

            int numOfImages = reader.getNumImages(true);
            if(numOfImages <= 0) {
                throw new SimpleImageException("a GIF without pictures inside, maybe it's a attack");
            }
            BufferedImage[] images = new BufferedImage[numOfImages];
            Node[] metadatas = new Node[numOfImages];
            IIOMetadata streamMetadata = reader.getStreamMetadata();
            
            for (int i = 0; i < numOfImages; i++) {
                images[i] = reader.read(i);
                metadatas[i] = reader.getImageMetadata(i).getAsTree(ImageWrapper.GIF_IMAGE_METADATA_NAME);
            }
            ImageWrapper img = new ImageWrapper(images);
            img.setImageFormat(ImageFormat.GIF);
            img.setStreamMetadata(streamMetadata.getAsTree(ImageWrapper.GIF_STREAM_METADATA_NAME));
            img.setMetadatas(metadatas);

            return img;
        } catch (Exception e) {
            throw new SimpleImageException(e);
        } finally {
            if(reader != null){
                reader.dispose();
            }
        }
    }

    public static ImageWrapper readGeneral(InputStream input)
            throws SimpleImageException {
        try {
            ImageWrapper img = null;
            ImageFormat format = ImageUtils.identifyFormat(input);
            if(format == null) {
                throw new IllegalArgumentException("Unsupported image format, only JPEG, GIF, PNG, BMP and TIFF are supported");
            }
            ByteArraySeekableStreamWrap wrap = null;
            wrap = ByteArraySeekableStreamWrap.wrapInputStream(input);

            /**
             * 利用JAI读取源图片
             */
            ParameterBlock pb = new ParameterBlock();
            pb.add(wrap);
            PlanarImage src = JAI.create("Stream", pb);
            img = new ImageWrapper(src, DEFAULT_HIGHT_QUALITY);
            img.setImageFormat(format);

            return img;
        } catch (Exception e) {
            throw new SimpleImageException(e);
        }
    }
}
