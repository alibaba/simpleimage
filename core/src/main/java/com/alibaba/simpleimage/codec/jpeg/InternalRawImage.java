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
 * TODO Comment of RawImage
 * 
 * @author wendell
 */
public class InternalRawImage {

    private int            width;
    private int            height;
    private int            numOfComponents;
    private JPEGColorSpace colorspace;
    private JPEGColorSpace rawColorspace;
    private byte[]         data;

    public void initData() {
        data = new byte[width * height * numOfComponents];
    }

    /**
     * @return the numOfComponents
     */
    public int getNumOfComponents() {
        return numOfComponents;
    }

    /**
     * @param numOfComponents the numOfComponents to set
     */
    public void setNumOfComponents(int numOfComponents) {
        this.numOfComponents = numOfComponents;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the colorspace
     */
    public JPEGColorSpace getColorspace() {
        return colorspace;
    }

    /**
     * @param colorspace the colorspace to set
     */
    public void setColorspace(JPEGColorSpace colorspace) {
        this.colorspace = colorspace;
    }

    /**
     * @return the rawColorspace
     */
    public JPEGColorSpace getRawColorspace() {
        return rawColorspace;
    }

    /**
     * @param rawColorspace the rawColorspace to set
     */
    public void setRawColorspace(JPEGColorSpace rawColorspace) {
        this.rawColorspace = rawColorspace;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }
}
