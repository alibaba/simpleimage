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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.EOFException;
import java.io.IOException;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.codec.AbstractImageDecoder;
import com.alibaba.simpleimage.codec.ExtendImageHeaderReader;
import com.alibaba.simpleimage.codec.convertor.ColorConvertor;
import com.alibaba.simpleimage.codec.convertor.FastInverseDCTCalculator;
import com.alibaba.simpleimage.codec.convertor.InverseColorConvertor;
import com.alibaba.simpleimage.codec.convertor.InverseDCTCalculator;
import com.alibaba.simpleimage.codec.convertor.NullColorConvertor;
import com.alibaba.simpleimage.codec.convertor.SlowInverseDCTCalculator;
import com.alibaba.simpleimage.codec.convertor.YCCK2CMYKColorConvertor;
import com.alibaba.simpleimage.codec.convertor.YCbCr2RGBColorConvertor;
import com.alibaba.simpleimage.codec.jpeg.ext.AdobeHeaderReader;
import com.alibaba.simpleimage.codec.jpeg.ext.ExtendImageHeader;
import com.alibaba.simpleimage.codec.jpeg.ext.ICCProfileReader;
import com.alibaba.simpleimage.codec.jpeg.ext.JFIFHeaderReader;
import com.alibaba.simpleimage.io.ImageInputStream;
import com.alibaba.simpleimage.jai.cmm.CMMColorSpace;

public class JPEGDecoder extends AbstractImageDecoder {

    private ImageInputStream     in;

    private HuffmanTable[]       dcHuffTables      = new HuffmanTable[4];
    private HuffmanTable[]       acHuffTables      = new HuffmanTable[4];
    private QuantizationTable[]  qTables           = new QuantizationTable[4];

    private FrameHeader          frameHeader;
    private ScanHeader           scanHeader;
    private int                  restartInterval;
    private ExtendImageHeader    extendImageHeader = new ExtendImageHeader();

    // Util class
    private InverseDCTCalculator inverseDCTCalculator;
    private ColorConvertor       colorConvertor;

    private InternalRawImage     rawImage;                                    //

    // Runtime var frame level
    private int[][]              singleMCUData     = new int[4][];            // used by baseline mode
    private int[]                singleBlockData   = new int[DCTSIZE2];
    private int[]                pixesBuffer       = null;
    private int                  x                 = 0, y = 0;                //
    private int[][][]            allMCUDatas;                                 // used by progressive mode
    private int                  MCUsPerRow;
    private int                  MCUsPerColumn;
    private int                  maxHSampleFactor  = 0;                       // Max Horizontal sampling factor of
    // components
    private int                  maxVSampleFactor  = 0;                       // Max Vertical sampling factor of
    // components

    // Runtime var scan level
    private int[]                blocksInMCU       = new int[10];
    private int                  blocksNumInMCU    = 0;
    private int[]                preDC;
    private int                  EOBRUN            = 0;

    private boolean              fastIDCTMode      = false;
    private boolean              supportICC        = false;
    private boolean              broken            = false;                   // indicate the image is broken or not

    public JPEGDecoder(ImageInputStream in, boolean fastIDCTMode, boolean supportICC){
        this.in = in;
        this.fastIDCTMode = fastIDCTMode;
        this.supportICC = supportICC;

        super.addExtendHeaderReader(MarkerConstants.APP0, new JFIFHeaderReader());
        if (supportICC) {
            super.addExtendHeaderReader(MarkerConstants.APP2, new ICCProfileReader());
        }
        super.addExtendHeaderReader(MarkerConstants.APP14, new AdobeHeaderReader());

        if (fastIDCTMode) {
            // temporary unsupported
            inverseDCTCalculator = new FastInverseDCTCalculator();
        } else {
            inverseDCTCalculator = new SlowInverseDCTCalculator();
        }
    }

    public JPEGDecoder(ImageInputStream in){
        this(in, false, false);
    }

    public ImageWrapper decode() throws IOException {
        int prefix = in.read();
        int magic = in.read();

        if ((prefix != 0xFF) || (magic != MarkerConstants.SOI)) {
            throw new IllegalArgumentException("Not JPEG file");
        }

        int marker = nextMarker();

        try {
            while (!isSOFnMarker(marker)) {
                readTables(marker);

                marker = nextMarker();

                if (marker == -1) {
                    throw new IOException("Unexpected end of file");
                }
            }
        } catch (JPEGMarkerException e) {
            // unknown marker detected
            throw new JPEGDecoderException("Decode JPEG fail");
        }

        marker = decodeFrame(marker);

        if (marker == -1 || marker == MarkerConstants.EOI) {
            if (rawImage == null) {
                throw new JPEGDecoderException("Decode JPEG fail");
            }

            if(marker == -1) {
                broken = true;
            }
            
            return createImage();
        } else {
            throw new JPEGDecoderException("Decode JPEG fail");
        }
    }

