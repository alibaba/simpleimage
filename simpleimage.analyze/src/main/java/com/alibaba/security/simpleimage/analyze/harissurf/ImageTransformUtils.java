package com.alibaba.security.simpleimage.analyze.harissurf;

import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class ImageTransformUtils {
	public static BufferedImage convertToGrayscale(BufferedImage input){
		BufferedImage output = new BufferedImage(input.getWidth(),input.getHeight(),input.getType());
		ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),null);
		op.filter(input,output);
		return output;
	}
	
	public static float BoxIntegral(IntegralImage img, int row, int col, int rows, int cols){
		int height = img.getHeight();
		int width = img.getWidth();
		
		// The subtraction by one for row/col is because row/col is inclusive.
		int r1 = Math.min(row,height) - 1;
		int c1 = Math.min(col,width)  - 1;
		int r2 = Math.min(row + rows,height) - 1;
		int c2 = Math.min(col + cols,width)  - 1;

		float A = (r1 >= 0 && c1 >= 0) ? img.getValue(c1,r1) : 0;
		float B = (r1 >= 0 && c2 >= 0) ? img.getValue(c2,r1) : 0;
		float C = (r2 >= 0 && c1 >= 0) ? img.getValue(c1,r2) : 0;
		float D = (r2 >= 0 && c2 >= 0) ? img.getValue(c2,r2) : 0;

//		System.out.println("height = " + height + ", width = " + width);
//		System.out.println("c1 = " + c1 + ", c2 = " + c2 + ", r1 = " + r1 + ", r2 = " + r2);
//		System.out.println("A = " + A + ", B = " + B + ", C = " + C + ", D = " + D); 
		
		return Math.max(0F,A - B - C + D);
	}
	
	public static BufferedImage getTransformedImage(BufferedImage image,double scaleX,double scaleY,double shearX,double shearY){
		AffineTransform transform = new AffineTransform();
		if ( scaleX > 0 && scaleY > 0 )
			transform.scale(scaleX, scaleY);
		if ( shearX > 0 && shearY > 0 )
			transform.shear(shearX, shearY);
		
		AffineTransformOp op = new AffineTransformOp(transform,AffineTransformOp.TYPE_BILINEAR);
		BufferedImage dest = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
		op.filter(image, dest);
		return dest;
	}
}
