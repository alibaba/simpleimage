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
package com.alibaba.simpleimage;

import com.alibaba.simpleimage.SimpleImageException;

/**
 * TODO Comment of ImageRender
 * 
 * @author wendell
 */
public abstract class ImageRender {

    protected ImageRender imageRender;

    /**
     * @param imageRender
     */
    public ImageRender(ImageRender imageRender){
        super();
        this.imageRender = imageRender;
    }

    public abstract ImageWrapper render() throws SimpleImageException;

    public void dispose() throws SimpleImageException {
        if (imageRender != null) {
            imageRender.dispose();
        }
    }
}
