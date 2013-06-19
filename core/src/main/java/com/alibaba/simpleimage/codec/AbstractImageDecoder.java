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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.codec.jpeg.CalculateConsts;

/**
 * @author wendell
 */
public abstract class AbstractImageDecoder implements ImageDecoder, CalculateConsts {

    protected Map<Integer, ExtendImageHeaderReader> extendImageHeaderReaders = new HashMap<Integer, ExtendImageHeaderReader>();

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.Decoder#addExtendHeaderReader(int,
     * com.alibaba.simpleimage.jpeg.ExtendImageHeaderReader)
     */
    public boolean addExtendHeaderReader(int marker, ExtendImageHeaderReader reader) {
        extendImageHeaderReaders.put(marker, reader);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.Decoder#decode()
     */
    public abstract ImageWrapper decode() throws IOException;

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.Decoder#getExtendHeaderReaders()
     */
    public Iterator<ExtendImageHeaderReader> getExtendHeaderReaders() {
        return extendImageHeaderReaders.values().iterator();
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.codec.Decoder#removeExtendHeaderReader(int)
     */
    public boolean removeExtendHeaderReader(int marker) {
        return extendImageHeaderReaders.remove(marker) != null;
    }
}
