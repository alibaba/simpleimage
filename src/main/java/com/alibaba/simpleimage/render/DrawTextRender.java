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

import java.awt.image.BufferedImage;
import java.io.InputStream;

import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.util.ImageDrawHelper;

public class DrawTextRender extends ImageRender {

    private DrawTextParameter drawTextParameter = null;
    private ImageWrapper      imgWrapper        = null;

    public DrawTextRender(ImageRender imageRender, DrawTextParameter param){
        super(imageRender);
        this.drawTextParameter = param;
    }

    public DrawTextRender(ImageWrapper imgWrapper, DrawTextParameter param){
        super(null);
        this.drawTextParameter = param;
        this.imgWrapper = imgWrapper;
    }

    public DrawTextRender(InputStream input, DrawTextParameter param){
        super(new ReadRender(input));
        this.drawTextParameter = param;
    }

    public DrawTextRender(InputStream input, boolean tosRGBColorSpace, DrawTextParameter param){
        super(new ReadRender(input, tosRGBColorSpace));
        this.drawTextParameter = param;
    }

    @Override
    public void dispose() throws SimpleImageException {
        super.dispose();

        drawTextParameter = null;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.ImageRender#render()
     */
    @Override
    public ImageWrapper render() throws SimpleImageException {
        if (drawTextParameter == null || drawTextParameter.getTextInfo() == null
            || drawTextParameter.getTextInfo().size() == 0) {
            if (imgWrapper == null) {
                return imageRender.render();
            }

            return imgWrapper;
        }

        if (imgWrapper == null) {
            imgWrapper = imageRender.render();
        }

        for (int i = 0; i < imgWrapper.getNumOfImages(); i++) {
            BufferedImage bi = imgWrapper.getAsBufferedImage(i);
            ImageDrawHelper.drawText(bi, this.drawTextParameter);
            imgWrapper.setImage(i, bi);
        }

        return imgWrapper;
    }

    /**
     * @return the drawTextParameter
     */
    public DrawTextParameter getDrawTextParameter() {
        return drawTextParameter;
    }

    /**
     * @param drawTextParameter the drawTextParameter to set
     */
    public void setDrawTextParameter(DrawTextParameter drawTextParameter) {
        this.drawTextParameter = drawTextParameter;
    }
}
