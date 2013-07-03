package com.alibaba.simpleimage.analyze.harissurf;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.Serializable;

public class IntegralImage implements Serializable {
	private static final long serialVersionUID = 1L;

	private float[][] mIntImage;
	private int mWidth = -1;
	private int mHeight = -1;

	public float[][] getValues() {
		return mIntImage;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public float getValue(int column, int row) {
		return mIntImage[column][row];
	}

	public IntegralImage(BufferedImage input) {
		mIntImage = new float[input.getWidth()][input.getHeight()];
		mWidth = mIntImage.length;
		mHeight = mIntImage[0].length;

		int width = input.getWidth();
		int height = input.getHeight();

		WritableRaster raster = input.getRaster();
		int[] pixel = new int[4];
		float sum;
		for (int y = 0; y < height; y++) {
			sum = 0F;
			for (int x = 0; x < width; x++) {
				raster.getPixel(x, y, pixel);
				/**
				 * TODO: FIX LOSS IN PRECISION HERE, DON'T ROUND BEFORE THE
				 * DIVISION (OR AFTER, OR AT ALL) This was done to match the C++
				 * version, can be removed after confident that it's working
				 * correctly.
				 */
				float intensity = Math.round((0.299D * pixel[0] + 0.587D
						* pixel[1] + 0.114D * pixel[2])) / 255F;
				sum += intensity;
				if (y == 0) {
					mIntImage[x][y] = sum;
				} else {
					mIntImage[x][y] = sum + mIntImage[x][y - 1];
				}
			}
		}
	}
}
