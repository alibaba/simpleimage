/**
 * Project: simpleimage File Created at 2011-3-4 $Id$ Copyright 2008 Alibaba.com Croporation Limited. All rights
 * reserved. This software is the confidential and proprietary information of Alibaba Company.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.alibaba.simpleimage.jai.scale;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.simpleimage.ImageFormat;
import com.alibaba.simpleimage.ImageRender;
import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleRender;
import com.alibaba.simpleimage.render.WriteRender;
import com.alibaba.simpleimage.render.ScaleParameter.Algorithm;

/**
 * TODO Comment of LanczosTest
 * 
 * @author wendell
 */
public class LanczosTest extends TestCase {

    static final File dir = new File("./src/test/resources/conf.test/simpleimage/scale");
    static final File resultDir = new File("./src/test/resources/conf.test/simpleimage/result");
    
    public void testQuality() throws Exception {
        String[] imgNames = { "color.jpg", "334.jpg", "sawtooth.jpg", "airport.jpg", "4.jpg", "6.jpg", "7.jpg",
                "8.jpg", "9.jpg", "IMG_0033.jpg" };
        for (String imgName : imgNames) {
            File in = new File(dir, imgName);
            File out = new File(resultDir, "LANCZOS_" + getShortname(imgName) + "_java_lanczos_1024_result.jpg");
            ScaleParameter scaleParam = new ScaleParameter(1024, 1024, Algorithm.LANCZOS);
            doScale(in, out, scaleParam);
        }
    }

    public void testQuailty2() throws Exception {
        String[] imgNames = { "st.jpg", "yuan.jpg" };
        for (String imgName : imgNames) {
            File in = new File(dir, imgName);
            File out = new File(resultDir, "LANCZOS_" + getShortname(imgName) + "_java_lanczos_300_result.jpg");
            ScaleParameter scaleParam = new ScaleParameter(300, 300, Algorithm.LANCZOS);
            doScale(in, out, scaleParam);
        }
    }

    public void testScaleGIF() throws Exception {
        File gif = new File(dir, "box_disapper.gif");
        File dest = new File(resultDir, "LANCZOS_box_disapper_java_lanczos_result.gif");
        ScaleParameter scaleParam = new ScaleParameter(310, 310, Algorithm.LANCZOS);
        doScale(gif, dest, scaleParam, ImageFormat.GIF, true);
    }

    public void testPNG() throws Exception {
        File png = new File(dir, "rings.png");
        File dest = new File(resultDir, "LANCZOS_rings_java_lanczos_result.jpg");
        ScaleParameter scaleParam = new ScaleParameter(310, 310, Algorithm.LANCZOS);
        doScale(png, dest, scaleParam);

        png = new File(dir, "a.png");
        dest = new File(resultDir, "LANCZOS_a_java_lanczos_result.jpg");
        scaleParam = new ScaleParameter(832, 1176, Algorithm.LANCZOS);
        doScale(png, dest, scaleParam);
    }
    
    public void testScaleGray() throws Exception {
        File gif = new File(dir, "gray.jpg");
        File dest = new File(resultDir, "LANCZOS_gray_java_lanczos_result.jpg");
        ScaleParameter scaleParam = new ScaleParameter(310, 310, Algorithm.LANCZOS);
        doScale(gif, dest, scaleParam, ImageFormat.JPEG, false );
    }

    public void testScaleCMYK() throws Exception {
        File cmyk = new File("./src/test/resources/conf.test/simpleimage/cmyk/cmyk_noprofile_1.jpg");
        File dest = new File(resultDir, "LANCZOS_cmyk1_java_lanczos_result.jpg");
        ScaleParameter scaleParam = new ScaleParameter(512, 512, Algorithm.LANCZOS);
        doScale(cmyk, dest, scaleParam, ImageFormat.JPEG, false);
        
        cmyk = new File("./src/test/resources/conf.test/simpleimage/cmyk/cmyk_noprofile_2.jpg");
        dest = new File(resultDir, "LANCZOS_cmyk2_java_lanczos_result.jpg");
        doScale(cmyk, dest, scaleParam, ImageFormat.JPEG, false);
        
        cmyk = new File("./src/test/resources/conf.test/simpleimage/cmyk/cmyk_noprofile_3.jpg");
        dest = new File(resultDir, "LANCZOS_cmyk3_java_lanczos_result.jpg");
        doScale(cmyk, dest, scaleParam, ImageFormat.JPEG, false);
        
        cmyk = new File("./src/test/resources/conf.test/simpleimage/cmyk/ycck_embedprofile_1.jpg");
        dest = new File(resultDir, "LANCZOS_ycck1_java_lanczos_result.jpg");
        doScale(cmyk, dest, scaleParam, ImageFormat.JPEG, false);
        
        cmyk = new File("./src/test/resources/conf.test/simpleimage/cmyk/ycck_embedprofile_2.jpg");
        dest = new File(resultDir, "LANCZOS_ycck2_java_lanczos_result.jpg");
        doScale(cmyk, dest, scaleParam, ImageFormat.JPEG, false);
        
        cmyk = new File("./src/test/resources/conf.test/simpleimage/cmyk/ycck_noprofile.jpg");
        dest = new File(resultDir, "LANCZOS_ycckn_java_lanczos_result.jpg");
        doScale(cmyk, dest, scaleParam, ImageFormat.JPEG, false);
    }
    
    
    public void testScaleBounds() throws Exception {
        File img = new File(dir, "color.jpg");
        for (int i = 1; i < 32; i++) {
            File dest = new File(resultDir, "LANCZOS_color_java_lanczos_" + i + "_result.jpg");
            ScaleParameter scaleParam = new ScaleParameter(i, i, Algorithm.LANCZOS);
            doScale(img, dest, scaleParam);
        }

        for (int i = 31; i < 150; i += 8) {
            File dest = new File(resultDir, "LANCZOS_color_java_lanczos_" + i + "_result.jpg");
            ScaleParameter scaleParam = new ScaleParameter(i, i, Algorithm.LANCZOS);
            doScale(img, dest, scaleParam);
        }

        for (int i = 1000; i <= 2000; i += 100) {
            File dest = new File(resultDir, "LANCZOS_color_java_lanczos_" + i + "_result.jpg");
            ScaleParameter scaleParam = new ScaleParameter(i, i, Algorithm.LANCZOS);
            doScale(img, dest, scaleParam);
        }
    }
    
    public void testSuperLargeImg() throws Exception {
        File img = new File(dir, "flower.jpg");
        File dest = new File(resultDir, "LANCZOS_flower_java_lanczos_result.jpg");
        ScaleParameter scaleParam = new ScaleParameter(1024, 1024, ScaleParameter.Algorithm.LANCZOS);
        doScale(img, dest, scaleParam);
    }

    private String getShortname(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        return name.substring(0, name.lastIndexOf("."));
    }
    
    private void doScale(File in, File out, ScaleParameter scaleParam) throws Exception {
        doScale(in, out, scaleParam, ImageFormat.JPEG, true);
    }

    private void doScale(File in, File out, ScaleParameter scaleParam, ImageFormat format, boolean toRGB) throws Exception {
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        ImageRender wr = null;
        try {
            inStream = new FileInputStream(in);
            outStream = new FileOutputStream(out);
            ImageRender rr = new ReadRender(inStream, toRGB);
            ImageRender sr = new ScaleRender(rr, scaleParam);
            wr = new WriteRender(sr, outStream, format);
            wr.render();
        } finally {
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(outStream);
            if (wr != null) {
                wr.dispose();
            }
        }
    }

}
