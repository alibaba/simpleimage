/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.simpleimage.jai.scale;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Map;

import javax.media.jai.GeometricOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

import com.alibaba.simpleimage.util.ImageUtils;

/**
 * 由于存在分块导致缩略后图片有问题，所以暂时放弃利用JAI实现Lanczos的想法，
 * lanczos算法在LanczosScaleOp中实现
 * @author wendell
 */
@Deprecated
@SuppressWarnings("unchecked")
public class LanczosOpImage extends GeometricOpImage {

	private static final double WORK_LOAD_FACTOR = 0.265;

	protected double scaleX;
	protected double scaleY;
	protected double scaleFactor;
	protected int destWidth;
	protected int destHeight;
	protected LanczosResizeFilter filter;
	protected Rectangle filterRect;

	protected int xTrans;
	protected int yTrans;
	
	public LanczosOpImage(RenderedImage source, ImageLayout layout, Map config,
			double scaleX, double scaleY) {
		super(vectorize(source), layoutHelper(source, scaleX, scaleY, layout),
				config, true, // cobbleSources,
				null, // BorderExtender
				new InterpolationLanczos(), null);

		this.scaleX = scaleX;
		this.scaleY = scaleY;

		this.destWidth = (int) (source.getWidth() * scaleX);
		this.destHeight = (int) (source.getHeight() * scaleY);
		if (destWidth <= 0) {
			destWidth = 1;
		}
		if (destHeight <= 0) {
			destHeight = 1;
		}

		this.filter = new LanczosResizeFilter();
		int bandsNum = source.getColorModel().getNumComponents();
		if (bandsNum != 3) {
			throw new IllegalArgumentException("Not supported");
		}

		this.scaleFactor = this.scaleX * this.scaleY;
		if (scaleFactor > WORK_LOAD_FACTOR) {
			filterRect = new Rectangle(destWidth, source.getHeight());
		} else {
			filterRect = new Rectangle(source.getWidth(), destHeight);
		}
	}

	private static ImageLayout layoutHelper(RenderedImage source,
			double scaleX, double scaleY, ImageLayout il) {
		ImageLayout layout = (il == null) ? new ImageLayout()
				: (ImageLayout) il.clone();

		if (scaleX <= 0.0 || scaleY <= 0.0) {
			throw new IllegalArgumentException("Illegal scaleX or scaleY value");
		}

		int dWidth = (int) (source.getWidth() * scaleX);
		if (dWidth <= 0) {
			dWidth = 1;
		}
		int dHeight = (int) (source.getHeight() * scaleY);
		if (dHeight <= 0) {
			dHeight = 1;
		}

		layout.setMinX((int) Math.floor(source.getMinX() * scaleX));
		layout.setMinY((int) Math.floor(source.getMinY() * scaleY));
		layout.setWidth(dWidth);
		layout.setHeight(dHeight);

		return layout;
	}
	