    protected ImageWrapper createImage() {
        Raster dstRaster = null;
        ColorSpace cs = null;

        if (frameHeader.isProgressiveMode()) {
            inverseDCT();

            writeFull();
        }

        if (rawImage.getColorspace() == JPEGColorSpace.Gray) {
            dstRaster = Raster.createInterleavedRaster(
                                                       new DataBufferByte(rawImage.getData(), rawImage.getData().length),
                                                       rawImage.getWidth(), rawImage.getHeight(),
                                                       rawImage.getWidth() * 1, 1, new int[] { 0 }, null);

            cs = getColorSpace();
        } else if (rawImage.getColorspace() == JPEGColorSpace.RGB) {
            dstRaster = Raster.createInterleavedRaster(
                                                       new DataBufferByte(rawImage.getData(), rawImage.getData().length),
                                                       rawImage.getWidth(), rawImage.getHeight(),
                                                       rawImage.getWidth() * 3, 3, new int[] { 0, 1, 2 }, null);

            cs = getColorSpace();
        } else if (rawImage.getColorspace() == JPEGColorSpace.CMYK) {
            dstRaster = Raster.createInterleavedRaster(
                                                       new DataBufferByte(rawImage.getData(), rawImage.getData().length),
                                                       rawImage.getWidth(), rawImage.getHeight(),
                                                       rawImage.getWidth() * 4, 4, new int[] { 0, 1, 2, 3 }, null);

            cs = getColorSpace();
        } else {
            throw new JPEGDecoderException("Unknow colorspace");
        }

        ColorModel cm = new ComponentColorModel(cs, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);

        BufferedImage img = new BufferedImage(cm, (WritableRaster) dstRaster, true, null);

        ImageWrapper wi = new ImageWrapper(img, getQuality(), broken);
        wi.setImageFormat(ImageFormat.JPEG);

        for (Component c : frameHeader.getComponents()) {
            wi.setHorizontalSamplingFactor(c.getIndex(), c.getH());
            wi.setVerticalSamplingFactor(c.getIndex(), c.getV());
        }

        return wi;
    }

    protected ColorSpace getColorSpace() {
        ColorSpace cs = null;

        if (supportICC) {
            if (extendImageHeader.isExistProfile()) {
                ICC_Profile profile = null;

                try {
                    synchronized (ICC_Profile.class) {
                        profile = ICC_Profile.getInstance(extendImageHeader.getProfileData());
                    }
                } catch (Exception ignore) {
                    profile = null;
                }

                if (profile != null) {
                    try {
                        cs = new ICC_ColorSpace(profile);
                    } catch (Exception ignore) {
                        cs = null;
                    }
                }
            }
        }

        // use standard color space if create color space from ICC_Profile fail
        // or doesn't exists ICC_Profile
        if (cs == null) {
            extendImageHeader.setExistProfile(false);

            if (rawImage.getColorspace() == JPEGColorSpace.CMYK) {
                cs = CMMColorSpace.getInstance(ColorSpace.TYPE_CMYK);
            } else if (rawImage.getColorspace() == JPEGColorSpace.RGB) {
                cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            } else {
                cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            }
        }

        return cs;
    }

    protected int decodeFrame(int marker) throws IOException {
        readFrameHeader(marker);

        determinColorspace();

        marker = nextMarker();

        for (int scanNum = 0;;) {
            scanNum++;

            if (frameHeader.isProgressiveMode()) {
                marker = decodeProgressiveScan(marker);
            } else {
                marker = decodeScan(marker);
            }

            if (marker == MarkerConstants.DNL && scanNum == 1) {
                decodeDNL();
            }

            if (marker == MarkerConstants.EOI || marker == -1) {
                return marker;
            }
        }
    }

    protected void calculateMCUs(int[] componentIndexes, Component[] components) {
        blocksNumInMCU = 0;

        for (int i : componentIndexes) {
            int blocks = components[i].getH() * components[i].getV();
            while ((blocks--) > 0) {
                blocksInMCU[blocksNumInMCU++] = i;
            }
        }
    }

    /**
     * Decode baseline and sequencal scan
     * 
     * @return
     * @throws IOException
     */
    protected int decodeScan(int marker) throws IOException {

        try {
            while (marker != MarkerConstants.SOS) {
                readTables(marker);

                marker = nextMarker();

                if (marker == -1) {
                    return marker;
                }
            }
        } catch (JPEGMarkerException e) {
            // just ignore unknown marker
            return -1;
        }

        // Decode scan header
        int[] componentIndexes = readScanHeader();
        Component[] components = frameHeader.getComponents();

        // Calculate MCU size
        calculateMCUs(componentIndexes, components);

        try {
            int restartsLeft = restartInterval;
            in.resetBuffer();
            resetDecoder();
            HuffmanTable curDCTable;
            HuffmanTable curACTable;
            QuantizationTable curQTable;

            for (int m = 0; m < MCUsPerColumn * MCUsPerRow; m++) {
                // If support restart interval
                if (restartInterval > 0 && restartsLeft == 0) {
                    marker = nextMarker();

                    if (marker > MarkerConstants.RST7 || marker < MarkerConstants.RST0) {
                        return marker;
                    }

                    in.resetBuffer();
                    resetDecoder();

                    restartsLeft = restartInterval;
                }

                // Begin decode MCU
                for (int c = 0, index = 0, ctr = 0; c < blocksNumInMCU; c++) {
                    index = blocksInMCU[c];

                    if (c == 0) {
                        ctr = 0;
                    } else {
                        if (index == blocksInMCU[c - 1]) {
                            ctr++;
                        } else {
                            ctr = 0;
                        }
                    }

                    // Begin decode block
                    curACTable = components[index].acHuffTable;
                    curDCTable = components[index].dcHuffTable;
                    curQTable = components[index].qTable;

                    // init 0
                    for (int z = 0; z < DCTSIZE2; z++) {
                        singleBlockData[z] = 0;
                    }

                    int s = 0, r = 0;

                    // Decode DC
                    s = curDCTable.decode(in);
                    if (s > 0) {
                        r = (int) in.readBits(s);
                        s = curDCTable.extend(r, s);
                    }

                    preDC[index] = s = preDC[index] + s;
                    singleBlockData[0] = s;

                    // Decode AC
                    for (int k = 1;;) {
                        s = curACTable.decode(in);

                        r = s >> 4;
                        s = s & 15;

                        if (s == 0) {
                            if (r == 15) {
                                k += 16;
                                continue;
                            } else {
                                break;
                            }
                        } else {
                            k += r;

                            r = (int) in.readBits(s);
                            s = curACTable.extend(r, s);

                            // Output coefficient in natural (dezigzagged) order
                            singleBlockData[MarkerConstants.BLOCK_NATURAL_ORDER[k]] = s;

                            if (k >= 63) {
                                break;
                            } else {
                                k++;
                            }
                        }
                    }
                    // End decode block

                    if (fastIDCTMode) {
                        throw new UnsupportedOperationException("Not implemented yet");
                    } else {
                        inverseDCTCalculator.calculate(singleBlockData, 0, curQTable.getQ(), singleMCUData[index],
                                                       ctr * DCTSIZE2, components[index].getHorizonDCTScaledSize(),
                                                       components[index].getVerticaDCTScaledSize());

                    }
                }
                // End decode MCU

                writeMCU();

                restartsLeft--;
            }

            marker = nextMarker();
        } catch (JPEGMarkerException e) { // Unexpected marker occur
            marker = e.getMarker();
        } catch (EOFException e) {
            marker = -1;
        } catch (ArrayIndexOutOfBoundsException e) {
            // just ignore huffman decode error
            marker = -1;
            broken = true;
        }

        return marker;
    }

