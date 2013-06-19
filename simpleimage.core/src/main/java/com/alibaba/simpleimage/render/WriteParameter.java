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

import java.util.Map;

public class WriteParameter {
    /**
     * 用户定义的元数据
     */
    private Map<String, String> userMeta       = null;
    // Used by JPEG
    private float               defaultQuality = 0.75f;
    private int[]               horizontalSamp = new int[4];
    private int[]               verticalSamp   = new int[4];    
    private boolean             qualitySet     = false;
    private boolean             samplingSet    = false;
    private QuantAlgorithm      quantAlgorithm = QuantAlgorithm.OctTree;

    public WriteParameter(Map<String, String> userMeta) {
        super();
        this.userMeta = userMeta;
    }

    public WriteParameter(QuantAlgorithm quantAlg) {
        this.quantAlgorithm = quantAlg;
    }

    public WriteParameter() {
        super();
    }

    public boolean isQualitySet() {
        return this.qualitySet;
    }

    public boolean isSamplingSet() {
        return this.samplingSet;
    }

    public void reset() {
        qualitySet = false;
        samplingSet = false;

        for (int i = 0; i < 4; i++) {
            horizontalSamp[i] = 0;
            verticalSamp[i] = 0;
        }
    }

    public void setHorizontalSubsampling(int component, int subsample) {
        samplingSet = true;
        horizontalSamp[component] = subsample;
    }

    public void setVerticalSubsampling(int component, int subsample) {
        samplingSet = true;
        verticalSamp[component] = subsample;
    }

    public int getVerticalSubsampling(int component) {
        return verticalSamp[component];
    }

    public int getHorizontalSubsampling(int component) {
        return horizontalSamp[component];
    }

    public Map<String, String> getUserMeta() {
        return userMeta;
    }

    public void setUserMeta(Map<String, String> userMeta) {
        this.userMeta = userMeta;
    }

    public float getDefaultQuality() {
        return defaultQuality;
    }

    public void setDefaultQuality(float defaultQuality) {
        qualitySet = true;
        this.defaultQuality = defaultQuality;
    }
    
    /**
     * @return the quantAlgorithm
     */
    public QuantAlgorithm getQuantAlgorithm() {
        return quantAlgorithm;
    }
    
    /**
     * @param quantAlgorithm the quantAlgorithm to set
     */
    public void setQuantAlgorithm(QuantAlgorithm quantAlgorithm) {
        this.quantAlgorithm = quantAlgorithm;
    }

    public enum QuantAlgorithm {
        OctTree, NeuQuant, MedianCut 
    }
}