	protected Rectangle backwardMapRect(Rectangle destRect, int sourceIndex) {
		double scale = Math.max(1.0 / scaleX, 1.0);
		double support = scale * filter.getSupport() * filter.getBlur();

		if (support < 0.5) {
			support = 0.5;
		}
		// (int) (start - center) + (int) center
		Rectangle srcRect = getSourceImage(sourceIndex).getBounds();
		
		double center = (destRect.x + 0.5) / scaleX;
		double t1 = center - support + 0.5;
		double t2 = center + support + 0.5;
		int start = (int) (t1 > 0 ? t1 : 0);
		int stop = (int) (t2 < (srcRect.width) ? t2 : srcRect.width);
		int startX = (int)(start - center) + (int)center;
		
//		int startX = (int)center;
		if (startX < 0) {
			startX = 0;
		}
		xTrans = (int)(center) - startX;

		center = (destRect.x + destRect.getWidth() - 1 + 0.5) / scaleX;
		t1 = center - support + 0.5;
		t2 = center + support + 0.5;
		// int endX = (int) (center + support + 0.5);
		start = (int) (t1 > 0 ? t1 : 0);
		stop = (int) (t2 < (srcRect.width) ? t2 : srcRect.width);
		int endX = stop;
//		int endX = (int)center;

		scale = Math.max(1.0 / scaleY, 1.0);
		support = scale * filter.getSupport() * filter.getBlur();

		if (support < 0.5) {
			support = 0.5;
		}
		// int startY = (int) ((destRect.getY() + 0.5) / scaleY - support +
		// 0.5);
		center = (destRect.y + 0.5) / scaleY;
		t1 = center - support + 0.5;
		start = (int) (t1 > 0 ? t1 : 0);
		int startY = (int)(start - center) + (int)center;
//		int startY = (int)center;
		if (startY < 0) {
			startY = 0;
		}
		yTrans = (int)center - startY;
		// int endY = (int) ((destRect.getY() + destRect.getHeight() - 1 + 0.5)
		// / scaleY + support + 0.5);
		center = (destRect.getY() + destRect.getHeight() - 1 + 0.5) / scaleY;
		t1 = center - support + 0.5;
		t2 = center + support + 0.5;
		start = (int) (t1 > 0 ? t1 : 0);
		stop = (int) (t2 < (srcRect.getHeight()) ? t2 : srcRect.getHeight());
		int endY = stop;
//		int endY = (int)center;
		
		Rectangle mapRect = new Rectangle(startX, startY, endX - startX + 1, endY
				- startY + 1);

		return mapRect.intersection(srcRect);
	}

	protected Rectangle forwardMapRect(Rectangle sourceRect, int sourceIndex) {
		return null;
	}

	protected Rectangle mapHorizontalComputeRect(Rectangle srcRect, Rectangle destRect) {
		return new Rectangle(destRect.x, srcRect.y, destRect.width, srcRect.height);
	}
	
	protected Rectangle mapVerticalComputeRect(Rectangle srcRect, Rectangle destRect) {
		return new Rectangle(srcRect.x, destRect.y, srcRect.width, destRect.height);
	}
	
	protected void computeRect(Raster[] sources, WritableRaster dest,
			Rectangle destRect) {
		RasterFormatTag[] formatTags = getFormatTags();
		RasterAccessor dst = new RasterAccessor(dest, destRect, formatTags[1],
				getColorModel());

		Rectangle mapRect = backwardMapRect(destRect, 0);
		Rectangle tmpRect = null;
		if (scaleFactor > WORK_LOAD_FACTOR) {
			tmpRect = mapHorizontalComputeRect(mapRect, destRect).intersection(
					filterRect);
		} else {
			tmpRect = mapVerticalComputeRect(mapRect, destRect).intersection(filterRect);
		}

		SampleModel nsm = dest.getSampleModel().createCompatibleSampleModel(
				tmpRect.width, tmpRect.height);
		WritableRaster filterRaster = Raster.createWritableRaster(nsm,
				tmpRect.getLocation());
		RasterAccessor f = new RasterAccessor(filterRaster, tmpRect,
				formatTags[1], getColorModel());

		RasterAccessor src = new RasterAccessor(sources[0],
				mapRect.intersection(sources[0].getBounds()), formatTags[0],
				getSourceImage(0).getColorModel());

		switch (dst.getDataType()) {
		case DataBuffer.TYPE_BYTE:
			computeRectByte(src, f, dst);
			break;
		case DataBuffer.TYPE_USHORT:
			computeRectUShort(src, f, dst);
			break;
		case DataBuffer.TYPE_SHORT:
			computeRectShort(src, f, dst);
			break;
		case DataBuffer.TYPE_INT:
			computeRectInt(src, f, dst);
			break;
		case DataBuffer.TYPE_FLOAT:
			computeRectFloat(src, f, dst);
			break;
		case DataBuffer.TYPE_DOUBLE:
			computeRectDouble(src, f, dst);
			break;
		default:
			throw new UnsupportedOperationException("Not implemented yet");
		}

		if (dst.isDataCopy()) {
			dst.clampDataArrays();
			dst.copyDataToRaster();
		}
	}

