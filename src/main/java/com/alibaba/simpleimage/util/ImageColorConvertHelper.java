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

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;

import com.alibaba.simpleimage.jai.cmm.CMMColorSpace;

public class ImageColorConvertHelper {

    static {
        JAIRegisterHelper.register();
    }

    public static PlanarImage convert2sRGB(PlanarImage src) {
        if(src.getColorModel() instanceof IndexColorModel) {
            src = convertIndexColorModel2RGB(src);
        }
        
        // 此处必须先处理CMYK， 否则， 再做ColorModel转换的时候，被当4通道处理掉了。切记。
        if (src.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_CMYK) {
            src = convertCMYK2RGB(src);
        } else if (src.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
            src = convertGray2RGB(src);
        }
        
        if ((!src.getColorModel().getColorSpace().isCS_sRGB()) || (src.getSampleModel().getDataType() != DataBuffer.TYPE_BYTE)) {
            src = generalColorConvert(src);
        } 

        return src;
    }

    public static PlanarImage generalColorConvert(PlanarImage src) {
        ColorSpace sRGBColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel sRGBColorModel = RasterFactory.createComponentColorModel(DataBuffer.TYPE_BYTE, sRGBColorSpace,
                                                                            false, false, Transparency.OPAQUE);
        ImageLayout rgbImageLayout = new ImageLayout();
        rgbImageLayout.setSampleModel(sRGBColorModel.createCompatibleSampleModel(src.getWidth(), src.getHeight()));
        RenderingHints rgbHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, rgbImageLayout);
        rgbHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        ParameterBlockJAI pb = new ParameterBlockJAI("colorconvert");
        pb.addSource(src);
        pb.setParameter("colormodel", sRGBColorModel);