    /**
     * Decode progressive scan
     * 
     * @return
     * @throws IOException
     */
    protected int decodeProgressiveScan(int marker) throws IOException {

        try {
            while (marker != MarkerConstants.SOS) {
                readTables(marker);

                marker = nextMarker();

                if (marker == -1) {
                    return marker;
                }
            }
        } catch (JPEGMarkerException e) {
            // just ignore unknown marker
            return -1;
        }

        int[] componentIndexes = readScanHeader();

        validateProgressiveParam();

        Component[] components = frameHeader.getComponents();

        // Calculate MCU size
        calculateMCUs(componentIndexes, components);

        // AC scans can only contain one component, DC scans may contain more
        // than one component
        try {
            int restartsLeft = restartInterval;
            in.resetBuffer();
            resetDecoder();
            HuffmanTable curDCTable;
            HuffmanTable curACTable;

            /*
             * Note:If the compressed image data is non-interleaved, the MCU is defined to be one data unit
             */
            // interleaved
            if (scanHeader.getSs() == 0) {
                for (int m = 0; m < MCUsPerColumn * MCUsPerRow; m++) {
                    // If support restart interval
                    if (restartInterval > 0 && restartsLeft == 0) {
                        marker = nextMarker();

                        if (marker > MarkerConstants.RST7 || marker < MarkerConstants.RST0) {
                            return marker;
                        }

                        in.resetBuffer();

                        resetDecoder();

                        restartsLeft = restartInterval;
                    }

                    // Begin decode MCU
                    for (int c = 0, index = 0, blkIndex = 0; c < blocksNumInMCU; c++) {
                        index = blocksInMCU[c];

                        if (c == 0) {
                            blkIndex = 0;
                        } else {
                            if (index == blocksInMCU[c - 1]) {
                                blkIndex++;
                            } else {
                                blkIndex = 0;
                            }
                        }
                        curDCTable = components[index].dcHuffTable;

                        if (scanHeader.getAh() == 0) { // first scan
                            // only DC
                            decodeDCFirst(index, curDCTable, scanHeader.getAl(), allMCUDatas[index][m], blkIndex);
                        } else { // subsequent scans
                            // only DC
                            decodeDCRefine(scanHeader.getAl(), allMCUDatas[index][m], blkIndex);

                        }
                    }
                    // End decode MCU

                    restartsLeft--;
                }
            } else {
                // non-interleaved
                int componentH = components[componentIndexes[0]].getH();
                int componentV = components[componentIndexes[0]].getV();
                int blksInVertica = (componentV == 1 ? MCUsPerColumn : ((frameHeader.getY() + 7) / DCTSIZE));
                int blksInHorizon = (componentH == 1 ? MCUsPerRow : ((frameHeader.getX() + 7) / DCTSIZE));
                int MCUIndex = 0, blkIndex = 0, secondBlkIndex = (componentH == 1 ? 1 : 2);

                curACTable = components[componentIndexes[0]].acHuffTable;

                for (int v = 0; v < blksInVertica; v++) {
                    MCUIndex = (v / componentV) * MCUsPerRow - 1;

                    for (int h = 0; h < blksInHorizon; h++) {
                        if (h % componentH == 0) {
                            MCUIndex++;
                            blkIndex = 0;

                            if (v % componentV != 0) {
                                blkIndex = secondBlkIndex;
                            }
                        }

                        // If support restart interval
                        if (restartInterval > 0 && restartsLeft == 0) {
                            marker = nextMarker();

                            if (marker > MarkerConstants.RST7 || marker < MarkerConstants.RST0) {
                                return marker;
                            }

                            in.resetBuffer();

                            resetDecoder();

                            restartsLeft = restartInterval;
                        }

                        if (scanHeader.getAh() == 0) {
                            decodeACFirst(curACTable, scanHeader.getSs(), scanHeader.getSe(), scanHeader.getAl(),
                                          allMCUDatas[componentIndexes[0]][MCUIndex], blkIndex);
                        } else {
                            decodeACRefine(curACTable, scanHeader.getSs(), scanHeader.getSe(), scanHeader.getAl(),
                                           allMCUDatas[componentIndexes[0]][MCUIndex], blkIndex);
                        }

                        blkIndex++;

                        /* There is always only one block per MCU */
                        restartsLeft--;
                    }
                }
            }

            marker = nextMarker();
        } catch (JPEGMarkerException e) {
            marker = e.getMarker();
        } catch (EOFException e) {
            marker = -1;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Note : just ignore huffman decode error
            marker = -1;
            broken = true;
        }

        return marker;
    }

