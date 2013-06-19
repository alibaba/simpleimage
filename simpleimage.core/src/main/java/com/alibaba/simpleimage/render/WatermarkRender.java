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

import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.util.ImageDrawHelper;

/**
 * TODO Comment of WatermarkRender
 * 
 * @author wendell
 */
public class WatermarkRender extends ImageRender {

    private WatermarkParameter param;
    private ImageWrapper       imageWrapper;

    public WatermarkRender(ImageRender imageRender, WatermarkParameter param){
        super(imageRender);
        this.param = param;
    }

    public WatermarkRender(ImageWrapper srcImage, WatermarkParameter param){
        super(null);
        this.imageWrapper = srcImage;
        this.param = param;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.ImageRender#render(java.awt.image.BufferedImage)
     */
    @Override
    public ImageWrapper render() throws SimpleImageException {
        if(imageWrapper == null) {
            imageWrapper = imageRender.render();
        }
        
        if(param == null) {
            return imageWrapper;
        }
        
        for (int i = 0; i < imageWrapper.getNumOfImages(); i++) {
            BufferedImage img = ImageDrawHelper.drawWatermark(imageWrapper.getAsBufferedImage(i), param);
            imageWrapper.setImage(i, img);
        }

        return imageWrapper;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.ImageRender#dispose()
     */
    @Override
    public void dispose() throws SimpleImageException {
        super.dispose();
        this.param = null;
    }

    /**
     * @return the param
     */
    public WatermarkParameter getParam() {
        return param;
    }

    /**
     * @param param the param to set
     */
    public void setParam(WatermarkParameter param) {
        this.param = param;
    }
}
