/**
 * Project: simpleimage-1.1 File Created at 2010-8-24 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.codec.jpeg;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.ImageWrapper;
import com.alibaba.simpleimage.io.ByteArrayOutputStream;
import com.alibaba.simpleimage.io.ImageInputStream;
import com.alibaba.simpleimage.io.ImageBitsInputStream;

/**
 * TODO Comment of JPEGDecodePerformanceTest
 * 
 * @author wendell
 */
public class JPEGDecodePerformanceTest extends TestCase {

    static File   imgDir    = new File("./src/test/resources/conf.test/simpleimage/codec/jpeg/performance");
    static int    TIMES     = 500;
    static int    FREQUENCY = 2;

    static String name      = "small.jpg";

    public void testNull() {
    	
    }

    protected void run(DecodeFacade decoder, File rootDir, String filename, int times, int frequency) throws Exception {
        long start, end, total = 0L;
        if (rootDir == null) {
            rootDir = imgDir;
        }

        FileInputStream inputStream = new FileInputStream(new File(rootDir, filename));

        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, temp);
        IOUtils.closeQuietly(inputStream);

        InputStream img = temp.toInputStream();
        temp = null;

        System.out.println("***********Performance Test**************");

        for (int i = 0; i < frequency; i++) {
            // System.gc();
            // Thread.sleep(5 * 1000L);

            start = System.currentTimeMillis();

            for (int t = 0; t < times; t++) {
                // File img = imgs[t % imgs.length];
                img.reset();
                BufferedImage bi = decoder.decode(img);
                bi.getHeight();
                bi = null;
            }

            end = System.currentTimeMillis();

            total += (end - start);
        }

        System.out.printf("Decoder : %s \n", decoder.getName());
        System.out.println("Image : " + filename);
        System.out.printf("Times : %d\n", times);
        System.out.printf("Frequency : %d\n", frequency);
        System.out.printf("Total time : %d ms\n", total);
        System.out.printf("Average time : %.2f ms\n", ((double) total / (times * frequency)));
    }

    interface DecodeFacade {

        public BufferedImage decode(InputStream img) throws IOException;

        public String getName();
    }

    protected DecodeFacade getAliDecoder() {
        DecodeFacade d = new DecodeFacade() {

            public BufferedImage decode(InputStream in) throws IOException {
                // FileInputStream in = null;
                ImageInputStream imageStream = null;
                try {
                    // in = new FileInputStream(img);
                    imageStream = new ImageBitsInputStream(in);
                    JPEGDecoder decoder = new JPEGDecoder(imageStream);
                    ImageWrapper wi = decoder.decode();

                    return wi.getAsBufferedImage();
                } finally {
                    // IOUtils.closeQuietly(in);
                    if (imageStream != null) {
                        imageStream.close();
                    }
                }
            }

            public String getName() {
                return "Ali Decoder";
            }

        };

        return d;
    }

    protected DecodeFacade getJDKDecoder() {
        DecodeFacade d = new DecodeFacade() {

            public BufferedImage decode(InputStream img) throws IOException {
                javax.imageio.stream.ImageInputStream stream = null;

                try {
                    stream = ImageIO.createImageInputStream(img);
                    Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
                    ImageReader reader = null;
                    while (readers.hasNext()) {
                        reader = readers.next();
                        if (reader.canReadRaster()) {
                            break;
                        }
                    }

                    reader.setInput(stream);
                    BufferedImage bi = reader.read(0);

                    return bi;
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }

            public String getName() {
                return "JDK Decoder";
            }

        };

        return d;
    }

    public static void main(String[] args) throws Exception {
        String filename = "big.jpg";
        int times = TIMES;
        int frequency = FREQUENCY;
        String decoder = "Ali";
        String imgDirStr = "/src/test/resources/conf.test/simpleimage/codec/jpeg/performance";
        File imgDir = null;

        if (args.length > 0) {
            imgDir = new File(args[0].trim() + imgDirStr);
        }

        if (args.length > 1) {
            filename = args[1].trim();
        }

        if (args.length > 2) {
            times = Integer.parseInt(args[2]);
        }

        if (args.length > 3) {
            frequency = Integer.parseInt(args[3]);
        }

        if (args.length > 4) {
            decoder = args[4].trim();
        }

        JPEGDecodePerformanceTest testInstance = new JPEGDecodePerformanceTest();

        if ("JDK".equalsIgnoreCase(decoder)) {
            testInstance.run(testInstance.getJDKDecoder(), imgDir, filename, times, frequency);
        } else {
            testInstance.run(testInstance.getAliDecoder(), imgDir, filename, times, frequency);
        }
    }
}