    protected void validateProgressiveParam() {
        if (scanHeader.getSs() == 0) {
            if (scanHeader.getSe() != 0) {
                throw new IllegalArgumentException("Invalid progressive parameters");
            }
        } else {
            /* need not check Ss/Se < 0 since they came from unsigned bytes */
            if (scanHeader.getSe() < scanHeader.getSs() || scanHeader.getSe() > 63) throw new IllegalArgumentException(
                                                                                                                       "Invalid progressive parameters");

            /* AC scans may have only one component */
            if (scanHeader.getNs() != 1) throw new IllegalArgumentException("Invalid progressive parameters");
        }

        if (scanHeader.getAh() != 0) {
            /* Successive approximation refinement scan: must have Al = Ah-1. */
            if (scanHeader.getAh() - 1 != scanHeader.getAl()) throw new IllegalArgumentException(
                                                                                                 "Invalid progressive parameters");
        }

        if (scanHeader.getAl() > 13) { /* need not check for < 0 */
            throw new IllegalArgumentException("Invalid progressive parameters");
        }
    }

    /**
     * can opt using array
     * 
     * @param marker
     * @return
     */
    protected boolean isSOFnMarker(int marker) {
        if (marker <= 0xC3 && marker >= 0xC0) {
            return true;
        }

        if (marker <= 0xCB && marker >= 0xC5) {
            return true;
        }

        if (marker <= 0xCF && marker >= 0xCD) {
            return true;
        }

        return false;
    }

    protected void readTables(int marker) throws IOException, JPEGMarkerException {
        switch (marker) {
            case MarkerConstants.APP0:
            case MarkerConstants.APP1:
            case MarkerConstants.APP2:
            case MarkerConstants.APP3:
            case MarkerConstants.APP4:
            case MarkerConstants.APP5:
            case MarkerConstants.APP6:
            case MarkerConstants.APP7:
            case MarkerConstants.APP8:
            case MarkerConstants.APP9:
            case MarkerConstants.APP10:
            case MarkerConstants.APP11:
            case MarkerConstants.APP12:
            case MarkerConstants.APP13:
            case MarkerConstants.APP14:
            case MarkerConstants.APP15:
                decodeExtendMarker(marker);
                break;
            case MarkerConstants.DAC:
                decodeDAC();
                break;
            case MarkerConstants.DHT:
                decodeHuffmanTable();
                break;
            case MarkerConstants.DQT:
                decodeQuantizationTable();
                break;
            case MarkerConstants.DRI:
                decodeDRI();
                break;
            case MarkerConstants.COM:
                decodeExtendMarker(MarkerConstants.COM);
                break;
            case MarkerConstants.DNL:
                decodeDNL();
                break;
            default:
                // Unknown marker, just throws it
                throw new JPEGMarkerException(marker);
        }
    }

    protected void decodeDNL() throws IOException {
        in.readShort(); // Skip Ld

        in.readUnsignedShort(); // Read NL just ignore, because the DNL marker is unsupported
    }

    protected void decodeDRI() throws IOException {
        int length;

        length = in.readUnsignedShort();

        if (length != 4) {
            throw new JPEGDecoderException("Bogus marker length");
        }

        restartInterval = in.readUnsignedShort();
    }

    protected void decodeHuffmanTable() throws IOException {
        int length;
        int bits[];
        int huffval[];

        int index, count;

        length = in.readUnsignedShort();
        length -= 2;

        while (length > 0) {
            index = in.read();

            count = 0;

            bits = new int[17];
            for (int i = 1; i <= 16; i++) {
                bits[i] = in.read();
                count += bits[i];
            }

            length -= (1 + 16);

            /*
             * Here we just do minimal validation of the counts to avoid walking off the end of our table space
             */
            if (count > 256 || count > length) {
                throw new JPEGDecoderException("Bogus Huffman table definition");
            }

            huffval = new int[256];
            for (int i = 0; i < count; i++) {
                huffval[i] = in.read();
            }

            length -= count;

            if ((index >> 4) == HuffmanTable.TYPE_AC) {
                /* AC table definition */
                index -= 0x10;
                acHuffTables[index] = new HuffmanTable(HuffmanTable.TYPE_AC, index, bits, huffval);
            } else {
                /* DC table definition */
                dcHuffTables[index] = new HuffmanTable(HuffmanTable.TYPE_DC, index, bits, huffval);
            }

            if (index < 0 || index >= 4) {
                throw new JPEGDecoderException("Bogus DHT index " + index);
            }
        }

        if (length != 0) {
            throw new JPEGDecoderException("Bogus marker length");
        }
    }

    protected void decodeQuantizationTable() throws IOException {
        int length, count = 0;
        int[] Q;

        length = in.readUnsignedShort();
        length -= 2;

        while (length > 0) {
            int prec = 0, n = 0;

            n = in.read();
            length--;

            prec = n >> 4; // Pq
            n &= 0x0F; // Tq

            if (n >= 4) {
                throw new JPEGDecoderException("Unsupport quantization table more than 4 ");
            }

            Q = new int[64];

            if (prec > 0) {
                if (length < 64 * 2) {
                    /* Initialize full table for safety. */
                    for (int i = 0; i < 64; i++) {
                        Q[i] = 1;
                    }
                    count = length >> 1;
                } else count = 64;
            } else {
                if (length < 64) {
                    /* Initialize full table for safety. */
                    for (int i = 0; i < 64; i++) {
                        Q[i] = 1;
                    }
                    count = length;
                } else count = 64;
            }

            for (int i = 0; i < count; i++) {
                if (prec > 0) {
                    Q[MarkerConstants.BLOCK_NATURAL_ORDER[i]] = in.readUnsignedShort();
                } else {
                    Q[MarkerConstants.BLOCK_NATURAL_ORDER[i]] = in.read();
                }
            }

            /*
             * For AA&N IDCT method, multipliers are equal to quantization coefficients scaled by
             * scalefactor[row]*scalefactor[col], where scalefactor[0] = 1 scalefactor[k] = cos(k*PI/16) * sqrt(2) for
             * k=1..7 For integer operation, the multiplier table is to be scaled by IFAST_SCALE_BITS. nothing to do for
             * slowIDCTMode
             */
            if (fastIDCTMode) {
                int half = 1 << 11;
                for (int i = 0; i < 64; i++) {
                    Q[i] = (Q[i] * MarkerConstants.AAN_SCALES[i] + half) >> 12;
                }
            }

            QuantizationTable qTable = new QuantizationTable(Q);
            qTables[n] = qTable;

            length -= count;
            if (prec > 0) length -= count;
        }

        if (length != 0) {
            throw new JPEGDecoderException("Bogus marker length");
        }
    }

