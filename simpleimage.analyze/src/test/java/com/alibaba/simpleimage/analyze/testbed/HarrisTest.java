package com.alibaba.simpleimage.analyze.testbed;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.harissurf.HarrisSurf;
import com.alibaba.simpleimage.analyze.harissurf.SURFInterestPoint;

public class HarrisTest {

    public static void main(String[] args) throws IOException {
        BufferedImage bi = ImageIO.read(new File("/Users/axman/Downloads/logo/img/alipay_logo1.png"));
        HarrisSurf tempalte_hs = new HarrisSurf(bi);
        tempalte_hs.getDescriptions(tempalte_hs.detectInterestPoints(), false);
        List<SURFInterestPoint> al = tempalte_hs.getInterestPoints();

        BufferedImage bi1 = ImageIO.read(new File("/Users/axman/Downloads/logo/img/alipay_logo1.png"));
        long start = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            HarrisSurf tempalte_hs1 = new HarrisSurf(bi1);
            tempalte_hs1.getDescriptions(tempalte_hs.detectInterestPoints(), false);
            List<SURFInterestPoint> al1 = tempalte_hs1.getInterestPoints();
            Map<SURFInterestPoint, SURFInterestPoint> ms = HarrisSurf.match(al, al1);
            HarrisSurf.joinsFilter(ms);
            HarrisSurf.geometricFilter(ms, bi.getWidth(), bi.getHeight());
            //System.out.println(ms.size());
        }
        System.out.println((System.currentTimeMillis() - start) / 100);
    }

    static int rgb2gray(int srgb) {
        int r = (srgb >> 16) & 0xFF;
        int g = (srgb >> 8) & 0xFF;
        int b = srgb & 0xFF;
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

}
