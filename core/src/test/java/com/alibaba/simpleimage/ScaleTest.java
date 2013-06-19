/**
 * Project: headquarters-biz-image File Created at 2010-6-11 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All
 * rights reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.jai.scale.LanczosScaleOp;
import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.WriteRender;

/**
 * TODO Comment of ScaleTest
 * 
 * @author wendell
 */
public class ScaleTest extends BaseTest {

    static final File path   = new File("./src/test/resources/conf.test/simpleimage/scale");

    static float      scale  = 0.2f;
    static double     dScale = 0.2;

    public void testNearest() throws Exception {
        doScale("Nearest");
    }

    public void testBilinear() throws Exception {
        doScale("Bilinear");
    }

    public void testBicubic() throws Exception {
        doScale("Bicubic");
    }

    public void testBicubic2() throws Exception {
        doScale("Bicubic2");
    }

    public void testSubsampleAverage() throws Exception {
        doScale("SubsampleAverage");
    }
    
    public void testLanczos() throws Exception {
        doScale("Lanczos");
    }

    public void doScale(String name) throws Exception {
        for (File imgFile : path.listFiles()) {
            if (imgFile.getName().lastIndexOf("jpg") < 0) {
                continue;
            }

            if (imgFile.getName().indexOf("result") > 0) {
                continue;
            }

            String filename = imgFile.getName().substring(0, imgFile.getName().lastIndexOf("."));
            InputStream in = new FileInputStream(imgFile);
            OutputStream out = new FileOutputStream(new File(resultDir, "SCALE_" + filename + "-" + name + ".jpg"));
            WriteRender wr = null;
            try {
                ReadRender rr = new ReadRender(in, true);

                ImageWrapper wi = rr.render();
                PlanarImage img = wi.getAsPlanarImage();
                if ("progbicu".equalsIgnoreCase(name)) {
                    img = doProgressiveBicubic(img);
                } else if ("Nearest".equalsIgnoreCase(name)) {
                    img = doScaleNearest(img);
                } else if ("Bilinear".equalsIgnoreCase(name)) {
                    img = doScaleBilinear(img);
                } else if ("Bicubic".equalsIgnoreCase(name)) {
                    img = doScaleBicubic(img);
                } else if ("Bicubic2".equalsIgnoreCase(name)) {
                    img = doScaleBicubic2(img);
                } else if ("SubsampleAverage".equalsIgnoreCase(name)) {
                    img = doSubsampleAverage(img);
                } else if ("lanczos".equalsIgnoreCase(name)){
                    img = doLanczos(img);
                } else {
                    throw new IllegalArgumentException("Unknown alg " + name);
                }

                wi.setImage(img);
                wr = new WriteRender(wi, out);

                wr.render();
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    public PlanarImage doProgressiveBicubic(PlanarImage op) throws Exception {
        int dstWidth = 220;
        int w = op.getWidth();

        while (w > dstWidth) {
            scale = 0.6f;

            ParameterBlock pb = new ParameterBlock();
            pb.addSource(op);
            pb.add(scale);
            pb.add(scale);
            pb.add(0.0F);
            pb.add(0.0F);
            pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
            RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                             RenderingHints.VALUE_RENDER_QUALITY);
            op = JAI.create("scale", pb, qualityHints);

            w = (int) (scale * w);
        }

        return op;
    }

    public PlanarImage doScaleNearest(PlanarImage op) throws Exception {
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(op);
        pb.add(scale);
        pb.add(scale);
        pb.add(0.0F);
        pb.add(0.0F);
        pb.add(Interpolation.getInstance(Interpolation.INTERP_NEAREST));
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                         RenderingHints.VALUE_RENDER_QUALITY);
        op = JAI.create("scale", pb, qualityHints);

        return op;
    }

    public PlanarImage doScaleBilinear(PlanarImage op) throws Exception {
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(op);
        pb.add(scale);
        pb.add(scale);
        pb.add(0.0F);
        pb.add(0.0F);
        pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                         RenderingHints.VALUE_RENDER_QUALITY);
        op = JAI.create("scale", pb, qualityHints);

        return op;
    }

    public PlanarImage doScaleBicubic(PlanarImage op) throws Exception {
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(op);
        pb.add(scale);
        pb.add(scale);
        pb.add(0.0F);
        pb.add(0.0F);
        pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                         RenderingHints.VALUE_RENDER_QUALITY);
        op = JAI.create("scale", pb, qualityHints);

        return op;
    }

    public PlanarImage doScaleBicubic2(PlanarImage op) throws Exception {
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(op);
        pb.add(scale);
        pb.add(scale);
        pb.add(0.0F);
        pb.add(0.0F);
        pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));
        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                         RenderingHints.VALUE_RENDER_QUALITY);
        op = JAI.create("scale", pb, qualityHints);

        return op;
    }

    public PlanarImage doSubsampleAverage(PlanarImage op) throws Exception {
        double zoom = dScale;
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(op);
        pb.add(zoom);
        pb.add(zoom);

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                                                         RenderingHints.VALUE_RENDER_QUALITY);
        RenderedOp zoomOp = JAI.create("SubsampleAverage", pb, qualityHints);

        return zoomOp;
    }
    
    public PlanarImage doLanczos(PlanarImage op) throws Exception {
        LanczosScaleOp scaleOp = new LanczosScaleOp(dScale, dScale);
        BufferedImage dst = scaleOp.compute(op.getAsBufferedImage());
        
        return PlanarImage.wrapRenderedImage(dst);
    }
}
