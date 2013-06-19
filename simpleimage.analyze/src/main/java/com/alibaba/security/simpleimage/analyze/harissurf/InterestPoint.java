package com.alibaba.security.simpleimage.analyze.harissurf;


public interface InterestPoint extends java.io.Serializable{
	public double getDistance(InterestPoint point);
	
	public float[] getLocation();
}
