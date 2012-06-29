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
 * TODO Comment of YCCK2CMYKColorConvertor
 * 
 * @author wendell
 */
public class YCCK2CMYKColorConvertor extends MapColorConvertor {

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convert(int[], int)
     */
    @Override
    public long convert(int[] input, int inPos) {
        int tempC, tempM, tempY, tempK, Y, Cb, Cr, K;

        Y = input[inPos++] & 0xFF;
        Cb = input[inPos++] & 0xFF;
        Cr = input[inPos++] & 0xFF;
        K = input[inPos] & 0xFF;

        tempC = 255 - sampleRangeLimitTable[sampleRangeLimitOffset + MAXJSAMPLE - (Y + Cr2R[Cr])];
        tempM = 255 - sampleRangeLimitTable[sampleRangeLimitOffset + MAXJSAMPLE - (Y + ((Cb2G[Cb] + Cr2G[Cr]) >> 16))];
        tempY = 255 - sampleRangeLimitTable[sampleRangeLimitOffset + MAXJSAMPLE - (Y + Cb2B[Cb])];
        tempK = 255 - K;

        return (tempC << 24) | ((tempM << 16) | (tempY << 8) | (tempK & 0xFFL));
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convertArray(int[], int, byte[], int)
     */
    @Override
    public byte[] convertBlock(int[] input, int inPos, byte[] output, int numOfComponents, int startCoordinate,
                               int row, int scanlineStride) {
        int index = 0, inputOffset = inPos, bounds = 0;
        int Y, Cb, Cr, K;
        int len = output.length;

        for (int i = 0; i < DCTSIZE; i++) {
            index = startCoordinate + i * scanlineStride;
            bounds = row * scanlineStride;

            for (int j = 0; j < DCTSIZE; j++) {
                Y = input[inputOffset++] & 0xFF;
                Cb = input[inputOffset++] & 0xFF;
                Cr = input[inputOffset++] & 0xFF;
                K = input[inputOffset++] & 0xFF;

                if (index >= len) {
                    return output;
                }

                if (index < bounds) {
                    output[index++] = (byte) (255 - sampleRangeLimitTable[sampleRangeLimitOffset + MAXJSAMPLE
                                                                          - (Y + Cr2R[Cr])]);
                    output[index++] = (byte) (255 - sampleRangeLimitTable[sampleRangeLimitOffset + MAXJSAMPLE
                                                                          - (Y + ((Cb2G[Cb] + Cr2G[Cr]) >> 16))]);
                    output[index++] = (byte) (255 - sampleRangeLimitTable[sampleRangeLimitOffset + MAXJSAMPLE
                                                                          - (Y + Cb2B[Cb])]);
                    output[index++] = (byte) (255 - K);
                }
            }

            row++;
        }

        return output;
    }
}
