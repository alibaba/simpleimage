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
package com.alibaba.simpleimage.codec.jpeg.ext;

import java.io.IOException;

import com.alibaba.simpleimage.codec.ExtendImageHeaderReader;
import com.alibaba.simpleimage.io.ImageInputStream;

/**
 * TODO Comment of AdobeHeaderReader
 * 
 * @author wendell
 */
public class AdobeHeaderReader implements ExtendImageHeaderReader {

    /*
     * (non-Javadoc)
     * @see
     * com.alibaba.simpleimage.jpeg.ExtendImageHeaderReader#readProperties(com.alibaba.simpleimage.jpeg.ImageInputStream
     * , com.alibaba.simpleimage.jpeg.ExtendImageHeader)
     */
    public void readProperties(ImageInputStream in, int len, ExtendImageHeader imageHeader) throws IOException {
        int transform = 0, numToRead = 0;

        if (len >= 14) {
            numToRead = 14;
        } else if (len > 0) {
            numToRead = len;
        } else {
            numToRead = 0;
        }

        byte[] datas = new byte[numToRead];

        in.read(datas);
        len -= numToRead;

        if (numToRead >= 12 && datas[0] == 0x41 && datas[1] == 0x64 && datas[2] == 0x6F && datas[3] == 0x62
            && datas[4] == 0x65) {
            // Found Adobe APP14 marker
            transform = datas[11];

            imageHeader.setSawAdobeMarker(true);
            imageHeader.setAdobeTransform(transform);
        }

        // skip any remaining data
        if (len > 0) {
            in.skipBytes(len);
        }
    }

}
