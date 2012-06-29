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
package com.alibaba.simpleimage.util;

import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.io.ByteArrayInputStream;
import com.alibaba.simpleimage.io.ByteArrayOutputStream;

/**
 * TODO Comment of SIUtils
 * 
 * @author wendell
 */
public final class ImageUtils {

    private static final float FLOAT_MIN = -Float.MAX_VALUE;

    private ImageUtils(){

    }

    public static void closeQuietly(ImageInputStream inStream) {
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException ignore) {

            }
        }
    }

    public static void closeQuietly(ImageOutputStream outStream) {
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException ignore) {

            }
        }
    }

    public static void closeQuietly(com.alibaba.simpleimage.io.ImageInputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {

            }
        }
    }

    public static void closeQuietly(ImageReader reader) {
        if (reader != null) {
            try {
                reader.dispose();
            } catch (Exception igonre) {

            }
        }
    }

    public static ImageLayout getImageLayoutHint(RenderingHints renderHints) {
        if (renderHints == null) {
            return null;
        } else {
            return (ImageLayout) renderHints.get(JAI.KEY_IMAGE_LAYOUT);
        }
    }

    public static boolean isJPEG(InputStream source) throws IOException {
        InputStream iis = source;

        if (!source.markSupported()) {
            throw new IllegalArgumentException("Input stream must support mark");
        }

        iis.mark(30);
        // If the first two bytes are a JPEG SOI marker, it's probably
        // a JPEG file. If they aren't, it definitely isn't a JPEG file.
        try {
            int byte1 = iis.read();
            int byte2 = iis.read();
            if ((byte1 == 0xFF) && (byte2 == 0xD8)) {

                return true;
            }
        } finally {
            iis.reset();
        }

        return false;
    }

    public static boolean isBMP(InputStream in) throws IOException {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("Input stream must support mark");
        }

        byte[] b = new byte[2];
        try {
            in.mark(30);
            in.read(b);
        } finally {
            in.reset();
        }

        return (b[0] == 0x42) && (b[1] == 0x4d);
    }

    public static boolean isGIF(InputStream in) throws IOException {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("Input stream must support mark");
        }

        byte[] b = new byte[6];

        try {
            in.mark(30);
            in.read(b);
        } finally {
            in.reset();
        }

        return b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8' && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    public static boolean isPNG(InputStream in) throws IOException {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("Input stream must support mark");
        }

        byte[] b = new byte[8];
        try {
            in.mark(30);
            in.read(b);
        } finally {
            in.reset();
        }

        return (b[0] == (byte) 137 && b[1] == (byte) 80 && b[2] == (byte) 78 && b[3] == (byte) 71 && b[4] == (byte) 13
                && b[5] == (byte) 10 && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    public static boolean isTIFF(InputStream in) throws IOException {
        if (!in.markSupported()) {
            throw new IllegalArgumentException("Input stream must support mark");
        }
        byte[] b = new byte[4];
        try {
            in.mark(30);
            in.read(b);
        } finally {
            in.reset();
        }

        return ((b[0] == (byte) 0x49 && b[1] == (byte) 0x49 && b[2] == (byte) 0x2a && b[3] == (byte) 0x00) || (b[0] == (byte) 0x4d
                                                                                                               && b[1] == (byte) 0x4d
                                                                                                               && b[2] == (byte) 0x00 && b[3] == (byte) 0x2a));
    }

    public static ImageFormat identifyFormat(InputStream in) throws IOException {
        if (isJPEG(in)) {
            return ImageFormat.JPEG;
        }

        if (isPNG(in)) {
            return ImageFormat.PNG;
        }

        if (isGIF(in)) {
            return ImageFormat.GIF;
        }

        if (isBMP(in)) {
            return ImageFormat.BMP;
        }

        if (isTIFF(in)) {
            return ImageFormat.TIFF;
        }
        
        return null;
    }

    public static InputStream createMemoryStream(InputStream input) throws IOException {
        if ((!(input instanceof ByteArrayInputStream)) && (!(input instanceof java.io.ByteArrayInputStream))) {
            ByteArrayOutputStream temp = new ByteArrayOutputStream();
            IOUtils.copy(input, temp);

            input = temp.toInputStream();
            temp = null;
        }

        return input;
    }

    public static final byte clampRoundByte(double in) {
        return (in > 0xFF ? (byte) 0xFF : (in >= 0 ? (byte) (in + 0.5) : (byte) 0));
    }

    public static final short clampRoundUShort(double in) {
        return (in > 0xFFFF ? (short) 0xFFFF : (in >= 0 ? (short) (in + 0.5) : (short) 0));
    }

    public static final short clampRoundShort(double in) {
        return (in > Short.MAX_VALUE ? Short.MAX_VALUE : (in >= Short.MIN_VALUE ? (short) Math.floor(in + 0.5) : Short.MIN_VALUE));
    }

    public static final int clampRoundInt(double in) {
        return (in > Integer.MAX_VALUE ? Integer.MAX_VALUE : (in >= Integer.MIN_VALUE ? (int) Math.floor(in + 0.5) : Integer.MIN_VALUE));
    }

    public static final float clampFloat(double in) {
        return (in > Float.MAX_VALUE ? Float.MAX_VALUE : (in >= FLOAT_MIN ? (float) in : FLOAT_MIN));
    }
}
