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
package com.alibaba.simpleimage.codec.convertor;

import com.alibaba.simpleimage.codec.jpeg.CalculateConsts;

/**
 * @author wendell
 */
public abstract class InverseDCTCalculator implements CalculateConsts {

    /**
     * input must be 8x8 integer array quant is a 8x8 QuantizationTable output is one of output destination, the other
     * is return object width is the output array's width height is the output array's height
     * 
     * @param input
     * @param quant
     * @param output
     * @param outputOffset
     * @param width
     * @param height
     * @return
     */
    public abstract Object calculate(int[] input, int inPos, int[] quant, int[] output, int outPos, int width,
                                     int height);
}
