package com.alibaba.simpleimage;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import com.alibaba.simpleimage.render.DrawTextParameter;
import com.alibaba.simpleimage.render.DrawTextRender;
import com.alibaba.simpleimage.render.FixDrawTextItem;
import com.alibaba.simpleimage.render.ReadRender;
import com.alibaba.simpleimage.render.ScaleParameter;
import com.alibaba.simpleimage.render.ScaleRender;
import com.alibaba.simpleimage.render.WriteRender;

public class FullStackTest extends BaseTest {

    static File path = new File("./src/test/resources/conf.test/simpleimage");

    public void doDrawScaleWrite(String dirName, String sourceName, String destName)
            throws Exception {
        // read
        InputStream in = null;
        OutputStream output = null;
        ImageRender wr = null;
        File dir = new File(path, dirName);
        File resultFile = new File(resultDir, destName);

        try {
            File f = new File(dir, sourceName);
            in = new FileInputStream(f);
            ReadRender rr = new ReadRender(in);

            DrawTextParameter dp = new DrawTextParameter();
            dp.addTextInfo(new FixDrawTextItem("1234554321"));
            DrawTextRender dtr = new DrawTextRender(rr, dp);
            // scale
            ScaleParameter param = new ScaleParameter(1024, 1024,
                    ScaleParameter.Algorithm.AUTO);
            ImageRender sr = new ScaleRender(dtr, param);

            output = new FileOutputStream(resultFile);
            ImageFormat outputFormat = sourceName.endsWith("gif") ? ImageFormat.GIF : ImageFormat.JPEG;
            wr = new WriteRender(sr, output, outputFormat);

            wr.render();
        } finally {
            if (wr != null) {
                wr.dispose();
            }
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(in);
        }

        doCheckResult(resultFile);
    }

    public void testConvertDrawScaleGIF() throws Exception {
        doDrawScaleWrite("gif", "GIF89a_1.gif", "GIF89-result.gif");
        doDrawScaleWrite("gif", "model.gif", "model-result.gif");
        doDrawScaleWrite("gif", "super1_gif.gif", "super1-result.gif");
        doDrawScaleWrite("gif", "super2_gif.gif", "super2-result.gif");
    }

    public void testConvertDrawScaleTIFF() throws Exception {
        doDrawScaleWrite("tiff", "input_16_matte.tiff", "input_16_matte-result.jpg");
        doDrawScaleWrite("tiff", "input_16.tiff", "input_16-result.jpg");
        doDrawScaleWrite("tiff", "input_256_planar_contig.tiff",
                "input_256_planar_contig-result.jpg");
        doDrawScaleWrite("tiff", "input_256_planar_separate.tiff",
                "input_256_planar_separate-result.jpg");
        doDrawScaleWrite("tiff", "input_256.tiff", "input_256-result.jpg");
        doDrawScaleWrite("tiff", "input_gray_4bit.tiff", "input_gray_4bit-result.jpg");
        doDrawScaleWrite("tiff", "input_gray_8bit.tiff", "input_gray_8bit-result.jpg");

        doDrawScaleWrite("tiff", "input_truecolor_stripped.tiff",
                "input_truecolor_stripped-result.jpg");
        doDrawScaleWrite("tiff", "input_truecolor_tiled32x32.tiff",
                "input_truecolor_tiled32x32-result.jpg");
        doDrawScaleWrite("tiff", "input_truecolor.tiff", "input_truecolor-result.jpg");
    }

    public void testConvertDrawScalePNG() throws Exception {
        doDrawScaleWrite("png", "input_256.png", "input_256png-result.jpg");
        doDrawScaleWrite("png", "super1_png.png", "super1_png-result.jpg");
        doDrawScaleWrite("png", "super2_png.png", "super2_png-result.jpg");
        doDrawScaleWrite("png", "input_bw.png", "input_bwpng-result.jpg");
        doDrawScaleWrite("png", "input_mono.png", "input_monopng-result.jpg");
        doDrawScaleWrite("png", "input_truecolor.png", "input_truecolorpng-result.jpg");
    }

    public void testConvertDrawScaleBMP() throws Exception {
        doDrawScaleWrite("bmp", "normal.bmp", "normal_bmp-result.jpg");
        doDrawScaleWrite("bmp", "super1_bmp.bmp", "super1_bmp-result.jpg");
        doDrawScaleWrite("bmp", "super2_bmp.bmp", "super2_bmp-result.jpg");
    }

    public void testBandsError() throws Exception {
        // 这张图片转换颜色空间失败，会从彩色照片转为黑白照片，原因是JAI认为该图片的ColorSpace的TYPE为灰度
//        doDrawScaleWrite("tiff", "input_256_matte.tiff", "input_256_matte-result.jpg");
        doDrawScaleWrite("tiff", "input_gray_8bit_matte.tiff",
                "input_gray_8bit_matte-result.jpg");
    }

