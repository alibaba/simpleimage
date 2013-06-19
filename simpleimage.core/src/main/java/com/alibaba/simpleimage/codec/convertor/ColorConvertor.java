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
public interface ColorConvertor extends CalculateConsts {

    public abstract byte[] convertBlock(int[] input, int inPos, byte[] output, int numOfComponents,
                                        int startCoordinate, int row, int scanlineStride);

    public long convert(int[] input, int inPos);
}
