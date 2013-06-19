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
package com.alibaba.simpleimage;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.WritableRenderedImageAdapter;

import org.w3c.dom.Node;

import com.alibaba.simpleimage.util.NodeUtils;

/**
 * 这个类存在的意义在于可以比BufferedImage和PlanarImage提供更多的图片的源信息，同时可以保存像GIF这样多副图片的图片格式。 比如说可以保存JPEG的quality，采样参数等
 * 对于PNG，BMP和GIF这三类无损压缩的图片格式，quality并没有意义 这里设置为93只是表示，这三类图片一旦按JPEG格式保存的话，默认的quality是93
 * 
 * @author wendell
 */
public class ImageWrapper extends MetadataRenderedImage {

    public static final int   DEFAULT_QUALITY = 93;

    protected RenderedImage[] images;

    // only support jpeg broken indicator
    protected boolean         broken;

    public ImageWrapper(BufferedImage bi){
        this(bi, DEFAULT_QUALITY, false);
    }

    public ImageWrapper(PlanarImage img){
        this(img, DEFAULT_QUALITY, false);
    }

    public ImageWrapper(BufferedImage bi, boolean isBroken){
        this(bi, DEFAULT_QUALITY, isBroken);
    }

    public ImageWrapper(PlanarImage img, boolean isBroken){
        this(img, DEFAULT_QUALITY, isBroken);
    }

    public ImageWrapper(BufferedImage bi, int quality){
        this(bi, quality, false);
    }

    public ImageWrapper(BufferedImage bi, int quality, boolean isBroken){
        this.quality = quality;
        this.images = new RenderedImage[1];
        this.images[0] = PlanarImage.wrapRenderedImage(bi);
        this.broken = isBroken;
    }

    public ImageWrapper(PlanarImage image, int quality){
        this(image, quality, false);
    }

    public ImageWrapper(PlanarImage image, int quality, boolean isBroken){
        this.images = new RenderedImage[1];
        this.images[0] = image;
        this.quality = quality;
        this.broken = isBroken;
    }

    public ImageWrapper(BufferedImage[] imgs){
        setImages(imgs);
        this.quality = DEFAULT_QUALITY;
    }

    public ImageWrapper(PlanarImage[] imgs){
        setImages(imgs);
        this.quality = DEFAULT_QUALITY;
    }

    public BufferedImage getAsBufferedImage(int index) {
        if (images[index] instanceof BufferedImage) {
            return (BufferedImage) images[index];
        } else if (images[index] instanceof PlanarImage) {
            return ((PlanarImage) images[index]).getAsBufferedImage();
        } else if (images[index] instanceof WritableRenderedImage) {
            return new WritableRenderedImageAdapter((WritableRenderedImage) images[index]).getAsBufferedImage();
        } else {
            return new RenderedImageAdapter(images[index]).getAsBufferedImage();
        }
    }

    public BufferedImage getAsBufferedImage() {
        return getAsBufferedImage(0);
    }

    public PlanarImage getAsPlanarImage(int index) {
        if (images[index] instanceof PlanarImage) {
            return (PlanarImage) images[index];
        } else if (images[index] instanceof BufferedImage) {
            return PlanarImage.wrapRenderedImage(images[index]);
        } else {
            return new RenderedImageAdapter(images[index]);
        }
    }

    public PlanarImage getAsPlanarImage() {
        return getAsPlanarImage(0);
    }

    public BufferedImage[] getAsBufferedImages() {
        BufferedImage[] imgs = new BufferedImage[images.length];

        for (int i = 0; i < imgs.length; i++) {
            imgs[i] = getAsBufferedImage(i);
        }

        return imgs;
    }

    public PlanarImage[] getAsPlanarImages() {
        PlanarImage[] imgs = new PlanarImage[images.length];

        for (int i = 0; i < imgs.length; i++) {
            imgs[i] = getAsPlanarImage(i);
        }

        return imgs;
    }