    protected void decodeDAC() throws IOException {
        throw new UnsupportedOperationException("Not implement yet");
    }

    protected void decodeExtendMarker(int marker) throws IOException {
        int length = in.readUnsignedShort();
        length -= 2;

        ExtendImageHeaderReader reader = extendImageHeaderReaders.get(marker);

        if (reader != null) {
            reader.readProperties(in, length, extendImageHeader);
        } else {
            if (length > 0) {
                in.skipBytes(length);
            }
        }
    }

    protected void determinColorspace() {
        switch (frameHeader.getNf()) {
            case 1:
                rawImage.setRawColorspace(JPEGColorSpace.Gray);
                rawImage.setColorspace(JPEGColorSpace.Gray);
                colorConvertor = new NullColorConvertor();
                break;

            case 3:
                if (extendImageHeader.isSawJFIFMarker()) {
                    /* JFIF implies YCbCr */
                    rawImage.setRawColorspace(JPEGColorSpace.YCbCr);
                    colorConvertor = new YCbCr2RGBColorConvertor();
                } else if (extendImageHeader.isSawAdobeMarker()) {
                    switch (extendImageHeader.getAdobeTransform()) {
                        case 0:
                            rawImage.setRawColorspace(JPEGColorSpace.RGB);
                            colorConvertor = new NullColorConvertor();
                            break;
                        case 1:
                            rawImage.setRawColorspace(JPEGColorSpace.YCbCr);
                            colorConvertor = new YCbCr2RGBColorConvertor();
                            break;
                        default:
                            /* assume it's YCbCr */
                            rawImage.setRawColorspace(JPEGColorSpace.YCbCr);
                            colorConvertor = new YCbCr2RGBColorConvertor();
                            break;
                    }
                } else {
                    /*
                     * Saw no special markers, try to guess from the component IDs
                     */
                    int cid0 = frameHeader.getComponents()[0].getC();
                    int cid1 = frameHeader.getComponents()[1].getC();
                    int cid2 = frameHeader.getComponents()[2].getC();

                    if (cid0 == 1 && cid1 == 2 && cid2 == 3) {
                        /* assume JFIF w/out marker */
                        rawImage.setRawColorspace(JPEGColorSpace.YCbCr);
                        colorConvertor = new YCbCr2RGBColorConvertor();
                    } else if (cid0 == 82 && cid1 == 71 && cid2 == 66) {
                        /* ASCII 'R', 'G', 'B' */
                        rawImage.setRawColorspace(JPEGColorSpace.RGB);
                        colorConvertor = new NullColorConvertor();
                    } else {
                        /* assume it's YCbCr */
                        rawImage.setRawColorspace(JPEGColorSpace.YCbCr);
                        colorConvertor = new YCbCr2RGBColorConvertor();
                    }
                }

                /* Always guess RGB is proper output colorspace. */
                rawImage.setColorspace(JPEGColorSpace.RGB);
                break;

            case 4:
                if (extendImageHeader.isSawAdobeMarker()) {
                    switch (extendImageHeader.getAdobeTransform()) {
                        case 0:
                            rawImage.setRawColorspace(JPEGColorSpace.CMYK);
                            colorConvertor = new InverseColorConvertor();
                            break;
                        case 2:
                            rawImage.setRawColorspace(JPEGColorSpace.YCCK);
                            colorConvertor = new YCCK2CMYKColorConvertor();
                            break;
                        default:
                            /* assume it's YCCK */
                            rawImage.setRawColorspace(JPEGColorSpace.YCCK);
                            colorConvertor = new YCCK2CMYKColorConvertor();
                            break;
                    }
                } else {
                    /* No special markers, assume straight CMYK. */
                    rawImage.setRawColorspace(JPEGColorSpace.CMYK);
                    colorConvertor = new InverseColorConvertor();
                }
                rawImage.setColorspace(JPEGColorSpace.CMYK);
                break;

            default:
                rawImage.setRawColorspace(JPEGColorSpace.UNKNOWN);
                rawImage.setColorspace(JPEGColorSpace.UNKNOWN);
                colorConvertor = new NullColorConvertor();
                break;
        }
    }

    protected void readFrameHeader(int marker) throws IOException {
        switch (marker) {
            case MarkerConstants.SOF0:/* Baseline */
                createFrameHeader(true, false);
                break;
            case MarkerConstants.SOF1:/* Extended sequential, Huffman */
                createFrameHeader(false, false);
                break;
            case MarkerConstants.SOF2:/* Progressive, Huffman */
                createFrameHeader(false, true);
                break;

            /* Currently unsupported SOFn types */
            case MarkerConstants.SOF9:/* Extended sequential, arithmetic */
            case MarkerConstants.SOF10:/* Progressive, arithmetic */
            case MarkerConstants.SOF3: /* Lossless, Huffman */
            case MarkerConstants.SOF5: /* Differential sequential, Huffman */
            case MarkerConstants.SOF6: /* Differential progressive, Huffman */
            case MarkerConstants.SOF7: /* Differential lossless, Huffman */
            case MarkerConstants.JPG: /* Reserved for JPEG extensions */
            case MarkerConstants.SOF11: /* Lossless, arithmetic */
            case MarkerConstants.SOF13: /* Differential sequential, arithmetic */
            case MarkerConstants.SOF14: /* Differential progressive, arithmetic */
            case MarkerConstants.SOF15: /* Differential lossless, arithmetic */
            default:
                throw new JPEGDecoderException("Unsupported SOFn types " + marker);
        }
    }

