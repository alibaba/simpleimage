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

/**
 * @author wendell
 */
public class Component {

    private int       index;
    private int       C;                   // Component identifier
    private int       H           = 1;     // Horizontal sampling factor
    private int       V           = 1;     // Vertical sampling factor
    private int       Tq;                  // Quantization table destination selector
    private int       HorizonDCTScaledSize;
    private int       VerticaDCTScaledSize;

    // runtime var
    HuffmanTable      dcHuffTable;         // DC Huffman table used by this component
    HuffmanTable      acHuffTable;         // AC Huffman table used by this component
    QuantizationTable qTable;              // Quantization table used by this component
    int               sampleTimes = 1;     // 

    public int getC() {
        return C;
    }

    public void setC(int c) {
        C = c;
    }

    public int getH() {
        return H;
    }

    public void setH(int h) {
        H = h;
    }

    public int getV() {
        return V;
    }

    public void setV(int v) {
        V = v;
    }

    public int getTq() {
        return Tq;
    }

    public void setTq(int tq) {
        Tq = tq;
    }

    public HuffmanTable getDcHuffTable() {
        return dcHuffTable;
    }

    public void setDcHuffTable(HuffmanTable dcHuffTable) {
        this.dcHuffTable = dcHuffTable;
    }

    public HuffmanTable getAcHuffTable() {
        return acHuffTable;
    }

    public void setAcHuffTable(HuffmanTable acHuffTable) {
        this.acHuffTable = acHuffTable;
    }

    public QuantizationTable getQTable() {
        return qTable;
    }

    public void setQTable(QuantizationTable table) {
        qTable = table;
    }

    /**
     * @return the sampleTimes
     */
    public int getSampleTimes() {
        return sampleTimes;
    }

    public void setSampleTimes(int st) {
        this.sampleTimes = st;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return the horizonDCTScaledSize
     */
    public int getHorizonDCTScaledSize() {
        return HorizonDCTScaledSize;
    }

    /**
     * @param horizonDCTScaledSize the horizonDCTScaledSize to set
     */
    public void setHorizonDCTScaledSize(int horizonDCTScaledSize) {
        HorizonDCTScaledSize = horizonDCTScaledSize;
    }

    /**
     * @return the verticaDCTScaledSize
     */
    public int getVerticaDCTScaledSize() {
        return VerticaDCTScaledSize;
    }

    /**
     * @param verticaDCTScaledSize the verticaDCTScaledSize to set
     */
    public void setVerticaDCTScaledSize(int verticaDCTScaledSize) {
        VerticaDCTScaledSize = verticaDCTScaledSize;
    }
}