	private void computeRectByte(RasterAccessor src, RasterAccessor mid,
			RasterAccessor dst) {
		if (scaleFactor > WORK_LOAD_FACTOR) {
			lanczosHorizontalFilterByte(src, mid);
			lanczosVerticalFilterByte(mid, dst);
		} else {
			lanczosVerticalFilterByte(src, mid);
			lanczosHorizontalFilterByte(mid, dst);
		}
	}

	private void computeRectUShort(RasterAccessor src, RasterAccessor mid,
			RasterAccessor dst) {
		if ((scaleX * scaleY) > WORK_LOAD_FACTOR) {
			// lanczosHorizontalFilterUShort(src, mid);
			// lanczosVerticalFilterUShort(mid, dst);
		} else {
			// lanczosVerticalFilterUShort(src, mid);
			// lanczosHorizontalFilterUShort(mid, dst);
		}
	}

	private void computeRectShort(RasterAccessor src, RasterAccessor mid,
			RasterAccessor dst) {
		if ((scaleX * scaleY) > WORK_LOAD_FACTOR) {
			// lanczosHorizontalFilterShort(src, mid);
			// lanczosVerticalFilterShort(mid, dst);
		} else {
			// lanczosVerticalFilterShort(src, mid);
			// lanczosHorizontalFilterShort(mid, dst);
		}
	}

	private void computeRectInt(RasterAccessor src, RasterAccessor mid,
			RasterAccessor dst) {
		if ((scaleX * scaleY) > WORK_LOAD_FACTOR) {
			// lanczosHorizontalFilterInt(src, mid);
			// lanczosVerticalFilterInt(mid, dst);
		} else {
			// lanczosVerticalFilterInt(src, mid);
			// lanczosHorizontalFilterInt(mid, dst);
		}
	}

	private void computeRectFloat(RasterAccessor src, RasterAccessor mid,
			RasterAccessor dst) {
		if ((scaleX * scaleY) > WORK_LOAD_FACTOR) {
			// lanczosHorizontalFilterFloat(src, mid);
			// lanczosVerticalFilterFloat(mid, dst);
		} else {
			// lanczosVerticalFilterFloat(src, mid);
			// lanczosHorizontalFilterFloat(mid, dst);
		}
	}

	private void computeRectDouble(RasterAccessor src, RasterAccessor mid,
			RasterAccessor dst) {
		if ((scaleX * scaleY) > WORK_LOAD_FACTOR) {
			// lanczosHorizontalFilterDouble(src, mid);
			// lanczosVerticalFilterDouble(mid, dst);
		} else {
			// lanczosVerticalFilterDouble(src, mid);
			// lanczosHorizontalFilterDouble(mid, dst);
		}
	}

	private void lanczosHorizontalFilterByte(RasterAccessor src,
			RasterAccessor dst) {
		double scale = Math.max(1.0 / scaleX, 1.0);
		double support = scale * filter.getSupport() * filter.getBlur();

		if (support < 0.5) {
			support = 0.5;
			scale = 1.0;
		}

		byte[][] srcDataArrays = src.getByteDataArrays();
		byte[][] dstDataArrays = dst.getByteDataArrays();
		double pixel[] = { 0, 0, 0, 0 };
		final int channel = src.getNumBands();
		scale = 1.0 / scale;

		int contributionsNum = (int) (2.0 * support + 3.0);
		LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionsNum];
		for (int t = 0; t < contributionsNum; t++) {
			contribution[t] = new LanczosContributionInfo();
		}

		double center, t1, t2, density;
		int start, stop, n;
		final int dstWidth = dst.getWidth(), dstHeight = dst.getHeight();
		final int srcWidth = src.getWidth();
		final int dstScanlineStride = dst.getScanlineStride();
		final int srcScanlineStride = src.getScanlineStride();

