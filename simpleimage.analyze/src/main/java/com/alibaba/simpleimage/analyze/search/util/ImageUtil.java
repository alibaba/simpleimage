package com.alibaba.simpleimage.analyze.search.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtil {
	public static BufferedImage resizeImage(BufferedImage src, int w, int h)
	{
		int width = src.getWidth();
		int height = src.getHeight();
		float scale_w = (float)(w) / width;
		float scale_h = (float)(h) / height;
		
		if(scale_w > 1 && height > 1 ){
			return src; 
		}
		
		float min_scale = Math.min(scale_w, scale_h);
		int scaledWidth = (int)(min_scale * width);
		int scaledHeight = (int)(min_scale * height);
		//System.out.println(scaledWidth);
		//System.out.println(scaledHeight);
		
		BufferedImage scaledImg = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = scaledImg.createGraphics();
		g.drawImage(src, 0, 0, scaledWidth, scaledHeight, null); 
    	g.dispose();
		return scaledImg;
	}
	
	public static void main(String [] args) throws IOException
	{
		String intput = "D:\\AliDrive\\0.jpg";
		String output = "D:\\AliDrive\\0_scale.jpg";
		
		BufferedImage img = ImageIO.read(new File(intput));
		BufferedImage resizedImg = resizeImage(img, 500, 500);
		ImageIO.write(resizedImg, "jpg", new File(output));
	}
}
