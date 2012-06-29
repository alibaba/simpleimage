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

import java.io.IOException;

import com.alibaba.simpleimage.codec.jpeg.JPEGMarkerException;

/**
 * @author wendell
 */
public interface ImageInputStream {

    int read() throws IOException;

    int readUnsignedShort() throws IOException;

    int read(byte[] b, int off, int len) throws IOException;

    int read(byte[] b) throws IOException;

    int readBit() throws IOException, JPEGMarkerException;

    short readShort() throws IOException;

    byte readByte() throws IOException;

    long readBits(int bit) throws IOException, JPEGMarkerException;

    int skipBytes(int n) throws IOException;

    void close() throws IOException;

    void resetBuffer() throws IOException;
}