		for (int x = 0; x < dstWidth; x++) {
			/*
			 * Get the location of the piexls that will be used to compute new
			 * pixel
			 */
			center = (x + 0.5) / scaleX + xTrans;
			t1 = center - support + 0.5;
			t2 = center + support + 0.5;
			start = (int) (t1 > 0 ? t1 : 0);
			stop = (int) (t2 < (srcWidth) ? t2 : srcWidth);
			density = 0.0;

			for (n = 0; n < (stop - start); n++) {
				contribution[n].pixel = start + n;
				contribution[n].weight = getLanczosResizeFilterWeight(scale
						* ((double) (start + n) - center + 0.5));
				density += contribution[n].weight;
			}
			if ((density != 0.0) && (density != 1.0)) {
				density = 1.0 / density;
				for (int i = 0; i < n; i++) {
					contribution[i].weight *= density;
				}
			}
			
			int tmp3 = x * channel;
			//TODO test
			int tmp5 = ((int) (start - center) + (int) center) * channel + xTrans * channel;
//			int tmp5 = start * channel;

			/* Start compute new piexl */
			for (int y = 0; y < dstHeight; y++) {
				pixel[0] = 0.0;
				pixel[1] = 0.0;
				pixel[2] = 0.0;
				pixel[3] = 0.0;

				int tmp6 = tmp5;
				for (int i = 0; i < n; i++) {
					// if(channel == 3) {
					pixel[0] += (srcDataArrays[0][tmp6++] & 0xFF)
							* contribution[i].weight;
					pixel[1] += (srcDataArrays[1][tmp6++] & 0xFF)
							* contribution[i].weight;
					pixel[2] += (srcDataArrays[2][tmp6++] & 0xFF)
							* contribution[i].weight;
					// } else if(channel == 1) {
					// pixel[0] += (srcDataArrays[0][tmp6++] & 0xFF) *
					// contribution[i].weight;
					// } else {
					// pixel[0] += (srcDataArrays[0][tmp6++] & 0xFF) *
					// contribution[i].weight;
					// pixel[1] += (srcDataArrays[1][tmp6++] & 0xFF) *
					// contribution[i].weight;
					// pixel[2] += (srcDataArrays[2][tmp6++] & 0xFF) *
					// contribution[i].weight;
					// pixel[3] += (srcDataArrays[3][tmp6] & 0xFF) *
					// contribution[i].weight;
					// }
				}

				int tmp4 = tmp3;
				// if(channel == 3) {
				dstDataArrays[0][tmp4++] = ImageUtils.clampRoundByte(pixel[0]);
				dstDataArrays[1][tmp4++] = ImageUtils.clampRoundByte(pixel[1]);
				dstDataArrays[2][tmp4] = ImageUtils.clampRoundByte(pixel[2]);
				// } else if(channel == 1) {
				// dstDataArrays[0][tmp3] = ImageUtils.clampRoundByte(pixel[0]);
				// } else {
				// dstDataArrays[0][tmp4++] =
				// ImageUtils.clampRoundByte(pixel[0]);
				// dstDataArrays[1][tmp4++] =
				// ImageUtils.clampRoundByte(pixel[1]);
				// dstDataArrays[2][tmp4++] =
				// ImageUtils.clampRoundByte(pixel[2]);
				// dstDataArrays[3][tmp4] = ImageUtils.clampRoundByte(pixel[3]);
				// }

				tmp3 += dstScanlineStride;
				tmp5 += srcScanlineStride;
			}
		}
	}

	private void lanczosVerticalFilterByte(RasterAccessor src,
			RasterAccessor dst) {
		double scale = Math.max(1.0 / scaleY, 1.0);
		double support = scale * filter.getSupport() * filter.getBlur();

		if (support < 0.5) {
			support = 0.5;
			scale = 1.0;
		}

		/* Get memory of filter array */
		scale = 1.0 / scale;
		byte[][] srcDataArrays = src.getByteDataArrays();
		byte[][] dstDataArrays = dst.getByteDataArrays();
		double pixel[] = { 0, 0, 0, 0 };
		final int channel = dst.getNumBands();

		int contributionNums = (int) (2.0 * support + 3.0);
		LanczosContributionInfo contribution[] = new LanczosContributionInfo[contributionNums];
		for (int t = 0; t < contributionNums; t++) {
			contribution[t] = new LanczosContributionInfo();
		}

		double center, t1, t2, density;
		int start, stop, n;
		final int dstHeight = dst.getHeight(), dstWidth = dst.getWidth();
		final int srcHeight = src.getHeight();
		final int srcScanlineStride = src.getScanlineStride();
		final int dstScanlineStride = dst.getScanlineStride();

		/* First, we compute rows pixel, then compute columns pixel */
		for (int y = 0; y < dstHeight; y++) {
			/*
			 * Get the location of the piexls that will be used to compute new
			 * pixel
			 */
			center = (y + 0.5) / scaleY;
			t1 = center - support + 0.5;
			t2 = center + support + 0.5;
			start = (int) (t1 > 0 ? t1 : 0);
			stop = (int) (t2 < (srcHeight) ? t2 : srcHeight);
			density = 0.0;
			/* Get the value of Sinc */
			for (n = 0; n < (stop - start); n++) {
				contribution[n].pixel = start + n;
				contribution[n].weight = getLanczosResizeFilterWeight(scale
						* ((double) (start + n) - center + 0.5));
				density += contribution[n].weight;
			}
			if ((density != 0.0) && (density != 1.0)) {
				density = 1.0 / density;
				for (int i = 0; i < n; i++) {
					contribution[i].weight *= density;
				}
			}

			int tmp3 = y * dstScanlineStride;
			int tmp5 = ((int) (center)) * srcScanlineStride
					+ ((int) (start - center)) * srcScanlineStride + yTrans * srcScanlineStride;
//			int tmp5 = start * srcScanlineStride;

			/* Start compute new piexl */
			for (int x = 0; x < dstWidth; x++) {
				pixel[0] = 0.0;
				pixel[1] = 0.0;
				pixel[2] = 0.0;
				pixel[3] = 0.0;

				int tmp6 = tmp5;
				for (int i = 0; i < n; i++) {
					int tmp7 = tmp6;
					// if(channel == 3) {
					pixel[0] += (srcDataArrays[0][tmp7++] & 0xFF)
							* contribution[i].weight;
					pixel[1] += (srcDataArrays[1][tmp7++] & 0xFF)
							* contribution[i].weight;
					pixel[2] += (srcDataArrays[2][tmp7] & 0xFF)
							* contribution[i].weight;
					// } else if(channel == 1) {
					// pixel[0] += (srcDataArrays[0][tmp7] & 0xFF) *
					// contribution[i].weight;
					// } else {
					// pixel[0] += (srcDataArrays[0][tmp7++] & 0xFF) *
					// contribution[i].weight;
					// pixel[1] += (srcDataArrays[1][tmp7++] & 0xFF) *
					// contribution[i].weight;
					// pixel[2] += (srcDataArrays[2][tmp7++] & 0xFF) *
					// contribution[i].weight;
					// pixel[3] += (srcDataArrays[3][tmp7] & 0xFF) *
					// contribution[i].weight;
					// }
					tmp6 += srcScanlineStride;
				}

				// if(channel == 3) {
				dstDataArrays[0][tmp3++] = ImageUtils.clampRoundByte(pixel[0]);
				dstDataArrays[1][tmp3++] = ImageUtils.clampRoundByte(pixel[1]);
				dstDataArrays[2][tmp3++] = ImageUtils.clampRoundByte(pixel[2]);
				// } else if(channel == 1) {
				// dstDataArrays[0][tmp3++] =
				// ImageUtils.clampRoundByte(pixel[0]);
				// } else {
				// dstDataArrays[0][tmp3++] =
				// ImageUtils.clampRoundByte(pixel[0]);
				// dstDataArrays[1][tmp3++] =
				// ImageUtils.clampRoundByte(pixel[1]);
				// dstDataArrays[2][tmp3++] =
				// ImageUtils.clampRoundByte(pixel[2]);
				// dstDataArrays[3][tmp3++] =
				// ImageUtils.clampRoundByte(pixel[3]);
				// }

				tmp5 += channel;
			}
		}
	}

	private double getLanczosResizeFilterWeight(final double x) {
		double blur;
		double scale;

		blur = Math.abs(x) / filter.getBlur();
		scale = filter.getScale() / filter.getWindowSupport();
		scale = filter.window(blur * scale);

		return scale * filter.filter(blur);
	}
}
