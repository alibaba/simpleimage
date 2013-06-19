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

/**
 * @author wendell
 */
public abstract class MapColorConvertor implements ColorConvertor {

    public static final int[] Cr2R = new int[256];
    public static final int[] Cb2B = new int[256];
    public static final int[] Cr2G = new int[256];
    public static final int[] Cb2G = new int[256];

    static {
        int nScale = 1 << 16;
        int nHalf = nScale >> 1;

        for (int i = 0, x = -128; i <= 255; i++, x++) {
            /* i is the actual input pixel value, in the range 0..MAXJSAMPLE */
            /* The Cb or Cr value we are thinking of is x = i - CENTERJSAMPLE */
            /* Cr=>R value is nearest int to 1.40200 x */
            Cr2R[i] = (((int) (1.40200 * nScale + 0.5)) * x + nHalf) >> 16;
            /* Cb=>B value is nearest int to 1.77200 x */
            Cb2B[i] = (((int) (1.77200 * nScale + 0.5)) * x + nHalf) >> 16;
            /* Cr=>G value is scaled-up -0.71414 x */
            Cr2G[i] = ((-(int) (0.71414 * nScale + 0.5) * x));
            /* Cb=>G value is scaled-up -0.34414 x */
            /* We also add in ONE_HALF so that need not do it in inner loop */
            Cb2G[i] = ((-((int) (0.34414 * nScale + 0.5)) * x) + nHalf);
        }
    } // end static statment

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convert(int[], int)
     */
    public abstract long convert(int[] input, int inPos);

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convertArray(int[], int, byte[], int)
     */
    public abstract byte[] convertBlock(int[] input, int inPos, byte[] output, int numOfComponents,
                                        int startCoordinate, int row, int scanlineStride);
}
