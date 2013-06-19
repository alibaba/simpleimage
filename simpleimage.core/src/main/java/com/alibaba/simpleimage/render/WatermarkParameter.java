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

import com.alibaba.simpleimage.ImageWrapper;

/**
 * WatermarkParameter
 * 
 * @author wendell
 */
public class WatermarkParameter {

    /**
     * 水印图片
     */
    private ImageWrapper watermark;
    /**
     * 透明度
     */
    private float        alpha = 1.0f;
    /**
     * 水印到横坐标
     */
    private int          x     = 0;
    /**
     * 水印到纵坐标
     */
    private int          y     = 0;

    /**
     * @param src2
     * @param param1
     * @param param2
     * @param param3
     */
    public WatermarkParameter(ImageWrapper watermark, float alpha, int x, int y){
        super();
        setWatermark(watermark);
        setAlpha(alpha);
        setX(x);
        setY(y);
    }

    /**
     * 
     */
    public WatermarkParameter(ImageWrapper watermark){
        super();
        setWatermark(watermark);
    }

    
    /**
     * @return the watermark
     */
    public ImageWrapper getWatermark() {
        return watermark;
    }

    
    /**
     * @param watermark the watermark to set
     */
    public void setWatermark(ImageWrapper watermark) {
        if(watermark == null) {
            throw new IllegalArgumentException("Watermark must not be null");
        }
        this.watermark = watermark;
    }

    
    /**
     * @return the alpha
     */
    public float getAlpha() {
        return alpha;
    }

    
    /**
     * @param alpha the alpha to set
     */
    public void setAlpha(float alpha) {
        if(alpha > 1.0f || alpha < 0.0f) {
            throw new IllegalArgumentException("Alpha must be in [0.0, 1.0]");
        }
        this.alpha = alpha;
    }

    
    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    
    /**
     * @param x the x to set
     */
    public void setX(int x) {
        if(x < 0) {
            throw new IllegalArgumentException("x must be greater than 0");
        }
        this.x = x;
    }

    
    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    
    /**
     * @param y the y to set
     */
    public void setY(int y) {
        if(y < 0) {
            throw new IllegalArgumentException("y must be greater than 0");
        }
        this.y = y;
    }
}
