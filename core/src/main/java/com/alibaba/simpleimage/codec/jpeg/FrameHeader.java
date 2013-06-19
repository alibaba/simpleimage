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

public class FrameHeader {

    private boolean     baseline;
    private boolean     progressiveMode;

    private int         LF;             // frame header length
    private int         P;              // Sample Precision (from the orignal image)
    private int         Y;              // Number of lines(actual height)
    private int         X;              // Number of samples per line (actual width)
    private int         Nf;             // Number of component in the frame

    private Component[] components;     // store using component index not ID

    public FrameHeader(boolean baseline, boolean progressiveMode){
        this.baseline = baseline;
        this.progressiveMode = progressiveMode;
    }

    public Component[] getComponents() {
        return this.components;
    }

    public Component getComponentByIndex(int index) {
        return components[index];
    }

    public Component getComponentByID(int ID) {
        for (int c = 0; c < components.length; c++) {
            if (components[c].getC() == ID) {
                return components[c];
            }
        }

        return null;
    }

    public void setComponents(Component[] components) {
        this.components = components;
    }

    public boolean isBaseline() {
        return baseline;
    }

    public void setBaseline(boolean baseline) {
        this.baseline = baseline;
    }

    public boolean isProgressiveMode() {
        return progressiveMode;
    }

    public void setProgressiveMode(boolean progressiveMode) {
        this.progressiveMode = progressiveMode;
    }

    public int getLF() {
        return LF;
    }

    public void setLF(int lf) {
        LF = lf;
    }

    public int getP() {
        return P;
    }

    public void setP(int p) {
        P = p;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getNf() {
        return Nf;
    }

    public void setNf(int nf) {
        Nf = nf;
    }
}
