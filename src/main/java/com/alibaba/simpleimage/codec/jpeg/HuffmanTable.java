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
package com.alibaba.simpleimage.codec.jpeg;

import java.io.IOException;

import com.alibaba.simpleimage.io.ImageInputStream;

public class HuffmanTable {

    public static final int TYPE_DC  = 0;
    public static final int TYPE_AC  = 1;

    // raw data
    private int             Lh;                                                                                   // Huffman
    private int             Tc;                                                                                   // Table
    private int             Th;                                                                                   // Huffman
    private int[]           L;                                                                                    // bits
    private int[]           V;                                                                                    // huffval

    // general data
    private int[]           mincode  = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
    private int[]           maxcode  = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
    private int[]           valptr   = new int[16 + 1];
    private int[]           huffcode = new int[256 + 1];
    private int[]           huffsize = new int[256 + 1];

    public HuffmanTable(int Tc, int Th, int[] bits, int[] huffval){
        this.Tc = Tc;
        this.Th = Th;
        this.L = bits;
        this.V = huffval;

        init();
    }

    /**
     *TODO JPEG Specification 112
     */
    private void init() {
        generalHuffsize();
        generalHuffcode();

        int i = 0, j = 0;
        for (;;) {
            i++;

            if (i > 16) {
                break;
            }

            if (L[i] == 0) {
                maxcode[i] = -1;
                continue;
            } else {
                valptr[i] = j;
                mincode[i] = huffcode[j];
                j = j + L[i] - 1;
                maxcode[i] = huffcode[j];
                j++;
            }
        }

        maxcode[17] = 0xFFFFFF;
    }

    private void generalHuffcode() {
        int k = 0, code = 0, si = huffsize[0];

        for (;;) {
            do {
                huffcode[k] = code;
                code++;
                k++;
            } while (huffsize[k] == si);

            if (huffsize[k] == 0) {
                break;
            }

            do {
                code = code << 1;
                si++;
            } while (huffsize[k] != si);
        }
    }

    private void generalHuffsize() {
        int k = 0, i = 1, j = 1;
        do {
            while (j <= L[i]) {
                huffsize[k] = i;
                k++;
                j++;
            }

            i++;
            j = 1;
        } while (i <= 16);

        huffsize[k] = 0;
    }

    public int decode(ImageInputStream in) throws IOException, JPEGMarkerException {
        int i = 1, j = 0, code = 0, value = 0;
        code = in.readBit();

        for (;;) {
            if (code > maxcode[i]) {
                i++;
                code = (code << 1) | in.readBit();
            } else {
                break;
            }
        }

        j = valptr[i];
        j = j + code - mincode[i];
        value = V[j];

        return value;
    }

    public int extend(int diff, int t) {
        int Vt = 1 << (t - 1); // source logic is Vt = Math.pow(2, t-1);

        if (diff < Vt) {
            Vt = (-1 << t) + 1;
            diff = diff + Vt;
        }

        return diff;
    }

    public int getLh() {
        return Lh;
    }

    public void setLh(int lh) {
        Lh = lh;
    }

    public int getTc() {
        return Tc;
    }

    public void setTc(int tc) {
        Tc = tc;
    }

    public int getTh() {
        return Th;
    }

    public void setTh(int th) {
        Th = th;
    }

    public int[] getL() {
        return L;
    }

    public void setL(int[] l) {
        L = l;
    }

    public int[] getV() {
        return V;
    }

    public void setV(int[] v) {
        V = v;
    }
}
