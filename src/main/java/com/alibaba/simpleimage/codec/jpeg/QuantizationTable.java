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

public class QuantizationTable {

    private int   Lq; // Quantization table definition length
    private int   Pq; // Quantization table element precision
    private int   Tq; // Quantization table destination identifier
    private int[] Q; // Quantization table elements (in natural order)

    public QuantizationTable(int[] q){
        this.Q = q;
    }

    public int getLq() {
        return Lq;
    }

    public void setLq(int lq) {
        Lq = lq;
    }

    public int getPq() {
        return Pq;
    }

    public void setPq(int pq) {
        Pq = pq;
    }

    public int getTq() {
        return Tq;
    }

    public void setTq(int tq) {
        Tq = tq;
    }

    public int[] getQ() {
        return Q;
    }

    public void setQ(int[] q) {
        Q = q;
    }
}