    public void setImages(BufferedImage[] imgs) {
        images = new RenderedImage[imgs.length];
        for (int i = 0; i < imgs.length; i++) {
            images[i] = imgs[i];
        }
    }

    public void setImages(PlanarImage[] imgs) {
        images = new RenderedImage[imgs.length];
        for (int i = 0; i < imgs.length; i++) {
            images[i] = imgs[i];
        }
    }

    public void setImage(int index, BufferedImage bi) {
        this.images[index] = bi;
    }

    public void setImage(BufferedImage bi) {
        setImage(0, bi);
    }

    public void setImage(int index, PlanarImage img) {
        this.images[index] = img;
    }

    public void setImage(PlanarImage img) {
        setImage(0, img);
    }

    public int getNumOfImages() {
        return images.length;
    }

    /**
     * 如果是除GIF以外的图片，getWidth()与getWidth(0)等价，既返回第一张图片的宽度 如果是GIF，则读取GIF的元信息来获取图片宽度，这个值不一定和getWidth(0)相等
     * 
     * @return 图片宽度
     */
    public int getWidth() {
        if (format == ImageFormat.GIF && streamMetadata != null) {
            Node screenDescNode = NodeUtils.getChild(streamMetadata, "LogicalScreenDescriptor");
            if (screenDescNode != null) {
                return NodeUtils.getIntAttr(screenDescNode, "logicalScreenWidth");
            }
        }

        return getWidth(0);
    }

    /**
     * 如果是除GIF以外的图片，getHeight()与getHeight(0)等价，既返回第一张图片的宽度 如果是GIF，则读取GIF的元信息来获取图片高度，这个值不一定和getHeight(0)相等
     * 
     * @return 图片高度
     */
    public int getHeight() {
        if (format == ImageFormat.GIF && streamMetadata != null) {
            Node screenDescNode = NodeUtils.getChild(streamMetadata, "LogicalScreenDescriptor");
            if (screenDescNode != null) {
                return NodeUtils.getIntAttr(screenDescNode, "logicalScreenHeight");
            }
        }

        return getHeight(0);
    }

    public int getWidth(int index) {
        if (index < 0 || index >= images.length) {
            throw new IndexOutOfBoundsException("Just totally have " + images.length + " images");
        }

        return images[index].getWidth();
    }

    public int getHeight(int index) {
        if (index < 0 || index >= images.length) {
            throw new IndexOutOfBoundsException("Just totally have " + images.length + " images");
        }

        return images[index].getHeight();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        int numOfImages = images.length;
        BufferedImage[] imgs = new BufferedImage[numOfImages];
        for (int i = 0; i < numOfImages; i++) {
            PlanarImage oldImg = getAsPlanarImage(i);
            imgs[i] = oldImg.getAsBufferedImage();
        }

        ImageWrapper newImgWrapper = new ImageWrapper(imgs);
        newImgWrapper.quality = quality;
        newImgWrapper.broken = broken;
        newImgWrapper.format = format;
        if (horizontalSamplingFactors != null) {
            newImgWrapper.horizontalSamplingFactors = horizontalSamplingFactors.clone();
        }
        if (verticalSamplingFactors != null) {
            newImgWrapper.verticalSamplingFactors = verticalSamplingFactors.clone();
        }
        if (streamMetadata != null) {
            newImgWrapper.streamMetadata = NodeUtils.cloneNode(streamMetadata);
        }
        if (metadatas != null) {
            newImgWrapper.metadatas = new Node[metadatas.length];
            for (int i = 0; i < metadatas.length; i++) {
                newImgWrapper.metadatas[i] = NodeUtils.cloneNode(metadatas[i]);
            }
        }

        return newImgWrapper;
    }

    /**
     * 这个参数只有图片格式是JPEG的时候才有意义，表示图片内容已经损坏
     * 
     * @return the broken
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * 这个参数只有图片格式是JPEG的时候才有意义，表示图片内容已经损坏
     * 
     * @param broken the broken to set
     */
    public void setBroken(boolean broken) {
        this.broken = broken;
    }
}
