/**
 * Project: simpleimage-1.1 File Created at 2010-9-6 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.io.ByteArrayOutputStream;
import com.alibaba.simpleimage.jai.scale.LanczosScaleOp;
import com.alibaba.simpleimage.render.ReadRender;
import com.sun.media.jai.opimage.SubsampleAverageOpImage;

/**
 * TODO Comment of ScaleSpeedTest
 * 
 * @author wendell
 */
public class ScaleSpeedTest extends TestCase {

    static File scaleDir = new File("./src/test/resources/conf.test/simpleimage/scale");

    public static void main(String[] args) throws Exception {
        String method = "CSubSample";
        String imgName = "large";
        int times = 1000;
        float scale = 0.5f;
        String imgDir = "/src/test/resources/conf.test/simpleimage/scale";
        File rootDir = null;

        if (args.length > 0) {
            rootDir = new File(args[0].trim() + imgDir);
        }

        if (args.length > 1) {
            method = args[1];
        }

        if (args.length > 2) {
            imgName = args[2];
        }

        if (args.length > 3) {
            scale = Float.parseFloat(args[3]);
        }

        if (args.length > 4) {
            times = Integer.parseInt(args[4]);
        }

        ScaleSpeedTest instance = new ScaleSpeedTest();

        if ("JSubSample".equalsIgnoreCase(method)) {
            instance.doScale(rootDir, imgName, instance.new JSubSampleScaler(), times, scale);
        } else if ("CSubSample".equalsIgnoreCase(method)) {
            instance.doScale(rootDir, imgName, instance.new CSubSampleScaler(), times, scale);
        } else if ("Bicurbe".equalsIgnoreCase(method)) {
            instance.doScale(rootDir, imgName, instance.new BicurbeScaler(), times, scale);
        } else if ("Bicurbe2".equalsIgnoreCase(method)) {
            instance.doScale(rootDir, imgName, instance.new Bicube2Scaler(), times, scale);
        } else if ("lanczos".equalsIgnoreCase(method)) {
            instance.doScale(rootDir, imgName, instance.new LanczosScaler(), times, scale);
        } else {
            throw new IllegalArgumentException("Unknown alg");
        }
    }

    public void testSpeed() throws Exception {
//        doScale(scaleDir, "color.jpg", new LanczosScaler(), 1, 0.256f);
    }

    public void doScale(File rootDir, String imgName, Scaler scaler, int times, float scale) throws Exception {
        FileInputStream inputStream = new FileInputStream(new File(rootDir, imgName));

        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, temp);
        IOUtils.closeQuietly(inputStream);

        InputStream img = temp.toInputStream();
        temp = null;

        System.out.println("***********Scale Performance Test**************");
        long start = 0L, end = 0L, total = 0L;

        img.reset();

        ReadRender rr = new ReadRender(img, false);
        ImageWrapper wi = rr.render();
        BufferedImage bi = wi.getAsBufferedImage();
        
        for (int i = 0; i < times; i++) {      
            start = System.currentTimeMillis();
            PlanarImage zoomOp = scaler.doScale(PlanarImage.wrapRenderedImage(bi), scale);
            zoomOp.getAsBufferedImage();
            end = System.currentTimeMillis();
            total += (end - start);
        }

        System.out.printf("Scale alg : %s \n", scaler.getName());
        System.out.println("Image : " + imgName);
        System.out.printf("Times : %d\n", times);
        System.out.printf("Total time : %d ms\n", total);
        System.out.printf("Average time : %.2f ms\n", ((double) total / times));
    }

    interface Scaler {

        public PlanarImage doScale(PlanarImage in, float scale);

        public String getName();
    }

    class BicurbeScaler implements Scaler {

        /*
         * (non-Javadoc)
         * @see com.alibaba.simpleimage.ScaleSpeedTest.Scaler#doScale(float)
         */
        public PlanarImage doScale(PlanarImage in, float scale) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(in);
            pb.add(scale);
            pb.add(scale);
            pb.add(0.0F);
            pb.add(0.0F);
            pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
            RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                             RenderingHints.VALUE_RENDER_QUALITY);
            return JAI.create("scale", pb, qualityHints);
        }

        public String getName() {
            return "Bicurbe";
        }
    }

    class Bicube2Scaler implements Scaler {

        /*
         * (non-Javadoc)
         * @see com.alibaba.simpleimage.ScaleSpeedTest.Scaler#doScale(float)
         */
        public PlanarImage doScale(PlanarImage in, float scale) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(in);
            pb.add(scale);
            pb.add(scale);
            pb.add(0.0F);
            pb.add(0.0F);
            pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
            RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                             RenderingHints.VALUE_RENDER_QUALITY);
            return JAI.create("scale", pb, qualityHints);
        }

        public String getName() {
            return "Bicurbe2";
        }
    }

    class CSubSampleScaler implements Scaler {

        /*
         * (non-Javadoc)
         * @see com.alibaba.simpleimage.ScaleSpeedTest.Scaler#doScale(javax.media .jai.PlanarImage, float)
         */
        public PlanarImage doScale(PlanarImage in, float scale) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(in);
            pb.add((double) scale);
            pb.add((double) scale);

            RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                             RenderingHints.VALUE_RENDER_QUALITY);
            return JAI.create("SubsampleAverage", pb, qualityHints);
        }

        public String getName() {
            return "CSubSample";
        }
    }

    class JSubSampleScaler implements Scaler {

        /*
         * (non-Javadoc)
         * @see com.alibaba.simpleimage.ScaleSpeedTest.Scaler#doScale(javax.media .jai.PlanarImage, float)
         */
        public PlanarImage doScale(PlanarImage in, float scale) {
            PlanarImage zoomOp = new SubsampleAverageOpImage(in, null, null, (double) scale, (double) scale);

            return zoomOp;
        }

        public String getName() {
            return "JSubSample";
        }
    }

    class LanczosScaler implements Scaler {

        public PlanarImage doScale(PlanarImage in, float scale) {
            LanczosScaleOp lanczosOp = new LanczosScaleOp(scale, scale);
            BufferedImage dest = lanczosOp.compute(in.getAsBufferedImage());
            
            return PlanarImage.wrapRenderedImage(dest);
        }

        public String getName() {
            return "Lanczos";
        }
    }
}
