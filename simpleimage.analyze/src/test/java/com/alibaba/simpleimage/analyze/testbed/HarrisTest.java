package com.alibaba.simpleimage.analyze.testbed;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.alibaba.simpleimage.analyze.harissurf.IntegralImage;
import com.alibaba.simpleimage.analyze.harris.Corner;
import com.alibaba.simpleimage.analyze.harris.HarrisFast;

public class HarrisTest {

	public static void main(String[] args) throws IOException {

		BufferedImage img = null;
		// BufferedImage img_output = null;
		// FileReader in = null;
		// FileWriter out = null;
		HarrisFast hf = null;
		// String filepath = "D:/aliDrive/test_image/phishing_test/template/";
		String filepath = "D:/AliDrive/test_image/phishing_test/target/";
		// String filename = "alipay_logo1.png";
		String filename = "alipay_2.png";
		// String filename = "icbu_logo1.png";
		int i, j;

		img = ImageIO.read(new File(filepath + filename));

		int width = img.getWidth();
		int height = img.getHeight();
		int[][] input = new int[width][height];

		for (i = 0; i < width - 1; i++) {
			for (j = 0; j < height - 1; j++) {
				input[i][j] = rgb2gray(img.getRGB(i, j));
			}
		}

		double sigma = 1.2;
		double k = 0.06;
		int spacing = 4;

		IntegralImage mIntegralImage = new IntegralImage(img);
		hf = new HarrisFast(input, width, height, mIntegralImage);
		hf.filter(sigma, k, spacing);

		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.GREEN);

		for (Corner corner : hf.corners) {
			g2d.fill(new Rectangle2D.Float(corner.getX() - 1, corner.getY() - 1, 2, 2));
		}

		g2d.dispose();

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(filepath + "out_" + filename);
			ImageIO.write(img, "png", fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static int rgb2gray(int srgb) {
		int r = (srgb >> 16) & 0xFF;
		int g = (srgb >> 8) & 0xFF;
		int b = srgb & 0xFF;
		return (int) (0.299 * r + 0.587 * g + 0.114 * b);
	}

}
