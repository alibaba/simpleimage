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
 * TODO Comment of YCbCr2RGBColorConvertor
 * 
 * @author wendell
 */
public class YCbCr2RGBColorConvertor extends MapColorConvertor {

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convert(int[], int)
     */
    @Override
    public long convert(int[] input, int inPos) {
        int Y = input[inPos++] & 0xFF;
        int Cb = input[inPos++] & 0xFF;
        int Cr = input[inPos] & 0xFF;

        byte r = (byte) sampleRangeLimitTable[sampleRangeLimitOffset + Y + Cr2R[Cr]];
        byte g = (byte) sampleRangeLimitTable[sampleRangeLimitOffset + Y + ((Cb2G[Cb] + Cr2G[Cr]) >> 16)];

        byte b = (byte) sampleRangeLimitTable[sampleRangeLimitOffset + Y + Cb2B[Cb]];

        return (0xFF000000L | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convertArray(int[], int, byte[], int)
     */
    @Override
    public byte[] convertBlock(int[] input, int inPos, byte[] output, int numOfComponents, int startCoordinate,
                               int row, int scanlineStride) {
        int index = 0, inputOffset = 0, bounds = 0;
        int Y, Cb, Cr;
        int len = output.length;

        for (int i = 0; i < DCTSIZE; i++) {
            index = startCoordinate + i * scanlineStride;
            bounds = row * scanlineStride;

            for (int j = 0; j < DCTSIZE; j++) {
                Y = input[inputOffset++] & 0xFF;
                Cb = input[inputOffset++] & 0xFF;
                Cr = input[inputOffset++] & 0xFF;

                if (index >= len) {
                    return output;
                }

                if (index < bounds) {
                    output[index++] = (byte) sampleRangeLimitTable[sampleRangeLimitOffset + Y + Cr2R[Cr]];
                    output[index++] = (byte) sampleRangeLimitTable[sampleRangeLimitOffset + Y
                                                                   + ((Cb2G[Cb] + Cr2G[Cr]) >> 16)];
                    output[index++] = (byte) sampleRangeLimitTable[sampleRangeLimitOffset + Y + Cb2B[Cb]];
                }
            }

            row++;
        }

        return output;
    }
}
