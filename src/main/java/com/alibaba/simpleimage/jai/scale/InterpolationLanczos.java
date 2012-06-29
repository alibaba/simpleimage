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

import javax.media.jai.Interpolation;

/**
 * TODO Comment of InterpolationLanczos
 * @author wendell
 *
 */
public class InterpolationLanczos extends Interpolation {

    private static final long serialVersionUID = -7851471618630298171L;

    /* (non-Javadoc)
     * @see javax.media.jai.Interpolation#interpolateH(int[], int)
     */
    @Override
    public int interpolateH(int[] samples, int xfrac) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /* (non-Javadoc)
     * @see javax.media.jai.Interpolation#interpolateH(float[], float)
     */
    @Override
    public float interpolateH(float[] samples, float xfrac) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /* (non-Javadoc)
     * @see javax.media.jai.Interpolation#interpolateH(double[], float)
     */
    @Override
    public double interpolateH(double[] samples, float xfrac) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
