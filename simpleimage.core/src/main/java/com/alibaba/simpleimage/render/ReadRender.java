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
package com.alibaba.simpleimage.render;

import java.io.InputStream;

import javax.media.jai.PlanarImage;

import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.util.ImageColorConvertHelper;
import com.alibaba.simpleimage.util.ImageReadHelper;

/**
 * ReadRender 支持多种方式读取图片，同时进行颜色空间转换（转成RGB） 目前支持的格式有 JPEG 包括 CMYK和RGB颜色空间 PNG 包括
 * 16位、256位颜色，黑白，单一色和真色的PNG GIF 包括GIF 87a和GIF 89a BMP TIFF 包括 16位、256位，灰度等
 * 
 * @author wendell
 */
public class ReadRender extends ImageRender {

    protected InputStream inStream;
    protected boolean     tosRGBColorSpace = true;
    protected boolean     needClean        = false;

    public ReadRender(InputStream input, boolean tosRGBColorSpace) {
        super(null);
        this.inStream = input;
        this.tosRGBColorSpace = tosRGBColorSpace;
    }

    public ReadRender(InputStream input) {
        this(input, true);
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.decorator.ImageRender#render()
     */
    @Override
    public ImageWrapper render() throws SimpleImageException {
        try {
            ImageWrapper imgWrapper;
            if (inStream == null) {
                throw new SimpleImageException("No input set");
            }

            imgWrapper = ImageReadHelper.read(inStream);

            if (tosRGBColorSpace) {
                for (int i = 0; i < imgWrapper.getNumOfImages(); i++) {
                    PlanarImage img = ImageColorConvertHelper.convert2sRGB(imgWrapper
                            .getAsPlanarImage(i));
                    imgWrapper.setImage(i, img);
                }
            }

            return imgWrapper;
        } catch (Exception e) {
            throw new SimpleImageException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.decorator.ImageRender#dispose()
     */
    @Override
    public void dispose() throws SimpleImageException {
        super.dispose();
    }
}
