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

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.util.ImageScaleHelper;

public class ScaleRender extends ImageRender {

    private ScaleParameter zoom       = null;
    private ImageWrapper   imgWrapper = null;

    public ScaleRender(ImageRender imageRender, ScaleParameter p){
        super(imageRender);
        this.zoom = p;
    }

    public ScaleRender(ImageWrapper imgWrapper, ScaleParameter p){
        super(null);
        this.zoom = p;
        this.imgWrapper = imgWrapper;
    }

    public ScaleRender(InputStream input, ScaleParameter param){
        super(new ReadRender(input));
        this.zoom = param;
    }

    public ScaleRender(InputStream input, boolean tosRGBColorSpace, ScaleParameter param){
        super(new ReadRender(input, tosRGBColorSpace));
        this.zoom = param;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.ImageRender#dispose()
     */
    @Override
    public void dispose() throws SimpleImageException {
        super.dispose();

        this.zoom = null;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.ImageRender#render()
     */
    @Override
    public ImageWrapper render() throws SimpleImageException {
        if(imgWrapper == null) {
            imgWrapper = imageRender.render();
        }
        
        if (zoom == null) {
            return imgWrapper;
        }
        
        if(imgWrapper.getImageFormat() == ImageFormat.GIF) {
            imgWrapper = ImageScaleHelper.scaleGIF(imgWrapper, zoom);
        } else {
            PlanarImage img = ImageScaleHelper.scale(imgWrapper.getAsPlanarImage(), zoom);
            imgWrapper.setImage(img);
        }

        return imgWrapper;
    }

    /**
     * @return the zoom
     */
    public ScaleParameter getZoom() {
        return zoom;
    }

    /**
     * @param zoom the zoom to set
     */
    public void setZoom(ScaleParameter zoom) {
        this.zoom = zoom;
    }
}
