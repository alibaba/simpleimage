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
package com.alibaba.simpleimage.jai.scale;

/**
 * TODO Comment of LanczosResizeFilter
 * 
 * @author wendell
 */
public class LanczosResizeFilter {
    private double support;
    private double windowSupport;
    private double scale;
    private double blur;
    private double scaleDivideWS;

    public LanczosResizeFilter() {
        this.support = 3.0;
        this.scale = 1.0;
        this.blur = 1.0;
        this.windowSupport = this.support;
        this.scaleDivideWS = this.scale / this.windowSupport;
    }

    public double filter(double x) {
        if (x == 0.0) {
            return 1.0;
        }
        double tmp = Math.PI * x;

        return Math.sin(tmp) / tmp;
    }

    public double window(double x) {
        if (x == 0.0) {
            return 1.0;
        }
        double tmp = Math.PI * x;

        return Math.sin(tmp) / tmp;
    }
    
    public double getScaleDivedeWindowSupport() {
        return scaleDivideWS;
    }

    /**
     * @return the support
     */
    public double getSupport() {
        return support;
    }

    /**
     * @return the windowSupport
     */
    public double getWindowSupport() {
        return windowSupport;
    }

    /**
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return the blur
     */
    public double getBlur() {
        return blur;
    }

    /**
     * @param support the support to set
     */
    public void setSupport(double support) {
        this.support = support;
    }

    /**
     * @param windowSupport the windowSupport to set
     */
    public void setWindowSupport(double windowSupport) {
        this.windowSupport = windowSupport;
    }

    /**
     * @param scale the scale to set
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * @param blur the blur to set
     */
    public void setBlur(double blur) {
        this.blur = blur;
    }

}
