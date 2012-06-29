/**
 * Project: headquarters-biz-image File Created at 2010-6-17 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All
 * rights reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.render;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.BaseTest;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.ImageWrapper;

/**
 * TODO Comment of WriteRenderTest
 * 
 * @author wendell
 */
public class WriteRenderTest extends BaseTest {

    static File path = new File("./src/test/resources/conf.test/simpleimage/scale");

    protected ImageWrapper getImage() throws Exception {
        File input = new File(path.getCanonicalPath() + File.separator + "st.jpg");
        PlanarImage img = JAI.create("ImageRead", input);
        ImageWrapper imgWrapper = new ImageWrapper(img, 0);

        return imgWrapper;
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.WriteRender#WriteRender(javax.media.jai.PlanarImage, java.io.OutputStream, java.lang.String)}
     * .
     */
    public void testWriteRenderPlanarImageOutputStreamString() throws Exception {
        OutputStream output = null;
        ImageRender wr = null;
        try {
            output = new FileOutputStream(new File(resultDir, "WRITE_st.jpg"));
            wr = new WriteRender(getImage(), output);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.WriteRender#WriteRender(javax.media.jai.PlanarImage, java.io.File, java.lang.String)}
     * .
     */
    public void testWriteRenderPlanarImageFileString() throws Exception {
        ImageRender wr = null;
        try {
            File file = new File(resultDir, "WRITE_st.jpg");
            wr = new WriteRender(getImage(), file);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
        }
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.WriteRender#WriteRender(javax.media.jai.PlanarImage, java.lang.String, java.lang.String)}
     * .
     */
    public void testWriteRenderPlanarImageStringString() throws Exception {
        ImageRender wr = null;
        try {
            wr = new WriteRender(getImage(), path.getCanonicalPath() + File.separator
                    + "WRITE_st.jpg");

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
        }
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.WriteRender#WriteRender(com.alibaba.simpleimage.ImageRender, java.io.OutputStream, java.lang.String)}
     * .
     */
    public void testWriteRenderImageRenderOutputStreamString() throws Exception {
        OutputStream output = null;
        InputStream input = null;
        ImageRender wr = null;
        try {
            output = new FileOutputStream(path.getCanonicalPath() + File.separator
                    + "WRITE_st.jpg");
            input = new FileInputStream(new File(path, "st.jpg"));
            ImageRender rr = new ReadRender(input);
            wr = new WriteRender(rr, output);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.WriteRender#WriteRender(com.alibaba.simpleimage.ImageRender, java.io.File, java.lang.String)}
     * .
     */
    public void testWriteRenderImageRenderFileString() throws Exception {
        File file = new File(path.getCanonicalPath() + File.separator + "WRITE_st.jpg");

        InputStream input = null;
        ImageRender wr = null;
        try {
            input = new FileInputStream(new File(path, "st.jpg"));
            ImageRender rr = new ReadRender(input);
            wr = new WriteRender(rr, file);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(input);
        }
    }

    /**
     * Test method for
     * {@link com.alibaba.simpleimage.render.WriteRender#WriteRender(com.alibaba.simpleimage.ImageRender, java.lang.String, java.lang.String)}
     * .
     */
    public void testWriteRenderImageRenderStringString() throws Exception {
        String output = path.getCanonicalPath() + File.separator + "WRITE_st.jpg";

        InputStream input = null;
        ImageRender wr = null;
        try {
            input = new FileInputStream(new File(path, "st.jpg"));
            ImageRender rr = new ReadRender(input);
            wr = new WriteRender(rr, output);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(input);
        }
    }
}
