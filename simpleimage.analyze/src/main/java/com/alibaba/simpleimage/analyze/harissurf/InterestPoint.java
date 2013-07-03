package com.alibaba.simpleimage.analyze.harissurf;


public interface InterestPoint extends java.io.Serializable{
	public double getDistance(InterestPoint point);
	
	public float[] getLocation();
}
