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

import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.util.ImageCropHelper;

public class CropRender extends ImageRender {

    public CropRender(ImageRender imageRender){
        super(imageRender);
    }

    private CropParameter cropParameter = null;
    private ImageWrapper  imgWrapper    = null;

    public CropRender(ImageRender imageRender, CropParameter param) {
        super(imageRender);
        this.cropParameter = param;
    }

    public CropRender(ImageWrapper imgWrapper, CropParameter param) {
        super(null);
        this.imgWrapper = imgWrapper;
        this.cropParameter = param;
    }

    public CropRender(InputStream input, CropParameter param){
        super(new ReadRender(input));
        this.cropParameter = param;
    }

    @Override
    public void dispose() throws SimpleImageException {
        super.dispose();
        cropParameter = null;
    }

    @Override
    public ImageWrapper render() throws SimpleImageException {
        if (cropParameter == null) {
            if(imgWrapper == null) {
                return imageRender.render();
            }
            
            return imgWrapper;
        }
        
        if(imgWrapper == null) {
            imgWrapper = imageRender.render();
        }
        
        for (int i = 0; i < imgWrapper.getNumOfImages(); i++) {
            PlanarImage pi = ImageCropHelper.crop(imgWrapper.getAsPlanarImage(i), this.cropParameter);
            imgWrapper.setImage(i, pi);
        }
        
        return imgWrapper;
    }

    public CropParameter getCropParameter() {
        return cropParameter;
    }

    public void setCropParameter(CropParameter cropParameter) {
        this.cropParameter = cropParameter;
    }

}
