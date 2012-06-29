/**
 * Project: simpleimage-1.1 File Created at 2010-8-10 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.codec.jpeg;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.io.ImageInputStream;
import com.alibaba.simpleimage.io.ImageBitsInputStream;

/**
 * @author wendell
 */
public class JPEGDecoderFunctionTest extends TestCase {

    static File jpegs      = new File("./src/test/resources/conf.test/simpleimage/codec/jpeg");

    static int  DIFF_VALUE = 10;

    public void testColorCMYKProg() throws Exception {
        // JAI JPEG image reader can't read progressive CMYK picture
        // readAndCompare("color", "cmyk_prog.jpg");
    }

    public void testColorCMYKSeq() throws Exception {
//        readAndCompare("color", "cmyk_seq.jpg", true);
    }

    public void testColorGrayProg() throws Exception {
        readAndCompare("color", "gray_prog.jpg", false);
    }

    public void testColorGraySeq() throws Exception {
        readAndCompare("color", "gray_seq.jpg", false);
    }

    public void testColorRGBProg() throws Exception {
        readAndCompare("color", "rgb_prog.jpg", false);
    }

    public void testColorRGBSeq() throws Exception {
        readAndCompare("color", "rgb_seq.jpg", false);
    }

    public void testColorBoundsWhite() throws Exception {
        readAndCompare("color", "rgb_bounds_seq_white.jpg", false);
    }

    public void testColorBoundsColor() throws Exception {
        readAndCompare("color", "rgb_bounds_seq_color.jpg", false);
    }

    public void testProgNoDRI() throws Exception {
        readAndCompare("DRI", "prog_noDRI.jpg", false);
    }

    public void testSeqDRI() throws Exception {
        readAndCompare("DRI", "seq_DRI.jpg", false);
    }

    public void testSeqNoDRI() throws Exception {
        readAndCompare("DRI", "seq_noDRI.jpg", true);
    }

    public void testPrecision() throws Exception {

    }

//    public void testCMYKEmbedProfile() throws Exception {
//        File profileDir = new File(jpegs, "profile");
//        File img = new File(profileDir, "cmyk_embedprofile.jpg");
//
//        ImageWrapper wi = getDecodedImage(img);
//        assertTrue(wi.getExtendImageHeader().isExistProfile());
//    }

//    public void testCMYKNoProfile() throws Exception {
//        File profileDir = new File(jpegs, "profile");
//        File img = new File(profileDir, "cmyk_noprofile.jpg");
//
//        ImageWrapper wi = getDecodedImage(img);
//        assertFalse(wi.getExtendImageHeader().isExistProfile());
//    }
//
//    public void testRGBEmbedProfile() throws Exception {
//        File profileDir = new File(jpegs, "profile");
//        File img = new File(profileDir, "rgb_embedprofile.jpg");
//
//        ImageWrapper wi = getDecodedImage(img);
//        assertTrue(wi.getExtendImageHeader().isExistProfile());
//    }
//
//    public void testRGBNoProfile() throws Exception {
//        File profileDir = new File(jpegs, "profile");
//        File img = new File(profileDir, "rgb_noprofile.jpg");
//
//        ImageWrapper wi = getDecodedImage(img);
//        assertFalse(wi.getExtendImageHeader().isExistProfile());
//    }
//
//    public void testProfileError() throws Exception {
//        File profileDir = new File(jpegs, "profile");
//        File img = new File(profileDir, "errimg-profileerror.jpg");
//
//        ImageWrapper wi = getDecodedImage(img);
//        assertFalse(wi.getExtendImageHeader().isExistProfile());
//    }

    public void testProgressive() throws Exception {
        readAndCompare("progressive", "prog_adobe_3.jpg", false);
        readAndCompare("progressive", "prog_adobe_4.jpg", false);
        readAndCompare("progressive", "prog_adobe_5.jpg", false);
    }

    public void testIJGQuality() throws Exception {
        File profileDir = new File(jpegs, "quality");

        File img = new File(profileDir, "quality_80.jpg");
        assertTrue(getQuality(img) == 80);

        img = new File(profileDir, "quality_90.jpg");
        assertTrue(getQuality(img) == 90);

        img = new File(profileDir, "quality_95.jpg");
        assertTrue(getQuality(img) == 95);

        img = new File(profileDir, "quality_unknow.jpg");
        assertTrue(getQuality(img) == 99);
    }

    // YCbCr H2V2 (2x2:1:1, 6 blocks per MCU)
    public void testSeqSampleH2V2() throws Exception {
        readAndCompare("sample", "seq_2x2_1x1_1x1.jpg", true);
    }

    //YCbCr H1V1 (1x1:1:1, 3 blocks per MCU)
    public void testSeqSampleH1V1() throws Exception {
        readAndCompare("sample", "seq_1x1_1x1_1x1.jpg", false);
    }

