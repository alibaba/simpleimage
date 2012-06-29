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

/**
 * 缩放参数定义
 * 
 * @author leon
 */
public class ScaleParameter {
    /**
     *最大宽度限制
     */
    private int       maxWidth  = 1024;
    /**
     * 最大高度限制
     */
    private int       maxHeight = 1024;

    /**
     * 缩略算法选择
     */
    private Algorithm algorithm = Algorithm.AUTO;

    public ScaleParameter(){

    }

    public ScaleParameter(int maxWidth, int maxHeight){
        super();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    /**
     * @param stretch
     * @param maxWidth
     * @param maxHeight
     * @param algorithm
     */
    public ScaleParameter(int maxWidth, int maxHeight, Algorithm algorithm){
        super();
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.algorithm = algorithm;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    /**
     * @return the algorithm
     */
    public Algorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public static enum Algorithm {
        INTERP_BICUBIC_2, INTERP_BICUBIC, SUBSAMPLE_AVG, PROGRESSIVE, LANCZOS, AUTO
    }
}