    public void testBaselineLimtiedError() throws Exception {
        doDrawScaleWrite("tiff", "input_gray_12bit.tiff", "input_gray_12bit-result.jpg");
        doDrawScaleWrite("tiff", "input_gray_16bit.tiff", "input_gray_16bit-result.jpg");
        doDrawScaleWrite("tiff", "input_truecolor_16.tiff", "input_truecolor_16-result.jpg");
        doDrawScaleWrite("png", "input_16.png", "input_16png-result.jpg");
    }

    public void testDrawScaleWriteJpeg() throws Exception {
        doDrawScaleWrite("cmyk", "cmyk_noprofile_1.jpg", "cmyk_noprofile_1-result.jpg");
        doDrawScaleWrite("cmyk", "cmyk_noprofile_2.jpg", "ccmyk_noprofile_2-result.jpg");
        doDrawScaleWrite("cmyk", "cmyk_noprofile_3.jpg", "cmyk_noprofile_3-result.jpg");
        doDrawScaleWrite("cmyk", "ycck_embedprofile_1.jpg", "ycck_embedprofile_1-result.jpg");
        doDrawScaleWrite("cmyk", "ycck_embedprofile_2.jpg", "ycck_embedprofile_2-result.jpg");
        doDrawScaleWrite("cmyk", "ycck_noprofile.jpg", "ycck_noprofile-result.jpg");

        doDrawScaleWrite("malformed", "datadamge.jpg", "datadamge-result.jpg");
        doDrawScaleWrite("malformed", "huffcodeerror.jpg", "huffcodeerror-result.jpg");
        doDrawScaleWrite("malformed", "prematureend.jpg", "prematureend-result.jpg");
        doDrawScaleWrite("malformed", "unknownmarker.jpg", "unknownmarker-result.jpg");

        doDrawScaleWrite("quality", "quality_80.jpg", "quality_80-result.jpg");
        doDrawScaleWrite("quality", "quality_90.jpg", "quality_90-result.jpg");
        doDrawScaleWrite("quality", "quality_95.jpg", "quality_95-result.jpg");
        doDrawScaleWrite("quality", "seq_1x1_1x1_1x1.jpg", "seq_1x1_1x1_1x1-result.jpg");
        doDrawScaleWrite("quality", "seq_1x2_1x1_1x1.jpg", "seq_1x2_1x1_1x1-result.jpg");
        doDrawScaleWrite("quality", "seq_2x1_1x1_1x1.jpg", "seq_2x1_1x1_1x1-result.jpg");
        doDrawScaleWrite("quality", "seq_2x2_1x1_1x1.jpg", "seq_2x2_1x1_1x1-result.jpg");

        doDrawScaleWrite("rgb", "adobe_RGB_1998.jpg", "adobe_RGB_1998-result.jpg");
        doDrawScaleWrite("rgb", "Apple_RGB.jpg", "Apple_RGB-result.jpg");
        doDrawScaleWrite("rgb", "CIE_RGB.jpg", "CIE_RGB-result.jpg");
        doDrawScaleWrite("rgb", "ColorMatch_RGB.jpg", "ColorMatch_RGB-result.jpg");
        doDrawScaleWrite("rgb", "e_sRGB.jpg", "e_sRGB-result.jpg");
        doDrawScaleWrite("rgb", "ProPhoto_RGB.jpg", "ProPhoto_RGB-result.jpg");
        doDrawScaleWrite("rgb", "KODAK_DC_Series_Digtal_Camera.jpg",
                "KODAK_DC_Series_Digtal_Camera-result.jpg");
        doDrawScaleWrite("rgb", "PAL-SECAM.jpg", "PAL-SECAM-result.jpg");
        doDrawScaleWrite("rgb", "RICOH_RUSSIAN-SC_040402.jpg", "RICOH_RUSSIAN-SC_040402-result.jpg");
        //        doDrawScaleWrite("ROMM-RGB.jpg", "ROMM-RGB-result.jpg");
        doDrawScaleWrite("rgb", "SMPTE-C.jpg", "SMPTE-C-result.jpg");
        doDrawScaleWrite("rgb", "sRGB_IEC61966-2.1.jpg", "sRGB_IEC61966-2.1-result.jpg");
        doDrawScaleWrite("rgb", "Wide_Gamut_RGB.jpg", "Wide_Gamut_RGB-result.jpg");
    }

    public void doCheckResult(File imgFile) throws Exception {
        BufferedImage img = ImageIO.read(imgFile);
        if (img.getWidth() > 1024 || img.getHeight() > 1024)
            assertTrue(img.toString() + " widht or height is illegal", false);
        if (img.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB) {
            assertTrue(img.toString() + " colorspace is illegal", false);
        }
    }
}
