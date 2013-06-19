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

public final class MarkerConstants implements CalculateConsts {

    /** For temporary use in arithmetic coding */
    public static final int TEM                       = 0x01;

    // Codes 0x02 - 0xBF are reserved

    // SOF markers for Nondifferential Huffman coding
    /** Baseline DCT */
    public static final int SOF0                      = 0xC0;
    /** Extended Sequential DCT */
    public static final int SOF1                      = 0xC1;
    /** Progressive DCT */
    public static final int SOF2                      = 0xC2;
    /** Lossless Sequential */
    public static final int SOF3                      = 0xC3;

    /** Define Huffman Tables */
    public static final int DHT                       = 0xC4;

    // SOF markers for Differential Huffman coding
    /** Differential Sequential DCT */
    public static final int SOF5                      = 0xC5;
    /** Differential Progressive DCT */
    public static final int SOF6                      = 0xC6;
    /** Differential Lossless */
    public static final int SOF7                      = 0xC7;

    /** Reserved for JPEG extensions */
    public static final int JPG                       = 0xC8;

    // SOF markers for Nondifferential arithmetic coding
    /** Extended Sequential DCT, Arithmetic coding */
    public static final int SOF9                      = 0xC9;
    /** Progressive DCT, Arithmetic coding */
    public static final int SOF10                     = 0xCA;
    /** Lossless Sequential, Arithmetic coding */
    public static final int SOF11                     = 0xCB;

    /** Define Arithmetic conditioning tables */
    public static final int DAC                       = 0xCC;

    // SOF markers for Differential arithmetic coding
    /** Differential Sequential DCT, Arithmetic coding */
    public static final int SOF13                     = 0xCD;
    /** Differential Progressive DCT, Arithmetic coding */
    public static final int SOF14                     = 0xCE;
    /** Differential Lossless, Arithmetic coding */
    public static final int SOF15                     = 0xCF;

    // Restart Markers
    public static final int RST0                      = 0xD0;
    public static final int RST1                      = 0xD1;
    public static final int RST2                      = 0xD2;
    public static final int RST3                      = 0xD3;
    public static final int RST4                      = 0xD4;
    public static final int RST5                      = 0xD5;
    public static final int RST6                      = 0xD6;
    public static final int RST7                      = 0xD7;
    /** Number of restart markers */
    public static final int RESTART_RANGE             = 8;

    /** Start of Image */
    public static final int SOI                       = 0xD8;
    /** End of Image */
    public static final int EOI                       = 0xD9;
    /** Start of Scan */
    public static final int SOS                       = 0xDA;

    /** Define Quantisation Tables */
    public static final int DQT                       = 0xDB;

    /** Define Number of lines */
    public static final int DNL                       = 0xDC;

    /** Define Restart Interval */
    public static final int DRI                       = 0xDD;

    /** Define Heirarchical progression */
    public static final int DHP                       = 0xDE;

    /** Expand reference image(s) */
    public static final int EXP                       = 0xDF;

    // Application markers
    /** APP0 used by JFIF */
    public static final int APP0                      = 0xE0;
    public static final int APP1                      = 0xE1;
    /** APP2 used by ICC Profile */
    public static final int APP2                      = 0xE2;
    public static final int APP3                      = 0xE3;
    public static final int APP4                      = 0xE4;
    public static final int APP5                      = 0xE5;
    public static final int APP6                      = 0xE6;
    public static final int APP7                      = 0xE7;
    public static final int APP8                      = 0xE8;
    public static final int APP9                      = 0xE9;
    public static final int APP10                     = 0xEA;
    public static final int APP11                     = 0xEB;
    public static final int APP12                     = 0xEC;
    public static final int APP13                     = 0xED;
    /** APP14 used by Adobe */
    public static final int APP14                     = 0xEE;
    public static final int APP15                     = 0xEF;

    // codes 0xF0 to 0xFD are reserved

    /** Comment marker */
    public static final int COM                       = 0xFE;

    // JFIF Resolution units
    /** The X and Y units simply indicate the aspect ratio of the pixels. */
    public static final int DENSITY_UNIT_ASPECT_RATIO = 0;
    /** Pixel density is in pixels per inch. */
    public static final int DENSITY_UNIT_DOTS_INCH    = 1;
    /** Pixel density is in pixels per centemeter. */
    public static final int DENSITY_UNIT_DOTS_CM      = 2;
    /** The max known value for DENSITY_UNIT */
    public static final int NUM_DENSITY_UNIT          = 3;

    // Adobe transform values
    public static final int ADOBE_IMPOSSIBLE          = -1;
    public static final int ADOBE_UNKNOWN             = 0;
    public static final int ADOBE_YCC                 = 1;
    public static final int ADOBE_YCCK                = 2;
}