    protected void createFrameHeader(boolean baseline, boolean progressive) throws IOException {
        if (frameHeader != null) {
            throw new JPEGDecoderException("Duplicate SOFn marker");
        }

        int length = in.readUnsignedShort();

        frameHeader = new FrameHeader(baseline, progressive);
        frameHeader.setLF(length);
        frameHeader.setP(in.read());
        frameHeader.setY(in.readUnsignedShort());
        frameHeader.setX(in.readUnsignedShort());
        frameHeader.setNf(in.read());

        length -= 8;

        if (frameHeader.getX() <= 0 || frameHeader.getY() <= 0 || frameHeader.getNf() <= 0) {
            throw new JPEGDecoderException("Illegal JPEG frame width or height or components");
        }

        if (length != frameHeader.getNf() * 3) {
            throw new JPEGDecoderException("Illegal JPEG frame length");
        }

        frameHeader.setComponents(new Component[frameHeader.getNf()]);

        for (int i = 0; i < frameHeader.getNf(); i++) {
            Component com = new Component();
            com.setIndex(i);
            com.setC(in.read());
            int factor = in.read();
            com.setH((factor >> 4) & 0x0F);
            com.setV((factor) & 0x0F);
            com.setTq(in.read());
            frameHeader.getComponents()[i] = com;

            if (com.getH() > maxHSampleFactor) {
                maxHSampleFactor = com.getH();
            }

            if (com.getV() > maxVSampleFactor) {
                maxVSampleFactor = com.getV();
            }
        }

        for (int i = 0; i < frameHeader.getNf(); i++) {
            Component comp = frameHeader.getComponents()[i];
            int st = (maxHSampleFactor * maxVSampleFactor) / (comp.getH() * comp.getV());

            comp.setSampleTimes(st);
            if (comp.getH() != maxHSampleFactor) {
                comp.setHorizonDCTScaledSize(maxHSampleFactor * 8);
            } else {
                comp.setHorizonDCTScaledSize(8);
            }

            if (comp.getV() != maxVSampleFactor) {
                comp.setVerticaDCTScaledSize(maxVSampleFactor * 8);
            } else {
                comp.setVerticaDCTScaledSize(8);
            }
        }

        if (rawImage == null) {
            rawImage = new InternalRawImage();
        }

        rawImage.setHeight(frameHeader.getY());
        rawImage.setWidth(frameHeader.getX());
        rawImage.setNumOfComponents(frameHeader.getNf());
        rawImage.initData();

        preDC = new int[frameHeader.getNf()];

        // Calculate some parameter
        MCUsPerRow = (((frameHeader.getX() + 7) / 8) + (maxHSampleFactor - 1)) / maxHSampleFactor;
        MCUsPerColumn = (((frameHeader.getY() + 7) / 8) + (maxVSampleFactor - 1)) / maxVSampleFactor;

        if (progressive) {
            int MCUs = MCUsPerRow * MCUsPerColumn;

            // x components, y max MCU, z MZU size
            allMCUDatas = new int[4][][];
            allMCUDatas[0] = new int[MCUs][DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
            if (frameHeader.getNf() > 1) {
                allMCUDatas[1] = new int[MCUs][DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
                allMCUDatas[2] = new int[MCUs][DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
            }
            if (frameHeader.getNf() > 3) {
                allMCUDatas[3] = new int[MCUs][DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
            }
        } else {
            singleMCUData[0] = new int[DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
            if (frameHeader.getNf() > 1) {
                singleMCUData[1] = new int[DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
                singleMCUData[2] = new int[DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
            }
            if (frameHeader.getNf() > 3) {
                singleMCUData[3] = new int[DCTSIZE2 * maxVSampleFactor * maxHSampleFactor];
            }
        }
    }

    protected void inverseDCT() {
        int numOfComponents = frameHeader.getNf();
        Component[] components = frameHeader.getComponents();
        int[] codeBlocks = new int[DCTSIZE2 * maxHSampleFactor * maxVSampleFactor];

        for (int m = 0; m < MCUsPerColumn * MCUsPerRow; m++) {
            // one MCU
            for (int c = 0; c < numOfComponents; c++) {
                int blkNum = components[c].getH() * components[c].getV();

                for (int i = 0; i < blkNum * DCTSIZE2; i++) {
                    codeBlocks[i] = allMCUDatas[c][m][i];
                }

                for (int b = 0; b < blkNum; b++) {
                    inverseDCTCalculator.calculate(codeBlocks, b * DCTSIZE2, components[c].qTable.getQ(),
                                                   allMCUDatas[c][m], b * DCTSIZE2,
                                                   components[c].getHorizonDCTScaledSize(),
                                                   components[c].getVerticaDCTScaledSize());
                }
            }
        }
    }

    /**
     * Used by progressive mode
     */
    protected void writeFull() {
        byte[] imageData = rawImage.getData();
        int numOfComponents = frameHeader.getNf();
        int[] pixes = new int[numOfComponents * DCTSIZE2];
        int blockIndex = 0;
        int startCoordinate = 0, scanlineStride = numOfComponents * rawImage.getWidth(), row = 0;

        x = 0;
        y = 0;

        for (int m = 0; m < MCUsPerColumn * MCUsPerRow; m++) {
            blockIndex = 0;

            // one MCU
            for (int v = 0; v < maxVSampleFactor; v++) {
                for (int h = 0; h < maxHSampleFactor; h++) {
                    row = y + 1;
                    startCoordinate = (y * rawImage.getWidth() + x) * numOfComponents;

                    // one block
                    for (int k = blockIndex * DCTSIZE2, i = 0; k < blockIndex * DCTSIZE2 + DCTSIZE2; k++) {
                        pixes[i++] = allMCUDatas[0][m][k];

                        if (numOfComponents > 1) {
                            pixes[i++] = allMCUDatas[1][m][k];
                            pixes[i++] = allMCUDatas[2][m][k];
                        }

                        if (numOfComponents > 3) {
                            pixes[i++] = allMCUDatas[3][m][k];
                        }

                        x++;

                        if (x % 8 == 0) {
                            y++;
                            x -= 8;
                        }
                    }

                    colorConvertor.convertBlock(pixes, 0, imageData, numOfComponents, startCoordinate, row,
                                                scanlineStride);

                    blockIndex++;

                    x += 8;
                    y -= 8;
                }

                x -= maxHSampleFactor * 8;
                y += 8;
            }

            x += maxHSampleFactor * 8;
            y -= maxVSampleFactor * 8;

            if (x >= rawImage.getWidth()) {
                x = 0;
                y += maxVSampleFactor * 8;
            }

        }
    }

    protected void writeMCU() {
        byte[] imageData = rawImage.getData();
        int numOfComponents = frameHeader.getNf();
        int blockIndex = 0;
        int startCoordinate = 0, row = 0, scanlineStride = numOfComponents * rawImage.getWidth();

        if (pixesBuffer == null) {
            pixesBuffer = new int[numOfComponents * DCTSIZE2];
        }

        // one MCU
        for (int v = 0; v < maxVSampleFactor; v++) {
            for (int h = 0; h < maxHSampleFactor; h++) {
                row = y + 1;
                startCoordinate = (y * rawImage.getWidth() + x) * numOfComponents;

                // one block
                for (int k = blockIndex * DCTSIZE2, i = 0; k < blockIndex * DCTSIZE2 + DCTSIZE2; k++) {
                    pixesBuffer[i++] = singleMCUData[0][k];

                    if (numOfComponents > 1) {
                        pixesBuffer[i++] = singleMCUData[1][k];
                        pixesBuffer[i++] = singleMCUData[2][k];
                    }

                    if (numOfComponents > 3) {
                        pixesBuffer[i++] = singleMCUData[3][k];
                    }

                    x++;

                    if (x % 8 == 0) {
                        y++;
                        x -= 8;
                    }
                }

                colorConvertor.convertBlock(pixesBuffer, 0, imageData, numOfComponents, startCoordinate, row,
                                            scanlineStride);

                blockIndex++;

                x += 8;
                y -= 8;
            }

            x -= maxHSampleFactor * 8;
            y += 8;
        }

        x += maxHSampleFactor * 8;
        y -= maxVSampleFactor * 8;

        if (x >= rawImage.getWidth()) {
            x = 0;
            y += maxVSampleFactor * 8;
        }
    }

    protected void resetDecoder() {
        for (int i = 0; i < frameHeader.getNf(); i++) {
            preDC[i] = 0;
        }

        EOBRUN = 0;
    }

    protected int nextMarker() throws IOException {
        for (;;) {
            int c = in.read();
            while (c != 0xFF && c != -1) {
                c = in.read();
            }

            do {
                c = in.read();
            } while (c == 0xFF);

            if (c != 0) {
                return c;
            }

            if (c == -1) {
                return -1;
            }
        }
    }

    protected int[] readScanHeader() throws IOException {
        scanHeader = new ScanHeader();

        int length = in.readUnsignedShort();
        scanHeader.setLs(length);
        length -= 2;

        scanHeader.setNs(in.read());
        length--;

        int[] componentsInScan = new int[scanHeader.getNs()];
        int Cs = 0, Ta = 0, Td = 0;
        for (int i = 0; i < scanHeader.getNs(); i++) {
            Cs = in.read();
            length--;

            int temp = in.read();
            length--;
            Td = (temp >> 4);
            Ta = (temp & 0x0F);

            Component component = frameHeader.getComponentByID(Cs);
            component.setDcHuffTable(dcHuffTables[Td]);
            component.setAcHuffTable(acHuffTables[Ta]);
            component.setQTable(qTables[component.getTq()]);

            componentsInScan[i] = component.getIndex();
        }

        scanHeader.setSs(in.read());
        length--;

        scanHeader.setSe(in.read());
        length--;

        int temp = in.read();
        length--;

        scanHeader.setAh((temp >> 4) & 15);
        scanHeader.setAl(temp & 15);

        if (length != 0) {
            throw new JPEGDecoderException("Bugos scan header length");
        }

        return componentsInScan;
    }

    /*
     * Compute the JPEG compression quality from the quantization tables.
     */
    public int getQuality() {
        int qvalue, sum = 0, quality = 0, tempQ[];

        for (int i = 0; i < qTables.length; i++) {
            if (qTables[i] != null) {
                tempQ = qTables[i].getQ();
                for (int j = 0; j < 64; j++) {
                    sum += tempQ[j];
                }
            }
        }

        if ((qTables[0] != null) && (qTables[1] != null)) {

            qvalue = (int) (qTables[0].getQ()[2] + qTables[0].getQ()[53] + qTables[1].getQ()[0] + qTables[1].getQ()[63]);

            for (int i = 0; i < 100; i++) {
                if ((qvalue < DOUBLE_QUANT_HASH[i]) && (sum < DOUBLE_QUANT_SUMS[i])) {
                    continue;
                }

                if (((qvalue <= DOUBLE_QUANT_HASH[i]) && (sum <= DOUBLE_QUANT_SUMS[i])) || (i >= 50)) {
                    quality = i + 1;
                }

                break;
            }
        } else if (qTables[0] != null) {

            qvalue = (qTables[0].getQ()[2] + qTables[0].getQ()[53]);

            for (int i = 0; i < 100; i++) {
                if ((qvalue < SINGLE_QUANT_HASH[i]) && (sum < SINGLE_QUANT_SUMS[i])) {
                    continue;
                }

                if (((qvalue <= SINGLE_QUANT_HASH[i]) && (sum <= SINGLE_QUANT_SUMS[i])) || (i >= 50)) {
                    quality = i + 1;
                }

                break;
            }
        }

        return quality;
    }

    /**
     * @param huffTable
     * @param Ss
     * @param Se
     * @param Al
     * @param mcu
     * @return
     * @throws IOException
     * @throws JPEGMarkerException
     */
    protected int decodeACRefine(HuffmanTable huffTable, int Ss, int Se, int Al, int[] mcu, int blkIndex)
                                                                                                         throws IOException,
                                                                                                         JPEGMarkerException {
        int p1 = 1 << Al; /* 1 in the bit position being coded */
        int m1 = (-1) << Al; /* -1 in the bit position being coded */

        int k = Ss, s = 0, r = 0, coef = 0, offset = blkIndex * DCTSIZE2;

        if (EOBRUN == 0) {
            for (; k <= Se; k++) {
                s = huffTable.decode(in);

                r = s >> 4;
                s &= 15;

                if (s != 0) {
                    if (s != 1) { /* size of new coef should always be 1 */
                        // warn, ignore
                    }

                    if (in.readBit() != 0) {
                        s = p1; /* newly nonzero coef is positive */
                    } else {
                        s = m1; /* newly nonzero coef is negative */
                    }
                } else {
                    if (r != 15) {
                        EOBRUN = 1 << r; // EOBr, run length is 2^r + appended
                        // bits
                        if (r != 0) {
                            r = (int) in.readBits(r);

                            EOBRUN += r;
                        }
                        break; /* rest of block is handled by EOB logic */
                    }
                    /* note s = 0 for processing ZRL */
                }

                /*
                 * Advance over already-nonzero coefs and r still-zero coefs, appending correction bits to the
                 * nonzeroes. A correction bit is 1 if the absolute value of the coefficient must be increased.
                 */
                do {
                    coef = mcu[offset + MarkerConstants.BLOCK_NATURAL_ORDER[k]];
                    if (coef != 0) {
                        if (in.readBit() != 0) {
                            if ((coef & p1) == 0) { // do nothing if already set
                                if (coef >= 0) {
                                    coef += p1;
                                } else {
                                    coef += m1;
                                }

                                mcu[offset + MarkerConstants.BLOCK_NATURAL_ORDER[k]] = coef;
                            }
                        }
                    } else {
                        if (--r < 0) {
                            break; /* reached target zero coefficient */
                        }
                    }

                    k++;
                } while (k <= Se);

                if (s != 0) {
                    mcu[offset + MarkerConstants.BLOCK_NATURAL_ORDER[k]] = s;
                }
            }
        }

        if (EOBRUN > 0) {
            /*
             * Scan any remaining coefficient positions after the end-of-band (the last newly nonzero coefficient, if
             * any). Append a correction bit to each already-nonzero coefficient. A correction bit is 1 if the absolute
             * value of the coefficient must be increased.
             */
            for (; k <= Se; k++) {
                coef = mcu[offset + MarkerConstants.BLOCK_NATURAL_ORDER[k]];
                if (coef != 0) {
                    if (in.readBit() != 0) {
                        if ((coef & p1) == 0) { // do nothing if already changed
                            if (coef >= 0) {
                                coef += p1;
                            } else {
                                coef += m1;
                            }

                            mcu[offset + MarkerConstants.BLOCK_NATURAL_ORDER[k]] = coef;
                        }
                    }
                }
            }

            /* Count one block completed in EOB run */
            EOBRUN--;
        }

        return 0;
    }

    protected int decodeDCRefine(int Al, int[] mcu, int blkIndex) throws IOException, JPEGMarkerException {
        int bit = 0, p1 = 1 << Al;

        bit = in.readBit();
        if (bit != 0) {
            // Note: since we use |=, repeating the assignment later is safe
            mcu[blkIndex * DCTSIZE2 + 0] |= p1;
        }

        return 0;
    }

    protected int decodeACFirst(HuffmanTable huffTable, int Ss, int Se, int Al, int[] mcu, int blkIndex)
                                                                                                        throws IOException,
                                                                                                        JPEGMarkerException {
        int r = 0, s = 0, offset = blkIndex * DCTSIZE2;

        if (EOBRUN > 0) { /* if it's a band of zeroes... */
            EOBRUN--; /* ...process it now (we do nothing) */
        } else {
            for (int k = Ss; k <= Se; k++) {
                s = huffTable.decode(in);

                r = s >> 4;
                s &= 15;

                if (s != 0) {
                    k += r;
                    r = (int) in.readBits(s);
                    s = huffTable.extend(r, s);

                    // point transform and store
                    mcu[offset + MarkerConstants.BLOCK_NATURAL_ORDER[k]] = (s << Al);
                } else {
                    if (r == 15) { /* ZRL */
                        k += 15; /* skip 15 zeroes in band */
                    } else { /* EOBr, run length is 2^r + appended bits */
                        EOBRUN = (1 << r);
                        if (r != 0) { /* EOBr, r > 0 */
                            r = (int) in.readBits(r);
                            EOBRUN += r;
                        }

                        EOBRUN--; /* this band is processed at this moment */
                        break; /* force end-of-band */
                    }
                }
            }
        }

        return 1;
    }

    protected int decodeDCFirst(int index, HuffmanTable huffTable, int Al, int[] mcu, int blkIndex) throws IOException,
                                                                                                   JPEGMarkerException {
        int s, r;

        s = huffTable.decode(in);

        if (s > 0) {
            r = (int) in.readBits(s);
            s = huffTable.extend(r, s);
        }

        preDC[index] = s = preDC[index] + s;

        // point transform and store
        mcu[blkIndex * DCTSIZE2 + 0] = (s << Al);

        return 0;
    }

    public ExtendImageHeader getExtendImageHeader() {
        return this.extendImageHeader;
    }
}
