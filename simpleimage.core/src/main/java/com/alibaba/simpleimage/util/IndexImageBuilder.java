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

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.ColorQuantizerDescriptor;
import javax.media.jai.operator.ColorQuantizerType;

import com.alibaba.simpleimage.render.WriteParameter;

/**
 * 类PaletteBuilder2.java的实现描述：TODO 类实现描述
 * 
 * @author wendell 2011-8-5 下午02:11:53
 */
public class IndexImageBuilder {

    public static RenderedImage createIndexedImage(RenderedImage src, WriteParameter.QuantAlgorithm quantAlgorithm) {
        IndexImageBuilder builder = new IndexImageBuilder(src, quantAlgorithm);

        return builder.createIndexedImage();
    }
    
    public static boolean needConvertToIndex(RenderedImage image) {
        SampleModel sampleModel = image.getSampleModel();
        ColorModel colorModel = image.getColorModel();

        return sampleModel.getNumBands() != 1 || sampleModel.getSampleSize()[0] > 8
               || colorModel.getComponentSize()[0] > 8;
    }

    protected RenderedImage                 src;
    protected WriteParameter.QuantAlgorithm quantAlgorithm;

    protected IndexImageBuilder(RenderedImage src, WriteParameter.QuantAlgorithm quantAlgorithm){
        this.src = src;
        this.quantAlgorithm = quantAlgorithm;
    }

    protected RenderedImage createIndexedImage() {
        if(quantAlgorithm == WriteParameter.QuantAlgorithm.OctTree) {
            return PaletteBuilder.createIndexedImage(src);
        }
        
        SampleModel sm = src.getSampleModel();
        if(!(sm.getNumBands() != 3  && sm.getDataType() == DataBuffer.TYPE_BYTE)) {
            PlanarImage pi = PlanarImage.wrapRenderedImage(src);
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(pi);
            pb.add(getQuantizationAlgorithm(quantAlgorithm));
            pi = JAI.create("ColorQuantizer", pb);
            
            return pi;
        }
            
        return PaletteBuilder.createIndexedImage(src);
    }

    private ColorQuantizerType getQuantizationAlgorithm(WriteParameter.QuantAlgorithm quantAlg) {
        if (quantAlg == WriteParameter.QuantAlgorithm.OctTree) {
            return ColorQuantizerDescriptor.OCTTREE;
        } else if (quantAlg == WriteParameter.QuantAlgorithm.NeuQuant) {
            return ColorQuantizerDescriptor.NEUQUANT;
        } else if (quantAlg == WriteParameter.QuantAlgorithm.MedianCut) {
            return ColorQuantizerDescriptor.MEDIANCUT;
        } else {
            throw new IllegalArgumentException("Unknown quantization algorithm " + quantAlg);
        }
    }

    /**
     * 此方法暂时不用，但是不删，做以后参考
     * @return
     */
    RenderedImage createSimpleIndexImage() {
        ColorModel srcCM = src.getColorModel();
        if(srcCM instanceof IndexColorModel) {
            return src;
        }
        
        Raster srcRaster = src.getData();
        boolean hasTransparent = srcCM.getTransparency() != Transparency.OPAQUE;
        int maxColorCount = 256;
        if(hasTransparent) {
            maxColorCount = 255;
        }
        
        int num = 0;
        Set<Integer> colorSet = new HashSet<Integer>();
        int[] colorArrays = new int[maxColorCount];
        for(int x = 0; x < src.getWidth(); x++) {
            for(int y = 0; y < src.getHeight(); y++) {
                int rgb = srcCM.getRGB(srcRaster.getDataElements(x, y, null)) & 0xFFFFFF;
                if(!colorSet.contains(rgb)) {
                    if(num == maxColorCount) {
                        return null;
                    }
                    
                    colorArrays[num++] = rgb;
                    colorSet.add(rgb);
                }
            }
        }
        if(num == 0) {
            throw new IllegalStateException("This image has no color");
        }
        
        int[] colors = null;
        if(hasTransparent) {
            colors = new int[num + 1];
            System.arraycopy(colorArrays, 0, colors, 1, num);
            colors[0] = 0;
        } else {
            colors = new int[num];
            System.arraycopy(colorArrays, 0, colors, 0, num);
        }
        
        Arrays.sort(colors);
        
        byte[] red = new byte[colors.length];
        byte[] green = new byte[colors.length];
        byte[] blue = new byte[colors.length];
        for(int i = 0; i < colors.length; i++) {
            red[i] = (byte)((colors[i] >> 16) & 0xFF);
            green[i] = (byte)((colors[i] >> 8) & 0xFF);
            blue[i] = (byte)((colors[i]) & 0xFF);
        }

        IndexColorModel destCM = null;
        if(hasTransparent) {
            destCM = new IndexColorModel(8, colors.length, red, green, blue, 0);
        } else {
            destCM = new IndexColorModel(8, colors.length, red, green, blue);
        }
        
        BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, destCM);
        WritableRaster raster = image.getRaster();
        for (int x = 0; x < src.getWidth(); x++) {
            for (int y = 0; y < src.getHeight(); y++) {
                int argb = srcCM.getRGB(srcRaster.getDataElements(x, y, null));
                if(hasTransparent && ((argb & 0xFF000000) != 0xFF000000)) {
                    raster.setSample(x, y, 0, 0);
                } else {
                    int index = Arrays.binarySearch(colors, (argb & 0x00FFFFFF));
                    if(index < 0) {
                        throw new IllegalStateException("Some colors not indexed");
                    }
                    raster.setSample(x, y, 0, index);
                }
            }
        }

        return image;
    }
}
