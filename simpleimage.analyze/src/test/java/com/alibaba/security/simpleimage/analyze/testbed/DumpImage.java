package com.alibaba.security.simpleimage.analyze.testbed;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.alibaba.security.simpleimage.analyze.sift.ImageMap;

public class DumpImage {
	public static void dump(ImageMap img, String outFile) {
		int w = img.xDim;
		int h = img.yDim;
		double[][] var = img.valArr;
		System.out.println(w + "," + var.length);
		System.out.println(h + "," + var[0].length);
		BufferedImage target = new BufferedImage(w, h,
				BufferedImage.TYPE_BYTE_GRAY);
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				int c = (int) (img.valArr[i][j] * 255);
				if (c < 0)
					c = -c;
				c = 255 - c;
				c *= 10;
				c %= 255;

				Color cl = new Color(c, c, c);
				target.setRGB(j, i, cl.getRGB());

			}
		}
		try {
			ImageIO.write(target, "JPEG", new File(outFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void dump(ImageMap[] mapArr, String outFile) {
		for (int i = 0; i < mapArr.length; i++)
			dump(mapArr[i], outFile + i + ".jpg");
	}

}
