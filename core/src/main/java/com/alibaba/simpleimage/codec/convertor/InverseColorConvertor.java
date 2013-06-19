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
 * TODO Comment of InverseColorConvertor
 * 
 * @author wendell
 */
public class InverseColorConvertor implements ColorConvertor {

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convert(int[], int)
     */
    public long convert(int[] input, int inPos) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.util.ColorConvertor#convertBlock(int[], int, byte[], int, int, int)
     */
    public byte[] convertBlock(int[] input, int inPos, byte[] output, int numOfComponents, int startCoordinate,
                               int row, int scanlineStride) {
        int index = 0, inputOffset = 0, bounds = 0;
        int len = output.length;

        for (int i = 0; i < DCTSIZE; i++) {
            index = startCoordinate + i * scanlineStride;
            bounds = row * scanlineStride;

            for (int j = 0; j < DCTSIZE; j++) {
                if (index >= len) {
                    return output;
                }

                if (index < bounds) {
                    for (int c = 0; c < numOfComponents; c++) {
                        output[index++] = (byte) (255 - (input[inputOffset++] & 0xFF));
                    }
                } else {
                    inputOffset += numOfComponents;
                }
            }

            row++;
        }

        return output;
    }

}