    // YCbCr H2V1 (2x1:1:1, 4 blocks per MCU)
    public void testSeqSampleH2V1() throws Exception {
        readAndCompare("sample", "seq_2x1_1x1_1x1.jpg", true);
    }

    // YCbCr H1V2 (1x2:1:1, 4 blocks per MCU)
    public void testSeqSampleH1V2() throws Exception {
        readAndCompare("sample", "seq_1x2_1x1_1x1.jpg", true);
    }

    public void testProgSampleH1V1() throws Exception {
        readAndCompare("sample", "prog_5_111.jpg", false);
    }

    public void testProgSampleH2V2_1() throws Exception {
        readAndCompare("sample", "prog_3_401x350_411.jpg", true);
    }

    public void testProgSampleH2V2_2() throws Exception {
        readAndCompare("sample", "prog_3_64x64_411.jpg", true);
    }

    public void testProgSampleH2V2() throws Exception {
        readAndCompare("sample", "prog_4_299x386_411.jpg", true);
    }

    public void testProgSampleH2V1() throws Exception {

    }

    public void testProgSampleH1V2() throws Exception {

    }

    public void testSeqBaseline111() throws Exception {
        readAndCompare("seq", "seq_baseline_111.jpg", false);
    }

    public void testSeqBaseline411() throws Exception {
        readAndCompare("seq", "seq_baseline_411.jpg", true);
    }

    public void testSeqOpt111() throws Exception {
        readAndCompare("seq", "seq_opt_111.jpg", false);
    }

    public void testSeqOpt411() throws Exception {
        readAndCompare("seq", "seq_opt_411.jpg", true);
    }

    public void testSize() throws Exception {
        File sizeDir = new File(jpegs, "size");

        for (File f : sizeDir.listFiles()) {
            if (!f.getName().endsWith("jpg")) {
                continue;
            }
            boolean ignoreErr = false;

            if (f.getName().indexOf("411") > 0) {
                ignoreErr = true;
            }

            try {
                readAndCompare("size", f.getName(), ignoreErr);
            } catch (Exception e) {
                System.out.println(f.getName() + " decode error");
                throw e;
            }
        }
    }

    protected void readAndCompare(String subDir, String filename, boolean ignoreError)
            throws Exception {
        File imgDir = new File(jpegs, subDir);
        File img = new File(imgDir, filename);

        BufferedImage left = getDecodedImage(img).getAsBufferedImage();
        BufferedImage right = getStandardImage(img);

        compareImage(img.getName(), left, right, ignoreError);
    }

    protected int getQuality(File f) throws Exception {
        FileInputStream in = null;
        ImageInputStream imageStream = null;
        try {
            in = new FileInputStream(f);
            imageStream = new ImageBitsInputStream(in);
            JPEGDecoder decoder = new JPEGDecoder(imageStream, false, false);
            decoder.decode();

            return decoder.getQuality();
        } finally {
            IOUtils.closeQuietly(in);
            if (imageStream != null) {
                imageStream.close();
            }
        }
    }

    protected ImageWrapper getDecodedImage(File f) throws Exception {
        FileInputStream in = null;
        ImageInputStream imageStream = null;
        try {
            in = new FileInputStream(f);
            imageStream = new ImageBitsInputStream(in);
            JPEGDecoder decoder = new JPEGDecoder(imageStream, false, false);
            ImageWrapper wi = decoder.decode();

            return wi;
        } finally {
            IOUtils.closeQuietly(in);
            if (imageStream != null) {
                imageStream.close();
            }
        }
    }

    protected BufferedImage getStandardImage(File f) throws Exception {
        return ImageIO.read(f);
    }

    protected void compareImage(String name, BufferedImage left, BufferedImage right,
                                boolean ignoreError) throws Exception {
        if (left.getWidth() != right.getWidth() || left.getHeight() != right.getHeight()) {
            assertTrue("size not equal", false);
        }

        Raster leftRaster = left.getData();
        Raster rightRaster = right.getData();
        int[] leftPixes = new int[4];
        int[] rightPixes = new int[4];

        for (int x = 0; x < right.getWidth(); x++) {
            for (int y = 0; y < right.getHeight(); y++) {
                leftPixes = leftRaster.getPixel(x, y, leftPixes);
                rightPixes = rightRaster.getPixel(x, y, rightPixes);

                for (int i = 0; i < leftRaster.getNumBands(); i++) {
                    if (Math.abs(leftPixes[i] - rightPixes[i]) > DIFF_VALUE) {
                        if (!ignoreError) {
                            assertTrue(name + "'s pix not equal, sub is "
                                    + (leftPixes[i] - rightPixes[i]), false);
                        }
                    }

                    leftPixes[i] = 0;
                    rightPixes[i] = 0;
                }
            }
        }
    }
}
