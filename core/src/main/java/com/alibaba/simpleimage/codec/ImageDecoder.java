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
package com.alibaba.simpleimage.codec;

import java.io.IOException;
import java.util.Iterator;

import com.alibaba.simpleimage.ImageWrapper;

/**
 * TODO Comment of Decoder
 * 
 * @author wendell
 */
public interface ImageDecoder {

    public ImageWrapper decode() throws IOException;

    public boolean addExtendHeaderReader(int marker, ExtendImageHeaderReader reader);

    public boolean removeExtendHeaderReader(int marker);

    public Iterator<ExtendImageHeaderReader> getExtendHeaderReaders();
}
