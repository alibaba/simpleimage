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
package com.alibaba.simpleimage.render;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.SimpleImageException;
import com.alibaba.simpleimage.util.ImageWriteHelper;

/**
 * 向流写出encode图形数据。
 * 
 * @author wendell
 */
public class WriteRender extends ImageRender {

    protected OutputStream   stream       = null;
    protected ImageWrapper   image;
    protected boolean        needClean    = false;
    protected WriteParameter param        = null;
    protected ImageFormat    outputFormat = ImageFormat.JPEG;

    public WriteRender(ImageWrapper image, OutputStream output) {
        this(image, output, ImageFormat.JPEG);
    }
    
    public WriteRender(ImageWrapper image, OutputStream output, ImageFormat format){
        this(image, output, format, new WriteParameter());
    }
    
    public WriteRender(ImageWrapper image, OutputStream output, ImageFormat format, WriteParameter param) {
        super(null);
        this.stream = output;
        this.image = image;
        this.outputFormat = format;
        this.param = param;

        validateParameters();
    }

    public WriteRender(ImageWrapper image, File output, ImageFormat format, WriteParameter param) throws SimpleImageException{
        super(null);
        try {
            this.stream = new FileOutputStream(output);
            this.needClean = true;
        } catch (FileNotFoundException e) {
            throw new SimpleImageException(e);
        }

        this.param = param;
        this.image = image;
        this.outputFormat = format;

        validateParameters();
    }
    
    public WriteRender(ImageWrapper image, File output) throws SimpleImageException {
        this(image, output, ImageFormat.JPEG);
    }
    
    public WriteRender(ImageWrapper image, File output, ImageFormat format) throws SimpleImageException {
        this(image, output, format, new WriteParameter());
    }

    public WriteRender(ImageWrapper image, String output, ImageFormat format, WriteParameter param) throws SimpleImageException{
        super(null);
        try {
            this.stream = new FileOutputStream(output);
            this.needClean = true;
        } catch (FileNotFoundException e) {
            throw new SimpleImageException(e);
        }

        this.image = image;
        this.param = param;
        this.outputFormat = format;

        validateParameters();
    }
    
    public WriteRender(ImageWrapper image, String output, ImageFormat format) throws SimpleImageException {
        this(image, output, format, new WriteParameter());
    }
    
    public WriteRender(ImageWrapper image, String output) throws SimpleImageException {
        this(image, output, ImageFormat.JPEG);
    }

    public WriteRender(ImageRender imageRender, OutputStream output, ImageFormat format, WriteParameter param){
        super(imageRender);

        this.stream = output;
        this.param = param;
        this.outputFormat = format;

        this.validateParameters();
    }

    public WriteRender(ImageRender imageRender, OutputStream output, ImageFormat format) {
        this(imageRender, output, format, new WriteParameter());
    }
    
    public WriteRender(ImageRender imageRender, OutputStream output) {
        this(imageRender, output, ImageFormat.JPEG);
    }
    
    public WriteRender(ImageRender imageRender, File output, ImageFormat format, WriteParameter param) throws SimpleImageException{
        super(imageRender);

        try {
            this.stream = new FileOutputStream(output);
            this.needClean = true;
        } catch (FileNotFoundException e) {
            throw new SimpleImageException(e);
        }

        this.param = param;
        this.outputFormat = format;

        this.validateParameters();
    }
    
    public WriteRender(ImageRender imageRender, File output, ImageFormat format) throws SimpleImageException {
        this(imageRender, output, format, new WriteParameter());
    }
    
    public WriteRender(ImageRender imageRender, File output) throws SimpleImageException {
        this(imageRender, output, ImageFormat.JPEG);
    }
    
    public WriteRender(ImageRender imageRender, String output, ImageFormat format, WriteParameter param) throws SimpleImageException{
        super(imageRender);

        try {
            this.stream = new FileOutputStream(output);
            this.needClean = true;
        } catch (FileNotFoundException e) {
            throw new SimpleImageException(e);
        }

        this.param = param;
        this.outputFormat = format;

        this.validateParameters();
    }

    public WriteRender(ImageRender imageRender, String output, ImageFormat format) throws SimpleImageException {
        this(imageRender, output, format, new WriteParameter());
    }
    
    public WriteRender(ImageRender imageRender, String output) throws SimpleImageException {
        this(imageRender, output, ImageFormat.JPEG);
    }
    
    protected void validateParameters() {
        if (stream == null) {
            throw new IllegalArgumentException("Output stream can not be null.");
        }
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.ImageRender#dispose()
     */
    @Override
    public void dispose() throws SimpleImageException {
        super.dispose();

        if (needClean) {
            if (stream != null) {
                try {
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    throw new SimpleImageException(e);
                }
            }
        }

        this.stream = null;
        this.image = null;
        this.param = null;
    }

    /*
     * (non-Javadoc)
     * @see com.alibaba.simpleimage.ImageRender#render()
     */
    @Override
    public ImageWrapper render() throws SimpleImageException {
        try {
            if (image == null) {
                image = imageRender.render();
            }

            ImageWriteHelper.write(image, stream, outputFormat, param);
        } catch (Exception e) {
            throw new SimpleImageException(e);
        }

        return null;
    }
}
