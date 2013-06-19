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
package com.alibaba.simpleimage.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.alibaba.simpleimage.codec.jpeg.JPEGMarkerException;

/**
 * @author wendell
 */
public class ImageBitsInputStream implements ImageInputStream {

    private InputStream in;

    // bits buffer
    private int         b   = 0;
    private int         cnt = 0;

    public ImageBitsInputStream(InputStream input){
        in = input;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#close()
     */
    public void close() throws IOException {
        in.close();
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#read()
     */
    public int read() throws IOException {
        cnt = 0;

        return in.read();
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        cnt = 0;

        return in.read(b, off, len);
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        cnt = 0;

        return in.read(b);
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#readBit()
     */
    public int readBit() throws IOException, JPEGMarkerException {
        int bit = 0;

        if (cnt == 0) {
            b = in.read();
            cnt = 8;

            if (b == 0xFF) {
                // Loop here to discard any padding FF's on terminating marker
                do {
                    b = in.read();
                } while (b == 0xFF);

                if (b == 0) {
                    /* Found FF/00, which represents an FF data byte */
                    b = 0xFF;
                    cnt = 8;
                } else {
                    throw new JPEGMarkerException(b);
                }
            } else if (b == -1) {
                throw new JPEGMarkerException(b);
            }
        }

        bit = b >> 7;
        cnt--;
        b = b << 1;

        return bit & 0x1;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#readBits(int)
     */
    public long readBits(int bit) throws IOException, JPEGMarkerException {
        if (bit < 0) {
            throw new IllegalArgumentException("bit must be greater than zero");
        }

        int i = 0, v = 0;
        while (i != bit) {
            i++;
            v = (v << 1) | readBit();
        }

        return v;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#skipBytes(int)
     */
    public int skipBytes(int n) throws IOException {
        cnt = 0;

        int total = 0;
        int cur = 0;

        while ((total < n) && ((cur = (int) in.skip(n - total)) > 0)) {
            total += cur;
        }

        return total;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#readUnsignedShort()
     */
    public int readUnsignedShort() throws IOException {
        cnt = 0;

        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) throw new EOFException();

        return (ch1 << 8) + (ch2 << 0);
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#readShort()
     */
    public short readShort() throws IOException {
        cnt = 0;

        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) throw new EOFException();

        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.jpeg.ImageInputStream#readByte()
     */
    public byte readByte() throws IOException {
        cnt = 0;

        int ch = in.read();
        if (ch < 0) throw new EOFException();

        return (byte) (ch);
    }

    public void resetBuffer() throws IOException {
        cnt = 0;
    }
}