        return JAI.create("colorconvert", pb, rgbHints);
    }

    /**
     * JAI在读cmyk格式的时候分2种：
     * 
     * <pre>
     * CMYK的图形读取，JAI使用的RGBA的模式的， 因此，为了替换使用自己的Color Profile， 直接使用format的操作。 
     * <li>如果cmyk自带了 ICC_Profile， 那么数据是不会被修改的。 这个情况下， 我们应该使用内置的Color Profile</li> 
     * <li>如果cmyk图形使用默认的ICC_Profile, 那么他使用内置的InvertedCMYKColorSpace， 这是时候颜色会发生反转</li>
     * </pre>
     * 
     * @param src 任意颜色空间图形
     * @return ColorSpace.CS_sRGB 表示的BufferedImage
     */
    public static PlanarImage convertCMYK2RGB(PlanarImage src) {

        ColorSpace srcColorSpace = src.getColorModel().getColorSpace();
        // check if BufferedImage is cmyk format
        if (srcColorSpace.getType() != ColorSpace.TYPE_CMYK) {
            return src;
        }

        /**
         * ICC_ColorSpace object mean jai read ColorSpace from image embed profile, we can not inverted cmyk color, and
         * can not repace BufferedImage's ColorSpace
         */
        if (srcColorSpace instanceof ICC_ColorSpace) {
            // -- Convert CMYK to RGB
            ColorSpace rgbColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel rgbColorModel = RasterFactory.createComponentColorModel(DataBuffer.TYPE_BYTE, rgbColorSpace,
                                                                               false, true, Transparency.OPAQUE);
            ImageLayout rgbImageLayout = new ImageLayout();
            rgbImageLayout.setSampleModel(rgbColorModel.createCompatibleSampleModel(src.getWidth(), src.getHeight()));
            RenderingHints rgbHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, rgbImageLayout);
            rgbHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            ParameterBlockJAI pb = new ParameterBlockJAI("colorconvert");
            pb.addSource(src);
            pb.setParameter("colormodel", rgbColorModel);

            return JAI.create("colorconvert", pb, rgbHints);
        } else {

            // get user defined color from ColorProfile data
            ColorSpace cmykColorSpace = CMMColorSpace.getInstance(src.getColorModel().getColorSpace().getType());

            ColorModel cmykColorModel = RasterFactory.createComponentColorModel(src.getSampleModel().getDataType(),
                                                                                cmykColorSpace, false, true,
                                                                                Transparency.OPAQUE);
            // replace ColorSpace by format convertor with CMYK ColorSpace
            ImageLayout cmykImageLayout = new ImageLayout();
            cmykImageLayout.setColorModel(cmykColorModel);
            RenderingHints cmykHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, cmykImageLayout);
            cmykHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            ParameterBlockJAI pb = new ParameterBlockJAI("format");
            pb.addSource(src);
            pb.setParameter("datatype", src.getSampleModel().getDataType());
            PlanarImage op = JAI.create("format", pb, cmykHints);

            // invert CMYK pixel value
            pb = new ParameterBlockJAI("invert");
            pb.addSource(src);
            op = JAI.create("invert", pb, cmykHints);

            // -- Convert CMYK to RGB
            ColorSpace rgbColorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel rgbColorModel = RasterFactory.createComponentColorModel(DataBuffer.TYPE_BYTE, rgbColorSpace,
                                                                               false, true, Transparency.OPAQUE);
            ImageLayout rgbImageLayout = new ImageLayout();
            rgbImageLayout.setSampleModel(rgbColorModel.createCompatibleSampleModel(op.getWidth(), op.getHeight()));
            RenderingHints rgbHints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, rgbImageLayout);
            rgbHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            pb = new ParameterBlockJAI("colorconvert");
            pb.addSource(op);
            pb.setParameter("colormodel", rgbColorModel);

            return JAI.create("colorconvert", pb, rgbHints);
        }// endif
    }

    public static PlanarImage convertIndexColorModel2RGB(PlanarImage src) {
        ColorModel cm = src.getColorModel();
        /**
         * IndexColorModel – works with pixel values consisting of a single sample that is an index into a fixed
         * colormap in the default sRGB ColorSpace. The colormap specifies red, green, blue, and optional alpha
         * components corresponding to each index.
         */
        // 索引调色板， 我们可以删除alpha通道。因此， 重要从索引色中获得RGB即可,这个主要针对GIF图像
        if (cm instanceof IndexColorModel) {
            // Retrieve the IndexColorModel
            IndexColorModel icm = (IndexColorModel) src.getColorModel();

            int newNumBands = icm.getNumComponents();
            if (newNumBands < 3) {
                throw new IllegalArgumentException("The number of Components of this image is less than 3");
            }
            // Cache the number of elements in each band of the colormap.
            int mapSize = icm.getMapSize();

            // Allocate an array for the lookup table data.
            byte[][] lutData = new byte[newNumBands][mapSize];

            // Load the lookup table data from the IndexColorModel.
            icm.getReds(lutData[0]);
            icm.getGreens(lutData[1]);
            icm.getBlues(lutData[2]);
            if (newNumBands == 4) {
                icm.getAlphas(lutData[3]);
            }

            // Create the lookup table customer .
            LookupTableJAI lut = new LookupTableJAI(lutData);

            // Replace the original image with the 3-band RGB image.
            src = JAI.create("lookup", src, lut);
        }

        return src;
    }

    /**
     * <pre>
     * 为了降低存储空间， 没有必要保持jpeg alpha通道. 如果
     * see: http://www.faqs.org/faqs/jpeg-faq/part1/section-12.html
     * </pre>
     * 
     * @param src
     * @return
     */
    public static PlanarImage convertRGBA2RGB(PlanarImage src) {
        ColorModel cm = src.getColorModel();

        // RGB并且为四通道， 删除掉alpha通道
        if (cm.getColorSpace().getType() == ColorSpace.TYPE_RGB && src.getNumBands() == 4) {
            if(!cm.getColorSpace().isCS_sRGB()) {
                src = generalColorConvert(src);
            }
            
            ParameterBlock pb = new ParameterBlock();
            int[] bitsRGB = { 8, 8, 8 };
            ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel cmRGB = new ComponentColorModel(sRGB, bitsRGB, false, false, Transparency.OPAQUE,
                                                       DataBuffer.TYPE_BYTE);
            ImageLayout il = new ImageLayout();

            il.setColorModel(cmRGB);

            RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);

            il.setSampleModel(cmRGB.createCompatibleSampleModel(src.getTileWidth(), src.getTileHeight()));
            pb = new ParameterBlock();
            pb.addSource(src);
            src = JAI.create("format", pb, rh);
        }

        return src;
    }

    /**
     * 这个转灰度图像为RGB，利用公式 r = gray, g = gray, b = gray 来做的转换
     * 
     * @param src
     * @return
     */
    public static PlanarImage convertGray2RGB(PlanarImage src) {
        ColorSpace srcCS = src.getColorModel().getColorSpace();
        if(srcCS.getType() == ColorSpace.TYPE_GRAY) {
            int x = 3;
            int y = src.getSampleModel().getNumBands() + 1;
            double[][] matrix = new double[x][y];
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    if (j == 0) {
                        matrix[i][j] = 1.0D;
                    } else {
                        matrix[i][j] = 0.0D;
                    }
                }
            }

            ParameterBlock pb = new ParameterBlock();
            pb.addSource(src);
            pb.add(matrix);
            src = JAI.create("bandcombine", pb, null);
        }

        return src;
    }

}
